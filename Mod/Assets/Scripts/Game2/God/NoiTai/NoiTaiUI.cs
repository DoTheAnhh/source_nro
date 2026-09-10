using System;
using System.Collections.Generic;

namespace Game2.God
{
    /// <summary>
    /// Bảng <b>Nội tại</b> — vẽ đè lên màn chơi, tự bắt chạm.
    /// </summary>
    /// <remarks>
    /// <para>Trước đây nội tại chỉ vào được qua một menu bốn nút của NPC, và
    /// muốn xem có những nội tại nào thì phải mở một bảng khác nữa. Bảng này gom
    /// hết vào một chỗ, mở thẳng từ tab Kỹ năng.</para>
    ///
    /// <para><b>Chỉ hiện nội tại của hành tinh mình.</b> Danh sách do máy chủ
    /// lọc sẵn theo <c>player.gender</c> rồi mới gửi xuống, nên không có đường
    /// nào nhìn thấy — hay mở trúng — nội tại của hành tinh khác.</para>
    ///
    /// <para><b>Mở nhanh:</b> chạm một dòng trong danh sách rồi bấm "Mở nhanh",
    /// gõ chỉ số mong muốn, máy chủ sẽ bốc bằng ngọc liên tục cho tới khi ra
    /// đúng nội tại ấy với chỉ số từ mức đã gõ trở lên, hoặc tới khi hết ngọc.
    /// Vòng bốc chạy <b>ở máy chủ</b> chứ không phải client bấm lặp: bấm lặp thì
    /// mỗi lần là một gói tin, và vài trăm gói liên tiếp là rớt kết nối.</para>
    /// </remarks>
    public class NoiTaiUI
    {
        private static NoiTaiUI instance;

        public static NoiTaiUI getInstance()
        {
            return instance ?? (instance = new NoiTaiUI());
        }

        public bool dangMo;

        private const int RONG = 268;
        private const int CAO = 258;

        private const int MAU_NEN = 0x2A2438;
        private const int MAU_VIEN = 0xC8933C;
        private const int MAU_O = 0x453C5A;
        private const int MAU_O_CHON = 0x6A4FA0;
        private const int MAU_VANG = 0xC8933C;
        private const int MAU_NGOC = 0x35A0A0;

        private const int CAO_DONG = 17;
        private const int SO_DONG_THAY = 6;

        private int x0, y0;
        private int cuon;
        private int dongChon = -1;

        /// <summary>Một nội tại trong danh sách của hành tinh này.</summary>
        public sealed class Muc
        {
            public int id;
            public int icon;
            public string moTa;
            public int chiSoMin;
            public int chiSoMax;
        }

        private readonly List<Muc> danhSach = new List<Muc>();

        private string tenDangCo = "Chưa có nội tại";
        private int iconDangCo = -1;
        private long giaVang;
        private int giaNgoc;
        private long vangDangCo;
        private int ngocDangCo;

        /// <summary>Đã nhận dữ liệu từ máy chủ lần nào chưa.</summary>
        private bool coDuLieu;

        // ==================================================================
        //  Mở / đóng
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

