
namespace Game2
{
    public class Menu
    {
    	public bool showMenu;
    
    	public MyVector menuItems;
    
    	public int menuSelectedItem;
    
    	public int menuX;
    
    	public int menuY;
    
    	public int menuW;
    
    	public int menuH;
    
    	public static int[] menuTemY;
    
    	public static int cmtoX;
    
    	public static int cmx;
    
    	public static int cmdy;
    
    	public static int cmvy;
    
    	public static int cmxLim;
    
    	public static int xc;
    
    	private Command left = new Command(mResources.SELECT, 0);
    
    	private Command right = new Command(mResources.CLOSE, 0, GameCanvas.w - 71, GameCanvas.h - mScreen.cmdH + 1);
    
    	private Command center;
    
    	public static Image imgMenu1;
    
    	public static Image imgMenu2;
    
    	private bool disableClose;
    
    	public int tDelay;
    
    	public int w;
    
    	private int pa;
    
    	private bool trans;
    
    	private int pointerDownTime;
    
    	private int pointerDownFirstX;
    
    	private int[] pointerDownLastX = new int[3];
    
    	private bool pointerIsDowning;
    
    	private bool isDownWhenRunning;
    
    	private bool wantUpdateList;
    
    	private int waitToPerform;
    
    	private int cmRun;
    
    	private bool touch;
    
    	private bool close;
    
    	private int cmvx;
    
    	private int cmdx;
    
    	private bool isClose;
    
    	public bool[] isNotClose;

    	// ------------------------------------------------------------------
    	//  Cuon ngang khi dai nut dai hon man hinh
    	// ------------------------------------------------------------------

    	/// <summary>Độ cuộn ngang hiện tại, tính bằng điểm.</summary>
    	private int cuonX;

    	/// <summary>Cuộn xa nhất — 0 nghĩa là cả dải vừa màn hình.</summary>
    	private int cuonToiDa;

    	/// <summary>Toạ độ x của mục đầu khi chưa cuộn.</summary>
    	private int xGocMenu;

    	/// <summary>Vùng nhìn thấy của cả dải nút.</summary>
    	private int xDai;
    	private int rongDai;

    	/// <summary>Đang giữ ngón/chuột để kéo dải nút.</summary>
    	private bool dangKeoMenu;
    	private int xBatKeo;
    	private int cuonLucBatKeo;

    	/// <summary>
    	/// Đã kéo thật sự chứ không phải chạm rồi nhả tại chỗ.
    	/// </summary>
    	/// <remarks>
    	/// Chỉ khi kéo thật mới nuốt cái nhả ngón. Nuốt cả những lần chạm không
    	/// nhúc nhích thì bấm chọn mục sẽ không bao giờ ăn — đúng lỗi đã gặp ở
    	/// dải thẻ khung chat.
    	/// </remarks>
    	private bool daKeoThat;
    
    	public static void loadBg()
    	{
    		imgMenu1 = GameCanvas.loadImage("/mainImage/myTexture2dbtMenu1.png");
    		imgMenu2 = GameCanvas.loadImage("/mainImage/myTexture2dbtMenu2.png");
    	}
    
    	public void startWithoutCloseButton(MyVector menuItems, int pos)
    	{
    		startAt(menuItems, pos);
    		disableClose = true;
    	}
    
    	/// <summary>Khoang chua duoi day man hinh.</summary>
    	/// <summary>
    	/// Khoảng hở giữa hàng nút và mép dưới màn hình.
    	/// </summary>
    	/// <remarks>
    	/// Trước đây là 6 điểm cố định — hàng nút gần như dán vào mép dưới, và ô chi
    	/// tiết vật phẩm (tự đặt mình ngay trên hàng nút) vì thế cũng bị dồn xuống
    	/// đáy, trông như chui gầm màn hình.
    	///
    	/// Lấy theo chiều cao màn hình chứ không đặt cứng: màn cao thì hở nhiều, màn
    	/// thấp thì hở ít, chỗ nào cũng cân. Chặn hai đầu để màn quá nhỏ không mất
    	/// chỗ và màn quá cao không đẩy cụm lên giữa mặt.
    	///
    	/// Ô chi tiết quá cao thì <c>Panel.popUpDetailInit</c> tự đẩy cả cụm xuống
    	/// lại (vòng <c>while (cp.cy &lt; 10)</c>), nên nâng số này không làm ô chi
    	/// tiết trôi khỏi mép trên.
    	/// </remarks>
    	private static int leDuoi()
    	{
    		int le = GameCanvas.h / 12;
    		if (le < 10)
    		{
    			le = 10;
    		}
    		if (le > 56)
    		{
    			le = 56;
    		}
    		return le;
    	}

    	/// <summary>Le trai/phai ben trong mot nut.</summary>
    	private const int LE_TRONG = 12;

    	/// <summary>Chan va tran be rong cua mot nut.</summary>
    	private const int RONG_TOI_THIEU = 78;
    	private const int RONG_TOI_DA = 180;

    	/// <summary>
    	/// Hẹp hơn mức này thì nhãn nút cụt tới mức đọc không ra.
    	/// </summary>
    	/// <remarks>
    	/// Nút chỉ ép nhỏ tới đây thôi. Còn thiếu chỗ thì cả dải cuộn ngang, chứ
    	/// không bóp tiếp — hai nút cùng bị cắt thành một chữ giống nhau thì bấm
    	/// nhầm là chuyện sớm muộn.
    	/// </remarks>
    	private const int RONG_DOC_DUOC = 62;

    	/// <summary>So dong chu toi da tren mot nut.</summary>
    	private const int DONG_TOI_DA = 2;

    	// Bang mau: vien vang, long nau, giong tong mau go cua game.
    	private const int MAU_VIEN = 0xC8933C;
    	private const int MAU_VIEN_CHON = 0xFFDE86;
    	private const int MAU_THAN_TREN = 0x8C5F27;
    	private const int MAU_THAN_DUOI = 0x5C3D13;
    	private const int MAU_THAN_TREN_CHON = 0xC98B3A;
    	private const int MAU_THAN_DUOI_CHON = 0x8A551B;

    	/// <summary>
    	/// Khung o chi tiet vat pham dang mo, hoac <c>null</c> neu khong co.
    	/// </summary>
    	/// <remarks>
    	/// O chi tiet la mot <c>ChatPopup</c> do bang giu trong truong <c>cp</c>.
    	/// Co no thi menu lua chon duoc dat NGAY DUOI o do, de "xem mon gi" va
    	/// "lam gi voi mon do" nam canh nhau. Khong co thi menu ve day man hinh
    	/// nhu cu — do la truong hop menu cua NPC.
    	/// </remarks>
    	private static int[] oChiTietVatPham()
    	{
    		ChatPopup cp = null;
    		if (GameCanvas.panel != null && GameCanvas.panel.isShow)
    		{
    			cp = GameCanvas.panel.cp;
    		}
    		if (cp == null && GameCanvas.panel2 != null && GameCanvas.panel2.isShow)
    		{
    			cp = GameCanvas.panel2.cp;
    		}
    		if (cp == null || cp.says == null || cp.says.Length == 0
    				|| cp.sayWidth <= 0 || cp.ch <= 0)
    		{
    			return null;
    		}
    		return new int[] { cp.cx, cp.cy, cp.sayWidth, cp.ch };
    	}

