// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game1.God
{
    /// <summary>
    /// Thẻ <b>Đua Vịt</b> trong bảng Trò chơi — năm con vịt bơi ba mươi giây.
    /// </summary>
    /// <remarks>
    /// <para><b>Kết quả không nằm ở đây.</b> Máy chủ xáo thứ tự về đích ngay đầu
    /// pha bơi rồi gửi xuống (<c>MiniGame.dnThuTu</c>). Màn này chỉ dựng một
    /// cuộc đua <i>trông như thật</i> mà kết thúc đúng theo thứ tự ấy: vượt
    /// nhau, hụt hơi, bứt tốc — nhưng con về nhất luôn là con máy chủ đã chọn.</para>
    ///
    /// <para><b>Mọi người thấy cùng một cuộc đua.</b> Đường bơi gieo theo số phiên
    /// và thứ tự về đích, không theo đồng hồ máy, nên hai người ngồi cạnh nhau
    /// thấy con nào vượt con nào ở cùng một chỗ.</para>
    ///
    /// <h2>Đường bơi</h2>
    ///
    /// <para>Mỗi con có một <i>đường tốc độ</i>: tốc độ nền cộng hai sóng sin lệch
    /// pha ngẫu nhiên. Cộng dồn lại rồi chia cho tổng thì được vị trí từ 0 tới 1
    /// — tức là con nào cũng tới đích <b>đúng</b> mốc đã định, bất kể giữa đường
    /// nó nhanh chậm ra sao. Mốc về đích xếp theo thứ hạng máy chủ gửi.</para>
    ///
    /// <para>Sóng sin đủ lớn để giữa đường các con thay nhau dẫn; và một nửa số
    /// ván con thắng được thêm một cú <b>bứt tốc cuối</b> — vì chuẩn hoá theo tổng
    /// nên bứt ở cuối nghĩa là bị bỏ lại lúc đầu, đúng kiểu lội ngược dòng.</para>
    ///
    /// <h2>Ảnh</h2>
    ///
    /// <para>Năm dải <c>dv_*_xN</c> trong <c>Resources</c>, mỗi dải tám khung xếp
    /// dọc, xuất sẵn cho từng mức zoom 1..4 (ảnh vẽ 1:1 theo điểm ảnh, nên một
    /// dải cho mọi zoom thì zoom thấp vịt to, zoom cao vịt bé tí). Khung 0 và 7
    /// là đứng yên, 1..6 là một vòng quạt chân.</para>
    /// </remarks>
    public partial class TroChoiUI
    {
        // ==================================================================
        //  Hằng số
        // ==================================================================

        /// <summary>Tên đầy đủ, cùng thứ tự với <c>DuaNguaManager.TEN_NGUA</c>.</summary>
        private static readonly string[] TEN_VIT = {
            "Vịt Goku", "Vịt Naruto", "Vịt Sasuke", "Vịt Vegeta", "Vịt Bulma"
        };

        /// <summary>Tên ngắn cho thẻ hẹp.</summary>
        private static readonly string[] TEN_NGAN_VIT = {
            "Goku", "Naruto", "Sasuke", "Vegeta", "Bulma"
        };

        /// <summary>Khoá tên tệp ảnh <c>dv_&lt;khoá&gt;_x&lt;zoom&gt;</c>.</summary>
        private static readonly string[] KHOA_VIT = {
            "goku", "naruto", "sasuke", "vegeta", "bulma"
        };

        /// <summary>Màu chủ đạo từng con, lấy theo bộ đồ của nó.</summary>
        private static readonly int[] MAU_VIT = {
            (0xF2 << 16) | (0x84 << 8) | 0x1E,
            (0xF0 << 16) | (0xB0 << 8) | 0x1C,
            (0x34 << 16) | (0x4E << 8) | 0x9A,
            (0x2E << 16) | (0x6C << 8) | 0xDA,
            (0x1F << 16) | (0xA8 << 8) | 0x9C
        };

        private static readonly int[] MAU_VIT_TOI = {
            (0xA8 << 16) | (0x4A << 8) | 0x08,
            (0xA8 << 16) | (0x6E << 8) | 0x06,
            (0x1A << 16) | (0x28 << 8) | 0x5C,
            (0x16 << 16) | (0x3A << 8) | 0x8C,
            (0x0C << 16) | (0x66 << 8) | 0x5E
        };

        private const int DV_SO_VIT = God.MiniGame.DN_SO_NGUA;

        /// <summary>Ba pha một ván. Chép theo <c>DuaNguaManager</c>, chỉ để vẽ thanh tiến độ.</summary>
        private const int DV_GIAY_DAT = 30;
        private const int DV_GIAY_DUA = 30;
        private const int DV_GIAY_KQ = 7;

        /// <summary>Hệ số trả ×10 — chỉ để bày ra, máy chủ mới là bên trả.</summary>
        private const int DV_HE_SO_X10 = 45;

        private const long DV_TOI_DA_MOI_CON = 500;
        private const long DV_TOI_DA_MOT_VAN = 1500;

        private static readonly long[] DV_MUC = { 10, 50, 100, 500 };

        /// <summary>Số khung trong mỗi dải ảnh vịt.</summary>
        private const int DV_SO_KHUNG = 8;

        // ---- đường đua, đơn vị logic ----

        /// <summary>Toạ độ vạch xuất phát trên đường đua.</summary>
        private const int DV_XUAT_PHAT = 74;

        /// <summary>Quãng bơi. Dài gần ba bề ngang bảng, nên máy quay phải chạy theo.</summary>
        private const int DV_QUANG_DUONG = 1500;

        private const int DV_DICH = DV_XUAT_PHAT + DV_QUANG_DUONG;

        /// <summary>Quãng đường ghi trên biển báo, cho có cảm giác đường dài.</summary>
        private const int DV_MET = 300;

        /// <summary>Từ mỏ tới tâm con vịt — vạch đích tính theo mỏ.</summary>
        private const int DV_MO_TOI_TAM = 20;

        // ---- nhịp trong pha bơi, mili giây kể từ đầu pha ----

        /// <summary>Đếm 3-2-1 trước khi thả vịt.</summary>
        private const int DV_MS_DEM_NGUOC = 1800;

        /// <summary>Con về nhất chạm đích lúc này.</summary>
        private const int DV_MS_VE_NHAT = 25400;

        /// <summary>Mỗi hạng sau về chậm chừng này.</summary>
        private const int DV_MS_CACH_HANG = 640;

        /// <summary>Số mẫu trên đường vị trí của mỗi con.</summary>
        private const int DV_SO_MAU = 180;

        // ==================================================================
        //  Trạng thái
        // ==================================================================

        /// <summary>Con đang chọn để đặt, -1 là chưa chọn. Chưa gửi đi đâu.</summary>
        private int dvChon = -1;

        /// <summary>Số thỏi đang gom cho <see cref="dvChon"/>, chưa xác nhận.</summary>
        private long dvTienCho;

        /// <summary>Đường vị trí đã dựng, 0..1 theo từng mẫu.</summary>
        private readonly float[][] dvDuong = new float[DV_SO_VIT][];

        /// <summary>Mốc về đích của từng con, mili giây kể từ đầu pha bơi.</summary>
        private readonly int[] dvMsVe = new int[DV_SO_VIT];

        /// <summary>Khoá của cuộc đua đang dựng — phiên cộng thứ tự về đích.</summary>
        private long dvKhoaDuong = long.MinValue;

        /// <summary>Mốc đầu pha bơi theo đồng hồ máy.</summary>
        private long dvMocDua;
        private long dvPhienMoc = -1;

        // ---- vùng bấm, ghi lại lúc vẽ ----
        private readonly int[][] dvOThe = new int[DV_SO_VIT][];
        private readonly int[][] dvONut = new int[DV_MUC.Length + 4][];

        // ==================================================================
        //  Ảnh
        // ==================================================================

        private static FrameImage[] fiVit;
        private static int fiVitZoom = -1;

        /// <summary>Nạp năm dải ảnh cho đúng mức zoom đang chạy.</summary>
        /// <remarks>
        /// Thiếu một con thì con đó vẽ bằng khối màu thay thế chứ không làm hỏng
        /// cả màn — nên bắt lỗi riêng từng con.
        /// </remarks>
        private static void napVit()
        {
            int z = mGraphics.zoomLevel;
            if (z < 1)
            {
                z = 1;
            }
            if (z > 4)
            {
                z = 4;
            }
            if (fiVit != null && fiVitZoom == z)
            {
                return;
            }
            fiVitZoom = z;
            fiVit = new FrameImage[DV_SO_VIT];
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                try
                {
                    fiVit[i] = napDai("dv_" + KHOA_VIT[i] + "_x" + z, DV_SO_KHUNG);
                }
                catch (System.Exception)
                {
                    fiVit[i] = null;
                }
            }
        }

        /// <summary>Vẽ một con vịt, neo giữa-đáy. Thiếu ảnh thì vẽ khối màu.</summary>
        private static void veMotVit(mGraphics g, int i, int khung, int x, int yDay)
        {
            FrameImage fi = (fiVit != null && i >= 0 && i < fiVit.Length) ? fiVit[i] : null;
            if (fi != null && fi.imgFrame != null)
            {
                veKhungNeo(g, fi, khung, x, yDay, 0,
                        mGraphics.BOTTOM | mGraphics.HCENTER);
                return;
            }
            g.setColor(MAU_VIT[i], 1f);
            g.fillRect(x - 14, yDay - 18, 28, 16, 8);
            g.fillRect(x + 4, yDay - 30, 14, 14, 7);
            g.setColor(0xF28C1E, 1f);
            g.fillRect(x + 16, yDay - 24, 7, 4, 2);
        }

        // ==================================================================
        //  Đồng hồ và đường bơi
        // ==================================================================

        /// <summary>
        /// Mili giây kể từ đầu pha bơi, -1 khi đang đặt cược.
        /// </summary>
        /// <remarks>
        /// <para>Máy chủ chỉ gửi số giây còn lại, làm tròn lên, mỗi giây một lần.
        /// Lấy thẳng con số đó thì vịt nhảy cóc từng giây. Nên giữ một mốc đầu pha
        /// theo đồng hồ máy rồi tự trôi, và chỉ nắn lại khi lệch quá một giây —
        /// nắn liên tục thì vịt giật lùi mỗi lần gói tin tới trễ.</para>
        ///
        /// <para>Mốc tốt nhất là lúc nhận gói chuyển pha (<c>dnMocBatDauDua</c>).
        /// Mở bảng giữa chừng thì không có mốc ấy, phải suy từ số giây.</para>
        /// </remarks>
        private int dvMsDua()
        {
            God.MiniGame m = God.MiniGame.gI();
            if (m.dnGiaiDoan == 2)
            {
                return DV_GIAY_DUA * 1000;
            }
            if (m.dnGiaiDoan != 1)
            {
                return -1;
            }
            long bayGio = mSystem.currentTimeMillis();
            // Giay con lai lam tron len: da troi qua tu (30 - giay) toi (31 - giay).
            long uoc = m.dnMocNhan - (long) ((DV_GIAY_DUA + 0.5f - m.dnGiay) * 1000f);
            if (dvPhienMoc != m.dnPhien)
            {
                dvPhienMoc = m.dnPhien;
                dvMocDua = (m.dnMocBatDauDua > 0L
                        && bayGio - m.dnMocBatDauDua < DV_GIAY_DUA * 1000L + 1000L)
                        ? m.dnMocBatDauDua : uoc;
            }
            else if (dvMocDua - uoc > 1300L || uoc - dvMocDua > 1300L)
            {
                dvMocDua = uoc;
            }
            long ms = bayGio - dvMocDua;
            if (ms < 0L)
            {
                ms = 0L;
            }
            if (ms > DV_GIAY_DUA * 1000L)
            {
                ms = DV_GIAY_DUA * 1000L;
            }
            return (int) ms;
        }

        /// <summary>Bộ sinh số ngẫu nhiên nhỏ, gieo được — mọi máy ra cùng dãy.</summary>
        private uint dvHat;

        private float dvNgauNhien()
        {
            // xorshift32
            uint x = dvHat;
            x ^= x << 13;
            x ^= x >> 17;
            x ^= x << 5;
            dvHat = x;
            return (x & 0xFFFFFF) / 16777216f;
        }

        private static float dvEm(float a, float b, float u)
        {
            if (u <= a)
            {
                return 0f;
            }
            if (u >= b)
            {
                return 1f;
            }
            float t = (u - a) / (b - a);
            return t * t * (3f - 2f * t);
        }

        /// <summary>Thứ hạng máy chủ gửi có hợp lệ không — đủ năm con, không trùng.</summary>
        private static bool dvThuTuHopLe()
        {
            int[] tt = God.MiniGame.gI().dnThuTu;
            int daCo = 0;
            for (int r = 0; r < DV_SO_VIT; r++)
            {
                int i = tt[r];
                if (i < 0 || i >= DV_SO_VIT || (daCo & (1 << i)) != 0)
                {
                    return false;
                }
                daCo |= 1 << i;
            }
            return true;
        }

        /// <summary>Dựng lại đường bơi nếu phiên hoặc thứ tự về đích vừa đổi.</summary>
        private void dvDungDuong()
        {
            God.MiniGame m = God.MiniGame.gI();
            if (!dvThuTuHopLe())
            {
                return;
            }
            long khoa = m.dnPhien * 100000L;
            for (int r = 0; r < DV_SO_VIT; r++)
            {
                khoa = khoa * 7 + m.dnThuTu[r];
            }
            if (khoa == dvKhoaDuong && dvDuong[0] != null)
            {
                return;
            }
            dvKhoaDuong = khoa;
            dvHat = (uint) (m.dnPhien * 2654435761L) ^ 0x9E3779B9u;
            dvHat ^= (uint) (m.dnThuTu[0] * 40503 + m.dnThuTu[1] * 977 + 17);
            if (dvHat == 0)
            {
                dvHat = 1;
            }
            for (int k = 0; k < 8; k++)
            {
                dvNgauNhien();
            }

            int thang = m.dnThuTu[0];
            for (int r = 0; r < DV_SO_VIT; r++)
            {
                int i = m.dnThuTu[r];
                dvMsVe[i] = DV_MS_VE_NHAT + r * DV_MS_CACH_HANG
                        + (r > 0 ? (int) (dvNgauNhien() * 160f) : 0);
            }
            // Mot nua so van con thang loi nguoc dong: bi bo lai luc dau, bung o
            // khuc cuoi.
            bool buTocCuoi = dvNgauNhien() < 0.55f;
            // Mot con (khong phai con thang) lao len dan dau tu som roi hut hoi.
            int conHutHoi = m.dnThuTu[1 + (int) (dvNgauNhien() * (DV_SO_VIT - 1))];

            for (int i = 0; i < DV_SO_VIT; i++)
            {
                float a1 = 0.20f + dvNgauNhien() * 0.18f;
                float f1 = 0.7f + dvNgauNhien() * 1.2f;
                float p1 = dvNgauNhien() * 6.2832f;
                float a2 = 0.07f + dvNgauNhien() * 0.10f;
                float f2 = 2.2f + dvNgauNhien() * 2.4f;
                float p2 = dvNgauNhien() * 6.2832f;
                float[] s = new float[DV_SO_MAU + 1];
                s[0] = 0f;
                for (int k = 1; k <= DV_SO_MAU; k++)
                {
                    float u = (k - 0.5f) / DV_SO_MAU;
                    float v = 1f
                            + a1 * UnityEngine.Mathf.Sin(6.2832f * f1 * u + p1)
                            + a2 * UnityEngine.Mathf.Sin(6.2832f * f2 * u + p2);
                    if (i == thang && buTocCuoi)
                    {
                        v += 0.62f * dvEm(0.66f, 0.86f, u);
                    }
                    if (i == conHutHoi)
                    {
                        v += 0.45f * (1f - dvEm(0.18f, 0.62f, u));
                    }
                    // Vo toc nhe luc moi tha: khong con nao phong vut ngay khung dau.
                    v *= 0.55f + 0.45f * dvEm(0f, 0.06f, u);
                    if (v < 0.28f)
                    {
                        v = 0.28f;
                    }
                    s[k] = s[k - 1] + v;
                }
                float tong = s[DV_SO_MAU];
                for (int k = 0; k <= DV_SO_MAU; k++)
                {
                    s[k] /= tong;
                }
                dvDuong[i] = s;
            }
        }

        /// <summary>Tiến độ 0..1 của con <paramref name="i"/> ở mốc <paramref name="ms"/>.</summary>
        private float dvTienDo(int i, int ms)
        {
            if (ms <= DV_MS_DEM_NGUOC || dvDuong[i] == null)
            {
                return 0f;
            }
            float u = (float) (ms - DV_MS_DEM_NGUOC) / (dvMsVe[i] - DV_MS_DEM_NGUOC);
            if (u >= 1f)
            {
                return 1f;
            }
            float viTri = u * DV_SO_MAU;
            int k = (int) viTri;
            float du = viTri - k;
            float[] s = dvDuong[i];
            return s[k] + (s[k + 1] - s[k]) * du;
        }

        /// <summary>Toạ độ <b>mỏ</b> con vịt trên đường đua.</summary>
        /// <remarks>
        /// Qua đích rồi thì trôi thêm một đoạn và chậm dần, chứ không đứng khựng
        /// ngay vạch — đứng khựng thì trông như bị chặn lại.
        /// </remarks>
        private float dvMo(int i, int ms)
        {
            if (ms < 0)
            {
                return DV_XUAT_PHAT;
            }
            if (ms >= dvMsVe[i] && dvDuong[i] != null)
            {
                float qua = (ms - dvMsVe[i]) / 750f;
                return DV_DICH + 64f * (1f - UnityEngine.Mathf.Exp(-qua));
            }
            return DV_XUAT_PHAT + dvTienDo(i, ms) * DV_QUANG_DUONG;
        }

        /// <summary>Tốc độ tương đối, 1 = tốc độ trung bình của cả cuộc đua.</summary>
        private float dvTocDo(int i, int ms)
        {
            if (ms <= DV_MS_DEM_NGUOC)
            {
                return 0f;
            }
            float a = dvMo(i, ms);
            float b = dvMo(i, ms + 120);
            float trungBinh = DV_QUANG_DUONG / (float) (DV_MS_VE_NHAT - DV_MS_DEM_NGUOC) * 120f;
            return (b - a) / trungBinh;
        }

        // ==================================================================
        //  Gom tiền
        // ==================================================================

        /// <summary>Con đã đặt trong ván này, -1 nếu chưa đặt con nào.</summary>
        /// <remarks>
        /// Mỗi ván chỉ một con — máy chủ chặn con thứ hai. Rải khắp các con thì
        /// ván nào cũng "thắng" một chút mà thua thì mất cả mấy cửa, trò chơi mất
        /// hết hồi hộp.
        /// </remarks>
        private static int dvConDaDat()
        {
            God.MiniGame m = God.MiniGame.gI();
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                if (m.dnCuaToi[i] > 0L)
                {
                    return i;
                }
            }
            return -1;
        }

        private bool dvChoDat()
        {
            return God.MiniGame.gI().dnGiaiDoan == 0;
        }

        private long dvTranGom()
        {
            God.MiniGame m = God.MiniGame.gI();
            if (dvChon < 0)
            {
                return 0L;
            }
            long daDat = 0L;
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                daDat += m.dnCuaToi[i];
            }
            long tran = m.tongThoi;
            long conCon = DV_TOI_DA_MOI_CON - m.dnCuaToi[dvChon];
            long conVan = DV_TOI_DA_MOT_VAN - daDat;
            if (conCon < tran)
            {
                tran = conCon;
            }
            if (conVan < tran)
            {
                tran = conVan;
            }
            return tran < 0L ? 0L : tran;
        }

        private void dvGom(long them)
        {
            if (dvChon < 0 || !dvChoDat())
            {
                return;
            }
            long tran = dvTranGom();
            dvTienCho += them;
            if (dvTienCho > tran)
            {
                dvTienCho = tran;
            }
            if (dvTienCho < 0L)
            {
                dvTienCho = 0L;
            }
        }

        private static string dvSo(long n)
        {
            if (n < 10000L)
            {
                return n + string.Empty;
            }
            if (n < 1000000L)
            {
                long k10 = n / 100L;
                return (k10 / 10L) + (k10 % 10L != 0 ? "," + (k10 % 10L) : string.Empty) + "K";
            }
            long m10 = n / 100000L;
            return (m10 / 10L) + (m10 % 10L != 0 ? "," + (m10 % 10L) : string.Empty) + "M";
        }

        // ==================================================================
        //  Vẽ
        // ==================================================================

        private void veDuaVit(mGraphics g)
        {
            napVit();
            God.MiniGame m = God.MiniGame.gI();
            if (!dvChoDat())
            {
                dvTienCho = 0L;
            }
            dvDungDuong();
            int ms = dvMsDua();
            int daDat = dvConDaDat();
            if (daDat >= 0 && dvChoDat())
            {
                // Moi van mot con: da dat con nao thi chi con cong them vao con do.
                dvChon = daDat;
            }

            int x = x0 + LE;
            int w = rong - LE * 2;
            // Duoi hang the con (Dat cuoc / Lich su cua toi / Lich su may chu).
            int yTren = yNoiDung() - 2;
            int yDuoi = y0 + cao - LE;
            int h = yDuoi - yTren;

            const int CAO_DAU = 18;
            const int CAO_NUT = 20;
            int caoThe = h * 22 / 100;
            if (caoThe > 58)
            {
                caoThe = 58;
            }
            if (caoThe < 34)
            {
                caoThe = 34;
            }
            int caoCanh = h - CAO_DAU - caoThe - CAO_NUT - 12;
            if (caoCanh < 70)
            {
                caoThe = 34;
                caoCanh = h - CAO_DAU - caoThe - CAO_NUT - 12;
            }

            veDauDuaVit(g, x, yTren, w, CAO_DAU);
            int yCanh = yTren + CAO_DAU + 4;
            veCanhDuaVit(g, x, yCanh, w, caoCanh, ms);
            int yThe = yCanh + caoCanh + 4;
            veHangTheVit(g, x, yThe, w, caoThe, ms);
            veHangNutVit(g, x, yThe + caoThe + 4, w, CAO_NUT);
        }

        // ---- hàng đầu: phiên, lịch sử, đồng hồ, thỏi vàng ----

        private void veDauDuaVit(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            string phienChu = "Phiên " + m.dnPhien;
            mFont.tahoma_7b_dark.drawString(g, phienChu, x, y + 3, mFont.LEFT);

            // Dong ho giua hang.
            int giay = m.dnGiay;
            if (giay < 0)
            {
                giay = 0;
            }
            string nhan;
            int mau;
            int tong;
            if (m.dnGiaiDoan == 0)
            {
                nhan = "ĐẶT CƯỢC";
                mau = giay <= 5 ? MAU_XIU_SANG : MAU_TAI_SANG;
                tong = DV_GIAY_DAT;
            }
            else if (m.dnGiaiDoan == 1)
            {
                nhan = "ĐANG BƠI";
                mau = MAU_VUNG_TOT;
                tong = DV_GIAY_DUA;
            }
            else
            {
                nhan = "KẾT QUẢ";
                mau = rgb(0xD8, 0x9A, 0x1E);
                tong = DV_GIAY_KQ;
            }
            nhan += "  00:" + (giay < 10 ? "0" : string.Empty) + giay;
            int rongDH = 132;
            int xDH = x + w / 2 - rongDH / 2;
            bool nhay = m.dnGiaiDoan == 0 && giay <= 5
                    && mSystem.currentTimeMillis() / 250 % 2 == 0;
            if (nhay)
            {
                veQuang(g, xDH, y, rongDH, 17, MAU_XIU_SANG, 0.9f, 3);
            }
            veTheDem(g, xDH, y, rongDH, nhan, mau);
            // Vach tien do chay duoi dong ho.
            float tiLe = tong > 0 ? 1f - (float) giay / tong : 0f;
            if (tiLe < 0f)
            {
                tiLe = 0f;
            }
            g.setColor(0x000000, 0.25f);
            g.fillRect(xDH + 6, y + 15, rongDH - 12, 2, 1);
            g.setColor(MAU_VANG, 0.95f);
            g.fillRect(xDH + 6, y + 15, (int) ((rongDH - 12) * tiLe), 2, 1);

            // Thoi vang, goc phai.
            string so = dvSo(m.tongThoi);
            int rongSo = mFont.tahoma_7b_dark.getWidth(so);
            mFont.tahoma_7b_dark.drawString(g, so, x + w, y + 3, mFont.RIGHT);
            // Tam icon cach mep chu nua be ngang icon (~22) cong mot khe: dat sat
            // hon thi thoi vang de len chu so dau tien.
            SmallImage.drawSmallImage(g, ICON_THOI_VANG, x + w - rongSo - 16, y + 8, 0,
                    mGraphics.VCENTER | mGraphics.HCENTER);

            // Lich su con thang, giua so phien va dong ho.
            int xLs = x + mFont.tahoma_7b_dark.getWidth(phienChu) + 8;
            int hetCho = xDH - 6;
            int co = 11;
            for (int k = 0; k < m.dnLichSu.Count; k++)
            {
                int cx = xLs + k * (co + 2);
                if (cx + co > hetCho)
                {
                    break;
                }
                int con = m.dnLichSu[k];
                if (con < 0 || con >= DV_SO_VIT)
                {
                    continue;
                }
                veHuyHieuVit(g, cx + co / 2, y + 8, co, con, k == 0);
            }
        }

        /// <summary>Huy hiệu tròn mang số làn và màu con vịt.</summary>
        private static void veHuyHieuVit(mGraphics g, int tamX, int tamY, int co,
                int con, bool sang)
        {
            if (sang)
            {
                g.setColor(MAU_VANG, 0.95f);
                g.fillRect(tamX - co / 2 - 1, tamY - co / 2 - 1, co + 2, co + 2, co / 2 + 1);
            }
            else
            {
                g.setColor(0xFFFFFF, 0.9f);
                g.fillRect(tamX - co / 2 - 1, tamY - co / 2 - 1, co + 2, co + 2, co / 2 + 1);
            }
            g.setColor(MAU_VIT[con], 1f);
            g.fillRect(tamX - co / 2, tamY - co / 2, co, co, co / 2);
            g.setColor(0xFFFFFF, 0.35f);
            g.fillRect(tamX - co / 2 + 2, tamY - co / 2 + 1, co - 4, co / 3, co / 3);
            mFont.tahoma_7b_white.drawString(g, (con + 1) + string.Empty, tamX,
                    tamY - 5, mFont.CENTER);
        }

        // ---- khung cảnh ----

        /// <summary>Bảng màu khung cảnh: trời trưa, cỏ non, nước hồ trong.</summary>
        private static readonly int DV_TROI_CAO = rgb(0x5C, 0xB4, 0xEC);
        private static readonly int DV_TROI_THAP = rgb(0xCF, 0xEE, 0xFF);
        private static readonly int DV_DOI_XA = rgb(0x8C, 0xC8, 0x72);
        private static readonly int DV_DOI_GAN = rgb(0x5E, 0xAE, 0x4C);
        private static readonly int DV_CO = rgb(0x6B, 0xBE, 0x4E);
        private static readonly int DV_CO_TOI = rgb(0x3F, 0x8C, 0x34);
        private static readonly int DV_CAT = rgb(0xEE, 0xD9, 0x9C);
        private static readonly int DV_NUOC_MAT = rgb(0x4C, 0xC0, 0xE4);
        private static readonly int DV_NUOC_SAU = rgb(0x17, 0x6E, 0xB0);
        private static readonly int DV_GO = rgb(0x8A, 0x5A, 0x30);

        /// <summary>Bốn màu dây cờ đuôi nheo trên bờ.</summary>
        private static readonly int[] DV_MAU_CO = { 0xE8453C, 0xFFD23F, 0x3FA7E8, 0x5CCB5F };

        /// <summary>Hàm băm số nguyên, để rải cây, sóng, lau sậy cố định theo chỗ.</summary>
        private static int dvBam(int k)
        {
            uint x = (uint) k * 2654435761u;
            x ^= x >> 15;
            x *= 2246822519u;
            x ^= x >> 13;
            return (int) (x & 0x7FFFFFFF);
        }

        private void veCanhDuaVit(mGraphics g, int x, int y, int w, int h, int ms)
        {
            God.MiniGame m = God.MiniGame.gI();
            long bayGio = mSystem.currentTimeMillis();

            // Vi tri moi con, va may quay theo con dan dau.
            float[] mo = new float[DV_SO_VIT];
            float moDau = DV_XUAT_PHAT;
            int conDau = -1;
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                mo[i] = dvMo(i, ms);
                if (mo[i] > moDau)
                {
                    moDau = mo[i];
                    conDau = i;
                }
            }
            float cam = 0f;
            if (ms >= 0)
            {
                cam = moDau - w * 0.70f;
                float camToiDa = DV_DICH + 118 - w;
                if (cam > camToiDa)
                {
                    cam = camToiDa;
                }
                if (cam < 0f)
                {
                    cam = 0f;
                }
            }
            int c = (int) cam;

            int hTroi = h * 17 / 100;
            int hBo = h * 13 / 100;
            int hBoGan = h * 8 / 100;
            if (hBoGan < 7)
            {
                hBoGan = 7;
            }
            int yNuoc = y + hTroi + hBo;
            int hNuoc = h - hTroi - hBo - hBoGan;
            int caoLan = hNuoc / DV_SO_VIT;

            // Khung vien kep bao ca canh.
            veKhungKep(g, x - 3, y - 3, w + 6, h + 6, DV_NUOC_SAU, MAU_VIEN, 1f);
            g.setClip(x, y, w, h);

            // Troi.
            veDaiMau(g, x, y, w, hTroi + hBo, DV_TROI_CAO, DV_TROI_THAP, 6);
            // Mat troi va quang.
            int xMt = x + w - 34;
            int yMt = y + 10;
            for (int r = 4; r >= 1; r--)
            {
                g.setColor(0xFFF3B0, 0.10f * (5 - r));
                g.fillRect(xMt - 8 - r * 4, yMt - 8 - r * 4, 16 + r * 8, 16 + r * 8, 8 + r * 4);
            }
            g.setColor(0xFFE680, 1f);
            g.fillRect(xMt - 8, yMt - 8, 16, 16, 8);
            // May troi cham, theo thi sai rat xa.
            for (int k = 0; k < 5; k++)
            {
                int dai = 26 + dvBam(k * 31) % 22;
                int vong = w + 80;
                int xm = x + (int) (((dvBam(k * 17) % vong) - cam * 0.08f
                        - bayGio / 90L % vong + vong * 4) % vong) - 40;
                int ym = y + 3 + dvBam(k * 7) % (hTroi > 8 ? hTroi - 6 : 2);
                g.setColor(0xFFFFFF, 0.85f);
                g.fillRect(xm, ym + 3, dai, 6, 3);
                g.fillRect(xm + dai / 5, ym, dai / 2, 7, 3);
            }

            // Doi xa (thi sai 0.2) va doi gan (0.4).
            int yBo = y + hTroi;
            for (int lop = 0; lop < 2; lop++)
            {
                float ts = lop == 0 ? 0.2f : 0.4f;
                int buoc = lop == 0 ? 90 : 70;
                int mau = lop == 0 ? DV_DOI_XA : DV_DOI_GAN;
                int dauK = (int) (cam * ts) / buoc - 1;
                for (int k = dauK; k < dauK + w / buoc + 3; k++)
                {
                    int xd = x + k * buoc - (int) (cam * ts);
                    int rd = buoc + 30 + dvBam(k * 13 + lop) % 40;
                    int cd = hBo / 2 + 4 + dvBam(k * 29 + lop) % (hBo / 2 + 4);
                    g.setColor(mau, 1f);
                    g.fillRect(xd - 15, yBo + hBo - cd + lop * 3, rd, cd + 8, cd);
                }
            }
            // Cay tren bo (thi sai 0.65).
            {
                float ts = 0.65f;
                const int buoc = 46;
                int dauK = (int) (cam * ts) / buoc - 1;
                for (int k = dauK; k < dauK + w / buoc + 3; k++)
                {
                    if (dvBam(k * 5 + 3) % 3 == 0)
                    {
                        continue;
                    }
                    int xc = x + k * buoc + dvBam(k * 11) % 20 - (int) (cam * ts);
                    int cc = 10 + dvBam(k * 19) % 10;
                    int yGoc = yNuoc - 3;
                    g.setColor(0x6A4020, 1f);
                    g.fillRect(xc - 1, yGoc - cc / 2 - 2, 3, cc / 2 + 2);
                    g.setColor(DV_CO_TOI, 1f);
                    g.fillRect(xc - cc / 2, yGoc - cc - 3, cc, cc, cc / 2);
                    g.setColor(DV_CO, 1f);
                    g.fillRect(xc - cc / 2 + 1, yGoc - cc - 3, cc - 3, cc - 3, cc / 2);
                }
            }
            // Co bo xa va day co day chang (thi sai 0.85).
            g.setColor(DV_CO, 1f);
            g.fillRect(x, yNuoc - 5, w, 5);
            g.setColor(DV_CAT, 1f);
            g.fillRect(x, yNuoc - 2, w, 2);
            {
                float ts = 0.85f;
                const int buoc = 9;
                int yDay = yBo + 4;
                g.setColor(0xFFFFFF, 0.55f);
                g.fillRect(x, yDay, w, 1);
                int dauK = (int) (cam * ts) / buoc - 1;
                for (int k = dauK; k < dauK + w / buoc + 3; k++)
                {
                    int xc = x + k * buoc - (int) (cam * ts);
                    g.setColor(DV_MAU_CO[((k % 4) + 4) % 4], 0.95f);
                    g.fillRect(xc, yDay + 1, 5, 4, 1);
                    g.fillRect(xc + 1, yDay + 5, 3, 1);
                }
            }

            // Bien bao quang duong tren bo xa (the gioi that, thi sai 1).
            for (int met = 50; met < DV_MET; met += 50)
            {
                int wx = DV_XUAT_PHAT + met * DV_QUANG_DUONG / DV_MET;
                int sxB = x + wx - c;
                if (sxB < x - 30 || sxB > x + w + 30)
                {
                    continue;
                }
                string chu = met + "m";
                int rb = mFont.tahoma_7b_dark.getWidth(chu) + 8;
                g.setColor(DV_GO, 1f);
                g.fillRect(sxB - 1, yNuoc - 14, 2, 12);
                g.setColor(0xFFFFFF, 1f);
                g.fillRect(sxB - rb / 2, yNuoc - 24, rb, 12, 3);
                g.setColor(DV_GO, 1f);
                g.fillRect(sxB - rb / 2, yNuoc - 13, rb, 1);
                mFont.tahoma_7b_dark.drawString(g, chu, sxB, yNuoc - 24, mFont.CENTER);
            }

            // Nuoc.
            veDaiMau(g, x, yNuoc, w, hNuoc + hBoGan, DV_NUOC_MAT, DV_NUOC_SAU, 10);
            // Dai lan xen ke sang toi.
            for (int l = 0; l < DV_SO_VIT; l++)
            {
                if (l % 2 == 1)
                {
                    g.setColor(0xFFFFFF, 0.05f);
                    g.fillRect(x, yNuoc + l * caoLan, w, caoLan);
                }
            }
            // Gon song troi theo the gioi: may quay chay la song lui, ra cam giac toc do.
            {
                const int buoc = 23;
                int troi = (int) (bayGio / 70L % buoc);
                int dauK = (c + troi) / buoc - 1;
                for (int k = dauK; k < dauK + w / buoc + 3; k++)
                {
                    for (int tang = 0; tang < 2; tang++)
                    {
                        int bam = dvBam(k * 3 + tang * 7919);
                        int xs = x + k * buoc - c - troi + bam % 11;
                        int ys = yNuoc + 3 + bam / 11 % (hNuoc > 6 ? hNuoc - 4 : 2);
                        int dai = 7 + bam / 101 % 10;
                        float anh = 0.10f + (bam / 7 % 10) * 0.015f;
                        g.setColor(0xFFFFFF, anh);
                        g.fillRect(xs, ys, dai, 1);
                    }
                }
            }
            // Day phao chia lan.
            for (int l = 1; l < DV_SO_VIT; l++)
            {
                int yd = yNuoc + l * caoLan;
                g.setColor(0xFFFFFF, 0.25f);
                g.fillRect(x, yd, w, 1);
                const int buoc = 12;
                int dauK = c / buoc - 1;
                for (int k = dauK; k < dauK + w / buoc + 3; k++)
                {
                    int xp = x + k * buoc - c;
                    g.setColor(k % 2 == 0 ? 0xE8453C : 0xFFFFFF, 0.95f);
                    g.fillRect(xp - 2, yd - 1, 5, 3, 1);
                }
            }

            // Vach xuat phat.
            {
                int sxV = x + DV_XUAT_PHAT - c;
                if (sxV > x - 60 && sxV < x + w + 60)
                {
                    g.setColor(0xFFFFFF, 0.85f);
                    g.fillRect(sxV - 1, yNuoc, 2, hNuoc);
                    g.setColor(DV_GO, 1f);
                    g.fillRect(sxV - 1, yNuoc - 18, 2, 18);
                    string chu = "XUẤT PHÁT";
                    int rb = mFont.tahoma_7b_white.getWidth(chu) + 10;
                    g.setColor(0x2E9E4A, 1f);
                    g.fillRect(sxV - rb / 2, yNuoc - 30, rb, 13, 4);
                    veChuyenSac(g, sxV - rb / 2, yNuoc - 30, rb, 13, 3);
                    mFont.tahoma_7b_white.drawString(g, chu, sxV, yNuoc - 29, mFont.CENTER);
                }
            }
            // Vach dich: bang co ca ro, hai cot, bang ron do.
            {
                int sxD = x + DV_DICH - c;
                if (sxD > x - 80 && sxD < x + w + 80)
                {
                    const int o = 3;
                    for (int yy = 0; yy < hNuoc; yy += o)
                    {
                        for (int cot = 0; cot < 2; cot++)
                        {
                            bool den = ((yy / o) + cot) % 2 == 0;
                            g.setColor(den ? 0x1A1A1A : 0xFFFFFF, 0.92f);
                            g.fillRect(sxD - 3 + cot * o, yNuoc + yy, o, o);
                        }
                    }
                    int yBang = y + 3;
                    int cotTrai = sxD - 34;
                    int cotPhai = sxD + 34;
                    g.setColor(0xFFFFFF, 1f);
                    g.fillRect(cotTrai - 1, yBang, 3, yNuoc - yBang);
                    g.fillRect(cotPhai - 1, yBang, 3, yNuoc - yBang);
                    for (int yy = yBang; yy < yNuoc; yy += 8)
                    {
                        g.setColor(0xE8453C, 1f);
                        g.fillRect(cotTrai - 1, yy, 3, 4);
                        g.fillRect(cotPhai - 1, yy, 3, 4);
                    }
                    g.setColor(0x000000, 0.25f);
                    g.fillRect(cotTrai + 1, yBang + 3, cotPhai - cotTrai, 14, 3);
                    g.setColor(0xD8352A, 1f);
                    g.fillRect(cotTrai - 2, yBang + 1, cotPhai - cotTrai + 4, 14, 3);
                    veChuyenSac(g, cotTrai - 2, yBang + 1, cotPhai - cotTrai + 4, 14, 3);
                    g.setColor(MAU_VANG, 0.9f);
                    g.fillRect(cotTrai, yBang + 13, cotPhai - cotTrai, 1);
                    mFont.tahoma_7b_white.drawString(g, "ĐÍCH", sxD, yBang + 2, mFont.CENTER);
                }
            }

            // Vit: lan tren ve truoc de con lan duoi de len (gan may quay hon).
            int[] hang = dvHangHienTai(mo);
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                veVitTrenLan(g, x, w, i, ms, mo[i], c, yNuoc + (i + 1) * caoLan - 2,
                        bayGio, hang[i], i == conDau);
            }

            // Bo gan: co va lau say (thi sai 1.25 — lop gan nhat chay nhanh nhat).
            int yBoGan = yNuoc + hNuoc;
            g.setColor(DV_CAT, 1f);
            g.fillRect(x, yBoGan, w, 2);
            g.setColor(DV_CO, 1f);
            g.fillRect(x, yBoGan + 2, w, hBoGan);
            {
                float ts = 1.25f;
                const int buoc = 7;
                int dauK = (int) (cam * ts) / buoc - 1;
                for (int k = dauK; k < dauK + w / buoc + 3; k++)
                {
                    int bam = dvBam(k * 23 + 5);
                    int xl = x + k * buoc - (int) (cam * ts) + bam % 5;
                    int cl = 6 + bam / 5 % 11;
                    g.setColor(bam % 3 == 0 ? DV_CO_TOI : rgb(0x4E, 0xA2, 0x3E), 1f);
                    g.fillRect(xl, yBoGan + 4 - cl, 2, cl + 2, 1);
                    if (bam % 7 == 0)
                    {
                        g.setColor(0x7A4A22, 1f);
                        g.fillRect(xl - 1, yBoGan + 2 - cl, 4, 5, 2);
                    }
                }
            }

            // Lop phu tren cung.
            veBanDoNho(g, x, y, w, mo);
            if (m.dnGiaiDoan == 0)
            {
                veLoiMoiDatCuoc(g, x, y, w, h);
            }
            else if (m.dnGiaiDoan == 1)
            {
                veBangDanDau(g, x, y, w, mo);
                veDemNguocDua(g, x, y, w, h, ms);
                veVeDich(g, x, yNuoc, caoLan, ms, mo, c);
            }
            else
            {
                veKetQuaDuaVit(g, x, y, w, h);
            }

            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        }

        /// <summary>Thứ hạng hiện tại theo vị trí — 0 là dẫn đầu.</summary>
        private int[] dvHangHienTai(float[] mo)
        {
            int[] hang = new int[DV_SO_VIT];
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                int r = 0;
                for (int j = 0; j < DV_SO_VIT; j++)
                {
                    if (mo[j] > mo[i] || (mo[j] == mo[i] && j < i))
                    {
                        r++;
                    }
                }
                hang[i] = r;
            }
            return hang;
        }

        private void veVitTrenLan(mGraphics g, int x, int w, int i, int ms, float mo,
                int c, int yDay, long bayGio, int hang, bool dauBang)
        {
            God.MiniGame m = God.MiniGame.gI();
            int tamX = x + (int) mo - DV_MO_TOI_TAM - c;
            // Con bi bo xa ngoai mep trai: ve mui ten bao o mep.
            if (tamX < x - 24)
            {
                g.setColor(MAU_VIT[i], 0.95f);
                g.fillRect(x + 2, yDay - 12, 10, 10, 5);
                g.setColor(0xFFFFFF, 1f);
                g.fillRect(x + 4, yDay - 8, 5, 2);
                g.fillRect(x + 4, yDay - 9, 2, 4);
                return;
            }
            if (tamX > x + w + 30)
            {
                return;
            }

            float toc = dvTocDo(i, ms);
            bool dangBoi = ms > DV_MS_DEM_NGUOC && toc > 0.05f;
            int khung;
            int nhun;
            if (dangBoi)
            {
                // Quat chan nhanh cham theo toc do: vit dang but thi chan quat dit.
                int chuKy = (int) (95f / (toc < 0.4f ? 0.4f : (toc > 1.8f ? 1.8f : toc)));
                khung = 1 + (int) ((bayGio / chuKy + i * 2) % 6);
                nhun = (int) (UnityEngine.Mathf.Sin(bayGio / 110f + i * 1.7f) * 1.4f);
            }
            else
            {
                khung = (bayGio / 650L + i) % 2 == 0 ? 0 : 7;
                nhun = (int) (UnityEngine.Mathf.Sin(bayGio / 380f + i * 1.3f) * 1.2f);
            }

            // Vet nuoc re sau duoi, dai theo toc do.
            if (dangBoi)
            {
                int dai = 8 + (int) (toc * 10f);
                for (int k = 0; k < 3; k++)
                {
                    int lech = (int) ((bayGio / 60L + k * 3 + i) % 4);
                    g.setColor(0xFFFFFF, 0.34f - k * 0.1f);
                    g.fillRect(tamX - 16 - k * (dai / 2 + 4) - dai - lech, yDay - 4 + k,
                            dai, 3, 1);
                }
            }
            // Bong duoi bung.
            g.setColor(DV_NUOC_SAU, 0.35f);
            g.fillRect(tamX - 16, yDay - 3, 32, 4, 2);

            veMotVit(g, i, khung, tamX, yDay + nhun);

            // Nhan tren dau: "BẠN" cho con minh da dat, huy chuong cho con dan dau.
            // Tren dinh dau (vit cao 40) mot khoang, khong de len toc.
            int yNhan = yDay - 56 + nhun;
            if (ms < 0)
            {
                // Cho xuat phat: bien ten nho canh mo, de nhan ra con nao la con nao.
                string ten = TEN_NGAN_VIT[i];
                int rt = mFont.tahoma_7b_white.getWidth(ten) + 10;
                int xt = tamX + 24;
                g.setColor(0x000000, 0.3f);
                g.fillRect(xt + 1, yDay - 21, rt, 12, 5);
                g.setColor(MAU_VIT[i], 0.95f);
                g.fillRect(xt, yDay - 22, rt, 12, 5);
                g.setColor(0xFFFFFF, 0.25f);
                g.fillRect(xt + 2, yDay - 21, rt - 4, 4, 2);
                mFont.tahoma_7b_white.drawString(g, ten, xt + rt / 2, yDay - 22, mFont.CENTER);
            }
            bool cuaToi = m.dnCuaToi[i] > 0L;
            if (dauBang && ms > DV_MS_DEM_NGUOC)
            {
                g.setColor(0x000000, 0.3f);
                g.fillRect(tamX + 7, yNhan + 1, 12, 12, 6);
                g.setColor(MAU_VANG, 1f);
                g.fillRect(tamX + 6, yNhan, 12, 12, 6);
                g.setColor(0xFFFFFF, 0.45f);
                g.fillRect(tamX + 8, yNhan + 1, 8, 4, 2);
                mFont.tahoma_7b_dark.drawString(g, "1", tamX + 12, yNhan, mFont.CENTER);
            }
            if (cuaToi)
            {
                int rn = 26;
                int xn = tamX - rn / 2 - 4;
                g.setColor(0x000000, 0.3f);
                g.fillRect(xn + 1, yNhan + 1, rn, 12, 4);
                g.setColor(rgb(0x2E, 0xA0, 0x4A), 1f);
                g.fillRect(xn, yNhan, rn, 12, 4);
                g.fillRect(xn + rn / 2 - 2, yNhan + 12, 4, 2);
                g.fillRect(xn + rn / 2 - 1, yNhan + 14, 2, 1);
                mFont.tahoma_7b_white.drawString(g, "BẠN", xn + rn / 2, yNhan, mFont.CENTER);
            }
        }

        /// <summary>Bản đồ nhỏ góc trên phải: cả đường đua với năm chấm màu.</summary>
        private void veBanDoNho(mGraphics g, int x, int y, int w, float[] mo)
        {
            int xb = x + w * 55 / 100;
            int rb = w - (xb - x) - 10;
            int yb = y + 5;
            g.setColor(0x0A2A40, 0.45f);
            g.fillRect(xb - 6, yb - 3, rb + 12, 13, 6);
            g.setColor(0xFFFFFF, 0.5f);
            g.fillRect(xb, yb + 3, rb, 2, 1);
            // Co dich o cuoi.
            for (int k = 0; k < 3; k++)
            {
                g.setColor(k % 2 == 0 ? 0xFFFFFF : 0x1A1A1A, 1f);
                g.fillRect(xb + rb - 2, yb - 1 + k * 3, 3, 3);
            }
            // Ve con chay sau truoc, con dan dau nam tren cung.
            int[] hang = dvHangHienTai(mo);
            for (int r = DV_SO_VIT - 1; r >= 0; r--)
            {
                for (int i = 0; i < DV_SO_VIT; i++)
                {
                    if (hang[i] != r)
                    {
                        continue;
                    }
                    float p = (mo[i] - DV_XUAT_PHAT) / DV_QUANG_DUONG;
                    if (p < 0f)
                    {
                        p = 0f;
                    }
                    if (p > 1f)
                    {
                        p = 1f;
                    }
                    int cx = xb + (int) (p * rb);
                    g.setColor(0xFFFFFF, 1f);
                    g.fillRect(cx - 4, yb, 8, 8, 4);
                    g.setColor(MAU_VIT[i], 1f);
                    g.fillRect(cx - 3, yb + 1, 6, 6, 3);
                }
            }
        }

        /// <summary>Góc trên trái lúc bơi: con dẫn đầu và hàng thứ hạng.</summary>
        private void veBangDanDau(mGraphics g, int x, int y, int w, float[] mo)
        {
            int[] hang = dvHangHienTai(mo);
            int dau = 0;
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                if (hang[i] == 0)
                {
                    dau = i;
                }
            }
            string chu = TEN_VIT[dau];
            int rc = mFont.tahoma_7b_white.getWidth(chu);
            int rb = 16 + rc + 6 + (DV_SO_VIT - 1) * 11 + 6;
            g.setColor(0x0A2A40, 0.5f);
            g.fillRect(x + 4, y + 3, rb, 16, 7);
            veHuyHieuVit(g, x + 12, y + 11, 11, dau, true);
            mFont.tahoma_7b_white.drawString(g, chu, x + 20, y + 5, mFont.LEFT);
            int xk = x + 20 + rc + 8;
            for (int r = 1; r < DV_SO_VIT; r++)
            {
                for (int i = 0; i < DV_SO_VIT; i++)
                {
                    if (hang[i] == r)
                    {
                        g.setColor(0xFFFFFF, 0.9f);
                        g.fillRect(xk - 1, y + 6, 10, 10, 5);
                        g.setColor(MAU_VIT[i], 1f);
                        g.fillRect(xk, y + 7, 8, 8, 4);
                        xk += 11;
                    }
                }
            }
        }

        /// <summary>Đếm 3-2-1 rồi "BƠI!" ở đầu pha bơi.</summary>
        private void veDemNguocDua(mGraphics g, int x, int y, int w, int h, int ms)
        {
            int tamX = x + w / 2;
            int tamY = y + h / 2 - 6;
            if (ms < DV_MS_DEM_NGUOC)
            {
                int buoc = DV_MS_DEM_NGUOC / 3;
                int so = 3 - ms / buoc;
                float t = (ms % buoc) / (float) buoc;
                int co = 26 + (int) (18 * t);
                g.setColor(0x000000, 0.35f * (1f - t));
                g.fillRect(tamX - co, tamY - co, co * 2, co * 2, co);
                g.setColor(MAU_VANG, 0.55f * (1f - t));
                g.fillRect(tamX - co + 3, tamY - co + 3, co * 2 - 6, co * 2 - 6, co - 3);
                mFont.bigNumber_yellow.drawString(g, so + string.Empty, tamX, tamY - 7,
                        mFont.CENTER);
                return;
            }
            if (ms < DV_MS_DEM_NGUOC + 900)
            {
                float t = (ms - DV_MS_DEM_NGUOC) / 900f;
                int rb = 70 + (int) (20 * t);
                g.setColor(0x000000, 0.3f * (1f - t));
                g.fillRect(tamX - rb / 2 + 2, tamY - 9, rb, 22, 8);
                g.setColor(rgb(0xE8, 0x45, 0x3C), 1f - t * 0.6f);
                g.fillRect(tamX - rb / 2, tamY - 11, rb, 22, 8);
                mFont.tahoma_7b_white.drawString(g, "BƠI!", tamX, tamY - 5, mFont.CENTER);
                veHatSang(g, tamX, tamY, 10, 40 + (int) (20 * t), MAU_VANG, 1f - t, 900);
            }
        }

        /// <summary>Loé sáng ở vạch đích khi từng con chạm vạch.</summary>
        private void veVeDich(mGraphics g, int x, int yNuoc, int caoLan, int ms,
                float[] mo, int c)
        {
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                int qua = ms - dvMsVe[i];
                if (qua < 0 || qua > 1400 || dvDuong[i] == null)
                {
                    continue;
                }
                int sx = x + DV_DICH - c;
                int sy = yNuoc + i * caoLan + caoLan / 2 - 10;
                float t = qua / 1400f;
                bool nhat = God.MiniGame.gI().dnThuTu[0] == i;
                veHatSang(g, sx, sy, nhat ? 12 : 6, (int) ((nhat ? 34 : 18) * (0.4f + t)),
                        nhat ? MAU_VANG : 0xFFFFFF, 1f - t, 1400);
                if (nhat)
                {
                    int rb = 64;
                    g.setColor(0x000000, 0.35f * (1f - t));
                    g.fillRect(sx - rb / 2 + 1, sy - 26, rb, 14, 5);
                    g.setColor(MAU_VANG, 1f - t * 0.7f);
                    g.fillRect(sx - rb / 2, sy - 27, rb, 14, 5);
                    mFont.tahoma_7b_dark.drawString(g, "VỀ NHẤT!", sx, sy - 26, mFont.CENTER);
                }
            }
        }

        /// <summary>Lời mời trong pha đặt cược, dưới cùng khung cảnh.</summary>
        private void veLoiMoiDatCuoc(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            int daDat = dvConDaDat();
            string chu = daDat >= 0
                    ? "Bạn đã đặt " + TEN_VIT[daDat] + " — xuất phát sau " + m.dnGiay + " giây"
                    : dvChon < 0
                    ? "Chọn một chú vịt — mỗi ván đặt được một con"
                    : "Xuất phát sau " + m.dnGiay + " giây — vịt về nhất ăn x4,5";
            int rc = mFont.tahoma_7b_white.getWidth(chu) + 18;
            int xc = x + w / 2 - rc / 2;
            int yc = y + h - 22;
            bool gap = m.dnGiay <= 5;
            g.setColor(0x000000, 0.3f);
            g.fillRect(xc + 1, yc + 1, rc, 16, 7);
            g.setColor(gap ? rgb(0xB8, 0x2E, 0x26) : rgb(0x0A, 0x3A, 0x5C), 0.82f);
            g.fillRect(xc, yc, rc, 16, 7);
            mFont.tahoma_7b_white.drawString(g, chu, x + w / 2, yc + 2, mFont.CENTER);
        }

        /// <summary>Bảng kết quả giữa khung cảnh, trong pha kết quả.</summary>
        private void veKetQuaDuaVit(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            if (!dvThuTuHopLe())
            {
                return;
            }
            int nhat = m.dnThuTu[0];
            int rb = Math.min(270, w - 24);
            int cb = Math.min(96, h - 14);
            int xb = x + (w - rb) / 2;
            int yb = y + (h - cb) / 2;

            g.setColor(0x000000, 0.28f);
            g.fillRect(x, y, w, h);
            veQuang(g, xb, yb, rb, cb, MAU_VANG, 0.9f, 4);
            veKhungKep(g, xb, yb, rb, cb, rgb(0xFF, 0xF4, 0xDC), MAU_VANG, 1f);
            veGocKhung(g, xb, yb, rb, cb, 0.9f);
            // Ruy bang tieu de.
            g.setColor(MAU_VIT[nhat], 1f);
            g.fillRect(xb + 3, yb + 3, rb - 6, 16, BO_GOC);
            veChuyenSac(g, xb + 3, yb + 3, rb - 6, 16, 4);
            veChuNoi(g, mFont.tahoma_7b_white, "VỀ NHẤT", xb + rb / 2, yb + 5, mFont.CENTER);

            // Con thang, voi quang no va hat sang.
            int xVit = xb + 42;
            int yVit = yb + cb - 16;
            veVongNo(g, xVit, yVit - 20, 20, 64, MAU_VANG, 1400);
            veHatSang(g, xVit, yVit - 20, 9, 34, MAU_VANG, 0.95f, 1600);
            long bayGio = mSystem.currentTimeMillis();
            int khung = 1 + (int) (bayGio / 110L % 6);
            veMotVit(g, nhat, khung, xVit, yVit);

            int xc = xb + 84;
            int yc = yb + 24;
            mFont.tahoma_7b_red.drawString(g, TEN_VIT[nhat], xc, yc, mFont.LEFT);
            mFont.tahoma_7_grey.drawString(g, "Trả x4,5 tiền cược", xc, yc + 12, mFont.LEFT);
            string nhiBa = "Nhì: " + TEN_NGAN_VIT[m.dnThuTu[1]]
                    + "   Ba: " + TEN_NGAN_VIT[m.dnThuTu[2]];
            mFont.tahoma_7b_dark.drawString(g, nhiBa, xc, yc + 24, mFont.LEFT);

            long datThang = m.dnCuaToi[nhat];
            long tongDat = 0L;
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                tongDat += m.dnCuaToi[i];
            }
            if (tongDat > 0L && cb >= 80)
            {
                if (datThang > 0L)
                {
                    long ve = (datThang * DV_HE_SO_X10 + 5L) / 10L;
                    mFont.tahoma_7b_green2.drawString(g, "Bạn thắng +" + dvSo(ve) + " thỏi vàng!",
                            xc, yc + 38, mFont.LEFT);
                }
                else
                {
                    mFont.tahoma_7b_red.drawString(g, "Bạn thua " + dvSo(tongDat) + " thỏi vàng",
                            xc, yc + 38, mFont.LEFT);
                }
            }
        }

        // ---- hàng thẻ vịt ----

        private void veHangTheVit(mGraphics g, int x, int y, int w, int h, int ms)
        {
            God.MiniGame m = God.MiniGame.gI();
            const int khe = 5;
            int rt = (w - khe * (DV_SO_VIT - 1)) / DV_SO_VIT;
            long bayGio = mSystem.currentTimeMillis();
            bool ketQua = m.dnGiaiDoan == 2 && dvThuTuHopLe();
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                int xt = x + i * (rt + khe);
                dvOThe[i] = new int[] { xt, y, rt, h };
                bool chon = dvChon == i;
                bool thang = ketQua && m.dnThuTu[0] == i;

                if (thang)
                {
                    float nhip = 0.6f + 0.4f * UnityEngine.Mathf.Sin(bayGio / 160f);
                    veQuang(g, xt, y, rt, h, MAU_VANG, nhip, 4);
                }
                else if (chon)
                {
                    veQuang(g, xt, y, rt, h, MAU_VANG, 0.8f, 3);
                }
                veKhungBo(g, xt, y, rt, h, MAU_THE, 1f, chon || thang ? MAU_VANG : MAU_VIEN,
                        chon || thang ? 1f : 0.6f, chon || thang ? 2 : 1);
                // Dai ten mau cua con.
                veDaiMau(g, xt + 2, y + 2, rt - 4, 13, MAU_VIT[i], MAU_VIT_TOI[i], 4);
                string ten = mFont.tahoma_7b_white.getWidth(TEN_VIT[i]) + 30 <= rt
                        ? TEN_VIT[i] : TEN_NGAN_VIT[i];
                veChuNoi(g, mFont.tahoma_7b_white, ten, xt + rt / 2, y + 3, mFont.CENTER);
                if (rt >= 80)
                {
                    veHuyHieuVit(g, xt + 8, y + 8, 9, i, false);
                }

                bool coAnh = h >= 50 && rt >= 86;
                int xChu = coAnh ? xt + 50 : xt + rt / 2;
                int canh = coAnh ? mFont.LEFT : mFont.CENTER;
                if (coAnh)
                {
                    // Dung yen: khung 0. Cho chay lien tuc thi nam the cung nhuc
                    // nhich, mat dan het su chu y khoi khung canh dua.
                    const int khung = 0;
                    g.setClip(xt + 2, y + 15, rt - 4, h - 17);
                    veMotVit(g, i, khung, xt + 25, y + h - 1);
                    g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
                    // Clip cua khung canh da duoc tra ve toan man hinh o tren.
                }
                int yc = y + 17;
                int buocDong = (h - 19) / 3;
                if (buocDong > 12)
                {
                    buocDong = 12;
                }
                if (buocDong < 9)
                {
                    buocDong = 9;
                }
                mFont.tahoma_7b_red.drawString(g, "x4,5", xChu, yc, canh);
                mFont.tahoma_7_grey.drawString(g, "Tổng " + dvSo(m.dnTongCon[i]), xChu,
                        yc + buocDong, canh);
                long cuaToi = m.dnCuaToi[i];
                if (cuaToi > 0L)
                {
                    mFont.tahoma_7b_green2.drawString(g, "Bạn " + dvSo(cuaToi), xChu,
                            yc + buocDong * 2, canh);
                }
                else if (chon && dvTienCho > 0L)
                {
                    mFont.tahoma_7_green2.drawString(g, "+" + dvSo(dvTienCho), xChu,
                            yc + buocDong * 2, canh);
                }

                if (chon)
                {
                    veVienChay(g, xt, y, rt, h, MAU_VANG);
                }
                int daDat = dvConDaDat();
                if (!dvChoDat() && !thang)
                {
                    g.setColor(0x000000, 0.10f);
                    g.fillRect(xt, y, rt, h, BO_GOC);
                }
                else if (dvChoDat() && daDat >= 0 && daDat != i)
                {
                    // Da dat con khac: the nay khoa trong van nay.
                    g.setColor(0x3A2A1A, 0.42f);
                    g.fillRect(xt, y, rt, h, BO_GOC);
                }
            }
        }

        // ---- hàng nút ----

        private void veHangNutVit(mGraphics g, int x, int y, int w, int h)
        {
            God.MiniGame m = God.MiniGame.gI();
            const int khe = 4;
            int soNho = DV_MUC.Length + 3;
            // Nut dat rong gap ruoi mot nut nho.
            float donVi = (w - khe * soNho) / (soNho + 1.8f);
            int rNho = (int) donVi;
            int rDat = w - (rNho + khe) * soNho;
            bool bam = dvChoDat() && dvChon >= 0;
            int xn = x;
            for (int k = 0; k < soNho; k++)
            {
                dvONut[k] = new int[] { xn, y, rNho, h };
                string chu;
                if (k < DV_MUC.Length)
                {
                    chu = "+" + DV_MUC[k];
                }
                else if (k == DV_MUC.Length)
                {
                    chu = "ALL";
                }
                else if (k == DV_MUC.Length + 1)
                {
                    chu = "Gõ số";
                }
                else
                {
                    chu = "Huỷ";
                }
                veKhungBo(g, xn, y, rNho, h, MAU_THE, bam ? 0.95f : 0.4f,
                        MAU_VIEN, bam ? 0.8f : 0.3f, 1);
                if (bam)
                {
                    veChuyenSac(g, xn + 1, y + 1, rNho - 2, h - 2, 4);
                }
                (bam ? mFont.tahoma_7b_dark : mFont.tahoma_7_grey).drawString(g, chu,
                        xn + rNho / 2, y + 5, mFont.CENTER);
                xn += rNho + khe;
            }
            int kDat = soNho;
            dvONut[kDat] = new int[] { xn, y, rDat, h };
            bool datDuoc = bam && dvTienCho > 0L;
            string nhan;
            if (!dvChoDat())
            {
                nhan = m.dnGiaiDoan == 1 ? "ĐANG BƠI..." : "CHỜ VÁN MỚI";
            }
            else if (dvChon < 0)
            {
                nhan = "CHỌN VỊT";
            }
            else if (dvTienCho <= 0L)
            {
                nhan = "CHỌN SỐ THỎI";
            }
            else
            {
                nhan = "ĐẶT " + dvSo(dvTienCho);
            }
            if (datDuoc)
            {
                veQuang(g, xn, y, rDat, h, MAU_VUNG_SANG, 0.8f, 2);
                veDaiMau(g, xn, y, rDat, h, MAU_VUNG_SANG, MAU_VUNG_TOT, 6);
                veVetSang(g, xn, y, rDat, h, 1600);
                veChuNoi(g, mFont.tahoma_7b_white, nhan, xn + rDat / 2, y + 5, mFont.CENTER);
            }
            else
            {
                veKhungBo(g, xn, y, rDat, h, rgb(0x88, 0x88, 0x88), 0.45f,
                        MAU_VIEN, 0.35f, 1);
                mFont.tahoma_7b_dark.drawString(g, nhan, xn + rDat / 2, y + 5, mFont.CENTER);
            }
        }

        // ==================================================================
        //  Hai thẻ lịch sử
        // ==================================================================

        /// <summary>Số dòng của thẻ lịch sử đang mở, cho nút sang trang.</summary>
        /// <remarks>
        /// Hai danh sách trong <c>MiniGame</c> dùng chung cho mọi trò, nên phải xem
        /// <c>lsCuaTro</c>: còn là của trò khác thì coi như chưa có gì.
        /// </remarks>
        private int dvSoDongLs()
        {
            God.MiniGame m = God.MiniGame.gI();
            if (m.lsCuaTro != God.MiniGame.TRO_DUA_NGUA)
            {
                return 0;
            }
            return the2 == THE2_LS_TOI ? m.lsToi.Count : m.lsServer.Count;
        }

        /// <summary>Huy hiệu màu của con vịt kèm tên ngắn, cho một ô bảng.</summary>
        private static void veTenVitO(mGraphics g, int x, int y, int con)
        {
            if (con < 0 || con >= DV_SO_VIT)
            {
                mFont.tahoma_7_grey.drawString(g, "?", x, y, mFont.LEFT);
                return;
            }
            veHuyHieuVit(g, x + 5, y + 6, 10, con, false);
            mFont.tahoma_7b_dark.drawString(g, TEN_NGAN_VIT[con], x + 13, y, mFont.LEFT);
        }

        /// <summary>Cột theo tỉ lệ bề ngang, để bảng hẹp vẫn không đè chữ.</summary>
        private int[] dvCot(params int[] phanNghin)
        {
            int w = rong - LE * 2;
            int[] x = new int[phanNghin.Length];
            for (int i = 0; i < x.Length; i++)
            {
                x[i] = w * phanNghin[i] / 1000;
            }
            return x;
        }

        private void veLsToiVit(mGraphics g)
        {
            God.MiniGame m = God.MiniGame.gI();
            int[] xCot = dvCot(0, 170, 400, 540, 780);
            veNenLs(g, new string[] { "Phiên", "Vịt đặt", "Cược", "Về nhất",
                    "Thắng/Thua" }, xCot);
            int tong = dvSoDongLs();
            if (tong == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Bạn chưa đặt cuộc đua nào.",
                        x0 + rong / 2, yNoiDung() + 40, mFont.CENTER);
                veNutTrang(g, 0);
                return;
            }
            int moiTrang = soDongMotTrang();
            int dau = trang * moiTrang;
            for (int i = 0; i < moiTrang && dau + i < tong; i++)
            {
                God.MiniGame.DongLsToi d = m.lsToi[dau + i];
                int[] o = oDongLs(i);
                g.setColor(d.thang ? 0xD8F0C8 : (i % 2 == 0 ? 0xFFFFFF : 0xE8D6B8),
                        d.thang ? 0.6f : 0.35f);
                g.fillRect(o[0], o[1], o[2], o[3], 3);
                mFont.tahoma_7b_dark.drawString(g, "#" + d.phien, o[0] + xCot[0] + 2,
                        o[1] + 2, mFont.LEFT);
                veTenVitO(g, o[0] + xCot[1], o[1] + 2, d.cua);
                mFont.tahoma_7b_dark.drawString(g, dvSo(d.soThoi), o[0] + xCot[2],
                        o[1] + 2, mFont.LEFT);
                veTenVitO(g, o[0] + xCot[3], o[1] + 2, d.ketQua);
                (d.thang ? mFont.tahoma_7b_green : mFont.tahoma_7b_red).drawString(g,
                        d.thang ? "+" + dvSo(d.tienThang) : "-" + dvSo(d.soThoi),
                        o[0] + xCot[4], o[1] + 2, mFont.LEFT);
            }
            veNutTrang(g, tong);
        }

        private void veLsServerVit(mGraphics g)
        {
            God.MiniGame m = God.MiniGame.gI();
            int[] xCot = dvCot(0, 150, 300, 450, 600, 760, 900);
            veNenLs(g, new string[] { "Phiên", "Nhất", "Nhì", "Ba", "Tổng cược",
                    "Đã trả", "Người" }, xCot);
            int tong = dvSoDongLs();
            if (tong == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Chưa có cuộc đua nào được ghi.",
                        x0 + rong / 2, yNoiDung() + 40, mFont.CENTER);
                veNutTrang(g, 0);
                return;
            }
            int moiTrang = soDongMotTrang();
            int dau = trang * moiTrang;
            for (int i = 0; i < moiTrang && dau + i < tong; i++)
            {
                God.MiniGame.DongLsServer d = m.lsServer[dau + i];
                int[] o = oDongLs(i);
                g.setColor(i % 2 == 0 ? 0xFFFFFF : 0xE8D6B8, 0.35f);
                g.fillRect(o[0], o[1], o[2], o[3], 3);
                mFont.tahoma_7b_dark.drawString(g, "#" + d.phien, o[0] + xCot[0] + 2,
                        o[1] + 2, mFont.LEFT);
                veTenVitO(g, o[0] + xCot[1], o[1] + 2, d.kq1);
                veTenVitO(g, o[0] + xCot[2], o[1] + 2, d.kq2);
                veTenVitO(g, o[0] + xCot[3], o[1] + 2, d.kq3);
                mFont.tahoma_7_blue.drawString(g, dvSo(d.tongCuoc), o[0] + xCot[4],
                        o[1] + 2, mFont.LEFT);
                mFont.tahoma_7_red.drawString(g, dvSo(d.tongTra), o[0] + xCot[5],
                        o[1] + 2, mFont.LEFT);
                mFont.tahoma_7b_dark.drawString(g, d.soNguoi + string.Empty,
                        o[0] + xCot[6], o[1] + 2, mFont.LEFT);
            }
            veNutTrang(g, tong);
        }

        // ==================================================================
        //  Chạm
        // ==================================================================

        /// <summary>Bắt chạm cho thẻ Đua Vịt. Gọi ở khung vừa nhả ngón.</summary>
        private void chamDuaVit()
        {
            for (int i = 0; i < DV_SO_VIT; i++)
            {
                int[] o = dvOThe[i];
                if (o == null || !cham(o[0], o[1], o[2], o[3]))
                {
                    continue;
                }
                int daDat = dvConDaDat();
                if (daDat >= 0 && daDat != i)
                {
                    GameScr.info1.addInfo("Mỗi ván chỉ đặt một con — bạn đã chọn " + TEN_VIT[daDat], 0);
                    return;
                }
                if (dvChoDat() && dvChon != i)
                {
                    // Doi con thi bo so dang gom: so do la cua con kia.
                    dvChon = i;
                    dvTienCho = 0L;
                }
                return;
            }
            for (int k = 0; k < dvONut.Length; k++)
            {
                int[] o = dvONut[k];
                if (o == null || !cham(o[0], o[1], o[2], o[3]))
                {
                    continue;
                }
                if (!dvChoDat() || dvChon < 0)
                {
                    return;
                }
                if (k < DV_MUC.Length)
                {
                    dvGom(DV_MUC[k]);
                }
                else if (k == DV_MUC.Length)
                {
                    dvGom(dvTranGom());
                }
                else if (k == DV_MUC.Length + 1)
                {
                    BanPhimSo.getInstance().moRa("Nhập số thỏi", dvTranGom(),
                            n => { dvTienCho = 0L; dvGom(n); });
                }
                else if (k == DV_MUC.Length + 2)
                {
                    dvTienCho = 0L;
                }
                else if (dvTienCho > 0L)
                {
                    God.MiniGame.gI().datCuoc(God.MiniGame.TRO_DUA_NGUA, dvChon, dvTienCho);
                    dvTienCho = 0L;
                }
                return;
            }
        }
    }
}
