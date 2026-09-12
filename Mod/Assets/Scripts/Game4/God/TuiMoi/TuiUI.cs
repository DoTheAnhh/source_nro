// Khong "using System": lop Math cua engine trung ten voi System.Math.

namespace Game4.God
{
    /// <summary>
    /// Màn nhân vật kiểu mới: sáu thẻ trên, trang bị và hành trang, thanh tiền.
    /// </summary>
    /// <remarks>
    /// <para>Bảng cũ trong <c>Panel</c> vẫn còn nguyên và vẫn mở bằng nút "Hành
    /// Trang" như trước. Màn này là bản thứ hai, mở bằng nút "Trang bị" trên hàng
    /// nút HUD, để so hai bên rồi mới quyết bỏ cái nào.</para>
    ///
    /// <para><b>Vì sao dựng rời hẳn thay vì sửa tiếp bảng cũ.</b> Bảng cũ dùng
    /// chung ba biến trạng thái với màn danh sách kiểu dòng — <c>selected</c>,
    /// <c>currentListLength</c>, <c>waitToPerform</c> — mà cả ba tính theo bề cao
    /// MỘT DÒNG 24 điểm. Lưới ô vuông không có "dòng" nào, nên mỗi lần chỉnh lưới
    /// lại làm lệch một trong ba biến đó, và triệu chứng luôn là "bấm vào ô không
    /// hiện gì". Màn này tự giữ ô đang chọn, tự vẽ hộp thông tin và tự gửi lệnh,
    /// không mượn biến nào của bảng cũ.</para>
    ///
    /// <para>Bố cục tính một lần trong <see cref="tinhBoCuc"/> rồi cả phần vẽ lẫn
    /// phần bắt chạm cùng đọc, tránh hai bên tính lệch nhau.</para>
    /// </remarks>
    public class TuiUI : IChatable
    {
        private static TuiUI instance;

        public static TuiUI getInstance()
        {
            return instance ?? (instance = new TuiUI());
        }

        public bool dangMo;

        // ==================================================================
        //  So do o trang bi
        // ==================================================================
        //  Chi so trong ba mang duoi la CHI SO O CUA MAY CHU:
        //
        //    0 Ao   1 Quan   2 Gang   3 Giay   4 Rada   5 Cai trang
        //    6 Giap luyen tap (item type 32)   7 Pet   8 Deo lung (co)
        //    9 Van bay (type 24)   10 Sach (type 35)   11 Chan menh (type 39)
        //
        //  Thu tu do MAY CHU dat, khong phai cho nay: InventoryService ghi
        //  itemsBody.set(template.type == 32 ? 6 : template.type, item).
        //
        //  Doi cach xep tren man hinh thi sua ba mang duoi. KHONG doi thu tu
        //  TEN_O — ten gan theo chi so o, doi la ten lech sang o khac.

        /// <summary>Cột trái, từ trên xuống.</summary>
        private static readonly int[] O_TRAI = { 0, 1, 2, 3, 4 };

        /// <summary>Cột phải, từ trên xuống.</summary>
        private static readonly int[] O_PHAI = { 5, 6, 8, 9, 7 };

        /// <summary>Hàng dưới: sách và chân mệnh, hai ô liền nhau ở giữa.</summary>
        private static readonly int[] O_DUOI = { 10, 11 };

        /// <summary>Tên từng ô, hiện khi ô trống. Xếp theo chỉ số ô.</summary>
        private static readonly string[] TEN_O = {
            "Áo", "Quần", "Găng", "Giày", "Rađa", "Cải trang",
            "Giáp luyện tập", "Pet", "Đeo lưng", "Ván bay",
            "Sách", "Chân mệnh"
        };

        // ==================================================================
        //  The
        // ==================================================================
        /// <summary>Tên sáu thẻ, theo đúng thứ tự hiện trên dải thẻ.</summary>
        /// <remarks>
        /// Thứ tự và cách viết hoa lấy đúng theo bản tham chiếu: chỉ hoa chữ đầu,
        /// không hoa từng từ. "Nhân vật" đứng thứ hai chứ không phải "Bản Thân"
        /// đứng đầu.
        ///
        /// <b>Số của từng thẻ phải khớp vị trí trong mảng này</b> — phần vẽ và
        /// phần bắt chạm đều lấy chỉ số mảng. Đổi thứ tự mảng mà quên đổi hằng số
        /// là bấm thẻ này ra nội dung thẻ khác.
        /// </remarks>
        private static readonly string[] TEN_THE = {
            "Nhiệm vụ", "Nhân vật", "Kỹ năng", "Đệ tử", "Bang hội", "Chức năng"
        };

        private const int THE_NHIEM_VU = 0;
        private const int THE_BAN_THAN = 1;
        private const int THE_KY_NANG = 2;
        private const int THE_DE_TU = 3;
        private const int THE_BANG_HOI = 4;
        private const int THE_CHUC_NANG = 5;

        private int theChon = THE_BAN_THAN;

        /// <summary>Hai thẻ con của "Nhân vật".</summary>
        private static readonly string[] TEN_THE_CON = { "Chỉ số", "Hành trang" };

        private const int CON_THONG_TIN = 0;
        private const int CON_HANH_TRANG = 1;

        private int theConChon = CON_THONG_TIN;

        /// <summary>Số cột ô của thẻ "Hành trang".</summary>
        private const int TUI_SO_COT = 6;

        /// <summary>
        /// Bề rộng dành cho thanh cuộn dọc, kể cả khoảng cách với nội dung.
        /// </summary>
        private const int RONG_CUON = 8;

        /// <summary>Lề giữa nét viền khung lưới và hàng ô ngoài cùng.</summary>
        private const int LE_LUOI = 3;

        /// <summary>Số hàng ô thấy cùng lúc ở thẻ "Hành trang".</summary>
        /// <remarks>
        /// Năm hàng, và ô là hình chữ nhật NGANG chứ không vuông. Bắt ô vuông thì
        /// bề cao của lưới do bề ngang quyết định, và nó luôn thấp hơn cột trang
        /// bị bên cạnh — chừa một dải trống ở đáy. Ô ngang thì lưới phủ kín đúng
        /// bằng cột kia.
        /// </remarks>
        private const int TUI_SO_HANG = 5;

        // ==================================================================
        //  Ban do nhanh — cot phai cua the "Nhiem vu"
        // ==================================================================

        /// <summary>Một điểm đến trong bảng <c>map_nhanh</c>.</summary>
        public class DiemMap
        {
            /// <summary>Khoá chính. Dùng chính nó để xin dịch chuyển.</summary>
            public int id;

            public string ten;

            /// <summary>Số hiện trong ngoặc vuông sau tên.</summary>
            public int mapId;
        }

        /// <summary>Một nhóm điểm đến ("Trái Đất", "Namếc"…).</summary>
        public class NhomMap
        {
            public string ten;

            public readonly System.Collections.Generic.List<DiemMap> diem =
                    new System.Collections.Generic.List<DiemMap>();
        }

        private readonly System.Collections.Generic.List<NhomMap> dsNhomMap =
                new System.Collections.Generic.List<NhomMap>();

        private int nhomChon;

        /// <summary>
        /// Dòng (hoặc hàng) đầu tiên đang hiện của danh sách đang cuộn.
        /// </summary>
        /// <remarks>
        /// Dùng CHUNG cho mọi thẻ có danh sách cuộn: lưới túi, danh sách kỹ năng,
        /// danh sách bản đồ, danh sách bang. Phần kéo cuộn ở <c>cuonLuoi()</c> ghi
        /// vào đúng biến này, nên thẻ nào giữ một biến cuộn riêng là kéo một đằng
        /// vẽ một nẻo. Đổi thẻ thì đặt lại về 0.
        /// </remarks>
        private int cuon;

        /// <summary>Đã hỏi máy chủ về bản đồ nhanh trong lần mở này chưa.</summary>
        private bool daXinBanDo;

        /// <summary>Nạp cây nhóm–điểm vừa nhận về.</summary>
        public void nhanBanDo(System.Collections.Generic.List<NhomMap> moi)
        {
            dsNhomMap.Clear();
            if (moi != null)
            {
                dsNhomMap.AddRange(moi);
            }
            if (nhomChon >= dsNhomMap.Count)
            {
                nhomChon = 0;
            }
            cuon = 0;
        }

        /// <summary>Nhóm đang chọn, hoặc null.</summary>
        private NhomMap nhomDangChon()
        {
            if (nhomChon < 0 || nhomChon >= dsNhomMap.Count)
            {
                return null;
            }
            return dsNhomMap[nhomChon];
        }

        /// <summary>Bề cao một nút nhóm.</summary>
        private const int CAO_NUT_NHOM = 19;

        /// <summary>Bề cao một dòng bản đồ.</summary>
        private const int CAO_DONG_MAP = 19;

        /// <summary>
        /// Vùng nút nhóm thứ <paramref name="i"/>: x, y, rộng, cao.
        /// </summary>
        /// <remarks>
        /// Xếp hai cột. Nhóm cuối cùng, nếu tổng số nhóm là số lẻ, chiếm trọn bề
        /// ngang — đúng như "Ngũ Hành Sơn" nằm một mình một hàng trong bản tham
        /// chiếu, chứ không để trơ nửa hàng trống.
        /// </remarks>
        private int[] oNutNhom(int i, int soNhom)
        {
            int rongCot = (rongPhai - 6) / 2;
            int hang = i / 2;
            bool motMinh = (i == soNhom - 1) && (soNhom % 2 == 1);
            int y = yThan + 3 + hang * CAO_NUT_NHOM;
            if (motMinh)
            {
                return new int[] { xPhai + 3, y, rongPhai - 6, CAO_NUT_NHOM - 2 };
            }
            int x = xPhai + 3 + (i % 2) * rongCot;
            return new int[] { x, y, rongCot - 3, CAO_NUT_NHOM - 2 };
        }

        /// <summary>Bề cao khung lưới nhóm, tính theo số hàng thật.</summary>
        private int caoKhungNhom()
        {
            int so = dsNhomMap.Count;
            if (so <= 0)
            {
                return CAO_NUT_NHOM + 8;
            }
            int hang = (so + 1) / 2;
            return hang * CAO_NUT_NHOM + 7;
        }

        private int yKhungMap()
        {
            return yThan + caoKhungNhom() + 5;
        }

        private int caoKhungMap()
        {
            return yThan + caoThan - yKhungMap();
        }

        private int soDongMapThay()
        {
            int n = (caoKhungMap() - CAO_DAI_TD - KHE_KHUNG - 6) / CAO_DONG_MAP;
            return (n < 1) ? 1 : n;
        }

        // ==================================================================
        // ==================================================================
        //  Mau
        // ==================================================================

        /// <summary>
        /// Gộp ba thành phần màu thành một số nguyên cho <c>setColor</c>.
        /// </summary>
        /// <remarks>
        /// <c>setColor</c> nhận một số nguyên RGB. Viết thẳng số thập phân
        /// (3810320) thì không ai đọc ra là màu gì, và sửa một sắc độ phải đi tính
        /// tay. Gọi qua đây thì đọc được ngay từng thành phần.
        /// </remarks>
        private static int rgb(int r, int g, int b)
        {
            return (r << 16) | (g << 8) | b;
        }

        // Bang mau KEM.
        //
        // Ba ban truoc lan luot la nau gan den (0x1A1008), nau dam (0x4A3524),
        // nau sang (0x7A5A3E). Ban nay la nen kem 0xFCE3C0 do nguoi dung chon.
        //
        // DOI NEN SANG THI PHAI DOI CA MAU CHU. Nen toi thi chu xam, trang va
        // vang doc duoc; tren nen kem thi ba mau do gan nhu bien mat. Vi vay ca
        // tep chu trong tep nay da doi sang bo cho nen sang:
        //
        //   nhan, chu mo   -> tahoma_7        (den)
        //   gia tri        -> tahoma_7b_dark  (nau dam)
        //   tieu de, nhan manh -> tahoma_7b_red
        //   dat/xong       -> tahoma_7b_green (dam hon green2)
        //
        // Them mot dong chu moi thi phai chon trong bo tren, dung lay
        // tahoma_7b_white hay tahoma_7b_yellow: tren nen kem la khong thay gi.
        private static readonly int MAU_NEN = rgb(0xFC, 0xE3, 0xC0);
        private static readonly int MAU_NEN_SANG = rgb(0xFF, 0xF3, 0xDE);

        /// <summary>Nền thẻ chưa chọn trên dải thẻ.</summary>
        private static readonly int MAU_TIEU_DE = rgb(0xFB, 0xE8, 0xCE);

        /// <summary>Nền ô vật phẩm và nút.</summary>
        private static readonly int MAU_O = rgb(0xF0, 0xDC, 0xBE);

        /// <summary>Nền khung nội dung — kem sáng nhất.</summary>
        private static readonly int MAU_O_MO = rgb(0xFD, 0xF0, 0xDC);

        /// <summary>Màu viền: nâu, KHÔNG phải vàng.</summary>
        /// <remarks>
        /// Bản trước dùng vàng <c>0xC28236</c> kèm một vệt sáng chạy quanh chu vi.
        /// Trên nền kem, viền vàng vừa chói vừa hút mắt ra mép thay vì vào nội
        /// dung. Nay là nâu tĩnh, và vệt chạy đã bỏ hẳn.
        /// </remarks>
        private static readonly int MAU_VIEN = rgb(0xA8, 0x6E, 0x3C);

        /// <summary>
        /// Màu nền của thẻ con ĐANG chọn — cam, dùng cho MỌI dải thẻ con.
        /// </summary>
        /// <remarks>
        /// Một màu duy nhất cho tất cả thẻ con bên trong sáu thẻ lớn, để nhìn vào
        /// là biết ngay đâu là thẻ đang mở. Trước đây mỗi dải tự chọn màu: chỗ thì
        /// nâu đậm, chỗ thì kem sáng, nên không có quy tắc nào đọc ra được.
        /// </remarks>
        private static readonly int MAU_THE_CON_CHON = rgb(0xF0, 0xA1, 0x64);

        /// <summary>Dải tiêu đề cột "Trang bị" — CAM, không phải kem.</summary>
        /// <remarks>
        /// Chỉ dải này màu cam; các dải tiêu đề khác vẫn kem. Đúng như bản tham
        /// chiếu: cột trang bị nổi lên nhờ dải cam, các cột còn lại lặng hơn.
        /// </remarks>
        private static readonly int MAU_DAI_CAM = rgb(0xF1, 0xA9, 0x6A);

        /// <summary>
        /// Nền ô trang bị và ô túi — kem sáng.
        /// </summary>
        /// <remarks>
        /// Đã đổi ba lần: nâu <c>0xC79B6F</c>, rồi cam <c>0xF0A164</c>, nay kem
        /// <c>0xFBEEDE</c>.
        ///
        /// <b>Kèm theo là màu chữ số lượng.</b> Trên nền nâu và nền cam thì chữ
        /// vàng đọc được; trên kem thì mất hẳn. Đổi màu ô mà quên đổi chữ là số
        /// lượng biến mất — nên hai thứ đó nằm cùng một chỗ trong tệp này để sửa
        /// một lần là thấy cả hai.
        /// </remarks>
        private static readonly int MAU_O_DO = rgb(0xFB, 0xEE, 0xDE);

        /// <summary>Viền ô — nâu đậm hơn nền ô.</summary>
        private static readonly int MAU_VIEN_O = rgb(0x8B, 0x62, 0x3A);

        /// <summary>
        /// Việc phải làm khi bước vào một thẻ.
        /// </summary>
        /// <remarks>
        /// Thông tin đệ tử KHÔNG tự về client. Máy chủ chỉ gửi khi được hỏi bằng
        /// gói -107 (<c>Service.petInfo</c>), và bảng cũ hỏi lúc mở màn đệ. Màn này
        /// không hỏi nên <c>Char.myPetz().cName</c> còn rỗng, và thẻ Đệ Tử báo
        /// "chưa có đệ" dù người chơi đang có đệ đi theo sau lưng.
        /// </remarks>
        private void vaoThe(int the)
        {
            if (the == THE_DE_TU)
            {
                Service.gI().petInfo();
                daXinDe = true;
            }
            if (the == THE_NHIEM_VU && dsNhomMap.Count == 0)
            {
                // Chi hoi khi chua co: cay nhom-diem hau nhu khong doi trong mot
                // phien choi, hoi lai moi lan vao the la ton goi tin vo ich.
                Service.gI().banDoXin();
                daXinBanDo = true;
            }
        }

        /// <summary>Đã hỏi máy chủ về đệ tử trong lần mở này chưa.</summary>
        private bool daXinDe;

        public void dong()
        {
            dangMo = false;
            hienHop = false;
            hienHopSo = false;
            hienHopKyNang = false;
            hienChonO = false;
            chiSoDangNang = -1;
            // Dung han viec dung nhieu: dong bang roi ma van gui tiep thi nguoi
            // choi khong con thay gi de biet no dang chay.
            conDung = 0;
            oDangDung = -1;
        }

        // ==================================================================
        //  Kich thuoc
        // ==================================================================

        /// <summary>Lề quanh mép bảng.</summary>
        private const int LE = 6;

        /// <summary>Bề cao một thẻ trên dải thẻ chính.</summary>
        private const int CAO_THE = 19;

        /// <summary>Bề cao một thẻ con.</summary>
        private const int CAO_THE_CON = 17;

        /// <summary>Bề cao thanh ba loại tiền.</summary>
        private const int CAO_TIEN = 18;

        /// <summary>Bề cao một nút trong hộp vật phẩm.</summary>
        private const int CAO_NUT = 20;

        /// <summary>Số ô của cột trang bị bên trái.</summary>
        private const int SO_HANG_COT = 5;

        /// <summary>Cỡ ô lớn nhất và nhỏ nhất.</summary>
        private const int O_TOI_DA = 74;

        private const int O_TOI_THIEU = 18;

        /// <summary>Chiều cao ước lượng của ảnh nhân vật, dùng để căn giữa.</summary>
        /// <remarks>
        /// Ước lượng, không đo được: ảnh ghép từ đầu, thân, chân, hào quang, cải
        /// trang nên không có một con số cao thật nào để đọc. Lệch vài điểm không
        /// ai thấy.
        /// </remarks>
        private const int CAO_NHAN_VAT = 52;

        /// <summary>Kéo quá bấy nhiêu điểm thì tính là cuộn, không phải bấm.</summary>
        private const int NGUONG_KEO = 6;

        // ==================================================================
        //  Mau con lai
        // ==================================================================

        private static readonly int MAU_TRANG = rgb(0xFF, 0xFF, 0xFF);
        private static readonly int MAU_DEN = rgb(0x00, 0x00, 0x00);

        /// <summary>Đỏ: nút X, dòng HP, dòng nhấn mạnh.</summary>
        private static readonly int MAU_DO = rgb(0xC0, 0x2E, 0x22);

        private static readonly int MAU_XANH = rgb(0x2E, 0x8B, 0x33);
        private static readonly int MAU_TIM = rgb(0x6B, 0x3F, 0xA8);

        /// <summary>Nền thẻ ĐANG chọn — nâu đậm, chữ trên nó phải sáng.</summary>
        private static readonly int MAU_VIEN_SANG = rgb(0x7A, 0x4A, 0x28);

        /// <summary>Màu ngôi sao pha lê đã ép — vàng sáng.</summary>
        private static readonly int MAU_VIEN_SANG_SAO = rgb(0xFF, 0xC1, 0x2E);

        /// <summary>Nền nút nhóm bản đồ đang mở — xanh lá nhạt.</summary>
        private static readonly int MAU_XANH_NHAT = rgb(0xA6, 0xE6, 0xB0);

        // ==================================================================
        //  Ma chuyen vat pham
        // ==================================================================
        //
        // BON so nay phai khop UseItem phia may chu, khong duoc doan:
        //     ITEM_BAG_TO_BODY = 4, ITEM_BODY_TO_BAG = 5,
        //     ITEM_BAG_TO_PET_BODY = 6, ITEM_BODY_PET_TO_BAG = 7.
        // Sai mot so la mon do bay sang cho khac hoac bien mat.

        /// <summary>Từ túi lên người mặc.</summary>
        private const int TUI_SANG_NGUOI = 4;

        /// <summary>Từ người mặc xuống túi.</summary>
        private const int NGUOI_SANG_TUI = 5;

        /// <summary>Từ túi sang cho đệ mặc.</summary>
        private const int TUI_SANG_DE = 6;

        /// <summary>Từ đệ đang mặc về túi mình.</summary>
        private const int DE_SANG_TUI = 7;

        // ==================================================================
        //  Ma viec trong hop vat pham
        // ==================================================================

        private const int VIEC_MAC = 0;
        private const int VIEC_CHO_PET = 1;
        private const int VIEC_DUNG = 2;
        private const int VIEC_LAY_RA = 3;
        private const int VIEC_LAY_RA_DE = 5;
        private const int VIEC_VUT = 6;
        private const int VIEC_DUNG_NHIEU = 7;

        // ==================================================================
        //  Hop vat pham dang mo
        // ==================================================================

        /// <summary>Đang mở hộp thông tin của một món.</summary>
        private bool hienHop;

        /// <summary>Món đang xem trong hộp, hoặc null.</summary>
        private Item monXem;

        /// <summary>Tên ô của món đang xem, để nói được cả khi ô trống.</summary>
        private string tenOXem = "";

        /// <summary>Chỉ số ô trong TÚI của món đang xem, hoặc -1.</summary>
        private int oTuiXem = -1;

        // ==================================================================
        //  Hop nhap so luong — dung cho "Dung nhieu"
        // ==================================================================

        /// <summary>Đang mở hộp nhập số lượng.</summary>
        private bool hienHopSo;

        // ==================================================================
        //  Hop nang chi so bang tiem nang
        // ==================================================================

        /// <summary>Đang mở hộp nâng chỉ số, hoặc -1.</summary>
        private int chiSoDangNang = -1;

        /// <summary>Ba mức nâng của HP và KI.</summary>
        /// <remarks>
        /// HP và KI mỗi lần nâng được <b>hai chục điểm</b> (xem
        /// <c>hpFrom1000TiemNang</c>), nên nâng từng lần một là vô nghĩa — mốc của
        /// chúng là 20, 200, 2000. Sức đánh, phòng thủ, chí mạng mỗi lần chỉ được
        /// một điểm nên mốc là 1, 10, 100.
        /// </remarks>
        private static readonly int[] MUC_NANG_HP = { 20, 200, 2000 };

        private static readonly int[] MUC_NANG_KHAC = { 1, 10, 100 };

        /// <summary>Ba mức nâng của chỉ số thứ <paramref name="chiSo"/>.</summary>
        private static int[] mucNangCua(int chiSo)
        {
            // 0 HP, 1 KI — hai chi so duy nhat nang theo hang chuc.
            return (chiSo <= 1) ? MUC_NANG_HP : MUC_NANG_KHAC;
        }

        /// <summary>
        /// Mỗi lần nâng thì chỉ số gốc thứ <paramref name="chiSo"/> tăng bao nhiêu.
        /// </summary>
        /// <remarks>
        /// <para>Máy chủ nhận <b>SỐ LẦN</b> nâng, không nhận lượng muốn tăng. Xem
        /// <c>NPoint.increasePoint</c>: HP và KI cộng <c>point * 20</c>, còn sức
        /// đánh, giáp, chí mạng cộng <c>point</c>.</para>
        ///
        /// <para>Đây chính là lỗi "chọn tăng 2000 HP mà nó tăng nhiều hơn hẳn":
        /// bản trước gửi thẳng 2000 làm số lần, tức 2000 × 20 = <b>40.000</b> HP.</para>
        ///
        /// <para>Đọc từ trường máy chủ gửi xuống chứ không viết cứng số 20: đó
        /// cũng là con số hiện trên dòng "… tiềm năng: tăng 20", nên bảng và máy
        /// chủ không thể lệch nhau. Chí mạng không có trường nào về client và máy
        /// chủ cộng đúng một điểm, nên trả về 1.</para>
        /// </remarks>
        private static int buocMotLan(int chiSo)
        {
            var c = Char.myCharz();
            int buoc;
            if (chiSo == 0)
            {
                buoc = c.hpFrom1000TiemNang;
            }
            else if (chiSo == 1)
            {
                buoc = c.mpFrom1000TiemNang;
            }
            else if (chiSo == 2)
            {
                buoc = c.damFrom1000TiemNang;
            }
            else if (chiSo == 3)
            {
                buoc = c.defFrom1000TiemNang;
            }
            else
            {
                buoc = 1;
            }
            return (buoc < 1) ? 1 : buoc;
        }

        /// <summary>Số lần nâng nhiều nhất gửi được trong một gói.</summary>
        /// <remarks>
        /// Máy chủ đọc số lần bằng <c>readShort</c>, nên một gói chỉ chở được tới
        /// 32767. Lượng lớn hơn thì chia thành nhiều gói — chi phí tiềm năng là
        /// tổng của một cấp số cộng nên chia nhỏ hay gửi một lần cũng tốn đúng
        /// bằng nhau, miễn là chỉ số gốc cập nhật giữa các gói, và nó có.
        /// </remarks>
        private const int TOI_DA_MOT_GOI = 30000;

        /// <summary>
        /// Nâng chỉ số gốc thêm <paramref name="luong"/> đơn vị.
        /// </summary>
        /// <remarks>
        /// Nhận LƯỢNG muốn tăng — 2000 nghĩa là HP gốc 200.000 thành 202.000 —
        /// rồi tự đổi sang số lần nâng. Lượng không chia hết cho một bước thì bỏ
        /// phần dư: nâng lố còn tệ hơn nâng thiếu, vì tiềm năng đã trừ rồi không
        /// lấy lại được.
        /// </remarks>
        private static void nangChiSoGoc(int chiSo, int luong)
        {
            if (luong <= 0)
            {
                return;
            }
            int buoc = buocMotLan(chiSo);
            int soLan = luong / buoc;
            while (soLan > 0)
            {
                int lo = (soLan > TOI_DA_MOT_GOI) ? TOI_DA_MOT_GOI : soLan;
                Service.gI().upPotential(chiSo, lo);
                soLan -= lo;
            }
        }

        private void moHopNang(int chiSo)
        {
            chiSoDangNang = chiSo;
            GameCanvas.clearAllPointerEvent();
        }

        /// <summary>Vùng hộp nâng: x, y, rộng, cao.</summary>
        private int[] oHopNang()
        {
            int w = Math.min(rong - 60, 230);
            int h = 84;
            return new int[] { x0 + (rong - w) / 2, y0 + (cao - h) / 2, w, h };
        }

        /// <summary>Vùng nút thứ <paramref name="i"/>: 0..2 ba mức, 3 huỷ.</summary>
        /// <summary>
        /// Vùng nút thứ <paramref name="i"/>: 0..2 ba mức, 3 "tới mức", 4 huỷ.
        /// </summary>
        private int[] oNutHopNang(int i)
        {
            int[] hop = oHopNang();
            int w = hop[2];
            if (i < 3)
            {
                int wn = (w - 20) / 3;
                return new int[] { hop[0] + 8 + i * (wn + 2), hop[1] + 40,
                    wn, 18 };
            }
            int wn2 = (w - 22) / 2;
            if (i == 3)
            {
                return new int[] { hop[0] + 8, hop[1] + 62, wn2, 18 };
            }
            return new int[] { hop[0] + w - 8 - wn2, hop[1] + 62, wn2, 18 };
        }

        /// <summary>Hộp nâng chỉ số, tự vẽ trong bảng.</summary>
        private void veHopNang(mGraphics g)
        {
            if (chiSoDangNang < 0
                    || chiSoDangNang >= TEN_CHI_SO_GOC.Length)
            {
                return;
            }
            int[] hop = oHopNang();
            g.setColor(MAU_DEN, 0.45f);
            g.fillRect(x0, y0, rong, cao, BO_GOC);
            veKhungBo(g, hop[0], hop[1], hop[2], hop[3], MAU_O_MO, 1f,
                    MAU_VIEN, 0.95f, 2);

            mFont.tahoma_7b_red.drawString(g,
                    "Nâng " + TEN_CHI_SO_GOC[chiSoDangNang],
                    hop[0] + hop[2] / 2, hop[1] + 5, mFont.CENTER);
            mFont.tahoma_7.drawString(g, dongGiaNang(chiSoDangNang),
                    hop[0] + hop[2] / 2, hop[1] + 21, mFont.CENTER);

            int[] muc = mucNangCua(chiSoDangNang);
            for (int i = 0; i < muc.Length; i++)
            {
                int[] o = oNutHopNang(i);
                veNut(g, o[0], o[1], o[2], o[3], "+" + muc[i], false);
            }
            int[] toiMuc = oNutHopNang(3);
            veNut(g, toiMuc[0], toiMuc[1], toiMuc[2], toiMuc[3],
                    "Tới mức…", true);
            int[] huy = oNutHopNang(4);
            veNut(g, huy[0], huy[1], huy[2], huy[3], "Huỷ", false);
        }

        /// <summary>Chạm trong hộp nâng. Trả true nếu đã nuốt sự kiện.</summary>
        private bool chamHopNang()
        {
            int[] muc = mucNangCua(chiSoDangNang);
            for (int i = 0; i < muc.Length; i++)
            {
                if (cham2(oNutHopNang(i)))
                {
                    nangChiSoGoc(chiSoDangNang, muc[i]);
                    chiSoDangNang = -1;
                    return true;
                }
            }
            if (cham2(oNutHopNang(3)))
            {
                // "Toi muc": go so lan muon nang. Man nay khong co ban phim rieng
                // nen nhan chu qua IChatable, giong o loc bang.
                // Ban phim so dung chung, co hien san TRAN cua chi so nay.
                //
                // Truoc day dung o go chu: vua phai tu go tung so, vua khong
                // biet nang duoc toi dau nen go bua roi may chu tu choi.
                int chiSo = chiSoDangNang;
                chiSoDangNang = -1;
                BanPhimSo.getInstance().moRa("Nâng tới mức", tranChiSoGoc(chiSo),
                        muc =>
                        {
                            long dangCo = soGocThuong(chiSo);
                            if (muc > dangCo)
                            {
                                nangChiSoGoc(chiSo, (int) (muc - dangCo));
                            }
                        });
                return true;
            }
            if (cham2(oNutHopNang(4)))
            {
                chiSoDangNang = -1;
                return true;
            }
            return true;
        }

        /// <summary>
        /// Trần của một chỉ số gốc, khớp với <c>NPoint</c> bên máy chủ.
        /// </summary>
        /// <remarks>
        /// Chép theo hằng số của máy chủ. Client hiện số này để người chơi biết
        /// nâng được tới đâu; máy chủ vẫn là bên chặn thật, nên lệch nhau thì chỉ
        /// là con số gợi ý sai chứ không thủng trần.
        ///
        /// Thứ tự chỉ số: 0 = HP, 1 = KI, 2 = sức đánh. Chỉ số khác chưa có trần
        /// riêng nên trả 0 = không giới hạn.
        /// </remarks>
        private static long tranChiSoGoc(int chiSo)
        {
            if (chiSo == 0 || chiSo == 1)
            {
                return 600000L;
            }
            return chiSo == 2 ? 25000L : 0L;
        }

        /// <summary>Số lượng đang chọn trong hộp.</summary>
        private int soDangChon = 1;

        /// <summary>Các mức cộng nhanh của hộp số lượng.</summary>
        private static readonly int[] MUC_CONG = { 1, 5, 10, 50 };

        /// <summary>Mở hộp nhập số lượng cho món đang xem.</summary>
        private void moHopSo()
        {
            soDangChon = 1;
            hienHopSo = true;
            GameCanvas.clearAllPointerEvent();
        }

        /// <summary>Số lượng nhiều nhất dùng được, tức số món đang có.</summary>
        private int soToiDa()
        {
            if (monXem == null || monXem.template == null)
            {
                return 1;
            }
            return (monXem.quantity < 1) ? 1 : monXem.quantity;
        }

        /// <summary>Vùng hộp số lượng: x, y, rộng, cao.</summary>
        private int[] oHopSo()
        {
            int w = Math.min(rong - 60, 230);
            int h = 92;
            return new int[] { x0 + (rong - w) / 2, y0 + (cao - h) / 2, w, h };
        }

        /// <summary>
        /// Vùng nút thứ <paramref name="i"/> của hộp số lượng.
        /// </summary>
        /// <remarks>
        /// Thứ tự: 0 trừ · 1 cộng · 2..5 bốn mức cộng nhanh · 6 tất cả · 7 dùng ·
        /// 8 huỷ. Một hàm cho cả phần vẽ lẫn phần bắt chạm.
        /// </remarks>
        private int[] oNutHopSo(int i)
        {
            int[] hop = oHopSo();
            int x = hop[0];
            int y = hop[1];
            int w = hop[2];
            if (i == 0)
            {
                return new int[] { x + 8, y + 22, 20, 18 };
            }
            if (i == 1)
            {
                return new int[] { x + w - 28, y + 22, 20, 18 };
            }
            if (i >= 2 && i <= 5)
            {
                int wn = (w - 20) / 5;
                return new int[] { x + 8 + (i - 2) * (wn + 2), y + 46, wn, 16 };
            }
            if (i == 6)
            {
                int wn = (w - 20) / 5;
                return new int[] { x + 8 + 4 * (wn + 2), y + 46, wn, 16 };
            }
            int wn2 = (w - 22) / 2;
            if (i == 7)
            {
                return new int[] { x + 8, y + 68, wn2, 18 };
            }
            return new int[] { x + w - 8 - wn2, y + 68, wn2, 18 };
        }

        /// <summary>Hộp nhập số lượng, tự vẽ trong bảng.</summary>
        private void veHopSo(mGraphics g)
        {
            if (!hienHopSo)
            {
                return;
            }
            int[] hop = oHopSo();
            g.setColor(MAU_DEN, 0.45f);
            g.fillRect(x0, y0, rong, cao, BO_GOC);
            veKhungBo(g, hop[0], hop[1], hop[2], hop[3], MAU_O_MO, 1f,
                    MAU_VIEN, 0.95f, 2);

            mFont.tahoma_7b_red.drawString(g, "Dùng bao nhiêu?",
                    hop[0] + hop[2] / 2, hop[1] + 5, mFont.CENTER);

            int[] tru = oNutHopSo(0);
            int[] cong = oNutHopSo(1);
            veNut(g, tru[0], tru[1], tru[2], tru[3], "−", false);
            veNut(g, cong[0], cong[1], cong[2], cong[3], "+", false);
            mFont.tahoma_7b_dark.drawString(g,
                    soDangChon + " / " + soToiDa(),
                    hop[0] + hop[2] / 2, hop[1] + 26, mFont.CENTER);

            for (int i = 0; i < MUC_CONG.Length; i++)
            {
                int[] o = oNutHopSo(2 + i);
                veNut(g, o[0], o[1], o[2], o[3], "+" + MUC_CONG[i], false);
            }
            int[] tatCa = oNutHopSo(6);
            veNut(g, tatCa[0], tatCa[1], tatCa[2], tatCa[3], "Hết", false);

            int[] ok = oNutHopSo(7);
            int[] huy = oNutHopSo(8);
            veNut(g, ok[0], ok[1], ok[2], ok[3], "Dùng", true);
            veNut(g, huy[0], huy[1], huy[2], huy[3], "Huỷ", false);
        }

