using Game5.Assets.src.e;
using UnityEngine;

namespace Game5.God
{
/*Author: HAIRMOD*/
    public class ClientManager : IActionListener
    {
        private static ClientManager instance { get; set; }
        private int xJ, yJ;

        /// <summary>Cụm ba nút chuyển map nhanh đang thu gọn.</summary>
        private static bool thuGonMuiTen;

        /// <summary>Đã đọc lựa chọn thu gọn từ bộ nhớ máy chưa.</summary>
        private static bool daDocThuGonMuiTen;

        /// <summary>
        /// Ô nút thu gọn cụm ba nút mũi tên — <b>ngay trên nút đầu tiên</b>.
        /// </summary>
        /// <remarks>
        /// Chỗ này không đổi khi cụm đã gọn, nên nút mở lại luôn nằm đúng chỗ
        /// vừa bấm.
        /// </remarks>
        private static int[] oThuGonMuiTen()
        {
            int co = GameScr.CO_NUT_THU_GON;
            int caoNut = (GameScr.imgNut != null) ? GameScr.imgNut.getHeight() : 34;
            int caoCan = (GameScr.imgAnalog1 != null)
                    ? GameScr.imgAnalog1.getHeight() : 56;
            int y = GameCanvas.h - 45 - caoCan - 10;
            return new int[] { 20 - co / 2, y - caoNut / 2 - co - 4, co, co };
        }

        /// <summary>Đọc lựa chọn thu gọn một lần, ở lần dùng đầu tiên.</summary>
        private static void docThuGonMuiTen()
        {
            if (daDocThuGonMuiTen)
            {
                return;
            }
            daDocThuGonMuiTen = true;
            thuGonMuiTen = Rms.loadRMSInt("thuGonMuiTen") == 1;
        }
        public static ClientManager getInstance()
        {
            return (instance == null) ? (instance = new ClientManager()) : instance;
        }
        public void Update()
        {
            NPoint.getInstance().Update();
            Items.getInstance().Update();
            Revive.getInstance().Update();
            Mobs.Update();
            nSkill.getInstance().Update();
            PetService.getInstance().Update();
            ListChars.getInstance().Update();
            // Man phuc loi tu xin lai du lieu theo nhip, cho con so tich luy
            // khong dung yen khi dang mo.
            PhucLoiUI.getInstance().capNhat();
            BossUI.getInstance().capNhat();
            // Man nhan vat kieu moi: no can nhip nay de gui tiep tung lan "dung
            // nhieu". Khong goi thi bam Dung o hop so luong xong khong co gi xay
            // ra, vi viec do chia thanh nhieu goi gui theo nhip chu khong gui hết
            // trong mot lan.
            TuiUI.getInstance().capNhat();
            // Luot cau ca chay tiep khi bang da dong: khong ai giu day thi
            // thanh tut dan, ve 0% la hut ca va bao len may chu. Khong goi o
            // day thi dong bang giua luot la luot dung hinh — mat tien moi ma
            // khong co ket qua nao.
            TroChoiUI.getInstance().capNhatNen();
            canhDoiMapKet();
        }

        // ------------------------------------------------------------------
        //  Chot an toan: khong bao gio ket o trang thai "dang doi ban do"
        // ------------------------------------------------------------------

        private static bool doiMapTruoc;
        private static long mocDoiMap;

        /// <summary>Quá lâu mà chưa vào được bản đồ mới thì thôi, mili giây.</summary>
        /// <remarks>
        /// Máy chủ trả lời trong dưới một giây ở mạng bình thường. Tám giây là
        /// để dành cho mạng rất kém.
        /// </remarks>
        private const long HAN_DOI_MAP = 8000L;

        /// <summary>
        /// Mở khoá nếu lượt đổi bản đồ treo mãi không xong.
        /// </summary>
        /// <remarks>
        /// <para><c>Char.ischangingMap</c> bật lên lúc gửi yêu cầu và chỉ tắt khi
        /// máy chủ trả về bản đồ mới, hoặc trả về lệnh đứng yên tại chỗ. Mất một
        /// trong hai gói ấy — mạng vấp, máy chủ bỏ gói vì trùng lượt — là cờ
        /// nằm bật <b>vĩnh viễn</b>.</para>
        ///
        /// <para>Và khi nó bật thì gần như cả game ngừng nhận thao tác: hộp
        /// thông tin không cập nhật, không gửi được lệnh di chuyển, các màn phụ
        /// bỏ qua phím. Nhìn ra đúng là <b>"đơ hết, bấm gì cũng không vào"</b>.
        /// Phải thoát game mới chơi lại được.</para>
        ///
        /// <para>Chốt này không sửa nguyên nhân — nguyên nhân đã chặn ở máy chủ
        /// bằng khoá đổi bản đồ. Nó chỉ bảo đảm rằng dù có chuyện gì thì sau tám
        /// giây người chơi vẫn điều khiển được, thay vì phải tắt game.</para>
        /// </remarks>
        private static void canhDoiMapKet()
        {
            if (!Char.ischangingMap)
            {
                doiMapTruoc = false;
                return;
            }
            long gio = mSystem.currentTimeMillis();
            if (!doiMapTruoc)
            {
                doiMapTruoc = true;
                mocDoiMap = gio;
                return;
            }
            if (gio - mocDoiMap < HAN_DOI_MAP)
            {
                return;
            }
            doiMapTruoc = false;
            Char.ischangingMap = false;
            Char.isLockKey = false;
            InfoDlg.hide();
            GameCanvas.clearKeyHold();
            GameCanvas.clearKeyPressed();
            GameScr.info1.addInfo("Chuyển bản đồ không xong, thử lại nhé", 0);
        }
        /// <summary>Có màn phụ nào (Phúc lợi, cấu hình Voice…) đang mở không.</summary>
        /// <remarks>
        /// Một chỗ duy nhất liệt kê các màn phụ. Khi có màn phụ mở, toàn bộ thao
        /// tác bên ngoài bị chặn — bấm xuyên qua bảng làm nhân vật chạy đi hoặc
        /// menu khác bật lên thì rất khó chịu, và người chơi tưởng bảng hỏng.
        ///
        /// Thêm màn phụ mới thì cộng thêm một vế ở đây, không phải đi sửa
        /// GameScr hay chỗ nào khác.
        /// </remarks>
        /// <summary>Nam id bong tai hop the duoc, xep tu cap CAO xuong cap thap.</summary>
        /// <remarks>
        /// Phai do theo id chu khong do theo ten: ten trong bang mau khong nhat
        /// quan — 454, 921, 2105, 2106 la "Bông tai Porata" con 1943 la
        /// "Hoa tai Potara". Ban cu do "bông tai" bang StartsWith nen ai lên cap
        /// 3 la bam f khong an, bao "Không Có Bông Tai" du dang giu bong tai.
        ///
        /// Xep cap cao truoc de nang cap co y nghia: co ca hai thi dung cai xin
        /// hon, khong phai cai tim thay dau tien.
        /// </remarks>
        private static readonly int[] ID_BONG_TAI = { 2106, 2105, 1943, 921, 454 };

