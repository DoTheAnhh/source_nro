using System.Collections.Generic;

namespace Game4.God
{
    /// <summary>
    /// Khung chat ở giữa cạnh dưới, có bốn thẻ lọc theo kênh và ô gõ chữ.
    /// </summary>
    /// <remarks>
    /// <para><b>Vì sao gom lại một chỗ.</b> Trước đây tin nhắn hệ thống hiện ra
    /// bằng <c>ChatPopup.serverChatPopUp</c> — một hộp nổi tự tắt sau vài giây và
    /// <b>không lưu lại gì</b>. Bỏ lỡ một dòng là mất hẳn. Khung này giữ lại và
    /// cho cuộn.</para>
    ///
    /// <para><b>Bốn kênh lấy ở đâu.</b> Client không nhận nhãn kênh từ máy chủ,
    /// nên kênh được suy ra ngay tại chỗ tin nhắn tới: chat bang đi qua
    /// <c>readClanMsg</c> nên chắc chắn là Bang hội; tin bắt đầu bằng
    /// "(Hệ thống)" là Hệ thống; còn lại là Thế giới. Đó là toàn bộ thông tin
    /// client thật sự có — không dựng thêm thẻ mà nội dung chỉ là bản sao của
    /// thẻ khác.</para>
    ///
    /// <para>Không đụng tới <c>ChatPopup</c>: hộp nổi cũ vẫn chạy như cũ, khung
    /// này chỉ <b>nghe thêm</b>.</para>
    /// </remarks>
    public class ChatUI
    {
        private static ChatUI instance;

        public static ChatUI getInstance()
        {
            return (instance == null) ? (instance = new ChatUI()) : instance;
        }

        // ==================================================================
        //  Kenh
        // ==================================================================

        /// <summary>
        /// Mã kênh, <b>bằng đúng chỉ số thẻ</b> trong <see cref="TEN_THE"/>.
        /// </summary>
        /// <remarks>
        /// Phần vẽ so thẳng <c>theChon</c> với <c>Dong.kenh</c>, nên hai bảng
        /// này phải cùng thứ tự. Đổi thứ tự thẻ mà quên đổi mã ở đây là chọn
        /// một thẻ lại thấy tin của thẻ khác.
        /// </remarks>
        public const int KENH_TAT_CA = 0;
        public const int KENH_HE_THONG = 1;
        public const int KENH_THE_GIOI = 2;
        public const int KENH_MAP = 3;
        public const int KENH_KHU = 4;
        public const int KENH_BANG = 5;

        /// <summary>
        /// Tên các thẻ — <b>viết tắt cho vừa sáu thẻ trên một dải</b>.
        /// </summary>
        /// <remarks>
        /// Sáu thẻ mà để tên đầy đủ thì dải phải rộng hơn cả khung chat, và thẻ
        /// cuối bị đẩy khuất ra ngoài — người chơi tưởng mất thẻ. Viết tắt hai
        /// chữ dài nhất là đủ chỗ cho cả sáu.
        /// </remarks>
        private static readonly string[] TEN_THE =
                { "Tất cả", "H.thống", "T.giới", "Map", "Khu", "Bang" };

        /// <summary>Tài khoản này gõ được tin hệ thống (quản trị viên) hay không.</summary>
        /// <remarks>Máy chủ báo qua gói 121 mỗi lần vào bản đồ.</remarks>
        private static bool quyenHeThong;

        public static void datQuyenHeThong(bool duoc)
        {
            quyenHeThong = duoc;
        }

        /// <summary>Vào game lần mới: dọn tin cũ của Map, Khu và Thế giới.</summary>
        /// <remarks>
        /// <para>Ba kênh đó chỉ có nghĩa với đúng lúc đang nói; giữ lại thì vào
        /// game thấy nguyên đống tin của phiên trước lẫn với tin mới.</para>
        ///
        /// <para>KHÔNG dọn tin bang: máy chủ giữ lịch sử tin bang và gửi lại cả
        /// danh sách, đó là chỗ người chơi đọc lại chuyện lúc mình offline. Tin
        /// hệ thống cũng để nguyên vì cùng lý do.</para>
        /// </remarks>
        public void donTinCuKhiVaoGame()
        {
            xoaKenh(KENH_MAP);
            xoaKenh(KENH_KHU);
            xoaKenh(KENH_THE_GIOI);
            for (int i = 0; i < tinMoi.Length; i++)
            {
                tinMoi[i] = false;
            }
            cuon = 0;
        }

        /// <summary>Thẻ nào đang có tin chưa đọc — để nháy báo.</summary>
        private readonly bool[] tinMoi = new bool[TEN_THE.Length];

        /// <summary>
        /// Đánh dấu thẻ có tin chưa đọc, cho thẻ và nút Chat nháy.
        /// </summary>
        /// <remarks>
        /// Chỉ bốn kênh người chơi nói với nhau: Thế giới, Map, Khu, Bang. Tin
        /// hệ thống chạy suốt ngày, cho nháy thì nút Chat nháy mãi và hết nghĩa.
        /// Thẻ đang mở sẵn thì coi như đọc rồi, không nháy.
        /// </remarks>
        private void danhDauTinMoi(int kenh)
        {
            if (kenh != KENH_THE_GIOI && kenh != KENH_MAP
                    && kenh != KENH_KHU && kenh != KENH_BANG
                    && kenh != KENH_HE_THONG)
            {
                return;
            }
            if (dangXem(kenh))
            {
                return;
            }
            // Chi danh dau dung the cua kenh do. "Tat ca" va "H.thong"
            // khong nhay: mot cai la cho xem gop, cai kia la tin may chu
            // chay suot ngay — nhay ca hai thi cham do luon sang, nhin mai
            // thanh quen va het tac dung bao.
            tinMoi[kenh] = true;
        }

        /// <summary>Người chơi đang nhìn thẳng vào thẻ này.</summary>
        private bool dangXem(int kenh)
        {
            return !thuGon && choHien() && theChon == kenh;
        }

        /// <summary>Xem thẻ nào thì thẻ đó hết nháy; xem "Tất cả" thì hết sạch.</summary>
        private void danhDauDaDoc(int the)
        {
            if (the < 0 || the >= tinMoi.Length)
            {
                return;
            }
            if (the == KENH_TAT_CA)
            {
                for (int i = 0; i < tinMoi.Length; i++)
                {
                    tinMoi[i] = false;
                }
                return;
            }
            tinMoi[the] = false;
            for (int i = 0; i < tinMoi.Length; i++)
            {
                if (i != KENH_TAT_CA && tinMoi[i])
                {
                    return;
                }
            }
            tinMoi[KENH_TAT_CA] = false;
        }