        /// <summary>Máy chủ gửi xuống toàn bộ dữ liệu bảng.</summary>
        public void nhanBang(string tenHienTai, int icon, long vang, int ngoc,
                long vangCo, int ngocCo, List<Muc> ds)
        {
            tenDangCo = (tenHienTai == null || tenHienTai.Length == 0)
                    ? "Chưa có nội tại" : tenHienTai;
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

            mFont.tahoma_7b_yellow.drawString(g, "Nội tại",
                    x0 + RONG / 2, y0 + 4, mFont.CENTER);
            veNut(g, x0 + RONG - 18, y0 + 3, 15, 12, "X", MAU_O);

            int y = y0 + 19;

            // ---- Đang có ----
            g.setColor(MAU_O, 0.9f);
            g.fillRect(x0 + 6, y, RONG - 12, 26, 4);
            if (iconDangCo >= 0)
            {
                SmallImage.drawSmallImage(g, iconDangCo, x0 + 20, y + 13, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);
            }
            mFont.tahoma_7_white.drawString(g, catBot(tenDangCo, 36),
                    x0 + 34, y + 3, mFont.LEFT);
            mFont.tahoma_7_grey.drawString(g,
                    "Vàng " + soCham(vangDangCo) + "   Ngọc " + ngocDangCo,
                    x0 + 34, y + 14, mFont.LEFT);
            y += 30;

            // ---- Hai nút mở ----
            int nua = (RONG - 18) / 2;
            bool duVang = vangDangCo >= giaVang;
            bool duNgoc = ngocDangCo >= giaNgoc;
            veNut(g, x0 + 6, y, nua, 17,
                    "Mở bằng vàng", duVang ? MAU_VANG : MAU_O);
            mFont.tahoma_7_grey.drawString(g, soCham(giaVang),
                    x0 + 6 + nua / 2, y + 18, mFont.CENTER);
            veNut(g, x0 + 12 + nua, y, nua, 17,
                    "Mở bằng ngọc", duNgoc ? MAU_NGOC : MAU_O);
            mFont.tahoma_7_grey.drawString(g, giaNgoc + " ngọc",
                    x0 + 12 + nua + nua / 2, y + 18, mFont.CENTER);
            y += 31;

            // ---- Danh sách ----
            mFont.tahoma_7_white.drawString(g,
                    "Tất cả nội tại của hành tinh bạn:", x0 + 6, y, mFont.LEFT);
            y += 12;

            int yDs = y;
            int caoDs = SO_DONG_THAY * CAO_DONG;
            g.setColor(MAU_O, 0.55f);
            g.fillRect(x0 + 6, yDs, RONG - 12, caoDs, 4);

            if (!coDuLieu)
            {
                mFont.tahoma_7_grey.drawString(g, "Đang tải...",
                        x0 + RONG / 2, yDs + caoDs / 2 - 6, mFont.CENTER);
            }
            else if (danhSach.Count == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Không có nội tại nào",
                        x0 + RONG / 2, yDs + caoDs / 2 - 6, mFont.CENTER);
            }
            else
            {
                int het = Math.min(danhSach.Count, cuon + SO_DONG_THAY);
                for (int i = cuon; i < het; i++)
                {
                    Muc m = danhSach[i];
                    int yd = yDs + (i - cuon) * CAO_DONG;
                    if (i == dongChon)
                    {
                        g.setColor(MAU_O_CHON, 1f);
                        g.fillRect(x0 + 7, yd, RONG - 14, CAO_DONG - 1, 3);
                    }
                    if (m.icon >= 0)
                    {
                        SmallImage.drawSmallImage(g, m.icon, x0 + 16, yd + 8, 0,
                                mGraphics.VCENTER | mGraphics.HCENTER);
                    }
                    (i == dongChon ? mFont.tahoma_7b_white : mFont.tahoma_7_white)
                            .drawString(g, catBot(m.moTa, 34), x0 + 28, yd + 3,
                                    mFont.LEFT);
                }
            }
            y = yDs + caoDs + 3;

            // ---- Cuộn ----
            if (danhSach.Count > SO_DONG_THAY)
            {
                veNut(g, x0 + 6, y, 36, 13, "Lên", MAU_O);
                veNut(g, x0 + 46, y, 36, 13, "Xuống", MAU_O);
                mFont.tahoma_7_grey.drawString(g,
                        (cuon + 1) + "-" + Math.min(danhSach.Count, cuon + SO_DONG_THAY)
                        + "/" + danhSach.Count, x0 + 88, y + 2, mFont.LEFT);
            }
            y += 17;

            // ---- Mở nhanh ----
            if (dongChon >= 0 && dongChon < danhSach.Count)
            {
                Muc m = danhSach[dongChon];
                veNut(g, x0 + 6, y, RONG - 12, 17,
                        "Mở nhanh: " + catBot(tenNgan(m.moTa), 22)
                        + " (" + m.chiSoMin + "-" + m.chiSoMax + ")", MAU_O_CHON);
            }
            else
            {
                mFont.tahoma_7_grey.drawString(g,
                        "Chạm một dòng để dùng \"Mở nhanh\"",
                        x0 + RONG / 2, y + 3, mFont.CENTER);
            }
        }