    	/// <summary>Cao mot nut o khung hinh nay — doi theo so dong chu.</summary>
    	private int caoNut = 30;

    	/// <summary>
    	/// Tinh be rong, be cao va cho dat cua menu theo NOI DUNG that.
    	/// </summary>
    	/// <remarks>
    	/// <para>Ngat dong phai lam SAU khi da biet be rong that. Ban truoc ngat
    	/// dong luc <c>menuW</c> con la 60 tam thoi roi mai sau moi dat be rong
    	/// that — nen chu bi vo doi som, hoac mot tu dai hon 60 diem thi tran han
    	/// ra ngoai nut. Do dung la canh "Cat vao hanh trang" bi tran chu.</para>
    	///
    	/// <para>Chay lai MOI KHUNG HINH chu khong chi mot lan luc mo: o chi tiet
    	/// vat pham co the duoc dat sau khi menu da mo, va man hinh doi co bat cu
    	/// luc nao.</para>
    	/// </remarks>
    	private void tinhBoCucMenu()
    	{
    		int n = (menuItems == null) ? 0 : menuItems.size();
    		if (n <= 0)
    		{
    			return;
    		}
    		mFont f = mFont.tahoma_7b_white;

    		// 1. Be rong lay theo muc dai nhat.
    		int rong = RONG_TOI_THIEU;
    		for (int i = 0; i < n; i++)
    		{
    			Command c = (Command)menuItems.elementAt(i);
    			if (c == null || c.caption == null)
    			{
    				continue;
    			}
    			int w = f.getWidth(c.caption) + LE_TRONG * 2;
    			if (w > rong)
    			{
    				rong = w;
    			}
    		}
    		if (rong > RONG_TOI_DA)
    		{
    			rong = RONG_TOI_DA;
    		}
    		// Ep cho vua man hinh, NHUNG khong ep qua muc doc duoc.
    		//
    		// Ban truoc chia thang be ngang man hinh cho so muc. Menu mod co muoi
    		// ba muc, tren dien thoai thanh moi nut ba muoi may diem — "Tan Sat" va
    		// "Tan Sat Nguoi" deu cut thanh "Tan Sat", nhin ra hai nut giong het
    		// nhau ma lam hai viec khac han.
    		//
    		// Gio nut giu be rong con doc duoc, va neu ca dai dai hon man hinh thi
    		// CUON NGANG — xem cuonToiDa duoi day.
    		int vua = (GameCanvas.w - 8) / n;
    		if (rong > vua)
    		{
    			rong = vua;
    		}
    		if (rong < RONG_DOC_DUOC)
    		{
    			rong = RONG_DOC_DUOC;
    		}
    		menuW = rong;

    		// 2. Ngat dong theo be rong THAT.
    		int soDong = 1;
    		for (int i = 0; i < n; i++)
    		{
    			Command c = (Command)menuItems.elementAt(i);
    			if (c == null || c.caption == null)
    			{
    				continue;
    			}
    			c.subCaption = f.splitFontArray(c.caption, menuW - LE_TRONG);
    			if (c.subCaption != null && c.subCaption.Length > soDong)
    			{
    				soDong = c.subCaption.Length;
    			}
    		}
    		if (soDong > DONG_TOI_DA)
    		{
    			soDong = DONG_TOI_DA;
    		}
    		// Cao = le tren duoi + so dong chu. Le rong tay cho nut day dan, khong
    		// phai mot vien mong om sat chu.
    		caoNut = 26 + soDong * 13;
    		menuH = caoNut;

    		// 3. Cho dat.
    		//
    		// CHIEU DOC luon lay tu day man hinh, KHONG lay theo o chi tiet.
    		//
    		// O chi tiet tu dat minh ngay tren menu (`cp.cy = menu.menuY - cp.ch`
    		// trong Panel). Neu menu lai bam theo `cp.cy` thi hai cai dinh nghia
    		// lan nhau: moi khung hinh menu tut xuong mot it, o chi tiet tut theo,
    		// va chi vai giay la ca hai troi khoi man hinh. Mot chieu phu thuoc
    		// thi khong bao gio co vong do.
    		//
    		// CHIEU NGANG thi bam theo o chi tiet: nho the ma nut lua chon nam
    		// thang duoi o vua xem, thay vi o chi tiet nam ben trai con nut lai
    		// nam giua man hinh — hai thu cua cung mot viec ma o hai noi.
    		int tong = n * menuW;
    		int leDuoiNay = leDuoi();
    		menuY = GameCanvas.h - menuH - leDuoiNay;
    		if (tong <= GameCanvas.w - 4)
    		{
    			// Vua man hinh: can giua nhu cu, khong co gi de cuon.
    			cuonToiDa = 0;
    			cuonX = 0;
    			int[] o = oChiTietVatPham();
    			xGocMenu = (o != null)
    					? o[0] + (o[2] - tong) / 2
    					: (GameCanvas.w - tong) / 2;
    			if (xGocMenu + tong > GameCanvas.w - 2)
    			{
    				xGocMenu = GameCanvas.w - 2 - tong;
    			}
    			if (xGocMenu < 2)
    			{
    				xGocMenu = 2;
    			}
    			xDai = xGocMenu;
    			rongDai = tong;
    		}
    		else
    		{
    			// Dai hon man hinh: trai cang het be ngang roi CUON.
    			xGocMenu = 2;
    			xDai = 2;
    			rongDai = GameCanvas.w - 4;
    			cuonToiDa = tong - rongDai;
    			if (cuonX > cuonToiDa)
    			{
    				cuonX = cuonToiDa;
    			}
    			if (cuonX < 0)
    			{
    				cuonX = 0;
    			}
    		}
    		menuX = xGocMenu - cuonX;
    		if (menuY + menuH > GameCanvas.h - leDuoiNay)
    		{
    			menuY = GameCanvas.h - menuH - leDuoiNay;
    		}
    		if (menuY < 2)
    		{
    			menuY = 2;
    		}
    	}

    	/// <summary>
    	/// Ban cu dat menu theo toa do cua cho goi. Ban moi BO QUA toa do do.
    	/// </summary>
    	/// <remarks>
    	/// Cho goi truyen vi tri cua o vua bam trong bang, ma o do co the nam sat
    	/// mep hoac bi bang khac de len. Dat co dinh o day man hinh thi menu luon
    	/// o mot cho quen thuoc va luon nhin thay duoc.
    	/// </remarks>
    	public void startAt(MyVector menuItems, int x, int y)
    	{
    		startAt(menuItems, 0);
    	}
    
