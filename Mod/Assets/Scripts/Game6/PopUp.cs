using Game6.God;

namespace Game6
{
    
    public class PopUp
    {
    	public static MyVector vPopups = new MyVector();
    
    	public int sayWidth;
    
    	public int sayRun;
    
    	public string[] says;
    
    	public int cx;
    
    	public int cy;
    
    	public int cw;
    
    	public int ch;
    
    	public static int f;
    
    	public static int tF;
    
    	public static int dir;
    
    	public bool isWayPoint;
    
    	public int tDelay;
    
    	private int timeDelay;
    
    	public Command command;
    
    	public bool isPaint = true;
    
    	public bool isHide;
    
    	public static Image goc;
    
    	public static Image imgPopUp;
    
    	public static Image imgPopUp2;
    
    	public Image imgFocus;
    
    	public Image imgUnFocus;
    
    	public PopUp(string info, int x, int y)
    	{
    		sayWidth = 100;
    		if (info.Length < 10)
    		{
    			sayWidth = 60;
    		}
    		if (GameCanvas.w == 128)
    		{
    			sayWidth = 128;
    		}
    		says = mFont.tahoma_7b_dark.splitFontArray(info, sayWidth - 10);
    		sayRun = 7;
    		cx = x - sayWidth / 2 - 1;
    		cy = y - 15 + sayRun - says.Length * 12 - 10;
    		cw = sayWidth + 2;
    		ch = (says.Length + 1) * 12 + 1;
    		while (cw % 10 != 0)
    		{
    			cw++;
    		}
    		while (ch % 10 != 0)
    		{
    			ch++;
    		}
    		if (x >= 0 && x <= 24)
    		{
    			cx += cw / 2 + 30;
    		}
    		if (x <= TileMap.tmw * 24 && x >= TileMap.tmw * 24 - 24)
    		{
    			cx -= cw / 2 + 6;
    		}
    		while (cx <= 30)
    		{
    			cx += 2;
    		}
    		while (cx + cw >= TileMap.tmw * 24 - 30)
    		{
    			cx -= 2;
    		}
    	}
    
    	public static void loadBg()
    	{
    		if (goc == null)
    		{
    			goc = GameCanvas.loadImage("/mainImage/myTexture2dbd3.png");
    		}
    		if (imgPopUp == null)
    		{
    			imgPopUp = GameCanvas.loadImage("/mainImage/myTexture2dimgPopup.png");
    		}
    		if (imgPopUp2 == null)
    		{
    			imgPopUp2 = GameCanvas.loadImage("/mainImage/myTexture2dimgPopup2.png");
    		}
    	}
    
    	public void updateXYWH(string[] info, int x, int y)
    	{
    		sayWidth = 0;
    		for (int i = 0; i < info.Length; i++)
    		{
    			if (sayWidth < mFont.tahoma_7b_dark.getWidth(info[i]))
    			{
    				sayWidth = mFont.tahoma_7b_dark.getWidth(info[i]);
    			}
    		}
    		sayWidth += 20;
    		says = info;
    		sayRun = 7;
    		cx = x - sayWidth / 2 - 1;
    		cy = y - 15 + sayRun - says.Length * 12 - 10;
    		cw = sayWidth + 2;
    		ch = (says.Length + 1) * 12 + 1;
    		while (cw % 10 != 0)
    		{
    			cw++;
    		}
    		while (ch % 10 != 0)
    		{
    			ch++;
    		}
    		if (x >= 0 && x <= 24)
    		{
    			cx += cw / 2 + 30;
    		}
    		if (x <= TileMap.tmw * 24 && x >= TileMap.tmw * 24 - 24)
    		{
    			cx -= cw / 2 + 6;
    		}
    		while (cx <= 30)
    		{
    			cx += 2;
    		}
    		while (cx + cw >= TileMap.tmw * 24 - 30)
    		{
    			cx -= 2;
    		}
    	}
    
    	public static void addPopUp(int x, int y, string info)
    	{
    		vPopups.addElement(new PopUp(info, x, y));
    	}
    
    	public static void addPopUp(PopUp p)
    	{
    		vPopups.addElement(p);
    	}
    
    	public static void removePopUp(PopUp p)
    	{
    		vPopups.removeElement(p);
    	}
    
    	public void paintClipPopUp(mGraphics g, int x, int y, int w, int h, int color, bool isFocus)
    	{
    		if (color == 1)
    		{
    			g.fillRect(x, y, w, h, 16777215, 90);
    		}
    		else
    		{
    			g.fillRect(x, y, w, h, 0, 77);
    		}
    	}
    
