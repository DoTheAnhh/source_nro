using System;
using UnityEngine;

namespace Game6
{
    
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
    		tfUser.setText(Rms.loadRMSString("acc6"));
    		tfPass.setText(Rms.loadRMSString("pass6"));
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
    		// Form tu ve nut va tu tinh bo cuc (xem veForm).
    		khoiTaoFormMoi();
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
    		// Mo lai man dang nhap (dang xuat, doi may chu...) thi ve che do Dang
    		// nhap — tru khi vua bam Dang ky: SoundMn goi actRegister TRUOC
    		// switchToMe, ServerListScreen goi SAU, nen can moc thoi gian.
    		if (cheDo != CD_DANG_NHAP && mSystem.currentTimeMillis() - mocMoDangKy > 1000L)
    		{
    			datCheDo(CD_DANG_NHAP);
    		}
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
    		string text = Rms.loadRMSString("acc6");
    		if (text != null && !text.Equals(string.Empty))
    		{
    			tfUser.setText(text);
    		}
    		string text2 = Rms.loadRMSString("pass6");
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

    		if (!matGo.Equals(tfPass2 == null ? matGo : tfPass2.getText()))
    		{
    			GameCanvas.startOKDlg("Hai ô mật khẩu không khớp nhau, hãy gõ lại.");
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
    		Service.gI().requestRegister(user, tfPass.getText(), Rms.loadRMSString("userAo6" + ServerListScreen.ipSelect), Rms.loadRMSString("passAo" + ServerListScreen.ipSelect), GameMidlet.VERSION);
    		Rms.saveRMSString("acc6", user);
    		Rms.saveRMSString("pass6", tfPass.getText());
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
    		string text = Rms.loadRMSString("acc6");
    		string text2 = Rms.loadRMSString("pass6");
    		if (text != null && !text.Equals(string.Empty))
    		{
    			isLogin2 = false;
    		}
    		else if (Rms.loadRMSString("userAo6" + ServerListScreen.ipSelect) != null && !Rms.loadRMSString("userAo6" + ServerListScreen.ipSelect).Equals(string.Empty))
    		{
    			isLogin2 = true;
    		}
    		else
    		{
    			isLogin2 = false;
    		}
    		if ((text == null || text.Equals(string.Empty)) && isLogin2)
    		{
    			text = Rms.loadRMSString("userAo6" + ServerListScreen.ipSelect);
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
    			Rms.saveRMSString("acc6", tfUser.getText().ToLower().Trim());
    			Rms.saveRMSString("pass6", tfPass.getText().ToLower().Trim());
    		}
    		else
    		{
    			Rms.saveRMSInt("check", 2);
    			Rms.saveRMSString("acc6", string.Empty);
    			Rms.saveRMSString("pass6", string.Empty);
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
    		capNhatO();
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
    		foreach (TField t in oCuaCheDo())
    		{
    			if (t.isFocus)
    			{
    				t.keyPressed(keyCode);
    				break;
    			}
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
    			veForm(g);
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
    		if (isContinueToLogin)
    		{
    			return;
    		}
    		// Len / xuong (hoac Tab) chuyen o, vong quanh cac o cua che do dang hien.
    		bool len = GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21];
    		bool xuong = GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22] || GameCanvas.keyPressed[16];
    		if ((len || xuong) && !(isLogin2 && cheDo == CD_DANG_NHAP))
    		{
    			TField[] os = oCuaCheDo();
    			int dang = -1;
    			for (int i = 0; i < os.Length; i++)
    			{
    				if (os[i].isFocus)
    				{
    					dang = i;
    				}
    			}
    			int toi = (dang < 0) ? 0 : ((dang + (len ? os.Length - 1 : 1)) % os.Length);
    			for (int i = 0; i < os.Length; i++)
    			{
    				os[i].setFocus(i == toi);
    			}
    			focus = toi;
    			GameCanvas.clearKeyPressed();
    		}
    		// Enter tren may tinh = nut chinh cua che do (Dang nhap / Dang ky / Doi).
    		if (Main.isPC && GameCanvas.keyPressed[25])
    		{
    			GameCanvas.keyPressed[25] = false;
    			int id = nutChinh();
    			if (id >= 0)
    			{
    				perform(id, null);
    			}
    		}
    		chamForm();
    		base.updateKey();
    		GameCanvas.clearKeyPressed();
    	}