    	/// <summary>
    	/// Menu sắp mở có <b>trùng nội dung</b> với menu đang mở không.
    	/// </summary>
    	/// <remarks>
    	/// So theo số mục và nhãn từng mục. Đủ để phân biệt "gọi lại đúng menu
    	/// đó mỗi khung hình" với "mở một menu khác".
    	/// </remarks>
    	private bool trungMenuDangMo(MyVector moi)
    	{
    		if (this.menuItems == null || moi == null
    				|| this.menuItems.size() != moi.size())
    		{
    			return false;
    		}
    		for (int i = 0; i < moi.size(); i++)
    		{
    			Command a = (Command)this.menuItems.elementAt(i);
    			Command b = (Command)moi.elementAt(i);
    			if (a == null || b == null || a.caption == null
    					|| !a.caption.Equals(b.caption))
    			{
    				return false;
    			}
    		}
    		return true;
    	}

    	public void startAt(MyVector menuItems, int pos)
    	{
    		// Menu cũ đang mở thì THAY bằng menu mới, đừng bỏ qua menu mới.
    		//
    		// Dòng cũ là `if (showMenu) return;` — nó lặng lẽ vứt menu vừa dựng
    		// đi, không báo lỗi, không log. Người chơi thấy: bấm một món trong
    		// rương mà nút "Lấy ra" không hiện; đổi sang thẻ khác rồi bấm lại thì
    		// hiện. Cùng một cửa đó dùng cho mọi màn có bảng: Bà Hạt Mít, Satan,
    		// rương ở nhà, Uron, Bunma, Appule, Dende…
    		//
    		// Chỉ cần MỘT chỗ nào đó để sót `showMenu = true` — và có hơn mười
    		// chỗ trong mã chạm vào cờ này, kể cả những chỗ chạy theo gói tin từ
    		// máy chủ — là menu tiếp theo biến mất. Vá từng chỗ gọi là không bao
    		// giờ hết; vá ở đây thì hết hẳn.
    		//
    		// Vẫn giữ đường thoát cho trường hợp gọi LẶP: nếu menu mới trùng y
    		// nội dung menu đang mở thì để nguyên. Nhờ vậy chỗ nào lỡ gọi
    		// startAt mỗi khung hình cũng không làm menu dựng lại liên tục —
    		// đúng nỗi lo đã ghi trong chú thích cũ ở Panel.
    		if (showMenu)
    		{
    			if (trungMenuDangMo(menuItems))
    			{
    				return;
    			}
    			showMenu = false;
    		}
    		isClose = false;
    		touch = false;
    		close = false;
    		tDelay = 0;
    		khungTuLucMo = 0;
    		God.NhatKy.ghi("MENU mo, so muc = "
    				+ ((menuItems == null) ? 0 : menuItems.size()));
    		// Mo bang moi thi khong khoa san. Neu nguoi choi van giu Enter tu cu bam
    		// truoc, khoi phim ben tren se khoa lai ngay o khung hinh dau tien.
    		khoaChon = false;
    		if (menuItems.size() == 1)
    		{
    			menuSelectedItem = 0;
    			Command command = (Command)menuItems.elementAt(0);
    			if (command != null && command.caption.Equals(mResources.saying))
    			{
    				command.performAction();
    				showMenu = false;
    				InfoDlg.showWait();
    				return;
    			}
    		}
    		SoundMn.gI().openMenu();
    		isNotClose = new bool[menuItems.size()];
    		for (int i = 0; i < isNotClose.Length; i++)
    		{
    			isNotClose[i] = false;
    		}
    		disableClose = false;
    		ChatPopup.currChatPopup = null;
    		Effect2.vEffect2.removeAllElements();
    		Effect2.vEffect2Outside.removeAllElements();
    		InfoDlg.hide();
    		if (menuItems.size() != 0)
    		{
    			this.menuItems = menuItems;
    			menuW = 60;
    			menuH = 60;
    			for (int j = 0; j < menuItems.size(); j++)
    			{
    				Command command2 = (Command)menuItems.elementAt(j);
    				command2.isPlaySoundButton = false;
    				int width = mFont.tahoma_7_yellow.getWidth(command2.caption);
    				command2.subCaption = mFont.tahoma_7_yellow.splitFontArray(command2.caption, menuW - 10);
    			}
    			// Dat thang o vi tri cuoi, KHONG hoat anh truot len.
    			//
    			// Ban cu dat menu o `GameCanvas.h` (duoi day man hinh) roi giao
    			// cho `updateMenu()` keo len tung khung. Chi can mot khung khong
    			// chay la menu nam ngoai man hinh — dung trang thai "dang mo" ma
    			// khong ai nhin thay, va khong co dau vet nao de lan ra.
    			menuTemY = new int[menuItems.size()];
    			tinhBoCucMenu();
    			for (int k = 0; k < menuTemY.Length; k++)
    			{
    				menuTemY[k] = menuY;
    			}
    			showMenu = true;
    			menuSelectedItem = 0;
    			cmxLim = this.menuItems.size() * menuW - GameCanvas.w;
    			if (cmxLim < 0)
    			{
    				cmxLim = 0;
    			}
    			cmtoX = 0;
    			cmx = 0;
    			xc = 50;
    			w = menuItems.size() * menuW - 1;
    			if (w > GameCanvas.w - 2)
    			{
    				w = GameCanvas.w - 2;
    			}
    			if (GameCanvas.isTouch && !Main.isPC)
    			{
    				menuSelectedItem = -1;
    			}
    		}
    	}
    
    	public bool isScrolling()
    	{
    		if ((!isClose && menuTemY[menuTemY.Length - 1] > menuY) || (isClose && menuTemY[menuTemY.Length - 1] < GameCanvas.h))
    		{
    			return true;
    		}
    		return false;
    	}
    
    	/// <summary>
    	/// Bat cham cho menu ve moi.
    	/// </summary>
    	/// <remarks>
    	/// <para>Giu nguyen hop dong cu nen 41 cho goi <c>startAt</c> khong phai
    	/// sua: chon mot muc thi dat <c>menuSelectedItem</c>, <c>touch = true</c>,
    	/// <c>close = false</c> roi goi <c>doCloseMenu()</c> — chinh ham do thuc
    	/// hien <c>command.performAction()</c>.</para>
    	///
    	/// <para>Bo qua ba khung hinh dau: cu cham MO menu van con nguyen trong
    	/// <c>isPointerJustRelease</c> khi menu bat dau chay, ma diem cham do nam
    	/// ngoai khung menu — khong chan thi menu tu dong ngay khi vua mo.</para>
    	/// </remarks>
    	/// <summary>Ma phim: trai, phai, len, xuong, va hai ma cua phim chon.</summary>
    	private const int PHIM_TRAI = 23;
    	private const int PHIM_PHAI = 24;
    	private const int PHIM_LEN = 21;
    	private const int PHIM_XUONG = 22;
    	private const int PHIM_CHON_A = 25;
    	private const int PHIM_CHON_B = 15;