        /// <summary>Bong tai cap cao nhat dang co trong hanh trang, hoac null.</summary>
        private static Item itemBongTai()
        {
            for (int i = 0; i < ID_BONG_TAI.Length; i++)
            {
                foreach (var it in Char.myCharz().arrItemBag)
                {
                    if (it != null && it.template != null
                            && it.template.id == ID_BONG_TAI[i])
                    {
                        return it;
                    }
                }
            }
            return null;
        }

        /// <summary>Id bong tai cap cao nhat dang co, hoac -1.</summary>
        private static int timBongTai()
        {
            Item it = itemBongTai();
            return it == null ? -1 : it.template.id;
        }

        public static bool coManPhuDangMo()
        {
            // Dang mo hop nhap chu thi KHONG tinh la co man phu.
            //
            // GameScr.updateKey() thay ham nay tra true la goi UpdateTouch roi
            // RETURN ngay. Ma phan bat nut "OK" / "Dong" cua hop nhap nam SAU cho
            // return do (GameScr, quanh dong 2735) — nen mo bang xong go chu thi
            // hai nut ay khong bao gio nhan duoc cu bam, va nguoi choi mac o hop
            // nhap khong thoat ra duoc.
            if (ChatTextField.gI() != null && ChatTextField.gI().isShow)
            {
                return false;
            }
            // Hai hop nhap chi tinh la "man phu" khi CO MAN PHU NAO DO dang mo,
            // hoac khi dang o ngoai cac bang cua game.
            //
            // Van an toan: neu mot hop bi ket o trang thai mo, no se khoa
            // `GameScr.updateKey` va ca game chet cung — NPC, cua hang, ban do
            // deu khong nhan cham. Da xay ra dung nhu vay mot lan. Nay bang cua
            // game dang mo thi bang do duoc uu tien, hop tu dong duoc bo qua.
            // KHONG dong hop khi bang dang TRUOT RA.
            //
            // Panel.hide() chi bat hoat anh dong; `isShow` van con true suot may
            // khung hinh cho toi khi bang truot het (Panel, cho dat isShow =
            // false trong update). Ma nhung cho mo hop nhap deu goi panel.hide()
            // NGAY TRUOC khi mo hop — "Mua nhieu", "Dung nhieu", nhap so lan
            // nang. The la hop vua mo xong lien bi dong o khung hinh ke tiep,
            // nguoi choi khong kip thay gi ca: bam "Mua nhieu" thi khong co o
            // dien so luong nao hien ra.
            //
            // `isClose` la co danh dau bang dang trong hoat anh dong, nen doc
            // them no thi phan biet duoc "bang dang mo that" voi "bang sap
            // bien mat".
            if (GameCanvas.panel != null && GameCanvas.panel.isShow
                    && !GameCanvas.panel.isClose)
            {
                BanPhimSo.getInstance().dong();
                if (!HopNhapChu.getInstance().vuaMo)
                {
                    HopNhapChu.getInstance().dong();
                }
            }
            return BanPhimSo.getInstance().dangMo
                    || HopNhapChu.getInstance().dangMo
                    || MenuTongUI.getInstance().dangMo
                    || TroChoiUI.getInstance().dangMo
                    || CoUI.getInstance().dangMo
                    || ChonNhanhUI.getInstance().dangMo
                    || PhucLoiUI.getInstance().dangMo
                    || SuKienUI.getInstance().dangMo
                    || BossUI.getInstance().dangMo
                    || TuiUI.getInstance().dangMo
                    || VoiceConfigUI.getInstance().dangMo
                    || TanSatUI.getInstance().dangMo
                    || NoiTaiUI.getInstance().dangMo;
        }

        public void UpdateTouch()

