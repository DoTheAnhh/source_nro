using System;
using System.Collections;
using Game5.Assets.src.e;
using UnityEngine;

namespace Game5
{
    
    public class mGraphics
    {
    	public static int HCENTER = 1;
    
    	public static int VCENTER = 2;
    
    	public static int LEFT = 4;
    
    	public static int RIGHT = 8;
    
    	public static int TOP = 16;
    
    	public static int BOTTOM = 32;
    
    	private float r;
    
    	private float g;
    
    	private float b;
    
    	private float a;
    
    	public int clipX;
    
    	public int clipY;
    
    	public int clipW;
    
    	public int clipH;
    
    	private bool isClip;
    
    	private bool isTranslate = true;
    
    	private int translateX;
    
    	private int translateY;
    
    	private float translateXf;
    
    	private float translateYf;
    
    	public static int zoomLevel = 1;
    
    	public const int BASELINE = 64;
    
    	public const int SOLID = 0;
    
    	public const int DOTTED = 1;
    
    	public const int TRANS_MIRROR = 2;
    
    	public const int TRANS_MIRROR_ROT180 = 1;
    
    	public const int TRANS_MIRROR_ROT270 = 4;
    
    	public const int TRANS_MIRROR_ROT90 = 7;
    
    	public const int TRANS_NONE = 0;
    
    	public const int TRANS_ROT180 = 3;
    
    	public const int TRANS_ROT270 = 6;
    
    	public const int TRANS_ROT90 = 5;
    
    	public static Hashtable cachedTextures = new Hashtable();

    	// ------------------------------------------------------------------
    	//  Kho ảnh một điểm — tra bằng MÃ SỐ, không ghép chuỗi
    	// ------------------------------------------------------------------

    	/// <summary>
    	/// Ảnh một điểm cho từng màu, tra bằng mã số.
    	/// </summary>
    	/// <remarks>
    	/// <para>Bản trước ghép khoá bằng chuỗi: <c>"fr" + 1 + 1 + r + g + b + a</c>.
    	/// Bốn biến ấy là <c>float</c>, nên <b>mỗi lần tô một hình chữ nhật</b> là
    	/// bốn lần <c>float.ToString()</c> cộng mấy lần nối chuỗi — chừng sáu mảnh
    	/// rác, cho <i>một</i> hình.</para>
    	///
    	/// <para>Một khung hình HUD tô cỡ năm trăm hình — riêng mấy dải chuyển màu
    	/// đã kẻ từng dòng một — tức ba nghìn mảnh rác mỗi khung, mười tám vạn mỗi
    	/// giây. Máy tính thì không thấy gì; điện thoại thì đó chính là những cú
    	/// khựng đều đặn của bộ dọn rác, đọc ra thành "tụt khung hình".</para>
    	///
    	/// <para>Khoá số gói bốn thành phần vào một <c>int</c> và tra trong
    	/// <c>Dictionary&lt;int,…&gt;</c> — không sinh mảnh rác nào. Ảnh vẽ ra vẫn
    	/// đúng từng điểm như cũ, vì vẫn là ảnh một điểm đúng màu ấy.</para>
    	/// </remarks>
    	private static readonly System.Collections.Generic.Dictionary<int, Texture2D>
    			khoAnhMau = new System.Collections.Generic.Dictionary<int, Texture2D>();

    	private static int kepByte(float v)
    	{
    		int n = (int)(v * 255f + 0.5f);
    		if (n < 0)
    		{
    			n = 0;
    		}
    		if (n > 255)
    		{
    			n = 255;
    		}
    		return n;
    	}

    	/// <summary>Ảnh một điểm mang đúng màu đang đặt.</summary>
    	private Texture2D anhMauHienTai()
    	{
    		int khoa = (kepByte(r) << 24) | (kepByte(g) << 16)
    				| (kepByte(b) << 8) | kepByte(a);
    		Texture2D t;
    		if (khoAnhMau.TryGetValue(khoa, out t) && t != null)
    		{
    			return t;
    		}
    		if (khoAnhMau.Count > 1024)
    		{
    			khoAnhMau.Clear();
    		}
    		t = new Texture2D(1, 1);
    		t.SetPixel(0, 0, new Color(r, g, b, (a > 1f) ? 1f : a));
    		Image.setTextureQuality(t);
    		t.Apply();
    		khoAnhMau[khoa] = t;
    		return t;
    	}

    	/// <summary>Số dòng của một tấm chuyển màu dựng sẵn.</summary>
    	private const int CAO_DAI_MAU = 64;

    	private static readonly System.Collections.Generic.Dictionary<long, Texture2D>
    			khoDaiMau = new System.Collections.Generic.Dictionary<long, Texture2D>();

    	/// <summary>Tấm chuyển màu dọc, dựng sẵn một lần cho mỗi cặp màu.</summary>
    	private static Texture2D anhDaiMau(int mauTren, int mauDuoi)
    	{
    		long khoa = ((long)(mauTren & 0xFFFFFF) << 24) | (long)(mauDuoi & 0xFFFFFF);
    		Texture2D t;
    		if (khoDaiMau.TryGetValue(khoa, out t) && t != null)
    		{
    			return t;
    		}
    		if (khoDaiMau.Count > 256)
    		{
    			khoDaiMau.Clear();
    		}
    		t = new Texture2D(1, CAO_DAI_MAU, TextureFormat.RGBA32, false);
    		t.filterMode = FilterMode.Bilinear;
    		t.wrapMode = TextureWrapMode.Clamp;
    		t.anisoLevel = 0;
    		float rT = ((mauTren >> 16) & 0xFF) / 255f;
    		float gT = ((mauTren >> 8) & 0xFF) / 255f;
    		float bT = (mauTren & 0xFF) / 255f;
    		float rD = ((mauDuoi >> 16) & 0xFF) / 255f;
    		float gD = ((mauDuoi >> 8) & 0xFF) / 255f;
    		float bD = (mauDuoi & 0xFF) / 255f;
    		for (int i = 0; i < CAO_DAI_MAU; i++)
    		{
    			// Dong 0 cua mot tam anh nam o DAY, nen dong 0 la mau duoi.
    			float k = (float)i / (float)(CAO_DAI_MAU - 1);
    			t.SetPixel(0, i, new Color(rD + (rT - rD) * k,
    					gD + (gT - gD) * k, bD + (bT - bD) * k, 1f));
    		}
    		t.Apply();
    		khoDaiMau[khoa] = t;
    		return t;
    	}

