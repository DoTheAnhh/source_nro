using System;
using System.Collections.Generic;
using UnityEngine;
using Game5.God;

namespace Game5
{
    /// <summary>
    /// Thoại trong game — bắt mic, gửi lên máy chủ, phát tiếng người khác.
    /// </summary>
    /// <remarks>
    /// <para><b>Ba kênh, mỗi lúc chỉ một.</b> Khu (cùng khu cùng bản đồ), Map
    /// (cùng bản đồ, mọi khu), Bang (cùng bang hội, không cần cùng chỗ). Máy chủ
    /// không nhớ ai đang bật kênh nào — kênh đi kèm từng gói, nên tắt mic là im
    /// ngay, không cần gói báo tắt.</para>
    ///
    /// <para><b>Vì sao µ-law 8 kHz.</b> Unity không có sẵn bộ mã hoá tiếng nói,
    /// mà nhúng Opus thì phải kèm thư viện gốc cho từng nền tảng. µ-law là bảng
    /// tra 8 bit, viết gọn trong vài chục dòng, cho 8 KB/s — nghe rõ lời, và một
    /// khu mười người nói cùng lúc vẫn chưa tới 1 Mbps. Đổi lại tiếng hơi rè so
    /// với Opus; với thoại trong game thì đủ.</para>
    ///
    /// <para><b>Phát lại.</b> Mỗi người nói một <c>AudioSource</c> riêng, dùng
    /// clip vòng và con trỏ ghi. Trộn tất cả vào một clip thì hai người nói cùng
    /// lúc sẽ đè lên nhau; tách ra thì Unity tự trộn.</para>
    /// </remarks>
    public class VoiceChat : MonoBehaviour
    {
        public const int KENH_TAT = -1;
        public const int KENH_KHU = 0;
        public const int KENH_MAP = 1;
        public const int KENH_BANG = 2;

        /// <summary>Tần số lấy mẫu. 8 kHz đủ cho tiếng nói, cao hơn chỉ tốn băng thông.</summary>
        private const int TAN_SO = 8000;

        /// <summary>Mỗi gói gửi bao nhiêu mẫu — 160 mẫu = 20 ms, cỡ gói thoại quen thuộc.</summary>
        private const int MAU_MOI_GOI = 160;

        /// <summary>Clip vòng của mic dài mấy giây.</summary>
        private const int GIAY_DEM_MIC = 1;

        /// <summary>Dưới ngưỡng này coi như im lặng, không gửi gói.</summary>
        private const float NGUONG_IM = 0.012f;

        /// <summary>Đệm lúc bắt đầu phát — 160 ms, đủ nuốt mạng giật mà chưa thấy trễ.</summary>
        private const int DEM_BAT_DAU = MAU_MOI_GOI * 8;

        /// <summary>Người nói im quá ngần này giây thì dừng loa của họ.</summary>
        private const float IM_THI_DUNG = 0.35f;

        /// <summary>
        /// Vừa phát tiếng người khác trong ngần này giây thì không gửi mic của mình.
        /// </summary>
        /// <remarks>Không có khử vọng: mic thu lại tiếng từ loa của chính máy mình
        /// rồi gửi ngược về, người nói nghe lại giọng mình một lần nữa.</remarks>
        private const float CHAN_VONG = 0.3f;

        private static float lanNhanTiengCuoi = -10f;

        /// <summary>Một đoạn im lặng ghi đè phía trước đầu ghi.</summary>
        private static readonly float[] IM = new float[MAU_MOI_GOI * 12];

        private static float[] IM_CA_CLIP;

        private static VoiceChat instance;

        public static VoiceChat gI()
        {
            if (instance == null)
            {
                GameObject go = GameObject.Find("__VoiceChat__");
                if (go == null)
                {
                    go = new GameObject("__VoiceChat__");
                    DontDestroyOnLoad(go);
                }
                instance = go.GetComponent<VoiceChat>();
                if (instance == null)
                {
                    instance = go.AddComponent<VoiceChat>();
                }
            }
            return instance;
        }