        /// <summary>Còn tin chưa đọc ở kênh nào không — nút Chat trên HUD nháy theo.</summary>
        public static bool coTinChuaDoc()
        {
            ChatUI c = instance;
            if (c == null)
            {
                return false;
            }
            for (int i = 0; i < c.tinMoi.Length; i++)
            {
                if (c.tinMoi[i])
                {
                    return true;
                }
            }
            return false;
        }

        /// <summary>Nhịp nháy dùng chung cho thẻ và nút Chat.</summary>
        public static bool nhipNhay()
        {
            return GameCanvas.gameTick % 16 < 9;
        }

        /// <summary>
        /// Tin báo boss — chạy ở dải băng trên đầu rồi, không đổ vào khung chat.
        /// </summary>
        /// <remarks>
        /// Boss lên xuống liên tục, mỗi lượt vài dòng; để vào khung thì tin của
        /// người chơi bị đẩy khuất chỉ sau vài giây.
        /// </remarks>
        private static bool laTinBoss(string chu)
        {
            if (chu == null)
            {
                return false;
            }
            return chu.ToLower().Contains("boss");
        }

        private class Dong
        {
            public readonly int kenh;
            /// <summary>Tên người gửi, hoặc <c>null</c> với tin của máy chủ.</summary>
            public readonly string ten;
            public readonly string chu;
            /// <summary>Tin này do chính mình gửi.</summary>
            public readonly bool laMinh;

            public Dong(int kenh, string ten, string chu, bool laMinh)
            {
                this.kenh = kenh;
                this.ten = ten;
                this.chu = chu;
                this.laMinh = laMinh;
            }
        }

        /// <summary>Một dòng đã ngắt, sẵn sàng để vẽ.</summary>
        private class DongVe
        {
            public string chu;
            /// <summary>Tên người gửi — chỉ dòng ĐẦU của mỗi tin mới có.</summary>
            public string ten;
            public bool laMinh;
        }

        /// <summary>Số dòng giữ lại; cũ hơn thì rơi khỏi đầu danh sách.</summary>
        /// <remarks>
        /// Có trần vì đây là danh sách chỉ thêm vào: chạy vài giờ mà không giới
        /// hạn thì nó lớn dần mãi và mỗi khung hình lại phải ngắt dòng thêm.
        /// </remarks>
        private const int TOI_DA_DONG = 120;

        private readonly List<Dong> nhatKy = new List<Dong>();

        /// <summary>Thẻ kênh đang mở. Mặc định là <b>Khu</b>.</summary>
        /// <remarks>
        /// <para>Trước đây mặc định là "Tất cả" — mà "Tất cả" chỉ để ĐỌC, không
        /// gửi được (xem <c>goDuoc()</c>: nó đòi thẻ phải là Bang, Thế giới, Map
        /// hoặc Khu). Người chơi mở chat, gõ một câu, bấm gửi và không có gì
        /// xảy ra; phải tự nhận ra là còn phải bấm chọn một thẻ nữa.</para>
        ///
        /// <para>Khu là kênh nói chuyện thường ngày — người đang đứng cùng chỗ
        /// với mình. Mở ra là gõ được ngay.</para>
        /// </remarks>
        private int theChon = KENH_KHU;
        private int cuon;
        private bool thuGon;

        // ==================================================================
        //  Nhan tin
        // ==================================================================

        /// <summary>Mã những tin bang đã ghi, để không ghi lại lần hai.</summary>
        /// <remarks>
        /// Máy chủ gửi lại <b>cả danh sách</b> tin bang mỗi lần có tin mới, chứ
        /// không chỉ gửi riêng dòng vừa thêm. Ghi thẳng mọi thứ nhận được thì
        /// mỗi dòng cũ lại vào nhật ký thêm một bản — đúng cảnh mở game lên
        /// thấy mọi câu hiện hai lần.
        ///
        /// Lọc theo mã tin chứ không theo nội dung: hai người nói cùng một câu
        /// là chuyện bình thường và cả hai đều phải hiện.
        /// </remarks>
        private readonly HashSet<int> maTinBangDaGhi = new HashSet<int>();

        /// <summary>Xoá sạch kênh Bang hội để nạp lại danh sách từ đầu.</summary>
        /// <remarks>
        /// Gọi ngay trước một lượt nạp cả danh sách tin bang. Không xoá thì mỗi
        /// lượt nạp lại chồng thêm một bản của mọi dòng cũ, và bộ lọc theo mã
        /// tin không cứu được vì máy chủ cấp mã mới sau mỗi phiên.
        /// </remarks>
        /// <summary>
        /// Người chơi vừa đổi chỗ đứng — dọn những kênh không còn đúng nữa.
        /// </summary>
        /// <remarks>
        /// <para>Chat khu và chat bản đồ chỉ có nghĩa với đúng chỗ vừa nói. Sang
        /// khu khác mà vẫn giữ lại tin của khu cũ thì người chơi đọc một câu rủ
        /// nhau đánh boss ở một nơi mình không còn đứng.</para>
        ///
        /// <para>Đổi bản đồ thì xoá cả hai: đổi bản đồ đương nhiên là đổi cả
        /// khu.</para>
        ///
        /// <para>Người mới vào khu cũng không thấy tin cũ — máy chủ không lưu
        /// lịch sử hai kênh này, nó chỉ phát cho ai đang ở đó ngay lúc gửi.</para>
        /// </remarks>
        public void doiChoDung(int mapCu, int khuCu, int mapMoi, int khuMoi)
        {
            if (mapCu != mapMoi)
            {
                xoaKenh(KENH_MAP);
                xoaKenh(KENH_KHU);
                return;
            }
            if (khuCu != khuMoi)
            {
                xoaKenh(KENH_KHU);
            }
        }

        private void xoaKenh(int kenh)
        {
            nhatKy.RemoveAll(d => d.kenh == kenh);
            gioiHanCuon();
        }

        public void batDauNapLaiBang()
        {
            nhatKy.RemoveAll(d => d.kenh == KENH_BANG);
            maTinBangDaGhi.Clear();
            demNapBang.Clear();
            dangNapBang = true;
        }