        // =====================================================================
        //  FORM DANG NHAP / DANG KY / DOI MAT KHAU / QUEN MAT KHAU
        // =====================================================================
        //
        // Ban cu dat o nhap va nut theo so co dinh tinh MOT LAN luc dung man
        // (khung rong 180 nhung o nhap tinh theo 184, nut "Hien" tho ra ngoai
        // khung, nut lay rong chuan 68 nen tran tren may hep). Tren dien thoai,
        // bat ban phim hay xoay man hinh la cac thu lech nhau — "vo UI".
        //
        // Nay bo cuc TINH LAI MOI KHUNG HINH tu GameCanvas.w/h: khung vua man,
        // o nhap va nut deu nam trong khung, nut chia deu be ngang. Ban phim dien
        // thoai dang bat thi khung day len de o dang go khong bi che.

        private const int CD_DANG_NHAP = 0;
        private const int CD_DANG_KY = 1;
        private const int CD_DOI_MK = 2;
        private const int CD_QUEN_MK = 3;

        /// <summary>Che do dang hien.</summary>
        private int cheDo = CD_DANG_NHAP;

        /// <summary>Luc actRegister bat che do dang ky (xem switchToMe).</summary>
        private long mocMoDangKy;

        /// <summary>Nhap lai mat khau (dang ky).</summary>
        private TField tfPass2;

        /// <summary>Mat khau moi va nhap lai (doi mat khau).</summary>
        private TField tfMkMoi;
        private TField tfMkMoi2;

        private int xForm;
        private int yForm;
        private int wForm;
        private int hForm;

        /// <summary>Nut cua form vua ve: x, y, w, h, idAction.</summary>
        private readonly System.Collections.Generic.List<int[]> oNutForm =
                new System.Collections.Generic.List<int[]>();

        private void khoiTaoFormMoi()
        {
            tfPass2 = taoOMatKhau("Nhập lại mật khẩu");
            tfMkMoi = taoOMatKhau("Mật khẩu mới");
            tfMkMoi2 = taoOMatKhau("Nhập lại mật khẩu mới");
            // Form tu ve nut cua minh. Ba o nut mem cu (trai / giua / phai) ve
            // chong len form o cho khac nhau tuy man — bo han.
            center = null;
            left = null;
            right = null;
        }

        private static TField taoOMatKhau(string ten)
        {
            TField t = new TField();
            t.setIputType(TField.INPUT_TYPE_PASSWORD);
            t.name = ten;
            t.height = mScreen.ITEM_HEIGHT + 2;
            return t;
        }

        /// <summary>Doi che do, don cac o cua che do moi.</summary>
        private void datCheDo(int cd)
        {
            cheDo = cd;
            isRes = cd == CD_DANG_KY;
            if (tfPass2 == null)
            {
                khoiTaoFormMoi();
            }
            tfPass2.setText(string.Empty);
            tfMkMoi.setText(string.Empty);
            tfMkMoi2.setText(string.Empty);
            apDungHienMk();
            foreach (TField t in tatCaO())
            {
                t.isFocus = false;
            }
            center = null;
            left = null;
            right = null;
        }

        private TField[] tatCaO()
        {
            return new TField[] { tfUser, tfPass, tfPass2, tfMkMoi, tfMkMoi2 };
        }

        /// <summary>Cac o cua che do dang hien, theo thu tu tu tren xuong.</summary>
        private TField[] oCuaCheDo()
        {
            if (tfPass2 == null)
            {
                khoiTaoFormMoi();
            }
            switch (cheDo)
            {
                case CD_DANG_KY:
                    return new TField[] { tfUser, tfPass, tfPass2 };
                case CD_DOI_MK:
                    return new TField[] { tfUser, tfPass, tfMkMoi, tfMkMoi2 };
                case CD_QUEN_MK:
                    return new TField[] { tfUser };
                default:
                    return new TField[] { tfUser, tfPass };
            }
        }

        private string[] nhanCuaCheDo()
        {
            switch (cheDo)
            {
                case CD_DANG_KY:
                    return new string[] { "Tài khoản", "Mật khẩu", "Nhập lại mật khẩu" };
                case CD_DOI_MK:
                    return new string[] { "Tài khoản", "Mật khẩu hiện tại", "Mật khẩu mới",
                        "Nhập lại mật khẩu mới" };
                case CD_QUEN_MK:
                    return new string[] { "Tài khoản" };
                default:
                    return new string[] { "Tài khoản", "Mật khẩu" };
            }
        }

