using System;
using UnityEngine;

namespace Game5
{
    
    public class CreateCharScr : mScreen, IActionListener
    {
    	public static CreateCharScr instance;
    
    	private PopUp p;
    
    	public static bool isCreateChar = false;
    
    	public static TField tAddName;
    
    	public static int indexGender;
    
    	public static int indexHair;
    
    	public static int selected;
    
    	public static int[][] hairID = new int[3][]
    	{
    		new int[3] { 64, 30, 31 },
    		new int[3] { 9, 29, 32 },
    		new int[3] { 6, 27, 28 }
    	};
    
    	public static int[] defaultLeg = new int[3] { 2, 13, 8 };
    
    	public static int[] defaultBody = new int[3] { 1, 12, 7 };
    
    	private int yButton;
    
    	private int disY;
    
    	private int[] bgID = new int[3] { 0, 4, 8 };
    
    	public int yBegin;
    
    	private int curIndex;
    
    	private int cx = 168;
    
    	private int cy = 350;
    
    	private int dy = 45;
    
    	private int cp1;
    
    	private int cf;
    
    	public CreateCharScr()
    	{
    		try
    		{
    			if (!GameCanvas.lowGraphic)
    			{
    				loadMapFromResource(new sbyte[3] { 39, 40, 41 });
    			}
    			loadMapTableFromResource(new sbyte[3] { 39, 40, 41 });
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Tao char loi " + ex.ToString());
    		}
    		if (GameCanvas.w <= 200)
    		{
    			GameScr.setPopupSize(128, 100);
    			GameScr.popupX = (GameCanvas.w - 128) / 2;
    			GameScr.popupY = 10;
    			cy += 15;
    			dy -= 15;
    		}
    		indexGender = 1;
    		tAddName = new TField();
    		tAddName.width = GameCanvas.loginScr.tfUser.width;
    		if (GameCanvas.w < 200)
    		{
    			tAddName.width = 60;
    		}
    		tAddName.height = mScreen.ITEM_HEIGHT + 2;
    		if (GameCanvas.w < 200)
    		{
    			tAddName.x = GameScr.popupX + 45;
    			tAddName.y = GameScr.popupY + 12;
    		}
    		else
    		{
    			tAddName.x = GameCanvas.w / 2 - tAddName.width / 2;
    			tAddName.y = 35;
    		}
    		if (!GameCanvas.isTouch)
    		{
    			tAddName.isFocus = true;
    		}
    		tAddName.setIputType(TField.INPUT_TYPE_ANY);
    		tAddName.showSubTextField = false;
    		tAddName.strInfo = mResources.char_name;
    		if (tAddName.getText().Equals("@"))
    		{
    			tAddName.setText(GameCanvas.loginScr.tfUser.getText().Substring(0, GameCanvas.loginScr.tfUser.getText().IndexOf("@")));
    		}
    		tAddName.name = mResources.char_name;
    		indexGender = 1;
    		indexHair = 0;
    		center = new Command(mResources.NEWCHAR, this, 8000, null);
    		left = new Command(mResources.BACK, this, 8001, null);
    		if (!GameCanvas.isTouch)
    		{
    			right = tAddName.cmdClear;
    		}
    		yBegin = tAddName.y;
    	}
    
    	public static CreateCharScr gI()
    	{
    		if (instance == null)
    		{
    			instance = new CreateCharScr();
    		}
    		return instance;
    	}
    
    	public static void init()
    	{
    	}
    
    	public static void loadMapFromResource(sbyte[] mapID)
    	{
    		Res.outz("newwwwwwwwww =============");
    		DataInputStream dataInputStream = null;
    		for (int i = 0; i < mapID.Length; i++)
    		{
    			dataInputStream = MyStream.readFile("/mymap/" + mapID[i]);
    			MapTemplate.tmw[i] = (ushort)dataInputStream.read();
    			MapTemplate.tmh[i] = (ushort)dataInputStream.read();
    			Cout.LogError("Thong TIn : " + MapTemplate.tmw[i] + "::" + MapTemplate.tmh[i]);
    			MapTemplate.maps[i] = new int[dataInputStream.available()];
    			Cout.LogError("lent= " + MapTemplate.maps[i].Length);
    			for (int j = 0; j < MapTemplate.tmw[i] * MapTemplate.tmh[i]; j++)
    			{
    				MapTemplate.maps[i][j] = dataInputStream.read();
    			}
    			MapTemplate.types[i] = new int[MapTemplate.maps[i].Length];
    		}
    	}
    