    	/// <summary>
    	/// Một dải chuyển màu dọc, bo góc — <b>vẽ bằng một lệnh duy nhất</b>.
    	/// </summary>
    	/// <remarks>
    	/// <para>Trước đây chỗ nào cần chuyển màu cũng kẻ từng dòng một điểm. Một ô
    	/// kỹ năng cao 28 là 26 lệnh vẽ; đáy màn hình có năm ô, hai nút nhanh, khung
    	/// nhân vật, khung mục tiêu… cộng lại vài trăm lệnh mỗi khung hình, mà mỗi
    	/// lệnh <c>GUI.DrawTexture</c> trên điện thoại không hề rẻ.</para>
    	///
    	/// <para>Dựng sẵn tấm ảnh 1×64 mang đúng dải màu ấy rồi kéo giãn ra: một
    	/// lệnh vẽ, lọc tuyến tính nên còn mượt hơn kẻ dòng, và bo góc vẫn do máy vẽ
    	/// lo như mọi hình chữ nhật khác.</para>
    	/// </remarks>
    	public void veDaiDoc(int x, int y, int w, int h, int radius,
    			int mauTren, int mauDuoi)
    	{
    		if (w <= 0 || h <= 0)
    		{
    			return;
    		}
    		radius *= zoomLevel;
    		float px = (float)(x * zoomLevel);
    		float py = (float)(y * zoomLevel);
    		w *= zoomLevel;
    		h *= zoomLevel;
    		if (isTranslate)
    		{
    			px += (float)translateX;
    			py += (float)translateY;
    		}
    		int cx = 0;
    		int cy = 0;
    		int cw = 0;
    		int ch = 0;
    		if (isClip)
    		{
    			cx = clipX;
    			cy = clipY;
    			cw = clipW;
    			ch = clipH;
    			if (isTranslate)
    			{
    				cx += clipTX;
    				cy += clipTY;
    			}
    			GUI.BeginGroup(new Rect((float)cx, (float)cy, (float)cw, (float)ch));
    		}
    		GUI.DrawTexture(new Rect(px - (float)cx, py - (float)cy, w, h),
    				anhDaiMau(mauTren, mauDuoi),
    				ScaleMode.StretchToFill, false, 0, Color.white,
    				new Vector4(borderSize, borderSize, borderSize, borderSize),
    				new Vector4(radius, radius, radius, radius));
    		if (isClip)
    		{
    			GUI.EndGroup();
    		}
    	}
    
    	public static int addYWhenOpenKeyBoard;
    
    	private int clipTX;
    
    	private int clipTY;
    
    	private int currentBGColor;
    
    	private Vector2 pos = new Vector2(0f, 0f);
    
    	private Rect rect;
    
    	private Matrix4x4 matrixBackup;
    
    	private Vector2 pivot;
    
    	public Vector2 size = new Vector2(128f, 128f);
    
    	public Vector2 relativePosition = new Vector2(0f, 0f);
    
    	public Color clTrans;
    
    	public static Color transParentColor = new Color(1f, 1f, 1f, 0f);
    
    	private Material lineMaterial;
    
    	private void cache(string key, Texture value)
    	{
    		if (cachedTextures.Count > 400)
    		{
    			cachedTextures.Clear();
    		}
    		if (value.width * value.height < GameCanvas.w * GameCanvas.h)
    		{
    			cachedTextures.Add(key, value);
    		}
    	}
    
    	public void translate(int tx, int ty)
    	{
    		tx *= zoomLevel;
    		ty *= zoomLevel;
    		translateX += tx;
    		translateY += ty;
    		isTranslate = true;
    		if (translateX == 0 && translateY == 0)
    		{
    			isTranslate = false;
    		}
    	}
    
    	public void translate(float x, float y)
    	{
    		translateXf += x;
    		translateYf += y;
    		isTranslate = true;
    		if (translateXf == 0f && translateYf == 0f)
    		{
    			isTranslate = false;
    		}
    	}
    
    	/// <summary>Do doi hien tai, tinh bang PIXEL MAN HINH.</summary>
    	/// <remarks>
    	/// getTranslateX() chia cho zoomLevel de tra ve toa do game, con
    	/// getTranslateY() lai cong them addYWhenOpenKeyBoard. Cho nao can
    	/// dat diem neo cho ma tran GUI thi phai la con so THO nay — dung
    	/// hai ham kia la lech, va lech am tham chu khong bao loi gi.
    	/// </remarks>
    	public int getTranslateXPx()
    	{
    		return translateX;
    	}

    	public int getTranslateYPx()
    	{
    		return translateY;
    	}

    	public int getTranslateX()
    	{
    		return translateX / zoomLevel;
    	}
    
    	public int getTranslateY()
    	{
    		return translateY / zoomLevel + addYWhenOpenKeyBoard;
    	}
    