        private string tieuDeCheDo()
        {
            switch (cheDo)
            {
                case CD_DANG_KY:
                    return "ĐĂNG KÝ TÀI KHOẢN";
                case CD_DOI_MK:
                    return "ĐỔI MẬT KHẨU";
                case CD_QUEN_MK:
                    return "QUÊN MẬT KHẨU";
                default:
                    return "ĐĂNG NHẬP";
            }
        }

        /// <summary>
        /// Hang nut cua che do: moi hang mot mang { chu, chu ngan, idAction, chinh }.
        /// </summary>
        private object[][][] nutCuaCheDo()
        {
            switch (cheDo)
            {
                case CD_DANG_KY:
                    return new object[][][] {
                        new object[][] {
                            new object[] { "Đăng ký", "Đăng ký", 2002, true },
                            new object[] { "Quay lại", "Quay lại", 2200, false } } };
                case CD_DOI_MK:
                    return new object[][][] {
                        new object[][] {
                            new object[] { "Đổi mật khẩu", "Đổi MK", 2101, true },
                            new object[] { "Quay lại", "Quay lại", 2200, false } } };
                case CD_QUEN_MK:
                    return new object[][][] {
                        new object[][] {
                            new object[] { "Liên hệ admin", "Liên hệ", 1005, true },
                            new object[] { "Quay lại", "Quay lại", 2200, false } } };
                default:
                    return new object[][][] {
                        new object[][] {
                            new object[] { "Đăng nhập", "Đăng nhập", 2008, true },
                            new object[] { "Đăng ký", "Đăng ký", 2201, false } },
                        new object[][] {
                            new object[] { "Đổi mật khẩu", "Đổi MK", 2202, false },
                            new object[] { "Quên mật khẩu", "Quên MK", 2203, false } } };
            }
        }

        /// <summary>Dong chu giai thich cua che do Quen mat khau.</summary>
        private static readonly string[] CHU_QUEN_MK = {
            "Tài khoản không gắn email hay số điện thoại,",
            "nên admin sẽ kiểm tra và cấp lại mật khẩu.",
            "Bấm Liên hệ admin, gửi tên tài khoản của bạn."
        };

        private const int DE_TRONG = 12;
        private const int CAO_NUT = 24;
        private const int CAO_TIEU_DE_FORM = 20;

        /// <summary>O mat khau duoc gan nut an / hien (o mat khau dau tien).</summary>
        private TField oCoNutMat()
        {
            return cheDo == CD_QUEN_MK ? null : tfPass;
        }