        /// <summary>Một dòng đang chờ trong lượt nạp lại danh sách.</summary>
        private class DongTam
        {
            public int ma;
            /// <summary>Tên người gửi — giữ RIÊNG để còn tô màu lúc vẽ.</summary>
            public string ten;
            public string chu;
        }

        /// <summary>Đang trong một lượt nạp lại cả danh sách tin bang.</summary>
        private bool dangNapBang;

        private readonly List<DongTam> demNapBang = new List<DongTam>();

        /// <summary>
        /// Kết thúc lượt nạp: xếp theo mã tin rồi mới ghi vào nhật ký.
        /// </summary>
        /// <remarks>
        /// <para><b>Xếp theo mã tin chứ không tin vào thứ tự nhận.</b> Máy chủ
        /// gửi lịch sử tin bang theo thứ tự mới-trước, nên ghi thẳng theo thứ tự
        /// nhận là khung chat hiện ngược — dòng mới nhất nằm trên cùng, đúng cảnh
        /// đăng xuất vào lại thấy sai thứ tự.</para>
        ///
        /// <para>Xếp theo mã thì đúng dù máy chủ đổi thứ tự gửi, mà mã tin thì
        /// tăng dần theo thời gian nên không cần thêm cột giờ.</para>
        /// </remarks>
        public void ketThucNapBang()
        {
            if (!dangNapBang)
            {
                return;
            }
            dangNapBang = false;
            demNapBang.Sort((a, b) => a.ma.CompareTo(b.ma));
            for (int i = 0; i < demNapBang.Count; i++)
            {
                them(KENH_BANG, demNapBang[i].ten, demNapBang[i].chu);
            }
            demNapBang.Clear();
        }

        public void nhanChatBang(int maTin, string ten, string chu)
        {
            if (dangNapBang)
            {
                // Dang nap ca danh sach: gom lai, xep xong o ketThucNapBang moi
                // ghi. Ghi ngay tung dong thi thu tu phu thuoc vao thu tu may
                // chu gui, ma thu tu do la moi-truoc.
                // Giu ten RIENG, khong gop vao noi dung.
                //
                // Gop thanh "Ten: chu" thi luc ve khong con ten de to mau, ca
                // dong ra mot mau trang — dung canh dang xuat vao lai thi tin
                // bang mat mau, trong khi tin moi nhan luc dang choi van co.
                DongTam t = new DongTam();
                t.ma = maTin;
                t.ten = (ten == null || ten.Length == 0) ? null : ten;
                t.chu = chu;
                demNapBang.Add(t);
                return;
            }
            if (maTin != 0)
            {
                if (maTinBangDaGhi.Contains(maTin))
                {
                    return;
                }
                maTinBangDaGhi.Add(maTin);
                if (maTinBangDaGhi.Count > TOI_DA_DONG * 3)
                {
                    // Don bot cho khoi phinh mai. Tin cu den muc nay thi da roi
                    // khoi nhat ky tu lau, khong con kha nang ghi trung nua.
                    maTinBangDaGhi.Clear();
                }
            }
            them(KENH_BANG, ten, chu);
        }

        /// <summary>Một dòng chat thế giới do máy chủ phát xuống.</summary>
        /// <remarks>
        /// Vào thẳng kênh Thế giới, không phải đoán theo nội dung như tin máy
        /// chủ: mã lệnh 45 chỉ mang đúng loại này.
        /// </remarks>
        public void nhanChatTheGioi(string ten, string chu)
        {
            them(KENH_THE_GIOI, ten, chu);
        }

        /// <summary>Một dòng chat bản đồ (mã lệnh 60).</summary>
        public void nhanChatMap(string ten, string chu)
        {
            them(KENH_MAP, ten, chu);
        }

        /// <summary>Một dòng chat khu (mã lệnh 67).</summary>
        public void nhanChatKhu(string ten, string chu)
        {
            them(KENH_KHU, ten, chu);
        }

        public void nhanTinMayChu(string chu)
        {
            if (chu == null)
            {
                return;
            }
            // Moi tin di qua duong nay deu la thong bao cua MAY CHU, nen vao
            // thang the He thong — khong doan theo noi dung nua.
            //
            // The The gioi chi chua chat the gioi cua nguoi choi (ma lenh 59).
            // Truoc day doan theo tien to "(He thong)", nen nhung thong bao
            // khong co tien to do — vi du cau chao khi vao game — bi xep nham
            // sang the The gioi.
            if (laTinBoss(chu))
            {
                return;
            }
            them(KENH_HE_THONG, null, chu);
        }

        private void them(int kenh, string chu)
        {
            them(kenh, null, chu);
        }

        private void them(int kenh, string ten, string chu)
        {
            if (chu == null || chu.Length == 0)
            {
                return;
            }
            // So ten voi ten nhan vat cua minh NGAY LUC NHAN, khong so luc ve:
            // luc ve co the da doi nhan vat, va nhung dong cu se doi mau theo.
            bool laMinh = ten != null && Char.myCharz() != null
                    && ten == Char.myCharz().cName;
            nhatKy.Add(new Dong(kenh, ten, chu, laMinh));
            if (!laMinh)
            {
                danhDauTinMoi(kenh);
            }
            while (nhatKy.Count > TOI_DA_DONG)
            {
                nhatKy.RemoveAt(0);
            }
        }

        // ==================================================================
        //  Kich thuoc
        // ==================================================================

        /// <summary>Be rong toi da cua khung.</summary>
        private const int RONG_TOI_DA = 268;

        /// <summary>
        /// Hẹp hơn mức này thì thẻ và tin nhắn không còn đọc được.
        /// </summary>
        /// <remarks>
        /// Để 170 thì trên điện thoại khung <b>không vừa</b> khoảng trống thật
        /// (chừng 110 điểm giữa hàng ô kỹ năng và cụm Capsule/Đậu thần) — và
        /// khi không vừa, nó tràn ngược sang trái đè lên hàng ô kỹ năng. Thà
        /// hẹp mà đứng đúng chỗ: thẻ đã cuộn ngang được, còn tin nhắn thì ngắt
        /// dòng chứ không mất chữ nào.
        /// </remarks>
        private const int RONG_TOI_THIEU = 108;
        private const int CAO_THE = 15;

        /// <summary>Bề rộng cố định của một thẻ.</summary>
        /// <remarks>
        /// Đủ chứa chữ dài nhất trong <c>TEN_THE</c> ("Hệ thống"). Chia đều bề
        /// ngang khung cho sáu thẻ thì mỗi thẻ chỉ còn hơn hai chục điểm, chữ
        /// bị cắt cụt — nên thẻ giữ bề rộng cố định và cả dải cuộn ngang.
        /// </remarks>
        private const int RONG_THE = 33;

