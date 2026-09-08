namespace Game1
{
    using System;
    
    public class ServerListScreen : mScreen, IActionListener
    {
    	public static string[] nameServer;
    
    	public static string[] address;
    
    	public static sbyte serverPriority;
    
    	public static bool[] hasConnected;
    
    	public static short[] port;
    
    	public static int selected;
    
    	public static bool isWait;
    
    	public static Command cmdUpdateServer;
    
    	public static sbyte[] language;
    
    	public static sbyte[] typeSv;
    
    	public static sbyte[] isNew;
    
    	private Command[] cmd;
    
    	private Command cmdCallHotline;
    
    	private int nCmdPlay;
    
    	public static Command cmdDeleteRMS;
    
    	private int lY;
    
    	public static string smartPhoneVN;
    
    	public static string javaVN;
    
    	public static string smartPhoneIn;
    
    	public static string javaIn;
    
    	public static string smartPhoneE;
    
    	public static string javaE;
    
    	public static string linkGetHost;
    
    	public static string linkDefault;
    
    	public const sbyte languageVersion = 2;
    
    	public new int keyTouch = -1;
    
    	private int tam;
    
    	public static bool stopDownload;
    
    	public static string linkweb;
    
    	public static int countDieConnect;
    
    	public static bool waitToLogin;
    
    	public static int tWaitToLogin;
    
    	public static string RMS_NRlink;
    
    	public static int[] lengthServer;
    
    	public static int ipSelect;
    
    	public static int flagServer;
    
    	public static bool bigOk;
    
    	public static int percent;
    
    	public static string strWait;
    
    	public static int nBig;
    
    	public static int nBg;
    
    	public static int demPercent;
    
    	public static int maxBg;
    
    	public static bool isGetData;
    
    	public static Command cmdDownload;
    
    	private Command cmdStart;
    
    	public string dataSize;
    
    	public static int p;
    
    	public static int testConnect;
    
    	public static bool loadScreen;
    
    	public static bool isAutoConect;
    
    	public ServerListScreen()
    	{
    		int num = 4;
    		int num2 = num * 32 + 23 + 33;
    		if (num2 >= GameCanvas.w)
    		{
    			num--;
    			num2 = num * 32 + 23 + 33;
    		}
    		initCommand();
    		if (!GameCanvas.isTouch)
    		{
    			selected = 0;
    			processInput();
    		}
    		GameScr.loadCamera(true, -1, -1);
    		GameScr.cmx = 100;
    		GameScr.cmy = 200;
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
    				int num3 = 2;
    				cmdCallHotline.y = num3 + 6;
    			}
    		}
    		cmdUpdateServer = new Command();
    		cmdUpdateServer.actionChat = delegate(string str)
    		{
    			string text = str;
    			string text2 = str;
    			if (text == null)
    			{
    				text = linkDefault;
    			}
    			else
    			{
    				if (text == null && text2 != null)
    				{
    					if (text2.Equals(string.Empty) || text2.Length < 20)
    					{
    						text2 = linkDefault;
    					}
    					getServerList(text2);
    				}
    				if (text != null && text2 == null)
    				{
    					if (text.Equals(string.Empty) || text.Length < 20)
    					{
    						text = linkDefault;
    					}
    					getServerList(text);
    				}
    				if (text != null && text2 != null)
    				{
    					if (text.Length > text2.Length)
    					{
    						getServerList(text);
    					}
    					else
    					{
    						getServerList(text2);
    					}
    				}
    			}
    		};
    		setLinkDefault(mSystem.LANGUAGE);
    	}
    
    	public static void createDeleteRMS()
    	{
    		if (cmdDeleteRMS == null)
    		{
    			if (GameCanvas.serverScreen == null)
    			{
    				GameCanvas.serverScreen = new ServerListScreen();
    			}
    			cmdDeleteRMS = new Command(string.Empty, GameCanvas.serverScreen, 14, null);
    			cmdDeleteRMS.x = GameCanvas.w - 78;
    			cmdDeleteRMS.y = GameCanvas.h - 26;
    		}
    	}
    
    	private void initCommand()
    	{
    		nCmdPlay = 0;
    		string text = Rms.loadRMSString("acc");
    		if (text == null)
    		{
    			if (Rms.loadRMS("userAo" + ipSelect) != null)
    			{
    				nCmdPlay = 1;
    			}
    		}
    		else if (text.Equals(string.Empty))
    		{
    			if (Rms.loadRMS("userAo" + ipSelect) != null)
    			{
    				nCmdPlay = 1;
    			}
    		}
    		else
    		{
    			nCmdPlay = 1;
    		}
    		cmd = new Command[(mGraphics.zoomLevel <= 1) ? (4 + nCmdPlay) : (3 + nCmdPlay)];
    		int num = GameCanvas.hh - 15 * cmd.Length + 28;
    		for (int i = 0; i < cmd.Length; i++)
    		{
    			switch (i)
    			{
    			case 0:
    				cmd[0] = new Command(string.Empty, this, 3, null);
    				if (text == null)
    				{
    					cmd[0].caption = mResources.playNew;
    					if (Rms.loadRMS("userAo" + ipSelect) != null)
    					{
    						cmd[0].caption = mResources.choitiep;
    					}
    					break;
    				}
    				if (text.Equals(string.Empty))
    				{
    					cmd[0].caption = mResources.playNew;
    					if (Rms.loadRMS("userAo" + ipSelect) != null)
    					{
    						cmd[0].caption = mResources.choitiep;
    					}
    					break;
    				}
    				cmd[0].caption = mResources.playAcc + ": " + text;
    				if (cmd[0].caption.Length > 23)
    				{
    					cmd[0].caption = cmd[0].caption.Substring(0, 23);
    					cmd[0].caption += "...";
    				}
    				break;
    			case 1:
    				if (nCmdPlay == 1)
    				{
    					cmd[1] = new Command(string.Empty, this, 10100, null);
    					cmd[1].caption = mResources.playNew;
    				}
    				else
    				{
    					cmd[1] = new Command(mResources.change_account, this, 7, null);
    				}
    				break;
    			case 2:
    				if (nCmdPlay == 1)
    				{
    					cmd[2] = new Command(mResources.change_account, this, 7, null);
    				}
    				else
    				{
    					cmd[2] = new Command(string.Empty, this, 17, null);
    				}
    				break;
    			case 3:
    				if (nCmdPlay == 1)
    				{
    					cmd[3] = new Command(string.Empty, this, 17, null);
    				}
    				else
    				{
    					cmd[3] = new Command(mResources.option, this, 8, null);
    				}
    				break;
    			case 4:
    				cmd[4] = new Command(mResources.option, this, 8, null);
    				break;
    			}
    			cmd[i].y = num;
    			cmd[i].setType();
    			cmd[i].x = (GameCanvas.w - cmd[i].w) / 2;
    			num += 30;
    		}
    	}
    
    	public static void doUpdateServer()
    	{
    		if (cmdUpdateServer == null && GameCanvas.serverScreen == null)
    		{
    			GameCanvas.serverScreen = new ServerListScreen();
    		}
    	}
    
    	public static void getServerList(string str)
    	{
    		lengthServer = new int[3];
    		string[] array = Res.split(str.Trim(), ",", 0);
    		Res.outz(">>> getServerList= " + str);
    		mResources.loadLanguague(sbyte.Parse(array[array.Length - 2]));
    		nameServer = new string[array.Length - 2];
    		address = new string[array.Length - 2];
    		port = new short[array.Length - 2];
    		language = new sbyte[array.Length - 2];
    		typeSv = new sbyte[array.Length - 2];
    		isNew = new sbyte[array.Length - 2];
    		hasConnected = new bool[2];
    		for (int i = 0; i < array.Length - 2; i++)
    		{
    			string[] array2 = Res.split(array[i].Trim(), ":", 0);
    			nameServer[i] = array2[0];
    			address[i] = array2[1];
    			port[i] = short.Parse(array2[2]);
    			language[i] = sbyte.Parse(array2[3].Trim());
    			try
    			{
    				typeSv[i] = sbyte.Parse(array2[4].Trim());
    			}
    			catch (Exception)
    			{
    				typeSv[i] = 0;
    			}
    			try
    			{
    				isNew[i] = sbyte.Parse(array2[5].Trim());
    			}
    			catch (Exception)
    			{
    				isNew[i] = 0;
    			}
    			lengthServer[language[i]]++;
    		}
    		serverPriority = sbyte.Parse(array[array.Length - 1]);
    		saveIP();
    	}
    
    	public override void paint(mGraphics g)
    	{
    		int num = 105;
    		if (!loadScreen)
    		{
    			g.setColor(0);
    			g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    			if (bigOk)
    			{
    			}
    		}
    		else
    		{
    			GameCanvas.paintBGGameScr(g);
    		}
    		int num2 = 2;
    		mFont.tahoma_7_white.drawString(g, "v" + GameMidlet.VERSION + "(" + mGraphics.zoomLevel + ")", GameCanvas.w - 2, num2 + 15, 1, mFont.tahoma_7_grey);
    		string empty = string.Empty;
    		empty = ((testConnect != 0) ? (empty + nameServer[ipSelect] + " connected") : (empty + nameServer[ipSelect] + " disconnect"));
    		if (mSystem.isTest)
    		{
    			mFont.tahoma_7_white.drawString(g, empty, GameCanvas.w - 2, num2 + 15 + 15, 1, mFont.tahoma_7_grey);
    		}
    		if (!isGetData || loadScreen)
    		{
    			if (mSystem.clientType == 1 && !GameCanvas.isTouch)
    			{
    				mFont.tahoma_7_white.drawString(g, linkweb, GameCanvas.w - 2, GameCanvas.h - 15, 1, mFont.tahoma_7_grey);
    			}
    			else
    			{
    				mFont.tahoma_7_white.drawString(g, linkweb, GameCanvas.w - 2, num2, 1, mFont.tahoma_7_grey);
    			}
    		}
    		else
    		{
    			mFont.tahoma_7_white.drawString(g, linkweb, GameCanvas.w - 2, num2, 1, mFont.tahoma_7_grey);
    		}
    		int num3 = ((GameCanvas.w < 200) ? 160 : 180);
    		if (cmdDeleteRMS != null)
    		{
    			mFont.tahoma_7_white.drawString(g, mResources.xoadulieu, GameCanvas.w - 2, GameCanvas.h - 15, 1, mFont.tahoma_7_grey);
    		}
    		if (GameCanvas.currentDialog == null)
    		{
    			if (!loadScreen)
    			{
    				if (!bigOk)
    				{
    					g.drawImage(LoginScr.imgTitle, GameCanvas.hw, GameCanvas.hh - 32, 3);
    					if (!isGetData)
    					{
    						mFont.tahoma_7b_white.drawString(g, mResources.taidulieudechoi, GameCanvas.hw, GameCanvas.hh + 24, 2);
    						if (cmdDownload != null)
    						{
    							cmdDownload.paint(g);
    						}
    					}
    					else
    					{
    						if (cmdDownload != null)
    						{
    							cmdDownload.paint(g);
    						}
    						mFont.tahoma_7b_white.drawString(g, mResources.downloading_data + percent + "%", GameCanvas.w / 2, GameCanvas.hh + 24, 2);
    						GameScr.paintOngMauPercent(GameScr.frBarPow20, GameScr.frBarPow21, GameScr.frBarPow22, GameCanvas.w / 2 - 50, GameCanvas.hh + 45, 100, 100f, g);
    						GameScr.paintOngMauPercent(GameScr.frBarPow0, GameScr.frBarPow1, GameScr.frBarPow2, GameCanvas.w / 2 - 50, GameCanvas.hh + 45, 100, percent, g);
    					}
    				}
    			}
    			else
    			{
    				int num4 = GameCanvas.hh - 15 * cmd.Length - 15;
    				if (num4 < 25)
    				{
    					num4 = 25;
    				}
    				if (LoginScr.imgTitle != null)
    				{
    					g.drawImage(LoginScr.imgTitle, GameCanvas.hw, num4, 3);
    				}
    				for (int i = 0; i < cmd.Length; i++)
    				{
    					cmd[i].paint(g);
    				}
    				g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    				if (testConnect == -1)
    				{
    					if (GameCanvas.gameTick % 20 > 10)
    					{
    						g.drawRegion(GameScr.imgRoomStat, 0, 14, 7, 7, 0, (GameCanvas.w - mFont.tahoma_7b_dark.getWidth(cmd[2 + nCmdPlay].caption) >> 1) - 10, cmd[2 + nCmdPlay].y + 10, 0);
    					}
    				}
    				else
    				{
    					g.drawRegion(GameScr.imgRoomStat, 0, testConnect * 7, 7, 7, 0, (GameCanvas.w - mFont.tahoma_7b_dark.getWidth(cmd[2 + nCmdPlay].caption) >> 1) - 10, cmd[2 + nCmdPlay].y + 9, 0);
    				}
    			}
    		}
    		base.paint(g);
    	}
    
    	public void selectServer()
    	{
    		flagServer = 30;
    		GameCanvas.startWaitDlg(mResources.PLEASEWAIT);
    		Session_ME.gI().close();
    		GameMidlet.IP = address[ipSelect];
    		GameMidlet.PORT = port[ipSelect];
    		GameMidlet.LANGUAGE = language[ipSelect];
    		Rms.saveRMSInt("svselect", ipSelect);
    		if (language[ipSelect] != mResources.language)
    		{
    			mResources.loadLanguague(language[ipSelect]);
    		}
    		LoginScr.serverName = nameServer[ipSelect];
    		initCommand();
    		loadScreen = true;
    		countDieConnect = 0;
    		Controller.isConnectOK = false;
    		testConnect = -1;
    		isAutoConect = true;
    	}
    
    	public override void update()
    	{
    		if (waitToLogin)
    		{
    			tWaitToLogin++;
    			if (tWaitToLogin == 50)
    			{
    				GameCanvas.serverScreen.selectServer();
    			}
    			if (tWaitToLogin == 100)
    			{
    				if (GameCanvas.loginScr == null)
    				{
    					GameCanvas.loginScr = new LoginScr();
    				}
    				GameCanvas.loginScr.doLogin();
    				Service.gI().finishUpdate();
    				waitToLogin = false;
    			}
    		}
    		if (flagServer > 0)
    		{
    			flagServer--;
    			if (flagServer == 0)
    			{
    				GameCanvas.endDlg();
    			}
    			if (testConnect == 2)
    			{
    				flagServer = 0;
    				GameCanvas.endDlg();
    			}
    		}
    		if (flagServer <= 0 && isAutoConect)
    		{
    			countDieConnect++;
    			if (countDieConnect > 100000)
    			{
    				countDieConnect = 0;
    			}
    		}
    		for (int i = 0; i < cmd.Length; i++)
    		{
    			if (i == selected)
    			{
    				cmd[i].isFocus = true;
    			}
    			else
    			{
    				cmd[i].isFocus = false;
    			}
    		}
    		GameScr.cmx++;
    		if (!loadScreen && (bigOk || percent == 100))
    		{
    			cmdDownload = null;
    		}
    		base.update();
    		if (Char.isLoadingMap || !loadScreen || !isAutoConect || GameCanvas.currentScreen != this || testConnect == 2)
    		{
    			return;
    		}
    		if (countDieConnect < ((mSystem.clientType != 1) ? 5 : 2))
    		{
    			if (flagServer <= 0)
    			{
    				flagServer = 30;
    				GameCanvas.startWaitDlg(mResources.PLEASEWAIT);
    				GameCanvas.connect();
    			}
    		}
    		else if (!Session_ME.gI().isConnected())
    		{
    			if (flagServer <= 0)
    			{
    				Command cmdYes = new Command(mResources.YES, GameCanvas.serverScreen, 18, null);
    				Command cmdNo = new Command(mResources.NO, GameCanvas.serverScreen, 19, null);
    				GameCanvas.startYesNoDlg(mResources.maychutathoacmatsong + "." + mResources.confirmChangeServer, cmdYes, cmdNo);
    				flagServer = 30;
    			}
    		}
    		else if (flagServer <= 0)
    		{
    			countDieConnect = 0;
    		}
    	}
    
    	private void processInput()
    	{
    		if (loadScreen)
    		{
    			center = new Command(string.Empty, this, cmd[selected].idAction, null);
    		}
    		else
    		{
    			center = cmdDownload;
    		}
    	}
    
    	public static void updateDeleteData()
    	{
    		if (cmdDeleteRMS != null && cmdDeleteRMS.isPointerPressInside())
    		{
    			cmdDeleteRMS.performAction();
    		}
    	}
    
    	public override void updateKey()
    	{
    		if (GameCanvas.isTouch)
    		{
    			updateDeleteData();
    			if (cmdCallHotline != null && cmdCallHotline.isPointerPressInside())
    			{
    				cmdCallHotline.performAction();
    			}
    			if (!loadScreen)
    			{
    				if (cmdDownload != null && cmdDownload.isPointerPressInside())
    				{
    					cmdDownload.performAction();
    				}
    				base.updateKey();
    				return;
    			}
    			for (int i = 0; i < cmd.Length; i++)
    			{
    				if (cmd[i] == null || !cmd[i].isPointerPressInside())
    				{
    					continue;
    				}
    				if (testConnect == -1 || testConnect == 0)
    				{
    					if (cmd[i].caption.IndexOf(mResources.server) != -1)
    					{
    						cmd[i].performAction();
    					}
    				}
    				else
    				{
    					cmd[i].performAction();
    				}
    			}
    		}
    		else if (loadScreen)
    		{
    			if (GameCanvas.keyPressed[8])
    			{
    				int num = ((mGraphics.zoomLevel <= 1) ? 4 : 2);
    				GameCanvas.keyPressed[8] = false;
    				selected++;
    				if (selected > num)
    				{
    					selected = 0;
    				}
    				processInput();
    			}
    			if (GameCanvas.keyPressed[2])
    			{
    				int num2 = ((mGraphics.zoomLevel <= 1) ? 4 : 2);
    				GameCanvas.keyPressed[2] = false;
    				selected--;
    				if (selected < 0)
    				{
    					selected = num2;
    				}
    				processInput();
    			}
    		}
    		if (!isWait)
    		{
    			base.updateKey();
    		}
    	}
    
    	public static void saveIP()
    	{
    		DataOutputStream dataOutputStream = new DataOutputStream();
    		try
    		{
    			dataOutputStream.writeByte(mResources.language);
    			dataOutputStream.writeByte((sbyte)nameServer.Length);
    			for (int i = 0; i < nameServer.Length; i++)
    			{
    				dataOutputStream.writeUTF(nameServer[i]);
    				dataOutputStream.writeUTF(address[i]);
    				dataOutputStream.writeShort(port[i]);
    				dataOutputStream.writeByte(language[i]);
    				try
    				{
    					dataOutputStream.writeByte(typeSv[i]);
    				}
    				catch (Exception)
    				{
    					dataOutputStream.writeByte(0);
    				}
    				try
    				{
    					dataOutputStream.writeByte(isNew[i]);
    				}
    				catch (Exception)
    				{
    					dataOutputStream.writeByte(0);
    				}
    			}
    			serverPriority = (sbyte)((!mSystem.isTest) ? serverPriority : (serverPriority + 5));
    			dataOutputStream.writeByte(serverPriority);
    			Rms.saveRMS(RMS_NRlink, dataOutputStream.toByteArray());
    			dataOutputStream.close();
    			SplashScr.loadIP();
    		}
    		catch (Exception)
    		{
    		}
    	}
    
    	public static bool allServerConnected()
    	{
    		for (int i = 0; i < 2; i++)
    		{
    			if (!hasConnected[i])
    			{
    				return false;
    			}
    		}
    		return true;
    	}
    
    	/// <summary>
    	/// Ep dia chi may chu ve dung gia tri dang nam trong <c>server_ip.txt</c>.
    	/// </summary>
    	/// <remarks>
    	/// <para>Danh sach may chu duoc cat vao RMS (khoa <c>NRlink…</c>) ngay lan
    	/// chay dau tien. Tu lan thu hai tro di, <see cref="loadIP"/> doc lai danh
    	/// sach do va KHONG he doc lai file — nen doi wifi xong, may chu ghi dia chi
    	/// moi vao file ma client van bam vao dia chi cu con nam trong bo nho dem.
    	/// Do chinh la canh "server dang chay, file dung dia chi, ma client khong
    	/// noi duoc".</para>
    	///
    	/// <para>Nay moi lan khoi dong deu doc lai file va ghi de len bo nho dem:
    	/// file la nguon duy nhat cua dia chi, bo nho dem chi con giu ten may chu
    	/// va ngon ngu.</para>
    	/// </remarks>
    	public static void epDiaChiTuFile()
    	{
    		string ip = diaChiMayChu();
    		if (ip == null || ip.Length == 0)
    		{
    			return;
    		}
    		smartPhoneVN = "DoTheAnh:" + ip + ":14445:0,0,0";
    		javaVN = smartPhoneVN;
    		linkDefault = javaVN;
    		if (address == null || nameServer == null)
    		{
    			return;
    		}
    		for (int i = 0; i < address.Length && i < nameServer.Length; i++)
    		{
    			// Chi sua may chu cua ban thu nay. Cac muc khac trong danh sach la may
    			// chu ngoai, dia chi cua ho khong lien quan gi den file tren may nay.
    			//
    			// Sua them moi muc mang dia chi LOOPBACK, bat ke ten la gi.
    			// 127.0.0.1 tren may nguoi choi tro ve chinh dien thoai ho — khong
    			// bao gio co may chu o do, nen do luon la rac con lai tu mot ban
    			// cu (thoi may chu chay ngay tren may lap trinh). De nguyen thi
    			// nguoi choi bam vao chi nhan duoc "May chu tat hoac mat song".
    			if (nameServer[i] == "DoTheAnh" || laLoopback(address[i]))
    			{
    				address[i] = ip;
    				port[i] = 14445;
    			}
    		}
    	}

    	public static void loadIP()
    	{
    		sbyte[] array = Rms.loadRMS(RMS_NRlink);
    		if (array == null)
    		{
    			// Doc lai file truoc khi dung linkDefault.
    			epDiaChiTuFile();
    			getServerList(linkDefault);
    			return;
    		}
    		DataInputStream dataInputStream = new DataInputStream(array);
    		if (dataInputStream == null)
    		{
    			return;
    		}
    		try
    		{
    			lengthServer = new int[3];
    			mResources.loadLanguague(dataInputStream.readByte());
    			sbyte b = dataInputStream.readByte();
    			nameServer = new string[b];
    			address = new string[b];
    			port = new short[b];
    			language = new sbyte[b];
    			typeSv = new sbyte[b];
    			isNew = new sbyte[b];
    			for (int i = 0; i < b; i++)
    			{
    				nameServer[i] = dataInputStream.readUTF();
    				address[i] = dataInputStream.readUTF();
    				port[i] = dataInputStream.readShort();
    				language[i] = dataInputStream.readByte();
    				try
    				{
    					typeSv[i] = dataInputStream.readByte();
    				}
    				catch (Exception)
    				{
    					typeSv[i] = 0;
    				}
    				try
    				{
    					isNew[i] = dataInputStream.readByte();
    				}
    				catch (Exception)
    				{
    					isNew[i] = 0;
    				}
    				lengthServer[language[i]]++;
    			}
    			serverPriority = dataInputStream.readByte();
    			dataInputStream.close();
    			// Bo nho dem chi giu ten va ngon ngu; dia chi lay tu file.
    			epDiaChiTuFile();
    			SplashScr.loadIP();
    		}
    		catch (Exception)
    		{
    		}
    	}
    
    	public override void switchToMe()
    	{
    		EffectManager.remove();
    		GameScr.cmy = 0;
    		GameScr.cmx = 0;
    		initCommand();
    		isWait = false;
    		GameCanvas.loginScr = null;
    		string text = Rms.loadRMSString("ResVersion");
    		int num = ((text == null || !(text != string.Empty)) ? (-1) : int.Parse(text));
    		if (num > 0)
    		{
    			loadScreen = true;
    			GameCanvas.loadBG(0);
    		}
    		bigOk = true;
    		cmd[2 + nCmdPlay].caption = mResources.server + ": " + nameServer[ipSelect];
    		center = new Command(string.Empty, this, cmd[selected].idAction, null);
    		cmd[1 + nCmdPlay].caption = mResources.change_account;
    		if (cmd.Length == 4 + nCmdPlay)
    		{
    			cmd[3 + nCmdPlay].caption = mResources.option;
    		}
    		Char.isLoadingMap = false;
    		mSystem.resetCurInapp();
    		base.switchToMe();
    	}
    
    	public void switchToMe2()
    	{
    		GameScr.cmy = 0;
    		GameScr.cmx = 0;
    		initCommand();
    		isWait = false;
    		GameCanvas.loginScr = null;
    		string text = Rms.loadRMSString("ResVersion");
    		int num = ((text == null || !(text != string.Empty)) ? (-1) : int.Parse(text));
    		if (num > 0)
    		{
    			loadScreen = true;
    			GameCanvas.loadBG(0);
    		}
    		bigOk = true;
    		cmd[2 + nCmdPlay].caption = mResources.server + ": " + nameServer[ipSelect];
    		center = new Command(string.Empty, this, cmd[selected].idAction, null);
    		cmd[1 + nCmdPlay].caption = mResources.change_account;
    		if (cmd.Length == 4 + nCmdPlay)
    		{
    			cmd[3 + nCmdPlay].caption = mResources.option;
    		}
    		mSystem.resetCurInapp();
    		base.switchToMe();
    	}
    
    	public void connectOk()
    	{
    	}
    
    	public void cancel()
    	{
    		if (GameCanvas.serverScreen == null)
    		{
    			GameCanvas.serverScreen = new ServerListScreen();
    		}
    		demPercent = 0;
    		percent = 0;
    		stopDownload = true;
    		GameCanvas.serverScreen.show2();
    		isGetData = false;
    		mSystem.println(">>>>>isGetData: " + isGetData);
    		cmdDownload.isFocus = true;
    		center = new Command(string.Empty, this, 2, null);
    	}
    
    	public void perform(int idAction, object p)
    	{
    		Res.outz("perform " + idAction);
    		if (idAction == 1000)
    		{
    			GameCanvas.connect();
    		}
    		if (idAction == 1 || idAction == 4)
    		{
    			Session_ME.gI().close();
    			isAutoConect = false;
    			countDieConnect = 0;
    			loadScreen = true;
    			testConnect = 0;
    			isGetData = false;
    			mSystem.println(">>>>>isGetData: " + isGetData);
    			Rms.clearAll();
    			switchToMe();
    		}
    		if (idAction == 2)
    		{
    			stopDownload = false;
    			cmdDownload = new Command(mResources.huy, this, 4, null);
    			cmdDownload.x = GameCanvas.w / 2 - mScreen.cmdW / 2;
    			cmdDownload.y = GameCanvas.hh + 65;
    			right = null;
    			if (!GameCanvas.isTouch)
    			{
    				cmdDownload.x = GameCanvas.w / 2 - mScreen.cmdW / 2;
    				cmdDownload.y = GameCanvas.h - mScreen.cmdH - 1;
    			}
    			center = new Command(string.Empty, this, 4, null);
    			if (!isGetData)
    			{
    				Service.gI().getResource(1, null);
    				if (!GameCanvas.isTouch)
    				{
    					cmdDownload.isFocus = true;
    					center = new Command(string.Empty, this, 4, null);
    					mSystem.println(">>>>>isGetData: " + isGetData);
    				}
    				isGetData = true;
    			}
    		}
    		if (idAction == 3)
    		{
    			Res.outz("toi day");
    			if (GameCanvas.loginScr == null)
    			{
    				GameCanvas.loginScr = new LoginScr();
    			}
    			GameCanvas.loginScr.switchToMe();
    			bool flag = Rms.loadRMSString("acc") != null && !Rms.loadRMSString("acc").Equals(string.Empty);
    			bool flag2 = Rms.loadRMSString("userAo" + ipSelect) != null && !Rms.loadRMSString("userAo" + ipSelect).Equals(string.Empty);
    			if (!flag && !flag2)
    			{
    				// Máy chưa có tài khoản nào: đây cũng là nút "Chơi mới", nên đi
    				// đúng đường đăng ký như nhánh 10100 thay vì tạo tài khoản ảo.
    				string text = Rms.loadRMSString("userAo" + ipSelect);
    				if (text == null || text.Equals(string.Empty))
    				{
    					LoginScr.serverName = nameServer[ipSelect];
    					GameCanvas.loginScr.isLogin2 = false;
    					GameCanvas.loginScr.actRegister();
    					return;
    				}
    				else
    				{
    					GameCanvas.loginScr.isLogin2 = true;
    					GameCanvas.connect();
    					Service.gI().setClientType();
    					Service.gI().login(text, string.Empty, GameMidlet.VERSION, 1);
    				}
    				if (Session_ME.connected)
    				{
    					GameCanvas.startWaitDlg();
    				}
    				else
    				{
    					GameCanvas.startOKDlg(mResources.maychutathoacmatsong);
    				}
    			}
    			else
    			{
    				GameCanvas.loginScr.doLogin();
    			}
    			LoginScr.serverName = nameServer[ipSelect];
    		}
    		if (idAction == 10100)
    		{
    			// "Chơi mới" mở màn ĐĂNG KÝ, không tạo tài khoản ảo nữa.
    			//
    			// Trước đây nút này gọi thẳng `login2("")` — máy chủ tự dựng một tài
    			// khoản ảo rồi nhảy luôn vào màn tạo nhân vật. Người chơi không có
    			// tên đăng nhập lẫn mật khẩu, nên xoá dữ liệu máy hay đổi máy là mất
    			// hẳn nhân vật mà không cách nào lấy lại.
    			//
    			// Đăng ký trước thì tài khoản là của người chơi: đăng nhập lại ở đâu
    			// cũng được. Xong bước đăng ký, máy chủ trả về rồi mới tới màn tạo
    			// nhân vật như cũ.
    			if (GameCanvas.loginScr == null)
    			{
    				GameCanvas.loginScr = new LoginScr();
    			}
    			LoginScr.serverName = nameServer[ipSelect];
    			GameCanvas.loginScr.isLogin2 = false;
    			GameCanvas.loginScr.switchToMe();
    			// Gọi SAU switchToMe: hàm kia không đụng tới `isRes`, nhưng đặt sau
    			// thì thứ tự đọc ra đúng như việc đang làm — mở màn, rồi bật chế độ
    			// đăng ký. Chưa nối máy chủ ở đây: `doRegister` tự nối lúc bấm gửi,
    			// nên không giữ một đường nối rỗng suốt lúc người ta gõ.
    			GameCanvas.loginScr.actRegister();
    		}
    		if (idAction == 5)
    		{
    			doUpdateServer();
    			if (nameServer.Length == 1)
    			{
    				return;
    			}
    			MyVector myVector = new MyVector(string.Empty);
    			for (int i = 0; i < nameServer.Length; i++)
    			{
    				myVector.addElement(new Command(nameServer[i], this, 6, null));
    			}
    			GameCanvas.menu.startAt(myVector, 0);
    			if (!GameCanvas.isTouch)
    			{
    				GameCanvas.menu.menuSelectedItem = ipSelect;
    			}
    		}
    		if (idAction == 6)
    		{
    			ipSelect = GameCanvas.menu.menuSelectedItem;
    			selectServer();
    		}
    		if (idAction == 7)
    		{
    			if (GameCanvas.loginScr == null)
    			{
    				GameCanvas.loginScr = new LoginScr();
    			}
    			GameCanvas.loginScr.switchToMe();
    		}
    		if (idAction == 8)
    		{
    			bool flag3 = Rms.loadRMSInt("lowGraphic") == 1;
    			MyVector myVector2 = new MyVector("cau hinh");
    			myVector2.addElement(new Command(mResources.cauhinhthap, this, 9, null));
    			myVector2.addElement(new Command(mResources.cauhinhcao, this, 10, null));
    			GameCanvas.menu.startAt(myVector2, 0);
    			if (flag3)
    			{
    				GameCanvas.menu.menuSelectedItem = 0;
    			}
    			else
    			{
    				GameCanvas.menu.menuSelectedItem = 1;
    			}
    		}
    		if (idAction == 9)
    		{
    			Rms.saveRMSInt("lowGraphic", 1);
    			GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
    		}
    		if (idAction == 10)
    		{
    			Rms.saveRMSInt("lowGraphic", 0);
    			GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
    		}
    		if (idAction == 11)
    		{
    			if (GameCanvas.loginScr == null)
    			{
    				GameCanvas.loginScr = new LoginScr();
    			}
    			GameCanvas.loginScr.switchToMe();
    			string text2 = Rms.loadRMSString("userAo" + ipSelect);
    			if (text2 == null || text2.Equals(string.Empty))
    			{
    				Service.gI().login2(string.Empty);
    			}
    			else
    			{
    				GameCanvas.loginScr.isLogin2 = true;
    				GameCanvas.connect();
    				Service.gI().setClientType();
    				Service.gI().login(text2, string.Empty, GameMidlet.VERSION, 1);
    			}
    			GameCanvas.startWaitDlg(mResources.PLEASEWAIT);
    			Res.outz("tao user ao");
    		}
    		if (idAction == 12)
    		{
    			GameMidlet.instance.exit();
    		}
    		if (idAction == 13 && (!isGetData || loadScreen))
    		{
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
    			}
    		}
    		if (idAction == 14)
    		{
    			Command cmdYes = new Command(mResources.YES, GameCanvas.serverScreen, 15, null);
    			Command cmdNo = new Command(mResources.NO, GameCanvas.serverScreen, 16, null);
    			GameCanvas.startYesNoDlg(mResources.deletaDataNote, cmdYes, cmdNo);
    		}
    		if (idAction == 15)
    		{
    			Rms.clearAll();
    			GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
    		}
    		if (idAction == 16)
    		{
    			InfoDlg.hide();
    			GameCanvas.currentDialog = null;
    		}
    		if (idAction == 17)
    		{
    			if (GameCanvas.serverScr == null)
    			{
    				GameCanvas.serverScr = new ServerScr();
    			}
    			GameCanvas.serverScr.switchToMe();
    		}
    		if (idAction == 18)
    		{
    			GameCanvas.endDlg();
    			InfoDlg.hide();
    			if (GameCanvas.serverScr == null)
    			{
    				GameCanvas.serverScr = new ServerScr();
    			}
    			GameCanvas.serverScr.switchToMe();
    		}
    		if (idAction == 19)
    		{
    			if (mSystem.clientType == 1)
    			{
    				InfoDlg.hide();
    				GameCanvas.currentDialog = null;
    			}
    			else
    			{
    				countDieConnect = 0;
    				testConnect = 0;
    				isAutoConect = true;
    			}
    		}
    	}
    
    	public void init()
    	{
    		if (!loadScreen)
    		{
    			cmdDownload = new Command(mResources.taidulieu, this, 2, null);
    			cmdDownload.isFocus = true;
    			cmdDownload.x = GameCanvas.w / 2 - mScreen.cmdW / 2;
    			cmdDownload.y = GameCanvas.hh + 45;
    			if (cmdDownload.y > GameCanvas.h - 26)
    			{
    				cmdDownload.y = GameCanvas.h - 26;
    			}
    		}
    		if (!GameCanvas.isTouch)
    		{
    			selected = 0;
    			processInput();
    		}
    	}
    
    	public void show2()
    	{
    		GameScr.cmx = 0;
    		GameScr.cmy = 0;
    		initCommand();
    		loadScreen = false;
    		percent = 0;
    		bigOk = false;
    		isGetData = false;
    		mSystem.println(">>>>>isGetData: " + isGetData);
    		p = 0;
    		demPercent = 0;
    		strWait = mResources.PLEASEWAIT;
    		Char.isLoadingMap = false;
    		init();
    		base.switchToMe();
    	}
    
    	public void setLinkDefault(sbyte language)
    	{
    		if (language == 2)
    		{
    			if (mSystem.clientType == 1)
    			{
    				linkDefault = javaIn;
    			}
    			else
    			{
    				linkDefault = smartPhoneIn;
    			}
    		}
    		else if (language == 1)
    		{
    			linkDefault = javaE;
    			if (mSystem.clientType == 1)
    			{
    				linkDefault = javaE;
    			}
    			else
    			{
    				linkDefault = smartPhoneE;
    			}
    		}
    		else
    		{
    			linkDefault = javaVN;
    			if (mSystem.clientType == 1)
    			{
    				linkDefault = javaVN;
    			}
    			else
    			{
    				linkDefault = smartPhoneVN;
    			}
    		}
    		mSystem.AddIpTest();
    	}
    
    	/// <summary>Dia chi may chu, doc tu Resources/server_ip.txt.</summary>
    	/// <remarks>
    	/// Doc tu file thay vi go cung trong ma: IP LAN doi theo DHCP, va nguoi
    	/// dung Unity chi can sua mot file van ban roi Build lai, khong phai mo
    	/// ma nguon.
    	///
    	/// File dat o Assets/Resources/server_ip.txt, noi dung la moi mot dia chi
    	/// nhu 192.168.1.48 (hoac ten mien). Thieu file thi dung so mac dinh
    	/// truyen vao.
    	///
    	/// Dung cho CA ban APK, ban PC va Unity Editor — xem cho goi ben duoi.
    	/// </remarks>
    	/// <summary>Ten file dia chi may chu, dat canh ban chay.</summary>
    	private const string TEN_FILE_IP = "server_ip.txt";

    	private static bool laDienThoai()
    	{
    		return UnityEngine.Application.platform
    				== UnityEngine.RuntimePlatform.Android
    			|| UnityEngine.Application.platform
    				== UnityEngine.RuntimePlatform.IPhonePlayer;
    	}

    	/// <summary>Địa chỉ này có trỏ về chính máy đang chạy không.</summary>
    	/// <remarks>
    	/// Trên máy người chơi thì loopback không bao giờ có máy chủ — nó chỉ là
    	/// rác còn lại từ một bản cũ, khi máy chủ chạy ngay trên máy lập trình.
    	/// </remarks>
    	private static bool laLoopback(string dc)
    	{
    		if (dc == null)
    		{
    			return false;
    		}
    		string d = dc.Trim().ToLower();
    		return d == "127.0.0.1" || d == "localhost" || d == "::1"
    				|| d.StartsWith("127.");
    	}

    	/// <summary>Lay dong dia chi dau tien, bo dong trong va dong ghi chu.</summary>
    	private static string locDiaChi(string noiDung)
    	{
    		if (noiDung == null)
    		{
    			return null;
    		}
    		string[] dong = noiDung.Split('\n');
    		for (int i = 0; i < dong.Length; i++)
    		{
    			string d = dong[i].Trim();
    			if (d.Length > 0 && !d.StartsWith("#"))
    			{
    				return d;
    			}
    		}
    		return null;
    	}

    	/// <summary>
    	/// Doc file dia chi dat canh ban chay.
    	/// </summary>
    	/// <remarks>
    	/// Doc luc CHAY, nen sua file la doi dia chi ngay — khong phai build lai.
    	/// Day la khac biet quan trong so voi <c>Resources</c>: Unity nuong ca thu
    	/// muc <c>Resources</c> vao ban build, nen server ghi lai file trong
    	/// <c>Assets/</c> thi ban .exe da build van giu dia chi cu.
    	///
    	/// Hai cho tim: thu muc luu tru rieng cua ung dung (dien thoai ghi duoc
    	/// vao day), va thu muc chua ban chay (tren PC la cho de file .exe).
    	/// </remarks>
    	private static string docFileNgoai()
    	{
    		string[] cho = {
    			UnityEngine.Application.persistentDataPath + "/" + TEN_FILE_IP,
    			UnityEngine.Application.dataPath + "/../" + TEN_FILE_IP
    		};
    		for (int i = 0; i < cho.Length; i++)
    		{
    			try
    			{
    				if (System.IO.File.Exists(cho[i]))
    				{
    					string s = locDiaChi(System.IO.File.ReadAllText(cho[i]));
    					if (s != null)
    					{
    						return s;
    					}
    				}
    			}
    			catch (System.Exception)
    			{
    				// Khong doc duoc cho nay thi thu cho ke tiep.
    			}
    		}
    		return null;
    	}

    	private static string docIpMayChu(string macDinh)
    	{
    		try
    		{
    			UnityEngine.TextAsset ta =
    				UnityEngine.Resources.Load<UnityEngine.TextAsset>("server_ip");
    			if (ta != null)
    			{
    				string s = locDiaChi(ta.text);
    				if (s != null)
    				{
    					return s;
    				}
    			}
    		}
    		catch (System.Exception)
    		{
    			// Chua nap duoc Resources -> dung so mac dinh.
    		}
    		return macDinh;
    	}

    	/// <summary>
    	/// Dia chi may chu, tu tim lay chu khong go cung.
    	/// </summary>
    	/// <remarks>
    	/// <para>Ba nguon xet theo thu tu:</para>
    	/// <list type="number">
    	///   <item>File <c>server_ip.txt</c> canh ban chay — doc luc CHAY, nen
    	///         doi dia chi chi can sua file, khong phai build lai.</item>
    	///   <item>Khong co file thi lay <b>160.22.107.46</b> — may chu tren
    	///         VPS, cong 14445.</item>
    	///   <item>Dien thoai khong co file thi doc <c>Resources/server_ip</c>
    	///         nuong luc build; thieu ca do thi cung ve VPS.</item>
    	/// </list>
    	///
    	/// <para>So mac dinh truoc day la 127.0.0.1. Hop khi may chu chay ngay
    	/// tren may dang lap trinh, nhung gio may chu o VPS nen loopback thanh
    	/// mot dia chi chet: moi may khong tu chay server se bao <i>"May chu tat
    	/// hoac mat song"</i> du VPS van song.</para>
    	///
    	/// <para>Muon chay thu tren may minh thi de mot file
    	/// <c>server_ip.txt</c> chua dong <c>127.0.0.1</c> canh ban chay. Nguon
    	/// thu nhat thang nguon thu hai, nen khong phai sua ma.</para>
    	/// </remarks>
    	private static string diaChiMayChu()
    	{
    		string s = docFileNgoai();
    		if (s != null)
    		{
    			return s;
    		}
    		if (!laDienThoai())
    		{
    			return "160.22.107.46";
    		}
    		return docIpMayChu("160.22.107.46");
    	}

    	static ServerListScreen()
    	{
            // Dia chi may chu tu tim lay — xem diaChiMayChu().
            smartPhoneVN = "DoTheAnh:" + diaChiMayChu() + ":14445:0,0,0";

    		javaVN = smartPhoneVN;
    		smartPhoneIn = "Naga:dragon.indonaga.com:14446:2:0:0,2,0";
    		javaIn = "Naga:52.74.230.22:14446:2:0:0,2,0";
    		smartPhoneE = "Universe 1:dragon.indonaga.com:14445:1:0:0,1,0";
    		javaE = "Universe 1:52.74.230.22:14445:1:0:0,1,0";
    		linkGetHost = "http://sv1.ngocrongonline.com/game/ngocrong031_t.php";
    		linkDefault = javaVN;
    		linkweb = "http://ngocrongonline.com";
    		// Doi ten khoa khi doi dia chi mac dinh: may nao da chay ban cu se
    		// con giu danh sach cu trong RMS va khong bao gio thay dia chi moi.
    		// Doi ten khoa moi lan doi cach lay dia chi: danh sach may chu duoc
    		// luu vao RMS, ban cu se doc lai danh sach cu do va che mat dia chi
    		// moi. Doi ten khoa la vut ban cu di.
    		// Doi "NRlink6" -> "NRlink7" de VUT danh sach cu tren may nguoi choi.
    		//
    		// May nao da chay mot ban truoc thi con giu nguyen danh sach may chu
    		// trong bo nho may, ke ca dia chi 127.0.0.1 tu thoi may chu chay ngay
    		// tren may lap trinh. Danh sach do duoc doc len TRUOC, va
    		// epDiaChiTuFile() chi sua duoc muc ten dung bang "DoTheAnh" — muc
    		// nao mang ten khac thi dia chi cu nam nguyen do.
    		//
    		// Doi ten khoa la lan chay ke tiep khong doc duoc gi, phai dung
    		// linkDefault — tuc dia chi trong server_ip.txt.
    		RMS_NRlink = "NRlink7";
    		lengthServer = new int[3];
    		isGetData = false;
    		testConnect = -1;
    		isAutoConect = true;
    	}
    }
}