    	/// <summary>Doc mot phim, va XOA no di de khong lap lai o khung sau.</summary>
    	private static bool phim(int ma)
    	{
    		if (ma < 0 || GameCanvas.keyPressed == null
    				|| ma >= GameCanvas.keyPressed.Length
    				|| !GameCanvas.keyPressed[ma])
    		{
    			return false;
    		}
    		GameCanvas.keyPressed[ma] = false;
    		return true;
    	}

    	/// <summary>Doi muc dang chon di <paramref name="buoc"/>, vong qua hai dau.</summary>
    	private void doiMucChon(int buoc)
    	{
    		int n = menuItems.size();
    		if (n <= 0)
    		{
    			return;
    		}
    		if (menuSelectedItem < 0)
    		{
    			// Ban cam ung mo menu voi muc chon = -1 (khong sang muc nao). Bam
    			// phim mui ten dau tien thi vao muc dau, khong nhay ve muc cuoi.
    			menuSelectedItem = (buoc > 0) ? 0 : n - 1;
    		}
    		else
    		{
    			menuSelectedItem = (menuSelectedItem + buoc + n) % n;
    		}
    		SoundMn.gI().panelClick();
    	}

    	/// <summary>
    	/// Bat phim mui ten, phim chon, va cham cho menu.
    	/// </summary>
    	/// <remarks>
    	/// Bo qua ba khung hinh dau (<c>khungTuLucMo</c>): cu bam MO menu con
    	/// nguyen trong hang doi, ma diem bam do nam ngoai khung menu — khong chan
    	/// thi menu tu dong ngay khi vua mo. Phim chon cung the: chinh phim vua mo
    	/// menu se lap tuc chon muc dau.
    	/// </remarks>
    	/// <summary>Điểm chạm hiện tại có nằm trong một bảng đang mở không.</summary>
    	/// <remarks>
    	/// Xét cả hai bảng: màn cửa hàng và màn NPC nâng cấp mở hai bảng cạnh
    	/// nhau, mà lưới đồ nằm ở bảng thứ hai.
    	/// </remarks>
    	private static bool chamTrongBang()
    	{
    		if (GameCanvas.panel != null && GameCanvas.panel.isShow
    				&& GameCanvas.isPointer(GameCanvas.panel.X, GameCanvas.panel.Y,
    						GameCanvas.panel.W, GameCanvas.panel.H))
    		{
    			return true;
    		}
    		return GameCanvas.panel2 != null && GameCanvas.panel2.isShow
    				&& GameCanvas.isPointer(GameCanvas.panel2.X, GameCanvas.panel2.Y,
    						GameCanvas.panel2.W, GameCanvas.panel2.H);
    	}

    	public void updateMenuKey()
    	{
    		if ((GameScr.gI().activeRongThan && GameScr.gI().isUseFreez) || !showMenu)
    		{
    			return;
    		}
    		tinhBoCucMenu();
    		if (khungTuLucMo <= SO_KHUNG_LANG)
    		{
    			// NUOT het phim dang cho, chu khong chi bo qua luot.
    			//
    			// keyPressed la co, khong phai hang doi: no bat cho toi khi CO AI
    			// DOC va xoa di. Ban truoc quang lang chi `return`, nen cu Enter mo
    			// bang NPC van nam nguyen ngoai cong, va dung khung hinh dau tien
    			// het quang lang la no chay vao chon muc dau.
    			//
    			// Vet ghi lai dung nhu vay: bang mo luc 47.592, den 47.691 —
    			// `khung tu luc mo = 7`, tuc ngay sau moc 6 — la co mot lan chon
    			// bang PHIM ma nguoi choi khong he bam. Vi the keo dai quang lang
    			// them bao nhieu cung vo ich: chi doi cho cu bam do no muon hon.
    			phim(PHIM_CHON_A);
    			phim(PHIM_CHON_B);
    			phim(PHIM_TRAI);
    			phim(PHIM_PHAI);
    			phim(PHIM_LEN);
    			phim(PHIM_XUONG);
    			return;
    		}
    		// Doc het cac ma cua cung mot phim TRUOC khi xet.
    		//
    		// Viet `phim(a) || phim(b)` thi C# ngan mach: a dung la b khong duoc
    		// goi, ma b van con trong hang doi va khung hinh sau lai kich hoat
    		// them mot lan. Voi phim chon — no dat ca hai ma 25 va 15 — dieu do
    		// thanh ra bam Enter mot cai chay hai lan.
    		bool sangTrai = phim(PHIM_TRAI);
    		if (phim(PHIM_LEN))
    		{
    			sangTrai = true;
    		}
    		bool sangPhai = phim(PHIM_PHAI);
    		if (phim(PHIM_XUONG))
    		{
    			sangPhai = true;
    		}
    		bool daChon = phim(PHIM_CHON_A);
    		if (phim(PHIM_CHON_B))
    		{
    			daChon = true;
    		}
    		// Mot lan CHON an mot lan NHA phim.
    		//
    		// Giu Enter thi Unity ban lai su kien KeyDown deu deu (nhip lap cua ban
    		// phim), moi lan lai dat keyPressed[25] va [15]. Bang NPC noi tiep nhau
    		// — chon xong may chu gui bang ke — nen mot lan giu Enter di het may
    		// bang lien, nhin ra dung nhu "mot cai Enter an hai lan".
    		//
    		// keyHold cho biet phim CON dang bi giu. Chon xong thi khoa lai, va chi
    		// mo khoa khi ca hai ma cua phim chon deu da nha ra.
    		if (khoaChon)
    		{
    			if (!GameCanvas.keyHold[PHIM_CHON_A]
    					&& !GameCanvas.keyHold[PHIM_CHON_B])
    			{
    				khoaChon = false;
    			}
    			daChon = false;
    		}
    		else if (daChon)
    		{
    			khoaChon = true;
    		}
    		if (sangTrai)
    		{
    			doiMucChon(-1);
    			cuonToiMucChon();
    			return;
    		}
    		if (sangPhai)
    		{
    			doiMucChon(1);
    			cuonToiMucChon();
    			return;
    		}
    		if (daChon)
    		{
    			God.NhatKy.ghi("MENU chon bang PHIM, muc = " + menuSelectedItem
    					+ ", khung tu luc mo = " + khungTuLucMo);
    			if (menuSelectedItem < 0)
    			{
    				menuSelectedItem = 0;
    			}
    			touch = true;
    			close = false;
    			doCloseMenu();
    			return;
    		}
    		if (capNhatCuonMenu())
    		{
    			return;
    		}
    		if (!GameCanvas.isPointerJustRelease)
    		{
    			return;
    		}
    		for (int i = 0; i < menuItems.size(); i++)
    		{
    			int[] o = oNut(i);
    			if (GameCanvas.isPointerHoldIn(o[0], o[1], o[2], o[3]))
    			{
    				God.NhatKy.ghi("MENU chon bang CHAM, muc = " + i
    						+ ", khung tu luc mo = " + khungTuLucMo);
    				GameCanvas.clearAllPointerEvent();
    				menuSelectedItem = i;
    				touch = true;
    				close = false;
    				doCloseMenu();
    				return;
    			}
    		}
    		if (!disableClose)
    		{
    			GameCanvas.clearAllPointerEvent();
    			touch = false;
    			close = true;
    			doCloseMenu();
    		}
    		return;
    	}