        /// <summary>Phần chữ trước dấu ngoặc — tên gọn của một nội tại.</summary>
        private static string tenNgan(string s)
        {
            if (s == null)
            {
                return "";
            }
            int k = s.IndexOf('[');
            return (k > 0) ? s.Substring(0, k).Trim() : s;
        }

        private static string catBot(string s, int toiDa)
        {
            if (s == null)
            {
                return "";
            }
            return s.Length <= toiDa ? s : (s.Substring(0, toiDa - 1) + "…");
        }

        /// <summary>Số nguyên có dấu chấm ngăn nghìn.</summary>
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

        private static void veNut(mGraphics g, int x, int y, int w, int h,
                string chu, int mau)
        {
            g.setColor(mau, 1f);
            g.fillRect(x, y, w, h, 3);
            g.setColor(MAU_VIEN, 0.7f);
            g.drawRect(x, y, w, h);
            mFont.tahoma_7_white.drawString(g, chu, x + w / 2,
                    y + (h - 11) / 2, mFont.CENTER);
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
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }

            if (cham(x0 + RONG - 18, y0 + 3, 15, 12))
            {
                dong();
                return true;
            }

            int y = y0 + 19;
            y += 30;   // qua ô "đang có"

            int nua = (RONG - 18) / 2;
            if (cham(x0 + 6, y, nua, 17))
            {
                Service.gI().noiTaiMoBangVang();
                return true;
            }
            if (cham(x0 + 12 + nua, y, nua, 17))
            {
                Service.gI().noiTaiMoBangNgoc();
                return true;
            }
            y += 31;
            y += 12;   // qua nhãn danh sách

            int yDs = y;
            int caoDs = SO_DONG_THAY * CAO_DONG;
            int het = Math.min(danhSach.Count, cuon + SO_DONG_THAY);
            for (int i = cuon; i < het; i++)
            {
                int yd = yDs + (i - cuon) * CAO_DONG;
                if (cham(x0 + 6, yd, RONG - 12, CAO_DONG))
                {
                    dongChon = (dongChon == i) ? -1 : i;
                    return true;
                }
            }
            y = yDs + caoDs + 3;

            if (danhSach.Count > SO_DONG_THAY)
            {
                if (cham(x0 + 6, y, 36, 13))
                {
                    cuon = Math.max(0, cuon - 1);
                    return true;
                }
                if (cham(x0 + 46, y, 36, 13))
                {
                    cuon = Math.min(danhSach.Count - SO_DONG_THAY, cuon + 1);
                    return true;
                }
            }
            y += 17;

            if (dongChon >= 0 && dongChon < danhSach.Count
                    && cham(x0 + 6, y, RONG - 12, 17))
            {
                hoiChiSoMongMuon(danhSach[dongChon]);
                return true;
            }
            return true;
        }

        /// <summary>
        /// Hỏi chỉ số mong muốn rồi giao cho máy chủ bốc.
        /// </summary>
        /// <remarks>
        /// Nói rõ khoảng chỉ số của chính nội tại đó ngay trên hộp nhập: gõ một
        /// con số cao hơn trần của nó thì bốc bao nhiêu lần cũng không ra, mà
        /// người chơi không có cách nào biết trần là bao nhiêu nếu không nói.
        /// </remarks>
        private void hoiChiSoMongMuon(Muc m)
        {
            HopNhapChu.getInstance().moRa(
                    "Mở nhanh — " + tenNgan(m.moTa) + "\n"
                    + "Chỉ số nội tại này từ " + m.chiSoMin + " đến " + m.chiSoMax + "\n"
                    + "Mỗi lần bốc tốn " + giaNgoc + " ngọc, đang có " + ngocDangCo + "\n"
                    + "Gõ chỉ số muốn đạt (để trống = chỉ cần đúng nội tại):",
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