        /// <summary>Tinh vi tri khung, o nhap va nut — goi moi khung hinh.</summary>
        private void tinhBoCuc()
        {
            TField[] os = oCuaCheDo();
            int fh = mScreen.ITEM_HEIGHT + 2;
            bool coNhan = GameCanvas.h >= 280;
            int caoO = (coNhan ? 12 : 0) + fh + 7;
            object[][][] nut = nutCuaCheDo();
            int caoChu = cheDo == CD_QUEN_MK ? CHU_QUEN_MK.Length * 12 + 6 : 0;

            int w = Math.min(GameCanvas.w - 16, 264);
            if (w < 170)
            {
                w = GameCanvas.w - 4;
            }
            int h = 8 + CAO_TIEU_DE_FORM + os.Length * caoO + caoChu + 2
                    + nut.Length * (CAO_NUT + 6) + 4;
            int x = (GameCanvas.w - w) / 2;
            int y;
            bool banPhim = !Main.isPC && TouchScreenKeyboard.visible;
            if (banPhim)
            {
                // Ban phim chiem nua duoi man: day khung len sat tren, va neu o
                // dang go van nam thap thi day them cho no hien tren ban phim.
                y = 6;
                // Chieu cao ban phim that (diem anh -> diem logic). Mot so may
                // Android tra 0: khi do lay 60% man, ban phim ngang thuong co vay.
                int caoPhim = 0;
                try
                {
                    caoPhim = (int)(TouchScreenKeyboard.area.height / mGraphics.zoomLevel);
                }
                catch (Exception)
                {
                }
                if (caoPhim <= 0 || caoPhim > GameCanvas.h * 3 / 4)
                {
                    caoPhim = GameCanvas.h * 60 / 100;
                }
                for (int i = 0; i < os.Length; i++)
                {
                    if (os[i].isFocus)
                    {
                        int day = y + 8 + CAO_TIEU_DE_FORM + (i + 1) * caoO;
                        int tran = GameCanvas.h - caoPhim - 4;
                        if (day > tran)
                        {
                            y -= day - tran;
                        }
                    }
                }
            }
            else
            {
                int duoiLogo = (imgTitle != null && GameCanvas.h > 220)
                        ? 60 + imgTitle.getHeight() / 2 + 8 : 8;
                y = Math.max(duoiLogo, (GameCanvas.h - h) / 2 + 20);
                if (y + h > GameCanvas.h - 30)
                {
                    y = Math.max(4, GameCanvas.h - 30 - h);
                }
            }
            // Khung PopUp ghep bang o 10 diem: canh le khong chia het cho 10
            // thi o cuoi ve lan ra ngoai goc khung (vet rang cua o mep).
            w = 20 + (w - 20) / 10 * 10;
            h = 20 + (h - 20 + 9) / 10 * 10;
            x = (GameCanvas.w - w) / 2;
            xForm = x;
            yForm = y;
            wForm = w;
            hForm = h;

            // O nhap.
            int yc = y + 8 + CAO_TIEU_DE_FORM;
            TField coMat = oCoNutMat();
            wNutMat = 0;
            for (int i = 0; i < os.Length; i++)
            {
                TField t = os[i];
                t.height = fh;
                t.x = x + DE_TRONG;
                t.y = yc + (coNhan ? 12 : 0);
                t.width = w - DE_TRONG * 2;
                if (t == coMat)
                {
                    // Nut an / hien sat mep phai khung. Chua 22 diem giua o nhap va
                    // nut: vung 20 diem quanh mep phai o nhap la nut XOA chu cua
                    // TField, dat nut vao do thi bam an / hien lai xoa sach.
                    wNutMat = fh;
                    xNutMat = x + w - DE_TRONG - wNutMat;
                    yNutMat = t.y;
                    t.width = xNutMat - 22 - t.x;
                }
                yc += caoO;
            }

            // Nut.
            oNutForm.Clear();
            int yn = yc + caoChu + 2;
            for (int hang = 0; hang < nut.Length; hang++)
            {
                int k = nut[hang].Length;
                int bw = (w - DE_TRONG * 2 - (k - 1) * 6) / k;
                for (int i = 0; i < k; i++)
                {
                    oNutForm.Add(new int[] { x + DE_TRONG + i * (bw + 6), yn, bw, CAO_NUT,
                        (int)nut[hang][i][2], hang, i });
                }
                yn += CAO_NUT + 6;
            }
        }

        private void veForm(mGraphics g)
        {
            tinhBoCuc();
            TField[] os = oCuaCheDo();
            string[] nhan = nhanCuaCheDo();
            bool coNhan = GameCanvas.h >= 280;

            // Logo chi ve khi con cho phia tren khung, khong de len form.
            if (imgTitle != null && GameCanvas.h > 220
                    && yForm >= 60 + imgTitle.getHeight() / 2 + 4)
            {
                g.drawImage(imgTitle, GameCanvas.hw, 60, 3);
            }

            PopUp.paintPopUp(g, xForm, yForm, wForm, hForm, -1, true);
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

            // Tieu de + gach chan.
            mFont.tahoma_7b_dark.drawString(g, tieuDeCheDo(), xForm + wForm / 2, yForm + 9,
                    mFont.CENTER);
            g.setColor(0xB07A3E, 0.6f);
            g.fillRect(xForm + DE_TRONG, yForm + 8 + CAO_TIEU_DE_FORM - 3, wForm - DE_TRONG * 2, 1);

            for (int i = 0; i < os.Length; i++)
            {
                if (coNhan)
                {
                    mFont.tahoma_7_grey.drawString(g, nhan[i], os[i].x + 2, os[i].y - 12,
                            mFont.LEFT);
                }
                os[i].paint(g);
                // TField.paint de lai vung cat bang khung chu cua no.
                g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            }

            if (wNutMat > 0)
            {
                veNutForm(g, xNutMat, yNutMat, wNutMat, wNutMat,
                        hienMatKhau ? "Ẩn" : "Hiện", false);
            }

            if (cheDo == CD_QUEN_MK)
            {
                int yc = os[os.Length - 1].y + os[os.Length - 1].height + 8;
                for (int i = 0; i < CHU_QUEN_MK.Length; i++)
                {
                    mFont.tahoma_7_grey.drawString(g, CHU_QUEN_MK[i], xForm + wForm / 2,
                            yc + i * 12, mFont.CENTER);
                }
            }

            object[][][] nut = nutCuaCheDo();
            foreach (int[] o in oNutForm)
            {
                object[] n = nut[o[5]][o[6]];
                string chu = (string)n[0];
                if (mFont.tahoma_7b_white.getWidth(chu) + 10 > o[2])
                {
                    chu = (string)n[1];
                }
                veNutForm(g, o[0], o[1], o[2], o[3], chu, (bool)n[3]);
            }
        }