        // ---------- trạng thái ----------
        public static int kenhDangBat = KENH_TAT;
        /// <summary>Loa bật/tắt RIÊNG cho từng kênh, chỉ mục theo KENH_*.</summary>
        /// <remarks>
        /// Tách theo kênh vì nhu cầu thật là "nghe bang, tắt khu" — một công tắc
        /// chung thì phải chọn nghe tất hoặc tắt tất. Gói tiếng gửi xuống có kèm
        /// kênh nên lọc được ngay lúc nhận.
        /// </remarks>
        public static readonly bool[] batLoa = { true, true, true };

        /// <summary>Tạm tắt mic RIÊNG cho từng kênh, chỉ mục theo KENH_*.</summary>
        /// <remarks>
        /// Tách khỏi việc đổi kênh: bấm nút kênh lần nữa là BỎ chọn kênh, muốn
        /// nói lại phải chọn lại. Nút này chỉ bịt miệng, chọn kênh vẫn nguyên.
        /// </remarks>
        public static readonly bool[] micTat = { false, false, false };

        /// <summary>Chỉ mục an toàn cho hai mảng trên.</summary>
        /// <summary>Kenh dang duoc xem/chinh trong hop cau hinh.</summary>
        /// <remarks>
        /// Loa va mic gio nho theo tung kenh, nen hai nut bat/tat phai biet dang
        /// chinh cho kenh nao. Bam mot nut kenh la doi luon cho nay, ke ca khi cu
        /// bam de TAT kenh do ? nguoi dung vua bam vao no thi dang nghi ve no.
        /// </remarks>
        public static int kenhXem = KENH_KHU;


        public static int chiMuc(int kenh)
        {
            return (kenh < KENH_KHU || kenh > KENH_BANG) ? KENH_KHU : kenh;
        }

        /// <summary>Âm lượng phát tiếng người khác, 0..4 (1 = giữ nguyên).</summary>
        /// <remarks>
        /// Mặc định 2, không phải 1. Tiếng đi qua µ-law 8 bit rồi mới tới đây,
        /// mà µ-law dồn phần lớn độ phân giải vào quãng nhỏ — phát lại nguyên
        /// biên độ thì nghe rất khẽ so với mọi âm khác trong game.
        /// </remarks>
        public static float amLuongLoa = 2f;

        /// <summary>Độ khuếch đại mic trước khi mã hoá, 0..4 (1 = giữ nguyên).</summary>
        /// <remarks>
        /// <para>Mặc định 2,5: mic tích hợp của laptop thu rất nhỏ, để 1 thì
        /// người nghe phải vặn loa hết cỡ mới nghe ra.</para>
        ///
        /// <para>Trần đã nâng từ 2 lên 4. Ở bản cũ người dùng kéo hết cỡ (2,0)
        /// vẫn thấy bé, tức là trần chứ không phải mic là chỗ vướng.</para>
        /// </remarks>
        public static float amLuongMic = 2.5f;
        public static string tenMic = "";

        private AudioClip clipMic;
        private int viTriDocMic;
        private readonly float[] demDoc = new float[MAU_MOI_GOI];
        private readonly sbyte[] demGui = new sbyte[MAU_MOI_GOI];

        /// <summary>Ai đang nói — hiện lên màn hình cho biết.</summary>
        public static readonly Dictionary<int, string> dangNoi =
                new Dictionary<int, string>();
        private static readonly Dictionary<int, float> hetNoiLuc =
                new Dictionary<int, float>();

        private readonly Dictionary<int, NguonPhat> nguon =
                new Dictionary<int, NguonPhat>();

        /// <summary>Một người nói = một AudioSource + một clip vòng.</summary>
        private class NguonPhat
        {
            public AudioSource loa;
            public AudioClip clip;
            public int viTriGhi;
            /// <summary>Kênh của gói gần nhất — để dừng loa theo kênh.</summary>
            public int kenh;
            /// <summary>Lúc nhận gói gần nhất (Time.time).</summary>
            public float nhanLanCuoi;
        }

        // ---------- thiết bị ----------
        /// <summary>Danh sách mic của máy. Rỗng nghĩa là máy không có mic nào.</summary>
        public static string[] danhSachMic()
        {
            try
            {
                return Microphone.devices ?? new string[0];
            }
            catch (Exception)
            {
                return new string[0];
            }
        }

