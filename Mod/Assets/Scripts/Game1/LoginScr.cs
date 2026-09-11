namespace Game1
{
    using System;
    using UnityEngine;
    
    public class LoginScr : mScreen, IActionListener
    {
    	public TField tfUser;
    
    	public TField tfPass;
    
    	public static bool isContinueToLogin = false;
    
    	private int focus;
    
    	private int wC;
    
    	private int yL;
    
    	private int defYL;
    
    	public bool isCheck;
    
    	public bool isRes;
    
    	public Command cmdLogin;
    
    	public Command cmdCheck;
    
    	public Command cmdFogetPass;
    
    	public Command cmdRes;
    
    	public Command cmdMenu;
    
    	public Command cmdBackFromRegister;
    
    	public string listFAQ = string.Empty;
    
    	public string titleFAQ;
    
    	public string subtitleFAQ;
    
    	private string numSupport = string.Empty;
    
    	public static bool isLocal = false;
    
    	public static bool isUpdateAll;
    
    	public static bool isUpdateData;
    
    	public static bool isUpdateMap;
    
    	public static bool isUpdateSkill;
    
    	public static bool isUpdateItem;
    
    	public static string serverName;
    
    	public static Image imgTitle;
    
    	public int plX;
    
    	public int plY;
    
    	public int lY;
    
    	public int lX;
    
    	public int logoDes;
    
    	public int lineX;
    
    	public int lineY;
    
    	public static int[] bgId = new int[5] { 0, 8, 2, 6, 9 };
    
    	public static bool isTryGetIPFromWap;
    
    	public static short timeLogin;
    
    	public static long lastTimeLogin;
    
    	public static long currTimeLogin;
    
    	private int yt;
    
    	private Command cmdSelect;
    
    	private Command cmdOK;
    
    	private int xLog;
    
    	private int yLog;
    
    	public static GameMidlet m;
    
    	private int yy = GameCanvas.hh - mScreen.ITEM_HEIGHT - 5;
    
    	private int freeAreaHeight;
    
    	private int xP;
    
    	private int yP;
    
    	private int wP;
    
    	private int hP;
    
    	private bool isRegistering;

    	/// <summary>Mốc thời gian (ms) mà hộp chờ "Đang đăng ký" tự hết hạn.</summary>
    	/// <remarks>Xem chú thích trong <c>update()</c>: đếm bằng đồng hồ thật,
    	/// không bằng khung hình, để hộp chờ không bao giờ xoay mãi.</remarks>
    	private long hanChoDangKy;
    
    	private string passRe = string.Empty;
    
    	public bool isFAQ;
    
    	private int tipid = -1;
    
    	public bool isLogin2;
    
    	private int v = 2;
    
    	private int g;
    
    	private int ylogo = -40;
    
    	private int dir = 1;
    
    	private Command cmdCallHotline;

    	/// <summary>Nút "Đổi M.khẩu" — tự vẽ, không dùng ô nút phải.</summary>
    	/// <remarks>Ô nút phải còn ăn phím Enter trên máy tính (xem updateKey): đặt nút
    	/// này vào đó thì Enter mở đổi mật khẩu thay vì đăng nhập.</remarks>
    	private Command cmdDoiMatKhau;

    	/// <summary>Mật khẩu mới vừa gõ ở bước một, chờ gõ lại ở bước hai.</summary>
    	private string mkMoiTam = string.Empty;

    	/// <summary>Đang hiện mật khẩu dạng chữ thường.</summary>
    	private bool hienMatKhau;

    	// O nut an / hien mat khau, ghi lai luc ve de phan bat cham dung cung so.
    	private int xNutMat;

    	private int yNutMat;

    	private int wNutMat;
    
    	public static bool isLoggingIn;
    
    	public LoginScr()
    	{
    		yLog = GameCanvas.hh - 30;
    		TileMap.bgID = (sbyte)(mSystem.currentTimeMillis() % 9);
    		if (TileMap.bgID == 5 || TileMap.bgID == 6)
    		{
    			TileMap.bgID = 4;
    		}
    		GameScr.loadCamera(true, -1, -1);
    		GameScr.cmx = 100;
    		GameScr.cmy = 200;
    		Main.closeKeyBoard();
    		if (GameCanvas.h > 200)
    		{
    			defYL = GameCanvas.hh - 80;
    		}
    		else
    		{
    			defYL = GameCanvas.hh - 65;
    		}
    		resetLogo();
    		wC = ((GameCanvas.w < 200) ? 140 : 160);
    		yt = GameCanvas.hh - mScreen.ITEM_HEIGHT - 5;
    		if (GameCanvas.h <= 160)
    		{
    			yt = 20;
    		}
    		tfUser = new TField();
    		tfUser.y = GameCanvas.hh - mScreen.ITEM_HEIGHT - 9;
    		tfUser.width = wC;
    		tfUser.height = mScreen.ITEM_HEIGHT + 2;
    		tfUser.isFocus = true;
    		tfUser.setIputType(TField.INPUT_TYPE_ANY);
    		tfUser.name = ((mResources.language != 2) ? (mResources.phone + "/") : string.Empty) + mResources.email;
    		tfPass = new TField();
    		tfPass.y = GameCanvas.hh - 4;
    		tfPass.setIputType(TField.INPUT_TYPE_PASSWORD);
    		tfPass.width = wC - (mScreen.ITEM_HEIGHT + 2) - 22;
    		tfPass.height = mScreen.ITEM_HEIGHT + 2;
    		yt += 35;
    		isCheck = true;
    		switch (Rms.loadRMSInt("check"))
    		{
    		case 1:
    			isCheck = true;
    			break;
    		case 2:
    			isCheck = false;
    			break;
    		}
    		tfUser.setText(Rms.loadRMSString("acc"));
    		tfPass.setText(Rms.loadRMSString("pass"));
    		if (cmdCallHotline == null)
    		{
    			cmdCallHotline = new Command("Gọi hotline", this, 13, null);
    			cmdCallHotline.x = GameCanvas.w - 75;
    			if (mSystem.clientType == 1 && !GameCanvas.isTouch)
    			{
    				cmdCallHotline.y = GameCanvas.h - 20;
    			}
    			else
    			{
    				int num = 2;
    				cmdCallHotline.y = num + 6;
    			}
    		}
    		focus = 0;
    		cmdLogin = new Command((GameCanvas.w <= 200) ? mResources.login2 : mResources.login, GameCanvas.instance, 888393, null);
    		cmdCheck = new Command(mResources.remember, this, 2001, null);
    		cmdRes = new Command(mResources.register, this, 2002, null);
    		cmdBackFromRegister = new Command(mResources.CANCEL, this, 10021, null);
    		left = (cmdMenu = new Command(mResources.MENU, this, 2003, null));
    		freeAreaHeight = tfUser.y - 2 * tfUser.height;
    		if (GameCanvas.isTouch)
    		{
    			cmdLogin.x = GameCanvas.w / 2 + 8;
    			cmdMenu.x = GameCanvas.w / 2 - mScreen.cmdW - 8;
    			if (GameCanvas.h >= 200)
    			{
    				cmdLogin.y = yLog + 110;
    				cmdMenu.y = yLog + 110;
    			}
    			cmdBackFromRegister.x = GameCanvas.w / 2 + 3;
    			cmdBackFromRegister.y = yLog + 110;
    			cmdRes.x = GameCanvas.w / 2 - 84;
    			cmdRes.y = cmdMenu.y;
    		}
    		wP = 170;
    		hP = ((!isRes) ? 100 : 110);
    		xP = GameCanvas.hw - wP / 2;
    		yP = tfUser.y - 15;
    		int num2 = 4;
    		int num3 = num2 * 32 + 23 + 33;
    		if (num3 >= GameCanvas.w)
    		{
    			num2--;
    			num3 = num2 * 32 + 23 + 33;
    		}
    		xLog = GameCanvas.w / 2 - num3 / 2;
    		yLog = GameCanvas.hh - 30;
    		lY = ((GameCanvas.w < 200) ? (tfUser.y - 30) : (yLog - 30));
    		tfUser.x = xLog + 10;
    		tfUser.y = yLog + 20;
    		cmdOK = new Command(mResources.OK, this, 2008, null);
    		cmdOK.x = GameCanvas.w / 2 - 84;
    		cmdOK.y = cmdLogin.y;
    		cmdFogetPass = new Command(mResources.forgetPass, this, 1003, null);
    		cmdFogetPass.x = GameCanvas.w / 2 + 3;
    		cmdFogetPass.y = cmdLogin.y;
    		// Ba nut mot hang: OK — Doi M.khau — Quen M.khau. Them hang duoi thi
    		// man dien thoai thap bi tran hoac de len logo.
    		cmdDoiMatKhau = new Command("Đổi M.khẩu", this, 2101, null);
    		cmdDoiMatKhau.y = cmdLogin.y;
    		if (cmdLogin.y > 0)
    		{
    			int rongNut = mScreen.cmdW;
    			cmdOK.x = GameCanvas.w / 2 - rongNut / 2 - 6 - rongNut;
    			cmdDoiMatKhau.x = GameCanvas.w / 2 - rongNut / 2;
    			cmdFogetPass.x = GameCanvas.w / 2 + rongNut / 2 + 6;
    		}
    		center = cmdOK;
    		left = cmdFogetPass;
    	}
    
    	public static void getServerLink()
    	{
    		try
    		{
    			if (isTryGetIPFromWap)
    			{
    				return;
    			}
    			Command command = new Command();
    			ActionChat actionChat = delegate(string str)
    			{
    				try
    				{
    					if (str != null && !(str == string.Empty))
    					{
    						Rms.saveIP(str);
    						if (str.Contains(":"))
    						{
    							int num = str.IndexOf(":");
    							string text = str.Substring(0, num);
    							string s = str.Substring(num + 1);
    							GameMidlet.IP = text;
    							GameMidlet.PORT = int.Parse(s);
    							Session_ME.gI().connect(text, int.Parse(s));
    							isTryGetIPFromWap = true;
    						}
    					}
    				}
    				catch (Exception)
    				{
    				}
    			};
    			command.actionChat = actionChat;
    		}
    		catch (Exception)
    		{
    		}
    	}
    
    	public override void switchToMe()
    	{
    		isRegistering = false;
    		SoundMn.gI().stopAll();
    		tfUser.isFocus = true;
    		tfPass.isFocus = false;
    		if (GameCanvas.isTouch)
    		{
    			tfUser.isFocus = false;
    		}
    		GameCanvas.loadBG(0);
    		base.switchToMe();
    	}
    
    	public void setUserPass()
    	{
    		string text = Rms.loadRMSString("acc");
    		if (text != null && !text.Equals(string.Empty))
    		{
    			tfUser.setText(text);
    		}
    		string text2 = Rms.loadRMSString("pass");
    		if (text2 != null && !text2.Equals(string.Empty))
    		{
    			tfPass.setText(text2);
    		}
    	}
    
    	public void updateTfWhenOpenKb()
    	{
    	}
    
    	protected void doMenu()
    	{
    		MyVector myVector = new MyVector();
    		myVector.addElement(new Command(mResources.registerNewAcc, this, 2004, null));
    		if (!isLogin2)
    		{
    			myVector.addElement(new Command(mResources.selectServer, this, 1004, null));
    		}
    		myVector.addElement(new Command(mResources.forgetPass, this, 1003, null));
    		myVector.addElement(new Command(mResources.website, this, 1005, null));
    		if (Main.isPC)
    		{
    			myVector.addElement(new Command(mResources.EXIT, GameCanvas.instance, 8885, null));
    		}
    		GameCanvas.menu.startAt(myVector, 0);
    	}
    
    	protected void doRegister()
    	{
    		if (tfUser.getText().Equals(string.Empty))
    		{
    			GameCanvas.startOKDlg(mResources.userBlank);
    			return;
    		}
    		if (tfPass.getText().Equals(string.Empty))
    		{
    			GameCanvas.startOKDlg(mResources.passwordBlank);
    			return;
    		}
    		if (tfUser.getText().Length < 5)
    		{
    			GameCanvas.startOKDlg(mResources.accTooShort);
    			return;
    		}
    		// Chi chu thuong a-z va so 0-9. KHONG con kiem tra email hay so dien
    		// thoai nua.
    		//
    		// Ban truoc bat tai khoan phai dung dang "ten@mien.com" hoac dang so
    		// "0912345678" — nhung tai khoan o day khong gui thu, khong gui tin
    		// nhan, khong xac thuc gi qua hai duong do. Phep kiem tra chi lam
    		// nguoi choi go lai nhieu lan roi bo.
    		//
    		// Quy tac nay khop DUNG voi GodGK.kiemTraTen ben server. Hai ben lech
    		// nhau la canh te nhat: client cho qua, server tu choi, va nguoi choi
    		// khong doan ra minh sai o dau.
    		string tenGo = tfUser.getText();
    		string matGo = tfPass.getText();
    		string text = loiChuThuongVaSo(tenGo, mResources.taiKhoanChu);
    		if (text == null)
    		{
    			text = loiChuThuongVaSo(matGo, mResources.matKhauChu);
    		}
    		if (text != null)
    		{
    			GameCanvas.startOKDlg(text);
    			return;
    		}
    		GameCanvas.msgdlg.setInfo(
    			mResources.plsCheckAcc + mResources.taiKhoanChu + ": " + tenGo
    				+ "\n" + mResources.matKhauChu + ": " + matGo,
    			new Command(mResources.ACCEPT, this, 4000, null), null,
    			new Command(mResources.NO, GameCanvas.instance, 8882, null));
    		GameCanvas.currentDialog = GameCanvas.msgdlg;
    	}

    	/// <summary>Chỉ cho chữ thường a-z và số 0-9.</summary>
    	/// <remarks>
    	/// <para>Viết tay từng ký tự chứ không dùng biểu thức chính quy, vì
    	/// <c>\w</c> khớp cả chữ hoa lẫn gạch dưới — đúng hai thứ vừa bỏ.</para>
    	///
    	/// <para>Chặn chữ hoa vì cột <c>username</c> bên máy chủ dùng bảng chữ
    	/// không phân biệt hoa thường: <i>Nam</i> và <i>nam</i> là một tài khoản
    	/// khi tra cơ sở dữ liệu, nhưng là hai chuỗi khác nhau ở mọi phép so
    	/// sánh trong mã. Chặn hẳn thì không còn khe cho hai cách hiểu đó lệch
    	/// nhau.</para>
    	/// </remarks>
    	/// <param name="s">chuỗi cần soát</param>
    	/// <param name="ten">"Tên tài khoản" hay "Mật khẩu", để ghép vào câu báo</param>
    	/// <returns>câu báo lỗi, hoặc <c>null</c> nếu hợp lệ</returns>
    	private string loiChuThuongVaSo(string s, string ten)
    	{
    		for (int i = 0; i < s.Length; i++)
    		{
    			char c = s[i];
    			if (c == ' ')
    			{
    				return ten + mResources.loiCoDauCach;
    			}
    			if (c >= 'A' && c <= 'Z')
    			{
    				return ten + mResources.loiVietHoa;
    			}
    			if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')))
    			{
    				return ten + mResources.loiKyTuLa;
    			}
    		}
    		return null;
    	}
    
    	protected void doRegister(string user)
    	{
    		isFAQ = false;
    		GameCanvas.startWaitDlg(mResources.CONNECTING);
    		GameCanvas.connect();
    		GameCanvas.startWaitDlg(mResources.REGISTERING);
    		passRe = tfPass.getText();
    		Service.gI().requestRegister(user, tfPass.getText(), Rms.loadRMSString("userAo" + ServerListScreen.ipSelect), Rms.loadRMSString("passAo" + ServerListScreen.ipSelect), GameMidlet.VERSION);
    		Rms.saveRMSString("acc", user);
    		Rms.saveRMSString("pass", tfPass.getText());
    		hanChoDangKy = mSystem.currentTimeMillis() + 15000L;
    		isRegistering = true;
    	}
    
    	public void doViewFAQ()
    	{
    		if (!listFAQ.Equals(string.Empty) || !listFAQ.Equals(string.Empty))
    		{
    		}
    		if (!Session_ME.connected)
    		{
    			isFAQ = true;
    			GameCanvas.connect();
    		}
    		GameCanvas.startWaitDlg();
    	}
    
    	protected void doSelectServer()
    	{
    		MyVector myVector = new MyVector();
    		if (isLocal)
    		{
    			myVector.addElement(new Command("Server LOCAL", this, 20004, null));
    		}
    		myVector.addElement(new Command("Server Bokken", this, 20001, null));
    		myVector.addElement(new Command("Server Shuriken", this, 20002, null));
    		myVector.addElement(new Command("Server Tessen (mới)", this, 20003, null));
    		GameCanvas.menu.startAt(myVector, 0);
    		if (loadIndexServer() != -1 && !GameCanvas.isTouch)
    		{
    			GameCanvas.menu.menuSelectedItem = loadIndexServer();
    		}
    	}
    
    	protected void saveIndexServer(int index)
    	{
    		Rms.saveRMSInt("indServer", index);
    	}
    
    	protected int loadIndexServer()
    	{
    		return Rms.loadRMSInt("indServer");
    	}
    
    	public void doLogin()
    	{
    		string text = Rms.loadRMSString("acc");
    		string text2 = Rms.loadRMSString("pass");
    		if (text != null && !text.Equals(string.Empty))
    		{
    			isLogin2 = false;
    		}
    		else if (Rms.loadRMSString("userAo" + ServerListScreen.ipSelect) != null && !Rms.loadRMSString("userAo" + ServerListScreen.ipSelect).Equals(string.Empty))
    		{
    			isLogin2 = true;
    		}
    		else
    		{
    			isLogin2 = false;
    		}
    		if ((text == null || text.Equals(string.Empty)) && isLogin2)
    		{
    			text = Rms.loadRMSString("userAo" + ServerListScreen.ipSelect);
    			text2 = "a";
    		}
    		if (text == null || text2 == null || GameMidlet.VERSION == null || text.Equals(string.Empty))
    		{
    			return;
    		}
    		if (text2.Equals(string.Empty))
    		{
    			focus = 1;
    			tfUser.isFocus = false;
    			tfPass.isFocus = true;
    			if (!GameCanvas.isTouch)
    			{
    				right = tfPass.cmdClear;
    			}
    			return;
    		}
    		if (!Session_ME.gI().isConnected())
    		{
    			GameCanvas.connect();
    		}
    		Res.outz("ccccccc " + text + " " + text2 + " " + GameMidlet.VERSION + " " + (sbyte)(isLogin2 ? 1 : 0));
    		Service.gI().login(text, text2, GameMidlet.VERSION, (sbyte)(isLogin2 ? 1 : 0));
    		if (Session_ME.connected)
    		{
    			GameCanvas.startWaitDlg();
    		}
    		else
    		{
    			GameCanvas.startOKDlg(mResources.maychutathoacmatsong);
    		}
    		focus = 0;
    		if (!isLogin2)
    		{
    			actRegisterLeft();
    		}
    		GameCanvas.timeBreakLoading = mSystem.currentTimeMillis() + 30000;
    	}
    
    	public void savePass()
    	{
    		if (isCheck)
    		{
    			Rms.saveRMSInt("check", 1);
    			Rms.saveRMSString("acc", tfUser.getText().ToLower().Trim());
    			Rms.saveRMSString("pass", tfPass.getText().ToLower().Trim());
    		}
    		else
    		{
    			Rms.saveRMSInt("check", 2);
    			Rms.saveRMSString("acc", string.Empty);
    			Rms.saveRMSString("pass", string.Empty);
    		}
    	}
    
    	public override void update()
    	{
    		if (isRegistering)
    		{
    			// Hộp chờ "Đang đăng ký" PHẢI có hạn.
    			//
    			// Trước đây hạn duy nhất nằm sau `Main.isWindowsPhone`, tức là trên
    			// máy tính và Android thì KHÔNG có hạn nào cả. Máy chủ không trả lời
    			// — bản cũ chưa có nhánh đăng ký, mất gói, đứt mạng giữa chừng — là
    			// quả cầu xoay mãi mãi, không một dòng chữ nào giải thích. Người chơi
    			// không còn đường nào ngoài tắt game.
    			//
    			// Biến `t` cũ còn đếm theo KHUNG HÌNH chứ không theo giây: đặt 20 rồi
    			// mỗi update() trừ một, tức hết hạn sau 20 khung — chưa tới một phần
    			// ba giây. Nay đo bằng đồng hồ thật.
    			if (GameCanvas.currentDialog != GameCanvas.msgdlg || !GameCanvas.msgdlg.isWait)
    			{
    				// Đã có gói trả về và hộp chờ đã bị thay bằng hộp khác (báo lỗi,
    				// hỏi lại...) -> không còn gì để đếm nữa.
    				isRegistering = false;
    			}
    			else if (mSystem.currentTimeMillis() >= hanChoDangKy)
    			{
    				isRegistering = false;
    				GameCanvas.endDlg();
    				GameCanvas.startOKDlg("Máy chủ không trả lời yêu cầu đăng ký.\nHãy kiểm tra kết nối mạng rồi thử lại.");
    			}
    		}
    		if (timeLogin > 0)
    		{
    			GameCanvas.startWaitDlg();
    			currTimeLogin = mSystem.currentTimeMillis();
    			if (currTimeLogin - lastTimeLogin >= 1000)
    			{
    				timeLogin--;
    				if (timeLogin == 0)
    				{
    					GameCanvas.loginScr.doLogin();
    				}
    				lastTimeLogin = currTimeLogin;
    			}
    		}
    		if (isLogin2 && !isRes)
    		{
    			tfUser.name = ((mResources.language != 2) ? (mResources.phone + "/") : string.Empty) + mResources.email;
    			tfPass.name = mResources.password;
    			tfUser.isPaintCarret = false;
    			tfPass.isPaintCarret = false;
    			tfUser.update();
    			tfPass.update();
    		}
    		else
    		{
    			tfUser.name = ((mResources.language != 2) ? (mResources.phone + "/") : string.Empty) + mResources.email;
    			tfPass.name = mResources.password;
    			tfUser.update();
    			tfPass.update();
    		}
    		if (TouchScreenKeyboard.visible)
    		{
    			mGraphics.addYWhenOpenKeyBoard = 50;
    		}
    		for (int i = 0; i < Effect2.vEffect2.size(); i++)
    		{
    			Effect2 effect = (Effect2)Effect2.vEffect2.elementAt(i);
    			effect.update();
    		}
    		if (isUpdateAll && !isUpdateData && !isUpdateItem && !isUpdateMap && !isUpdateSkill)
    		{
    			isUpdateAll = false;
    			mSystem.gcc();
    			Service.gI().finishUpdate();
    		}
    		GameScr.cmx++;
    		if (GameScr.cmx > GameCanvas.w * 3 + 100)
    		{
    			GameScr.cmx = 100;
    		}
    		if (ChatPopup.currChatPopup != null)
    		{
    			return;
    		}
    		GameCanvas.debug("LGU1", 0);
    		GameCanvas.debug("LGU2", 0);
    		GameCanvas.debug("LGU3", 0);
    		updateLogo();
    		GameCanvas.debug("LGU4", 0);
    		GameCanvas.debug("LGU5", 0);
    		if (g >= 0)
    		{
    			ylogo += dir * g;
    			g += dir * v;
    			if (g <= 0)
    			{
    				dir *= -1;
    			}
    			if (ylogo > 0)
    			{
    				dir *= -1;
    				g -= 2 * v;
    			}
    		}
    		GameCanvas.debug("LGU6", 0);
    		if (tipid >= 0 && GameCanvas.gameTick % 100 == 0)
    		{
    			doChangeTip();
    		}
    		if (isLogin2 && !isRes)
    		{
    			tfUser.isPaintCarret = false;
    			tfPass.isPaintCarret = false;
    			tfUser.update();
    			tfPass.update();
    		}
    		else
    		{
    			tfUser.name = ((mResources.language != 2) ? (mResources.phone + "/") : string.Empty) + mResources.email;
    			tfPass.name = mResources.password;
    			tfUser.update();
    			tfPass.update();
    		}
    		if (GameCanvas.isTouch)
    		{
    			if (isRes)
    			{
    				center = cmdRes;
    				left = cmdBackFromRegister;
    			}
    			else
    			{
    				center = cmdOK;
    				left = cmdFogetPass;
    			}
    		}
    		else if (isRes)
    		{
    			center = cmdRes;
    			left = cmdBackFromRegister;
    		}
    		else
    		{
    			center = cmdOK;
    			left = cmdFogetPass;
    		}
    		if (!Main.isPC && !TouchScreenKeyboard.visible && !Main.isMiniApp && !Main.isWindowsPhone)
    		{
    			string text = tfUser.getText().ToLower().Trim();
    			string text2 = tfPass.getText().ToLower().Trim();
    			if (!text.Equals(string.Empty) && !text2.Equals(string.Empty))
    			{
    				doLogin();
    			}
    			Main.isMiniApp = true;
    		}
    		updateTfWhenOpenKb();
    	}
    
    	private void doChangeTip()
    	{
    		tipid++;
    		if (tipid >= mResources.tips.Length)
    		{
    			tipid = 0;
    		}
    		if (GameCanvas.currentDialog == GameCanvas.msgdlg && GameCanvas.msgdlg.isWait)
    		{
    			GameCanvas.msgdlg.setInfo(mResources.tips[tipid]);
    		}
    	}
    
    	public void updateLogo()
    	{
    		if (defYL != yL)
    		{
    			yL += defYL - yL >> 1;
    		}
    	}
    
    	public override void keyPress(int keyCode)
    	{
    		if (tfUser.isFocus)
    		{
    			tfUser.keyPressed(keyCode);
    		}
    		else if (tfPass.isFocus)
    		{
    			tfPass.keyPressed(keyCode);
    		}
    		base.keyPress(keyCode);
    	}
    
    	public override void unLoad()
    	{
    		base.unLoad();
    	}
    
    	public override void paint(mGraphics g)
    	{
    		GameCanvas.debug("PLG1", 1);
    		GameCanvas.paintBGGameScr(g);
    		GameCanvas.debug("PLG2", 2);
    		int num = tfUser.y - 50;
    		if (GameCanvas.h <= 220)
    		{
    			num += 5;
    		}
    		mFont.tahoma_7_white.drawString(g, "v" + GameMidlet.VERSION, GameCanvas.w - 2, 17, 1, mFont.tahoma_7_grey);
    		if (mSystem.clientType == 1 && !GameCanvas.isTouch)
    		{
    			mFont.tahoma_7_white.drawString(g, ServerListScreen.linkweb, GameCanvas.w - 2, GameCanvas.h - 15, 1, mFont.tahoma_7_grey);
    		}
    		else
    		{
    			mFont.tahoma_7_white.drawString(g, ServerListScreen.linkweb, GameCanvas.w - 2, 2, 1, mFont.tahoma_7_grey);
    		}
    		if (ChatPopup.currChatPopup != null || ChatPopup.serverChatPopUp != null)
    		{
    			return;
    		}
    		if (GameCanvas.currentDialog == null)
    		{
    			int h = 105;
    			int w = ((GameCanvas.w < 200) ? 160 : 180);
    			PopUp.paintPopUp(g, xLog, yLog - 10, w, h, -1, true);
    			if (GameCanvas.h > 160 && imgTitle != null)
    			{
    				g.drawImage(imgTitle, GameCanvas.hw, 60, 3);
    			}
    			GameCanvas.debug("PLG4", 1);
    			int num2 = 4;
    			int num3 = num2 * 32 + 23 + 33;
    			if (num3 >= GameCanvas.w)
    			{
    				num2--;
    				num3 = num2 * 32 + 23 + 33;
    			}
    			xLog = GameCanvas.w / 2 - num3 / 2;
    			tfUser.x = xLog + 10;
    			tfUser.y = yLog + 20;
    			tfPass.x = xLog + 10;
    			tfPass.y = yLog + 55;
    			tfUser.paint(g);
    			tfPass.paint(g);
    			// TField.paint de lai vung cat (clip) dung bang khung chu cua o mat
    			// khau — khong mo lai thi nut an / hien va nut Doi M.khau bi cat mat.
    			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			if (!isRes)
    			{
    				veNutMatKhau(g);
    				if (cmdDoiMatKhau != null && cmdDoiMatKhau.y > 0)
    				{
    					cmdDoiMatKhau.paint(g);
    				}
    			}
    			int num4 = 0;
    			if (GameCanvas.w >= 176)
    			{
    				num4 = 50;
    			}
    			else
    			{
    				mFont.tahoma_7b_green2.drawString(g, mResources.acc + ":", tfUser.x - 35, tfUser.y + 7, 0);
    				mFont.tahoma_7b_green2.drawString(g, mResources.pwd + ":", tfPass.x - 35, tfPass.y + 7, 0);
    				mFont.tahoma_7b_green2.drawString(g, mResources.server + ":" + serverName, GameCanvas.w / 2, tfPass.y + 32, 2);
    				num4 = 0;
    			}
    		}
    		veDongTacGia(g);
    		base.paint(g);
    	}

    	/// <summary>Dòng tên người làm, dưới cùng màn đăng nhập.</summary>
    	/// <remarks>
    	/// <b>Đã bỏ dòng "Server: &lt;IP&gt;:&lt;cổng&gt;".</b> Trước đây in ra để dễ
    	/// biết client đang nối vào đâu — IP LAN do router cấp theo DHCP nên hay đổi.
    	/// Nhưng đó là địa chỉ máy chủ phơi ra cho mọi người đứng ở màn đăng nhập
    	/// đọc được, kể cả người không nên biết. Cần tra thì xem log Unity hoặc
    	/// <c>data/config</c>, không phải in lên màn hình.
    	/// </remarks>
    	private void veDongTacGia(mGraphics g)
    	{
    		int y = GameCanvas.h - 26;
    		mFont.tahoma_7_white.drawString(g, "Developed by Do The Anh",
    			GameCanvas.w / 2, y, mFont.CENTER, mFont.tahoma_7_grey);
    	}
    
    	public override void updateKey()
    	{
    		if (GameCanvas.isTouch)
    		{
    			if (cmdCallHotline != null && cmdCallHotline.isPointerPressInside())
    			{
    				cmdCallHotline.performAction();
    			}
    		}
    		else if (mSystem.clientType == 1 && GameCanvas.keyPressed[13])
    		{
    			GameCanvas.keyPressed[13] = false;
    			cmdCallHotline.performAction();
    		}
    		if (isContinueToLogin)
    		{
    			return;
    		}
    		if (!GameCanvas.isTouch)
    		{
    			if (tfUser.isFocus)
    			{
    				right = tfUser.cmdClear;
    			}
    			else
    			{
    				right = tfPass.cmdClear;
    			}
    		}
    		if (GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21])
    		{
    			focus--;
    			if (focus < 0)
    			{
    				focus = 1;
    			}
    		}
    		else if (GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22] || GameCanvas.keyPressed[16])
    		{
    			focus++;
    			if (focus > 1)
    			{
    				focus = 0;
    			}
    		}
    		if (GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21] || GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22] || GameCanvas.keyPressed[16])
    		{
    			GameCanvas.clearKeyPressed();
    			if (!isLogin2 || isRes)
    			{
    				if (focus == 1)
    				{
    					tfUser.isFocus = false;
    					tfPass.isFocus = true;
    				}
    				else if (focus == 0)
    				{
    					tfUser.isFocus = true;
    					tfPass.isFocus = false;
    				}
    				else
    				{
    					tfUser.isFocus = false;
    					tfPass.isFocus = false;
    				}
    			}
    		}
    		if (GameCanvas.isTouch)
    		{
    			if (isRes)
    			{
    				center = cmdRes;
    				left = cmdBackFromRegister;
    			}
    			else
    			{
    				center = cmdOK;
    				left = cmdFogetPass;
    			}
    		}
    		else if (isRes)
    		{
    			center = cmdRes;
    			left = cmdBackFromRegister;
    		}
    		else
    		{
    			center = cmdOK;
    			left = cmdFogetPass;
    		}
    		// Nut nho cuoi o mat khau: an / hien. Nam NGOAI vung 40 diem quanh mep
    		// phai o nhap — vung ay la nut xoa het chu cua TField.
    		if (!isRes && wNutMat > 0 && GameCanvas.isPointerJustRelease
    			&& GameCanvas.isPointerHoldIn(xNutMat, yNutMat, wNutMat, wNutMat))
    		{
    			hienMatKhau = !hienMatKhau;
    			tfPass.setIputType(hienMatKhau ? TField.INPUT_TYPE_ANY : TField.INPUT_TYPE_PASSWORD);
    			tfPass.setText(tfPass.getText());
    			GameCanvas.clearAllPointerEvent();
    		}
    		if (!isRes && cmdDoiMatKhau != null && cmdDoiMatKhau.y > 0 && cmdDoiMatKhau.isPointerPressInside())
    		{
    			cmdDoiMatKhau.performAction();
    		}
    		if (GameCanvas.isPointerJustRelease && (!isLogin2 || isRes))
    		{
    			if (GameCanvas.isPointerHoldIn(tfUser.x, tfUser.y, tfUser.width, tfUser.height))
    			{
    				focus = 0;
    			}
    			else if (GameCanvas.isPointerHoldIn(tfPass.x, tfPass.y, tfPass.width, tfPass.height))
    			{
    				focus = 1;
    			}
    		}
    		if (Main.isPC && GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] && right != null)
    		{
    			right.performAction();
    		}
    		base.updateKey();
    		GameCanvas.clearKeyPressed();
    	}
    
    	/// <summary>Nút nhỏ cuối ô mật khẩu: bấm để ẩn / hiện mật khẩu.</summary>
    	private void veNutMatKhau(mGraphics g)
    	{
    		wNutMat = tfPass.height;
    		xNutMat = tfPass.x + tfPass.width + 22;
    		yNutMat = tfPass.y;
    		g.setColor(0x8B5A2B);
    		g.fillRect(xNutMat, yNutMat, wNutMat, wNutMat);
    		g.setColor(0xF3D9A8);
    		g.fillRect(xNutMat + 1, yNutMat + 1, wNutMat - 2, wNutMat - 2);
    		mFont.tahoma_7b_dark.drawString(g, hienMatKhau ? "Ẩn" : "Hiện", xNutMat + wNutMat / 2,
    			yNutMat + wNutMat / 2 - 6, mFont.CENTER);
    	}

    	public void resetLogo()
    	{
    		yL = -50;
    	}
    
    	public void perform(int idAction, object p)
    	{
    		switch (idAction)
    		{
    		case 13:
    			switch (mSystem.clientType)
    			{
    			case 1:
    				mSystem.callHotlineJava();
    				break;
    			case 3:
    			case 5:
    				mSystem.callHotlineIphone();
    				break;
    			case 6:
    				mSystem.callHotlineWindowsPhone();
    				break;
    			case 4:
    				mSystem.callHotlinePC();
    				break;
    			case 2:
    				break;
    			}
    			break;
    		case 1000:
    			try
    			{
    				GameMidlet.instance.platformRequest((string)p);
    			}
    			catch (Exception)
    			{
    			}
    			GameCanvas.endDlg();
    			break;
    		case 1001:
    			GameCanvas.endDlg();
    			isRes = false;
    			break;
    		case 1002:
    		{
    			GameCanvas.startWaitDlg();
    			string text = Rms.loadRMSString("userAo" + ServerListScreen.ipSelect);
    			if (text == null || text.Equals(string.Empty))
    			{
    				Service.gI().login2(string.Empty);
    				break;
    			}
    			GameCanvas.loginScr.isLogin2 = true;
    			GameCanvas.connect();
    			Service.gI().setClientType();
    			Service.gI().login(text, string.Empty, GameMidlet.VERSION, 1);
    			break;
    		}
    		case 1004:
    			ServerListScreen.doUpdateServer();
    			GameCanvas.serverScreen.switchToMe();
    			break;
    		case 10021:
    			actRegisterLeft();
    			break;
    		case 1003:
    			GameCanvas.startOKDlg("Hỏi admin đi cu !");
    			break;
    		case 1005:
    			try
    			{
    				GameMidlet.instance.platformRequest("http://ngocrongonline.com");
    				break;
    			}
    			catch (Exception)
    			{
    				break;
    			}
    		case 10041:
    			Rms.saveRMSInt("lowGraphic", 0);
    			GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
    			break;
    		case 10042:
    			Rms.saveRMSInt("lowGraphic", 1);
    			GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
    			break;
    		case 2001:
    			if (isCheck)
    			{
    				isCheck = false;
    			}
    			else
    			{
    				isCheck = true;
    			}
    			break;
    		case 2002:
    			doRegister();
    			break;
    		case 2003:
    			doMenu();
    			break;
    		case 2004:
    			actRegister();
    			break;
    		case 2008:
    			Rms.saveRMSString("acc", tfUser.getText().Trim());
    			Rms.saveRMSString("pass", tfPass.getText().Trim());
    			if (ServerListScreen.loadScreen)
    			{
    				GameCanvas.serverScreen.switchToMe();
    			}
    			else
    			{
    				GameCanvas.serverScreen.show2();
    			}
    			break;
    		case 2101:
    			// Doi mat khau: tai khoan va mat khau HIEN TAI lay tu hai o dang nhap,
    			// roi hoi mat khau moi hai lan.
    			if (tfUser.getText().Trim().Length == 0 || tfPass.getText().Trim().Length == 0)
    			{
    				GameCanvas.startOKDlg("Nhập tài khoản và mật khẩu HIỆN TẠI vào hai ô, rồi bấm Đổi M.khẩu.");
    				break;
    			}
    			GameCanvas.inputDlg.show("Mật khẩu mới (ít nhất 5 ký tự)", new Command(mResources.OK, this, 2102, null), TField.INPUT_TYPE_PASSWORD);
    			break;
    		case 2102:
    			mkMoiTam = GameCanvas.inputDlg.tfInput.getText().Trim();
    			if (mkMoiTam.Length < 5)
    			{
    				GameCanvas.endDlg();
    				GameCanvas.startOKDlg("Mật khẩu mới phải có ít nhất 5 ký tự.");
    				break;
    			}
    			GameCanvas.inputDlg.show("Nhập lại mật khẩu mới", new Command(mResources.OK, this, 2103, null), TField.INPUT_TYPE_PASSWORD);
    			break;
    		case 2103:
    		{
    			string nhapLai = GameCanvas.inputDlg.tfInput.getText().Trim();
    			GameCanvas.endDlg();
    			if (!nhapLai.Equals(mkMoiTam))
    			{
    				GameCanvas.startOKDlg("Hai lần nhập mật khẩu mới không khớp.");
    				break;
    			}
    			GameCanvas.startWaitDlg();
    			GameCanvas.connect();
    			Service.gI().setClientType();
    			Service.gI().doiMatKhau(tfUser.getText().Trim(), tfPass.getText().Trim(), mkMoiTam);
    			break;
    		}
    		case 4000:
    			doRegister(tfUser.getText());
    			break;
    		}
    	}
    
    	public void actRegisterLeft()
    	{
    		if (isLogin2)
    		{
    			doLogin();
    			return;
    		}
    		isRes = false;
    		tfPass.isFocus = false;
    		tfUser.isFocus = true;
    		left = cmdMenu;
    	}
    
    	public void actRegister()
    	{
    		GameCanvas.endDlg();
    		isRes = true;
    		tfPass.isFocus = false;
    		tfUser.isFocus = true;
    	}
    
    	public void backToRegister()
    	{
    		GameCanvas.timeBreakLoading = mSystem.currentTimeMillis() + 30000;
    		ServerListScreen.countDieConnect = 0;
    		if (GameCanvas.loginScr.isLogin2)
    		{
    			GameCanvas.startYesNoDlg(mResources.note, new Command(mResources.YES, GameCanvas.panel, 10019, null), new Command(mResources.NO, GameCanvas.panel, 10020, null));
    			return;
    		}
    		if (Main.isWindowsPhone)
    		{
    			GameMidlet.isBackWindowsPhone = true;
    		}
    		GameCanvas.instance.resetToLoginScr = false;
    		GameCanvas.instance.doResetToLoginScr(GameCanvas.loginScr);
    	}
    }
}