        /// <summary>Pha hai mau theo ty le <paramref name="t"/> (0..1).</summary>
        private static int tronMau(int a, int b, float t)
        {
            int ra = (a >> 16) & 0xFF;
            int ga = (a >> 8) & 0xFF;
            int ba = a & 0xFF;
            int rb = (b >> 16) & 0xFF;
            int gb = (b >> 8) & 0xFF;
            int bb = b & 0xFF;
            return ((int)(ra + (rb - ra) * t) << 16)
                    | ((int)(ga + (gb - ga) * t) << 8)
                    | (int)(ba + (bb - ba) * t);
        }

        /// <summary>
        /// Hộp bo góc dùng chung: bóng đổ, viền nâu-vàng, lòng sáng chuyển màu.
        /// </summary>
        /// <remarks>
        /// <para>Bản cũ ghép hộp từ bốn hình chữ nhật đen và bốn <b>ảnh</b> góc
        /// (<c>goc</c>). Ảnh đó có thể không nạp được, và khi ấy
        /// <c>drawRegion</c> lặng lẽ không vẽ gì — hộp hiện ra với bốn góc bị
        /// khoét vuông mà không báo lỗi. Vẽ hoàn toàn bằng mã thì luôn có hình,
        /// và bo góc khớp đúng với nút lựa chọn ở <c>Menu</c>.</para>
        ///
        /// <para><b>Lòng hộp giữ màu SÁNG.</b> Chữ bên trong do chỗ gọi quyết
        /// định và phần lớn là màu tối — tên món, mô tả, vạch ngăn màu đen. Đổi
        /// lòng sang nâu đậm cho hợp tông màu nút thì chữ chìm hẳn, không đọc
        /// được. Đây là lý do tham số <c>color</c> không còn được dùng ở nhánh
        /// này: mọi chỗ gọi nhánh đó đều truyền màu trắng.</para>
        /// </remarks>
        private static void veHopBoGoc(mGraphics g, int x, int y, int w, int h)
        {
            const int BO = 8;
            const int MAU_VIEN = 0xB07636;
            const int MAU_VIEN_TRONG = 0xF3D9A6;
            const int LONG_TREN = 0xFFF8EC;
            const int LONG_DUOI = 0xF2DFC4;

            // Khong ve bong do den phia sau hop.
            // Vien nau, roi mot net vang nhat ben trong cho co chieu sau.
            g.setColor(MAU_VIEN, 1f);
            g.fillRect(x, y, w, h, BO);
            g.setColor(MAU_VIEN_TRONG, 1f);
            g.fillRect(x + 2, y + 2, w - 4, h - 4, BO - 2);

            // Long chuyen mau. Moi dai keo tu dinh cua no XUONG TAN DAY hop, va
            // ve dai cao nhat truoc. Ve tung dai roi rac thanh hinh chu nhat
            // vuong goc thi goc vuong tho han ra ngoai khung bo goc.
            int xt = x + 3;
            int yt = y + 3;
            int wt = w - 6;
            int ht = h - 6;
            if (wt <= 0 || ht <= 0)
            {
                return;
            }
            // Chuyen mau MIN tung dong mot, ma goc van tron.
            //
            // Ve tung dai bo goc chong len nhau thi dai cuoi thap qua, ban kinh
            // bi kep gan ve 0 va de goc vuong len goc tron cua dai truoc. Tach
            // hai viec: hai dau to bang hai hinh BO GOC, phan giua ke tung dong.
            int R = BO - 3;
            g.setColor(LONG_TREN, 1f);
            g.fillRect(xt, yt, wt, ht, R);
            g.setColor(LONG_DUOI, 1f);
            g.fillRect(xt, yt + ht - R * 2, wt, R * 2, R);
            for (int i = R; i < ht - R; i++)
            {
                g.setColor(tronMau(LONG_TREN, LONG_DUOI, (float)i / (float)(ht - 1)), 1f);
                g.fillRect(xt, yt + i, wt, 1);
            }
        }

        public static void paintPopUp(mGraphics g, int x, int y, int w, int h, int color, bool isButton)

