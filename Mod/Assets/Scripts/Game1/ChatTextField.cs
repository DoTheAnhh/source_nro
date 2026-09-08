namespace Game1
{
    public class ChatTextField : IActionListener
    {
    	private static ChatTextField instance;
    
    	public TField tfChat;
    
    	public bool isShow;
    
    	public IChatable parentScreen;
    
    	private long lastChatTime;
    
    	public Command left;
    
    	public Command cmdChat;
    
    	public Command right;
    
    	public Command center;
    
    	private int x;
    
    	private int y;
    
    	private int w;
    
    	private int h;
    
    	private bool isPublic;
    
    	public Command cmdChat2;
    
    	public int yBegin;
    
    	public int yUp;
    
    	public int KC;
    
    	public string to;
    
    	public string strChat = "Chat ";
    
    	public ChatTextField()
    	{
    		tfChat = new TField();
    		if (Main.isWindowsPhone)
    		{
    			tfChat.showSubTextField = false;
    		}
    		if (Main.isIPhone)
    		{
    			tfChat.isPaintMouse = false;
    		}
    		tfChat.name = "chat";
    		if (Main.isWindowsPhone)
    		{
    			tfChat.strInfo = tfChat.name;
    		}
    		tfChat.width = GameCanvas.w - 6;
    		if (Main.isPC && tfChat.width > 250)
    		{
    			tfChat.width = 250;
    		}
    		tfChat.height = mScreen.ITEM_HEIGHT + 2;
    		tfChat.x = GameCanvas.w / 2 - tfChat.width / 2;
    		tfChat.isFocus = true;
    		tfChat.setMaxTextLenght(80);
    	}
    
    	public void initChatTextField()
    	{
    		left = new Command(mResources.OK, this, 8000, null, 1, GameCanvas.h - mScreen.cmdH + 1);
    		right = new Command(mResources.DELETE, this, 8001, null, GameCanvas.w - 70, GameCanvas.h - mScreen.cmdH + 1);
    		center = null;
    		w = tfChat.width + 20;
    		h = tfChat.height + 26;
    		x = GameCanvas.w / 2 - w / 2;
    		y = tfChat.y - 18;
    		if (Main.isPC && w > 320)
    		{
    			w = 320;
    		}
    		left.x = x;
    		right.x = x + w - 68;
    		if (GameCanvas.isTouch)
    		{
    			tfChat.y -= 5;
    			y -= 20;
    			h += 30;
    			left.x = GameCanvas.w / 2 - 68 - 5;
    			right.x = GameCanvas.w / 2 + 5;
    			left.y = GameCanvas.h - 30;
    			right.y = GameCanvas.h - 30;
    		}
    		cmdChat = new Command();
    		ActionChat actionChat = delegate(string str)
    		{
    			tfChat.justReturnFromTextBox = false;
    			tfChat.setText(str);
    			parentScreen.onChatFromMe(str, to);
    			tfChat.setText(string.Empty);
    			right.caption = mResources.CLOSE;
    		};
    		cmdChat.actionChat = actionChat;
    		cmdChat2 = new Command();
    		cmdChat2.actionChat = delegate(string str)
    		{
    			tfChat.justReturnFromTextBox = false;
    			if (parentScreen != null)
    			{
    				tfChat.setText(str);
    				parentScreen.onChatFromMe(str, to);
    				tfChat.setText(string.Empty);
    				tfChat.clearKb();
    				if (right != null)
    				{
    					right.performAction();
    				}
    			}
    			isShow = false;
    		};
    		yBegin = tfChat.y;
    		yUp = GameCanvas.h / 2 - 2 * tfChat.height;
    		if (Main.isWindowsPhone)
    		{
    			tfChat.showSubTextField = false;
    		}
    		if (Main.isIPhone)
    		{
    			tfChat.isPaintMouse = false;
    		}
    	}
    
    	public void updateWhenKeyBoardVisible()
    	{
    	}
    
    	public void keyPressed(int keyCode)
    	{
    		if (isShow)
    		{
    			tfChat.keyPressed(keyCode);
    		}
    		if (tfChat.getText().Equals(string.Empty))
    		{
    			right.caption = mResources.CLOSE;
    		}
    		else
    		{
    			right.caption = mResources.DELETE;
    		}
    	}
    
    	public static ChatTextField gI()
    	{
    		return (instance != null) ? instance : (instance = new ChatTextField());
    	}
    
    	/// <summary>
    	/// Mo khung chat, bat dau bang ky tu vua go.
    	/// </summary>
    	/// <remarks>
    	/// Truoc day goi <c>tfChat.keyPressed(firstCharacter)</c>, nhung ham do
    	/// da bi bo trong tu lau — ca than ham nam trong khoi chu thich, no chi
    	/// con <c>return false</c>. Nghia la ky tu dau bi mat, va khung chat
    	/// khong bao gio mo (dieu kien ngay duoi doi o phai co chu).
    	/// </remarks>
    	/// <summary>
    	/// Cua vao "go mot phim la mo chat" — CO TINH de trong.
    	/// </summary>
    	/// <remarks>
    	/// <para>Ham nay goi tu <c>GameScr.updateKey</c> voi <b>moi phim</b> nguoi
    	/// choi bam trong luc choi. Ban goc goi
    	/// <c>tfChat.keyPressed(firstCharacter)</c> roi chi mo khung chat neu o da
    	/// co chu — ma <c>TField.keyPressed</c> tu lau chi con <c>return false</c>,
    	/// nen o luon rong va khung chat khong bao gio mo tu duong nay.</para>
    	///
    	/// <para>Noi hop nhap moi vao day la lam song lai mot duong da chet nhieu
    	/// nam: bam W hay A de di chuyen cung bat hop chat len, va hop do an het
    	/// cham nen ca man hinh dong cung. Nen giu nguyen nep dang chay — muon mo
    	/// chat thi bam nut chat hoac phim R.</para>
    	/// </remarks>
    	public void startChat(int firstCharacter, IChatable parentScreen, string to)
    	{
    	}

    	/// <summary>
    	/// Mo hop nhap chu dung chung cho khung chat.
    	/// </summary>
    	/// <remarks>
    	/// <para>Dung <c>God.HopNhapChu</c> thay cho o nhap cua game de go duoc
    	/// tieng Viet — xem ghi chu trong lop do de biet vi sao khong sua thang
    	/// vao <c>TField</c>.</para>
    	///
    	/// <para>Duong GUI khong doi: van la
    	/// <c>parentScreen.onChatFromMe(chu, to)</c>, nen moi man dang dung khung
    	/// chat khong phai sua gi.</para>
    	///
    	/// <para>Gui xong thi <b>mo lai</b> hop neu khong phai kieu dung mot lan:
    	/// chat thuong von cho go tiep ma khong phai bam mo lai.</para>
    	/// </remarks>
    	/// <summary>
    	/// Kieu nhap cua lan mo hop gan nhat.
    	/// </summary>
    	/// <remarks>
    	/// Dung de biet co bat go tieng Viet hay khong: o nhap SO thi bat len chi
    	/// gay hai — go "1s" ra "1s" thi khong sao, nhung go "as" ra "a" co dau la
    	/// nguoi choi khong hieu chuyen gi.
    	/// </remarks>
    	public int kieuNhap = TField.INPUT_TYPE_ANY;

    	private void moHopChat(IChatable parentScreen, string to, string banDau)
    	{
    		moHopChat(parentScreen, to, banDau, "Chat", true);
    	}

    	private void moHopChat(IChatable parentScreen, string to, string banDau,
    			string tieuDe, bool goTiengViet)
    	{
    		this.parentScreen = parentScreen;
    		this.to = to;
    		isShow = false;
    		God.HopNhapChu.getInstance().moRa(tieuDe, banDau, 100, goTiengViet,
    				chu =>
    		{
    			long luc = mSystem.currentTimeMillis();
    			if (luc - lastChatTime >= 1000 && parentScreen != null)
    			{
    				lastChatTime = luc;
    				parentScreen.onChatFromMe(chu, to);
    			}
    			// KHONG tu mo lai sau khi gui.
    			//
    			// Ban truoc mo lai de go tiep cho tien, nhung nhu vay hop o trang
    			// thai MO VINH VIEN cho toi khi nguoi choi bam Dong. Ma hop dang
    			// mo thi `coManPhuDangMo()` luon true, va `GameScr.updateKey` thay
    			// vay la return ngay — Panel, NPC, cua hang deu khong nhan duoc
    			// cham nua. Dung la loi "tat ca NPC khong hien option de chon".
    		});
    	}
    
    	public void startChat(IChatable parentScreen, string to)
    	{
    		tuDongDong = false;
    		isPublic = false;
    		moHopChat(parentScreen, to, string.Empty);
    	}
    
    	/// <summary>Bấm OK hoặc Đóng một lần là hộp đóng hẳn.</summary>
	/// <remarks>
	/// Chỉ các màn mod đặt cờ này (qua <c>God.Utils.startChat</c>). Chat thường
	/// giữ nguyên nếp cũ: OK là gửi rồi <b>ở lại</b> để nhắn tiếp, và nút phải là
	/// "Xoá" khi còn chữ, chỉ thành "Đóng" khi ô đã rỗng.
	///
	/// Nếp ấy đúng cho khung chat nhưng sai hẳn với một ô nhập dùng một lần —
	/// nhập số lần nâng, gõ khẩu hiệu, gõ tên bang. Ở đó bấm OK xong hộp vẫn
	/// hiện, ô đã bị xoá trắng, trông y như một hộp mới vừa bật lên; còn muốn
	/// thoát thì phải bấm "Đóng" hai lần vì lần đầu chỉ xoá chữ.
	/// </remarks>
	public bool tuDongDong;

	public void startChat2(IChatable parentScreen, string to)
    	{
    		isPublic = false;
    		// `strChat` la tieu de ma cho goi da dat (vi du "Chat bang", "Nang toi
    		// muc"). Truoc day hop moi bo qua no va luon ghi "Chat".
    		moHopChat(parentScreen, to, string.Empty,
    				string.IsNullOrEmpty(strChat) ? "Nhập chữ" : strChat,
    				kieuNhap != TField.INPUT_TYPE_NUMERIC);
    	}
    
    	public void updateKey()
    	{
    	}
    
    	public void update()
    	{
    		if (!isShow)
    		{
    			return;
    		}
    		tfChat.update();
    		if (Main.isWindowsPhone)
    		{
    			updateWhenKeyBoardVisible();
    		}
    		if (tfChat.justReturnFromTextBox)
    		{
    			tfChat.justReturnFromTextBox = false;
    			parentScreen.onChatFromMe(tfChat.getText(), to);
    			tfChat.setText(string.Empty);
    			right.caption = mResources.CLOSE;
    		}
    		if (!Main.isPC)
    		{
    			return;
    		}
    		if (GameCanvas.keyPressed[15])
    		{
    			if (left != null && tfChat.getText() != string.Empty)
    			{
    				left.performAction();
    			}
    			GameCanvas.keyPressed[15] = false;
    			GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    		}
    		if (GameCanvas.keyPressed[14])
    		{
    			if (right != null)
    			{
    				right.performAction();
    			}
    			GameCanvas.keyPressed[14] = false;
    		}
    	}
    
    	public void close()
    	{
    		tfChat.setText(string.Empty);
    		isShow = false;
    	}
    
    	public void paint(mGraphics g)
    	{
    		if (isShow && !Main.isIPhone)
    		{
    			int num = ((!Main.isWindowsPhone) ? (y - KC) : (tfChat.y - 5));
    			int num2 = ((!Main.isWindowsPhone) ? x : 0);
    			int num3 = ((!Main.isWindowsPhone) ? w : GameCanvas.w);
    			PopUp.paintPopUp(g, num2, num, num3, h, -1, true);
    			if (Main.isPC)
    			{
    				mFont.tahoma_7b_green2.drawString(g, strChat + to, tfChat.x, tfChat.y - ((!GameCanvas.isTouch) ? 12 : 17), 0);
    				GameCanvas.paintz.paintCmdBar(g, left, center, right);
    			}
    			tfChat.paint(g);
    		}
    	}
    
    	public void perform(int idAction, object p)
    	{
    		switch (idAction)
    		{
    		case 8000:
    			Cout.LogError("perform chat 8000");
    			if (parentScreen != null)
    			{
    				long num = mSystem.currentTimeMillis();
    				if (num - lastChatTime >= 1000)
    				{
    					lastChatTime = num;
    					parentScreen.onChatFromMe(tfChat.getText(), to);
    					tfChat.setText(string.Empty);
    					right.caption = mResources.CLOSE;
    					tfChat.clearKb();
    					if (tuDongDong)
    					{
    						isShow = false;
    					}
    				}
    			}
    			break;
    		case 8001:
    			Cout.LogError("perform chat 8001");
    			if (tuDongDong)
    			{
    				// Mot cu bam la dong. Khong xet o con chu hay khong: nut nay
    				// dang mang chu "Dong", ma bam "Dong" lai chi xoa chu thi
    				// nguoi choi tuong hop bi treo.
    				isShow = false;
    				tfChat.clear();
    				parentScreen.onCancelChat();
    				break;
    			}
    			if (tfChat.getText().Equals(string.Empty))
    			{
    				isShow = false;
    				parentScreen.onCancelChat();
    			}
    			tfChat.clear();
    			break;
    		case 8002:
    			break;
    		}
    	}
    }
}