    	public void setClip(int x, int y, int w, int h)
    	{
    		x *= zoomLevel;
    		y *= zoomLevel;
    		w *= zoomLevel;
    		h *= zoomLevel;
    		clipTX = translateX;
    		clipTY = translateY;
    		clipX = x;
    		clipY = y;
    		clipW = w;
    		clipH = h;
    		isClip = true;
    	}
    
    	public int getClipX()
    	{
    		return GameScr.cmx;
    	}
    
    	public int getClipY()
    	{
    		return GameScr.cmy;
    	}
    
    	public int getClipWidth()
    	{
    		return GameScr.gW;
    	}
    
    	public int getClipHeight()
    	{
    		return GameScr.gH;
        }
        public void fillRect(int x, int y, int w, int h, int Radius)
        {
            Radius *= zoomLevel;
            float num = (float)(x * zoomLevel);
            float num2 = (float)(y * zoomLevel);
            w *= zoomLevel;
            h *= zoomLevel;
    
            if (this.isTranslate)
            {
                num += (float)this.translateX;
                num2 += (float)this.translateY;
            }
            Texture2D texture2D = anhMauHienTai();
            int num4 = 0;
            int num5 = 0;
            int num6 = 0;
            int num7 = 0;
            if (this.isClip)
            {
                num4 = this.clipX;
                num5 = this.clipY;
                num6 = this.clipW;
                num7 = this.clipH;
                if (this.isTranslate)
                {
                    num4 += this.clipTX;
                    num5 += this.clipTY;
                }
                GUI.BeginGroup(new Rect((float)num4, (float)num5, (float)num6, (float)num7));
            }
    
            GUI.DrawTexture(new Rect(num - (float)num4, num2 - (float)num5, w, h), texture2D,
                ScaleMode.StretchToFill, false, 0, Color.white, new Vector4(borderSize, borderSize, borderSize, borderSize),
                new Vector4(Radius, Radius, Radius, Radius));
    
            if (this.isClip)
            {
                GUI.EndGroup();
            }
        }
        public float borderSize;
    
        public void fillRect(int x, int y, int w, int h, int color, int alpha)
    	{
    		float alpha2 = 0.5f;
    		setColor(color, alpha2);
    		fillRect(x, y, w, h);
    	}
    
    	public void drawLine(int x1, int y1, int x2, int y2)
    	{
    		x1 *= zoomLevel;
    		y1 *= zoomLevel;
    		x2 *= zoomLevel;
    		y2 *= zoomLevel;
    		if (y1 == y2)
    		{
    			if (x1 > x2)
    			{
    				int num = x2;
    				x2 = x1;
    				x1 = num;
    			}
    			fillRect(x1, y1, x2 - x1, 1);
    			return;
    		}
    		if (x1 == x2)
    		{
    			if (y1 > y2)
    			{
    				int num2 = y2;
    				y2 = y1;
    				y1 = num2;
    			}
    			fillRect(x1, y1, 1, y2 - y1);
    			return;
    		}
    		if (isTranslate)
    		{
    			x1 += translateX;
    			y1 += translateY;
    			x2 += translateX;
    			y2 += translateY;
    		}
    		string key = "dl" + r + g + b;
    		Texture2D texture2D = (Texture2D)cachedTextures[key];
    		if (texture2D == null)
    		{
    			texture2D = new Texture2D(1, 1);
    			Color color = new Color(r, g, b);
    			texture2D.SetPixel(0, 0, color);
    			texture2D.Apply();
    			cache(key, texture2D);
    		}
    		Vector2 vector = new Vector2(x1, y1);
    		Vector2 vector2 = new Vector2(x2, y2);
    		Vector2 vector3 = vector2 - vector;
    		float num3 = 57.29578f * Mathf.Atan(vector3.y / vector3.x);
    		if (vector3.x < 0f)
    		{
    			num3 += 180f;
    		}
    		int num4 = (int)Mathf.Ceil(0f);
    		GUIUtility.RotateAroundPivot(num3, vector);
    		int num5 = 0;
    		int num6 = 0;
    		int num7 = 0;
    		int num8 = 0;
    		if (isClip)
    		{
    			num5 = clipX;
    			num6 = clipY;
    			num7 = clipW;
    			num8 = clipH;
    			if (isTranslate)
    			{
    				num5 += clipTX;
    				num6 += clipTY;
    			}
    		}
    		if (isClip)
    		{
    			GUI.BeginGroup(new Rect(num5, num6, num7, num8));
    		}
    		Graphics.DrawTexture(new Rect(vector.x - (float)num5, vector.y - (float)num4 - (float)num6, vector3.magnitude, 1f), texture2D);
    		if (isClip)
    		{
    			GUI.EndGroup();
    		}
    		GUIUtility.RotateAroundPivot(0f - num3, vector);
    	}
    
    	public Color setColorMiniMap(int rgb)
    	{
    		int num = rgb & 0xFF;
    		int num2 = (rgb >> 8) & 0xFF;
    		int num3 = (rgb >> 16) & 0xFF;
    		float num4 = (float)num / 256f;
    		float num5 = (float)num2 / 256f;
    		float num6 = (float)num3 / 256f;
    		return new Color(num6, num5, num4);
    	}
    
    	public float[] getRGB(Color cl)
    	{
    		float num = 256f * cl.r;
    		float num2 = 256f * cl.g;
    		float num3 = 256f * cl.b;
    		return new float[3] { num, num2, num3 };
    	}
    
    	public void drawRect(int x, int y, int w, int h)
    	{
    		int num = 1;
    		fillRect(x, y, w, num);
    		fillRect(x, y, num, h);
    		fillRect(x + w, y, num, h + 1);
    		fillRect(x, y + h, w + 1, num);
    	}
    