    	public void loadMapTableFromResource(sbyte[] mapID)
    	{
    		if (GameCanvas.lowGraphic)
    		{
    			return;
    		}
    		DataInputStream dataInputStream = null;
    		try
    		{
    			for (int i = 0; i < mapID.Length; i++)
    			{
    				dataInputStream = MyStream.readFile("/mymap/mapTable" + mapID[i]);
    				Cout.LogError("mapTable : " + mapID[i]);
    				short num = dataInputStream.readShort();
    				MapTemplate.vCurrItem[i] = new MyVector();
    				Res.outz("nItem= " + num);
    				for (int j = 0; j < num; j++)
    				{
    					short id = dataInputStream.readShort();
    					short num2 = dataInputStream.readShort();
    					short num3 = dataInputStream.readShort();
    					if (TileMap.getBIById(id) != null)
    					{
    						BgItem bIById = TileMap.getBIById(id);
    						BgItem bgItem = new BgItem();
    						bgItem.id = id;
    						bgItem.idImage = bIById.idImage;
    						bgItem.dx = bIById.dx;
    						bgItem.dy = bIById.dy;
    						bgItem.x = num2 * TileMap.size;
    						bgItem.y = num3 * TileMap.size;
    						bgItem.layer = bIById.layer;
    						MapTemplate.vCurrItem[i].addElement(bgItem);
    						if (!BgItem.imgNew.containsKey(bgItem.idImage + string.Empty))
    						{
    							try
    							{
    								Image image = GameCanvas.loadImage("/mapBackGround/" + bgItem.idImage + ".png");
    								if (image == null)
    								{
    									BgItem.imgNew.put(bgItem.idImage + string.Empty, Image.createRGBImage(new int[1], 1, 1, true));
    									Service.gI().getBgTemplate(bgItem.idImage);
    								}
    								else
    								{
    									BgItem.imgNew.put(bgItem.idImage + string.Empty, image);
    								}
    							}
    							catch (Exception)
    							{
    								Image image2 = GameCanvas.loadImage("/mapBackGround/" + bgItem.idImage + ".png");
    								if (image2 == null)
    								{
    									image2 = Image.createRGBImage(new int[1], 1, 1, true);
    									Service.gI().getBgTemplate(bgItem.idImage);
    								}
    								BgItem.imgNew.put(bgItem.idImage + string.Empty, image2);
    							}
    							BgItem.vKeysLast.addElement(bgItem.idImage + string.Empty);
    						}
    						if (!BgItem.isExistKeyNews(bgItem.idImage + string.Empty))
    						{
    							BgItem.vKeysNew.addElement(bgItem.idImage + string.Empty);
    						}
    						bgItem.changeColor();
    					}
    					else
    					{
    						Res.outz("item null");
    					}
    				}
    			}
    		}
    		catch (Exception ex2)
    		{
    			Cout.println("LOI TAI loadMapTableFromResource" + ex2.ToString());
    		}
    	}
    