        /// <summary>Chạm trong hộp số lượng. Trả true nếu đã nuốt sự kiện.</summary>
        private bool chamHopSo()
        {
            int toiDa = soToiDa();
            if (cham2(oNutHopSo(0)))
            {
                soDangChon = Math.max(1, soDangChon - 1);
                return true;
            }
            if (cham2(oNutHopSo(1)))
            {
                soDangChon = Math.min(toiDa, soDangChon + 1);
                return true;
            }
            for (int i = 0; i < MUC_CONG.Length; i++)
            {
                if (cham2(oNutHopSo(2 + i)))
                {
                    soDangChon = Math.min(toiDa, soDangChon + MUC_CONG[i]);
                    return true;
                }
            }
            if (cham2(oNutHopSo(6)))
            {
                soDangChon = toiDa;
                return true;
            }
            if (cham2(oNutHopSo(7)))
            {
                batDauDungNhieu(soDangChon);
                hienHopSo = false;
                hienHop = false;
                return true;
            }
            if (cham2(oNutHopSo(8)))
            {
                hienHopSo = false;
                return true;
            }
            // Bam ra ngoai hop: nuot su kien, khong de lot xuong bang ben duoi.
            return true;
        }

        // ==================================================================
        //  Dung nhieu — gui tung lan theo nhip
        // ==================================================================

        /// <summary>Còn bao nhiêu lần dùng nữa.</summary>
        private int conDung;

        /// <summary>Chỉ số ô túi đang dùng dở.</summary>
        private int oDangDung = -1;

        /// <summary>Mốc thời gian được phép gửi lần dùng kế tiếp.</summary>
        private long lucDungKe;

        /// <summary>
        /// Nhịp giữa hai lần dùng, tính bằng mili giây.
        /// </summary>
        /// <remarks>
        /// Gửi liên tiếp không nghỉ thì máy chủ coi là spam và bỏ bớt, mà client
        /// vẫn trừ số lần nên đếm ra một đằng dùng được một nẻo. Nhịp này cũng cho
        /// gói hành trang kịp về để lần sau dùng đúng ô.
        /// </remarks>
        private const long NHIP_DUNG = 260L;

        private void batDauDungNhieu(int soLan)
        {
            if (oTuiXem < 0 || soLan < 1)
            {
                return;
            }
            oDangDung = oTuiXem;
            conDung = soLan;
            lucDungKe = 0L;
        }

        /// <summary>
        /// Gửi tiếp một lần dùng nếu đang dùng nhiều dở.
        /// </summary>
        /// <remarks>
        /// Gọi mỗi khung hình từ <see cref="capNhat"/>. Dừng khi hết lượt, hoặc
        /// khi ô đó không còn món — hết món thì gửi tiếp cũng vô ích.
        /// </remarks>
        private void chayDungNhieu()
        {
            if (conDung <= 0 || oDangDung < 0)
            {
                return;
            }
            long gio = mSystem.currentTimeMillis();
            if (gio < lucDungKe)
            {
                return;
            }
            Item[] tui = Char.myCharz().arrItemBag;
            if (tui == null || oDangDung >= tui.Length
                    || tui[oDangDung] == null
                    || tui[oDangDung].template == null)
            {
                conDung = 0;
                oDangDung = -1;
                return;
            }
            Service.gI().useItem((sbyte) 0, (sbyte) 1, (sbyte) oDangDung,
                    (short) -1);
            conDung--;
            lucDungKe = gio + NHIP_DUNG;
        }

        /// <summary>Gọi mỗi khung hình khi bảng đang mở.</summary>
        public void capNhat()
        {
            if (!dangMo)
            {
                return;
            }
            chayDungNhieu();
        }

        /// <summary>Chỉ số ô ĐANG MẶC của món đang xem, hoặc -1.</summary>
        private int oMacXem = -1;

        /// <summary>
        /// Vùng từng nút trong hộp vật phẩm: mỗi phần tử là
        /// <c>{x, y, rộng, cao, mã việc}</c>.
        /// </summary>
        /// <remarks>
        /// Ghi lại lúc VẼ rồi phần bắt chạm đọc lại, thay vì mỗi bên tự tính. Số
        /// nút đổi theo món (mặc/cho đệ/vứt, hay dùng/dùng nhiều/vứt), nên tính
        /// lại ở hai nơi là sớm muộn cũng lệch.
        /// </remarks>
        private int[][] nutHop = new int[0][];

        // ==================================================================
        //  Trang thai keo cuon
        // ==================================================================

        private bool dangKeo;

        /// <summary>Đã kéo xa quá ngưỡng, nên cái nhả ngón này không tính là bấm.</summary>
        private bool daKeoXa;

        private int yMocKeo;

        /// <summary>
        /// Đang có hộp nhập chữ mở hay không.
        /// </summary>
        /// <remarks>
        /// Bảng này được vẽ SAU cùng trong <c>ClientManager.GUI</c> — sau cả hộp
        /// nhập chữ — nên nếu vẫn vẽ thì nó phủ kín hộp, và người chơi gõ vào một
        /// thứ không thấy. Cũng phải nhả cả phần bắt chạm, kẻo bấm "OK" của hộp
        /// lại rơi vào nút nào của bảng nằm đúng chỗ đó.
        ///
        /// Ẩn chứ không đóng: gõ xong hộp tự tắt và bảng hiện lại nguyên trạng,
        /// đúng thẻ đang xem.
        /// </remarks>
        private static bool dangGoChu()
        {
            return ChatTextField.gI() != null && ChatTextField.gI().isShow;
        }

        /// <summary>Mở bảng.</summary>
        public void moRa()
        {
            dangMo = true;
            hienHop = false;
            cuon = 0;
            // Bo khung chi tiet bang: mo lai bang ma con dinh mot bang cu tu lan
            // truoc thi nguoi choi thay ngay mot khung khong ai goi.
            bangXem = null;
            vaoThe(theChon);
        }

        // ==================================================================
        //  Bo cuc — moi truong duoi day do tinhBoCuc() dat lai moi khung hinh
        // ==================================================================
        //
        // Mot cho duy nhat tinh, moi noi khac chi doc. Truoc day tung co ham tu
        // tinh lai toa do rieng luc bat cham, va no lech khoi phan ve vai diem —
        // bam trung o ben canh ma khong co gi bao.

        /// <summary>Goc va co ca bang.</summary>
        private int x0, y0, rong, cao;

        /// <summary>Dai the tren, vung than, thanh tien.</summary>
        private int yThe, yThan, yTien, caoThan;

        /// <summary>Cot trai va cot phai.</summary>
        private int xTrai, rongTrai, xPhai, rongPhai;

        /// <summary>Dinh hang o trang bi dau tien.</summary>
        private int yLuoiTB;

        /// <summary>Dai the con, va vung noi dung cua cot phai.</summary>
        private int yTheCon, yNoiDung, caoNoiDung;

        /// <summary>Cỡ một ô trang bị (vuông).</summary>
        private int oTrangBi;

        /// <summary>Bề rộng và bề cao một ô túi — ô túi là chữ nhật ngang.</summary>
        private int oTuiNgang, oTuiDoc;

        /// <summary>
        /// Bước nhảy giữa hai hàng ô trang bị.
        /// </summary>
        /// <remarks>
        /// Bằng ĐÚNG cỡ ô. Cộng thêm khe vào đây thì phép tính cỡ ô ở
        /// <c>tinhBoCuc</c> phải trừ đi đúng bấy nhiêu — hai chỗ lệch nhau là lưới
        /// tràn khỏi khung, và đó là lỗi đã xảy ra. Khe nhìn thấy giữa hai ô do
        /// <c>veKhungBo</c> tạo ra: nó thụt vào một điểm mỗi bên.
        /// </remarks>
        private int buocOTrangBi()
        {
            return oTrangBi;
        }

        private void tinhBoCuc()
        {
            // Ti le lay theo ban tham chieu: ngang gap khoang 1,6 lan cao. Ban
            // truoc la 460x320 (1,44) nen hai cot bi bop lai va luoi tui chi vua
            // bon cot.
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

            yThe = y0 + 3;
            yThan = yThe + CAO_THE + 4;
            yTien = y0 + cao - CAO_TIEN - 3;
            caoThan = yTien - yThan - 3;

            xTrai = x0 + LE;
            rongTrai = (rong - LE * 3) * 49 / 100;
            xPhai = xTrai + rongTrai + LE;
            rongPhai = x0 + rong - LE - xPhai;

            int yLuoiDinh = yThan + CAO_DAI_TD + KHE_KHUNG + 3;
            int caoLuoi = caoThan - CAO_DAI_TD - KHE_KHUNG - 5;
            if (caoLuoi < 60)
            {
                caoLuoi = 60;
            }

            // O trang bi phai vua CA HAI chieu.
            //
            // Be cao: NAM hang, khong phai sau. Sach va Chan menh nam ngay tren
            // hang cuoi, kep giua hai cot — dung nhu ban tham chieu. Danh rieng
            // mot hang thu sau cho chung thi ca luoi phai co lai va o nho han han.
            //
            // Be ngang: hang cuoi co BON o (cot trai, Sach, Chan menh, cot phai)
            // nen phai chia cho bon, khong phai cho ba.
            // Buoc nhay giua hai hang BANG DUNG co o, khong cong them khe.
            //
            // Ban truoc tinh co o theo (caoLuoi - 2*khe)/5 ma buoc nhay lai la
            // o + khe, nen nam hang chiem caoLuoi + 3*khe — tran khoi khung dung
            // 15 diem. Lam nhu de tu: o dinh nhau, va chinh phan ve o
            // (veKhungBo thut vao 1 diem moi ben) da tao khe nhin thay.
            int theoNgang = (rongTrai - 8) / 4;
            int theoDoc = caoLuoi / SO_HANG_COT;
            oTrangBi = Math.min(theoNgang, theoDoc);
            if (oTrangBi > O_TOI_DA)
            {
                oTrangBi = O_TOI_DA;
            }
            if (oTrangBi < O_TOI_THIEU)
            {
                oTrangBi = O_TOI_THIEU;
            }

            yLuoiTB = yLuoiDinh
                    + (caoLuoi - SO_HANG_COT * buocOTrangBi()) / 2;
            if (yLuoiTB < yLuoiDinh)
            {
                yLuoiTB = yLuoiDinh;
            }

            yTheCon = yThan;
            yNoiDung = yTheCon + CAO_THE_CON + 3;
            caoNoiDung = yThan + caoThan - yNoiDung;

            // Co o tui lay theo CA HAI chieu: sau cot ngang va bon hang doc.
            //
            // Chia moi be ngang thi so hang ra bao nhieu tuy be cao, va gan
            // nhu luon con mot dai thua o day. Chia ca hai roi lay so nho hon
            // thi luoi luon dung 6x4 va vua khit.
            // Hai chieu tinh RIENG: be ngang chia sau cot, be cao chia nam hang.
            // Khong lay so nho hon nhu truoc — lay so nho hon la ep o thanh vuong,
            // va vuong thi luoi khong the vua ca hai chieu cung luc.
            // Chua BE RONG THANH CUON ra khoi phan chia cot.
            //
            // Khong chua thi cot o cuoi cham sat mep phai, va thanh cuon ve len de
            // mat mot phan anh vat pham o cot do.
            // Tru LE_LUOI hai ben cho net vien khung: khong tru thi hang o ngoai
            // cung ve de len chinh net vien vua ve.
            oTuiNgang = (rongPhai - LE_LUOI * 2 - RONG_CUON) / TUI_SO_COT;
            oTuiDoc = (caoNoiDung - LE_LUOI * 2) / TUI_SO_HANG;
            if (oTuiNgang < O_TOI_THIEU)
            {
                oTuiNgang = O_TOI_THIEU;
            }
            if (oTuiDoc < 14)
            {
                oTuiDoc = 14;
            }
        }

        /// <summary>Vùng nội dung của các thẻ chiếm cả bề ngang.</summary>
        private int xRong()
        {
            return x0 + LE;
        }

        private int rongRong()
        {
            return rong - LE * 2;
        }