        /// <summary>Số điểm dải thẻ đã trượt sang trái.</summary>
        private int cuonThe;

        /// <summary>Đang giữ ngón kéo danh sách tin.</summary>
        private bool dangKeo;

        /// <summary>Toạ độ dọc của mốc kéo gần nhất.</summary>
        private int yBatKeo;

        /// <summary>Lượt giữ ngón này đã thật sự cuộn được ít nhất một dòng.</summary>
        private bool daCuonKhiKeo;

        /// <summary>Vùng của cả dải thẻ — dùng để bắt lăn chuột ngang.</summary>
        private int[] oDaiThe = new int[0];
        private const int CAO_DONG = 11;
        private const int SO_DONG_THAY = 4;
        private const int CAO_O_GO = 13;
        private const int LE = 5;
        private const int O_THU_GON = 17;

        /// <summary>
        /// Be rong khung — LAP VUA khoang trong con lai o day man hinh.
        /// </summary>
        /// <remarks>
        /// Day man hinh co ba thu chia nhau: hang o ky nang ben trai, cum
        /// nut Capsule/Dau than ben phai, khung chat o giua. Dat be rong
        /// cung thi cua so rong de thua mot khoang, cua so hep lai chong
        /// len hai ben. Tinh tu khoang trong that thi khung luon vua khit.
        /// </remarks>
        /// <summary>
        /// Mép trái xa nhất khung chat được phép chạm tới.
        /// </summary>
        /// <remarks>
        /// Ngay sau hàng ô kỹ năng. Khung chat nằm cùng hàng với nó ở sát đáy
        /// màn hình, nên hai bên tránh nhau theo <b>chiều ngang</b>.
        /// </remarks>
        private int mepTraiChoPhep()
        {
            return mepPhaiHangKyNang() + 8;
        }

        private int rongKhung()
        {
            int trai = mepTraiChoPhep();
            if (trai < 4)
            {
                trai = 4;
            }
            int phai = GameScr.mepTraiCumNutNhanh() - 8;
            int w = phai - trai;
            if (w > RONG_TOI_DA)
            {
                w = RONG_TOI_DA;
            }
            if (w < RONG_TOI_THIEU)
            {
                w = RONG_TOI_THIEU;
            }
            if (w > GameCanvas.w - 12)
            {
                w = GameCanvas.w - 12;
            }
            return w;
        }

        private int caoKhung()
        {
            return CAO_THE + 3 + SO_DONG_THAY * CAO_DONG + 3 + CAO_O_GO + LE;
        }

        /// <summary>
        /// Mép trái của khung — <b>căn giữa</b> theo bề ngang màn hình.
        /// </summary>
        /// <remarks>
        /// Dán sát góc trái thì nó chồng lên hàng ô kỹ năng, và ở màn hình rộng
        /// thì nằm lệch hẳn một bên trong khi cả khoảng giữa để trống.
        /// </remarks>
        private int xKhung()
        {
            int w = rongKhung();
            int x = (GameCanvas.w - w) / 2;

            // Day sang phai cho khoi hang o ky nang.
            //
            // Khung nay can giua, con hang o ky nang neo vao mep trai — tren
            // cua so hep hai thu giao nhau, va vi ca hai deu nam sat day nen
            // chung de chong len nhau. Doc thang be rong that cua hang o
            // (GameScr.xSkill + so cot * GameScr.wSkill) chu khong doan mot con
            // so: hang o tu co lai khi man hinh hep.
            int trai = mepTraiChoPhep();
            if (trai > 0 && x < trai)
            {
                x = trai;
            }
            // Nhung khong duoc day den muc tho ra ngoai mep phai.
            int xToiDa = GameCanvas.w - w - 4;
            if (x > xToiDa)
            {
                x = xToiDa;
            }
            if (x < 4)
            {
                x = 4;
            }
            return x;
        }

        /// <summary>Mép phải của hàng ô kỹ năng, hoặc 0 nếu chưa dựng xong.</summary>
        /// <remarks>
        /// Hàng ô xếp thành hai dòng, mỗi dòng một nửa số ô — nên bề ngang của
        /// cả hàng là <b>nửa</b> số ô nhân bước, không phải cả mười ô.
        /// </remarks>
        private static int mepPhaiHangKyNang()
        {
            if (GameScr.thuGonKyNang)
            {
                // Hang o da gon: chi con cai nut mo lai, khung chat lan het
                // phan cho vua tra ra.
                int[] o = GameScr.oThuGonKyNang();
                return o[0] + o[2];
            }
            int[] xs = GameScr.xS;
            if (xs == null || xs.Length == 0 || GameScr.wSkill <= 0)
            {
                return 0;
            }
            int soCot = (xs.Length > 5) ? (xs.Length / 2) : xs.Length;
            return GameScr.xSkill + soCot * GameScr.wSkill;
        }

        /// <summary>
        /// Mép trên của khung — <b>nằm TRÊN hàng ô kỹ năng</b>.
        /// </summary>
        /// <remarks>
        /// Trước đây khung dán sát đáy màn hình, mà hàng ô kỹ năng cũng ở
        /// đáy: khung căn giữa còn hàng ô bắt đầu từ mép trái, nên trên cửa
        /// sổ hẹp hai thứ chồng lên nhau — đúng cảnh trong ảnh chụp.
        ///
        /// Hai hàng ô kỹ năng chiếm chừng 68 điểm tính từ đáy (hàng dưới 30
        /// điểm, hàng trên cách 32, cộng lề). Nhấc khung lên khỏi khoảng đó
        /// thì hai thứ không bao giờ đụng nhau, dù cửa sổ hẹp tới đâu.
        /// </remarks>
        private int yKhung()
        {
            // Sat day man hinh, cung mot le voi hang o ky nang.
            //
            // Truoc day khung bi nhac han len khoi hang o ky nang de hai thu
            // khong dam nhau. Gio tranh nhau theo CHIEU NGANG (xem xKhung), nen
            // khong con ly do gi de no lo lung giua man choi nua.
            int y = GameCanvas.h - caoKhung() - 4;
            if (y < 4)
            {
                y = 4;
            }
            return y;
        }