    	public override void switchToMe()
    	{
    		LoginScr.isContinueToLogin = false;
    		GameCanvas.menu.showMenu = false;
    		GameCanvas.endDlg();
    		base.switchToMe();
    		indexGender = Res.random(0, 3);
    		indexHair = Res.random(0, 3);
    		doChangeMap();
    		Char.isLoadingMap = false;
    		// Xoa ten cu moi lan vao man tao nhan vat.
    		//
    		// O nhap la mot truong TINH (`static TField tAddName`) nen chu cua
    		// lan dang ky truoc con nguyen: dang ky tai khoan moi ma o ten van
    		// hien ten cu.
    		tAddName.setText(string.Empty);

    		// setFocus, KHONG phai setFocusWithKb.
    		//
    		// setFocusWithKb tu goi ban phim cua may len va gan no vao `TField.kb`
    		// - mot ban chu RIENG, khong lien quan gi toi hop `HopNhapChu` dang
    		// hien tren man. Hai ban chu chay song song: go thi chu vao ban cua
    		// TField, con hop thi ve `noiDung` cua no, khong bao gio doi. Nhin ra
    		// man hinh la o ten dung im con thanh nhap cua ban phim thi chay chu
    		// - va xoa kieu gi cung khong xoa duoc cai ten dang hien.
    		//
    		// Nay `HopNhapChu` tu goi ban phim cua may khi mo (xem
    		// `moBanPhimMay`), nen o day chi can dat con tro, khong goi them ban
    		// phim thu hai.
    		tAddName.setFocus(true);
    		ServerListScreen.countDieConnect = 0;
    	}
    
    	public void doChangeMap()
    	{
    		TileMap.maps = new int[MapTemplate.maps[indexGender].Length];
    		for (int i = 0; i < MapTemplate.maps[indexGender].Length; i++)
    		{
    			TileMap.maps[i] = MapTemplate.maps[indexGender][i];
    		}
    		TileMap.types = MapTemplate.types[indexGender];
    		TileMap.pxh = MapTemplate.pxh[indexGender];
    		TileMap.pxw = MapTemplate.pxw[indexGender];
    		TileMap.tileID = MapTemplate.pxw[indexGender];
    		TileMap.tmw = MapTemplate.tmw[indexGender];
    		TileMap.tmh = MapTemplate.tmh[indexGender];
    		TileMap.tileID = bgID[indexGender] + 1;
    		TileMap.loadMainTile();
    		TileMap.loadTileCreatChar();
    		GameCanvas.loadBG(bgID[indexGender]);
    		GameScr.loadCamera(false, cx, cy);
    	}
    
    	public override void keyPress(int keyCode)
    	{
    		tAddName.keyPressed(keyCode);
    	}
    
    	public override void update()
    	{
    		cp1++;
    		if (cp1 > 30)
    		{
    			cp1 = 0;
    		}
    		if (cp1 % 15 < 5)
    		{
    			cf = 0;
    		}
    		else
    		{
    			cf = 1;
    		}
    		tAddName.update();
    		if (selected != 0)
    		{
    			tAddName.isFocus = false;
    		}
    	}
    