        {
            if (!isButton)
            {
                veHopBoGoc(g, x, y, w, h);
                return;
            }
            if (!ListChars.getInstance().HideMap)
            {
                Image arg = ((color != 1) ? imgPopUp : imgPopUp2);
                g.drawRegion(arg, 0, 0, 10, 10, 0, x, y, 0);
                g.drawRegion(arg, 0, 20, 10, 10, 0, x + w - 10, y, 0);
                g.drawRegion(arg, 0, 50, 10, 10, 0, x, y + h - 10, 0);
                g.drawRegion(arg, 0, 70, 10, 10, 0, x + w - 10, y + h - 10, 0);
                int num = (((w - 20) % 10 != 0) ? ((w - 20) / 10 + 1) : ((w - 20) / 10));
                int num2 = (((h - 20) % 10 != 0) ? ((h - 20) / 10 + 1) : ((h - 20) / 10));
                for (int i = 0; i < num; i++)
                {
                    g.drawRegion(arg, 0, 10, 10, 10, 0, x + 10 + i * 10, y, 0);
                }
                for (int j = 0; j < num2; j++)
                {
                    g.drawRegion(arg, 0, 30, 10, 10, 0, x, y + 10 + j * 10, 0);
                }
                for (int k = 0; k < num; k++)
                {
                    g.drawRegion(arg, 0, 60, 10, 10, 0, x + 10 + k * 10, y + h - 10, 0);
                }
                for (int l = 0; l < num2; l++)
                {
                    g.drawRegion(arg, 0, 40, 10, 10, 0, x + w - 10, y + 10 + l * 10, 0);
                }
            }
            g.setColor((color != 1) ? 16770503 : 12052656);
            g.fillRect(x + 10, y + 10, w - 20, h - 20);
        }
    
        public void paint(mGraphics g)
    	{
    		if (isPaint && says != null && ChatPopup.currChatPopup == null && !isHide)
    		{
    			paintClipPopUp(g, cx, cy - GameCanvas.transY, cw, ch, (timeDelay != 0) ? 1 : 0, true);
    			for (int i = 0; i < says.Length; i++)
    			{
    				((timeDelay != 0) ? mFont.tahoma_7b_green2 : mFont.tahoma_7b_white).drawString(g, says[i], cx + cw / 2, cy + (ch / 2 - says.Length * 12 / 2) + i * 12 - GameCanvas.transY, 2);
    			}
    		}
    	}
    
    	private void update()
    	{
    		if (Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId == 0)
    		{
    			if (cx + cw >= GameScr.cmx && cx <= GameCanvas.w + GameScr.cmx && cy + ch >= GameScr.cmy && cy <= GameCanvas.h + GameScr.cmy)
    			{
    				isHide = false;
    			}
    			else
    			{
    				isHide = true;
    			}
    		}
    		if (Char.myCharz().taskMaint == null || (Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId != 0))
    		{
    			if (cx + cw / 2 >= Char.myCharz().cx - 100 && cx + cw / 2 <= Char.myCharz().cx + 100 && cy + ch >= GameScr.cmy && cy <= GameCanvas.h + GameScr.cmy)
    			{
    				isHide = false;
    			}
    			else
    			{
    				isHide = true;
    			}
    		}
    		if (timeDelay > 0)
    		{
    			timeDelay--;
    			if (timeDelay == 0 && command != null)
    			{
    				command.performAction();
    			}
    		}
    		if (!isWayPoint)
    		{
    			return;
    		}
    		if (Char.myCharz().taskMaint != null)
    		{
    			if (Char.myCharz().taskMaint.taskId == 0)
    			{
    				if (Char.myCharz().taskMaint.index == 0)
    				{
    					isPaint = false;
    				}
    				if (Char.myCharz().taskMaint.index == 1)
    				{
    					isPaint = true;
    				}
    				if (Char.myCharz().taskMaint.index > 1 && Char.myCharz().taskMaint.index < 6)
    				{
    					isPaint = false;
    				}
    			}
    			else if (!isPaint)
    			{
    				tDelay++;
    				if (tDelay == 50)
    				{
    					isPaint = true;
    				}
    			}
    		}
    		else if (!isPaint)
    		{
    			Hint.isPaint = false;
    			tDelay++;
    			if (tDelay == 50)
    			{
    				isPaint = true;
    				Hint.isPaint = true;
    			}
    		}
    	}
    
    	public void doClick(int timeDelay)
    	{
    		this.timeDelay = timeDelay;
    	}
    
    	public static void paintAll(mGraphics g)
    	{
    		for (int i = 0; i < vPopups.size(); i++)
    		{
    			((PopUp)vPopups.elementAt(i)).paint(g);
    		}
    	}
    
    	public static void updateAll()
    	{
    		for (int i = 0; i < vPopups.size(); i++)
    		{
    			((PopUp)vPopups.elementAt(i)).update();
    		}
    	}
    }
}