        /// <summary>Mot nut cua form: vien nau, than cam (nut chinh) hoac kem.</summary>
        private static void veNutForm(mGraphics g, int x, int y, int w, int h, string chu,
                bool chinh)
        {
            bool dangNhan = GameCanvas.isPointerDown && GameCanvas.isPointerHoldIn(x, y, w, h);
            g.setColor(0x000000, 0.25f);
            g.fillRect(x + 1, y + 2, w, h, 6);
            g.setColor(0x7A3F12, 1f);
            g.fillRect(x, y, w, h, 6);
            int mau = chinh ? (dangNhan ? 0xC96A1C : 0xF08A2A) : (dangNhan ? 0xD9B27A : 0xF3D9A8);
            g.setColor(mau, 1f);
            g.fillRect(x + 1, y + 1, w - 2, h - 2, 5);
            g.setColor(0xFFFFFF, dangNhan ? 0.08f : 0.22f);
            g.fillRect(x + 3, y + 2, w - 6, h / 2 - 2, 4);
            if (chinh)
            {
                mFont.tahoma_7b_dark.drawString(g, chu, x + w / 2 + 1, y + h / 2 - 5, mFont.CENTER);
                mFont.tahoma_7b_white.drawString(g, chu, x + w / 2, y + h / 2 - 6, mFont.CENTER);
            }
            else
            {
                mFont.tahoma_7b_dark.drawString(g, chu, x + w / 2, y + h / 2 - 6, mFont.CENTER);
            }
        }

        /// <summary>Doi mat khau: kiem tra ca bon o roi gui mot lan.</summary>
        private void doDoiMatKhau()
        {
            string tk = tfUser.getText().Trim();
            string cu = tfPass.getText().Trim();
            string moi = tfMkMoi.getText().Trim();
            string lai = tfMkMoi2.getText().Trim();
            if (tk.Length == 0 || cu.Length == 0)
            {
                GameCanvas.startOKDlg("Nhập tài khoản và mật khẩu hiện tại.");
                return;
            }
            if (moi.Length < 5)
            {
                GameCanvas.startOKDlg("Mật khẩu mới phải có ít nhất 5 ký tự.");
                return;
            }
            string loi = loiChuThuongVaSo(moi, mResources.matKhauChu);
            if (loi != null)
            {
                GameCanvas.startOKDlg(loi);
                return;
            }
            if (!moi.Equals(lai))
            {
                GameCanvas.startOKDlg("Hai ô mật khẩu mới không khớp nhau.");
                return;
            }
            if (moi.Equals(cu))
            {
                GameCanvas.startOKDlg("Mật khẩu mới phải khác mật khẩu hiện tại.");
                return;
            }
            GameCanvas.startWaitDlg();
            GameCanvas.connect();
            Service.gI().setClientType();
            Service.gI().doiMatKhau(tk, cu, moi);
        }

        /// <summary>Nut chinh cua che do dang hien (phim Enter tren may tinh).</summary>
        private int nutChinh()
        {
            foreach (object[][] hang in nutCuaCheDo())
            {
                foreach (object[] n in hang)
                {
                    if ((bool)n[3])
                    {
                        return (int)n[2];
                    }
                }
            }
            return -1;
        }

