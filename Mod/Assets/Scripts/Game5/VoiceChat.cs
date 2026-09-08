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

        /// <summary>Âm lượng phát tiếng người khác, 0..2 (1 = giữ nguyên).</summary>
        public static float amLuongLoa = 1f;

        /// <summary>Độ khuếch đại mic trước khi mã hoá, 0..2 (1 = giữ nguyên).</summary>
        /// <remarks>
        /// Nhân vào mẫu rồi mới cắt biên: mic laptop thường thu rất nhỏ, không
        /// khuếch đại thì người nghe phải vặn loa hết cỡ mới nghe ra.
        /// </remarks>
        public static float amLuongMic = 1f;
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

                if (micTat[chiMuc(kenhDangBat)] || !coTieng(demDoc))
                {
                    continue;       // tat mic hoac im lang thi khong ton goi
                }
                for (int i = 0; i < MAU_MOI_GOI; i++)
                {
                    // Khuech dai roi CAT BIEN: khong cat thi mau tran ra ngoai
                    // [-1,1] va µ-law goi ve dau nguoc lai, nghe thanh tieng ret.
                    float v = demDoc[i] * amLuongMic;
                    if (v > 1f) { v = 1f; }
                    if (v < -1f) { v = -1f; }
                    demGui[i] = MaHoa.tuFloat(v);
                }
                Service.gI().guiTieng((sbyte)kenhDangBat, demGui);
            }
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
                mau[i] = MaHoa.raFloat(tieng[i]) * amLuongLoa;
            }
            n.clip.SetData(mau, n.viTriGhi);
            n.viTriGhi = (n.viTriGhi + soByte) % n.clip.samples;
            if (!n.loa.isPlaying)
            {
                // Bat dau phat cham lai mot chut so voi con tro ghi, de goi den
                // muon mot nhip van con du du lieu ma phat, khong bi ngat quang.
                n.loa.timeSamples = (n.viTriGhi + n.clip.samples - MAU_MOI_GOI * 4)
                        % n.clip.samples;
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