        /// <summary>Tên mic đang dùng, hoặc mic mặc định nếu chưa chọn.</summary>
        public static string micHienTai()
        {
            string[] ds = danhSachMic();
            if (ds.Length == 0)
            {
                return "";
            }
            for (int i = 0; i < ds.Length; i++)
            {
                if (ds[i] == tenMic)
                {
                    return ds[i];
                }
            }
            return ds[0];
        }

        // ---------- bật / tắt ----------
        /// <summary>Bật một kênh; bật kênh đang bật thì tắt.</summary>
        public static void doiKenh(int kenh)
        {
            VoiceChat v = gI();
            kenhXem = chiMuc(kenh);
            if (kenhDangBat == kenh)
            {
                v.tatMic();
                kenhDangBat = KENH_TAT;
                dungLoa(KENH_TAT);
                Utils.addInfo1("Voice", false);
                return;
            }
            // Mot luc chi mot kenh: dat kenh moi la kenh cu tu tat, khong can
            // tat tay tung cai.
            kenhDangBat = kenh;
            v.batMic();
            Utils.addInfo1("Voice " + tenKenh(kenh), kenhDangBat != KENH_TAT);
        }

        public static string tenKenh(int kenh)
        {
            switch (kenh)
            {
                case KENH_KHU:
                    return "Khu";
                case KENH_MAP:
                    return "Map";
                case KENH_BANG:
                    return "Bang";
                default:
                    return "Tắt";
            }
        }

        private void batMic()
        {
            tatMic();
            string mic = micHienTai();
            if (mic.Length == 0 && danhSachMic().Length == 0)
            {
                GameScr.info1.addInfo("Máy không có mic nào", 0);
                kenhDangBat = KENH_TAT;
                return;
            }
            try
            {
                clipMic = Microphone.Start(mic, true, GIAY_DEM_MIC, TAN_SO);
                viTriDocMic = 0;
            }
            catch (Exception)
            {
                GameScr.info1.addInfo("Không mở được mic", 0);
                kenhDangBat = KENH_TAT;
            }
        }

        private void tatMic()
        {
            try
            {
                string mic = micHienTai();
                if (Microphone.IsRecording(mic))
                {
                    Microphone.End(mic);
                }
            }
            catch (Exception)
            {
            }
            clipMic = null;
            viTriDocMic = 0;
        }

        // ---------- vòng chạy ----------
        private void Update()
        {
            donNguoiNoi();
            donLoaIm();
            if (kenhDangBat == KENH_TAT || clipMic == null)
            {
                return;
            }
            try
            {
                docVaGui();
            }
            catch (Exception)
            {
                // Rut mic giua chung -> tat cho gon, khong de ban loi moi khung hinh.
                tatMic();
                kenhDangBat = KENH_TAT;
            }
        }

        private void docVaGui()
        {
            string mic = micHienTai();
            int viTriMic = Microphone.GetPosition(mic);
            int tong = clipMic.samples;
            if (viTriMic < 0 || tong <= 0)
            {
                return;
            }
            int coSan = viTriMic - viTriDocMic;
            if (coSan < 0)
            {
                coSan += tong;      // con tro mic da vong lai dau clip
            }
            while (coSan >= MAU_MOI_GOI)
            {
                clipMic.GetData(demDoc, viTriDocMic);
                viTriDocMic = (viTriDocMic + MAU_MOI_GOI) % tong;
                coSan -= MAU_MOI_GOI;

                // Dang phat tieng nguoi khac thi khong gui: mic se thu lai tieng
                // tu loa va gui nguoc ve — nguoi noi nghe vong giong minh.
                if (micTat[chiMuc(kenhDangBat)] || !coTieng(demDoc)
                        || Time.time - lanNhanTiengCuoi < CHAN_VONG)
                {
                    continue;       // tat mic hoac im lang thi khong ton goi
                }
                for (int i = 0; i < MAU_MOI_GOI; i++)
                {
                    demGui[i] = MaHoa.tuFloat(nenMem(demDoc[i] * amLuongMic));
                }
                Service.gI().guiTieng((sbyte)kenhDangBat, demGui);
            }
        }

