// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game2.God
{
    /// <summary>
    /// Menu <b>Trò chơi</b> — Tài Xỉu, lịch sử của tôi, lịch sử máy chủ.
    /// </summary>
    /// <remarks>
    /// <para><b>Bàn nằm ở máy chủ, không ở đây.</b> Cả máy chủ chung một vòng
    /// đếm ngược, nên màn này chỉ vẽ lại con số máy chủ gửi xuống chứ không tự
    /// quyết. Tự đếm thì mỗi máy lệch một kiểu, mà đây là tiền.</para>
    ///
    /// <para>Đồng hồ vẫn <b>tự trôi giữa hai gói tin</b>: máy chủ gửi mỗi giây
    /// một nhịp, nhưng mạng trễ thì con số đứng hình. Nên giữ mốc thời gian lúc
    /// nhận gói rồi trừ dần cho mượt, và mỗi lần có gói mới thì nắn lại theo máy
    /// chủ — máy chủ luôn là bên nói đúng.</para>
    ///
    /// <h2>Ba pha một ván</h2>
    ///
    /// <para>Đặt cược (30 giây) → xóc bát (10 giây) → hiện kết quả (3 giây) →
    /// ván mới.</para>
    ///
    /// <para>Máy chủ tung xúc xắc ngay ở <b>đầu</b> pha xóc bát và gửi xuống
    /// luôn, nên hiệu ứng kéo bát hé dần là thật: mặt lộ ra dưới vành bát đúng
    /// là mặt cuối cùng. Cửa cược đã đóng trước đó nên biết sớm mười giây cũng
    /// không đặt thêm được gì.</para>
    ///
    /// <h2>Vẽ hiệu ứng bằng gì</h2>
    ///
    /// <para><c>mGraphics</c> không có chuyển sắc, không có xoay, không có bóng
    /// đổ — chỉ có <c>fillRect</c> bo góc và màu có độ trong. Nên:</para>
    /// <list type="bullet">
    ///   <li><b>Chuyển sắc</b> giả bằng vài dải chữ nhật chồng lên nhau, độ
    ///       trong giảm dần.</li>
    ///   <li><b>Quầng sáng</b> là mấy khung bo góc đồng tâm, càng ra ngoài càng
    ///       mờ.</li>
    ///   <li><b>Rung xúc xắc</b> lấy theo <b>đồng hồ máy</b> chứ không theo số
    ///       khung hình: máy khoẻ máy yếu chạy khác tốc độ khung, lấy theo khung
    ///       thì máy yếu rung chậm như phim quay chậm.</li>
    /// </list>
    /// </remarks>
    public class TroChoiUI
    {
        private static TroChoiUI instance;

        public static TroChoiUI getInstance()
        {
            return instance ?? (instance = new TroChoiUI());
        }

        public bool dangMo;

        // ------------------------------------------------------------------
        //  Màu
        // ------------------------------------------------------------------
        private static int rgb(int r, int g, int b)
        {
            return (r << 16) | (g << 8) | b;
        }

        private static readonly int MAU_NEN = rgb(0xFC, 0xE3, 0xC0);
        private static readonly int MAU_TIEU_DE = rgb(0xF0, 0xA1, 0x64);
        private static readonly int MAU_THE = rgb(0xFD, 0xF0, 0xDC);
        private static readonly int MAU_VIEN = rgb(0xA8, 0x6E, 0x3C);
        private static readonly int MAU_CHON = rgb(0xF0, 0xA1, 0x64);

        /// <summary>
        /// Hai cửa, mỗi cửa một cặp màu: đậm làm nền, sáng phủ từ trên xuống.
        /// </summary>
        /// <remarks>
        /// <para>Cặp màu chứ không phải một màu phẳng. Bản trước tô một màu rồi
        /// phủ mấy dải <b>trắng</b> mờ lên cho có khối — mà phủ trắng thì màu nào
        /// cũng bị kéo về phía xám: xanh thành xanh bạc màu, đỏ thành đỏ gạch.
        /// Đó là lý do hai ô nhìn bệt.</para>
        ///
        /// <para>Nay lớp phủ chính là màu sáng của cửa đó, nên kết quả là một dải
        /// chuyển thật giữa hai màu và giữ nguyên độ tươi.</para>
        /// </remarks>
        private static readonly int MAU_TAI = rgb(0x0B, 0x2E, 0x63);
        private static readonly int MAU_TAI_SANG = rgb(0x3D, 0x8F, 0xE0);
        private static readonly int MAU_XIU = rgb(0x5E, 0x0F, 0x17);
        private static readonly int MAU_XIU_SANG = rgb(0xD6, 0x45, 0x3A);
        private static readonly int MAU_VANG = rgb(0xFF, 0xC4, 0x4D);
        private static readonly int MAU_SAN = rgb(0x3B, 0x2A, 0x1B);

        /// <summary>Mã icon thỏi vàng trong kho icon máy chủ.</summary>
        /// <remarks>
        /// Khác mã vật phẩm (457): <c>drawSmallImage</c> tra theo mã icon. Lấy
        /// từ cột <c>icon_id</c> của bảng <c>item_template</c>.
        /// </remarks>
        private const int ICON_THOI_VANG = 4028;

        private const int BO_GOC = 6;

        private static void veKhungBo(mGraphics g, int x, int y, int w, int h,
                int mauNen, float aNen, int mauVien, float aVien, int day)
        {
            g.setColor(mauVien, aVien);
            g.fillRect(x, y, w, h, BO_GOC);
            g.setColor(mauNen, aNen);
            g.fillRect(x + day, y + day, w - day * 2, h - day * 2, BO_GOC - 1);
        }

        /// <summary>
        /// Chuyển sắc dọc giả, sáng trên tối dưới.
        /// </summary>
        /// <remarks>
        /// <c>mGraphics</c> không có chuyển sắc thật. Xếp <paramref name="soDai"/>
        /// dải trắng mờ dần từ trên xuống là mắt đọc ra khối cong, đủ để ô nút
        /// không phẳng lì như một mảng màu.
        /// </remarks>
        private static void veChuyenSac(mGraphics g, int x, int y, int w, int h,
                int soDai)
        {
            int caoDai = h / soDai;
            if (caoDai < 1)
            {
                return;
            }
            for (int i = 0; i < soDai; i++)
            {
                float a = 0.16f * (1f - (float) i / soDai);
                g.setColor(0xFFFFFF, a);
                g.fillRect(x, y + i * caoDai, w, caoDai);
            }
        }

        /// <summary>Trộn hai màu theo tỉ lệ <paramref name="t"/> (0 = a, 1 = b).</summary>
        private static int tronMau(int a, int b, float t)
        {
            int r = (int) (((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
            int g2 = (int) (((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
            int b2 = (int) ((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
            return (r << 16) | (g2 << 8) | b2;
        }

        /// <summary>
        /// Dải chuyển sắc bám theo góc bo của khung.
        /// </summary>
        /// <remarks>
        /// <para>Mỗi dải kéo từ đỉnh của nó <b>xuống tận đáy</b> khung, và vẽ từ
        /// dải cao nhất trước:</para>
        /// <code>
        ///   dải 0  |=================|   sáng nhất, cao hết khung
        ///   dải 1    |===============|
        ///   dải 2      |=============|   tối dần
        /// </code>
        /// <para>Nhờ vậy đáy của <b>mọi</b> dải trùng đáy khung nên hai góc dưới
        /// luôn đúng; còn góc trên bo tròn của mỗi dải thì nằm đè lên dải trước
        /// đó — dải cao hơn, màu liền kề — nên chỗ khuyết ấy hiện ra đúng màu bên
        /// cạnh, mắt không đọc ra được. Dải 0 cao hết khung nên bốn góc của nó
        /// chính là bốn góc khung.</para>
        ///
        /// <para>Bo góc cho từng dải rời cũng không xong: dải ở giữa sẽ lòi góc
        /// tròn của chính nó ra hai bên sườn, thành một viền răng cưa.</para>
        ///
        /// <para>Dùng màu <b>đặc</b> nối nhau thay vì phủ màu trong chồng lớp:
        /// phủ chồng lớp thì mỗi lớp là một bước nhảy độ trong, mắt đọc ra thành
        /// những vạch ngang.</para>
        /// </remarks>
        private static void veDaiMau(mGraphics g, int x, int y, int w, int h,
                int mauTren, int mauDuoi, int soDai)
        {
            // So dai suy tu chieu cao chu khong lay nguyen tham so: moi dai
            // khoang 3px thi buoc mau du nho de mat khong tach duoc ra thanh
            // vach. Dat cung mot con so thi o cao bi soc, o thap thi ve thua.
            if (soDai < 2)
            {
                soDai = 2;
            }
            int theoCao = h / 3;
            if (theoCao > soDai)
            {
                soDai = theoCao;
            }
            if (soDai > 48)
            {
                soDai = 48;
            }
            if (h < soDai)
            {
                g.setColor(mauDuoi, 1f);
                g.fillRect(x, y, w, h, BO_GOC);
                return;
            }
            for (int i = 0; i < soDai; i++)
            {
                int dinh = i * h / soDai;
                g.setColor(tronMau(mauTren, mauDuoi, (float) i / (soDai - 1)), 1f);
                g.fillRect(x, y + dinh, w, h - dinh, BO_GOC);
            }
        }

        /// <summary>
        /// Khung viền kép: vòng vàng ngoài, vòng tối trong, rồi mới đến thân.
        /// </summary>
        /// <remarks>
        /// Hai vòng lồng nhau làm cái ô có bề dày — nhìn ra một tấm biển có khung
        /// thật, thay vì một mảng màu bo góc. Vòng tối ở giữa quan trọng: thiếu
        /// nó thì vàng dính thẳng vào màu thân và cả hai cùng nhạt đi.
        /// </remarks>
        private static void veKhungKep(mGraphics g, int x, int y, int w, int h,
                int mauThan, int mauVien, float aVien)
        {
            g.setColor(mauVien, aVien);
            g.fillRect(x, y, w, h, BO_GOC + 2);
            g.setColor(0x1A1108, 0.85f);
            g.fillRect(x + 2, y + 2, w - 4, h - 4, BO_GOC + 1);
            g.setColor(mauThan, 1f);
            g.fillRect(x + 3, y + 3, w - 6, h - 6, BO_GOC);
        }

        /// <summary>Bốn hoa tiết vàng ở bốn góc trong của khung.</summary>
        private static void veGocKhung(mGraphics g, int x, int y, int w, int h,
                float alpha)
        {
            const int co = 5;
            const int le = 5;
            g.setColor(MAU_VANG, alpha);
            g.fillRect(x + le, y + le, co, co, 2);
            g.fillRect(x + w - le - co, y + le, co, co, 2);
            g.fillRect(x + le, y + h - le - co, co, co, 2);
            g.fillRect(x + w - le - co, y + h - le - co, co, co, 2);
        }

        /// <summary>
        /// Tấm nền chìm có vòng vàng mảnh bao quanh.
        /// </summary>
        /// <remarks>
        /// Vòng vàng là <b>viền</b> chạy hết chu vi, không phải một vạch ngang
        /// vắt qua giữa — nên nó không rơi vào cái lỗi "vạch thừa" đã bỏ trước
        /// đây: viền đi theo đúng hình nên mắt đọc ra là khung.
        /// </remarks>
        private static void veTamChim(mGraphics g, int x, int y, int w, int h,
                float aVien)
        {
            g.setColor(MAU_VANG, aVien);
            g.fillRect(x, y, w, h, 5);
            g.setColor(0x000000, 0.42f);
            g.fillRect(x + 1, y + 1, w - 2, h - 2, 4);
        }

        /// <summary>Quầng sáng quanh một khung: mấy viền đồng tâm mờ dần.</summary>
        private static void veQuang(mGraphics g, int x, int y, int w, int h,
                int mau, float manh, int lop)
        {
            for (int i = lop; i >= 1; i--)
            {
                g.setColor(mau, manh * (0.30f / lop) * (lop - i + 1));
                g.fillRect(x - i, y - i, w + i * 2, h + i * 2, BO_GOC + i);
            }
        }

        /// <summary>
        /// Vệt sáng trượt ngang qua một ô.
        /// </summary>
        /// <remarks>
        /// <para>Ba dải dọc mờ dần hai bên, chạy từ trái sang phải theo đồng hồ
        /// máy. Đây là chỗ đáng lẽ dùng vệt chéo cho đẹp hơn, nhưng vẽ chéo thì
        /// phải xếp hàng chục lát cắt ngang cho <b>mỗi</b> vệt — vài trăm lời
        /// gọi vẽ mỗi khung hình chỉ để lấy cái nghiêng.</para>
        ///
        /// <para>Cắt vùng vẽ theo đúng ô, không thì vệt chạy tràn ra ngoài.</para>
        /// </remarks>
        private static void veVetSang(mGraphics g, int x, int y, int w, int h,
                int chuKy)
        {
            long t = mSystem.currentTimeMillis() % chuKy;
            int xv = x - 30 + (int) ((w + 60L) * t / chuKy);
            g.setClip(x + 2, y + 2, w - 4, h - 4);
            g.setColor(0xFFFFFF, 0.05f);
            g.fillRect(xv - 9, y + 2, 6, h - 4);
            g.setColor(0xFFFFFF, 0.11f);
            g.fillRect(xv - 3, y + 2, 6, h - 4);
            g.setColor(0xFFFFFF, 0.05f);
            g.fillRect(xv + 3, y + 2, 6, h - 4);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        }

        /// <summary>
        /// Viền chạy nháy quanh một ô — các gạch vàng bò vòng quanh.
        /// </summary>
        /// <remarks>
        /// Chỉ vẽ cho ô <b>đang được chọn</b>. Vẽ cho cả hai ô thì vừa tốn vừa
        /// mất tác dụng: cái viền này có nghĩa là "đang chọn cái này".
        /// </remarks>
        private static void veVienChay(mGraphics g, int x, int y, int w, int h,
                int mau)
        {
            const int DAI = 9;
            const int BUOC = 18;
            int chuVi = (w + h) * 2;
            int lech = (int) (mSystem.currentTimeMillis() / 22 % BUOC);
            g.setColor(mau, 0.95f);
            for (int d = lech; d < chuVi; d += BUOC)
            {
                int con = DAI;
                int p = d;
                // Chay quanh chu vi theo chieu kim dong ho, cat khuc o moi goc.
                while (con > 0 && p < chuVi)
                {
                    if (p < w)
                    {
                        int n = Math.min(con, w - p);
                        g.fillRect(x + p, y, n, 2);
                        p += n;
                        con -= n;
                    }
                    else if (p < w + h)
                    {
                        int q = p - w;
                        int n = Math.min(con, h - q);
                        g.fillRect(x + w - 2, y + q, 2, n);
                        p += n;
                        con -= n;
                    }
                    else if (p < w * 2 + h)
                    {
                        int q = p - w - h;
                        int n = Math.min(con, w - q);
                        g.fillRect(x + w - q - n, y + h - 2, n, 2);
                        p += n;
                        con -= n;
                    }
                    else
                    {
                        int q = p - w * 2 - h;
                        int n = Math.min(con, h - q);
                        g.fillRect(x, y + h - q - n, 2, n);
                        p += n;
                        con -= n;
                    }
                }
            }
        }

        /// <summary>
        /// Chữ có bóng nổi: vẽ hai lần, bản tối lệch xuống một pixel.
        /// </summary>
        /// <remarks>
        /// Không có hàm vẽ viền chữ nên đây là cách rẻ nhất để chữ trắng không
        /// bị chìm vào nền màu — một lời gọi vẽ thêm cho mỗi dòng.
        /// </remarks>
        private static void veChuNoi(mGraphics g, mFont mfChinh, string chu,
                int x, int y, int canh)
        {
            mFont.tahoma_7b_dark.drawString(g, chu, x + 1, y + 1, canh);
            mfChinh.drawString(g, chu, x, y, canh);
        }

        /// <summary>Hoa tiết hình thoi nhỏ: hai ô vuông chồng, một cái xoay 45 độ giả.</summary>
        /// <remarks>
        /// Không xoay được hình nên "hình thoi" ở đây là một ô vuông nhỏ bo tròn
        /// gần hết, đặt giữa hai gạch ngang. Ở cỡ vài pixel thì mắt đọc ra là
        /// một hoa tiết, không ai soi nó vuông hay thoi.
        /// </remarks>
        private static void veHoaThi(mGraphics g, int x, int y)
        {
            g.setColor(MAU_VANG, 0.9f);
            g.fillRect(x - 3, y - 3, 6, 6, 3);
            g.setColor(0xFFFFFF, 0.5f);
            g.fillRect(x - 1, y - 1, 2, 2, 1);
            g.setColor(MAU_VANG, 0.45f);
            g.fillRect(x - 9, y - 1, 4, 2, 1);
            g.fillRect(x + 5, y - 1, 4, 2, 1);
        }

        /// <summary>Vòng sáng nở ra rồi tan — dùng lúc báo cửa thắng.</summary>
        private static void veVongNo(mGraphics g, int tamX, int tamY, int coBatDau,
                int coKetThuc, int mau, int chuKy)
        {
            float t = (mSystem.currentTimeMillis() % chuKy) / (float) chuKy;
            int co = coBatDau + (int) ((coKetThuc - coBatDau) * t);
            g.setColor(mau, 0.5f * (1f - t));
            g.fillRect(tamX - co / 2, tamY - co / 2, co, co, co / 2);
        }

        /// <summary>
        /// Mấy hạt sáng bay quanh một điểm.
        /// </summary>
        /// <remarks>
        /// Rải đều theo góc bằng sin/cos rồi cho bán kính nở dần theo thời gian.
        /// Mỗi hạt lệch pha một chút nên chúng không nở thành một vòng phẳng.
        /// </remarks>
        private static void veHatSang(mGraphics g, int tamX, int tamY, int soHat,
                int banKinh, int mau, float manh, int chuKy)
        {
            for (int i = 0; i < soHat; i++)
            {
                float t = ((mSystem.currentTimeMillis() + i * 137L) % chuKy)
                        / (float) chuKy;
                float goc = 6.2832f * i / soHat + t * 1.2f;
                int r = (int) (banKinh * (0.45f + 0.55f * t));
                int hx = tamX + (int) (UnityEngine.Mathf.Cos(goc) * r);
                int hy = tamY + (int) (UnityEngine.Mathf.Sin(goc) * r);
                int co = 3 + (int) (2 * (1f - t));
                g.setColor(mau, manh * (1f - t));
                g.fillRect(hx - co / 2, hy - co / 2, co, co, co / 2);
            }
        }

        // ------------------------------------------------------------------
        //  Thẻ
        // ------------------------------------------------------------------
        /// <summary>Tầng 1: chọn trò chơi.</summary>
        /// <remarks>
        /// <para>Thêm trò mới = thêm một tên vào đây. Nhánh <c>veChuaCo</c> trong
        /// <c>ve()</c> giữ sẵn cho việc đó; hiện chưa với tới được vì mảng chỉ
        /// có một phần tử, nhưng đó là chỗ nối nên để nguyên.</para>
        ///
        /// <para>Tầng 1 dành riêng cho việc chọn TRÒ CHƠI. Trước đây nó là ba mục
        /// của riêng Tài Xỉu nên không có chỗ nào đặt trò thứ hai.</para>
        /// </remarks>
        /// <remarks>
        /// Thứ tự này là thứ tự <b>đã làm xong</b>: Tài Xỉu và Câu Cá chơi được
        /// nên đứng trước, bốn trò còn lại vẫn là "Sắp có" nên đẩy về sau. Người
        /// mở bảng lên gặp ngay hai trò chơi được, không phải cuộn qua bốn tab
        /// trống.
        /// </remarks>
        private static readonly string[] TEN_THE = {
            "Tài Xỉu", "Câu Cá", "Bầu Cua", "Xóc Đĩa",
            "Đua Ngựa", "Đào Vàng", "Cao Thấp"
        };
        private const int THE_TAI_XIU = 0;
        private const int THE_CAU_CA = 1;
        private const int THE_BAU_CUA = 2;
        private const int THE_XOC_DIA = 3;
        private const int THE_DUA_NGUA = 4;
        private const int THE_DAO_VANG = 5;
        private const int THE_CAO_THAP = 6;
        private int the;

        /// <summary>Mã trò gửi lên máy chủ, ứng với từng thẻ tầng 1.</summary>
        /// <remarks>
        /// Tài Xỉu đi đường riêng (mã gói 118) nên chỗ của nó là -1. Sáu trò còn
        /// lại chung mã gói 117, phân biệt bằng con số này.
        /// </remarks>
        private static readonly int[] MA_TRO_CUA_THE = {
            -1,
            God.MiniGame.TRO_CAU_CA,
            God.MiniGame.TRO_BAU_CUA,
            God.MiniGame.TRO_XOC_DIA,
            God.MiniGame.TRO_DUA_NGUA,
            God.MiniGame.TRO_DAO_VANG,
            God.MiniGame.TRO_CAO_THAP
        };

        // ------------------------------------------------------------------
        //  Dải thẻ tầng 1 — CUỘN NGANG
        // ------------------------------------------------------------------

        /// <summary>Bề rộng cố định của một thẻ trò chơi.</summary>
        /// <remarks>
        /// Bảy trò mà chia đều bề ngang bảng thì mỗi thẻ chỉ còn hơn bảy chục
        /// điểm — "Đua Ngựa" vừa khít, nhưng thêm một trò nữa là chữ cụt hết.
        /// Thẻ giữ bề rộng cố định và cả dải trượt ngang, thêm bao nhiêu trò
        /// cũng không phải tính lại.
        /// </remarks>
        private const int RONG_THE_TRO = 92;

        /// <summary>Cạnh một nút mũi tên cuộn dải thẻ.</summary>
        private const int CO_NUT_CUON = 16;

        /// <summary>Dải thẻ đang trượt sang trái bao nhiêu điểm.</summary>
        private int cuonThe;

        /// <summary>Vùng nhìn thấy của dải thẻ, ghi lại lúc vẽ để bắt chạm.</summary>
        private int[] oDaiThe = new int[0];

        /// <summary>Đang giữ để kéo dải thẻ.</summary>
        private bool dangKeoThe;
        private int xBatKeoThe;
        private int cuonLucBatKeoThe;
        private bool daKeoThat;

        /// <summary>Tầng 2: ba mục bên trong Tài Xỉu.</summary>
        private static readonly string[] TEN_THE_2 = {
            "Đặt cược", "Lịch sử của tôi", "Lịch sử máy chủ"
        };
        private const int THE2_DAT_CUOC = 0;
        private const int THE2_LS_TOI = 1;
        private const int THE2_LS_SERVER = 2;
        private int the2;

        // ------------------------------------------------------------------
        //  Trạng thái bàn
        // ------------------------------------------------------------------
        public const sbyte CUA_XIU = 0;
        public const sbyte CUA_TAI = 1;

        private const sbyte PHA_DAT_CUOC = 0;
        private const sbyte PHA_LAC = 1;
        private const sbyte PHA_KET_QUA = 2;

        /// <summary>
        /// Độ dài từng pha, tính bằng giây.
        /// </summary>
        /// <remarks>
        /// Chép theo <c>TaiXiuManager</c> bên máy chủ, và <b>chỉ dùng để vẽ
        /// thanh tiến độ</b>. Lệch thì thanh chạy hơi sai nhịp chứ không hỏng gì
        /// — con số giây thật vẫn là con số máy chủ gửi xuống.
        /// </remarks>
        private static readonly int[] DAI_PHA = { 30, 11, 3 };

        private sbyte pha;
        private int giayConLai;
        private long tongTai;
        private long tongXiu;
        private sbyte cuaCuaToi = -1;
        private long soThoiCuaToi;
        private long thoiVangKhoa;
        private long thoiVangThuong;
        private long phien;
        private readonly int[] xucXac = new int[3];

        /// <summary>
        /// Cửa thắng của ván đang chạy, do <b>máy chủ</b> tính. <c>-1</c> = chưa có.
        /// </summary>
        /// <remarks>
        /// Trước đây màn này tự so <c>tổng >= 11</c> ở bốn chỗ — tức là luật chơi
        /// nằm trong mã client, sửa client là sửa được luật, và đổi ngưỡng bên
        /// máy chủ thì client cũ hiện một đằng máy chủ trả tiền một nẻo. Nay chỉ
        /// đọc con số máy chủ gửi xuống.
        /// </remarks>
        private sbyte ketQuaVan = -1;
        private readonly List<sbyte> lichSuNhanh = new List<sbyte>();

        private long mocNhanNhip;

        /// <summary>Mốc lúc chuyển sang pha kết quả, để chạy hiệu ứng loé.</summary>
        private long mocHienKetQua;

        /// <summary>
        /// Cửa đang chọn để đặt, <c>-1</c> là chưa chọn.
        /// </summary>
        /// <remarks>
        /// Đây là lựa chọn <b>tại máy</b>, chưa gửi đi đâu cả. Chỉ tới lúc bấm
        /// XÁC NHẬN mới có một gói tin rời máy.
        /// </remarks>
        private sbyte cuaChon = -1;

        /// <summary>Số thỏi đang gom cho <see cref="cuaChon"/>, chưa xác nhận.</summary>
        private long tienCho;


        /// <summary>Các mức bấm nhanh — bấm là <b>cộng dồn</b>.</summary>
        private static readonly long[] MUC = { 10, 20, 50, 100 };

        // ------------------------------------------------------------------
        //  Bảng báo thắng thua
        // ------------------------------------------------------------------
        private bool hienBaoKetQua;
        private bool thangVanRoi;
        private long tienVanRoi;
        private long phienVanRoi;

        // ------------------------------------------------------------------
        //  Lịch sử
        // ------------------------------------------------------------------
        public class DongLsToi
        {
            public long phien;
            public sbyte cua;
            public long soThoi;
            public sbyte ketQua;
            public bool thang;
            public long tienThang;
        }

        public class DongLsServer
        {
            public long phien;
            public int x1;
            public int x2;
            public int x3;
            public sbyte ketQua;
            public long tongTai;
            public long tongXiu;
            public int soNguoi;
        }

        private readonly List<DongLsToi> lsToi = new List<DongLsToi>();
        private readonly List<DongLsServer> lsServer = new List<DongLsServer>();
        private int trang;

        /// <summary>Ba cách đọc cùng một bảng số, trong thẻ "Lịch sử máy chủ".</summary>
        private static readonly string[] TEN_THE_CON = { "Bảng", "Biểu đồ", "Cầu" };
        private const int THE_CON_BANG = 0;
        private const int THE_CON_BIEU_DO = 1;
        private const int THE_CON_CAU = 2;
        private int theCon;

        /// <summary>Cầu đang cuộn xuống bao nhiêu hàng.</summary>
        private int cuonCau;

        /// <summary>Cuộn xa nhất của cầu, tính lại mỗi khung hình lúc vẽ.</summary>
        private int cuonCauToiDa;

        /// <summary>Cỡ một ô cầu — bước cuộn dọc bằng đúng một ô.</summary>
        private int buocCuonCau = 20;

        /// <summary>Vùng lưới cầu, để bắt lăn chuột và giữ kéo.</summary>
        private int[] oLuoiCau = new int[0];

        /// <summary>Cột và dòng của từng ván trên cầu, dựng lại mỗi lần vẽ.</summary>
        private int[] cotCau = new int[0];
        private int[] dongCau = new int[0];

        /// <summary>Mũi tên nhỏ, một tam giác xếp bằng mấy vạch dọc.</summary>
        private static void veMuiTenNho(mGraphics g, int[] o, bool trai,
                bool bamDuoc)
        {
            g.setColor(0x000000, 0.35f);
            g.fillRect(o[0] + 1, o[1] + 1, o[2], o[3], 4);
            g.setColor(bamDuoc ? MAU_THE : rgb(0x77, 0x77, 0x77),
                    bamDuoc ? 0.95f : 0.4f);
            g.fillRect(o[0], o[1], o[2], o[3], 4);
            int tamX = o[0] + o[2] / 2;
            int tamY = o[1] + o[3] / 2;
            int huong = trai ? -1 : 1;
            for (int i = 0; i < 4; i++)
            {
                int cao = 7 - i * 2;
                if (cao < 1)
                {
                    cao = 1;
                }
                g.setColor(bamDuoc ? MAU_VIEN : rgb(0x55, 0x55, 0x55), 1f);
                g.fillRect(tamX - huong * 2 + huong * i, tamY - cao / 2, 1, cao);
            }
        }

        /// <summary>Đường xúc xắc nào đang hiện trên biểu đồ.</summary>
        /// <remarks>
        /// Ba đường chồng lên nhau thì rối; tắt bớt là cách duy nhất để dò một
        /// con riêng. Trạng thái giữ tại máy, không gửi đi đâu.
        /// </remarks>
        private readonly bool[] hienXucXac = { true, true, true };

        /// <summary>Màu của ba đường xúc xắc: lục, vàng, đỏ.</summary>
        private static readonly int[] MAU_XUC_XAC = {
            (0x3F << 16) | (0xB9 << 8) | 0x50,
            (0xE8 << 16) | (0xC4 << 8) | 0x2E,
            (0xD4 << 16) | (0x3B << 8) | 0x3B
        };

        // ------------------------------------------------------------------
        //  Vòng đời
        // ------------------------------------------------------------------
        public void moRa()
        {
            the = THE_TAI_XIU;
            the2 = THE2_DAT_CUOC;
            trang = 0;
            hienBaoKetQua = false;
            dangMo = true;
            Service.gI().taiXiuMoBang();
        }

        public void dong()
        {
            if (dangMo)
            {
                // Bao may chu thoi gui nhip: dong bang roi ma van nhan mot goi
                // moi giay la phi duong truyen cua ca hai ben.
                Service.gI().taiXiuDongBang();
            }
            dangMo = false;
        }

        public void nhanTrangThai(sbyte pha, int giay, long tongTai, long tongXiu,
                sbyte cuaCuaToi, long soThoiCuaToi, long khoa, long thuong,
                int x1, int x2, int x3, List<sbyte> lichSu, long phien,
                sbyte ketQua)
        {
            // Sang van moi thi tu dong dep bang bao di.
            //
            // Bang bao nuot het cham (no nam tren cung, khong nuot thi bam
            // xuyen qua trung nut o duoi ma khong nhin thay). Neu de no nam
            // mai thi nguoi choi quen tat se ngoi nhin ca van sau troi qua ma
            // khong dat duoc gi.
            if (pha == PHA_DAT_CUOC)
            {
                hienBaoKetQua = false;
            }
            // Sang van moi, hoac may chu vua nhan cuoc: bo so dang gom di.
            // Giu lai thi van sau mo len da thay san mot con so tu van truoc.
            if (pha != this.pha || cuaCuaToi >= 0)
            {
                huyGom();
            }
            // Van moi thi up bat lai tu dau.
            if (pha == PHA_DAT_CUOC && this.pha != PHA_DAT_CUOC)
            {
                datLaiNan();
            }
            this.pha = pha;
            this.giayConLai = giay;
            this.tongTai = tongTai;
            this.tongXiu = tongXiu;
            this.cuaCuaToi = cuaCuaToi;
            this.soThoiCuaToi = soThoiCuaToi;
            this.thoiVangKhoa = khoa;
            this.thoiVangThuong = thuong;
            this.phien = phien;
            xucXac[0] = x1;
            xucXac[1] = x2;
            xucXac[2] = x3;
            ketQuaVan = ketQua;
            lichSuNhanh.Clear();
            if (lichSu != null)
            {
                lichSuNhanh.AddRange(lichSu);
            }
            mocNhanNhip = mSystem.currentTimeMillis();
        }

        public void nhanNhip(sbyte pha, int giay, long tongTai, long tongXiu)
        {
            this.pha = pha;
            this.giayConLai = giay;
            this.tongTai = tongTai;
            this.tongXiu = tongXiu;
            mocNhanNhip = mSystem.currentTimeMillis();
        }

        public void nhanKetQua(int x1, int x2, int x3, sbyte ketQua, long tienThang,
                long khoa, long thuong)
        {
            xucXac[0] = x1;
            xucXac[1] = x2;
            xucXac[2] = x3;
            ketQuaVan = ketQua;
            thoiVangKhoa = khoa;
            thoiVangThuong = thuong;
            mocHienKetQua = mSystem.currentTimeMillis();
            lichSuNhanh.Insert(0, ketQua);
            while (lichSuNhanh.Count > 20)
            {
                lichSuNhanh.RemoveAt(lichSuNhanh.Count - 1);
            }

            // Bang bao chi hien khi CO dat cuoc van nay. Khong dat ma van bat
            // len mot bang "ban khong thang" thi thanh phien moi ba muoi giay.
            if (cuaCuaToi >= 0)
            {
                hienBaoKetQua = true;
                thangVanRoi = tienThang > 0;
                tienVanRoi = tienThang > 0 ? tienThang : soThoiCuaToi;
                phienVanRoi = phien;
            }

            cuaCuaToi = -1;
            soThoiCuaToi = 0;
        }

        public void nhanLsToi(List<DongLsToi> ds)
        {
            lsToi.Clear();
            if (ds != null)
            {
                lsToi.AddRange(ds);
            }
            trang = 0;
        }

        public void nhanLsServer(List<DongLsServer> ds)
        {
            lsServer.Clear();
            if (ds != null)
            {
                lsServer.AddRange(ds);
            }
            trang = 0;
        }

        private int giayHienThi()
        {
            long troi = (mSystem.currentTimeMillis() - mocNhanNhip) / 1000;
            long con = giayConLai - troi;
            return con < 0 ? 0 : (int) con;
        }

        /// <summary>Đã xác nhận cược ván này thì khoá hết.</summary>
        private bool daDatVanNay()
        {
            return cuaCuaToi >= 0;
        }

        private bool choDatCuoc()
        {
            return pha == PHA_DAT_CUOC && !daDatVanNay();
        }

        /// <summary>Số thỏi đang có, cả khoá lẫn thường.</summary>
        private long tongThoiVang()
        {
            return thoiVangKhoa + thoiVangThuong;
        }

        /// <summary>
        /// Cộng thêm vào số đang gom, chặn ở số thỏi đang có.
        /// </summary>
        /// <remarks>
        /// <para><b>Mọi nút đều cộng dồn</b> — bấm 100 rồi bấm 100 nữa là 200.
        /// Nút ALL cộng một số rất lớn nên sau khi bị chặn nó thành "tất tay";
        /// nhờ vậy không cần một quy tắc riêng cho nó.</para>
        ///
        /// <para>Chặn ngay ở đây chứ không đợi máy chủ từ chối: gom quá số mình
        /// có rồi bấm xác nhận mới bị báo lỗi thì mất toi một ván.</para>
        /// </remarks>
        private void gomThem(long them)
        {
            if (cuaChon < 0 || !choDatCuoc())
            {
                return;
            }
            long tran = tongThoiVang();
            tienCho += them;
            if (tienCho > tran)
            {
                tienCho = tran;
            }
            if (tienCho < 0)
            {
                tienCho = 0;
            }
        }

        /// <summary>Bỏ hết số đang gom.</summary>
        private void huyGom()
        {
            tienCho = 0;
            cuaChon = -1;
        }

        // ------------------------------------------------------------------
        //  Bố cục — cùng cỡ với menu trang bị
        // ------------------------------------------------------------------
        private const int CAO_TIEU_DE = 24;
        private const int CAO_THE = 20;
        private const int LE = 10;

        private int x0;
        private int y0;
        private int rong;
        private int cao;

        private void tinhBoCuc()
        {
            rong = Math.min(GameCanvas.w - 10, 540);
            cao = Math.min(GameCanvas.h - 10, 330);
            if (rong < 300)
            {
                rong = 300;
            }
            if (cao < 190)
            {
                cao = 190;
            }
            x0 = (GameCanvas.w - rong) / 2;
            y0 = (GameCanvas.h - cao) / 2;
        }

        /// <summary>Ô thẻ thứ <paramref name="i"/> trên một dải, ở độ cao <paramref name="y"/>.</summary>
        private int[] oTheTren(int i, int soThe, int y)
        {
            int rongThe = (rong - LE * 2 - 4 * (soThe - 1)) / soThe;
            return new int[] {
                x0 + LE + i * (rongThe + 4), y, rongThe, CAO_THE };
        }

        /// <summary>Bề ngang phần nhìn thấy của dải thẻ.</summary>
        private int rongDaiThe()
        {
            return rong - LE * 2 - (CO_NUT_CUON + 2) * 2;
        }

        /// <summary>Cuộn xa nhất, 0 nghĩa là cả dải vừa chỗ.</summary>
        private int cuonTheToiDa()
        {
            int can = TEN_THE.Length * RONG_THE_TRO;
            int co = can - rongDaiThe();
            return co < 0 ? 0 : co;
        }

        /// <summary>Ô của thẻ trò chơi thứ <paramref name="i"/>.</summary>
        private int[] oThe(int i)
        {
            int xDai = x0 + LE + CO_NUT_CUON + 2;
            return new int[] {
                xDai + i * RONG_THE_TRO - cuonThe,
                y0 + CAO_TIEU_DE + 3,
                RONG_THE_TRO - 4,
                CAO_THE };
        }

        /// <summary>Ô nút mũi tên cuộn dải thẻ.</summary>
        private int[] oNutCuonThe(bool trai)
        {
            return new int[] {
                trai ? (x0 + LE) : (x0 + rong - LE - CO_NUT_CUON),
                y0 + CAO_TIEU_DE + 3 + (CAO_THE - CO_NUT_CUON) / 2,
                CO_NUT_CUON, CO_NUT_CUON };
        }

        /// <summary>Kéo dải thẻ sao cho thẻ đang mở nằm trong tầm nhìn.</summary>
        private void cuonToiTheDangMo()
        {
            int toiDa = cuonTheToiDa();
            if (toiDa <= 0)
            {
                cuonThe = 0;
                return;
            }
            int x = the * RONG_THE_TRO;
            if (x < cuonThe)
            {
                cuonThe = x;
            }
            else if (x + RONG_THE_TRO > cuonThe + rongDaiThe())
            {
                cuonThe = x + RONG_THE_TRO - rongDaiThe();
            }
            gioiHanCuonThe();
        }

        private void gioiHanCuonThe()
        {
            int toiDa = cuonTheToiDa();
            if (cuonThe > toiDa)
            {
                cuonThe = toiDa;
            }
            if (cuonThe < 0)
            {
                cuonThe = 0;
            }
        }

        private int[] oThe2(int i)
        {
            return oTheTren(i, TEN_THE_2.Length,
                    y0 + CAO_TIEU_DE + CAO_THE + 6);
        }

        private int yNoiDung()
        {
            return y0 + CAO_TIEU_DE + CAO_THE * 2 + 11;
        }

        private int caoNoiDung()
        {
            return y0 + cao - LE - yNoiDung();
        }

        /// <summary>Đỉnh của hàng đĩa và hai ô cửa.</summary>
        private int yBan()
        {
            return yNoiDung() + 20;
        }

        /// <summary>Ô cửa Tài (trái) hoặc Xỉu (phải), cao bằng cái đĩa.</summary>
        private int[] oCua(bool tai)
        {
            int rongO = (rong - LE * 2 - coDia() - 30) / 2;
            int x = tai ? x0 + LE : x0 + rong - LE - rongO;
            return new int[] { x, yBan(), rongO, coDia() };
        }

        /// <summary>Bề ngang nút HUỶ — chỉ ba chữ nên không cần rộng.</summary>
        private const int RONG_NUT_HUY = 40;

        /// <summary>Chiều cao hàng nút trong ô cửa.</summary>
        private const int CAO_NUT_CUA = 22;

        /// <summary>
        /// Nút XÁC NHẬN — <b>neo vào đáy ô cửa</b>, nằm cùng hàng với nút HUỶ.
        /// </summary>
        /// <remarks>
        /// Trước đây hai nút xếp thành hai hàng chồng lên nhau. Ô cửa cao bằng
        /// cái đĩa, mà đĩa co lại theo cửa sổ, nên trên màn hình thấp hai hàng
        /// nút ăn hết chỗ và đè lên dòng chữ ngay trên chúng. Gộp một hàng thì
        /// tiết kiệm được 26 điểm chiều cao mà không mất nút nào.
        /// </remarks>
        private int[] oNutDat(bool tai)
        {
            int[] o = oCua(tai);
            return new int[] {
                o[0] + 12, o[1] + o[3] - CAO_NUT_CUA - 5,
                o[2] - 24 - RONG_NUT_HUY - 6, CAO_NUT_CUA };
        }

        private int[] oNutHuy(bool tai)
        {
            int[] o = oCua(tai);
            return new int[] {
                o[0] + o[2] - 12 - RONG_NUT_HUY, o[1] + o[3] - CAO_NUT_CUA - 5,
                RONG_NUT_HUY, CAO_NUT_CUA };
        }

        private int[] oMuc(int i)
        {
            int soNut = MUC.Length + 2;
            int rongNut = (rong - LE * 2 - 4 * (soNut - 1)) / soNut;
            return new int[] {
                x0 + LE + i * (rongNut + 4),
                yBan() + coDia() + 8,
                rongNut, 22 };
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

            g.setColor(0, 0.55f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            veQuang(g, x0, y0, rong, cao, MAU_VANG, 0.5f, 3);
            veKhungBo(g, x0, y0, rong, cao, MAU_NEN, 0.98f, MAU_VIEN, 1f, 2);

            // Thanh tieu de + chuyen sac cho no co khoi.
            g.setColor(MAU_TIEU_DE, 1f);
            g.fillRect(x0 + 2, y0 + 2, rong - 4, CAO_TIEU_DE, BO_GOC);
            veChuyenSac(g, x0 + 2, y0 + 2, rong - 4, CAO_TIEU_DE, 5);
            // Hai nep vang chay het be ngang, ken giua la mot vach mo.
            g.setColor(MAU_VANG, 0.75f);
            g.fillRect(x0 + 8, y0 + CAO_TIEU_DE - 1, rong - 16, 1);
            g.setColor(0x000000, 0.18f);
            g.fillRect(x0 + 8, y0 + CAO_TIEU_DE, rong - 16, 1);

            // Hai hoa tiet hinh thoi hai ben chu.
            veHoaThi(g, x0 + rong / 2 - 46, y0 + 2 + CAO_TIEU_DE / 2);
            veHoaThi(g, x0 + rong / 2 + 46, y0 + 2 + CAO_TIEU_DE / 2);

            mFont.tahoma_7b_white.drawString(g, "TRÒ CHƠI", x0 + rong / 2,
                    y0 + 7, mFont.CENTER);
            veNutDong(g, x0 + rong - 24, y0 + 5);

            veDaiTheTro(g);
            if (the == THE_TAI_XIU)
            {
                for (int i = 0; i < TEN_THE_2.Length; i++)
                {
                    veMotThe(g, oThe2(i), TEN_THE_2[i], i == the2);
                }
            }

            if (the == THE_CAU_CA)
            {
                veCauCa(g);
            }
            else if (the != THE_TAI_XIU)
            {
                veChuaCo(g);
            }
            else if (the2 == THE2_DAT_CUOC)
            {
                veTaiXiu(g);
            }
            else if (the2 == THE2_LS_TOI)
            {
                veLsToi(g);
            }
            else
            {
                veLsServer(g);
            }

            if (hienBaoKetQua)
            {
                veBaoKetQua(g);
            }
        }

        /// <summary>
        /// Dải thẻ chọn trò chơi — <b>cuộn ngang</b>, có hai nút mũi tên.
        /// </summary>
        /// <remarks>
        /// Cắt theo đúng vùng nhìn thấy nên thẻ trượt ra ngoài thì biến mất ở
        /// mép dải chứ không thò ra ngoài khung bảng. Hai nút mũi tên xám đi khi
        /// đã hết đường — không có dấu ấy thì một dải bị cắt trông y hệt một dải
        /// đủ, và mấy trò cuối coi như không tồn tại.
        /// </remarks>
        private void veDaiTheTro(mGraphics g)
        {
            gioiHanCuonThe();
            int xDai = x0 + LE + CO_NUT_CUON + 2;
            int yDai = y0 + CAO_TIEU_DE + 3;
            int wDai = rongDaiThe();
            oDaiThe = new int[] { xDai, yDai, wDai, CAO_THE };

            g.setClip(xDai, yDai - 2, wDai, CAO_THE + 4);
            for (int i = 0; i < TEN_THE.Length; i++)
            {
                int[] o = oThe(i);
                if (o[0] + o[2] <= xDai || o[0] >= xDai + wDai)
                {
                    continue;
                }
                veMotThe(g, o, TEN_THE[i], i == the);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            int toiDa = cuonTheToiDa();
            veNutCuonThe(g, oNutCuonThe(true), true, cuonThe > 0);
            veNutCuonThe(g, oNutCuonThe(false), false, cuonThe < toiDa);
        }

        /// <summary>Một nút mũi tên của dải thẻ.</summary>
        private static void veNutCuonThe(mGraphics g, int[] o, bool trai, bool bamDuoc)
        {
            veKhungBo(g, o[0], o[1], o[2], o[3],
                    bamDuoc ? MAU_THE : rgb(0x88, 0x88, 0x88),
                    bamDuoc ? 0.95f : 0.4f,
                    MAU_VIEN, bamDuoc ? 0.85f : 0.35f, 1);
            if (bamDuoc)
            {
                veChuyenSac(g, o[0] + 1, o[1] + 1, o[2] - 2, o[3] - 2, 4);
            }
            int tamX = o[0] + o[2] / 2;
            int tamY = o[1] + o[3] / 2;
            int huong = trai ? -1 : 1;
            for (int i = 0; i < 4; i++)
            {
                int cao = 9 - i * 2;
                if (cao < 1)
                {
                    cao = 1;
                }
                g.setColor(bamDuoc ? MAU_VIEN : rgb(0x55, 0x55, 0x55), 1f);
                g.fillRect(tamX - huong * 2 + huong * i, tamY - cao / 2, 1, cao);
            }
        }

        /// <summary>
        /// Lăn chuột, bấm mũi tên và giữ kéo để cuộn dải thẻ.
        /// </summary>
        /// <returns><c>true</c> khi đã nuốt cú chạm này.</returns>
        private bool capNhatCuonThe()
        {
            int toiDa = cuonTheToiDa();
            int[] oT = oNutCuonThe(true);
            if (toiDa > 0 && cham(oT[0], oT[1], oT[2], oT[3]))
            {
                cuonThe -= RONG_THE_TRO;
                gioiHanCuonThe();
                return true;
            }
            int[] oP = oNutCuonThe(false);
            if (toiDa > 0 && cham(oP[0], oP[1], oP[2], oP[3]))
            {
                cuonThe += RONG_THE_TRO;
                gioiHanCuonThe();
                return true;
            }
            if (toiDa <= 0 || oDaiThe.Length != 4)
            {
                dangKeoThe = false;
                daKeoThat = false;
                return false;
            }
            if (GameCanvas.pXYScrollMouse != 0
                    && GameCanvas.pxMouse >= oDaiThe[0]
                    && GameCanvas.pxMouse <= oDaiThe[0] + oDaiThe[2]
                    && GameCanvas.pyMouse >= oDaiThe[1]
                    && GameCanvas.pyMouse <= oDaiThe[1] + oDaiThe[3])
            {
                cuonThe += GameCanvas.pXYScrollMouse > 0
                        ? -RONG_THE_TRO : RONG_THE_TRO;
                gioiHanCuonThe();
                return true;
            }
            if (GameCanvas.isPointerDown
                    && GameCanvas.isPointerHoldIn(oDaiThe[0], oDaiThe[1],
                            oDaiThe[2], oDaiThe[3]))
            {
                if (!dangKeoThe)
                {
                    dangKeoThe = true;
                    xBatKeoThe = GameCanvas.px;
                    cuonLucBatKeoThe = cuonThe;
                    daKeoThat = false;
                }
                int lech = xBatKeoThe - GameCanvas.px;
                if (lech > 4 || lech < -4)
                {
                    daKeoThat = true;
                }
                cuonThe = cuonLucBatKeoThe + lech;
                gioiHanCuonThe();
                return false;
            }
            if (dangKeoThe)
            {
                // Nha ngon: pointerReleased dat isPointerDown = false va
                // isPointerJustRelease = true CUNG MOT LUC, nen khung hinh nay
                // roi vao day chu khong vao nhanh keo o tren.
                dangKeoThe = false;
                if (daKeoThat)
                {
                    // Chi nuot cu nha khi da keo THAT. Nuot ca nhung lan cham
                    // khong nhuc nhich thi bam chon the se khong bao gio an.
                    daKeoThat = false;
                    GameCanvas.clearAllPointerEvent();
                    return true;
                }
            }
            return false;
        }

        /// <summary>
        /// Đổi sang một trò khác.
        /// </summary>
        /// <remarks>
        /// Báo cho máy chủ biết đang đóng bảng trò cũ và mở bảng trò mới: máy chủ
        /// chỉ gửi nhịp mỗi giây cho người đang mở, nên không báo thì hoặc là
        /// nhận nhịp của trò không còn nhìn, hoặc là mở trò mới mà đồng hồ đứng
        /// im tới tận ván sau.
        /// </remarks>
        private void doiTro(int i)
        {
            if (i == the)
            {
                return;
            }
            dongBangTro(the);
            the = i;
            the2 = THE2_DAT_CUOC;
            trang = 0;
            cuonToiTheDangMo();
            moBangTro(the);
        }

        private void moBangTro(int t)
        {
            if (t == THE_TAI_XIU)
            {
                Service.gI().taiXiuMoBang();
                return;
            }
            if (t >= 0 && t < MA_TRO_CUA_THE.Length && MA_TRO_CUA_THE[t] >= 0)
            {
                God.MiniGame.gI().moBang((sbyte) MA_TRO_CUA_THE[t]);
            }
        }

        private void dongBangTro(int t)
        {
            if (t == THE_TAI_XIU)
            {
                Service.gI().taiXiuDongBang();
                return;
            }
            if (t >= 0 && t < MA_TRO_CUA_THE.Length && MA_TRO_CUA_THE[t] >= 0)
            {
                God.MiniGame.gI().dongBang((sbyte) MA_TRO_CUA_THE[t]);
            }
        }

        // ==================================================================
        //  Thẻ Câu Cá
        // ==================================================================

        /// <summary>Tên năm loài, cùng thứ tự với <c>CauCaManager.TEN_CA</c>.</summary>
        /// <remarks>
        /// <b>Năm</b> loài, đúng bằng số tệp trong <c>data/icon/Ca</c>. Ba chỗ
        /// phải cùng một thứ tự: mảng này, <c>TEN_CA</c> bên máy chủ, và dải
        /// sprite <c>cc_ca</c>. Lệch một chỗ là tên một con mà hình một con.
        /// </remarks>
        private static readonly string[] TEN_CA = {
            "Cá ngựa con", "Cá thu", "Cá cờ", "Cá mập", "Cá vàng"
        };

        /// <summary>Số thỏi từng loài — chỉ để bày bảng, máy chủ mới là bên trả.</summary>
        private static readonly int[] THUONG_CA = { 10, 20, 50, 100, 200 };

        // ------------------------------------------------------------------
        //  Bảng màu Câu Cá
        // ------------------------------------------------------------------

        /// <summary>
        /// Màu khung cảnh, dựng quanh <b>một gốc lam ngọc</b>.
        /// </summary>
        /// <remarks>
        /// Cả cảnh chỉ đi trong hai họ màu: lam ngọc cho trời nước, hổ phách cho
        /// gỗ và thỏi vàng. Hai họ nằm đối nhau trên vòng màu nên nền lạnh tự
        /// đẩy mọi thứ ấm ra trước, không cần viền đậm. Bộ sprite Cần Câu cũng
        /// nằm trong hai họ ấy nên nó ăn vào cảnh mà không phải chỉnh màu.
        /// </remarks>
        private static readonly int MAU_TROI_CAO = rgb(0x2E, 0x7A, 0xA8);
        private static readonly int MAU_TROI_THAP = rgb(0xA8, 0xE2, 0xEE);
        private static readonly int MAU_NUOC_MAT = rgb(0x1F, 0x8E, 0x9A);
        private static readonly int MAU_NUOC_SAU = rgb(0x06, 0x2C, 0x3E);
        private static readonly int MAU_GO = rgb(0x7A, 0x4B, 0x28);
        private static readonly int MAU_GO_SANG = rgb(0xB0, 0x7A, 0x40);
        private static readonly int MAU_GO_TOI = rgb(0x46, 0x29, 0x12);
        private static readonly int MAU_BOT = rgb(0xDE, 0xF7, 0xF7);

        /// <summary>Màu đại diện từng loài, lấy từ sắc trội của sprite nó.</summary>
        /// <remarks>
        /// Không dùng để vẽ con cá — con cá là sprite. Màu này chỉ cho viền ô
        /// trong bảng và quầng sáng, để mỗi ô đọc ra ngay là loài nào.
        /// </remarks>
        private static readonly int[] MAU_CA = {
            rgb(0xF2, 0xA8, 0x2E), rgb(0x5A, 0x8F, 0xD0), rgb(0x3E, 0x6E, 0xC8),
            rgb(0x5A, 0x7A, 0xA8), rgb(0xFF, 0x9E, 0x2A)
        };

        private static readonly int MAU_VUNG_XAU = rgb(0x8A, 0x4B, 0x3C);
        private static readonly int MAU_VUNG_TOT = rgb(0x4F, 0xA8, 0x5C);
        private static readonly int MAU_VUNG_SANG = rgb(0x8F, 0xE8, 0x6B);

        // ------------------------------------------------------------------
        //  Bộ sprite
        // ------------------------------------------------------------------

        /// <summary>
        /// Bảy dải sprite, nạp một lần rồi giữ.
        /// </summary>
        /// <remarks>
        /// <para>Nguồn: bốn bảng xếp <b>ngang</b> trong
        /// <c>Server/data/icon/CanCau</c> (nền tối đặc) và năm tệp trong
        /// <c>Server/data/icon/Ca</c> (nền đã trong suốt). Một bước xử lý ngoài
        /// game <b>đo khung bao sát từng con</b> rồi thu về đúng cỡ sẽ vẽ và xếp
        /// lại theo chiều <b>dọc</b> — vì <c>FrameImage</c> suy số khung bằng
        /// <c>img.getHeight() / frameHeight</c> và cắt bằng
        /// <c>idx * frameHeight</c>.</para>
        ///
        /// <para><b>Số truyền vào đây phải khớp đúng cỡ khung đã xuất.</b> Lệch
        /// một điểm là <c>idx * frameHeight</c> rơi vào giữa khung, và mỗi ô vẽ
        /// ra hai ba con xếp lên nhau — đúng lỗi vừa gặp.</para>
        ///
        /// <para>Thu nhỏ sẵn chứ không thu lúc vẽ: <c>mGraphics</c> không có hàm
        /// vẽ theo tỉ lệ dùng được (<c>drawImageScale</c> đặt
        /// <c>GUI.color = red</c>, một lỗi còn sót).</para>
        ///
        /// <para>Nạp lười ở lần vẽ đầu: bảng Trò Chơi có thể không bao giờ được
        /// mở, mà bảy dải ảnh nằm không thì tốn bộ nhớ vô ích.</para>
        /// </remarks>
        private static FrameImage fiCan;
        private static FrameImage fiPhao;
        private static FrameImage fiToe;
        private static FrameImage fiKeo;
        private static FrameImage fiNhay;
        private static FrameImage fiCa;
        private static FrameImage fiCaBe;

        /// <summary>
        /// Số <b>ô</b> trong mỗi dải, và số <b>khung thật</b> được dùng.
        /// </summary>
        /// <remarks>
        /// <para>Hai con số khác nhau, và phải khác nhau. Ảnh xuất ở cỡ
        /// <b>lũy thừa hai</b> cả hai chiều, vì Unity nhập ảnh với
        /// <c>nPOTScale = ToNearest</c>: ảnh không phải lũy thừa hai bị
        /// <b>co về lũy thừa hai gần nhất</b>. Một dải 40×130 thành 32×128, và
        /// chiều cao khung suy ra là 128/5 = 25 trong khi nội dung nằm ở bước 26
        /// — lệch dần từng khung, mỗi ô vẽ ra một phần khung này cộng một mảnh
        /// khung kia. Đó là <b>gốc của mọi lỗi cắt khung</b> đã gặp.</para>
        ///
        /// <para>Chiều cao lũy thừa hai thì không chia chẵn cho 5 hay 6 hay 12.
        /// Nên dải có nhiều ô hơn số khung thật, phần dư để trống: 8 ô cho 5 con
        /// cá, 16 ô cho 12 khung cột nước. Đổi lại là chiều cao ô luôn chia
        /// chẵn.</para>
        ///
        /// <para><b>Cắt</b> thì dùng số ô; <b>chạy hoạt hình</b> thì dùng số
        /// khung thật — dùng số ô để chạy hoạt hình sẽ nhảy vào mấy ô trống.</para>
        /// </remarks>
        private const int SO_O_CAN = 8;
        private const int SO_O_PHAO = 8;
        private const int SO_O_TOE = 16;
        private const int SO_O_KEO = 8;
        private const int SO_O_NHAY = 8;
        private const int SO_O_CA = 8;

        private const int SO_KHUNG_CAN = 4;
        private const int SO_KHUNG_PHAO = 6;
        private const int SO_KHUNG_TOE = 12;
        private const int SO_KHUNG_KEO = 8;
        private const int SO_KHUNG_NHAY = 8;

        private static bool daThuNapSprite;

        private static void napSprite()
        {
            if (daThuNapSprite)
            {
                return;
            }
            daThuNapSprite = true;
            try
            {
                // Khai SO O de cat, khong phai so khung that.
                fiCan = napDai("cc_can", SO_O_CAN);
                fiPhao = napDai("cc_phao", SO_O_PHAO);
                fiToe = napDai("cc_toe", SO_O_TOE);
                fiKeo = napDai("cc_keo", SO_O_KEO);
                fiNhay = napDai("cc_nhay", SO_O_NHAY);
                fiCa = napDai("cc_ca", SO_O_CA);
                fiCaBe = napDai("cc_cabe", SO_O_CA);
            }
            catch (System.Exception)
            {
                fiCan = null;
                fiPhao = null;
                fiToe = null;
                fiKeo = null;
                fiNhay = null;
                fiCa = null;
                fiCaBe = null;
            }
        }

        /// <summary>
        /// Nạp một dải sprite, <b>suy cỡ khung từ chính ảnh</b>.
        /// </summary>
        /// <remarks>
        /// <para>Chỉ cần biết <b>số khung</b>. Bề rộng khung bằng đúng bề rộng
        /// ảnh — dải xếp dọc nên nó rộng đúng một khung — còn chiều cao khung là
        /// chiều cao ảnh chia số khung.</para>
        ///
        /// <para>Bản trước khai cả hai con số bằng tay, và chúng <b>phải khớp
        /// đúng cỡ đã xuất</b>. Xuất lại ở cỡ khác một cái là hỏng theo hai
        /// đường cùng lúc: <c>idx × frameHeight</c> lệch dần từng khung, và
        /// <c>frameWidth</c> lớn hơn bề rộng ảnh làm phép đọc tràn ra ngoài
        /// texture — sinh ra một vệt xám cạnh mỗi hình. Suy từ ảnh thì đổi cỡ
        /// bao nhiêu lần cũng không phải sửa mã.</para>
        ///
        /// <para>Số khung thì <b>không</b> suy được: nó là thuộc tính của hoạt
        /// hình, không nằm trong tệp ảnh. Nhưng nó cũng là con số không đổi khi
        /// thu nhỏ hay cắt lại, nên khai bằng tay là an toàn.</para>
        /// </remarks>
        private static FrameImage napDai(string ten, int soKhung)
        {
            Image img = Image.createImage(ten);
            if (img == null || soKhung < 1)
            {
                return null;
            }
            int caoKhung = img.getHeight() / soKhung;
            if (caoKhung < 1)
            {
                caoKhung = img.getHeight();
            }
            return new FrameImage(img, img.getWidth(), caoKhung);
        }

        /// <summary>Vẽ một khung, bỏ qua trong im lặng nếu thiếu ảnh.</summary>
        private static void veKhung(mGraphics g, FrameImage fi, int idx, int x,
                int y, int trans)
        {
            veKhungNeo(g, fi, idx, x, y, trans,
                    mGraphics.VCENTER | mGraphics.HCENTER);
        }

        /// <summary>Vẽ một khung với điểm neo tự chọn.</summary>
        /// <remarks>
        /// Cần câu neo <b>góc dưới-trái</b> chứ không neo giữa: trong ô sprite
        /// nó cũng được xếp sát góc ấy (tay cầm nằm đó ở mọi khung). Neo giữa thì
        /// tâm ô lệch khỏi tay cầm, và cái cần trông vừa nhỏ vừa đặt sai chỗ.
        /// </remarks>
        private static void veKhungNeo(mGraphics g, FrameImage fi, int idx, int x,
                int y, int trans, int neo)
        {
            if (fi == null || fi.imgFrame == null)
            {
                return;
            }
            if (idx < 0)
            {
                idx = 0;
            }
            if (idx >= fi.nFrame)
            {
                idx = fi.nFrame - 1;
            }
            fi.drawFrame(idx, x, y, trans, neo, g);
        }

        /// <summary>
        /// Chữ <b>không viền</b>, cho riêng màn Câu Cá.
        /// </summary>
        /// <remarks>
        /// <para><c>veChuNoi</c> vẽ hai lớp: một lớp <c>tahoma_7b_dark</c> lệch
        /// một điểm làm viền, rồi lớp chữ chính. Trên khung cảnh này cái viền ấy
        /// đọc ra thành một khối đen sau mỗi dòng — và tệ nhất là ở chữ "0",
        /// nơi lớp chính <i>cũng</i> là <c>tahoma_7b_dark</c>: hai lớp đen chồng
        /// nhau thành một vệt đặc.</para>
        ///
        /// <para>Nên màn này vẽ chữ một lớp. Đổi lại thì màu chữ phải tự đủ
        /// tương phản với chỗ nó nằm: chữ sáng trên nước, chữ nâu đậm trên ô kem.</para>
        /// </remarks>
        private static void veChuPhang(mGraphics g, mFont f, string chu, int x,
                int y, int canh)
        {
            f.drawString(g, chu, x, y, canh);
        }

        /// <summary>
        /// Một con cá.
        /// </summary>
        /// <remarks>
        /// <para>Sprite gốc hướng sang <b>trái</b> — cả năm tệp đều vậy. Nên con
        /// bơi sang <b>phải</b> mới cần lật gương (<c>transform = 2</c>); bản
        /// trước tôi làm ngược nên con nào cũng bơi giật lùi.</para>
        ///
        /// <para>Quầng vẽ trước sprite nên nó nằm dưới, không làm nhoè con cá.</para>
        /// </remarks>
        private static void veCaSprite(mGraphics g, FrameImage fi, int loai,
                int cx, int cy, bool sangPhai, bool sang)
        {
            if (loai < 0 || loai >= TEN_CA.Length)
            {
                return;
            }
            if (sang && loai < MAU_CA.Length)
            {
                veTron(g, cx, cy, 38, MAU_CA[loai], 0.16f);
                veTron(g, cx, cy, 24, MAU_CA[loai], 0.24f);
            }
            veKhung(g, fi, loai, cx, cy, sangPhai ? 2 : 0);
        }

        // ------------------------------------------------------------------
        //  Các pha một lượt câu
        // ------------------------------------------------------------------

        /// <summary>Chưa quăng.</summary>
        private const int CC_RANH = 0;
        /// <summary>Dây đang bay ra.</summary>
        private const int CC_BAY = 1;
        /// <summary>Phao nổi, chờ cá cắn.</summary>
        private const int CC_CHO_CAN = 2;
        /// <summary>Cá vừa cắn — cửa sổ để kịp bấm KÉO.</summary>
        private const int CC_CA_CAN = 3;
        /// <summary>Đang vật lộn kéo cá.</summary>
        private const int CC_VAT_LON = 4;
        /// <summary>Đang hiện kết quả.</summary>
        private const int CC_KET_QUA = 5;

        private int ccPha = CC_RANH;
        private long ccMocPha;

        /// <summary>Thanh tiến trình, 0 tới 100. Số thực để nhích mượt.</summary>
        private float ccTienTrinh;

        private long ccMocKhungTruoc;

        /// <summary>Vạch tiến của người chơi trên thanh vật lộn, 0 tới 1.</summary>
        private float ccViTriVach = 0.5f;

        /// <summary>Tâm vùng xanh — con cá — trên thanh vật lộn, 0 tới 1.</summary>
        private float ccTamVung = 0.5f;

        /// <summary>Tốc độ vạch tiến đi lên khi giữ, và tụt về khi thả.</summary>
        /// <remarks>
        /// <para>Lên nhanh hơn tụt một chút: giữ thì thấy đáp lại ngay, thả thì
        /// có khoảng lặng để chỉnh.</para>
        ///
        /// <para>Cả thanh đi hết chừng <b>một giây rưỡi</b>. Bản trước là bảy
        /// phần mười giây — nhanh quá, vạch vọt qua cái khe xanh trước khi mắt
        /// kịp thấy, nên căn được là do may chứ không do tay.</para>
        /// </remarks>
        private const float CC_TOC_VACH_LEN = 0.65f;
        private const float CC_TOC_VACH_XUONG = 0.50f;
        private bool ccDangGiu;
        private bool ccVachTrongVung;
        private bool ccDaBao;
        private bool ccBatDuoc;
        private long ccMocVatLon;
        private bool ccDangDeNut;

        /// <summary>Mốc các pha, tính bằng mili giây.</summary>
        private const int CC_DAI_BAY = 620;

        /// <summary>
        /// Cửa sổ phản xạ: cá cắn rồi mà không bấm KÉO trong quãng này là mất.
        /// </summary>
        /// <remarks>
        /// Một giây bốn: đủ để người đang nhìn kịp bấm, đủ ngắn để phải nhìn
        /// thật. Dài hơn thì bấm lúc nào cũng kịp và cái chờ mất hết ý nghĩa.
        /// </remarks>
        private const int CC_DAI_CA_CAN = 1400;

        private const int CC_DAI_KET_QUA = 2600;

        private long ccTrongPha()
        {
            return ccMocPha <= 0 ? 0 : (mSystem.currentTimeMillis() - ccMocPha);
        }

        private void ccVaoPha(int pha)
        {
            ccPha = pha;
            ccMocPha = mSystem.currentTimeMillis();
        }

        /// <summary>
        /// Làm mượt hai đầu: chậm khi khởi động, chậm khi về đích.
        /// </summary>
        /// <remarks>
        /// Nội suy thẳng thì vật bắt đầu và dừng đột ngột — mắt đọc ra là một con
        /// số đang chạy chứ không phải một vật đang di chuyển. Công thức
        /// <c>t·t·(3 − 2t)</c> có đạo hàm bằng không ở cả hai đầu, tức vận tốc
        /// khởi từ 0 và về 0, mà chỉ tốn hai phép nhân.
        /// </remarks>
        private static float lamMuot(float t)
        {
            if (t <= 0f)
            {
                return 0f;
            }
            if (t >= 1f)
            {
                return 1f;
            }
            return t * t * (3f - 2f * t);
        }

        /// <summary>
        /// Có đang giữ hay không — nút <b>hoặc</b> phím Space.
        /// </summary>
        /// <remarks>
        /// <para>Space cho bản máy tính: giữ chuột suốt cả pha vật lộn thì tay
        /// che mất thanh, mà bàn phím thì rảnh.</para>
        ///
        /// <para>Đọc thẳng <c>Input.GetKey</c> chứ không qua bảng phím của game:
        /// bảng phím báo <i>lúc bấm</i> và <i>lúc nhả</i>, còn ở đây cần biết
        /// <b>đang giữ hay không</b> ở từng khung hình — đó đúng là việc của
        /// <c>GetKey</c>.</para>
        ///
        /// <para>Chặn khi đang gõ chữ: gõ dấu cách trong khung chat mà cần câu
        /// giật lên thì không ai chat nổi.</para>
        /// </remarks>
        private bool ccGiuSpace()
        {
            try
            {
                if (God.HopNhapChu.getInstance().dangMo
                        || God.BanPhimSo.getInstance().dangMo)
                {
                    return false;
                }
                // KHONG doc TField.currentTField.isFocus o day.
                //
                // LoginScr dat tfUser.isFocus = true tren ban may tinh va khong
                // ai xoa co do sau khi vao game — nen doc no thi phim Space
                // KHONG BAO GIO an. Day dung la loi da gap voi W A S D.
                //
                // Ba hop nhap that o tren da du: khung chat, hop nhap chu, ban
                // phim so. Cai nao mo thi chac chan dang go.
                return UnityEngine.Input.GetKey(UnityEngine.KeyCode.Space);
            }
            catch (System.Exception)
            {
                return false;
            }
        }

        /// <summary>
        /// Đang giữ hay không — <b>đọc thẳng từ Unity</b>, không qua cờ của game.
        /// </summary>
        /// <remarks>
        /// <para><c>GameCanvas.isPointerDown</c> là một <i>cờ chốt</i> do lớp
        /// mạng đặt và nhiều nơi khác xoá. Bản trước dựa vào nó để nhận biết
        /// "đang giữ ngón" và cú bấm <b>chưa bao giờ ăn</b>. Đọc thẳng
        /// <c>Input.GetMouseButton</c> thì không ai xoá được, và nó cũng đúng cho
        /// cảm ứng — Unity gán cú chạm đầu tiên vào nút chuột 0.</para>
        ///
        /// <para>Toạ độ chuột đổi về hệ của bảng đúng cách <c>Main.checkInput</c>
        /// làm: chia cho <c>zoomLevel</c>, đảo trục dọc, cộng phần đẩy lên khi
        /// bàn phím mở. Không đổi thì vùng bấm lệch hẳn ở mức phóng khác 1.</para>
        /// </remarks>
        /// <summary>
        /// Đang giữ hay không, <b>không xét vị trí</b> — cho pha kéo cá.
        /// </summary>
        /// <remarks>
        /// Hợp ba tín hiệu độc lập về cơ chế: đọc thẳng Unity, cờ chốt của
        /// engine, và phím Space. Chỉ cần một cái chạy là kéo được. Ba đường
        /// khác hẳn nhau nên khó mà cùng hỏng.
        /// </remarks>
        private bool ccGiuBatKyDau()
        {
            try
            {
                if (ccGiuSpace())
                {
                    return true;
                }
                if (UnityEngine.Input.GetMouseButton(0))
                {
                    return true;
                }
                return GameCanvas.isPointerDown;
            }
            catch (System.Exception)
            {
                return GameCanvas.isPointerDown;
            }
        }

        private bool ccGiuRaw(int[] o)
        {
            try
            {
                if (ccGiuSpace())
                {
                    return true;
                }
                if (!UnityEngine.Input.GetMouseButton(0))
                {
                    return false;
                }
                UnityEngine.Vector3 v = UnityEngine.Input.mousePosition;
                int px = (int) (v.x / mGraphics.zoomLevel);
                int py = (int) ((UnityEngine.Screen.height - v.y)
                        / mGraphics.zoomLevel) + mGraphics.addYWhenOpenKeyBoard;
                return px >= o[0] - 6 && px <= o[0] + o[2] + 6
                        && py >= o[1] - 6 && py <= o[1] + o[3] + 6;
            }
            catch (System.Exception)
            {
                return false;
            }
        }

        /// <summary>
        /// Nhích thanh tiến trình và vạch cá theo thời gian thật.
        /// </summary>
        /// <remarks>
        /// <para><b>Nhân với khoảng thời gian đã trôi</b>, không nhích một lượng
        /// cố định mỗi khung hình: máy chạy sáu mươi khung và máy chạy ba mươi
        /// khung phải kéo được con cá trong cùng số giây.</para>
        ///
        /// <para>Chặn bước thời gian ở 100 mili giây: mở lại bảng sau khi treo
        /// máy thì bước có thể là vài giây, và thanh nhảy một phát tới đầu hoặc
        /// cạn — người chơi mất con cá vì một cú khựng của máy.</para>
        ///
        /// <para>Vạch cá đi bằng <b>hai hàm sin lệch pha</b>: một hàm cho ra
        /// chuyển động đều đặn đoán trước được, hai hàm chồng nhau thì nó đổi
        /// hướng thất thường như con cá thật đang vùng.</para>
        /// </remarks>
        private void ccCapNhat()
        {
            God.MiniGame m = God.MiniGame.gI();
            long bayGio = mSystem.currentTimeMillis();
            float daTroi = (bayGio - ccMocKhungTruoc) / 1000f;
            ccMocKhungTruoc = bayGio;
            if (daTroi < 0f)
            {
                daTroi = 0f;
            }
            if (daTroi > 0.1f)
            {
                daTroi = 0.1f;
            }

            // ccDangGiu do chamCauCa dat moi khung, doc thang tu Unity. Khong
            // dat lai o day: ve va cham chay o hai cho khac nhau trong mot khung
            // hinh, ghi de o day thi mot trong hai ben luon doc ra gia tri cu.

            switch (ccPha)
            {
                case CC_NGAM:
                    // Cu nha do chamCauCa bat — no chay o moi khung hinh nen
                    // thay duoc canh xuong. Khong lam gi o day, vi hai cho cung
                    // bat mot viec thi de lech nhau.
                    break;

                case CC_BAY:
                    if (ccTrongPha() >= CC_DAI_BAY)
                    {
                        ccVaoPha(CC_CHO_CAN);
                    }
                    break;

                case CC_CHO_CAN:
                    // Quang cho do MAY CHU boc, gui kem luc quang. Het quang do
                    // thi ca can.
                    if (ccTrongPha() >= m.ccChoMs)
                    {
                        ccVaoPha(CC_CA_CAN);
                    }
                    break;

                case CC_CA_CAN:
                    // Khong bam kip: ca nha moi di. Bao truot len may chu.
                    if (ccTrongPha() >= CC_DAI_CA_CAN)
                    {
                        ccKetThuc(false);
                    }
                    break;

                case CC_VAT_LON:
                    {
                        float t = (bayGio - ccMocVatLon) / 1000f;

                        // ---- VUNG XANH: con ca, tu di chuyen ----
                        //
                        // Hai hai hàm sin lệch pha, chu ky vai giay. Mot ham cho
                        // ra chuyen dong deu dan doan truoc duoc; hai ham chong
                        // nhau thi no doi huong that thuong nhu con ca dang vung,
                        // va cu vai giay lai doi han sang mot khuc khac cua thanh.
                        float w = m.ccTocDoVach / 100f;
                        float a = UnityEngine.Mathf.Sin(t * w * 1.6f);
                        float b = UnityEngine.Mathf.Sin(t * w * 3.1f + 1.7f);
                        ccTamVung = 0.5f + (a * 0.34f + b * 0.12f);
                        float nuaVung = m.ccRongVung / 200f;
                        if (ccTamVung < nuaVung)
                        {
                            ccTamVung = nuaVung;
                        }
                        if (ccTamVung > 1f - nuaVung)
                        {
                            ccTamVung = 1f - nuaVung;
                        }

                        // ---- VACH TIEN: cua nguoi choi, GIU THI LEN ----
                        //
                        // Giu thi vach di sang phai, tha thi no tu tut ve trai.
                        // Nen giu vach nam trong vung xanh la mot viec lien tuc:
                        // giu qua thi vuot len, tha qua thi rot lai.
                        if (ccDangGiu)
                        {
                            ccViTriVach += CC_TOC_VACH_LEN * daTroi;
                        }
                        else
                        {
                            ccViTriVach -= CC_TOC_VACH_XUONG * daTroi;
                        }
                        if (ccViTriVach < 0f)
                        {
                            ccViTriVach = 0f;
                        }
                        if (ccViTriVach > 1f)
                        {
                            ccViTriVach = 1f;
                        }

                        ccVachTrongVung = UnityEngine.Mathf.Abs(
                                ccViTriVach - ccTamVung) <= nuaVung;

                        if (ccVachTrongVung)
                        {
                            ccTienTrinh += m.ccTocDoTang * daTroi;
                        }
                        else
                        {
                            // Ra ngoai vung: tut RAT TU TU.
                            //
                            // He so 0.1 chu khong 0.5 — tuc mot phan nam muc
                            // truoc. Sua o day chu khong sua cot "giam" trong
                            // bang do kho: chia nam roi lam tron ve so nguyen
                            // thi nam muc do kho don lai gan nhau het (6 9 12
                            // 16 20 thanh 1 2 2 3 4), mat luon thu tu de-kho.
                            // Doi he so thi giu duoc dung ti le giua nam muc.
                            ccTienTrinh -= m.ccTocDoGiam * 0.1f * daTroi;
                        }

                        if (ccTienTrinh >= 100f)
                        {
                            ccTienTrinh = 100f;
                            ccKetThuc(true);
                        }
                        else if (ccTienTrinh <= 0f)
                        {
                            ccTienTrinh = 0f;
                            ccKetThuc(false);
                        }
                    }
                    break;

                case CC_KET_QUA:
                    if (ccTrongPha() >= CC_DAI_KET_QUA)
                    {
                        ccVaoPha(CC_RANH);
                    }
                    break;
            }
        }

        /// <summary>Kết thúc lượt và báo lên máy chủ đúng một lần.</summary>
        /// <remarks>
        /// Cờ <c>ccDaBao</c> canh cho khỏi gửi hai lần: thanh có thể chạm mốc rồi
        /// khung sau vẫn còn ở pha ấy, mà hai gói báo thì máy chủ nhận một cái
        /// vào ván treo còn một cái vào chỗ không có gì.
        /// </remarks>
        private void ccKetThuc(bool batDuoc)
        {
            ccBatDuoc = batDuoc;
            if (!ccDaBao)
            {
                ccDaBao = true;
                God.MiniGame.gI().cauCaBaoKetQua(batDuoc);
            }
            ccVaoPha(CC_KET_QUA);
        }

        /// <summary>Máy chủ đã nhận cú quăng: vào pha bay dây rồi chờ.</summary>
        /// <remarks>
        /// Gọi từ <c>MiniGame.docCaCan</c>. Đặt ở đây chứ không để lớp mạng tự
        /// sửa trạng thái màn vẽ: lớp mạng chỉ biết con số, còn lúc nào chuyển
        /// pha là việc của màn.
        /// </remarks>
        public void cauCaBatDauLuot()
        {
            ccTienTrinh = 0f;
            ccDaBao = false;
            ccDangDeNut = false;
            ccMocKhungTruoc = mSystem.currentTimeMillis();
            ccVaoPha(CC_BAY);
        }

        /// <summary>Máy chủ chốt kết quả: dùng con số của nó, không dùng của mình.</summary>
        public void cauCaNhanKetQua(bool batDuoc)
        {
            ccBatDuoc = batDuoc;
            if (ccPha != CC_KET_QUA)
            {
                ccVaoPha(CC_KET_QUA);
            }
        }

        // ------------------------------------------------------------------
        //  Màn Câu Cá
        // ------------------------------------------------------------------

        // ------------------------------------------------------------------
        //  Đáy biển: rong, đá, san hô
        // ------------------------------------------------------------------

        private static readonly int MAU_CAT = rgb(0xC9, 0xB0, 0x7E);
        private static readonly int MAU_CAT_TOI = rgb(0x8A, 0x74, 0x4C);
        private static readonly int MAU_RONG = rgb(0x2E, 0x7A, 0x4E);
        private static readonly int MAU_RONG_SANG = rgb(0x5F, 0xB0, 0x6B);
        private static readonly int MAU_DA = rgb(0x3E, 0x55, 0x60);
        private static readonly int MAU_SAN_HO = rgb(0xD8, 0x6E, 0x7A);

        /// <summary>
        /// Đáy biển: dải cát, mấy hòn đá, bụi san hô, và rong lay theo nước.
        /// </summary>
        /// <remarks>
        /// <para>Vị trí mọi thứ suy từ <b>chỉ số của nó</b> qua một phép nhân
        /// rồi lấy dư, chứ không bốc ngẫu nhiên: bốc mỗi khung hình thì cả đáy
        /// biển nhảy chỗ liên tục, mà giữ một mảng ngẫu nhiên thì phải cấp bộ nhớ
        /// và khởi tạo cho một thứ chỉ để nhìn. Phép dư cho ra dáng rải rác mà
        /// vẫn đứng yên qua các khung.</para>
        ///
        /// <para>Rong lay bằng <b>lát ngang</b>: mỗi lát dịch ngang một chút theo
        /// <c>sin</c>, và biên độ lớn dần về phía ngọn. Gốc rong bám đáy nên nó
        /// gần như không nhích — đó là chỗ làm cho cả bụi rong trông như đang
        /// chịu dòng nước, chứ không phải một hình đang bị lắc.</para>
        /// </remarks>
        private void veDayBien(mGraphics g, int x, int yDay, int w)
        {
            long t = mSystem.currentTimeMillis();

            // Dai cat: mep tren gon song nhe cho khoi thang bang.
            int caoCat = 9;
            for (int i = 0; i < w; i++)
            {
                int lech = (int) (UnityEngine.Mathf.Sin(i * 0.09f) * 1.6f)
                        + (int) (UnityEngine.Mathf.Sin(i * 0.031f) * 2.2f);
                g.setColor(MAU_CAT_TOI, 0.85f);
                g.fillRect(x + i, yDay - caoCat + lech, 1, caoCat + 4);
                g.setColor(MAU_CAT, 0.9f);
                g.fillRect(x + i, yDay - caoCat + lech, 1, 3);
            }

            // Da: ba hon, moi hon la mot chong lat doc theo nua duong tron.
            for (int i = 0; i < 3; i++)
            {
                int dx = x + 30 + (i * 137) % (w - 60);
                int r = 7 + (i * 5) % 6;
                for (int k = -r; k <= r; k++)
                {
                    float u = 1f - (k / (float) r) * (k / (float) r);
                    if (u <= 0f)
                    {
                        continue;
                    }
                    int cao = (int) (UnityEngine.Mathf.Sqrt(u) * r * 0.8f);
                    g.setColor(k < 0 ? MAU_DA : tronMau(MAU_DA, 0x000000, 0.3f),
                            0.95f);
                    g.fillRect(dx + k, yDay - 4 - cao, 1, cao + 5);
                }
            }

            // San ho: mot bui nhanh nho toe ra.
            for (int i = 0; i < 2; i++)
            {
                int dx = x + 70 + (i * 211) % (w - 120);
                for (int nh = -2; nh <= 2; nh++)
                {
                    int cao = 7 - UnityEngine.Mathf.Abs(nh) * 2;
                    g.setColor(tronMau(MAU_SAN_HO, MAU_NUOC_SAU,
                            0.15f * UnityEngine.Mathf.Abs(nh)), 0.9f);
                    g.fillRect(dx + nh * 3, yDay - 4 - cao, 2, cao + 2, 1);
                }
            }

            // Rong: nam bui, moi bui vai nhanh.
            for (int i = 0; i < 5; i++)
            {
                int goc = x + 16 + (i * 173) % (w - 32);
                int soNhanh = 2 + (i % 3);
                for (int n = 0; n < soNhanh; n++)
                {
                    int caoNhanh = 16 + ((i * 7 + n * 11) % 16);
                    int gx = goc + (n - soNhanh / 2) * 4;
                    // Moi nhanh lech pha rieng nen bui rong khong lay dong bo.
                    float phaGoc = (i * 1.7f + n * 0.9f);
                    for (int k = 0; k <= caoNhanh; k++)
                    {
                        float p = k / (float) caoNhanh;
                        // Bien do lon dan ve ngon: goc bam day gan nhu khong nhich.
                        float bien = 3.4f * p * p;
                        int lech = (int) (UnityEngine.Mathf.Sin(
                                (t % 3400) / 3400f * 6.2832f + phaGoc + p * 2.2f)
                                * bien);
                        g.setColor(tronMau(MAU_RONG, MAU_RONG_SANG, p * 0.7f),
                                0.92f);
                        g.fillRect(gx + lech, yDay - 4 - k, 2, 2);
                    }
                }
            }
        }

        // ------------------------------------------------------------------
        //  Pha ngắm: căn tầm quăng
        // ------------------------------------------------------------------

        /// <summary>Đang giữ để căn tầm quăng.</summary>
        private const int CC_NGAM = 6;

        /// <summary>Thanh tầm chạy hết một lượt trong bấy nhiêu mili giây.</summary>
        /// <remarks>
        /// <para>Hai giây sáu — <b>chậm rì</b>, và cố ý vậy. Đây là bước ngắm
        /// chứ không phải bước thử phản xạ: người chơi cần thấy con trỏ bò tới
        /// mức mình muốn rồi mới nhả. Chín trăm mili giây như bản trước thì cả
        /// thanh trôi qua trong một cái nháy mắt, và mức "xa" thành ra ăn may.</para>
        ///
        /// <para>Con trỏ chạy qua rồi chạy về nên giữ lâu cũng không lợi gì —
        /// muốn tầm xa thì phải nhả đúng lúc nó ở đầu bên phải.</para>
        /// </remarks>
        private const int CC_MOT_LUOT_NGAM = 2600;

        /// <summary>Mốc lúc bắt đầu giữ để căn.</summary>
        private long ccMocNgam;

        /// <summary>Đã thật sự giữ ngón trong pha ngắm chưa.</summary>
        /// <remarks>
        /// Cần cờ này để phân biệt <i>thả tay sau khi giữ</i> với <i>chưa từng
        /// giữ</i>. Không có nó thì ngay khung đầu của pha ngắm — lúc ngón còn
        /// chưa kịp được đọc là "đang giữ" — đã bị coi là đã thả, và cú quăng
        /// bắn ra ở tầm gần nhất.
        /// </remarks>
        private bool ccDaGiuTrongNgam;

        /// <summary>Phải giữ ít nhất bấy nhiêu mili giây mới tính là đang căn.</summary>
        private const int CC_GIU_TOI_THIEU = 140;

        /// <summary>
        /// Nhả tay rồi thì quăng.
        /// </summary>
        /// <remarks>
        /// <para>Gọi từ <b>hai</b> chỗ: <c>chamCauCa</c> lo đường chuột, và
        /// <c>ccCapNhat</c> lo đường phím Space. Cùng một hàm nên hai đường
        /// không thể lệch luật nhau.</para>
        ///
        /// <para>Đọc <c>isPointerDown</c> trực tiếp chứ không qua
        /// <c>ccDangDeNut</c>: cờ ấy do <c>chamCauCa</c> đặt, mà vẽ và chạm chạy
        /// ở hai chỗ khác nhau trong một khung hình — nên có lúc nó trễ một
        /// khung. Đọc thẳng thì không có độ trễ nào.</para>
        ///
        /// <para>Chờ <c>CC_GIU_TOI_THIEU</c> mili giây đầu: một cú chạm nhanh
        /// chỉ kéo dài vài chục mili giây, không đợi thì nó thành ra quăng ngay
        /// ở tầm 0.</para>
        /// </remarks>
        /// <summary>Chốt tầm đang căn và gửi cú quăng lên máy chủ.</summary>
        /// <remarks>
        /// Một chỗ duy nhất gửi cú quăng, dùng cho cả ba đường vào: nhả chuột,
        /// bấm lần hai, và nhả phím Space. Ba đường mà ba bản sao thì sớm muộn
        /// một bản lệch luật.
        /// </remarks>
        private void ccQuangNgay()
        {
            ccTamDaQuang = ccTamDangNgam();
            God.MiniGame.gI().cauCaQuang(ccTamDaQuang);
            // Chua vao pha bay o day: doi may chu tra goi "ca can" roi
            // cauCaBatDauLuot moi chuyen pha. Chuyen som thi may chu tu choi
            // (het gio nghi, khong du vang) ma man hinh van dien hoat hinh.
            ccVaoPha(CC_RANH);
            GameCanvas.clearAllPointerEvent();
        }

        /// <summary>Tầm đã căn của cú quăng vừa rồi, 0..100.</summary>
        private int ccTamDaQuang;

        /// <summary>
        /// Tầm đang căn, 0 tới 100.
        /// </summary>
        /// <remarks>
        /// Chạy lên rồi xuống bằng hàm cos nên <b>đổi chiều mềm ở hai đầu</b>:
        /// một đường gấp khúc thì tới mức 100 nó dội lại cứng ngắc, nhìn ra ngay
        /// là máy đếm. Và vì nó tự chạy xuống, người chơi phải <i>bắt</i> đúng
        /// mức mình muốn chứ không giữ lâu là được xa.
        /// </remarks>
        private int ccTamDangNgam()
        {
            if (ccPha != CC_NGAM)
            {
                return 0;
            }
            long troi = mSystem.currentTimeMillis() - ccMocNgam;
            float pha = (troi % (CC_MOT_LUOT_NGAM * 2))
                    / (float) (CC_MOT_LUOT_NGAM * 2);
            float v = 0.5f - UnityEngine.Mathf.Cos(pha * 6.2832f) * 0.5f;
            int tam = (int) (v * 100f);
            if (tam < 0)
            {
                tam = 0;
            }
            if (tam > 100)
            {
                tam = 100;
            }
            return tam;
        }

        /// <summary>Điểm phao rơi ứng với một tầm, tính theo bề ngang cảnh.</summary>
        /// <remarks>
        /// Tầm 0 rơi ngay sát mạn thuyền, tầm 100 rơi gần mép phải. Neo hai đầu
        /// vào bề ngang thật của cảnh nên bảng rộng hẹp thế nào thì đường ngắm
        /// vẫn đúng.
        /// </remarks>
        private int ccXTheoTam(int x, int w, int tam)
        {
            int gan = x + 62;
            int xaNhat = x + w - 26;
            return gan + (xaNhat - gan) * tam / 100;
        }

        /// <summary>
        /// Đường ngắm: vòng cung chấm từ ngọn cần tới điểm rơi, và vòng đích.
        /// </summary>
        /// <remarks>
        /// <para>Vòng cung là <b>chuỗi chấm</b> — <c>mGraphics</c> không vẽ được
        /// đường chéo, chỉ có hình chữ nhật. Chấm thưa dần về cuối để mắt đọc ra
        /// chiều bay chứ không thành một sợi dây liền.</para>
        ///
        /// <para>Vòng đích <b>đổi màu theo mức tầm</b> đúng ba mức mà máy chủ
        /// chia: gần, vừa, xa. Nhờ thế người chơi thấy được mình đang ở mức nào
        /// mà không phải đọc số — và ba mức ấy mới là thứ quyết định cá to cá nhỏ.</para>
        /// </remarks>
        private void veDuongNgam(mGraphics g, int x, int w, int yMatNuoc)
        {
            if (ccPha != CC_NGAM)
            {
                return;
            }
            int tam = ccTamDangNgam();
            int xCan = x + 40;
            int yCan = yMatNuoc - 42;
            int xRoi = ccXTheoTam(x, w, tam);
            int mau = ccMauTheoTam(tam);

            // Vong cung cham: cao nhat o giua duong bay.
            int soCham = 16;
            for (int i = 1; i <= soCham; i++)
            {
                float p = i / (float) soCham;
                int dx = xCan + (int) ((xRoi - xCan) * p);
                int cao = (int) (UnityEngine.Mathf.Sin(p * 3.1416f) * 26f);
                int dy = yCan + (int) ((yMatNuoc - yCan) * p) - cao;
                int co = (i % 2 == 0) ? 2 : 1;
                g.setColor(mau, 0.75f - 0.35f * p);
                g.fillRect(dx, dy, co, co);
            }

            // Vong dich tren mat nuoc, nhip theo thoi gian.
            float nhip = 0.5f + 0.5f * UnityEngine.Mathf.Sin(
                    (mSystem.currentTimeMillis() % 700) / 700f * 6.2832f);
            int co2 = 16 + (int) (6 * nhip);
            veTron(g, xRoi, yMatNuoc + 1, co2 + 8, mau, 0.14f);
            g.setColor(mau, 0.85f);
            g.fillRect(xRoi - co2 / 2, yMatNuoc - 1, co2, 2, 1);
            g.fillRect(xRoi - co2 / 2, yMatNuoc + 3, co2, 2, 1);
            g.fillRect(xRoi - co2 / 2, yMatNuoc - 1, 2, 6, 1);
            g.fillRect(xRoi + co2 / 2 - 2, yMatNuoc - 1, 2, 6, 1);
            veTron(g, xRoi, yMatNuoc + 1, 4, mau, 0.95f);
        }

        /// <summary>Màu ứng với ba mức tầm mà máy chủ chia.</summary>
        /// <remarks>
        /// Ba ngưỡng này phải khớp <c>XA_VUA</c> và <c>XA_XA</c> bên máy chủ —
        /// hiện màu một mức mà máy chủ tính mức khác thì người chơi thấy vòng
        /// xanh rồi nhận cá của mức đỏ.
        /// </remarks>
        private static int ccMauTheoTam(int tam)
        {
            if (tam >= 75)
            {
                return MAU_VUNG_SANG;
            }
            if (tam >= 40)
            {
                return MAU_VANG;
            }
            return MAU_VUNG_XAU;
        }

        /// <summary>Tên ngắn của ba mức tầm.</summary>
        private static string ccTenMucTam(int tam)
        {
            if (tam >= 75)
            {
                return "XA";
            }
            if (tam >= 40)
            {
                return "VỪA";
            }
            return "GẦN";
        }

        /// <summary>
        /// Thanh tầm quăng, hiện dưới cùng trong lúc đang căn.
        /// </summary>
        /// <remarks>
        /// Ba vùng màu tương ứng ba mức, và con trỏ chạy trên đó. Vẽ ba vùng chứ
        /// không một dải chuyển màu trơn: người chơi cần biết mình <b>đang ở mức
        /// nào</b>, mà một dải trơn thì không có ranh nào để nhắm vào.
        /// </remarks>
        private void veThanhTam(mGraphics g, int x, int y, int w, int h)
        {
            int tam = ccTamDangNgam();
            int caoT = 15;
            int yT = y + (h - caoT) / 2;

            g.setColor(MAU_VUNG_XAU, 0.85f);
            g.fillRect(x, yT, w, caoT, 3);
            g.setColor(MAU_VANG, 0.9f);
            g.fillRect(x + w * 40 / 100, yT, w * 35 / 100, caoT, 3);
            g.setColor(MAU_VUNG_SANG, 0.95f);
            g.fillRect(x + w * 75 / 100, yT, w * 25 / 100, caoT, 3);
            veChuyenSac(g, x, yT, w, caoT, 5);

            int xt = x + w * tam / 100;
            veTron(g, xt, yT + caoT / 2, 24, 0xFFFFFF, 0.22f);
            g.setColor(0x000000, 0.5f);
            g.fillRect(xt - 2, yT - 3, 5, caoT + 6, 2);
            g.setColor(0xFFFFFF, 1f);
            g.fillRect(xt - 1, yT - 2, 3, caoT + 4, 1);
            veChuPhang(g, mFont.tahoma_7b_white, ccTenMucTam(tam) + "  " + tam,
                    x + w / 2, yT + 3, mFont.CENTER);
        }

        /// <summary>
        /// Màn Câu Cá.
        /// </summary>
        /// <remarks>
        /// <para><b>Thứ tự vẽ đi từ xa tới gần</b>: trời, nước, đàn cá, bọt, phao
        /// và dây, thuyền. Lớp sau che lớp trước, nên cá bơi <i>dưới</i> phao và
        /// thuyền nổi <i>trên</i> nước mà không phải tính chiều sâu.</para>
        ///
        /// <para><b>Không có khung tối sau chữ.</b> Chữ trong khung cảnh chỉ có
        /// một nét viền tối một điểm là đủ đọc trên nước; dán thêm một khối đen
        /// sau mỗi dòng thì cảnh thành ra lỗ chỗ.</para>
        /// </remarks>
        private void veCauCa(mGraphics g)
        {
            napSprite();
            ccCapNhat();

            int x = x0 + LE;
            int y = yNoiDung();
            int w = rong - LE * 2;
            int h = y0 + cao - LE - y;

            int caoDuoi = 62;
            int caoCanh = h - caoDuoi - 6;
            if (caoCanh < 70)
            {
                caoCanh = h - caoDuoi;
            }

            veKhungBo(g, x - 3, y - 3, w + 6, caoCanh + 6, MAU_NUOC_SAU, 0.95f,
                    MAU_VIEN, 0.85f, 2);
            g.setClip(x, y, w, caoCanh);
            int yMatNuoc = y + caoCanh * 2 / 5;
            veTroi(g, x, y, w, yMatNuoc - y);
            veNuoc(g, x, yMatNuoc, w, y + caoCanh - yMatNuoc);
            veDayBien(g, x, y + caoCanh, w);
            veDanCa(g, x, yMatNuoc + 10, w, y + caoCanh - yMatNuoc - 34);
            veBongBongNuoc(g, x, yMatNuoc, w, y + caoCanh - yMatNuoc);
            veDuongNgam(g, x, w, yMatNuoc);
            veDayVaPhao(g, x, w, yMatNuoc);
            veThuyen(g, x, yMatNuoc);
            veCaTrenMoc(g, x, w, yMatNuoc);
            veChuTrenCanh(g, x, y, w, caoCanh);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            veSoThoiCauCa(g, x + w - 6, y + 4);
            veChanDoan(g, x + 4, y + caoCanh - 12);
            veHangDuoi(g, x, y + caoCanh + 6, w, caoDuoi - 6);
        }

        /// <summary>Trời: dải đậm ở đỉnh nhạt ở chân trời, vầng nắng, mây trôi.</summary>
        private void veTroi(mGraphics g, int x, int y, int w, int h)
        {
            if (h <= 0)
            {
                return;
            }
            veDaiMau(g, x, y, w, h, MAU_TROI_CAO, MAU_TROI_THAP, 10);

            int nx = x + w - 34;
            int ny = y + 14;
            veTron(g, nx, ny, 34, 0xFFF3C4, 0.12f);
            veTron(g, nx, ny, 24, 0xFFF3C4, 0.22f);
            veTron(g, nx, ny, 14, 0xFFFBE8, 0.85f);
            veTron(g, nx, ny, 8, 0xFFFFFF, 0.95f);

            // May: chu ky rat dai nen chuyen dong gan nhu khong thay duoc, dung
            // y do. May bay nhanh thi khung canh thanh ra bo boi.
            long t = mSystem.currentTimeMillis();
            for (int i = 0; i < 3; i++)
            {
                int chuKy = 42000 + i * 11000;
                int mx = x - 46 + (int) ((w + 92L) * ((t + i * 15000L) % chuKy) / chuKy);
                int my = y + 9 + i * 10;
                g.setColor(0xFFFFFF, 0.40f - i * 0.10f);
                g.fillRect(mx, my, 26, 7, 3);
                g.fillRect(mx + 6, my - 4, 15, 7, 3);
                g.fillRect(mx + 16, my - 1, 12, 6, 3);
            }
        }

        /// <summary>
        /// Mặt nước: dải tối dần xuống sâu, sóng lăn tăn, ánh nắng gợn.
        /// </summary>
        /// <remarks>
        /// Sóng là mấy vạch ngang mảnh trượt ngang với <b>ba tốc độ khác nhau</b>.
        /// Cùng tốc thì cả mặt nước trôi thành một khối, mắt đọc ra ngay là ảnh bị
        /// kéo chứ không phải nước đang động.
        /// </remarks>
        private void veNuoc(mGraphics g, int x, int y, int w, int h)
        {
            if (h <= 0)
            {
                return;
            }
            veDaiMau(g, x, y, w, h, MAU_NUOC_MAT, MAU_NUOC_SAU, 12);

            g.setColor(MAU_BOT, 0.34f);
            g.fillRect(x, y - 1, w, 1);
            g.setColor(MAU_BOT, 0.14f);
            g.fillRect(x, y, w, 2);

            long t = mSystem.currentTimeMillis();
            for (int lop = 0; lop < 3; lop++)
            {
                int chuKy = 6400 + lop * 3100;
                int lech = (int) (w * (t % chuKy) / chuKy);
                int yy = y + 6 + lop * 8;
                g.setColor(0xFFFFFF, 0.16f - lop * 0.04f);
                for (int i = -1; i < w / 38 + 2; i++)
                {
                    int vx = x + i * 38 + lech;
                    if (vx > x + w)
                    {
                        vx -= w + 38;
                    }
                    g.fillRect(vx, yy, 18, 1);
                    g.fillRect(vx + 23, yy + 3, 10, 1);
                }
            }

            int nx = x + w - 34;
            for (int i = 0; i < 6; i++)
            {
                float pha = (t % 3000) / 3000f + i * 0.17f;
                int lech = (int) (UnityEngine.Mathf.Sin(pha * 6.2832f) * 5);
                g.setColor(0xFFF3C4, 0.09f - i * 0.012f);
                g.fillRect(nx - 8 + lech, y + i * 9, 16, 6, 3);
            }
        }

        /// <summary>
        /// Đàn cá bơi ngang, mỗi loài một tầng một tốc độ.
        /// </summary>
        /// <remarks>
        /// Năm con cho năm loài, nên người chơi thấy trước hình dạng và cỡ của
        /// thứ mình đang chờ. Con đang bị kéo thì <b>rút khỏi đàn</b> — nó đã ở
        /// trên móc, vẽ nó bơi thong dong dưới nước nữa là hai con.
        /// </remarks>
        private void veDanCa(mGraphics g, int x, int y, int w, int h)
        {
            if (h <= 14)
            {
                return;
            }
            God.MiniGame m = God.MiniGame.gI();
            long t = mSystem.currentTimeMillis();
            int dangKeo = (ccPha == CC_VAT_LON || ccPha == CC_KET_QUA)
                    ? m.ccLoaiCan : -1;

            // Trong luc cho, ca DON LAI phia phao. Con sap can di han toi tan
            // moi; may con kia chi tap trung lai gan cho co ve nhu ca dan dang
            // to mo, roi tan ra khi mot con da cap.
            float donLai = 0f;
            if (ccPha == CC_CHO_CAN && m.ccChoMs > 0)
            {
                donLai = lamMuot(ccTrongPha() / (float) m.ccChoMs);
            }
            else if (ccPha == CC_CA_CAN)
            {
                donLai = 1f;
            }
            int xPhao = ccXPhao(x, w);
            int yMoi = y - 6;

            for (int i = 0; i < TEN_CA.Length; i++)
            {
                if (i == dangKeo)
                {
                    continue;
                }
                bool sangPhai = (i % 2 == 1);
                // Chu ky dai gan gap doi ban truoc: ca boi cham lai cho de nhin.
                int chuKy = 20000 + i * 4200;
                long pha = (t + i * 3300L) % chuKy;
                int di = (int) ((w + 90L) * pha / chuKy);
                int cx = sangPhai ? (x - 45 + di) : (x + w + 45 - di);
                int cy = y + 8 + (i * (h - 18) / TEN_CA.Length);
                cy += (int) (UnityEngine.Mathf.Sin((pha / (float) chuKy) * 12.566f) * 3);

                if (donLai > 0f)
                {
                    bool laConCan = (i == m.ccLoaiCan);
                    // Con sap can keo het duong; may con kia chi keo mot phan
                    // ba, va dung lai o mot vong quanh moi.
                    float keo = laConCan ? donLai : (donLai * 0.34f);
                    int dichX = laConCan ? xPhao
                            : (xPhao + ((i % 2 == 0) ? -34 : 30));
                    int dichY = laConCan ? yMoi : (yMoi + 8 + (i % 3) * 7);
                    int cxCu = cx;
                    cx += (int) ((dichX - cx) * keo);
                    cy += (int) ((dichY - cy) * keo);
                    // Huong theo chieu dang di chuyen, khong theo chieu boi cu:
                    // ca dang tien ve moi thi phai quay dau ve phia moi.
                    if (keo > 0.05f)
                    {
                        sangPhai = (dichX > cxCu);
                    }
                }
                veCaSprite(g, fiCa, i, cx, cy, sangPhai,
                        ccPha == CC_CA_CAN && i == m.ccLoaiCan);
            }
        }

        /// <summary>Bọt khí nổi lên từ đáy rồi tan ở mặt nước.</summary>
        private static void veBongBongNuoc(mGraphics g, int x, int y, int w, int h)
        {
            if (h <= 10)
            {
                return;
            }
            long t = mSystem.currentTimeMillis();
            for (int i = 0; i < 10; i++)
            {
                int chuKy = 3800 + i * 470;
                float p = ((t + i * 900L) % chuKy) / (float) chuKy;
                int bx = x + 14 + (i * 37) % (w - 20);
                bx += (int) (UnityEngine.Mathf.Sin(p * 9.4f + i) * 3);
                int by = y + h - (int) (h * p);
                int co = 2 + (i % 3);
                // Mo dan ca hai dau: hien ra o day, tan o mat nuoc.
                g.setColor(MAU_BOT, 0.30f * UnityEngine.Mathf.Sin(p * 3.1416f));
                g.fillRect(bx, by, co, co, co / 2);
            }
        }

        /// <summary>
        /// Điểm phao neo trên mặt nước — <b>theo tầm đã căn</b>.
        /// </summary>
        /// <remarks>
        /// Dùng chung một hàm với đường ngắm nên chỗ phao rơi đúng bằng chỗ vòng
        /// đích vừa chỉ. Hai công thức riêng thì sớm muộn lệch nhau, và người
        /// chơi căn một chỗ mà phao rơi một chỗ.
        /// </remarks>
        private int ccXPhao(int x, int w)
        {
            return ccXTheoTam(x, w, ccTamDaQuang);
        }

        /// <summary>
        /// Dây cần, phao, và cột nước toé.
        /// </summary>
        /// <remarks>
        /// <para>Phao và cột nước dùng <b>sprite</b>. Dây thì vẫn vẽ bằng mã: nó
        /// phải nối đúng từ ngọn cần tới phao, mà hai điểm ấy đổi chỗ theo pha —
        /// một ảnh dây cố định không theo được.</para>
        ///
        /// <para>Dây là chuỗi điểm chứ không phải đường thẳng: <c>mGraphics</c>
        /// chỉ vẽ được hình chữ nhật. Cái võng ở giữa cho nó ra dáng sợi dây, và
        /// độ võng <b>giảm khi đang kéo</b> — lúc vật lộn thì dây căng.</para>
        ///
        /// <para>Lúc <b>cá cắn</b> thì phao dập hẳn xuống kèm cột nước — đó là
        /// dấu hiệu người chơi phải chờ để thấy, nên nó phải rõ.</para>
        /// </remarks>
        private void veDayVaPhao(mGraphics g, int x, int w, int yMatNuoc)
        {
            if (ccPha == CC_RANH)
            {
                return;
            }
            int xCan = x + 40;
            int yCan = yMatNuoc - 42;
            int xPhao = ccXPhao(x, w);

            float tienDo = 1f;
            if (ccPha == CC_BAY)
            {
                tienDo = lamMuot(ccTrongPha() / (float) CC_DAI_BAY);
            }
            int xHt = xCan + (int) ((xPhao - xCan) * tienDo);

            int nhap = (int) (UnityEngine.Mathf.Sin(
                    (mSystem.currentTimeMillis() % 1900) / 1900f * 6.2832f) * 2);
            int yPhao = yMatNuoc + nhap;
            float cang = 0f;

            if (ccPha == CC_CA_CAN)
            {
                // Ca can: phao dap manh, nhip nhanh.
                cang = 0.7f;
                yPhao = yMatNuoc + 5 + (int) (5 * UnityEngine.Mathf.Sin(
                        (mSystem.currentTimeMillis() % 240) / 240f * 6.2832f));
            }
            else if (ccPha == CC_VAT_LON)
            {
                cang = 0.4f + 0.6f * (ccTienTrinh / 100f);
                yPhao = yMatNuoc + 4 + (int) (4 * UnityEngine.Mathf.Sin(
                        (mSystem.currentTimeMillis() % 420) / 420f * 6.2832f));
            }

            g.setColor(0xFFFFFF, 0.5f);
            const int SO_DIEM = 28;
            float doVong = 7f * (1f - cang * 0.9f);
            for (int i = 0; i <= SO_DIEM; i++)
            {
                float p = i / (float) SO_DIEM;
                int dx = xCan + (int) ((xHt - xCan) * p);
                int vong = (int) (UnityEngine.Mathf.Sin(p * 3.1416f) * doVong);
                int dy = yCan + (int) ((yPhao - yCan) * p) + vong;
                g.fillRect(dx, dy, 1, 1);
            }

            // Cot nuoc toe: chay MOT luot khi phao vua roi, va lai mot luot khi
            // ca can.
            if (fiToe != null)
            {
                long tp = ccTrongPha();
                if (ccPha == CC_CHO_CAN && tp < 560)
                {
                    veKhung(g, fiToe, (int) (tp * SO_KHUNG_TOE / 560),
                            xPhao, yMatNuoc - 8, 0);
                }
                else if (ccPha == CC_CA_CAN && tp < 500)
                {
                    veKhung(g, fiToe, (int) (tp * SO_KHUNG_TOE / 500),
                            xPhao, yMatNuoc - 8, 0);
                }
            }

            if (ccPha != CC_KET_QUA)
            {
                int chia = (ccPha == CC_CA_CAN) ? 60
                        : ((ccPha == CC_VAT_LON) ? 80 : 160);
                int kp = (int) ((mSystem.currentTimeMillis() / chia)
                        % SO_KHUNG_PHAO);
                veKhung(g, fiPhao, kp, xHt, yPhao - 2, 0);
            }
        }

        /// <summary>
        /// Con cá trên móc, lúc vật lộn và lúc kéo lên.
        /// </summary>
        /// <remarks>
        /// Hai dải khác nhau: <c>cc_keo</c> cho lúc còn dưới nước đang vùng, và
        /// <c>cc_nhay</c> cho khoảnh khắc nó bật khỏi mặt nước. Đổi khung theo
        /// tiến trình nên càng kéo được nhiều thì con cá càng lên cao — người chơi
        /// đọc được tiến trình ngay trên khung cảnh.
        /// </remarks>
        private void veCaTrenMoc(mGraphics g, int x, int w, int yMatNuoc)
        {
            int xPhao = ccXPhao(x, w);

            if (ccPha == CC_VAT_LON)
            {
                float p = ccTienTrinh / 100f;
                int cy = yMatNuoc + 24 - (int) (28 * p);
                int k = (int) (p * SO_KHUNG_KEO);
                int lac = (int) (UnityEngine.Mathf.Sin(
                        (mSystem.currentTimeMillis() % 380) / 380f * 6.2832f) * 3);
                veKhung(g, fiKeo, k, xPhao + lac, cy, 0);
                return;
            }

            if (ccPha == CC_KET_QUA && ccBatDuoc && fiNhay != null)
            {
                long tp = ccTrongPha();
                if (tp < 900)
                {
                    int k = (int) (tp * SO_KHUNG_NHAY / 900);
                    int len = (int) (28 * lamMuot(tp / 900f));
                    veKhung(g, fiNhay, k, xPhao, yMatNuoc - 6 - len, 0);
                }
                else
                {
                    veKhung(g, fiNhay, SO_KHUNG_NHAY - 1, xPhao,
                            yMatNuoc - 34, 0);
                }
            }
        }

        /// <summary>
        /// Chiếc thuyền gỗ và cái cần.
        /// </summary>
        /// <remarks>
        /// Thuyền dựng bằng lát dọc — không có sprite thuyền trong bộ ảnh. Mạn
        /// theo một đường cong: mũi vút lên, giữa thấp, lòng thuyền sâu theo
        /// <c>sin</c>. Cần thì dùng <b>sprite</b>, khung chọn theo pha: thẳng lúc
        /// rảnh, cong dần khi kéo.
        /// </remarks>
        private void veThuyen(mGraphics g, int x, int yMatNuoc)
        {
            int tx = x + 6;
            int nhipThuyen = (int) (UnityEngine.Mathf.Sin(
                    (mSystem.currentTimeMillis() % 2600) / 2600f * 6.2832f) * 1.6f);
            int ty = yMatNuoc - 8 + nhipThuyen;
            const int DAI = 48;

            g.setColor(0x000000, 0.20f);
            g.fillRect(tx + 3, yMatNuoc + 1, DAI - 6, 4, 2);

            for (int i = 0; i <= DAI; i++)
            {
                float p = i / (float) DAI;
                float sau = UnityEngine.Mathf.Sin(p * 3.1416f);
                int caoLat = 3 + (int) (7f * sau);
                int lenMep = (int) (5f * p * p);
                int yTren = ty - lenMep;
                g.setColor(tronMau(MAU_GO, MAU_GO_SANG, 0.25f + 0.4f * sau), 1f);
                g.fillRect(tx + i, yTren, 1, caoLat);
                g.setColor(MAU_GO_SANG, 0.9f);
                g.fillRect(tx + i, yTren, 1, 2);
                g.setColor(MAU_GO_TOI, 0.8f);
                g.fillRect(tx + i, yTren + caoLat - 1, 1, 1);
            }
            g.setColor(MAU_GO_TOI, 0.35f);
            g.fillRect(tx + 4, ty + 4, DAI - 8, 1);
            g.setColor(0xFFFFFF, 0.10f);
            g.fillRect(tx + 4, ty + 6, DAI - 8, 1);

            // Nguoi ngoi cau: mot khoi nho, vi cai can moi la thu can nhin.
            int nx = tx + 13;
            g.setColor(rgb(0x2F, 0x6B, 0x8C), 1f);
            g.fillRect(nx, ty - 8, 8, 11, 3);
            g.setColor(rgb(0xE8, 0xB9, 0x8A), 1f);
            g.fillRect(nx + 1, ty - 14, 7, 7, 3);
            g.setColor(rgb(0xE3, 0xC1, 0x76), 1f);
            g.fillRect(nx - 3, ty - 15, 14, 3, 1);

            int soKhungCan = SO_KHUNG_CAN;
            int kc = 0;
            if (ccPha == CC_BAY)
            {
                kc = (int) ((soKhungCan - 1) * lamMuot(ccTrongPha()
                        / (float) CC_DAI_BAY));
            }
            else if (ccPha == CC_CHO_CAN)
            {
                kc = 1;
            }
            else if (ccPha == CC_CA_CAN)
            {
                // Giat theo nhip phao dap.
                kc = 2 + (int) ((mSystem.currentTimeMillis() / 120) % 2);
            }
            else if (ccPha == CC_VAT_LON)
            {
                kc = 1 + (int) ((soKhungCan - 2) * (ccTienTrinh / 100f));
                if (ccDangGiu && kc < soKhungCan - 1)
                {
                    kc++;
                }
            }
            else if (ccPha == CC_KET_QUA)
            {
                kc = ccBatDuoc ? (soKhungCan - 1) : 0;
            }
            // Neo goc duoi-trai vao tay nguoi cau: trong o sprite cai can cung
            // xep sat goc ay, nen hai goc trung nhau thi can nam dung cho va
            // hien du co.
            veKhungNeo(g, fiCan, kc, tx + 18, ty + 2, 0,
                    mGraphics.BOTTOM | mGraphics.LEFT);
        }

        /// <summary>
        /// Chữ nổi trên khung cảnh: tên con đang kéo, lời báo cá cắn, kết quả.
        /// </summary>
        /// <remarks>
        /// <b>Không có khung tối phía sau.</b> Chữ chỉ có nét viền một điểm —
        /// <c>veChuNoi</c> lo việc đó — và thế là đủ đọc trên nước. Dán một khối
        /// đen sau mỗi dòng thì khung cảnh thành ra lỗ chỗ, mà cũng chẳng dễ đọc
        /// hơn.
        /// </remarks>
        private void veChuTrenCanh(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            int loai = m.ccLoaiCan;
            int giua = x + w / 2;

            if (ccPha == CC_NGAM)
            {
                int tam = ccTamDangNgam();
                veChuPhang(g, mFont.tahoma_7b_white,
                        "Nhả tay để quăng — " + ccTenMucTam(tam), giua,
                        y + 6, mFont.CENTER);
                return;
            }

            if (ccPha == CC_CHO_CAN)
            {
                // Ba dau cham chay vong: cho biet la dang cho, khong phai treo.
                int soCham = 1 + (int) ((mSystem.currentTimeMillis() / 420) % 3);
                string s = "Đang chờ cá";
                for (int i = 0; i < soCham; i++)
                {
                    s += ".";
                }
                veChuPhang(g, mFont.tahoma_7_white, s, giua, y + 6, mFont.CENTER);
                return;
            }

            if (ccPha == CC_CA_CAN)
            {
                // Nhay theo nhip nhanh: mat bat duoc cai nhay truoc khi doc chu.
                bool sang = (mSystem.currentTimeMillis() / 130) % 2 == 0;
                int xPhao = ccXPhao(x, w);
                veTron(g, xPhao, y + h * 2 / 5 - 26, sang ? 30 : 22,
                        MAU_VANG, sang ? 0.30f : 0.16f);
                veChuPhang(g, sang ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_white,
                        "CÁ CẮN — KÉO!", giua, y + 6, mFont.CENTER);
                return;
            }

            if (ccPha == CC_VAT_LON && loai >= 0 && loai < TEN_CA.Length)
            {
                veChuPhang(g, mFont.tahoma_7b_white, TEN_CA[loai], giua, y + 6,
                        mFont.CENTER);
                veChuPhang(g, ccDangGiu ? mFont.tahoma_7b_green
                        : mFont.tahoma_7_white,
                        ccDangGiu ? "ĐANG KÉO" : "Giữ bất cứ đâu để kéo",
                        giua, y + 18, mFont.CENTER);
                return;
            }

            if (ccPha != CC_KET_QUA || loai < 0 || loai >= TEN_CA.Length)
            {
                return;
            }

            // Ket qua: chi chu va quang sang, khong khung.
            float p = ccTrongPha() / (float) CC_DAI_KET_QUA;
            if (p > 1f)
            {
                p = 1f;
            }
            float mo = p > 0.72f ? (1f - lamMuot((p - 0.72f) / 0.28f)) : 1f;
            int len = (int) (10 * lamMuot(p < 0.3f ? (p / 0.3f) : 1f));
            int yc = y + h - 40 - len;
            bool duocTien = ccBatDuoc && THUONG_CA[loai] > 0;

            if (duocTien)
            {
                veHatSang(g, giua, yc + 12, 9, 46, MAU_VANG, 0.75f * mo, 1600);
                veTron(g, giua, yc + 12, 64, MAU_VANG, 0.14f * mo);
            }
            if (!ccBatDuoc)
            {
                veChuPhang(g, mFont.tahoma_7b_red, "TUỘT MẤT!", giua, yc,
                        mFont.CENTER);
                veChuPhang(g, mFont.tahoma_7_white, TEN_CA[loai] + " thoát rồi",
                        giua, yc + 15, mFont.CENTER);
                return;
            }
            veChuPhang(g, mFont.tahoma_7b_green, "ĐƯỢC " + TEN_CA[loai] + "!",
                    giua, yc, mFont.CENTER);
            if (duocTien)
            {
                SmallImage.drawSmallImage(g, ICON_THOI_VANG, giua - 24, yc + 21,
                        0, mGraphics.VCENTER | mGraphics.HCENTER);
                veChuPhang(g, mFont.tahoma_7b_yellow, "+" + THUONG_CA[loai],
                        giua - 12, yc + 15, mFont.LEFT);
            }
            else
            {
                veChuPhang(g, mFont.tahoma_7_white, "Chẳng đáng đồng nào...",
                        giua, yc + 15, mFont.CENTER);
            }
        }

        /// <summary>Số thỏi vàng đang có, góc trên phải khung cảnh.</summary>
        /// <remarks>
        /// Chỉ icon và con số có nét viền, <b>không khung</b>. Con số vàng trên
        /// nền trời lam đã đủ tương phản; thêm một khối đen sau nó chỉ làm góc
        /// khung cảnh bị đục một lỗ.
        /// </remarks>
        private void veSoThoiCauCa(mGraphics g, int xPhai, int y)
        {
            God.MiniGame m = God.MiniGame.gI();
            string s = m.tongThoi + string.Empty;
            SmallImage.drawSmallImage(g, ICON_THOI_VANG,
                    xPhai - mFont.tahoma_7b_yellow.getWidth(s) - 12, y + 6, 0,
                    mGraphics.VCENTER | mGraphics.HCENTER);
            veChuPhang(g, mFont.tahoma_7b_yellow, s, xPhai, y + 2, mFont.RIGHT);
        }

        /// <summary>
        /// Một dòng nhỏ ghi trạng thái, để lần ra lỗi mà không phải đoán.
        /// </summary>
        /// <remarks>
        /// <para>Đặt tạm trong lúc còn đang gỡ chỗ "bấm quăng không ăn". Nó cho
        /// biết đúng những con số quyết định cú quăng: pha đang chạy, số vàng so
        /// với tiền mồi, giờ nghỉ còn lại, và hai cờ chạm.</para>
        ///
        /// <para>Bật tắt bằng <see cref="CC_HIEN_CHAN_DOAN"/>. Xong việc thì đặt
        /// về <c>false</c> — để lại thì nó là rác nằm trên khung cảnh.</para>
        /// </remarks>
        private const bool CC_HIEN_CHAN_DOAN = false;

        private void veChanDoan(mGraphics g, int x, int y)
        {
            if (!CC_HIEN_CHAN_DOAN)
            {
                return;
            }
            God.MiniGame m = God.MiniGame.gI();
            int[] o = oNutGiu();
            bool trong = GameCanvas.isPointerHoldIn(o[0] - 4, o[1] - 4,
                    o[2] + 8, o[3] + 8);
            string s = "pha=" + ccPha
                    + " vang=" + m.tongThoi + "/" + m.ccTienMoi
                    + " cho=" + m.ccGiayCho
                    + " down=" + (GameCanvas.isPointerDown ? 1 : 0)
                    + " nha=" + (GameCanvas.isPointerJustRelease ? 1 : 0)
                    + " trong=" + (trong ? 1 : 0)
                    + " px=" + GameCanvas.px + "," + GameCanvas.py
                    + " nut=" + o[0] + "," + o[1];
            veChuNoi(g, mFont.tahoma_7_white, s, x, y, mFont.LEFT);
        }

        // ------------------------------------------------------------------
        //  Hàng dưới: hai thanh hoặc bảng cá, và nút
        // ------------------------------------------------------------------

        private const int CC_RONG_NUT = 84;

        /// <summary>Ô nút, luôn ở một chỗ bất kể pha nào.</summary>
        private int[] oNutGiu()
        {
            return new int[] { x0 + rong - LE - CC_RONG_NUT,
                y0 + cao - LE - 52, CC_RONG_NUT, 50 };
        }

        /// <summary>Hàng dưới cùng: đổi nội dung theo pha, nút cố định.</summary>
        private void veHangDuoi(mGraphics g, int x, int y, int w, int h)
        {
            int wTrai = w - CC_RONG_NUT - 6;
            if (wTrai > 40)
            {
                if (ccPha == CC_VAT_LON)
                {
                    veHaiThanh(g, x, y, wTrai, h);
                }
                else if (ccPha == CC_NGAM)
                {
                    veThanhTam(g, x, y, wTrai, h);
                }
                else
                {
                    veBangCa(g, x, y + h / 2 - 20, wTrai, 40);
                }
            }
            veNutGiu(g);
        }

        /// <summary>
        /// Hai thanh của pha vật lộn.
        /// </summary>
        /// <remarks>
        /// <para><b>Thanh trên — thanh vật lộn.</b> Một vạch duy nhất là con cá,
        /// nó <b>chạy đổi chỗ liên tục</b> theo hai hàm sin lệch pha. Giữa thanh
        /// là vùng an toàn; giữ nút khi vạch nằm trong vùng ấy thì tiến trình lên,
        /// giữ khi vạch ra ngoài thì tiến trình <i>tụt</i> — kéo mạnh sai lúc là
        /// cá vùng ra.</para>
        ///
        /// <para><b>Thanh dưới — thanh tiến trình.</b> Đầy là kéo được, cạn là
        /// tuột. Tốc độ lên xuống lấy theo <b>giá con cá</b>, máy chủ gửi kèm lúc
        /// cá cắn.</para>
        ///
        /// <para>Vùng an toàn nằm <b>giữa</b> thanh chứ không chạy: một thứ chạy
        /// là đủ để phải nhìn. Cả hai chạy thì khó vì rối, không phải khó vì cần
        /// tay nghề.</para>
        /// </remarks>
        private void veHaiThanh(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            int caoT = 15;
            int khe = 8;
            int yT1 = y + (h - caoT * 2 - khe) / 2;
            int yT2 = yT1 + caoT + khe;

            // ---- thanh vat lon ----
            g.setColor(MAU_VUNG_XAU, 0.85f);
            g.fillRect(x, yT1, w, caoT, 3);
            float nuaVung = m.ccRongVung / 200f;
            int wVung = (int) (w * nuaVung * 2);
            // Vung xanh nam theo TAM DANG DI CHUYEN, khong con dinh o giua.
            int xVung = x + (int) (w * ccTamVung) - wVung / 2;
            g.setColor(ccVachTrongVung ? MAU_VUNG_SANG : MAU_VUNG_TOT,
                    ccVachTrongVung ? 1f : 0.85f);
            g.fillRect(xVung, yT1, wVung, caoT, 3);
            veChuyenSac(g, x, yT1, w, caoT, 5);
            if (ccVachTrongVung && ccDangGiu)
            {
                veVienChay(g, xVung, yT1, wVung, caoT, 0xFFFFFF);
            }

            // Vach ca: mot vach sang co quang, va con ca nho tren dinh.
            int xv = x + (int) (w * ccViTriVach);
            veTron(g, xv, yT1 + caoT / 2, 24, 0xFFFFFF, 0.20f);
            g.setColor(0x000000, 0.5f);
            g.fillRect(xv - 2, yT1 - 3, 5, caoT + 6, 2);
            g.setColor(0xFFFFFF, 1f);
            g.fillRect(xv - 1, yT1 - 2, 3, caoT + 4, 1);
            veCaSprite(g, fiCaBe, m.ccLoaiCan, xv, yT1 - 12,
                    ccViTriVach > 0.5f, ccVachTrongVung);

            // ---- thanh tien trinh ----
            g.setColor(0x000000, 0.42f);
            g.fillRect(x, yT2, w, caoT, 3);
            int wDay = (int) (w * ccTienTrinh / 100f);
            if (wDay > 0)
            {
                // Mau doi theo muc: do khi sap tuot, xanh khi sap keo duoc.
                int mau = ccTienTrinh < 30f ? MAU_VUNG_XAU
                        : (ccTienTrinh < 70f ? MAU_VANG : MAU_VUNG_TOT);
                g.setColor(mau, 1f);
                g.fillRect(x, yT2, wDay, caoT, 3);
                veChuyenSac(g, x, yT2, wDay, caoT, 5);
                veVetSang(g, x, yT2, wDay, caoT, 1300);
                g.setColor(0xFFFFFF, 0.8f);
                g.fillRect(x + wDay - 2, yT2, 2, caoT);
            }
            // Vach moc: chi ra muc bat dau, de biet dang hon hay kem luc dau.
            int xMoc = x + (int) (w * m.ccTienTrinhDau / 100f);
            g.setColor(0xFFFFFF, 0.35f);
            g.fillRect(xMoc, yT2, 1, caoT);
            veChuPhang(g, mFont.tahoma_7b_white, ((int) ccTienTrinh) + "%",
                    x + w / 2, yT2 + 3, mFont.CENTER);
        }

        /// <summary>
        /// Bảng năm loài cá và số thỏi từng loài.
        /// </summary>
        /// <remarks>
        /// Nền ô lấy <b>tông kem của bảng</b> pha nhẹ màu loài, chứ không phải
        /// một khối xanh thẳm: năm ô tối đục đặt trên nền kem trông như năm lỗ
        /// khoét. Pha nhẹ thì vẫn phân biệt được loài mà cả hàng còn liền mạch
        /// với bảng.
        /// </remarks>
        private void veBangCa(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            int vuaCau = (ccPha == CC_KET_QUA && ccBatDuoc) ? m.ccLoaiCan : -1;

            int soO = TEN_CA.Length;
            int wO = (w - (soO - 1) * 3) / soO;
            for (int i = 0; i < soO; i++)
            {
                int ox = x + i * (wO + 3);
                bool sang = (i == vuaCau);
                if (sang)
                {
                    veQuang(g, ox, y, wO, h, MAU_VANG, 1f, 3);
                }
                veKhungBo(g, ox, y, wO, h,
                        sang ? MAU_CHON : tronMau(MAU_THE, MAU_CA[i], 0.30f),
                        sang ? 0.95f : 0.9f, sang ? MAU_VANG : MAU_VIEN,
                        sang ? 0.95f : 0.5f, 1);
                veChuyenSac(g, ox + 1, y + 1, wO - 2, h - 2, 5);
                if (sang)
                {
                    veVienChay(g, ox, y, wO, h, MAU_VANG);
                }
                veCaSprite(g, fiCaBe, i, ox + wO / 2, y + 13, false, sang);
                // Nen o la tong kem sang, nen chu ve MOT lop mau nau dam la du
                // doc. Ban truoc dung veChuNoi voi lop chinh cung la
                // tahoma_7b_dark: bong den cong chu den chong nhau thanh mot
                // vet dac — dung cai khoi den sau chu "0".
                string s = THUONG_CA[i] > 0 ? ("+" + THUONG_CA[i]) : "0";
                veChuPhang(g, THUONG_CA[i] > 0 ? mFont.tahoma_7b_yellow
                        : mFont.tahoma_7b_dark, s, ox + wO / 2, y + h - 12,
                        mFont.CENTER);
            }
        }

        /// <summary>
        /// Nút: quăng, rồi kéo, rồi giữ.
        /// </summary>
        /// <remarks>
        /// <para>Một nút gánh cả ba việc theo pha, vì cả ba đều là "bấm chỗ này":
        /// <b>QUĂNG</b> lúc rảnh, <b>KÉO!</b> lúc cá cắn, <b>GIỮ!</b> lúc vật lộn.
        /// Ba nút riêng thì hai cái luôn xám, chỉ tổ chiếm chỗ.</para>
        ///
        /// <para>Lúc cá cắn thì nút <b>nhấp nháy</b> và phình ra một chút: đây là
        /// khoảnh khắc duy nhất trong lượt có hạn giờ, nên nó phải giành lấy con
        /// mắt.</para>
        ///
        /// <para>Đang đè thì nút lún xuống và đổi màu theo <b>vạch có trong vùng
        /// an toàn hay không</b> — xanh là đang đúng, đỏ là đang kéo sai và tiến
        /// trình đang tụt. Mắt để ở thanh mà khoé mắt vẫn biết mình đúng hay sai.</para>
        /// </remarks>
        private void veNutGiu(mGraphics g)
        {
            God.MiniGame m = God.MiniGame.gI();
            int[] o = oNutGiu();
            bool doiTien = m.tongThoi < m.ccTienMoi;
            bool dangCho = m.ccGiayCho > 0;
            bool vatLon = (ccPha == CC_VAT_LON);
            bool caCan = (ccPha == CC_CA_CAN);
            bool ngam = (ccPha == CC_NGAM);
            bool dangChay = (ccPha == CC_BAY || ccPha == CC_CHO_CAN
                    || ccPha == CC_KET_QUA);
            bool bamDuoc = vatLon || caCan || ngam
                    || (!doiTien && !dangCho && !dangChay);

            bool nhay = caCan && (mSystem.currentTimeMillis() / 130) % 2 == 0;
            int mauNen;
            if (vatLon && ccDangGiu)
            {
                mauNen = ccVachTrongVung ? MAU_VUNG_TOT : MAU_VUNG_XAU;
            }
            else if (caCan)
            {
                mauNen = nhay ? MAU_VUNG_SANG : MAU_VUNG_TOT;
            }
            else if (ngam)
            {
                mauNen = ccMauTheoTam(ccTamDangNgam());
            }
            else if (vatLon)
            {
                mauNen = MAU_TIEU_DE;
            }
            else
            {
                mauNen = bamDuoc ? MAU_TIEU_DE : rgb(0x8A, 0x8A, 0x8A);
            }

            bool dangDe = (vatLon && ccDangGiu) || ngam
                    || (!vatLon && ccDangDeNut);
            int lun = dangDe ? 3 : 0;
            int caoMat = o[3] - 4;
            int phinh = caCan ? 2 : 0;

            if (bamDuoc)
            {
                veQuang(g, o[0] - phinh, o[1] + lun - phinh, o[2] + phinh * 2,
                        caoMat + phinh * 2, MAU_VANG,
                        (dangDe || nhay) ? 1f : 0.65f, 3);
            }
            // Chan nut lo ra duoi: cho nut co be day, va lun thi chan thu lai.
            g.setColor(MAU_GO_TOI, bamDuoc ? 0.95f : 0.4f);
            g.fillRect(o[0], o[1] + 4, o[2], o[3] - 4, BO_GOC);

            veKhungBo(g, o[0] - phinh, o[1] + lun - phinh, o[2] + phinh * 2,
                    caoMat + phinh * 2, mauNen, bamDuoc ? 1f : 0.55f,
                    bamDuoc ? MAU_VANG : MAU_VIEN, bamDuoc ? 0.9f : 0.4f, 2);
            veChuyenSac(g, o[0] + 2, o[1] + lun + 2, o[2] - 4, caoMat - 4, 6);
            if (dangDe || caCan)
            {
                veVienChay(g, o[0], o[1] + lun, o[2], caoMat, 0xFFFFFF);
            }
            else if (bamDuoc)
            {
                veVetSang(g, o[0], o[1], o[2], caoMat, 2400);
            }

            // Mot nut ganh ca bon buoc, doi chu theo pha. Bon nut rieng thi ba
            // cai luon xam, chi to chiem cho.
            string chu1;
            string chu2;
            if (ngam)
            {
                chu1 = "NHẢ ĐỂ NÉM";
                chu2 = ccTenMucTam(ccTamDangNgam());
            }
            else if (vatLon)
            {
                chu1 = ccDangGiu ? "ĐANG KÉO" : "GIỮ DÂY";
                chu2 = ((int) ccTienTrinh) + "%";
            }
            else if (caCan)
            {
                chu1 = "GIẬT DÂY";
                chu2 = "ngay!";
            }
            else if (doiTien)
            {
                chu1 = "Thiếu mồi";
                chu2 = "";
            }
            else if (ccPha == CC_CHO_CAN)
            {
                chu1 = "Đang chờ";
                chu2 = "cá...";
            }
            else if (dangChay)
            {
                chu1 = "...";
                chu2 = "";
            }
            else if (dangCho)
            {
                chu1 = "Chờ";
                chu2 = m.ccGiayCho + "s";
            }
            else
            {
                chu1 = "QUĂNG CẦN";
                chu2 = "-" + m.ccTienMoi;
            }
            mFont f = bamDuoc ? mFont.tahoma_7b_white : mFont.tahoma_7_grey;
            int yc = o[1] + lun + 7;
            veChuPhang(g, f, chu1, o[0] + o[2] / 2, yc, mFont.CENTER);
            if (chu2.Length > 0)
            {
                veChuPhang(g, (vatLon && ccVachTrongVung) || caCan || ngam
                        ? mFont.tahoma_7b_yellow : f,
                        chu2, o[0] + o[2] / 2, yc + 12, mFont.CENTER);
            }
            if (Main.isPC)
            {
                veChuPhang(g, mFont.tahoma_7_grey, "hoặc SPACE",
                        o[0] + o[2] / 2, yc + 24, mFont.CENTER);
            }
        }

        /// <summary>
        /// Bắt chạm trong màn Câu Cá.
        /// </summary>
        /// <remarks>
        /// <para><b>Không dùng <c>cham()</c></b>: hàm đó nuốt cú chạm ngay lúc bấm
        /// xuống, nên không có cách nào biết ngón còn đang đè — mà pha vật lộn cần
        /// biết điều đó ở từng khung hình.</para>
        ///
        /// <para><b>Quăng và kéo bắn ngay lúc BẤM</b>, không chờ nhả. Bản trước
        /// chờ nhả rồi kiểm lại ngón có còn trong nút — mà <c>isPointerHoldIn</c>
        /// trả về false khi cú nhả đã bị một bảng khác nuốt trước đó, nên cú quăng
        /// không bao giờ bắn. Bắn lúc bấm cũng đúng hơn về cảm giác: hai việc này
        /// cần phản hồi tức thì, nhất là cú kéo có hạn giờ.</para>
        ///
        /// <para>Space cũng bấm được cả hai việc ấy, không chỉ để giữ.</para>
        /// </remarks>
        /// <summary>
        /// Bắt chạm trong màn Câu Cá — <b>toàn bộ bằng cú bấm rời</b>.
        /// </summary>
        /// <remarks>
        /// <para>Dùng <c>cham()</c>, đúng hàm mà dải thẻ và nút đóng đang dùng
        /// tốt. Bản trước tự đọc <c>isPointerDown</c> để nhận biết "đang giữ
        /// ngón" — và cú bấm <b>chưa bao giờ ăn</b>. Tôi soát hết đường gọi mà
        /// không chứng minh được nó hỏng ở đâu, nên thôi không dựa vào cờ ấy
        /// nữa: cái gì đã chạy tốt cho mấy nút kia thì dùng lại.</para>
        ///
        /// <para>Đổi lại phải bỏ cơ chế giữ. Hoá ra thế lại đúng hơn với việc:
        /// <b>giật dây</b> là động tác bấm rời, không phải động tác giữ. Nên cả
        /// bốn bước — quăng, nhấc, giật, rồi kéo — đều là bấm.</para>
        ///
        /// <para>Phím Space làm được đúng những việc ấy, để bản máy tính không
        /// phải rời tay khỏi bàn phím.</para>
        /// </remarks>
        private bool chamCauCa()
        {
            God.MiniGame m = God.MiniGame.gI();
            int[] o = oNutGiu();

            // TU CANH LEN XUONG, khong dung cham().
            //
            // cham() dua vao isPointerJustRelease, ma ham nay gio chay o MOI
            // khung hinh — ke ca luc ngon dang giu. Nen phai tu suy ra cu bam
            // va cu nha tu trang thai "dang giu":
            //   canh len   = khung nay giu, khung truoc khong  -> vua BAM
            //   canh xuong = khung nay khong, khung truoc giu   -> vua NHA
            bool giuTrongNut = ccGiuRaw(o);
            bool vuaBam = giuTrongNut && !ccGiuKhungTruoc;
            bool vuaNha = !giuTrongNut && ccGiuKhungTruoc;
            ccGiuKhungTruoc = giuTrongNut;

            // Pha keo ca dung GIU, va giu O DAU CUNG DUOC trong bang.
            //
            // VI SAO BO PHEP THU VI TRI: ban truoc doi ngon phai nam trong nut,
            // tinh toa do chuot y het cach Main.checkInput lam — va no VAN
            // khong an. Toi soat het duong goi ma khong tim ra cho hong, nen
            // thoi khong doan nua.
            //
            // Trong pha keo thi ca BANG la cai can: giu bat cu dau cung la keo.
            // Tren cam ung day lai la cach dung hon — ngon khong phai canh dung
            // mot o be trong khi mat dang dan vao thanh vach.
            //
            // HOP BA TIN HIEU DOC LAP, chi can mot cai chay la du:
            //   1. Input.GetMouseButton  — doc thang Unity, khong ai xoa duoc
            //   2. GameCanvas.isPointerDown — co chot cua engine
            //   3. phim Space
            // Ba duong khac han nhau ve co che, nen kho ma cung hong.
            //
            // LUON tra ve true: de nhanh dinh tuyen phia ngoai khong goi
            // clearAllPointerEvent() — ham do xoa co chot, va xoa moi khung thi
            // duong so 2 vinh vien khong bao gio bat duoc.
            if (ccPha == CC_VAT_LON)
            {
                ccDangGiu = ccGiuBatKyDau();
                return true;
            }

            switch (ccPha)
            {
                case CC_RANH:
                    // Bat dau giu: mo thanh tam, con tro chay.
                    if (vuaBam && m.ccGiayCho <= 0
                            && m.tongThoi >= m.ccTienMoi)
                    {
                        ccMocNgam = mSystem.currentTimeMillis();
                        ccDaGiuTrongNgam = false;
                        ccVaoPha(CC_NGAM);
                        return true;
                    }
                    return false;

                case CC_NGAM:
                    // Nha tay la nem. Cho 150 mili giay dau de cu bam mo thanh
                    // tam khong bi doc luon la cu nha.
                    if (giuTrongNut)
                    {
                        ccDaGiuTrongNgam = true;
                        return true;
                    }
                    if (vuaNha && ccTrongPha() >= 150)
                    {
                        ccQuangNgay();
                        return true;
                    }
                    return false;

                case CC_CA_CAN:
                    // Giat day: dat moc, vao pha keo.
                    if (vuaBam)
                    {
                        ccMocVatLon = mSystem.currentTimeMillis();
                        ccMocKhungTruoc = ccMocVatLon;
                        ccTienTrinh = m.ccTienTrinhDau;
                        ccViTriVach = 0.5f;
                        ccVaoPha(CC_VAT_LON);
                        return true;
                    }
                    return false;

                default:
                    return false;
            }
        }

        /// <summary>Có đang giữ trong nút ở khung hình trước.</summary>
        /// <remarks>
        /// Giữ để suy ra <i>cú bấm</i> và <i>cú nhả</i> từ <i>trạng thái đang
        /// giữ</i>. Hàm bắt chạm giờ chạy ở mọi khung hình, nên không còn cú
        /// "bấm" hay "nhả" nào được đưa tới sẵn — phải tự tìm cạnh lên và cạnh
        /// xuống. Không có cờ này thì giữ một giây thành ra sáu chục cú bấm.
        /// </remarks>
        private bool ccGiuKhungTruoc;


        private void veMotThe(mGraphics g, int[] o, string chu, bool sang)
        {
            if (sang)
            {
                veQuang(g, o[0], o[1], o[2], o[3], MAU_VANG, 0.7f, 2);
            }
            veKhungBo(g, o[0], o[1], o[2], o[3],
                    sang ? MAU_CHON : MAU_THE, sang ? 1f : 0.9f,
                    MAU_VIEN, sang ? 0.95f : 0.45f, 1);
            if (sang)
            {
                veChuyenSac(g, o[0] + 1, o[1] + 1, o[2] - 2, o[3] - 2, 4);
                // Gach chan sang duoi the dang mo.
                g.setColor(MAU_VANG, 0.9f);
                g.fillRect(o[0] + o[2] / 4, o[1] + o[3] - 2, o[2] / 2, 2, 1);
            }
            (sang ? mFont.tahoma_7b_white : mFont.tahoma_7b_dark)
                    .drawString(g, chu, o[0] + o[2] / 2, o[1] + 4, mFont.CENTER);
        }

        /// <summary>Chỗ giữ sẵn cho hai trò chơi chưa làm.</summary>
        private void veChuaCo(mGraphics g)
        {
            int x = x0 + LE;
            int y = yNoiDung();
            int w = rong - LE * 2;
            int h = y0 + cao - LE - y;
            veKhungBo(g, x - 3, y - 3, w + 6, h + 3, MAU_THE, 0.6f,
                    MAU_VIEN, 0.45f, 1);
            mFont.tahoma_7b_dark.drawString(g, "Sắp có", x + w / 2,
                    y + h / 2 - 14, mFont.CENTER);
            mFont.tahoma_7_grey.drawString(g, "Trò chơi này chưa mở.",
                    x + w / 2, y + h / 2 + 2, mFont.CENTER);
        }

        // ------------------------------------------------------------------
        //  Thẻ Tài Xỉu
        // ------------------------------------------------------------------
        private void veTaiXiu(mGraphics g)
        {
            int giua = x0 + rong / 2;
            int y = yNoiDung();

            mFont.tahoma_7b_dark.drawString(g, "Phiên " + phien, x0 + LE, y,
                    mFont.LEFT);
            // So thoi vang len chung hang voi so phien, o goc phai.
            //
            // Truoc no nam mot minh mot hang duoi day bang, canh mot dong chu
            // nhac viec. Ca hang do la 18 diem chieu cao chi de noi hai thu ma
            // cho khac da noi roi: so thoi thi hang nao cung doc duoc o day, con
            // loi nhac "chon cua truoc" thi chinh o cua da ghi "Bam de chon
            // cua". Bo hang do di, cai dia duoc rong them dung 18 diem.
            //
            // Mot con so thay vi tach khoa/thuong: nguoi choi khong chon duoc
            // tieu loai nao truoc — may chu luon tru khoa truoc roi moi toi
            // thuong — nen tach ra chi la mot con so khong lam gi duoc.
            mFont.tahoma_7_green2.drawString(g, "Thỏi vàng: " + tongThoiVang(),
                    x0 + rong - LE, y, mFont.RIGHT);

            veDongHo(g, giua, y);

            veCua(g, true);
            veCua(g, false);
            veSanXucXac(g, giua, yBan());

            for (int i = 0; i < MUC.Length + 2; i++)
            {
                int[] o = oMuc(i);
                // Khong con trang thai "dang chon" cho tung muc: nay bam la
                // CONG DON vao cua dang chon, khong phai chon mot muc.
                // Chua chon cua thi ca hang xam di, cho biet phai chon cua truoc.
                bool bamDuoc = choDatCuoc() && cuaChon >= 0;
                veKhungBo(g, o[0], o[1], o[2], o[3],
                        MAU_THE, bamDuoc ? 0.95f : 0.4f,
                        MAU_VIEN, bamDuoc ? 0.8f : 0.3f, 1);
                if (bamDuoc)
                {
                    veChuyenSac(g, o[0] + 1, o[1] + 1, o[2] - 2, o[3] - 2, 4);
                }
                string chu = i < MUC.Length ? "+" + MUC[i]
                        : (i == MUC.Length ? "ALL" : "Gõ số");
                mFont mf = bamDuoc ? mFont.tahoma_7b_dark : mFont.tahoma_7_grey;
                mf.drawString(g, chu, o[0] + o[2] / 2, o[1] + 6, mFont.CENTER);
            }

            veDaiLichSu(g, yBan() + coDia() + 38);
        }

        /// <summary>Đồng hồ đếm ngược, nhấp nháy khi sắp hết giờ.</summary>
        private void veDongHo(mGraphics g, int giua, int y)
        {
            int giay = giayHienThi();
            string nhan;
            int mau;
            if (pha == PHA_DAT_CUOC)
            {
                nhan = "ĐẶT CƯỢC  00:" + (giay < 10 ? "0" : "") + giay;
                // Ban SANG cho cac chi tiet nho: ban dam la mau NEN cua o cua,
                // dat nguyen no len mot thanh cao 15px thi doc ra gan nhu den.
                mau = giay <= 5 ? MAU_XIU_SANG : MAU_TAI_SANG;
            }
            else if (pha == PHA_LAC && daMoHan)
            {
                // Da tu mo bat: hien luon ket qua, nhung van kem dong ho —
                // van chua chot, con phai cho het gio chung cua may chu.
                bool tai = ketQuaVan == CUA_TAI;
                nhan = (tai ? "TÀI " : "XỈU ") + tongXucXac()
                        + "  —  00:" + (giay < 10 ? "0" : "") + giay;
                mau = tai ? MAU_TAI_SANG : MAU_XIU_SANG;
            }
            else if (pha == PHA_LAC)
            {
                nhan = "ĐANG XÓC BÁT  00:" + (giay < 10 ? "0" : "") + giay;
                mau = MAU_VANG;
            }
            else
            {
                bool taiKq = ketQuaVan == CUA_TAI;
                nhan = taiKq ? "KẾT QUẢ: TÀI" : "KẾT QUẢ: XỈU";
                mau = taiKq ? MAU_TAI_SANG : MAU_XIU_SANG;
            }

            int w = 150;
            int h = 15;
            int x = giua - w / 2;

            // Nhap nhay o nam giay cuoi: nhip lay theo dong ho may nen may yeu
            // hay khoe deu nhay cung mot toc do.
            float manh = 1f;
            if (pha == PHA_DAT_CUOC && giay <= 5)
            {
                manh = 0.55f + 0.45f * nhip(400);
            }
            veQuang(g, x, y - 2, w, h, mau, manh * 0.8f, 2);
            g.setColor(0x000000, 0.30f);
            g.fillRect(x - 1, y - 1, w + 2, h + 2, BO_GOC);
            g.setColor(mau, manh);
            g.fillRect(x, y - 2, w, h, BO_GOC);
            veChuyenSac(g, x, y - 2, w, h, 4);
            mFont.tahoma_7b_white.drawString(g, nhan, giua, y + 1, mFont.CENTER);

            // Thanh tien do: mot cai vach chay het la het pha. Con so giay van
            // o tren, nhung thanh nay doc duoc bang liec mat.
            int dai = DAI_PHA[pha < 0 || pha > 2 ? 0 : pha];
            float con = dai <= 0 ? 0f : (float) giay / dai;
            if (con > 1f)
            {
                con = 1f;
            }
            int yT = y + h - 1;
            g.setColor(0x000000, 0.35f);
            g.fillRect(x, yT, w, 3, 1);
            g.setColor(0xFFFFFF, 0.85f);
            g.fillRect(x, yT, (int) (w * con), 3, 1);
        }

        private int tongXucXac()
        {
            return xucXac[0] + xucXac[1] + xucXac[2];
        }

        /// <summary>
        /// Người chơi này đã thấy kết quả chưa.
        /// </summary>
        /// <remarks>
        /// <para>Đúng khi máy chủ đã sang pha kết quả, <b>hoặc</b> khi người
        /// chơi tự kéo bát ra sớm. Kéo bát ra là đã nhìn thấy ba con rồi, giấu
        /// tiếp con số tổng và cửa thắng thì chỉ tổ bắt người ta tự cộng.</para>
        ///
        /// <para>Chỉ chi phối phần <b>nhìn thấy</b>. Bảng báo thắng/thua và tiền
        /// nong vẫn chờ gói tin của máy chủ — đó là tiền, không để client tự
        /// quyết. Vòng chơi cũng không đổi: giờ giấc chung cho cả máy chủ.</para>
        /// </remarks>
        private bool daBietKetQua()
        {
            return pha == PHA_KET_QUA || (pha == PHA_LAC && daMoHan);
        }

        /// <summary>Mốc lúc <i>người này</i> thấy kết quả, để chạy hiệu ứng loé.</summary>
        private long mocBietKetQua()
        {
            return (pha == PHA_LAC && daMoHan) ? mocMoHan : mocHienKetQua;
        }

        /// <summary>Nhịp 0..1 rồi 1..0 theo chu kỳ, lấy theo đồng hồ máy.</summary>
        private static float nhip(int chuKy)
        {
            long t = mSystem.currentTimeMillis() % (chuKy * 2);
            return t < chuKy ? (float) t / chuKy : 1f - (float) (t - chuKy) / chuKy;
        }

        private void veCua(mGraphics g, bool tai)
        {
            int[] o = oCua(tai);
            int mau = tai ? MAU_TAI : MAU_XIU;
            int mauSang = tai ? MAU_TAI_SANG : MAU_XIU_SANG;
            bool cuaToi = daDatVanNay() && (cuaCuaToi == CUA_TAI) == tai;
            bool thang = daBietKetQua() && ketQuaVan >= 0
                    && (ketQuaVan == CUA_TAI) == tai;

            bool laCuaDangChon = cuaChon >= 0 && (cuaChon == CUA_TAI) == tai;

            if (thang)
            {
                // Cua thang: quang vang dap theo nhip cho de thay.
                veQuang(g, o[0], o[1], o[2], o[3], MAU_VANG,
                        0.6f + 0.4f * nhip(500), 4);
            }
            else if (cuaToi || laCuaDangChon)
            {
                veQuang(g, o[0], o[1], o[2], o[3], MAU_VANG, 0.5f, 2);
            }

            // Bong do duoi ca o, cho no dung tren nen kem chu khong dan len.
            g.setColor(0x000000, 0.28f);
            g.fillRect(o[0] + 2, o[1] + 4, o[2], o[3], BO_GOC + 2);

            // Vien kep: vong vang ngoai, vong toi trong, roi den than.
            veKhungKep(g, o[0], o[1], o[2], o[3], mau,
                    MAU_VANG, (thang || cuaToi || laCuaDangChon) ? 1f : 0.65f);

            // Dai chuyen sac: sang o tren, dam dan xuong duoi.
            veDaiMau(g, o[0] + 3, o[1] + 3, o[2] - 6, o[3] - 6, mauSang, mau, 14);

            // Vet loe mem o phan tren, cho mat bat duoc nguon sang.
            g.setColor(0xFFFFFF, 0.10f);
            g.fillRect(o[0] + 7, o[1] + 5, o[2] - 14, o[3] / 4, BO_GOC);

            // Chan o toi lai: khoi mau nao cung can mot dau chan.
            g.setColor(0x000000, 0.24f);
            g.fillRect(o[0] + 3, o[1] + o[3] - o[3] / 5, o[2] - 6, o[3] / 5 - 3,
                    BO_GOC);

            veGocKhung(g, o[0], o[1], o[2], o[3],
                    (thang || cuaToi || laCuaDangChon) ? 0.95f : 0.5f);


            // Vet sang truot: chi cho o dang chon hoac o vua thang, de no la
            // dau hieu chu khong phai hoa tiet chung.
            if (laCuaDangChon || thang)
            {
                veVetSang(g, o[0], o[1], o[2], o[3], thang ? 1100 : 2200);
            }
            if (laCuaDangChon && !daDatVanNay())
            {
                veVienChay(g, o[0] + 1, o[1] + 1, o[2] - 2, o[3] - 2, MAU_VANG);
            }
            if (thang)
            {
                veVongNo(g, o[0] + o[2] / 2, o[1] + o[3] / 2,
                        o[2] / 2, o[2] + 30, MAU_VANG, 900);
                veHatSang(g, o[0] + o[2] / 2, o[1] + o[3] / 2, 8,
                        o[2] / 2 + 10, MAU_VANG, 0.85f, 1000);
            }

            // Ba hang trong o cua xep TU TREN XUONG, hang nut neo o DAY o.
            //
            // Ban truoc dong cung moi hang mot con so — 7, 26, 42, 68 va hai
            // hang nut o cao-56, cao-28 — vua khit khi o cua cao 150. Nhung o
            // cua cao bang cai dia, ma dia thi co lai theo cua so, nen tren man
            // hinh thap moi hang de len hang duoi: "11 - 18" de len chu XÁC
            // NHẬN, con HUỶ de len dong "Bam de chon cua".
            //
            // Bo hai nhan khoang diem ("11 - 18", "3 - 10") — luat Tai Xiu ai
            // cung biet — roi gop hai hang nut lam mot. Bay nhieu la du cho de
            // ba hang con lai khong bao gio cham nhau, ke ca o co dia nho nhat.
            int wTam = o[2] - 26;
            int yTen = o[1] + 5;
            veTamChim(g, o[0] + 13, yTen, wTam, 18, 0.8f);
            veChuNoi(g, mFont.tahoma_7b_yellow, tai ? "TÀI" : "XỈU",
                    o[0] + o[2] / 2, yTen + 3, mFont.CENTER);

            // Tam tien: cung kieu tam chim, de hai tam doc ra la mot bo.
            int yTong = yTen + 21;
            veTamChim(g, o[0] + 13, yTong, wTam, 20, 0.65f);
            // Can giua CA CUM icon + con so trong tam nen, khong phai can rieng
            // con so roi nhet icon vao mep trai — lam vay thi cum lech han sang
            // phai, ma con so cang dai lech cang ro.
            string soTien = "" + (tai ? tongTai : tongXiu);
            int wSo = mFont.tahoma_7b_yellow.getWidth(soTien);
            int wCum = 16 + 4 + wSo;
            int xCum = o[0] + o[2] / 2 - wCum / 2;
            SmallImage.drawSmallImage(g, ICON_THOI_VANG, xCum + 8, yTong + 10,
                    0, mGraphics.VCENTER | mGraphics.HCENTER);
            veChuNoi(g, mFont.tahoma_7b_yellow, soTien,
                    xCum + 16 + 4, yTong + 6, mFont.LEFT);

            // Dong so dang gom cho cua nay.
            bool laCuaChon = cuaChon >= 0 && (cuaChon == CUA_TAI) == tai;
            bool daDatCuaNay = daDatVanNay() && (cuaCuaToi == CUA_TAI) == tai;
            string dongGom;
            if (daDatCuaNay)
            {
                dongGom = "Đã đặt " + soThoiCuaToi;
            }
            else if (laCuaChon)
            {
                dongGom = "Đang gom " + tienCho;
            }
            else
            {
                dongGom = "Bấm để chọn cửa";
            }
            // Dong nay an vao giua khoang trong con lai — khoang trong bao
            // nhieu cung duoc, ke ca chi vua du mot dong.
            int dinhNut = oNutDat(tai)[1];
            int yGom = (yTong + 20 + dinhNut) / 2 - 5;
            if (yGom < yTong + 21)
            {
                yGom = yTong + 21;
            }
            veChuNoi(g,
                    laCuaChon || daDatCuaNay ? mFont.tahoma_7b_yellow
                            : mFont.tahoma_7_white,
                    dongGom, o[0] + o[2] / 2, yGom, mFont.CENTER);

            veNutXacNhan(g, tai, laCuaChon);
            veNutHuy(g, tai, laCuaChon);
        }

        /// <summary>
        /// Nút XÁC NHẬN của một cửa — bấm là gói tin rời máy.
        /// </summary>
        /// <remarks>
        /// Chỉ sáng khi cửa này đang được chọn <b>và</b> đã gom được số dương.
        /// Xác nhận một số 0 thì máy chủ cũng bỏ qua, nhưng để nút sáng lên rồi
        /// bấm không thấy gì xảy ra là kiểu hỏng khó chịu nhất.
        /// </remarks>
        private void veNutXacNhan(mGraphics g, bool tai, bool laCuaChon)
        {
            int[] n = oNutDat(tai);
            bool bamDuoc = choDatCuoc() && laCuaChon && tienCho > 0;
            if (bamDuoc)
            {
                // Dap nhip khi da san sang: mat bat vao dung cai nut can bam.
                veQuang(g, n[0], n[1], n[2], n[3], MAU_VANG,
                        0.45f + 0.55f * nhip(700), 3);
            }
            veNenNut(g, n, bamDuoc, MAU_VANG);
            string nhan;
            if (daDatVanNay())
            {
                nhan = "ĐÃ CHỐT";
            }
            else if (pha != PHA_DAT_CUOC)
            {
                nhan = "HẾT GIỜ";
            }
            else
            {
                nhan = bamDuoc ? "XÁC NHẬN " + tienCho : "XÁC NHẬN";
            }
            (bamDuoc ? mFont.tahoma_7b_dark : mFont.tahoma_7_grey)
                    .drawString(g, nhan, n[0] + n[2] / 2, n[1] + 6, mFont.CENTER);
        }

        /// <summary>
        /// Nền một nút trong ô cửa.
        /// </summary>
        /// <remarks>
        /// Tắt thì <b>tối và trong</b> chứ không xám nhạt. Ô cửa nền màu đậm, đặt
        /// một mảng xám nhạt lên đó thì nút tắt lại nổi hơn nút bật — ngược hẳn
        /// điều cần nói.
        /// </remarks>
        private static void veNenNut(mGraphics g, int[] n, bool bat, int mau)
        {
            if (bat)
            {
                g.setColor(0x000000, 0.35f);
                g.fillRect(n[0] + 1, n[1] + 2, n[2], n[3], 5);
                g.setColor(0x6B4A18, 1f);
                g.fillRect(n[0], n[1], n[2], n[3], 5);
                g.setColor(mau, 1f);
                g.fillRect(n[0] + 1, n[1] + 1, n[2] - 2, n[3] - 2, 4);
                veDaiMau(g, n[0] + 1, n[1] + 1, n[2] - 2, n[3] - 2,
                        tronMau(mau, 0xFFFFFF, 0.45f), mau, 6);
                return;
            }
            g.setColor(0x000000, 0.30f);
            g.fillRect(n[0], n[1], n[2], n[3], 5);
            g.setColor(0xFFFFFF, 0.10f);
            g.fillRect(n[0] + 1, n[1] + 1, n[2] - 2, n[3] - 2, 4);
        }

        /// <summary>
        /// Nút HUỶ của một cửa — xoá số đang gom.
        /// </summary>
        /// <remarks>
        /// Chỉ dùng được <b>trước khi xác nhận</b>. Xác nhận rồi thì tiền đã ra
        /// khỏi hành trang và nằm trong ván đang chạy; huỷ được lúc đó thì
        /// người ta chỉ việc chờ tới lúc gần mở bát rồi rút cược nào đang thua.
        /// </remarks>
        private void veNutHuy(mGraphics g, bool tai, bool laCuaChon)
        {
            int[] n = oNutHuy(tai);
            bool bamDuoc = choDatCuoc() && laCuaChon && tienCho > 0;
            veNenNut(g, n, bamDuoc, rgb(0xE0, 0x6A, 0x4A));
            (bamDuoc ? mFont.tahoma_7b_dark : mFont.tahoma_7_grey)
                    .drawString(g, "HUỶ", n[0] + n[2] / 2, n[1] + 6, mFont.CENTER);
        }

        // ------------------------------------------------------------------
        //  Đĩa và bát — góc nhìn từ trên xuống
        // ------------------------------------------------------------------

        /// <summary>Đường kính cái đĩa khi có đủ chỗ.</summary>
        private const int CO_DIA_TOI_DA = 150;

        /// <summary>Đĩa nhỏ hơn mức này thì ba con xúc xắc chồng lên nhau.</summary>
        private const int CO_DIA_TOI_THIEU = 84;

        /// <summary>
        /// Đường kính cái đĩa — <b>co lại khi bảng không đủ cao</b>.
        /// </summary>
        /// <remarks>
        /// <para>Mọi thứ trong thẻ Tài Xỉu xếp từ trên xuống theo con số cố định:
        /// đĩa 150, rồi 38 điểm tới dòng cược, rồi 18 nữa tới dải lịch sử. Cộng
        /// lại vừa khít chiều cao tối đa 330 của bảng — nên cửa sổ thấp hơn một
        /// chút là phần đuôi tụt hẳn ra ngoài đáy bảng và bị cắt.</para>
        ///
        /// <para>Tính ngược từ chỗ trống thật thì đĩa tự nhỏ lại và cả cái đuôi
        /// luôn nằm trong bảng, dù cửa sổ cao bao nhiêu.</para>
        /// </remarks>
        private int coDia()
        {
            // Tru het nhung gi co dinh nam duoi cai dia: 8 diem khe, hang muc
            // cuoc cao 22, 8 diem khe nua, roi dai lich su cao 18.
            int con = (y0 + cao - LE) - yNoiDung() - 20 - 8 - 22 - 8 - 18;
            if (con > CO_DIA_TOI_DA)
            {
                con = CO_DIA_TOI_DA;
            }
            if (con < CO_DIA_TOI_THIEU)
            {
                con = CO_DIA_TOI_THIEU;
            }
            return con;
        }

        /// <summary>
        /// Đường kính cái bát úp trên đĩa — <b>luôn nhỏ hơn đĩa một vành</b>.
        /// </summary>
        /// <remarks>
        /// Trước đây là hằng số 126, vừa với cái đĩa 150. Từ lúc đĩa biết co lại
        /// theo cửa sổ thì cái bát giữ nguyên 126 hoá ra <i>to hơn cả đĩa</i>:
        /// nó trùm lên đồng hồ đếm ngược ở trên và hàng mức cược ở dưới, đúng
        /// cái vòng tròn to tướng đè lên nửa bảng.
        /// </remarks>
        private int coBat()
        {
            // Chi hep hon dia 8 diem — vua du de con thay vanh vang cua dia.
            //
            // Ban truoc hep hon 24 diem, nen hai vong TOI cua long dia
            // (coDia-12 mau 0x6B451E va coDia-18 mau 0x3B2A1B) lo ra thanh mot
            // vanh gan nhu den bao quanh cai bat. Va vi bat nho hon vung xuc
            // xac o cac co dia lon, ba con xuc xac tho ra ngoai mep bat.
            return coDia() - 8;
        }

        /// <summary>
        /// Cạnh một con xúc xắc — cũng co theo đĩa.
        /// </summary>
        /// <remarks>
        /// Cùng một lỗi với cái bát: cạnh 40 cố định trong khi đĩa co lại thì ba
        /// con lòi ra ngoài lòng đĩa và bị <c>setClip</c> xén cụt.
        /// </remarks>
        private int canhXX()
        {
            int c = coDia() * 40 / CO_DIA_TOI_DA;
            return c < 22 ? 22 : c;
        }

        /// <summary>Bán kính tam giác ba con xúc xắc.</summary>
        /// <remarks>
        /// Kẹp lại sao cho mép ngoài con xúc xắc vẫn nằm trong lòng đĩa — lòng
        /// đĩa là <c>coDia() - 18</c>, xem <c>veDia</c>.
        /// </remarks>
        private int banKinhXucXac()
        {
            int r = coDia() * 32 / CO_DIA_TOI_DA;
            int toiDa = (coDia() - 18 - canhXX()) / 2;
            return r > toiDa ? toiDa : r;
        }

        /// <summary>Giây xóc trước khi cho nặn. Phần còn lại của pha là để nặn.</summary>
        private const float GIAY_XOC = 1f;

        /// <summary>
        /// Giây của pha xóc bát. <b>Phải khớp <c>TaiXiuManager.GIAY_LAC</c></b>
        /// bên máy chủ — số này dùng để biết đã tới lúc cho nặn chưa.
        /// </summary>
        private const float GIAY_LAC = 11f;

        /// <summary>Kéo tay được xa nhất bao nhiêu pixel.</summary>
        private const int NAN_XA_NHAT = 104;

        /// <summary>
        /// Kéo quá đây là bát <b>bật hẳn ra</b>, không úp lại nữa.
        /// </summary>
        /// <remarks>
        /// Đặt thấp hơn <see cref="NAN_XA_NHAT"/> để còn quãng nặn hé mà chưa
        /// mở: kéo ít thì buông ra bát trôi về, kéo quá ngưỡng này mới là quyết
        /// định mở.
        /// </remarks>
        private const int NGUONG_MO = 68;

        /// <summary>Bát bay ra xa bao nhiêu rồi mất hẳn.</summary>
        private const int BAY_XA = 210;

        /// <summary>Bát trượt ra hết trong bao nhiêu giây.</summary>
        /// <remarks>
        /// Cố tình chậm. Bát trượt nhanh thì ba con xúc xắc hiện ra cùng một
        /// lúc, mất hẳn cái khoảng nín thở lúc chúng lộ dần từng con.
        /// </remarks>
        private const float GIAY_BAY = 1.7f;

        /// <summary>Độ lệch hiện tại của bát so với tâm đĩa.</summary>
        private float nanX;
        private float nanY;

        /// <summary>Đang giữ ngón/chuột để nặn.</summary>
        private bool dangNan;

        /// <summary>Phải giữ bát bao nhiêu mili giây rồi mới kéo được.</summary>
        /// <remarks>
        /// Có quãng giữ này thì động tác thành "đặt tay lên bát rồi mới nhấc",
        /// chứ chạm phát nào bát chạy phát đó thì vừa dễ mở nhầm vừa mất cái
        /// nhịp chần chừ vốn là phần hay nhất của việc nặn.
        /// </remarks>
        private const int MS_GIU_TRUOC_KHI_KEO = 150;

        /// <summary>Mốc lúc đặt tay lên bát, 0 là chưa đặt.</summary>
        private long mocBamBat;

        /// <summary>Vị trí con trỏ lúc bắt đầu kéo.</summary>
        /// <remarks>
        /// Kéo tính theo <b>độ dời</b> so với điểm này chứ không gán thẳng tâm
        /// bát vào con trỏ. Gán thẳng thì bấm ở mép bát xong vừa đủ giờ giữ là
        /// cả cái bát giật một cái cho tâm nó chui vào con trỏ — nhìn như bát
        /// bị hút vào chuột. Tính theo độ dời thì bấm chỗ nào chỗ đó đi theo
        /// tay, đúng như cầm một vật thật.
        /// </remarks>
        private int bamX;
        private int bamY;

        /// <summary>Độ lệch của bát ngay lúc bắt đầu kéo.</summary>
        private float nanGocX;
        private float nanGocY;

        /// <summary>Chỗ bát đang chạy tới. Bát bám theo đây chứ không nhảy thẳng.</summary>
        private float dichX;
        private float dichY;

        /// <summary>
        /// Đã kéo bát ra hẳn — ván này khỏi úp lại.
        /// </summary>
        /// <remarks>
        /// Chốt lại chứ không cho úp lại: người chơi đã nhìn thấy ba con rồi,
        /// úp bát lại chỉ là che mắt chính mình. Cờ này chỉ đổi cái người chơi
        /// <b>nhìn thấy</b>, không đụng gì tới vòng chơi — giờ mở bát vẫn do
        /// máy chủ quyết và chung cho tất cả.
        /// </remarks>
        private bool daMoHan;

        /// <summary>Hướng bát bay ra, lấy lúc vừa quá ngưỡng.</summary>
        private float huongMoX;
        private float huongMoY;

        /// <summary>Mốc lúc bát bắt đầu bay ra.</summary>
        private long mocMoHan;

        /// <summary>Mốc thời gian khung hình trước, để chuyển động theo giây thật.</summary>
        private long mocKhungTruoc;

        /// <summary>
        /// Toạ độ ba con xúc xắc, dựng sẵn để hàm vẽ không cấp phát gì.
        /// </summary>
        /// <remarks>
        /// Bản trước tạo mới một mảng hai chiều <b>mỗi khung hình</b>. Sáu chục
        /// khung một giây là sáu chục mảng rác, và Unity dọn rác thì khung hình
        /// khựng một cái — đúng thứ phá cảm giác mượt mà mọi thứ khác đang cố
        /// tạo ra.
        /// </remarks>
        private readonly int[] xDinh = new int[3];
        private readonly int[] yDinh = new int[3];

        /// <summary>
        /// Vẽ một hình tròn.
        /// </summary>
        /// <remarks>
        /// <c>mGraphics</c> không có hàm vẽ hình tròn, nhưng <c>fillRect</c> đẩy
        /// thẳng bán kính bo góc xuống <c>GUI.DrawTexture</c> của Unity, mà Unity
        /// kẹp bán kính lại ở nửa cạnh nhỏ nhất. Nên một ô <b>vuông</b> với bán
        /// kính bằng nửa cạnh ra đúng một hình tròn — một lời gọi vẽ, không phải
        /// ghép hàng trăm dải ngang.
        /// </remarks>
        private static void veTron(mGraphics g, int tamX, int tamY, int co,
                int mau, float alpha)
        {
            if (co <= 0)
            {
                return;
            }
            g.setColor(mau, alpha);
            g.fillRect(tamX - co / 2, tamY - co / 2, co, co, co / 2);
        }

        /// <summary>Tâm đĩa trên màn hình.</summary>
        private int tamDiaX()
        {
            return x0 + rong / 2;
        }

        private int tamDiaY(int yKhung)
        {
            return yKhung + coDia() / 2;
        }

        /// <summary>
        /// Đĩa nhìn từ trên xuống, ba con xúc xắc, và cái bát úp lên.
        /// </summary>
        /// <remarks>
        /// <para><b>Thứ tự vẽ là đĩa → xúc xắc → bát.</b> Bát vẽ sau cùng nên nó
        /// che xúc xắc; nặn bát lệch sang bên nào thì hở ra bên đó.</para>
        ///
        /// <para>Vùng vẽ cắt theo <b>ô vuông ngoại tiếp đĩa</b> chứ không cắt
        /// tròn — <c>setClip</c> chỉ nhận hình chữ nhật. Không sao vì đĩa và bát
        /// đều nằm gọn trong ô đó; cắt chỉ để xúc xắc không văng ra ngoài.</para>
        /// </remarks>
        private void veSanXucXac(mGraphics g, int giua, int y)
        {
            capNhatNan();

            int tx = tamDiaX();
            int ty = tamDiaY(y);

            veDia(g, tx, ty);

            g.setClip(tx - coDia() / 2, ty - coDia() / 2, coDia(), coDia());
            veBaXucXac(g, tx, ty);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            veCaiBat(g, tx, ty);

            // Hat sang bung ra dung luc bat vua bat khoi dia.
            if (daMoHan)
            {
                long troiMo = mSystem.currentTimeMillis() - mocMoHan;
                if (troiMo < 700)
                {
                    veHatSang(g, tx, ty, 10, coDia() / 2 + 16, MAU_VANG,
                            0.9f * (1f - troiMo / 700f), 700);
                }
            }

            if (daBietKetQua())
            {
                long troi = mSystem.currentTimeMillis() - mocBietKetQua();
                if (troi >= 0 && troi < 450)
                {
                    veTron(g, tx, ty, coDia(), 0xFFFFFF, 0.5f * (1f - troi / 450f));
                }
                int wT = 74;
                g.setColor(0x000000, 0.55f);
                g.fillRect(tx - wT / 2, ty + coDia() / 2 - 20, wT, 16, 8);
                mFont.tahoma_7b_yellow.drawString(g, "Tổng " + tongXucXac(), tx,
                        ty + coDia() / 2 - 17, mFont.CENTER);
            }

        }

        /// <summary>
        /// Đĩa gỗ nhìn từ trên: các vòng đồng tâm, vành có khía, lòng lõm.
        /// </summary>
        /// <remarks>
        /// Khía vành xếp bằng sin/cos quanh một vòng tròn. Mười sáu khía là đủ
        /// để mắt thấy "cái vành có hoa văn" mà không thành lởm chởm.
        /// </remarks>
        private void veDia(mGraphics g, int tx, int ty)
        {
            veTron(g, tx + 3, ty + 4, coDia() + 4, 0x000000, 0.32f);
            veTron(g, tx, ty, coDia(), MAU_VANG, 0.95f);
            veTron(g, tx, ty, coDia() - 3, rgb(0xC8, 0x93, 0x3C), 1f);

            // Khia quanh vanh.
            int rKhia = (coDia() - 9) / 2;
            for (int i = 0; i < 16; i++)
            {
                float goc = 6.2832f * i / 16f;
                int kx = tx + (int) (UnityEngine.Mathf.Cos(goc) * rKhia);
                int ky = ty + (int) (UnityEngine.Mathf.Sin(goc) * rKhia);
                g.setColor(0x000000, 0.20f);
                g.fillRect(kx - 2, ky - 2, 4, 4, 2);
            }

            veTron(g, tx, ty, coDia() - 12, rgb(0x6B, 0x45, 0x1E), 1f);
            veTron(g, tx, ty, coDia() - 18, MAU_SAN, 1f);
            // Long dia lom: vien trong toi o tren, sang o duoi — nguoc voi mat
            // loi, va do chinh la cai lam no doc ra thanh cai long chao.
            veTron(g, tx, ty - 2, coDia() - 22, 0x000000, 0.22f);
            veTron(g, tx, ty + 3, coDia() - 26, 0xFFFFFF, 0.05f);
            veTron(g, tx - 9, ty - 10, coDia() - 44, 0xFFFFFF, 0.045f);
        }

        private void veBaXucXac(mGraphics g, int tx, int ty)
        {
            int banKinh = banKinhXucXac();
            int canh = canhXX();
            // Ba dinh tam giac: mot tren, hai duoi.
            xDinh[0] = tx;
            yDinh[0] = ty - banKinh + 2;
            xDinh[1] = tx - banKinh - 1;
            yDinh[1] = ty + banKinh - canh / 4;
            xDinh[2] = tx + banKinh + 1;
            yDinh[2] = ty + banKinh - canh / 4;

            bool rung = pha == PHA_LAC && !choNan();
            for (int i = 0; i < 3; i++)
            {
                int dx = 0;
                int dy = 0;
                int mat = xucXac[i];
                if (mat < 1 || mat > 6)
                {
                    mat = 1;
                }
                if (rung)
                {
                    // Bat con up kin nen chang ai thay mat nao — doi mat lien tuc
                    // va nay quanh cho, lech pha ba con cho khoi nhu mot khoi.
                    long t = mSystem.currentTimeMillis();
                    mat = (int) (((t / 70) + i * 3) % 6) + 1;
                    dx = (int) ((nhip(250 + i * 40) - 0.5f) * 13);
                    dy = (int) ((nhip(180 + i * 55) - 0.5f) * 13);

                    // Hai bong mo o vi tri vai phan nghin giay TRUOC: mat ghep
                    // lai thanh vet nhoe, ra cam giac dang van that nhanh.
                    for (int b = 1; b <= 2; b++)
                    {
                        int bx = (int) ((nhipLui(250 + i * 40, b * 55) - 0.5f) * 13);
                        int by = (int) ((nhipLui(180 + i * 55, b * 55) - 0.5f) * 13);
                        veBongXucXac(g, xDinh[i] - canh / 2 + bx,
                                yDinh[i] - canh / 2 + by, canh, 0.20f / b);
                    }
                }
                veMotXucXac(g, xDinh[i] - canh / 2 + dx,
                        yDinh[i] - canh / 2 + dy, canh, mat, rung);
            }
        }

        /// <summary>
        /// Cái bát úp nhìn từ trên xuống.
        /// </summary>
        /// <remarks>
        /// Nhìn từ trên thì bát là một khối vòm tròn: vành ngoài tối, càng vào
        /// giữa càng sáng, cộng một vệt sáng lệch và cái núm ở đỉnh. Bốn vòng
        /// tròn đồng tâm là đủ để mắt đọc ra khối cầu.
        /// </remarks>
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

            // Rung khi con up kin.
            if (pha == PHA_LAC && !choNan())
            {
                tx += (int) ((nhip(105) - 0.5f) * 8);
                ty += (int) ((nhip(85) - 0.5f) * 8);
            }

            // Da bo bong do den duoi cai bat.
            //
            // Bat gio gan bang ca cai dia, nen vong bong lech ra khong con roi
            // len mat dia nua ma tho han ra ngoai vanh vang — doc ra thanh mot
            // vien den bao quanh, chu khong ra bong.

            // Vom bat: cang vao giua cang sang, bon vong la du de mat doc ra
            // khoi cau. Vanh ngoai cung mau vang lam nep.
            veTron(g, tx, ty, coBat(), MAU_VANG, 0.75f * ro);
            veTron(g, tx, ty, coBat() - 3, rgb(0x5E, 0x37, 0x18), ro);
            veTron(g, tx, ty, coBat() - 9, rgb(0x8E, 0x58, 0x28), ro);
            veTron(g, tx, ty, coBat() - 28, rgb(0xA8, 0x6C, 0x33), ro);
            veTron(g, tx, ty, coBat() - 54, rgb(0xBE, 0x7E, 0x3E), ro);

            // Vet men bong lech len goc trai — mat can mot huong sang de doc
            // ra khoi tron, du dang nhin tu tren xuong.
            veTron(g, tx - coBat() / 5, ty - coBat() / 5, coBat() / 3, 0xFFFFFF, 0.16f * ro);

            // Vet long lanh chay vong quanh vanh bat: mot cham sang di theo
            // duong tron, cho men bat co ve uot chu khong phang li.
            float gocL = (mSystem.currentTimeMillis() % 2600) / 2600f * 6.2832f;
            int rL = (coBat() - 16) / 2;
            int lx = tx + (int) (UnityEngine.Mathf.Cos(gocL) * rL);
            int ly = ty + (int) (UnityEngine.Mathf.Sin(gocL) * rL);
            veTron(g, lx, ly, 12, 0xFFFFFF, 0.20f * ro);
            veTron(g, lx, ly, 5, 0xFFFFFF, 0.35f * ro);

            // Num o dinh bat.
            veTron(g, tx, ty, 15, 0x000000, 0.18f * ro);
            veTron(g, tx, ty, 13, rgb(0xD8, 0x9B, 0x4E), ro);
            veTron(g, tx, ty, 6, MAU_VANG, 0.95f * ro);
            veTron(g, tx - 2, ty - 2, 3, 0xFFFFFF, 0.7f * ro);

            // Anh sang hat ra tu duoi vanh bat khi da nhac len khoi tam.
            float xa = UnityEngine.Mathf.Sqrt(nanX * nanX + nanY * nanY);
            if (xa > 4f)
            {
                float m = xa / NGUONG_MO;
                if (m > 1f)
                {
                    m = 1f;
                }
                veTron(g, txDia, tyDia, coBat() + 16, MAU_VANG, 0.16f * m * ro);
            }

            if (pha == PHA_LAC && !choNan())
            {
                // Bui bay quanh mieng bat khi dang xoc.
                veHatSang(g, tx, ty, 6, coBat() / 2 + 6, MAU_VANG, 0.35f, 700);
                mFont.tahoma_7b_yellow.drawString(g, "XÓC...", tx, ty - 5,
                        mFont.CENTER);
            }
            else if (choNan() && !daMoHan)
            {
                // Vong sang nhac rang cai bat nay keo duoc.
                veVongNo(g, tx, ty, coBat() - 10, coBat() + 22, MAU_VANG, 1400);
            }
        }

        /// <summary>Đã tới quãng cho người chơi nặn chưa.</summary>
        private bool choNan()
        {
            if (pha != PHA_LAC)
            {
                return false;
            }
            // giayConLai dem NGUOC nen lay bu ra phan da troi qua.
            return GIAY_LAC - giayHienThi() >= GIAY_XOC;
        }

        /// <summary>
        /// Cập nhật độ lệch của bát theo ngón tay, và cho nó trôi về khi buông.
        /// </summary>
        /// <remarks>
        /// <para><b>Nặn được mọi hướng</b>: độ lệch là một véc-tơ tự do, chỉ bị
        /// kẹp lại ở <see cref="NAN_XA_NHAT"/> pixel tính từ tâm — kéo hướng nào
        /// cũng hở ra hướng đó.</para>
        ///
        /// <para><b>Trôi về theo giây thật, không theo khung hình.</b> Nhân với
        /// số giây đã trôi giữa hai khung nên máy chạy 30 hay 120 khung mỗi giây
        /// đều thấy bát về đúng một tốc độ.</para>
        /// </remarks>
        private void capNhatNan()
        {
            long gio = mSystem.currentTimeMillis();
            float dt = (gio - mocKhungTruoc) / 1000f;
            mocKhungTruoc = gio;
            // Khung dau tien, hoac vua doi the ve: khong nhay mot buoc dai.
            if (dt <= 0f || dt > 0.25f)
            {
                dt = 0.016f;
            }

            if (daMoHan)
            {
                // Bay thang ra theo huong da chot, cham dan o cuoi.
                float t = (gio - mocMoHan) / 1000f / GIAY_BAY;
                if (t > 1f)
                {
                    t = 1f;
                }
                // Cham dan tu tu roi moi vut di, khong phai vut ngay.
                //
                // Duong cong binh phuong: nua dau chi di duoc mot phan tu quang
                // duong, nen xuc xac lo ra tung con mot. Dung duong cong nguoc
                // (nhanh truoc cham sau) thi bat bien mat gan het ngay trong ba
                // phan muoi giay dau — het hoi hop.
                float e = t * t;
                nanX = huongMoX * (NGUONG_MO + (BAY_XA - NGUONG_MO) * e);
                nanY = huongMoY * (NGUONG_MO + (BAY_XA - NGUONG_MO) * e);
                return;
            }

            if (dangNan && choNan())
            {
                // Chay toi dich thay vi nhay thang vao do.
                //
                // He so mu de toc do khong phu thuoc so khung hinh: may 30 hay
                // 144 khung mot giay deu di het chung mot quang trong cung mot
                // khoang thoi gian. Dat ~90% quang duong trong khoang mot phan
                // tam giay — du nhanh de bam sat tay, du cham de bo cai giat luc
                // bat dau va loc rung tay.
                float k = 1f - UnityEngine.Mathf.Pow(0.000000001f, dt);
                nanX += (dichX - nanX) * k;
                nanY += (dichY - nanY) * k;
                return;
            }

            // Troi ve tam. He so mu cho toc do doc lap voi so khung hinh.
            float con = UnityEngine.Mathf.Pow(0.0025f, dt);
            nanX *= con;
            nanY *= con;
            dichX = nanX;
            dichY = nanY;
            if (UnityEngine.Mathf.Abs(nanX) < 0.4f)
            {
                nanX = 0f;
            }
            if (UnityEngine.Mathf.Abs(nanY) < 0.4f)
            {
                nanY = 0f;
            }
        }

        /// <summary>Bát đã mờ đi bao nhiêu khi đang bay ra. 1 là còn rõ.</summary>
        private float doRoCuaBat()
        {
            if (!daMoHan)
            {
                return 1f;
            }
            float t = (mSystem.currentTimeMillis() - mocMoHan) / 1000f / GIAY_BAY;
            // Mo dan chu khong cat dut: bat truot ra khoi dia thi no de len hai
            // o cua ben canh, mo di thi quang de len do khong con cham mat.
            //
            // Chi bat dau mo o quang SAU. Mo som thi bat trong suot trong khi
            // van con dang che xuc xac — nhin ra ngay la mot lop ve, khong con
            // giong cai bat that dang duoc keo di.
            if (t < 0.55f)
            {
                return 1f;
            }
            float a = 1f - (t - 0.55f) / 0.45f;
            return a < 0f ? 0f : a;
        }

        /// <summary>Xoá trạng thái nặn khi sang ván mới.</summary>
        private void datLaiNan()
        {
            daMoHan = false;
            dangNan = false;
            mocBamBat = 0L;
            nanX = 0f;
            nanY = 0f;
            dichX = 0f;
            dichY = 0f;
        }

        /// <summary>
        /// Đặt chỗ bát cần chạy tới, theo độ dời của ngón tay.
        /// </summary>
        /// <remarks>
        /// <para>Lấy <b>độ dời</b> so với điểm bấm chứ không gán tâm bát vào con
        /// trỏ: bấm ở mép thì mép đi theo tay, bấm ở đỉnh thì đỉnh đi theo tay.
        /// Gán thẳng thì bát luôn giật một cái lúc bắt đầu kéo.</para>
        ///
        /// <para>Chỉ đặt <b>đích</b>, không gán thẳng vị trí — <c>capNhatNan</c>
        /// cho bát chạy tới đó, vừa mượt vừa lọc rung tay.</para>
        /// </remarks>
        private void keoBatTheoNgon(int px, int py, int yKhung)
        {
            float dx = nanGocX + (px - bamX);
            float dy = nanGocY + (py - bamY);
            float dai = UnityEngine.Mathf.Sqrt(dx * dx + dy * dy);
            if (dai > NAN_XA_NHAT)
            {
                dx = dx / dai * NAN_XA_NHAT;
                dy = dy / dai * NAN_XA_NHAT;
            }
            dichX = dx;
            dichY = dy;

            // Qua nguong thi chot: bat bay han ra theo dung huong dang keo.
            if (dai >= NGUONG_MO && !daMoHan)
            {
                daMoHan = true;
                dangNan = false;
                mocMoHan = mSystem.currentTimeMillis();
                huongMoX = dx / dai;
                huongMoY = dy / dai;
            }
        }

        /// <summary>Nhịp như <c>nhip</c> nhưng lùi lại <paramref name="lui"/> mili giây.</summary>
        private static float nhipLui(int chuKy, int lui)
        {
            long t = (mSystem.currentTimeMillis() - lui) % (chuKy * 2);
            if (t < 0)
            {
                t += chuKy * 2;
            }
            return t < chuKy ? (float) t / chuKy : 1f - (float) (t - chuKy) / chuKy;
        }

        /// <summary>Bóng mờ của con xúc xắc, dùng làm vệt nhoè khi đang lăn.</summary>
        private static void veBongXucXac(mGraphics g, int x, int y, int canh,
                float manh)
        {
            g.setColor(0xF7F3EA, manh);
            g.fillRect(x, y, canh, canh, 7);
        }

        /// <summary>Một con xúc xắc: khối trắng bo góc, chấm đỏ.</summary>
        private static void veMotXucXac(mGraphics g, int x, int y, int canh,
                int mat, bool dangRung)
        {
            // Bong do tren mat dia.
            g.setColor(0x000000, 0.32f);
            g.fillRect(x + 2, y + 4, canh, canh, 8);

            // Vien toi lam duong tach khoi nen dia.
            g.setColor(0x2A211A, 0.92f);
            g.fillRect(x, y, canh, canh, 8);

            // Than nga, roi hai lop sang/toi lech nhau cho ra mat vat.
            g.setColor(0xEFE8DA, 1f);
            g.fillRect(x + 2, y + 2, canh - 4, canh - 4, 7);
            g.setColor(0xFFFFFF, 0.85f);
            g.fillRect(x + 3, y + 3, canh - 8, canh - 8, 6);
            g.setColor(0x000000, 0.07f);
            g.fillRect(x + 6, y + 6, canh - 8, canh - 8, 6);

            // Vet loe o goc tren trai — nguon sang chung cua ca bang.
            g.setColor(0xFFFFFF, 0.55f);
            g.fillRect(x + 4, y + 4, (canh - 8) / 2, (canh - 8) / 3, 4);

            veChamXucXac(g, x, y, canh, mat);
        }

        /// <summary>
        /// Các chấm của một mặt xúc xắc.
        /// </summary>
        /// <remarks>
        /// Toạ độ chấm ghi theo lưới 3x3 rồi mới quy ra pixel, nên đổi cỡ con
        /// xúc xắc không phải tính lại từng chấm.
        /// </remarks>
        private static void veChamXucXac(mGraphics g, int x, int y, int canh, int mat)
        {
            if (mat < 1 || mat > 6)
            {
                return;
            }
            int[][] luoi;
            switch (mat)
            {
                case 1: luoi = MAT_1; break;
                case 2: luoi = MAT_2; break;
                case 3: luoi = MAT_3; break;
                case 4: luoi = MAT_4; break;
                case 5: luoi = MAT_5; break;
                default: luoi = MAT_6; break;
            }
            int coCham = 6;
            int le = 6;
            int buoc = (canh - le * 2 - coCham) / 2;
            for (int k = 0; k < luoi.Length; k++)
            {
                int cx = x + le + luoi[k][0] * buoc;
                int cy = y + le + luoi[k][1] * buoc;
                g.setColor(0x000000, 0.22f);
                g.fillRect(cx + 1, cy + 1, coCham, coCham, coCham / 2);
                g.setColor(0xC0392B, 1f);
                g.fillRect(cx, cy, coCham, coCham, coCham / 2);
            }
        }

        // Sau mat xuc xac, dung san mot lan. Truoc day moi lan ve mot mat lai
        // tao moi ca mang — moi khung ba con, moi giay sau chuc khung.
        private static readonly int[][] MAT_1 = { new int[] { 1, 1 } };
        private static readonly int[][] MAT_2 = {
            new int[] { 0, 0 }, new int[] { 2, 2 } };
        private static readonly int[][] MAT_3 = {
            new int[] { 0, 0 }, new int[] { 1, 1 }, new int[] { 2, 2 } };
        private static readonly int[][] MAT_4 = {
            new int[] { 0, 0 }, new int[] { 2, 0 },
            new int[] { 0, 2 }, new int[] { 2, 2 } };
        private static readonly int[][] MAT_5 = {
            new int[] { 0, 0 }, new int[] { 2, 0 }, new int[] { 1, 1 },
            new int[] { 0, 2 }, new int[] { 2, 2 } };
        private static readonly int[][] MAT_6 = {
            new int[] { 0, 0 }, new int[] { 2, 0 },
            new int[] { 0, 1 }, new int[] { 2, 1 },
            new int[] { 0, 2 }, new int[] { 2, 2 } };

        /// <summary>Dải kết quả gần đây: chấm xanh Tài, chấm đỏ Xỉu.</summary>
        /// <summary>Đường kính một chấm lịch sử.</summary>
        private const int CO_CHAM = 11;

        /// <summary>Chỗ chừa quanh mỗi chấm để vòng viền có nơi mà vẽ.</summary>
        /// <remarks>
        /// Chừa cho <b>mọi</b> ô, kể cả ô không có viền. Hai bản trước hỏng theo
        /// hai kiểu ngược nhau: vẽ viền ra ngoài mà không chừa chỗ thì chấm đầu
        /// to hơn và cả hàng lệch; vẽ viền vào trong thì chấm đầu nhỏ hơn và chữ
        /// tràn ra ngoài phần màu. Chừa sẵn thì mọi chấm bằng nhau, còn viền nằm
        /// gọn trong phần đã chừa — không đẩy ai, không ăn vào chấm.
        /// </remarks>
        private const int LE_VIEN = 2;

        private void veDaiLichSu(mGraphics g, int y)
        {
            int n = lichSuNhanh.Count;
            if (n == 0)
            {
                return;
            }
            int o = CO_CHAM + LE_VIEN * 2;
            const int khe = 2;
            int tong = n * (o + khe) - khe;
            int x = x0 + rong / 2 - tong / 2;

            for (int i = 0; i < n; i++)
            {
                int xO = x + i * (o + khe);
                int xCham = xO + LE_VIEN;
                int yCham = y + LE_VIEN;
                bool tai = lichSuNhanh[i] == CUA_TAI;
                int mauCham = tai ? MAU_TAI_SANG : MAU_XIU_SANG;

                if (i == 0)
                {
                    // Van vua xong: vong vien ve o kich thuoc CA O, tuc la ra
                    // ngoai cham nhung van nam trong phan da chua san.
                    g.setColor(MAU_VANG, 0.75f + 0.25f * nhip(800));
                    g.fillRect(xO, y, o, o, o / 2);
                }

                g.setColor(0x000000, 0.2f);
                g.fillRect(xCham + 1, yCham + 1, CO_CHAM, CO_CHAM, CO_CHAM / 2);
                g.setColor(mauCham, 0.97f);
                g.fillRect(xCham, yCham, CO_CHAM, CO_CHAM, CO_CHAM / 2);

                mFont.tahoma_7b_white.drawString(g, tai ? "T" : "X",
                        xCham + CO_CHAM / 2, yCham + 1, mFont.CENTER);
            }
        }

        // ------------------------------------------------------------------
        //  Bảng báo thắng thua
        // ------------------------------------------------------------------
        private void veBaoKetQua(mGraphics g)
        {
            int w = 220;
            int h = 100;
            int x = x0 + (rong - w) / 2;
            int y = y0 + (cao - h) / 2;

            g.setColor(0, 0.5f);
            g.fillRect(x0 + 2, y0 + 2, rong - 4, cao - 4, BO_GOC);

            int mau = thangVanRoi ? rgb(0x3F, 0x9A, 0x4C) : rgb(0xB0, 0x3A, 0x2E);
            veQuang(g, x, y, w, h, thangVanRoi ? MAU_VANG : mau,
                    0.6f + 0.4f * nhip(600), 4);
            veKhungBo(g, x, y, w, h, MAU_NEN, 0.99f, MAU_VIEN, 1f, 2);

            g.setColor(mau, 1f);
            g.fillRect(x + 2, y + 2, w - 4, 22, BO_GOC);
            veChuyenSac(g, x + 2, y + 2, w - 4, 22, 5);
            mFont.tahoma_7b_white.drawString(g,
                    thangVanRoi ? "BẠN THẮNG!" : "BẠN THUA",
                    x + w / 2, y + 7, mFont.CENTER);

            if (thangVanRoi)
            {
                veHatSang(g, x + w / 2, y + h / 2, 10, w / 2 - 6, MAU_VANG,
                        0.7f, 1200);
                veVongNo(g, x + w / 2, y + h / 2, 40, w - 20, MAU_VANG, 1100);
            }

            mFont.tahoma_7b_dark.drawString(g, "Phiên " + phienVanRoi,
                    x + w / 2, y + 32, mFont.CENTER);

            mFont mfT = thangVanRoi ? mFont.tahoma_7b_green : mFont.tahoma_7b_red;
            mfT.drawString(g,
                    (thangVanRoi ? "+" : "-") + tienVanRoi + " thỏi vàng",
                    x + w / 2, y + 48, mFont.CENTER);

            int[] n = oNutDongBao();
            veKhungBo(g, n[0], n[1], n[2], n[3], MAU_VANG, 1f, MAU_VIEN, 0.9f, 1);
            veChuyenSac(g, n[0] + 1, n[1] + 1, n[2] - 2, n[3] - 2, 4);
            mFont.tahoma_7b_dark.drawString(g, "ĐÓNG", n[0] + n[2] / 2,
                    n[1] + 5, mFont.CENTER);
        }

        private int[] oNutDongBao()
        {
            int w = 220;
            int h = 100;
            int x = x0 + (rong - w) / 2;
            int y = y0 + (cao - h) / 2;
            return new int[] { x + w / 2 - 35, y + h - 26, 70, 20 };
        }

        // ------------------------------------------------------------------
        //  Hai thẻ lịch sử
        // ------------------------------------------------------------------
        private const int CAO_DONG_LS = 16;

        /// <summary>Cao 22 khi có dải thẻ con ở trên, 0 khi không.</summary>
        private int lechTheCon()
        {
            return (the == THE_TAI_XIU && the2 == THE2_LS_SERVER) ? 22 : 0;
        }

        private int soDongMotTrang()
        {
            int n = (caoNoiDung() - 34 - lechTheCon()) / CAO_DONG_LS;
            return n < 1 ? 1 : n;
        }

        private int[] oDongLs(int i)
        {
            return new int[] {
                x0 + LE, yNoiDung() + 16 + lechTheCon() + i * CAO_DONG_LS,
                rong - LE * 2, CAO_DONG_LS - 2 };
        }

        /// <summary>Ô một thẻ con trên dải thẻ con.</summary>
        private int[] oTheCon(int i)
        {
            int w = (rong - LE * 2 - 4 * (TEN_THE_CON.Length - 1))
                    / TEN_THE_CON.Length;
            return new int[] { x0 + LE + i * (w + 4), yNoiDung() - 2, w, 18 };
        }

        /// <summary>Vùng vẽ của hai màn biểu đồ và cầu.</summary>
        private int yThan()
        {
            return yNoiDung() + lechTheCon() + 2;
        }

        private int caoThan2()
        {
            return y0 + cao - LE - yThan();
        }

        private int[] oNutTrang(bool lui)
        {
            int y = y0 + cao - LE - 18;
            return new int[] { lui ? x0 + LE : x0 + LE + 54, y, 50, 18 };
        }

        /// <summary>Khung nền + tiêu đề cột, dùng chung cho hai thẻ lịch sử.</summary>
        private void veNenLs(mGraphics g, string[] cot, int[] xCot)
        {
            int yT = yNoiDung() + lechTheCon();
            veKhungBo(g, x0 + LE - 3, yT - 4, rong - LE * 2 + 6,
                    caoNoiDung() - 18 - lechTheCon(), MAU_THE, 0.75f,
                    MAU_VIEN, 0.5f, 1);
            for (int i = 0; i < cot.Length; i++)
            {
                mFont.tahoma_7b_dark.drawString(g, cot[i], x0 + LE + xCot[i],
                        yT, mFont.LEFT);
            }
            g.setColor(MAU_VIEN, 0.5f);
            g.fillRect(x0 + LE, yT + 12, rong - LE * 2, 1);
        }

        private void veNutTrang(mGraphics g, int tongDong)
        {
            int moiTrang = soDongMotTrang();
            int soTrang = (tongDong + moiTrang - 1) / moiTrang;
            if (soTrang < 1)
            {
                soTrang = 1;
            }
            for (int k = 0; k < 2; k++)
            {
                bool lui = (k == 0);
                bool duoc = lui ? trang > 0 : trang < soTrang - 1;
                int[] o = oNutTrang(lui);
                veKhungBo(g, o[0], o[1], o[2], o[3],
                        duoc ? MAU_CHON : MAU_THE, duoc ? 1f : 0.4f,
                        MAU_VIEN, duoc ? 0.9f : 0.3f, 1);
                (duoc ? mFont.tahoma_7b_white : mFont.tahoma_7_grey)
                        .drawString(g, lui ? "< Trước" : "Sau >",
                                o[0] + o[2] / 2, o[1] + 4, mFont.CENTER);
            }
            mFont.tahoma_7b_dark.drawString(g,
                    "Trang " + (trang + 1) + "/" + soTrang,
                    x0 + rong - LE, y0 + cao - LE - 14, mFont.RIGHT);
        }

        private void veLsToi(mGraphics g)
        {
            int[] xCot = { 0, 70, 140, 230, 320 };
            veNenLs(g, new string[] { "Phiên", "Cửa", "Cược", "Kết quả",
                    "Thắng/Thua" }, xCot);

            if (lsToi.Count == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Bạn chưa chơi ván nào.",
                        x0 + rong / 2, yNoiDung() + 40, mFont.CENTER);
                veNutTrang(g, 0);
                return;
            }

            int moiTrang = soDongMotTrang();
            int dau = trang * moiTrang;
            for (int i = 0; i < moiTrang && dau + i < lsToi.Count; i++)
            {
                DongLsToi d = lsToi[dau + i];
                int[] o = oDongLs(i);
                g.setColor(i % 2 == 0 ? 0xFFFFFF : 0xE8D6B8, 0.35f);
                g.fillRect(o[0], o[1], o[2], o[3], 3);

                mFont.tahoma_7b_dark.drawString(g, "" + d.phien,
                        o[0] + xCot[0], o[1] + 2, mFont.LEFT);
                (d.cua == CUA_TAI ? mFont.tahoma_7b_blue : mFont.tahoma_7b_red)
                        .drawString(g, d.cua == CUA_TAI ? "TÀI" : "XỈU",
                                o[0] + xCot[1], o[1] + 2, mFont.LEFT);
                mFont.tahoma_7b_dark.drawString(g, "" + d.soThoi,
                        o[0] + xCot[2], o[1] + 2, mFont.LEFT);
                (d.ketQua == CUA_TAI ? mFont.tahoma_7b_blue : mFont.tahoma_7b_red)
                        .drawString(g, d.ketQua == CUA_TAI ? "TÀI" : "XỈU",
                                o[0] + xCot[3], o[1] + 2, mFont.LEFT);
                (d.thang ? mFont.tahoma_7b_green : mFont.tahoma_7b_red)
                        .drawString(g,
                                d.thang ? "+" + d.tienThang : "-" + d.soThoi,
                                o[0] + xCot[4], o[1] + 2, mFont.LEFT);
            }
            veNutTrang(g, lsToi.Count);
        }

        private void veLsServer(mGraphics g)
        {
            // Dai the con.
            for (int i = 0; i < TEN_THE_CON.Length; i++)
            {
                int[] o = oTheCon(i);
                bool sang = (i == theCon);
                veKhungBo(g, o[0], o[1], o[2], o[3],
                        sang ? MAU_CHON : MAU_THE, sang ? 1f : 0.85f,
                        MAU_VIEN, sang ? 0.9f : 0.4f, 1);
                if (sang)
                {
                    veChuyenSac(g, o[0] + 1, o[1] + 1, o[2] - 2, o[3] - 2, 4);
                }
                (sang ? mFont.tahoma_7b_white : mFont.tahoma_7b_dark)
                        .drawString(g, TEN_THE_CON[i], o[0] + o[2] / 2,
                                o[1] + 3, mFont.CENTER);
            }

            if (theCon == THE_CON_BIEU_DO)
            {
                veBieuDo(g);
                return;
            }
            if (theCon == THE_CON_CAU)
            {
                veCau(g);
                return;
            }
            veBangServer(g);
        }

        private void veBangServer(mGraphics g)
        {
            int[] xCot = { 0, 70, 150, 200, 270, 380 };
            veNenLs(g, new string[] { "Phiên", "Xúc xắc", "Tổng", "Kết quả",
                    "Tài", "Xỉu" }, xCot);

            if (lsServer.Count == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Chưa có ván nào được ghi.",
                        x0 + rong / 2, yNoiDung() + 40, mFont.CENTER);
                veNutTrang(g, 0);
                return;
            }

            int moiTrang = soDongMotTrang();
            int dau = trang * moiTrang;
            for (int i = 0; i < moiTrang && dau + i < lsServer.Count; i++)
            {
                DongLsServer d = lsServer[dau + i];
                int[] o = oDongLs(i);
                g.setColor(i % 2 == 0 ? 0xFFFFFF : 0xE8D6B8, 0.35f);
                g.fillRect(o[0], o[1], o[2], o[3], 3);

                mFont.tahoma_7b_dark.drawString(g, "" + d.phien,
                        o[0] + xCot[0], o[1] + 2, mFont.LEFT);
                mFont.tahoma_7b_dark.drawString(g,
                        d.x1 + " " + d.x2 + " " + d.x3,
                        o[0] + xCot[1], o[1] + 2, mFont.LEFT);
                mFont.tahoma_7b_dark.drawString(g, "" + (d.x1 + d.x2 + d.x3),
                        o[0] + xCot[2], o[1] + 2, mFont.LEFT);
                (d.ketQua == CUA_TAI ? mFont.tahoma_7b_blue : mFont.tahoma_7b_red)
                        .drawString(g, d.ketQua == CUA_TAI ? "TÀI" : "XỈU",
                                o[0] + xCot[3], o[1] + 2, mFont.LEFT);
                mFont.tahoma_7_blue.drawString(g, "" + d.tongTai,
                        o[0] + xCot[4], o[1] + 2, mFont.LEFT);
                mFont.tahoma_7_red.drawString(g, "" + d.tongXiu,
                        o[0] + xCot[5], o[1] + 2, mFont.LEFT);
            }
            veNutTrang(g, lsServer.Count);
        }


        /// <summary>Màu chấm theo kết quả: Tài xanh, Xỉu xám.</summary>
        private static int mauChamKq(sbyte kq)
        {
            return kq == CUA_TAI ? rgb(0x2E, 0x86, 0xC1) : rgb(0x9A, 0x9A, 0x9A);
        }

        /// <summary>
        /// Biểu đồ điểm: đường tổng ở trên, ba đường xúc xắc ở dưới.
        /// </summary>
        /// <remarks>
        /// <para>Đọc theo <b>chiều thời gian</b> nên duyệt ngược danh sách —
        /// máy chủ trả về mới trước.</para>
        ///
        /// <para>Trục tổng để 3..18 cố định chứ không co theo dữ liệu: đây là
        /// biểu đồ để so các ván với nhau, trục co giãn thì hai ảnh chụp ở hai
        /// thời điểm không so được.</para>
        /// </remarks>
        private void veBieuDo(mGraphics g)
        {
            int x = x0 + LE;
            int y = yThan();
            int w = rong - LE * 2;
            int h = caoThan2();

            veKhungBo(g, x - 3, y - 3, w + 6, h + 3, MAU_SAN, 0.9f, MAU_VIEN, 0.6f, 1);

            if (lsServer.Count == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Chưa có ván nào được ghi.",
                        x + w / 2, y + h / 2, mFont.CENTER);
                return;
            }

            // Mot the chi ve MOT bieu do, hai mui ten o hai goc doi qua lai.
            //
            // Truoc day hai bieu do chia doi chieu cao, moi cai con hon tam
            // chuc diem: cham de len nhau, duong thi bet lai gan nhu mot vach
            // ngang. Cho moi luc mot cai thi bieu do dang xem duoc CA chieu
            // cao, doc ra ro han.
            bool veTong = (bieuDoNao == 0);
            int phanVe = h - 20 - (veTong ? 0 : (CAO_NUT_XX + 4));
            int yVe = y + 14;
            int caoVe = phanVe;

            int leTrai = 22;
            int xVe = x + leTrai;
            int wVe = w - leTrai - 8;

            // Chi ve nhung van GAN NHAT vua man, khong nhoi het sau chuc van.
            //
            // Sau chuc cham tren nam tram pixel la moi cham cach nhau tam tam
            // pixel — cac vong tron de len nhau va con so ben trong doc khong
            // ra. Muon xem het thi con the "Bang", o day uu tien nhin duoc.
            int n = wVe / BUOC_TOI_THIEU + 1;
            if (n > lsServer.Count)
            {
                n = lsServer.Count;
            }
            if (n < 2)
            {
                n = lsServer.Count < 2 ? lsServer.Count : 2;
            }
            int buoc = n > 1 ? wVe / (n - 1) : wVe;

            // Dong dau: van gan nhat.
            DongLsServer m = lsServer[0];
            mFont.tahoma_7b_yellow.drawString(g, "Phiên gần nhất: " + m.phien
                    + "   " + (m.ketQua == CUA_TAI ? "Tài" : "Xỉu")
                    + " (" + m.x1 + "-" + m.x2 + "-" + m.x3 + ")",
                    x + w / 2, y + 1, mFont.CENTER);

            // Hai mui ten sat hai goc tren: doi qua lai giua hai bieu do.
            veMuiTenNho(g, oMuiTenBieuDo(true), true, true);
            veMuiTenNho(g, oMuiTenBieuDo(false), false, true);
            mFont.tahoma_7_grey.drawString(g,
                    veTong ? "Tổng" : "Từng con", x + 18, y + 1, mFont.LEFT);

            veLuoi(g, xVe, yVe, wVe, caoVe);

            // Vach doc mo danh dau tung van, cho de doi chieu.
            g.setColor(0xFFFFFF, 0.05f);
            for (int i = 0; i < n; i += 2)
            {
                g.fillRect(xVe + i * buoc - buoc / 2, yVe, buoc, caoVe);
            }

            if (veTong)
            {
                // Nhan truc tong.
                for (int v = 3; v <= 18; v += 3)
                {
                    int yy = yVe + caoVe - (v - 3) * caoVe / 15;
                    mFont.tahoma_7_blue.drawString(g, "" + v, x + leTrai - 4,
                            yy - 4, mFont.RIGHT);
                }

                // Duong tong. Duyet nguoc: i = 0 la van CU nhat tren truc.
                g.setColor(rgb(0x4A, 0x90, 0xD9), 0.9f);
                for (int i = 0; i + 1 < n; i++)
                {
                    DongLsServer a = lsServer[n - 1 - i];
                    DongLsServer b = lsServer[n - 2 - i];
                    g.drawLine(xVe + i * buoc, yTong(a, yVe, caoVe),
                            xVe + (i + 1) * buoc, yTong(b, yVe, caoVe));
                }
                for (int i = 0; i < n; i++)
                {
                    DongLsServer d = lsServer[n - 1 - i];
                    int cx = xVe + i * buoc;
                    int cy = yTong(d, yVe, caoVe);
                    int co = 16;
                    g.setColor(0x000000, 0.35f);
                    g.fillRect(cx - co / 2 + 1, cy - co / 2 + 1, co, co, co / 2);
                    g.setColor(mauChamKq(d.ketQua), 1f);
                    g.fillRect(cx - co / 2, cy - co / 2, co, co, co / 2);
                    mFont.tahoma_7b_white.drawString(g,
                            "" + (d.x1 + d.x2 + d.x3), cx, cy - 4, mFont.CENTER);
                }
                return;
            }

            // Nhan truc mat xuc xac.
            for (int v = 1; v <= 6; v++)
            {
                int yy = yVe + caoVe - (v - 1) * caoVe / 5;
                mFont.tahoma_7_blue.drawString(g, "" + v, x + leTrai - 4, yy - 4,
                        mFont.RIGHT);
            }

            // Ba duong xuc xac.
            for (int k = 0; k < 3; k++)
            {
                if (!hienXucXac[k])
                {
                    continue;
                }
                int mau = MAU_XUC_XAC[k];
                g.setColor(mau, 0.85f);
                for (int i = 0; i + 1 < n; i++)
                {
                    int va = matThu(lsServer[n - 1 - i], k);
                    int vb = matThu(lsServer[n - 2 - i], k);
                    g.drawLine(xVe + i * buoc, yMat(va, yVe, caoVe),
                            xVe + (i + 1) * buoc, yMat(vb, yVe, caoVe));
                }
                for (int i = 0; i < n; i++)
                {
                    int v = matThu(lsServer[n - 1 - i], k);
                    int cx = xVe + i * buoc;
                    int cy = yMat(v, yVe, caoVe);
                    g.setColor(0x000000, 0.32f);
                    g.fillRect(cx - 4, cy - 4, 11, 11, 6);
                    g.setColor(mau, 1f);
                    g.fillRect(cx - 5, cy - 5, 11, 11, 6);
                }
            }

            for (int k = 0; k < 3; k++)
            {
                int[] o = oNutXucXac(k);
                bool hien = hienXucXac[k];
                if (hien)
                {
                    veQuang(g, o[0], o[1], o[2], o[3], MAU_XUC_XAC[k], 0.55f, 2);
                }
                veKhungBo(g, o[0], o[1], o[2], o[3],
                        hien ? MAU_XUC_XAC[k] : rgb(0x77, 0x77, 0x77),
                        hien ? 0.95f : 0.5f,
                        hien ? MAU_VANG : MAU_VIEN, hien ? 0.9f : 0.4f, 1);
                veChuyenSac(g, o[0] + 1, o[1] + 1, o[2] - 2, o[3] - 2, 4);
                (hien ? mFont.tahoma_7b_white : mFont.tahoma_7_grey)
                        .drawString(g, "XÚC XẮC " + (k + 1),
                                o[0] + o[2] / 2, o[1] + 4, mFont.CENTER);
            }
        }

        /// <summary>Biểu đồ đang xem: 0 = tổng ba con, 1 = từng con xúc xắc.</summary>
        private int bieuDoNao;

        /// <summary>Ô của mũi tên đổi biểu đồ, sát hai góc trên của khung.</summary>
        /// <remarks>
        /// Ngang hàng với dòng "Phiên gần nhất" ở giữa — ba thứ cùng một hàng
        /// thì đọc ra là một bộ điều khiển.
        /// </remarks>
        private int[] oMuiTenBieuDo(bool trai)
        {
            int x = x0 + LE;
            int w = rong - LE * 2;
            int co = 14;
            return new int[] {
                trai ? x : x + w - co, yThan() - 1, co, 13 };
        }

        /// <summary>Cao dành cho hàng ba nút bật/tắt đường xúc xắc.</summary>
        private const int CAO_NUT_XX = 18;

        /// <summary>
        /// Hai chấm liền nhau phải cách nhau ít nhất bấy nhiêu pixel.
        /// </summary>
        /// <remarks>
        /// Chấm tổng rộng 16px; để 22 thì hai chấm sát nhau vẫn hở, con số bên
        /// trong đọc được. Số ván vẽ ra suy ngược từ đây chứ không đặt cứng —
        /// bảng rộng ra thì tự hiện thêm ván.
        /// </remarks>
        private const int BUOC_TOI_THIEU = 22;

        /// <summary>Ô nút "Xí ngầu k" ở đáy biểu đồ.</summary>
        private int[] oNutXucXac(int k)
        {
            int w = 84;
            int khe = 8;
            int tong = 3 * w + 2 * khe;
            int x = x0 + rong / 2 - tong / 2 + k * (w + khe);
            return new int[] { x, y0 + cao - LE - CAO_NUT_XX, w, CAO_NUT_XX };
        }

        private static int matThu(DongLsServer d, int k)
        {
            return k == 0 ? d.x1 : (k == 1 ? d.x2 : d.x3);
        }

        private static int yTong(DongLsServer d, int yTren, int caoTren)
        {
            int t = d.x1 + d.x2 + d.x3;
            if (t < 3)
            {
                t = 3;
            }
            return yTren + caoTren - (t - 3) * caoTren / 15;
        }

        private static int yMat(int v, int yDuoi, int caoDuoi)
        {
            if (v < 1)
            {
                v = 1;
            }
            return yDuoi + caoDuoi - (v - 1) * caoDuoi / 5;
        }

        /// <summary>Lưới kẻ mờ làm nền cho biểu đồ.</summary>
        private static void veLuoi(mGraphics g, int x, int y, int w, int h)
        {
            g.setColor(rgb(0xA8, 0x6E, 0x3C), 0.35f);
            for (int i = 0; i <= 5; i++)
            {
                g.fillRect(x, y + i * h / 5, w, 1);
            }
            for (int i = 0; i <= 10; i++)
            {
                g.fillRect(x + i * w / 10, y, 1, h);
            }
        }

        /// <summary>
        /// Cầu: kết quả xếp thành cột theo chuỗi thắng liên tiếp.
        /// </summary>
        /// <remarks>
        /// <para>Cùng kết quả với ván trước thì xuống dòng trong <b>cùng cột</b>;
        /// khác thì sang cột mới. Đây là cách đọc quen thuộc ở mọi bàn tài xỉu —
        /// nhìn hình cột là thấy ngay cầu đang bệt hay đang nhảy.</para>
        ///
        /// <para>Chạm trần cột thì bò ngang sang cột kế bên chứ không tràn ra
        /// ngoài lưới, đúng như cách bàn thật vẽ.</para>
        /// </remarks>
        private void veCau(mGraphics g)
        {
            int x = x0 + LE;
            int y = yThan();
            int w = rong - LE * 2;
            int h = caoThan2();

            veKhungBo(g, x - 3, y - 3, w + 6, h + 3, MAU_SAN, 0.9f, MAU_VIEN, 0.6f, 1);

            if (lsServer.Count == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Chưa có ván nào được ghi.",
                        x + w / 2, y + h / 2, mFont.CENTER);
                return;
            }

            int soTai = 0;
            for (int i = 0; i < lsServer.Count; i++)
            {
                if (lsServer[i].ketQua == CUA_TAI)
                {
                    soTai++;
                }
            }
            int soXiu = lsServer.Count - soTai;

            // Hai the dem o tren.
            veTheDem(g, x + w / 2 - 86, y, 80, "TÀI: " + soTai, MAU_TAI_SANG);
            veTheDem(g, x + w / 2 + 6, y, 80, "XỈU: " + soXiu, MAU_XIU_SANG);

            int yLuoi = y + 22;
            int caoLuoi = h - 26;

            // O co dinh 20 diem, chu khong chia deu chieu cao cho so hang.
            //
            // Ban truoc chot cung 6..10 hang roi chia chieu cao ra, va mach nao
            // dai hon so hang do thi khong dung len duoc nua ma "bo ngang" sang
            // cot ben — mot mach muoi van Tai lien doc ra thanh hai mach nam
            // van, tuc la sai han cai ma nguoi soi cau muon thay.
            //
            // Nay luoi cao bao nhieu la tuy mach dai nhat, con khung thi CUON
            // DOC. Khong mach nao bi cat cut nua.
            int oCo = 20;
            if (oCo * 4 > caoLuoi)
            {
                oCo = caoLuoi / 4;
            }
            if (oCo < 9)
            {
                oCo = 9;
            }
            int hangThay = caoLuoi / oCo;
            if (hangThay < 1)
            {
                hangThay = 1;
            }
            int soCot = w / oCo;
            if (soCot < 1)
            {
                soCot = 1;
            }

            // 1. Dung cot/dong cho tung van — khong chan so hang.
            int n = lsServer.Count;
            if (cotCau.Length < n)
            {
                cotCau = new int[n];
                dongCau = new int[n];
            }
            int cot = 0;
            int dong = 0;
            int hangCan = 1;
            sbyte truoc = -1;
            for (int k = 0; k < n; k++)
            {
                sbyte kq = lsServer[n - 1 - k].ketQua;
                if (truoc < 0)
                {
                    cot = 0;
                    dong = 0;
                }
                else if (kq == truoc)
                {
                    dong++;
                }
                else
                {
                    cot++;
                    dong = 0;
                }
                truoc = kq;
                cotCau[k] = cot;
                dongCau[k] = dong;
                if (dong + 1 > hangCan)
                {
                    hangCan = dong + 1;
                }
            }
            // Van moi nhat luon phai nhin thay: neo cua so vao cot cuoi.
            int cotBatDau = cot + 1 - soCot;
            if (cotBatDau < 0)
            {
                cotBatDau = 0;
            }

            // 2. Gioi han cuon doc.
            cuonCauToiDa = hangCan - hangThay;
            if (cuonCauToiDa < 0)
            {
                cuonCauToiDa = 0;
            }
            gioiHanCuonCau();
            buocCuonCau = oCo;
            oLuoiCau = new int[] { x, yLuoi, soCot * oCo, hangThay * oCo };

            // 3. Luoi va cham, cat gon trong vung nhin thay.
            g.setClip(x, yLuoi, soCot * oCo, hangThay * oCo);
            g.setColor(rgb(0xA8, 0x6E, 0x3C), 0.30f);
            for (int i = 0; i <= hangThay; i++)
            {
                g.fillRect(x, yLuoi + i * oCo, soCot * oCo, 1);
            }
            for (int i = 0; i <= soCot; i++)
            {
                g.fillRect(x + i * oCo, yLuoi, 1, hangThay * oCo);
            }

            for (int k = 0; k < n; k++)
            {
                int c = cotCau[k] - cotBatDau;
                int d = dongCau[k] - cuonCau;
                if (c < 0 || c >= soCot || d < 0 || d >= hangThay)
                {
                    continue;
                }
                DongLsServer v = lsServer[n - 1 - k];
                int cx = x + c * oCo + oCo / 2;
                int cy = yLuoi + d * oCo + oCo / 2;
                int co = oCo - 4;
                if (k == n - 1)
                {
                    // Van moi nhat: khoanh vang nhap nhay.
                    g.setColor(MAU_VANG, 0.7f + 0.3f * nhip(700));
                    g.fillRect(cx - co / 2 - 2, cy - co / 2 - 2, co + 4, co + 4,
                            (co + 4) / 2);
                }
                else
                {
                    g.setColor(0x000000, 0.35f);
                    g.fillRect(cx - co / 2 + 1, cy - co / 2 + 1, co, co, co / 2);
                }
                g.setColor(mauChamKq(v.ketQua), 1f);
                g.fillRect(cx - co / 2, cy - co / 2, co, co, co / 2);
                if (oCo >= 14)
                {
                    mFont.tahoma_7b_white.drawString(g,
                            "" + (v.x1 + v.x2 + v.x3), cx, cy - 4, mFont.CENTER);
                }
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            // 4. Thanh cuon ben phai — chi hien khi con hang nam ngoai.
            if (cuonCauToiDa > 0)
            {
                int xT = x + soCot * oCo + 2;
                int caoT = hangThay * oCo;
                g.setColor(0x000000, 0.30f);
                g.fillRect(xT, yLuoi, 3, caoT, 1);
                int caoTay = caoT * hangThay / hangCan;
                if (caoTay < 8)
                {
                    caoTay = 8;
                }
                int yTay = yLuoi + (caoT - caoTay) * cuonCau / cuonCauToiDa;
                g.setColor(MAU_VANG, 0.85f);
                g.fillRect(xT, yTay, 3, caoTay, 1);
            }
        }

        /// <summary>Đang giữ ngón/chuột để kéo cầu theo chiều dọc.</summary>
        private bool dangKeoCau;
        private int yBatKeoCau;
        private int cuonLucBatKeoCau;

        /// <summary>
        /// Lăn chuột và giữ kéo để cuộn cầu theo chiều dọc.
        /// </summary>
        /// <returns><c>true</c> khi đã nuốt cú chạm này.</returns>
        private bool capNhatCuonCau()
        {
            if (cuonCauToiDa <= 0 || oLuoiCau.Length != 4)
            {
                dangKeoCau = false;
                return false;
            }
            if (GameCanvas.pXYScrollMouse != 0
                    && GameCanvas.pxMouse >= oLuoiCau[0]
                    && GameCanvas.pxMouse <= oLuoiCau[0] + oLuoiCau[2]
                    && GameCanvas.pyMouse >= oLuoiCau[1]
                    && GameCanvas.pyMouse <= oLuoiCau[1] + oLuoiCau[3])
            {
                cuonCau += GameCanvas.pXYScrollMouse > 0 ? -1 : 1;
                gioiHanCuonCau();
                return true;
            }
            if (GameCanvas.isPointerDown
                    && GameCanvas.isPointerHoldIn(oLuoiCau[0], oLuoiCau[1],
                            oLuoiCau[2], oLuoiCau[3]))
            {
                if (!dangKeoCau)
                {
                    dangKeoCau = true;
                    yBatKeoCau = GameCanvas.py;
                    cuonLucBatKeoCau = cuonCau;
                }
                if (buocCuonCau > 0)
                {
                    cuonCau = cuonLucBatKeoCau
                            + (yBatKeoCau - GameCanvas.py) / buocCuonCau;
                }
                gioiHanCuonCau();
                return true;
            }
            dangKeoCau = false;
            return false;
        }

        private void gioiHanCuonCau()
        {
            if (cuonCau > cuonCauToiDa)
            {
                cuonCau = cuonCauToiDa;
            }
            if (cuonCau < 0)
            {
                cuonCau = 0;
            }
        }

        private static void veTheDem(mGraphics g, int x, int y, int w,
                string chu, int mau)
        {
            g.setColor(0x000000, 0.3f);
            g.fillRect(x + 1, y + 1, w, 17, 5);
            g.setColor(mau, 0.95f);
            g.fillRect(x, y, w, 17, 5);
            veChuyenSac(g, x, y, w, 17, 4);
            mFont.tahoma_7b_white.drawString(g, chu, x + w / 2, y + 3,
                    mFont.CENTER);
        }

        private void veNutDong(mGraphics g, int x, int y)
        {
            const int canh = 18;
            veKhungBo(g, x, y, canh, canh, rgb(0xD8, 0x4A, 0x3A), 1f,
                    MAU_VIEN, 0.9f, 1);
            g.setColor(0xFFFFFF, 1f);
            const int soDiem = 8;
            const int coDiem = 2;
            const int benX = soDiem - 1 + coDiem;
            int xX = x + (canh - benX) / 2;
            int yX = y + (canh - benX) / 2;
            for (int i = 0; i < soDiem; i++)
            {
                g.fillRect(xX + i, yX + i, coDiem, coDiem);
                g.fillRect(xX + benX - coDiem - i, yX + i, coDiem, coDiem);
            }
        }

        // ------------------------------------------------------------------
        //  Chạm
        // ------------------------------------------------------------------
        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            tinhBoCuc();

            // Keo de nan bat. Xu ly TRUOC cai chan "chi khi vua tha ngon":
            // nan la mot thao tac KEO, no dien ra trong luc ngon van dang giu.
            if (the == THE_TAI_XIU && the2 == THE2_DAT_CUOC
                    && !hienBaoKetQua && choNan() && !daMoHan)
            {
                if (GameCanvas.isPointerDown
                        && GameCanvas.isPointerHoldIn(tamDiaX() - coDia() / 2,
                                tamDiaY(yBan()) - coDia() / 2, coDia(), coDia()))
                {
                    if (mocBamBat == 0L)
                    {
                        mocBamBat = mSystem.currentTimeMillis();
                    }
                    else if (!dangNan && mSystem.currentTimeMillis() - mocBamBat
                            >= MS_GIU_TRUOC_KHI_KEO)
                    {
                        // Chot moc NGAY LUC nay chu khong phai luc dat tay: tay
                        // co the da nhich trong nua giay giu, lay moc cu thi bat
                        // nhay dung bang quang nhich do.
                        dangNan = true;
                        bamX = GameCanvas.px;
                        bamY = GameCanvas.py;
                        nanGocX = nanX;
                        nanGocY = nanY;
                        dichX = nanX;
                        dichY = nanY;
                    }
                }
                if (dangNan)
                {
                    if (GameCanvas.isPointerDown)
                    {
                        keoBatTheoNgon(GameCanvas.px, GameCanvas.py, yBan());
                    }
                    else
                    {
                        // Buong ngon: thoi nan, bat tu troi ve tam.
                        dangNan = false;
                        mocBamBat = 0L;
                        GameCanvas.clearAllPointerEvent();
                        return true;
                    }
                    return true;
                }
                if (!GameCanvas.isPointerDown)
                {
                    // Nha tay truoc khi du nua giay: dem lai tu dau lan sau.
                    mocBamBat = 0L;
                }
                if (mocBamBat != 0L)
                {
                    // Dang giu nhung chua du gio: van nuot cham, khong de no roi
                    // xuong ban do phia duoi.
                    return true;
                }
            }
            else
            {
                dangNan = false;
            }

            // Cau Ca xu ly TRUOC cai chan "chi khi vua tha ngon" ngay duoi.
            //
            // DAY LA LOI GOC cua ca chuoi "bam quang khong an, giu day khong
            // tang": cai chan kia lam MOI THU phia sau no chi chay o dung khung
            // hinh ngon VUA NHA. Cu giu khong bao gio toi duoc, con o khung nha
            // thi isPointerDown da la false — nen phep thu "isPointerDown va
            // ngon trong nut" luon ra false.
            //
            // Khoi keo nan bat cua Tai Xiu cung nam truoc cai chan nay, va vi
            // dung mot ly do: nan la thao tac KEO, dien ra trong luc ngon van
            // dang giu.
            if (the == THE_CAU_CA)
            {
                if (chamCauCa())
                {
                    return true;
                }
            }

            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }

            // Bang bao nuot het cham: no nam tren cung, bam xuyen qua la bam
            // trung nut o duoi ma khong nhin thay.
            if (hienBaoKetQua)
            {
                int[] n = oNutDongBao();
                if (cham(n[0], n[1], n[2], n[3]))
                {
                    hienBaoKetQua = false;
                }
                GameCanvas.clearAllPointerEvent();
                return true;
            }

            if (cham(x0 + rong - 24, y0 + 5, 18, 18))
            {
                dong();
                return true;
            }

            if (capNhatCuonThe())
            {
                return true;
            }
            for (int i = 0; i < TEN_THE.Length; i++)
            {
                int[] o = oThe(i);
                // The nam ngoai vung thay duoc thi khong dang ky o cham: dang
                // ky ca thi bam vao mep dai lai trung mot the vo hinh.
                if (o[0] + o[2] <= oDaiThe[0]
                        || o[0] >= oDaiThe[0] + oDaiThe[2])
                {
                    continue;
                }
                if (cham(o[0], o[1], o[2], o[3]))
                {
                    doiTro(i);
                    return true;
                }
            }
            if (the == THE_TAI_XIU)
            {
                for (int i = 0; i < TEN_THE_2.Length; i++)
                {
                    int[] o = oThe2(i);
                    if (cham(o[0], o[1], o[2], o[3]))
                    {
                        doiThe(i);
                        return true;
                    }
                }
            }

            if (the == THE_CAU_CA)
            {
                // Da xu ly o tren, truoc cai chan "chi khi vua tha ngon". Den
                // day thi chi con viec nuot cu cham cho khoi lot xuong man choi.
                return true;
            }
            if (the != THE_TAI_XIU)
            {
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            if (the2 == THE2_DAT_CUOC)
            {
                if (chamTaiXiu())
                {
                    return true;
                }
            }
            else
            {
                if (the2 == THE2_LS_SERVER)
                {
                    for (int i = 0; i < TEN_THE_CON.Length; i++)
                    {
                        int[] o = oTheCon(i);
                        if (cham(o[0], o[1], o[2], o[3]))
                        {
                            theCon = i;
                            trang = 0;
                            return true;
                        }
                    }
                    if (theCon == THE_CON_BIEU_DO)
                    {
                        for (int k = 0; k < 3; k++)
                        {
                            int[] o = oNutXucXac(k);
                            if (cham(o[0], o[1], o[2], o[3]))
                            {
                                hienXucXac[k] = !hienXucXac[k];
                                return true;
                            }
                        }
                        int[] oT = oMuiTenBieuDo(true);
                        int[] oP = oMuiTenBieuDo(false);
                        if (cham(oT[0], oT[1], oT[2], oT[3])
                                || cham(oP[0], oP[1], oP[2], oP[3]))
                        {
                            // Chi co hai bieu do, nen ca hai mui ten deu la
                            // "doi sang cai kia".
                            bieuDoNao = 1 - bieuDoNao;
                            return true;
                        }
                    }
                    if (theCon == THE_CON_CAU && capNhatCuonCau())
                    {
                        return true;
                    }
                }
                // Nut sang trang chi co o man bang.
                if ((the2 == THE2_LS_TOI || theCon == THE_CON_BANG) && chamTrang())
                {
                    return true;
                }
            }

            // Bam ra ngoai bang KHONG dong bang — chi dau X moi dong.
            //
            // Van nuot cu cham: khong nuot thi no roi xuong ban do phia duoi va
            // nhan vat chay toi cho vua bam.
            GameCanvas.clearAllPointerEvent();
            return true;
        }

        private void doiThe(int i)
        {
            the2 = i;
            trang = 0;
            // Xin lai moi lan mo the: lich su doi sau moi van, giu ban cu thi
            // nguoi choi vua choi xong mo ra khong thay van vua roi.
            if (i == THE2_LS_TOI)
            {
                Service.gI().taiXiuXinLichSuToi();
            }
            else if (i == THE2_LS_SERVER)
            {
                Service.gI().taiXiuXinLichSuServer();
            }
        }

        private bool chamTrang()
        {
            int tongDong = the2 == THE2_LS_TOI ? lsToi.Count : lsServer.Count;
            int moiTrang = soDongMotTrang();
            int soTrang = (tongDong + moiTrang - 1) / moiTrang;
            for (int k = 0; k < 2; k++)
            {
                bool lui = (k == 0);
                int[] o = oNutTrang(lui);
                if (cham(o[0], o[1], o[2], o[3]))
                {
                    if (lui && trang > 0)
                    {
                        trang--;
                    }
                    else if (!lui && trang < soTrang - 1)
                    {
                        trang++;
                    }
                    return true;
                }
            }
            return false;
        }

        private bool chamTaiXiu()
        {
            // Hai nut cua tung cua truoc, roi moi toi than o cua: nut nam TRONG
            // o nen xet o truoc thi bam nut nao cung thanh chon cua.
            for (int k = 0; k < 2; k++)
            {
                bool tai = (k == 0);
                sbyte cua = tai ? CUA_TAI : CUA_XIU;
                bool laCuaChon = cuaChon == cua;

                int[] n = oNutDat(tai);
                if (cham(n[0], n[1], n[2], n[3]))
                {
                    if (choDatCuoc() && laCuaChon && tienCho > 0)
                    {
                        Service.gI().taiXiuDatCuoc(cua, tienCho);
                    }
                    return true;
                }

                int[] h = oNutHuy(tai);
                if (cham(h[0], h[1], h[2], h[3]))
                {
                    if (choDatCuoc() && laCuaChon)
                    {
                        huyGom();
                    }
                    return true;
                }

                int[] o = oCua(tai);
                if (cham(o[0], o[1], o[2], o[3]))
                {
                    if (choDatCuoc() && cuaChon != cua)
                    {
                        // Doi cua thi bo so dang gom: so do la cua cua kia.
                        cuaChon = cua;
                        tienCho = 0;
                    }
                    return true;
                }
            }

            for (int i = 0; i < MUC.Length + 2; i++)
            {
                int[] o = oMuc(i);
                if (!cham(o[0], o[1], o[2], o[3]))
                {
                    continue;
                }
                if (!choDatCuoc() || cuaChon < 0)
                {
                    return true;
                }
                if (i < MUC.Length)
                {
                    gomThem(MUC[i]);
                }
                else if (i == MUC.Length)
                {
                    // Tat tay: cong mot so that lon roi de gomThem chan lai o so
                    // thoi dang co — khoi phai mot quy tac rieng cho nut nay.
                    gomThem(tongThoiVang());
                }
                else
                {
                    // Ban phim so dung chung — xem God/Chung/BanPhimSo.cs.
                    BanPhimSo.getInstance().moRa("Nhập số lượng",
                            tongThoiVang(), n => gomThem(n));
                }
                return true;
            }
            return false;
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