        // ==================================================================
        //  Ve — khung, vien chay, be dung
        // ==================================================================
        public void ve(mGraphics g)
        {
            if (!dangMo)
            {
                return;
            }
            tinhBoCuc();

            g.setColor(MAU_DEN, 0.6f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            // Nen hai lop: lop duoi dam, lop tren sang hon va thut vao, cho khung
            // co do sau chu khong phang mot mau.
            // Vien ngoai cung to bang mot hinh BO GOC lop duoi, roi nen bang mot
            // hinh bo goc nho hon de len — cung cach veKhungBo dung. Truoc day
            // vien ve bang bon vach thang nen bon goc bang vuong trong khi nen da
            // bo tron.
            g.setColor(MAU_VIEN, 1f);
            g.fillRect(x0, y0, rong, cao, 12);
            g.setColor(MAU_NEN, 1f);
            g.fillRect(x0 + 2, y0 + 2, rong - 4, cao - 4, 11);

            veDaiThe(g);
            switch (theChon)
            {
                case THE_BAN_THAN:
                    veBanThan(g);
                    break;
                case THE_KY_NANG:
                    veKyNang(g);
                    break;
                case THE_NHIEM_VU:
                    veNhiemVu(g);
                    break;
                case THE_BANG_HOI:
                    veBangHoi(g);
                    break;
                case THE_DE_TU:
                    veDeTu(g);
                    break;
                default:
                    veChucNang(g);
                    break;
            }
            veThanhTien(g);
            // Ve sau cung: hop thong tin phai nam tren moi thu khac.
            veHopMon(g);
            veHopSo(g);
            veHopNang(g);
            veHopKyNang(g);
            veChonO(g);

            // Ve LAI hop nhap chu len tren bang.
            //
            // GameScr ve hop nhap o dong 5516, con bang nay ve o 5601 — tuc bang
            // luon nam tren va che kin hop. Doi thu tu trong GameScr thi anh huong
            // ca man choi, nen ve lai hop mot lan nua o day: ton them mot luot ve
            // nhung hop nam dung tren cung, va bang van thay duoc phia sau.
            if (dangGoChu())
            {
                ChatTextField.gI().paint(g);
            }
        }


        /// <summary>
        /// Bệ đứng dưới chân nhân vật.
        /// </summary>
        /// <remarks>
        /// Không có hàm vẽ hình bầu dục, nên xếp mấy hình chữ nhật bo góc rất tròn
        /// lồng nhau để giả một cái đĩa. Thêm một vòng sáng phập phồng theo
        /// <c>gameTick</c> cho nhân vật khỏi trông như đang lơ lửng.
        /// </remarks>
        /// <summary>Khung ảnh của dáng đứng chờ: 0 hoặc 1, đổi theo nhịp.</summary>
        /// <remarks>
        /// Lấy lại đúng nhịp mà <c>Char.updateChar</c> dùng cho trạng thái đứng
        /// chờ (nhánh <c>case 14</c>): đếm 0..29, phần dư dưới 5 thì khung 0, còn
        /// lại khung 1 — tức thở ra một nhịp ngắn rồi giữ một nhịp dài.
        ///
        /// Truyền thẳng 0 như bản trước thì cả nhân vật lẫn đệ tử đứng cứng như
        /// ảnh dán, trong khi ngoài màn chơi họ vẫn thở. Đây là hai khung duy
        /// nhất chắc chắn có ở mọi bộ ảnh thân người, vì chính game dùng chúng
        /// cho dáng đứng — nên không phải đoán chỉ số khung nào hợp lệ.
        /// </remarks>
        private static int khungDungCho()
        {
            int nhip = GameCanvas.gameTick % 30;
            return (nhip % 15 < 5) ? 0 : 1;
        }

        private void veBeDung(mGraphics g, int xGiua, int yChan, int rongBe)
        {
            int caoBe = Math.max(8, rongBe / 5);

            // Vong sang phap phong: bien do nho, chu ky cham, de no la nhip nen
            // chu khong phai mot cai nhay nhot.
            int nhip = (GameCanvas.gameTick / 4) % 20;
            int phong = (nhip < 10) ? nhip : (20 - nhip);
            g.setColor(MAU_VIEN, 0.10f);
            g.fillRect(xGiua - rongBe / 2 - phong / 2, yChan - caoBe / 2 - 2,
                    rongBe + phong, caoBe + 4, caoBe);

            g.setColor(MAU_O_MO, 0.85f);
            g.fillRect(xGiua - rongBe / 2, yChan - caoBe / 2, rongBe, caoBe,
                    caoBe / 2);
            g.setColor(MAU_VIEN, 0.55f);
            g.fillRect(xGiua - rongBe * 2 / 5, yChan - caoBe / 2 + 1,
                    rongBe * 4 / 5, caoBe - 2, caoBe / 2);
            g.setColor(MAU_VIEN_SANG, 0.35f);
            g.fillRect(xGiua - rongBe / 4, yChan - caoBe / 2 + 2,
                    rongBe / 2, caoBe / 3, caoBe / 3);
        }

        /// <summary>Bề rộng một thẻ trên, đã chừa chỗ cho nút X.</summary>
        /// <remarks>
        /// Một hàm duy nhất cho cả phần vẽ và phần bắt chạm. Trước đây phần vẽ
        /// chia đều cả bề ngang còn nút X thì đè lên thẻ cuối — bấm vào thẻ
        /// "Chức Năng" là đóng luôn bảng.
        /// </remarks>
        private int rongMotThe()
        {
            return (rong - LE * 2 - CANH_NUT_X - 4) / TEN_THE.Length;
        }

        /// <summary>Cạnh nút X. Ô VUÔNG, và cạnh LẺ.</summary>
        /// <remarks>
        /// Cả phần chia bề rộng thẻ trên và cả vùng nút đều đọc hằng số này, nên
        /// đổi một chỗ là đổi cả hai — để hai nơi tự đoán lấy thì nút X đè lên thẻ
        /// cuối, đúng lỗi đã từng xảy ra.
        ///
        /// Cạnh lẻ để dấu X — do <see cref="veChuX"/> vẽ, rộng và cao đều 9, cùng
        /// lẻ — nằm giữa đúng điểm trên cả hai chiều.
        /// </remarks>
        private const int CANH_NUT_X = 19;

        /// <summary>Vùng nút X ở góc phải dải thẻ.</summary>
        private int[] oNutX()
        {
            return new int[] { x0 + rong - LE - CANH_NUT_X, yThe, CANH_NUT_X,
                CANH_NUT_X };
        }

        /// <summary>Cạnh của dấu X do tay vẽ.</summary>
        private const int CANH_CHU_X = 9;

        /// <summary>
        /// Vẽ dấu X bằng hai nét chéo, tự vẽ chứ không lấy từ phông chữ.
        /// </summary>
        /// <remarks>
        /// <para>Mực chữ "X" của phông tahoma_7b là 6×7 — rộng <b>chẵn</b> mà cao
        /// <b>lẻ</b> — nên không có ô vuông nào căn giữa nó đúng điểm trên cả hai
        /// chiều: chiều nào khớp thì chiều kia lệch nửa điểm, và trên một nút nhỏ
        /// chỉ có đúng một ký tự thì nửa điểm ấy nhìn ra ngay.</para>
        ///
        /// <para>Vẽ tay thì nét rộng 9 và cao 9, cùng lẻ, nên vừa giữa đúng điểm
        /// trong một ô vuông cạnh lẻ. Cùng lối với <c>veMotSao</c>, cũng vẽ bằng
        /// <c>fillRect</c> chứ không dùng phông.</para>
        /// </remarks>
        private static void veChuX(mGraphics g, int xGiua, int yGiua)
        {
            // Tam khoi 2x2, moi khoi lech mot diem: mot net tu trai xuong phai,
            // mot net tu phai xuong trai. Ca hinh trai ra dung 9x9 va doi xung
            // theo ca hai truc.
            int x = xGiua - CANH_CHU_X / 2;
            int y = yGiua - CANH_CHU_X / 2;
            for (int i = 0; i < CANH_CHU_X - 1; i++)
            {
                g.fillRect(x + i, y + i, 2, 2);
                g.fillRect(x + CANH_CHU_X - 2 - i, y + i, 2, 2);
            }
        }

        /// <summary>
        /// Dải thẻ trên cùng, kiểu viên thuốc có nét viền nâu.
        /// </summary>
        /// <remarks>
        /// Thẻ đang chọn tô nâu đậm, chữ phải đổi sang SÁNG — dùng chữ nâu trên
        /// nền nâu là mất hẳn dòng chữ.
        /// </remarks>
        private void veDaiThe(mGraphics g)
        {
            int w = rongMotThe();
            for (int i = 0; i < TEN_THE.Length; i++)
            {
                bool sang = (i == theChon);
                int x = x0 + LE + i * w;
                veVienThuoc(g, x, yThe, w - 3, CAO_THE, sang);
                mFont mf = sang ? mFont.tahoma_7b_white : mFont.tahoma_7b_dark;
                mf.drawString(g, TEN_THE[i], x + (w - 3) / 2, yThe + 5,
                        mFont.CENTER);
            }
            veNutX(g);
        }

        /// <summary>Một viên thuốc: nền bo góc + nét viền nâu.</summary>
        private void veVienThuoc(mGraphics g, int x, int y, int w, int h,
                bool dam)
        {
            veKhungBo(g, x, y, w, h,
                    dam ? MAU_VIEN_SANG : MAU_TIEU_DE, 0.98f,
                    MAU_VIEN, dam ? 1f : 0.75f, 1);
        }

        /// <summary>
        /// Nút X đóng bảng, góc trên bên phải.
        /// </summary>
        /// <remarks>
        /// Trước đây phần bắt chạm đã có vùng cho nút này nhưng phần vẽ thì
        /// không — một nút vô hình: đúng chỗ thì đóng được, mà không ai biết là có
        /// chỗ đó. Giờ vẽ ra.
        /// </remarks>
        private void veNutX(mGraphics g)
        {
            int[] o = oNutX();
            // Doi so cuoi la BE DAY VIEN, khong phai ban kinh bo goc.
            //
            // De 4 thi veKhungBo to mau vien day 4 diem moi be roi moi to do o
            // giua, nen o nhin nhu mot vong nau to bao quanh mot o do nho — dung
            // loi "background do chua full het o". Ban kinh bo goc lay tu hang so
            // BO_GOC dung chung ca bang.
            veKhungBo(g, o[0], o[1], o[2], o[3], MAU_DO, 0.95f,
                    MAU_VIEN, 0.9f, 1);

            // Tam o vuong canh le nam dung tai mot diem nguyen, va veChuX cung
            // doi xung quanh tam no — nen dau X can giua dung diem.
            g.setColor(0xffffff, 1f);
            veChuX(g, o[0] + o[2] / 2, o[1] + o[3] / 2);
        }

        /// <summary>Mã vật phẩm thỏi vàng trong hành trang.</summary>
        private const int ID_THOI_VANG = 457;

        /// <summary>Mã icon của thỏi vàng trong kho icon máy chủ.</summary>
        /// <remarks>
        /// Khác mã vật phẩm: <c>drawSmallImage</c> tra theo mã icon, còn 457 là
        /// mã vật phẩm. Lấy từ cột <c>icon_id</c> của bảng <c>item_template</c>.
        /// </remarks>
        private const int ICON_THOI_VANG = 4028;

        /// <summary>
        /// Thanh tiền: vàng, ngọc, hồng ngọc, và thỏi vàng.
        /// </summary>
        /// <remarks>
        /// <para>Trải hết <b>cả hai cột</b> chứ không chỉ cột phải. Bốn con số
        /// nhét vào một nửa bảng thì chữ dính nhau; mà thanh này là chân của cả
        /// bảng nên nó cũng nên rộng bằng cả bảng.</para>
        ///
        /// <para>Thỏi vàng phải <b>đếm trong hành trang</b> chứ không đọc một
        /// trường sẵn có như ba loại kia: nó là vật phẩm, không phải một con số
        /// trên nhân vật. Cộng cả bản khoá lẫn bản thường, vì đây là "đang có
        /// bao nhiêu", không phải "tiêu được bao nhiêu".</para>
        /// </remarks>
        private void veThanhTien(mGraphics g)
        {
            int xT = xTrai;
            int rongT = xPhai + rongPhai - xTrai;
            veKhungBo(g, xT, yTien, rongT, CAO_TIEN, MAU_TIEU_DE, 0.95f,
                    MAU_VIEN, 0.8f, 1);

            var c = Char.myCharz();
            int o = rongT / 4;
            veMotTien(g, Panel.imgXu, c.xuStr, xT, o);
            veMotTien(g, Panel.imgLuong, c.luongStr, xT + o, o);
            veMotTien(g, Panel.imgLuongKhoa, c.luongKhoaStr, xT + o * 2, o);
            veMotTien(g, null, "" + demThoiVang(), xT + o * 3, o);
        }

        /// <summary>Nửa bề ngang của icon tiền.</summary>
        private const int NUA_ICON_TIEN = 8;

        /// <summary>Khe giữa icon và con số.</summary>
        private const int KHE_ICON_SO = 6;

        /// <summary>
        /// Một ô tiền: icon và con số, <b>căn giữa trong ô của nó</b>.
        /// </summary>
        /// <remarks>
        /// <para>Bốn ô chia đều bề ngang thanh, mỗi cụm icon+số nằm giữa ô của
        /// mình. Bản trước đặt cụm ở <i>mép trái</i> mỗi ô nên cả hàng dồn về
        /// trái và hở một khoảng trống ở cuối — bốn con số dài ngắn khác nhau
        /// càng làm nó lệch trông thấy.</para>
        ///
        /// <para>Bề rộng cụm đo bằng <c>getWidth</c> của chính phông đang vẽ,
        /// nên số dài số ngắn đều nằm giữa.</para>
        ///
        /// <para><paramref name="anh"/> để <c>null</c> thì lấy icon thỏi vàng từ
        /// kho icon máy chủ — ba loại tiền kia là ảnh rời của <c>Panel</c>, còn
        /// thỏi vàng là vật phẩm nên chỉ có mã icon.</para>
        /// </remarks>
        private void veMotTien(mGraphics g, Image anh, string so, int xO, int rongO)
        {
            string s = (so == null) ? "0" : s0(so);
            int wChu = mFont.tahoma_7b_red.getWidth(s);
            int wCum = NUA_ICON_TIEN * 2 + KHE_ICON_SO + wChu;
            int xIcon = xO + (rongO - wCum) / 2 + NUA_ICON_TIEN;

            if (anh != null)
            {
                g.drawImage(anh, xIcon, yTien + CAO_TIEN / 2, 3);
            }
            else
            {
                SmallImage.drawSmallImage(g, ICON_THOI_VANG, xIcon,
                        yTien + CAO_TIEN / 2, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);
            }
            mFont.tahoma_7b_red.drawString(g, s,
                    xIcon + NUA_ICON_TIEN + KHE_ICON_SO, yTien + 4, mFont.LEFT);
        }

        private static string s0(string s)
        {
            return s;
        }

        /// <summary>Tổng thỏi vàng trong hành trang, cả khoá lẫn thường.</summary>
        private static long demThoiVang()
        {
            var c = Char.myCharz();
            if (c == null || c.arrItemBag == null)
            {
                return 0;
            }
            long tong = 0;
            for (int i = 0; i < c.arrItemBag.Length; i++)
            {
                Item it = c.arrItemBag[i];
                if (it != null && it.template != null
                        && it.template.id == ID_THOI_VANG)
                {
                    tong += it.quantity;
                }
            }
            return tong;
        }

        // ==================================================================
        //  The "Ban Than"
        // ==================================================================
        /// <summary>
        /// Bề cao chừa dưới cột "Trang bị".
        /// </summary>
        /// <remarks>
        /// Bằng 0: khối "Sức mạnh" và sáu dòng chỉ số đã bỏ khỏi cột này để dành
        /// hết chỗ cho ô trang bị — ô to lên thì ảnh vật phẩm mới đọc được. Muốn
        /// đưa khối đó trở lại thì đặt lại số này rồi gọi <c>veChiSoDuoiNguoi</c>
        /// trong <c>veBanThan</c>; phần tính bố cục đã trừ sẵn theo hằng số này.
        /// </remarks>
        private const int CAO_KHOI_CHI_SO = 0;

        private void veBanThan(mGraphics g)
        {
            veKhungCoTieuDe(g, xTrai, yThan, rongTrai, caoThan, "Trang bị", 0,
                    MAU_DAI_CAM);
            veKhuTrangBi(g);
            veDaiTheCon(g);
            if (theConChon == CON_THONG_TIN)
            {
                veThongTin(g);
            }
            else
            {
                veHanhTrang(g);
            }
        }

        /// <summary>
        /// Khối chỉ số dưới chân nhân vật ở cột "Trang bị".
        /// </summary>
        /// <remarks>
        /// Hai cột nhãn-liền-số, không dàn hai mép. Bản tham chiếu viết
        /// "Hp: 413.761.750.407" sát nhau chứ không đẩy con số ra tận mép phải, vì
        /// số dài mà tách xa nhãn thì đọc phải rê mắt cả dòng.
        /// </remarks>
        private void veChiSoDuoiNguoi(mGraphics g)
        {
            var c = Char.myCharz();
            int yKhoi = yThan + caoThan - CAO_KHOI_CHI_SO;
            int giua = xTrai + rongTrai / 2;

            mFont.tahoma_7b_dark.drawString(g,
                    "Sức mạnh: " + NinjaUtil.getMoneys(c.cPower),
                    giua, yKhoi, mFont.CENTER);

            int xCot1 = xTrai + 8;
            int xCot2 = giua + 4;
            int y = yKhoi + 15;
            veCapChiSo(g, xCot1, xCot2, y,
                    "Hp: " + NinjaUtil.getMoneys((long)(c.cHPFull)),
                    "Ki: " + NinjaUtil.getMoneys(c.cMPFull));
            y += 12;
            veCapChiSo(g, xCot1, xCot2, y,
                    "Sd: " + NinjaUtil.getMoneys(c.cDamFull),
                    "Giáp: " + NinjaUtil.getMoneys(c.cDefull));
            y += 12;
            veCapChiSo(g, xCot1, xCot2, y,
                    "Crit: " + c.cCriticalFull + "%",
                    "Sdcm: " + congChiSo(CS_SUC_DANH_CM) + "%");
        }

        /// <summary>Một hàng hai cột của khối chỉ số dưới nhân vật.</summary>
        /// <remarks>
        /// Chữ nâu đậm, không phải xanh dương: bản tham chiếu để cả khối này cùng
        /// một màu tối, chỉ dòng "Sức mạnh" ở trên là nổi hơn.
        /// </remarks>
        private void veCapChiSo(mGraphics g, int x1, int x2, int y,
                string trai, string phai)
        {
            mFont.tahoma_7b_dark.drawString(g, trai, x1, y, mFont.LEFT);
            mFont.tahoma_7b_dark.drawString(g, phai, x2, y, mFont.LEFT);
        }

        private void veKhuTrangBi(mGraphics g)
        {
            Item[] mac = Char.myCharz().arrItemBody;
            int o = oTrangBi;

            // Chan nhan vat = giua khoi nam hang o, day xuong nua nguoi.
            //
            // Khoi o nay bi khoi chi so o duoi an mat mot phan be cao, nen phan
            // tinh bo cuc da tru CAO_KHOI_CHI_SO ra khoi caoThan truoc khi chia
            // sau hang — xem tinhBoCuc.
            //
            // paintCharBody nhan toa do CHAN chu khong phai tam, nen muon nguoi
            // nam giua khoi thi phai cong them nua chieu cao anh. Truoc day dat
            // chan o day khoi (yThan + 5*o) nen ca nguoi doi ve nua duoi va chua
            // mot vung trong to o nua tren.
            // Can giua BON hang tren, khong tinh hang cuoi: hang cuoi co bon o
            // nen phan giua no da bi Sach va Chan menh chiem.
            int giuaKhoi = yLuoiTB + (SO_HANG_COT - 1) * buocOTrangBi() / 2;
            int yChan = giuaKhoi + CAO_NHAN_VAT / 2;

            // Be dung ve TRUOC nhan vat, nhan vat truoc cac o.
            //
            // Ve o truoc thi anh nhan vat de len khung o hai ben, o dang chon mat
            // vien vang va nhin nhu khong chon duoc.
            veBeDung(g, xTrai + rongTrai / 2, yChan,
                    Math.min(rongTrai - o * 2 - 8, 90));
            Char.myCharz().paintCharBody(g, xTrai + rongTrai / 2, yChan - 4,
                    1, khungDungCho(), true);

            if (mac == null)
            {
                return;
            }
            for (int m = 0; m < mac.Length; m++)
            {
                int[] xy = viTriO(m);
                if (xy == null)
                {
                    continue;
                }
                veMotO(g, mac[m], xy[0], xy[1], o,
                        m < TEN_O.Length ? TEN_O[m] : "");
            }
        }

        /// <summary>
        /// Hai thẻ con của cột phải, vẽ như thẻ dính lên đỉnh khung.
        /// </summary>
        /// <remarks>
        /// Thẻ đang xem tô nền kem <b>giống nền khung ngay dưới</b> và không vẽ
        /// nét dưới, nên nhìn như nó liền một khối với khung — đúng kiểu thẻ trong
        /// bản tham chiếu. Thẻ không xem tô sẫm hơn và có nét viền đủ bốn phía nên
        /// trông như đang nằm phía sau.
        /// </remarks>
        private void veDaiTheCon(mGraphics g)
        {
            int w = rongPhai / TEN_THE_CON.Length;
            for (int i = 0; i < TEN_THE_CON.Length; i++)
            {
                bool sang = (i == theConChon);
                int x = xPhai + i * w;
                veKhungBo(g, x, yTheCon, w - 2, CAO_THE_CON,
                        sang ? MAU_THE_CON_CHON : MAU_TIEU_DE, 0.98f,
                        MAU_VIEN, sang ? 1f : 0.55f, 1);
                mFont mf = sang ? mFont.tahoma_7b_dark : mFont.tahoma_7;
                mf.drawString(g, TEN_THE_CON[i], x + (w - 2) / 2, yTheCon + 4,
                        mFont.CENTER);
            }
        }

        /// <summary>
        /// Thẻ "Thông tin": toàn bộ chỉ số nhân vật.
        /// </summary>
        /// <remarks>
        /// <b>Né đòn</b> cộng từ chỉ số 108 của đồ ĐANG MẶC. Máy chủ có sẵn
        /// <c>tlNeDon</c> nhưng không gửi xuống client, mà thêm một trường vào gói
        /// tin thì phải sửa cả hai đầu — nên tự cộng lấy. Vì thế số này chỉ tính
        /// phần đến từ trang bị; né từ nguồn khác máy chủ giữ riêng, nên né thật có
        /// thể cao hơn. Chính xác và hút máu cũng vậy (chỉ số 109 và 110).
        /// </remarks>
        private void veThongTin(mGraphics g)
        {
            veKhungLom(g, xPhai, yNoiDung, rongPhai, caoNoiDung);

            var c = Char.myCharz();
            int y = yNoiDung + 4;
            y = veDong(g, y, "Tên", c.cName, MAU_TRANG);
            y = veDong(g, y, "Sức mạnh", NinjaUtil.getMoneys(c.cPower), MAU_VIEN);
            y = veDong(g, y, "HP",
                    NinjaUtil.getMoneys((long)(c.cHP)) + " / " + NinjaUtil.getMoneys((long)(c.cHPFull)),
                    MAU_DO);
            y = veDong(g, y, "KI",
                    NinjaUtil.getMoneys(c.cMP) + " / " + NinjaUtil.getMoneys(c.cMPFull),
                    MAU_TRANG);
            y = veDong(g, y, "Sức đánh", NinjaUtil.getMoneys(c.cDamFull), MAU_TRANG);
            y = veDong(g, y, "Giáp", NinjaUtil.getMoneys(c.cDefull), MAU_TRANG);
            y = veDong(g, y, "Chí mạng", c.cCriticalFull + "%", MAU_TRANG);
            y = veDong(g, y, "Né đòn", congChiSo(CS_NE_DON) + "%", MAU_XANH);
            y = veDong(g, y, "Chính xác", congChiSo(CS_CHINH_XAC) + "%", MAU_XANH);
            y = veDong(g, y, "Hút HP", congChiSo(CS_HUT_HP) + "%", MAU_TIM);
            y = veDong(g, y, "Điểm năng động",
                    NinjaUtil.getMoneys(c.cNangdong), MAU_TRANG);
            veDong(g, y, "Xếp hạng", "" + c.rank, MAU_TRANG);
        }

        /// <summary>Một dòng chỉ số: nhãn sát mép trái, số sát mép phải.</summary>
        /// <summary>
        /// Cắt chuỗi cho vừa <paramref name="rong"/> điểm, thêm dấu ba chấm.
        /// </summary>
        /// <remarks>
        /// Đo theo <b>điểm ảnh</b> chứ không theo số ký tự. Bản trước cắt cứng
        /// 18 ký tự, mà một dòng như "19.713.453 / 32.708.512" dài 23 ký tự nên
        /// bị cắt mất nửa sau — người chơi không còn đọc được số tối đa. Đo theo
        /// điểm thì chỉ cắt đúng lúc thật sự hết chỗ.
        /// </remarks>
        private static string catTheoRong(mFont f, string s, int rong)
        {
            if (s == null)
            {
                return "";
            }
            if (rong <= 0 || f.getWidth(s) <= rong)
            {
                return s;
            }
            for (int i = s.Length - 1; i > 0; i--)
            {
                string t = s.Substring(0, i) + "…";
                if (f.getWidth(t) <= rong)
                {
                    return t;
                }
            }
            return s;
        }

        private int veDong(mGraphics g, int y, string nhan, string so, int mau)
        {
            mFont.tahoma_7.drawString(g, nhan, xPhai + 6, y, mFont.LEFT);
            mFont fs = fontCua(mau);
            int con = rongPhai - 16 - mFont.tahoma_7.getWidth(nhan);
            fs.drawString(g, catTheoRong(fs, so, con), xPhai + rongPhai - 6, y,
                    mFont.RIGHT);
            return y + 13;
        }

        /// <summary>Phông chữ theo màu — <c>drawString</c> không nhận màu rời.</summary>
        private static mFont fontCua(int mau)
        {
            if (mau == MAU_DO)
            {
                return mFont.tahoma_7b_red;
            }
            if (mau == MAU_XANH)
            {
                return mFont.tahoma_7b_green;
            }
            if (mau == MAU_VIEN)
            {
                // MAU_VIEN la vang: tren nen kem thi khong doc duoc, nen so lieu
                // duoc nhan manh dung chu DO.
                return mFont.tahoma_7b_red;
            }
            if (mau == MAU_TIM)
            {
                return mFont.tahoma_7b_blue;
            }
            return mFont.tahoma_7b_dark;
        }

        // Ma chi so, tra tu NPoint ben may chu (khong doan theo ten trong bang):
        //   108  "#% Né đòn"                    -> tlNeDon
        //    18  "Chính xác: +#%"               -> tlchinhxac
        //    95  "Biến #% tấn công thành HP"    -> tlHutHp
        //
        // Dung so 109/110 la SAI: trong bang nay 109 la "Hôi, giảm #% HP" va 110
        // la "Dò pha lê".
        private const int CS_NE_DON = 108;
        private const int CS_CHINH_XAC = 18;
        private const int CS_HUT_HP = 95;

        /// <summary>"+#% sức đánh chí mạng" — cột "Sdcm" của bản tham chiếu.</summary>
        private const int CS_SUC_DANH_CM = 5;

        /// <summary>Tổng một chỉ số cộng từ đồ đang mặc.</summary>
        private static int congChiSo(int idChiSo)
        {
            int tong = 0;
            Item[] mac = Char.myCharz().arrItemBody;
            if (mac == null)
            {
                return 0;
            }
            for (int i = 0; i < mac.Length; i++)
            {
                if (mac[i] == null || mac[i].itemOption == null)
                {
                    continue;
                }
                for (int j = 0; j < mac[i].itemOption.Length; j++)
                {
                    ItemOption op = mac[i].itemOption[j];
                    if (op != null && op.optionTemplate != null
                            && op.optionTemplate.id == idChiSo)
                    {
                        tong += op.param;
                    }
                }
            }
            return tong;
        }

        /// <summary>Bề cao dải tiêu đề trên đỉnh mỗi khung.</summary>
        private const int CAO_DAI_TD = 17;

        /// <summary>Bán kính bo góc dùng chung cho mọi khung và nút.</summary>
        private const int BO_GOC = 7;

        /// <summary>
        /// Một khung bo góc: viền bo, nền bo, không còn góc vuông ở đâu.
        /// </summary>
        /// <remarks>
        /// <c>mGraphics.drawRect</c> <b>không nhận bán kính</b> — nó luôn vẽ nét
        /// vuông. Vì thế mọi khung trước đây có nền bo tròn mà viền lại vuông, hở
        /// bốn góc. Ở đây vẽ viền bằng cách tô một hình bo tròn LỚN HƠN bằng màu
        /// viền, rồi tô nền bo tròn đè lên giữa; phần thừa lộ ra chính là nét
        /// viền, và nó bo theo.
        /// </remarks>
        /// <param name="day">Bề dày nét viền, thường là 1.</param>
        private void veKhungBo(mGraphics g, int x, int y, int w, int h,
                int mauNen, float moNen, int mauVien, float moVien, int day)
        {
            g.setColor(mauVien, moVien);
            g.fillRect(x, y, w, h, BO_GOC);
            g.setColor(mauNen, moNen);
            g.fillRect(x + day, y + day, w - day * 2, h - day * 2, BO_GOC - 1);
        }

        /// <summary>
        /// Khung nội dung kèm dải tiêu đề: tên khung ở giữa, một nét gạch dưới.
        /// </summary>
        /// <remarks>
        /// Mọi khung trong bản tham chiếu đều có dải này ("Trang bị", "Chỉ Số",
        /// "Skill", "Danh sách bang", "Chat bang"). Vẽ nó ở một chỗ để sáu thẻ
        /// không ai tự căn tiêu đề theo cách riêng rồi lệch nhau vài điểm.
        /// </remarks>
        /// <param name="mauChu">
        /// 0 nâu đậm · 1 xanh dương · 2 xanh lá · 3 đỏ — theo đúng màu tiêu đề
        /// từng khung trong bản tham chiếu.
        /// </param>
        /// <returns>Đỉnh vùng nội dung, tức ngay dưới dải tiêu đề.</returns>
        private int veKhungCoTieuDe(mGraphics g, int x, int y, int w, int h,
                string ten, int mauChu)
        {
            return veKhungCoTieuDe(g, x, y, w, h, ten, mauChu, MAU_NEN_SANG);
        }

        /// <summary>Bản đầy đủ, chọn được cả màu nền dải.</summary>
        /// <summary>
        /// Dải tiêu đề là một khung RIÊNG, tách khỏi khung nội dung.
        /// </summary>
        /// <remarks>
        /// Hai khung riêng, mỗi khung bo góc và có viền đủ bốn phía, cách nhau một
        /// khe nhỏ — đúng như bản tham chiếu. Bản trước vẽ dải nằm bên trong khung
        /// nội dung nên hai góc trên của dải bị bo theo khung mẹ và nhìn như một
        /// mảnh dán vào, không phải một thanh riêng.
        /// </remarks>
        private int veKhungCoTieuDe(mGraphics g, int x, int y, int w, int h,
                string ten, int mauChu, int mauDai)
        {
            veKhungBo(g, x, y, w, CAO_DAI_TD, mauDai, 0.98f, MAU_VIEN, 0.85f, 1);

            int yND = y + CAO_DAI_TD + KHE_KHUNG;
            veKhungLom(g, x, yND, w, h - CAO_DAI_TD - KHE_KHUNG);

            mFont mf;
            if (mauChu == 1)
            {
                mf = mFont.tahoma_7b_blue;
            }
            else if (mauChu == 2)
            {
                mf = mFont.tahoma_7b_green;
            }
            else if (mauChu == 3)
            {
                mf = mFont.tahoma_7b_red;
            }
            else
            {
                mf = mFont.tahoma_7b_dark;
            }
            mf.drawString(g, ten, x + w / 2, y + 3, mFont.CENTER);
            return yND + 2;
        }

        /// <summary>Khe giữa dải tiêu đề và khung nội dung ngay dưới.</summary>
        private const int KHE_KHUNG = 3;

        /// <summary>
        /// Khung nội dung: nền kem, một nét viền nâu, bo góc.
        /// </summary>
        /// <remarks>
        /// Chỉ MỘT nét viền. Bản trước viền mờ 0.35 nên gần như không thấy ranh
        /// giới giữa khung và nền bảng, hai mảng kem dính vào nhau.
        /// </remarks>
        private void veKhungLom(mGraphics g, int x, int y, int w, int h)
        {
            veKhungBo(g, x, y, w, h, MAU_O_MO, 1f, MAU_VIEN, 0.85f, 1);
        }

        /// <summary>Thẻ "Hành trang": lưới ô, cuộn dọc theo hàng.</summary>
        /// <summary>Bề cao khung lưới túi, làm tròn xuống bội của cỡ ô.</summary>
        private int caoLuoiTui()
        {
            return TUI_SO_HANG * oTuiDoc + LE_LUOI * 2;
        }

        private void veHanhTrang(mGraphics g)
        {
            Item[] tui = Char.myCharz().arrItemBag;

            // Khung luoi tui to NAU DAM, khong kem nhu cac khung khac.
            //
            // Khung quanh luoi, cung kieu voi khung khoi trang bi ben cot trai.
            //
            // Khung cao HET vung noi dung, khong co lai cho vua nam hang o.
            //
            // Be cao vung hiem khi chia het cho be cao mot o, nen co lai thi day
            // khung thieu tới bon diem so voi khung trang bi ben cot trai — hai
            // vien ngang nhau ma khong thang hang, nhin ra ngay. Phan du gio thanh
            // le duoi trong khung; le do khong nhin thay, con vien lech thi co.
            veKhungBo(g, xPhai, yNoiDung, rongPhai, caoNoiDung,
                    MAU_O_MO, 1f, MAU_VIEN, 0.85f, 1);
            if (tui == null)
            {
                return;
            }
            gioiHanCuon(soHangTui());
            int thay = TUI_SO_HANG;
            if (thay < 1)
            {
                thay = 1;
            }
            for (int hang = cuon; hang < cuon + thay; hang++)
            {
                for (int cot = 0; cot < TUI_SO_COT; cot++)
                {
                    int i = hang * TUI_SO_COT + cot;
                    if (i >= tui.Length)
                    {
                        break;
                    }
                    veMotOCN(g, tui[i],
                            xPhai + LE_LUOI + cot * oTuiNgang,
                            yNoiDung + LE_LUOI + (hang - cuon) * oTuiDoc,
                            oTuiNgang, oTuiDoc, "");
                }
            }
            if (soHangTui() > thay)
            {
                veVachCuon(g, xPhai + rongPhai - RONG_CUON + 2, yNoiDung,
                        caoNoiDung, soHangTui(), thay);
            }
        }

        // ==================================================================
        //  Nam the con lai
        // ==================================================================

        // ==================================================================
        //  The "Ky Nang" — hai the con
        // ==================================================================
        /// <summary>Tên năm chỉ số nâng được bằng tiềm năng.</summary>
        /// <remarks>
        /// Thứ tự này là <b>thứ tự mã</b> mà <c>Service.upPotential</c> chờ. Đổi
        /// thứ tự ở đây là bấm nâng HP mà lại nâng giáp.
        /// </remarks>
        private static readonly string[] TEN_CHI_SO_GOC = {
            "HP gốc", "KI gốc", "Sức đánh gốc", "Phòng thủ gốc", "Chí mạng gốc"
        };

        private const int CAO_DONG_CS = 30;

        /// <summary>Bề cao một dòng kỹ năng, và cỡ ô ảnh của nó.</summary>
        /// <remarks>
        /// Nới gấp rưỡi so với bản trước (24 lên 36). Ảnh kỹ năng vẽ ở cỡ gốc
        /// trong một dòng 24 điểm thì gần như dính vào hai dòng chữ hai bên;
        /// dòng 36 điểm cho ảnh thở và tên kỹ năng không phải cắt bớt.
        /// </remarks>
        private const int CAO_DONG_KN = 36;

        /// <summary>
        /// Đỉnh vùng nội dung cột "Skill" của khung hình vừa vẽ.
        /// </summary>
        /// <remarks>
        /// Ghi lại lúc vẽ chứ không tính lại lúc cuộn hay bắt chạm: đỉnh cột phụ
        /// thuộc bề cao dải tiêu đề, tính lại theo công thức riêng là vùng cuộn
        /// lệch khỏi vùng vẽ mà không có gì báo.
        /// </remarks>
        private int yDauKyNang;

        /// <summary>
        /// Thẻ "Kỹ năng": "Chỉ Số" bên trái, "Skill" bên phải, cùng lúc.
        /// </summary>
        /// <remarks>
        /// Bản trước xếp hai phần này thành hai thẻ con phải bấm qua lại. Bản tham
        /// chiếu để cả hai cạnh nhau — nâng một chỉ số rồi thấy ngay kỹ năng nào
        /// vừa đủ tiềm năng để học, không phải nhớ số rồi bấm sang thẻ khác.
        ///
        /// Vì thế dải thẻ con của thẻ này đã bỏ hẳn, cùng với biến nhớ thẻ đang
        /// chọn — không còn gì để chọn thì giữ lại chỉ gây hiểu nhầm.
        /// </remarks>
        private void veKyNang(mGraphics g)
        {
            // Chu NAU DAM (ma 0), khong phai xanh duong: dai tieu de da doi sang
            // nen cam, va xanh duong tren cam thi doc rat met.
            int yND = veKhungCoTieuDe(g, xTrai, yThan, rongTrai, caoThan,
                    "Chỉ Số", 0, MAU_DAI_CAM);
            veChiSoGoc(g, xTrai, rongTrai, yND);

            int yND2 = veKhungCoTieuDe(g, xPhai, yThan, rongPhai, caoThan,
                    "Skill", 0, MAU_DAI_CAM);
            yND2 = veNutNoiTai(g, xPhai, rongPhai, yND2);
            veDsKyNang(g, xPhai, rongPhai, yND2);
        }

        /// <summary>
        /// Thẻ con "Chỉ số": năm chỉ số gốc, giá nâng và mức tăng mỗi lần.
        /// </summary>
        /// <remarks>
        /// <para><b>Giá nâng.</b> Một lần nâng tốn <c>chỉ_số_gốc + 1000</c> tiềm
        /// năng — cùng công thức <c>Panel.doFireSkill</c> dùng (<c>num2 = 1000</c>).
        /// Chép công thức tính giá sang đây là chấp nhận được vì nó chỉ để
        /// <b>hiện</b>; phần trừ tiềm năng thật vẫn do máy chủ làm sau khi
        /// <c>Panel</c> gửi <c>upPotential</c>.</para>
        ///
        /// <para><b>Mức tăng.</b> Đọc từ <c>hpFrom1000TiemNang</c> và các trường
        /// cùng họ. Crit <b>không có</b> trường nào như thế, nên dòng crit không
        /// ghi mức tăng — ghi bừa "tăng 1" là đoán, mà đoán sai thì người chơi
        /// tính nhầm cả kế hoạch nâng.</para>
        ///
        /// <para>Không có ảnh riêng cho từng chỉ số nên mỗi dòng dùng một vạch màu
        /// làm dấu, thay vì đoán mã ảnh và hiện ra ảnh của món khác.</para>
        /// </remarks>
        private void veChiSoGoc(mGraphics g, int x, int w, int yDau)
        {
            var c = Char.myCharz();
            int y = yDau + 3;

            // ---- hang dau: tiem nang, xep hang, the luc, nang dong ----
            mFont.tahoma_7b_green.drawString(g,
                    "TN: " + NinjaUtil.getMoneys(c.cTiemNang), x + 8, y,
                    mFont.LEFT);
            mFont.tahoma_7b_red.drawString(g, "Top: " + c.rank, x + w - 8, y,
                    mFont.RIGHT);
            y += 13;
            mFont.tahoma_7b_dark.drawString(g,
                    "Thể lực: " + c.cStamina + "/" + c.cMaxStamina, x + 8, y,
                    mFont.LEFT);
            mFont.tahoma_7b_dark.drawString(g,
                    "Năng động: " + NinjaUtil.getMoneys(c.cNangdong),
                    x + w - 8, y, mFont.RIGHT);
            y += 16;

            for (int i = 0; i < TEN_CHI_SO_GOC.Length; i++)
            {
                int[] o = oDongChiSo(i);
                if (o[1] + o[3] > yNoiDungPhu() + caoNoiDungPhu())
                {
                    break;
                }
                g.setColor(MAU_O, 0.75f);
                g.fillRect(o[0], o[1], o[2], o[3] - 2, 4);
                g.setColor(mauChiSo(i), 0.95f);
                g.fillRect(o[0] + 2, o[1] + 2, 4, o[3] - 6, 2);

                mFont.tahoma_7b_blue.drawString(g,
                        TEN_CHI_SO_GOC[i] + ": " + soChiSoGoc(i),
                        o[0] + 12, o[1] + 2, mFont.LEFT);
                mFont.tahoma_7b_dark.drawString(g, dongGiaNang(i),
                        o[0] + 12, o[1] + 15, mFont.LEFT);
            }
        }

        /// <summary>Vùng một dòng chỉ số: x, y, rộng, cao.</summary>
        /// <summary>Vùng một dòng chỉ số: x, y, rộng, cao.</summary>
        /// <remarks>
        /// Tính theo <b>cột trái</b>. Cột "Chỉ Số" giờ chỉ chiếm nửa bảng, dùng
        /// <c>rongRong()</c> như bản trước là vùng bấm tràn sang cả cột "Skill" —
        /// bấm vào một kỹ năng bên phải lại mở hộp nâng tiềm năng.
        ///
        /// Số 37: hai hàng chữ đầu (tiềm năng/Top, thể lực/năng động) cộng khoảng
        /// cách. Đi cùng phần vẽ trong <see cref="veChiSoGoc"/>.
        /// </remarks>
        private int[] oDongChiSo(int i)
        {
            return new int[] {
                xTrai + 4,
                yThan + CAO_DAI_TD + KHE_KHUNG + 2 + 37 + i * CAO_DONG_CS,
                rongTrai - 8, CAO_DONG_CS };
        }

        private static int mauChiSo(int i)
        {
            if (i == 0)
            {
                return MAU_DO;
            }
            if (i == 1)
            {
                return MAU_TIM;
            }
            if (i == 4)
            {
                return MAU_XANH;
            }
            return MAU_VIEN;
        }

        /// <summary>Trị số gốc hiện tại của chỉ số thứ <paramref name="i"/>.</summary>
        private static string soChiSoGoc(int i)
        {
            var c = Char.myCharz();
            if (i == 0)
            {
                return NinjaUtil.getMoneys((long)(c.cHPGoc));
            }
            if (i == 1)
            {
                return NinjaUtil.getMoneys(c.cMPGoc);
            }
            if (i == 2)
            {
                return NinjaUtil.getMoneys(c.cDamGoc);
            }
            if (i == 3)
            {
                return "" + c.cDefGoc;
            }
            return c.cCriticalGoc + "%";
        }

        /// <summary>
        /// Chỉ số gốc thứ <paramref name="i"/> dưới dạng SỐ.
        /// </summary>
        /// <remarks>
        /// Khác <c>soChiSoGoc</c>, hàm đó trả về chuỗi đã định dạng ("18.804.104")
        /// để hiện lên bảng. Ở đây cần con số để trừ ra phần còn thiếu tới mức
        /// muốn đạt.
        /// </remarks>
        private static long soGocThuong(int i)
        {
            var c = Char.myCharz();
            if (i == 0)
            {
                return (long)c.cHPGoc;
            }
            if (i == 1)
            {
                return c.cMPGoc;
            }
            if (i == 2)
            {
                return c.cDamGoc;
            }
            if (i == 3)
            {
                return c.cDefGoc;
            }
            return c.cCriticalGoc;
        }

        /// <summary>Dòng "giá tiềm năng: tăng N" của chỉ số thứ <paramref name="i"/>.</summary>
        private static string dongGiaNang(int i)
        {
            var c = Char.myCharz();
            long gia;
            int tang = -1;
            if (i == 0)
            {
                gia = (long)c.cHPGoc + 1000;
                tang = c.hpFrom1000TiemNang;
            }
            else if (i == 1)
            {
                gia = c.cMPGoc + 1000;
                tang = c.mpFrom1000TiemNang;
            }
            else if (i == 2)
            {
                gia = c.cDamGoc + 1000;
                tang = c.damFrom1000TiemNang;
            }
            else if (i == 3)
            {
                gia = c.cDefGoc + 1000;
                tang = c.defFrom1000TiemNang;
            }
            else
            {
                // Crit khong co truong "from1000TiemNang" nao ve client.
                return NinjaUtil.getMoneys(c.cCriticalGoc + 1000)
                        + " tiềm năng";
            }
            return NinjaUtil.getMoneys(gia) + " tiềm năng: tăng " + tang;
        }

        /// <summary>
        /// Thẻ con "Kỹ năng": MỌI kỹ năng của hành tinh đang chơi.
        /// </summary>
        /// <remarks>
        /// Liệt kê từ <c>GameScr.nClasss[cgender].skillTemplates</c> — bảng mẫu kỹ
        /// năng của hành tinh, chứ không phải <c>Char.vSkill</c>. <c>vSkill</c> chỉ
        /// chứa kỹ năng ĐÃ HỌC, nên kỹ năng chưa học sẽ không hiện và người chơi
        /// không biết hành tinh mình còn những gì.
        ///
        /// Cấp đã học tra từ <c>vSkill</c> theo <c>template.id</c>; không tìm thấy
        /// nghĩa là chưa học.
        /// </remarks>
        /// <summary>Cao của ô "Nội tại" — <b>đúng bằng một dòng kỹ năng</b>.</summary>
        /// <remarks>
        /// Lấy thẳng <c>CAO_DONG_KN</c> chứ không gõ một con số riêng: ô này
        /// nằm ngay trên danh sách kỹ năng và phải đọc ra như cùng một danh
        /// sách. Gõ số riêng thì đổi chiều cao dòng kỹ năng là hai bên lệch
        /// nhau, mà không có gì báo.
        /// </remarks>
        private const int CAO_O_NOI_TAI = CAO_DONG_KN;

        /// <summary>
        /// Ô mở bảng Nội tại, đặt trên đầu cột "Skill".
        /// </summary>
        /// <remarks>
        /// <para>Nội tại là một kỹ năng bị động, nên chỗ của nó là cạnh danh sách
        /// kỹ năng. Trước bản này nó <b>không có ô nào trong bảng nhân vật</b>:
        /// đường duy nhất vào là một NPC ngoài bản đồ, và không có gì trong game
        /// nói cho người chơi biết chuyện đó.</para>
        ///
        /// <para>Ô hiện luôn nội tại đang mang, để biết mình đang có gì mà không
        /// phải mở bảng ra xem.</para>
        /// </remarks>
        private int veNutNoiTai(mGraphics g, int x, int w, int yDau)
        {
            // Ve Y HET mot dong ky nang: cung nen MAU_O, cung o anh co vien,
            // cung vi tri chu, cung phong chu.
            //
            // Truoc do o nay dung nen MAU_O_DO — mot mau KEM SANG — roi viet
            // dong thu hai bang chu TRANG len tren. Chu trang tren nen kem thi
            // gan nhu khong doc duoc gi, va nhin ra la mot dong bi mo.
            int y = yDau + 3;
            yDauNoiTai = y;
            int caoDong = CAO_O_NOI_TAI;
            veKhungBo(g, x + 4, y, w - 8, caoDong - 3,
                    MAU_O, 0.9f, MAU_VIEN, 0.45f, 1);

            // Icon noi tai — dung dung anh may chu van gui.
            //
            // `Panel.spearcialImage` la ma anh di kem goi 112 ma viec 0, tuc
            // chinh icon ma bang noi tai cu cua game van ve. Dung lai thi doi
            // noi tai la icon doi theo, khong phai dung mot bang anh thu hai
            // roi lo hai ben lech nhau.
            int oAnh = caoDong - 9;
            veKhungBo(g, x + 8, y + 3, oAnh, oAnh, MAU_O_DO, 1f,
                    MAU_VIEN_O, 0.85f, 1);
            if (Panel.spearcialImage > 0)
            {
                SmallImage.drawSmallImage(g, Panel.spearcialImage,
                        x + 8 + oAnh / 2, y + 3 + oAnh / 2, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);
            }

            int xChu = x + 12 + oAnh;
            mFont.tahoma_7b_blue.drawString(g, "Nội tại", xChu, y + 5,
                    mFont.LEFT);
            string s = Panel.specialInfo;
            bool coNoiTai = (s != null && s.Length > 0);
            if (!coNoiTai)
            {
                s = "Chạm để mở";
            }
            int k = s.IndexOf('[');
            if (k > 0)
            {
                s = s.Substring(0, k).Trim();
            }
            (coNoiTai ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green)
                    .drawString(g, catBot(s, 26), xChu, y + 18, mFont.LEFT);
            return y + caoDong;
        }

        /// <summary>Vùng bấm của ô Nội tại — cùng công thức với lúc vẽ.</summary>
        private int[] oNutNoiTai()
        {
            // yThan + chieu cao dai tieu de: veKhungCoTieuDe tra ve dung moc do,
            // ma o day khong goi lai duoc nen tinh lai bang chinh con so no dung.
            return new int[] { xPhai + 4, yDauNoiTai, rongPhai - 8, CAO_O_NOI_TAI };
        }

        /// <summary>Mốc trên của ô Nội tại ở khung hình vừa vẽ.</summary>
        private int yDauNoiTai;

        private void veDsKyNang(mGraphics g, int x, int w, int yDau)
        {
            yDauKyNang = yDau;
            SkillTemplate[] ds = mauKyNangHanhTinh();
            if (ds == null || ds.Length == 0)
            {
                mFont.tahoma_7.drawString(g, "Chưa nhận được bảng kỹ năng.",
                        x + w / 2, yDau + 20, mFont.CENTER);
                return;
            }
            int caoDong = CAO_DONG_KN;
            int thay = (yThan + caoThan - yDau - 4) / caoDong;
            if (thay < 1)
            {
                thay = 1;
            }
            gioiHanCuon(ds.Length);
            for (int i = cuon; i < ds.Length && i - cuon < thay; i++)
            {
                SkillTemplate mau = ds[i];
                if (mau == null)
                {
                    continue;
                }
                int y = yDau + 3 + (i - cuon) * caoDong;
                int cap = capDaHoc(mau.id);

                veKhungBo(g, x + 4, y, w - 8, caoDong - 3,
                        MAU_O, cap > 0 ? 0.9f : 0.5f, MAU_VIEN, 0.45f, 1);

                // O anh rieng, bo goc, giong ban tham chieu: anh ky nang nam trong
                // mot o co vien chu khong tha noi tren nen dong.
                int oAnh = caoDong - 9;
                veKhungBo(g, x + 8, y + 3, oAnh, oAnh, MAU_O_DO, 1f,
                        MAU_VIEN_O, 0.85f, 1);
                SmallImage.drawSmallImage(g, mau.iconId, x + 8 + oAnh / 2,
                        y + 3 + oAnh / 2, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);

                int xChu = x + 12 + oAnh;
                mFont mfTen = (cap > 0) ? mFont.tahoma_7b_blue
                        : mFont.tahoma_7b_green;
                mfTen.drawString(g, catBot(mau.name, 26), xChu, y + 5,
                        mFont.LEFT);
                if (cap > 0)
                {
                    mFont.tahoma_7b_dark.drawString(g,
                            "Cấp: " + cap + "/" + mau.maxPoint,
                            xChu, y + 18, mFont.LEFT);
                }
                else
                {
                    mFont.tahoma_7.drawString(g, "chưa học", xChu, y + 18,
                            mFont.LEFT);
                }
            }
            if (ds.Length > thay)
            {
                veVachCuon(g, x + w - 6, yNoiDungPhu(), caoNoiDungPhu(),
                        ds.Length, thay);
            }
        }

        /// <summary>Bảng mẫu kỹ năng của hành tinh đang chơi, hoặc null.</summary>
        private static SkillTemplate[] mauKyNangHanhTinh()
        {
            if (GameScr.nClasss == null)
            {
                return null;
            }
            int ht = Char.myCharz().cgender;
            if (ht < 0 || ht >= GameScr.nClasss.Length
                    || GameScr.nClasss[ht] == null)
            {
                return null;
            }
            return GameScr.nClasss[ht].skillTemplates;
        }

        /// <summary>Cấp đã học của mẫu kỹ năng này, hoặc 0.</summary>
        private static int capDaHoc(int idMau)
        {
            MyVector ds = Char.myCharz().vSkill;
            if (ds == null)
            {
                return 0;
            }
            for (int i = 0; i < ds.size(); i++)
            {
                Skill sk = (Skill) ds.elementAt(i);
                if (sk != null && sk.template != null && sk.template.id == idMau)
                {
                    return sk.point;
                }
            }
            return 0;
        }

        /// <summary>
        /// Thẻ "Nhiệm Vụ": nhiệm vụ chính đang làm và tiến độ từng bước.
        /// </summary>
        /// <remarks>
        /// Client chỉ giữ nhiệm vụ CHÍNH (<c>taskMaint</c>), không giữ nhiệm vụ
        /// hằng ngày hay nhiệm vụ phụ — những thứ đó máy chủ không gửi xuống. Nói
        /// rõ ra ở đây, chứ để trống thì trông như đang lỗi.
        /// </remarks>
        /// <summary>Chữ đứng đầu mọi dòng thưởng trong mô tả nhiệm vụ.</summary>
        /// <remarks>
        /// Phần thưởng KHÔNG có cột riêng trong cơ sở dữ liệu — nó nằm lẫn trong
        /// chính chuỗi <c>detail</c> của bảng <c>task_main_template</c>, mỗi dòng
        /// một khoản, ví dụ:
        /// <pre>
        ///   Tìm đến %3, tiêu diệt bọn quái %4 và nhặt về 10 đùi gà
        ///   Thưởng 1 k sức mạnh
        ///   Thưởng 1 k tiềm năng
        ///   Học được kỹ năng bay
        /// </pre>
        /// Nên chỗ cắt là <b>dòng đầu tiên bắt đầu bằng "Thưởng"</b>, và mọi dòng
        /// từ đó xuống thuộc phần thưởng — kể cả dòng không bắt đầu bằng chữ đó
        /// ("Học được kỹ năng bay"), vì <c>splitFontArray</c> có thể ngắt một khoản
        /// dài thành hai dòng và dòng nối sẽ không có chữ "Thưởng".
        /// </remarks>
        private const string DAU_DONG_THUONG = "Thưởng";

        /// <summary>
        /// Thẻ "Nhiệm Vụ": tiêu đề trên cùng, rồi khung nội dung, rồi khung thưởng.
        /// </summary>
        /// <remarks>
        /// Tiêu đề nằm NGOÀI cả hai khung: nó là tên của cả nhiệm vụ, không thuộc
        /// riêng phần nội dung hay phần thưởng.
        /// </remarks>
        private void veNhiemVu(mGraphics g)
        {
            veBanDoNhanh(g);
            int x = xTrai;
            int w = rongTrai;

            Task t = Char.myCharz().taskMaint;
            if (t == null || t.names == null || t.names.Length == 0)
            {
                veKhungLom(g, x, yThan, w, caoThan);
                veChuGiua(g, x, w, "Chưa nhận nhiệm vụ nào.");
                return;
            }

            // ---- tieu de ----
            //
            // names va details la CAC DONG DA NGAT cua ten va mo ta nhiem vu:
            // constructor cua Task goi splitFontArray(name, ...). Chung KHONG
            // phai mot muc cho moi buoc. Lay names[t.index] la lay dong thu
            // "index" cua tieu de roi hien no nhu ca tieu de — bo buoc dau va sai
            // hoan toan tu buoc thu hai tro di.
            int y = yThan + 4;
            for (int i = 0; i < t.names.Length; i++)
            {
                mFont.tahoma_7b_red.drawString(g, t.names[i],
                        x + w / 2, y, mFont.CENTER);
                y += 12;
            }
            y += 4;

            // ---- hai khung: noi dung tren, thuong duoi ----
            int cat = viTriDongThuong(t);
            int soDongThuong = (cat < 0) ? 0 : (t.details.Length - cat);
            int caoThuong = 18 + Math.max(1, soDongThuong) * 12;

            // Chan be cao khung thuong lai hai phan nam chieu cao than.
            //
            // Truoc day khung thuong cao bao nhieu dong thi lay bang nhieu, nen
            // mot nhiem vu ghi nhieu khoan thuong day no cao qua ca than bang:
            // dinh khung thuong leo len tren ca tieu de, va khung noi dung bi ep
            // xuong 40 roi TRAN ra ngoai vien. Gio khung nao dai qua thi cuon
            // trong long no, chu khong an cho cua khung kia.
            int caoThuongToiDa = caoThan * 2 / 5;
            if (caoThuong > caoThuongToiDa)
            {
                caoThuong = caoThuongToiDa;
            }

            int yThuong = yThan + caoThan - caoThuong;
            int caoNoiDung2 = yThuong - 5 - y;
            if (caoNoiDung2 < 40)
            {
                caoNoiDung2 = 40;
            }

            // Ghi lai hai vung: phan keo cuon can biet chung nam dau, ma no chay
            // o capNhat chu khong o day.
            yKhungNdNv = y;
            caoKhungNdNv = caoNoiDung2;
            yKhungThuongNv = yThuong;
            caoKhungThuongNv = caoThuong;

            veKhungLom(g, x, y, w, caoNoiDung2);
            veNoiDungNv(g, t, x, w, y, caoNoiDung2, cat);

            veKhungLom(g, x, yThuong, w, caoThuong);
            veThuongNv(g, t, x, w, yThuong, caoThuong, cat);
        }

        // Hai khung cua the "Nhiem vu" cuon rieng nhau.
        //
        // Bien `cuon` chung dang dung cho danh sach ban do o cot phai — cot phai
        // hien cung luc voi cot trai, nen khong the dung chung: keo mot khung la
        // ca ba nhay.
        private int cuonNdNv;
        private int cuonThuongNv;
        private bool dangKeoNv;
        private int yMocKeoNv;

        /// <summary>Khung đang kéo: 0 nội dung, 1 thưởng, -1 không kéo.</summary>
        private int khungKeoNv = -1;

        private int yKhungNdNv;
        private int caoKhungNdNv;
        private int yKhungThuongNv;
        private int caoKhungThuongNv;

        /// <summary>Số dòng thấy cùng lúc trong một khung cao <paramref name="cao"/>.</summary>
        private static int soDongNvThay(int cao)
        {
            int n = (cao - 18) / 12;
            return (n < 1) ? 1 : n;
        }

        /// <summary>
        /// Kéo cuộn hai khung của thẻ "Nhiệm vụ".
        /// </summary>
        /// <remarks>
        /// Riêng hẳn với <see cref="cuonLuoi"/>: hàm kia chỉ biết một vùng cuộn
        /// cho mỗi thẻ, còn ở đây có ba vùng hiện cùng lúc. Vùng nào nhận cú kéo
        /// thì quyết định lúc <b>đặt ngón xuống</b>, rồi giữ nguyên đến khi nhả —
        /// xét lại theo vị trí ngón đang di thì kéo qua ranh giới hai khung là
        /// nhảy sang cuộn khung kia giữa cú kéo.
        /// </remarks>
        private void cuonNhiemVu()
        {
            Task t = Char.myCharz().taskMaint;
            if (t == null || t.details == null)
            {
                cuonNdNv = 0;
                cuonThuongNv = 0;
                return;
            }
            int cat = viTriDongThuong(t);
            int soNd = dsDongNoiDungNv(t, cat).Count;
            int soTh = (cat < 0) ? 0 : (t.details.Length - cat);
            int thayNd = soDongNvThay(caoKhungNdNv);
            int thayTh = soDongNvThay(caoKhungThuongNv);

            if (GameCanvas.pXYScrollMouse != 0)
            {
                int khung = khungTaiDiem(GameCanvas.pxMouse, GameCanvas.pyMouse);
                int b = (GameCanvas.pXYScrollMouse > 0) ? -1 : 1;
                if (khung == 0)
                {
                    cuonNdNv += b;
                }
                else if (khung == 1)
                {
                    cuonThuongNv += b;
                }
            }

            if (GameCanvas.isPointerDown)
            {
                if (!dangKeoNv)
                {
                    khungKeoNv = khungTaiDiem(GameCanvas.pxFirst,
                            GameCanvas.pyFirst);
                    if (khungKeoNv < 0)
                    {
                        return;
                    }
                    dangKeoNv = true;
                    yMocKeoNv = GameCanvas.pyFirst;
                }
                int buoc = (GameCanvas.py - yMocKeoNv) / 12;
                if (buoc != 0)
                {
                    if (khungKeoNv == 0)
                    {
                        cuonNdNv -= buoc;
                    }
                    else
                    {
                        cuonThuongNv -= buoc;
                    }
                    yMocKeoNv += buoc * 12;
                }
            }
            else
            {
                dangKeoNv = false;
                khungKeoNv = -1;
            }

            cuonNdNv = ganTrongKhoang(cuonNdNv, soNd - thayNd);
            cuonThuongNv = ganTrongKhoang(cuonThuongNv, soTh - thayTh);
        }

        /// <summary>Khung nhiệm vụ tại điểm: 0 nội dung, 1 thưởng, -1 ngoài.</summary>
        private int khungTaiDiem(int x, int y)
        {
            if (x < xTrai || x > xTrai + rongTrai)
            {
                return -1;
            }
            if (y >= yKhungNdNv && y <= yKhungNdNv + caoKhungNdNv)
            {
                return 0;
            }
            if (y >= yKhungThuongNv
                    && y <= yKhungThuongNv + caoKhungThuongNv)
            {
                return 1;
            }
            return -1;
        }

        private static int ganTrongKhoang(int v, int toiDa)
        {
            if (toiDa < 0)
            {
                toiDa = 0;
            }
            if (v > toiDa)
            {
                v = toiDa;
            }
            return (v < 0) ? 0 : v;
        }

        /// <summary>Một dòng chữ trong khung nội dung nhiệm vụ.</summary>
        private class DongNv
        {
            public string chu;
            public string chuPhai;
            public mFont phong;
        }

        /// <summary>
        /// Dựng đúng dãy dòng mà khung nội dung sẽ vẽ: mô tả rồi các bước.
        /// </summary>
        /// <remarks>
        /// <para>Dựng thành một dãy chứ không vẽ trực tiếp, vì cuộn cần biết
        /// <b>tổng số dòng</b> trước khi vẽ dòng đầu — vẽ tới đâu cắt tới đó thì
        /// không tính được tay cuộn, và cũng không biết còn bao nhiêu dòng
        /// dưới.</para>
        ///
        /// <para><b>Đọc đúng hai trường.</b> <c>counts[i]</c> là <b>mốc</b> của
        /// bước thứ i, còn <c>count</c> là <b>tiến độ</b> của bước ĐANG LÀM (bước
        /// <c>index</c>) — chỉ có một con số tiến độ cho cả nhiệm vụ, không phải
        /// một con cho mỗi bước. Bản trước tôi dùng ngược hai trường này nên vừa
        /// tô màu sai vừa in số ngược ("500/3" thay vì "3/500").</para>
        ///
        /// <para><b>Bước không có mốc thì bỏ hẳn số.</b> Máy chủ đặt sẵn mốc
        /// <c>-1</c> cho mọi bước rồi mới ghi mốc thật lên; bước nào nó không ghi
        /// thì giữ <c>-1</c>. In "0/-1" hay "0/0" ra là nói sai — những bước đó là
        /// loại "đến gặp NPC", không có gì để đếm.</para>
        ///
        /// <para><b>Ba màu, không phải hai.</b> Xong là xanh, đang làm là đỏ,
        /// chưa tới là xám. Gộp "đang làm" vào "chưa tới" thì cả danh sách chỉ có
        /// xanh với xám và không đọc ra mình đang ở bước nào.</para>
        /// </remarks>
        private System.Collections.Generic.List<DongNv> dsDongNoiDungNv(Task t, int cat)
        {
            System.Collections.Generic.List<DongNv> ds = new System.Collections.Generic.List<DongNv>();
            if (t.details != null)
            {
                int het = (cat < 0) ? t.details.Length : cat;
                for (int i = 0; i < het; i++)
                {
                    DongNv d = new DongNv();
                    d.chu = t.details[i];
                    d.chuPhai = null;
                    d.phong = mFont.tahoma_7;
                    ds.Add(d);
                }
            }
            if (t.subNames == null)
            {
                return ds;
            }

            // Mot dong trong de tach mo ta khoi danh sach buoc.
            if (ds.Count > 0)
            {
                DongNv trong = new DongNv();
                trong.chu = "";
                trong.chuPhai = null;
                trong.phong = mFont.tahoma_7;
                ds.Add(trong);
            }

            int dangLam = t.index;
            for (int i = 0; i < t.subNames.Length; i++)
            {
                if (t.subNames[i] == null || t.subNames[i].Length == 0)
                {
                    continue;
                }
                int moc = (t.counts != null && i < t.counts.Length)
                        ? t.counts[i] : -1;
                bool xong = (i < dangLam)
                        || (i == dangLam && moc > 0 && t.count >= moc);

                DongNv d = new DongNv();
                d.chu = catBot(t.subNames[i], 40);
                if (xong)
                {
                    d.phong = mFont.tahoma_7b_green;
                }
                else if (i == dangLam)
                {
                    d.phong = mFont.tahoma_7b_red;
                }
                else
                {
                    d.phong = mFont.tahoma_7;
                }
                if (moc > 0)
                {
                    int lam = xong ? moc : ((i == dangLam) ? t.count : 0);
                    if (lam > moc)
                    {
                        lam = moc;
                    }
                    d.chuPhai = lam + "/" + moc;
                }
                ds.Add(d);
            }
            return ds;
        }

        /// <summary>Dòng mô tả đầu tiên thuộc phần thưởng, hoặc -1 nếu không có.</summary>
        private static int viTriDongThuong(Task t)
        {
            if (t.details == null)
            {
                return -1;
            }
            for (int i = 0; i < t.details.Length; i++)
            {
                if (t.details[i] != null
                        && t.details[i].TrimStart().StartsWith(DAU_DONG_THUONG))
                {
                    return i;
                }
            }
            return -1;
        }

        /// <summary>
        /// Cột phải của thẻ "Nhiệm vụ": lưới nhóm ở trên, danh sách bản đồ dưới.
        /// </summary>
        /// <remarks>
        /// Dữ liệu đến từ bảng <c>map_nhanh</c> qua gói 120. Client không tự biết
        /// có những bản đồ nào, nên khi chưa nhận được gói thì nói thẳng là đang
        /// tải chứ không vẽ khung trống.
        /// </remarks>
        private void veBanDoNhanh(mGraphics g)
        {
            veKhungLom(g, xPhai, yThan, rongPhai, caoKhungNhom());
            int so = dsNhomMap.Count;
            for (int i = 0; i < so; i++)
            {
                int[] o = oNutNhom(i, so);
                bool dangXem = (i == nhomChon);

                // Nhom dang mo dung DUNG mau highlight chung cua moi the con.
                //
                // Truoc day rieng cho nay to xanh la, thanh ra ba kieu highlight
                // trong cung mot bang: cam o dai the con, xanh la o day, nau dam o
                // nut viec. Nguoi choi khong doc ra mau nao nghia la "dang chon".
                veKhungBo(g, o[0], o[1], o[2], o[3],
                        dangXem ? MAU_THE_CON_CHON : MAU_O, dangXem ? 1f : 0.9f,
                        MAU_VIEN, dangXem ? 0.9f : 0.55f, 1);

                mFont mf = mFont.tahoma_7b_dark;
                mf.drawString(g, catBot(dsNhomMap[i].ten, 18),
                        o[0] + o[2] / 2, o[1] + 3, mFont.CENTER);
            }

            int yKhung = yKhungMap();
            int caoKhung = caoKhungMap();
            NhomMap nh = nhomDangChon();
            int yND = veKhungCoTieuDe(g, xPhai, yKhung, rongPhai, caoKhung,
                    nh == null ? "Bản đồ" : catBot(nh.ten, 22), 0,
                    MAU_THE_CON_CHON);

            if (dsNhomMap.Count == 0)
            {
                mFont.tahoma_7.drawString(g,
                        daXinBanDo ? "Đang tải danh sách bản đồ…"
                                : "Chưa có danh sách bản đồ.",
                        xPhai + rongPhai / 2, yKhung + caoKhung / 2 - 5,
                        mFont.CENTER);
                return;
            }
            if (nh == null)
            {
                return;
            }

            int thay = soDongMapThay();
            // Dung chung bien cuon voi cac the khac: phan keo cuon o cuonLuoi()
            // ghi vao dung bien nay, giu mot bien rieng la keo mot dang ma ve mot
            // dang.
            gioiHanCuon(nh.diem.Count);
            for (int i = cuon; i < nh.diem.Count && i - cuon < thay; i++)
            {
                int y = yND + (i - cuon) * CAO_DONG_MAP;
                veKhungBo(g, xPhai + 4, y, rongPhai - 8, CAO_DONG_MAP - 2,
                        MAU_O, 0.85f, MAU_VIEN, 0.45f, 1);
                mFont.tahoma_7b_dark.drawString(g,
                        catBot(nh.diem[i].ten, 24) + " [" + nh.diem[i].mapId + "]",
                        xPhai + rongPhai / 2, y + 3, mFont.CENTER);
            }
            if (nh.diem.Count > thay)
            {
                veVachCuon(g, xPhai + rongPhai - 6, yND,
                        caoKhung - CAO_DAI_TD - 4, nh.diem.Count, thay);
            }
        }

        /// <summary>
        /// Khung trên: mô tả nhiệm vụ và các bước, cuộn dọc được.
        /// </summary>
        /// <remarks>
        /// Mô tả và các bước nằm trong CÙNG một dãy dòng nên cuộn chung một tay:
        /// tách hai vùng cuộn trong một khung thì kéo ở nửa trên hay nửa dưới lại
        /// ra hai kết quả khác nhau, và người chơi không thấy được ranh giới đó.
        /// </remarks>
        private void veNoiDungNv(mGraphics g, Task t, int x, int w,
                int yKhung, int caoKhung, int cat)
        {
            mFont.tahoma_7b_red.drawString(g, "NỘI DUNG", x + 8, yKhung + 3,
                    mFont.LEFT);
            System.Collections.Generic.List<DongNv> ds = dsDongNoiDungNv(t, cat);
            int thay = soDongNvThay(caoKhung);
            cuonNdNv = ganTrongKhoang(cuonNdNv, ds.Count - thay);

            int y = yKhung + 16;
            bool coCuon = ds.Count > thay;
            int rongChu = w - 16 - (coCuon ? 8 : 0);
            for (int i = cuonNdNv; i < ds.Count && i - cuonNdNv < thay; i++)
            {
                DongNv d = ds[i];
                d.phong.drawString(g, d.chu, x + 8, y, mFont.LEFT);
                if (d.chuPhai != null)
                {
                    d.phong.drawString(g, d.chuPhai, x + 8 + rongChu, y,
                            mFont.RIGHT);
                }
                y += 12;
            }
            if (coCuon)
            {
                veVachCuonTai(g, x + w - 6, yKhung + 16, caoKhung - 20,
                        ds.Count, thay, cuonNdNv);
            }
        }

        /// <summary>
        /// Khung dưới: phần thưởng của nhiệm vụ.
        /// </summary>
        /// <remarks>
        /// <b>Chưa có nguồn dữ liệu.</b> Phần thưởng nhiệm vụ không nằm ở đâu đọc
        /// được: client không nhận trường nào về thưởng, và cả ba bảng
        /// <c>task_main_template</c>, <c>task_sub_template</c>,
        /// <c>side_task_template</c> đều không có cột thưởng — máy chủ trả thưởng
        /// bằng mã viết thẳng trong {@code TaskService}, rải rác theo từng nhiệm
        /// vụ.
        ///
        /// Vì thế khung này nói thẳng là chưa có, chứ không lấy
        /// <c>contentInfo</c> đắp vào: chuỗi đó là câu thoại gợi ý của bước
        /// (xem <c>Char.taskAction</c>), gắn nhãn "Thưởng" lên nó là nói sai.
        ///
        /// Muốn hiện thật thì cần một cột thưởng trong bảng nhiệm vụ rồi gửi
        /// xuống bằng một mã gói riêng — thêm trường vào gói nhiệm vụ đang chạy là
        /// đụng vào giao thức lõi.
        /// </remarks>
        private void veThuongNv(mGraphics g, Task t, int x, int w,
                int yKhung, int caoKhung, int cat)
        {
            mFont.tahoma_7b_red.drawString(g, "THƯỞNG", x + 8, yKhung + 3,
                    mFont.LEFT);
            if (cat < 0)
            {
                mFont.tahoma_7.drawString(g, "Nhiệm vụ này không ghi thưởng.",
                        x + w / 2, yKhung + 20, mFont.CENTER);
                return;
            }
            int so = t.details.Length - cat;
            int thay = soDongNvThay(caoKhung);
            cuonThuongNv = ganTrongKhoang(cuonThuongNv, so - thay);

            int y = yKhung + 16;
            for (int i = cuonThuongNv; i < so && i - cuonThuongNv < thay; i++)
            {
                mFont.tahoma_7b_green.drawString(g, t.details[cat + i], x + 8,
                        y, mFont.LEFT);
                y += 12;
            }
            if (so > thay)
            {
                veVachCuonTai(g, x + w - 6, yKhung + 16, caoKhung - 20,
                        so, thay, cuonThuongNv);
            }
        }

        /// <summary>Bề cao một dòng trong danh sách bang.</summary>
        private const int CAO_DONG_BANG = 30;

        /// <summary>
        /// Thẻ "Bang hội": trái là của mình, phải là danh sách bang.
        /// </summary>
        /// <remarks>
        /// Hai trạng thái khác hẳn nhau. <b>Chưa có bang</b>: khung trên có hai nút
        /// "Tìm bang" và "Lập bang", khung dưới trống báo chưa tham gia, cột phải
        /// là danh sách bang để xin vào. <b>Đã có bang</b>: khung trên là thông
        /// tin bang, khung dưới là nút rời bang, cột phải vẫn giữ danh sách bang —
        /// bạn nói muốn xem được bang khác kể cả khi đã có bang.
        /// </remarks>
        /// <summary>Hai thẻ con của cột trái "Bang hội", khi CHƯA có bang.</summary>
        private static readonly string[] TEN_THE_BANG = {
            "Thông tin", "Tìm bang"
        };

        /// <summary>Hai thẻ con khi ĐÃ có bang.</summary>
        /// <remarks>
        /// Đã ở trong bang thì "Tìm bang" gần như vô dụng — máy chủ chặn vào bang
        /// thứ hai — nên chỗ đó dành cho danh sách thành viên, thứ người trong
        /// bang xem hằng ngày.
        /// </remarks>
        private static readonly string[] TEN_THE_BANG_CO = {
            "Thông tin", "Thành viên"
        };

        /// <summary>Tên hai thẻ con đang dùng.</summary>
        private static string[] tenTheBang()
        {
            return coBangHoi() ? TEN_THE_BANG_CO : TEN_THE_BANG;
        }

        private const int BANG_THONG_TIN = 0;

        /// <summary>Thẻ con thứ hai: "Tìm bang" khi chưa có bang, "Thành viên" khi đã có.</summary>
        private const int BANG_TIM = 1;

        private int theBangChon = BANG_THONG_TIN;

        /// <summary>
        /// Thẻ "Bang hội": trái là hai thẻ con, phải là chat bang.
        /// </summary>
        /// <remarks>
        /// Danh sách bang chuyển vào thẻ con "Tìm bang" của cột trái, nhường cột
        /// phải cho chat. Chat là thứ dùng liên tục, còn danh sách bang chỉ xem
        /// lúc muốn đổi bang — để nó chiếm nửa bảng cả ngày là không đáng.
        /// </remarks>
        private void veBangHoi(mGraphics g)
        {
            // Cot phai doi theo the con ben trai: dang xem bang minh thi la chat,
            // dang tim bang thi la danh sach bang khac. Hai viec khong bao gio can
            // cung luc, ma go vao hai cot rieng thi mat nua bang cho thu khong
            // dung.
            bool coBang = coBangHoi();
            if (bangXem != null)
            {
                // Dang xem MOT bang cu the trong ket qua tim: chi tiet chiem ca
                // cot phai. Xem chi tiet la viec dung mot luc roi quay lai, nen no
                // muon o day chu khong can them cot rieng.
                veChiTietBangKhac(g);
            }
            else if (coBang && theBangChon == BANG_TIM)
            {
                veDsThanhVien(g, "Thành viên bang", myMemberCuaToi());
            }
            else if (!coBang && theBangChon == BANG_TIM)
            {
                veDsBangKhac(g);
            }
            else
            {
                veChatBang(g);
            }

            veDaiTheNho(g, tenTheBang(), theBangChon, xTrai, rongTrai);
            int yND = yNoiDungPhu();
            int caoND = caoNoiDungPhu();
            veKhungLom(g, xTrai, yND, rongTrai, caoND);
            if (theBangChon == BANG_THONG_TIN)
            {
                veBangThongTin(g, yND, caoND);
            }
            else if (coBang)
            {
                veBangThanhVien(g, yND, caoND);
            }
            else
            {
                veBangTimBang(g, yND, caoND);
            }
        }

        /// <summary>Danh sách thành viên bang của mình, hoặc null.</summary>
        /// <remarks>
        /// Máy chủ gửi kèm ngay trong gói thông tin bang (-537) và client đã cất
        /// vào <c>GameCanvas.panel.myMember</c>, nên màn này chỉ đọc lại — không
        /// cần xin thêm gói nào.
        /// </remarks>
        private static MyVector myMemberCuaToi()
        {
            return (GameCanvas.panel == null) ? null : GameCanvas.panel.myMember;
        }

        /// <summary>Cột trái thẻ con "Thành viên": vài con số về bang.</summary>
        /// <remarks>
        /// Ba dòng theo đúng thứ bậc: chủ bang, phó bang, rồi tổng thành viên —
        /// đọc từ trên xuống là đi từ người đứng đầu tới cả bang.
        /// </remarks>
        private void veBangThanhVien(mGraphics g, int yND, int caoND)
        {
            Clan cl = Char.myCharz().clan;
            MyVector ds = myMemberCuaToi();
            int so = (ds == null) ? 0 : ds.size();

            int soPho = 0;
            for (int i = 0; i < so; i++)
            {
                Member m = (Member) ds.elementAt(i);
                if (m != null && m.role == 1)
                {
                    soPho++;
                }
            }

            // Cung bo mau voi danh sach ben phai: chu bang do, pho bang xanh la,
            // thanh vien chu toi. Dung hai bo mau cho cung mot thu bac la bat
            // nguoi choi hoc hai lan.
            int y = yND + 8;
            mFont.tahoma_7b_red.drawString(g,
                    "Chủ bang: " + ((cl == null) ? "" : catBot(cl.leaderName, 18)),
                    xTrai + 8, y, mFont.LEFT);
            y += 13;
            mFont.tahoma_7b_green.drawString(g, "Phó bang: " + soPho,
                    xTrai + 8, y, mFont.LEFT);
            y += 13;
            mFont.tahoma_7b_dark.drawString(g,
                    "Thành viên: " + so + "/" + ((cl == null) ? 0 : cl.maxMember),
                    xTrai + 8, y, mFont.LEFT);

            veNutMotDong(g, oNutRoiBang(), "Rời bang");
        }

        /// <summary>Thẻ con "Thông tin": bang của mình, và nút rời bang.</summary>
        private void veBangThongTin(mGraphics g, int yND, int caoND)
        {
            Clan cl = Char.myCharz().clan;
            if (cl == null || cl.ID <= 0)
            {
                mFont.tahoma_7.drawString(g, "Chưa tham gia bang.",
                        xTrai + rongTrai / 2, yND + caoND / 2 - 5,
                        mFont.CENTER);
                mFont.tahoma_7.drawString(g,
                        "Sang thẻ \"Tìm bang\" để xin vào hoặc lập bang.",
                        xTrai + rongTrai / 2, yND + caoND / 2 + 8,
                        mFont.CENTER);
                return;
            }
            mFont.tahoma_7b_green.drawString(g,
                    "Bang hội: " + catBot(cl.name, 20),
                    xTrai + rongTrai / 2, yND + 5, mFont.CENTER);

            int xC1 = xTrai + 8;
            int xC2 = xTrai + rongTrai / 2 + 2;

            // Khau hieu NGAY DUOI ten bang: mot dai rieng, mau khac han cac dong
            // chi so.
            //
            // Ten va khau hieu la mot cap — khau hieu la cau bang tu dat cho
            // minh, doc lien voi ten moi co nghia. De no o duoi cung nhu ban truoc
            // thi no lot vao giua cac con so va doc nhu mot dong chi so nua.
            //
            // Chu bang co them nut sua ngay canh: bat nguoi ta ra Quy Lao Kame chi
            // de doi mot dong chu la mot vong duong khong can thiet.
            bool laChuBang = laChuBangHoi();
            int yKh = yKhauHieu();
            int rongDai = rongTrai - 16 - (laChuBang ? 34 : 0);
            veKhungBo(g, xC1 - 2, yKh - 2, rongDai, 14,
                    MAU_DAI_CAM, 0.55f, MAU_VIEN, 0.5f, 3);
            string kh = (cl.slogan == null || cl.slogan.Length == 0)
                    ? "(chưa có khẩu hiệu)" : cl.slogan;
            veChuChay(g, mFont.tahoma_7b_red, kh, xC1 + 2, yKh, rongDai - 6);
            if (laChuBang)
            {
                veNutMotDong(g, oNutSuaKhauHieu(yKh), "Sửa");
            }

            int y = yKh + 18;
            veCapChiSoXanh(g, xC1, xC2, y,
                    "Slot: " + cl.currMember + "/" + cl.maxMember,
                    "Cấp bang: " + cl.level);
            y += 13;
            veCapChiSoXanh(g, xC1, xC2, y,
                    "Điểm thành tích: " + cl.clanPoint,
                    "Csl bang: " + (cl.powerPoint == null ? "0" : cl.powerPoint));
            y += 13;
            // Do, cung mau voi ten chu bang trong danh sach thanh vien.
            mFont.tahoma_7b_red.drawString(g,
                    "Chủ bang: " + catBot(cl.leaderName, 20), xC1, y,
                    mFont.LEFT);
            y += 13;
            mFont.tahoma_7b_blue.drawString(g, "Biểu tượng:", xC1, y,
                    mFont.LEFT);
            veAnhBang(g, cl, xC1 + 66, y + 5);

            veNutMotDong(g, oNutRoiBang(), "Rời bang");
        }

        /// <summary>Mình có phải chủ bang hay không.</summary>
        /// <remarks>
        /// Xét <c>Char.myCharz().role</c> chứ KHÔNG xét <c>clan.leaderID</c>: gói
        /// -537 không mang trường nào cho <c>leaderID</c> (nó gửi
        /// <c>leaderName</c>), nên trường ấy luôn bằng 0 và mọi phép so với id
        /// nhân vật đều ra sai — kể cả với chính chủ bang. Còn <c>role</c> thì gói
        /// đó có gửi, và 0 là chủ bang (xem <c>Member.getRole</c>).
        /// </remarks>
        private static bool laChuBangHoi()
        {
            return Char.myCharz().role == 0;
        }

        /// <summary>
        /// Thẻ con "Tìm bang": các nút, và lưới cờ khi đang lập bang.
        /// </summary>
        /// <remarks>
        /// Danh sách bang nằm ở CỘT PHẢI (xem <see cref="veDsBangKhac"/>), không
        /// nhồi vào đây — cột trái hẹp, một danh sách trong đó chỉ hiện được vài
        /// dòng.
        ///
        /// Thẻ này chỉ tồn tại khi <b>chưa</b> có bang — đã có bang thì thẻ con
        /// thứ hai là "Thành viên" (xem <see cref="tenTheBang"/>). Nên ở đây khỏi
        /// phải xét trường hợp đang ở trong bang.
        /// </remarks>
        private void veBangTimBang(mGraphics g, int yND, int caoND)
        {
            // CHI MOT nut tim. Ban truoc co hai nut lam viec gan giong nhau
            // ("Tim bang" gui thang chuoi cu, "Loc ten..." go chuoi moi) — nguoi
            // choi khong doan duoc nen bam cai nao, va bam "Tim bang" luc dau chi
            // gui chuoi rong. Gio mot nut: bam la go ten, go xong la tim.
            veNutMotDong(g, oNutBang(0), locBang.Length > 0
                    ? "Tìm: " + catBot(locBang, 9) : "Tìm bang");
            veNutMotDong(g, oNutBang(1), "Tạo bang");

            if (dangChonCo)
            {
                veLuoiCo(g, yND + 22, caoND - 22);
                return;
            }
            mFont.tahoma_7.drawString(g, "Bấm \"Tìm bang\" rồi nhập tên.",
                    xTrai + rongTrai / 2, yND + caoND / 2 - 9, mFont.CENTER);
            mFont.tahoma_7.drawString(g, "Chọn một bang bên phải để xem.",
                    xTrai + rongTrai / 2, yND + caoND / 2 + 3, mFont.CENTER);
        }

        /// <summary>Đang ở trong một bang hay không.</summary>
        private static bool coBangHoi()
        {
            Clan cl = Char.myCharz().clan;
            return cl != null && cl.ID > 0;
        }

        /// <summary>Đỉnh danh sách bang ở cột phải.</summary>
        private int yDauDsBang()
        {
            return yThan + CAO_DAI_TD + KHE_KHUNG + 4;
        }

        /// <summary>Số cột của lưới cờ bang.</summary>
        private const int CO_SO_COT = 4;

        /// <summary>
        /// Lưới cờ để chọn khi lập bang.
        /// </summary>
        /// <remarks>
        /// Danh sách cờ nằm ở <c>ClanImage.vClanImage</c>, do gói mã 1 nạp vào.
        /// Mỗi cờ có giá riêng (vàng và ngọc) nên ghi giá dưới ảnh — chọn xong mới
        /// biết hết tiền thì mất một lượt gõ tên vô ích.
        /// </remarks>
        /// <summary>
        /// Lưới cờ để chọn khi lập bang, cuộn dọc được.
        /// </summary>
        /// <remarks>
        /// Máy chủ gửi <b>34</b> mẫu đeo lưng (cờ, khăn, ba lô…) — xem
        /// <c>FlagBagService.getFlagsForChooseClan</c>. Bốn cột thì thành chín
        /// hàng, mà khung chỉ vừa hai hàng: bản trước cắt phần thừa và người chơi
        /// chỉ thấy tám cái đầu, tưởng game chỉ có nhiêu đó.
        ///
        /// Ảnh từng cờ về sau, theo từng gói riêng do <c>addClanImage</c> xin —
        /// nên ô nào chưa có ảnh thì để trống một nhịp rồi tự hiện.
        /// </remarks>
        private void veLuoiCo(mGraphics g, int yKhung, int caoKhung)
        {
            mFont.tahoma_7b_red.drawString(g, "Chọn cờ cho bang",
                    xTrai + rongTrai / 2, yKhung + 4, mFont.CENTER);
            int soCo = ClanImage.vClanImage.size();
            if (soCo == 0)
            {
                mFont.tahoma_7.drawString(g, "Đang tải danh sách cờ…",
                        xTrai + rongTrai / 2, yKhung + caoKhung / 2 - 5,
                        mFont.CENTER);
                return;
            }
            int hangThay = soHangCoThay();
            gioiHanCuon((soCo + CO_SO_COT - 1) / CO_SO_COT);
            int dau = cuon * CO_SO_COT;
            for (int i = dau; i < soCo && i < dau + hangThay * CO_SO_COT; i++)
            {
                int[] o = oCoBang(i - dau, yKhung);
                ClanImage ci = (ClanImage) ClanImage.vClanImage.elementAt(i);
                veKhungBo(g, o[0], o[1], o[2], o[3], MAU_O_DO, 1f,
                        MAU_VIEN_O, 0.85f, 1);
                if (ci != null && ci.idImage != null && ci.idImage.Length > 0)
                {
                    SmallImage.drawSmallImage(g, ci.idImage[0],
                            o[0] + o[2] / 2, o[1] + o[3] / 2 - 4, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                }
                if (ci != null)
                {
                    mFont.tahoma_7b_dark.drawString(g, "" + ci.xu,
                            o[0] + o[2] / 2, o[1] + o[3] - 11, mFont.CENTER);
                }
            }
            int tongHang = (soCo + CO_SO_COT - 1) / CO_SO_COT;
            if (tongHang > hangThay)
            {
                veVachCuon(g, xTrai + rongTrai - 6, yKhung + 16,
                        caoKhung - 16, tongHang, hangThay);
            }
        }

        /// <summary>Số hàng cờ thấy cùng lúc.</summary>
        private int soHangCoThay()
        {
            int o = (rongTrai - 10) / CO_SO_COT;
            int cao = yNoiDungPhu() + caoNoiDungPhu() - (yNoiDungPhu() + 22) - 16;
            int n = cao / o;
            return (n < 1) ? 1 : n;
        }

        /// <summary>Vùng một ô cờ: x, y, rộng, cao.</summary>
        private int[] oCoBang(int i, int yKhung)
        {
            int o = (rongTrai - 10) / CO_SO_COT;
            int x = xTrai + 5 + (i % CO_SO_COT) * o;
            int y = yKhung + 16 + (i / CO_SO_COT) * o;
            return new int[] { x, y, o - 3, o - 3 };
        }

        /// <summary>
        /// Vùng nút thứ <paramref name="i"/> của khung bang: x, y, rộng, cao.
        /// </summary>
        /// <remarks>
        /// HAI nút trên một hàng: "Tìm bang" và "Tạo bang". Chỉ dùng ở thẻ con
        /// "Tìm bang", tức chỉ khi chưa có bang.
        /// </remarks>
        private int[] oNutBang(int i)
        {
            int w = (rongTrai - 16) / 2;
            return new int[] { xTrai + 5 + i * (w + 3), yThan + 17, w - 1, 18 };
        }

        /// <summary>Nút một dòng chữ.</summary>
        /// <summary>Nút một dòng chữ, chữ căn giữa theo cả hai chiều.</summary>
        /// <remarks>
        /// Toạ độ y tính từ bề cao THẬT của nút, không phải một số cố định: nút
        /// nào cao khác 18 thì chữ dồn lên đỉnh.
        /// </remarks>
        private void veNutMotDong(mGraphics g, int[] o, string chu)
        {
            veKhungBo(g, o[0], o[1], o[2], o[3], MAU_TIEU_DE, 0.95f,
                    MAU_VIEN, 0.7f, 1);
            mFont.tahoma_7b_blue.drawString(g, chu, o[0] + o[2] / 2,
                    o[1] + (o[3] - 10) / 2, mFont.CENTER);
        }

        /// <summary>Chuỗi lọc tên bang người chơi vừa gõ.</summary>
        private string locBang = "";

        /// <summary>Đang ở bước chọn cờ để lập bang.</summary>
        private bool dangChonCo;

        /// <summary>Cờ vừa chọn, chờ gõ tên bang.</summary>
        private int coDaChon = -1;

        // Viec dang go chu: phai biet chuoi vua go la de LAM GI.
        //
        // Man nay dung chung mot duong nhan chu (IChatable) cho ca o loc bang va
        // o ten bang moi. Khong ghi lai dang go cho viec nao thi ten bang moi se
        // roi vao o loc, va nguoc lai.
        private const int GO_LOC_BANG = 0;
        private const int GO_TEN_BANG = 1;
        private const int GO_MUC_NANG = 2;
        private const int GO_CHAT_BANG = 3;
        private const int GO_KHAU_HIEU = 4;

        /// <summary>Khung hình trước còn đang gõ chữ.</summary>
        private bool vuaGoChu;

        /// <summary>
        /// Mở hộp gõ chữ của game, và nhớ là bảng đang chờ chữ.
        /// </summary>
        /// <remarks>
        /// Phải đặt cờ ở đây, lúc MỞ. Trong lúc hộp còn hiện thì
        /// <c>capNhatCham</c> không hề chạy — <c>GameScr.updateKey</c> chỉ gọi
        /// <c>UpdateTouch</c> khi có màn phụ đang mở, mà <c>coManPhuDangMo</c> trả
        /// false suốt lúc gõ chữ (để hai nút OK / Đóng nhận được cú bấm). Đặt cờ
        /// trong <c>capNhatCham</c> thì nó không bao giờ được đặt.
        /// </remarks>
        private void moHopGoChu(string ten, string goiY, int kieu)
        {
            vuaGoChu = true;
            Utils.startChat(this, ten, goiY, kieu);
        }

        /// <summary>Chỉ số đang chờ gõ số lần nâng, hoặc -1.</summary>
        private int chiSoGoMuc = -1;

        private int viecGoChu = GO_LOC_BANG;

        /// <summary>
        /// Người chơi gõ xong chuỗi lọc bang.
        /// </summary>
        /// <remarks>
        /// Màn này nhận chữ gõ vào qua <c>IChatable</c> vì nó không có bàn phím
        /// riêng. Chỉ dùng cho ô lọc bang; nếu về sau có thêm ô nhập nào nữa thì
        /// phải mang theo một biến cho biết đang gõ cho ô nào, kẻo chuỗi của ô này
        /// rơi vào ô kia.
        /// </remarks>
        public void onChatFromMe(string text, string to)
        {
            string chu = (text == null) ? "" : text.Trim();
            if (viecGoChu == GO_CHAT_BANG)
            {
                if (chu.Length > 0)
                {
                    // Ma 0 = CHAT phia may chu. Xem ClanService.
                    Service.gI().clanMessage(0, chu, -1);
                }
                // Tra lai viec mac dinh: khong tra thi chuoi go o o LOC lan sau
                // lai bi gui thanh tin nhan bang.
                viecGoChu = GO_LOC_BANG;
                return;
            }
            if (viecGoChu == GO_MUC_NANG)
            {
                // Con so go vao la MUC MUON DAT, khong phai so lan nang.
                //
                // Nut ghi "Nang toi muc" nen no phai lam dung nhu vay: go 250000
                // o HP la keo HP goc len 250000. Hieu la so lan nang thi go
                // 250000 se cong 5 trieu HP — lech gap hai chuc lan, va tiem nang
                // da tru thi khong lay lai duoc.
                int muc = docSo(chu);
                if (muc > 0 && chiSoGoMuc >= 0)
                {
                    long dangCo = soGocThuong(chiSoGoMuc);
                    if (muc > dangCo)
                    {
                        nangChiSoGoc(chiSoGoMuc, (int) (muc - dangCo));
                    }
                }
                chiSoGoMuc = -1;
                // Tra lai viec mac dinh: khong tra thi chuoi go o o TIM BANG lan
                // sau lai bi hieu la muc nang.
                viecGoChu = GO_LOC_BANG;
                return;
            }
            if (viecGoChu == GO_KHAU_HIEU)
            {
                if (chu.Length > 0)
                {
                    // Ma 4 = ACCEPT_CHANGE_INFO_CLAN. May chu doc byte anh roi
                    // chuoi khau hieu, va changeInfoClan chon viec THEO CHUOI:
                    // chuoi RONG thi no doi CO thay vi doi khau hieu — nen o day
                    // chi goi khi da co chu, keo di sua khau hieu ma thanh doi co
                    // bang ve so 0.
                    Service.gI().getClan((sbyte) 4, (sbyte) 0, chu);
                }
                viecGoChu = GO_LOC_BANG;
                return;
            }
            if (viecGoChu == GO_TEN_BANG)
            {
                if (chu.Length > 0 && coDaChon >= 0)
                {
                    // Ma 2 la ACCEPT_CREATE_CLAN phia may chu. Xem ClanService.
                    Service.gI().getClan((sbyte) 2, (sbyte) coDaChon, chu);
                }
                dangChonCo = false;
                coDaChon = -1;
                return;
            }
            locBang = chu;
            Service.gI().searchClan(locBang);
            daXinBang = true;
        }

        public void onCancelChat()
        {
        }

        /// <summary>Vùng nút "Sửa" khẩu hiệu, nằm cuối dải khẩu hiệu.</summary>
        private int[] oNutSuaKhauHieu(int yKhauHieuHienTai)
        {
            return new int[] { xTrai + rongTrai - 40, yKhauHieuHienTai - 2,
                32, 14 };
        }

        /// <summary>Toạ độ y của dải khẩu hiệu: ngay dưới dòng tên bang.</summary>
        /// <remarks>
        /// Một hàm duy nhất cho cả phần vẽ và phần bắt chạm nút "Sửa". Trước đây
        /// hai chỗ tự cộng lấy toạ độ riêng, nên đổi cách xếp ở phần vẽ là nút vẽ
        /// một chỗ mà bấm một chỗ khác.
        /// </remarks>
        private int yKhauHieu()
        {
            return yNoiDungPhu() + 19;
        }

        /// <summary>Vùng nút "Rời bang": giữa, sát đáy cột trái.</summary>
        /// <remarks>
        /// Cùng bề cao 18 với mọi nút khác. Trước để 30 nên nút cao gấp rưỡi các
        /// nút cùng bảng, và <c>veNutMotDong</c> đặt chữ ở <c>y + 4</c> nên chữ
        /// dồn lên đỉnh chứ không nằm giữa.
        /// </remarks>
        private int[] oNutRoiBang()
        {
            int w = (rongTrai - 18) / 2;
            return new int[] { xTrai + rongTrai / 2 - w / 2,
                yThan + caoThan - 26, w, 18 };
        }

        private void veCapChiSoXanh(mGraphics g, int x1, int x2, int y,
                string trai, string phai)
        {
            mFont.tahoma_7b_blue.drawString(g, trai, x1, y, mFont.LEFT);
            mFont.tahoma_7b_blue.drawString(g, phai, x2, y, mFont.LEFT);
        }

        /// <summary>
        /// Cột phải: danh sách bang, mỗi dòng là cờ bang, tên và số thành viên.
        /// </summary>
        /// <remarks>
        /// Danh sách này máy chủ gửi qua gói tìm bang có sẵn và nằm ở
        /// <c>GameCanvas.panel.clans</c>. Màn này chỉ đọc lại, không thêm gói mới.
        /// </remarks>
        /// <summary>
        /// Mã loại của tin nhắn "xin vào bang" trong chat bang.
        /// </summary>
        /// <remarks>
        /// Cùng mã việc mà client gửi lên khi xin vào (<c>clanMessage(2, …)</c>),
        /// và máy chủ đặt lại vào <c>type</c> của tin nhắn khi đẩy xuống.
        /// </remarks>
        private const int LOAI_XIN_DAU = 1;
        private const int LOAI_XIN_VAO = 2;

        /// <summary>
        /// Cột phải khi đang ở thẻ "Tìm bang": danh sách các bang khác.
        /// </summary>
        private void veDsBangKhac(mGraphics g)
        {
            veKhungCoTieuDe(g, xPhai, yThan, rongPhai, caoThan,
                    "Bang hội khác", 0, MAU_DAI_CAM);

            Clan[] ds = (GameCanvas.panel == null) ? null : GameCanvas.panel.clans;
            int yDau = yDauDsBang();
            if (ds == null || ds.Length == 0)
            {
                mFont.tahoma_7.drawString(g,
                        daXinBang ? "Đang tải danh sách bang…"
                                : "Bấm \"Tìm bang\" ở khung bên trái.",
                        xPhai + rongPhai / 2, yThan + caoThan / 2 - 5,
                        mFont.CENTER);
                return;
            }
            int thay = (yThan + caoThan - yDau - 4) / CAO_DONG_BANG;
            if (thay < 1)
            {
                thay = 1;
            }
            gioiHanCuon(ds.Length);
            for (int i = cuon; i < ds.Length && i - cuon < thay; i++)
            {
                veMotDongBang(g, ds[i], yDau + (i - cuon) * CAO_DONG_BANG);
            }
            if (ds.Length > thay)
            {
                veVachCuon(g, xPhai + rongPhai - 6, yDau,
                        yThan + caoThan - yDau, ds.Length, thay);
            }
        }

        /// <summary>Bề cao một dòng thành viên.</summary>
        /// <remarks>
        /// Đủ cho một ô ảnh mặt 32 điểm. Ảnh mặt trong game rộng quanh 30 điểm,
        /// nên ô 22 của bản trước cắt mất viền ảnh và chỉ để lộ khúc giữa — trông
        /// như ảnh bị hỏng.
        /// </remarks>
        private const int CAO_DONG_TV = 36;

        /// <summary>Cỡ ô ảnh mặt trong một dòng thành viên.</summary>
        private const int O_MAT_TV = 32;

        /// <summary>
        /// Cột phải: danh sách thành viên, cuộn dọc được.
        /// </summary>
        /// <remarks>
        /// Dùng cho cả bang của mình và bang đang xem — chỉ khác nguồn dữ liệu,
        /// còn cách vẽ một dòng thì y hệt, nên không tách làm hai hàm.
        ///
        /// Màu tên theo chức: chủ bang đỏ, phó bang xanh lá, thành viên thường
        /// chữ tối — cùng quy ước với bảng cũ (xem <c>Member.getRole</c>) để người
        /// chơi không phải học lại một bộ màu thứ hai.
        /// </remarks>
        private void veDsThanhVien(mGraphics g, string tieuDe, MyVector ds)
        {
            int yND = veKhungCoTieuDe(g, xPhai, yThan, rongPhai, caoThan,
                    tieuDe, 0, MAU_DAI_CAM);
            veDsThanhVienTrong(g, ds, yND, yThan + caoThan - 4);
        }

        /// <summary>Phần ruột của danh sách thành viên, trong khoảng y cho sẵn.</summary>
        private void veDsThanhVienTrong(mGraphics g, MyVector ds, int yDau,
                int yHet)
        {
            int so = (ds == null) ? 0 : ds.size();
            if (so == 0)
            {
                mFont.tahoma_7.drawString(g, "Chưa có danh sách thành viên.",
                        xPhai + rongPhai / 2, (yDau + yHet) / 2 - 5,
                        mFont.CENTER);
                return;
            }
            int thay = (yHet - yDau) / CAO_DONG_TV;
            if (thay < 1)
            {
                thay = 1;
            }
            gioiHanCuon(so);
            System.Collections.Generic.List<Member> dsXep = xepTheoChuc(ds);
            for (int i = cuon; i < dsXep.Count && i - cuon < thay; i++)
            {
                veMotDongThanhVien(g, dsXep[i],
                        yDau + (i - cuon) * CAO_DONG_TV);
            }
            if (so > thay)
            {
                veVachCuon(g, xPhai + rongPhai - 6, yDau, yHet - yDau,
                        so, thay);
            }
        }

        /// <summary>
        /// Xếp danh sách thành viên: chủ bang trên, rồi phó bang, rồi thành viên.
        /// </summary>
        /// <remarks>
        /// Máy chủ gửi theo thứ tự trong bảng, tức gần như thứ tự vào bang — chủ
        /// bang có thể nằm lọt giữa danh sách. Xếp lại theo chức để ai nhìn cũng
        /// biết ngay bang do ai dẫn.
        ///
        /// Chèn tuần tự chứ không gọi <c>Sort</c> với bộ so sánh: chỉ vài chục
        /// dòng, và giữ nguyên thứ tự gốc trong cùng một chức thì lần vẽ nào cũng
        /// ra cùng một danh sách — sắp xếp không bền sẽ làm các dòng đổi chỗ qua
        /// từng khung hình.
        /// </remarks>
        private static System.Collections.Generic.List<Member> xepTheoChuc(
                MyVector ds)
        {
            System.Collections.Generic.List<Member> ra =
                    new System.Collections.Generic.List<Member>();
            int so = (ds == null) ? 0 : ds.size();
            for (int chuc = 0; chuc <= 2; chuc++)
            {
                for (int i = 0; i < so; i++)
                {
                    Member m = (Member) ds.elementAt(i);
                    if (m != null && m.role == chuc)
                    {
                        ra.Add(m);
                    }
                }
            }
            // Chuc la nhung so khac 0..2 thi van phai hien, keo mat nguoi.
            for (int i = 0; i < so; i++)
            {
                Member m = (Member) ds.elementAt(i);
                if (m != null && (m.role < 0 || m.role > 2))
                {
                    ra.Add(m);
                }
            }
            return ra;
        }

        /// <summary>Một dòng thành viên: mặt, tên, chức, sức mạnh.</summary>
        /// <remarks>
        /// Chủ bang và phó bang có nền <b>highlight</b>: hai người này quyết mọi
        /// việc của bang nên phải tìm thấy ngay giữa một danh sách dài.
        ///
        /// Ảnh mặt bị <c>setClip</c> giữ trong đúng ô của nó. Ảnh mặt rộng hơn ô
        /// 22 điểm, nên không cắt thì nó tràn sang phải và đè lên tên.
        /// </remarks>
        private void veMotDongThanhVien(mGraphics g, Member m, int y)
        {
            if (m == null)
            {
                return;
            }
            int oAnh = O_MAT_TV;
            veKhungBo(g, xPhai + 4, y, oAnh, oAnh, MAU_O_DO, 1f,
                    MAU_VIEN_O, 0.85f, 1);
            g.setClip(xPhai + 5, y + 1, oAnh - 2, oAnh - 2);
            veMatThanhVien(g, m, xPhai + 4 + oAnh / 2, y + oAnh / 2);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            // Chua them 6 diem giua o anh va khung chu: sat nhau thi chu dinh vao
            // net vien cua o anh va doc rat kho.
            int xChu = xPhai + 10 + oAnh;
            int rongChu = rongPhai - 16 - oAnh;

            int mauNen = MAU_O;
            float dam = 0.85f;
            if (m.role == 0)
            {
                mauNen = MAU_THE_CON_CHON;
                dam = 1f;
            }
            else if (m.role == 1)
            {
                mauNen = MAU_DAI_CAM;
                dam = 0.75f;
            }
            veKhungBo(g, xChu, y, rongChu, oAnh, mauNen, dam, MAU_VIEN,
                    (m.role <= 1) ? 0.85f : 0.4f, 1);

            mFont mfTen = mFont.tahoma_7b_dark;
            if (m.role == 0)
            {
                mfTen = mFont.tahoma_7b_red;
            }
            else if (m.role == 1)
            {
                mfTen = mFont.tahoma_7b_green;
            }
            mfTen.drawString(g, catBot(m.name, 14), xChu + 5, y + 5,
                    mFont.LEFT);
            mFont.tahoma_7.drawString(g,
                    "Sm: " + (m.powerPoint == null ? "0" : m.powerPoint),
                    xChu + 5, y + 17, mFont.LEFT);
            mFont.tahoma_7b_blue.drawString(g, Member.getRole(m.role),
                    xChu + rongChu - 5, y + 5, mFont.RIGHT);
            mFont.tahoma_7.drawString(g, "Đc: " + m.clanPoint,
                    xChu + rongChu - 5, y + 17, mFont.RIGHT);
        }

        /// <summary>
        /// Mặt của một thành viên.
        /// </summary>
        /// <remarks>
        /// <para>Hai đường: có <c>headICON</c> thì vẽ thẳng ảnh đó, không thì tra
        /// <c>GameScr.parts[head]</c> rồi vẽ khung mặt theo <c>Char.CharInfo</c>.
        /// Bỏ nhánh thứ hai thì mọi thành viên không có ảnh đại diện riêng đều
        /// thành ô trống.</para>
        ///
        /// <para><b>Cả hai đường đều vẽ vào GIỮA ô, và KHÔNG cộng
        /// <c>dx</c>/<c>dy</c>.</b> Hai số đó là độ lệch của một bộ phận so với
        /// gốc của cả thân người — <c>dy</c> của phần đầu là một số âm lớn, vì
        /// đầu nằm cao hẳn trên điểm chân. Cộng chúng vào một ô 32 điểm là đẩy
        /// ảnh ra ngoài ô, và đó là lý do ô ảnh trước đây chỉ hiện một khúc.</para>
        /// </remarks>
        private static void veMatThanhVien(mGraphics g, Member m, int xGiua,
                int yGiua)
        {
            int giua = mGraphics.VCENTER | mGraphics.HCENTER;
            if (m.headICON != -1)
            {
                SmallImage.drawSmallImage(g, m.headICON, xGiua, yGiua, 0, giua);
                return;
            }
            if (GameScr.parts == null || m.head < 0
                    || m.head >= GameScr.parts.Length)
            {
                return;
            }
            Part p = GameScr.parts[m.head];
            if (p == null || p.pi == null)
            {
                return;
            }
            int k = Char.CharInfo[0][0][0];
            if (k < 0 || k >= p.pi.Length || p.pi[k] == null)
            {
                return;
            }
            SmallImage.drawSmallImage(g, p.pi[k].id, xGiua, yGiua, 0, giua);
        }

        /// <summary>Bang đang xem chi tiết trong kết quả tìm, hoặc null.</summary>
        private Clan bangXem;

        /// <summary>Thẻ con đang xem của khung chi tiết bang: 0 thông tin, 1 thành viên.</summary>
        private int theBangXemChon;

        /// <summary>Hai thẻ con của khung chi tiết một bang khác.</summary>
        private static readonly string[] TEN_THE_XEM = {
            "Thông tin", "Thành viên"
        };

        /// <summary>
        /// Cột phải: chi tiết một bang trong kết quả tìm.
        /// </summary>
        /// <remarks>
        /// Danh sách thành viên của bang khác về theo gói -50 và client cất vào
        /// <c>GameCanvas.panel.member</c> — KHÁC <c>myMember</c> là bang của
        /// mình. Gói ấy xin một lần lúc bấm vào bang, nên khi mới bấm thì danh
        /// sách còn rỗng và khung nói thẳng là đang tải.
        /// </remarks>
        private void veChiTietBangKhac(mGraphics g)
        {
            int yND = veKhungCoTieuDe(g, xPhai, yThan, rongPhai, caoThan,
                    catBot(bangXem.name, 22), 0, MAU_DAI_CAM);

            veDaiTheNho(g, TEN_THE_XEM, theBangXemChon, xPhai, rongPhai,
                    yND + 1);
            int yNoi = yND + CAO_THE_CON + 4;
            int yHetNoi = oNutXinVao()[1] - 4;

            if (theBangXemChon == 1)
            {
                MyVector ds = (GameCanvas.panel == null)
                        ? null : GameCanvas.panel.member;
                veDsThanhVienTrong(g, ds, yNoi, yHetNoi);
            }
            else
            {
                veThongTinBangKhac(g, yNoi);
            }

            veNutMotDong(g, oNutXinVao(), "Xin vào bang");
            veNutMotDong(g, oNutQuayLai(), "Quay lại");
        }

        /// <summary>Khối thông tin của bang đang xem.</summary>
        private void veThongTinBangKhac(mGraphics g, int yDau)
        {
            Clan cl = bangXem;
            int xC1 = xPhai + 8;
            int y = yDau + 4;

            veKhungBo(g, xC1 - 2, y - 2, rongPhai - 16, 14,
                    MAU_DAI_CAM, 0.55f, MAU_VIEN, 0.5f, 3);
            string kh = (cl.slogan == null || cl.slogan.Length == 0)
                    ? "(chưa có khẩu hiệu)" : cl.slogan;
            veChuChay(g, mFont.tahoma_7b_red, kh, xC1 + 2, y,
                    rongPhai - 22);
            y += 20;

            mFont.tahoma_7b_dark.drawString(g,
                    "Chủ bang: " + catBot(cl.leaderName, 18), xC1, y,
                    mFont.LEFT);
            y += 13;
            mFont.tahoma_7b_dark.drawString(g,
                    "Thành viên: " + cl.currMember + "/" + cl.maxMember,
                    xC1, y, mFont.LEFT);
            y += 13;
            mFont.tahoma_7b_dark.drawString(g, "Cấp bang: " + cl.level,
                    xC1, y, mFont.LEFT);
            y += 13;
            mFont.tahoma_7b_dark.drawString(g,
                    "Điểm thành tích: " + cl.clanPoint, xC1, y, mFont.LEFT);
            y += 13;
            mFont.tahoma_7b_dark.drawString(g,
                    "Sức mạnh: " + (cl.powerPoint == null ? "0" : cl.powerPoint),
                    xC1, y, mFont.LEFT);
            y += 15;
            mFont.tahoma_7b_blue.drawString(g, "Biểu tượng:", xC1, y,
                    mFont.LEFT);
            veAnhBang(g, cl, xC1 + 66, y + 5);
        }

        /// <summary>Vùng nút "Xin vào bang" của khung chi tiết.</summary>
        private int[] oNutXinVao()
        {
            int w = rongPhai - 16;
            return new int[] { xPhai + 8, yThan + caoThan - 46, w, 18 };
        }

        /// <summary>Vùng nút "Quay lại" của khung chi tiết.</summary>
        private int[] oNutQuayLai()
        {
            int w = rongPhai - 16;
            return new int[] { xPhai + 8, yThan + caoThan - 24, w, 18 };
        }

        /// <summary>Bề cao một dòng chat bang.</summary>
        private const int CAO_DONG_CHAT = 26;

        /// <summary>
        /// Cột phải: chat bang, kèm hai nút "Nhắn" và "Xin đậu".
        /// </summary>
        /// <remarks>
        /// Tin nhắn nằm ở <c>ClanMessage.vMessage</c> — máy chủ đẩy vào đó qua gói
        /// chat bang có sẵn, màn này chỉ đọc lại. Dòng nào là lời xin đậu thì kèm
        /// bộ đếm <c>recieve/maxCap</c> và bấm được để cho đậu.
        /// </remarks>
        private void veChatBang(mGraphics g)
        {
            int yND = veKhungCoTieuDe(g, xPhai, yThan, rongPhai, caoThan,
                    "Chat bang", 0, MAU_DAI_CAM);

            Clan cl = Char.myCharz().clan;
            if (cl == null || cl.ID <= 0)
            {
                mFont.tahoma_7.drawString(g, "Vào bang mới có chat bang.",
                        xPhai + rongPhai / 2, yThan + caoThan / 2 - 5,
                        mFont.CENTER);
                return;
            }

            veNutMotDong(g, oNutChat(0), "Nhắn");
            veNutMotDong(g, oNutChat(1), "Xin đậu");

            int yDau = yND + 2;
            int yHet = oNutChat(0)[1] - 3;
            int thay = (yHet - yDau) / CAO_DONG_CHAT;
            if (thay < 1)
            {
                thay = 1;
            }
            int so = ClanMessage.vMessage.size();
            if (so == 0)
            {
                mFont.tahoma_7.drawString(g, "Chưa có tin nhắn nào.",
                        xPhai + rongPhai / 2, yDau + 20, mFont.CENTER);
                return;
            }
            gioiHanCuon(so);
            for (int i = cuon; i < so && i - cuon < thay; i++)
            {
                veMotDongChat(g, (ClanMessage) ClanMessage.vMessage.elementAt(i),
                        yDau + (i - cuon) * CAO_DONG_CHAT);
            }
            if (so > thay)
            {
                veVachCuon(g, xPhai + rongPhai - 6, yDau, yHet - yDau,
                        so, thay);
            }
        }

        /// <summary>Một dòng chat: tên người nhắn, rồi nội dung.</summary>
        /// <remarks>
        /// Ba loại dòng, và phải nhìn ra loại nào ngay: <c>type 0</c> chat thường,
        /// <c>type 1</c> xin đậu, <c>type 2</c> xin vào bang. Hai loại sau máy chủ
        /// KHÔNG gửi chữ nào trong <c>chat</c> — bản trước vẽ <c>chat[0]</c> nên
        /// chúng hiện thành dòng trống, trông y như một tin nhắn rỗng và không ai
        /// biết là bấm được, cũng không biết bấm vào thì xảy ra gì.
        /// </remarks>
        /// <summary>Màu tên người nhắn trong chat bang, theo chức trong bang.</summary>
        /// <remarks>
        /// <para>Chủ bang đỏ, phó bang xanh lá, thành viên đen — nhìn một dòng là
        /// biết ai đang nói. Trước đây tên nào cũng đỏ nên không phân biệt
        /// được.</para>
        ///
        /// <para>Chưa nạp xong danh sách thành viên thì còn một đường nữa: so tên
        /// với tên chủ bang trong phần thông tin bang.</para>
        /// </remarks>
        private mFont chuTenTheoChuc(ClanMessage cm)
        {
            if (cm == null)
            {
                return mFont.tahoma_7b_dark;
            }
            MyVector ds = myMemberCuaToi();
            int so = (ds == null) ? 0 : ds.size();
            for (int i = 0; i < so; i++)
            {
                Member m = (Member) ds.elementAt(i);
                if (m == null)
                {
                    continue;
                }
                bool trungNguoi = (cm.playerId > 0 && m.ID == cm.playerId)
                        || (m.name != null && m.name == cm.playerName);
                if (!trungNguoi)
                {
                    continue;
                }
                if (m.role == 0)
                {
                    return mFont.tahoma_7b_red;
                }
                return (m.role == 1) ? mFont.tahoma_7b_green : mFont.tahoma_7b_dark;
            }
            Clan cl = Char.myCharz().clan;
            if (cl != null && cl.leaderName != null && cl.leaderName == cm.playerName)
            {
                return mFont.tahoma_7b_red;
            }
            return mFont.tahoma_7b_dark;
        }

        private void veMotDongChat(mGraphics g, ClanMessage cm, int y)
        {
            if (cm == null)
            {
                return;
            }
            veKhungBo(g, xPhai + 4, y, rongPhai - 12, CAO_DONG_CHAT - 3,
                    MAU_O_DO, 1f, MAU_VIEN_O, 0.6f, 1);
            chuTenTheoChuc(cm).drawString(g, catBot(cm.playerName, 16),
                    xPhai + 10, y + 2, mFont.LEFT);

            if (cm.type == LOAI_XIN_DAU)
            {
                mFont.tahoma_7b_blue.drawString(g,
                        "xin đậu (" + cm.recieve + "/" + cm.maxCap + ")",
                        xPhai + 10, y + 13, mFont.LEFT);
                // Loi xin cua CHINH minh thi khong co nut cho.
                //
                // Tu cho dau cho minh la lay dau tu ruong roi tra lai vao hanh
                // trang cua chinh minh — khong duoc gi ma van tinh mot luot cho.
                // May chu cung chan viec nay; an nut o day de nguoi choi khong bam
                // vao mot thu chi tra ve loi.
                if (cm.playerId != Char.myCharz().charID)
                {
                    veNutMotDong(g, oNutTrongDongChat(y), "Cho đậu");
                }
                return;
            }
            if (cm.type == LOAI_XIN_VAO)
            {
                mFont.tahoma_7b_blue.drawString(g, "xin vào bang",
                        xPhai + 10, y + 13, mFont.LEFT);
                veNutMotDong(g, oNutTrongDongChat(y), "Cho vào");
                return;
            }
            string noi = "";
            if (cm.chat != null && cm.chat.Length > 0)
            {
                noi = cm.chat[0];
            }
            mFont.tahoma_7b_dark.drawString(g, catBot(noi, 30),
                    xPhai + 10, y + 13, mFont.LEFT);
        }

        /// <summary>Nút hành động cuối một dòng xin đậu / xin vào bang.</summary>
        private int[] oNutTrongDongChat(int y)
        {
            return new int[] { xPhai + rongPhai - 54, y + 3, 42, 16 };
        }

        /// <summary>Vùng nút chat thứ <paramref name="i"/>: 0 nhắn, 1 xin đậu.</summary>
        private int[] oNutChat(int i)
        {
            int w = (rongPhai - 22) / 2;
            return new int[] { xPhai + 8 + i * (w + 6),
                yThan + caoThan - 24, w, 18 };
        }

        private void veMotDongBang(mGraphics g, Clan cl, int y)
        {
            if (cl == null)
            {
                return;
            }
            int oAnh = CAO_DONG_BANG - 6;
            veKhungBo(g, xPhai + 4, y, oAnh, oAnh, MAU_O_DO, 1f,
                    MAU_VIEN_O, 0.85f, 1);
            veAnhBang(g, cl, xPhai + 4 + oAnh / 2, y + oAnh / 2);

            int xChu = xPhai + 8 + oAnh;
            veKhungBo(g, xChu, y, rongPhai - 14 - oAnh, oAnh,
                    MAU_O, 0.85f, MAU_VIEN, 0.4f, 1);
            mFont.tahoma_7b_green.drawString(g, catBot(cl.name, 20),
                    xChu + 6, y + 6, mFont.LEFT);
            mFont.tahoma_7b_dark.drawString(g,
                    cl.currMember + "/" + cl.maxMember,
                    xPhai + rongPhai - 12, y + 6, mFont.RIGHT);
        }

        /// <summary>Đã hỏi máy chủ danh sách bang trong lần mở này chưa.</summary>
        private bool daXinBang;

        /// <summary>
        /// Chạm trong thẻ "Bang hội".
        /// </summary>
        /// <remarks>
        /// "Tìm bang" gọi <c>searchClan("")</c> — chuỗi rỗng nghĩa là lấy tất cả,
        /// đúng cách bảng cũ vẫn dùng. Kết quả về thẳng
        /// <c>GameCanvas.panel.clans</c> nên màn này chỉ việc đọc lại.
        ///
        /// "Lập bang" và "Rời bang" đều đóng bảng trước: cả hai mở hộp nhập tên
        /// hoặc hộp xác nhận của game, để bảng che kín thì không thấy hộp đâu.
        /// </remarks>
        private bool chamBangHoi()
        {
            Clan cl = Char.myCharz().clan;
            bool coBang = (cl != null && cl.ID > 0);

            // Dang xem chi tiet mot bang thi CHI khung do nhan thao tac.
            //
            // Khung chi tiet che kin cot phai, nen de cac nut phia sau van bat
            // cham la bam vao chi tiet lai chay viec cua danh sach nam duoi.
            if (bangXem != null)
            {
                return chamChiTietBang();
            }

            // Dai the con cua COT TRAI.
            int t = theNhoTaiDiem(tenTheBang().Length, xTrai, rongTrai);
            if (t >= 0)
            {
                if (theBangChon != t)
                {
                    cuon = 0;
                }
                theBangChon = t;
                return true;
            }

            // Cot phai: chat bang. Chi bat cham khi cot phai DANG la chat.
            //
            // Da co bang va dang o the "Thanh vien" thi cot phai la danh sach
            // thanh vien, khong phai chat — de nguyen thi hai nut "Nhan" va "Xin
            // dau" van an cham o duoi danh sach du khong con ve ra.
            if (coBang && theBangChon == BANG_THONG_TIN)
            {
                if (cham2(oNutChat(0)))
                {
                    viecGoChu = GO_CHAT_BANG;
                    moHopGoChu("Chat bang", "Nhập tin nhắn",
                            TField.INPUT_TYPE_ANY);
                    return true;
                }
                if (cham2(oNutChat(1)))
                {
                    // Ma 1 = ASK_FOR_PEA phia may chu. Xem ClanService.
                    Service.gI().clanMessage(1, null, -1);
                    return true;
                }
                if (chamDongChat())
                {
                    return true;
                }
            }

            if (theBangChon != BANG_TIM)
            {
                if (coBang && laChuBangHoi()
                        && cham2(oNutSuaKhauHieu(yKhauHieu())))
                {
                    viecGoChu = GO_KHAU_HIEU;
                    moHopGoChu("Khẩu hiệu bang",
                            "Nhập khẩu hiệu mới", TField.INPUT_TYPE_ANY);
                    return true;
                }
                if (coBang && cham2(oNutRoiBang()))
                {
                    dong();
                    Service.gI().leaveClan();
                }
                return true;
            }

            // The con thu hai khi DA co bang la "Thanh vien", khong phai "Tim
            // bang": chi con nut roi bang, va danh sach ben phai de xem.
            if (coBang)
            {
                if (cham2(oNutRoiBang()))
                {
                    dong();
                    Service.gI().leaveClan();
                }
                return true;
            }

            {
                if (cham2(oNutBang(0)))
                {
                    viecGoChu = GO_LOC_BANG;
                    // Go ten bang bang ban phim cua may: man nay khong co ban phim
                    // rieng, ma go tay bang cac nut ve tay thi khong the goi la de
                    // dung. Go xong onChatFromMe goi searchClan.
                    moHopGoChu("Tìm bang",
                            "Nhập tên bang cần tìm", TField.INPUT_TYPE_ANY);
                    return true;
                }
                if (cham2(oNutBang(1)))
                {
                    // Ma 1 = REQUEST_FLAGS_CHOOSE_CREATE_CLAN.
                    //
                    // Ban truoc goi ma 0, ma 0 KHONG CO trong switch cua
                    // ClanService — may chu nhan goi roi bo qua, nen bam "Tao
                    // bang" khong co gi xay ra. Chinh la loi bam vao thay bang
                    // bien mat ma khong hien gi (luc do con goi ca dong()).
                    //
                    // Ma 1 tra ve danh sach co, client nap vao ClanImage. Sau do
                    // chon co roi go ten, va gui ma 2 = ACCEPT_CREATE_CLAN.
                    Service.gI().getClan((sbyte) 1, (sbyte) 0, "");
                    dangChonCo = true;
                    return true;
                }
                if (dangChonCo)
                {
                    int yDuoiCo = yNoiDungPhu() + 22;
                    int soCo = ClanImage.vClanImage.size();
                    int hangThay = soHangCoThay();
                    int dauCo = cuon * CO_SO_COT;
                    for (int i = dauCo;
                            i < soCo && i < dauCo + hangThay * CO_SO_COT; i++)
                    {
                        int[] o = oCoBang(i - dauCo, yDuoiCo);
                        if (cham(o[0], o[1], o[2], o[3]))
                        {
                            ClanImage ci =
                                    (ClanImage) ClanImage.vClanImage.elementAt(i);
                            coDaChon = (ci == null) ? -1 : ci.ID;
                            viecGoChu = GO_TEN_BANG;
                            moHopGoChu("Lập bang",
                                    "Nhập tên bang", TField.INPUT_TYPE_ANY);
                            return true;
                        }
                    }
                }
            }

            // Bam mot bang trong danh sach: MO CHI TIET bang do.
            //
            // Truoc day bam la gui thang loi xin vao. Do la mot viec khong the
            // hoan lai, quyet dinh chi dua tren mot dong ten — nen gio bam vao la
            // mo khung chi tiet (thong tin + thanh vien), con xin vao thi co nut
            // rieng trong khung ay.
            Clan[] ds = (GameCanvas.panel == null) ? null : GameCanvas.panel.clans;
            if (ds == null)
            {
                return true;
            }
            int yDau = yDauDsBang();
            int thay = (yThan + caoThan - yDau - 4) / CAO_DONG_BANG;
            for (int i = cuon; i < ds.Length && i - cuon < thay; i++)
            {
                int y = yDau + (i - cuon) * CAO_DONG_BANG;
                if (cham(xPhai + 4, y, rongPhai - 12, CAO_DONG_BANG - 6))
                {
                    moChiTietBang(ds[i]);
                    return true;
                }
            }
            return true;
        }

        /// <summary>
        /// Mở khung chi tiết một bang, và xin danh sách thành viên của nó.
        /// </summary>
        /// <remarks>
        /// Gói -50 (<c>clanMember</c>) là đường DUY NHẤT lấy thành viên của bang
        /// khác; kết quả tìm bang (-47) chỉ có thông tin chung. Xin ngay lúc mở
        /// chứ không đợi người chơi bấm sang thẻ "Thành viên": gói đi và về mất
        /// một nhịp, xin sớm thì lúc bấm sang là đã có sẵn.
        /// </remarks>
        private void moChiTietBang(Clan cl)
        {
            bangXem = cl;
            theBangXemChon = 0;
            cuon = 0;
            if (GameCanvas.panel != null)
            {
                GameCanvas.panel.member = null;
            }
            Service.gI().clanMember(cl.ID);
        }

        /// <summary>Chạm trong khung chi tiết một bang khác.</summary>
        private bool chamChiTietBang()
        {
            int yND = yThan + CAO_DAI_TD + KHE_KHUNG;
            int tx = theNhoTaiDiem(TEN_THE_XEM.Length, xPhai, rongPhai,
                    yND + 1);
            if (tx >= 0)
            {
                if (theBangXemChon != tx)
                {
                    cuon = 0;
                }
                theBangXemChon = tx;
                return true;
            }
            if (cham2(oNutQuayLai()))
            {
                bangXem = null;
                cuon = 0;
                return true;
            }
            if (cham2(oNutXinVao()))
            {
                if (coBangHoi())
                {
                    // Da co bang thi khong xin bang khac: may chu chan, va bam vao
                    // chi nhan lai mot loi.
                    return true;
                }
                // Ma 2 = ASK_FOR_JOIN_CLAN, va doi so thu ba la ID BANG.
                //
                // KHONG dung joinClan: ham do phia may chu nhan mot
                // "clanMessageId" — id cua loi xin vao trong chat bang — de CHU
                // BANG duyet hoac tu choi. Truyen id bang vao do thi may chu di
                // tim mot loi xin mang so ay, khong thay, va bo qua trong im
                // lang: bam vao khong co gi xay ra.
                Service.gI().clanMessage(2, null, bangXem.ID);
                bangXem = null;
                cuon = 0;
                return true;
            }
            return true;
        }

        /// <summary>
        /// Bấm một dòng chat: nếu là lời xin đậu thì cho đậu.
        /// </summary>
        /// <remarks>
        /// Chỉ dòng nào có <c>maxCap &gt; 0</c> mới là lời xin đậu. Dòng chat
        /// thường bấm vào không làm gì — gửi <c>clanDonate</c> cho một dòng chat
        /// thường thì máy chủ không tìm ra lời xin nào và lặng lẽ bỏ qua, người
        /// chơi tưởng đã cho mà chưa cho.
        /// </remarks>
        private bool chamDongChat()
        {
            int so = ClanMessage.vMessage.size();
            if (so == 0)
            {
                return false;
            }
            int yDau = yThan + CAO_DAI_TD + KHE_KHUNG + 4;
            int yHet = oNutChat(0)[1] - 3;
            int thay = (yHet - yDau) / CAO_DONG_CHAT;
            for (int i = cuon; i < so && i - cuon < thay; i++)
            {
                ClanMessage cm = (ClanMessage) ClanMessage.vMessage.elementAt(i);
                if (cm != null && cm.type == LOAI_XIN_DAU
                        && cm.playerId == Char.myCharz().charID)
                {
                    // Loi xin dau cua chinh minh: khong bam duoc, dung nhu khong
                    // co nut. Xem veMotDongChat.
                    continue;
                }
                if (cm == null || (cm.type != LOAI_XIN_DAU
                        && cm.type != LOAI_XIN_VAO))
                {
                    // Dong chat thuong: bam vao khong lam gi. Gui clanDonate cho
                    // mot dong chat thuong thi may chu khong tim ra loi xin nao va
                    // lang le bo qua — nguoi choi tuong da cho ma chua cho.
                    //
                    // Loc theo `type` chu khong theo `maxCap`: mot loi xin dau da
                    // du nguoi cho co maxCap == recieve, van la loi xin dau va van
                    // phai xu ly dung nhu the.
                    continue;
                }
                int y = yDau + (i - cuon) * CAO_DONG_CHAT;
                if (cham2(oNutTrongDongChat(y))
                        || cham(xPhai + 4, y, rongPhai - 12, CAO_DONG_CHAT - 3))
                {
                    if (cm.type == LOAI_XIN_VAO)
                    {
                        // Duyet loi xin vao. Ma 0 = ACCEPT_ASK_JOIN_CLAN, va doi
                        // so dau la id CUA LOI XIN, khong phai id bang.
                        Service.gI().joinClan(cm.id, (sbyte) 0);
                    }
                    else
                    {
                        Service.gI().clanDonate(cm.id);
                    }
                    return true;
                }
            }
            return false;
        }

        /// <summary>Chạm vào một vùng cho sẵn dạng {x, y, rộng, cao}.</summary>
        private static bool cham2(int[] o)
        {
            return cham(o[0], o[1], o[2], o[3]);
        }

        /// <summary>
        /// Ảnh biểu tượng bang.
        /// </summary>
        /// <remarks>
        /// Phải qua <c>ClanImage</c> chứ không vẽ thẳng <c>imgID</c>: <c>imgID</c>
        /// là số thứ tự trong bộ ảnh bang, không phải mã ảnh nhỏ. Kiểm
        /// <c>isExistClanImage</c> trước, vì bang mới lập có thể mang mã chưa có
        /// ảnh và <c>getClanImage</c> sẽ trả về rỗng.
        /// </remarks>
        /// <summary>
        /// Ảnh biểu tượng của một bang; chưa có thì xin máy chủ gửi.
        /// </summary>
        /// <remarks>
        /// Client KHÔNG có sẵn ảnh biểu tượng nào. <c>ClanImage.vClanImage</c> chỉ
        /// đầy lên khi có ai xin, và <c>addClanImage</c> chính là lời xin đó — nó
        /// gọi <c>Service.clanImage</c>. Bản trước chỉ vẽ khi ảnh đã có mà không
        /// bao giờ xin, nên ô biểu tượng trống mãi, trừ khi trước đó đã mở bảng cũ
        /// hoặc đã vào bước chọn cờ — hai chỗ có xin.
        ///
        /// Xin một lần cho mỗi mã ảnh: <c>isExistClanImage</c> thành true ngay lúc
        /// đăng ký, kể cả khi <c>idImage</c> còn rỗng, nên vòng vẽ sau không xin
        /// lại. Ảnh về sau một nhịp rồi tự hiện.
        /// </remarks>
        private void veAnhBang(mGraphics g, Clan cl, int xGiua, int yGiua)
        {
            if (!ClanImage.isExistClanImage(cl.imgID))
            {
                ClanImage moi = new ClanImage();
                moi.ID = cl.imgID;
                ClanImage.addClanImage(moi);
                return;
            }
            ClanImage ci = ClanImage.getClanImage((short) cl.imgID);
            if (ci == null || ci.idImage == null || ci.idImage.Length == 0)
            {
                return;
            }
            SmallImage.drawSmallImage(g, ci.idImage[0], xGiua, yGiua, 0,
                    mGraphics.VCENTER | mGraphics.HCENTER);
        }

        // ==================================================================
        //  The "De Tu" — ba the con
        // ==================================================================
        /// <summary>Hai thẻ con của "Đệ tử".</summary>
        /// <remarks>
        /// Kỹ năng không còn là một thẻ riêng: nó chuyển sang <b>cột phải</b>, nằm
        /// cạnh trang bị, giống hệt cách thẻ "Kỹ năng" của sư phụ để "Chỉ Số" và
        /// "Skill" cạnh nhau. Xem đồ đệ đang mặc và kỹ năng nó có cùng lúc thì
        /// mới so được, chứ bấm qua lại thì phải nhớ.
        /// </remarks>
        /// <summary>Hai thẻ con nằm ở CỘT PHẢI của "Đệ tử".</summary>
        /// <remarks>
        /// Cột trái luôn là trang bị của đệ, không đổi. Cột phải mới có thẻ để
        /// chọn giữa kỹ năng và trạng thái — cùng cách bố trí "Chỉ Số | Skill"
        /// của sư phụ, chỉ khác là bên phải chọn được.
        /// </remarks>
        private static readonly string[] TEN_THE_DE = {
            "Chỉ số", "Kỹ năng", "Trạng thái"
        };

        private const int DE_CHI_SO = 0;
        private const int DE_KY_NANG = 1;
        private const int DE_TRANG_THAI = 2;

        private int theDeChon = DE_CHI_SO;

        /// <summary>
        /// Việc đệ tử đang làm, đúng thứ tự mã mà máy chủ chờ.
        /// </summary>
        /// <remarks>
        /// <c>Service.petStatus(sbyte)</c> nhận đúng <b>chỉ số trong mảng này</b>,
        /// nên thêm hay bớt một mục là mọi mục phía sau gọi sang việc khác. Thứ tự
        /// lấy từ <c>Panel.strStatus</c> — chỗ duy nhất máy chủ và client đã thống
        /// nhất.
        ///
        /// "Hợp thể vĩnh viễn" chỉ có với <c>cgender == 1</c>; xem
        /// <see cref="soViecDe"/>.
        /// </remarks>
        private static readonly string[] VIEC_DE = {
            "Đi theo", "Bảo vệ", "Tấn công", "Về nhà",
            "Hợp thể", "Hợp thể vĩnh viễn"
        };

        /// <summary>Số việc bày ra được cho nhân vật này.</summary>
        private static int soViecDe()
        {
            return (Char.myCharz().cgender == 1) ? VIEC_DE.Length
                    : (VIEC_DE.Length - 1);
        }

        /// <summary>Thẻ "Đệ Tử".</summary>
        private void veDeTu(mGraphics g)
        {
            Char de = Char.myPetz();
            if (de == null || de.cName == null || de.cName.Length == 0)
            {
                // Chua co TEN nghia la goi -107 chua ve, khong chac la chua co de.
                // Noi "chua co de" ngay la noi sai voi nguoi dang co de.
                veKhungLom(g, xTrai, yThan, rongTrai, caoThan);
                veKhungLom(g, xPhai, yThan, rongPhai, caoThan);
                veChuGiua(g, xTrai, rongTrai, daXinDe
                        ? "Đang tải thông tin đệ tử…"
                        : "Bạn chưa có đệ tử.");
                return;
            }

            // Cot trai: trang bi cua de, luon hien.
            veKhungCoTieuDe(g, xTrai, yThan, rongTrai, caoThan, "Trang bị", 0,
                    MAU_DAI_CAM);
            veDeTrangBi(g, de, xTrai, rongTrai);

            // Cot phai: hai the con.
            veDaiTheNho(g, TEN_THE_DE, theDeChon, xPhai, rongPhai);
            veKhungLom(g, xPhai, yNoiDungPhu(), rongPhai, caoNoiDungPhu());

            if (theDeChon == DE_CHI_SO)
            {
                veDeChiSo(g, de, xPhai, rongPhai, yNoiDungPhu());
            }
            else if (theDeChon == DE_KY_NANG)
            {
                veDeKyNang(g, de, xPhai, rongPhai, yNoiDungPhu());
            }
            else
            {
                veDeTrangThai(g, de, xPhai, rongPhai);
            }
        }

        /// <summary>Đồ đệ tử đang mặc, và đệ đứng trên bệ.</summary>
        private void veDeTrangBi(mGraphics g, Char de, int x, int w)
        {
            int y0d = yThan + CAO_DAI_TD + KHE_KHUNG + 2;
            int caoD = yThan + caoThan - y0d;

            // De dung CHINH GIUA cot, giua hai cot o.
            //
            // Truoc day dat o mot phan nam be ngang vi khoi o nam ben phai; gio
            // o xep hai mep nhu cua su phu nen giua moi la dung cho.
            int xAnh = x + w / 2;
            int yChan = y0d + caoD / 2 + CAO_NHAN_VAT / 2;
            veBeDung(g, xAnh, yChan, 70);
            // Doi so cuoi la false: KHONG ve do deo lung.
            //
            // paintCharBody ve co bang khi truyen true, va no lay tu truong `bag`
            // cua chinh doi tuong Char. De tu khong thuoc bang nao, nhung `bag`
            // cua no la gia tri con sot lai nen preview hien de dang deo co bang
            // cua nguoi choi — sai hoan toan.
            de.paintCharBody(g, xAnh, yChan - 4, 1, khungDungCho(), false);

            Item[] mac = de.arrItemBody;
            if (mac == null || mac.Length == 0)
            {
                mFont.tahoma_7.drawString(g, "Đệ chưa mặc gì.",
                        x + w * 2 / 3, y0d + caoD / 2 - 5, mFont.CENTER);
                return;
            }
            for (int i = 0; i < mac.Length; i++)
            {
                int[] o = oODeTu(i);
                if (o == null)
                {
                    continue;
                }
                // Truyen ten o de o trong hien chu, giong cot trang bi cua su
                // phu. Truyen chuoi rong thi o trong khong noi len duoc no danh
                // cho mon gi.
                veMotO(g, mac[i], o[0], o[1], o[2],
                        i < TEN_O.Length ? TEN_O[i] : "");
            }
        }

        /// <summary>
        /// Vùng ô trang bị thứ <paramref name="i"/> của đệ: x, y, cỡ ô.
        /// </summary>
        /// <remarks>
        /// Đệ chỉ có bảy ô nên xếp thành một khối bên phải, không cần sơ đồ hai cột
        /// như nhân vật. Số cột tính từ số ô thật, chứ đóng cứng bốn cột thì đệ nào
        /// máy chủ cho thêm ô là hàng cuối lệch hẳn.
        /// </remarks>
        /// <summary>Năm ô mép trái của đệ.</summary>
        /// <remarks>
        /// Xếp như của sư phụ: năm ô sát mép trái, hai ô sát mép phải, đệ đứng
        /// giữa. Chỉ số trong hai mảng là <b>chỉ số ô của máy chủ</b>, giống
        /// <c>arrItemBody</c> của người chơi.
        ///
        /// Đệ chỉ có bảy ô (<c>QTY_MAX_ITEM_BODY_PET</c>), nên hai ô mép phải là
        /// cải trang và giáp luyện tập — hai thứ đệ mặc được mà không phải áo
        /// quần găng giày.
        /// </remarks>
        private static readonly int[] O_DE_TRAI = { 0, 1, 2, 3, 4 };

        /// <summary>Hai ô mép phải của đệ: cải trang và giáp luyện tập.</summary>
        private static readonly int[] O_DE_PHAI = { 5, 6 };

        /// <summary>Cỡ một ô của đệ, và vị trí ô thứ <paramref name="i"/>.</summary>
        private int oCoDeTu()
        {
            int theoDoc = (caoThan - CAO_DAI_TD - KHE_KHUNG - 8)
                    / O_DE_TRAI.Length;
            int theoNgang = (rongTrai - 8) / 3;
            int o = Math.min(theoDoc, theoNgang);
            if (o > O_TOI_DA)
            {
                o = O_TOI_DA;
            }
            if (o < O_TOI_THIEU)
            {
                o = O_TOI_THIEU;
            }
            return o;
        }

        /// <summary>Toạ độ ô mang chỉ số <paramref name="m"/>, hoặc null.</summary>
        private int[] oODeTu(int m)
        {
            int o = oCoDeTu();
            int yDinh = yThan + CAO_DAI_TD + KHE_KHUNG + 2;
            int yDau = yDinh
                    + (yThan + caoThan - yDinh - O_DE_TRAI.Length * o) / 2;
            for (int i = 0; i < O_DE_TRAI.Length; i++)
            {
                if (O_DE_TRAI[i] == m)
                {
                    return new int[] { xTrai + 3, yDau + i * o, o };
                }
            }
            for (int i = 0; i < O_DE_PHAI.Length; i++)
            {
                if (O_DE_PHAI[i] == m)
                {
                    return new int[] { xTrai + rongTrai - o - 3,
                        yDau + i * o, o };
                }
            }
            return null;
        }

        /// <summary>
        /// Chỉ số của đệ.
        /// </summary>
        /// <remarks>
        /// Chỉ những trường gói <c>-107</c> thật sự mang xuống. Đệ không có bảng
        /// chỉ số phụ như sư phụ (né, chính xác, hút máu) vì máy chủ không gửi —
        /// bịa thêm dòng rỗng ở đây chỉ làm người đọc tưởng đệ có mà đang bằng 0.
        /// </remarks>
        private void veDeChiSo(mGraphics g, Char de, int x, int w, int yDau)
        {
            int y = yDau + 5;
            y = veDongDe(g, x, w, y, "Tên", de.cName);
            y = veDongDe(g, x, w, y, "Cấp", de.currStrLevel);
            y = veDongDe(g, x, w, y, "Sức mạnh",
                    NinjaUtil.getMoneys(de.cPower));
            y = veDongDe(g, x, w, y, "Tiềm năng",
                    NinjaUtil.getMoneys(de.cTiemNang));
            y = veDongDe(g, x, w, y, "HP",
                    NinjaUtil.getMoneys((long)(de.cHP)) + " / " + NinjaUtil.getMoneys((long)(de.cHPFull)));
            y = veDongDe(g, x, w, y, "KI",
                    NinjaUtil.getMoneys(de.cMP) + " / " + NinjaUtil.getMoneys(de.cMPFull));
            y = veDongDe(g, x, w, y, "Sức đánh",
                    NinjaUtil.getMoneys(de.cDamFull));
            y = veDongDe(g, x, w, y, "Giáp", NinjaUtil.getMoneys(de.cDefull));
            y = veDongDe(g, x, w, y, "Chí mạng", de.cCriticalFull + "%");
            veDongDe(g, x, w, y, "Thể lực",
                    de.cStamina + " / " + de.cMaxStamina);
        }

        /// <summary>Một dòng chỉ số của đệ: nhãn trái, số phải.</summary>
        private int veDongDe(mGraphics g, int x, int w, int y,
                string nhan, string so)
        {
            mFont.tahoma_7.drawString(g, nhan, x + 8, y, mFont.LEFT);
            int con = w - 20 - mFont.tahoma_7.getWidth(nhan);
            mFont.tahoma_7b_dark.drawString(g,
                    catTheoRong(mFont.tahoma_7b_dark, so, con), x + w - 8, y,
                    mFont.RIGHT);
            return y + 13;
        }

        /// <summary>Kỹ năng của đệ: icon, tên, cấp.</summary>
        /// <remarks>
        /// Ô kỹ năng chưa mở về client với <c>template == null</c> và một câu
        /// <c>moreInfo</c> nói điều kiện mở. Bỏ qua những ô đó thì danh sách ngắn
        /// đi mà không nói vì sao — hiện thẳng câu đó ra.
        /// </remarks>
        private void veDeKyNang(mGraphics g, Char de, int x, int w, int yDau)
        {
            Skill[] ds = de.arrPetSkill;
            if (ds == null || ds.Length == 0)
            {
                mFont.tahoma_7.drawString(g, "Đệ chưa có kỹ năng nào.",
                        x + w / 2, yDau + 20, mFont.CENTER);
                return;
            }
            int caoDong = CAO_DONG_KN;
            int y = yDau + 3;
            for (int i = 0; i < ds.Length; i++)
            {
                if (y + caoDong > yNoiDungPhu() + caoNoiDungPhu())
                {
                    break;
                }
                Skill sk = ds[i];
                if (sk == null)
                {
                    continue;
                }
                // Cung kieu voi dong ky nang cua su phu: nen bo goc co vien, va
                // anh nam trong mot o rieng cung bo goc.
                veKhungBo(g, x + 4, y, w - 8, caoDong - 3,
                        MAU_O, 0.9f, MAU_VIEN, 0.45f, 1);
                int oAnh = caoDong - 9;
                veKhungBo(g, x + 8, y + 3, oAnh, oAnh, MAU_O_DO, 1f,
                        MAU_VIEN_O, 0.85f, 1);
                int xChu = x + 12 + oAnh;

                if (sk.template == null)
                {
                    mFont.tahoma_7.drawString(g,
                            catBot((sk.moreInfo == null) ? "Chưa mở"
                                    : sk.moreInfo, 34),
                            xChu, y + 10, mFont.LEFT);
                }
                else
                {
                    SmallImage.drawSmallImage(g, sk.template.iconId,
                            x + 8 + oAnh / 2, y + 3 + oAnh / 2, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                    mFont.tahoma_7b_blue.drawString(g,
                            catBot(sk.template.name, 26), xChu, y + 5,
                            mFont.LEFT);
                    mFont.tahoma_7b_dark.drawString(g,
                            "Cấp: " + sk.point + "/" + sk.template.maxPoint,
                            xChu, y + 18, mFont.LEFT);
                }
                y += caoDong;
            }
        }

        /// <summary>Chỉ số đệ và các việc sai đệ làm.</summary>
        /// <summary>
        /// Thẻ con "Trạng thái": chỉ các việc sai đệ làm.
        /// </summary>
        /// <remarks>
        /// Bản trước vẽ cả bảng chỉ số ở nửa trái rồi mới đến các nút. Từ khi
        /// "Chỉ số" thành một thẻ con riêng thì bảng đó bị vẽ hai lần, và vì cùng
        /// một cột nên nó đè lên nhau. Ở đây bỏ hẳn, để các nút dùng trọn bề
        /// ngang.
        /// </remarks>
        /// <summary>Thẻ con "Trạng thái" của đệ: các việc sai đệ làm.</summary>
        /// <remarks>
        /// Không tô sáng việc đang làm. Sáu dòng này là <b>lệnh để bấm</b>, không
        /// phải một danh sách để chọn một cái: bấm "Về nhà" rồi bấm "Đi theo" là
        /// hai lệnh riêng, chứ không phải đổi lựa chọn. Tô sáng một dòng làm nó
        /// đọc ra như thẻ đang mở, và trùng luôn với màu highlight của các dải thẻ
        /// con thật.
        /// </remarks>
        private void veDeTrangThai(mGraphics g, Char de, int x, int w)
        {
            for (int i = 0; i < soViecDe(); i++)
            {
                int[] o = oNutViecDe(i);
                veNut(g, o[0], o[1], o[2], o[3], VIEC_DE[i], false);
            }
        }

        /// <summary>Vùng nút việc thứ <paramref name="i"/> của đệ.</summary>
        /// <summary>Vùng nút việc thứ <paramref name="i"/> của đệ.</summary>
        /// <remarks>
        /// Tính theo CỘT PHẢI. Bản trước tính theo cả bề ngang bảng và nhét vào
        /// nửa phải của nó, nên từ khi thẻ đệ chia hai cột thì nút nằm lệch hẳn ra
        /// ngoài khung.
        /// </remarks>
        private int[] oNutViecDe(int i)
        {
            return new int[] {
                xPhai + 6,
                yNoiDungPhu() + 6 + i * (CAO_NUT + 4),
                rongPhai - 12, CAO_NUT };
        }

        // ==================================================================
        //  The "Chuc Nang" — bon the con, moi dong mot o danh dau
        // ==================================================================
        private static readonly string[] TEN_THE_CN = {
            "Chức năng", "Tự động", "Hiển thị", "Hệ thống"
        };

        private int theCnChon = 0;

        /// <summary>
        /// Các mục của từng thẻ con, ghi bằng <b>mã hành động của mod</b>.
        /// </summary>
        /// <remarks>
        /// <para>Mã lấy từ <c>ClientManager.MenuClient()</c> — cùng bộ mã mà menu
        /// mod cũ dùng, và cùng <c>perform</c> xử lý. Không chép lại phần bật/tắt
        /// sang đây: chép là hai bản logic rồi trôi khỏi nhau, sửa một bên là bên
        /// kia sai âm thầm.</para>
        ///
        /// <para>Mã 10 ("Nút Chuyển Tab") có trong menu cũ nhưng
        /// <c>ClientManager.perform</c> <b>không có nhánh nào xử lý</b> — bấm vào
        /// không làm gì. Cố tình bỏ ra khỏi đây; muốn có thì phải viết nhánh xử lý
        /// trước.</para>
        /// </remarks>
        private static readonly int[][] MA_CHUC_NANG = {
            new int[] { 2, 3, 12 },
            new int[] { 1, 7, 4, 9 },
            new int[] { 19, 6, 8 },
            new int[] { 100003, 100001, 100002, 16, 17 }
        };

        private static readonly string[][] TEN_MUC_CN = {
            new string[] { "Tự Đánh", "Tàn Sát", "Tàn Sát Người" },
            new string[] { "Tự Động Hồi Sinh", "Auto Nhặt", "Auto Up Đệ",
                "Auto Login" },
            new string[] { "Thông tin đệ tử", "D.s Nhân Vật", "Giảm Đồ Họa" },
            new string[] { "Sổ sưu tầm", "Đổi cờ", "Đăng xuất", "Cấu hình Voice", "Âm thanh" }
        };

        // Thẻ con thứ tư đổi từ "Màn khác" sang "Hệ thống".
        //
        // Ba mục cũ — Màn hình Boss / Sự kiện / Phúc lợi — trùng y nguyên với
        // popup menu tổng ở nút ba gạch. Hai cửa vào cùng một màn là hai chỗ
        // phải nhớ sửa khi đổi, mà không được gì thêm.
        //
        // "Cấu hình Voice" ở lại: popup menu tổng không có mục này, bỏ đi là
        // chỉ còn vào được qua menu bánh răng.
        //
        // "Bản đồ" (mã 11) bỏ khỏi thẻ "Chức năng" theo yêu cầu.
        // `ClientManager` vẫn còn nhánh xử lý mã 11 cho menu bánh răng.

        /// <summary>Mã từ 1001 trở lên không phải mã bật/tắt: không vẽ ô đánh dấu.</summary>
        private const int MA_MAN_PHU = 1001;

        /// <summary>Mã từ 100000 trở lên là mã hành động CỦA GAME, không phải của mod.</summary>
        /// <remarks>
        /// Hai bảng mã khác nhau và rất dễ gọi lẫn: mã mod đi qua
        /// <c>ClientManager.getInstance().perform</c>, mã game đi qua
        /// <c>GameCanvas.gI().perform</c>. Gọi lẫn thì hoặc không làm gì, hoặc
        /// làm sai việc — nên tách hẳn bằng ngưỡng này.
        /// </remarks>
        private const int MA_GAME = 100000;

        private void veChucNang(mGraphics g)
        {
            int x = xRong();
            int w = rongRong();
            veDaiTheNho(g, TEN_THE_CN, theCnChon, x, w);
            veKhungLom(g, x, yNoiDungPhu(), w, caoNoiDungPhu());

            int[] ma = MA_CHUC_NANG[theCnChon];
            string[] ten = TEN_MUC_CN[theCnChon];
            for (int i = 0; i < ma.Length && i < ten.Length; i++)
            {
                int[] o = oMucChucNang(i);
                bool laToggle = ma[i] != 16 && ma[i] < MA_MAN_PHU;
                g.setColor(MAU_O, 0.65f);
                g.fillRect(o[0], o[1], o[2], o[3], 4);
                if (laToggle)
                {
                    bool bat = ClientManager.trangThaiMod(ma[i]);
                    mFont mfO = bat ? mFont.tahoma_7b_green
                            : mFont.tahoma_7;
                    mfO.drawString(g, bat ? "[x]" : "[ ]", o[0] + 8,
                            o[1] + 5, mFont.LEFT);
                }
                else
                {
                    mFont.tahoma_7b_red.drawString(g, "»", o[0] + 8,
                            o[1] + 5, mFont.LEFT);
                }
                mFont.tahoma_7b_dark.drawString(g, ten[i], o[0] + 30,
                        o[1] + 5, mFont.LEFT);
            }
        }

        /// <summary>Vùng một dòng của thẻ "Chức Năng".</summary>
        private int[] oMucChucNang(int i)
        {
            int caoDong = 22;
            return new int[] {
                xRong() + 4,
                yNoiDungPhu() + 4 + i * caoDong,
                rongRong() - 8, caoDong - 2 };
        }

        /// <summary>Làm việc của dòng thứ <paramref name="i"/> ở thẻ con hiện tại.</summary>
        private void lamChucNang(int i)
        {
            int[] ma = MA_CHUC_NANG[theCnChon];
            if (i < 0 || i >= ma.Length)
            {
                return;
            }
            // Cai gi mo ra man/hop moi thi phai dong bang nay truoc, khong
            // thi thu vua mo nam duoi bang.
            if (ma[i] >= MA_MAN_PHU || ma[i] == 16)
            {
                dong();
            }
            if (ma[i] >= MA_GAME)
            {
                // Doi co (100001) va Dang xuat (100002): dung lai dung hai lenh
                // san co cua game — `GameScr.cmdDoiCo` va `cmdLogOut` — chu
                // khong viet lai than lenh. 100001 goi `Service.getFlag`,
                // 100002 goi `LoginScr.backToRegister`.
                GameCanvas.gI().perform(ma[i], null);
                return;
            }
            ClientManager.getInstance().perform(ma[i], null);
        }

        // ==================================================================
        //  Dai the con dung chung, va vung noi dung duoi no
        // ==================================================================

        /// <summary>Dải thẻ con chiếm cả bề ngang, dùng cho Đệ Tử và Chức Năng.</summary>
        private void veDaiTheNho(mGraphics g, string[] ten, int dangChon,
                int x, int w)
        {
            veDaiTheNho(g, ten, dangChon, x, w, yThan);
        }

        /// <summary>Bản nhận toạ độ y, cho dải thẻ không nằm ở đỉnh thân.</summary>
        /// <remarks>
        /// Khung chi tiết một bang khác có dải thẻ nằm giữa cột phải, dưới dải
        /// tiêu đề. Bản đóng cứng <c>yThan</c> sẽ vẽ dải ấy đè lên tiêu đề.
        /// </remarks>
        private void veDaiTheNho(mGraphics g, string[] ten, int dangChon,
                int x, int w, int yDai)
        {
            int wThe = w / ten.Length;
            for (int i = 0; i < ten.Length; i++)
            {
                bool sang = (i == dangChon);
                int xt = x + i * wThe;
                // Cung mau voi dai the tren cung: nau dam chu trang khi chon.
                veKhungBo(g, xt, yDai, wThe - 2, CAO_THE_CON,
                        sang ? MAU_THE_CON_CHON : MAU_TIEU_DE, 0.98f,
                        MAU_VIEN, sang ? 1f : 0.75f, 1);
                mFont mf = mFont.tahoma_7b_dark;
                mf.drawString(g, ten[i], xt + (wThe - 2) / 2, yDai + 4,
                        mFont.CENTER);
            }
        }


        private int yNoiDungPhu()
        {
            return yThan + CAO_THE_CON + 3;
        }

        private int caoNoiDungPhu()
        {
            return caoThan - CAO_THE_CON - 3;
        }

        /// <summary>Thẻ con nào của dải phụ đang bị chạm, hoặc -1.</summary>
        private int theNhoTaiDiem(int soThe, int x, int w)
        {
            return theNhoTaiDiem(soThe, x, w, yThan);
        }

        /// <summary>Bản nhận toạ độ y, đi cặp với bản vẽ cùng tham số.</summary>
        private int theNhoTaiDiem(int soThe, int x, int w, int yDai)
        {
            int wThe = w / soThe;
            for (int i = 0; i < soThe; i++)
            {
                if (cham(x + i * wThe, yDai, wThe - 2, CAO_THE_CON))
                {
                    return i;
                }
            }
            return -1;
        }

        // ==================================================================
        //  Ve chung
        // ==================================================================

        /// <summary>
        /// Vẽ một dòng chữ trong bề rộng cho sẵn; dài quá thì cho chạy phải sang
        /// trái.
        /// </summary>
        /// <remarks>
        /// <para>Vừa bề rộng thì vẽ tĩnh như thường — chữ ngắn mà vẫn trôi thì
        /// khó đọc và không có lý do gì.</para>
        ///
        /// <para>Dài quá thì chạy, và <b>cắt bằng <c>setClip</c></b> chứ không cắt
        /// chuỗi: cắt chuỗi rồi thêm "…" là mất hẳn phần đuôi, còn đây là để đọc
        /// được cả câu.</para>
        ///
        /// <para>Vòng chạy dài bằng bề rộng chữ cộng một quãng nghỉ, nên câu chạy
        /// hết rồi mới quay lại từ mép phải — không nối đuôi liền vào đầu, thứ
        /// đọc ra như một câu khác.</para>
        /// </remarks>
        private void veChuChay(mGraphics g, mFont mf, string chu, int x, int y,
                int rong)
        {
            if (chu == null || chu.Length == 0)
            {
                return;
            }
            int rongChu = mf.getWidth(chu);
            if (rongChu <= rong)
            {
                mf.drawString(g, chu, x, y, mFont.LEFT);
                return;
            }

            // Mot vong = be rong chu + quang nghi. Toc do mot diem moi hai khung
            // hinh: nhanh hon thi chu nhoe, cham hon thi cau dai doc mai khong het.
            int nghi = rong / 2;
            int vong = rongChu + nghi;
            int buoc = (GameCanvas.gameTick / 2) % vong;

            g.setClip(x, y - 2, rong, 14);
            mf.drawString(g, chu, x + rong - buoc, y, mFont.LEFT);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        }

        private void veChuGiua(mGraphics g, int x, int w, string chu)
        {
            mFont.tahoma_7.drawString(g, chu, x + w / 2,
                    yThan + caoThan / 2 - 5, mFont.CENTER);
        }


        /// <summary>
        /// Vẽ một ô: khung, ảnh vật phẩm, số lượng, cấp nâng.
        /// </summary>
        /// <param name="ten">Tên ô, chỉ hiện khi ô trống.</param>
        /// <summary>
        /// Một ô vật phẩm: nền nâu bo góc, viền nâu đậm, ảnh ở giữa.
        /// </summary>
        /// <remarks>
        /// Ô tô <b>nâu</b> chứ không kem như phần còn lại của bảng. Bản tham chiếu
        /// làm vậy có lý: ảnh vật phẩm phần lớn là màu sáng, đặt trên nền kem thì
        /// chìm; nền nâu làm mỗi ô nổi hẳn thành một khối riêng.
        /// </remarks>
        /// <summary>Ô vuông — gọi lại bản chữ nhật với hai chiều bằng nhau.</summary>
        private void veMotO(mGraphics g, Item it, int x, int y, int o, string ten)
        {
            veMotOCN(g, it, x, y, o, o, ten);
        }

        /// <summary>
        /// Một ô vật phẩm hình chữ nhật: nền nâu bo góc, viền nâu đậm, ảnh giữa.
        /// </summary>
        /// <remarks>
        /// Ô tô <b>nâu</b> chứ không kem như phần còn lại của bảng: ảnh vật phẩm
        /// phần lớn màu sáng, đặt trên nền kem thì chìm.
        /// </remarks>
        private void veMotOCN(mGraphics g, Item it, int x, int y, int w, int h,
                string ten)
        {
            bool dangXem = hienHop && monXem != null && it == monXem;
            veKhungBo(g, x + 1, y + 1, w - 2, h - 2,
                    dangXem ? MAU_VIEN_SANG : MAU_O_DO, 1f,
                    MAU_VIEN_O, dangXem ? 1f : 0.8f, 1);
            // Khong ve vach loe o dinh o.
            //
            // Y ban dau la cho o co do noi. Thuc te no thanh mot gach khac mau
            // nam trong o, va vi o nao cung co nen ca luoi day gach ngang.

            if (it == null || it.template == null)
            {
                if (ten != null && ten.Length > 0)
                {
                    veTenO(g, ten, x, y, w, h);
                }
                return;
            }
            SmallImage.drawSmallImage(g, it.template.iconID, x + w / 2,
                    y + h / 2, 0, mGraphics.VCENTER | mGraphics.HCENTER);
            if (it.quantity > 1)
            {
                // So luong: chu NAU DAM, khong miếng nen.
                //
                // Doi theo mau o: den khi o sang, trang tren chip nau khi o kem,
                // vang khi o nau/cam, va gio nau dam vi o da thanh kem. Chu vang
                // tren kem la mat han con so.
                mFont.tahoma_7b_dark.drawString(g, "" + it.quantity,
                        x + w - 3, y + h - 12, mFont.RIGHT);
            }
            int cap = capNang(it);
            if (cap > 0)
            {
                mFont.tahoma_7b_red.drawString(g, "+" + cap, x + 4, y + 2,
                        mFont.LEFT);
            }
        }

        /// <summary>
        /// Một dòng sao pha lê, vẽ thành hàng hình sao.
        /// </summary>
        /// <remarks>
        /// Máy chủ gửi xuống dạng chữ, ví dụ "8 Sao Pha Lê (đã ép)" hoặc
        /// "8 Sao Pha Lê (chưa ép)". Đọc số ở đầu để biết vẽ mấy ngôi, và đọc chữ
        /// trong ngoặc để biết tô đặc hay để rỗng — sao đã ép tô vàng, sao chưa ép
        /// chỉ có nét viền mờ.
        ///
        /// Đọc theo chữ vì đó là tất cả những gì gói tin mang: chỉ số sao về client
        /// dưới dạng một chuỗi đã dựng sẵn, không có trường số riêng.
        /// </remarks>
        /// <summary>
        /// MỘT hàng sao duy nhất: ngôi đã ép tô vàng, ngôi chưa ép để mờ.
        /// </summary>
        /// <remarks>
        /// Máy chủ gửi xuống <b>hai</b> dòng chữ riêng — "8 Sao Pha Lê (đã ép)" và
        /// "8 Sao Pha Lê (chưa ép)" — nên bản trước vẽ ra hai hàng sao chồng nhau,
        /// đọc không ra món có bao nhiêu ngôi. Ở đây gộp: <c>chuoiSao</c> mang
        /// theo cả hai con số dưới dạng "đãÉp/tổng", và vẽ đúng một hàng.
        /// </remarks>
        private void veDongSao(mGraphics g, string chu, int x, int y)
        {
            int gach = chu.IndexOf('/');
            if (gach <= 0)
            {
                mFont.tahoma_7.drawString(g, chu, x, y, mFont.LEFT);
                return;
            }
            int daEp = docSo(chu.Substring(0, gach));
            int tong = docSo(chu.Substring(gach + 1));
            if (tong <= 0)
            {
                return;
            }
            if (tong > TOI_DA_SAO)
            {
                tong = TOI_DA_SAO;
            }
            for (int k = 0; k < tong; k++)
            {
                veMotSao(g, x + k * 10, y + 1, k < daEp);
            }
        }

        /// <summary>Số nguyên ở đầu chuỗi, hoặc 0.</summary>
        private static int docSo(string chu)
        {
            int so = 0;
            int i = 0;
            while (i < chu.Length && chu[i] >= 48 && chu[i] <= 57)
            {
                so = so * 10 + (chu[i] - 48);
                i++;
            }
            return so;
        }

        /// <summary>
        /// Một ngôi sao 8 điểm, ghép từ các hình chữ nhật.
        /// </summary>
        /// <remarks>
        /// <c>mGraphics</c> chỉ có <c>fillRect</c> và <c>drawRect</c> — không có
        /// hàm vẽ đa giác, và bộ ảnh của game cũng không có sẵn ngôi sao rời để
        /// dùng. Nên ngôi sao đây ghép từ một vạch dọc, một vạch ngang và bốn ô
        /// nhỏ ở bốn góc chéo; ở cỡ 8 điểm thì mắt đọc ra là ngôi sao.
        /// </remarks>
        private void veMotSao(mGraphics g, int x, int y, bool daEp)
        {
            int mau = daEp ? MAU_VIEN_SANG_SAO : MAU_VIEN;
            float mo = daEp ? 1f : 0.35f;
            g.setColor(mau, mo);
            g.fillRect(x + 3, y, 2, 8);
            g.fillRect(x, y + 3, 8, 2);
            g.fillRect(x + 1, y + 1, 2, 2);
            g.fillRect(x + 5, y + 1, 2, 2);
            g.fillRect(x + 1, y + 5, 2, 2);
            g.fillRect(x + 5, y + 5, 2, 2);
        }

        /// <summary>Tên ô trống, ngắt hai dòng nếu dài hơn ô.</summary>
        /// <remarks>
        /// Vẽ một dòng thì "Giáp luyện tập" tràn khỏi khung, đè lên ô bên cạnh.
        /// </remarks>
        private void veTenO(mGraphics g, string ten, int x, int y,
                int w, int h)
        {
            int giua = x + w / 2;
            int cat = ten.LastIndexOf(' ');
            if (cat < 0 || mFont.tahoma_7.getWidth(ten) <= w - 4)
            {
                mFont.tahoma_7.drawString(g, ten, giua, y + h / 2 - 5,
                        mFont.CENTER);
                return;
            }
            mFont.tahoma_7.drawString(g, ten.Substring(0, cat), giua,
                    y + h / 2 - 11, mFont.CENTER);
            mFont.tahoma_7.drawString(g, ten.Substring(cat + 1), giua,
                    y + h / 2 + 1, mFont.CENTER);
        }

        /// <summary>
        /// Thanh cuộn dọc bên mép phải của một khung.
        /// </summary>
        /// <remarks>
        /// Người gọi phải truyền <c>xv</c> đã <b>thụt vào</b> khỏi nét viền. Vẽ
        /// đúng lên mép thì thanh cuộn che mất nét viền phải, và vì nó chỉ hiện
        /// khi danh sách dài hơn khung nên trông như riêng khung đó thiếu viền —
        /// đúng lỗi đã gặp ở danh sách bản đồ.
        /// </remarks>
        private void veVachCuon(mGraphics g, int xv, int yv, int caoVung,
                int soDong, int thay)
        {
            veVachCuonTai(g, xv, yv, caoVung, soDong, thay, cuon);
        }

        /// <summary>
        /// Vạch cuộn với vị trí truyền vào, không lấy biến <c>cuon</c> chung.
        /// </summary>
        /// <remarks>
        /// Thẻ "Nhiệm vụ" có <b>ba</b> vùng cuộn cùng lúc (nội dung, thưởng, danh
        /// sách bản đồ) nên không thể dùng một biến chung: vẽ tay cuộn theo
        /// <c>cuon</c> thì cả ba tay nhảy cùng nhau dù chỉ kéo một khung.
        /// </remarks>
        private void veVachCuonTai(mGraphics g, int xv, int yv, int caoVung,
                int soDong, int thay, int viTri)
        {
            g.setColor(MAU_O_MO, 0.9f);
            g.fillRect(xv, yv, 3, caoVung);
            int caoTay = Math.max(10, caoVung * thay / soDong);
            int yTay = yv + (caoVung - caoTay) * viTri
                    / Math.max(1, soDong - thay);
            g.setColor(MAU_VIEN, 0.95f);
            g.fillRect(xv, yTay, 3, caoTay, 2);
        }

        private void veNut(mGraphics g, int x, int y, int w, int h, string chu,
                bool sang)
        {
            veKhungBo(g, x, y, w, h, sang ? MAU_VIEN : MAU_O,
                    sang ? 0.95f : 0.92f, MAU_VIEN, 0.7f, 1);

            // Khong ve vet loe o dinh nut.
            //
            // Vet do la mot vach khac mau nam ngang trong nut, doc ra nhu mot
            // duong ke lac cho chu khong ra hieu ung noi khoi. Nen phang mot mau
            // nhin sach hon.

            // Nut sang co nen NAU DAM nen chu phai TRANG; nut thuong nen kem nen
            // chu nau dam. Dung mot mau cho ca hai la mot ben mat chu.
            mFont mf = sang ? mFont.tahoma_7b_white : mFont.tahoma_7b_dark;
            mf.drawString(g, chu, x + w / 2, y + h / 2 - 5, mFont.CENTER);
        }

        // ==================================================================
        //  Gan ky nang vao o phim tat
        // ==================================================================

        /// <summary>Kỹ năng đang xem trong thẻ "Kỹ năng", hoặc <c>null</c>.</summary>
        private SkillTemplate kyNangXem;

        /// <summary>Đang mở hộp chi tiết một kỹ năng.</summary>
        private bool hienHopKyNang;

        /// <summary>Đang mở bảng chọn ô 1..0 để gán kỹ năng.</summary>
        private bool hienChonO;

        private int[][] nutHopKyNang = new int[0][];
        private int[][] nutChonO = new int[0][];

        /// <summary>Số ô phím tắt — đúng bằng bảng của game.</summary>
        private const int SO_O_PHIM = 10;

        /// <summary>Cạnh một ô trong bảng chọn phím tắt.</summary>
        private const int O_PHIM = 34;

        /// <summary>
        /// Bảng phím tắt mà game <b>đang thật sự</b> dùng để bắn.
        /// </summary>
        /// <remarks>
        /// Game giữ hai bảng: <c>keySkill</c> cho lối chơi bàn phím và
        /// <c>onScreenSkill</c> cho cảm ứng, và phần vẽ thanh kỹ năng chọn bảng
        /// theo <c>GameCanvas.isTouch</c>. Gán vào đúng bảng đang dùng thì ô trên
        /// màn hình mới đổi — ghi vào bảng kia là gán xong mà nhìn không thấy gì
        /// đổi, một lỗi không để lại dấu vết nào.
        /// </remarks>
        private static Skill[] bangPhimTat()
        {
            return (!GameCanvas.isTouch) ? GameScr.keySkill : GameScr.onScreenSkill;
        }

        /// <summary>Nhãn ô thứ <paramref name="i"/>: 1..9 rồi 0, như bàn phím.</summary>
        private static string tenOPhim(int i)
        {
            return (i < 9) ? (i + 1) + "" : "0";
        }

        /// <summary>Hộp chi tiết một kỹ năng, kèm nút gán phím.</summary>
        private void veHopKyNang(mGraphics g)
        {
            if (!hienHopKyNang || kyNangXem == null)
            {
                nutHopKyNang = new int[0][];
                return;
            }
            int cap = capDaHoc(kyNangXem.id);
            bool daHoc = cap > 0;
            // Chua hoc thi khong cho gan: gan duoc mot ky nang chua hoc thi o
            // phim tat tro toi mot thu khong ban duoc, bam ra khong co gi xay ra.
            string[] ten = daHoc
                    ? new string[] { "Gán vào ô", "Đóng" }
                    : new string[] { "Đóng" };

            int wHop = Math.min(rong - 60, 200);
            int hHop = 40 + CAO_NUT + 12;
            int xHop = x0 + (rong - wHop) / 2;
            int yHop = y0 + (cao - hHop) / 2;

            g.setColor(MAU_DEN, 0.5f);
            g.fillRect(x0, y0, rong, cao, 12);
            veKhungBo(g, xHop, yHop, wHop, hHop, MAU_NEN_SANG, 1f,
                    MAU_VIEN, 0.95f, 2);

            SmallImage.drawSmallImage(g, kyNangXem.iconId, xHop + 20, yHop + 18,
                    0, mGraphics.VCENTER | mGraphics.HCENTER);
            mFont.tahoma_7b_red.drawString(g, catBot(kyNangXem.name, 22),
                    xHop + 38, yHop + 7, mFont.LEFT);
            mFont.tahoma_7b_dark.drawString(g,
                    daHoc ? ("Cấp: " + cap + "/" + kyNangXem.maxPoint) : "Chưa học",
                    xHop + 38, yHop + 21, mFont.LEFT);

            nutHopKyNang = new int[ten.Length][];
            int wNut = (wHop - 20 - (ten.Length - 1) * 6) / ten.Length;
            int yNut = yHop + hHop - CAO_NUT - 6;
            for (int i = 0; i < ten.Length; i++)
            {
                int xNut = xHop + 10 + i * (wNut + 6);
                veNut(g, xNut, yNut, wNut, CAO_NUT, ten[i], true);
                // Ma 0 = gan phim, ma 1 = dong. Khi chua hoc chi co mot nut va
                // no luon la "Dong".
                nutHopKyNang[i] = new int[] { xNut, yNut, wNut, CAO_NUT,
                        daHoc ? i : 1 };
            }
        }

        /// <summary>Bảng chọn ô phím tắt: 1..9 rồi 0, kèm ảnh kỹ năng đang nằm ở đó.</summary>
        /// <remarks>
        /// Vẽ luôn ảnh của kỹ năng đang chiếm mỗi ô, để người chơi thấy mình sắp
        /// ghi đè lên cái gì thay vì phải nhớ.
        /// </remarks>
        private void veChonO(mGraphics g)
        {
            if (!hienChonO)
            {
                nutChonO = new int[0][];
                return;
            }
            Skill[] bang = bangPhimTat();
            const int COT = 5;
            const int KHE = 4;
            int hang = (SO_O_PHIM + COT - 1) / COT;
            int wHop = COT * O_PHIM + (COT - 1) * KHE + 20;
            int hHop = 22 + hang * O_PHIM + (hang - 1) * KHE + 8 + CAO_NUT + 6;
            int xHop = x0 + (rong - wHop) / 2;
            int yHop = y0 + (cao - hHop) / 2;

            g.setColor(MAU_DEN, 0.5f);
            g.fillRect(x0, y0, rong, cao, 12);
            veKhungBo(g, xHop, yHop, wHop, hHop, MAU_NEN_SANG, 1f,
                    MAU_VIEN, 0.95f, 2);
            mFont.tahoma_7b_red.drawString(g, "Chọn ô phím tắt",
                    xHop + wHop / 2, yHop + 5, mFont.CENTER);

            nutChonO = new int[SO_O_PHIM + 1][];
            int yLuoi = yHop + 21;
            for (int i = 0; i < SO_O_PHIM; i++)
            {
                int xo = xHop + 10 + (i % COT) * (O_PHIM + KHE);
                int yo = yLuoi + (i / COT) * (O_PHIM + KHE);
                veKhungBo(g, xo, yo, O_PHIM, O_PHIM, MAU_O_DO, 1f,
                        MAU_VIEN_O, 0.85f, 1);
                Skill sk = (bang != null && i < bang.Length) ? bang[i] : null;
                if (sk != null && sk.template != null)
                {
                    SmallImage.drawSmallImage(g, sk.template.iconId,
                            xo + O_PHIM / 2, yo + O_PHIM / 2 - 2, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                }
                mFont.tahoma_7b_dark.drawString(g, tenOPhim(i),
                        xo + 3, yo + O_PHIM - 13, mFont.LEFT);
                nutChonO[i] = new int[] { xo, yo, O_PHIM, O_PHIM, i };
            }
            int yNut = yHop + hHop - CAO_NUT - 6;
            veNut(g, xHop + 10, yNut, wHop - 20, CAO_NUT, "Đóng", true);
            nutChonO[SO_O_PHIM] = new int[] { xHop + 10, yNut, wHop - 20,
                    CAO_NUT, -1 };
        }

        private bool chamHopKyNang()
        {
            for (int i = 0; i < nutHopKyNang.Length; i++)
            {
                int[] o = nutHopKyNang[i];
                if (o != null && cham(o[0], o[1], o[2], o[3]))
                {
                    hienHopKyNang = false;
                    if (o[4] == 0)
                    {
                        hienChonO = true;
                    }
                    return true;
                }
            }
            hienHopKyNang = false;
            GameCanvas.clearAllPointerEvent();
            return true;
        }

        private bool chamChonO()
        {
            for (int i = 0; i < nutChonO.Length; i++)
            {
                int[] o = nutChonO[i];
                if (o != null && cham(o[0], o[1], o[2], o[3]))
                {
                    if (o[4] >= 0)
                    {
                        ganKyNangVaoO(o[4]);
                    }
                    hienChonO = false;
                    return true;
                }
            }
            hienChonO = false;
            GameCanvas.clearAllPointerEvent();
            return true;
        }

        /// <summary>Gán kỹ năng đang xem vào ô phím tắt thứ <paramref name="o"/>.</summary>
        private void ganKyNangVaoO(int o)
        {
            if (kyNangXem == null)
            {
                return;
            }
            Skill kn = Char.myCharz().getSkill(kyNangXem);
            if (kn == null)
            {
                return;
            }
            Skill[] bang = bangPhimTat();
            if (bang == null || o < 0 || o >= bang.Length)
            {
                return;
            }
            // Go ky nang khoi o cu TRUOC khi dat vao o moi.
            //
            // Khong go thi mot ky nang nam o hai o. Bam o cu van ra dung ky nang
            // ay, nen nguoi choi tuong minh chua gan xong va gan lai them lan
            // nua — thanh ra ba o cung mot chieu.
            for (int i = 0; i < bang.Length; i++)
            {
                if (bang[i] == kn)
                {
                    bang[i] = null;
                }
            }
            bang[o] = kn;
            // Gui len may chu de lan dang nhap sau van con. Hai bang co hai lenh
            // luu rieng; goi nham lenh la luu bang khong doi.
            if (!GameCanvas.isTouch)
            {
                GameScr.gI().saveKeySkillToRMS();
            }
            else
            {
                GameScr.gI().saveonScreenSkillToRMS();
            }
        }

        private static string catBot(string s, int toiDa)
        {
            if (s == null)
            {
                return "";
            }
            return s.Length <= toiDa ? s : s.Substring(0, toiDa - 1) + "…";
        }

        /// <summary>Cấp nâng của món (chỉ số 72), hoặc 0.</summary>
        private static int capNang(Item it)
        {
            if (it == null || it.itemOption == null)
            {
                return 0;
            }
            for (int i = 0; i < it.itemOption.Length; i++)
            {
                ItemOption op = it.itemOption[i];
                if (op != null && op.optionTemplate != null
                        && op.optionTemplate.id == 72)
                {
                    return op.param;
                }
            }
            return 0;
        }

        // ==================================================================
        //  Hop chi tiet mot mon
        // ==================================================================

        /// <summary>Một dòng trong hộp: chữ, và vai của nó để biết tô màu gì.</summary>
        private class DongMoTa
        {
            public string chu;

            /// <summary>0 thường · 1 tên set · 2 mốc đã đạt · 3 mốc chưa đạt.</summary>
            public int vai;

            public DongMoTa(string chu, int vai)
            {
                this.chu = chu;
                this.vai = vai;
            }
        }

        private const int VAI_THUONG = 0;
        private const int VAI_TEN_SET = 1;
        private const int VAI_MOC_DAT = 2;
        private const int VAI_MOC_CHUA = 3;

        /// <summary>4 — dòng mô tả của mẫu vật phẩm.</summary>
        private const int VAI_MO_TA = 4;

        /// <summary>5 — dòng sao pha lê, vẽ thành hình sao chứ không phải chữ.</summary>
        private const int VAI_SAO = 5;

        /// <summary>Dòng chỉ số có phần cộng từ sao pha lê.</summary>
        private const int VAI_SAO_CONG = 6;

        /// <summary>Chữ nhận ra một dòng sao pha lê trong mô tả vật phẩm.</summary>
        private const string CHU_SAO = "Sao Pha Lê";

        /// <summary>Số sao pha lê nhiều nhất một món có được.</summary>
        /// <remarks>
        /// Tám, là trần của game này. Chỉ số 107 trên món đồ là trần riêng của
        /// nó, và về lý thuyết dữ liệu có thể để cao hơn — chặn ở đây để hàng sao
        /// không bao giờ dài hơn thực tế, và máy chủ cũng chặn cùng con số này
        /// trong <c>EpSaoTrangBi</c>.
        /// </remarks>
        private const int TOI_DA_SAO = 8;

        // Cac chi so ma DA PHA LE cong vao, lay tu Item.getOptionDaPhaLe phia may
        // chu. Mon co sao thi nhung dong nay duoc to nen de biet chung an theo so
        // sao.
        //
        // Vi sao phai lam theo cach nay: voi mon 8 sao tro xuong, may chu CONG GOP
        // phan thuong cua sao vao chinh chi so nen (addOptionParam), nen goi tin
        // khong con tach duoc dau la nen dau la sao — con so da tron. Doi chieu
        // theo danh sach chi so ma da pha le co the cong la cach duy nhat nhan ra
        // dong nao co phan cua sao.
        private static readonly int[] CHI_SO_SAO = {
            50, 77, 80, 81, 94, 95, 96, 97, 98, 99, 100, 101, 103, 108, 153, 160
        };

        /// <summary>Chỉ số này có phải loại mà đá pha lê cộng vào hay không.</summary>
        private static bool laChiSoSao(int idChiSo)
        {
            for (int i = 0; i < CHI_SO_SAO.Length; i++)
            {
                if (CHI_SO_SAO[i] == idChiSo)
                {
                    return true;
                }
            }
            return false;
        }

        /// <summary>Số ký tự nhiều nhất một dòng trong hộp chứa được.</summary>
        /// <remarks>
        /// Đếm theo ký tự chứ không đo bề rộng thật: hộp rộng cố định, và mô tả
        /// vật phẩm toàn chữ thường nên đếm ký tự là đủ sát.
        /// </remarks>
        private const int KY_TU_MOI_DONG = 40;

        /// <summary>Chẻ một đoạn dài thành nhiều dòng rồi thêm vào danh sách.</summary>
        private static void themDongNgat(
                System.Collections.Generic.List<DongMoTa> ds,
                string chu, int vai)
        {
            string[] tu = chu.Split(' ');
            string dong = "";
            for (int i = 0; i < tu.Length; i++)
            {
                string thu = (dong.Length == 0) ? tu[i] : (dong + " " + tu[i]);
                if (thu.Length > KY_TU_MOI_DONG && dong.Length > 0)
                {
                    ds.Add(new DongMoTa(dong, vai));
                    dong = tu[i];
                }
                else
                {
                    dong = thu;
                }
            }
            if (dong.Length > 0)
            {
                ds.Add(new DongMoTa(dong, vai));
            }
        }

        /// <summary>
        /// Hộp thông tin của món vừa bấm, kèm nút thao tác.
        /// </summary>
        /// <remarks>
        /// Màn này tự vẽ hộp và tự gửi lệnh chứ không gọi menu vật phẩm của bảng
        /// cũ. Menu đó đọc <c>selected</c> và <c>currentListLength</c> — hai biến
        /// tính theo dòng 24 điểm — nên với lưới ô vuông nó thoát ngay ở
        /// <c>selected == -1</c> và bấm vào ô không ra gì.
        ///
        /// Ô trống cũng mở hộp, nói "chưa mặc gì": bấm mà im hẳn thì không phân
        /// biệt được ô trống với màn hình bị treo.
        /// </remarks>
        private void veHopMon(mGraphics g)
        {
            if (!hienHop)
            {
                return;
            }
            DongMoTa[] dong = dongMoTa();
            string[] tenNut = tenCacNut();

            int wHop = Math.min(rong - 30, 270);
            int hHop = 30 + dong.Length * 12 + 8;
            if (tenNut.Length > 0)
            {
                hHop += CAO_NUT + 8;
            }
            int xHop = x0 + (rong - wHop) / 2;
            int yHop = y0 + (cao - hHop) / 2;
            if (yHop < y0 + 4)
            {
                yHop = y0 + 4;
            }

            g.setColor(MAU_DEN, 0.5f);
            g.fillRect(x0, y0, rong, cao, 12);
            veKhungBo(g, xHop, yHop, wHop, hHop, MAU_NEN_SANG, 1f,
                    MAU_VIEN, 0.95f, 2);

            int y = yHop + 6;
            if (monXem != null && monXem.template != null)
            {
                SmallImage.drawSmallImage(g, monXem.template.iconID, xHop + 18,
                        y + 9, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                int cap = capNang(monXem);
                string ten = monXem.template.name
                        + ((cap > 0) ? (" [+" + cap + "]") : "");
                mFont.tahoma_7b_red.drawString(g, catBot(ten, 30),
                        xHop + 36, y + 4, mFont.LEFT);
            }
            else
            {
                mFont.tahoma_7b_red.drawString(g,
                        (tenOXem.Length > 0 ? tenOXem : "Ô trống"),
                        xHop + wHop / 2, y + 4, mFont.CENTER);
            }
            y += 24;
            for (int i = 0; i < dong.Length; i++)
            {
                veDongMoTa(g, dong[i], xHop + 10, y, wHop - 20);
                y += 12;
            }

            // Nut ve SAU khi da biet y, va vung bam ghi lai de phan cham doc dung
            // cho — khong tinh lai theo cong thuc rieng.
            nutHop = new int[tenNut.Length][];
            if (tenNut.Length == 0)
            {
                return;
            }
            int wNut = (wHop - 20 - (tenNut.Length - 1) * 6) / tenNut.Length;
            int yNut = yHop + hHop - CAO_NUT - 6;
            for (int i = 0; i < tenNut.Length; i++)
            {
                int xNut = xHop + 10 + i * (wNut + 6);
                veNut(g, xNut, yNut, wNut, CAO_NUT, tenNut[i], true);
                nutHop[i] = new int[] { xNut, yNut, wNut, CAO_NUT, maNut(i) };
            }
        }

        private void veDongMoTa(mGraphics g, DongMoTa d, int x, int y, int w)
        {
            if (d.vai == VAI_SAO)
            {
                veDongSao(g, d.chu, x, y);
                return;
            }
            if (d.vai == VAI_TEN_SET)
            {
                // Ten set: to nen vang mo cho no tach khoi cac dong chi so, vi day
                // la dong quyet dinh y nghia cua ca cum ben duoi.
                g.setColor(MAU_VIEN, 0.28f);
                g.fillRect(x - 3, y - 2, w + 6, 13, 4);
                mFont.tahoma_7b_red.drawString(g, catBot(d.chu, 42), x, y,
                        mFont.LEFT);
                return;
            }
            if (d.vai == VAI_MOC_DAT)
            {
                // Moc DANG co hieu luc: to nen xanh, khac han mau ten set, de doc
                // mot cai la biet minh dang huong moc nao.
                g.setColor(MAU_XANH, 0.26f);
                g.fillRect(x - 3, y - 2, w + 6, 13, 4);
                mFont.tahoma_7b_green.drawString(g, catBot(d.chu, 42), x, y,
                        mFont.LEFT);
                return;
            }
            if (d.vai == VAI_SAO_CONG)
            {
                // Nen VANG SANG, cung ho mau voi ngoi sao da ep — nhin la noi
                // duoc dong nay voi hang sao o tren.
                g.setColor(MAU_VIEN_SANG_SAO, 0.30f);
                g.fillRect(x - 3, y - 2, w + 6, 13, 4);
                mFont.tahoma_7b_dark.drawString(g, catBot(d.chu, 42), x, y,
                        mFont.LEFT);
                return;
            }
            mFont mf = (d.vai == VAI_MOC_CHUA || d.vai == VAI_MO_TA)
                    ? mFont.tahoma_7 : mFont.tahoma_7b_dark;
            mf.drawString(g, catBot(d.chu, 42), x, y, mFont.LEFT);
        }

        /// <summary>
        /// Các dòng mô tả của món đang xem, mỗi dòng kèm vai để tô màu.
        /// </summary>
        /// <remarks>
        /// <b>Cách nhận ra set.</b> Dòng tên set là chỉ số có tên bắt đầu bằng
        /// "Set ". Các dòng mốc là chỉ số có tên bắt đầu bằng một con số rồi tới
        /// chữ "món" — ví dụ "3 món: + 30% sát thương Liên Hoàn".
        ///
        /// <b>Cách đếm số món cùng set.</b> So bằng <c>optionTemplate.id</c> của
        /// dòng tên set, không so bằng chuỗi: hai set khác nhau có thể trùng chữ
        /// đầu, mà id thì không bao giờ trùng.
        ///
        /// Mốc được tô là mốc CAO NHẤT mà số món đang mặc còn đạt tới. Tô mọi mốc
        /// nhỏ hơn cũng đúng về nghĩa nhưng đọc rối, không thấy ngay mình đang ở
        /// đâu.
        /// </remarks>
        private DongMoTa[] dongMoTa()
        {
            System.Collections.Generic.List<DongMoTa> ds =
                    new System.Collections.Generic.List<DongMoTa>();
            if (monXem == null || monXem.template == null)
            {
                ds.Add(new DongMoTa("Chưa mặc gì ở ô này.", VAI_THUONG));
                return ds.ToArray();
            }
            if (monXem.quantity > 1)
            {
                ds.Add(new DongMoTa("Số lượng: " + monXem.quantity, VAI_THUONG));
            }
            // Dong mo ta cua mau vat pham, ngat theo be rong hop.
            //
            // Day la cho duy nhat noi mon nay DUNG DE LAM GI. Thieu no thi hop
            // chi liet ke chi so, va nhung mon khong co chi so nao (nguyen lieu,
            // vat pham nhiem vu) hien ra trong rong.
            if (monXem.template.description != null
                    && monXem.template.description.Length > 0)
            {
                themDongNgat(ds, monXem.template.description, VAI_MO_TA);
            }
            if (monXem.template.strRequire > 0)
            {
                ds.Add(new DongMoTa("Sức mạnh yêu cầu: "
                        + NinjaUtil.getMoneys(monXem.template.strRequire),
                        VAI_THUONG));
            }
            int soMon = demMonCungSet(idOptionSet());
            int soSaoDaEp = 0;
            int soLoSao = 0;
            if (monXem.itemOption != null)
            {
                for (int i = 0; i < monXem.itemOption.Length; i++)
                {
                    ItemOption op = monXem.itemOption[i];
                    if (op == null || op.optionTemplate == null)
                    {
                        continue;
                    }
                    // Bo chi so 72: no la CAP NANG, da hien ngay sau ten dang
                    // "[+7]". De lai thi hop co hai dong noi cung mot viec.
                    if (op.optionTemplate.id == 72)
                    {
                        continue;
                    }
                    string s = op.getOptionString();
                    if (s == null || s.Length == 0)
                    {
                        continue;
                    }
                    // Hai dong sao gop lai, khong them vao danh sach ngay.
                    if (op.optionTemplate.name != null
                            && op.optionTemplate.name.Contains(CHU_SAO))
                    {
                        // Chi so 102 la SO SAO DA EP, 107 la TONG SO LO sao.
                        //
                        // Ten hai chi so doc ra nhu mot cap "da ep / chua ep" nen
                        // ban truoc cong ca hai lai lam tong. Sai: may chu chan ep
                        // bang dieu kien `star >= starEmpty` (EpSaoTrangBi), tuc
                        // 107 chinh la TRAN, khong phai phan con lai. Mot mon
                        // 8/8 sao vi the ve ra 16 ngoi voi 8 ngoi sang.
                        if (s.Contains("đã ép"))
                        {
                            soSaoDaEp = docSo(s);
                        }
                        else
                        {
                            soLoSao = docSo(s);
                        }
                        continue;
                    }
                    int vai = vaiCuaDong(op.optionTemplate.name, soMon);
                    // Chi danh vai "duoc sao cong" cho dong chi so THUONG.
                    //
                    // Dong ten set va dong moc set cung co the mang id nam trong
                    // danh sach tren, ma chung khong phai phan cua sao — ghi de vai
                    // cua chung la mat luon phan to moc dang huong.
                    if (vai == VAI_THUONG && laChiSoSao(op.optionTemplate.id))
                    {
                        vai = VAI_SAO_CONG;
                    }
                    ds.Add(new DongMoTa(s, vai));
                }
            }
            // MOT dong sao duy nhat, dat len dau cac dong chi so.
            //
            // Tong so ngoi lay theo TRAN (chi so 107). Mon nao chi co 102 ma
            // khong co 107 thi lay chinh so da ep lam tran, keo hien ra mot hang
            // rong trong khi mon dang co sao.
            int tongSao = (soLoSao > soSaoDaEp) ? soLoSao : soSaoDaEp;
            if (tongSao > TOI_DA_SAO)
            {
                tongSao = TOI_DA_SAO;
            }
            if (tongSao > 0)
            {
                ds.Insert(0, new DongMoTa(soSaoDaEp + "/" + tongSao, VAI_SAO));
            }
            // Mon khong co sao nao thi khong dong nao duoc sao cong: tra vai ve
            // thuong, keo to nen mot dong chi so von co san cua mon.
            if (soSaoDaEp <= 0)
            {
                for (int i = 0; i < ds.Count; i++)
                {
                    if (ds[i].vai == VAI_SAO_CONG)
                    {
                        ds[i] = new DongMoTa(ds[i].chu, VAI_THUONG);
                    }
                }
            }
            if (ds.Count == 0)
            {
                ds.Add(new DongMoTa("Không có chỉ số nào.", VAI_THUONG));
            }
            return ds.ToArray();
        }

        /// <summary>Vai của một dòng dựa trên tên chỉ số gốc.</summary>
        private int vaiCuaDong(string ten, int soMon)
        {
            if (ten == null)
            {
                return VAI_THUONG;
            }
            if (ten.StartsWith("Set "))
            {
                return VAI_TEN_SET;
            }
            int moc = mocCuaDong(ten);
            if (moc <= 0)
            {
                return VAI_THUONG;
            }
            return (moc == mocDangHuong(soMon)) ? VAI_MOC_DAT : VAI_MOC_CHUA;
        }

        /// <summary>Con số đứng đầu dòng mốc, ví dụ "3 món: …" cho 3. Không phải mốc thì 0.</summary>
        /// <remarks>
        /// Phải có chữ "món" ngay sau con số. Thiếu điều kiện đó thì
        /// "8 Sao Pha Lê" cũng bị nhận là một mốc set và bị tô nhầm.
        /// </remarks>
        private static int mocCuaDong(string ten)
        {
            int i = 0;
            int so = 0;
            while (i < ten.Length && ten[i] >= 48 && ten[i] <= 57)
            {
                so = so * 10 + (ten[i] - 48);
                i++;
            }
            if (so == 0 || i >= ten.Length)
            {
                return 0;
            }
            return ten.Substring(i).TrimStart().StartsWith("món") ? so : 0;
        }

        /// <summary>Mốc cao nhất mà <paramref name="soMon"/> món còn đạt tới.</summary>
        private int mocDangHuong(int soMon)
        {
            int cao = 0;
            if (monXem == null || monXem.itemOption == null)
            {
                return 0;
            }
            for (int i = 0; i < monXem.itemOption.Length; i++)
            {
                ItemOption op = monXem.itemOption[i];
                if (op == null || op.optionTemplate == null)
                {
                    continue;
                }
                int moc = mocCuaDong(op.optionTemplate.name);
                if (moc > 0 && moc <= soMon && moc > cao)
                {
                    cao = moc;
                }
            }
            return cao;
        }

        /// <summary>Mã chỉ số của dòng tên set trên món đang xem, hoặc -1.</summary>
        private int idOptionSet()
        {
            if (monXem == null || monXem.itemOption == null)
            {
                return -1;
            }
            for (int i = 0; i < monXem.itemOption.Length; i++)
            {
                ItemOption op = monXem.itemOption[i];
                if (op != null && op.optionTemplate != null
                        && op.optionTemplate.name != null
                        && op.optionTemplate.name.StartsWith("Set "))
                {
                    return op.optionTemplate.id;
                }
            }
            return -1;
        }

        /// <summary>Số món ĐANG MẶC có cùng mã chỉ số set này.</summary>
        /// <remarks>
        /// Đếm trên bộ đồ của <b>chủ món đang xem</b>: đang xem đồ của đệ thì
        /// đếm đồ đệ mặc, xem đồ mình thì đếm đồ mình.
        ///
        /// Trước đây luôn đếm <c>Char.myCharz()</c>. Đồ của đệ mang mã chỉ số
        /// set mà bộ đồ của người lại không có món nào mang mã đó, nên số món
        /// luôn ra 0, <c>mocDangHuong</c> trả về 0 và không mốc nào khớp — thẻ
        /// Đệ tử vì thế mất hẳn vạch tô mốc, trong khi thẻ Nhân vật vẫn có.
        /// </remarks>
        private int demMonCungSet(int idSet)
        {
            if (idSet < 0)
            {
                return 0;
            }
            Char chuMon = (oMacDeXem >= 0) ? Char.myPetz() : Char.myCharz();
            if (chuMon == null)
            {
                return 0;
            }
            Item[] mac = chuMon.arrItemBody;
            if (mac == null)
            {
                return 0;
            }
            int dem = 0;
            for (int i = 0; i < mac.Length; i++)
            {
                if (mac[i] == null || mac[i].template == null
                        || mac[i].itemOption == null)
                {
                    continue;
                }
                for (int j = 0; j < mac[i].itemOption.Length; j++)
                {
                    ItemOption op = mac[i].itemOption[j];
                    if (op != null && op.optionTemplate != null
                            && op.optionTemplate.id == idSet)
                    {
                        dem++;
                        break;
                    }
                }
            }
            return dem;
        }

        // ==================================================================
        //  Nut thao tac trong hop
        // ==================================================================

        /// <summary>
        /// Tên các nút thao tác cho món đang xem.
        /// </summary>
        /// <remarks>
        /// Chỉ bày nút cho việc <b>làm được thật</b>. Món trong túi không mặc được
        /// thì không có nút "Mặc"; ô trống thì không có nút nào. Bày nút cho việc
        /// máy chủ sẽ từ chối thì bấm vào chỉ nhận một câu báo lỗi, tệ hơn là không
        /// có nút.
        /// </remarks>
        /// <summary>Đệ tử có mặc được món này hay không.</summary>
        /// <remarks>
        /// Đệ chỉ có <b>bảy</b> ô: năm món bên trái (áo, quần, găng, giày, rađa)
        /// cộng cải trang và giáp luyện tập — tức loại 0..6, đúng như
        /// <c>O_DE_TRAI</c> và <c>O_DE_PHAI</c> khai. Ván bay, đeo lưng, sách,
        /// chân mệnh, pet thì đệ không có ô nào để đặt.
        ///
        /// Loại 32 tính là ô 6 (giáp luyện tập), cùng quy ước với
        /// <c>InventoryService</c> phía máy chủ.
        ///
        /// Bày nút "Cho đệ" cho những món ấy thì bấm vào chỉ nhận một câu báo lỗi
        /// — tệ hơn là không có nút.
        /// </remarks>
        private static bool deMacDuoc(Item it)
        {
            if (it == null || it.template == null)
            {
                return false;
            }
            int loai = (it.template.type == 32) ? 6 : it.template.type;
            return loai >= 0 && loai <= 6;
        }

        private string[] tenCacNut()
        {
            if (monXem == null || monXem.template == null)
            {
                return new string[0];
            }
            if (oMacXem >= 0 || oMacDeXem >= 0)
            {
                return new string[] { "Lấy ra" };
            }
            if (oTuiXem < 0)
            {
                return new string[0];
            }
            if (monXem.isTypeBody())
            {
                if (Char.myCharz().havePet && deMacDuoc(monXem))
                {
                    return new string[] { "Mặc", "Cho đệ", "Vứt" };
                }
                return new string[] { "Mặc", "Vứt" };
            }
            // Mon gop duoc thi them "Dung nhieu": hoi so luong roi dung lan luot.
            if (monXem.quantity > 1)
            {
                return new string[] { "Dùng", "Dùng nhiều", "Vứt" };
            }
            return new string[] { "Dùng", "Vứt" };
        }

        /// <summary>Mã việc của nút thứ <paramref name="i"/>, khớp thứ tự <see cref="tenCacNut"/>.</summary>
        private int maNut(int i)
        {
            if (oMacXem >= 0)
            {
                return VIEC_LAY_RA;
            }
            if (oMacDeXem >= 0)
            {
                return VIEC_LAY_RA_DE;
            }
            if (monXem != null && monXem.template != null && monXem.isTypeBody())
            {
                if (i == 0)
                {
                    return VIEC_MAC;
                }
                if (Char.myCharz().havePet && deMacDuoc(monXem) && i == 1)
                {
                    return VIEC_CHO_PET;
                }
                return VIEC_VUT;
            }
            if (i == 0)
            {
                return VIEC_DUNG;
            }
            if (monXem != null && monXem.quantity > 1 && i == 1)
            {
                return VIEC_DUNG_NHIEU;
            }
            return VIEC_VUT;
        }

        /// <summary>
        /// Gửi lệnh của nút vừa bấm.
        /// </summary>
        /// <remarks>
        /// Gọi thẳng <c>Service</c> chứ không đi qua <c>Panel.perform</c>: mấy mã
        /// việc trong đó đọc <c>selected</c>, <c>newSelected</c> và
        /// <c>currItem.indexUI</c> của bảng cũ, mà màn này không có mấy biến ấy.
        /// Đi tắt qua đó là phải dựng lại đúng trạng thái của bảng cũ — dễ sai và
        /// sai thì <b>gửi lệnh lên sai món</b>.
        ///
        /// Ba mã 4/5/6 là giao thức của gói -40, trùng với
        /// <c>BAG_BODY</c>/<c>BODY_BAG</c>/<c>BAG_PET</c> bên <c>Panel</c>.
        /// </remarks>
        /// <summary>
        /// Phím Enter khi đang mở hộp chi tiết món = bấm nút đầu tiên.
        /// </summary>
        /// <remarks>
        /// <para>Với trang bị trong hành trang nút đầu là "Mặc", nên Enter là mặc
        /// vào người. Với món đang mặc thì nút đầu là "Lấy ra", với món dùng được
        /// thì là "Dùng".</para>
        ///
        /// <para>Lấy <b>nút đầu tiên</b> chứ không gõ cứng việc "Mặc": như vậy
        /// Enter luôn làm đúng việc chính của món đang xem, người chơi không phải
        /// nhớ món nào thì Enter mới có tác dụng.</para>
        ///
        /// <para>Đọc thẳng <c>UnityEngine.Input</c> vì bảng phím ảo của engine
        /// đổi phím thành mã phím game rồi đẩy vào màn hình đang mở, mà bảng này
        /// không nằm trong đường đó.</para>
        ///
        /// <para>Không nhận phím khi hộp nhập số đang mở: lúc ấy Enter là để xác
        /// nhận con số vừa gõ.</para>
        /// </remarks>
        public void nhanPhim()
        {
            if (!dangMo || !hienHop || hienHopSo)
            {
                return;
            }
            if (!UnityEngine.Input.GetKeyDown(UnityEngine.KeyCode.Return)
                    && !UnityEngine.Input.GetKeyDown(UnityEngine.KeyCode.KeypadEnter))
            {
                return;
            }
            if (nutHop == null || nutHop.Length == 0 || nutHop[0] == null)
            {
                return;
            }
            lamViec(nutHop[0][4]);
            // Dong HOP chu khong dong ca bang — giong het luc bam nut bang chuot.
            hienHop = false;
        }

        private void lamViec(int viec)
        {
            if (monXem == null || monXem.template == null)
            {
                return;
            }
            switch (viec)
            {
                case VIEC_LAY_RA:
                    if (oMacXem >= 0)
                    {
                        Service.gI().getItem((sbyte) NGUOI_SANG_TUI,
                                (sbyte) oMacXem);
                    }
                    break;
                case VIEC_MAC:
                    Service.gI().getItem((sbyte) TUI_SANG_NGUOI,
                            (sbyte) oTuiXem);
                    break;
                case VIEC_CHO_PET:
                    Service.gI().getItem((sbyte) TUI_SANG_DE,
                            (sbyte) oTuiXem);
                    break;
                case VIEC_LAY_RA_DE:
                    if (oMacDeXem >= 0)
                    {
                        Service.gI().getItem((sbyte) DE_SANG_TUI,
                                (sbyte) oMacDeXem);
                    }
                    break;
                case VIEC_DUNG:
                    // Chi so truyen di la CHI SO TRONG arrItemBag, khong phai
                    // monXem.indexUI.
                    //
                    // indexUI chi duoc dat lai moi khi goi hanh trang ve; dung do
                    // vat pham xong may chu don lai tui thi no da cu, va lenh sau
                    // tro vao o khac. Chi so o tui thi da biet chac tu luc mo hop.
                    //
                    // Doi so: (0 = dung, noi = 1 la tui, chi so, -1).
                    Service.gI().useItem((sbyte) 0, (sbyte) 1,
                            (sbyte) oTuiXem, (short) -1);
                    break;
                case VIEC_VUT:
                    // So 1 o cho dau la "bo ra dat", khong phai "su dung": cung
                    // goi useItem nhung viec khac han. Xem Panel, ma 2003.
                    Service.gI().useItem((sbyte) 1, (sbyte) 1,
                            (sbyte) oTuiXem, (short) -1);
                    break;
                case VIEC_DUNG_NHIEU:
                    // Mo hop so luong CUA MAN NAY, khong goi luong cu.
                    //
                    // Luong "Dung nhanh" trong God.Items goi
                    // GameCanvas.panel.hide() roi bat hop nhap cua game — man nay
                    // khong phai GameCanvas.panel nen bang van mo va che kin hop
                    // do. Truoc phai dong ca bang de thay hop, tuc bam mot cai la
                    // mat sach man dang xem.
                    moHopSo();
                    break;
                default:
                    break;
            }
            hienHop = false;
        }

        // ==================================================================
        //  Toa do o — dung chung cho phan ve va phan bat cham
        // ==================================================================

        /// <summary>
        /// Toạ độ góc trên-trái của ô trang bị mang chỉ số <paramref name="m"/>,
        /// hoặc <c>null</c> nếu sơ đồ không xếp chỉ số đó.
        /// </summary>
        private int[] viTriO(int m)
        {
            int o = oTrangBi;
            int buoc = buocOTrangBi();
            int xL = xTrai + 2;
            int xR = xTrai + rongTrai - o - 2;

            for (int i = 0; i < O_TRAI.Length; i++)
            {
                if (O_TRAI[i] == m)
                {
                    return new int[] { xL, yLuoiTB + i * buoc };
                }
            }
            for (int i = 0; i < O_PHAI.Length; i++)
            {
                if (O_PHAI[i] == m)
                {
                    return new int[] { xR, yLuoiTB + i * buoc };
                }
            }
            for (int i = 0; i < O_DUOI.Length; i++)
            {
                if (O_DUOI[i] == m)
                {
                    // Nam TREN HANG CUOI, kep vao giua hai cot: Sach sat ngay ben
                    // phai o cuoi cot trai, Chan menh sat ngay ben trai o cuoi cot
                    // phai. Dung nhu ban tham chieu.
                    int yD = yLuoiTB + (SO_HANG_COT - 1) * buoc;
                    int x = (i == 0) ? (xL + buoc) : (xR - buoc);
                    return new int[] { x, yD };
                }
            }
            // May chu them o ma so do chua biet den. Bo qua chu khong doan cho
            // dat: dat bua thi no de len mot o that, bam vao ra sai mon.
            return null;
        }

        /// <summary>Chỉ số ô trang bị ở điểm này, hoặc -1.</summary>
        private int oTrangBiTaiDiem(int px, int py)
        {
            Item[] mac = Char.myCharz().arrItemBody;
            if (mac == null)
            {
                return -1;
            }
            int o = oTrangBi;
            for (int m = 0; m < mac.Length; m++)
            {
                int[] xy = viTriO(m);
                if (xy == null)
                {
                    continue;
                }
                if (px >= xy[0] && px < xy[0] + o
                        && py >= xy[1] && py < xy[1] + o)
                {
                    return m;
                }
            }
            return -1;
        }

        private int soHangTui()
        {
            Item[] tui = Char.myCharz().arrItemBag;
            int len = (tui == null) ? 0 : tui.Length;
            return (len + TUI_SO_COT - 1) / TUI_SO_COT;
        }

        /// <summary>Chỉ số ô túi ở điểm này, hoặc -1.</summary>
        private int oTuiTaiDiem(int px, int py)
        {
            Item[] tui = Char.myCharz().arrItemBag;
            if (tui == null || py < yNoiDung || py >= yNoiDung + caoNoiDung)
            {
                return -1;
            }
            int cot = (px - xPhai - LE_LUOI) / oTuiNgang;
            int hang = (py - yNoiDung - LE_LUOI) / oTuiDoc + cuon;
            if (cot < 0 || cot >= TUI_SO_COT || hang < 0)
            {
                return -1;
            }
            int i = hang * TUI_SO_COT + cot;
            return (i < tui.Length) ? i : -1;
        }

        // ==================================================================
        //  Cham
        // ==================================================================

        /// <summary>Xử lý chạm. Trả <c>true</c> nếu đã nuốt sự kiện.</summary>
        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            if (dangGoChu())
            {
                // Dang go chu thi hop nhap doc het thao tac. Ghi lai de khung
                // hinh dau tien sau khi no dong con biet ma nuot cu nha ngon.
                vuaGoChu = true;
                return false;
            }
            if (vuaGoChu)
            {
                // Nuot cu nha ngon cua nut OK / Dong.
                //
                // Nhanh xu ly hai nut do trong GameScr (quanh dong 2735) KHONG dat
                // lai GameCanvas.isPointerJustRelease — khac han nhanh cung viec
                // trong mScreen.updateKey, cho ay co dat. Nen sau khi hop nhap
                // dong, cu nha ngon van con nguyen, va bang bat duoc no ngay khung
                // hinh sau: toa do nut OK nam trung o mo hop nhap, thanh ra bam OK
                // la hop nhap mo lai, mo mai khong dut.
                vuaGoChu = false;
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            tinhBoCuc();
            cuonLuoi();
            if (theChon == THE_NHIEM_VU)
            {
                // Chay CANH cuonLuoi chu khong thay no: cuonLuoi lo danh sach ban
                // do o cot phai, ham nay lo hai khung o cot trai. Hai ham khong
                // tranh nhau vi trongVungCuon cua the nay chi nhan cot phai.
                cuonNhiemVu();
            }
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }
            if (daKeoXa)
            {
                // Vua keo de cuon, khong phai bam chon. Khong chan cho nay thi
                // moi lan cuon xong lai bat ra hop thong tin cua o duoi ngon.
                daKeoXa = false;
                return true;
            }
            if (hienChonO)
            {
                return chamChonO();
            }
            if (hienHopKyNang)
            {
                return chamHopKyNang();
            }
            if (chiSoDangNang >= 0)
            {
                return chamHopNang();
            }
            if (hienHopSo)
            {
                return chamHopSo();
            }
            if (hienHop)
            {
                // Bam nut thi lam viec; bam ra ngoai nut thi dong hop.
                for (int i = 0; i < nutHop.Length; i++)
                {
                    int[] o = nutHop[i];
                    if (o != null && cham(o[0], o[1], o[2], o[3]))
                    {
                        lamViec(o[4]);
                        // Dong HOP sau khi lam xong, khong dong ca bang.
                        //
                        // Mon vua dung co the da het hoac doi so luong, nen giu
                        // hop mo la no hien so lieu cu. Dong bang thi nguoi choi
                        // phai mo lai tu dau chi de dung mon thu hai.
                        hienHop = false;
                        return true;
                    }
                }
                hienHop = false;
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            int[] oX = oNutX();
            if (cham(oX[0], oX[1], oX[2], oX[3]))
            {
                dong();
                return true;
            }
            int wThe = rongMotThe();
            for (int i = 0; i < TEN_THE.Length; i++)
            {
                if (cham(x0 + LE + i * wThe, yThe, wThe - 2, CAO_THE))
                {
                    if (theChon != i)
                    {
                        // Doi the thi ve dau: hang thu muoi cua the nay khong co
                        // nghia gi voi the kia.
                        cuon = 0;
                        vaoThe(i);
                    }
                    theChon = i;
                    return true;
                }
            }
            if (theChon == THE_BAN_THAN)
            {
                return chamBanThan();
            }
            if (theChon == THE_CHUC_NANG)
            {
                int t = theNhoTaiDiem(TEN_THE_CN.Length, xRong(), rongRong());
                if (t >= 0)
                {
                    theCnChon = t;
                    return true;
                }
                for (int i = 0; i < MA_CHUC_NANG[theCnChon].Length; i++)
                {
                    int[] o = oMucChucNang(i);
                    if (cham(o[0], o[1], o[2], o[3]))
                    {
                        lamChucNang(i);
                        return true;
                    }
                }
            }
            if (theChon == THE_DE_TU)
            {
                return chamDeTu();
            }
            if (theChon == THE_KY_NANG)
            {
                return chamKyNang();
            }
            if (theChon == THE_NHIEM_VU)
            {
                return chamBanDo();
            }
            if (theChon == THE_BANG_HOI)
            {
                return chamBangHoi();
            }
            // Bam vao cho trong trong bang: nuot su kien chu khong tra ve false.
            // Tra ve false la cham xuyen qua bang xuong man choi, nhan vat chay
            // di hoac menu khac bat len.
            return true;
        }

        /// <summary>
        /// Chạm ở cột phải thẻ "Nhiệm vụ": chọn nhóm, hoặc đi tới một bản đồ.
        /// </summary>
        /// <remarks>
        /// Bấm một bản đồ là <b>đóng bảng rồi mới xin dịch chuyển</b>: người chơi
        /// vừa bấm đi là muốn thấy bản đồ mới ngay, để bảng che kín màn hình thì
        /// phải bấm thoát thêm một lần nữa.
        ///
        /// Client chỉ gửi <c>id</c> của điểm, không gửi toạ độ — máy chủ tra lại
        /// trong <c>map_nhanh</c>. Gửi toạ độ là để client tự chọn nơi muốn tới,
        /// kể cả nơi đã bị tắt.
        /// </remarks>
        private bool chamBanDo()
        {
            int so = dsNhomMap.Count;
            for (int i = 0; i < so; i++)
            {
                int[] o = oNutNhom(i, so);
                if (cham(o[0], o[1], o[2], o[3]))
                {
                    if (nhomChon != i)
                    {
                        // Doi nhom thi ve dau danh sach: dong thu muoi cua nhom
                        // nay khong co nghia gi voi nhom kia.
                        cuon = 0;
                    }
                    nhomChon = i;
                    return true;
                }
            }

            NhomMap nh = nhomDangChon();
            if (nh == null)
            {
                return true;
            }
            int yND = yKhungMap() + CAO_DAI_TD + KHE_KHUNG + 2;
            int thay = soDongMapThay();
            for (int i = cuon; i < nh.diem.Count && i - cuon < thay; i++)
            {
                int y = yND + (i - cuon) * CAO_DONG_MAP;
                if (cham(xPhai + 4, y, rongPhai - 8, CAO_DONG_MAP - 2))
                {
                    int id = nh.diem[i].id;
                    dong();
                    Service.gI().banDoDi(id);
                    return true;
                }
            }
            return true;
        }

        private bool chamBanThan()
        {
            int wCon = rongPhai / TEN_THE_CON.Length;
            for (int i = 0; i < TEN_THE_CON.Length; i++)
            {
                if (cham(xPhai + i * wCon, yTheCon, wCon - 2, CAO_THE_CON))
                {
                    if (theConChon != i)
                    {
                        cuon = 0;
                    }
                    theConChon = i;
                    return true;
                }
            }
            int m = oTrangBiTaiDiem(GameCanvas.px, GameCanvas.py);
            if (m >= 0)
            {
                Item[] mac = Char.myCharz().arrItemBody;
                moHop(mac[m], m < TEN_O.Length ? TEN_O[m] : "", m, -1);
                return true;
            }
            if (theConChon == CON_HANH_TRANG)
            {
                int i = oTuiTaiDiem(GameCanvas.px, GameCanvas.py);
                if (i >= 0)
                {
                    moHop(Char.myCharz().arrItemBag[i], "", -1, i);
                    return true;
                }
            }
            return true;
        }

        /// <summary>Chạm trong thẻ "Kỹ Năng".</summary>
        /// <summary>
        /// Chạm trong thẻ "Kỹ năng".
        /// </summary>
        /// <remarks>
        /// Không còn dải thẻ con: hai cột hiện cùng lúc nên không có gì để chọn
        /// qua lại. Chỉ còn năm dòng chỉ số bên cột trái là bấm được.
        /// </remarks>
        private bool chamKyNang()
        {
            for (int i = 0; i < TEN_CHI_SO_GOC.Length; i++)
            {
                int[] o = oDongChiSo(i);
                if (cham(o[0], o[1], o[2], o[3]))
                {
                    // Mo hop nang CUA MAN NAY, khong dong bang de goi hop cua
                    // game. Bam mot cai la mat sach man dang xem thi rat kho dung.
                    moHopNang(i);
                    return true;
                }
            }
            // O "Noi tai" nam tren dau cot phai, xet TRUOC danh sach ky nang:
            // hai vung nay khong chong nhau, nhung xet truoc thi thu tu doc ra
            // giong thu tu nhin thay.
            int[] oNT = oNutNoiTai();
            if (cham(oNT[0], oNT[1], oNT[2], oNT[3]))
            {
                God.NoiTaiUI.getInstance().mo();
                return true;
            }
            // Cot phai: bam mot ky nang thi mo hop chi tiet kem nut gan phim.
            //
            // Vung bam dung DUNG cong thuc cua veDsKyNang — cung yDauKyNang,
            // cung CAO_DONG_KN, cung moc cuon. Tinh rieng mot cong thuc thu hai
            // la hai ben lech nhau ngay lan dau doi chieu cao dong.
            SkillTemplate[] ds = mauKyNangHanhTinh();
            if (ds != null)
            {
                int thay = (yThan + caoThan - yDauKyNang - 4) / CAO_DONG_KN;
                if (thay < 1)
                {
                    thay = 1;
                }
                for (int i = cuon; i < ds.Length && i - cuon < thay; i++)
                {
                    int y = yDauKyNang + 3 + (i - cuon) * CAO_DONG_KN;
                    if (cham(xPhai + 4, y, rongPhai - 8, CAO_DONG_KN - 3))
                    {
                        kyNangXem = ds[i];
                        hienHopKyNang = true;
                        return true;
                    }
                }
            }
            return true;
        }

        /// <summary>Chạm trong thẻ "Đệ Tử".</summary>
        private bool chamDeTu()
        {
            // Bat cham DAI THE TRUOC cai chan "chua co de".
            //
            // Dai the con van duoc VE du chua co de, nen chan truoc thi nguoi
            // choi thay ba the ma bam khong an — dung loi "menu de tu khong doi
            // duoc the Chi so / Ky nang". De co ten hay chua la chuyen cua phan
            // NOI DUNG, khong lien quan toi viec doi the.
            //
            // Dai the con nam o COT PHAI. Truoc day do theo ca be ngang bang nen
            // bam vao o trang bi hang tren cung ben trai cung tinh la bam the —
            // dung loi "bam chu Ky nang thi nhay tab".
            int t = theNhoTaiDiem(TEN_THE_DE.Length, xPhai, rongPhai);
            if (t >= 0)
            {
                theDeChon = t;
                return true;
            }

            Char de = Char.myPetz();
            if (!Char.myCharz().havePet || de == null
                    || de.cName == null || de.cName.Length == 0)
            {
                return true;
            }
            {
                Item[] mac = de.arrItemBody;
                if (mac != null)
                {
                    for (int i = 0; i < mac.Length; i++)
                    {
                        int[] o = oODeTu(i);
                        if (o == null)
                        {
                            continue;
                        }
                        if (cham(o[0], o[1], o[2], o[2]))
                        {
                            moHopDe(mac[i], i);
                            return true;
                        }
                    }
                }
            }
            if (theDeChon == DE_TRANG_THAI)
            {
                for (int i = 0; i < soViecDe(); i++)
                {
                    int[] o = oNutViecDe(i);
                    if (cham(o[0], o[1], o[2], o[3]))
                    {
                        Service.gI().petStatus((sbyte) i);
                        return true;
                    }
                }
            }
            return true;
        }

        /// <summary>Mở hộp cho một ô đồ của ĐỆ, nút là "Lấy ra" về túi mình.</summary>
        private void moHopDe(Item it, int oDe)
        {
            monXem = it;
            tenOXem = "Ô của đệ";
            // Danh dau bang oMacDe chu khong dung oMacXem: hai o cung chi so nhung
            // mot cai la nguoi, mot cai la de — lan lon la lenh "lay ra" coi do
            // cua de nhu do cua minh va thao sai mon.
            oMacXem = -1;
            oTuiXem = -1;
            oMacDeXem = oDe;
            hienHop = true;
            GameCanvas.clearAllPointerEvent();
        }

        /// <summary>Chỉ số ô trang bị CỦA ĐỆ đang xem, hoặc -1.</summary>
        private int oMacDeXem = -1;

        private void moHop(Item it, string tenO, int oMac, int oTui)
        {
            // Ô TRỐNG trong hành trang thì bỏ qua hẳn.
            //
            // Hộp "Ô trống — Chưa mặc gì ở ô này" từng là cố ý: bấm mà im hẳn
            // thì không phân biệt được ô trống với màn hình treo. Nhưng lý do ấy
            // chỉ đúng với các Ô TRANG BỊ — ở đó ô trống là một chỗ có ý nghĩa
            // ("chưa mặc áo"), và hộp nói ra điều đó.
            //
            // Hành trang thì khác: nó có hàng chục ô trống liền nhau, và người
            // chơi lướt qua chúng suốt. Mở một hộp rồi phải bấm tắt cho mỗi lần
            // chạm nhầm là phiền chứ không phải giúp.
            //
            // oMac < 0 nghĩa là ô này KHÔNG phải ô trang bị — tức ô hành trang,
            // ô rương, ô đệ. Kèm điều kiện món rỗng thì đúng là "ô trống trong
            // hành trang".
            if ((it == null || it.template == null) && oMac < 0)
            {
                hienHop = false;
                GameCanvas.clearAllPointerEvent();
                return;
            }
            monXem = it;
            tenOXem = (tenO == null) ? "" : tenO;
            oMacXem = oMac;
            oTuiXem = oTui;
            // Xoa dau o cua de: khong xoa thi bam do cua minh ngay sau khi xem do
            // cua de, nut "Lay ra" van goi lenh thao do cua DE.
            oMacDeXem = -1;
            hienHop = true;
            GameCanvas.clearAllPointerEvent();
        }

        /// <summary>Cuộn lưới đang xem bằng bánh xe hoặc kéo ngón.</summary>
        private void cuonLuoi()
        {
            int soDong = soDongCuonDuoc();
            int thay = soHangThayDuoc();
            int toiDa = Math.max(0, soDong - thay);
            if (toiDa <= 0)
            {
                cuon = 0;
                dangKeo = false;
                return;
            }
            int buocMot = buocCuon();
            if (GameCanvas.pXYScrollMouse != 0
                    && trongVungCuon(GameCanvas.pxMouse, GameCanvas.pyMouse))
            {
                cuon += GameCanvas.pXYScrollMouse > 0 ? -1 : 1;
            }
            if (GameCanvas.isPointerDown)
            {
                if (!dangKeo)
                {
                    if (!trongVungCuon(GameCanvas.pxFirst, GameCanvas.pyFirst))
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
                // Doi quang ngon vua di thanh PHAN LE cua hang, khong chia
                // nguyen nua.
                //
                // Ban cu lay `(py - yMocKeo) / buocMot` — phep chia nguyen. Mot
                // hang cao gan bon chuc diem, nen phai keo tron bon chuc diem
                // moi thay luoi nhuc nhich, roi no nhay THANG mot hang. Cam
                // giac dung la "keo mai khong di roi giat mot cai" — do la cai
                // khung ma nguoi dung gap tren dien thoai.
                //
                // Nay phan le duoc giu lai o `duCuon`, nen luoi chay deu theo
                // ngon; chi phan NGUYEN moi doi thanh so hang, va nho the moi
                // cong thuc ve/bat cham theo hang van dung y nguyen.
                int dy = GameCanvas.py - yMocKeo;
                if (dy != 0)
                {
                    yMocKeo = GameCanvas.py;
                    float hang = (float) dy / buocMot;
                    duCuon -= hang;
                    // Van toc lay trung binh truot, khong lay rieng quang cuoi:
                    // mot cai giat nho luc nha ngon khong duoc quyet dinh ca
                    // cu quan tinh.
                    vanTocCuon = vanTocCuon * 0.55f - hang * 0.45f;
                    apDuCuon();
                }
            }
            else
            {
                dangKeo = false;
                // Quan tinh: nha ngon roi luoi con troi tiep va cham dan.
                //
                // Day moi la thu lam cho cam giac "muot". Ban cu dung phat khi
                // nha ngon, nen luot mot danh sach dai phai vuot lien tuc chuc
                // lan.
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
            gioiHanCuon(soDong);
            if (cuon != truoc)
            {
                // Cham bien: dung han, dung de van toc day mai vao tuong.
                vanTocCuon = 0f;
                duCuon = 0f;
            }
        }

        /// <summary>Ma sát của quán tính: mỗi khung hình còn lại bấy nhiêu.</summary>
        /// <remarks>
        /// 0,90 cho quãng trôi chừng nửa giây ở 60 khung hình mỗi giây — đủ để
        /// lướt qua một hành trang dài bằng vài cú vuốt, không lâu tới mức phải
        /// chờ nó dừng mới bấm được vào ô.
        /// </remarks>
        private const float MA_SAT_CUON = 0.90f;

        /// <summary>Phần lẻ của hàng đang tích lại, khoảng -1 tới 1.</summary>
        private float duCuon;

        /// <summary>Vận tốc cuộn, tính bằng HÀNG mỗi khung hình.</summary>
        private float vanTocCuon;

        /// <summary>Chuyển phần NGUYÊN của <c>duCuon</c> thành số hàng đã cuộn.</summary>
        /// <remarks>
        /// Giữ lại phần lẻ. Làm tròn về 0 (ép kiểu <c>int</c>) chứ không làm
        /// tròn gần nhất: làm tròn gần nhất thì một cú chạm rung tay nhích nửa
        /// hàng cũng đủ đẩy lưới đi một hàng.
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

        /// <summary>Số dòng cuộn được của thẻ đang xem.</summary>
        private int soDongCuonDuoc()
        {
            if (theChon == THE_NHIEM_VU)
            {
                NhomMap nh = nhomDangChon();
                return (nh == null) ? 0 : nh.diem.Count;
            }
            if (theChon == THE_BANG_HOI)
            {
                // Cot phai doi noi dung theo the con, nen so dong cuon duoc cung
                // phai doi theo. Lay mot nguon co dinh la keo chat lai cuon theo
                // so bang, hoac nguoc lai.
                if (dangChonCo)
                {
                    int soCo = ClanImage.vClanImage.size();
                    return (soCo + CO_SO_COT - 1) / CO_SO_COT;
                }
                if (bangXem != null)
                {
                    if (theBangXemChon != 1)
                    {
                        return 0;
                    }
                    MyVector dsTv = (GameCanvas.panel == null)
                            ? null : GameCanvas.panel.member;
                    return (dsTv == null) ? 0 : dsTv.size();
                }
                if (theBangChon == BANG_TIM)
                {
                    if (coBangHoi())
                    {
                        MyVector dsTv = myMemberCuaToi();
                        return (dsTv == null) ? 0 : dsTv.size();
                    }
                    Clan[] ds = (GameCanvas.panel == null)
                            ? null : GameCanvas.panel.clans;
                    return (ds == null) ? 0 : ds.Length;
                }
                return ClanMessage.vMessage.size();
            }
            if (theChon == THE_KY_NANG)
            {
                SkillTemplate[] ds = mauKyNangHanhTinh();
                return (ds == null) ? 0 : ds.Length;
            }
            if (theChon == THE_BAN_THAN && theConChon == CON_HANH_TRANG)
            {
                return soHangTui();
            }
            return 0;
        }

        private int soHangThayDuoc()
        {
            if (theChon == THE_NHIEM_VU)
            {
                return soDongMapThay();
            }
            if (theChon == THE_BANG_HOI)
            {
                if (dangChonCo)
                {
                    return soHangCoThay();
                }
                int buoc = buocDongBang();
                int cao = (bangXem == null)
                        ? (caoThan - CAO_DAI_TD - KHE_KHUNG - 6)
                        : (oNutXinVao()[1] - 4
                                - (yThan + CAO_DAI_TD + KHE_KHUNG
                                        + CAO_THE_CON + 4));
                int n = cao / buoc;
                return (n < 1) ? 1 : n;
            }
            if (theChon == THE_KY_NANG)
            {
                int n = (yThan + caoThan - yDauKyNang - 4) / CAO_DONG_KN;
                return (n < 1) ? 1 : n;
            }
            int m = TUI_SO_HANG;
            return (m < 1) ? 1 : m;
        }

        private int buocCuon()
        {
            if (theChon == THE_NHIEM_VU)
            {
                return CAO_DONG_MAP;
            }
            if (theChon == THE_BANG_HOI)
            {
                if (dangChonCo)
                {
                    return (rongTrai - 10) / CO_SO_COT;
                }
                return buocDongBang();
            }
            return (theChon == THE_KY_NANG) ? CAO_DONG_KN : oTuiDoc;
        }

        /// <summary>
        /// Bề cao một dòng của thứ đang cuộn trong thẻ "Bang hội".
        /// </summary>
        /// <remarks>
        /// Bốn thứ khác nhau dùng chung một biến cuộn: chat, danh sách bang, danh
        /// sách thành viên, và lưới cờ. Bước cuộn phải khớp bề cao dòng đang vẽ,
        /// kẻo kéo một nhịp lại nhảy nửa dòng và vùng bấm lệch khỏi vùng vẽ.
        /// </remarks>
        private int buocDongBang()
        {
            if (bangXem != null)
            {
                return CAO_DONG_TV;
            }
            if (theBangChon != BANG_TIM)
            {
                return CAO_DONG_CHAT;
            }
            return coBangHoi() ? CAO_DONG_TV : CAO_DONG_BANG;
        }

        private bool trongVungCuon(int x, int y)
        {
            if (theChon == THE_NHIEM_VU)
            {
                return x >= xPhai && x <= xPhai + rongPhai
                        && y >= yKhungMap() && y <= yThan + caoThan;
            }
            if (theChon == THE_BANG_HOI)
            {
                if (dangChonCo)
                {
                    // Luoi co nam o COT TRAI.
                    return x >= xTrai && x <= xTrai + rongTrai
                            && y >= yNoiDungPhu()
                            && y <= yNoiDungPhu() + caoNoiDungPhu();
                }
                return x >= xPhai && x <= xPhai + rongPhai
                        && y >= yThan && y <= yThan + caoThan;
            }
            if (theChon == THE_KY_NANG)
            {
                // Chi cot PHAI ("Skill") cuon duoc; cot trai co dung nam dong,
                // khong bao gio du de phai cuon.
                return x >= xPhai && x <= xPhai + rongPhai
                        && y >= yDauKyNang && y <= yThan + caoThan;
            }
            return x >= xPhai && x <= xPhai + rongPhai
                    && y >= yNoiDung && y <= yNoiDung + caoNoiDung;
        }

        private void gioiHanCuon(int soDong)
        {
            int toiDa = Math.max(0, soDong - soHangThayDuoc());
            if (cuon > toiDa)
            {
                cuon = toiDa;
            }
            if (cuon < 0)
            {
                cuon = 0;
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