        /// <summary>Ngưỡng bắt đầu nén. Dưới mức này mẫu đi qua nguyên vẹn.</summary>
        private const float NGUONG_NEN = 0.7f;

        /// <summary>
        /// Ép mẫu về trong khoảng [-1, 1] bằng cách <b>nén</b>, không phải cắt.
        /// </summary>
        /// <remarks>
        /// <para><b>Đây là chỗ sinh ra tiếng rè.</b> Bản cũ khuếch đại rồi cắt
        /// phẳng: <c>if (v > 1f) v = 1f;</c>. Cắt phẳng biến đỉnh sóng sin thành
        /// một đoạn nằm ngang, và một sóng vuông thì mang theo cả một chuỗi hoạ
        /// ba bậc cao — tai người nghe ra đúng là tiếng rè, tiếng xé. Càng kéo
        /// to càng nhiều mẫu bị cắt nên càng rè, và người dùng lại tưởng phải
        /// kéo to hơn nữa.</para>
        ///
        /// <para>Nén mềm thì phần dưới <see cref="NGUONG_NEN"/> đi qua y nguyên
        /// — tiếng nói bình thường không bị đụng tới — còn phần trên bị bẻ cong
        /// dần và tiệm cận 1 mà không bao giờ chạm tới. Đỉnh sóng vẫn là đỉnh
        /// sóng, chỉ thấp hơn, nên không đẻ ra hoạ ba.</para>
        ///
        /// <para>Vẫn kẹp lần cuối: mẫu vào là NaN thì mọi phép tính trên đều ra
        /// NaN, mà NaN lọt xuống µ-law là một tiếng nổ.</para>
        /// </remarks>
        private static float nenMem(float v)
        {
            float dau = v < 0f ? -1f : 1f;
            float a = v * dau;
            if (a > NGUONG_NEN)
            {
                a = NGUONG_NEN + (1f - NGUONG_NEN)
                        * (1f - 1f / (1f + (a - NGUONG_NEN) * 3f));
            }
            v = a * dau;
            if (!(v >= -1f && v <= 1f))
            {
                v = v > 0f ? 1f : (v < 0f ? -1f : 0f);
            }
            return v;
        }

        /// <summary>Khung này có ai nói không, hay chỉ là nền phòng.</summary>
        private static bool coTieng(float[] mau)
        {
            float dinh = 0f;
            for (int i = 0; i < mau.Length; i++)
            {
                float v = mau[i] < 0f ? -mau[i] : mau[i];
                if (v > dinh)
                {
                    dinh = v;
                }
            }
            // So sau khi khuech dai: mic nho ma van lay nguong goc thi van im tit.
            return dinh * amLuongMic >= NGUONG_IM;
        }