    	public void fillRect(int x, int y, int w, int h)
    	{
    		x *= zoomLevel;
    		y *= zoomLevel;
    		w *= zoomLevel;
    		h *= zoomLevel;
    		if (w < 0 || h < 0)
    		{
    			return;
    		}
    		if (isTranslate)
    		{
    			x += translateX;
    			y += translateY;
    		}
    		Texture2D texture2D = anhMauHienTai();
    		int num3 = 0;
    		int num4 = 0;
    		int num5 = 0;
    		int num6 = 0;
    		if (isClip)
    		{
    			num3 = clipX;
    			num4 = clipY;
    			num5 = clipW;
    			num6 = clipH;
    			if (isTranslate)
    			{
    				num3 += clipTX;
    				num4 += clipTY;
    			}
    		}
    		if (isClip)
    		{
    			GUI.BeginGroup(new Rect(num3, num4, num5, num6));
    		}
    		GUI.DrawTexture(new Rect(x - num3, y - num4, w, h), texture2D);
    		if (isClip)
    		{
    			GUI.EndGroup();
    		}
    	}
    
    	public void setColor(int rgb)
    	{
    		int num = rgb & 0xFF;
    		int num2 = (rgb >> 8) & 0xFF;
    		int num3 = (rgb >> 16) & 0xFF;
    		b = (float)num / 256f;
    		g = (float)num2 / 256f;
    		r = (float)num3 / 256f;
    		a = 255f;
    	}
    
    	public void setColor(Color color)
    	{
    		b = color.b;
    		g = color.g;
    		r = color.r;
    	}
    
    	public void setBgColor(int rgb)
    	{
    		if (rgb != currentBGColor)
    		{
    			currentBGColor = rgb;
    			int num = rgb & 0xFF;
    			int num2 = (rgb >> 8) & 0xFF;
    			int num3 = (rgb >> 16) & 0xFF;
    			b = (float)num / 256f;
    			g = (float)num2 / 256f;
    			r = (float)num3 / 256f;
    			Main.main.GetComponent<Camera>().backgroundColor = new Color(r, g, b);
    		}
    	}
    
    	public void drawString(string s, int x, int y, GUIStyle style)
    	{
    		x *= zoomLevel;
    		y *= zoomLevel;
    		if (isTranslate)
    		{
    			x += translateX;
    			y += translateY;
    		}
    		int num = 0;
    		int num2 = 0;
    		int num3 = 0;
    		int num4 = 0;
    		if (isClip)
    		{
    			num = clipX;
    			num2 = clipY;
    			num3 = clipW;
    			num4 = clipH;
    			if (isTranslate)
    			{
    				num += clipTX;
    				num2 += clipTY;
    			}
    		}
    		if (isClip)
    		{
    			GUI.BeginGroup(new Rect(num, num2, num3, num4));
    		}
    		GUI.Label(new Rect(x - num, y - num2, ScaleGUI.WIDTH, 100f), s, style);
    		if (isClip)
    		{
    			GUI.EndGroup();
    		}
    	}
    
    	public void setColor(int rgb, float alpha)
    	{
    		int num = rgb & 0xFF;
    		int num2 = (rgb >> 8) & 0xFF;
    		int num3 = (rgb >> 16) & 0xFF;
    		b = (float)num / 256f;
    		g = (float)num2 / 256f;
    		r = (float)num3 / 256f;
    		a = alpha;
    	}
    
    	public void drawString(string s, int x, int y, GUIStyle style, int w)
    	{
    		x *= zoomLevel;
    		y *= zoomLevel;
    		if (isTranslate)
    		{
    			x += translateX;
    			y += translateY;
    		}
    		int num = 0;
    		int num2 = 0;
    		int num3 = 0;
    		int num4 = 0;
    		if (isClip)
    		{
    			num = clipX;
    			num2 = clipY;
    			num3 = clipW;
    			num4 = clipH;
    			if (isTranslate)
    			{
    				num += clipTX;
    				num2 += clipTY;
    			}
    		}
    		if (isClip)
    		{
    			GUI.BeginGroup(new Rect(num, num2, num3, num4));
    		}
    		GUI.Label(new Rect(x - num, y - num2 - 4, w, 100f), s, style);
    		if (isClip)
    		{
    			GUI.EndGroup();
    		}
    	}
    
    	private void UpdatePos(int anchor)
    	{
    		Vector2 vector = new Vector2(0f, 0f);
    		switch (anchor)
    		{
    		case 3:
    			vector = new Vector2(size.x / 2f, size.y / 2f);
    			break;
    		case 20:
    			vector = new Vector2(0f, 0f);
    			break;
    		case 17:
    			vector = new Vector2(Screen.width / 2, 0f);
    			break;
    		case 24:
    			vector = new Vector2(Screen.width, 0f);
    			break;
    		case 6:
    			vector = new Vector2(0f, Screen.height / 2);
    			break;
    		case 10:
    			vector = new Vector2(Screen.width, Screen.height / 2);
    			break;
    		case 36:
    			vector = new Vector2(0f, Screen.height);
    			break;
    		case 33:
    			vector = new Vector2(Screen.width / 2, Screen.height);
    			break;
    		case 40:
    			vector = new Vector2(Screen.width, Screen.height);
    			break;
    		}
    		pos = vector + relativePosition;
    		rect = new Rect(pos.x - size.x * 0.5f, pos.y - size.y * 0.5f, size.x, size.y);
    		pivot = new Vector2(rect.xMin + rect.width * 0.5f, rect.yMin + rect.height * 0.5f);
    	}
    