        {
            // Ban phim so xet TRUOC tat ca: no la hop chan, phai an cham truoc
            // ca man da goi no ra.
            if (BanPhimSo.getInstance().capNhatCham())
            {
                return;
            }
            if (HopNhapChu.getInstance().capNhatCham())
            {
                return;
            }
            // Hop cau hinh nuot cham truoc: bam xuyen qua hop ma nhan vat chay
            // di hoac menu khac mo len thi rat kho chiu.
            // Hai bang nay xet TRUOC TuiUI vi chung nam TREN no.
            //
            // Bang Noi tai mo tu the Ky nang cua chinh TuiUI, nen neu TuiUI xet
            // truoc thi no nuot sach cham va bang Noi tai khong bao gio nhan
            // duoc gi — bam nut nao cung khong an.
            if (NoiTaiUI.getInstance().capNhatCham())
            {
                return;
            }
            if (TanSatUI.getInstance().capNhatCham())
            {
                return;
            }
            if (TuiUI.getInstance().capNhatCham())
            {
                return;
            }
            if (MenuTongUI.getInstance().capNhatCham())
            {
                return;
            }
            if (CoUI.getInstance().capNhatCham())
            {
                return;
            }
            if (ChonNhanhUI.getInstance().capNhatCham())
            {
                return;
            }
            if (TroChoiUI.getInstance().capNhatCham())
            {
                return;
            }
            if (BossUI.getInstance().capNhatCham())
            {
                return;
            }
            if (SuKienUI.getInstance().capNhatCham())
            {
                return;
            }
            if (PhucLoiUI.getInstance().capNhatCham())
            {
                return;
            }

            if (VoiceConfigUI.getInstance().capNhatCham())
            {
                return;
            }
            // Khung chat xet TRUOC nut HUD: no nam o goc duoi ben trai,
            // khong chong len nut nao, va de sau thi cham vao the chat
            // se roi xuong nhanh "cham vao ban do" — nhan vat chay toi.
            if (ChatUI.getInstance().capNhatCham())
            {
                return;
            }
            if (chamNutHud())
            {
                return;
            }
            ListChars.getInstance().UpdateTouch();
            // Banh rang: bat cham NGOAI khoi isAnalog. Nam trong do thi ai tat
            // can dieu khien ao la khong bam duoc nut mo menu mod nua.
            if (choVeNutHud() && !GameCanvas.isPointerMove
                    && GameCanvas.isPointerHoldIn(xBanhRang() - NUT_CANH / 2, TAM_Y - NUT_CANH / 2, NUT_CANH, NUT_CANH))
            {
                mScreen.keyTouch = 999;
                if (GameCanvas.isPointerJustRelease && GameCanvas.isPointerClick)
                {
                    GameCanvas.keyAsciiPress = 'x';
                    GameCanvas.clearAllPointerEvent();
                }
                return;
            }
            if (GameScr.isAnalog == 1)
            {

                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(GameScr.xHP + 20 + 5 - GameScr.imgNut.getWidth() / 2, GameScr.yHP + 20 - 6 - 40 + 10 - GameScr.imgNut.getHeight() / 2, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 14;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'c';
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }
                }
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(GameScr.xTG + 20 - GameScr.imgNut.getWidth() / 2, GameScr.yTG - 20 - GameScr.imgNut.getHeight() / 2, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 1000;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'f';
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }
                }
                // Nut tron "doi khu" da bo — xem chu thich o cho ve trong GUI().
                docThuGonMuiTen();
                int[] oTGM = oThuGonMuiTen();
                if (!GameCanvas.isPointerMove
                        && GameCanvas.isPointerHoldIn(oTGM[0], oTGM[1], oTGM[2], oTGM[3]))
                {
                    if (GameCanvas.isPointerJustRelease)
                    {
                        thuGonMuiTen = !thuGonMuiTen;
                        Rms.saveRMSInt("thuGonMuiTen", thuGonMuiTen ? 1 : 0);
                        GameCanvas.clearAllPointerEvent();
                    }
                    return;
                }
                if (thuGonMuiTen)
                {
                    return;
                }
                xJ = 20;
                yJ = GameCanvas.h - 45 - GameScr.imgAnalog1.getHeight() - 10;
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(xJ - GameScr.imgNut.getWidth() / 2, yJ - GameScr.imgNut.getHeight() / 2, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 1002;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'j';
                        GameCanvas.clearAllPointerEvent();
                    }
                    return;
                }
                xJ += GameScr.imgNut.getWidth() + 20;
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(xJ - GameScr.imgNut.getWidth() / 2, yJ - GameScr.imgNut.getHeight() / 2, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 1003;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'k';
                        GameCanvas.clearAllPointerEvent();
                    }
                    return;
                }
                xJ += GameScr.imgNut.getWidth() + 20;
                if (!GameCanvas.isPointerMove && GameCanvas.isPointerHoldIn(xJ - GameScr.imgNut.getWidth() / 2, yJ - GameScr.imgNut.getHeight() / 2, GameScr.imgNut.getWidth(), GameScr.imgNut.getHeight()))
                {
                    mScreen.keyTouch = 1004;
                    if (GameCanvas.isPointerJustRelease)
                    {
                        GameCanvas.keyAsciiPress = 'l';
                        GameCanvas.clearAllPointerEvent();
                    }
                    return;
                }
            }
        }
        /// <summary>Mốc kết thúc lượt đấm máy đo sức mạnh, tính bằng mili giây.</summary>
        /// <remarks>
        /// Máy chủ chỉ báo một lần lúc mở lượt là "còn bao nhiêu giây"; client tự
        /// đếm lùi bằng đồng hồ của mình. Bắn gói mỗi giây thì tốn đường truyền
        /// mà chữ vẫn giật theo độ trễ mạng.
        /// </remarks>
        public static long ketThucDemNguocMayDam;

        /// <summary>Dòng chữ đỏ đếm lùi giữa màn hình khi đang trong lượt đấm.</summary>
        /// <summary>Chữ đếm ngược trên nền mờ, đọc được trên mọi bản đồ.</summary>
        private static void veChuDemNguoc(mGraphics g, string s, int giuaX, int y)
        {
            int w = mFont.tahoma_7b_red.getWidth(s);
            // Vien bo goc, khong dung `drawRect` (ham do khong nhan ban kinh).
            g.setColor(0xC8933C, 0.8f);
            g.fillRect(giuaX - w / 2 - 9, y - 4, w + 18, 19, 9);
            g.setColor(0x241809, 0.85f);
            g.fillRect(giuaX - w / 2 - 8, y - 3, w + 16, 17, 8);
            mFont.tahoma_7b_red.drawString(g, s, giuaX, y, mFont.CENTER);
        }

        private void veDemNguocMayDam(mGraphics g)

        {
            if (ketThucDemNguocMayDam <= 0)
            {
                return;
            }
            long conLai = ketThucDemNguocMayDam - mSystem.currentTimeMillis();
            if (conLai <= 0)
            {
                ketThucDemNguocMayDam = 0;
                return;
            }
            // Làm tròn LÊN: còn 0,4 giây mà hiện "0 giây" thì nhìn như đã hết giờ
            // trong khi vẫn còn đấm được.
            int giay = (int)((conLai + 999) / 1000);
            // Dat DUOI khung doi tuong dang chon (khung do cao toi ~36 diem).
            //
            // Toa do cu la y=26, nam dung giua khung ay — chu dem nguoc chong
            // len ten va thanh mau cua muc tieu, ca hai deu khong doc duoc.
            veChuDemNguoc(g, "Còn " + giay + " giây", GameCanvas.w / 2, 46);
        }

        /// <summary>Toạ độ hàng nút HUD: bánh răng, sự kiện, phúc lợi.</summary>
        /// <remarks>
        /// Ba nút chung một <b>đường tâm ngang</b> (<c>TAM_Y</c>) chứ không chung
        /// mép trên, để icon to nhỏ khác nhau vẫn thẳng hàng.
        ///
        /// Hai icon Sự kiện / Phúc lợi lấy thẳng từ <b>kho icon của máy chủ</b>
        /// theo id, y như nút bánh răng — không đóng gói PNG rời nữa. Nhờ vậy
        /// khỏi lo cỡ ảnh, khỏi xoá phông, và mỗi mức zoom client tự lấy đúng
        /// bản trong data/icon/x1..x4.
        ///
        /// Không nút nào vẽ nền: nền tròn đen mờ vẽ mỗi khung hình chính là cái
        /// viền đen quanh icon.
        /// </remarks>
        private const int TAM_Y = 15;

        /// <summary>Tâm ngang của nút ba gạch.</summary>
        /// <remarks>
        /// Hằng số RIÊNG, không dùng chung <see cref="TAM_Y"/>: <c>TAM_Y</c> còn
        /// là tâm của nút bánh răng nằm bên trái, đổi nó là đổi luôn nút đó.
        ///
        /// Giá trị 19 để nút ba gạch <b>cùng đường tâm với nút chat</b>: nút chat
        /// cao 34 và đặt tại y=2, nên tâm nó ở 19. Lấy 15 như trước thì ba gạch
        /// vừa lệch tâm so với nút chat vừa chạm sát viền trên màn hình.
        /// </remarks>
        /// <summary>
        /// Đường tâm ngang của nút ba gạch.
        /// </summary>
        /// <remarks>
        /// Giữ nguyên ở 19 — thẳng hàng với hàng trên của lưới 2×2 bên trái.
        /// Đã thử dời xuống 35 cho nó nằm giữa hai hàng, nhưng chỗ cũ mới là chỗ
        /// người chơi quen tay tìm tới.
        /// </remarks>
        private const int TAM_Y_BA_GACH = 19;
        private const int NUT_CANH = 30;
        /// <summary>Mau loe khi bam. 225225225 trong ma cu KHONG phai mau trang —
        /// giai ra la (108,170,9), tuc mau xanh la.</summary>
        private const int MAU_LOE = 16777215;
        /// <summary>Toa do TAM cua nut banh rang.</summary>
        /// <remarks>
        /// Ba hang so X_SU_KIEN / X_PHUC_LOI / X_BOSS va ba id icon di kem da bo
        /// cung luc bo <c>veNutIcon</c>: ba nut do gio nam trong popup menu tong.
        /// </remarks>
        /// <remarks>
        /// Doi tu 168 sang 190: khung nhan vat moi (chan dung tron + hai thanh
        /// mau) keo dai toi khoang 165 diem, nen nut banh rang o 168 nam de len
        /// duoi thanh KI.
        /// </remarks>
        /// <summary>
        /// Tam ngang cua nut banh rang.
        /// </summary>
        /// <remarks>
        /// Bam theo MEP PHAI cua khung nhan vat chu khong dat cung mot con
        /// so: khung do doi be rong theo chan dung va theo do dai ten, dat
        /// cung thi luc ho ra mot khoang, luc de len ca thanh mau.
        /// </remarks>
        /// <summary>Mép phải của nút bánh răng — bảng mục tiêu dừng sau chỗ này.</summary>
        public static int mepPhaiBanhRang()
        {
            return xBanhRang() + NUT_CANH / 2;
        }

        private static int xBanhRang()
        {
            return GameScr.mepPhaiKhungNguoiChoi + NUT_CANH / 2 + 4;
        }


        // Da bo nut HUD "Trang bi".
        //
        // No tung la cua thu hai vao man trang bi kieu moi, dat canh Boss. Gio
        // nut menu tren HUD (GameScr.actMenu) da mo thang man do, nen hai nut lam
        // cung mot viec — bo cai nay cho hang nut gon lai.

        /// <summary>Có được vẽ hàng nút HUD lúc này không.</summary>
        /// <remarks>
        /// Đang mở bảng, khung chat hay menu thì giấu đi: ba nút này nằm đè lên
        /// mọi thứ, để nguyên là che mất nội dung phía dưới.
        /// </remarks>
        private static bool choVeNutHud()
        {
            return GameCanvas.isTouch && !GameCanvas.panel.isShow
                    && !ChatTextField.gI().isShow && !GameCanvas.menu.showMenu;
        }

        /// <summary>
        /// Nut tron cua hang HUD, ve bang ma thay cho anh imgNut/imgNutF.
        /// </summary>
        /// <remarks>
        /// <para>Ve bang ma vi anh chi co mot co: doi kich thuoc la re nhoe, va
        /// doi mau thi phai xuat lai anh. Ve bang ma thi net o moi muc zoom.</para>
        ///
        /// <para>Giu nguyen TAM va DUONG KINH cua anh cu de phan bat cham khong
        /// phai sua theo — chi doi cach ve.</para>
        /// </remarks>
        private static void veNutTron(mGraphics g, int tamX, int tamY, int co,
                bool dangBam)
        {
            // Mot ban ve duy nhat cho moi nut tron cua HUD — xem
            // GameScr.veNutTronHud. Giu ham nay lam ten goi quen thuoc cho
            // chuoi cho goi ben duoi.
            GameScr.veNutTronHud(g, tamX, tamY, co, dangBam);
        }

        private void veNutHud(mGraphics g)
        {
            if (!choVeNutHud())
            {
                return;
            }
            // Ve canh giua nhu hai nut kia: truoc day banh rang ve theo goc tren
            // trai nen tam cua no lech sang trai, ba nut nhin khong cach deu.
            if (mScreen.keyTouch == 999)
            {
                g.setColor(MAU_LOE, 0.35f);
                g.fillRect(xBanhRang() - NUT_CANH / 2, TAM_Y - NUT_CANH / 2,
                        NUT_CANH, NUT_CANH, 20);
            }
            SmallImage.drawSmallImage(g, 4387, xBanhRang(), TAM_Y,
                    0, mGraphics.VCENTER | mGraphics.HCENTER);

            veNutBaGach(g);
        }

        /// <summary>Toạ độ tâm nút ba gạch — góc trên phải.</summary>
        private static int xBaGach()
        {
            return GameCanvas.w - 22;
        }

        /// <summary>Mã chạm riêng của nút ba gạch.</summary>
        private const int CHAM_BA_GACH = 995;

        /// <summary>
        /// Nút ba gạch: cửa duy nhất vào menu tổng.
        /// </summary>
        /// <remarks>
        /// <para>Vẽ tay ba vạch bo góc trên một ô vuông tối, không dùng ảnh: kho
        /// icon của máy chủ không có hình nào như vậy, và vẽ tay thì nét ở mọi
        /// mức zoom — khỏi phải làm bốn bản PNG cho x1..x4.</para>
        ///
        /// <para>Ba nút Sự kiện / Phúc lợi / Boss trước đây nằm ở đây đã chuyển
        /// vào trong popup, nên hàng nút HUD không dài ra theo số màn phụ nữa.</para>
        /// </remarks>
        private void veNutBaGach(mGraphics g)
        {
            int x = xBaGach();
            int nua = NUT_CANH / 2;
            bool dangBam = mScreen.keyTouch == CHAM_BA_GACH;

            // Nen: o vuong bo goc, toi va hoi trong -> noi ro tren moi nen map.
            //
            // Khong ve vach vien tren/duoi nua: hai vach do cat ngang het be
            // ngang nut nen doc ra nhu hai duong ke lac cho, khong ra vien.
            g.setColor(0x2A211A, dangBam ? 0.92f : 0.72f);
            g.fillRect(x - nua, TAM_Y_BA_GACH - nua, NUT_CANH, NUT_CANH, 8);

            // Ba vach.
            int rongVach = 16;
            int caoVach = 2;
            int buoc = 6;
            g.setColor(dangBam ? 0xFFFFFF : 0xF7E3C6, 1f);
            for (int i = -1; i <= 1; i++)
            {
                g.fillRect(x - rongVach / 2,
                        TAM_Y_BA_GACH + i * buoc - caoVach / 2,
                        rongVach, caoVach, 1);
            }
        }


        /// <summary>Bắt chạm cho hai nút mới. Trả true nếu đã nuốt sự kiện.</summary>
        /// <summary>Bat cham vao nut ba gach.</summary>
        /// <remarks>
        /// <para>Ba vung cham cho Su kien / Phuc loi / Boss truoc day nam o day
        /// da bo: ba nut do chuyen vao popup nen <c>veNutIcon</c> khong con ai
        /// goi, tuc la ba vung cham do VO HINH — bam vao khoang trong o tren man
        /// hinh lai mo mot man hinh nao do.</para>
        ///
        /// <para>Bat buoc goi <c>clearAllPointerEvent()</c> khi nhan cham:
        /// trong <c>GameScr.updateKey()</c>, <c>checkClick()</c> chay SAU ham
        /// nay va doc lai cham chua ai xoa thanh "cham vao map" — ket qua la
        /// bam nut xong nhan vat chay tơi cho vua bam. Ham do xoa dung ba thu
        /// ma <c>checkClick()</c> doc: <c>isPointerJustRelease</c>,
        /// <c>lastSingleClick</c> va <c>isPointerDowning</c>.</para>
        /// </remarks>
        private bool chamNutHud()
        {
            if (!choVeNutHud())
            {
                return false;
            }
            // LAN CHUOT tren bang Nhiem vu.
            //
            // Xet truoc phan nha ngon: banh xe khong sinh ra su kien nha ngon
            // nao, de sau cac nhanh kia thi khong bao gio toi luot.
            int[] oNV2 = GameScr.oBangNhiemVu;
            if (oNV2 != null && oNV2.Length == 4 && GameCanvas.pXYScrollMouse != 0
                    && GameCanvas.pxMouse >= oNV2[0]
                    && GameCanvas.pxMouse <= oNV2[0] + oNV2[2]
                    && GameCanvas.pyMouse >= oNV2[1]
                    && GameCanvas.pyMouse <= oNV2[1] + oNV2[3])
            {
                GameScr.cuonNhiemVu += GameCanvas.pXYScrollMouse > 0 ? -1 : 1;
                return true;
            }
            // Hai nut cuon cua bang Nhiem vu.
            int[] oLenNV = GameScr.oCuonNhiemVuLen;
            if (oLenNV != null && oLenNV.Length == 4 && !GameCanvas.isPointerMove
                    && GameCanvas.isPointerHoldIn(oLenNV[0], oLenNV[1],
                            oLenNV[2], oLenNV[3]))
            {
                if (GameCanvas.isPointerJustRelease)
                {
                    GameCanvas.clearAllPointerEvent();
                    GameScr.cuonNhiemVu--;
                }
                return true;
            }
            int[] oXuongNV = GameScr.oCuonNhiemVuXuong;
            if (oXuongNV != null && oXuongNV.Length == 4 && !GameCanvas.isPointerMove
                    && GameCanvas.isPointerHoldIn(oXuongNV[0], oXuongNV[1],
                            oXuongNV[2], oXuongNV[3]))
            {
                if (GameCanvas.isPointerJustRelease)
                {
                    GameCanvas.clearAllPointerEvent();
                    GameScr.cuonNhiemVu++;
                }
                return true;
            }
            // Dau thu gon cua bang Nhiem vu.
            //
            // Vung bam do CHINH cho ve ghi ra (GameScr.oThuGonNhiemVu), khong
            // tinh lai o day: bang co the tut len tut xuong theo chieu cao cua
            // nhung thu ve truoc no, dat so cung la bam truot ngay.
            int[] oNV = GameScr.oThuGonNhiemVu;
            if (oNV != null && oNV.Length == 4 && !GameCanvas.isPointerMove
                    && GameCanvas.isPointerHoldIn(oNV[0] - 3, oNV[1] - 3,
                            oNV[2] + 6, oNV[3] + 6))
            {
                // Chi hoi isPointerJustRelease, KHONG doi isPointerClick.
                // Co isPointerClick khong phai luc nao cung bat cho mot cu
                // bam thuong; TuiUI khong dung no va bat cham luon an.
                if (GameCanvas.isPointerJustRelease)
                {
                    GameCanvas.clearAllPointerEvent();
                    GameScr.thuGonNhiemVu = !GameScr.thuGonNhiemVu;
                }
                return true;
            }
            // Bam vao chan dung o goc tren ben trai thi mo man hanh trang.
            //
            // Vung nay trung dung o ve chan dung trong GameScr.veKhungNguoiChoi
            // (x=4, y=3, canh 34). Hai cho dat so rieng nen doi mot ben la lech;
            // doi ben kia thi phai doi ca hai.
            int[] oCD = GameScr.oChanDung;
            if (oCD != null && oCD.Length == 4 && !GameCanvas.isPointerMove
                    && GameCanvas.isPointerHoldIn(oCD[0], oCD[1], oCD[2], oCD[3]))
            {
                if (GameCanvas.isPointerJustRelease)
                {
                    GameCanvas.clearAllPointerEvent();
                    TuiUI.getInstance().moRa();
                }
                return true;
            }
            if (!GameCanvas.isPointerMove
                    && GameCanvas.isPointerHoldIn(xBaGach() - NUT_CANH / 2,
                            TAM_Y_BA_GACH - NUT_CANH / 2, NUT_CANH, NUT_CANH))
            {
                mScreen.keyTouch = CHAM_BA_GACH;
                if (GameCanvas.isPointerJustRelease && GameCanvas.isPointerClick)
                {
                    GameCanvas.clearAllPointerEvent();
                    MenuTongUI.getInstance().moRa();
                }
                return true;
            }
            return false;
        }


        public void GUI(mGraphics g)
        {
            PlayerInfo.getInstance().paintInfoPlayer(g);
            Boss.getInstance().PaintBossInfo(g);
            veDemNguocMayDam(g);
            ListChars.getInstance().paintPlayerMap(g);
            if (GameScr.isAnalog == 1)
            {
                if (GameCanvas.isTouch && !GameCanvas.panel.isShow && !ChatTextField.gI().isShow && !GameCanvas.menu.showMenu)
                {
                    {
                        // Ve icon cua DUNG cai bong tai phim f se dung, lay tu
                        // cung mot ham tim. Truoc day phan ve va phan bam do
                        // theo hai danh sach roi nhau, nen icon hien cap 1 ma
                        // bam lai dung cap 2.
                        Item item = itemBongTai();
                        if (item != null)
                        {
                            Small img = SmallImage.imgNew[item.template.iconID];
                            if (img == null)
                            {
                                SmallImage.createImage(item.template.iconID);
                            }
                            if (img != null)
                            {
                                veNutTron(g, GameScr.xTG + 20, GameScr.yTG - 20,
                                        GameScr.imgNut.getWidth(), mScreen.keyTouch == 1000);
                                g.drawImage(img.img, GameScr.xTG + 21, GameScr.yTG - 20, mGraphics.HCENTER | mGraphics.VCENTER);
                            }
                        }
                    }
                    // Da bo nut tron "doi khu" (cai vong den ghi so khu) o gan
                    // goc phai duoi.
                    //
                    // No lam dung mot viec: gui phim 'm'. Ma nut "Khu N" o goc
                    // TREN ben phai gui dung phim do, lai co chu ro rang thay
                    // vi mot con so tran. Hai nut cung mot viec thi cai nam
                    // giua vung nut cam ung chi to choan cho — ma cho o day
                    // dang phai chia cho khung chat va hai cum nut.
                    docThuGonMuiTen();
                    GameScr.veNutThuGon(g, oThuGonMuiTen(), thuGonMuiTen);
                    if (!thuGonMuiTen)
                    {
                        xJ = 20;
                        yJ = GameCanvas.h - 45 - GameScr.imgAnalog1.getHeight() - 10;
                        veNutTron(g, xJ, yJ, GameScr.imgNut.getWidth(), mScreen.keyTouch == 1002);
                        g.drawImage(GameScr.arrow5, xJ, yJ, mGraphics.HCENTER | mGraphics.VCENTER);
                        xJ += GameScr.imgNut.getWidth() + 20;
                        veNutTron(g, xJ, yJ, GameScr.imgNut.getWidth(), mScreen.keyTouch == 1003);
                        g.drawImage(GameScr.arrow4, xJ, yJ, mGraphics.HCENTER | mGraphics.VCENTER);
                        xJ += GameScr.imgNut.getWidth() + 20;
                        veNutTron(g, xJ, yJ, GameScr.imgNut.getWidth(), mScreen.keyTouch == 1004);
                        g.drawImage(GameScr.arrow, xJ, yJ, mGraphics.HCENTER | mGraphics.VCENTER);
                    }
                    // Da go: nut capsule bi ve TRUNG. GameScr da ve nut nay roi, co dieu kien
                    // (isudungCapsun4 -> icon 1088, isudungCapsun3 -> icon 1087) va thap hon 10px.
                    // Ve lai o day lam hien ra hai nut chong nhau lech 10px.
                }
            }
            // Bang nhiem vu ve SAU ba nut mui ten, de no nam tren.
            //
            // Truoc day bang duoc ve trong GameScr.paintInfoBar, tuc truoc ca
            // chuoi nay — nhiem vu dai vai buoc la bang cham toi hang nut mui
            // ten o goc trai duoi, va ba cai nut do de len mat bang.
            GameScr.veBangNhiemVu(g);
            // Da go: logo CHU BE RONG ONLINE truoc day duoc ve de len man choi game
            // o day, khong kem bat ky dieu kien nao.
            veNutHud(g);
            // Ve sau cung: hai man nay phai nam tren moi thu khac.
            VoiceConfigUI.getInstance().ve(g);
            PhucLoiUI.getInstance().ve(g);
            // Ve SAU ba man kia de popup menu nam tren cung khi vua bam mo.
            MenuTongUI.getInstance().ve(g);
            CoUI.getInstance().ve(g);
            ChonNhanhUI.getInstance().ve(g);
            TroChoiUI.getInstance().ve(g);
            SuKienUI.getInstance().ve(g);
            BossUI.getInstance().ve(g);
            TuiUI.getInstance().ve(g);
            // Hai bang nay ve SAU CUNG, sau ca TuiUI.
            //
            // Bang Noi tai mo TU BEN TRONG TuiUI (the Ky nang). Ve truoc TuiUI
            // thi TuiUI phu kin len no — bam vao o "Noi tai" thi bang co mo
            // that, nhung khong ai nhin thay gi ca.
            TanSatUI.getInstance().ve(g);
            NoiTaiUI.getInstance().ve(g);
            // Khung chat va hai hop nhap KHONG ve o day nua.
            //
            // Chuoi nay chay ben trong GameScr.paint, tuc TRUOC khi GameCanvas
            // ve bang, menu va hop thoai — nen dat o day la ba thu tren cung
            // lai bi cac bang do de len. Chuyen han xuong cuoi GameCanvas.paint
            // (xem cho do) de chung nam tren moi thu, dung thu tu:
            // khung chat truoc, roi toi hai hop nhap.
        }
        /// <summary>
        /// Phím dành cho màn phụ đang mở.
        /// </summary>
        /// <remarks>
        /// <c>KeyPressed</c> nuốt sạch phím khi có màn phụ, mà nó cũng chỉ được
        /// gọi lúc <b>không</b> có màn phụ nào. Nên màn phụ muốn nhận phím thì
        /// phải có một đường riêng — đây là đường đó.
        /// </remarks>
        public void PhimManPhu()
        {
            TuiUI.getInstance().nhanPhim();
        }

        public void KeyPressed(int keyCode)
        {
            // Man phu dang mo thi nuot moi phim: bam x luc dang xem bang qua ma
            // menu mod bat len chong lo la hong.
            if (coManPhuDangMo())
            {
                return;
            }
            switch (keyCode)
            {
                case 'x':
                    ClientManager.getInstance().MenuClient();
                    break;
                case 't':
                    // Mo BANG CHON kieu tan sat thay vi bat/tat thang.
                    //
                    // Cong tac cu chi lam duoc mot viec: danh moi thu trong
                    // tam. Cac lua chon tinh hon — chi danh vai loai, ne sieu
                    // quai, co danh nguoi hay khong — van nam trong ma
                    // (Mobs.TypeMobsTanSat, neSieuQuai, tsPlayer) nhung khong
                    // co duong nao tu trong game cham toi. Bang nay la duong do,
                    // va no van co nut bat/tat ngay dong dau.
                    TanSatUI.getInstance().mo();
                    break;
                case 'j':
                    MapController.getInstance().NextMap(0);
                    break;
                case 'k':
                    MapController.getInstance().NextMap(2);
                    break;
                case 'l':
                    MapController.getInstance().NextMap(1);
                    break;
                case 'c':
                    if (Utils.findItemBag(193))
                    {
                        Utils.UseItem(193);
                    }
                    else if (Utils.findItemBag(194))
                    {
                        Utils.UseItem(194);
                    }
                    else
                    {
                        GameScr.info1.addInfo("Không Có Capsule", 0);
                    }
                    break;
                case 'f':
                    {
                        int idBongTai = timBongTai();
                        if (idBongTai > 0)
                        {
                            Utils.UseItem(idBongTai);
                        }
                        else
                        {
                            GameScr.info1.addInfo("Không Có Bông Tai", 0);
                        }
                    }
                    break;
                case 'm':
                    Service.gI().openUIZone();
                    break;
                case 'e':
                    perform(1, null);
                    break;
                // 'a' gio la phim di chuyen sang trai (WASD), nen Tu Danh doi
                // sang 'q'. Lenh chat "ak" van giu nguyen.
                case 'q':
                    perform(2, null);
                    break;
                case 'g':
                    bool flag10 = Char.myCharz().charFocus == null;
                    if (flag10)
                    {
                        GameScr.info1.addInfo("Vui Lòng Chọn Mục Tiêu!", 0);
                    }
                    else
                    {
                        Service.gI().giaodich(0, Char.myCharz().charFocus.charID, -1, -1);
                        GameScr.info1.addInfo("Đã Gửi Lời Mời Giao Dịch Đến: " + Char.myCharz().charFocus.cName, 0);
                    }
                    break;
            }
        }
        private void MenuClient()
        {
            string[] listIndex;
            listIndex = new string[]
            { 
                "Bản đồ",
                "Hồi Sinh",
                "Tự Đánh",
                "Tàn Sát",
                "Tàn Sát Người",
                "Auto Up Đệ",
                "Thông Báo BOSS",
                "D.s Nhân Vật",
                "Auto Nhặt",
                "Giảm Đồ Họa",
                "Auto Login",
                "Nút Chuyển Tab",
                "Cấu hình Voice"
            };
            // Ma hanh dong di CAP voi ten muc. Truoc day ma lay theo vi tri
            // (i + 1), nen chen mot muc vao giua la moi muc phia sau chay
            // sang chuc nang khac ma khong bao gi ca.
            // Ba kenh voice khong con muc rieng trong menu: doi kenh nam trong hop
            // cau hinh. Ma 13/14/15 van giu vi lenh chat vk/vm/vb goi thang toi.
            // Ma 18 = mo bang chon kieu tan sat (thay cho ma 3 bat/tat thang).
            // Ma 3 van giu cho cho nao goi thang toi no.
            //
            // 18 chu khong phai 17: 17 da la nut bat/tat am thanh trong cung
            // switch cua perform().
            int[] maHanhDong = { 11, 1, 2, 18, 12, 4, 5, 6, 7, 8, 9, 10, 16 };
            MyVector myVector = new MyVector();
            for(int i = 0; i < listIndex.Length && i < maHanhDong.Length; i++)
            {
                myVector.addElement(new Command(listIndex[i], this, maHanhDong[i], null));
            }
            GameCanvas.menu.startAt(myVector, 0);
        }
        /// <summary>Trần tốc độ game. Trên mức này thì máy chủ và client lệch nhau.</summary>
        /// <remarks>
        /// Máy chủ mới là bên chốt vị trí và sát thương. Chạy nhanh gấp mấy lần
        /// thì client gửi lệnh dày hơn máy chủ chịu nhận, và những gì vượt quá
        /// bị bỏ — nhìn ra là nhân vật giật về sau, đấm không ăn. Gấp đôi là mức
        /// còn chạy trơn.
        /// </remarks>
        private const float TOC_DO_TOI_DA = 2f;

        private static float kepTocDo(float x)
        {
            if (x < 0.25f)
            {
                return 0.25f;   // 0 la game dung han, khong con duong go lenh
            }
            return x > TOC_DO_TOI_DA ? TOC_DO_TOI_DA : x;
        }

        /// <summary>
        /// Lệnh <c>tocdo</c> — đổi tốc độ chạy và số khung hình mỗi giây.
        /// </summary>
        /// <remarks>
        /// <para>Gõ <c>tocdo</c> để xem đang ở mức nào, <c>tocdo 1.5</c> để đổi,
        /// <c>tocdo 1</c> để về bình thường.</para>
        ///
        /// <para>Đổi <b>cả hai</b> thứ: <c>Time.timeScale</c> là tốc độ chạy của
        /// mọi thứ trong game, còn <c>targetFrameRate</c> là số khung hình mỗi
        /// giây. Chỉ tăng timeScale mà giữ nguyên khung hình thì mọi thứ nhanh
        /// lên nhưng giật hơn — nhìn còn tệ hơn lúc đầu.</para>
        ///
        /// <para>Đây là việc chạy <b>ở máy người chơi</b>, không hỏi máy chủ. Nên
        /// nó không cho lợi thế gì: sát thương, tiềm năng, hồi chiêu đều do máy
        /// chủ đếm bằng đồng hồ của máy chủ.</para>
        /// </remarks>
        private void doiTocDo(string text)
        {
            string[] phan = text.Split(' ');
            if (phan.Length < 2 || phan[1].Trim().Length == 0)
            {
                GameScr.info1.addInfo("Tốc độ: x" + Time.timeScale
                        + " — gõ 'tocdo 1.5' để đổi (0.25 đến "
                        + TOC_DO_TOI_DA + ")", 0);
                return;
            }
            float x;
            if (!float.TryParse(phan[1].Trim().Replace(',', '.'),
                    System.Globalization.NumberStyles.Float,
                    System.Globalization.CultureInfo.InvariantCulture, out x))
            {
                GameScr.info1.addInfo("Phải là một con số, ví dụ 1.5", 0);
                return;
            }
            x = kepTocDo(x);
            Time.timeScale = x;
            // 60 khung la muc goc; nhan theo toc do va kep o 120 — cao hon thi
            // man hinh cua may khong theo kip, chi ton pin.
            int khung = (int)(60f * x);
            if (khung > 120)
            {
                khung = 120;
            }
            if (khung < 30)
            {
                khung = 30;
            }
            Application.targetFrameRate = khung;
            GameScr.info1.addInfo("Tốc độ game: x" + x + " (" + khung + " khung/giây)", 0);
        }

        public bool Chat(string text)
        {
            if (text == "tocdo" || text.StartsWith("tocdo "))
            {
                doiTocDo(text);
                return true;
            }
            if(text.StartsWith("cheat "))
            {
                // Kep lai trong khoang chay duoc.
                //
                // Ban cu nhan thang so nguoi choi go vao Time.timeScale, khong
                // chan gi ca: go "cheat 0" la game DUNG HAN va khong con duong
                // nao go lenh khac de mo lai; go mot so am thi Unity chay lui
                // dong ho va moi thu tinh theo thoi gian deu hong.
                int cheat;
                if (!int.TryParse(text.Split(' ')[1], out cheat))
                {
                    GameScr.info1.addInfo("Phải là một con số", 0);
                    return true;
                }
                Time.timeScale = kepTocDo(cheat);
                GameScr.info1.addInfo("Tốc độ game: x" + Time.timeScale, 0);
                return true;
            }
            if(text.StartsWith("k "))
            {
                int zone = int.Parse(text.Split(' ')[1]);
                Service.gI().requestChangeZone(zone, -1);
                return true;
            }
            switch (text)
            {
                case "ahs":
                    perform(1, null);
                    return true;
                case "alogin":
                    perform(9, null);
                    return true;
                case "ak":
                    perform(2, null);
                    return true;
                case "gdh":
                    perform(8, null);
                    return true;
                case "tbb":
                    perform(5, null);
                    return true;
                case "dsnv":
                    perform(6, null);
                    return true;
                case "adt":
                    perform(4, null);
                    return true;
                case "ts":
                    // Mo bang chon, khong bat/tat thang nua — xem TanSatUI.
                    perform(18, null);
                    return true;
                case "ts!":
                    // Duong tat cho ai chi muon bat/tat that nhanh, khong qua
                    // bang chon. Giu lai vi thoi quen cu la go "ts" roi go tiep.
                    perform(3, null);
                    return true;
                case "tsn":
                    perform(12, null);
                    return true;
                case "vk":
                    perform(13, null);
                    return true;
                case "vm":
                    perform(14, null);
                    return true;
                case "vb":
                    perform(15, null);
                    return true;
                case "voice":
                    perform(16, null);
                    return true;
            }
            return false;
        }
        /// <summary>
        /// Chức năng mang mã này đang bật hay tắt.
        /// </summary>
        /// <remarks>
        /// <para>Đặt ngay cạnh <see cref="perform"/> là có ý: mã nào bật/tắt được
        /// thì trạng thái của nó phải đọc được ở cùng một chỗ. Tách hai nơi thì
        /// thêm một chức năng mới rất dễ chỉ sửa một bên, và màn hình sẽ vẽ ô đánh
        /// dấu sai mà không có gì báo.</para>
        ///
        /// <para>Mã không phải loại bật/tắt (mở bản đồ, mở hộp cấu hình) trả về
        /// <c>false</c>; bên vẽ tự biết những mã đó không có ô đánh dấu.</para>
        /// </remarks>
        public static bool trangThaiMod(int idAction)
        {
            switch (idAction)
            {
                case 1:
                    return Revive.getInstance().getRevive();
                case 2:
                    return nSkill.getInstance().canAttack;
                case 3:
                    return Mobs.IsTanSat;
                case 4:
                    return PetService.getInstance().getUp();
                case 5:
                    return Boss.getInstance().isShow;
                case 6:
                    return ListChars.getInstance().isShow;
                case 7:
                    return Mobs.IsAutoPickItems;
                case 8:
                    return ListChars.getInstance().HideMap;
                case 9:
                    return PlayerInfo.getInstance().canLogin;
                case 12:
                    return Mobs.tsPlayer;
                case 17:
                    // Am thanh cua ca game. Doc thang co cua game chu khong giu
                    // mot ban rieng: giu ban rieng thi tat/bat o cho khac (menu
                    // cau hinh cu) la o danh dau nay hien sai.
                    return GameCanvas.isPlaySound;
                default:
                    return false;
            }
        }

        public void perform(int idAction, object p)
        {
            switch (idAction)
            {
                case 1:
                    Revive.getInstance().setRevive();
                    Utils.addInfo1("Tự Động Hồi Sinh", Revive.getInstance().getRevive());
                    break;
                case 2:
                    nSkill.getInstance().canAttack =! nSkill.getInstance().canAttack;
                    Utils.addInfo1("Tự Đánh", nSkill.getInstance().canAttack);
                    break;
                case 3:
                    Mobs.IsTanSat = !Mobs.IsTanSat;
                    if (Mobs.IsTanSat)
                    {
                        Mobs.tsPlayer = false;
                    }
                    Utils.addInfo1("Tàn Sát", Mobs.IsTanSat);
                    break;
                case 16:
                    VoiceConfigUI.getInstance().mo();
                    break;
                case 18:
                    // Mo bang chon kieu tan sat. Xem TanSatUI de biet vi sao
                    // tach ra khoi cong tac bat/tat o ma 3.
                    //
                    // Ma 18 chu khong phai 17: 17 da la nut bat/tat am thanh.
                    TanSatUI.getInstance().mo();
                    break;
                case 13:
                    VoiceChat.doiKenh(VoiceChat.KENH_KHU);
                    break;
                case 14:
                    VoiceChat.doiKenh(VoiceChat.KENH_MAP);
                    break;
                case 15:
                    VoiceChat.doiKenh(VoiceChat.KENH_BANG);
                    break;
                case 12:
                    // Chi danh boss va nguoi choi. Hai che do loai tru nhau:
                    // nhanh tan sat thuong nham MOI quai nen bat ca hai thi
                    // che do nay khong bao gio den luot chay.
                    Mobs.tsPlayer = !Mobs.tsPlayer;
                    if (Mobs.tsPlayer)
                    {
                        Mobs.IsTanSat = false;
                    }
                    Utils.addInfo1("Tàn Sát Người", Mobs.tsPlayer);
                    break;
                case 4:
                    PetService.getInstance().setUp();
                    Utils.addInfo1("Auto Up Đệ", PetService.getInstance().getUp());
                    break;
                case 5:
                    Boss.getInstance().isShow =! Boss.getInstance().isShow;
                    Utils.addInfo1("Thông báo BOSS", Boss.getInstance().isShow);
                    break;
                case 6:
                    ListChars.getInstance().isShow = !ListChars.getInstance().isShow;
                    Utils.addInfo1("D.s Nhân Vật", ListChars.getInstance().isShow);
                    break;
                case 7:
                    Mobs.IsAutoPickItems =!Mobs.IsAutoPickItems;
                    Utils.addInfo1("Auto Nhặt", Mobs.IsAutoPickItems);
                    break;
                case 8:
                    ListChars.getInstance().HideMap = !ListChars.getInstance().HideMap;
                    Utils.addInfo1("Giảm Đồ Họa", ListChars.getInstance().HideMap);
                    break;
                // Muc "Ban do": may chu ve menu chon khu vuc roi chon ban do,
                // vi danh sach nam trong CSDL (bang map_nhanh) — client khong
                // biet truoc co nhung ban do nao.
                case 11:
                    Service.gI().chat("bando");
                    break;
                case 9:
                    PlayerInfo.getInstance().canLogin =!PlayerInfo.getInstance().canLogin;
                    Utils.addInfo1("Auto Login", PlayerInfo.getInstance().canLogin);
                    break;
                case 17:
                    // Goi thang ham co san cua game chu khong tu dao co.
                    //
                    // soundToolOption() lam ba viec, thieu cai nao cung hong:
                    // dao co, NAP hoac DONG nhac nen theo map dang dung, va GHI
                    // lua chon vao bo nho may (Rms "isPlaySound") de lan sau mo
                    // game van tat. Chi lat GameCanvas.isPlaySound thi nhac dang
                    // phat van phat tiep, va tat game la mat lua chon.
                    SoundMn.gI().soundToolOption();
                    Utils.addInfo1("Âm thanh", GameCanvas.isPlaySound);
                    break;
            }
        }
    }
}