    	public override void updateKey()
    	{
    		// Man nay khong di qua GameScr nen chuoi cham cua ClientManager khong
    		// chay toi. Tu hoi hop nhap truoc moi thu khac.
    		if (God.HopNhapChu.getInstance().capNhatCham())
    		{
    			return;
    		}
    		if (GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21])
    		{
    			selected--;
    			if (selected < 0)
    			{
    				selected = mResources.MENUNEWCHAR.Length - 1;
    			}
    		}
    		if (GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22])
    		{
    			selected++;
    			if (selected >= mResources.MENUNEWCHAR.Length)
    			{
    				selected = 0;
    			}
    		}
    		if (selected == 0)
    		{
    			if (!GameCanvas.isTouch)
    			{
    				right = tAddName.cmdClear;
    			}
    			tAddName.update();
    		}
    		if (selected == 1)
    		{
    			if (GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23])
    			{
    				indexGender--;
    				if (indexGender < 0)
    				{
    					indexGender = mResources.MENUGENDER.Length - 1;
    				}
    				doChangeMap();
    			}
    			if (GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24])
    			{
    				indexGender++;
    				if (indexGender > mResources.MENUGENDER.Length - 1)
    				{
    					indexGender = 0;
    				}
    				doChangeMap();
    			}
    			right = null;
    		}
    		if (selected == 2)
    		{
    			if (GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23])
    			{
    				indexHair--;
    				if (indexHair < 0)
    				{
    					indexHair = mResources.hairStyleName[0].Length - 1;
    				}
    			}
    			if (GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24])
    			{
    				indexHair++;
    				if (indexHair > mResources.hairStyleName[0].Length - 1)
    				{
    					indexHair = 0;
    				}
    			}
    			right = null;
    		}
    		if (GameCanvas.isPointerJustRelease)
    		{
    			// Vung bam doc tu oThe() — cung mot nguon voi phan ve.
    			//
    			// Ban cu tinh lai bang mot day so go tay (110 / 60 / 78 / 45) va
    			// suy ra o nao bang phep chia toa do, nen chi can le hay khoang
    			// cach doi mot chut la bam trung o ben canh. Va vi phan ve dung
    			// dung day so ay o mot ham khac, sua mot ben la lech hai ben.
    			int[][] o = oThe();
    			if (GameCanvas.isPointerHoldIn(o[1][0], o[1][1], o[1][2], o[1][3]))
    			{
    				selected = 0;
    				tAddName.isFocus = true;
    				// Mo hop nhap chung thay cho o nhap cua game.
    				//
    				// O cu dua vao ban phim cua Unity — tren PC thi khong co gi
    				// hien ra ca, nen khong go noi ten. Hop chung doc thang phim
    				// nen go duoc chu hoa, chu thuong, dau cach, va tieng Viet
    				// theo bo go cua may.
    				//
    				// Chan mo lai khi dang mo: cho nay chay theo `isPointerHoldIn`
    				// nen giu ngon la moi khung hinh goi mot lan.
    				if (!God.HopNhapChu.getInstance().dangMo)
    				{
    					God.HopNhapChu.getInstance().moRa(mResources.char_name,
    							tAddName.getText(), 15,
    							chu => tAddName.setText(chu));
    				}
    			}
    			for (int i = 0; i < 3; i++)
    			{
    				if (GameCanvas.isPointerHoldIn(o[2 + i][0], o[2 + i][1],
    						o[2 + i][2], o[2 + i][3]))
    				{
    					selected = 1;
    					if (indexGender != i)
    					{
    						indexGender = i;
    						// Toc cua hanh tinh cu co the khong co ban o hanh
    						// tinh moi — keo ve trong khoang truoc khi ve.
    						if (indexHair >= mResources.hairStyleName[indexGender].Length)
    						{
    							indexHair = 0;
    						}
    						doChangeMap();
    					}
    				}
    				if (GameCanvas.isPointerHoldIn(o[5 + i][0], o[5 + i][1],
    						o[5 + i][2], o[5 + i][3]))
    				{
    					selected = 2;
    					indexHair = i;
    				}
    			}
    		}
    		if (!TouchScreenKeyboard.visible)
    		{
    			base.updateKey();
    		}
    		GameCanvas.clearKeyHold();
    		GameCanvas.clearKeyPressed();
    	}
    
    	public override void paint(mGraphics g)
    	{
    		if (Char.isLoadingMap)
    		{
    			return;
    		}
    		GameCanvas.paintBGGameScr(g);
    		g.translate(-GameScr.cmx, -GameScr.cmy);
    		if (!GameCanvas.lowGraphic)
    		{
    			for (int i = 0; i < MapTemplate.vCurrItem[indexGender].size(); i++)
    			{
    				BgItem bgItem = (BgItem)MapTemplate.vCurrItem[indexGender].elementAt(i);
    				if (bgItem.idImage != -1 && bgItem.layer == 1)
    				{
    					bgItem.paint(g);
    				}
    			}
    		}
    		if (mSystem.clientType == 5)
    		{
    			GameCanvas.paint_ios_bg(g);
    		}
    		else
    		{
    			TileMap.paintTilemap(g);
    		}
    		int num = 30;
    		if (GameCanvas.w == 128)
    		{
    			num = 20;
    		}
    		int num2 = hairID[indexGender][indexHair];
    		int num3 = defaultLeg[indexGender];
    		int num4 = defaultBody[indexGender];
    		g.drawImage(TileMap.bong, cx, cy + dy, 3);
    		Part part = GameScr.parts[num2];
    		Part part2 = GameScr.parts[num3];
    		Part part3 = GameScr.parts[num4];
    		SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[cf][0][0]].id, cx + Char.CharInfo[cf][0][1] + part.pi[Char.CharInfo[cf][0][0]].dx, cy - Char.CharInfo[cf][0][2] + part.pi[Char.CharInfo[cf][0][0]].dy + dy, 0, 0);
    		SmallImage.drawSmallImage(g, part2.pi[Char.CharInfo[cf][1][0]].id, cx + Char.CharInfo[cf][1][1] + part2.pi[Char.CharInfo[cf][1][0]].dx, cy - Char.CharInfo[cf][1][2] + part2.pi[Char.CharInfo[cf][1][0]].dy + dy, 0, 0);
    		SmallImage.drawSmallImage(g, part3.pi[Char.CharInfo[cf][2][0]].id, cx + Char.CharInfo[cf][2][1] + part3.pi[Char.CharInfo[cf][2][0]].dx, cy - Char.CharInfo[cf][2][2] + part3.pi[Char.CharInfo[cf][2][0]].dy + dy, 0, 0);
    		if (!GameCanvas.lowGraphic)
    		{
    			for (int j = 0; j < MapTemplate.vCurrItem[indexGender].size(); j++)
    			{
    				BgItem bgItem2 = (BgItem)MapTemplate.vCurrItem[indexGender].elementAt(j);
    				if (bgItem2.idImage != -1 && bgItem2.layer == 3)
    				{
    					bgItem2.paint(g);
    				}
    			}
    		}
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		if (GameCanvas.w < 200)
    		{
    			GameCanvas.paintz.paintFrame(GameScr.popupX, GameScr.popupY, GameScr.popupW, GameScr.popupH, g);
    			SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, GameCanvas.w / 2 + Char.CharInfo[0][0][1] + part.pi[Char.CharInfo[0][0][0]].dx, GameScr.popupY + 30 + 3 * num - Char.CharInfo[0][0][2] + part.pi[Char.CharInfo[0][0][0]].dy + dy, 0, 0);
    			SmallImage.drawSmallImage(g, part2.pi[Char.CharInfo[0][1][0]].id, GameCanvas.w / 2 + Char.CharInfo[0][1][1] + part2.pi[Char.CharInfo[0][1][0]].dx, GameScr.popupY + 30 + 3 * num - Char.CharInfo[0][1][2] + part2.pi[Char.CharInfo[0][1][0]].dy + dy, 0, 0);
    			SmallImage.drawSmallImage(g, part3.pi[Char.CharInfo[0][2][0]].id, GameCanvas.w / 2 + Char.CharInfo[0][2][1] + part3.pi[Char.CharInfo[0][2][0]].dx, GameScr.popupY + 30 + 3 * num - Char.CharInfo[0][2][2] + part3.pi[Char.CharInfo[0][2][0]].dy + dy, 0, 0);
    			for (int k = 0; k < mResources.MENUNEWCHAR.Length; k++)
    			{
    				if (selected == k)
    				{
    					g.drawRegion(GameScr.arrow, 0, 0, 13, 16, 2, GameScr.popupX + 10 + ((GameCanvas.gameTick % 7 > 3) ? 1 : 0), GameScr.popupY + 35 + k * num, StaticObj.VCENTER_HCENTER);
    					g.drawRegion(GameScr.arrow, 0, 0, 13, 16, 0, GameScr.popupX + GameScr.popupW - 10 - ((GameCanvas.gameTick % 7 > 3) ? 1 : 0), GameScr.popupY + 35 + k * num, StaticObj.VCENTER_HCENTER);
    				}
    				mFont.tahoma_7b_dark.drawString(g, mResources.MENUNEWCHAR[k], GameScr.popupX + 20, GameScr.popupY + 30 + k * num, 0);
    			}
    			mFont.tahoma_7b_dark.drawString(g, mResources.MENUGENDER[indexGender], GameScr.popupX + 70, GameScr.popupY + 30 + num, mFont.LEFT);
    			mFont.tahoma_7b_dark.drawString(g, mResources.hairStyleName[indexGender][indexHair], GameScr.popupX + 55, GameScr.popupY + 30 + 2 * num, mFont.LEFT);
    			tAddName.paint(g);
    		}
    		else
    		{
    			veTheTaoNhanVat(g, part, part2, part3);
    		}
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		mFont.tahoma_7b_white.drawString(g, mResources.server + " " + LoginScr.serverName, 5, 5, 0, mFont.tahoma_7b_dark);
    		if (!TouchScreenKeyboard.visible)
    		{
    			base.paint(g);
    		}
    		// Ve sau cung: hop nhap la hop chan, phai nam tren moi thu cua man nay.
    		God.HopNhapChu.getInstance().ve(g);
    	}
    
    	// ==================================================================
    	//  Thẻ tạo nhân vật — bản vẽ lại
    	// ==================================================================
    	//
    	// Bản cũ rải ba thứ ra giữa màn hình: ô nhập tên nổi lơ lửng ở trên,
    	// rồi hai hàng nút hình `imgLbtn` — cùng một ảnh nút dùng cho cả hành
    	// tinh lẫn kiểu tóc, không nhãn nhóm, không viền, không biết đâu là
    	// nhóm nào. Toạ độ thì tính bằng một mớ số 110 / 60 / 78 / 45 gõ thẳng
    	// vào giữa hàm vẽ, và phần bắt chạm tính LẠI cũng bằng đúng mớ số ấy ở
    	// một hàm khác — sửa một bên là lệch.
    	//
    	// Nay gom vào một thẻ: mọi vùng do `oThe()` tính một lần, cả lúc vẽ lẫn
    	// lúc bắt chạm đều đọc từ đó.

    	private const int MAU_THE = 0x241C33;
    	private const int MAU_THE_VIEN = 0xC8933C;
    	private const int MAU_O_TAT = 0x453C5A;
    	private const int MAU_O_BAT = 0x7A5CC0;
    	private const int MAU_KHUNG_XEM = 0x191430;

    	/// <summary>Màu nhấn của ba hành tinh: Trái Đất, Namếc, Xayda.</summary>
    	private static readonly int[] MAU_HANH_TINH = { 0x3D7BD4, 0x3FA84A, 0xD4A93D };

    	/// <summary>
    	/// Các vùng của thẻ, tính một lần: {x, y, rộng, cao}.
    	/// </summary>
    	/// <remarks>
    	/// Trả về một mảng để cả phần vẽ và phần bắt chạm dùng CHUNG. Đây là chỗ
    	/// bản cũ hay sai nhất: hai bên tự tính lấy bằng cùng một dãy số gõ tay,
    	/// và chỉ cần sửa một bên là bấm trượt.
    	///
    	/// Thứ tự: 0 thẻ, 1 ô tên, 2..4 ba nút hành tinh, 5..7 ba nút tóc,
    	/// 8 khung xem trước.
    	/// </remarks>
    	private int[][] oThe()
    	{
    		int rong = GameCanvas.w - 24;
    		if (rong > 306)
    		{
    			rong = 306;
    		}
    		if (rong < 210)
    		{
    			rong = 210;
    		}
    		int cao = 152;
    		int x = (GameCanvas.w - rong) / 2;
    		int y = 14;
    		// Ban phim may dang che nua duoi man hinh thi keo the len.
    		if (mGraphics.addYWhenOpenKeyBoard != 0)
    		{
    			y = 6;
    		}

    		int rongXem = 92;
    		int xTrai = x + 8;
    		int rongTrai = rong - rongXem - 24;
    		int rongNut = (rongTrai - 8) / 3;

    		int[][] o = new int[9][];
    		o[0] = new int[] { x, y, rong, cao };
    		o[1] = new int[] { xTrai, y + 22, rongTrai, 21 };
    		for (int i = 0; i < 3; i++)
    		{
    			o[2 + i] = new int[] { xTrai + i * (rongNut + 4), y + 60, rongNut, 22 };
    			o[5 + i] = new int[] { xTrai + i * (rongNut + 4), y + 104, rongNut, 22 };
    		}
    		o[8] = new int[] { x + rong - rongXem - 8, y + 22, rongXem, cao - 30 };
    		return o;
    	}

    	private void veTheTaoNhanVat(mGraphics g, Part part, Part part2, Part part3)
    	{
    		int[][] o = oThe();
    		int[] the = o[0];

    		// Nen the: mot khoi toi bo goc, vien vang — de doc tren moi ban do,
    		// ba ban do nen cua ba hanh tinh sang toi rat khac nhau.
    		g.setColor(0, 0.35f);
    		g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    		g.setColor(MAU_THE_VIEN, 0.9f);
    		g.fillRect(the[0] - 2, the[1] - 2, the[2] + 4, the[3] + 4, 10);
    		g.setColor(MAU_THE, 0.97f);
    		g.fillRect(the[0], the[1], the[2], the[3], 9);

    		mFont.tahoma_7b_yellow.drawString(g, "TẠO NHÂN VẬT",
    				the[0] + the[2] / 2, the[1] + 5, mFont.CENTER);

    		// ---- o ten ----
    		int[] oTen = o[1];
    		bool dangChonTen = (selected == 0);
    		g.setColor(dangChonTen ? MAU_O_BAT : MAU_O_TAT, 1f);
    		g.fillRect(oTen[0], oTen[1], oTen[2], oTen[3], 4);
    		g.setColor(MAU_THE_VIEN, 0.6f);
    		g.drawRect(oTen[0], oTen[1], oTen[2], oTen[3]);
    		string ten = tAddName.getText();
    		if (ten == null || ten.Length == 0)
    		{
    			mFont.tahoma_7_grey.drawString(g, "Chạm để nhập tên...",
    					oTen[0] + 6, oTen[1] + 5, mFont.LEFT);
    		}
    		else
    		{
    			mFont.tahoma_7b_white.drawString(g, ten, oTen[0] + 6, oTen[1] + 5,
    					mFont.LEFT);
    			// Con tro nhay, de biet o dang nhan chu.
    			if (dangChonTen && GameCanvas.gameTick % 14 < 7)
    			{
    				int wTen = mFont.tahoma_7b_white.getWidth(ten);
    				g.setColor(0xFFFFFF, 0.9f);
    				g.fillRect(oTen[0] + 8 + wTen, oTen[1] + 4, 1, 12);
    			}
    		}

    		// ---- hanh tinh ----
    		mFont.tahoma_7_white.drawString(g, "Hành tinh", o[2][0], o[2][1] - 13,
    				mFont.LEFT);
    		for (int i = 0; i < 3; i++)
    		{
    			veNutChon(g, o[2 + i], mResources.MENUGENDER[i], i == indexGender,
    					MAU_HANH_TINH[i]);
    		}

    		// ---- kieu toc ----
    		mFont.tahoma_7_white.drawString(g, "Kiểu tóc", o[5][0], o[5][1] - 13,
    				mFont.LEFT);
    		for (int i = 0; i < 3; i++)
    		{
    			veNutChon(g, o[5 + i], mResources.hairStyleName[indexGender][i],
    					i == indexHair, MAU_HANH_TINH[indexGender]);
    		}

    		// ---- khung xem truoc ----
    		//
    		// Ve lai nhan vat NGAY TRONG the, khong trong cay vao hinh dung
    		// ngoai ban do: hinh do nam sau lop nen va co the bi cay coi hay o
    		// dia hinh che, tuy ban do cua tung hanh tinh.
    		int[] xem = o[8];
    		g.setColor(MAU_KHUNG_XEM, 1f);
    		g.fillRect(xem[0], xem[1], xem[2], xem[3], 6);
    		g.setColor(MAU_HANH_TINH[indexGender], 0.85f);
    		g.drawRect(xem[0], xem[1], xem[2], xem[3]);
    		mFont.tahoma_7_grey.drawString(g, "Xem trước",
    				xem[0] + xem[2] / 2, xem[1] + 3, mFont.CENTER);

    		int xNv = xem[0] + xem[2] / 2;
    		int yNv = xem[1] + xem[3] - 26;
    		if (TileMap.bong != null)
    		{
    			g.drawImage(TileMap.bong, xNv, yNv + 4, 3);
    		}
    		veMotPhan(g, part, 0, xNv, yNv);
    		veMotPhan(g, part2, 1, xNv, yNv);
    		veMotPhan(g, part3, 2, xNv, yNv);

    		mFont.tahoma_7b_yellow.drawString(g, mResources.MENUGENDER[indexGender],
    				xem[0] + xem[2] / 2, xem[1] + xem[3] - 14, mFont.CENTER);
    	}

    	/// <summary>Vẽ một bộ phận nhân vật ở khung hình đang chạy.</summary>
    	/// <remarks>
    	/// Tách ra vì ba lời gọi giống hệt nhau chỉ khác chỉ số bộ phận, và dòng
    	/// gốc dài tới mức không đọc ra nổi nó đang cộng những gì.
    	/// </remarks>
    	private void veMotPhan(mGraphics g, Part phan, int chiSo, int x, int y)
    	{
    		if (phan == null || phan.pi == null)
    		{
    			return;
    		}
    		int khung = Char.CharInfo[cf][chiSo][0];
    		if (khung < 0 || khung >= phan.pi.Length || phan.pi[khung] == null)
    		{
    			return;
    		}
    		SmallImage.drawSmallImage(g, phan.pi[khung].id,
    				x + Char.CharInfo[cf][chiSo][1] + phan.pi[khung].dx,
    				y - Char.CharInfo[cf][chiSo][2] + phan.pi[khung].dy, 0, 0);
    	}

    	private static void veNutChon(mGraphics g, int[] o, string chu, bool bat,
    			int mauNhan)
    	{
    		g.setColor(bat ? mauNhan : MAU_O_TAT, 1f);
    		g.fillRect(o[0], o[1], o[2], o[3], 4);
    		if (bat)
    		{
    			g.setColor(0xFFFFFF, 0.85f);
    			g.drawRect(o[0], o[1], o[2], o[3]);
    		}
    		(bat ? mFont.tahoma_7b_white : mFont.tahoma_7_white)
    				.drawString(g, chu, o[0] + o[2] / 2, o[1] + (o[3] - 11) / 2,
    						mFont.CENTER);
    	}

    	public void perform(int idAction, object p)
    	{
    		switch (idAction)
    		{
    		case 8000:
    			if (tAddName.getText().Equals(string.Empty))
    			{
    				GameCanvas.startOKDlg(mResources.char_name_blank);
    				break;
    			}
    			if (tAddName.getText().Length < 5)
    			{
    				GameCanvas.startOKDlg(mResources.char_name_short);
    				break;
    			}
    			if (tAddName.getText().Length > 15)
    			{
    				GameCanvas.startOKDlg(mResources.char_name_long);
    				break;
    			}
    			InfoDlg.showWait();
    			Service.gI().createChar(tAddName.getText(), indexGender, hairID[indexGender][indexHair]);
    			break;
    		case 8001:
    			if (GameCanvas.loginScr.isLogin2)
    			{
    				GameCanvas.startYesNoDlg(mResources.note, new Command(mResources.YES, this, 10019, null), new Command(mResources.NO, this, 10020, null));
    				break;
    			}
    			if (Main.isWindowsPhone)
    			{
    				GameMidlet.isBackWindowsPhone = true;
    			}
    			Session_ME.gI().close();
    			GameCanvas.serverScreen.switchToMe();
    			break;
    		case 10020:
    			GameCanvas.endDlg();
    			break;
    		case 10019:
    			Session_ME.gI().close();
    			GameCanvas.endDlg();
    			GameCanvas.serverScreen.switchToMe();
    			break;
    		}
    	}
    }
}