    	public void drawRegion(Image arg0, int x0, int y0, int w0, int h0, int arg5, int x, int y, int arg8)
    	{
    		if (arg0 != null)
    		{
    			x *= zoomLevel;
    			y *= zoomLevel;
    			x0 *= zoomLevel;
    			y0 *= zoomLevel;
    			w0 *= zoomLevel;
    			h0 *= zoomLevel;
    			_drawRegion(arg0, x0, y0, w0, h0, arg5, x, y, arg8);
    		}
    	}
    
    	public void drawRegion(Image arg0, int x0, int y0, int w0, int h0, int arg5, float x, float y, int arg8)
    	{
    		if (arg0 != null)
    		{
    			x *= (float)zoomLevel;
    			y *= (float)zoomLevel;
    			x0 *= zoomLevel;
    			y0 *= zoomLevel;
    			w0 *= zoomLevel;
    			h0 *= zoomLevel;
    			__drawRegion(arg0, x0, y0, w0, h0, arg5, x, y, arg8);
    		}
    	}
    
    	public void drawRegion(Image arg0, int x0, int y0, int w0, int h0, int arg5, int x, int y, int arg8, bool isClip)
    	{
    		drawRegion(arg0, x0, y0, w0, h0, arg5, x, y, arg8);
    	}
    
    	public void __drawRegion(Image image, int x0, int y0, int w, int h, int transform, float x, float y, int anchor)
    	{
    		if (image == null)
    		{
    			return;
    		}
    		if (isTranslate)
    		{
    			x += (float)translateX;
    			y += (float)translateY;
    		}
    		float num = w;
    		float num2 = h;
    		float num3 = 0f;
    		float num4 = 0f;
    		float num5 = 0f;
    		float num6 = 0f;
    		float num7 = 1f;
    		float num8 = 0f;
    		int num9 = 1;
    		if ((anchor & HCENTER) == HCENTER)
    		{
    			num5 -= num / 2f;
    		}
    		if ((anchor & VCENTER) == VCENTER)
    		{
    			num6 -= num2 / 2f;
    		}
    		if ((anchor & RIGHT) == RIGHT)
    		{
    			num5 -= num;
    		}
    		if ((anchor & BOTTOM) == BOTTOM)
    		{
    			num6 -= num2;
    		}
    		x += num5;
    		y += num6;
    		int num10 = 0;
    		int num11 = 0;
    		int num12 = 0;
    		int num13 = 0;
    		if (isClip)
    		{
    			num10 = clipX;
    			num11 = clipY;
    			num12 = clipW;
    			num13 = clipH;
    			if (isTranslate)
    			{
    				num10 += clipTX;
    				num11 += clipTY;
    			}
    			Rect r = new Rect(x, y, w, h);
    			Rect rect = intersectRect(r2: new Rect(num10, num11, num12, num13), r1: r);
    			if (rect.width <= 0f || rect.height <= 0f)
    			{
    				return;
    			}
    			num = rect.width;
    			num2 = rect.height;
    			num3 = rect.x - r.x;
    			num4 = rect.y - r.y;
    		}
    		float num14 = 0f;
    		float num15 = 0f;
    		switch (transform)
    		{
    		case 2:
    			num14 += num;
    			num7 = -1f;
    			if (isClip)
    			{
    				if ((float)num10 > x)
    				{
    					num8 = 0f - num3;
    				}
    				else if ((float)(num10 + num12) < x + (float)w)
    				{
    					num8 = 0f - ((float)(num10 + num12) - x - (float)w);
    				}
    			}
    			break;
    		case 1:
    			num9 = -1;
    			num15 += num2;
    			break;
    		case 3:
    			num9 = -1;
    			num15 += num2;
    			num7 = -1f;
    			num14 += num;
    			break;
    		}
    		int num16 = 0;
    		int num17 = 0;
    		if (transform == 5 || transform == 6 || transform == 4 || transform == 7)
    		{
    			matrixBackup = GUI.matrix;
    			size = new Vector2(w, h);
    			relativePosition = new Vector2(x, y);
    			UpdatePos(3);
    			switch (transform)
    			{
    			case 6:
    				UpdatePos(3);
    				break;
    			case 5:
    				size = new Vector2(w, h);
    				UpdatePos(3);
    				break;
    			}
    			switch (transform)
    			{
    			case 5:
    				GUIUtility.RotateAroundPivot(90f, pivot);
    				break;
    			case 6:
    				GUIUtility.RotateAroundPivot(270f, pivot);
    				break;
    			case 4:
    				GUIUtility.RotateAroundPivot(270f, pivot);
    				num14 += num;
    				num7 = -1f;
    				if (isClip)
    				{
    					if ((float)num10 > x)
    					{
    						num8 = 0f - num3;
    					}
    					else if ((float)(num10 + num12) < x + (float)w)
    					{
    						num8 = 0f - ((float)(num10 + num12) - x - (float)w);
    					}
    				}
    				break;
    			case 7:
    				GUIUtility.RotateAroundPivot(270f, pivot);
    				num9 = -1;
    				num15 += num2;
    				break;
    			}
    		}
    		Graphics.DrawTexture(new Rect(x + num3 + num14 + (float)num16, y + num4 + (float)num17 + num15, num * num7, num2 * (float)num9), image.texture, new Rect(((float)x0 + num3 + num8) / (float)image.texture.width, ((float)image.texture.height - num2 - ((float)y0 + num4)) / (float)image.texture.height, num / (float)image.texture.width, num2 / (float)image.texture.height), 0, 0, 0, 0);
    		if (transform == 5 || transform == 6 || transform == 4 || transform == 7)
    		{
    			GUI.matrix = matrixBackup;
    		}
    	}
    