        // ---------- nhận ----------
        /// <summary>Nhận một gói tiếng từ máy chủ.</summary>
        /// <summary>Nhan mot goi tieng tu may chu.</summary>
        /// <remarks>
        /// Loc theo kenh CUA GOI chu khong theo kenh dang bat: nho vay tat loa
        /// rieng tung kenh duoc — nghe bang ma khong nghe khu.
        /// </remarks>
        public void nhan(int kenhGoi, int idNguoiNoi, string tenNguoiNoi,
                sbyte[] tieng, int soByte)
        {
            if (!batLoa[chiMuc(kenhGoi)] || soByte <= 0)
            {
                return;
            }
            dangNoi[idNguoiNoi] = tenNguoiNoi;
            hetNoiLuc[idNguoiNoi] = Time.time + 0.5f;

            NguonPhat n = layNguon(idNguoiNoi);
            if (n == null)
            {
                return;
            }
            float[] mau = new float[soByte];
            for (int i = 0; i < soByte; i++)
            {
                // Nén mềm luôn ở đây: âm lượng loa cũng đẩy được quá 1, và
                // AudioClip.SetData cắt phẳng y như mic nếu để tràn.
                mau[i] = nenMem(MaHoa.raFloat(tieng[i]) * amLuongLoa);
            }
            n.kenh = kenhGoi;
            n.nhanLanCuoi = Time.time;
            lanNhanTiengCuoi = Time.time;
            int tong = n.clip.samples;
            if (n.loa.isPlaying)
            {
                // Dau ghi phai luon di TRUOC dau phat. Mang giat de dau phat
                // vuot qua thi goi toi se ghi vao cho vua phat xong, het mot vong
                // dem (2 giay) moi nghe thay — dung canh "noi mot lan nghe hai lan".
                int truoc = (n.viTriGhi - n.loa.timeSamples + tong) % tong;
                if (truoc < MAU_MOI_GOI || truoc > tong / 2)
                {
                    n.viTriGhi = (n.loa.timeSamples + DEM_BAT_DAU) % tong;
                }
            }
            ghiVong(n.clip, mau, soByte, n.viTriGhi);
            n.viTriGhi = (n.viTriGhi + soByte) % tong;
            // Xoa sach doan phia truoc: goi sau den muon thi loa phat im lang,
            // khong phat lai tieng cu con nam trong vong dem.
            ghiVong(n.clip, IM, IM.Length, n.viTriGhi);
            if (!n.loa.isPlaying)
            {
                // Bat dau phat cham lai DEM_BAT_DAU so voi dau ghi — doan truoc
                // do la im lang (vong dem da xoa sach khi dung).
                n.loa.timeSamples = (n.viTriGhi + tong - DEM_BAT_DAU) % tong;
                n.loa.loop = true;
                n.loa.Play();
            }
        }

        private NguonPhat layNguon(int id)
        {
            NguonPhat n;
            if (nguon.TryGetValue(id, out n))
            {
                return n;
            }
            try
            {
                GameObject go = new GameObject("voice_" + id);
                go.transform.SetParent(transform);
                n = new NguonPhat();
                n.loa = go.AddComponent<AudioSource>();
                n.clip = AudioClip.Create("voice_" + id, TAN_SO * 2, 1, TAN_SO, false);
                n.loa.clip = n.clip;
                n.loa.spatialBlend = 0f;    // 2D: nghe deu, khong theo vi tri
                n.viTriGhi = 0;
                nguon[id] = n;
                return n;
            }
            catch (Exception)
            {
                return null;
            }
        }


        // ---------- dừng loa ----------
        /// <summary>Dừng một nguồn phát và xoá sạch vòng đệm của nó.</summary>
        private static void dungNguon(NguonPhat n)
        {
            try
            {
                n.loa.Stop();
            }
            catch (Exception)
            {
            }
            try
            {
                if (IM_CA_CLIP == null || IM_CA_CLIP.Length != n.clip.samples)
                {
                    IM_CA_CLIP = new float[n.clip.samples];
                }
                n.clip.SetData(IM_CA_CLIP, 0);
            }
            catch (Exception)
            {
            }
            n.viTriGhi = 0;
        }

        /// <summary>Người nói im quá lâu thì dừng loa của họ — không để vòng đệm phát lại tiếng cũ.</summary>
        private void donLoaIm()
        {
            foreach (NguonPhat n in nguon.Values)
            {
                if (n.loa != null && n.loa.isPlaying && Time.time - n.nhanLanCuoi > IM_THI_DUNG)
                {
                    dungNguon(n);
                }
            }
        }

        /// <summary>Dừng mọi tiếng đang phát của một kênh; <see cref="KENH_TAT"/> là mọi kênh.</summary>
        public static void dungLoa(int kenh)
        {
            VoiceChat v = instance;
            if (v == null)
            {
                return;
            }
            foreach (NguonPhat n in v.nguon.Values)
            {
                if (kenh == KENH_TAT || n.kenh == kenh)
                {
                    dungNguon(n);
                }
            }
            dangNoi.Clear();
            hetNoiLuc.Clear();
        }