        /// <summary>Bam cham cho form: nut an / hien va cac nut. Tra true khi da nuot.</summary>
        private bool chamForm()
        {
            if (!GameCanvas.isPointerJustRelease)
            {
                return false;
            }
            if (wNutMat > 0 && GameCanvas.isPointerHoldIn(xNutMat, yNutMat, wNutMat, wNutMat))
            {
                hienMatKhau = !hienMatKhau;
                apDungHienMk();
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            foreach (int[] o in oNutForm)
            {
                if (GameCanvas.isPointerHoldIn(o[0], o[1], o[2], o[3]))
                {
                    GameCanvas.clearAllPointerEvent();
                    SoundMn.gI().buttonClick();
                    perform(o[4], null);
                    return true;
                }
            }
            return false;
        }

        /// <summary>An / hien ap cho MOI o mat khau, khong rieng o dau.</summary>
        private void apDungHienMk()
        {
            foreach (TField t in new TField[] { tfPass, tfPass2, tfMkMoi, tfMkMoi2 })
            {
                if (t != null)
                {
                    t.setIputType(hienMatKhau ? TField.INPUT_TYPE_ANY : TField.INPUT_TYPE_PASSWORD);
                    t.setText(t.getText());
                }
            }
        }

        /// <summary>Cap nhat cac o cua che do dang hien — moi khung mot lan.</summary>
        private void capNhatO()
        {
            if (tfPass2 == null)
            {
                khoiTaoFormMoi();
            }
            tfUser.name = "Tên tài khoản";
            tfPass.name = cheDo == CD_DOI_MK ? "Mật khẩu hiện tại" : "Mật khẩu";
            // Dang nhap bang tai khoan ao (isLogin2) thi hai o chi de xem.
            bool chiXem = isLogin2 && cheDo == CD_DANG_NHAP;
            foreach (TField t in oCuaCheDo())
            {
                t.update();
                if (chiXem)
                {
                    t.isPaintCarret = false;
                }
            }
            center = null;
            left = null;
            right = null;
        }

        /// <summary>
        /// Controller hoi truoc khi day sang man chon may chu vi co thong bao
        /// (goi -26). Dang ky / doi mat khau loi thi GIU form de nguoi choi sua
        /// lai o sai — ban cu day di mat, phai go lai tu dau.
        /// </summary>
        public bool giuFormKhiBao(string thongBao)
        {
            if (cheDo == CD_DOI_MK)
            {
                if (thongBao != null && thongBao.StartsWith("Đổi mật khẩu thành công"))
                {
                    // Xong: ve man dang nhap, o mat khau dien san mat khau moi.
                    string moi = tfMkMoi.getText().Trim();
                    datCheDo(CD_DANG_NHAP);
                    tfPass.setText(moi);
                }
                return true;
            }
            return cheDo == CD_DANG_KY;
        }

        /// <summary>Mo trang lien he admin (link web cua may chu).</summary>
        private static void moLienHe()
        {
            string link = ServerListScreen.linkweb;
            if (link == null || link.Trim().Length == 0)
            {
                GameCanvas.startOKDlg("Hãy liên hệ admin qua fanpage / nhóm của máy chủ để lấy lại mật khẩu.");
                return;
            }
            link = link.Trim();
            if (!link.StartsWith("http"))
            {
                link = "http://" + link;
            }
            try
            {
                GameMidlet.instance.platformRequest(link);
            }
            catch (Exception)
            {
            }
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
    			string text = Rms.loadRMSString("userAo6" + ServerListScreen.ipSelect);
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
    	case 2203:
    		datCheDo(CD_QUEN_MK);
    		break;
    	case 1005:
    		moLienHe();
    		break;
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
    			Rms.saveRMSString("acc6", tfUser.getText().Trim());
    			Rms.saveRMSString("pass6", tfPass.getText().Trim());
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
    		doDoiMatKhau();
    		break;
    	case 2200:
    		datCheDo(CD_DANG_NHAP);
    		setUserPass();
    		break;
    	case 2201:
    		datCheDo(CD_DANG_KY);
    		tfUser.setText(string.Empty);
    		tfPass.setText(string.Empty);
    		break;
    	case 2202:
    		datCheDo(CD_DOI_MK);
    		break;
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
    		if (cheDo != CD_DANG_NHAP)
    		{
    			datCheDo(CD_DANG_NHAP);
    		}
    		tfPass.isFocus = false;
    		tfUser.isFocus = true;
    	}
    	
    	public void actRegister()
    	{
    		GameCanvas.endDlg();
    		datCheDo(CD_DANG_KY);
    		mocMoDangKy = mSystem.currentTimeMillis();
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