    	public void _drawRegion(Image image, float x0, float y0, int w, int h, int transform, int x, int y, int anchor)
    	{
    		if (image == null)
    		{
    			return;
    		}
    		if (isTranslate)
    		{
    			x += translateX;
    			y += translateY;
    		}
    		float num = w;
    		float num2 = h;
    		float num3 = 0f;
    		float num4 = 0f;
    		float num5 = 0f;
    		float num6 = 0f;
    		float num7 = 1f;
    		float num8 = 0f;
    		int num9 = 1;
    		if ((anchor & HCENTER) == HCENTER)
    		{
    			num5 -= num / 2f;
    		}
    		if ((anchor & VCENTER) == VCENTER)
    		{
    			num6 -= num2 / 2f;
    		}
    		if ((anchor & RIGHT) == RIGHT)
    		{
    			num5 -= num;
    		}
    		if ((anchor & BOTTOM) == BOTTOM)
    		{
    			num6 -= num2;
    		}
    		x += (int)num5;
    		y += (int)num6;
    		int num10 = 0;
    		int num11 = 0;
    		int num12 = 0;
    		int num13 = 0;
    		if (isClip)
    		{
    			num10 = clipX;
    			num11 = clipY;
    			num12 = clipW;
    			num13 = clipH;
    			if (isTranslate)
    			{
    				num10 += clipTX;
    				num11 += clipTY;
    			}
    			Rect r = new Rect(x, y, w, h);
    			Rect rect = intersectRect(r2: new Rect(num10, num11, num12, num13), r1: r);
    			if (rect.width <= 0f || rect.height <= 0f)
    			{
    				return;
    			}
    			num = rect.width;
    			num2 = rect.height;
    			num3 = rect.x - r.x;
    			num4 = rect.y - r.y;
    		}
    		float num14 = 0f;
    		float num15 = 0f;
    		switch (transform)
    		{
    		case 2:
    			num14 += num;
    			num7 = -1f;
    			if (isClip)
    			{
    				if (num10 > x)
    				{
    					num8 = 0f - num3;
    				}
    				else if (num10 + num12 < x + w)
    				{
    					num8 = -(num10 + num12 - x - w);
    				}
    			}
    			break;
    		case 1:
    			num9 = -1;
    			num15 += num2;
    			break;
    		case 3:
    			num9 = -1;
    			num15 += num2;
    			num7 = -1f;
    			num14 += num;
    			break;
    		}
    		int num16 = 0;
    		int num17 = 0;
    		if (transform == 5 || transform == 6 || transform == 4 || transform == 7)
    		{
    			matrixBackup = GUI.matrix;
    			size = new Vector2(w, h);
    			relativePosition = new Vector2(x, y);
    			UpdatePos(3);
    			switch (transform)
    			{
    			case 6:
    				UpdatePos(3);
    				break;
    			case 5:
    				size = new Vector2(w, h);
    				UpdatePos(3);
    				break;
    			}
    			switch (transform)
    			{
    			case 5:
    				GUIUtility.RotateAroundPivot(90f, pivot);
    				break;
    			case 6:
    				GUIUtility.RotateAroundPivot(270f, pivot);
    				break;
    			case 4:
    				GUIUtility.RotateAroundPivot(270f, pivot);
    				num14 += num;
    				num7 = -1f;
    				if (isClip)
    				{
    					if (num10 > x)
    					{
    						num8 = 0f - num3;
    					}
    					else if (num10 + num12 < x + w)
    					{
    						num8 = -(num10 + num12 - x - w);
    					}
    				}
    				break;
    			case 7:
    				GUIUtility.RotateAroundPivot(270f, pivot);
    				num9 = -1;
    				num15 += num2;
    				break;
    			}
    		}
    		Graphics.DrawTexture(new Rect((float)x + num3 + num14 + (float)num16, (float)y + num4 + (float)num17 + num15, num * num7, num2 * (float)num9), image.texture, new Rect((x0 + num3 + num8) / (float)image.texture.width, ((float)image.texture.height - num2 - (y0 + num4)) / (float)image.texture.height, num / (float)image.texture.width, num2 / (float)image.texture.height), 0, 0, 0, 0);
    		if (transform == 5 || transform == 6 || transform == 4 || transform == 7)
    		{
    			GUI.matrix = matrixBackup;
    		}
    	}
    
    	public void drawRegionGui(Image image, float x0, float y0, int w, int h, int transform, float x, float y, int anchor)
    	{
    		GUI.color = setColorMiniMap(807956);
    		x *= (float)zoomLevel;
    		y *= (float)zoomLevel;
    		x0 *= (float)zoomLevel;
    		y0 *= (float)zoomLevel;
    		w *= zoomLevel;
    		h *= zoomLevel;
    	}
    
    	public void drawRegion2(Image image, float x0, float y0, int w, int h, int transform, int x, int y, int anchor)
    	{
    		GUI.color = image.colorBlend;
    		if (isTranslate)
    		{
    			x += translateX;
    			y += translateY;
    		}
    		string key = "dg" + x0 + y0 + w + h + transform + image.GetHashCode();
    		Texture2D texture2D = (Texture2D)cachedTextures[key];
    		if (texture2D == null)
    		{
    			Image image2 = Image.createImage(image, (int)x0, (int)y0, w, h, transform);
    			texture2D = image2.texture;
    			cache(key, texture2D);
    		}
    		int num = 0;
    		int num2 = 0;
    		int num3 = 0;
    		int num4 = 0;
    		float num5 = w;
    		float num6 = h;
    		float num7 = 0f;
    		float num8 = 0f;
    		if ((anchor & HCENTER) == HCENTER)
    		{
    			num7 -= num5 / 2f;
    		}
    		if ((anchor & VCENTER) == VCENTER)
    		{
    			num8 -= num6 / 2f;
    		}
    		if ((anchor & RIGHT) == RIGHT)
    		{
    			num7 -= num5;
    		}
    		if ((anchor & BOTTOM) == BOTTOM)
    		{
    			num8 -= num6;
    		}
    		x += (int)num7;
    		y += (int)num8;
    		if (isClip)
    		{
    			num = clipX;
    			num2 = clipY;
    			num3 = clipW;
    			num4 = clipH;
    			if (isTranslate)
    			{
    				num += clipTX;
    				num2 += clipTY;
    			}
    		}
    		if (isClip)
    		{
    			GUI.BeginGroup(new Rect(num, num2, num3, num4));
    		}
    		GUI.DrawTexture(new Rect(x - num, y - num2, w, h), texture2D);
    		if (isClip)
    		{
    			GUI.EndGroup();
    		}
    		GUI.color = new Color(1f, 1f, 1f, 1f);
    	}
    
