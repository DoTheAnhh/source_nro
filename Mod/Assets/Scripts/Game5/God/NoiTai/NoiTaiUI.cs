using System;
using System.Collections.Generic;

namespace Game5.God
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
    /// <h2>Mọi vùng do <see cref="oBang"/> tính một lần</h2>
    ///
    /// <para>Cả phần vẽ lẫn phần bắt chạm đều đọc từ đó. Bản trước hai bên tự
    /// cộng lấy toạ độ bằng cùng một dãy số gõ tay, và chỉ cần một chỗ lệch vài
    /// điểm là chữ chồng lên nhau còn nút thì bấm trượt.</para>
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
        //  Màu
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
        //  Kích thước
        // ==================================================================

        /// <summary>Cao một dòng trong danh sách.</summary>
        private const int CAO_DONG = 24;

        /// <summary>Số dòng thấy được cùng lúc.</summary>
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

        private string tenDangCo = "";
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
        //  Toạ độ — MỘT nguồn cho cả vẽ lẫn chạm
        // ==================================================================

        /// <summary>
        /// Các vùng của bảng: {x, y, rộng, cao}.
        /// </summary>
        /// <remarks>
        /// Thứ tự: 0 khung, 1 nút X, 2 thẻ "đang mang", 3 hàng tiền,
        /// 4 nút mở bằng vàng, 5 nút mở bằng ngọc, 6 nhãn danh sách,
        /// 7 vùng danh sách, 8 nút Lên, 9 nút Xuống, 10 nút Mở nhanh.
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

        /// <summary>Số dòng thật sự thấy được ở khung hình vừa tính.</summary>
        private int soDongThay = SO_DONG_THAY;

        // ==================================================================
        //  Vẽ
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

            mFont.tahoma_7_grey.drawString(g, "Nội tại của hành tinh bạn",
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
            mFont.tahoma_7b_yellow.drawString(g, "NỘI TẠI",
                    k[0] + k[2] / 2, k[1] + 4, mFont.CENTER);
            veNut(g, nutX, "X", MAU_O, true);
        }

        /// <summary>Thẻ "đang mang": icon, tên, và dòng mô tả tác dụng.</summary>
        /// <remarks>
        /// Tên và mô tả tách làm hai dòng riêng. Bản trước nhét cả chuỗi máy chủ
        /// gửi vào một dòng rồi cắt bớt, nên phần <i>tác dụng</i> — thứ đáng đọc
        /// nhất — luôn là phần bị cắt mất.
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
                mFont.tahoma_7_grey.drawString(g, "Đang tải...",
                        xChu, k[1] + 14, mFont.LEFT);
                return;
            }
            if (tenDangCo.Length == 0)
            {
                mFont.tahoma_7b_yellow.drawString(g, "Chưa có nội tại",
                        xChu, k[1] + 7, mFont.LEFT);
                mFont.tahoma_7_grey.drawString(g, "Mở một cái ở dưới",
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

        /// <summary>Hàng tiền: vàng bên trái, ngọc bên phải, không chồng nhau.</summary>
        private void veHangTien(mGraphics g, int[] k)
        {
            g.setColor(MAU_O_MO, 1f);
            g.fillRect(k[0], k[1], k[2], k[3], 4);
            mFont.tahoma_7b_yellow.drawString(g, "Vàng " + soCham(vangDangCo),
                    k[0] + 6, k[1] + 2, mFont.LEFT);
            mFont.tahoma_7b_green.drawString(g, "Ngọc " + soCham(ngocDangCo),
                    k[0] + k[2] - 6, k[1] + 2, mFont.RIGHT);
        }

        /// <summary>
        /// Hai nút mở, giá nằm <b>trong</b> nút.
        /// </summary>
        /// <remarks>
        /// Bản trước để giá ở một dòng chữ xám ngay <i>dưới</i> nút, và dòng ấy
        /// rơi đúng vào chỗ phần tiếp theo bắt đầu — hai thứ chồng lên nhau. Giá
        /// là một phần của cái nút, nên nó thuộc về bên trong nút.
        /// </remarks>
        private void veHaiNutMo(mGraphics g, int[] kv, int[] kn)
        {
            bool duVang = vangDangCo >= giaVang;
            bool duNgoc = ngocDangCo >= giaNgoc;

            veNutHaiDong(g, kv, "Mở bằng vàng", soCham(giaVang),
                    duVang ? MAU_NUT_VANG : MAU_NUT_TAT);
            veNutHaiDong(g, kn, "Mở bằng ngọc", giaNgoc + " ngọc",
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
                mFont.tahoma_7_grey.drawString(g, "Đang tải...",
                        k[0] + k[2] / 2, k[1] + k[3] / 2 - 6, mFont.CENTER);
                return;
            }
            if (danhSach.Count == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Không có nội tại nào",
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
            veNut(g, len, "Lên", MAU_O, cuon > 0);
            veNut(g, xuong, "Xuống", MAU_O,
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
                        "Chạm một dòng ở trên để dùng \"Mở nhanh\"",
                        k[0] + k[2] / 2, k[1] + (k[3] - 11) / 2, mFont.CENTER);
                return;
            }
            Muc m = danhSach[dongChon];
            veNut(g, k, "MỞ NHANH — " + tenNgan(m.moTa)
                    + " (" + m.chiSoMin + "-" + m.chiSoMax + ")",
                    MAU_NHANH, true);
        }

        // ==================================================================
        //  Vẽ nút
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
        //  Chữ
        // ==================================================================

        /// <summary>Phần tên, tức chữ trước dấu "+" đầu tiên.</summary>
        /// <remarks>
        /// Máy chủ gửi cả câu, ví dụ "Chiêu đấm Galick +5% đến 25% sát thương".
        /// Tách ra để tên và tác dụng nằm hai dòng, thay vì một dòng dài rồi bị
        /// cắt mất đúng phần tác dụng.
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

        /// <summary>Phần tác dụng, tức phần còn lại sau tên.</summary>
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

        /// <summary>Cắt chuỗi cho vừa BỀ RỘNG, không phải vừa số ký tự.</summary>
        /// <remarks>
        /// Đếm ký tự thì chữ hoa và chữ có dấu rộng hơn hẳn chữ thường, nên cùng
        /// một số ký tự có chuỗi vừa khít, có chuỗi tràn ra ngoài khung. Đo bằng
        /// chính phông sắp vẽ thì không bao giờ tràn.
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
            while (n > 1 && f.getWidth(s.Substring(0, n) + "…") > rongToiDa)
            {
                n--;
            }
            return s.Substring(0, n) + "…";
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

        // ==================================================================
        //  Chạm
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
                    + "Mỗi lần bốc tốn " + giaNgoc + " ngọc, đang có " + soCham(ngocDangCo) + "\n"
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