    	private void updateMenuKeyCu()
    	{
    		if ((GameScr.gI().activeRongThan && GameScr.gI().isUseFreez) || !showMenu || isScrolling())
    		{
    			return;
    		}
    		bool flag = false;
    		if (GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21] || GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23])
    		{
    			flag = true;
    			menuSelectedItem--;
    			if (menuSelectedItem < 0)
    			{
    				menuSelectedItem = menuItems.size() - 1;
    			}
    		}
    		else if (GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22] || GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24])
    		{
    			flag = true;
    			menuSelectedItem++;
    			if (menuSelectedItem > menuItems.size() - 1)
    			{
    				menuSelectedItem = 0;
    			}
    		}
    		else if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    		{
    			if (center != null)
    			{
    				if (center.idAction > 0)
    				{
    					if (center.actionListener == GameScr.gI())
    					{
    						GameScr.gI().actionPerform(center.idAction, center.p);
    					}
    					else
    					{
    						perform(center.idAction, center.p);
    					}
    				}
    			}
    			else
    			{
    				waitToPerform = 2;
    			}
    		}
    		else if (GameCanvas.keyPressed[12] && !GameScr.gI().isRongThanMenu())
    		{
    			if (isScrolling())
    			{
    				return;
    			}
    			if (left.idAction > 0)
    			{
    				perform(left.idAction, left.p);
    			}
    			else
    			{
    				waitToPerform = 2;
    			}
    			SoundMn.gI().buttonClose();
    		}
    		else if (!GameScr.gI().isRongThanMenu() && !disableClose && (GameCanvas.keyPressed[13] || mScreen.getCmdPointerLast(right)))
    		{
    			if (isScrolling())
    			{
    				return;
    			}
    			if (!close)
    			{
    				close = true;
    			}
    			isClose = true;
    			SoundMn.gI().buttonClose();
    		}
    		if (flag)
    		{
    			cmtoX = menuSelectedItem * menuW + menuW - GameCanvas.w / 2;
    			if (cmtoX > cmxLim)
    			{
    				cmtoX = cmxLim;
    			}
    			if (cmtoX < 0)
    			{
    				cmtoX = 0;
    			}
    			if (menuSelectedItem == menuItems.size() - 1 || menuSelectedItem == 0)
    			{
    				cmx = cmtoX;
    			}
    		}
    		bool flag2 = true;
    		if (GameCanvas.panel.cp != null && GameCanvas.panel.cp.isClip)
    		{
    			if (!GameCanvas.isPointerHoldIn(GameCanvas.panel.cp.cx, 0, GameCanvas.panel.cp.sayWidth + 2, GameCanvas.panel.cp.ch))
    			{
    				flag2 = true;
    			}
    			else
    			{
    				flag2 = false;
    				GameCanvas.panel.cp.updateKey();
    			}
    		}
    		// Bo qua vai khung hinh dau: cu cham MO menu van con nguyen trong
    		// `isPointerJustRelease` khi menu bat dau chay, ma diem cham do nam
    		// ngoai khung menu (nguoi choi bam vao mon do trong bang, con menu
    		// hien o day man hinh) — nen dieu kien duoi day khop ngay va dong
    		// menu di truoc khi no kip truot len. Nhin ra la "menu khong hien".
    		if (khungTuLucMo > SO_KHUNG_LANG && !disableClose && GameCanvas.isPointerJustRelease && !GameCanvas.isPointer(menuX, menuY, w, menuH) && !pointerIsDowning && !GameScr.gI().isRongThanMenu() && flag2)
    		{
    			if (!isScrolling())
    			{
    				pointerDownTime = (pointerDownFirstX = 0);
    				pointerIsDowning = false;
    				// Cham vao BANG thi de bang xu ly luon cu cham ay.
    				//
    				// Truoc day dong menu xong la xoa sach su kien cham, nen cu
    				// cham do chi lam mot viec: dong menu. Nguoi choi bam mon thu
    				// hai thi khong ra gi, phai bam them mot lan nua — dung canh
    				// "cu cai hien cai khong".
    				//
    				// startAt da biet thay menu dang mo bang menu moi, nen de cu
    				// cham di tiep la bang mo dung menu cua mon vua bam.
    				if (!chamTrongBang())
    				{
    					GameCanvas.clearAllPointerEvent();
    				}
    				Res.outz("menu select= " + menuSelectedItem);
    				isClose = true;
    				close = true;
    				SoundMn.gI().buttonClose();
    			}
    			return;
    		}
    		if (GameCanvas.isPointerDown)
    		{
    			if (!pointerIsDowning && GameCanvas.isPointer(menuX, menuY, w, menuH))
    			{
    				for (int i = 0; i < pointerDownLastX.Length; i++)
    				{
    					pointerDownLastX[0] = GameCanvas.px;
    				}
    				pointerDownFirstX = GameCanvas.px;
    				pointerIsDowning = true;
    				isDownWhenRunning = cmRun != 0;
    				cmRun = 0;
    			}
    			else if (pointerIsDowning)
    			{
    				pointerDownTime++;
    				if (pointerDownTime > 5 && pointerDownFirstX == GameCanvas.px && !isDownWhenRunning)
    				{
    					pointerDownFirstX = -1000;
    					menuSelectedItem = (cmtoX + GameCanvas.px - menuX) / menuW;
    				}
    				int num = GameCanvas.px - pointerDownLastX[0];
    				if (num != 0 && menuSelectedItem != -1)
    				{
    					menuSelectedItem = -1;
    				}
    				for (int num2 = pointerDownLastX.Length - 1; num2 > 0; num2--)
    				{
    					pointerDownLastX[num2] = pointerDownLastX[num2 - 1];
    				}
    				pointerDownLastX[0] = GameCanvas.px;
    				cmtoX -= num;
    				if (cmtoX < 0)
    				{
    					cmtoX = 0;
    				}
    				if (cmtoX > cmxLim)
    				{
    					cmtoX = cmxLim;
    				}
    				if (cmx < 0 || cmx > cmxLim)
    				{
    					num /= 2;
    				}
    				cmx -= num;
    				if (cmx < -(GameCanvas.h / 3))
    				{
    					wantUpdateList = true;
    				}
    				else
    				{
    					wantUpdateList = false;
    				}
    			}
    		}
    		if (GameCanvas.isPointerJustRelease && pointerIsDowning)
    		{
    			int i2 = GameCanvas.px - pointerDownLastX[0];
    			GameCanvas.isPointerJustRelease = false;
    			if (Res.abs(i2) < 20 && Res.abs(GameCanvas.px - pointerDownFirstX) < 20 && !isDownWhenRunning)
    			{
    				cmRun = 0;
    				cmtoX = cmx;
    				pointerDownFirstX = -1000;
    				menuSelectedItem = (cmtoX + GameCanvas.px - menuX) / menuW;
    				pointerDownTime = 0;
    				waitToPerform = 10;
    			}
    			else if (menuSelectedItem != -1 && pointerDownTime > 5)
    			{
    				pointerDownTime = 0;
    				waitToPerform = 1;
    			}
    			else if (menuSelectedItem == -1 && !isDownWhenRunning)
    			{
    				if (cmx < 0)
    				{
    					cmtoX = 0;
    				}
    				else if (cmx > cmxLim)
    				{
    					cmtoX = cmxLim;
    				}
    				else
    				{
    					int num3 = GameCanvas.px - pointerDownLastX[0] + (pointerDownLastX[0] - pointerDownLastX[1]) + (pointerDownLastX[1] - pointerDownLastX[2]);
    					num3 = ((num3 > 10) ? 10 : ((num3 < -10) ? (-10) : 0));
    					cmRun = -num3 * 100;
    				}
    			}
    			pointerIsDowning = false;
    			pointerDownTime = 0;
    			GameCanvas.isPointerJustRelease = false;
    		}
    		GameCanvas.clearKeyPressed();
    		GameCanvas.clearKeyHold();
    	}
    
    	public void moveCamera()
    	{
    		if (cmRun != 0 && !pointerIsDowning)
    		{
    			cmtoX += cmRun / 100;
    			if (cmtoX < 0)
    			{
    				cmtoX = 0;
    			}
    			else if (cmtoX > cmxLim)
    			{
    				cmtoX = cmxLim;
    			}
    			else
    			{
    				cmx = cmtoX;
    			}
    			cmRun = cmRun * 9 / 10;
    			if (cmRun < 100 && cmRun > -100)
    			{
    				cmRun = 0;
    			}
    		}
    		if (cmx != cmtoX && !pointerIsDowning)
    		{
    			cmvx = cmtoX - cmx << 2;
    			cmdx += cmvx;
    			cmx += cmdx >> 4;
    			cmdx &= 15;
    		}
    	}
    
    	/// <summary>Toa do y dang ve cua muc dau — de do khi menu khong hien.</summary>
    	public int doCaoDangVe()
    	{
    		return (menuTemY == null || menuTemY.Length == 0) ? -9999 : menuTemY[0];
    	}

    	/// <summary>Anh nen cua menu da nap duoc chua.</summary>
    	public bool coAnhNen()
    	{
    		return imgMenu1 != null;
    	}

    	/// <summary>Vung cua nut thu <paramref name="i"/>.</summary>
    	public int[] oNut(int i)
    	{
    		return new int[] { menuX + i * menuW + 1, menuY, menuW - 2, menuH };
    	}

    	/// <summary>Kéo dải nút sao cho mục đang chọn nằm trong tầm nhìn.</summary>
    	/// <remarks>
    	/// Đổi mục bằng phím mũi tên mà dải không chạy theo thì chọn tới mục thứ
    	/// mười là đang chọn một cái nút nằm ngoài màn hình.
    	/// </remarks>
    	private void cuonToiMucChon()
    	{
    		if (cuonToiDa <= 0 || menuSelectedItem < 0 || menuW <= 0)
    		{
    			return;
    		}
    		int x = menuSelectedItem * menuW;
    		if (x < cuonX)
    		{
    			cuonX = x;
    		}
    		else if (x + menuW > cuonX + rongDai)
    		{
    			cuonX = x + menuW - rongDai;
    		}
    		if (cuonX < 0)
    		{
    			cuonX = 0;
    		}
    		if (cuonX > cuonToiDa)
    		{
    			cuonX = cuonToiDa;
    		}
    		menuX = xGocMenu - cuonX;
    	}

    	/// <summary>
    	/// Lăn chuột và giữ kéo để cuộn dải nút theo chiều ngang.
    	/// </summary>
    	/// <returns><c>true</c> khi vừa kéo xong — lần nhả này không tính là chọn.</returns>
    	private bool capNhatCuonMenu()
    	{
    		if (cuonToiDa <= 0)
    		{
    			dangKeoMenu = false;
    			daKeoThat = false;
    			return false;
    		}
    		if (GameCanvas.pXYScrollMouse != 0
    				&& GameCanvas.pxMouse >= xDai
    				&& GameCanvas.pxMouse <= xDai + rongDai
    				&& GameCanvas.pyMouse >= menuY
    				&& GameCanvas.pyMouse <= menuY + menuH)
    		{
    			cuonX += GameCanvas.pXYScrollMouse > 0 ? -menuW : menuW;
    			if (cuonX < 0)
    			{
    				cuonX = 0;
    			}
    			if (cuonX > cuonToiDa)
    			{
    				cuonX = cuonToiDa;
    			}
    			menuX = xGocMenu - cuonX;
    		}
    		if (GameCanvas.isPointerDown
    				&& GameCanvas.isPointerHoldIn(xDai, menuY, rongDai, menuH))
    		{
    			if (!dangKeoMenu)
    			{
    				dangKeoMenu = true;
    				xBatKeo = GameCanvas.px;
    				cuonLucBatKeo = cuonX;
    				daKeoThat = false;
    			}
    			int lech = xBatKeo - GameCanvas.px;
    			if (lech > 4 || lech < -4)
    			{
    				daKeoThat = true;
    			}
    			cuonX = cuonLucBatKeo + lech;
    			if (cuonX < 0)
    			{
    				cuonX = 0;
    			}
    			if (cuonX > cuonToiDa)
    			{
    				cuonX = cuonToiDa;
    			}
    			menuX = xGocMenu - cuonX;
    			return false;
    		}
    		if (dangKeoMenu)
    		{
    			// Nha ngon: `pointerReleased` dat isPointerDown = false va
    			// isPointerJustRelease = true CUNG MOT LUC, nen khung hinh nay roi
    			// vao day chu khong vao nhanh keo o tren.
    			dangKeoMenu = false;
    			if (daKeoThat)
    			{
    				daKeoThat = false;
    				GameCanvas.clearAllPointerEvent();
    				return true;
    			}
    		}
    		return false;
    	}

    	/// <summary>Pha hai mau theo ty le <paramref name="t"/> (0..1).</summary>
    	private static int tronMau(int a, int b, float t)
    	{
    		int ra = (a >> 16) & 0xFF, ga = (a >> 8) & 0xFF, ba = a & 0xFF;
    		int rb = (b >> 16) & 0xFF, gb = (b >> 8) & 0xFF, bb = b & 0xFF;
    		int r = (int)(ra + (rb - ra) * t);
    		int gg = (int)(ga + (gb - ga) * t);
    		int bl = (int)(ba + (bb - ba) * t);
    		return (r << 16) | (gg << 8) | bl;
    	}

    	/// <summary>
    	/// To nen chuyen mau cho than nut, bo goc theo dung khung nut.
    	/// </summary>
    	/// <remarks>
    	/// May ve khong co ham chuyen mau. Cach lam: moi dai keo tu dinh cua no
    	/// XUONG TAN DAY nut, ve dai cao nhat truoc. Ve tung dai roi rac thanh
    	/// hinh chu nhat vuong goc thi goc vuong tho ra ngoai khung bo goc.
    	/// </remarks>
    	private static void veThanChuyenMau(mGraphics g, int x, int y, int w, int h,
    			int mauTren, int mauDuoi, int bo)
    	{
    		// Chuyen mau MIN tung dong mot, ma goc van tron.
    		//
    		// Ve tung dai bo goc chong len nhau thi dai cuoi thap qua, ban kinh bi
    		// kep gan ve 0 va de goc vuong len goc tron cua dai truoc; chua bang
    		// cach giam so dai thi het loi goc nhung mau giat cap thay ro.
    		//
    		// Tach hai viec: hai dau — noi co goc bo tron — to bang hai hinh BO
    		// GOC, phan giua ke tung dong mot diem. Phan giua cach hai mep it nhat
    		// `bo` diem nen khong dong nao cham toi goc.
    		// Mot lenh ve, thay cho hang chuc dong ke tay — xem mGraphics.veDaiDoc.
    		g.veDaiDoc(x, y, w, h, bo, mauTren, mauDuoi);
    	}

    	/// <summary>
    	/// Ve menu chon — ban ve moi, khong dung anh nen cu.
    	/// </summary>
    	/// <remarks>
    	/// <para>Ve bang ma thay cho <c>imgMenu1</c>/<c>imgMenu2</c>: hai anh do
    	/// co the khong nap duoc, va khi ay <c>drawImage</c> lang le khong ve gi —
    	/// menu thanh vo hinh ma khong bao loi. Ve bang ma thi luon co hinh.</para>
    	///
    	/// <para>Chu cat bot khi qua dai thay vi de tran ra ngoai nut: tran chu
    	/// de len nut ben canh, doc ra thanh mot dong dinh vao nhau.</para>
    	/// </remarks>
    	public void paintMenu(mGraphics g)
    	{
    		if (GameScr.gI().activeRongThan && GameScr.gI().isUseFreez)
    		{
    			return;
    		}
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		tinhBoCucMenu();

    		int n = menuItems.size();
    		// Cat theo dung vung dai nut: cuon ngang thi hai dau nut phai bien mat
    		// o mep dai chu khong tho ra giua man choi.
    		g.setClip(xDai, menuY - 2, rongDai, menuH + 4);
    		for (int i = 0; i < n; i++)
    		{
    			int[] o = oNut(i);
    			if (o[0] + o[2] < xDai || o[0] > xDai + rongDai)
    			{
    				continue;
    			}
    			bool chon = (i == menuSelectedItem);

    			// Khong ve bong do den phia sau nut: quang toi lech ve mot ben
    			// khien nut nhin nhu bi dan lech chu khong nhu noi len.
    			// Vien: vang dam khi dang chon.
    			g.setColor(chon ? MAU_VIEN_CHON : MAU_VIEN, 1f);
    			g.fillRect(o[0], o[1], o[2], o[3], 7);

    			// Than chuyen mau, sang o tren toi o duoi.
    			veThanChuyenMau(g, o[0] + 2, o[1] + 2, o[2] - 4, o[3] - 4,
    					chon ? MAU_THAN_TREN_CHON : MAU_THAN_TREN,
    					chon ? MAU_THAN_DUOI_CHON : MAU_THAN_DUOI, 6);

    			// Vet sang mong o nua tren cho nut co do cong.
    			g.setColor(0xFFFFFF, chon ? 0.20f : 0.10f);
    			g.fillRect(o[0] + 3, o[1] + 3, o[2] - 6, (o[3] - 6) / 2, 5);

    			// Vach sang o day cho muc dang chon, ro rang hon mot mau nen khac.
    			if (chon)
    			{
    				g.setColor(MAU_VIEN_CHON, 0.9f);
    				g.fillRect(o[0] + 6, o[1] + o[3] - 4, o[2] - 12, 2, 1);
    			}

    			Command lenh = (Command)menuItems.elementAt(i);
    			string[] dong = lenh.subCaption;
    			if (dong == null || dong.Length == 0)
    			{
    				dong = new string[1] { lenh.caption };
    			}
    			int soDong = dong.Length;
    			if (soDong > DONG_TOI_DA)
    			{
    				soDong = DONG_TOI_DA;
    			}
    			int giua = o[0] + o[2] / 2;
    			int yChu = o[1] + (o[3] - soDong * 12) / 2;
    			for (int j = 0; j < soDong; j++)
    			{
    				string s = catVua(dong[j], o[2] - 6);
    				// Bong chu mot diem cho chu noi len khoi than nut.
    				mFont.tahoma_7b_dark.drawString(g, s, giua + 1, yChu + j * 12 + 1,
    						mFont.CENTER);
    				mFont.tahoma_7b_white.drawString(g, s, giua, yChu + j * 12,
    						mFont.CENTER);
    			}
    		}
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		veDauCuon(g);
    		return;
    	}

    	/// <summary>
    	/// Hai mũi tên báo còn nút ở ngoài tầm nhìn.
    	/// </summary>
    	/// <remarks>
    	/// Không có dấu này thì dải nút bị cắt trông y hệt một dải đủ — người chơi
    	/// không có lý do gì để thử kéo, và mấy mục cuối coi như không tồn tại.
    	/// </remarks>
    	private void veDauCuon(mGraphics g)
    	{
    		if (cuonToiDa <= 0)
    		{
    			return;
    		}
    		int y = menuY + menuH / 2;
    		if (cuonX > 0)
    		{
    			veMuiTen(g, xDai + 5, y, -1);
    		}
    		if (cuonX < cuonToiDa)
    		{
    			veMuiTen(g, xDai + rongDai - 5, y, 1);
    		}
    	}

    	/// <summary>Một mũi tên nhỏ, xếp bằng mấy vạch dọc ngắn dần.</summary>
    	private static void veMuiTen(mGraphics g, int x, int y, int huong)
    	{
    		for (int i = 0; i < 5; i++)
    		{
    			int cao = 10 - i * 2;
    			if (cao < 1)
    			{
    				cao = 1;
    			}
    			g.setColor(0x000000, 0.45f);
    			g.fillRect(x + huong * i + 1, y - cao / 2 + 1, 2, cao);
    			g.setColor(MAU_VIEN_CHON, 0.95f);
    			g.fillRect(x + huong * i, y - cao / 2, 2, cao);
    		}
    	}

    	/// <summary>Cat chu cho vua <paramref name="rong"/> diem, them dau ba cham.</summary>
    	private static string catVua(string s, int rong)
    	{
    		if (s == null)
    		{
    			return "";
    		}
    		mFont f = mFont.tahoma_7b_white;
    		if (f.getWidth(s) <= rong)
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

    	private void paintMenuCu(mGraphics g)
    	{
    		if (GameScr.gI().activeRongThan && GameScr.gI().isUseFreez)
    		{
    			return;
    		}
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		g.translate(-cmx, 0);
    		for (int i = 0; i < menuItems.size(); i++)
    		{
    			if (i == menuSelectedItem)
    			{
    				g.drawImage(imgMenu2, menuX + i * menuW + 1, menuTemY[i], 0);
    			}
    			else
    			{
    				g.drawImage(imgMenu1, menuX + i * menuW + 1, menuTemY[i], 0);
    			}
    			Command command = (Command)menuItems.elementAt(i);
    			string[] array = command.subCaption;
    			if (array == null)
    			{
    				array = new string[1] { ((Command)menuItems.elementAt(i)).caption };
    			}
    			int num = menuTemY[i] + (menuH - array.Length * 14) / 2 + 1;
    			for (int j = 0; j < array.Length; j++)
    			{
    				if (i == menuSelectedItem)
    				{
    					mFont.tahoma_7b_green2.drawString(g, array[j], menuX + i * menuW + menuW / 2, num + j * 14, 2);
    				}
    				else if (command.isDisplay)
    				{
    					mFont.tahoma_7b_red.drawString(g, array[j], menuX + i * menuW + menuW / 2, num + j * 14, 2);
    				}
    				else
    				{
    					mFont.tahoma_7b_dark.drawString(g, array[j], menuX + i * menuW + menuW / 2, num + j * 14, 2);
    				}
    			}
    		}
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    	}
    
    	public void doCloseMenu()
    	{
    		Res.outz("CLOSE MENU");
    		God.NhatKy.ghi("MENU dong: close=" + close + " touch=" + touch
    				+ " muc=" + menuSelectedItem);
    		isClose = false;
    		showMenu = false;
    		InfoDlg.hide();
    		if (close)
    		{
    			GameCanvas.panel.cp = null;
    			Char.chatPopup = null;
    			if (GameCanvas.panel2 != null && GameCanvas.panel2.cp != null)
    			{
    				GameCanvas.panel2.cp = null;
    			}
    		}
    		else
    		{
    			if (!touch)
    			{
    				return;
    			}
    			GameCanvas.panel.cp = null;
    			if (GameCanvas.panel2 != null && GameCanvas.panel2.cp != null)
    			{
    				GameCanvas.panel2.cp = null;
    			}
    			if (menuSelectedItem >= 0)
    			{
    				Command command = (Command)menuItems.elementAt(menuSelectedItem);
    				if (command != null)
    				{
    					SoundMn.gI().buttonClose();
    					command.performAction();
    				}
    			}
    		}
    	}
    
    	public void performSelect()
    	{
    		InfoDlg.hide();
    		if (menuSelectedItem >= 0)
    		{
    			Command command = (Command)menuItems.elementAt(menuSelectedItem);
    			if (command != null)
    			{
    				command.performAction();
    			}
    		}
    	}
    
    	/// <summary>
    	/// So khung hinh da troi ke tu luc menu duoc mo.
    	/// </summary>
    	/// <remarks>
    	/// Dung de chan cu cham VUA MO menu khoi dong luon chinh no — xem cho
    	/// kiem tra trong <c>updateMenuKey</c>.
    	///
    	/// Khong dung duoc <c>tDelay</c>: bien do bi dat lai ve 0 ngay khi menu
    	/// truot len xong, nen sau do no khong con noi duoc gi ve tuoi cua menu.
    	/// </remarks>
    	/// <summary>Số khung hình đầu sau khi menu mở là bỏ qua mọi phím và chạm.</summary>
    	/// <remarks>
    	/// Cú bấm mở menu (Enter cạnh NPC, hay cú chạm vào NPC) vẫn còn nằm trong
    	/// hàng đợi khi bảng vừa hiện ra. Không có quãng lặng này thì chính cú đó
    	/// chọn luôn mục đầu — nhìn ra thành "Enter ăn hai lần".
    	///
    	/// Sáu khung ở 60 hình một giây là một phần mười giây: đủ nuốt cú bấm cũ mà
    	/// người chơi không kịp thấy chậm.
    	/// </remarks>
    	private const int SO_KHUNG_LANG = 6;

    	/// <summary>Đã chọn một mục và phím chọn vẫn chưa nhả ra.</summary>
    	private bool khoaChon;

    	private int khungTuLucMo;

    	public void updateMenu()
    	{
    		if (khungTuLucMo < 1000)
    		{
    			khungTuLucMo++;
    		}
    		moveCamera();
    		if (!isClose)
    		{
    			tDelay++;
    			for (int i = 0; i < menuTemY.Length; i++)
    			{
    				if (menuTemY[i] > menuY)
    				{
    					int num = menuTemY[i] - menuY >> 1;
    					if (num < 1)
    					{
    						num = 1;
    					}
    					if (tDelay > i)
    					{
    						menuTemY[i] -= num;
    					}
    				}
    			}
    			if (menuTemY[menuTemY.Length - 1] <= menuY)
    			{
    				tDelay = 0;
    			}
    		}
    		else
    		{
    			tDelay++;
    			for (int j = 0; j < menuTemY.Length; j++)
    			{
    				if (menuTemY[j] < GameCanvas.h)
    				{
    					int num2 = (GameCanvas.h - menuTemY[j] >> 1) + 2;
    					if (num2 < 1)
    					{
    						num2 = 1;
    					}
    					if (tDelay > j)
    					{
    						menuTemY[j] += num2;
    					}
    				}
    			}
    			if (menuTemY[menuTemY.Length - 1] >= GameCanvas.h)
    			{
    				tDelay = 0;
    				doCloseMenu();
    			}
    		}
    		if (xc != 0)
    		{
    			xc >>= 1;
    			if (xc < 0)
    			{
    				xc = 0;
    			}
    		}
    		if (isScrolling() || waitToPerform <= 0)
    		{
    			return;
    		}
    		waitToPerform--;
    		if (waitToPerform == 0)
    		{
    			if (menuSelectedItem >= 0 && !isNotClose[menuSelectedItem])
    			{
    				isClose = true;
    				touch = true;
    				GameCanvas.panel.cp = null;
    			}
    			else
    			{
    				performSelect();
    			}
    		}
    	}
    
    	public void perform(int idAction, object p)
    	{
    	}
    }
}