    	public void drawImagaByDrawTexture(Image image, float x, float y)
    	{
    		x *= (float)zoomLevel;
    		y *= (float)zoomLevel;
    		GUI.DrawTexture(new Rect(x + (float)translateX, y + (float)translateY, image.getRealImageWidth(), image.getRealImageHeight()), image.texture);
    	}
    
    	public void drawImage(Image image, int x, int y, int anchor)
    	{
    		if (image != null)
    		{
    			drawRegion(image, 0, 0, getImageWidth(image), getImageHeight(image), 0, x, y, anchor);
    		}
    	}
    
    	public void drawImageFog(Image image, int x, int y, int anchor)
    	{
    		if (image != null)
    		{
    			drawRegion(image, 0, 0, image.texture.width, image.texture.height, 0, x, y, anchor);
    		}
    	}
    
    	public void drawImage(Image image, int x, int y)
    	{
    		if (image != null)
    		{
    			drawRegion(image, 0, 0, getImageWidth(image), getImageHeight(image), 0, x, y, TOP | LEFT);
    		}
    	}
    
    	public void drawImage(Image image, float x, float y, int anchor)
    	{
    		if (image != null)
    		{
    			drawRegion(image, 0, 0, getImageWidth(image), getImageHeight(image), 0, x, y, anchor);
    		}
    	}
    
    	public void drawRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight)
    	{
    		drawRect(x, y, w, h);
    	}
    
    	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
    	{
    		fillRect(x, y, width, height);
    	}
    
    	public void reset()
    	{
    		isClip = false;
    		isTranslate = false;
    		translateX = 0;
    		translateY = 0;
    	}
    
    	public Rect intersectRect(Rect r1, Rect r2)
    	{
    		float num = r1.x;
    		float num2 = r1.y;
    		float x = r2.x;
    		float y = r2.y;
    		float num3 = num;
    		num3 += r1.width;
    		float num4 = num2;
    		num4 += r1.height;
    		float num5 = x;
    		num5 += r2.width;
    		float num6 = y;
    		num6 += r2.height;
    		if (num < x)
    		{
    			num = x;
    		}
    		if (num2 < y)
    		{
    			num2 = y;
    		}
    		if (num3 > num5)
    		{
    			num3 = num5;
    		}
    		if (num4 > num6)
    		{
    			num4 = num6;
    		}
    		num3 -= num;
    		num4 -= num2;
    		if (num3 < -30000f)
    		{
    			num3 = -30000f;
    		}
    		if (num4 < -30000f)
    		{
    			num4 = -30000f;
    		}
    		return new Rect(num, num2, (int)num3, (int)num4);
    	}
    
    	public void drawImageScale(Image image, int x, int y, int w, int h, int tranform)
    	{
    		GUI.color = Color.red;
    		x *= zoomLevel;
    		y *= zoomLevel;
    		w *= zoomLevel;
    		h *= zoomLevel;
    		if (image != null)
    		{
    			Graphics.DrawTexture(new Rect(x + translateX, y + translateY, (tranform != 0) ? (-w) : w, h), image.texture);
    		}
    	}
    
    	public void drawImageSimple(Image image, int x, int y)
    	{
    		x *= zoomLevel;
    		y *= zoomLevel;
    		if (image != null)
    		{
    			Graphics.DrawTexture(new Rect(x, y, image.w, image.h), image.texture);
    		}
    	}
    
    	public static int getImageWidth(Image image)
    	{
    		return image.getWidth();
    	}
    
    	public static int getImageHeight(Image image)
    	{
    		return image.getHeight();
    	}
    
    	public static bool isNotTranColor(Color color)
    	{
    		if (color == Color.clear || color == transParentColor)
    		{
    			return false;
    		}
    		return true;
    	}
    
    	public static Image blend(Image img0, float level, int rgb)
    	{
    		int num = rgb & 0xFF;
    		int num2 = (rgb >> 8) & 0xFF;
    		int num3 = (rgb >> 16) & 0xFF;
    		float num4 = (float)num / 256f;
    		float num5 = (float)num2 / 256f;
    		float num6 = (float)num3 / 256f;
    		Color color = new Color(num6, num5, num4);
    		Color[] pixels = img0.texture.GetPixels();
    		float num7 = color.r;
    		float num8 = color.g;
    		float num9 = color.b;
    		for (int i = 0; i < pixels.Length; i++)
    		{
    			Color color2 = pixels[i];
    			if (isNotTranColor(color2))
    			{
    				float num10 = (num7 - color2.r) * level + color2.r;
    				float num11 = (num8 - color2.g) * level + color2.g;
    				float num12 = (num9 - color2.b) * level + color2.b;
    				if (num10 > 255f)
    				{
    					num10 = 255f;
    				}
    				if (num10 < 0f)
    				{
    					num10 = 0f;
    				}
    				if (num11 > 255f)
    				{
    					num11 = 255f;
    				}
    				if (num11 < 0f)
    				{
    					num11 = 0f;
    				}
    				if (num12 < 0f)
    				{
    					num12 = 0f;
    				}
    				if (num12 > 255f)
    				{
    					num12 = 255f;
    				}
    				pixels[i].r = num10;
    				pixels[i].g = num11;
    				pixels[i].b = num12;
    			}
    		}
    		Image image = Image.createImage(img0.getRealImageWidth(), img0.getRealImageHeight());
    		image.texture.SetPixels(pixels);
    		Image.setTextureQuality(image.texture);
    		image.texture.Apply();
    		Cout.LogError2("BLEND ----------------------------------------------------");
    		return image;
    	}
    