        /// <summary>
        /// Mép phải của khung chat, để chỗ khác xếp nút ngay cạnh nó.
        /// </summary>
        /// <remarks>
        /// Khung chat căn giữa nên mép phải của nó đổi theo bề ngang màn hình.
        /// Bên ngoài mà tự đoán một con số thì màn hình khác cỡ là hở ra hoặc
        /// chồng lên; hỏi thẳng khung chat thì lúc nào cũng đúng.
        /// </remarks>
        public static int mepPhaiKhungChat()
        {
            ChatUI c = getInstance();
            return c.xKhung() + c.rongKhung();
        }

        /// <summary>Mép trái của khung chat.</summary>
        public static int mepTraiKhungChat()
        {
            return getInstance().xKhung();
        }

        // ==================================================================
        //  Ve
        // ==================================================================

        private int[] oThuGon = new int[0];
        private int[][] oThe = new int[0][];
        private int[] oGo = new int[0];
        private int[] oLen = new int[0];
        private int[] oXuong = new int[0];

        public void ve(mGraphics g)
        {
            if (!choHien())
            {
                oThuGon = new int[0];
                oThe = new int[0][];
                oGo = new int[0];
                return;
            }
            if (thuGon)
            {
                // Thu gon ve SAT MEP PHAI cua khung chat.
                //
                // Truoc day no nam giua man hinh, tuc giua khung chat luc mo
                // ra — bam thu gon la cai nut nhay tu goc phai vao chinh
                // giua, bam mo lai thi nhay nguoc ra. Neo vao mep phai thi
                // nut dung yen mot cho o ca hai trang thai.
                int xg = xKhung() + rongKhung() - O_THU_GON - LE;
                int yg = GameCanvas.h - O_THU_GON - 5;
                GameScr.veNenBangHud(g, xg, yg, O_THU_GON, O_THU_GON);
                g.setColor(0xFFE9A3, 1f);
                for (int i = 0; i < 3; i++)
                {
                    g.fillRect(xg + 4, yg + 4 + i * 4, O_THU_GON - 8, 2);
                }
                if (coTinChuaDoc() && nhipNhay())
                {
                    // Thu gon roi van phai biet co tin moi: cham do o goc nut.
                    g.setColor(0xFF4A3C, 1f);
                    g.fillRect(xg + O_THU_GON - 7, yg + 2, 5, 5, 3);
                }
                oThuGon = new int[] { xg, yg, O_THU_GON, O_THU_GON };
                oThe = new int[0][];
                oGo = new int[0];
                return;
            }

            // Khung dang mo o the nao thi the do coi nhu da doc.
            danhDauDaDoc(theChon);

            int xK = xKhung();
            int yK = yKhung();
            int wK = rongKhung();
            int hK = caoKhung();
            GameScr.veNenBangHud(g, xK, yK, wK, hK);

            // Dai the — CUON NGANG.
            //
            // Sau the ma chia deu be ngang khung thi moi the con hon hai chuc
            // diem, khong the nao chua noi chu "He thong". Nen the co be rong
            // CO DINH, va ca dai truot ngang khi khong du cho.
            int xDai = xK + LE;
            // Dai the chiem HET be ngang: nut thu gon da chuyen xuong goc duoi.
            int wDai = wK - LE * 2;
            int wCanDu = TEN_THE.Length * RONG_THE;
            int cuonToiDa = wCanDu - wDai;
            if (cuonToiDa < 0)
            {
                cuonToiDa = 0;
            }
            if (cuonThe > cuonToiDa)
            {
                cuonThe = cuonToiDa;
            }
            if (cuonThe < 0)
            {
                cuonThe = 0;
            }
            // Keo the DANG CHON vao trong tam nhin.
            //
            // Doi the bang phim hay bang lenh thi the moi co the dang nam ngoai
            // vung thay duoc; khong keo lai thi nguoi choi thay mot dai the
            // khong the nao dang sang.
            int xTheChon = theChon * RONG_THE;
            if (xTheChon < cuonThe)
            {
                cuonThe = xTheChon;
            }
            else if (xTheChon + RONG_THE > cuonThe + wDai)
            {
                cuonThe = xTheChon + RONG_THE - wDai;
            }

            oThe = new int[TEN_THE.Length][];
            g.setClip(xDai, yK + 3, wDai, CAO_THE);
            for (int i = 0; i < TEN_THE.Length; i++)
            {
                int xT = xDai + i * RONG_THE - cuonThe;
                // The nam han ngoai vung thay duoc thi khong dang ky o cham:
                // dang ky ca thi bam vao mep khung lai trung mot the vo hinh.
                if (xT + RONG_THE - 2 <= xDai || xT >= xDai + wDai)
                {
                    oThe[i] = new int[0];
                    continue;
                }
                bool chon = (i == theChon);
                g.setColor(chon ? 0xFFDE86 : 0x6B4718, 1f);
                g.fillRect(xT, yK + 3, RONG_THE - 2, CAO_THE, 5);
                (chon ? mFont.tahoma_7b_dark : mFont.tahoma_7b_white)
                        .drawString(g, TEN_THE[i], xT + (RONG_THE - 2) / 2,
                                yK + 4, mFont.CENTER);
                if (tinMoi[i] && nhipNhay())
                {
                    // Cham do goc the: nhin luot ca dai la biet the nao co tin moi.
                    g.setColor(0xFF4A3C, 1f);
                    g.fillRect(xT + RONG_THE - 9, yK + 5, 5, 5, 3);
                }
                oThe[i] = new int[] { xT, yK + 3, RONG_THE - 2, CAO_THE };
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            oDaiThe = new int[] { xDai, yK + 3, wDai, CAO_THE };

            // Noi dung.
            List<DongVe> dong = dongDangXem(wK - LE * 2 - 4);
            int bd = dong.Count - SO_DONG_THAY - cuon;
            if (bd < 0)
            {
                bd = 0;
            }
            int yD = yK + CAO_THE + 6;
            for (int i = bd; i < dong.Count && i - bd < SO_DONG_THAY; i++)
            {
                DongVe v = dong[i];
                int x = xK + LE + 2;
                int y = yD + (i - bd) * CAO_DONG;
                if (v.ten != null)
                {
                    string nhan = v.ten + ": ";
                    veTenCoVien(g, nhan, x, y, v.laMinh);
                    x += fontTen(v.laMinh).getWidth(nhan);
                }
                mFont.tahoma_7_white.drawString(g, v.chu, x, y, mFont.LEFT);
            }

            // Hai nut cuon o canh phai vung tin nhan.
            //
            // Co ca banh xe chuot roi van lam hai nut nay: ban choi tren dien
            // thoai khong co banh xe nao, va ngay tren PC thi khong phai chuot
            // nao cung bao duoc pXYScrollMouse.
            int wND = wK - LE * 2 - 4;
            int xN = xK + wK - LE - 12;
            int caoND = SO_DONG_THAY * CAO_DONG;
            veNutCuon(g, xN, yD, true);
            veNutCuon(g, xN, yD + caoND - 12, false);
            oLen = new int[] { xN, yD, 12, 12 };
            oXuong = new int[] { xN, yD + caoND - 12, 12, 12 };

            // O go chu — CHI ve o the cho phep gui.
            //
            // "Tat ca" la cho xem gop, khong phai mot kenh de gui vao.
            // "He thong" va "The gioi" la tin mot chieu tu may chu.
            // Ve mot o go o nhung the do la moi nguoi choi go vao mot cho
            // khong dan toi dau.
            // Nut thu gon o GOC DUOI ben phai khung.
            //
            // Truoc no nam cuoi dai the o tren, tuc goc tren ben phai. Nhung
            // luc khung thu gon lai thi cai nut ay o nguyen goc duoi ben phai —
            // hai vi tri khac nhau cho cung mot nut, bam mo roi phai dua mat
            // len tren tim lai chO de bam dong. Dua han xuong duoi cho hai
            // trang thai cung mot cho.
            int yTG = yK + hK - CAO_O_GO - LE + 1;
            int xTG = xK + wK - LE - O_THU_GON;
            g.setColor(0x6B4718, 1f);
            g.fillRect(xTG, yTG, O_THU_GON, CAO_O_GO, 5);
            g.setColor(0xFFE9A3, 1f);
            g.fillRect(xTG + 4, yTG + (CAO_O_GO - 2) / 2, O_THU_GON - 8, 2);
            oThuGon = new int[] { xTG, yTG, O_THU_GON, CAO_O_GO };

            if (!theNayGuiDuoc())
            {
                oGo = new int[0];
                return;
            }
            int yGo = yTG;
            int wGo = wK - LE * 2 - O_THU_GON - 4;
            g.setColor(0xC8933C, 0.95f);
            g.fillRect(xK + LE, yGo, wGo, CAO_O_GO, 6);
            g.setColor(0x241809, 0.9f);
            g.fillRect(xK + LE + 1, yGo + 1, wGo - 2, CAO_O_GO - 2, 5);
            mFont.tahoma_7_grey.drawString(g, goiYGo(),
                    xK + LE + 6, yGo + 2, mFont.LEFT);
            oGo = new int[] { xK + LE, yGo, wGo, CAO_O_GO };
        }

        /// <summary>
        /// Màu tên người nhắn.
        /// </summary>
        /// <remarks>
        /// Nền khung chat là nâu ấm. Hai màu cũ đều chìm vào nó: vàng
        /// <c>tahoma_7b_yellow</c> thật ra là cam <c>#FFAA00</c> — cùng họ màu
        /// với nền, còn <c>tahoma_7b_green2</c> là xanh rất tối <c>#005325</c>,
        /// tối ngang phần đáy dải màu của khung.
        ///
        /// Hai màu mới đều là màu <b>lạnh</b>, nằm khác hẳn phía trên vòng màu
        /// so với nâu, nên tách ra ngay cả khi chưa có viền:
        /// mình <c>#637DFF</c>, người khác <c>#70B474</c>.
        /// </remarks>
        private static mFont fontTen(bool laMinh)
        {
            return laMinh ? mFont.tahoma_7b_blue : mFont.tahoma_7b_focus;
        }

        /// <summary>Tên người nhắn, viền tối bốn phía cho nổi khỏi nền.</summary>
        /// <remarks>
        /// Viền bốn phía chứ không phải một nét bóng chéo như tên nhân vật ngoài
        /// bản đồ: tên trong khung chat ngắn và nằm đầu dòng, tô dày một chút thì
        /// đọc ra ngay chứ không nhoè. Bỏ hai đường chéo để nét không bị dày quá
        /// ở cỡ chữ này.
        /// </remarks>
        private static void veTenCoVien(mGraphics g, string nhan, int x, int y,
                bool laMinh)
        {
            mFont vien = mFont.tahoma_7b_dark;
            vien.drawString(g, nhan, x - 1, y, mFont.LEFT);
            vien.drawString(g, nhan, x + 1, y, mFont.LEFT);
            vien.drawString(g, nhan, x, y - 1, mFont.LEFT);
            vien.drawString(g, nhan, x, y + 1, mFont.LEFT);
            fontTen(laMinh).drawString(g, nhan, x, y, mFont.LEFT);
        }

        /// <summary>Nút cuộn: một tam giác nhỏ dựng bằng các vạch ngang.</summary>
        /// <remarks>
        /// Máy vẽ không có hàm vẽ đa giác, nên tam giác được xếp từ những vạch
        /// ngang dài dần (hoặc ngắn dần). Bảy vạch là vừa đủ để mắt đọc ra mũi
        /// tên ở cỡ 12 điểm.
        /// </remarks>
        private static void veNutCuon(mGraphics g, int x, int y, bool len)
        {
            g.setColor(0x6B4718, 1f);
            g.fillRect(x, y, 12, 12, 5);
            g.setColor(0xFFE9A3, 1f);
            for (int i = 0; i < 4; i++)
            {
                int rong = len ? (i * 2 + 1) : (7 - i * 2);
                g.fillRect(x + 6 - rong / 2 - (rong % 2 == 0 ? 0 : 0),
                        y + 4 + i, rong, 1);
            }
        }

        /// <summary>
        /// Các dòng của thẻ đang xem, đã ngắt theo bề rộng khung.
        /// </summary>
        /// <remarks>
        /// Ngắt ở đây chứ không lúc nhận tin: bề rộng khung đổi theo cỡ cửa sổ,
        /// ngắt sẵn thì đổi cỡ xong mọi dòng cũ đều sai chỗ ngắt.
        /// </remarks>
        private List<DongVe> dongDangXem(int rongChu)
        {
            List<DongVe> ds = new List<DongVe>();
            for (int i = 0; i < nhatKy.Count; i++)
            {
                Dong d = nhatKy[i];
                if (theChon != KENH_TAT_CA && d.kenh != theChon)
                {
                    continue;
                }
                // O the "Tat ca" thi ghi kem ten kenh o dau dong: [Map] Ten: chu.
                // Khong co no thi ba kenh tron lan nhau, doc khong biet tin nao
                // cua dau.
                string tenHien = d.ten;
                string chuHien = d.chu;
                if (theChon == KENH_TAT_CA)
                {
                    string nhanKenh = "[" + TEN_THE[d.kenh] + "] ";
                    if (tenHien != null)
                    {
                        tenHien = nhanKenh + tenHien;
                    }
                    else
                    {
                        chuHien = nhanKenh + chuHien;
                    }
                }
                // Ngat dong theo be rong CON LAI sau ten: dong dau bi ten chiem
                // mat mot doan, ngat theo be rong day du thi dong do tran vien.
                int truTen = (tenHien == null) ? 0
                        : fontTen(d.laMinh).getWidth(tenHien + ": ");
                string[] a = mFont.tahoma_7_white.splitFontArray(chuHien,
                        rongChu - truTen);
                for (int j = 0; j < a.Length; j++)
                {
                    DongVe v = new DongVe();
                    v.chu = a[j];
                    v.ten = (j == 0) ? tenHien : null;
                    v.laMinh = d.laMinh;
                    ds.Add(v);
                }
            }
            return ds;
        }

        /// <summary>Thẻ đang xem có gửi được tin đi không.</summary>
        /// <remarks>
        /// Chỉ Bang hội. "Tất cả" là chỗ xem gộp. "Hệ thống" và "Thế giới"
        /// là tin một chiều: máy chủ phát xuống, người chơi không có lệnh nào
        /// gửi ngược lên hai kênh đó — thông báo thế giới đi qua vật phẩm loa
        /// (biểu mẫu 2005 phía máy chủ), còn tin hệ thống thì chỉ quản trị phát
        /// từ bảng điều khiển.
        /// </remarks>
        private bool theNayGuiDuoc()
        {
            if (theChon == KENH_HE_THONG)
            {
                // O nhap cua the He thong chi mo cho quan tri vien.
                return quyenHeThong;
            }
            return theChon == KENH_BANG || theChon == KENH_THE_GIOI
                    || theChon == KENH_MAP || theChon == KENH_KHU;
        }

        /// <summary>Câu gợi ý trong ô gõ, theo thẻ đang xem.</summary>
        private string goiYGo()
        {
            switch (theChon)
            {
                case KENH_BANG:
                    return "Nhấn để chat bang…";
                case KENH_MAP:
                    return "Nhấn để chat bản đồ…";
                case KENH_KHU:
                    return "Nhấn để chat khu…";
                case KENH_HE_THONG:
                    return "Nhấn để gửi tin hệ thống…";
                default:
                    return "Nhấn để chat thế giới…";
            }
        }

        /// <summary>Tiêu đề hộp gõ, theo thẻ đang xem.</summary>
        private static string tieuDeGo(int kenh)
        {
            switch (kenh)
            {
                case KENH_BANG:
                    return "Chat bang";
                case KENH_MAP:
                    return "Chat bản đồ";
                case KENH_KHU:
                    return "Chat khu";
                default:
                    return "Chat thế giới";
            }
        }

        /// <summary>
        /// Khung chat có được vẽ và nhận chạm ở khung hình này không.
        /// </summary>
        /// <remarks>
        /// Chỉ đòi <b>đang ở trong game</b>. Trước đây còn tắt khi mở bảng, mở
        /// menu hay mở ô chat cũ — mà đó chính là những lúc người chơi hay cần
        /// liếc lại tin nhắn nhất, và tắt đi thì đang đọc dở một câu là mất.
        ///
        /// Khung nằm dưới đáy màn hình, giữa bề ngang, nên nó không che mất
        /// phần thao tác của bảng nào.
        /// </remarks>
        private static bool choHien()
        {
            return GameCanvas.currentScreen == GameScr.instance;
        }

        // ==================================================================
        //  Cham
        // ==================================================================

        /// <summary>Trúng vùng này không; trúng thì nuốt luôn sự kiện.</summary>
        /// <remarks>
        /// Đúng công thức mà <c>TuiUI</c> dùng và chạy được: chỉ hỏi
        /// <c>isPointerHoldIn</c> rồi xoá sự kiện. Bản trước còn đòi thêm
        /// <c>isPointerClick</c> — cờ đó không phải lúc nào cũng bật cho một cú
        /// bấm thường, nên bấm thẻ mà không đổi được thẻ.
        /// </remarks>
        private static bool cham(int[] o)
        {
            if (o == null || o.Length != 4)
            {
                return false;
            }
            if (GameCanvas.isPointerHoldIn(o[0], o[1], o[2], o[3]))
            {
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            return false;
        }

        public bool capNhatCham()
        {
            if (!choHien())
            {
                return false;
            }
            // Cuon bang banh xe khi con tro nam trong khung — xet truoc ca nha
            // ngon vi banh xe khong sinh ra su kien nha ngon nao.
            // Lan chuot TREN DAI THE thi cuon dai the theo chieu ngang.
            //
            // Xet truoc phan cuon tin nhan: hai vung long nhau, de sau thi lan
            // chuot o dai the lai keo danh sach tin.
            if (!thuGon && GameCanvas.pXYScrollMouse != 0
                    && oDaiThe.Length == 4
                    && GameCanvas.pxMouse >= oDaiThe[0]
                    && GameCanvas.pxMouse <= oDaiThe[0] + oDaiThe[2]
                    && GameCanvas.pyMouse >= oDaiThe[1]
                    && GameCanvas.pyMouse <= oDaiThe[1] + oDaiThe[3])
            {
                cuonThe += GameCanvas.pXYScrollMouse > 0
                        ? -RONG_THE : RONG_THE;
                return true;
            }
            if (!thuGon && GameCanvas.pXYScrollMouse != 0
                    && trongKhung(GameCanvas.pxMouse, GameCanvas.pyMouse))
            {
                cuon += GameCanvas.pXYScrollMouse > 0 ? 1 : -1;
                gioiHanCuon();
                return true;
            }
            // Giu va keo trong vung tin nhan.
            //
            // Doi mot QUANG DUONG bang chieu cao mot dong roi moi cuon mot dong,
            // va nho lai moc sau moi buoc — lam theo kieu "delta moi khung hinh"
            // thi keo cham vai diem cung khong nhuc nhich, con keo nhanh thi
            // truot cai vut.
            if (!thuGon && GameCanvas.isPointerDown
                    && trongKhung(GameCanvas.pxMouse, GameCanvas.pyMouse))
            {
                if (!dangKeo)
                {
                    dangKeo = true;
                    yBatKeo = GameCanvas.pyMouse;
                    daCuonKhiKeo = false;
                }
                int lech = GameCanvas.pyMouse - yBatKeo;
                if (lech >= CAO_DONG || lech <= -CAO_DONG)
                {
                    cuon += lech / CAO_DONG;
                    yBatKeo += (lech / CAO_DONG) * CAO_DONG;
                    gioiHanCuon();
                    daCuonKhiKeo = true;
                }
                return true;
            }
            if (dangKeo && !GameCanvas.isPointerDown)
            {
                dangKeo = false;
                // CHI nuot cu nha ngon khi that su da keo duoc mot doan.
                //
                // Ban truoc nuot moi cu nha ngon — ma mot cu BAM cung di qua
                // day (ngon xuong roi len trong khung), nen tat ca cac nut cua
                // khung chat chet cung: khong doi duoc the, khong thu gon duoc,
                // khong mo duoc o go. Bam va keo chi khac nhau o cho co dich
                // chuyen hay khong, nen phai xet dung cho do.
                if (daCuonKhiKeo)
                {
                    daCuonKhiKeo = false;
                    GameCanvas.clearAllPointerEvent();
                    return true;
                }
                // Khong keo — de nguyen su kien cho phan bam ben duoi xu ly.
            }

            if (!GameCanvas.isPointerJustRelease)
            {
                return false;
            }
            if (cham(oThuGon))
            {
                thuGon = !thuGon;
                return true;
            }
            if (thuGon)
            {
                return false;
            }
            for (int i = 0; i < oThe.Length; i++)
            {
                if (cham(oThe[i]))
                {
                    if (theChon != i)
                    {
                        // Doi the thi ve day: vi tri cuon cua the nay khong co y
                        // nghia gi voi the kia.
                        cuon = 0;
                    }
                    theChon = i;
                    danhDauDaDoc(i);
                    return true;
                }
            }
            if (cham(oLen))
            {
                cuon++;
                gioiHanCuon();
                return true;
            }
            if (cham(oXuong))
            {
                cuon--;
                gioiHanCuon();
                return true;
            }
            if (cham(oGo))
            {
                moHopGo();
                return true;
            }
            return false;
        }

        private bool trongKhung(int px, int py)
        {
            int xK = xKhung();
            int yK = yKhung();
            return px >= xK && px <= xK + rongKhung()
                    && py >= yK && py <= yK + caoKhung();
        }

        /// <summary>
        /// Mở hộp gõ chữ, gửi xong thì gửi đúng kênh đang xem.
        /// </summary>
        /// <remarks>
        /// Dùng <c>HopNhapChu</c> chứ không gọi khung chat cũ của game: hộp đó
        /// đọc phím qua <c>Event.current</c> nên gõ được tiếng Việt bằng bộ gõ
        /// của máy.
        ///
        /// Thẻ "Bang hội" gửi vào bang; các thẻ còn lại gửi chat thường. Thẻ
        /// "Hệ thống" và "Thế giới" là tin một chiều từ máy chủ, người chơi
        /// không gửi vào đó được, nên gửi chat thường là nghĩa gần nhất.
        /// </remarks>
        /// <summary>
        /// Mở hộp gõ chữ cho <b>nút chat ở góc phải màn hình</b>.
        /// </summary>
        /// <remarks>
        /// <para>Nút đó trước đây đi đường riêng: nó gọi
        /// <c>ChatTextField.startChat</c> rồi <c>Service.chat(text)</c> — chat
        /// THƯỜNG, không thuộc kênh nào. Còn khung chat ở đáy màn hình có bốn
        /// thẻ kênh và gửi đúng kênh đang mở. Hai đường khác nhau cho cùng một
        /// việc: người chơi đang xem thẻ "Khu", bấm nút góc phải gõ một câu, và
        /// câu đó không ra kênh Khu.</para>
        ///
        /// <para>Nay nút góc phải gọi thẳng vào đây, nên nó dùng chung thẻ đang
        /// chọn, chung hộp gõ tiếng Việt, chung mọi thứ.</para>
        ///
        /// <para>Mở luôn khung chat nếu đang thu gọn: gõ xong mà không thấy câu
        /// mình vừa gửi ở đâu thì không biết nó đã đi hay chưa.</para>
        /// </remarks>
        public void moGoChuTuNgoai()
        {
            thuGon = false;
            moHopGo();
        }

        private void moHopGo()
        {
            int kenhLucGo = theChon;
            HopNhapChu.getInstance().moRa(tieuDeGo(kenhLucGo),
                    "", 100, delegate (string s)
                    {
                        if (s == null || s.Trim().Length == 0)
                        {
                            return;
                        }
                        if (kenhLucGo == KENH_MAP)
                        {
                            // KHONG ghi tai cho: may chu phat lai cho ca minh.
                            Service.gI().chatMap(s);
                            return;
                        }
                        if (kenhLucGo == KENH_KHU)
                        {
                            Service.gI().chatKhu(s);
                            return;
                        }
                        if (kenhLucGo == KENH_HE_THONG)
                        {
                            // May chu phat lai cho ca minh, khong ghi tai cho.
                            Service.gI().chatHeThong(s);
                            return;
                        }
                        if (kenhLucGo == KENH_THE_GIOI)
                        {
                            // KHONG ghi tai cho: may chu phat lai cho MOI
                            // nguoi, ke ca minh. Ghi them o day la dong nao
                            // minh gui cung hien hai lan.
                            Service.gI().chatTheGioi(s);
                            return;
                        }
                        if (kenhLucGo == KENH_BANG)
                        {
                            if (Char.myCharz().clan == null)
                            {
                                them(KENH_BANG, "Bạn chưa vào bang nào.");
                                return;
                            }
                            // KHONG ghi tai cho.
                            //
                            // Do bang thuc te: may chu phat tin bang cho CA
                            // nguoi gui, nen ghi them o day la moi dong minh
                            // gui deu hien HAI lan.
                            Service.gI().clanMessage(0, s,
                                    Char.myCharz().clan.ID);
                        }
                    });
        }

        private void gioiHanCuon()
        {
            int tong = dongDangXem(rongKhung() - LE * 2 - 4).Count;
            int toiDa = tong - SO_DONG_THAY;
            if (toiDa < 0)
            {
                toiDa = 0;
            }
            if (cuon > toiDa)
            {
                cuon = toiDa;
            }
            if (cuon < 0)
            {
                cuon = 0;
            }
        }
    }
}