        /// <summary>Đổi khu hoặc bản đồ: kênh Khu, Map của chỗ cũ không còn đúng nữa.</summary>
        public static void daDoiCho()
        {
            if (kenhDangBat == KENH_KHU || kenhDangBat == KENH_MAP)
            {
                tatKenh("Voice đã tắt vì đổi khu / bản đồ");
            }
            dungLoa(KENH_KHU);
            dungLoa(KENH_MAP);
        }

        /// <summary>Rời bang (rời, bị đuổi, bang giải tán): tắt kênh Bang.</summary>
        public static void daRoiBang()
        {
            if (kenhDangBat == KENH_BANG)
            {
                tatKenh("Voice đã tắt vì rời bang");
            }
            dungLoa(KENH_BANG);
        }

        private static void tatKenh(string bao)
        {
            VoiceChat v = instance;
            if (v != null)
            {
                v.tatMic();
            }
            kenhDangBat = KENH_TAT;
            try
            {
                GameScr.info1.addInfo(bao, 0);
            }
            catch (Exception)
            {
            }
        }

        /// <summary>Ghi vào clip vòng, tự quấn qua cuối clip.</summary>
        private static void ghiVong(AudioClip clip, float[] data, int soMau, int tu)
        {
            int tong = clip.samples;
            tu = ((tu % tong) + tong) % tong;
            int dau = System.Math.Min(soMau, tong - tu);
            if (dau == soMau && soMau == data.Length)
            {
                clip.SetData(data, tu);
                return;
            }
            float[] p1 = new float[dau];
            Array.Copy(data, 0, p1, 0, dau);
            clip.SetData(p1, tu);
            int con = soMau - dau;
            if (con > 0)
            {
                float[] p2 = new float[con];
                Array.Copy(data, dau, p2, 0, con);
                clip.SetData(p2, 0);
            }
        }

        /// <summary>Xoá tên khỏi danh sách "đang nói" khi im quá nửa giây.</summary>
        private void donNguoiNoi()
        {
            if (hetNoiLuc.Count == 0)
            {
                return;
            }
            List<int> het = null;
            foreach (KeyValuePair<int, float> kv in hetNoiLuc)
            {
                if (Time.time > kv.Value)
                {
                    if (het == null)
                    {
                        het = new List<int>();
                    }
                    het.Add(kv.Key);
                }
            }
            if (het == null)
            {
                return;
            }
            for (int i = 0; i < het.Count; i++)
            {
                hetNoiLuc.Remove(het[i]);
                dangNoi.Remove(het[i]);
            }
        }

        /// <summary>
        /// µ-law: nén 16 bit xuống 8 bit theo thang loga.
        /// </summary>
        /// <remarks>
        /// Tai người nghe theo thang loga nên chia đều 256 mức theo loga giữ được
        /// nhiều chi tiết ở âm nhỏ hơn hẳn so với cắt thẳng xuống 8 bit tuyến
        /// tính. Đây là chuẩn G.711 dùng trong điện thoại — cùng bài toán.
        /// </remarks>
        private static class MaHoa
        {
            private const int CAN = 132;
            private const int TOI_DA = 32635;

            public static sbyte tuFloat(float f)
            {
                int mau = (int)(f * 32767f);
                if (mau > 32767)
                {
                    mau = 32767;
                }
                if (mau < -32768)
                {
                    mau = -32768;
                }
                int dau = (mau >> 8) & 0x80;
                if (dau != 0)
                {
                    mau = -mau;
                }
                if (mau > TOI_DA)
                {
                    mau = TOI_DA;
                }
                mau += CAN;
                int mu = 7;
                for (int mask = 0x4000; (mau & mask) == 0 && mu > 0; mask >>= 1)
                {
                    mu--;
                }
                int dinhTri = (mau >> (mu + 3)) & 0x0F;
                return (sbyte)(~(dau | (mu << 4) | dinhTri));
            }

            public static float raFloat(sbyte b)
            {
                int u = ~b & 0xFF;
                int dau = u & 0x80;
                int mu = (u >> 4) & 0x07;
                int dinhTri = u & 0x0F;
                int mau = ((dinhTri << 3) + CAN) << mu;
                mau -= CAN;
                if (dau != 0)
                {
                    mau = -mau;
                }
                return mau / 32768f;
            }
        }
    }
}