    	public static Color setColorObj(int rgb)
    	{
    		int num = rgb & 0xFF;
    		int num2 = (rgb >> 8) & 0xFF;
    		int num3 = (rgb >> 16) & 0xFF;
    		float num4 = (float)num / 256f;
    		float num5 = (float)num2 / 256f;
    		float num6 = (float)num3 / 256f;
    		return new Color(num6, num5, num4);
    	}
    
    	public void fillTrans(Image imgTrans, int x, int y, int w, int h)
    	{
    		setColor(0, 0.5f);
    		fillRect(x * zoomLevel, y * zoomLevel, w * zoomLevel, h * zoomLevel);
    	}
    
    	public static int blendColor(float level, int color, int colorBlend)
    	{
    		Color color2 = setColorObj(colorBlend);
    		float num = color2.r * 255f;
    		float num2 = color2.g * 255f;
    		float num3 = color2.b * 255f;
    		Color color3 = setColorObj(color);
    		float num4 = (num + color3.r) * level + color3.r;
    		float num5 = (num2 + color3.g) * level + color3.g;
    		float num6 = (num3 + color3.b) * level + color3.b;
    		if (num4 > 255f)
    		{
    			num4 = 255f;
    		}
    		if (num4 < 0f)
    		{
    			num4 = 0f;
    		}
    		if (num5 > 255f)
    		{
    			num5 = 255f;
    		}
    		if (num5 < 0f)
    		{
    			num5 = 0f;
    		}
    		if (num6 < 0f)
    		{
    			num6 = 0f;
    		}
    		if (num6 > 255f)
    		{
    			num6 = 255f;
    		}
    		return (int)num6 & (255 + ((int)num5 << 8)) & (255 + ((int)num4 << 16)) & 0xFF;
    	}
    
    	public static int getIntByColor(Color cl)
    	{
    		float num = cl.r * 255f;
    		float num2 = cl.b * 255f;
    		float num3 = cl.g * 255f;
    		return (((int)num & 0xFF) << 16) | (((int)num3 & 0xFF) << 8) | ((int)num2 & 0xFF);
    	}
    
    	public static int getRealImageWidth(Image img)
    	{
    		return img.w;
    	}
    
    	public static int getRealImageHeight(Image img)
    	{
    		return img.h;
    	}
    
    	public void fillArg(int i, int j, int k, int l, int m, int n)
    	{
    		fillRect(i * zoomLevel, j * zoomLevel, k * zoomLevel, l * zoomLevel);
    	}
    
    	public void CreateLineMaterial()
    	{
    		if (!lineMaterial)
    		{
    			lineMaterial = new Material("Shader \"Lines/Colored Blended\" {SubShader { Pass {  Blend SrcAlpha OneMinusSrcAlpha  ZWrite Off Cull Off Fog { Mode Off }  BindChannels { Bind \"vertex\", vertex Bind \"color\", color }} } }");
    			lineMaterial.hideFlags = HideFlags.HideAndDontSave;
    			lineMaterial.shader.hideFlags = HideFlags.HideAndDontSave;
    		}
    	}
    
    	public void drawlineGL(MyVector totalLine)
    	{
    		lineMaterial.SetPass(0);
    		GL.PushMatrix();
    		GL.Begin(1);
    		for (int i = 0; i < totalLine.size(); i++)
    		{
    			mLine mLine2 = (mLine)totalLine.elementAt(i);
    			GL.Color(new Color(mLine2.r, mLine2.g, mLine2.b, mLine2.a));
    			int num = mLine2.x1 * zoomLevel;
    			int num2 = mLine2.y1 * zoomLevel;
    			int num3 = mLine2.x2 * zoomLevel;
    			int num4 = mLine2.y2 * zoomLevel;
    			if (isTranslate)
    			{
    				num += translateX;
    				num2 += translateY;
    				num3 += translateX;
    				num4 += translateY;
    			}
    			for (int j = 0; j < zoomLevel; j++)
    			{
    				GL.Vertex(new Vector2(num + j, num2 + j));
    				GL.Vertex(new Vector2(num3 + j, num4 + j));
    				if (j > 0)
    				{
    					GL.Vertex(new Vector2(num + j, num2));
    					GL.Vertex(new Vector2(num3 + j, num4));
    					GL.Vertex(new Vector2(num, num2 + j));
    					GL.Vertex(new Vector2(num3, num4 + j));
    				}
    			}
    		}
    		GL.End();
    		GL.PopMatrix();
    		totalLine.removeAllElements();
    	}
    
    	public void drawLine(mGraphics g, int x, int y, int xTo, int yTo, int nLine, int color)
    	{
    		MyVector myVector = new MyVector();
    		for (int i = 0; i < nLine; i++)
    		{
    			myVector.addElement(new mLine(x, y, xTo + i, yTo + i, color));
    		}
    		g.drawlineGL(myVector);
    	}
    
    	internal void drawRegion(Small img, int p1, int p2, int p3, int p4, int transform, int x, int y, int anchor)
    	{
    		throw new NotImplementedException();
    	}
    }
}
