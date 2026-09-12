using System;
using Game4.Assets.src.g;
using Game4.God;
using UnityEngine;

namespace Game4
{
    
    public class GameScr : mScreen, IChatable
    {
    	public bool isWaitingDoubleClick;
    
    	public long timeStartDblClick;
    
    	public long timeEndDblClick;
    
    	public static bool isPaintOther = false;
    
    	public static MyVector textTime = new MyVector(string.Empty);
    
    	public static bool isLoadAllData = false;
    
    	public static GameScr instance;
    
        public static Image doikhu;
    
        public static Image arrow4, arrow5;
    
        public static int gW;
    
    	public static int gH;
    
    	public static int gW2;
    
    	public static int gssw;
    
    	public static int gssh;
    
    	public static int gH34;
    
    	public static int gW3;
    
    	public static int gH3;
    
    	public static int gH23;
    
    	public static int gW23;
    
    	public static int gH2;
    
    	public static int csPadMaxH;
    
    	public static int cmdBarH;
    
    	public static int gW34;
    
    	public static int gW6;
    
    	public static int gH6;
    
    	public static int cmx;
    
    	public static int cmy;
    
    	public static int cmdx;
    
    	public static int cmdy;
    
    	public static int cmvx;
    
    	public static int cmvy;
    
    	public static int cmtoX;
    
    	public static int cmtoY;
    
    	public static int cmxLim;
    
    	public static int cmyLim;
    
    	public static int gssx;
    
    	public static int gssy;
    
    	public static int gssxe;
    
    	public static int gssye;
    
    	public Command cmdback;
    
    	public Command cmdBag;
    
    	public Command cmdSkill;
    
    	public Command cmdTiemnang;
    
    	public Command cmdtrangbi;
    
    	public Command cmdInfo;
    
    	public Command cmdFocus;
    
    	public Command cmdFire;
    
    	public static int d;
    
    	public static int hpPotion;
    
    	public static SkillPaint[] sks;
    
    	public static Arrowpaint[] arrs;
    
    	public static DartInfo[] darts;
    
    	public static Part[] parts;
    
    	public static EffectCharPaint[] efs;
    
    	public static int lockTick;
    
    	private int moveUp;
    
    	private int moveDow;
    
    	private int idTypeTask;
    
    	private bool isstarOpen;
    
    	private bool isChangeSkill;
    
    	public static MyVector vClan = new MyVector();
    
    	public static MyVector vPtMap = new MyVector();
    
    	public static MyVector vFriend = new MyVector();
    
    	public static MyVector vEnemies = new MyVector();
    
    	public static MyVector vCharInMap = new MyVector();
    
    	public static MyVector vItemMap = new MyVector();
    
    	public static MyVector vMobAttack = new MyVector();
    
    	public static MyVector vSet = new MyVector();
    
    	public static MyVector vMob = new MyVector();
    
    	public static MyVector vNpc = new MyVector();
    
    	public static MyVector vFlag = new MyVector();
    
    	public static NClass[] nClasss;
    
    	public static int indexSize = 28;
    
    	public static int indexTitle = 0;
    
    	public static int indexSelect = 0;
    
    	public static int indexRow = -1;
    
    	public static int indexRowMax;
    
    	public static int indexMenu = 0;
    
    	public Item itemFocus;
    
    	public ItemOptionTemplate[] iOptionTemplates;
    
    	public SkillOptionTemplate[] sOptionTemplates;
    
    	private static Scroll scrInfo = new Scroll();
    
    	public static Scroll scrMain = new Scroll();
    
    	public static MyVector vItemUpGrade = new MyVector();
    
    	public static bool isTypeXu;
    
    	public static bool isViewNext;
    
    	public static bool isViewClanMemOnline = false;
    
    	public static bool isViewClanInvite = true;
    
    	public static bool isChop;
    
    	public static string titleInputText = string.Empty;
    
    	public static int tickMove;
    
    	public static bool isPaintAlert = false;
    
    	public static bool isPaintTask = false;
    
    	public static bool isPaintTeam = false;
    
    	public static bool isPaintFindTeam = false;
    
    	public static bool isPaintFriend = false;
    
    	public static bool isPaintEnemies = false;
    
    	public static bool isPaintItemInfo = false;
    
    	public static bool isHaveSelectSkill = false;
    
    	public static bool isPaintSkill = false;
    
    	public static bool isPaintInfoMe = false;
    
    	public static bool isPaintStore = false;
    
    	public static bool isPaintNonNam = false;
    
    	public static bool isPaintNonNu = false;
    
    	public static bool isPaintAoNam = false;
    
    	public static bool isPaintAoNu = false;
    
    	public static bool isPaintGangTayNam = false;
    
    	public static bool isPaintGangTayNu = false;
    
    	public static bool isPaintQuanNam = false;
    
    	public static bool isPaintQuanNu = false;
    
    	public static bool isPaintGiayNam = false;
    
    	public static bool isPaintGiayNu = false;
    
    	public static bool isPaintLien = false;
    
    	public static bool isPaintNhan = false;
    
    	public static bool isPaintNgocBoi = false;
    
    	public static bool isPaintPhu = false;
    
    	public static bool isPaintWeapon = false;
    
    	public static bool isPaintStack = false;
    
    	public static bool isPaintStackLock = false;
    
    	public static bool isPaintGrocery = false;
    
    	public static bool isPaintGroceryLock = false;
    
    	public static bool isPaintUpGrade = false;
    
    	public static bool isPaintConvert = false;
    
    	public static bool isPaintUpGradeGold = false;
    
    	public static bool isPaintUpPearl = false;
    
    	public static bool isPaintBox = false;
    
    	public static bool isPaintSplit = false;
    
    	public static bool isPaintCharInMap = false;
    
    	public static bool isPaintTrade = false;
    
    	public static bool isPaintZone = false;
    
    	public static bool isPaintMessage = false;
    
    	public static bool isPaintClan = false;
    
    	public static bool isRequestMember = false;
    
    	public static Char currentCharViewInfo;
    
    	public static long[] exps;
    
    	public static int[] crystals;
    
    	public static int[] upClothe;
    
    	public static int[] upAdorn;
    
    	public static int[] upWeapon;
    
    	public static int[] coinUpCrystals;
    
    	public static int[] coinUpClothes;
    
    	public static int[] coinUpAdorns;
    
    	public static int[] coinUpWeapons;
    
    	public static int[] maxPercents;
    
    	public static int[] goldUps;
    
    	public int tMenuDelay;
    
    	public int zoneCol = 6;
    
    	public int[] zones;
    
    	public int[] pts;
    
    	public int[] numPlayer;
    
    	public int[] maxPlayer;
    
    	public int[] rank1;
    
    	public int[] rank2;
    
    	public string[] rankName1;
    
    	public string[] rankName2;
    
    	public int typeTrade;
    
    	public int typeTradeOrder;
    
    	public int coinTrade;
    
    	public int coinTradeOrder;
    
    	public int timeTrade;
    
    	public int indexItemUse = -1;
    
    	public int cLastFocusID = -1;
    
    	public int cPreFocusID = -1;
    
    	public bool isLockKey;
    
    	public static int[] tasks;
    
    	public static int[] mapTasks;
    
    	public static Image imgRoomStat;
    
    	public static Image frBarPow0;
    
    	public static Image frBarPow1;
    
    	public static Image frBarPow2;
    
    	public static Image frBarPow20;
    
    	public static Image frBarPow21;
    
    	public static Image frBarPow22;
    
    	public MyVector texts;
    
    	public string textsTitle;
    
    	public static sbyte vcData;
    
    	public static sbyte vcMap;
    
    	public static sbyte vcSkill;
    
    	public static sbyte vcItem;
    
    	public static sbyte vsData;
    
    	public static sbyte vsMap;
    
    	public static sbyte vsSkill;
    
    	public static sbyte vsItem;
    
    	public static sbyte vcTask;
    
    	public static Image imgArrow;
    
    	public static Image imgArrow2;
    
    	public static Image imgChat;
    
    	public static Image imgChat2;
    
    	public static Image imgMenu;
    
    	public static Image imgFocus;
    
    	public static Image imgFocus2;
    
    	public static Image imgSkill;
    
    	public static Image imgSkill2;
    
    	public static Image imgFire0;
    
    	public static Image imgFire1;
    
    	public static Image imgNR1;
    
    	public static Image imgNR2;
    
    	public static Image imgNR3;
    
    	public static Image imgNR4;
    
    	public static Image imgLbtn;
    
    	public static Image imgLbtnFocus;
    
    	public static Image imgLbtn2;
    
    	public static Image imgLbtnFocus2;
    
    	public static Image imgAnalog1;
    
    	public static Image imgAnalog2;
    
    	public string tradeName = string.Empty;
    
    	public string tradeItemName = string.Empty;
    
    	public int timeLengthMap;
    
    	public int timeStartMap;
    
    	public static sbyte typeViewInfo = 0;
    
    	public static sbyte typeActive = 0;
    
    	public static InfoMe info1 = new InfoMe();
    
    	public static InfoMe info2 = new InfoMe();
    
    	public static Image imgPanel;
    
    	public static Image imgPanel2;
    
    	public static Image imgHP;
    
    	public static Image imgMP;
    
    	public static Image imgSP;
    
    	public static Image imgHPLost;
    
    	public static Image imgMPLost;
    
    	public static Image imgHP_tm_do;
    
    	public static Image imgHP_tm_vang;
    
    	public static Image imgHP_tm_xam;
    
    	public static Image imgHP_tm_xanh;
    
    	public Mob mobCapcha;
    
    	public MagicTree magicTree;
    
    	private short l;
    
    	public static int countEff;
    
    	public static GamePad gamePad = new GamePad();
    
    	public static Image imgChatPC;
    
    	public static Image imgChatsPC2;
    
    	public static int isAnalog = 0;
    
    	public static Image img_ct_bar_0 = mSystem.loadImage("/mainImage/i_pve_bar_0.png");
    
    	public static Image img_ct_bar_1 = mSystem.loadImage("/mainImage/i_pve_bar_1.png");
    
    	public static bool isUseTouch;
    
    	public Command cmdDoiCo;
    
    	public Command cmdLogOut;
    
    	public Command cmdChatTheGioi;
    
    	public Command cmdshowInfo;
    
    	private static Command[] cmdTestLogin = null;
    
    	public const int numSkill = 10;
    
    	public const int numSkill_2 = 5;
    
    	public static Skill[] keySkill = new Skill[10];
    
    	public static Skill[] onScreenSkill = new Skill[10];
    
    	public Command cmdMenu;
    
    	public static int firstY;
    
    	public static int wSkill;
    
    	public static long deltaTime;
    
    	public bool isPointerDowning;
    
    	public bool isChangingCameraMode;
    
    	private int ptLastDownX;
    
    	private int ptLastDownY;
    
    	private int ptFirstDownX;
    
    	private int ptFirstDownY;
    
    	private int ptDownTime;
    
    	private bool disableSingleClick;
    
    	public long lastSingleClick;
    
    	public bool clickMoving;
    
    	public bool clickOnTileTop;
    
    	public bool clickMovingRed;
    
    	private int clickToX;
    
    	private int clickToY;
    
    	private int lastClickCMX;
    
    	private int lastClickCMY;
    
    	private int clickMovingP1;
    
    	private int clickMovingTimeOut;
    
    	private long lastMove;
    
    	public static bool isNewClanMessage;
    
    	private long lastFire;
    
    	private long lastUsePotion;
    
    	public int auto;
    
    	public int dem;
    
    	private string strTam = string.Empty;
    
    	private int a;
    
    	public bool isFreez;
    
    	public bool isUseFreez;
    
    	public static Image imgTrans;
    
    	public bool isRongThanXuatHien;
    
    	public bool isRongNamek;
    
    	public bool isSuperPower;
    
    	public int tPower;
    
    	public int xPower;
    
    	public int yPower;
    
    	public int dxPower;
    
    	public bool activeRongThan;
    
    	public bool isMeCallRongThan;
    
    	public int mautroi;
    
    	public int mapRID;
    
    	public int zoneRID;
    
    	public int bgRID = -1;
    
    	public static int tam = 0;
    
    	public static bool isAutoPlay;
    
    	public static bool canAutoPlay;
    
    	public static bool isChangeZone;
    
    	private int timeSkill;
    
    	private int nSkill;
    
    	private int selectedIndexSkill = -1;
    
    	private Skill lastSkill;
    
    	private bool doSeleckSkillFlag;
    
    	public string strCapcha;
    
    	private long longPress;
    
    	private int move;
    
    	public bool flareFindFocus;
    
    	private int flareTime;
    
    	public int keyTouchSkill = -1;
    
    	private long lastSendUpdatePostion;
    
    	public static long lastTick;
    
    	public static long currTick;
    
    	private int timeAuto;
    
    	public static long lastXS;
    
    	public static long currXS;
    
    	public static int secondXS;
    
    	public int runArrow;
    
    	public static int isPaintRada;
    
    	public static Image imgNut;
    
    	public static Image imgNutF;
    
    	public int[] keyCapcha;
    
    	public static Image imgCapcha;
    
    	public string keyInput;
    
    	public static int disXC;
    
    	public static bool isPaint = true;
    
    	public static int shock_scr;
    
    	private static int[] shock_x = new int[4] { 1, -1, 1, -1 };
    
    	private static int[] shock_y = new int[4] { 1, -1, -1, 1 };
    
    	private int tDoubleDelay;
    
    	public static Image arrow;
    
    	private static int yTouchBar;
    
    	/// <summary>Bề rộng và bề cao ô chat — bằng đúng ba ô Cờ / Khu / Tab.</summary>
    	private const int W_CHAT = 42;

    	private const int H_CHAT = 26;

    	private static int xC;
    
    	private static int yC;
    
    	private static int xL;
    
    	private static int yL;
    
    	public int xR;
    
    	public int yR;
    
    	private static int xU;
    
    	private static int yU;
    
    	private static int xF;
    
    	private static int yF;
    
    	public static int xHP;
    
    	public static int yHP;
    
        public static int xTG;
    
        public static int yTG;
    
    	public static int[] xS;
    
    	public static int[] yS;
    
    	public static int xSkill;
    
    	public static int ySkill;
    
    	public static int padSkill;
    
    	public int dMP;
    
    	public int twMp;
    
    	public bool isInjureMp;
    
    	public double dHP;
    
    	public int twHp;
    
    	public bool isInjureHp;
    
    	private long curr;
    
    	private long last;
    
    	private int secondVS;
    
    	private int[] idVS = new int[2] { -1, -1 };
    
    	public static string[] flyTextString;
    
    	public static int[] flyTextX;
    
    	public static int[] flyTextY;
    
    	public static int[] flyTextYTo;
    
    	public static int[] flyTextDx;
    
    	public static int[] flyTextDy;
    
    	public static int[] flyTextState;
    
    	public static int[] flyTextColor;
    
    	public static int[] flyTime;
    
    	public static int[] splashX;
    
    	public static int[] splashY;
    
    	public static int[] splashState;
    
    	public static int[] splashF;
    
    	public static int[] splashDir;
    
    	public static Image[] imgSplash;
    
    	public static int cmdBarX;
    
    	public static int cmdBarY;
    
    	public static int cmdBarW;
    
    	public static int cmdBarLeftW;
    
    	public static int cmdBarRightW;
    
    	public static int cmdBarCenterW;
    
    	public static int hpBarX;
    
    	public static int hpBarY;
    
    	public static int spBarW;
    
    	public static int mpBarW;
    
    	public static int expBarW;
    
    	public static int lvPosX;
    
    	public static int moneyPosX;
    
    	public static int hpBarH;
    
    	public static int girlHPBarY;
    
    	public static long hpBarW;
    
    	public static Image[] imgCmdBar;
    
    	private int imgScrW;
    
    	public static int popupY;
    
    	public static int popupX;
    
    	public static int isborderIndex;
    
    	public static int isselectedRow;
    
    	private static Image imgNolearn;
    
    	public int cmxp;
    
    	public int cmvxp;
    
    	public int cmdxp;
    
    	public int cmxLimp;
    
    	public int cmyLimp;
    
    	public int cmyp;
    
    	public int cmvyp;
    
    	public int cmdyp;
    
    	private int indexTiemNang;
    
    	private string alertURL;
    
    	private string fnick;
    
    	public static int xstart;
    
    	public static int ystart;
    
    	public static int popupW = 140;
    
    	public static int popupH = 160;
    
    	public static int cmySK;
    
    	public static int cmtoYSK;
    
    	public static int cmdySK;
    
    	public static int cmvySK;
    
    	public static int cmyLimSK;
    
    	public static int columns = 6;
    
    	public static int rows;
    
    	private int totalRowInfo;
    
    	private int ypaintKill;
    
    	private int ylimUp;
    
    	private int ylimDow;
    
    	private int yPaint;
    
    	public static int indexEff = 0;
    
    	public static EffectCharPaint effUpok;
    
    	public static int inforX;
    
    	public static int inforY;
    
    	public static int inforW;
    
    	public static int inforH;
    
    	public Command cmdDead;
    
    	public static bool notPaint = false;
    
    	public static bool isPing = false;
    
    	public static int INFO = 0;
    
    	public static int STORE = 1;
    
    	public static int ZONE = 2;
    
    	public static int UPGRADE = 3;
    
    	private int Hitem = 30;
    
    	private int maxSizeRow = 5;
    
    	private int isTranKyNang;
    
    	private bool isTran;
    
    	private int cmY_Old;
    
    	private int cmX_Old;
    
    	public PopUpYesNo popUpYesNo;
    
    	public static MyVector vChatVip = new MyVector();
    
    	public static int vBig;
    
    	public bool isFireWorks;
    
    	public int[] winnumber;
    
    	public int[] randomNumber;
    
    	public int[] tMove;
    
    	public int[] moveCount;
    
    	public int[] delayMove;
    
    	public int moveIndex;
    
    	private bool isWin;
    
    	private string strFinish;
    
    	private int tShow;
    
    	private int xChatVip;
    
    	private int currChatWidth;
    
    	private bool startChat;
    
    	public sbyte percentMabu;
    
    	public bool mabuEff;
    
    	public int tMabuEff;
    
    	public static bool isPaintChatVip;
    
    	public static sbyte mabuPercent;
    
    	public static sbyte isNewMember;
    
    	private string yourNumber = string.Empty;
    
    	private string[] strPaint;
    
    	public static Image imgHP_NEW;
    
    	public static InfoPhuBan phuban_Info;
    
    	public static FrameImage fra_PVE_Bar_0;
    
    	public static FrameImage fra_PVE_Bar_1;
    
    	public static Image imgVS;
    
    	public static Image imgBall;
    
    	public static Image imgKhung;
    
    	public int countFrameSkill;
    
    	public static Image imgBgIOS;
    
    	public static int nCT_TeamB = 50;
    
    	public static int nCT_TeamA = 50;
    
    	public static long nCT_timeBallte;
    
    	public static string nCT_team;
    
    	public static int nCT_nBoyBaller = 100;
    
    	public static bool isPaint_CT;
    
    	public static sbyte nCT_floor;
    
    	public static bool is_Paint_boardCT_Expand;
    
    	private static int xRect;
    
    	private static int yRect;
    
    	private static int wRect;
    
    	private static int hRect;
    
    	public static MyVector res_CT = new MyVector();
    
    	public static int nTop = 1;
    
    	public static bool isPickNgocRong = false;
    
    	public static int nUSER_CT;
    
    	public static int nUSER_MAX_CT;
    
    	public static bool isudungCapsun;
    
    	public static bool isudungCapsun4;
    
    	public static bool isudungCapsun3;
    
    	public GameScr()
    	{
    		if (GameCanvas.w == 128 || GameCanvas.h <= 208)
    		{
    			indexSize = 20;
    		}
    		cmdback = new Command(string.Empty, 11021);
    		cmdMenu = new Command("menu", 11000);
    		cmdFocus = new Command(string.Empty, 11001);
    		cmdMenu.img = imgMenu;
    		cmdMenu.w = mGraphics.getImageWidth(cmdMenu.img) + 20;
    		cmdMenu.isPlaySoundButton = false;
    		cmdFocus.img = imgFocus;
    		if (GameCanvas.isTouch)
    		{
    			cmdMenu.x = 0;
    			cmdMenu.y = 50;
    			cmdFocus = null;
    		}
    		else
    		{
    			cmdMenu.x = 0;
    			cmdMenu.y = gH - 30;
    			cmdFocus.x = gW - 32;
    			cmdFocus.y = gH - 32;
    		}
    		right = cmdFocus;
    		isPaintRada = 1;
    		if (GameCanvas.isTouch)
    		{
    			isHaveSelectSkill = true;
    		}
    		cmdDoiCo = new Command("Đổi cờ", GameCanvas.gI(), 100001, null);
    		cmdLogOut = new Command("Logout", GameCanvas.gI(), 100002, null);
    		cmdChatTheGioi = new Command("chat world", GameCanvas.gI(), 100003, null);
    		cmdshowInfo = new Command("InfoLog", GameCanvas.gI(), 100004, null);
    		cmdDoiCo.setType();
    		cmdLogOut.setType();
    		cmdChatTheGioi.setType();
    		cmdshowInfo.setType();
    		cmdChatTheGioi.x = GameCanvas.w - cmdChatTheGioi.w;
    		cmdshowInfo.x = GameCanvas.w - cmdshowInfo.w;
    		cmdLogOut.x = GameCanvas.w - cmdLogOut.w;
    		cmdDoiCo.x = GameCanvas.w - cmdDoiCo.w;
    		cmdChatTheGioi.y = cmdChatTheGioi.h + mFont.tahoma_7_white.getHeight();
    		cmdshowInfo.y = cmdChatTheGioi.h * 2 + mFont.tahoma_7_white.getHeight();
    		cmdLogOut.y = cmdChatTheGioi.h * 3 + mFont.tahoma_7_white.getHeight();
    		cmdDoiCo.y = cmdChatTheGioi.h * 4 + mFont.tahoma_7_white.getHeight();
    	}
    
    	public static void loadBg()
        {
            doikhu = GameCanvas.loadImage("/mainImage/doikhu.png");
            arrow4 = GameCanvas.loadImage("/mainImage/myTexture2darrow4.png");
            arrow5 = GameCanvas.loadImage("/mainImage/myTexture2darrow5.png");
            fra_PVE_Bar_0 = new FrameImage(mSystem.loadImage("/mainImage/i_pve_bar_0.png"), 6, 15);
    		fra_PVE_Bar_1 = new FrameImage(mSystem.loadImage("/mainImage/i_pve_bar_1.png"), 38, 21);
    		imgVS = mSystem.loadImage("/mainImage/i_vs.png");
    		imgBall = mSystem.loadImage("/mainImage/i_charlife.png");
    		imgHP_NEW = mSystem.loadImage("/mainImage/i_hp.png");
    		imgKhung = mSystem.loadImage("/mainImage/i_khung.png");
    		imgLbtn = GameCanvas.loadImage("/mainImage/myTexture2dbtnl.png");
    		imgLbtnFocus = GameCanvas.loadImage("/mainImage/myTexture2dbtnlf.png");
    		imgLbtn2 = GameCanvas.loadImage("/mainImage/myTexture2dbtnl2.png");
    		imgLbtnFocus2 = GameCanvas.loadImage("/mainImage/myTexture2dbtnlf2.png");
    		imgPanel = GameCanvas.loadImage("/mainImage/myTexture2dpanel.png");
    		imgPanel2 = GameCanvas.loadImage("/mainImage/panel2.png");
    		imgHP = GameCanvas.loadImage("/mainImage/myTexture2dHP.png");
    		imgSP = GameCanvas.loadImage("/mainImage/SP.png");
    		imgHPLost = GameCanvas.loadImage("/mainImage/myTexture2dhpLost.png");
    		imgMPLost = GameCanvas.loadImage("/mainImage/myTexture2dmpLost.png");
    		imgMP = GameCanvas.loadImage("/mainImage/myTexture2dMP.png");
    		imgSkill = GameCanvas.loadImage("/mainImage/myTexture2dskill.png");
    		imgSkill2 = GameCanvas.loadImage("/mainImage/myTexture2dskill2.png");
    		imgMenu = GameCanvas.loadImage("/mainImage/myTexture2dmenu.png");
    		imgFocus = GameCanvas.loadImage("/mainImage/myTexture2dfocus.png");
    		imgHP_tm_do = GameCanvas.loadImage("/mainImage/tm-do.png");
    		imgHP_tm_vang = GameCanvas.loadImage("/mainImage/tm-vang.png");
    		imgHP_tm_xam = GameCanvas.loadImage("/mainImage/tm-xam.png");
    		imgHP_tm_xanh = GameCanvas.loadImage("/mainImage/tm-xanh.png");
    		imgChatPC = GameCanvas.loadImage("/pc/chat.png");
    		imgChatsPC2 = GameCanvas.loadImage("/pc/chat2.png");
    		if (GameCanvas.isTouch)
    		{
    			imgArrow = GameCanvas.loadImage("/mainImage/myTexture2darrow.png");
    			imgArrow2 = GameCanvas.loadImage("/mainImage/myTexture2darrow2.png");
    			imgChat = GameCanvas.loadImage("/mainImage/myTexture2dchat.png");
    			imgChat2 = GameCanvas.loadImage("/mainImage/myTexture2dchat2.png");
    			imgFocus2 = GameCanvas.loadImage("/mainImage/myTexture2dfocus2.png");
    			imgNutDauThan = GameCanvas.loadImage("/mainImage/nutdauthan.png");
    			imgNutCapsule = GameCanvas.loadImage("/mainImage/nutcapsule.png");
    			imgAnalog1 = GameCanvas.loadImage("/mainImage/myTexture2danalog1.png");
    			imgAnalog2 = GameCanvas.loadImage("/mainImage/myTexture2danalog2.png");
    			imgFire0 = GameCanvas.loadImage("/mainImage/myTexture2dfirebtn0.png");
    			imgFire1 = GameCanvas.loadImage("/mainImage/myTexture2dfirebtn1.png");
    		}
    		imgNR1 = GameCanvas.loadImage("/mainImage/myTexture2dPea_0.png");
    		imgNR2 = GameCanvas.loadImage("/mainImage/myTexture2dPea_1.png");
    		imgNR3 = GameCanvas.loadImage("/mainImage/myTexture2dPea_2.png");
    		imgNR4 = GameCanvas.loadImage("/mainImage/myTexture2dPea_3.png");
    		flyTextX = new int[5];
    		flyTextY = new int[5];
    		flyTextDx = new int[5];
    		flyTextDy = new int[5];
    		flyTextState = new int[5];
    		flyTextString = new string[5];
    		flyTextYTo = new int[5];
    		flyTime = new int[5];
    		flyTextColor = new int[8];
    		for (int i = 0; i < 5; i++)
    		{
    			flyTextState[i] = -1;
    		}
    		sbyte[] array = Rms.loadRMS("NRdataVersion");
    		sbyte[] array2 = Rms.loadRMS("NRmapVersion");
    		sbyte[] array3 = Rms.loadRMS("NRskillVersion");
    		sbyte[] array4 = Rms.loadRMS("NRitemVersion");
    		if (array != null)
    		{
    			vcData = array[0];
    		}
    		if (array2 != null)
    		{
    			vcMap = array2[0];
    		}
    		if (array3 != null)
    		{
    			vcSkill = array3[0];
    		}
    		if (array4 != null)
    		{
    			vcItem = array4[0];
    		}
    		imgNut = GameCanvas.loadImage("/mainImage/myTexture2dnut.png");
    		imgNutF = GameCanvas.loadImage("/mainImage/myTexture2dnutF.png");
    		MobCapcha.init();
    		isAnalog = !Main.isPC ? 1 : 0;
    		gamePad = new GamePad();
    		arrow = GameCanvas.loadImage("/mainImage/myTexture2darrow3.png");
    		imgTrans = GameCanvas.loadImage("/bg/trans.png");
    		imgRoomStat = GameCanvas.loadImage("/mainImage/myTexture2dstat.png");
    		frBarPow0 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor20.png");
    		frBarPow1 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor21.png");
    		frBarPow2 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor22.png");
    		frBarPow20 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor00.png");
    		frBarPow21 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor01.png");
    		frBarPow22 = GameCanvas.loadImage("/mainImage/myTexture2dlineColor02.png");
    	}
    
    	public void initSelectChar()
    	{
    		readPart();
    		SmallImage.init();
    	}
    
    	public static void paintOngMauPercent(Image img0, Image img1, Image img2, float x, float y, int size, float pixelPercent, mGraphics g)
    	{
    		int clipX = g.getClipX();
    		int clipY = g.getClipY();
    		int clipWidth = g.getClipWidth();
    		int clipHeight = g.getClipHeight();
    		g.setClip((int)x, (int)y, (int)pixelPercent, 13);
    		int num = size / 15 - 2;
    		for (int i = 0; i < num; i++)
    		{
    			g.drawImage(img1, x + (float)((i + 1) * 15), y, 0);
    		}
    		g.drawImage(img0, x, y, 0);
    		g.drawImage(img1, x + (float)size - 30f, y, 0);
    		g.drawImage(img2, x + (float)size - 15f, y, 0);
    		g.setClip(clipX, clipY, clipWidth, clipHeight);
    	}
    
    	public void initTraining()
    	{
    		if (CreateCharScr.isCreateChar)
    		{
    			CreateCharScr.isCreateChar = false;
    			right = null;
    		}
    	}
    
    	public bool isMapDocNhan()
    	{
    		if (TileMap.mapID >= 53 && TileMap.mapID <= 62)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isMapFize()
    	{
    		if (TileMap.mapID >= 63)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public override void switchToMe()
    	{
    		vChatVip.removeAllElements();
    		ServerListScreen.isWait = false;
    		if (BackgroudEffect.isHaveRain())
    		{
    			SoundMn.gI().rain();
    		}
    		LoginScr.isContinueToLogin = false;
    		// KHONG ha man hinh cho o day, va cung khong bao may chu ngay.
    		//
    		// Toi day dia hinh moi chi vua bat dau dung. Bao xong bay gio la may
    		// chu mo khoa va tha luot doi ban do ke tiep di, hai bo du lieu dan
    		// vao nhau — dung canh nen vo, mau 0/0. GameCanvas.update se ha man
    		// hinh cho va gui goi -39 khi ban do that su xong.
    		if (!isPaintOther)
    		{
    			GameCanvas.canGuiXongTaiBanDo = true;
    		}
    		if (TileMap.isTrainingMap())
    		{
    			initTraining();
    		}
    		info1.isUpdate = true;
    		info2.isUpdate = true;
    		resetButton();
    		isLoadAllData = true;
    		isPaintOther = false;
    		base.switchToMe();
    	}
    
    	public static int getMaxExp(int level)
    	{
    		int num = 0;
    		for (int i = 0; i <= level; i++)
    		{
    			num += (int)exps[i];
    		}
    		return num;
    	}
    
    	public static void resetAllvector()
    	{
    		vCharInMap.removeAllElements();
    		Teleport.vTeleport.removeAllElements();
    		vItemMap.removeAllElements();
    		Effect2.vEffect2.removeAllElements();
    		Effect2.vAnimateEffect.removeAllElements();
    		Effect2.vEffect2Outside.removeAllElements();
    		Effect2.vEffectFeet.removeAllElements();
    		Effect2.vEffect3.removeAllElements();
    		vMobAttack.removeAllElements();
    		vMob.removeAllElements();
    		vNpc.removeAllElements();
    		Char.myCharz().vMovePoints.removeAllElements();
    	}
    
    	public void loadSkillShortcut()
    	{
    	}
    
    	public void onOSkill(sbyte[] oSkillID)
    	{
    		Cout.println("GET onScreenSkill!");
    		onScreenSkill = new Skill[10];
    		if (oSkillID == null)
    		{
    			loadDefaultonScreenSkill();
    			return;
    		}
    		for (int i = 0; i < oSkillID.Length; i++)
    		{
    			for (int j = 0; j < Char.myCharz().vSkillFight.size(); j++)
    			{
    				Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(j);
    				if (skill.template.id == oSkillID[i])
    				{
    					onScreenSkill[i] = skill;
    					break;
    				}
    			}
    		}
    	}
    
    	public void onKSkill(sbyte[] kSkillID)
    	{
    		Cout.println("GET KEYSKILL!");
    		keySkill = new Skill[10];
    		if (kSkillID == null)
    		{
    			loadDefaultKeySkill();
    			return;
    		}
    		for (int i = 0; i < kSkillID.Length; i++)
    		{
    			for (int j = 0; j < Char.myCharz().vSkillFight.size(); j++)
    			{
    				Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(j);
    				if (skill.template.id == kSkillID[i])
    				{
    					keySkill[i] = skill;
    					break;
    				}
    			}
    		}
    	}
    
    	public void onCSkill(sbyte[] cSkillID)
    	{
    		Cout.println("GET CURRENTSKILL!");
    		if (cSkillID == null || cSkillID.Length == 0)
    		{
    			if (Char.myCharz().vSkillFight.size() > 0)
    			{
    				Char.myCharz().myskill = (Skill)Char.myCharz().vSkillFight.elementAt(0);
    			}
    		}
    		else
    		{
    			for (int i = 0; i < Char.myCharz().vSkillFight.size(); i++)
    			{
    				Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(i);
    				if (skill.template.id == cSkillID[0])
    				{
    					Char.myCharz().myskill = skill;
    					break;
    				}
    			}
    		}
    		if (Char.myCharz().myskill != null)
    		{
    			Service.gI().selectSkill(Char.myCharz().myskill.template.id);
    			saveRMSCurrentSkill(Char.myCharz().myskill.template.id);
    		}
    	}
    
    	private void loadDefaultonScreenSkill()
    	{
    		Cout.println("LOAD DEFAULT ONmScreen SKILL");
    		for (int i = 0; i < onScreenSkill.Length && i < Char.myCharz().vSkillFight.size(); i++)
    		{
    			Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(i);
    			onScreenSkill[i] = skill;
    		}
    		saveonScreenSkillToRMS();
    	}
    
    	private void loadDefaultKeySkill()
    	{
    		Cout.println("LOAD DEFAULT KEY SKILL");
    		for (int i = 0; i < keySkill.Length && i < Char.myCharz().vSkillFight.size(); i++)
    		{
    			Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(i);
    			keySkill[i] = skill;
    		}
    		saveKeySkillToRMS();
    	}
    
    	public void doSetOnScreenSkill(SkillTemplate skillTemplate)
    	{
    		Skill skill = Char.myCharz().getSkill(skillTemplate);
    		MyVector myVector = new MyVector();
    		for (int i = 0; i < 10; i++)
    		{
    			Command command = new Command(p: new object[2]
    			{
    				skill,
    				i + string.Empty
    			}, caption: mResources.into_place + (i + 1), action: 11120);
    			Skill skill2 = onScreenSkill[i];
    			if (skill2 != null)
    			{
    				command.isDisplay = true;
    			}
    			myVector.addElement(command);
    		}
    		GameCanvas.menu.startAt(myVector, 0);
    	}
    
    	public void doSetKeySkill(SkillTemplate skillTemplate)
    	{
    		Cout.println("DO SET KEY SKILL");
    		Skill skill = Char.myCharz().getSkill(skillTemplate);
    		string[] array = ((!TField.isQwerty) ? mResources.key_skill : mResources.key_skill_qwerty);
    		MyVector myVector = new MyVector();
    		for (int i = 0; i < 10; i++)
    		{
    			myVector.addElement(new Command(p: new object[2]
    			{
    				skill,
    				i + string.Empty
    			}, caption: array[i], action: 11121));
    		}
    		GameCanvas.menu.startAt(myVector, 0);
    	}
    
    	public void saveonScreenSkillToRMS()
    	{
    		sbyte[] array = new sbyte[onScreenSkill.Length];
    		for (int i = 0; i < onScreenSkill.Length; i++)
    		{
    			if (onScreenSkill[i] == null)
    			{
    				array[i] = -1;
    			}
    			else
    			{
    				array[i] = onScreenSkill[i].template.id;
    			}
    		}
    		Service.gI().changeOnKeyScr(array);
    	}
    
    	public void saveKeySkillToRMS()
    	{
    		sbyte[] array = new sbyte[keySkill.Length];
    		for (int i = 0; i < keySkill.Length; i++)
    		{
    			if (keySkill[i] == null)
    			{
    				array[i] = -1;
    			}
    			else
    			{
    				array[i] = keySkill[i].template.id;
    			}
    		}
    		Service.gI().changeOnKeyScr(array);
    	}
    
    	public void saveRMSCurrentSkill(sbyte id)
    	{
    	}
    
    	public void addSkillShortcut(Skill skill)
    	{
    		Cout.println("ADD SKILL SHORTCUT TO SKILL " + skill.template.id);
    		for (int i = 0; i < onScreenSkill.Length; i++)
    		{
    			if (onScreenSkill[i] == null)
    			{
    				onScreenSkill[i] = skill;
    				break;
    			}
    		}
    		for (int j = 0; j < keySkill.Length; j++)
    		{
    			if (keySkill[j] == null)
    			{
    				keySkill[j] = skill;
    				break;
    			}
    		}
    		if (Char.myCharz().myskill == null)
    		{
    			Char.myCharz().myskill = skill;
    		}
    		saveKeySkillToRMS();
    		saveonScreenSkillToRMS();
    	}
    
    	public bool isBagFull()
    	{
    		for (int num = Char.myCharz().arrItemBag.Length - 1; num >= 0; num--)
    		{
    			if (Char.myCharz().arrItemBag[num] == null)
    			{
    				return false;
    			}
    		}
    		return true;
    	}
    
    	public void createConfirm(string[] menu, Npc npc)
    	{
    		resetButton();
    		isLockKey = true;
    		left = new Command(menu[0], 130011, npc);
    		right = new Command(menu[1], 130012, npc);
    	}
    
    	/// <summary>
    	/// Dung menu cua NPC tu danh sach may chu gui xuong.
    	/// </summary>
    	/// <remarks>
    	/// <para><b>Phai don menu cu truoc.</b> <c>Menu.startAt</c> mo dau bang
    	/// <c>if (showMenu) return;</c> — con menu cu dang mo thi no LANG LE bo qua
    	/// menu moi, khong bao loi, khong ghi log gi. Nguoi choi thay dung hien
    	/// tuong "lan dau bam thi hien, tu lan hai tro di khong hien nua".</para>
    	///
    	/// <para>Menu cu bi ket o trang thai mo vi nhieu duong: cap nhat cua no nam
    	/// trong mot chuoi else-if trong <c>GameCanvas</c>, ChatPopup hay hop thoai
    	/// dang mo la nhanh cua menu khong chay, nen no khong tu dong lai duoc.</para>
    	///
    	/// <para>Dat lai co thay vi goi <c>doCloseMenu()</c>: ham do con don
    	/// ChatPopup va bang, ma goi tin nay vua moi dat ChatPopup moi — don di la
    	/// mat luon loi thoai NPC.</para>
    	/// </remarks>
    	public void createMenu(string[] menu, Npc npc)
    	{
    		GameCanvas.menu.showMenu = false;
    		MyVector myVector = new MyVector();
    		for (int i = 0; i < menu.Length; i++)
    		{
    			myVector.addElement(new Command(menu[i], 11057, npc));
    		}
    		GameCanvas.menu.startAt(myVector, 2);
    	}
    
    	public void readPart()
    	{
    		DataInputStream dataInputStream = null;
    		try
    		{
    			dataInputStream = new DataInputStream(Rms.loadRMS("NR_part"));
    			int num = dataInputStream.readShort();
    			parts = new Part[num];
    			for (int i = 0; i < num; i++)
    			{
    				int type = dataInputStream.readByte();
    				parts[i] = new Part(type);
    				for (int j = 0; j < parts[i].pi.Length; j++)
    				{
    					parts[i].pi[j] = new PartImage();
    					parts[i].pi[j].id = dataInputStream.readShort();
    					parts[i].pi[j].dx = dataInputStream.readByte();
    					parts[i].pi[j].dy = dataInputStream.readByte();
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("LOI TAI readPart " + ex.ToString());
    		}
    		finally
    		{
    			try
    			{
    				dataInputStream.close();
    			}
    			catch (Exception ex2)
    			{
    				Res.outz2("LOI TAI readPart 2" + ex2.StackTrace);
    			}
    		}
    	}
    
    	public void readEfect()
    	{
    		DataInputStream dataInputStream = null;
    		try
    		{
    			dataInputStream = new DataInputStream(Rms.loadRMS("NR_effect"));
    			int num = dataInputStream.readShort();
    			efs = new EffectCharPaint[num];
    			for (int i = 0; i < num; i++)
    			{
    				efs[i] = new EffectCharPaint();
    				efs[i].idEf = dataInputStream.readShort();
    				efs[i].arrEfInfo = new EffectInfoPaint[dataInputStream.readByte()];
    				for (int j = 0; j < efs[i].arrEfInfo.Length; j++)
    				{
    					efs[i].arrEfInfo[j] = new EffectInfoPaint();
    					efs[i].arrEfInfo[j].idImg = dataInputStream.readShort();
    					efs[i].arrEfInfo[j].dx = dataInputStream.readByte();
    					efs[i].arrEfInfo[j].dy = dataInputStream.readByte();
    				}
    			}
    		}
    		catch (Exception)
    		{
    		}
    		finally
    		{
    			try
    			{
    				dataInputStream.close();
    			}
    			catch (Exception ex2)
    			{
    				Cout.LogError("Loi ham Eff: " + ex2.ToString());
    			}
    		}
    	}
    
    	public void readArrow()
    	{
    		DataInputStream dataInputStream = null;
    		try
    		{
    			dataInputStream = new DataInputStream(Rms.loadRMS("NR_arrow"));
    			int num = dataInputStream.readShort();
    			arrs = new Arrowpaint[num];
    			for (int i = 0; i < num; i++)
    			{
    				arrs[i] = new Arrowpaint();
    				arrs[i].id = dataInputStream.readShort();
    				arrs[i].imgId[0] = dataInputStream.readShort();
    				arrs[i].imgId[1] = dataInputStream.readShort();
    				arrs[i].imgId[2] = dataInputStream.readShort();
    			}
    		}
    		catch (Exception)
    		{
    		}
    		finally
    		{
    			try
    			{
    				dataInputStream.close();
    			}
    			catch (Exception ex2)
    			{
    				Cout.LogError("Loi ham readArrow: " + ex2.ToString());
    			}
    		}
    	}
    
    	public void readDart()
    	{
    		DataInputStream dataInputStream = null;
    		try
    		{
    			dataInputStream = new DataInputStream(Rms.loadRMS("NR_dart"));
    			int num = dataInputStream.readShort();
    			darts = new DartInfo[num];
    			for (int i = 0; i < num; i++)
    			{
    				darts[i] = new DartInfo();
    				darts[i].id = dataInputStream.readShort();
    				darts[i].nUpdate = dataInputStream.readShort();
    				darts[i].va = dataInputStream.readShort() * 256;
    				darts[i].xdPercent = dataInputStream.readShort();
    				int num2 = dataInputStream.readShort();
    				darts[i].tail = new short[num2];
    				for (int j = 0; j < num2; j++)
    				{
    					darts[i].tail[j] = dataInputStream.readShort();
    				}
    				num2 = dataInputStream.readShort();
    				darts[i].tailBorder = new short[num2];
    				for (int k = 0; k < num2; k++)
    				{
    					darts[i].tailBorder[k] = dataInputStream.readShort();
    				}
    				num2 = dataInputStream.readShort();
    				darts[i].xd1 = new short[num2];
    				for (int l = 0; l < num2; l++)
    				{
    					darts[i].xd1[l] = dataInputStream.readShort();
    				}
    				num2 = dataInputStream.readShort();
    				darts[i].xd2 = new short[num2];
    				for (int m = 0; m < num2; m++)
    				{
    					darts[i].xd2[m] = dataInputStream.readShort();
    				}
    				num2 = dataInputStream.readShort();
    				darts[i].head = new short[num2][];
    				for (int n = 0; n < num2; n++)
    				{
    					short num3 = dataInputStream.readShort();
    					darts[i].head[n] = new short[num3];
    					for (int num4 = 0; num4 < num3; num4++)
    					{
    						darts[i].head[n][num4] = dataInputStream.readShort();
    					}
    				}
    				num2 = dataInputStream.readShort();
    				darts[i].headBorder = new short[num2][];
    				for (int num5 = 0; num5 < num2; num5++)
    				{
    					short num6 = dataInputStream.readShort();
    					darts[i].headBorder[num5] = new short[num6];
    					for (int num7 = 0; num7 < num6; num7++)
    					{
    						darts[i].headBorder[num5][num7] = dataInputStream.readShort();
    					}
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi ham ReadDart: " + ex.ToString());
    		}
    		finally
    		{
    			try
    			{
    				dataInputStream.close();
    			}
    			catch (Exception ex2)
    			{
    				Cout.LogError("Loi ham reaaDart: " + ex2.ToString());
    			}
    		}
    	}
    
    	public void readSkill()
    	{
    		DataInputStream dataInputStream = null;
    		try
    		{
    			dataInputStream = new DataInputStream(Rms.loadRMS("NR_skill"));
    			int num = dataInputStream.readShort();
    			int num2 = Skills.skills.size();
    			sks = new SkillPaint[num2];
    			for (int i = 0; i < num; i++)
    			{
    				short num3 = dataInputStream.readShort();
    				if (num3 == 1111)
    				{
    					num3 = (short)(num - 1);
    				}
    				sks[num3] = new SkillPaint();
    				sks[num3].id = num3;
    				sks[num3].effectHappenOnMob = dataInputStream.readShort();
    				if (sks[num3].effectHappenOnMob <= 0)
    				{
    					sks[num3].effectHappenOnMob = 80;
    				}
    				sks[num3].numEff = dataInputStream.readByte();
    				sks[num3].skillStand = new SkillInfoPaint[dataInputStream.readByte()];
    				for (int j = 0; j < sks[num3].skillStand.Length; j++)
    				{
    					sks[num3].skillStand[j] = new SkillInfoPaint();
    					sks[num3].skillStand[j].status = dataInputStream.readByte();
    					sks[num3].skillStand[j].effS0Id = dataInputStream.readShort();
    					sks[num3].skillStand[j].e0dx = dataInputStream.readShort();
    					sks[num3].skillStand[j].e0dy = dataInputStream.readShort();
    					sks[num3].skillStand[j].effS1Id = dataInputStream.readShort();
    					sks[num3].skillStand[j].e1dx = dataInputStream.readShort();
    					sks[num3].skillStand[j].e1dy = dataInputStream.readShort();
    					sks[num3].skillStand[j].effS2Id = dataInputStream.readShort();
    					sks[num3].skillStand[j].e2dx = dataInputStream.readShort();
    					sks[num3].skillStand[j].e2dy = dataInputStream.readShort();
    					sks[num3].skillStand[j].arrowId = dataInputStream.readShort();
    					sks[num3].skillStand[j].adx = dataInputStream.readShort();
    					sks[num3].skillStand[j].ady = dataInputStream.readShort();
    				}
    				sks[num3].skillfly = new SkillInfoPaint[dataInputStream.readByte()];
    				for (int k = 0; k < sks[num3].skillfly.Length; k++)
    				{
    					sks[num3].skillfly[k] = new SkillInfoPaint();
    					sks[num3].skillfly[k].status = dataInputStream.readByte();
    					sks[num3].skillfly[k].effS0Id = dataInputStream.readShort();
    					sks[num3].skillfly[k].e0dx = dataInputStream.readShort();
    					sks[num3].skillfly[k].e0dy = dataInputStream.readShort();
    					sks[num3].skillfly[k].effS1Id = dataInputStream.readShort();
    					sks[num3].skillfly[k].e1dx = dataInputStream.readShort();
    					sks[num3].skillfly[k].e1dy = dataInputStream.readShort();
    					sks[num3].skillfly[k].effS2Id = dataInputStream.readShort();
    					sks[num3].skillfly[k].e2dx = dataInputStream.readShort();
    					sks[num3].skillfly[k].e2dy = dataInputStream.readShort();
    					sks[num3].skillfly[k].arrowId = dataInputStream.readShort();
    					sks[num3].skillfly[k].adx = dataInputStream.readShort();
    					sks[num3].skillfly[k].ady = dataInputStream.readShort();
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi ham readSkill: " + ex.ToString());
    		}
    		finally
    		{
    			try
    			{
    				dataInputStream.close();
    			}
    			catch (Exception ex2)
    			{
    				Cout.LogError("Loi ham readskill: " + ex2.ToString());
    			}
    		}
    	}
    
    	public static GameScr gI()
    	{
    		if (instance == null)
    		{
    			instance = new GameScr();
    		}
    		return instance;
    	}
    
    	public static void clearGameScr()
    	{
    		instance = null;
    	}
    
    	public void loadGameScr()
    	{
    		loadSplash();
    		Res.init();
    		loadInforBar();
    	}
    
    	public void doMenuInforMe()
    	{
    		scrMain.clear();
    		scrInfo.clear();
    		isViewNext = false;
    		cmdBag = new Command(mResources.MENUME[0], 1100011);
    		cmdSkill = new Command(mResources.MENUME[1], 1100012);
    		cmdTiemnang = new Command(mResources.MENUME[2], 1100013);
    		cmdInfo = new Command(mResources.MENUME[3], 1100014);
    		cmdtrangbi = new Command(mResources.MENUME[4], 1100015);
    		MyVector myVector = new MyVector();
    		myVector.addElement(cmdBag);
    		myVector.addElement(cmdSkill);
    		myVector.addElement(cmdTiemnang);
    		myVector.addElement(cmdInfo);
    		myVector.addElement(cmdtrangbi);
    		GameCanvas.menu.startAt(myVector, 3);
    	}
    
    	public void doMenusynthesis()
    	{
    		MyVector myVector = new MyVector();
    		myVector.addElement(new Command(mResources.SYNTHESIS[0], 110002));
    		myVector.addElement(new Command(mResources.SYNTHESIS[1], 1100032));
    		myVector.addElement(new Command(mResources.SYNTHESIS[2], 1100033));
    		GameCanvas.menu.startAt(myVector, 3);
    	}
    
    	public static void loadCamera(bool fullmScreen, int cx, int cy)
    	{
    		gW = GameCanvas.w;
    		cmdBarH = 39;
    		gH = GameCanvas.h;
    		cmdBarW = gW;
    		cmdBarX = 0;
    		cmdBarY = GameCanvas.h - Paint.hTab - cmdBarH;
    		girlHPBarY = 0;
    		csPadMaxH = GameCanvas.h / 6;
    		if (csPadMaxH < 48)
    		{
    			csPadMaxH = 48;
    		}
    		gW2 = gW >> 1;
    		gH2 = gH >> 1;
    		gW3 = gW / 3;
    		gH3 = gH / 3;
    		gW23 = gH - 120;
    		gH23 = gH * 2 / 3;
    		gW34 = 3 * gW / 4;
    		gH34 = 3 * gH / 4;
    		gW6 = gW / 6;
    		gH6 = gH / 6;
    		gssw = gW / TileMap.size + 2;
    		gssh = gH / TileMap.size + 2;
    		if (gW % 24 != 0)
    		{
    			gssw++;
    		}
    		cmxLim = (TileMap.tmw - 1) * TileMap.size - gW;
    		cmyLim = (TileMap.tmh - 1) * TileMap.size - gH;
    		if (cx == -1 && cy == -1)
    		{
    			cmx = (cmtoX = Char.myCharz().cx - gW2 + gW6 * Char.myCharz().cdir);
    			cmy = (cmtoY = Char.myCharz().cy - gH23);
    		}
    		else
    		{
    			cmx = (cmtoX = cx - gW23 + gW6 * Char.myCharz().cdir);
    			cmy = (cmtoY = cy - gH23);
    		}
    		firstY = cmy;
    		if (cmx < 24)
    		{
    			cmx = (cmtoX = 24);
    		}
    		if (cmx > cmxLim)
    		{
    			cmx = (cmtoX = cmxLim);
    		}
    		if (cmy < 0)
    		{
    			cmy = (cmtoY = 0);
    		}
    		if (cmy > cmyLim)
    		{
    			cmy = (cmtoY = cmyLim);
    		}
    		gssx = cmx / TileMap.size - 1;
    		if (gssx < 0)
    		{
    			gssx = 0;
    		}
    		gssy = cmy / TileMap.size;
    		gssxe = gssx + gssw;
    		gssye = gssy + gssh;
    		if (gssy < 0)
    		{
    			gssy = 0;
    		}
    		if (gssye > TileMap.tmh - 1)
    		{
    			gssye = TileMap.tmh - 1;
    		}
    		TileMap.countx = (gssxe - gssx) * 4;
    		if (TileMap.countx > TileMap.tmw)
    		{
    			TileMap.countx = TileMap.tmw;
    		}
    		TileMap.county = (gssye - gssy) * 4;
    		if (TileMap.county > TileMap.tmh)
    		{
    			TileMap.county = TileMap.tmh;
    		}
    		TileMap.gssx = (Char.myCharz().cx - 2 * gW) / TileMap.size;
    		if (TileMap.gssx < 0)
    		{
    			TileMap.gssx = 0;
    		}
    		TileMap.gssxe = TileMap.gssx + TileMap.countx;
    		if (TileMap.gssxe > TileMap.tmw)
    		{
    			TileMap.gssxe = TileMap.tmw;
    		}
    		TileMap.gssy = (Char.myCharz().cy - 2 * gH) / TileMap.size;
    		if (TileMap.gssy < 0)
    		{
    			TileMap.gssy = 0;
    		}
    		TileMap.gssye = TileMap.gssy + TileMap.county;
    		if (TileMap.gssye > TileMap.tmh)
    		{
    			TileMap.gssye = TileMap.tmh;
    		}
    		ChatTextField.gI().parentScreen = instance;
    		ChatTextField.gI().tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
    		ChatTextField.gI().initChatTextField();
    		if (GameCanvas.isTouch)
    		{
    			yTouchBar = gH - 88;
    			// Goc tren phai, hai hang:
    			//
    			//     [Co ] [Khu ] [ba gach]
    			//           [Tab ] [ chat  ]
    			//
    			// O chat nam NGAY DUOI nut ba gach, mep phai hai cai thang nhau o
    			// w-7. Nut ba gach chi rong 30 con o chat rong 42, nen o chat thua
    			// ra 12 diem ve ben trai — van khong dung vao o Tab vi giua hai cai
    			// con khe 6 diem.
    			//
    			// Ba o Co / Khu / Tab do TabControll.xepLaiCho() dat: Khu va Tab
    			// chung cot x = w-85, Co o cot trai x = w-133.
    			xC = gW - 49;
    			yC = 38;
    			xF = gW - 55;
    			yF = yTouchBar + 35;
    			xTG = gW - 37;
    			yTG = yTouchBar - 1;
    			if (GameCanvas.w >= 450)
    			{
    				yTG -= 12;
    				yHP -= 7;
    				xF -= 10;
    				yF -= 5;
    				xTG -= 10;
    			}
    		}
    		setSkillBarPosition();
    		disXC = ((GameCanvas.w <= 200) ? 30 : 40);
    		if (Rms.loadRMSInt("viewchat") == -1)
    		{
    			GameCanvas.panel.isViewChatServer = true;
    		}
    		else
    		{
    			GameCanvas.panel.isViewChatServer = Rms.loadRMSInt("viewchat") == 1;
    		}
    	}
    
    	public static void setSkillBarPosition()
    	{
    		if (!daDocThuGon)
    		{
    			daDocThuGon = true;
    			thuGonKyNang = Rms.loadRMSInt("thuGonKyNang") == 1;
    		}
    		Skill[] array = ((!GameCanvas.isTouch) ? keySkill : onScreenSkill);
    		xS = new int[array.Length];
    		yS = new int[array.Length];
    		if (GameCanvas.isTouchControlSmallScreen && isUseTouch)
    		{
    			xSkill = 23;
    			ySkill = 52;
    			padSkill = 5;
    			for (int i = 0; i < xS.Length; i++)
    			{
    				xS[i] = i * (25 + padSkill);
    				yS[i] = ySkill;
    				if (xS.Length > 5 && i >= xS.Length / 2)
    				{
    					xS[i] = (i - xS.Length / 2) * (25 + padSkill);
    					yS[i] = ySkill - 32;
    				}
    			}
    			xHP = array.Length * (25 + padSkill);
    			yHP = ySkill;
    		}
    		else
    		{
    			wSkill = 30;
    			if (GameCanvas.w <= 320)
    			{
    				ySkill = gH - wSkill - 6;
    				xSkill = gW2 - array.Length * wSkill / 2 - 25;
    			}
    			else
    			{
    				// Buoc cua hang o ky nang.
    				//
    				// Ban cu de 40 trong khi o chi rong 30 — moi o phi 10 diem,
    				// muoi o la 100 diem trong. Tren cua so 1027x638 (zoom 2,
    				// tuc 513 diem game) thi hang o chiem 78% be ngang; keo ve
    				// 32 con 62%.
    				//
    				// KHONG the ha duoi 31: anh bieu tuong ky nang la bitmap co
    				// dinh 24 diem ve bang drawRegion — ham do khong thu nho
    				// duoc, nen o hep hon la icon tho ra ngoai vien.
    				wSkill = 32;
    				int canDu = xS.Length * wSkill + 20;
    				if (canDu > GameCanvas.w)
    				{
    					wSkill = (GameCanvas.w - 20) / xS.Length;
    				}
    				if (wSkill < 31)
    				{
    					wSkill = 31;
    				}
    				xSkill = 0;
    				// Neo theo mot so co dinh chu khong theo wSkill: wSkill la
    				// BUOC ngang, dung no lam chieu cao thi doi buoc mot cai la
    				// ca hang o truot len xuong theo.
    				ySkill = GameCanvas.h - 33;
    			}
    			for (int j = 0; j < xS.Length; j++)
    			{
    				xS[j] = j * wSkill;
    				yS[j] = ySkill;
    				if (xS.Length > 5 && j >= xS.Length / 2)
    				{
    					xS[j] = (j - xS.Length / 2) * wSkill;
    					yS[j] = ySkill - 32;
    				}
    			}
    			xHP = array.Length * wSkill;
    			yHP = ySkill;
    		}
    		if (!GameCanvas.isTouch)
    		{
    			return;
    		}
    		xSkill = 17;
    		ySkill = GameCanvas.h - 40;
    		if (gamePad.isSmallGamePad && isAnalog == 1)
    		{
    			xHP = array.Length * wSkill;
    			yHP = ySkill;
    		}
    		else
    		{
    			xHP = GameCanvas.w - 45;
    			yHP = GameCanvas.h - 45;
    		}
    		setTouchBtn();
    		for (int k = 0; k < xS.Length; k++)
    		{
    			xS[k] = k * wSkill;
    			yS[k] = ySkill;
    			if (xS.Length > 5 && k >= xS.Length / 2)
    			{
    				xS[k] = (k - xS.Length / 2) * wSkill;
    				yS[k] = ySkill - 32;
    			}
    		}
    	}
    
    	private static void updateCamera()
    	{
    		if (isPaintOther)
    		{
    			return;
    		}
    		if (cmx != cmtoX || cmy != cmtoY)
    		{
    			cmvx = cmtoX - cmx << 2;
    			cmvy = cmtoY - cmy << 2;
    			cmdx += cmvx;
    			cmx += cmdx >> 4;
    			cmdx &= 15;
    			cmdy += cmvy;
    			cmy += cmdy >> 4;
    			cmdy &= 15;
    			if (cmx < 24)
    			{
    				cmx = 24;
    			}
    			if (cmx > cmxLim)
    			{
    				cmx = cmxLim;
    			}
    			if (cmy < 0)
    			{
    				cmy = 0;
    			}
    			if (cmy > cmyLim)
    			{
    				cmy = cmyLim;
    			}
    		}
    		gssx = cmx / TileMap.size - 1;
    		if (gssx < 0)
    		{
    			gssx = 0;
    		}
    		gssy = cmy / TileMap.size;
    		gssxe = gssx + gssw;
    		gssye = gssy + gssh;
    		if (gssy < 0)
    		{
    			gssy = 0;
    		}
    		if (gssye > TileMap.tmh - 1)
    		{
    			gssye = TileMap.tmh - 1;
    		}
    		TileMap.gssx = (Char.myCharz().cx - 2 * gW) / TileMap.size;
    		if (TileMap.gssx < 0)
    		{
    			TileMap.gssx = 0;
    		}
    		TileMap.gssxe = TileMap.gssx + TileMap.countx;
    		if (TileMap.gssxe > TileMap.tmw)
    		{
    			TileMap.gssxe = TileMap.tmw;
    			TileMap.gssx = TileMap.gssxe - TileMap.countx;
    		}
    		TileMap.gssy = (Char.myCharz().cy - 2 * gH) / TileMap.size;
    		if (TileMap.gssy < 0)
    		{
    			TileMap.gssy = 0;
    		}
    		TileMap.gssye = TileMap.gssy + TileMap.county;
    		if (TileMap.gssye > TileMap.tmh)
    		{
    			TileMap.gssye = TileMap.tmh;
    			TileMap.gssy = TileMap.gssye - TileMap.county;
    		}
    		scrMain.updatecm();
    		scrInfo.updatecm();
    	}
    
    	public bool testAct()
    	{
    		for (sbyte b = 2; b < 9; b += 2)
    		{
    			if (GameCanvas.keyHold[b])
    			{
    				return false;
    			}
    		}
    		return true;
    	}
    
    	public void clanInvite(string strInvite, int clanID, int code)
    	{
    		ClanObject clanObject = new ClanObject();
    		clanObject.code = code;
    		clanObject.clanID = clanID;
    		startYesNoPopUp(strInvite, new Command(mResources.YES, 12002, clanObject), new Command(mResources.NO, 12003, clanObject));
    	}
    
    	public void playerMenu(Char c)
    	{
    		auto = 0;
    		GameCanvas.clearKeyHold();
    		if (Char.myCharz().charFocus.charID < 0 || Char.myCharz().charID < 0)
    		{
    			return;
    		}
    		MyVector vPlayerMenu = GameCanvas.panel.vPlayerMenu;
    		if (vPlayerMenu.size() > 0)
    		{
    			return;
    		}
    		if (Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId > 1)
    		{
    			vPlayerMenu.addElement(new Command(mResources.make_friend, 11112, Char.myCharz().charFocus));
    			vPlayerMenu.addElement(new Command(mResources.trade, 11113, Char.myCharz().charFocus));
    		}
    		if (Char.myCharz().clan != null && Char.myCharz().role < 2 && Char.myCharz().charFocus.clanID == -1)
    		{
    			vPlayerMenu.addElement(new Command(mResources.CHAR_ORDER[4], 110391));
    		}
    		if (Char.myCharz().charFocus.statusMe != 14 && Char.myCharz().charFocus.statusMe != 5)
    		{
    			if (Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId >= 14)
    			{
    				vPlayerMenu.addElement(new Command(mResources.CHAR_ORDER[0], 2003));
    			}
    		}
    		else if (Char.myCharz().myskill.template.type != 4)
    		{
    		}
    		if (Char.myCharz().clan != null && Char.myCharz().clan.ID == Char.myCharz().charFocus.clanID && Char.myCharz().charFocus.statusMe != 14 && Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId >= 14)
    		{
    			vPlayerMenu.addElement(new Command(mResources.CHAR_ORDER[1], 2004));
    		}
    		int num = Char.myCharz().nClass.skillTemplates.Length;
    		for (int i = 0; i < num; i++)
    		{
    			SkillTemplate skillTemplate = Char.myCharz().nClass.skillTemplates[i];
    			Skill skill = Char.myCharz().getSkill(skillTemplate);
    			if (skill != null && skillTemplate.isBuffToPlayer() && skill.point >= 1)
    			{
    				vPlayerMenu.addElement(new Command(skillTemplate.name, 12004, skill));
    			}
    		}
    	}
    
    	public bool isAttack()
    	{
    		if (checkClickToBotton(Char.myCharz().charFocus))
    		{
    			return false;
    		}
    		if (checkClickToBotton(Char.myCharz().mobFocus))
    		{
    			return false;
    		}
    		if (checkClickToBotton(Char.myCharz().npcFocus))
    		{
    			return false;
    		}
    		if (ChatTextField.gI().isShow)
    		{
    			return false;
    		}
    		if (InfoDlg.isLock || Char.myCharz().isLockAttack || Char.isLockKey)
    		{
    			return false;
    		}
    		if (Char.myCharz().myskill != null && Char.myCharz().myskill.template.id == 6 && Char.myCharz().itemFocus != null)
    		{
    			pickItem();
    			return false;
    		}
    		if (Char.myCharz().myskill != null && Char.myCharz().myskill.template.type == 2 && Char.myCharz().npcFocus == null && Char.myCharz().myskill.template.id != 6)
    		{
    			if (!checkSkillValid())
    			{
    				return false;
    			}
    			return true;
    		}
    		if (Char.myCharz().skillPaint != null || (Char.myCharz().mobFocus == null && Char.myCharz().npcFocus == null && Char.myCharz().charFocus == null && Char.myCharz().itemFocus == null))
    		{
    			return false;
    		}
    		if (Char.myCharz().mobFocus != null)
    		{
    			if (Char.myCharz().mobFocus.isBigBoss() && Char.myCharz().mobFocus.status == 4)
    			{
    				Char.myCharz().mobFocus = null;
    				Char.myCharz().currentMovePoint = null;
    			}
    			isAutoPlay = true;
    			if (!isMeCanAttackMob(Char.myCharz().mobFocus))
    			{
    				Res.outz("can not attack");
    				return false;
    			}
    			if (mobCapcha != null)
    			{
    				return false;
    			}
    			if (Char.myCharz().myskill == null)
    			{
    				return false;
    			}
    			if (Char.myCharz().isSelectingSkillUseAlone())
    			{
    				return false;
    			}
    			int num = -1;
    			int num2 = Res.abs(Char.myCharz().cx - cmx) * mGraphics.zoomLevel;
    			if (Char.myCharz().charFocus != null)
    			{
    				num = Res.abs(Char.myCharz().cx - Char.myCharz().charFocus.cx) * mGraphics.zoomLevel;
    			}
    			else if (Char.myCharz().mobFocus != null)
    			{
    				num = Res.abs(Char.myCharz().cx - Char.myCharz().mobFocus.x) * mGraphics.zoomLevel;
    			}
    			if ((Char.myCharz().mobFocus.status == 1 || Char.myCharz().mobFocus.status == 0 || Char.myCharz().myskill.template.type == 4 || num == -1 || num > num2) && Char.myCharz().myskill.template.type == 4)
    			{
    				if (Char.myCharz().mobFocus.x < Char.myCharz().cx)
    				{
    					Char.myCharz().cdir = -1;
    				}
    				else
    				{
    					Char.myCharz().cdir = 1;
    				}
    				doSelectSkill(Char.myCharz().myskill, true);
    			}
    			if (!checkSkillValid())
    			{
    				return false;
    			}
    			if (Char.myCharz().cx < Char.myCharz().mobFocus.getX())
    			{
    				Char.myCharz().cdir = 1;
    			}
    			else
    			{
    				Char.myCharz().cdir = -1;
    			}
    			int num3 = Math.abs(Char.myCharz().cx - Char.myCharz().mobFocus.getX());
    			int num4 = Math.abs(Char.myCharz().cy - Char.myCharz().mobFocus.getY());
    			Char.myCharz().cvx = 0;
    			if (num3 <= Char.myCharz().myskill.dx && num4 <= Char.myCharz().myskill.dy)
    			{
    				if (Char.myCharz().myskill.template.id == 20)
    				{
    					return true;
    				}
    				if (num4 > num3 && Res.abs(Char.myCharz().cy - Char.myCharz().mobFocus.getY()) > 30 && Char.myCharz().mobFocus.getTemplate().type == 4)
    				{
    					Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().cx + Char.myCharz().cdir, Char.myCharz().mobFocus.getY());
    					Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    					GameCanvas.clearKeyHold();
    					GameCanvas.clearKeyPressed();
    					return false;
    				}
    				int num5 = 20;
    				bool flag = false;
    				if (Char.myCharz().mobFocus is BigBoss || Char.myCharz().mobFocus is BigBoss2)
    				{
    					flag = true;
    				}
    				if (Char.myCharz().myskill.dx > 100)
    				{
    					num5 = 60;
    					if (num3 < 20)
    					{
    						Char.myCharz().createShadow(Char.myCharz().cx, Char.myCharz().cy, 10);
    					}
    				}
    				bool flag2 = false;
    				if ((TileMap.tileTypeAtPixel(Char.myCharz().cx, Char.myCharz().cy + 3) & 2) == 2)
    				{
    					int num6 = ((Char.myCharz().cx > Char.myCharz().mobFocus.getX()) ? 1 : (-1));
    					if ((TileMap.tileTypeAtPixel(Char.myCharz().mobFocus.getX() + num5 * num6, Char.myCharz().cy + 3) & 2) != 2)
    					{
    						flag2 = true;
    					}
    				}
    				if (num3 <= num5 && !flag2)
    				{
    					if (Char.myCharz().cx > Char.myCharz().mobFocus.getX())
    					{
    						int num7 = Char.myCharz().mobFocus.getX() + num5 + (flag ? 30 : 0);
    						int i = Char.myCharz().mobFocus.getX();
    						bool flag3 = false;
    						for (; i < num7; i += 24)
    						{
    							if (TileMap.tileTypeAtPixel(i, Char.myCharz().cy + 3) == 8 || TileMap.tileTypeAtPixel(i, Char.myCharz().cy + 3) == 4)
    							{
    								flag3 = true;
    								break;
    							}
    						}
    						if (flag3)
    						{
    							Char.myCharz().cx = i - 24;
    						}
    						else
    						{
    							Char.myCharz().cx = num7;
    						}
    						Char.myCharz().cdir = -1;
    					}
    					else
    					{
    						int num8 = Char.myCharz().mobFocus.getX() - num5 - (flag ? 30 : 0);
    						int num9 = Char.myCharz().mobFocus.getX();
    						bool flag4 = false;
    						while (num9 > num8)
    						{
    							if (TileMap.tileTypeAtPixel(num9, Char.myCharz().cy + 3) == 8 || TileMap.tileTypeAtPixel(num9, Char.myCharz().cy + 3) == 4)
    							{
    								flag4 = true;
    								break;
    							}
    							num9 -= 24;
    						}
    						if (flag4)
    						{
    							Char.myCharz().cx = num9 + 24;
    						}
    						else
    						{
    							Char.myCharz().cx = num8;
    						}
    						Char.myCharz().cdir = 1;
    					}
    					Service.gI().charMove();
    				}
    				GameCanvas.clearKeyHold();
    				GameCanvas.clearKeyPressed();
    				return true;
    			}
    			bool flag5 = false;
    			if (Char.myCharz().mobFocus is BigBoss || Char.myCharz().mobFocus is BigBoss2)
    			{
    				flag5 = true;
    			}
    			int num10 = (Char.myCharz().myskill.dx - ((!flag5) ? 20 : 50)) * ((Char.myCharz().cx > Char.myCharz().mobFocus.getX()) ? 1 : (-1));
    			if (num3 <= Char.myCharz().myskill.dx)
    			{
    				num10 = 0;
    			}
    			Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().mobFocus.getX() + num10, Char.myCharz().mobFocus.getY());
    			Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    			return false;
    		}
    		if (Char.myCharz().npcFocus != null)
    		{
    			if (Char.myCharz().npcFocus.isHide)
    			{
    				return false;
    			}
    			if (Char.myCharz().cx < Char.myCharz().npcFocus.cx)
    			{
    				Char.myCharz().cdir = 1;
    			}
    			else
    			{
    				Char.myCharz().cdir = -1;
    			}
    			if (Char.myCharz().cx < Char.myCharz().npcFocus.cx)
    			{
    				Char.myCharz().npcFocus.cdir = -1;
    			}
    			else
    			{
    				Char.myCharz().npcFocus.cdir = 1;
    			}
    			int num11 = Math.abs(Char.myCharz().cx - Char.myCharz().npcFocus.cx);
    			int num12 = Math.abs(Char.myCharz().cy - Char.myCharz().npcFocus.cy);
    			if (num12 > 40)
    			{
    				Char.myCharz().cy = Char.myCharz().npcFocus.cy - 40;
    			}
    			if (num11 < 60)
    			{
    				GameCanvas.clearKeyHold();
    				GameCanvas.clearKeyPressed();
    				if (tMenuDelay == 0)
    				{
    					if (Char.myCharz().taskMaint != null && Char.myCharz().taskMaint.taskId == 0)
    					{
    						if (Char.myCharz().taskMaint.index < 4 && Char.myCharz().npcFocus.template.npcTemplateId == 4)
    						{
    							return false;
    						}
    						if (Char.myCharz().taskMaint.index < 3 && Char.myCharz().npcFocus.template.npcTemplateId == 3)
    						{
    							return false;
    						}
    					}
    					tMenuDelay = 50;
    					InfoDlg.showWait();
    					Service.gI().charMove();
    					God.NhatKy.ghi("NPC xin mo bang, npc = "
    							+ Char.myCharz().npcFocus.template.npcTemplateId);
    					Service.gI().openMenu(Char.myCharz().npcFocus.template.npcTemplateId);
    					// An Enter mo bang NPC thi phai NUOT luon cu bam do.
    					//
    					// keyPressed khong tu tat sau moi khung hinh, phai co ai do
    					// goi clearKeyPressed. Nhanh nay quen goi, nen co Enter con
    					// bat nguyen; bang NPC vua hien ra la Menu doc ngay co do va
    					// tu chon muc dau — nhin ra thanh "mot cu Enter an hai lan".
    					//
    					// Nhanh di bo toi cho NPC ngay ben duoi da xoa dung nhu vay
    					// tu lau; day chi la lam not cho con thieu.
    					GameCanvas.clearKeyHold();
    					GameCanvas.clearKeyPressed();
    				}
    			}
    			else
    			{
    				int num13 = (20 + Res.r.nextInt(20)) * ((Char.myCharz().cx > Char.myCharz().npcFocus.cx) ? 1 : (-1));
    				Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().npcFocus.cx + num13, Char.myCharz().cy);
    				Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    				GameCanvas.clearKeyHold();
    				GameCanvas.clearKeyPressed();
    			}
    			return false;
    		}
    		if (Char.myCharz().charFocus != null)
    		{
    			if (mobCapcha != null)
    			{
    				return false;
    			}
    			if (Char.myCharz().cx < Char.myCharz().charFocus.cx)
    			{
    				Char.myCharz().cdir = 1;
    			}
    			else
    			{
    				Char.myCharz().cdir = -1;
    			}
    			int num14 = Math.abs(Char.myCharz().cx - Char.myCharz().charFocus.cx);
    			int num15 = Math.abs(Char.myCharz().cy - Char.myCharz().charFocus.cy);
    			if (Char.myCharz().isMeCanAttackOtherPlayer(Char.myCharz().charFocus) || Char.myCharz().isSelectingSkillBuffToPlayer())
    			{
    				if (Char.myCharz().myskill == null)
    				{
    					return false;
    				}
    				if (!checkSkillValid())
    				{
    					return false;
    				}
    				if (Char.myCharz().cx < Char.myCharz().charFocus.cx)
    				{
    					Char.myCharz().cdir = 1;
    				}
    				else
    				{
    					Char.myCharz().cdir = -1;
    				}
    				Char.myCharz().cvx = 0;
    				if (num14 <= Char.myCharz().myskill.dx && num15 <= Char.myCharz().myskill.dy)
    				{
    					if (Char.myCharz().myskill.template.id == 20)
    					{
    						return true;
    					}
    					int num16 = 20;
    					if (Char.myCharz().myskill.dx > 60)
    					{
    						num16 = 60;
    						if (num14 < 20)
    						{
    							Char.myCharz().createShadow(Char.myCharz().cx, Char.myCharz().cy, 10);
    						}
    					}
    					bool flag6 = false;
    					if ((TileMap.tileTypeAtPixel(Char.myCharz().cx, Char.myCharz().cy + 3) & 2) == 2)
    					{
    						int num17 = ((Char.myCharz().cx > Char.myCharz().charFocus.cx) ? 1 : (-1));
    						if ((TileMap.tileTypeAtPixel(Char.myCharz().charFocus.cx + num16 * num17, Char.myCharz().cy + 3) & 2) != 2)
    						{
    							flag6 = true;
    						}
    					}
    					if (num14 <= num16 && !flag6)
    					{
    						if (Char.myCharz().cx > Char.myCharz().charFocus.cx)
    						{
    							Char.myCharz().cx = Char.myCharz().charFocus.cx + num16;
    							Char.myCharz().cdir = -1;
    						}
    						else
    						{
    							Char.myCharz().cx = Char.myCharz().charFocus.cx - num16;
    							Char.myCharz().cdir = 1;
    						}
    						Service.gI().charMove();
    					}
    					GameCanvas.clearKeyHold();
    					GameCanvas.clearKeyPressed();
    					return true;
    				}
    				int num18 = (Char.myCharz().myskill.dx - 20) * ((Char.myCharz().cx > Char.myCharz().charFocus.cx) ? 1 : (-1));
    				if (num14 <= Char.myCharz().myskill.dx)
    				{
    					num18 = 0;
    				}
    				Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().charFocus.cx + num18, Char.myCharz().charFocus.cy);
    				Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    				GameCanvas.clearKeyHold();
    				GameCanvas.clearKeyPressed();
    				return false;
    			}
    			if (num14 < 60 && num15 < 40)
    			{
    				playerMenu(Char.myCharz().charFocus);
    				if (!GameCanvas.isTouch && Char.myCharz().charFocus.charID >= 0 && TileMap.mapID != 51 && TileMap.mapID != 52 && popUpYesNo == null)
    				{
    					GameCanvas.panel.setTypePlayerMenu(Char.myCharz().charFocus);
    					GameCanvas.panel.show();
    					Service.gI().getPlayerMenu(Char.myCharz().charFocus.charID);
    					Service.gI().messagePlayerMenu(Char.myCharz().charFocus.charID);
    				}
    			}
    			else
    			{
    				int num19 = (20 + Res.r.nextInt(20)) * ((Char.myCharz().cx > Char.myCharz().charFocus.cx) ? 1 : (-1));
    				Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().charFocus.cx + num19, Char.myCharz().charFocus.cy);
    				Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    				GameCanvas.clearKeyHold();
    				GameCanvas.clearKeyPressed();
    			}
    			return false;
    		}
    		if (Char.myCharz().itemFocus != null)
    		{
    			pickItem();
    			return false;
    		}
    		return true;
    	}
    
    	public bool isMeCanAttackMob(Mob m)
    	{
    		if (m == null)
    		{
    			return false;
    		}
    		if (Char.myCharz().cTypePk == 5)
    		{
    			return true;
    		}
    		if (Char.myCharz().isAttacPlayerStatus() && !m.isMobMe)
    		{
    			return false;
    		}
    		if (Char.myCharz().mobMe != null && m.Equals(Char.myCharz().mobMe))
    		{
    			return false;
    		}
    		Char @char = findCharInMap(m.mobId);
    		if (@char == null)
    		{
    			return true;
    		}
    		if (@char.cTypePk == 5)
    		{
    			return true;
    		}
    		if (Char.myCharz().isMeCanAttackOtherPlayer(@char))
    		{
    			return true;
    		}
    		return false;
    	}
    
    	private bool checkSkillValid()
    	{
    		if (Char.myCharz().myskill != null && ((Char.myCharz().myskill.template.manaUseType != 1 && Char.myCharz().cMP < Char.myCharz().myskill.manaUse) || (Char.myCharz().myskill.template.manaUseType == 1 && Char.myCharz().cMP < Char.myCharz().cMPFull * Char.myCharz().myskill.manaUse / 100)))
    		{
    			info1.addInfo(mResources.NOT_ENOUGH_MP, 0);
    			auto = 0;
    			return false;
    		}
    		if (Char.myCharz().myskill == null || (Char.myCharz().myskill.template.maxPoint > 0 && Char.myCharz().myskill.point == 0))
    		{
    			GameCanvas.startOKDlg(mResources.SKILL_FAIL);
    			return false;
    		}
    		return true;
    	}
    
    	private bool checkSkillValid2()
    	{
    		if (Char.myCharz().myskill != null && ((Char.myCharz().myskill.template.manaUseType != 1 && Char.myCharz().cMP < Char.myCharz().myskill.manaUse) || (Char.myCharz().myskill.template.manaUseType == 1 && Char.myCharz().cMP < Char.myCharz().cMPFull * Char.myCharz().myskill.manaUse / 100)))
    		{
    			return false;
    		}
    		if (Char.myCharz().myskill == null || (Char.myCharz().myskill.template.maxPoint > 0 && Char.myCharz().myskill.point == 0))
    		{
    			return false;
    		}
    		return true;
    	}
    
    	public void resetButton()
    	{
    		GameCanvas.menu.showMenu = false;
    		ChatTextField.gI().close();
    		ChatTextField.gI().center = null;
    		isLockKey = false;
    		typeTrade = 0;
    		indexMenu = 0;
    		indexSelect = 0;
    		indexItemUse = -1;
    		indexRow = -1;
    		indexRowMax = 0;
    		indexTitle = 0;
    		typeTrade = (typeTradeOrder = 0);
    		mSystem.endKey();
    		if (Char.myCharz().cHP <= 0 || Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5)
    		{
    			if (Char.myCharz().meDead)
    			{
    				cmdDead = new Command(mResources.DIES[0], 11038);
    				center = cmdDead;
    				Char.myCharz().cHP = 0;
    			}
    			isHaveSelectSkill = false;
    		}
    		else
    		{
    			isHaveSelectSkill = true;
    		}
    		scrMain.clear();
    	}
    
    	public override void keyPress(int keyCode)
    	{
    		base.keyPress(keyCode);
    	}
    
    	public override void updateKey()
    	{
    		if (Controller.isStopReadMessage || Char.myCharz().isTeleport || Char.myCharz().isPaintNewSkill || InfoDlg.isLock)
    		{
    			return;
    		}
    		// Man phu (Phuc loi, cau hinh Voice...) dang mo thi CHI no nhan thao
    		// tac. Chan ngay day chu khong chan ben trong UpdateTouch: cai kia
    		// chay SAU updateKeyTouchControl(), luc do can dieu khien ao va cac
    		// nut cua game da xu ly cham roi.
    		if (God.ClientManager.coManPhuDangMo())
    		{
    			God.ClientManager.getInstance().PhimManPhu();
    			God.ClientManager.getInstance().UpdateTouch();
    			return;
    		}
    		if (GameCanvas.isTouch && !ChatTextField.gI().isShow && !GameCanvas.menu.showMenu)
    		{
    			updateKeyTouchControl();
                ClientManager.getInstance().UpdateTouch();
            }

    		checkAuto();
    		GameCanvas.debug("F2", 0);
    		if (ChatPopup.currChatPopup != null)
    		{
    			Command cmdNextLine = ChatPopup.currChatPopup.cmdNextLine;
    			if ((GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] || mScreen.getCmdPointerLast(cmdNextLine)) && cmdNextLine != null)
    			{
    				GameCanvas.isPointerJustRelease = false;
    				GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    				mScreen.keyTouch = -1;
    				if (cmdNextLine != null)
    				{
    					cmdNextLine.performAction();
    				}
    			}
    		}
    		else if (!ChatTextField.gI().isShow)
    		{
    			if ((GameCanvas.keyPressed[12] || mScreen.getCmdPointerLast(GameCanvas.currentScreen.left)) && left != null)
    			{
    				GameCanvas.isPointerJustRelease = false;
    				GameCanvas.isPointerClick = false;
    				GameCanvas.keyPressed[12] = false;
    				mScreen.keyTouch = -1;
    				if (left != null)
    				{
    					left.performAction();
    				}
    			}
    			if ((GameCanvas.keyPressed[13] || mScreen.getCmdPointerLast(GameCanvas.currentScreen.right)) && right != null)
    			{
    				GameCanvas.isPointerJustRelease = false;
    				GameCanvas.isPointerClick = false;
    				GameCanvas.keyPressed[13] = false;
    				mScreen.keyTouch = -1;
    				if (right != null)
    				{
    					right.performAction();
    				}
    			}
    			if ((GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] || mScreen.getCmdPointerLast(GameCanvas.currentScreen.center)) && center != null)
    			{
    				GameCanvas.isPointerJustRelease = false;
    				GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    				mScreen.keyTouch = -1;
    				if (center != null)
    				{
    					center.performAction();
    				}
    			}
    		}
    		else
    		{
    			if (ChatTextField.gI().left != null && (GameCanvas.keyPressed[12] || mScreen.getCmdPointerLast(ChatTextField.gI().left)) && ChatTextField.gI().left != null)
    			{
    				ChatTextField.gI().left.performAction();
    			}
    			if (ChatTextField.gI().right != null && (GameCanvas.keyPressed[13] || mScreen.getCmdPointerLast(ChatTextField.gI().right)) && ChatTextField.gI().right != null)
    			{
    				ChatTextField.gI().right.performAction();
    			}
    			if (ChatTextField.gI().center != null && (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] || mScreen.getCmdPointerLast(ChatTextField.gI().center)) && ChatTextField.gI().center != null)
    			{
    				ChatTextField.gI().center.performAction();
    			}
    		}
    		GameCanvas.debug("F6", 0);
    		updateKeyAlert();
    		GameCanvas.debug("F7", 0);
    		if (Char.myCharz().currentMovePoint != null)
    		{
    			for (int i = 0; i < GameCanvas.keyPressed.Length; i++)
    			{
    				if (GameCanvas.keyPressed[i])
    				{
    					Char.myCharz().currentMovePoint = null;
    					break;
    				}
    			}
    		}
    		GameCanvas.debug("F8", 0);
    		if (ChatTextField.gI().isShow && GameCanvas.keyAsciiPress != 0)
    		{
    			ChatTextField.gI().keyPressed(GameCanvas.keyAsciiPress);
    			GameCanvas.keyAsciiPress = 0;
    		}
    		else if (isLockKey)
    		{
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    		}
    		else
    		{
    			if (GameCanvas.menu.showMenu || isOpenUI() || Char.isLockKey)
    			{
    				return;
    			}
    			if (GameCanvas.keyPressed[10])
    			{
    				GameCanvas.keyPressed[10] = false;
    				doUseHP();
    				GameCanvas.clearKeyPressed();
    			}
    			if (GameCanvas.keyPressed[11] && mobCapcha == null)
    			{
    				if (popUpYesNo != null)
    				{
    					popUpYesNo.cmdYes.performAction();
    				}
    				else if (info2.info.info != null && info2.info.info.charInfo != null)
    				{
    					GameCanvas.panel.setTypeMessage();
    					GameCanvas.panel.show();
    				}
    				GameCanvas.keyPressed[11] = false;
    				GameCanvas.clearKeyPressed();
    			}
    			if (GameCanvas.keyAsciiPress != 0 && TField.isQwerty && GameCanvas.keyAsciiPress == 32)
    			{
    				doUseHP();
    				GameCanvas.keyAsciiPress = 0;
    				GameCanvas.clearKeyPressed();
    			}
    			if (GameCanvas.keyAsciiPress != 0 && mobCapcha == null && TField.isQwerty && GameCanvas.keyAsciiPress == 121)
    			{
    				if (popUpYesNo != null)
    				{
    					popUpYesNo.cmdYes.performAction();
    					GameCanvas.keyAsciiPress = 0;
    					GameCanvas.clearKeyPressed();
    				}
    				else if (info2.info.info != null && info2.info.info.charInfo != null)
    				{
    					GameCanvas.panel.setTypeMessage();
    					GameCanvas.panel.show();
    					GameCanvas.keyAsciiPress = 0;
    					GameCanvas.clearKeyPressed();
    				}
    			}
    			if (GameCanvas.keyPressed[10] && mobCapcha == null)
    			{
    				GameCanvas.keyPressed[10] = false;
    				info2.doClick(10);
    				GameCanvas.clearKeyPressed();
    			}
    			checkDrag();
    			if (!Char.myCharz().isFlyAndCharge)
    			{
    				checkClick();
    			}
    			if (Char.myCharz().cmdMenu != null && Char.myCharz().cmdMenu.isPointerPressInside())
    			{
    				Char.myCharz().cmdMenu.performAction();
    			}
    			if (Char.myCharz().skillPaint != null)
    			{
    				return;
    			}
    			if (GameCanvas.keyAsciiPress != 0)
    			{
    				if (mobCapcha == null)
    				{
    					if (TField.isQwerty)
    					{
    						if (GameCanvas.keyPressed[1])
    						{
    							if (keySkill[0] != null)
    							{
    								doSelectSkill(keySkill[0], true);
    							}
    						}
    						else if (GameCanvas.keyPressed[2])
    						{
    							if (keySkill[1] != null)
    							{
    								doSelectSkill(keySkill[1], true);
    							}
    						}
    						else if (GameCanvas.keyPressed[3])
    						{
    							if (keySkill[2] != null)
    							{
    								doSelectSkill(keySkill[2], true);
    							}
    						}
    						else if (GameCanvas.keyPressed[4])
    						{
    							if (keySkill[3] != null)
    							{
    								doSelectSkill(keySkill[3], true);
    							}
    						}
    						else if (GameCanvas.keyPressed[5])
    						{
    							if (keySkill[4] != null)
    							{
    								doSelectSkill(keySkill[4], true);
    							}
    						}
    						else if (GameCanvas.keyPressed[6])
    						{
    							if (keySkill[5] != null)
    							{
    								doSelectSkill(keySkill[5], true);
    							}
    						}
    						else if (GameCanvas.keyPressed[7])
    						{
    							if (keySkill[6] != null)
    							{
    								doSelectSkill(keySkill[6], true);
    							}
    						}
    						else if (GameCanvas.keyPressed[8])
    						{
    							if (keySkill[7] != null)
    							{
    								doSelectSkill(keySkill[7], true);
    							}
    						}
    						else if (GameCanvas.keyPressed[9])
    						{
    							if (keySkill[8] != null)
    							{
    								doSelectSkill(keySkill[8], true);
    							}
    						}
    						else if (GameCanvas.keyPressed[0])
    						{
    							if (keySkill[9] != null)
    							{
    								doSelectSkill(keySkill[9], true);
    							}
    						}
    						else if (GameCanvas.keyAsciiPress == 114)
    						{
    							ChatTextField.gI().startChat(this, string.Empty);
    						}
                            ClientManager.getInstance().KeyPressed(GameCanvas.keyAsciiPress);
                        }
    					else if (!GameCanvas.isMoveNumberPad)
    					{
    						ChatTextField.gI().startChat(GameCanvas.keyAsciiPress, this, string.Empty);
    					}
    					else if (GameCanvas.keyAsciiPress == 55)
    					{
    						if (keySkill[0] != null)
    						{
    							doSelectSkill(keySkill[0], true);
    						}
    					}
    					else if (GameCanvas.keyAsciiPress == 56)
    					{
    						if (keySkill[1] != null)
    						{
    							doSelectSkill(keySkill[1], true);
    						}
    					}
    					else if (GameCanvas.keyAsciiPress == 57)
    					{
    						if (keySkill[(!Main.isPC) ? 2 : 21] != null)
    						{
    							doSelectSkill(keySkill[2], true);
    						}
    					}
    					else if (GameCanvas.keyAsciiPress == 48)
    					{
    						ChatTextField.gI().startChat(this, string.Empty);
    					}
    				}
    				else
    				{
    					char[] array = keyInput.ToCharArray();
    					MyVector myVector = new MyVector();
    					for (int j = 0; j < array.Length; j++)
    					{
    						myVector.addElement(array[j] + string.Empty);
    					}
    					myVector.removeElementAt(0);
    					string text = (char)GameCanvas.keyAsciiPress + string.Empty;
    					if (text.Equals(string.Empty) || text == null || text.Equals("\n"))
    					{
    						text = "-";
    					}
    					myVector.insertElementAt(text, myVector.size());
    					keyInput = string.Empty;
    					for (int k = 0; k < myVector.size(); k++)
    					{
    						keyInput += ((string)myVector.elementAt(k)).ToUpper();
    					}
    					Service.gI().mobCapcha((char)GameCanvas.keyAsciiPress);
    				}
    				GameCanvas.keyAsciiPress = 0;
    			}
    			if (ditChuyenGiuTrangThai())
    			{
    				GameCanvas.debug("F9", 0);
    			}
    			else if (Char.myCharz().statusMe == 1)
    			{
    				GameCanvas.debug("F10", 0);
    				if (!doSeleckSkillFlag)
    				{
    					if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    					{
    						GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    						doFire(false, false);
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
    					{
    						if (!Char.myCharz().isLockMove)
    						{
    							setCharJump(0);
    						}
    					}
    					else if (GameCanvas.keyHold[1] && mobCapcha == null)
    					{
    						if (!Main.isPC)
    						{
    							Char.myCharz().cdir = -1;
    							if (!Char.myCharz().isLockMove)
    							{
    								setCharJump(-4);
    							}
    						}
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 5 : 25] && mobCapcha == null)
    					{
    						if (!Main.isPC)
    						{
    							Char.myCharz().cdir = 1;
    							if (!Char.myCharz().isLockMove)
    							{
    								setCharJump(4);
    							}
    						}
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
    					{
    						isAutoPlay = false;
    						Char.myCharz().isAttack = false;
    						if (Char.myCharz().cdir == 1)
    						{
    							Char.myCharz().cdir = -1;
    						}
    						else if (!Char.myCharz().isLockMove)
    						{
    							if (Char.myCharz().cx - Char.myCharz().cxSend != 0)
    							{
    								Service.gI().charMove();
    							}
    							Char.myCharz().statusMe = 2;
    							Char.myCharz().cvx = -Char.myCharz().cspeed;
    						}
    						Char.myCharz().holder = false;
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    					{
    						isAutoPlay = false;
    						Char.myCharz().isAttack = false;
    						if (Char.myCharz().cdir == -1)
    						{
    							Char.myCharz().cdir = 1;
    						}
    						else if (!Char.myCharz().isLockMove)
    						{
    							if (Char.myCharz().cx - Char.myCharz().cxSend != 0)
    							{
    								Service.gI().charMove();
    							}
    							Char.myCharz().statusMe = 2;
    							Char.myCharz().cvx = Char.myCharz().cspeed;
    						}
    						Char.myCharz().holder = false;
    					}
    				}
    			}
    			else if (Char.myCharz().statusMe == 2)
    			{
    				GameCanvas.debug("F11", 0);
    				if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    				{
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    					doFire(false, true);
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
    				{
    					if (Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0)
    					{
    						Service.gI().charMove();
    					}
    					Char.myCharz().cvy = -10;
    					Char.myCharz().statusMe = 3;
    					Char.myCharz().cp1 = 0;
    				}
    				else if (GameCanvas.keyHold[1] && mobCapcha == null)
    				{
    					if (Main.isPC)
    					{
    						if (Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0)
    						{
    							Service.gI().charMove();
    						}
    						Char.myCharz().cdir = -1;
    						Char.myCharz().cvy = -10;
    						Char.myCharz().cvx = -4;
    						Char.myCharz().statusMe = 3;
    						Char.myCharz().cp1 = 0;
    					}
    				}
    				else if (GameCanvas.keyHold[3] && mobCapcha == null)
    				{
    					if (!Main.isPC)
    					{
    						if (Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0)
    						{
    							Service.gI().charMove();
    						}
    						Char.myCharz().cdir = 1;
    						Char.myCharz().cvy = -10;
    						Char.myCharz().cvx = 4;
    						Char.myCharz().statusMe = 3;
    						Char.myCharz().cp1 = 0;
    					}
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == 1)
    					{
    						Char.myCharz().cdir = -1;
    					}
    					else
    					{
    						Char.myCharz().cvx = -Char.myCharz().cspeed + Char.myCharz().cBonusSpeed;
    					}
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == -1)
    					{
    						Char.myCharz().cdir = 1;
    					}
    					else
    					{
    						Char.myCharz().cvx = Char.myCharz().cspeed + Char.myCharz().cBonusSpeed;
    					}
    				}
    			}
    			else if (Char.myCharz().statusMe == 3)
    			{
    				isAutoPlay = false;
    				GameCanvas.debug("F12", 0);
    				if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    				{
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    					doFire(false, true);
    				}
    				if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23] || (GameCanvas.keyHold[1] && mobCapcha == null))
    				{
    					if (Char.myCharz().cdir == 1)
    					{
    						Char.myCharz().cdir = -1;
    					}
    					else
    					{
    						Char.myCharz().cvx = -Char.myCharz().cspeed;
    					}
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24] || (GameCanvas.keyHold[3] && mobCapcha == null))
    				{
    					if (Char.myCharz().cdir == -1)
    					{
    						Char.myCharz().cdir = 1;
    					}
    					else
    					{
    						Char.myCharz().cvx = Char.myCharz().cspeed;
    					}
    				}
    				if ((GameCanvas.keyHold[(!Main.isPC) ? 2 : 21] || ((GameCanvas.keyHold[1] || GameCanvas.keyHold[3]) && mobCapcha == null)) && Char.myCharz().canFly && Char.myCharz().cMP > 0 && Char.myCharz().cp1 < 8 && Char.myCharz().cvy > -4)
    				{
    					Char.myCharz().cp1++;
    					Char.myCharz().cvy = -7;
    				}
    			}
    			else if (Char.myCharz().statusMe == 4)
    			{
    				GameCanvas.debug("F13", 0);
    				if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    				{
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    					doFire(false, true);
    				}
    				if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21] && Char.myCharz().cMP > 0 && Char.myCharz().canFly)
    				{
    					isAutoPlay = false;
    					if ((Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0) && (Res.abs(Char.myCharz().cx - Char.myCharz().cxSend) > 96 || Res.abs(Char.myCharz().cy - Char.myCharz().cySend) > 24))
    					{
    						Service.gI().charMove();
    					}
    					Char.myCharz().cvy = -10;
    					Char.myCharz().statusMe = 3;
    					Char.myCharz().cp1 = 0;
    				}
    				if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == 1)
    					{
    						Char.myCharz().cdir = -1;
    					}
    					else
    					{
    						Char.myCharz().cp1++;
    						Char.myCharz().cvx = -Char.myCharz().cspeed;
    						if (Char.myCharz().cp1 > 5 && Char.myCharz().cvy > 6)
    						{
    							Char.myCharz().statusMe = 10;
    							Char.myCharz().cp1 = 0;
    							Char.myCharz().cvy = 0;
    						}
    					}
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == -1)
    					{
    						Char.myCharz().cdir = 1;
    					}
    					else
    					{
    						Char.myCharz().cp1++;
    						Char.myCharz().cvx = Char.myCharz().cspeed;
    						if (Char.myCharz().cp1 > 5 && Char.myCharz().cvy > 6)
    						{
    							Char.myCharz().statusMe = 10;
    							Char.myCharz().cp1 = 0;
    							Char.myCharz().cvy = 0;
    						}
    					}
    				}
    			}
    			else if (Char.myCharz().statusMe == 10)
    			{
    				GameCanvas.debug("F14", 0);
    				if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    				{
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    					doFire(false, true);
    				}
    				if (Char.myCharz().canFly && Char.myCharz().cMP > 0)
    				{
    					if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
    					{
    						isAutoPlay = false;
    						if ((Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0) && (Res.abs(Char.myCharz().cx - Char.myCharz().cxSend) > 96 || Res.abs(Char.myCharz().cy - Char.myCharz().cySend) > 24))
    						{
    							Service.gI().charMove();
    						}
    						Char.myCharz().cvy = -10;
    						Char.myCharz().statusMe = 3;
    						Char.myCharz().cp1 = 0;
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
    					{
    						isAutoPlay = false;
    						if (Char.myCharz().cdir == 1)
    						{
    							Char.myCharz().cdir = -1;
    						}
    						else
    						{
    							Char.myCharz().cvx = -(Char.myCharz().cspeed + 1);
    						}
    					}
    					else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    					{
    						if (Char.myCharz().cdir == -1)
    						{
    							Char.myCharz().cdir = 1;
    						}
    						else
    						{
    							Char.myCharz().cvx = Char.myCharz().cspeed + 1;
    						}
    					}
    				}
    			}
    			else if (Char.myCharz().statusMe == 7)
    			{
    				GameCanvas.debug("F15", 0);
    				if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
    				{
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    				}
    				if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == 1)
    					{
    						Char.myCharz().cdir = -1;
    					}
    					else
    					{
    						Char.myCharz().cvx = -Char.myCharz().cspeed + 2;
    					}
    				}
    				else if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    				{
    					isAutoPlay = false;
    					if (Char.myCharz().cdir == -1)
    					{
    						Char.myCharz().cdir = 1;
    					}
    					else
    					{
    						Char.myCharz().cvx = Char.myCharz().cspeed - 2;
    					}
    				}
    			}
    			GameCanvas.debug("F17", 0);
    			if (GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22] && GameCanvas.keyAsciiPress != 56)
    			{
    				GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22] = false;
    				Char.myCharz().delayFall = 0;
    			}
    			if (GameCanvas.keyPressed[10])
    			{
    				GameCanvas.keyPressed[10] = false;
    				doUseHP();
    			}
    			GameCanvas.debug("F20", 0);
    			GameCanvas.clearKeyPressed();
    			GameCanvas.debug("F23", 0);
    			doSeleckSkillFlag = false;
    		}
    	}
    
    	/// <summary>Trạng thái đang độn thổ.</summary>
    	private const int TT_DON_THO = 7;

    	/// <summary>Trạng thái khinh công — lơ lửng đứng yên trên không.</summary>
    	private const int TT_KHINH_CONG = 10;

    	/// <summary>Mỗi khung hình W hoặc S nhấc/hạ nhân vật bấy nhiêu điểm.</summary>
    	private const int BUOC_DOC_WASD = 3;

    	/// <summary>
    	/// W A S D dịch chuyển nhân vật <b>mà không phá trạng thái đang mang</b>.
    	/// </summary>
    	/// <remarks>
    	/// <para>Chuỗi xử lý phím bình thường chia theo <c>statusMe</c>, và ở hai
    	/// trạng thái đặc biệt nó làm hỏng đúng cái người chơi đang muốn giữ:</para>
    	/// <list type="bullet">
    	/// <item>Khinh công (10): bấm lên là <c>statusMe</c> nhảy sang 3 — tức bật
    	/// nhảy, rơi khỏi thế lơ lửng. Bấm xuống thì không ai xử lý.</item>
    	/// <item>Độn thổ (7): chỉ đi được trái phải, không có lên xuống.</item>
    	/// </list>
    	///
    	/// <para>Hàm này chạy TRƯỚC chuỗi kia và nuốt lượt nếu đã lo xong, nên hai
    	/// trạng thái ấy đi được cả bốn hướng mà <c>statusMe</c> không đổi. Bốn phím
    	/// mũi tên vẫn đi đường cũ — đây chỉ là lối riêng của W A S D.</para>
    	///
    	/// <para>Vẫn báo <c>charMove</c> theo đúng ngưỡng như mọi nhánh khác, nên
    	/// máy chủ vẫn nắm được vị trí thật và vẫn là bên quyết định.</para>
    	/// </remarks>
    	private bool ditChuyenGiuTrangThai()
    	{
    		Char c = Char.myCharz();
    		if (c == null || c.isLockMove)
    		{
    			return false;
    		}
    		int tt = c.statusMe;
    		if (tt != TT_DON_THO && tt != TT_KHINH_CONG)
    		{
    			return false;
    		}
    		bool len = GameCanvas.phimWasd[0] && GameCanvas.keyHold[21];
    		bool xuong = GameCanvas.phimWasd[1] && GameCanvas.keyHold[22];
    		bool trai = GameCanvas.phimWasd[2] && GameCanvas.keyHold[23];
    		bool phai = GameCanvas.phimWasd[3] && GameCanvas.keyHold[24];
    		if (!len && !xuong && !trai && !phai)
    		{
    			return false;
    		}
    		isAutoPlay = false;

    		// Toc do ngang giu dung con so cua tung trang thai: don tho cham hon
    		// hai diem, khinh cong nhanh hon mot diem.
    		int toc = (tt == TT_DON_THO) ? (c.cspeed - 2) : (c.cspeed + 1);
    		if (toc < 1)
    		{
    			toc = 1;
    		}
    		if (trai)
    		{
    			c.cdir = -1;
    			c.cvx = -toc;
    		}
    		else if (phai)
    		{
    			c.cdir = 1;
    			c.cvx = toc;
    		}

    		// Len xuong doi THANG toa do va ep cvy ve 0: de trong luc keo thi nhan
    		// vat roi khoi the lo lung, ma roi la mat trang thai.
    		if (len || xuong)
    		{
    			c.cy += len ? (-BUOC_DOC_WASD) : BUOC_DOC_WASD;
    			c.cvy = 0;
    			if (c.cy < 24)
    			{
    				c.cy = 24;
    			}
    			if (TileMap.pxh > 0 && c.cy > TileMap.pxh)
    			{
    				c.cy = TileMap.pxh;
    			}
    		}

    		if ((c.cx - c.cxSend != 0 || c.cy - c.cySend != 0)
    				&& (Res.abs(c.cx - c.cxSend) > 96 || Res.abs(c.cy - c.cySend) > 24))
    		{
    			Service.gI().charMove();
    		}
    		return true;
    	}

    	public bool isVsMap()
    	{
    		return true;
    	}
    
    	private void checkDrag()
    	{
    		if (isAnalog == 1 || gamePad.disableCheckDrag())
    		{
    			return;
    		}
    		Char.myCharz().cmtoChar = true;
    		if (isUseTouch)
    		{
    			return;
    		}
    		if (GameCanvas.isPointerJustDown)
    		{
    			GameCanvas.isPointerJustDown = false;
    			isPointerDowning = true;
    			ptDownTime = 0;
    			ptLastDownX = (ptFirstDownX = GameCanvas.px);
    			ptLastDownY = (ptFirstDownY = GameCanvas.py);
    		}
    		if (isPointerDowning)
    		{
    			int num = GameCanvas.px - ptLastDownX;
    			int num2 = GameCanvas.py - ptLastDownY;
    			if (!isChangingCameraMode && (Res.abs(GameCanvas.px - ptFirstDownX) > 15 || Res.abs(GameCanvas.py - ptFirstDownY) > 15))
    			{
    				isChangingCameraMode = true;
    			}
    			ptLastDownX = GameCanvas.px;
    			ptLastDownY = GameCanvas.py;
    			ptDownTime++;
    			if (isChangingCameraMode)
    			{
    				Char.myCharz().cmtoChar = false;
    				cmx -= num;
    				cmy -= num2;
    				if (cmx < 24)
    				{
    					int num3 = (24 - cmx) / 3;
    					if (num3 != 0)
    					{
    						cmx += num - num / num3;
    					}
    				}
    				if (cmx < (isVsMap() ? 24 : 0))
    				{
    					cmx = (isVsMap() ? 24 : 0);
    				}
    				if (cmx > cmxLim)
    				{
    					int num4 = (cmx - cmxLim) / 3;
    					if (num4 != 0)
    					{
    						cmx += num - num / num4;
    					}
    				}
    				if (cmx > cmxLim + ((!isVsMap()) ? 24 : 0))
    				{
    					cmx = cmxLim + ((!isVsMap()) ? 24 : 0);
    				}
    				if (cmy < 0)
    				{
    					int num5 = -cmy / 3;
    					if (num5 != 0)
    					{
    						cmy += num2 - num2 / num5;
    					}
    				}
    				if (cmy < -((!isVsMap()) ? 24 : 0))
    				{
    					cmy = -((!isVsMap()) ? 24 : 0);
    				}
    				if (cmy > cmyLim)
    				{
    					cmy = cmyLim;
    				}
    				cmtoX = cmx;
    				cmtoY = cmy;
    			}
    		}
    		if (isPointerDowning && GameCanvas.isPointerJustRelease)
    		{
    			isPointerDowning = false;
    			isChangingCameraMode = false;
    			if (Res.abs(GameCanvas.px - ptFirstDownX) > 15 || Res.abs(GameCanvas.py - ptFirstDownY) > 15)
    			{
    				GameCanvas.isPointerJustRelease = false;
    			}
    		}
    	}
    
    	private void checkClick()
    	{
    		if (isCharging())
    		{
    			return;
    		}
    		if (popUpYesNo != null && popUpYesNo.cmdYes != null && popUpYesNo.cmdYes.isPointerPressInside())
    		{
    			popUpYesNo.cmdYes.performAction();
    		}
    		else
    		{
    			if (checkClickToCapcha())
    			{
    				return;
    			}
    			long num = mSystem.currentTimeMillis();
    			if (lastSingleClick != 0)
    			{
    				lastSingleClick = 0L;
    				GameCanvas.isPointerJustDown = false;
    				if (!disableSingleClick)
    				{
    					checkSingleClick();
    					GameCanvas.isPointerJustRelease = false;
    					isWaitingDoubleClick = true;
    					timeStartDblClick = mSystem.currentTimeMillis();
    				}
    			}
    			if (isWaitingDoubleClick)
    			{
    				timeEndDblClick = mSystem.currentTimeMillis();
    				if (timeEndDblClick - timeStartDblClick < 300 && GameCanvas.isPointerJustRelease)
    				{
    					isWaitingDoubleClick = false;
    					checkDoubleClick();
    				}
    			}
    			if (GameCanvas.isPointerJustRelease)
    			{
    				disableSingleClick = checkSingleClickEarly();
    				lastSingleClick = num;
    				lastClickCMX = cmx;
    				lastClickCMY = cmy;
    				GameCanvas.isPointerJustRelease = false;
    			}
    		}
    	}
    
    	private IMapObject findClickToItem(int px, int py)
    	{
    		IMapObject mapObject = null;
    		int num = 0;
    		int num2 = 30;
    		MyVector[] array = new MyVector[4] { vMob, vNpc, vItemMap, vCharInMap };
    		for (int i = 0; i < array.Length; i++)
    		{
    			for (int j = 0; j < array[i].size(); j++)
    			{
    				IMapObject mapObject2 = (IMapObject)array[i].elementAt(j);
    				if (mapObject2.isInvisible())
    				{
    					continue;
    				}
    				if (mapObject2 is Mob)
    				{
    					Mob mob = (Mob)mapObject2;
    					if (mob.isMobMe && mob.Equals(Char.myCharz().mobMe))
    					{
    						continue;
    					}
    				}
    				int x = mapObject2.getX();
    				int y = mapObject2.getY();
    				int w = mapObject2.getW();
    				int h = mapObject2.getH();
    				if (!inRectangle(px, py, x - w / 2 - num2, y - h - num2, w + num2 * 2, h + num2 * 2))
    				{
    					continue;
    				}
    				if (mapObject == null)
    				{
    					mapObject = mapObject2;
    					num = Res.abs(px - x) + Res.abs(py - y);
    					if (i == 1)
    					{
    						return mapObject;
    					}
    				}
    				else
    				{
    					int num3 = Res.abs(px - x) + Res.abs(py - y);
    					if (num3 < num)
    					{
    						mapObject = mapObject2;
    						num = num3;
    					}
    				}
    			}
    		}
    		return mapObject;
    	}
    
    	private Mob findClickToMOB(int px, int py)
    	{
    		int num = 30;
    		Mob mob = null;
    		int num2 = 0;
    		for (int i = 0; i < vMob.size(); i++)
    		{
    			Mob mob2 = (Mob)vMob.elementAt(i);
    			if (mob2.isInvisible())
    			{
    				continue;
    			}
    			if (mob2 != null)
    			{
    				Mob mob3 = mob2;
    				if (mob3.isMobMe && mob3.Equals(Char.myCharz().mobMe))
    				{
    					continue;
    				}
    			}
    			int x = mob2.getX();
    			int y = mob2.getY();
    			int w = mob2.getW();
    			int h = mob2.getH();
    			if (!inRectangle(px, py, x - w / 2 - num, y - h - num, w + num * 2, h + num * 2))
    			{
    				continue;
    			}
    			if (mob == null)
    			{
    				mob = mob2;
    				num2 = Res.abs(px - x) + Res.abs(py - y);
    				continue;
    			}
    			int num3 = Res.abs(px - x) + Res.abs(py - y);
    			if (num3 < num2)
    			{
    				mob = mob2;
    				num2 = num3;
    			}
    		}
    		return mob;
    	}
    
    	private bool inRectangle(int xClick, int yClick, int x, int y, int w, int h)
    	{
    		return xClick >= x && xClick <= x + w && yClick >= y && yClick <= y + h;
    	}
    
    	private bool checkSingleClickEarly()
    	{
    		int num = GameCanvas.px + cmx;
    		int num2 = GameCanvas.py + cmy;
    		Char.myCharz().cancelAttack();
    		IMapObject mapObject = findClickToItem(num, num2);
    		if (mapObject != null)
    		{
    			if (Char.myCharz().isAttacPlayerStatus() && Char.myCharz().charFocus != null && !mapObject.Equals(Char.myCharz().charFocus) && !mapObject.Equals(Char.myCharz().charFocus.mobMe) && mapObject is Char)
    			{
    				Char @char = (Char)mapObject;
    				if (@char.cTypePk != 5 && !@char.isAttacPlayerStatus())
    				{
    					checkClickMoveTo(num, num2, 2);
    					return false;
    				}
    			}
    			if (Char.myCharz().mobFocus == mapObject || Char.myCharz().itemFocus == mapObject)
    			{
    				doDoubleClickToObj(mapObject);
    				return true;
    			}
    			if (TileMap.mapID == 51 && mapObject.Equals(Char.myCharz().npcFocus))
    			{
    				checkClickMoveTo(num, num2, 3);
    				return false;
    			}
    			if (Char.myCharz().skillPaint != null || Char.myCharz().arr != null || Char.myCharz().dart != null || Char.myCharz().skillInfoPaint() != null)
    			{
    				return false;
    			}
    			Char.myCharz().focusManualTo(mapObject);
    			mapObject.stopMoving();
    			return false;
    		}
    		return false;
    	}
    
    	private void checkDoubleClick()
    	{
    		int num = GameCanvas.px + lastClickCMX;
    		int num2 = GameCanvas.py + lastClickCMY;
    		int cy = Char.myCharz().cy;
    		if (isLockKey)
    		{
    			return;
    		}
    		IMapObject mapObject = findClickToItem(num, num2);
    		if (mapObject != null)
    		{
    			if (mapObject is Mob && !isMeCanAttackMob((Mob)mapObject))
    			{
    				checkClickMoveTo(num, num2, 4);
    			}
    			else
    			{
    				if (checkClickToBotton(mapObject) || (!mapObject.Equals(Char.myCharz().npcFocus) && mobCapcha != null))
    				{
    					return;
    				}
    				if (Char.myCharz().isAttacPlayerStatus() && Char.myCharz().charFocus != null && !mapObject.Equals(Char.myCharz().charFocus) && !mapObject.Equals(Char.myCharz().charFocus.mobMe) && mapObject is Char)
    				{
    					Char @char = (Char)mapObject;
    					if (@char.cTypePk != 5 && !@char.isAttacPlayerStatus())
    					{
    						checkClickMoveTo(num, num2, 5);
    						return;
    					}
    				}
    				if (TileMap.mapID == 51 && mapObject.Equals(Char.myCharz().npcFocus))
    				{
    					checkClickMoveTo(num, num2, 6);
    				}
    				else
    				{
    					doDoubleClickToObj(mapObject);
    				}
    			}
    		}
    		else if (!checkClickToPopup(num, num2) && !checkClipTopChatPopUp(num, num2) && !Main.isPC)
    		{
    			checkClickMoveTo(num, num2, 7);
    		}
    	}
    
    	private bool checkClickToBotton(IMapObject Object)
    	{
    		if (Object == null)
    		{
    			return false;
    		}
    		int y = Object.getY();
    		int num = Char.myCharz().cy;
    		if (y < num)
    		{
    			while (y < num)
    			{
    				num -= 5;
    				if (TileMap.tileTypeAt(Char.myCharz().cx, num, 8192))
    				{
    					auto = 0;
    					Char.myCharz().cancelAttack();
    					Char.myCharz().currentMovePoint = null;
    					return true;
    				}
    			}
    		}
    		return false;
    	}
    
    	public void doDoubleClickToObj(IMapObject obj)
    	{
    		if ((obj.Equals(Char.myCharz().npcFocus) || mobCapcha == null) && !checkClickToBotton(obj))
    		{
    			checkEffToObj(obj, false);
    			Char.myCharz().cancelAttack();
    			Char.myCharz().currentMovePoint = null;
    			Char.myCharz().cvx = (Char.myCharz().cvy = 0);
    			obj.stopMoving();
    			auto = 10;
    			doFire(false, true);
    			clickToX = obj.getX();
    			clickToY = obj.getY();
    			clickOnTileTop = false;
    			clickMoving = true;
    			clickMovingRed = true;
    			clickMovingTimeOut = 20;
    			clickMovingP1 = 30;
    		}
    	}
    
    	private void checkSingleClick()
    	{
    		int xClick = GameCanvas.px + lastClickCMX;
    		int yClick = GameCanvas.py + lastClickCMY;
    		if (!isLockKey && !checkClickToPopup(xClick, yClick) && !checkClipTopChatPopUp(xClick, yClick))
    		{
    			checkClickMoveTo(xClick, yClick, 0);
    		}
    	}
    
    	private bool checkClipTopChatPopUp(int xClick, int yClick)
    	{
    		if (Equals(info2) && gI().popUpYesNo != null)
    		{
    			return false;
    		}
    		if (info2.info.info != null && info2.info.info.charInfo != null)
    		{
    			int num = 0;
    			int num2 = 0;
    			num = Res.abs(info2.cmx) + info2.info.X - 40;
    			num2 = Res.abs(info2.cmy) + info2.info.Y;
    			if (inRectangle(xClick - cmx, yClick - cmy, num, num2, 200, info2.info.H))
    			{
    				info2.doClick(10);
    				return true;
    			}
    		}
    		return false;
    	}
    
    	private bool checkClickToPopup(int xClick, int yClick)
    	{
    		for (int i = 0; i < PopUp.vPopups.size(); i++)
    		{
    			PopUp popUp = (PopUp)PopUp.vPopups.elementAt(i);
    			if (inRectangle(xClick, yClick, popUp.cx, popUp.cy, popUp.cw, popUp.ch))
    			{
    				if (popUp.cy <= 24 && TileMap.isInAirMap() && Char.myCharz().cTypePk != 0)
    				{
    					return false;
    				}
    				if (popUp.isPaint)
    				{
    					popUp.doClick(10);
    					return true;
    				}
    			}
    		}
    		return false;
    	}
    
    	private void checkClickMoveTo(int xClick, int yClick, int index)
    	{
    		if (gamePad.disableClickMove())
    		{
    			return;
    		}
    		Char.myCharz().cancelAttack();
    		if (xClick < TileMap.pxw && xClick > TileMap.pxw - 32)
    		{
    			Char.myCharz().currentMovePoint = new MovePoint(TileMap.pxw, yClick);
    			return;
    		}
    		if (xClick < 32 && xClick > 0)
    		{
    			Char.myCharz().currentMovePoint = new MovePoint(0, yClick);
    			return;
    		}
    		if (xClick < TileMap.pxw && xClick > TileMap.pxw - 48)
    		{
    			Char.myCharz().currentMovePoint = new MovePoint(TileMap.pxw, yClick);
    			return;
    		}
    		if (xClick < 48 && xClick > 0)
    		{
    			Char.myCharz().currentMovePoint = new MovePoint(0, yClick);
    			return;
    		}
    		clickToX = xClick;
    		clickToY = yClick;
    		clickOnTileTop = false;
    		Char.myCharz().delayFall = 0;
    		int num = ((!Char.myCharz().canFly || Char.myCharz().cMP <= 0) ? 1000 : 0);
    		if (clickToY > Char.myCharz().cy && Res.abs(clickToX - Char.myCharz().cx) < 12)
    		{
    			return;
    		}
    		for (int i = 0; i < 60 + num && clickToY + i < TileMap.pxh - 24; i += 24)
    		{
    			if (TileMap.tileTypeAt(clickToX, clickToY + i, 2))
    			{
    				clickToY = TileMap.tileYofPixel(clickToY + i);
    				clickOnTileTop = true;
    				break;
    			}
    		}
    		for (int j = 0; j < 40 + num; j += 24)
    		{
    			if (TileMap.tileTypeAt(clickToX, clickToY - j, 2))
    			{
    				clickToY = TileMap.tileYofPixel(clickToY - j);
    				clickOnTileTop = true;
    				break;
    			}
    		}
    		clickMoving = true;
    		clickMovingRed = false;
    		clickMovingP1 = ((!clickOnTileTop) ? 30 : ((yClick >= clickToY) ? clickToY : yClick));
    		Char.myCharz().delayFall = 0;
    		if (!clickOnTileTop && clickToY < Char.myCharz().cy - 50)
    		{
    			Char.myCharz().delayFall = 20;
    		}
    		clickMovingTimeOut = 30;
    		auto = 0;
    		if (Char.myCharz().holder)
    		{
    			Char.myCharz().removeHoleEff();
    		}
    		Char.myCharz().currentMovePoint = new MovePoint(clickToX, clickToY);
    		Char.myCharz().cdir = ((Char.myCharz().cx - Char.myCharz().currentMovePoint.xEnd <= 0) ? 1 : (-1));
    		Char.myCharz().endMovePointCommand = null;
    		isAutoPlay = false;
    	}
    
    	private void checkAuto()
    	{
    		long num = mSystem.currentTimeMillis();
    		if (GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21] || GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23] || GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24] || GameCanvas.keyPressed[1] || GameCanvas.keyPressed[3])
    		{
    			auto = 0;
    			isAutoPlay = false;
    		}
    		if (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] && !isPaintPopup())
    		{
    			if (auto == 0)
    			{
    				if (num - lastFire < 800 && checkSkillValid2() && (Char.myCharz().mobFocus != null || (Char.myCharz().charFocus != null && Char.myCharz().isMeCanAttackOtherPlayer(Char.myCharz().charFocus))))
    				{
    					Res.outz("toi day");
    					auto = 10;
    					GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = false;
    				}
    			}
    			else
    			{
    				auto = 0;
    				GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23] = (GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24] = false);
    			}
    			lastFire = num;
    		}
    		if (GameCanvas.gameTick % 5 == 0 && auto > 0 && Char.myCharz().currentMovePoint == null)
    		{
    			if (Char.myCharz().myskill != null && (Char.myCharz().myskill.template.isUseAlone() || Char.myCharz().myskill.paintCanNotUseSkill))
    			{
    				return;
    			}
    			if ((Char.myCharz().mobFocus != null && Char.myCharz().mobFocus.status != 1 && Char.myCharz().mobFocus.status != 0 && Char.myCharz().charFocus == null) || (Char.myCharz().charFocus != null && Char.myCharz().isMeCanAttackOtherPlayer(Char.myCharz().charFocus)))
    			{
    				if (Char.myCharz().myskill.paintCanNotUseSkill)
    				{
    					return;
    				}
    				doFire(false, true);
    			}
    		}
    		if (auto > 1)
    		{
    			auto--;
    		}
    	}
    
    	public void doUseHP()
    	{
    		if (Char.myCharz().stone || Char.myCharz().blindEff || Char.myCharz().holdEffID > 0)
    		{
    			return;
    		}
    		long num = mSystem.currentTimeMillis();
    		if (num - lastUsePotion >= 10000)
    		{
    			if (!Char.myCharz().doUsePotion())
    			{
    				info1.addInfo(mResources.HP_EMPTY, 0);
    				return;
    			}
    			ServerEffect.addServerEffect(11, Char.myCharz(), 5);
    			ServerEffect.addServerEffect(104, Char.myCharz(), 4);
    			lastUsePotion = num;
    			SoundMn.gI().eatPeans();
    		}
    	}
    
    	public void activeSuperPower(int x, int y)
    	{
    		if (!isSuperPower)
    		{
    			SoundMn.gI().bigeExlode();
    			isSuperPower = true;
    			tPower = 0;
    			dxPower = 0;
    			xPower = x - cmx;
    			yPower = y - cmy;
    		}
    	}
    
    	public void activeRongThanEff(bool isMe)
    	{
    		activeRongThan = true;
    		isUseFreez = true;
    		isMeCallRongThan = true;
    		if (isMe)
    		{
    			Effect me = new Effect(20, Char.myCharz().cx, Char.myCharz().cy - 77, 2, 8, 1);
    			EffecMn.addEff(me);
    		}
    	}
    
    	/// <summary>
    	/// Moc luc may chu bao rong bien mat, de con biet la da cho qua lau.
    	/// </summary>
    	public long mocAnRongThan;

    	public void hideRongThanEff()
    	{
    		activeRongThan = false;
    		isUseFreez = true;
    		isMeCallRongThan = false;
    		mocAnRongThan = mSystem.currentTimeMillis();
    	}

    	/// <summary>
    	/// Duong thoat cuoi cung cho con rong.
    	///
    	/// Canh chop trang lam rong bien mat chi chay trong paint(), va cai cong
    	/// vao no doi ChatPopup.currChatPopup == null. May chu lai gui loi chao
    	/// cua rong ngay truoc goi "bien mat", nen rat hay co dung mot hop thoai
    	/// dang mo dung luc ay: canh chop khong bao gio chay, isUseFreez ket o
    	/// true, con rong treo lai giua troi toi. Dung canh vua gap.
    	///
    	/// Sau hai giay ma van chua chop thi thoi, an thang.
    	/// </summary>
    	public void chotAnRongThanNeuKet()
    	{
    		if (activeRongThan || mocAnRongThan == 0L)
    		{
    			return;
    		}
    		if (!isRongThanXuatHien && !isUseFreez)
    		{
    			mocAnRongThan = 0L;
    			return;
    		}
    		if (mSystem.currentTimeMillis() - mocAnRongThan < 2000L)
    		{
    			return;
    		}
    		isUseFreez = false;
    		dem = 0;
    		mocAnRongThan = 0L;
    		hideRongThan();
    	}
    
    	public void doiMauTroi()
    	{
    		isRongThanXuatHien = true;
    		mautroi = mGraphics.blendColor(0.4f, 0, GameCanvas.colorTop[GameCanvas.colorTop.Length - 1]);
    	}
    
    	public void callRongThan(int x, int y)
    	{
    		Res.outz("VE RONG THAN O VI TRI x= " + x + " y=" + y);
    		doiMauTroi();
    		Effect me = new Effect((!isRongNamek) ? 17 : 25, x, y - 77, 2, -1, 1);
    		EffecMn.addEff(me);
    	}
    
    	public void hideRongThan()
    	{
    		isRongThanXuatHien = false;
    		EffecMn.removeEff(17);
    		if (isRongNamek)
    		{
    			isRongNamek = false;
    			EffecMn.removeEff(25);
    		}
    	}
    
    	private void autoPlay()
    	{
    		if (timeSkill > 0)
    		{
    			timeSkill--;
    		}
    		if (!canAutoPlay || isChangeZone || Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5 || Char.myCharz().isCharge || Char.myCharz().isFlyAndCharge || Char.myCharz().isUseChargeSkill())
    		{
    			return;
    		}
    		bool flag = false;
    		for (int i = 0; i < vMob.size(); i++)
    		{
    			Mob mob = (Mob)vMob.elementAt(i);
    			if (mob.status != 0 && mob.status != 1)
    			{
    				flag = true;
    			}
    		}
    		if (!flag)
    		{
    			return;
    		}
    		bool flag2 = false;
    		for (int j = 0; j < Char.myCharz().arrItemBag.Length; j++)
    		{
    			Item item = Char.myCharz().arrItemBag[j];
    			if (item != null && item.template.type == 6)
    			{
    				flag2 = true;
    				break;
    			}
    		}
    		if (!flag2 && GameCanvas.gameTick % 150 == 0)
    		{
    			Service.gI().requestPean();
    		}
    		if (Char.myCharz().cHP <= Char.myCharz().cHPFull * 20 / 100 || Char.myCharz().cMP <= Char.myCharz().cMPFull * 20 / 100)
    		{
    			doUseHP();
    		}
    		if (Char.myCharz().mobFocus == null || (Char.myCharz().mobFocus != null && Char.myCharz().mobFocus.isMobMe))
    		{
    			for (int k = 0; k < vMob.size(); k++)
    			{
    				Mob mob2 = (Mob)vMob.elementAt(k);
    				if (mob2.status != 0 && mob2.status != 1 && mob2.hp > 0 && !mob2.isMobMe)
    				{
    					Char.myCharz().cx = mob2.x;
    					Char.myCharz().cy = mob2.y;
    					Char.myCharz().mobFocus = mob2;
    					Service.gI().charMove();
    					break;
    				}
    			}
    		}
    		else if (Char.myCharz().mobFocus.hp <= 0 || Char.myCharz().mobFocus.status == 1 || Char.myCharz().mobFocus.status == 0)
    		{
    			Char.myCharz().mobFocus = null;
    		}
    		if (Char.myCharz().mobFocus == null || timeSkill != 0 || (Char.myCharz().skillInfoPaint() != null && Char.myCharz().indexSkill < Char.myCharz().skillInfoPaint().Length && Char.myCharz().dart != null && Char.myCharz().arr != null))
    		{
    			return;
    		}
    		Skill skill = null;
    		if (GameCanvas.isTouch)
    		{
    			for (int l = 0; l < onScreenSkill.Length; l++)
    			{
    				if (onScreenSkill[l] == null || onScreenSkill[l].paintCanNotUseSkill || onScreenSkill[l].template.id == 10 || onScreenSkill[l].template.id == 11 || onScreenSkill[l].template.id == 14 || onScreenSkill[l].template.id == 23 || onScreenSkill[l].template.id == 7 || Char.myCharz().skillInfoPaint() != null || onScreenSkill[l].template.isSkillSpec())
    				{
    					continue;
    				}
    				int num = 0;
    				num = ((onScreenSkill[l].template.manaUseType == 2) ? 1 : ((onScreenSkill[l].template.manaUseType == 1) ? (onScreenSkill[l].manaUse * Char.myCharz().cMPFull / 100) : onScreenSkill[l].manaUse));
    				if (Char.myCharz().cMP >= num)
    				{
    					if (skill == null)
    					{
    						skill = onScreenSkill[l];
    					}
    					else if (skill.coolDown < onScreenSkill[l].coolDown)
    					{
    						skill = onScreenSkill[l];
    					}
    				}
    			}
    			if (skill != null)
    			{
    				doSelectSkill(skill, true);
    				doDoubleClickToObj(Char.myCharz().mobFocus);
    			}
    			return;
    		}
    		for (int m = 0; m < keySkill.Length; m++)
    		{
    			if (keySkill[m] == null || keySkill[m].paintCanNotUseSkill || keySkill[m].template.id == 10 || keySkill[m].template.id == 11 || keySkill[m].template.id == 14 || keySkill[m].template.id == 23 || keySkill[m].template.id == 7 || Char.myCharz().skillInfoPaint() != null)
    			{
    				continue;
    			}
    			int num2 = 0;
    			num2 = ((keySkill[m].template.manaUseType == 2) ? 1 : ((keySkill[m].template.manaUseType == 1) ? (keySkill[m].manaUse * Char.myCharz().cMPFull / 100) : keySkill[m].manaUse));
    			if (Char.myCharz().cMP >= num2)
    			{
    				if (skill == null)
    				{
    					skill = keySkill[m];
    				}
    				else if (skill.coolDown < keySkill[m].coolDown)
    				{
    					skill = keySkill[m];
    				}
    			}
    		}
    		if (skill != null)
    		{
    			doSelectSkill(skill, true);
    			doDoubleClickToObj(Char.myCharz().mobFocus);
    		}
    	}
    
    	private void doFire(bool isFireByShortCut, bool skipWaypoint)
    	{
    		tam++;
    		Waypoint waypoint = Char.myCharz().isInEnterOfflinePoint();
    		Waypoint waypoint2 = Char.myCharz().isInEnterOnlinePoint();
    		if (!skipWaypoint && waypoint != null && (Char.myCharz().mobFocus == null || (Char.myCharz().mobFocus != null && Char.myCharz().mobFocus.templateId == 0)))
    		{
    			waypoint.popup.command.performAction();
    		}
    		else if (!skipWaypoint && waypoint2 != null && (Char.myCharz().mobFocus == null || (Char.myCharz().mobFocus != null && Char.myCharz().mobFocus.templateId == 0)))
    		{
    			waypoint2.popup.command.performAction();
    		}
    		else
    		{
    			if ((TileMap.mapID == 51 && Char.myCharz().npcFocus != null) || Char.myCharz().statusMe == 14)
    			{
    				return;
    			}
    			Char.myCharz().cvx = (Char.myCharz().cvy = 0);
    			if (Char.myCharz().isSelectingSkillUseAlone() && Char.myCharz().focusToAttack())
    			{
    				if (checkSkillValid())
    				{
    					Char.myCharz().currentFireByShortcut = isFireByShortCut;
    					Char.myCharz().useSkillNotFocus();
    				}
    			}
    			else if (isAttack())
    			{
    				if (Char.myCharz().isUseChargeSkill() && Char.myCharz().focusToAttack())
    				{
    					if (checkSkillValid())
    					{
    						Char.myCharz().currentFireByShortcut = isFireByShortCut;
    						Char.myCharz().sendUseChargeSkill();
    					}
    					else
    					{
    						Char.myCharz().stopUseChargeSkill();
    					}
    				}
    				else
    				{
    					bool flag = TileMap.tileTypeAt(Char.myCharz().cx, Char.myCharz().cy, 2);
    					Char.myCharz().setSkillPaint(sks[Char.myCharz().myskill.skillId], (!flag) ? 1 : 0);
    					if (flag)
    					{
    						Char.myCharz().delayFall = 20;
    					}
    					Char.myCharz().currentFireByShortcut = isFireByShortCut;
    				}
    			}
    			if (Char.myCharz().isSelectingSkillBuffToPlayer())
    			{
    				auto = 0;
    			}
    		}
    	}
    
    	private void askToPick()
    	{
    		Npc npc = new Npc(5, 0, -100, 100, 5, info1.charId[Char.myCharz().cgender][2]);
    		string nhatvatpham = mResources.nhatvatpham;
    		string[] menu = new string[2]
    		{
    			mResources.YES,
    			mResources.NO
    		};
    		npc.idItem = 673;
    		gI().createMenu(menu, npc);
    		ChatPopup.addChatPopupWithIcon(nhatvatpham, 100000, npc, 5820);
    	}
    
    	private void pickItem()
    	{
    		if (Char.myCharz().itemFocus == null)
    		{
    			return;
    		}
    		if (Char.myCharz().cx < Char.myCharz().itemFocus.x)
    		{
    			Char.myCharz().cdir = 1;
    		}
    		else
    		{
    			Char.myCharz().cdir = -1;
    		}
    		int num = Math.abs(Char.myCharz().cx - Char.myCharz().itemFocus.x);
    		int num2 = Math.abs(Char.myCharz().cy - Char.myCharz().itemFocus.y);
    		if (num <= 40 && num2 < 40)
    		{
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    			if (Char.myCharz().itemFocus.template.id != 673)
    			{
    				Service.gI().pickItem(Char.myCharz().itemFocus.itemMapID);
    			}
    			else
    			{
    				askToPick();
    			}
    		}
    		else
    		{
    			Char.myCharz().currentMovePoint = new MovePoint(Char.myCharz().itemFocus.x, Char.myCharz().itemFocus.y);
    			Char.myCharz().endMovePointCommand = new Command(null, null, 8002, null);
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    		}
    	}
    
    	public bool isCharging()
    	{
    		if (Char.myCharz().isFlyAndCharge || Char.myCharz().isUseSkillAfterCharge || Char.myCharz().isStandAndCharge || Char.myCharz().isWaitMonkey || isSuperPower || Char.myCharz().isFreez)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void doSelectSkill(Skill skill, bool isShortcut)
    	{
    		// taskMaint co the la null trong khoang doi map: may chu chua gui lai
    		// nhiem vu. Doc thang .taskId o day nem NullReference ngay giua luc
    		// chon chieu, va o duong VE thi mot lan nem la ca khung hinh khong ve
    		// duoc nua — game dung im o man cho.
    		if (Char.myCharz().isCreateDark || isCharging()
    				|| Char.myCharz().taskMaint == null
    				|| Char.myCharz().taskMaint.taskId <= 1)
    		{
    			return;
    		}
    		Char.myCharz().myskill = skill;
    		if (lastSkill != skill && lastSkill != null)
    		{
    			Service.gI().selectSkill(skill.template.id);
    			saveRMSCurrentSkill(skill.template.id);
    			resetButton();
    			lastSkill = skill;
    			selectedIndexSkill = -1;
    			gI().auto = 0;
    			return;
    		}
    		if (Char.myCharz().isUseSkillSpec())
    		{
    			Res.outz(">>>use skill spec: " + skill.template.id);
    			Char.myCharz().sendNewAttack(skill.template.id);
    			saveRMSCurrentSkill(skill.template.id);
    			resetButton();
    			lastSkill = skill;
    			selectedIndexSkill = -1;
    			gI().auto = 0;
    			return;
    		}
    		if (Char.myCharz().isSelectingSkillUseAlone())
    		{
    			Res.outz("use skill not focus");
    			doUseSkillNotFocus(skill);
    			lastSkill = skill;
    			return;
    		}
    		selectedIndexSkill = -1;
    		if (skill == null)
    		{
    			return;
    		}
    		Res.outz("only select skill");
    		if (lastSkill != skill)
    		{
    			Service.gI().selectSkill(skill.template.id);
    			saveRMSCurrentSkill(skill.template.id);
    			resetButton();
    		}
    		if (Char.myCharz().charFocus != null || !Char.myCharz().isSelectingSkillBuffToPlayer())
    		{
    			if (Char.myCharz().focusToAttack())
    			{
    				doFire(isShortcut, true);
    				doSeleckSkillFlag = true;
    			}
    			lastSkill = skill;
    		}
    	}
    
    	public void doUseSkill(Skill skill, bool isShortcut)
    	{
    		if ((TileMap.mapID == 112 || TileMap.mapID == 113) && Char.myCharz().cTypePk == 0)
    		{
    			return;
    		}
    		if (Char.myCharz().isSelectingSkillUseAlone())
    		{
    			Res.outz("HERE");
    			doUseSkillNotFocus(skill);
    			return;
    		}
    		selectedIndexSkill = -1;
    		if (skill != null)
    		{
    			Service.gI().selectSkill(skill.template.id);
    			saveRMSCurrentSkill(skill.template.id);
    			resetButton();
    			Char.myCharz().myskill = skill;
    			doFire(isShortcut, true);
    		}
    	}
    
    	public void doUseSkillNotFocus(Skill skill)
    	{
    		if (((TileMap.mapID != 112 && TileMap.mapID != 113) || Char.myCharz().cTypePk != 0) && checkSkillValid())
    		{
    			selectedIndexSkill = -1;
    			if (skill != null)
    			{
    				Service.gI().selectSkill(skill.template.id);
    				saveRMSCurrentSkill(skill.template.id);
    				resetButton();
    				Char.myCharz().myskill = skill;
    				Char.myCharz().useSkillNotFocus();
    				Char.myCharz().currentFireByShortcut = true;
    				auto = 0;
    			}
    		}
    	}
    
    	public void sortSkill()
    	{
    		for (int i = 0; i < Char.myCharz().vSkillFight.size() - 1; i++)
    		{
    			Skill skill = (Skill)Char.myCharz().vSkillFight.elementAt(i);
    			for (int j = i + 1; j < Char.myCharz().vSkillFight.size(); j++)
    			{
    				Skill skill2 = (Skill)Char.myCharz().vSkillFight.elementAt(j);
    				if (skill2.template.id < skill.template.id)
    				{
    					Skill skill3 = skill2;
    					skill2 = skill;
    					skill = skill3;
    					Char.myCharz().vSkillFight.setElementAt(skill, i);
    					Char.myCharz().vSkillFight.setElementAt(skill2, j);
    				}
    			}
    		}
    	}
    
    	public void updateKeyTouchCapcha()
    	{
    		if (isNotPaintTouchControl())
    		{
    			return;
    		}
    		for (int i = 0; i < strCapcha.Length; i++)
    		{
    			keyCapcha[i] = -1;
    			if (!GameCanvas.isTouchControl)
    			{
    				continue;
    			}
    			int num = (GameCanvas.w - strCapcha.Length * disXC) / 2;
    			int w = strCapcha.Length * disXC;
    			int y = GameCanvas.h - 40;
    			int h = disXC;
    			if (!GameCanvas.isPointerHoldIn(num, y, w, h))
    			{
    				continue;
    			}
    			int num2 = (GameCanvas.px - num) / disXC;
    			if (i == num2)
    			{
    				keyCapcha[i] = 1;
    			}
    			if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease && i == num2)
    			{
    				char[] array = keyInput.ToCharArray();
    				MyVector myVector = new MyVector();
    				for (int j = 0; j < array.Length; j++)
    				{
    					myVector.addElement(array[j] + string.Empty);
    				}
    				myVector.removeElementAt(0);
    				myVector.insertElementAt(strCapcha[i] + string.Empty, myVector.size());
    				keyInput = string.Empty;
    				for (int k = 0; k < myVector.size(); k++)
    				{
    					keyInput += ((string)myVector.elementAt(k)).ToUpper();
    				}
    				Service.gI().mobCapcha(strCapcha[i]);
    			}
    		}
    	}
    
    	public bool checkClickToCapcha()
    	{
    		if (mobCapcha == null)
    		{
    			return false;
    		}
    		int x = (GameCanvas.w - 5 * disXC) / 2;
    		int w = 5 * disXC;
    		int y = GameCanvas.h - 40;
    		int h = disXC;
    		if (GameCanvas.isPointerHoldIn(x, y, w, h))
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void checkMouseChat()
    	{
    		if (GameCanvas.isMouseFocus(xC, yC, W_CHAT, H_CHAT))
    		{
    			if (!TileMap.isOfflineMap())
    			{
    				mScreen.keyMouse = 15;
    			}
    		}
    		else if (GameCanvas.isMouseFocus(xHP, yHP, 40, 40))
    		{
    			if (Char.myCharz().statusMe != 14)
    			{
    				mScreen.keyMouse = 10;
    			}
    		}
    		else if (GameCanvas.isMouseFocus(xF, yF, 40, 40))
    		{
    			if (Char.myCharz().statusMe != 14)
    			{
    				mScreen.keyMouse = 5;
    			}
    		}
    		else if (cmdMenu != null && GameCanvas.isMouseFocus(cmdMenu.x, cmdMenu.y, cmdMenu.w / 2, cmdMenu.h))
    		{
    			mScreen.keyMouse = 1;
    		}
    		else
    		{
    			mScreen.keyMouse = -1;
    		}
    	}
    
    	private void updateKeyTouchControl()
    	{
    		if (isNotPaintTouchControl())
    		{
    			return;
    		}
    		mScreen.keyTouch = -1;
    		if (GameCanvas.isTouchControl)
    		{
    			if (GameCanvas.isPointerHoldIn(0, 0, 60, 50) && GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    			{
    				if (Char.myCharz().cmdMenu != null)
    				{
    					Char.myCharz().cmdMenu.performAction();
    				}
    				Char.myCharz().currentMovePoint = null;
    				GameCanvas.clearAllPointerEvent();
    				flareFindFocus = true;
    				flareTime = 5;
    				return;
    			}
    			if (Main.isPC)
    			{
    				checkMouseChat();
    			}
    			if (!TileMap.isOfflineMap() && GameCanvas.isPointerHoldIn(xC, yC, W_CHAT, H_CHAT))
    			{
    				mScreen.keyTouch = 15;
    				GameCanvas.isPointerJustDown = false;
    				isPointerDowning = false;
    				if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    				{
    					// Dung chung duong gui voi khung chat o day man hinh.
    					//
    					// Nut nay truoc day goi ChatTextField.startChat, va cai
    					// do gui bang Service.chat(text) — chat THUONG, khong
    					// thuoc kenh nao. Con khung chat co bon the kenh va gui
    					// dung the dang mo. Hai duong khac nhau cho cung mot
    					// viec: dang xem the "Khu", bam nut nay go mot cau, va
    					// cau do khong ra kenh Khu.
    					God.ChatUI.getInstance().moGoChuTuNgoai();
    					SoundMn.gI().buttonClick();
    					Char.myCharz().currentMovePoint = null;
    					GameCanvas.clearAllPointerEvent();
    					return;
    				}
    			}
    			if (Char.myCharz().cmdMenu != null && GameCanvas.isPointerHoldIn(Char.myCharz().cmdMenu.x - 17, Char.myCharz().cmdMenu.y - 17, 34, 34))
    			{
    				mScreen.keyTouch = 20;
    				GameCanvas.isPointerJustDown = false;
    				isPointerDowning = false;
    				if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    				{
    					GameCanvas.clearAllPointerEvent();
    					Char.myCharz().cmdMenu.performAction();
    					return;
    				}
    			}
    			updateGamePad();
    			int[] oDauThan = oNutDauThan();
    			if (GameCanvas.isPointerHoldIn(oDauThan[0], oDauThan[1], oDauThan[2], oDauThan[3]) && Char.myCharz().statusMe != 14 && mobCapcha == null)
    			{
    				mScreen.keyTouch = 10;
    				GameCanvas.isPointerJustDown = false;
    				isPointerDowning = false;
    				if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    				{
    					GameCanvas.keyPressed[10] = true;
    					GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    				}
    			}
    			int[] oCapsule = oNutCapsule();
    			// Nhan ca o MOI cua nut Capsule lan o CU: o cu van la cho cua nut
    			// nhat Ngoc Rong, chi rieng Capsule doi cho.
    			if ((GameCanvas.isPointerHoldIn(oCapsule[0], oCapsule[1], oCapsule[2], oCapsule[3])
    					|| ((isAnalog != 0) ? GameCanvas.isPointerHoldIn(xHP + 5, yHP - 6 - 34 + 10, 34, 34) : GameCanvas.isPointerHoldIn(xHP + 5, yHP - 6 - 40 + 10, 40, 40)))
    					&& Char.myCharz().statusMe != 14 && mobCapcha == null)
    			{
    				if (isPickNgocRong)
    				{
    					mScreen.keyTouch = 14;
    					GameCanvas.isPointerJustDown = false;
    					isPointerDowning = false;
    					if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    					{
    						GameCanvas.keyPressed[14] = true;
    						GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    						isPickNgocRong = false;
    						Service.gI().useItem(-1, -1, -1, -1);
    					}
    				}
    				else if (isudungCapsun4)
    				{
    					mScreen.keyTouch = 14;
    					GameCanvas.isPointerJustDown = false;
    					isPointerDowning = false;
    					if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    					{
    						GameCanvas.keyPressed[14] = true;
    						GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    						for (int i = 0; i < Char.myCharz().arrItemBag.Length; i++)
    						{
    							Item item = Char.myCharz().arrItemBag[i];
    							if (item == null)
    							{
    								continue;
    							}
    							Res.err("find " + item.template.id);
    							if (item.template.id == 194)
    							{
    								isudungCapsun4 = item.quantity > 0;
    								if (isudungCapsun4)
    								{
    									Service.gI().useItem(0, 1, (sbyte)i, -1);
    									break;
    								}
    							}
    						}
    					}
    				}
    				else if (isudungCapsun3)
    				{
    					mScreen.keyTouch = 14;
    					GameCanvas.isPointerJustDown = false;
    					isPointerDowning = false;
    					if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    					{
    						GameCanvas.keyPressed[14] = true;
    						GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    						for (int j = 0; j < Char.myCharz().arrItemBag.Length; j++)
    						{
    							Item item2 = Char.myCharz().arrItemBag[j];
    							if (item2 != null && item2.template.id == 193)
    							{
    								isudungCapsun3 = ((item2.quantity > 0) ? true : false);
    								if (isudungCapsun3)
    								{
    									Service.gI().useItem(0, 1, (sbyte)j, -1);
    									break;
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    		if (mobCapcha != null)
    		{
    			updateKeyTouchCapcha();
    		}
    		else if (isHaveSelectSkill)
    		{
    			if (isCharging())
    			{
    				return;
    			}
    			if (GameCanvas.isTouch && !Main.isPC)
    			{
    				int[] oTG = oThuGonKyNang();
    				if (GameCanvas.isPointerHoldIn(oTG[0], oTG[1], oTG[2], oTG[3]))
    				{
    					if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    					{
    						thuGonKyNang = !thuGonKyNang;
    						Rms.saveRMSInt("thuGonKyNang", thuGonKyNang ? 1 : 0);
    						GameCanvas.clearAllPointerEvent();
    					}
    					return;
    				}
    			}
    			if (thuGonKyNang)
    			{
    				return;
    			}
    			keyTouchSkill = -1;
    			bool flag = false;
    			if (onScreenSkill.Length > 5 && (GameCanvas.isPointerHoldIn(xSkill + xS[0] - wSkill / 2 + 12, yS[0] - wSkill / 2 + 12, 5 * wSkill, wSkill) || GameCanvas.isPointerHoldIn(xSkill + xS[5] - wSkill / 2 + 12, yS[5] - wSkill / 2 + 12, 5 * wSkill, wSkill)))
    			{
    				flag = true;
    			}
    			if (flag || GameCanvas.isPointerHoldIn(xSkill + xS[0] - wSkill / 2 + 12, yS[0] - wSkill / 2 + 12, 5 * wSkill, wSkill) || (!GameCanvas.isTouchControl && GameCanvas.isPointerHoldIn(xSkill + xS[0] - wSkill / 2 + 12, yS[0] - wSkill / 2 + 12, wSkill, onScreenSkill.Length * wSkill)))
    			{
    				GameCanvas.isPointerJustDown = false;
    				isPointerDowning = false;
    				int num = (GameCanvas.pxLast - (xSkill + xS[0] - wSkill / 2 + 12)) / wSkill;
    				if (flag && GameCanvas.pyLast < yS[0])
    				{
    					num += 5;
    				}
    				keyTouchSkill = num;
    				if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    				{
    					GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    					selectedIndexSkill = num;
    					if (indexSelect < 0)
    					{
    						indexSelect = 0;
    					}
    					if (!Main.isPC)
    					{
    						if (selectedIndexSkill > onScreenSkill.Length - 1)
    						{
    							selectedIndexSkill = onScreenSkill.Length - 1;
    						}
    					}
    					else if (selectedIndexSkill > keySkill.Length - 1)
    					{
    						selectedIndexSkill = keySkill.Length - 1;
    					}
    					Skill skill = null;
    					skill = (Main.isPC ? keySkill[selectedIndexSkill] : onScreenSkill[selectedIndexSkill]);
    					if (skill != null)
    					{
    						doSelectSkill(skill, true);
    					}
    				}
    			}
    		}
    		if (GameCanvas.isPointerJustRelease)
    		{
    			if (GameCanvas.keyHold[1] || GameCanvas.keyHold[(!Main.isPC) ? 2 : 21] || GameCanvas.keyHold[3] || GameCanvas.keyHold[(!Main.isPC) ? 4 : 23] || GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
    			{
    				GameCanvas.isPointerJustRelease = false;
    			}
    			GameCanvas.keyHold[1] = false;
    			GameCanvas.keyHold[(!Main.isPC) ? 2 : 21] = false;
    			GameCanvas.keyHold[3] = false;
    			GameCanvas.keyHold[(!Main.isPC) ? 4 : 23] = false;
    			GameCanvas.keyHold[(!Main.isPC) ? 6 : 24] = false;
    		}
    	}
    
    	public void setCharJumpAtt()
    	{
    		Char.myCharz().cvy = -10;
    		Char.myCharz().statusMe = 3;
    		Char.myCharz().cp1 = 0;
    	}
    
    	public void setCharJump(int cvx)
    	{
    		if (Char.myCharz().cx - Char.myCharz().cxSend != 0 || Char.myCharz().cy - Char.myCharz().cySend != 0)
    		{
    			Service.gI().charMove();
    		}
    		Char.myCharz().cvy = -10;
    		Char.myCharz().cvx = cvx;
    		Char.myCharz().statusMe = 3;
    		Char.myCharz().cp1 = 0;
    	}
    
    	public void updateOpen()
    	{
    		if (isstarOpen)
    		{
    			if (moveUp > -3)
    			{
    				moveUp -= 4;
    			}
    			else
    			{
    				moveUp = -2;
    			}
    			if (moveDow < GameCanvas.h + 3)
    			{
    				moveDow += 4;
    			}
    			else
    			{
    				moveDow = GameCanvas.h + 2;
    			}
    			if (moveUp <= -2 && moveDow >= GameCanvas.h + 2)
    			{
    				isstarOpen = false;
    			}
    		}
    	}
    
    	public void initCreateCommand()
    	{
    	}
    
    	public void checkCharFocus()
    	{
    	}
    
    	public void updateXoSo()
    	{
    		if (tShow == 0)
    		{
    			return;
    		}
    		currXS = mSystem.currentTimeMillis();
    		if (currXS - lastXS > 1000)
    		{
    			lastXS = mSystem.currentTimeMillis();
    			secondXS++;
    		}
    		if (secondXS > 20)
    		{
    			for (int i = 0; i < winnumber.Length; i++)
    			{
    				randomNumber[i] = winnumber[i];
    			}
    			tShow--;
    			if (tShow == 0)
    			{
    				yourNumber = string.Empty;
    				info1.addInfo(strFinish, 0);
    				secondXS = 0;
    			}
    			return;
    		}
    		if (moveIndex > winnumber.Length - 1)
    		{
    			tShow--;
    			if (tShow == 0)
    			{
    				yourNumber = string.Empty;
    				info1.addInfo(strFinish, 0);
    			}
    			return;
    		}
    		if (moveIndex < randomNumber.Length)
    		{
    			if (tMove[moveIndex] == 15)
    			{
    				if (randomNumber[moveIndex] == winnumber[moveIndex] - 1)
    				{
    					delayMove[moveIndex] = 10;
    				}
    				if (randomNumber[moveIndex] == winnumber[moveIndex])
    				{
    					tMove[moveIndex] = -1;
    					moveIndex++;
    				}
    			}
    			else if (GameCanvas.gameTick % 5 == 0)
    			{
    				tMove[moveIndex]++;
    			}
    		}
    		for (int j = 0; j < winnumber.Length; j++)
    		{
    			if (tMove[j] == -1)
    			{
    				continue;
    			}
    			moveCount[j]++;
    			if (moveCount[j] > tMove[j] + delayMove[j])
    			{
    				moveCount[j] = 0;
    				randomNumber[j]++;
    				if (randomNumber[j] >= 10)
    				{
    					randomNumber[j] = 0;
    				}
    			}
    		}
    	}
    
    	public override void update()
        {
            ClientManager.getInstance().Update();
            chotAnRongThanNeuKet();
            if (GameCanvas.keyPressed[16])
    		{
    			GameCanvas.keyPressed[16] = false;
    			Char.myCharz().findNextFocusByKey();
    		}
    		if (GameCanvas.keyPressed[13] && !GameCanvas.panel.isShow)
    		{
    			GameCanvas.keyPressed[13] = false;
    			Char.myCharz().findNextFocusByKey();
    		}
    		if (GameCanvas.keyPressed[17])
    		{
    			GameCanvas.keyPressed[17] = false;
    			Char.myCharz().searchItem();
    			if (Char.myCharz().itemFocus != null)
    			{
    				pickItem();
    			}
    		}
    		if (GameCanvas.gameTick % 100 == 0 && TileMap.mapID == 137)
    		{
    			shock_scr = 30;
    		}
    		if (isAutoPlay && GameCanvas.gameTick % 20 == 0)
    		{
    			autoPlay();
    		}
    		updateXoSo();
    		mSystem.checkAdComlete();
    		SmallImage.update();
    		try
    		{
    			if (LoginScr.isContinueToLogin)
    			{
    				LoginScr.isContinueToLogin = false;
    			}
    			if (tickMove == 1)
    			{
    				lastTick = mSystem.currentTimeMillis();
    			}
    			if (tickMove == 100)
    			{
    				tickMove = 0;
    				currTick = mSystem.currentTimeMillis();
    				int second = (int)(currTick - lastTick) / 1000;
    				Service.gI().checkMMove(second);
    			}
    			if (lockTick > 0)
    			{
    				lockTick--;
    				if (lockTick == 0)
    				{
    					Controller.isStopReadMessage = false;
    				}
    			}
    			checkCharFocus();
    			GameCanvas.debug("E1", 0);
    			updateCamera();
    			GameCanvas.debug("E2", 0);
    			ChatTextField.gI().update();
    			GameCanvas.debug("E3", 0);
    			for (int i = 0; i < vCharInMap.size(); i++)
    			{
    				((Char)vCharInMap.elementAt(i)).update();
    			}
    			for (int i = 0; i < Teleport.vTeleport.size(); i++)
    			{
    				((Teleport)Teleport.vTeleport.elementAt(i)).update();
    			}
    			Char.myCharz().update();
    			if (Char.myCharz().statusMe == 1)
    			{
    			}
    			if (popUpYesNo != null)
    			{
    				popUpYesNo.update();
    			}
    			EffecMn.update();
    			GameCanvas.debug("E5x", 0);
    			for (int i = 0; i < vMob.size(); i++)
    			{
    				((Mob)vMob.elementAt(i)).update();
    			}
    			GameCanvas.debug("E6", 0);
    			for (int i = 0; i < vNpc.size(); i++)
    			{
    				((Npc)vNpc.elementAt(i)).update();
    			}
    			nSkill = onScreenSkill.Length;
    			for (int i = onScreenSkill.Length - 1; i >= 0; i--)
    			{
    				Skill skill = onScreenSkill[i];
    				if (skill != null)
    				{
    					nSkill = i + 1;
    					break;
    				}
    				nSkill--;
    			}
    			setSkillBarPosition();
    			GameCanvas.debug("E7", 0);
    			GameCanvas.gI().updateDust();
    			GameCanvas.debug("E8", 0);
    			updateFlyText();
    			PopUp.updateAll();
    			updateSplash();
    			updateSS();
    			GameCanvas.updateBG();
    			GameCanvas.debug("E9", 0);
    			updateClickToArrow();
    			GameCanvas.debug("E10", 0);
    			for (int i = 0; i < vItemMap.size(); i++)
    			{
    				((ItemMap)vItemMap.elementAt(i)).update();
    			}
    			GameCanvas.debug("E11", 0);
    			GameCanvas.debug("E13", 0);
    			for (int i = Effect2.vRemoveEffect2.size() - 1; i >= 0; i--)
    			{
    				Effect2.vEffect2.removeElement(Effect2.vRemoveEffect2.elementAt(i));
    				Effect2.vRemoveEffect2.removeElementAt(i);
    			}
    			for (int i = 0; i < Effect2.vEffect2.size(); i++)
    			{
    				Effect2 effect = (Effect2)Effect2.vEffect2.elementAt(i);
    				effect.update();
    			}
    			for (int i = 0; i < Effect2.vEffect2Outside.size(); i++)
    			{
    				Effect2 effect2 = (Effect2)Effect2.vEffect2Outside.elementAt(i);
    				effect2.update();
    			}
    			for (int i = 0; i < Effect2.vAnimateEffect.size(); i++)
    			{
    				Effect2 effect3 = (Effect2)Effect2.vAnimateEffect.elementAt(i);
    				effect3.update();
    			}
    			for (int i = 0; i < Effect2.vEffectFeet.size(); i++)
    			{
    				Effect2 effect4 = (Effect2)Effect2.vEffectFeet.elementAt(i);
    				effect4.update();
    			}
    			for (int i = 0; i < Effect2.vEffect3.size(); i++)
    			{
    				Effect2 effect5 = (Effect2)Effect2.vEffect3.elementAt(i);
    				effect5.update();
    			}
    			BackgroudEffect.updateEff();
    			info1.update();
    			info2.update();
    			GameCanvas.debug("E15", 0);
    			if (currentCharViewInfo != null && !currentCharViewInfo.Equals(Char.myCharz()))
    			{
    				currentCharViewInfo.update();
    			}
    			runArrow++;
    			if (runArrow > 3)
    			{
    				runArrow = 0;
    			}
    			if (isInjureHp)
    			{
    				twHp++;
    				if (twHp == 20)
    				{
    					twHp = 0;
    					isInjureHp = false;
    				}
    			}
    			else if (dHP > Char.myCharz().cHP)
    			{
    				double num = (dHP - Char.myCharz().cHP) / 2;
    				if (num < 1)
    				{
    					num = 1;
    				}
    				dHP -= num;
    			}
    			else
    			{
    				dHP = Char.myCharz().cHP;
    			}
    			if (isInjureMp)
    			{
    				twMp++;
    				if (twMp == 20)
    				{
    					twMp = 0;
    					isInjureMp = false;
    				}
    			}
    			else if (dMP > Char.myCharz().cMP)
    			{
    				int num2 = dMP - Char.myCharz().cMP >> 1;
    				if (num2 < 1)
    				{
    					num2 = 1;
    				}
    				dMP -= num2;
    			}
    			else
    			{
    				dMP = Char.myCharz().cMP;
    			}
    			if (tMenuDelay > 0)
    			{
    				tMenuDelay--;
    			}
    			if (isRongThanMenu())
    			{
    				int num3 = 100;
    				while (yR - num3 < cmy)
    				{
    					cmy--;
    				}
    			}
    			for (int i = 0; i < Char.vItemTime.size(); i++)
    			{
    				((ItemTime)Char.vItemTime.elementAt(i)).update();
    			}
    			for (int i = 0; i < textTime.size(); i++)
    			{
    				((ItemTime)textTime.elementAt(i)).update();
    			}
    			updateChatVip();
    		}
    		catch (Exception)
    		{
    		}
    		int num4 = GameCanvas.gameTick % 4000;
    		if (num4 == 1000)
    		{
    			checkRemoveImage();
    		}
    		EffectManager.update();
    	}
    
    	public void updateKeyChatPopUp()
    	{
    	}
    
    	public bool isRongThanMenu()
    	{
    		if (isMeCallRongThan)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void paintEffect(mGraphics g)
    	{
    		for (int i = 0; i < Effect2.vEffect2.size(); i++)
    		{
    			Effect2 effect = (Effect2)Effect2.vEffect2.elementAt(i);
    			if (effect != null && !(effect is ChatPopup))
    			{
    				effect.paint(g);
    			}
    		}
    		if (!GameCanvas.lowGraphic)
    		{
    			for (int i = 0; i < Effect2.vAnimateEffect.size(); i++)
    			{
    				Effect2 effect2 = (Effect2)Effect2.vAnimateEffect.elementAt(i);
    				effect2.paint(g);
    			}
    		}
    		for (int i = 0; i < Effect2.vEffect2Outside.size(); i++)
    		{
    			Effect2 effect3 = (Effect2)Effect2.vEffect2Outside.elementAt(i);
    			effect3.paint(g);
    		}
    	}
    
    	public void paintBgItem(mGraphics g, int layer)
    	{
    		for (int i = 0; i < TileMap.vCurrItem.size(); i++)
    		{
    			BgItem bgItem = (BgItem)TileMap.vCurrItem.elementAt(i);
    			if (bgItem.idImage != -1 && bgItem.layer == layer)
    			{
    				bgItem.paint(g);
    			}
    		}
    		if (TileMap.mapID == 48 && layer == 3 && GameCanvas.bgW != null && GameCanvas.bgW[0] != 0)
    		{
    			for (int j = 0; j < TileMap.pxw / GameCanvas.bgW[0] + 1; j++)
    			{
    				g.drawImage(GameCanvas.imgBG[0], j * GameCanvas.bgW[0], TileMap.pxh - GameCanvas.bgH[0] - 70, 0);
    			}
    		}
    	}
    
    	public void paintBlackSky(mGraphics g)
    	{
    		if (!GameCanvas.lowGraphic)
    		{
    			g.fillTrans(imgTrans, 0, 0, GameCanvas.w, GameCanvas.h);
    		}
    	}
    
    	public void paintCapcha(mGraphics g)
    	{
    		MobCapcha.paint(g, Char.myCharz().cx, Char.myCharz().cy);
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		if (GameCanvas.menu.showMenu || GameCanvas.panel.isShow || ChatPopup.currChatPopup != null || !GameCanvas.isTouch)
    		{
    			return;
    		}
    		for (int i = 0; i < strCapcha.Length; i++)
    		{
    			int x = (GameCanvas.w - strCapcha.Length * disXC) / 2 + i * disXC + disXC / 2;
    			if (keyCapcha[i] == -1)
    			{
    				g.drawImage(imgNut, x, GameCanvas.h - 25, 3);
    				mFont.tahoma_7b_dark.drawString(g, strCapcha[i] + string.Empty, x, GameCanvas.h - 30, 2);
    			}
    			else
    			{
    				g.drawImage(imgNutF, x, GameCanvas.h - 25, 3);
    				mFont.tahoma_7b_green2.drawString(g, strCapcha[i] + string.Empty, x, GameCanvas.h - 30, 2);
    			}
    		}
    	}
    
    	public override void paint(mGraphics g)
    	{
    		try { 
    		countEff = 0;
    		if (!isPaint)
    		{
    			return;
    		}
    		GameCanvas.debug("PA1", 1);
    		// !activeRongThan la luc dang AN rong: khong de mot hop thoai
    		// dang mo chan canh chop lai, khong thi rong treo mai.
    		if (isFreez || (isUseFreez && (ChatPopup.currChatPopup == null || !activeRongThan)))
    		{
    			dem++;
    			if ((dem < 30 && dem >= 0 && GameCanvas.gameTick % 4 == 0) || (dem >= 30 && dem <= 50 && GameCanvas.gameTick % 3 == 0) || dem > 50)
    			{
    				g.setColor(16777215);
    				g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    				if (dem <= 50)
    				{
    					return;
    				}
    				if (isUseFreez)
    				{
    					isUseFreez = false;
    					dem = 0;
    					if (activeRongThan)
    					{
    						callRongThan(xR, yR);
    					}
    					else
    					{
    						hideRongThan();
    					}
    				}
    				paintInfoBar(g);
    				g.translate(-cmx, -cmy);
    				g.translate(0, GameCanvas.transY);
    				Char.myCharz().paint(g);
    				mSystem.paintFlyText(g);
    				resetTranslate(g);
    				paintSelectedSkill(g);
    				return;
    			}
    		}
    		GameCanvas.debug("PA2", 1);
    		GameCanvas.paintBGGameScr(g);
    		paint_ios_bg(g);
    		if ((isRongThanXuatHien || isFireWorks) && TileMap.bgID != 3)
    		{
    			paintBlackSky(g);
    		}
    		GameCanvas.debug("PA3", 1);
    		if (shock_scr > 0)
    		{
    			g.translate(-cmx + shock_x[shock_scr % shock_x.Length], -cmy + shock_y[shock_scr % shock_y.Length]);
    			shock_scr--;
    		}
    		else
    		{
    			g.translate(-cmx, -cmy);
    		}
    		if (isSuperPower)
    		{
    			int tx = ((GameCanvas.gameTick % 3 != 0) ? (-3) : 3);
    			g.translate(tx, 0);
    		}
    		BackgroudEffect.paintBehindTileAll(g);
    		EffecMn.paintLayer1(g);
    		TileMap.paintTilemap(g);
    		TileMap.paintOutTilemap(g);
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char @char = (Char)vCharInMap.elementAt(i);
    			if (@char.isMabuHold && TileMap.mapID == 128)
    			{
    				@char.paintHeadWithXY(g, @char.cx, @char.cy, 0);
    			}
    		}
    		if (Char.myCharz().isMabuHold && TileMap.mapID == 128)
    		{
    			Char.myCharz().paintHeadWithXY(g, Char.myCharz().cx, Char.myCharz().cy, 0);
    		}
    		paintBgItem(g, 2);
    		if (Char.myCharz().cmdMenu != null && GameCanvas.isTouch)
    		{
    			if (mScreen.keyTouch == 20)
    			{
    				g.drawImage(imgChat2, Char.myCharz().cmdMenu.x + cmx, Char.myCharz().cmdMenu.y + cmy, mGraphics.HCENTER | mGraphics.VCENTER);
    			}
    			else
    			{
    				g.drawImage(imgChat, Char.myCharz().cmdMenu.x + cmx, Char.myCharz().cmdMenu.y + cmy, mGraphics.HCENTER | mGraphics.VCENTER);
    			}
    		}
    		GameCanvas.debug("PA4", 1);
    		GameCanvas.debug("PA5", 1);
    		BackgroudEffect.paintBackAll(g);
    		EffectManager.lowEffects.paintAll(g);
    		for (int i = 0; i < Effect2.vEffectFeet.size(); i++)
    		{
    			Effect2 effect = (Effect2)Effect2.vEffectFeet.elementAt(i);
    			effect.paint(g);
    		}
    		for (int i = 0; i < Teleport.vTeleport.size(); i++)
    		{
    			((Teleport)Teleport.vTeleport.elementAt(i)).paintHole(g);
    		}
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			Npc npc = (Npc)vNpc.elementAt(i);
    			if (npc.cHP > 0)
    			{
    				npc.paintShadow(g);
    			}
    		}
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			((Npc)vNpc.elementAt(i)).paint(g);
    		}
    		g.translate(0, GameCanvas.transY);
    		GameCanvas.debug("PA7", 1);
    		GameCanvas.debug("PA8", 1);
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char char2 = null;
    			try
    			{
    				char2 = (Char)vCharInMap.elementAt(i);
    			}
    			catch (Exception ex)
    			{
    				Cout.LogError("Loi ham paint char gamesc: " + ex.ToString());
    			}
    			if (char2 != null && (!GameCanvas.panel.isShow || !GameCanvas.panel.isTypeShop()) && char2.isShadown)
    			{
    				char2.paintShadow(g);
    			}
    		}
    		Char.myCharz().paintShadow(g);
    		EffecMn.paintLayer2(g);
    		for (int i = 0; i < vMob.size(); i++)
    		{
    			((Mob)vMob.elementAt(i)).paint(g);
    		}
    		for (int i = 0; i < Teleport.vTeleport.size(); i++)
    		{
    			((Teleport)Teleport.vTeleport.elementAt(i)).paint(g);
    		}
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char char3 = null;
    			try
    			{
    				char3 = (Char)vCharInMap.elementAt(i);
    			}
    			catch (Exception)
    			{
    			}
    			if (char3 != null && (!GameCanvas.panel.isShow || !GameCanvas.panel.isTypeShop()))
    			{
    				char3.paint(g);
    			}
    		}
    		Char.myCharz().paint(g);
    		if (Char.myCharz().skillPaint != null && Char.myCharz().skillInfoPaint() != null && Char.myCharz().indexSkill < Char.myCharz().skillInfoPaint().Length)
    		{
    			Char.myCharz().paintCharWithSkill(g);
    			Char.myCharz().paintMount2(g);
    		}
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char char4 = null;
    			try
    			{
    				char4 = (Char)vCharInMap.elementAt(i);
    			}
    			catch (Exception ex3)
    			{
    				Cout.LogError("Loi ham paint char gamescr: " + ex3.ToString());
    			}
    			if (char4 != null && (!GameCanvas.panel.isShow || !GameCanvas.panel.isTypeShop()) && char4.skillPaint != null && char4.skillInfoPaint() != null && char4.indexSkill < char4.skillInfoPaint().Length)
    			{
    				char4.paintCharWithSkill(g);
    				char4.paintMount2(g);
    			}
    		}
    		for (int i = 0; i < vItemMap.size(); i++)
    		{
    			((ItemMap)vItemMap.elementAt(i)).paint(g);
    		}
    		g.translate(0, -GameCanvas.transY);
    		GameCanvas.debug("PA9", 1);
    		paintSplash(g);
    		GameCanvas.debug("PA10", 1);
    		GameCanvas.debug("PA11", 1);
    		GameCanvas.debug("PA13", 1);
    		paintEffect(g);
    		paintBgItem(g, 3);
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			Npc npc2 = (Npc)vNpc.elementAt(i);
    			npc2.paintName(g);
    		}
    		EffecMn.paintLayer3(g);
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			Npc npc3 = (Npc)vNpc.elementAt(i);
    			if (npc3.chatInfo != null && npc3 != null)
    			{
    				npc3.chatInfo.paint(g, npc3.cx, npc3.cy - npc3.ch - GameCanvas.transY, npc3.cdir);
    			}
    		}
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char char5 = null;
    			try
    			{
    				char5 = (Char)vCharInMap.elementAt(i);
    			}
    			catch (Exception)
    			{
    			}
    			if (char5 != null && char5.chatInfo != null)
    			{
    				char5.chatInfo.paint(g, char5.cx, char5.cy - char5.ch, char5.cdir);
    			}
    		}
    		if (Char.myCharz().chatInfo != null)
    		{
    			Char.myCharz().chatInfo.paint(g, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch, Char.myCharz().cdir);
    		}
    		EffectManager.mid_2Effects.paintAll(g);
    		EffectManager.midEffects.paintAll(g);
    		BackgroudEffect.paintFrontAll(g);
    		for (int j = 0; j < TileMap.vCurrItem.size(); j++)
    		{
    			BgItem bgItem = (BgItem)TileMap.vCurrItem.elementAt(j);
    			if (bgItem.idImage != -1 && bgItem.layer > 3)
    			{
    				bgItem.paint(g);
    			}
    		}
    		PopUp.paintAll(g);
    		if (TileMap.mapID == 120)
    		{
    			if (percentMabu != 100)
    			{
    				int w = percentMabu * mGraphics.getImageWidth(imgHPLost) / 100;
    				int num = percentMabu;
    				g.drawImage(imgHPLost, TileMap.pxw / 2 - mGraphics.getImageWidth(imgHPLost) / 2, 220, 0);
    				g.setClip(TileMap.pxw / 2 - mGraphics.getImageWidth(imgHPLost) / 2, 220, w, 10);
    				g.drawImage(imgHP, TileMap.pxw / 2 - mGraphics.getImageWidth(imgHPLost) / 2, 220, 0);
    				g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			}
    			if (mabuEff)
    			{
    				tMabuEff++;
    				if (GameCanvas.gameTick % 3 == 0)
    				{
    					Effect me = new Effect(19, Res.random(TileMap.pxw / 2 - 50, TileMap.pxw / 2 + 50), 340, 2, 1, -1);
    					EffecMn.addEff(me);
    				}
    				if (GameCanvas.gameTick % 15 == 0)
    				{
    					Effect me2 = new Effect(18, Res.random(TileMap.pxw / 2 - 5, TileMap.pxw / 2 + 5), Res.random(300, 320), 2, 1, -1);
    					EffecMn.addEff(me2);
    				}
    				if (tMabuEff == 100)
    				{
    					activeSuperPower(TileMap.pxw / 2, 300);
    				}
    				if (tMabuEff == 110)
    				{
    					tMabuEff = 0;
    					mabuEff = false;
    				}
    			}
    		}
    		BackgroudEffect.paintFog(g);
    		bool flag = true;
    		for (int i = 0; i < BackgroudEffect.vBgEffect.size(); i++)
    		{
    			BackgroudEffect backgroudEffect = (BackgroudEffect)BackgroudEffect.vBgEffect.elementAt(i);
    			if (backgroudEffect.typeEff == 0)
    			{
    				flag = false;
    				break;
    			}
    		}
    		if (mGraphics.zoomLevel <= 1 || Main.isIpod || Main.isIphone4)
    		{
    			flag = false;
    		}
    		if (flag && !isRongThanXuatHien)
    		{
    			int num2 = TileMap.pxw / (mGraphics.getImageWidth(TileMap.imgLight) + 50);
    			if (num2 <= 0)
    			{
    				num2 = 1;
    			}
    			if (TileMap.tileID != 28)
    			{
    				for (int i = 0; i < num2; i++)
    				{
    					int num3 = 100 + i * (mGraphics.getImageWidth(TileMap.imgLight) + 50) - cmx / 2;
    					int num4 = -20;
    					int imageWidth = mGraphics.getImageWidth(TileMap.imgLight);
    					if (num3 + imageWidth >= cmx && num3 <= cmx + GameCanvas.w && num4 + mGraphics.getImageHeight(TileMap.imgLight) >= cmy && num4 <= cmy + GameCanvas.h)
    					{
    						g.drawImage(TileMap.imgLight, 100 + i * (mGraphics.getImageWidth(TileMap.imgLight) + 50) - cmx / 2, num4, 0);
    					}
    				}
    			}
    		}
    		mSystem.paintFlyText(g);
    		GameCanvas.debug("PA14", 1);
    		GameCanvas.debug("PA15", 1);
    		GameCanvas.debug("PA16", 1);
    		paintArrowPointToNPC(g);
    		GameCanvas.debug("PA17", 1);
    		if (!isPaintOther && isPaintRada == 1 && !GameCanvas.panel.isShow)
    		{
    			paintInfoBar(g);
    		}
    		resetTranslate(g);
    		paint_xp_bar(g);
    		if (!isPaintOther)
    		{
    			if (GameCanvas.open3Hour && TileMap.mapID != 170)
    			{
    				if (GameCanvas.w > 250)
    				{
    					g.drawImage(GameCanvas.img12, 160, 6, 0);
    					mFont.tahoma_7_white.drawString(g, "Dành cho người chơi trên 12 tuổi.", 180, 2, 0);
    					mFont.tahoma_7_white.drawString(g, "Chơi quá 180 phút mỗi ngày ", 180, 12, 0);
    					mFont.tahoma_7_white.drawString(g, "sẽ hại sức khỏe.", 180, 22, 0);
    				}
    				else
    				{
    					g.drawImage(GameCanvas.img12, 5, GameCanvas.h - 67, 0);
    					mFont.tahoma_7_white.drawString(g, "Dành cho người chơi trên 12 tuổi.", 25, GameCanvas.h - 70, 0);
    					mFont.tahoma_7_white.drawString(g, "Chơi quá 180 phút mỗi ngày sẽ hại sức khỏe.", 25, GameCanvas.h - 60, 0);
    				}
    			}
    			GameCanvas.debug("PA21", 1);
    			GameCanvas.debug("PA18", 1);
    			g.translate(-g.getTranslateX(), -g.getTranslateY());
    			if ((TileMap.mapID == 128 || TileMap.mapID == 127) && mabuPercent != 0)
    			{
    				int num5 = 30;
    				int num6 = 200;
    				g.setColor(0);
    				g.fillRect(num5 - 27, num6 - 112, 54, 8);
    				g.setColor(16711680);
    				g.setClip(num5 - 25, num6 - 110, mabuPercent, 4);
    				g.fillRect(num5 - 25, num6 - 110, 50, 4);
    				g.setClip(0, 0, 3000, 3000);
    				mFont.tahoma_7b_white.drawString(g, "Mabu", num5, num6 - 112 + 10, 2, mFont.tahoma_7b_dark);
    			}
    			if (Char.myCharz().isFusion)
    			{
    				Char.myCharz().tFusion++;
    				if (GameCanvas.gameTick % 3 == 0)
    				{
    					g.setColor(16777215);
    					g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    				}
    				if (Char.myCharz().tFusion >= 100)
    				{
    					Char.myCharz().fusionComplete();
    				}
    			}
    			for (int i = 0; i < vCharInMap.size(); i++)
    			{
    				Char char6 = null;
    				try
    				{
    					char6 = (Char)vCharInMap.elementAt(i);
    				}
    				catch (Exception)
    				{
    				}
    				if (char6 != null && char6.isFusion && Char.isCharInScreen(char6))
    				{
    					char6.tFusion++;
    					if (GameCanvas.gameTick % 3 == 0)
    					{
    						g.setColor(16777215);
    						g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    					}
    					if (char6.tFusion >= 100)
    					{
    						char6.fusionComplete();
    					}
    				}
    			}
    			GameCanvas.paintz.paintTabSoft(g);
    			GameCanvas.debug("PA19", 1);
    			GameCanvas.debug("PA20", 1);
    			resetTranslate(g);
    			paintSelectedSkill(g);
    			GameCanvas.debug("PA22", 1);
    			resetTranslate(g);
    			if (GameCanvas.isTouch && GameCanvas.isTouchControl)
    			{
    				paintTouchControl(g);
    			}
    			resetTranslate(g);
    			paintChatVip(g);
    			if (!GameCanvas.panel.isShow && GameCanvas.currentDialog == null && ChatPopup.currChatPopup == null && ChatPopup.serverChatPopUp == null && GameCanvas.currentScreen.Equals(instance))
    			{
    				base.paint(g);
    				// Bo vet loe cua nut tam giac: nut do khong con duoc ve nua
    				// (xem Char.cs, cho gan `left`), ve vet loe o toa do cu thi
    				// thanh mot dom sang lo lung khong gan voi cai gi.
    			}
    			resetTranslate(g);
    			int num7 = 100 + ((Char.vItemTime.size() != 0) ? (textTime.size() * 12) : 0);
    			if (Char.myCharz().clan != null)
    			{
    				int num8 = 0;
    				int num9 = 0;
    				int num10 = (GameCanvas.h - 100 - 60) / 12;
    				for (int i = 0; i < vCharInMap.size(); i++)
    				{
    					Char char7 = (Char)vCharInMap.elementAt(i);
    					if (char7.clanID == -1 || char7.clanID != Char.myCharz().clan.ID)
    					{
    						continue;
    					}
    					if (char7.isOutX() && char7.cx < Char.myCharz().cx)
    					{
    						int num11 = num10;
    						if (Char.vItemTime.size() != 0)
    						{
    							num11 -= textTime.size();
    						}
    						if (num8 <= num11)
    						{
    							mFont.tahoma_7_green.drawString(g, char7.cName, 20, num7 - 12 + num8 * 12, mFont.LEFT, mFont.tahoma_7_grey);
    							char7.paintHp(g, 10, num7 + num8 * 12 - 5);
    							num8++;
    						}
    					}
    					else if (char7.isOutX() && char7.cx > Char.myCharz().cx && num9 <= num10)
    					{
    						mFont.tahoma_7_green.drawString(g, char7.cName, GameCanvas.w - 25, num7 - 12 + num9 * 12, mFont.RIGHT, mFont.tahoma_7_grey);
    						char7.paintHp(g, GameCanvas.w - 15, num7 + num9 * 12 - 5);
    						num9++;
    					}
    				}
    			}
    			ChatTextField.gI().paint(g);
    			// Co nguoi xin vao bang: nhay o nut BA GACH.
    			//
    			// Duong di toi cho xu ly la ba gach -> Nhan vat -> the Bang hoi, nen
    			// dau hieu phai bat dau tu ba gach. Ve mot cham do nho o goc nut,
    			// cung kieu voi cham bao cua cac man khac, thay cho anh loe cu.
    			if (isNewClanMessage && !GameCanvas.panel.isShow
    					&& GameCanvas.gameTick % 8 < 5)
    			{
    				g.setColor(0xFF4A3C, 1f);
    				g.fillRect(cmdMenu.x + 20, cmdMenu.y - 2, 6, 6, 3);
    			}
    			if (isSuperPower)
    			{
    				dxPower += 5;
    				if (tPower >= 0)
    				{
    					tPower += dxPower;
    				}
    				Res.outz("x power= " + xPower);
    				if (tPower < 0)
    				{
    					tPower--;
    					if (tPower == -20)
    					{
    						isSuperPower = false;
    						tPower = 0;
    						dxPower = 0;
    					}
    				}
    				else if ((xPower - tPower > 0 || tPower < TileMap.pxw) && tPower > 0)
    				{
    					g.setColor(16777215);
    					if (!GameCanvas.lowGraphic)
    					{
    						g.fillArg(0, 0, GameCanvas.w, GameCanvas.h, 0, 0);
    					}
    					else
    					{
    						g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    					}
    				}
    				else
    				{
    					tPower = -1;
    				}
    			}
    			// Hieu ung dem gio (cuong no, an chay...) xep vao COT TRAI.
    			//
    			// Toa do cu bam theo cmdMenu.x va hai so cung 55 / 45 / 90, nen
    			// hang icon nam de len khung nhan vat va bang ten ban do. Xin cho
    			// qua cot thi no luon nam ngay duoi thu ve truoc no.
    			// Hieu ung dem gio nam BEN PHAI bang ten ban do, khong con xen vao
    			// cot cua bang Nhiem vu: hai thu khong lien quan gi nhau, xep
    			// chung mot cot thi bat mot hieu ung la ca bang nhiem vu tut xuong.
    			for (int i = 0; i < Char.vItemTime.size(); i++)
    			{
    				// Nang len cho chu gio nam GON TRONG dai bang ten ban do.
    				//
    				// ItemTime.paint ve so gio o y + 15. De y = +15 thi so gio roi
    				// xuong +30, tuc ngay duoi day bang — va de len dai tieu de
    				// cua bang Nhiem vu ngay ben duoi.
    				((ItemTime)Char.vItemTime.elementAt(i)).paint(g,
    						xPhaiBangBanDo + 14 + i * 24, yBangBanDo + 9);
    			}
    			for (int i = 0; i < textTime.size(); i++)
    			{
    				((ItemTime)textTime.elementAt(i)).paintText(g,
    						xPhaiBangBanDo + 4, yBangBanDo + 32 + i * 12);
    			}
    			paintXoSo(g);
    			if (mResources.language == 1)
    			{
    				long second = mSystem.currentTimeMillis() - deltaTime;
    				mFont.tahoma_7b_white.drawString(g, NinjaUtil.getDate2(second), 10, GameCanvas.h - 65, 0, mFont.tahoma_7b_dark);
    			}
    			if (!yourNumber.Equals(string.Empty))
    			{
    				for (int i = 0; i < strPaint.Length; i++)
    				{
    					mFont.tahoma_7b_white.drawString(g, strPaint[i], 5, 85 + i * 18, 0, mFont.tahoma_7b_dark);
    				}
    			}
    		}
    		int num12 = 0;
    		int num13 = GameCanvas.hw;
    		if (num13 > 200)
    		{
    			num13 = 200;
    		}
    		paintPhuBanBar(g, num12 + GameCanvas.w / 2, 0, num13);
    		EffectManager.hiEffects.paintAll(g);
    		if (nCT_timeBallte > mSystem.currentTimeMillis() && TileMap.mapID == 170 && isPaint_CT && nCT_nBoyBaller / 2 > 0)
    		{
    			try
    			{
    				paint_CT(g, num12 + GameCanvas.w / 2, 0, num13);
    			}
    			catch (Exception)
    			{
    			}
    		}
    		if (TileMap.mapID == 172)
    		{
    			string text = mResources.WAIT + "  " + nUSER_CT + "/" + nUSER_MAX_CT;
    			mFont.tahoma_7b_dark.drawString(g, mResources.WAIT + "  " + nUSER_CT + "/" + nUSER_MAX_CT, GameCanvas.w - 10, 40, 1);
    		}
            ClientManager.getInstance().GUI(g);
        }
    		catch(Exception e)
    		{
    			Debug.LogException(e);
    		}
    		}
    	private void paintXoSo(mGraphics g)
    	{
    		if (tShow != 0)
    		{
    			string text = string.Empty;
    			for (int i = 0; i < winnumber.Length; i++)
    			{
    				text = text + randomNumber[i] + " ";
    			}
    			PopUp.paintPopUp(g, 20, 45, 95, 35, 16777215, false);
    			mFont.tahoma_7b_dark.drawString(g, mResources.kquaVongQuay, 68, 50, 2);
    			mFont.tahoma_7b_dark.drawString(g, text + string.Empty, 68, 65, 2);
    		}
    	}
    
    	private void checkEffToObj(IMapObject obj, bool isnew)
    	{
    		if (obj == null || tDoubleDelay > 0)
    		{
    			return;
    		}
    		tDoubleDelay = 10;
    		int x = obj.getX();
    		int num = 1;
    		int num2 = Res.abs(Char.myCharz().cx - x);
    		num = ((num2 <= 80) ? 1 : ((num2 > 80 && num2 <= 200) ? 2 : ((num2 <= 200 || num2 > 400) ? 4 : 3)));
    		if (!isnew)
    		{
    			if (obj.Equals(Char.myCharz().mobFocus) || (obj.Equals(Char.myCharz().charFocus) && Char.myCharz().isMeCanAttackOtherPlayer(Char.myCharz().charFocus)))
    			{
    				ServerEffect.addServerEffect(135, obj.getX(), obj.getY(), num);
    			}
    			else if (obj.Equals(Char.myCharz().npcFocus) || obj.Equals(Char.myCharz().itemFocus) || obj.Equals(Char.myCharz().charFocus))
    			{
    				ServerEffect.addServerEffect(136, obj.getX(), obj.getY(), num);
    			}
    		}
    		else
    		{
    			ServerEffect.addServerEffect(136, obj.getX(), obj.getY(), num);
    		}
    	}
    
    	private void updateClickToArrow()
    	{
    		if (tDoubleDelay > 0)
    		{
    			tDoubleDelay--;
    		}
    		if (clickMoving)
    		{
    			clickMoving = false;
    			IMapObject mapObject = findClickToItem(clickToX, clickToY);
    			if (mapObject == null || (mapObject != null && mapObject.Equals(Char.myCharz().npcFocus) && TileMap.mapID == 51))
    			{
    				ServerEffect.addServerEffect(134, clickToX, clickToY + GameCanvas.transY / 2, 3);
    			}
    		}
    	}
    
    	private void paintWaypointArrow(mGraphics g)
    	{
    		int num = 10;
    		Task taskMaint = Char.myCharz().taskMaint;
    		if (taskMaint != null && taskMaint.taskId == 0 && ((taskMaint.index != 1 && taskMaint.index < 6) || taskMaint.index == 0))
    		{
    			return;
    		}
    		for (int i = 0; i < TileMap.vGo.size(); i++)
    		{
    			Waypoint waypoint = (Waypoint)TileMap.vGo.elementAt(i);
    			if (waypoint.minY == 0 || waypoint.maxY >= TileMap.pxh - 24)
    			{
    				if (waypoint.maxY <= TileMap.pxh / 2)
    				{
    					int x = waypoint.minX + (waypoint.maxX - waypoint.minX) / 2;
    					int y = waypoint.minY + (waypoint.maxY - waypoint.minY) / 2 + runArrow;
    					if (GameCanvas.isTouch)
    					{
    						y = waypoint.maxY + (waypoint.maxY - waypoint.minY) + runArrow + num;
    					}
    					g.drawRegion(arrow, 0, 0, 13, 16, 6, x, y, StaticObj.VCENTER_HCENTER);
    				}
    				else if (waypoint.minY >= TileMap.pxh / 2)
    				{
    					g.drawRegion(arrow, 0, 0, 13, 16, 4, waypoint.minX + (waypoint.maxX - waypoint.minX) / 2, waypoint.minY - 12 - runArrow, StaticObj.VCENTER_HCENTER);
    				}
    			}
    			else if (waypoint.minX >= 0 && waypoint.minX < 24)
    			{
    				if (!GameCanvas.isTouch)
    				{
    					g.drawRegion(arrow, 0, 0, 13, 16, 2, waypoint.maxX + 12 + runArrow, waypoint.maxY - 12, StaticObj.VCENTER_HCENTER);
    				}
    				else
    				{
    					g.drawRegion(arrow, 0, 0, 13, 16, 2, waypoint.maxX + 12 + runArrow, waypoint.maxY - 32, StaticObj.VCENTER_HCENTER);
    				}
    			}
    			else if (waypoint.minX <= TileMap.tmw * 24 && waypoint.minX >= TileMap.tmw * 24 - 48)
    			{
    				if (!GameCanvas.isTouch)
    				{
    					g.drawRegion(arrow, 0, 0, 13, 16, 0, waypoint.minX - 12 - runArrow, waypoint.maxY - 12, StaticObj.VCENTER_HCENTER);
    				}
    				else
    				{
    					g.drawRegion(arrow, 0, 0, 13, 16, 0, waypoint.minX - 12 - runArrow, waypoint.maxY - 32, StaticObj.VCENTER_HCENTER);
    				}
    			}
    			else
    			{
    				g.drawRegion(arrow, 0, 0, 13, 16, 4, waypoint.minX + (waypoint.maxX - waypoint.minX) / 2, waypoint.maxY - 48 - runArrow, StaticObj.VCENTER_HCENTER);
    			}
    		}
    	}
    
    	public static Npc findNPCInMap(short id)
    	{
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			Npc npc = (Npc)vNpc.elementAt(i);
    			if (npc.template.npcTemplateId == id)
    			{
    				return npc;
    			}
    		}
    		return null;
    	}
    
    	public static Char findCharInMap(int charId)
    	{
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char @char = (Char)vCharInMap.elementAt(i);
    			if (@char.charID == charId)
    			{
    				return @char;
    			}
    		}
    		return null;
    	}
    
    	public static Mob findMobInMap(sbyte mobIndex)
    	{
    		return (Mob)vMob.elementAt(mobIndex);
    	}
    
    	public static Mob findMobInMap(int mobId)
    	{
    		for (int i = 0; i < vMob.size(); i++)
    		{
    			Mob mob = (Mob)vMob.elementAt(i);
    			if (mob.mobId == mobId)
    			{
    				return mob;
    			}
    		}
    		return null;
    	}
    
    	public static Npc getNpcTask()
    	{
    		for (int i = 0; i < vNpc.size(); i++)
    		{
    			Npc npc = (Npc)vNpc.elementAt(i);
    			if (npc.template.npcTemplateId == getTaskNpcId())
    			{
    				return npc;
    			}
    		}
    		return null;
    	}
    
    	private void paintArrowPointToNPC(mGraphics g)
    	{
    		try
    		{
    			if (ChatPopup.currChatPopup != null)
    			{
    				return;
    			}
    			int num = getTaskNpcId();
    			if (num == -1)
    			{
    				return;
    			}
    			Npc npc = null;
    			for (int i = 0; i < vNpc.size(); i++)
    			{
    				Npc npc2 = (Npc)vNpc.elementAt(i);
    				if (npc2.template.npcTemplateId == num)
    				{
    					if (npc == null)
    					{
    						npc = npc2;
    					}
    					else if (Res.abs(npc2.cx - Char.myCharz().cx) < Res.abs(npc.cx - Char.myCharz().cx))
    					{
    						npc = npc2;
    					}
    				}
    			}
    			if (npc == null || npc.statusMe == 15 || (npc.cx > cmx && npc.cx < cmx + gW && npc.cy > cmy && npc.cy < cmy + gH) || GameCanvas.gameTick % 10 < 5)
    			{
    				return;
    			}
    			int num2 = npc.cx - Char.myCharz().cx;
    			int num3 = npc.cy - Char.myCharz().cy;
    			int x = 0;
    			int y = 0;
    			int arg = 0;
    			if (num2 > 0 && num3 >= 0)
    			{
    				if (Res.abs(num2) >= Res.abs(num3))
    				{
    					x = gW - 10;
    					y = gH / 2 + 30;
    					if (GameCanvas.isTouch)
    					{
    						y = gH / 2 + 10;
    					}
    					arg = 0;
    				}
    				else
    				{
    					x = gW / 2;
    					y = gH - 10;
    					arg = 5;
    				}
    			}
    			else if (num2 >= 0 && num3 < 0)
    			{
    				if (Res.abs(num2) >= Res.abs(num3))
    				{
    					x = gW - 10;
    					y = gH / 2 + 30;
    					if (GameCanvas.isTouch)
    					{
    						y = gH / 2 + 10;
    					}
    					arg = 0;
    				}
    				else
    				{
    					x = gW / 2;
    					y = 10;
    					arg = 6;
    				}
    			}
    			if (num2 < 0 && num3 >= 0)
    			{
    				if (Res.abs(num2) >= Res.abs(num3))
    				{
    					x = 10;
    					y = gH / 2 + 30;
    					if (GameCanvas.isTouch)
    					{
    						y = gH / 2 + 10;
    					}
    					arg = 3;
    				}
    				else
    				{
    					x = gW / 2;
    					y = gH - 10;
    					arg = 5;
    				}
    			}
    			else if (num2 <= 0 && num3 < 0)
    			{
    				if (Res.abs(num2) >= Res.abs(num3))
    				{
    					x = 10;
    					y = gH / 2 + 30;
    					if (GameCanvas.isTouch)
    					{
    						y = gH / 2 + 10;
    					}
    					arg = 3;
    				}
    				else
    				{
    					x = gW / 2;
    					y = 10;
    					arg = 6;
    				}
    			}
    			resetTranslate(g);
    			g.drawRegion(arrow, 0, 0, 13, 16, arg, x, y, StaticObj.VCENTER_HCENTER);
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi ham arrow to npc: " + ex.ToString());
    		}
    	}
    
    	public static void resetTranslate(mGraphics g)
    	{
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		g.setClip(0, -200, GameCanvas.w, 200 + GameCanvas.h);
    	}
    
    	/// <summary>
    	/// Khung mot o ky nang, ve bang ma thay cho anh imgSkill/imgSkill2.
    	/// </summary>
    	/// <remarks>
    	/// <para>Ve bang ma vi mot bo anh khung phai co bon ban cho bon muc zoom
    	/// (x1..x4), va doi mau thi phai xuat lai anh. Ve bang ma thi net o moi
    	/// muc zoom va doi mau chi la doi mot con so.</para>
    	///
    	/// <para>Vien hai lop: lop ngoai toi de tach o khoi nen map (nen map co
    	/// the sang hay toi tuy hanh tinh), lop trong sang de o co khoi. O dang
    	/// chon doi lop trong sang mau cam va day gap doi.</para>
    	/// </remarks>
    	/// <param name="x">goc tren trai cua khung</param>
    	/// <param name="canh">be rong = be cao cua khung</param>
    	/// <param name="dangChon">o dang duoc chon</param>
    	/// <summary>
    	/// Cạnh một ô kỹ năng — <b>co theo bước ngang của hàng ô</b>.
    	/// </summary>
    	/// <remarks>
    	/// <para>Trước đây cạnh ô là số 30 viết thẳng ở năm chỗ vẽ khác nhau, trong
    	/// khi bước ngang <c>wSkill</c> lại là một biến. Thu hàng ô nhỏ lại thì các
    	/// ô chồng mép lên nhau, vì cái khung không hề biết bước đã đổi.</para>
    	///
    	/// <para>Sàn 26: biểu tượng kỹ năng là ảnh bitmap cố định 24 điểm vẽ bằng
    	/// <c>drawRegion</c> — hàm đó không thu nhỏ được, nên ô hẹp hơn nữa là biểu
    	/// tượng thò ra ngoài vành.</para>
    	/// </remarks>
    	public static int canhOKyNang()
    	{
    		int c = wSkill - 2;
    		if (c > 30)
    		{
    			c = 30;
    		}
    		if (c < 26)
    		{
    			c = 26;
    		}
    		return c;
    	}

    	/// <summary>Tâm của một ô kỹ năng, tính từ góc trên trái của bước ô.</summary>
    	/// <remarks>
    	/// Khung đặt tại <c>(xS - 1, yS - 1)</c> nên tâm nó ở <c>cạnh / 2 - 1</c>.
    	/// Bản trước ghi thẳng số 14 (đúng với cạnh 30); đổi cạnh mà quên chỗ này
    	/// thì biểu tượng lệch khỏi khung.
    	/// </remarks>
    	public static int tamOKyNang()
    	{
    		return canhOKyNang() / 2 - 1;
    	}

    	public static void veKhungKyNang(mGraphics g, int x, int y, int canh,
    			bool dangChon)
    	{
    		// 1. Quang sang toa ra khi o dang duoc chon.
    		//
    		// Bon vong mo dan tu trong ra chu khong mot mang mau phang: mang phang
    		// co mot duong bien ro, nhin nhu dan them mot mieng giay quanh o.
    		if (dangChon)
    		{
    			for (int i = 5; i >= 1; i--)
    			{
    				g.setColor(MAU_LOE_O, 0.09f * (6 - i));
    				g.fillRect(x - i, y - i, canh + i * 2, canh + i * 2, BO_O + i);
    			}
    		}

    		// Da bo bong do den phia sau o.
    		//
    		// Bong lech xuong duoi ba diem, ma o ky nang xep lien nhau thanh mot
    		// hang — nen bong cua o nay do len o ben canh, doc ra thanh mot vet den
    		// chay suot day man hinh chu khong ra "o noi len khoi nen". Vanh ngoai
    		// gan den o buoc 3 da du tach o khoi moi nen.

    		// 3. Vanh ngoai toi: duong vien cung cua ca o, tach no khoi moi nen.
    		g.setColor(0x120C07, 1f);
    		g.fillRect(x, y, canh, canh, BO_O);

    		// 4. Vanh vang chuyen mau — sang o tren, tram o duoi.
    		//
    		// Mot vanh mot mau nhin bet nhu dan hinh; chuyen mau doc lam canh tren
    		// bat sang va canh duoi chim xuong, thanh ra o co do day.
    		veDaiDoc(g, x + 1, y + 1, canh - 2, canh - 2, BO_O - 1,
    				dangChon ? 0xFFEFC4 : 0xD9A96A,
    				dangChon ? 0xC98A2E : 0x6E4A22);

    		// 5. Long o: toi va chuyen mau, cho icon ky nang phia tren noi han len.
    		//
    		// Do bo chi 3 trong khi vanh ngoai bo 8. Long o rong 26 ma icon ky nang
    		// 24, chi con 1px le moi ben, nen bo goc lon la duong cong an vao bon
    		// goc vuong cua icon. Khong the cat bitmap thanh hinh bo goc duoc:
    		// `drawRegion` — duong ma icon ky nang di qua — khong doc clip.
    		int day = dangChon ? 3 : 2;
    		int xt = x + day;
    		int yt = y + day;
    		int ct = canh - day * 2;
    		// Long o sang, cung bo mau voi hai nut nhanh — ca dai HUD duoi day
    		// man hinh doc ra la mot bo. De gan den nhu truoc thi o TRONG nhin
    		// nhu mot cai lo thung tren nen ban do, con o co icon thi icon mau
    		// tram bi chim vao nen.
    		veDaiDoc(g, xt, yt, ct, ct, 3,
    				dangChon ? 0xB08A58 : 0x8A6A42,
    				dangChon ? 0x70522C : 0x4E3A22);

    		// 6. Vet loang o nua tren: anh sang tu tren roi xuong nen nua tren cua
    		// long o bat sang, nua duoi chim.
    		g.setColor(0xFFFFFF, 0.09f);
    		g.fillRect(xt, yt, ct, ct / 2, 3);
    		// Hat sang o day o — anh sang doi tu vanh vang duoi hat nguoc len.
    		g.setColor(0xFFD98A, 0.14f);
    		g.fillRect(xt + 2, yt + ct - 1, ct - 4, 1);

    		// 7. Bon dinh tan o bon goc: chi tiet nho lam o ra chat tam kim loai
    		// dong dinh chu khong phai mot o mau.
    		if (canh >= 26)
    		{
    			g.setColor(0xFFE9A3, dangChon ? 0.85f : 0.55f);
    			int d2 = day + 2;
    			g.fillRect(x + d2, y + d2, 2, 2, 1);
    			g.fillRect(x + canh - d2 - 2, y + d2, 2, 2, 1);
    			g.fillRect(x + d2, y + canh - d2 - 2, 2, 2, 1);
    			g.fillRect(x + canh - d2 - 2, y + canh - d2 - 2, 2, 2, 1);
    		}
    	}

    	/// <summary>
    	/// Nút tròn của hàng HUD: vành vàng chuyển màu, lòng lõm, vệt loáng.
    	/// </summary>
    	/// <remarks>
    	/// Để ở đây chứ không ở <c>ClientManager</c> vì cả nút đậu thần lẫn mấy nút
    	/// điều khiển đều dùng chung một hình nền này — hai bản vẽ riêng thì chỉ cần
    	/// sửa một bên là hai nút cạnh nhau lệch nhau ngay.
    	/// </remarks>
    	public static void veNutTronHud(mGraphics g, int tamX, int tamY, int co,
    			bool dangBam)
    	{
    		int x = tamX - co / 2;
    		int y = tamY - co / 2;
    		int r = co / 2;

    		if (dangBam)
    		{
    			for (int i = 5; i >= 1; i--)
    			{
    				g.setColor(MAU_LOE_O, 0.09f * (6 - i));
    				g.fillRect(x - i, y - i, co + i * 2, co + i * 2, r + i);
    			}
    		}
    		// Da bo bong do den phia sau nut tron — cung ly do voi o ky
    		// nang: bong lech xuong duoi doc ra thanh mot vet den chu khong
    		// ra khoi noi len.
    		g.setColor(0x120C07, 1f);
    		g.fillRect(x, y, co, co, r);
    		veDaiDoc(g, x + 1, y + 1, co - 2, co - 2, r - 1,
    				dangBam ? 0xFFEFC4 : 0xD9A96A,
    				dangBam ? 0xC98A2E : 0x6E4A22);
    		int day = 3;
    		veDaiDoc(g, x + day, y + day, co - day * 2, co - day * 2,
    				(co - day * 2) / 2,
    				dangBam ? 0x7A4A18 : 0x33261A,
    				dangBam ? 0x3A2409 : 0x0C0805);
    		g.setColor(0xFFFFFF, dangBam ? 0.20f : 0.11f);
    		g.fillRect(x + day + 1, y + day + 1, co - day * 2 - 2,
    				(co - day * 2 - 2) / 2, r);
    	}

    	/// <summary>
    	/// Đậu thần đã hồi được bao nhiêu phần trăm.
    	/// </summary>
    	/// <remarks>
    	/// Bản cũ quy về thang 0..20 điểm rồi vẽ thẳng, và có một chỗ hụt: ngay lúc
    	/// vừa dùng xong thì số điểm bằng 0, mà nhánh <c>num3 == 0</c> lại tô ĐẦY —
    	/// nên vừa ăn đậu là thanh nhảy về đầy rồi mới tụt xuống. Trả về phần trăm
    	/// thì không còn trường hợp riêng nào: 0 là vừa dùng, 100 là dùng được.
    	/// </remarks>
    	private static int phanNapDauThan(long tuLucDung)
    	{
    		if (tuLucDung < 0L || tuLucDung >= 10000L)
    		{
    			return 100;
    		}
    		return (int)(tuLucDung * 100L / 10000L);
    	}

    	/// <summary>Ảnh nút Đậu thần và nút Capsule, vẽ sẵn, đã bo góc.</summary>
    	public static Image imgNutDauThan;

    	public static Image imgNutCapsule;

    	/// <summary>
    	/// Cạnh của một nút nhanh.
    	/// </summary>
    	/// <remarks>
    	/// Bằng đúng cạnh một ô kỹ năng ở hàng phím tắt, vì nút nhanh dùng chung
    	/// khung với chúng: hai cụm nút cạnh nhau mà khác cỡ thì nhìn ra ngay.
    	/// Ảnh biểu tượng bên trong là 21 điểm — chừa hẳn một vòng lòng ô quanh nó,
    	/// đúng lối ô kỹ năng của game nhập vai: cái khung mới là thứ đọc ra trước,
    	/// biểu tượng nằm lọt bên trong chứ không lấp đầy.
    	/// </remarks>
    	private const int CO_NUT_NHANH = 28;

    	/// <summary>
    	/// Ô của nút Capsule: x, y, rộng, cao.
    	/// </summary>
    	/// <remarks>
    	/// <b>Một nguồn duy nhất cho cả chỗ vẽ lẫn chỗ bắt chạm.</b> Trước đây toạ độ
    	/// nút được viết tay ở hai nơi rời nhau, nên dịch nút đi là phần bắt chạm ở
    	/// lại chỗ cũ.
    	///
    	/// Đặt ở phần bảy phần mười bề ngang, sát đáy: khoảng trống giữa khung chat
    	/// và cụm nút góc phải. Có chặn để không bao giờ chồng lên cụm góc phải dù
    	/// màn hình hẹp tới đâu.
    	/// </remarks>
    	public static int[] oNutCapsule()
    	{
    		// Nam NGAY TREN o Dau than, cung mot cot.
    		//
    		// Truoc day hai nut nam hai ben khung chat. Xep chong len nhau thi ca
    		// cum gon vao mot cot hep, khong an vao be ngang von da chat cua day
    		// man hinh — cho do con phai chia cho hang o ky nang va khung chat.
    		int[] o = oNutDauThan();
    		if (GameCanvas.isTouch && isAnalog != 0)
    		{
    			// Ban dien thoai: hai nut nay va cum Dam / Chuyen muc tieu xep
    			// thanh mot luoi hai hang hai cot — Dau than ngang hang nut Dam,
    			// Capsule ngang hang nut Chuyen muc tieu. Bon nut deu tam thi ca
    			// goc phai duoi doc ra la MOT cum, thay vi hai cot le nhau.
    			return new int[] { o[0], yTG + 20 - o[3] / 2, o[2], o[3] };
    		}
    		// Cach o Dau than 12 diem: sat qua thi hai o nhin nhu mot khoi.
    		return new int[] { o[0], o[1] - o[3] - 12, o[2], o[3] };
    	}

    	/// <summary>Ô của nút Đậu thần — nằm bên PHẢI khung chat.</summary>
    	public static int[] oNutDauThan()
    	{
    		// Neo vao MEP PHAI man hinh, khong bam theo khung chat nua.
    		//
    		// Bam theo khung chat thi hai thu keo nhau: khung chat rong ra la nut
    		// bi day sang phai, ma nut day sang phai lai lam khung chat het cho.
    		// Neo vao mep man hinh thi nut dung yen, con khung chat tu tinh be rong
    		// theo khoang con lai — xem ChatUI.rongKhung.
    		return new int[] { mepTraiCumNutNhanh(), yNutNhanh(),
    				CO_NUT_NHANH, CO_NUT_NHANH };
    	}

    	/// <summary>Mép trái của cụm nút nhanh — khung chat dừng trước chỗ này.</summary>
    	/// <remarks>
    	/// Bản điện thoại đứng riêng một cột <b>bên trái</b> cụm Đấm / Chuyển mục
    	/// tiêu, không neo vào mép màn hình như bản máy tính.
    	/// <para>Cụm Đấm / Chuyển mục tiêu chiếm hẳn cột ngoài cùng bên phải
    	/// (<c>xF = w - 45</c>, rộng 40) và chỉ có trên điện thoại. Neo hai nút
    	/// nhanh vào mép màn hình thì chúng nằm <i>ngay dưới</i> hai nút cảm ứng to
    	/// tướng ấy — đúng cái cảnh hai chữ "Capsule" và "Đậu thần" thò ra từ dưới
    	/// nút đấm.</para>
    	/// </remarks>
    	public static int mepTraiCumNutNhanh()
    	{
    		if (GameCanvas.isTouch && isAnalog != 0)
    		{
    			return xF - 6 - CO_NUT_NHANH;
    		}
    		return GameCanvas.w - CO_NUT_NHANH - 4;
    	}

    	/// <summary>Mép trên của hai nút nhanh — cùng một hàng.</summary>
    	private static int yNutNhanh()
    	{
    		if (GameCanvas.isTouch && isAnalog != 0)
    		{
    			// Ngang tam nut Dam — nut do ve o tam (xF + 20, yF + 20).
    			return yF + 20 - CO_NUT_NHANH / 2;
    		}
    		return GameCanvas.h - CO_NUT_NHANH - 12;
    	}

    	/// <summary>
    	/// Biển tên gắn dưới chân một nút nhanh.
    	/// </summary>
    	/// <remarks>
    	/// Nền tối, viền vàng, chữ vàng — cùng bộ với nhãn phím tắt của hàng ô kỹ
    	/// năng, nên cả dải HUD dưới đáy đọc ra là một bộ.
    	///
    	/// Đè lên mép dưới của nút chứ không thả rời bên dưới: dán vào chân nút thì
    	/// mắt đọc ra ngay là tên của <b>cái nút này</b>, còn thả rời một khoảng thì
    	/// nó lửng lơ giữa nút và mép màn hình.
    	/// </remarks>
    	private static void veBienTen(mGraphics g, int[] o, string chu)
    	{
    		if (chu == null || chu.Length == 0)
    		{
    			return;
    		}
    		// Le trong 6 thay vi 9: bien cang hep thi cang it tho ra hai ben nut.
    		mFont f = mFont.tahoma_7b_yellow;
    		int w = f.getWidth(chu) + 6;
    		int h = 12;
    		int x = o[0] + o[2] / 2 - w / 2;
    		int y = o[1] + o[3] - 4;
    		// Bien ten CAN GIUA nut, chi chan o hai mep man hinh.
    		//
    		// Ban truoc keo bien sang trai cho mep phai cua no khong vuot qua mep
    		// phai cua nut. Bien rong hon nut chung mot chuc diem, nen ket qua la
    		// bien lech han sang trai so voi cai bieu tuong no dang chu thich —
    		// nhin ra hai thu roi nhau chu khong phai mot nut co nhan.
    		//
    		// Chan o mep man hinh thi bien luon nam duoi dung bieu tuong, ma cung
    		// khong bao gio bi cat cut o goc.
    		int xToiDa = GameCanvas.w - 2 - w;
    		if (x > xToiDa)
    		{
    			x = xToiDa;
    		}
    		if (x < 2)
    		{
    			x = 2;
    		}
    		// Vien: vanh dong, cung mau voi vanh cua nut.
    		g.setColor(0xC8933C, 1f);
    		g.fillRect(x, y, w, h, 5);
    		g.setColor(0x1C1208, 1f);
    		g.fillRect(x + 1, y + 1, w - 2, h - 2, 4);
    		f.drawString(g, chu, x + w / 2, y, mFont.CENTER);
    	}

    	/// <summary>
    	/// Một nút nhanh: khung ô kỹ năng làm nền, biểu tượng đặt giữa.
    	/// </summary>
    	/// <remarks>
    	/// Dùng lại <see cref="veKhungKyNang"/> chứ không dựng một cái khung riêng.
    	///
    	/// Hai lý do. Một: thả biểu tượng trần lên nền bản đồ thì nó trông trống
    	/// trơ, không ra một cái nút bấm được. Hai: hai nút này nằm cùng một hàng
    	/// với thanh phím tắt, dùng chung khung thì cả dải HUD dưới đáy màn hình đọc
    	/// ra là một bộ, còn mỗi nút một kiểu viền thì rời rạc.
    	///
    	/// Nhờ thế phần trạng thái đang bấm cũng có sẵn: quầng sáng, vành vàng dày
    	/// lên, lòng ô sáng lên — không phải viết lại.
    	/// </remarks>
    	private static void veNutAnh(mGraphics g, int[] o, Image anh, bool dangBam)
    	{
    		veKhungNutNhanh(g, o[0], o[1], o[2], dangBam);
    		if (anh == null)
    		{
    			return;
    		}
    		// Ha xuong 1 diem khi bam: nut lun xuong duoi ngon tay.
    		g.drawImage(anh, o[0] + o[2] / 2, o[1] + o[3] / 2 + (dangBam ? 1 : 0),
    				mGraphics.HCENTER | mGraphics.VCENTER);
    	}

    	/// <summary>Mã icon vẽ trong nút Capsule, lấy từ kho icon máy chủ.</summary>
    	/// <remarks>
    	/// Dùng icon của máy chủ thay cho ảnh <c>nutcapsule.png</c> tự vẽ: icon máy
    	/// chủ đã có sẵn đủ bốn mức phóng và đúng bộ màu với mọi vật phẩm khác
    	/// trong game.
    	/// </remarks>
    	public const int ICON_NUT_CAPSULE = 3894;

    	/// <summary>
    	/// Một nút nhanh, biểu tượng lấy từ <b>kho icon máy chủ</b>.
    	/// </summary>
    	/// <remarks>
    	/// Giống <see cref="veNutAnh"/> nhưng nhận mã icon thay vì một
    	/// <c>Image</c>. <c>SmallImage.drawSmallImage</c> tự nạp icon ở lần vẽ đầu
    	/// nếu chưa có, nên chỗ gọi không phải lo phần nạp.
    	/// </remarks>
    	private static void veNutIcon(mGraphics g, int[] o, int maIcon, bool dangBam)
    	{
    		veKhungNutNhanh(g, o[0], o[1], o[2], dangBam);
    		SmallImage.drawSmallImage(g, maIcon, o[0] + o[2] / 2,
    				o[1] + o[3] / 2 + (dangBam ? 1 : 0), 0,
    				mGraphics.VCENTER | mGraphics.HCENTER);
    	}

    	/// <summary>
    	/// Khung của một nút nhanh — bản dày dặn hơn khung ô kỹ năng.
    	/// </summary>
    	/// <remarks>
    	/// <para>Ô kỹ năng chỉ rộng 30 điểm và nằm san sát nhau nên khung của nó
    	/// phải mỏng. Hai nút nhanh thì đứng riêng một cụm, có chỗ cho một cái vành
    	/// dày và một lòng ô sâu — nhìn ra là nút bấm được chứ không phải một ô
    	/// trong lưới.</para>
    	///
    	/// <para>Các lớp, từ ngoài vào: quầng sáng, bóng đổ, vành ngoài tối, vành
    	/// vàng chuyển màu, một nét sáng mảnh ngay trong vành (chỗ ánh sáng gãy ở
    	/// mép vát), lòng ô chuyển màu, vệt kính chéo ở góc trên trái, và cuối cùng
    	/// là nét hắt sáng ở đáy lòng ô.</para>
    	/// </remarks>
    	/// <summary>Khung nút nhanh, ô vuông.</summary>
    	public static void veKhungNutNhanh(mGraphics g, int x, int y, int canh,
    			bool dangBam)
    	{
    		veKhungNutNhanh(g, x, y, canh, canh, dangBam);
    	}

    	/// <summary>
    	/// Khung nút nhanh, rộng và cao khai riêng.
    	/// </summary>
    	/// <remarks>
    	/// Nút Tab chứa cả chữ "Tab 1" nên nó rộng hơn là cao, còn hai nút
    	/// Capsule và Đậu thần thì vuông. Một bản vẽ duy nhất cho cả ba, chỉ
    	/// khác kích thước — ba bản vẽ riêng thì sửa một cái là ba nút cạnh
    	/// nhau lệch nhau.
    	/// </remarks>
    	public static void veKhungNutNhanh(mGraphics g, int x, int y, int rong,
    			int cao, bool dangBam)
    	{
    		int bo = 7;

    		// 1. Quang sang quanh o, chi khi dang bam.
    		if (dangBam)
    		{
    			for (int i = 5; i >= 1; i--)
    			{
    				g.setColor(MAU_LOE_O, 0.10f * (6 - i));
    				g.fillRect(x - i, y - i, rong + i * 2, cao + i * 2, bo + i);
    			}
    		}

    		// Da bo bong do den phia sau nut — cung ly do voi o ky nang.

    		// 3. Vien ngoai gan den — duong bao cung cua ca o.
    		g.setColor(0x100A05, 1f);
    		g.fillRect(x, y, rong, cao, bo);

    		// 4. Vanh ĐỒNG chuyen mau. Am va tram hon vang cu: do dong cho o ra chat
    		// khung kim loai cua game nhap vai, khong phai mot nut nhua bong.
    		veDaiDoc(g, x + 1, y + 1, rong - 2, cao - 2, bo - 1,
    				dangBam ? 0xFFE7AE : 0xD7A960,
    				dangBam ? 0xA9701F : 0x6B4A1E);

    		// 5. VIEN KEP: mot net toi mong ngay trong vanh dong.
    		//
    		// Day la thu lam khung ra ve nhap vai nhat — mot cai nep chim chay vong
    		// quanh, tach vanh ngoai khoi long o. Mot vien lien mot mau nhin ra
    		// nhua; hai vien co nep chim o giua nhin ra kim loai dap.
    		g.setColor(0x281A0B, 1f);
    		g.fillRect(x + 3, y + 3, rong - 6, cao - 6, bo - 3);

    		// 6. Long o SAU va gan den, chuyen mau nhe.
    		int day = 4;
    		int xt = x + day;
    		int yt = y + day;
    		int cr = rong - day * 2;
    		int ct = cao - day * 2;
    		// Long o SANG han len cho bieu tuong noi bat.
    		//
    		// Ban truoc long o gan den (0x1B140D -> 0x050403). Hai bieu tuong deu
    		// mau tram — hat dau xanh la, capsule xanh xam — nen dat tren nen gan
    		// den la chim nghim. Nen nau sang thi ca hai bat han len, ma vanh dong
    		// ben ngoai van dam hon nen o van doc ra la mot cai o khoet sau.
    		veDaiDoc(g, xt, yt, cr, ct, 3,
    				dangBam ? 0xB08A58 : 0x8A6A42,
    				dangBam ? 0x70522C : 0x4E3A22);

    		// 7. Bong do TRONG long o: net toi o canh tren va canh trai, cho o nhu
    		// khoet sau vao tam kim loai chu khong dan len tren.
    		g.setColor(0x000000, 0.45f);
    		g.fillRect(xt, yt, cr, 1);
    		g.fillRect(xt, yt, 1, ct);

    		// 8. Net hat sang o canh duoi long o — anh sang doi tu vanh duoi len.
    		g.setColor(0xFFD98A, 0.13f);
    		g.fillRect(xt + 2, yt + ct - 1, cr - 4, 1);

    		// 9. BON DINH TAN o bon goc vanh dong: mot cham sang tren nen toi, dung
    		// lam mot cai dinh nhin nghieng.
    		int d2 = 4;
    		veDinhTan(g, x + d2, y + d2, dangBam);
    		veDinhTan(g, x + rong - d2 - 1, y + d2, dangBam);
    		veDinhTan(g, x + d2, y + cao - d2 - 1, dangBam);
    		veDinhTan(g, x + rong - d2 - 1, y + cao - d2 - 1, dangBam);
    	}

    	/// <summary>Một cái đinh tán nhỏ: chấm tối làm lỗ, chấm sáng làm đầu đinh.</summary>
    	private static void veDinhTan(mGraphics g, int tamX, int tamY, bool dangBam)
    	{
    		g.setColor(0x2A1B0C, 0.9f);
    		g.fillRect(tamX - 1, tamY - 1, 3, 3, 1);
    		g.setColor(0xFFEFC4, dangBam ? 1f : 0.8f);
    		g.fillRect(tamX - 1, tamY - 1, 2, 2, 1);
    	}

    	/// <summary>
    	/// Cụm hai nút nhanh: Capsule và Đậu thần.
    	/// </summary>
    	/// <remarks>
    	/// Dùng ảnh vẽ sẵn chứ không dựng hình bằng mã: hình vẽ bằng mã chỉ ghép được
    	/// từ chữ nhật bo góc nên trông thô, còn ảnh thì đủ bốn mức phóng.
    	///
    	/// Thời gian hồi đậu thần đọc bằng <b>màn tối phủ từ trên xuống</b>, rút dần
    	/// khi sắp dùng được — không thêm hình khối nào vào màn chơi.
    	/// </remarks>
    	/// <param name="phanNap">0 = vừa dùng xong, 100 = dùng được ngay.</param>
    	public static void veCumNutNhanh(mGraphics g, int phanNap)
    	{
    		if (isudungCapsun3 || isudungCapsun4)
    		{
    			int[] oc = oNutCapsule();
    			veNutIcon(g, oc, ICON_NUT_CAPSULE, mScreen.keyTouch == 14);
    			veBienTen(g, oc, "Capsule");
    		}
    		int[] od = oNutDauThan();
    		veNutAnh(g, od, imgNutDauThan, mScreen.keyTouch == 10);
    		veBienTen(g, od, "Đậu thần");
    		if (phanNap < 100)
    		{
    			int cao = od[3] * (100 - phanNap) / 100;
    			if (cao > 0)
    			{
    				g.setColor(0x000000, 0.55f);
    				g.fillRect(od[0], od[1], od[2], cao, 8);
    			}
    		}
    		// Huy hieu so dau: the vang, vien toi, chu toi — doc duoc tren nen sang
    		// cua chinh no. Dat de len goc duoi phai cua khung nhu mot con tem.
    		string s = string.Empty + hpPotion;
    		int wS = mFont.tahoma_7b_dark.getWidth(s) + 9;
    		int hS = 13;
    		if (wS < hS)
    		{
    			wS = hS;
    		}
    		// Len goc TREN phai: goc duoi da la cho cua bien ten.
    		int xS2 = od[0] + od[2] - wS + 3;
    		int yS2 = od[1] - 3;
    		g.setColor(0x000000, 0.55f);
    		g.fillRect(xS2 + 1, yS2 + 2, wS, hS, hS / 2);
    		g.setColor(0x3A2409, 1f);
    		g.fillRect(xS2, yS2, wS, hS, hS / 2);
    		veDaiDoc(g, xS2 + 1, yS2 + 1, wS - 2, hS - 2, (hS - 2) / 2,
    				0xFFE9A3, 0xD79A2E);
    		mFont.tahoma_7b_dark.drawString(g, s, xS2 + wS / 2, yS2 + 1,
    				mFont.CENTER);
    	}

    	/// <summary>Độ bo góc của một ô kỹ năng.</summary>
    	private const int BO_O = 8;

    	/// <summary>Màu quầng sáng quanh ô đang chọn.</summary>
    	private const int MAU_LOE_O = 0xFFD98A;

    	/// <summary>
    	/// Hình bo góc, màu chuyển dần từ trên xuống dưới.
    	/// </summary>
    	/// <remarks>
    	/// Máy vẽ không có hàm chuyển màu, nên phải kẻ từng dòng một điểm. Nhưng kẻ
    	/// dòng thẳng thì góc vuông thò ra ngoài hình bo góc, nên chia làm ba phần:
    	/// tô cả hình bằng màu trên, tô một hình bo góc cao <c>2R</c> ở đáy bằng màu
    	/// dưới, rồi mới kẻ từng dòng cho khoảng giữa — khoảng ấy cách hai mép ít
    	/// nhất <c>R</c> điểm nên không dòng nào chạm tới góc.
    	/// </remarks>
    	public static void veDaiDoc(mGraphics g, int x, int y, int w, int h, int r,
    			int mauTren, int mauDuoi)
    	{
    		if (w <= 0 || h <= 0)
    		{
    			return;
    		}
    		if (r < 0)
    		{
    			r = 0;
    		}
    		if (r * 2 > h)
    		{
    			r = h / 2;
    		}
    		// Mot lenh ve, thay cho hang chuc dong ke tay — xem mGraphics.veDaiDoc.
    		g.veDaiDoc(x, y, w, h, r, mauTren, mauDuoi);
    	}

    	/// <summary>
    	/// Nhãn phím tắt của một ô: thẻ nhỏ bo góc, viền vàng, chữ vàng.
    	/// </summary>
    	/// <remarks>
    	/// Trước đây nhãn là chữ trắng thả trần trên nền bản đồ (có chỗ là một khối
    	/// chữ nhật vuông góc màu nâu đỏ). Nền bản đồ đổi theo từng hành tinh — trời
    	/// sáng, tuyết trắng — nên chữ trắng có lúc mất hút. Thẻ nền tối thì nhãn
    	/// đọc được ở mọi bản đồ, và cùng bộ màu với ô ngay dưới nó.
    	/// </remarks>
    	private static void veNhanPhim(mGraphics g, int tamX, int tamY, string nhan)
    	{
    		if (nhan == null || nhan.Length == 0)
    		{
    			return;
    		}
    		int w = mFont.tahoma_7b_white.getWidth(nhan) + 9;
    		if (w < 15)
    		{
    			w = 15;
    		}
    		int h = 13;
    		int x = tamX - w / 2;
    		int y = tamY - h / 2;
    		g.setColor(0x000000, 0.45f);
    		g.fillRect(x + 1, y + 2, w, h, 5);
    		g.setColor(0xC8933C, 1f);
    		g.fillRect(x, y, w, h, 5);
    		g.setColor(0x1C1208, 1f);
    		g.fillRect(x + 1, y + 1, w - 2, h - 2, 4);
    		g.setColor(0xFFFFFF, 0.07f);
    		g.fillRect(x + 1, y + 1, w - 2, (h - 2) / 2, 4);
    		mFont.tahoma_7b_yellow.drawString(g, nhan, tamX, y + 1, mFont.CENTER);
    	}

    	private void paintTouchControl(mGraphics g)
    	{
    		if (isNotPaintTouchControl())
    		{
    			return;
    		}
    		resetTranslate(g);
    		if (!TileMap.isOfflineMap() && !isVS())
    		{
    			// O chat: cung khung, cung chu voi ba o Co / Khu / Tab.
    			//
    			// Bo hai anh tron mau cam (imgChat / imgChatPC). Chung la anh mot
    			// co, mau cam khong an nhap voi tong dong vang cua ca hang, va hinh
    			// TRON dung canh bon o VUONG thi lac han.
    			//
    			// Ghi chu "Chat" nhu ba o kia: doc ra ngay, khong phai doan mot cai
    			// bong bong chu R nghia la gi.
    			bool dangBam = mScreen.keyTouch == 15 || mScreen.keyMouse == 15;
    			int yVe = yC + mGraphics.addYWhenOpenKeyBoard;
    			veKhungNutNhanh(g, xC, yVe, W_CHAT, H_CHAT, dangBam);
    			int yChu = yVe + H_CHAT / 2 - mFont.tahoma_7b_yellow.getHeight() / 2
    					+ (dangBam ? 1 : 0);
    			// Khong con cham do o nut Chat.
    			//
    			// Khung chat lon nam ngay canh nut nay va cac the trong no da tu
    			// nhay bao tin moi roi; them mot cham nua o ngoai la bao hai lan
    			// cho cung mot viec.
    			mFont.tahoma_7b_dark.drawString(g, "Chat", xC + W_CHAT / 2 + 1,
    					yChu + 1, mFont.CENTER);
    			mFont.tahoma_7b_yellow.drawString(g, "Chat", xC + W_CHAT / 2,
    					yChu, mFont.CENTER);
    		}
    		if (isUseTouch)
    		{
    		}
    	}
    
    	public void paintImageBarRight(mGraphics g, Char c)
    	{
    		int num = (int)(c.cHP * hpBarW / ((c.cHPFull > 0) ? c.cHPFull : 1));
    		int num2 = c.cMP * mpBarW;
    		int num3 = (int)(dHP * hpBarW / ((c.cHPFull > 0) ? c.cHPFull : 1));
    		int num4 = dMP * mpBarW;
    		g.setClip(GameCanvas.w / 2 + 58 - mGraphics.getImageWidth(imgPanel), 0, 95, 100);
    		g.drawRegion(imgPanel, 0, 0, mGraphics.getImageWidth(imgPanel), mGraphics.getImageHeight(imgPanel), 2, GameCanvas.w / 2 + 60, 0, mGraphics.RIGHT | mGraphics.TOP);
    		g.setClip((int)(GameCanvas.w / 2 + 60 - 83 - hpBarW + hpBarW - num3), 5, num3, 10);
    		g.drawImage(imgHPLost, GameCanvas.w / 2 + 60 - 83, 5, mGraphics.RIGHT | mGraphics.TOP);
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		g.setClip((int)(GameCanvas.w / 2 + 60 - 83 - hpBarW + hpBarW - num), 5, num, 10);
    		g.drawImage(imgHP, GameCanvas.w / 2 + 60 - 83, 5, mGraphics.RIGHT | mGraphics.TOP);
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		g.setClip((int)(GameCanvas.w / 2 + 60 - 83 - mpBarW + hpBarW - num4), 20, num4, 6);
    		g.drawImage(imgMPLost, GameCanvas.w / 2 + 60 - 83, 20, mGraphics.RIGHT | mGraphics.TOP);
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		g.setClip((int)(GameCanvas.w / 2 + 60 - 83 - mpBarW + hpBarW - num2), 20, num2, 6);
    		g.drawImage(imgMP, GameCanvas.w / 2 + 60 - 83, 20, mGraphics.RIGHT | mGraphics.TOP);
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    	}
    
    	/// <summary>
    	/// Thanh HP/KI ve bang ma: nen chim, ruot chuyen sac, vet bong o tren.
    	/// </summary>
    	/// <remarks>
    	/// <para>Ve bang ma thay cho anh imgHP/imgMP cat theo setClip. Anh chi co
    	/// mot co nen doi chieu cao thanh la re nhoe, va doi mau thi phai xuat lai
    	/// anh; ve bang ma thi net o moi muc zoom va doi mau chi la doi mot so.</para>
    	///
    	/// <para>Bo tron hai dau bang ban kinh bang nua chieu cao — thanh mau nhin
    	/// men hon han goc vuong.</para>
    	/// </remarks>
    	private static void veThanhMau(mGraphics g, int x, int y, int rong, int cao,
    			int mauDam, int mauSang)
    	{
    		if (rong <= 0)
    		{
    			return;
    		}
    		int r = cao / 2;
    		g.setColor(mauDam, 1f);
    		g.fillRect(x, y, rong, cao, r);
    		g.setColor(mauSang, 0.85f);
    		g.fillRect(x, y, rong, cao / 2, r);
    		g.setColor(0xFFFFFF, 0.30f);
    		g.fillRect(x + 1, y + 1, rong - 2, cao / 3, r);
    	}

    	// Bang mau thanh trang thai. HP do, KI xanh — moi mau mot cap dam/sang de
    	// ruot thanh co do chuyen sac.
    	private const int MAU_HP_DAM = 0xB01B1B;
    	private const int MAU_HP_SANG = 0xFF4D4D;
    	private const int MAU_KI_DAM = 0x1B5FB0;
    	private const int MAU_KI_SANG = 0x4DA6FF;
    	private const int MAU_VANG = 0xC8933C;
    	private const int MAU_VANG_SANG = 0xFFE9A3;

    	/// <summary>Pha hai màu theo tỉ lệ <paramref name="t"/> (0..1).</summary>
    	private static int tronMauHud(int a, int b, float t)
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
    	/// Nền bảng HUD: bóng đổ, viền vàng, lòng nâu chuyển màu.
    	/// </summary>
    	/// <remarks>
    	/// Mỗi dải màu kéo từ đỉnh của nó <b>xuống tận đáy</b> bảng, và vẽ dải cao
    	/// nhất trước. Vẽ từng dải rời rạc thành hình chữ nhật vuông góc thì góc
    	/// vuông thò hẳn ra ngoài khung bo góc — máy vẽ không có hàm chuyển màu
    	/// nào khác.
    	/// </remarks>
    	public static void veNenBangHud(mGraphics g, int x, int y, int w, int h)
    	{
    		const int BO = 9;
    		// Khong ve bong do den phia sau: no tao mot quang toi lech ve mot ben,
    		// nhin nhu bang bi dan lech chu khong nhu bang noi len.
    		g.setColor(MAU_VANG, 1f);
    		g.fillRect(x, y, w, h, BO);
    		g.setColor(0xA87433, 1f);
    		g.fillRect(x + 2, y + 2, w - 4, h - 4, BO - 2);

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
    		// Cach cu ve tung DAI bo goc chong len nhau. Dai cuoi thap qua nen ban
    		// kinh bi kep gan ve 0 va de goc vuong len goc tron cua dai truoc; chua
    		// bang cach giam so dai thi het loi goc nhung chuyen mau giat cap.
    		//
    		// Cach nay tach hai viec ra. Hai dau tren duoi — noi co goc bo tron —
    		// to bang hai hinh BO GOC. Phan giua thi ke tung dong mot diem, va vi
    		// no cach hai mep it nhat R diem nen khong dong nao cham toi goc.
    		// Hai dau dai mau cua nen bang.
    		//
    		// Da thu keo ca hai len sang mot bac cho chu de doc hon: nhin ra xau va
    		// chu trang bat dau chim vao nen, nen tra lai mau cu. Cho chu noi khoi
    		// nen thi sua o CHU — doi mau va to vien — chu khong dong vao nen.
    		int R = BO - 3;
    		const int MAU_TREN = 0xC79A55;
    		const int MAU_DUOI = 0x8A5E24;
    		g.veDaiDoc(xt, yt, wt, ht, R, MAU_TREN, MAU_DUOI);
    		// Vet sang mong o nua tren cho bang co do cong.
    		g.setColor(0xFFFFFF, 0.10f);
    		g.fillRect(xt, yt, wt, ht / 2, R);
    	}

    	/// <summary>
    	/// Chan dung tron: vong vang ben ngoai, long toi, anh dau o giua.
    	/// </summary>

    	/// <remarks>
    	/// Hinh tron ve bang MOT lenh <c>fillRect</c> voi ban kinh bo goc bang nua
    	/// canh — may ve kep ban kinh lai toi da nua canh nho, nen o vuong co ban
    	/// kinh nua canh la mot hinh tron that.
    	/// </remarks>
    	private static void veChanDung(mGraphics g, int tamX, int tamY, int d, int avatar)
    	{
    		int r = d / 2;
    		g.setColor(MAU_VANG, 1f);
    		g.fillRect(tamX - r, tamY - r, d, d, r);
    		g.setColor(MAU_VANG_SANG, 1f);
    		g.fillRect(tamX - r + 2, tamY - r + 2, d - 4, d - 4, (d - 4) / 2);
    		g.setColor(0x3B2712, 1f);
    		g.fillRect(tamX - r + 3, tamY - r + 3, d - 6, d - 6, (d - 6) / 2);
    		if (avatar >= 0)
    		{
    			// CAT anh vao trong vong tron.
    			//
    			// Anh cua may chu to hon vong tron nhieu, va drawSmallImage khong
    			// co tham so thu nho. Khong cat thi no tran ra ngoai khung, de len
    			// bang ten ban do ngay ben duoi va len ca canh trai man hinh.
    			// Cat theo HINH VUONG NOI TIEP vong tron, khong phai theo ca o
    			// vuong bao quanh no.
    			//
    			// setClip chi cat duoc hinh chu nhat. Cat theo o bao quanh thi bon
    			// goc cua o nam NGOAI duong tron, nen anh dau tho ra khoi vanh —
    			// dung canh "icon tran vien". Hinh vuong noi tiep co nua canh
    			// r/√2 ≈ 0,707·r nen moi diem cua no deu nam trong duong tron, va
    			// khong con gi tho ra duoc.
    			// Cat theo CA LONG VANH, khong phai hinh vuong noi tiep.
    			//
    			// Ban truoc cat theo hinh vuong noi tiep (nua canh 0,7·r) cho chac
    			// chan khong diem nao tho ra ngoai duong tron — nhung the la o cat
    			// hep hon vanh nhieu, anh dau bi xen bon phia.
    			//
    			// Cung ban truoc lai thu nho anh bang ma tran GUI. Cach do sai o
    			// mot cho tinh vi: diem neo phai tinh bang pixel man hinh, ma toa do
    			// ve con cong them `translate` cua mGraphics — khung nhan vat duoc
    			// ve luc translate KHAC 0, nen diem neo lech va ca cai dau bi keo
    			// xuong duoi ben trai. Do la canh "avatar van lech lam".
    			//
    			// Bo ca hai: cat theo o vuong bao quanh long vanh, ve anh o co that.
    			// Bon goc cua o do nam ngoai duong tron, nhung anh dau von tron nen
    			// gan nhu khong bao gio cham toi goc.
    			int nuaCanh = r - 3;
    			g.setClip(tamX - nuaCanh, tamY - nuaCanh, nuaCanh * 2, nuaCanh * 2);

    			// Thu nho anh dau con MOT NUA.
    			//
    			// Anh dau cua may chu ve o co that thi to gan gap doi vanh, nen no
    			// tran kin o cat va bi xen bon phia.
    			//
    			// Diem neo cua ma tran GUI phai la PIXEL MAN HINH, va pixel man
    			// hinh = toa do game x zoomLevel CONG do doi hien tai. Lan truoc
    			// toi bo quen phan do doi — khung nhan vat duoc ve luc do doi khac
    			// 0, nen ca cai dau bi keo lech han xuong duoi ben trai. Doc do doi
    			// bang getTranslateXPx/YPx: hai ham getTranslateX/Y kia da chia
    			// zoom va cong them addYWhenOpenKeyBoard, dung vao day la lech tiep.
    			UnityEngine.Matrix4x4 luuMaTran = UnityEngine.GUI.matrix;
    			UnityEngine.GUIUtility.ScaleAroundPivot(
    					new UnityEngine.Vector2(0.5f, 0.5f),
    					new UnityEngine.Vector2(
    							tamX * mGraphics.zoomLevel + g.getTranslateXPx(),
    							tamY * mGraphics.zoomLevel + g.getTranslateYPx()));
    			SmallImage.drawSmallImage(g, avatar, tamX, tamY, 0,
    					mGraphics.VCENTER | mGraphics.HCENTER);
    			UnityEngine.GUI.matrix = luuMaTran;
    			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    		}
    	}

    	/// <summary>Mã ảnh cái ĐẦU nhân vật đang đội, hoặc <c>-1</c>.</summary>
    	/// <remarks>
    	/// Dùng đầu thật chứ không dùng <c>avatarz()</c>: avatar là ảnh chân dung
    	/// cố định theo hành tinh, đổi tóc hay đội mũ mới đều không thấy gì. Đầu
    	/// thì đúng cái người chơi đang nhìn thấy trên bản đồ.
    	///
    	/// Tra qua <c>GameScr.parts</c> nên phải phòng lúc bảng parts chưa tải về
    	/// hoặc mã đầu nằm ngoài bảng — trả <c>-1</c> để chỗ gọi vẽ ô rỗng thay vì
    	/// ném lỗi giữa lúc vẽ và mất luôn phần còn lại của khung hình.
    	/// </remarks>
    	private static int maAnhDau(Char c)
    	{
    		return (c == null) ? -1 : maAnhDauTheoBoPhan(c.head);
    	}

    	/// <summary>Mã ảnh đầu ứng với một mã bộ phận, hoặc <c>-1</c>.</summary>
    	/// <remarks>
    	/// Tách riêng để NPC dùng lại được. NPC không có trường <c>head</c> như
    	/// nhân vật, nhưng <c>NpcTemplate</c> có <c>headId</c> — cùng một bảng
    	/// <c>GameScr.parts</c>. Trước đây khung mục tiêu lấy <c>npcFocus.avatar</c>,
    	/// mà NPC trên bản đồ hầu hết có avatar bằng 0 nên ô chân dung trống trơn.
    	/// </remarks>
    	private static int maAnhDauTheoBoPhan(int maBoPhan)
    	{
    		if (parts == null || maBoPhan < 0 || maBoPhan >= parts.Length)
    		{
    			return -1;
    		}
    		Part p = parts[maBoPhan];
    		if (p == null || p.pi == null)
    		{
    			return -1;
    		}
    		int k = Char.CharInfo[0][0][0];
    		if (k < 0 || k >= p.pi.Length || p.pi[k] == null)
    		{
    			return -1;
    		}
    		return p.pi[k].id;
    	}

    	// ==============================================================
    	//  Cot lop phu ben trai
    	// ==============================================================

    	/// <summary>Con trỏ dọc của cột lớp phủ bên trái.</summary>
    	private static int yCotTrai;

    	/// <summary>
    	/// Đặt lại con trỏ về ngay dưới khung nhân vật. Gọi <b>một lần</b> mỗi
    	/// khung hình, trước mọi thứ xếp vào cột.
    	/// </summary>
    	/// <remarks>
    	/// <para>Trước đây mỗi lớp phủ tự đặt toạ độ cứng: tên bản đồ ở (85,30),
    	/// số khu ở (85,40), hàng icon hiệu ứng ở y=55, dòng chữ hiệu ứng ở y=45
    	/// hoặc 90, danh sách bật/tắt ở giữa cạnh trái. Chúng không biết gì về
    	/// nhau, nên hễ có thêm một thứ là chồng lên thứ khác — và chồng ở chỗ
    	/// nào thì tuỳ lúc chơi mới lộ ra.</para>
    	///
    	/// <para>Nay tất cả xin chỗ qua <see cref="xinChoCotTrai"/> theo đúng thứ
    	/// tự vẽ, nên không bao giờ đè nhau và khoảng trống tự co lại khi một
    	/// nhóm vắng mặt.</para>
    	/// </remarks>
    	public static void batDauCotTrai()
    	{
    		yCotTrai = dayKhungNguoiChoi() + 4;
    	}

    	/// <summary>Xin một khoảng cao <paramref name="cao"/> điểm; trả về y của nó.</summary>
    	public static int xinChoCotTrai(int cao)
    	{
    		int y = yCotTrai;
    		yCotTrai += cao;
    		return y;
    	}

    	/// <summary>Đẩy con trỏ xuống tới <paramref name="y"/> nếu nó đang ở trên.</summary>
    	public static void dayCotTraiToi(int y)
    	{
    		if (y > yCotTrai)
    		{
    			yCotTrai = y;
    		}
    	}

    	/// <summary>Lề trái dùng chung cho mọi thứ trong cột.</summary>
    	public const int LE_COT_TRAI = 8;

    	/// <summary>Phần "  X:123 Y:456" nối sau số khu.</summary>
    	/// <remarks>
    	/// Đọc thẳng từ nhân vật của mình nên nó chạy theo từng bước chân. Không
    	/// hỏi máy chủ, và cũng không hỏi được: máy chủ chỉ biết vị trí ở thời
    	/// điểm gói tin cuối, còn ô này phải khớp với cái người chơi đang nhìn.
    	///
    	/// Trả về chuỗi rỗng khi chưa có nhân vật — lúc mới vào game hay đang
    	/// đổi bản đồ. Bảng bản đồ vẫn phải vẽ được, chỉ thiếu phần toạ độ.
    	/// </remarks>
    	private static string toaDoChu()
    	{
    		Char c = Char.myCharz();
    		if (c == null)
    		{
    			return "";
    		}
    		return "  X:" + c.cx + " Y:" + c.cy;
    	}

    	/// <summary>
    	/// Bảng tên bản đồ và số khu, xếp ngay dưới khung nhân vật.
    	/// </summary>
    	/// <remarks>
    	/// Có nền mờ và viền vàng vì hai dòng này nằm đè lên bản đồ: chữ trắng có
    	/// bóng trên nền trời sáng vẫn khó đọc, nền mờ thì đọc được ở mọi map.
    	///
    	/// Bề ngang đo theo dòng dài nhất chứ không đặt cứng — tên map dài ngắn
    	/// rất khác nhau, đặt cứng thì hoặc thừa một khoảng trống to, hoặc chữ
    	/// tràn ra ngoài viền.
    	/// </remarks>
    	public static void veBangBanDo(mGraphics g)
    	{
    		string d1 = TileMap.mapID + " - " + TileMap.mapName;
    		string d2 = "Khu " + TileMap.zoneID + toaDoChu();
    		int w = mFont.tahoma_7_white.getWidth(d1);
    		int w2 = mFont.tahoma_7_white.getWidth(d2);
    		if (w2 > w)
    		{
    			w = w2;
    		}
    		int x = LE_COT_TRAI;
    		int y = xinChoCotTrai(31);
    		// Vien vang bo goc ve lot sau, roi nen toi de len — khong dung
    		// `drawRect` vi ham do khong nhan ban kinh, vien se ra khung vuong
    		// bao quanh mot cai nen da bo tron.
    		g.setColor(MAU_VANG, 0.85f);
    		g.fillRect(x - 5, y - 4, w + 14, 29, 8);
    		g.setColor(0x241809, 0.80f);
    		g.fillRect(x - 4, y - 3, w + 12, 27, 7);
    		mFont.tahoma_7_white.drawStringBd(g, d1, x, y, 0, mFont.tahoma_7_grey);
    		mFont.tahoma_7_white.drawStringBd(g, d2, x, y + 12, 0, mFont.tahoma_7_grey);

    		// Cong bo mep phai va mep tren de hang hieu ung dem gio bam theo.
    		xPhaiBangBanDo = x + w + 8;
    		yBangBanDo = y - 4;
    	}

    	/// <summary>Mép phải và mép trên của bảng tên bản đồ ở khung hình này.</summary>
    	/// <remarks>
    	/// Hàng hiệu ứng đếm giờ neo vào đây thay vì tự đặt toạ độ: bảng bản đồ
    	/// rộng hẹp theo tên map, đặt số cứng thì map tên dài là hai thứ chồng lên
    	/// nhau.
    	/// </remarks>
    	public static int xPhaiBangBanDo = 170;
    	public static int yBangBanDo = 50;

    	/// <summary>
    	/// Dấu hiệu tình trạng của một bước nhiệm vụ.
    	/// </summary>
    	/// <remarks>
    	/// Ba tình trạng vẽ ba HÌNH khác nhau chứ không chỉ khác màu: xong là chấm
    	/// đặc, đang làm là chấm to có vành sáng, chưa tới là vòng rỗng. Chỉ dựa vào
    	/// màu thì người phân biệt màu kém đọc ra một danh sách giống hệt nhau.
    	/// </remarks>
    	private static void veDauBuoc(mGraphics g, int tamX, int tamY, int vai)
    	{
    		if (vai == 0)
    		{
    			g.setColor(0x4CD964, 1f);
    			g.fillRect(tamX - 3, tamY - 3, 6, 6, 3);
    			return;
    		}
    		if (vai == 1)
    		{
    			g.setColor(0xFFE9A3, 1f);
    			g.fillRect(tamX - 4, tamY - 4, 8, 8, 4);
    			g.setColor(0xE03A3A, 1f);
    			g.fillRect(tamX - 3, tamY - 3, 6, 6, 3);
    			return;
    		}
    		g.setColor(0xFFFFFF, 0.55f);
    		g.fillRect(tamX - 3, tamY - 3, 6, 6, 3);
    		g.setColor(0x6B4718, 1f);
    		g.fillRect(tamX - 2, tamY - 2, 4, 4, 2);
    	}

    	/// <summary>Bề rộng bảng Nhiệm vụ trên HUD.</summary>

    	private const int RONG_BANG_NV = 187;

    	/// <summary>Số dòng nhiệm vụ hiện cùng lúc; phần còn lại phải cuộn.</summary>
    	private const int SO_DONG_NV = 7;

    	/// <summary>Số dòng đã cuộn qua ở bảng Nhiệm vụ.</summary>
    	public static int cuonNhiemVu;

    	/// <summary>Vùng bấm hai nút cuộn của bảng Nhiệm vụ.</summary>
    	public static int[] oCuonNhiemVuLen = new int[0];
    	public static int[] oCuonNhiemVuXuong = new int[0];

    	/// <summary>Khung bảng Nhiệm vụ ở khung hình này: x, y, rộng, cao.</summary>
    	/// <remarks>
    	/// Phần bắt lăn chuột đọc ô này để biết con trỏ có đang nằm trên bảng hay
    	/// không. Lăn chuột ở giữa bản đồ mà bảng nhiệm vụ cũng cuộn theo thì rất
    	/// khó chịu.
    	/// </remarks>
    	public static int[] oBangNhiemVu = new int[0];

    	/// <summary>Nút cuộn nhỏ: tam giác dựng từ các vạch ngang.</summary>
    	private static void veNutCuonNV(mGraphics g, int x, int y, bool len)
    	{
    		g.setColor(0x6B4718, 1f);
    		g.fillRect(x, y, 11, 11, 5);
    		g.setColor(0xFFE9A3, 1f);
    		for (int i = 0; i < 4; i++)
    		{
    			int rong = len ? (i * 2 + 1) : (7 - i * 2);
    			g.fillRect(x + 5 - rong / 2, y + 4 + i, rong, 1);
    		}
    	}

    	/// <summary>Bảng Nhiệm vụ đang thu gọn hay không.</summary>
    	public static bool thuGonNhiemVu;

    	/// <summary>Vùng bấm của dấu thu gọn: x, y, rộng, cao. Rỗng khi bảng ẩn.</summary>
    	public static int[] oThuGonNhiemVu = new int[0];


    	/// <summary>
    	/// Bảng Nhiệm vụ dán bên trái màn hình: tên nhiệm vụ và các bước của nó.
    	/// </summary>
    	/// <remarks>
    	/// <para>Đọc thẳng <c>Char.myCharz().taskMaint</c> — đúng nguồn mà bảng
    	/// nhiệm vụ trong <c>Panel</c> dùng. Không giữ bản sao riêng: giữ bản sao
    	/// thì mỗi lần máy chủ đẩy tiến độ mới lại phải nhớ cập nhật cả hai chỗ,
    	/// và quên một chỗ thì hai bảng nói hai điều khác nhau.</para>
    	///
    	/// <para>Bước <b>đang làm</b> tô vàng và có số đếm; bước đã qua tô xám;
    	/// bước chưa tới để trắng mờ. Không tô gì thì cả danh sách trông như nhau
    	/// và người chơi phải tự đoán mình đang ở đâu.</para>
    	///
    	/// <para>Xin chỗ qua <see cref="xinChoCotTrai"/> nên nó tự nằm dưới bảng
    	/// tên bản đồ, và mọi thứ phía dưới tự tụt xuống theo chiều cao thật của
    	/// bảng này.</para>
    	/// </remarks>
    	public static void veBangNhiemVu(mGraphics g)
    	{
    		Char c = Char.myCharz();
    		if (c == null || c.taskMaint == null || c.taskMaint.names == null
    				|| c.taskMaint.names.Length == 0)
    		{
    			return;
    		}
    		Task t = c.taskMaint;

    		// Thu gon = thu HAN ve mot o vuong nho, khong giu lai dai tieu de.
    		//
    		// Ban truoc van ve nguyen dai "Nhiem vu" rong 187 diem — thu gon ma
    		// gan nhu khong doi lai duoc bao nhieu cho, dung y bam nut la de lay
    		// lai cho.
    		if (thuGonNhiemVu)
    		{
    			int oc = 20;
    			int xc = LE_COT_TRAI;
    			int ycc = xinChoCotTrai(oc + 4);
    			veNenBangHud(g, xc, ycc, oc, oc);
    			// Net day 2 diem va lui nua do day, khong thi dau cong lech mot
    			// diem ve mot ben tren o co canh chan.
    			g.setColor(0xFFE9A3, 1f);
    			g.fillRect(xc + 5, ycc + (oc - 2) / 2, oc - 10, 2);
    			g.fillRect(xc + (oc - 2) / 2, ycc + 5, 2, oc - 10);
    			oThuGonNhiemVu = new int[] { xc, ycc, oc, oc };
    			oBangNhiemVu = new int[0];
    			oCuonNhiemVuLen = new int[0];
    			oCuonNhiemVuXuong = new int[0];
    			return;
    		}

    		// Dung san danh sach dong roi moi do chieu cao: ve truoc roi doan
    		// chieu cao thi vien luon lech mot nhip so voi chu.
    		System.Collections.Generic.List<string> ten =
    				new System.Collections.Generic.List<string>();
    		for (int i = 0; i < t.names.Length; i++)
    		{
    			if (t.names[i] != null && t.names[i].Length > 0)
    			{
    				ten.Add(t.names[i]);
    			}
    		}
    		System.Collections.Generic.List<string> buoc =
    				new System.Collections.Generic.List<string>();
    		System.Collections.Generic.List<int> vaiBuoc =
    				new System.Collections.Generic.List<int>();
    		if (t.subNames != null)
    		{
    			for (int i = 0; i < t.subNames.Length; i++)
    			{
    				if (t.subNames[i] == null || t.subNames[i].Length == 0)
    				{
    					continue;
    				}
    				// Khong con "- " dat truoc: dau hieu tinh trang duoc VE
    				// (xem veDauBuoc) nen mot gach ngang o day thanh thua.
    				string s = t.subNames[i];
    				if (i == t.index && t.counts != null && i < t.counts.Length
    						&& t.counts[i] > 1)
    				{
    					s += " (" + t.count + "/" + t.counts[i] + ")";
    				}
    				buoc.Add(s);
    				// 0 = da xong, 1 = dang lam, 2 = chua toi.
    				vaiBuoc.Add((i < t.index) ? 0 : ((i == t.index) ? 1 : 2));
    			}
    		}

    		int wKhung = RONG_BANG_NV;
    		int wChu = wKhung - 14;
    		// Ngat dong TRUOC khi do chieu cao, va ngat theo be rong that cua khung.
    		System.Collections.Generic.List<string> dong =
    				new System.Collections.Generic.List<string>();
    		System.Collections.Generic.List<int> vai =
    				new System.Collections.Generic.List<int>();
    		// Dong nay co phai dong DAU cua mot buoc khong — chi dong dau moi ve
    		// dau hieu tinh trang; dong xuong hang cua cung buoc thi khong.
    		System.Collections.Generic.List<bool> dauDong =
    				new System.Collections.Generic.List<bool>();
    		for (int i = 0; i < ten.Count; i++)
    		{
    			string[] a = mFont.tahoma_7b_yellow.splitFontArray(ten[i], wChu);
    			for (int j = 0; j < a.Length; j++)
    			{
    				dong.Add(a[j]);
    				vai.Add(3);
    				dauDong.Add(false);
    			}
    		}
    		// Buoc thut vao cho dau hieu, nen be rong chu con lai hep hon. Ngat
    		// theo dung be rong that, khong thi dong cuoi tho ra ngoai vien.
    		for (int i = 0; i < buoc.Count; i++)
    		{
    			string[] a = mFont.tahoma_7.splitFontArray(buoc[i], wChu - 10);
    			for (int j = 0; j < a.Length; j++)
    			{
    				dong.Add(a[j]);
    				vai.Add(vaiBuoc[i]);
    				dauDong.Add(j == 0);
    			}
    		}
    		if (dong.Count == 0)
    		{
    			return;
    		}

    		int CAO_TD = 15;
    		// Chi hien toi da SO_DONG_NV dong, phan con lai cuon.
    		//
    		// Nhiem vu dai muoi may buoc thi bang cao gan het canh trai man hinh,
    		// che mat ban do. Cat bot va cho cuon thi van doc duoc het ma khong
    		// chiem cho.
    		int soThay = dong.Count;
    		if (soThay > SO_DONG_NV)
    		{
    			soThay = SO_DONG_NV;
    		}
    		if (cuonNhiemVu > dong.Count - soThay)
    		{
    			cuonNhiemVu = dong.Count - soThay;
    		}
    		if (cuonNhiemVu < 0)
    		{
    			cuonNhiemVu = 0;
    		}
    		int hKhung = CAO_TD + 4 + soThay * 12 + 6;
    		int x = LE_COT_TRAI;
    		int y = xinChoCotTrai(hKhung + 4);

    		veNenBangHud(g, x, y, wKhung, hKhung);
    		oBangNhiemVu = new int[] { x, y, wKhung, hKhung };

    		// Dai tieu de: mot vach vang nhat cho phan dau tach khoi noi dung.
    		// Dai tieu de chuyen mau, ke tung dong nhu nen bang — mot mau phang o
    		// day nhin tach han ra khoi phan than da co do chuyen.
    		// Dai tieu de chi cao 13 diem ma ban kinh bo goc la 6 — KHONG con dai
    		// giua nao de ke tung dong ma khong cham goc. Ke nhu nen bang thi
    		// nhung dong o tren cung va duoi cung de VUONG len dung bon goc tron,
    		// dung canh "bo vien mau dang sai".
    		//
    		// Voi dai thap the nay, hai hinh bo goc chong len nhau la du: sang o
    		// tren, dam o duoi, goc van tron tuyet doi.
    		int hTD = CAO_TD - 2;
    		g.setColor(0xFFE3A6, 1f);
    		g.fillRect(x + 3, y + 3, wKhung - 6, hTD, 6);
    		g.setColor(0xE8B457, 1f);
    		g.fillRect(x + 3, y + 3 + hTD / 2, wKhung - 6, hTD - hTD / 2, 6);
    		mFont.tahoma_7b_dark.drawString(g, "Nhiệm vụ", x + wKhung / 2, y + 3,
    				mFont.CENTER);

    		// Dau thu gon o goc phai dai tieu de.
    		//
    		// Ghi lai vung bam NGAY TAI CHO VE: phan bat cham doc lai o nay nen
    		// hai ben khong the lech nhau. Dat so rieng o phan cham la doi mot
    		// ben thi ben kia sai am tham.
    		// Dau thu gon dat BEN TRAI dai tieu de.
    		//
    		// Ben phai la cho mat nhin luot qua cuoi cung; dat nut o do thi phai
    		// ra tan mep bang moi bam duoc. Ben trai nam ngay tren duong doc voi
    		// bang ten ban do va o chan dung phia tren, tay di thang mot mach.
    		int oTG = CAO_TD - 6;
    		int xTG = x + 6;
    		int yTG = y + 5;
    		g.setColor(0x3B2712, 0.85f);
    		g.fillRect(xTG, yTG, oTG, oTG, 3);
    		g.setColor(0xFFE9A3, 1f);
    		g.fillRect(xTG + 2, yTG + (oTG - 2) / 2, oTG - 4, 2);
    		oThuGonNhiemVu = new int[] { xTG, yTG, oTG, oTG };

    		int yc = y + CAO_TD + 4;
    		for (int k = 0; k < soThay; k++)
    		{
    			int i = k + cuonNhiemVu;
    			mFont f;
    			switch (vai[i])
    			{
    			case 3:
    				// Ten nhiem vu.
    				f = mFont.tahoma_7b_yellow;
    				break;
    			case 0:
    				// Da xong -> xanh la.
    				f = mFont.tahoma_7b_green2;
    				break;
    			case 1:
    				// Dang lam -> do, cho no bat han ra khoi danh sach.
    				f = mFont.tahoma_7b_red;
    				break;
    			default:
    				// Chua toi -> trang.
    				f = mFont.tahoma_7_white;
    				break;
    			}
    			int xChu = (vai[i] == 3) ? (x + 7) : (x + 17);
    			if (vai[i] != 3 && i < dauDong.Count && dauDong[i])
    			{
    				veDauBuoc(g, x + 9, yc + k * 12 + 4, vai[i]);
    			}
    			// Ve MOT lan, khong kem font bong.
    			//
    			// Doi so cuoi cua drawString la font ve chong phia sau lam bong.
    			// Chu nho ma co bong xam thi hai lop net dinh vao nhau va doc ra
    			// mo — nen mo la vi the, khong phai vi mau chu.
    			f.drawString(g, dong[i], xChu, yc + k * 12, mFont.LEFT);
    		}
    		// Hai nut cuon, chi hien khi con dong bi cat.
    		if (dong.Count > soThay)
    		{
    			int xN = x + wKhung - 13;
    			veNutCuonNV(g, xN, yc - 1, true);
    			veNutCuonNV(g, xN, yc + soThay * 12 - 11, false);
    			oCuonNhiemVuLen = new int[] { xN, yc - 1, 11, 11 };
    			oCuonNhiemVuXuong = new int[] { xN, yc + soThay * 12 - 11, 11, 11 };
    		}
    		else
    		{
    			oCuonNhiemVuLen = new int[0];
    			oCuonNhiemVuXuong = new int[0];
    		}
    	}

    	/// <summary>Ô chân dung của người chơi ở góc trên bên trái.</summary>

    	/// <remarks>
    	/// Ba hằng số này là <b>nguồn duy nhất</b> cho cả phần vẽ lẫn vùng bấm
    	/// (<c>ClientManager.chamNutHud</c> đọc lại chúng). Trước đây hai nơi đặt
    	/// số riêng, nên đổi cỡ chân dung một bên là vùng bấm lệch ngay mà không
    	/// có dấu hiệu gì.
    	/// </remarks>
    	/// <summary>Vùng ô chân dung ở khung hình này: x, y, rộng, cao.</summary>
    	/// <remarks>
    	/// Phần bắt chạm đọc ô này chứ không tự dựng lại từ ba hằng số bên dưới.
    	/// Đo bằng nhật ký: mọi cú bấm đều ra <c>trungAvt=False</c>, kể cả khi bấm
    	/// đúng giữa ảnh đầu — hai bên đang không nói cùng một hệ toạ độ. Lấy
    	/// thẳng ô mà chỗ vẽ vừa dùng thì chúng không thể lệch nhau nữa.
    	/// </remarks>
    	public static int[] oChanDung = new int[0];

    	// ------------------------------------------------------------------
    	//  Thu gọn cụm ô kỹ năng
    	// ------------------------------------------------------------------

    	/// <summary>Cụm ô kỹ năng đang thu gọn.</summary>
    	/// <remarks>
    	/// Chỉ dùng ở bản cảm ứng. Bản máy tính bấm kỹ năng bằng phím số, thu hàng
    	/// ô lại thì mất luôn chỗ đọc phím tắt nào ứng với kỹ năng nào.
    	/// </remarks>
    	public static bool thuGonKyNang;

    	/// <summary>Đã đọc lựa chọn thu gọn từ bộ nhớ máy chưa.</summary>
    	private static bool daDocThuGon;

    	/// <summary>Cạnh một nút thu gọn.</summary>
    	public const int CO_NUT_THU_GON = 14;

    	/// <summary>
    	/// Ô nút thu gọn cụm ô kỹ năng — <b>ngay trên góc trái hàng ô</b>.
    	/// </summary>
    	/// <remarks>
    	/// Chỗ này không đổi khi cụm đã gọn, nên nút mở lại luôn nằm đúng chỗ vừa
    	/// bấm. Neo vào <c>xSkill</c>/<c>ySkill</c> — hai con số vẫn được tính dù
    	/// hàng ô có được vẽ hay không.
    	/// </remarks>
    	public static int[] oThuGonKyNang()
    	{
    		int[] xs = xS;
    		int yTren = (xs != null && xs.Length > 5) ? (ySkill - 32) : ySkill;
    		return new int[] { xSkill, yTren - CO_NUT_THU_GON - 3,
    			CO_NUT_THU_GON, CO_NUT_THU_GON };
    	}

    	/// <summary>
    	/// Nút thu gọn: khung nhỏ, dấu trừ khi đang mở, dấu cộng khi đã gọn.
    	/// </summary>
    	/// <remarks>
    	/// Dùng lại khung của nút nhanh nên nó đọc ra cùng một bộ với cả dải HUD,
    	/// không phải một cái nút lạ dán thêm vào.
    	/// </remarks>
    	public static void veNutThuGon(mGraphics g, int[] o, bool dangGon)
    	{
    		if (o == null || o.Length != 4)
    		{
    			return;
    		}
    		veKhungNutNhanh(g, o[0], o[1], o[2], o[3], false);
    		g.setColor(0xFFE9A3, 0.95f);
    		g.fillRect(o[0] + 4, o[1] + o[3] / 2 - 1, o[2] - 8, 2, 1);
    		if (dangGon)
    		{
    			g.fillRect(o[0] + o[2] / 2 - 1, o[1] + 4, 2, o[3] - 8, 1);
    		}
    	}

    	public const int X_CHAN_DUNG = 4;

    	public const int Y_CHAN_DUNG = 3;
    	public const int D_CHAN_DUNG = 34;

    	/// <summary>Mép dưới của khung nhân vật — chỗ khác xếp tiếp từ đây xuống.</summary>
    	public static int dayKhungNguoiChoi()
    	{
    		return Y_CHAN_DUNG + D_CHAN_DUNG + 4 + mGraphics.addYWhenOpenKeyBoard;
    	}

    	/// <summary>Bề rộng và bề cao thanh máu nổi trên đầu mục tiêu.</summary>
    	public const int RONG_THANH_NOI = 34;
    	public const int CAO_THANH_NOI = 6;

    	/// <summary>
    	/// Thanh máu nổi trên đầu quái, boss, NPC hay người chơi khác.
    	/// </summary>
    	/// <remarks>
    	/// <para>Vẽ bằng mã thay cho bộ ảnh <c>imgHP_tm_xam</c> /
    	/// <c>imgHP_tm_do</c> / <c>_vang</c> / <c>_xanh</c> cắt theo
    	/// <c>drawRegion</c>. Bốn ảnh đó chỉ có một cỡ, một kiểu, và muốn đổi màu
    	/// thì phải xuất lại ảnh; vẽ bằng mã thì nét ở mọi mức phóng to và đổi màu
    	/// chỉ là đổi một hằng số.</para>
    	///
    	/// <para><paramref name="perVet"/> là mức máu <b>trước cú đánh vừa rồi</b>.
    	/// Phần chênh với mức hiện tại vẽ màu trắng và tụt dần — nhờ vệt đó người
    	/// chơi thấy được mình vừa lấy đi bao nhiêu, thay vì chỉ thấy thanh ngắn
    	/// đi một cái.</para>
    	/// </remarks>
    	public static void veThanhMauNoi(mGraphics g, int tamX, int y, int rong,
    			int cao, int per, int perVet)
    	{
    		if (rong <= 0 || cao <= 0)
    		{
    			return;
    		}
    		if (per < 0)
    		{
    			per = 0;
    		}
    		if (per > 100)
    		{
    			per = 100;
    		}
    		int x = tamX - rong / 2;
    		int r = cao / 2;

    		// Vien vang bo goc, ve lot phia sau.
    		g.setColor(MAU_VANG, 0.95f);
    		g.fillRect(x - 1, y - 1, rong + 2, cao + 2, r + 1);
    		// Ranh chim.
    		g.setColor(0x000000, 0.62f);
    		g.fillRect(x, y, rong, cao, r);

    		// Vet mau vua mat.
    		if (perVet > per)
    		{
    			if (perVet > 100)
    			{
    				perVet = 100;
    			}
    			g.setColor(0xFFFFFF, 0.85f);
    			g.fillRect(x, y, rong * perVet / 100, cao, r);
    		}

    		int mauDam;
    		int mauSang;
    		if (per < 30)
    		{
    			mauDam = 0xA81414;
    			mauSang = 0xFF5A5A;
    		}
    		else if (per < 60)
    		{
    			mauDam = 0xB5760C;
    			mauSang = 0xFFC64D;
    		}
    		else
    		{
    			mauDam = 0x1B7A2E;
    			mauSang = 0x63E07E;
    		}
    		int w = rong * per / 100;
    		if (w > 0)
    		{
    			veThanhMau(g, x, y, w, cao, mauDam, mauSang);
    		}
    	}

    	/// <summary>
    	/// Tên: chữ trắng, viền tối bao quanh, nét dày hơn chữ thường.
    	/// </summary>
    	/// <remarks>
    	/// <para>Không dùng <c>tahoma_8b</c> dù nó to hơn: bộ chữ đó nướng
    	/// sẵn màu NÂU trong ảnh, đặt lên nền nâu của khung là chìm hẳn.
    	/// Trong cả bộ font của game không có bản đậm màu trắng nào lớn hơn
    	/// <c>tahoma_7b_white</c>.</para>
    	///
    	/// <para>Bù lại bằng cách vẽ chồng: viền tối tám hướng rồi chữ trắng
    	/// đè lên, cộng một lượt trắng lệch một điểm cho nét dày lên. Nhìn
    	/// to và rõ hơn hẳn một lượt vẽ đơn, mà vẫn trắng.</para>
    	/// </remarks>
    	private static void veTenDay(mGraphics g, string s, int x, int y, int canh)
    	{
    		if (s == null || s.Length == 0)
    		{
    			return;
    		}
    		// MOT net bong duy nhat, khong to vien bon phia.
    		//
    		// Ban truoc ve vien nam huong roi con to them mot luot trang lech mot
    		// diem. Net day len that, nhung voi co chu nho thi cac net dinh vao
    		// nhau va doc ra nhoe han. Mot bong cheo la du de chu noi khoi moi nen.
    		mFont.tahoma_7b_dark.drawString(g, s, x + 1, y + 1, canh);
    		mFont.tahoma_7b_white.drawString(g, s, x, y, canh);
    	}

    	/// <summary>Chu trang co bong mot diem — doc duoc tren nen bat ky.</summary>


    	private static void veChuNoi(mGraphics g, mFont f, string s, int x, int y, int canh)
    	{
    		if (s == null || s.Length == 0)
    		{
    			return;
    		}
    		mFont.tahoma_7b_dark.drawString(g, s, x + 1, y + 1, canh);
    		f.drawString(g, s, x, y, canh);
    	}

    	/// <summary>
    	/// Thanh trang thai co ranh chim va vien vang, chu ghi giua thanh.
    	/// </summary>
    	private static void veThanhCoKhung(mGraphics g, int x, int y, int w, int h,
    			long hienTai, long toiDa, int mauDam, int mauSang, string chu)
    	{
    		if (w <= 0 || h <= 0)
    		{
    			return;
    		}
    		int r = h / 2;
    		// Vien vang BO GOC, ve lot phia sau.
    		//
    		// Ban truoc vien bang `drawRect` — ham do khong nhan ban kinh nen no
    		// ve mot khung VUONG bao quanh mot cai ranh da bo tron. Nhin ra bon
    		// goc vuong tho ra ngoai duong cong. Ve mot hinh bo goc lot phia sau
    		// roi to ranh de len thi vien luon cong dung theo ruot.
    		g.setColor(MAU_VANG, 0.95f);
    		g.fillRect(x - 1, y - 1, w + 2, h + 2, r + 1);
    		// Ranh chim: nen toi de ruot mau noi han len.
    		g.setColor(0x000000, 0.55f);
    		g.fillRect(x, y, w, h, r);

    		long max = (toiDa > 0L) ? toiDa : 1L;
    		long cur = hienTai;
    		if (cur < 0L)
    		{
    			cur = 0L;
    		}
    		if (cur > max)
    		{
    			cur = max;
    		}
    		int wf = (int)(cur * w / max);
    		if (wf > 0)
    		{
    			veThanhMau(g, x, y, wf, h, mauDam, mauSang);
    		}

    		// Chu NHO tren thanh mau.
    		//
    		// Co chu cu (tahoma_7b_white, cao 11) gan bang chieu cao ca thanh, nen
    		// so day het long thanh va cham ca vao dong ten ngay phia tren. Ban nho
    		// van doc duoc so ma cach ten ra han.
    		//
    		// Can giua theo chieu cao THAT cua font chu khong tru mot so cung: doi
    		// font mot cai la con so cung do sai ngay.
    		mFont fSo = (mFont.tahoma_7_whiteSmall != null)
    				? mFont.tahoma_7_whiteSmall : mFont.tahoma_7b_white;
    		// Ve THANG, khong qua veChuNoi.
    		//
    		// veChuNoi to net bong bang tahoma_7b_dark — mot font co LON. Ghep no
    		// voi font nho o day thi net bong to hon chinh chu no do bong, thanh ra
    		// mot mang den lem nhem quanh con so. Long thanh von da toi san nen chu
    		// trang doc ro ma khong can bong.
    		// Thu nho con so con BON PHAN NAM.
    		//
    		// Bo phong chu nay khong co ban nho hon: moi muc phong nap dung mot tep
    		// phong tu FontSys/x{zoom}, va co chu la co cua chinh tep do. Nen muon
    		// nho hon thi phai thu bang ma tran GUI.
    		//
    		// Diem neo tinh bang PIXEL MAN HINH = toa do game nhan zoomLevel CONG do
    		// doi hien tai — dung getTranslateXPx/YPx chu khong phai getTranslateX/Y
    		// (hai ham kia da chia zoom va cong addYWhenOpenKeyBoard).
    		int yChu = y + (h - fSo.getHeight()) / 2;
    		UnityEngine.Matrix4x4 luuMaTran = UnityEngine.GUI.matrix;
    		UnityEngine.GUIUtility.ScaleAroundPivot(
    				new UnityEngine.Vector2(0.8f, 0.8f),
    				new UnityEngine.Vector2(
    						(x + w / 2) * mGraphics.zoomLevel + g.getTranslateXPx(),
    						(yChu + fSo.getHeight() / 2) * mGraphics.zoomLevel
    								+ g.getTranslateYPx()));
    		fSo.drawString(g, chu, x + w / 2, yChu, mFont.CENTER);
    		UnityEngine.GUI.matrix = luuMaTran;
    	}

    	/// <summary>
    	/// Khung trang thai cua nguoi choi: chan dung tron, ten, thanh HP va KI.
    	/// </summary>
    	/// <remarks>
    	/// Ve hoan toan bang ma thay cho <c>imgPanel</c> + <c>imgHP</c>/<c>imgMP</c>
    	/// cat theo <c>setClip</c>. Anh chi co mot co: doi chieu cao thanh la re
    	/// nhoe, va doi mau thi phai xuat lai anh. Ve bang ma thi net o moi muc
    	/// phong to va doi mau chi la doi mot hang so.
    	///
    	/// KHONG dung o man VS: nhanh do van giu <c>paintImageBar</c> cu vi bo cuc
    	/// hai ben trai/phai gan chat voi kich thuoc cua anh nen.
    	/// </remarks>
    	public static void veKhungNguoiChoi(mGraphics g, Char c)
    	{
    		if (c == null)
    		{
    			return;
    		}
    		int x0 = X_CHAN_DUNG;
    		int y0 = Y_CHAN_DUNG + mGraphics.addYWhenOpenKeyBoard;
    		int d = D_CHAN_DUNG;
    		int xB = x0 + d + 7;
    		// Thanh dai 108, va con so tren no da duoc thu nho bon phan nam.
    		//
    		// Con so ghi tren thanh la "17.605.032/21.069.430" — hai muoi mot ky tu.
    		// Tren thanh 96 diem no cham hai mep va tho ca ra ngoai. Cho thanh 120
    		// thi vua, nhung ca khung nhan vat rong ra theo va an mat cho o goc tren
    		// trai. Thu nho con so roi thi 108 la du, ma khung gon lai duoc.
    		int wB = 108;

    		// Tam bang OM SAT hai thanh mau.
    		//
    		// Ban truoc dat be rong bang mot phep cong roi rac (wB + d/2 + 14) nen
    		// mep phai cua bang khong lien quan gi toi mep phai cua thanh — thua
    		// ra mot dai xam. Gio tinh THANG tu mep phai cua thanh cong le, nen
    		// vien luon bam theo thanh du sau nay co doi wB.
    		int xBang = x0 + d / 2;
    		int yBang = y0;
    		// Le phai rong bang le trai cua thanh so voi mep bang, va bang cao
    		// dung bang chan dung — de ten va hai thanh nam GON trong vien chu
    		// khong tho ra ngoai mot vai diem.
    		int wBang = (xB + wB + 9) - xBang;

    		// Bo cuc doc tinh TRUOC khi ve nen, va tinh TU TREN XUONG.
    		//
    		// Ban truoc neo hai thanh vao DAY khung roi dat ten o tren, nen khi
    		// khung thap di (chan dung tu 44 xuong 38) thi dong ten va thanh HP an
    		// vao nhau — dung canh ten de len thanh mau.
    		//
    		// Tinh xuoi tu dong ten thi khoang ho giua ten va thanh luon dung bang
    		// so da khai, du sau nay co doi co chu hay co chan dung.
    		int caoThanh = 8;
    		int cachThanh = 2;
    		int caoTen = mFont.tahoma_7b_white.getHeight();
    		int yTen = yBang + 2;
    		int yThanh1 = yTen + caoTen + 2;
    		int yThanh2 = yThanh1 + caoThanh + cachThanh;

    		// Khung phai du cao cho ca ba dong. Chan dung van la muc toi thieu de
    		// vien khong cat ngang vong tron.
    		int hBang = yThanh2 + caoThanh + 3 - yBang;
    		if (hBang < d)
    		{
    			hBang = d;
    		}
    		veNenBangHud(g, xBang, yBang, wBang, hBang);

    		// Vung bam lay CA khung, khong chi rieng vong tron chan dung.
    		//
    		// Vong tron canh 44 la muc tieu nho, va do bang nhat ky thi khong cu
    		// bam nao cua nguoi dung roi trung no. Ca khung — chan dung cong hai
    		// thanh mau — la mot muc tieu rong gap ba, va bam vao dau trong do
    		// cung deu co nghia la "xem nhan vat cua toi".
    		// Vung bam PHU KIN goc tren ben trai, khong chi rieng khung.
    		//
    		// Do bang nhat ky: bam nua phai cua khung thi mo duoc, nua trai
    		// thi khong — nen le ra vung bam phai an ca hai nua. Cho no chay
    		// tu SAT MEP (0,0): bam vao bat cu dau o goc tren trai cung chi co
    		// mot nghia la "xem nhan vat cua toi".
    		oChanDung = new int[] { 0, 0, xB + wB + 10, y0 + d + 4 };

    		veChanDung(g, x0 + d / 2, y0 + d / 2, d, maAnhDau(c));

    		veTenDay(g, c.cName, xB + 2, yTen, mFont.LEFT);
    		veThanhCoKhung(g, xB, yThanh1, wB, caoThanh, (long)c.cHP, (long)c.cHPFull,
    				MAU_HP_DAM, MAU_HP_SANG,
    				NinjaUtil.getMoneys((long)(c.cHP)) + "/" + NinjaUtil.getMoneys((long)(c.cHPFull)));
    		veThanhCoKhung(g, xB, yThanh2, wB, caoThanh, c.cMP, c.cMPFull,
    				MAU_KI_DAM, MAU_KI_SANG,
    				NinjaUtil.getMoneys(c.cMP) + "/" + NinjaUtil.getMoneys(c.cMPFull));

    		// Cong bo mep phai cua khung, de nut banh rang dung sat canh no.
    		//
    		// Nut do von neo vao mot con so cung (X_BANH_RANG = 190) nen moi lan
    		// khung nhan vat doi be rong la no lai ho ra hoac de len.
    		mepPhaiKhungNguoiChoi = xBang + wBang;
    	}

    	/// <summary>Mép phải của khung nhân vật ở khung hình vừa vẽ.</summary>
    	/// <remarks>
    	/// Kiểu <b>công bố ô vừa vẽ</b>: chỗ vẽ ghi lại con số nó vừa dùng, chỗ khác
    	/// đọc lại — nhờ thế hai bên không thể lệch nhau.
    	/// </remarks>
    	public static int mepPhaiKhungNguoiChoi = 190;

    	/// <summary>
    	/// Khung doi tuong dang chon, dat giua canh tren.
    	/// </summary>
    	/// <remarks>
    	/// Giu NGUYEN thu tu uu tien cua ban cu — cua vao/ra, quai, NPC, nguoi
    	/// choi, roi chinh minh — chi doi cach ve. Doi thu tu la doi hanh vi, ma
    	/// day chi la viec nang cap giao dien.
    	///
    	/// Thanh mau chi ve khi biet CHAC muc toi da. Quai co <c>maxHp</c>; NPC
    	/// thi khong co gi de do, ve mot thanh day 100% la bia ra so lieu.
    	/// </remarks>
    	private void veKhungMucTieu(mGraphics g)
    	{
    		Char toi = Char.myCharz();
    		if (toi == null)
    		{
    			return;
    		}
    		if (toi.isInEnterOfflinePoint() != null || toi.isInEnterOnlinePoint() != null)
    		{
    			veBangMuc(g, mResources.enter, null, -1, 0L, 0L);
    			return;
    		}
    		if (toi.mobFocus != null)
    		{
    			string ten = (toi.mobFocus.getTemplate() != null)
    					? toi.mobFocus.getTemplate().name : "";
    			if (toi.mobFocus.templateId != 0)
    			{
    				veBangMuc(g, ten, null, -1, toi.mobFocus.hp, toi.mobFocus.maxHp);
    			}
    			else
    			{
    				veBangMuc(g, ten, null, -1, 0L, 0L);
    			}
    			return;
    		}
    		if (toi.npcFocus != null)
    		{
    			string phu = null;
    			if (toi.npcFocus.template.npcTemplateId == 4 && gI().magicTree != null)
    			{
    				phu = gI().magicTree.currPeas + "/" + gI().magicTree.maxPeas;
    			}
    			veBangMuc(g, toi.npcFocus.template.name, phu,
    					maAnhDauTheoBoPhan(toi.npcFocus.template.headId), 0L, 0L);
    			return;
    		}
    		if (toi.charFocus != null && toi.charFocus.isPet)
    		{
    			// Pet ve TOAN THAN chu khong lay moi cai dau.
    			//
    			// maAnhDau() tra ve manh anh "dau" cua bo phan — voi pet thi manh
    			// do chi la cai mom, nhin ra khong biet la con gi. Ve ca than nhu
    			// con dang dung ngoai ban do thi nhan ra ngay.
    			veBangPet(g, toi.charFocus);
    			return;
    		}
    		if (toi.charFocus != null)
    		{
    			// Nguoi choi khac cung dung DAU that, giong khung cua minh: avatar
    			// la chan dung co dinh theo hanh tinh nen ai cung nhu ai.
    			veBangMuc(g, toi.charFocus.cName, null, maAnhDau(toi.charFocus),
    					(long)toi.charFocus.cHP, (long)toi.charFocus.cHPFull);
    			return;
    		}
    		veBangMuc(g, toi.cName, NinjaUtil.getMoneys(toi.cPower) + "", -1, 0L, 0L);
    	}

    	/// <summary>
    	/// Bảng mục tiêu dành riêng cho <b>đệ tử</b>: vẽ cả thân và tên của nó.
    	/// </summary>
    	/// <remarks>
    	/// Đệ tử không có tên riêng gửi kèm trong <c>cName</c> ở mọi trường hợp,
    	/// nên khi trống thì lấy "Đệ tử" — thà một nhãn đúng chung còn hơn một ô
    	/// trống không cho biết đang chọn cái gì.
    	/// </remarks>
    	/// <summary>Ô trang bị giữ vật phẩm Pet.</summary>
    	/// <remarks>
    	/// Số 7 lấy theo bảng tên ô của màn trang bị: Áo, Quần, Găng, Giày,
    	/// Rađa, Cải trang, Giáp luyện tập, <b>Pet</b>, Đeo lưng, Ván bay, Sách,
    	/// Chân mệnh.
    	/// </remarks>
    	private const int O_PET = 7;

    	/// <summary>Tên vật phẩm Pet đang đeo, hoặc <c>null</c>.</summary>
    	private static string tenVatPhamPet()
    	{
    		Item[] mac = Char.myCharz().arrItemBody;
    		if (mac == null || O_PET >= mac.Length)
    		{
    			return null;
    		}
    		Item it = mac[O_PET];
    		if (it == null || it.template == null || it.template.name == null
    				|| it.template.name.Length == 0)
    		{
    			return null;
    		}
    		return it.template.name;
    	}


    	private static void veBangPet(mGraphics g, Char pet)
    	{
    		// TÊN CỦA CHÍNH ĐỐI TƯỢNG ĐANG CHỌN đứng trước.
    		//
    		// Bản trước lấy tên VẬT PHẨM PET đang đeo (`tenVatPhamPet()`) làm
    		// nguồn thứ nhất. Nhưng khung này dùng chung cho hai thứ khác hẳn
    		// nhau: con thú cưng đeo ở ô Pet, và ĐỆ TỬ. Bấm vào đệ tử thì nó vẫn
    		// đi lấy tên món đồ trong ô Pet — ra "Pet Capybara đeo ba lô", tức là
    		// tên của một con khác đang đứng ngay cạnh.
    		//
    		// `cName` KHÔNG rỗng như ghi chú cũ nói: máy chủ gửi tên đệ tử kèm
    		// dấu `$` ở đầu (xem DetuService — "$Đệ tử", "$Mabư", "$Ubu"...), và
    		// `Char.paintName` cắt dấu đó ra rồi bật cờ `isPet`. Nên tới đây
    		// `cName` đã là tên thật của con đang chọn.
    		//
    		// Vật phẩm pet chỉ còn là đường lùi, cho trường hợp gói tin chưa mang
    		// tên tới.
    		string ten = (pet.cName != null && pet.cName.Length > 0)
    				? pet.cName : tenVatPhamPet();
    		if (ten == null || ten.Length == 0)
    		{
    			ten = "Đệ tử";
    		}
    		int d = D_CHAN_DUNG;
    		string chuMau = NinjaUtil.getMoneys((long)(pet.cHP)) + "/"
    				+ NinjaUtil.getMoneys((long)(pet.cHPFull));
    		int wB = rongThanhMuc(chuMau, d);
    		int y0 = 3 + mGraphics.addYWhenOpenKeyBoard;
    		int cao = d;
    		int rong = wB + d / 2 + 15;
    		int x0 = xBangMucTieu(rong, d / 2);
    		int xBang = x0 + d / 2;
    		veNenBangHud(g, xBang, y0, rong, cao);

    		// Vong tron nhu chan dung, nhung long la CA THAN con pet.
    		int tamX = x0 + d / 2;
    		int tamY = y0 + cao / 2;
    		int r = d / 2;
    		g.setColor(MAU_VANG, 1f);
    		g.fillRect(tamX - r, tamY - r, d, d, r);
    		g.setColor(MAU_VANG_SANG, 1f);
    		g.fillRect(tamX - r + 2, tamY - r + 2, d - 4, d - 4, (d - 4) / 2);
    		g.setColor(0x3B2712, 1f);
    		g.fillRect(tamX - r + 3, tamY - r + 3, d - 6, d - 6, (d - 6) / 2);
    		int nuaCanh = (r * 7) / 10;
    		g.setClip(tamX - nuaCanh, tamY - nuaCanh, nuaCanh * 2, nuaCanh * 2);
    		// Ve CA con pet — dau, than, chan — chu khong chi moi cai dau.
    		//
    		// Anh dau khong du: nhieu con pet gan nhu ca hinh nam o phan than, lay
    		// moi dau thi trong o chi thay mot mau to.
    		//
    		// Rap tu bo phan thi phai chon dung KHUNG HINH trong bang CharInfo. Ep
    		// khung 0 la sai voi nhung con pet co bo phan rieng — Voi Chin Nga
    		// chang han — cac manh roi vao sai cho va hien ra mot dam ron. Lay dung
    		// khung va huong con pet dang co ngoai ban do (pet.cf, pet.cdir) thi con
    		// nao cung ve giong het luc no dung tren ban do.
    		//
    		// CAO_PET la chieu cao uoc luong cua mot con pet theo toa do game. Ti le
    		// tinh nguoc tu chieu cao o cat nen ca con lot gon trong vanh, khong bi
    		// xen dau xen chan. Con nao trong ra ve nhinh hon o cat thi nang so nay.
    		const int CAO_PET = 52;
    		float tiLePet = (float) (nuaCanh * 2) / CAO_PET;
    		UnityEngine.Matrix4x4 luuMaTran = UnityEngine.GUI.matrix;
    		// Diem neo phai tinh bang PIXEL MAN HINH, tuc nhan them zoomLevel.
    		// mGraphics tu nhan zoomLevel vao toa do truoc khi ve (xem setClip,
    		// fillRect...), nghia la ma tran GUI lam viec bang pixel man hinh chu
    		// khong phai toa do game. Truyen thang toa do game thi diem neo roi vao
    		// nua duong va ca hinh bi keo lech sang trai.
    		UnityEngine.GUIUtility.ScaleAroundPivot(
    				new UnityEngine.Vector2(tiLePet, tiLePet),
    				new UnityEngine.Vector2(tamX * mGraphics.zoomLevel,
    						tamY * mGraphics.zoomLevel));
    		try
    		{
    			// Khung DUNG YEN, huong sang phai: cdir = 1, cf = 0.
    			//
    			// Lay khung dang chay (pet.cf) thi anh trong o nhay theo tung buoc
    			// chan cua con pet, va luc no danh nhau thi tay chan vung ra ngoai
    			// vanh. O chan dung thi mot tu the dung yen la du.
    			//
    			// paintCharBody neo theo CHAN, nen ha chan xuong nua than thi ca
    			// con nam can giua o cat.
    			pet.paintCharBody(g, tamX, tamY + CAO_PET / 2, 1, 0, false);
    		}
    		catch (System.Exception)
    		{
    			// Anh chua tai ve thi bo qua, dung de vo ca khung hinh.
    		}
    		UnityEngine.GUI.matrix = luuMaTran;
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);

    		int xB = x0 + d + 7;
    		veTenChay(g, ten, xB, y0 + 3, wB);
    		veThanhCoKhung(g, xB, y0 + 18, wB, 10, (long)pet.cHP, (long)pet.cHPFull,
    				MAU_HP_DAM, MAU_HP_SANG, chuMau);
    	}

    	/// <summary>
    	/// Toạ độ trái của bảng mục tiêu — <b>căn giữa, nhưng né nút bánh răng</b>.
    	/// </summary>
    	/// <remarks>
    	/// Bảng mục tiêu căn giữa bề ngang màn hình. Trên bản điện thoại màn hẹp,
    	/// nửa bề ngang lại rơi đúng vào chỗ nút bánh răng gắn cạnh khung nhân vật,
    	/// nên vòng tròn chân dung đè lên nửa cái bánh răng.
    	/// <para>Đẩy sang phải cho qua nút ấy, nhưng không đẩy tới mức thò ra ngoài
    	/// mép phải — màn hình quá hẹp thì thà chạm nút còn hơn mất nửa cái tên.</para>
    	/// </remarks>
    	private static int xBangMucTieu(int rong, int lechTrai)
    	{
    		int tong = rong + lechTrai;
    		int x = GameCanvas.w / 2 - tong / 2;
    		int toiThieu = God.ClientManager.mepPhaiBanhRang() + 6;
    		if (x < toiThieu)
    		{
    			x = toiThieu;
    		}
    		// Mep phai dung TRUOC nut "Co" o goc tren phai, khong phai truoc mep
    		// man hinh. Hai thu nam cung mot hang ngang, de sat nhau thi doc ra mot
    		// khoi lien — dung canh trong anh chup.
    		int toiDa = TabControll.mepTraiNutCo() - 8 - tong;
    		if (x > toiDa && toiDa > toiThieu)
    		{
    			x = toiDa;
    		}
    		return x;
    	}

    	/// <summary>
    	/// Biểu tượng kỹ năng, <b>thu nhỏ cho lọt vào lòng ô bo góc</b>.
    	/// </summary>
    	/// <remarks>
    	/// <para>Lòng ô rộng 26 mà ảnh biểu tượng là bitmap cố định 24 điểm, nên chỉ
    	/// còn một điểm lề mỗi bên: bốn góc vuông của ảnh ăn thẳng vào đường cong
    	/// của ô.</para>
    	///
    	/// <para>Không cắt ảnh thành hình bo góc được — <c>drawRegion</c> đi qua
    	/// <c>Graphics.DrawTexture</c>, hàm đó không đọc vùng cắt của GUI. Nhưng nó
    	/// <i>có</i> đọc ma trận GUI, nên thu nhỏ ảnh một chút là bốn góc lùi vào
    	/// trong đường cong, đọc ra đúng như ảnh được bo theo ô.</para>
    	/// </remarks>
    	private static void veIconKyNang(mGraphics g, Skill skill, int tamX, int tamY)
    	{
    		if (skill == null)
    		{
    			return;
    		}
    		UnityEngine.Matrix4x4 luuMaTran = UnityEngine.GUI.matrix;
    		UnityEngine.GUIUtility.ScaleAroundPivot(
    				new UnityEngine.Vector2(0.86f, 0.86f),
    				new UnityEngine.Vector2(
    						tamX * mGraphics.zoomLevel + g.getTranslateXPx(),
    						tamY * mGraphics.zoomLevel + g.getTranslateYPx()));
    		skill.paint(tamX, tamY, g);
    		UnityEngine.GUI.matrix = luuMaTran;
    	}

    	/// <summary>Bề rộng tối thiểu của thanh trên bảng mục tiêu.</summary>
    	private const int RONG_THANH_MUC = 66;

    	/// <summary>
    	/// Bề rộng thanh máu của bảng mục tiêu — <b>đủ chứa con số, và không hơn</b>.
    	/// </summary>
    	/// <remarks>
    	/// <para>Máu boss ghi tới "300.000.000/300.000.000" — hai mươi ba ký tự.
    	/// Thanh cố định 66 điểm thì con số thò hẳn ra hai bên, đúng cảnh trong ảnh
    	/// chụp.</para>
    	///
    	/// <para>Nới ra theo con số thật, nhưng có trần: cả bảng phải lọt giữa nút
    	/// bánh răng bên trái và nút "Cờ" bên phải. Hai mốc ấy do chính chỗ vẽ chúng
    	/// công bố, nên trần này không bao giờ lệch khỏi bố cục thật.</para>
    	///
    	/// <para>Nhân bốn phần năm vì con số được vẽ thu nhỏ 0,8 lần trong
    	/// <c>veThanhCoKhung</c>.</para>
    	/// </remarks>
    	private static int rongThanhMuc(string chuSo, int d)
    	{
    		int w = RONG_THANH_MUC;
    		if (chuSo != null && chuSo.Length > 0)
    		{
    			mFont f = (mFont.tahoma_7_whiteSmall != null)
    					? mFont.tahoma_7_whiteSmall : mFont.tahoma_7b_white;
    			int can = f.getWidth(chuSo) * 4 / 5 + 12;
    			if (can > w)
    			{
    				w = can;
    			}
    		}
    		// Phan bang nam NGOAI thanh mau: chan dung cong hai le.
    		int phuThem = (d > 0) ? (d + 15) : 16;
    		int choToiDa = TabControll.mepTraiNutCo() - 8
    				- (God.ClientManager.mepPhaiBanhRang() + 6) - phuThem;
    		if (w > choToiDa)
    		{
    			w = choToiDa;
    		}
    		if (w < 40)
    		{
    			w = 40;
    		}
    		return w;
    	}

    	/// <summary>Mốc bắt đầu cho chữ chạy, và tên đang chạy.</summary>
    	private static long mocTenChay;

    	private static string tenDangChay;

    	/// <summary>
    	/// Tên trên bảng mục tiêu — <b>chạy từ phải sang trái nếu quá dài</b>.
    	/// </summary>
    	/// <remarks>
    	/// Tên vật phẩm pet có thể dài tới hai chục ký tự ("Pet Capybara đeo ba
    	/// lô"), gấp rưỡi bề ngang bảng. Trước đây nó cứ tràn ra ngoài, chạy đè cả
    	/// sang nút "Cờ" bên phải.
    	/// <para>Vừa chỗ thì đứng yên và căn giữa như cũ — chữ chạy khi không cần
    	/// thiết chỉ làm mỏi mắt.</para>
    	/// </remarks>
    	private static void veTenChay(mGraphics g, string ten, int x, int y, int w)
    	{
    		if (ten == null || ten.Length == 0 || w <= 0)
    		{
    			return;
    		}
    		mFont f = mFont.tahoma_7b_white;
    		int wT = f.getWidth(ten);
    		if (wT <= w)
    		{
    			veTenDay(g, ten, x + w / 2, y, mFont.CENTER);
    			return;
    		}
    		// Doi ten thi chay lai tu dau, khong noi tiep cho cua ten truoc.
    		if (!ten.Equals(tenDangChay))
    		{
    			tenDangChay = ten;
    			mocTenChay = mSystem.currentTimeMillis();
    		}
    		int khe = 26;
    		int chuKy = wT + khe;
    		int lech = (int) (((mSystem.currentTimeMillis() - mocTenChay) / 28)
    				% chuKy);
    		g.setClip(x, y - 2, w, f.getHeight() + 4);
    		veTenDay(g, ten, x - lech, y, mFont.LEFT);
    		// Ban thu hai chay ngay sau ban thu nhat, cho vong lap khong co khoang
    		// trong dai bang ca be rong khung.
    		veTenDay(g, ten, x - lech + chuKy, y, mFont.LEFT);
    		g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    	}

    	/// <summary>Mot bang muc tieu: chan dung (neu co), ten, va thanh mau (neu do duoc).</summary>

    	private static void veBangMuc(mGraphics g, string ten, string dongPhu,
    			int avatar, long hp, long hpMax)
    	{
    		if (ten == null)
    		{
    			ten = "";
    		}
    		// Cung co voi chan dung cua nguoi choi.
    		//
    		// De 30 thi anh dau cua NPC/boss — von to hon the — bi cat chi con
    		// giua mat, nhin ra mot khoi den khong nhan ra la ai.
    		int d = (avatar >= 0) ? D_CHAN_DUNG : 0;
    		string chuMau = (hpMax > 0L)
    				? (NinjaUtil.getMoneys(hp) + "/" + NinjaUtil.getMoneys(hpMax))
    				: null;
    		int wB = rongThanhMuc(chuMau, d);
    		int y0 = 3 + mGraphics.addYWhenOpenKeyBoard;

    		// Cao bang DUNG chan dung khi co anh, de vien om sat vong tron thay vi
    		// cat ngang qua no. Khong co anh thi bang gon lai vua hai dong chu.
    		int cao = (d > 0) ? (d - 4) : ((dongPhu != null) ? 30 : 18);
    		// Le HAI BEN bang nhau.
    		//
    		// Ban truoc cong 8 cho ca bang trong khi thanh mau bat dau o +8, nen
    		// ben trai thua 8 diem con ben phai khong con gi — thanh mau cham sat
    		// mep vien. Nhin ra lech han mot ben du ca hai deu "can giua".
    		int rong = wB + ((d > 0) ? (d / 2 + 15) : 16);
    		int x0 = xBangMucTieu(rong, (d > 0) ? d / 2 : 0);

    		// Nen day du nhu khung nhan vat: bong do, vien vang, long nau chuyen
    		// mau. Ban truoc chi la mot o den mo — nhin nhu mot mieng dan lot,
    		// khong an nhap gi voi khung ben trai.
    		int xBang = x0 + ((d > 0) ? d / 2 : 0);
    		veNenBangHud(g, xBang, y0 + 2, rong, cao);

    		int xB = xBang + 8;
    		if (d > 0)
    		{
    			veChanDung(g, x0 + d / 2, y0 + 2 + cao / 2, d, avatar);
    			xB = x0 + d + 7;
    		}
    		veTenChay(g, ten, xB, y0 + 3, wB);
    		if (hpMax > 0L)
    		{
    			veThanhCoKhung(g, xB, y0 + 18, wB, 10, hp, hpMax,
    					MAU_HP_DAM, MAU_HP_SANG, chuMau);
    		}
    		else if (dongPhu != null)
    		{
    			veChuNoi(g, mFont.tahoma_7b_white, dongPhu, xB + wB / 2, y0 + 18,
    					mFont.CENTER);
    		}
    	}

    	private void paintImageBar(mGraphics g, bool isLeft, Char c)

    	{
    		if (c != null)
    		{
    			int num = 0;
    			int num2 = 0;
    			int num3 = 0;
    			int num4 = 0;
			// Mau so an toan. May chu co luc gui cHPFull / cMPFull = 0 (nhan vat
			// vua tao, hoac goi chi so chua toi), va phep chia o duoi nem
			// DivideByZeroException GIUA luc ve => ca phan con lai cua khung
			// hinh khong kip ve, map hien ra vo tung manh.
			double hpFull = (c.cHPFull > 0) ? c.cHPFull : 1.0;
			int mpFull = (c.cMPFull > 0) ? c.cMPFull : 1;
    			if (c.charID == Char.myCharz().charID)
    			{
    				num = (int)(dHP * hpBarW / hpFull);
    				num2 = dMP * mpBarW / mpFull;
    				num3 = (int)(c.cHP * hpBarW / hpFull);
    				num4 = c.cMP * mpBarW / mpFull;
    			}
    			else
    			{
    				num = (int)(c.dHP * hpBarW / hpFull);
    				num2 = c.perCentMp * mpBarW / 100;
    				num3 = (int)(c.cHP * hpBarW / hpFull);
    				num4 = c.perCentMp * mpBarW / 100;
    			}
    			if (Char.myCharz().secondPower > 0)
    			{
    				int maxPP = (Char.myCharz().maxPowerPoint > 0) ? Char.myCharz().maxPowerPoint : 1;
				int w = Char.myCharz().powerPoint * spBarW / maxPP;
    				g.drawImage(imgPanel2, 58, 29, 0);
    				g.setClip(83, 31, w, 10);
    				g.drawImage(imgSP, 83, 31, 0);
    				g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    				mFont.tahoma_7_white.drawString(g, Char.myCharz().strInfo + ":" + Char.myCharz().powerPoint + "/" + Char.myCharz().maxPowerPoint, 115, 29, 2);
    			}
    			if (c.charID != Char.myCharz().charID)
    			{
    				g.setClip(mGraphics.getImageWidth(imgPanel) - 95, 0, 95, 100);
    			}
    			g.drawImage(imgPanel, 0, 0, 0);
    			if (isLeft)
    			{
    				g.setClip(83, 5, num, 10);
    			}
    			else
    			{
    				g.setClip((int)(83 + hpBarW - num), 5, num, 10);
    			}
    			g.drawImage(imgHPLost, 83, 5, 0);
    			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			if (isLeft)
    			{
    				g.setClip(83, 5, num3, 10);
    			}
    			else
    			{
    				g.setClip((int)(83 + hpBarW - num3), 5, num3, 10);
    			}
    			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			// Thanh HP ve bang ma, khong con cat anh theo setClip.
    			veThanhMau(g, isLeft ? 83 : (int)(83 + hpBarW - num3), 5,
    					num3, 10, 0xB01B1B, 0xFF4D4D);
    			if (isLeft)
    			{
    				g.setClip(83, 20, num2, 6);
    			}
    			else
    			{
    				g.setClip(83 + mpBarW - num2, 20, num2, 6);
    			}
    			g.drawImage(imgMPLost, 83, 20, 0);
    			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			if (isLeft)
    			{
    				g.setClip(83, 20, num4, 6);
    			}
    			else
    			{
    				g.setClip(83 + mpBarW - num4, 20, num4, 6);
    			}
    			g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			// Thanh KI ve bang ma.
    			veThanhMau(g, isLeft ? 83 : (83 + mpBarW - num4), 20,
    					num4, 6, 0x1B5FB0, 0x4DA6FF);
    			if (Char.myCharz().cMP == 0 && GameCanvas.gameTick % 10 > 5)
    			{
    				g.setClip(83, 20, 2, 6);
    				g.drawImage(imgMPLost, 83, 20, 0);
    				g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
    			}
    		}
    	}
    
    	public void getInjure()
    	{
    	}
    
    	public void starVS()
    	{
    		curr = (last = mSystem.currentTimeMillis());
    		secondVS = 180;
    	}
    
    	private Char findCharVS1()
    	{
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char @char = (Char)vCharInMap.elementAt(i);
    			if (@char.cTypePk != 0)
    			{
    				return @char;
    			}
    		}
    		return null;
    	}
    
    	private Char findCharVS2()
    	{
    		for (int i = 0; i < vCharInMap.size(); i++)
    		{
    			Char @char = (Char)vCharInMap.elementAt(i);
    			if (@char.cTypePk != 0 && @char != findCharVS1())
    			{
    				return @char;
    			}
    		}
    		return null;
    	}
    
    	private void paintInfoBar(mGraphics g)
    	{
    		resetTranslate(g);
    		batDauCotTrai();
    		if (TileMap.mapID == 130 && findCharVS1() != null && findCharVS2() != null)
    		{
    			g.translate(GameCanvas.w / 2 - 62, 0);
    			paintImageBar(g, true, findCharVS1());
    			g.translate(-(GameCanvas.w / 2 - 65), 0);
    			paintImageBarRight(g, findCharVS2());
    			findCharVS1().paintHeadWithXY(g, 137, 25, 0);
    			findCharVS2().paintHeadWithXY(g, GameCanvas.w - 15 - 122, 25, 2);
    		}
    		else if (isVS() && Char.myCharz().charFocus != null)
    		{
    			g.translate(GameCanvas.w / 2 - 62, 0);
    			paintImageBar(g, true, Char.myCharz().charFocus);
    			g.translate(-(GameCanvas.w / 2 - 65), 0);
    			paintImageBarRight(g, Char.myCharz());
    			Char.myCharz().paintHeadWithXY(g, 137, 25, 0);
    			Char.myCharz().charFocus.paintHeadWithXY(g, GameCanvas.w - 15 - 122, 25, 2);
    		}
    		else if (ispaintPhubangBar() && isSmallScr())
    		{
    			paintHPBar_NEW(g, 1, 1, Char.myCharz());
    		}
    		else
    		{
    			veKhungNguoiChoi(g, Char.myCharz());
    			veKhungMucTieu(g);
    			veBangBanDo(g);
    			// Bang nhiem vu KHONG ve o day nua — xem ClientManager.GUI.
    			//
    			// Chuoi nay chay truoc ClientManager.GUI, ma ba nut mui ten chuyen
    			// map nhanh nam trong do. Nen bang nhiem vu keo dai xuong duoi la
    			// ba cai nut do de len mat bang.
    		}
    		g.translate(-g.getTranslateX(), -g.getTranslateY());
    		if (isVS() && secondVS > 0)
    		{
    			curr = mSystem.currentTimeMillis();
    			if (curr - last >= 1000)
    			{
    				last = mSystem.currentTimeMillis();
    				secondVS--;
    			}
    			mFont.tahoma_7b_white.drawString(g, secondVS + string.Empty, GameCanvas.w / 2, 13, 2, mFont.tahoma_7b_dark);
    		}
    		if (flareFindFocus)
    		{
    			g.drawImage(ItemMap.imageFlare, 40, 35, mGraphics.BOTTOM | mGraphics.HCENTER);
    			flareTime--;
    			if (flareTime < 0)
    			{
    				flareTime = 0;
    				flareFindFocus = false;
    			}
    		}
    	}
    
    	public bool isVS()
    	{
    		if (TileMap.isVoDaiMap() && (Char.myCharz().cTypePk != 0 || (TileMap.mapID == 130 && findCharVS1() != null && findCharVS2() != null)))
    		{
    			return true;
    		}
    		return false;
    	}
    
    	private void paintSelectedSkill(mGraphics g)
    	{
    		if (mobCapcha != null)
    		{
    			paintCapcha(g);
    		}
    		else
    		{
    			if (GameCanvas.currentDialog != null || ChatPopup.currChatPopup != null || GameCanvas.menu.showMenu || isPaintPopup() || GameCanvas.panel.isShow || Char.myCharz().taskMaint == null
    					|| Char.myCharz().taskMaint.taskId == 0 || ChatTextField.gI().isShow || GameCanvas.currentScreen == MoneyCharge.instance)
    			{
    				return;
    			}
    			long num = mSystem.currentTimeMillis();
    			long num2 = num - lastUsePotion;
    			int num3 = 0;
    			if (num2 < 10000)
    			{
    				num3 = (int)(num2 * 20 / 10000);
    			}
    			if (!GameCanvas.isTouch)
    			{
    				veKhungKyNang(g, xSkill + xHP - 1, yHP - 1, canhOKyNang(),
    						mScreen.keyTouch == 10);
    				SmallImage.drawSmallImage(g, 542, xSkill + xHP + 3, yHP + 3, 0, 0);
    				mFont.number_gray.drawString(g, string.Empty + hpPotion, xSkill + xHP + 22, yHP + 15, 1);
    				if (num2 < 10000)
    				{
    					g.setColor(2721889);
    					num3 = (int)(num2 * 20 / 10000);
    					g.fillRect(xSkill + xHP + 3, yHP + 3 + num3, 20, 20 - num3);
    				}
    			}
    			else if (Char.myCharz().statusMe != 14)
    			{
    				if (gamePad.isSmallGamePad)
    				{
    					if (isAnalog != 1)
    					{
    						veCumNutNhanh(g, phanNapDauThan(num2));
    						if (isPickNgocRong)
    						{
    							g.drawImage((mScreen.keyTouch != 14) ? imgNR1 : imgNR2, xHP + 5, yHP - 6 - 40 + 10, 0);
    						}
    					}
    					else if (isAnalog == 1)
    					{
    						int num4 = 10;
    						veKhungKyNang(g, xSkill + xHP - 1, yHP - 1 + num4, canhOKyNang(),
    								mScreen.keyTouch == 10);
    						SmallImage.drawSmallImage(g, 542, xSkill + xHP + 3, yHP + 3 + num4, 0, 0);
    						mFont.number_gray.drawString(g, string.Empty + hpPotion, xSkill + xHP + 22, yHP + 13 + num4, 1);
    						if (num2 < 10000)
    						{
    							g.setColor(2721889);
    							num3 = (int)(num2 * 20 / 10000);
    							g.fillRect(xSkill + xHP + 3, yHP + 3 + num3 + num4, 20, 20 - num3);
    						}
    						if (isPickNgocRong)
    						{
    							g.drawImage((mScreen.keyTouch != 14) ? imgNR3 : imgNR4, xHP + 20 + 5, yHP + 20 - 6 - 40 + 10, mGraphics.HCENTER | mGraphics.VCENTER);
    						}
    					}
    				}
    				else if (isAnalog != 1)
    				{
    					veCumNutNhanh(g, phanNapDauThan(num2));
    					if (isPickNgocRong)
    					{
    						g.drawImage((mScreen.keyTouch != 14) ? imgNR1 : imgNR2, xHP, yHP - 6 - 40, 0);
    					}
    				}
    				else
    				{
    				veCumNutNhanh(g, phanNapDauThan(num2));
    					if (isPickNgocRong)
    					{
    						g.drawImage((mScreen.keyTouch != 14) ? imgNR3 : imgNR4, xHP + 20 + 5, yHP + 20 - 6 - 40 + 10, mGraphics.HCENTER | mGraphics.VCENTER);
    					}
    				}
    			}
    			if (isHaveSelectSkill && GameCanvas.isTouch && !Main.isPC)
    			{
    				veNutThuGon(g, oThuGonKyNang(), thuGonKyNang);
    			}
    			if (isHaveSelectSkill && !thuGonKyNang)
    			{
    				Skill[] array = (Main.isPC ? keySkill : ((!GameCanvas.isTouch) ? keySkill : onScreenSkill));
    				if (mScreen.keyTouch == 10)
    				{
    				}
    				if (!GameCanvas.isTouch)
    				{
    				veNhanPhim(g, xSkill + xHP + 12, yHP - 10 + 11, "*");
    				}
    				int num5 = (Main.isPC ? array.Length : ((!GameCanvas.isTouch) ? array.Length : nSkill));
    				for (int i = 0; i < num5; i++)
    				{
    					if (Main.isPC)
    					{
    						string[] array2 = (TField.isQwerty ? new string[10] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" } : new string[5] { "7", "8", "9", "10", "11" });
    						int num6 = -13;
    						if (num5 > 5 && i < 5)
    						{
    							num6 = 27;
    						}
    						veNhanPhim(g, xSkill + xS[i] + tamOKyNang(), yS[i] + num6 + 6,
    								array2[i]);
    					}
    					else if (!GameCanvas.isTouch)
    					{
    						string[] array3 = (TField.isQwerty ? new string[5] { "Q", "W", "E", "R", "T" } : new string[5] { "7", "8", "9", "1", "3" });
    						veNhanPhim(g, xSkill + xS[i] + 12, yS[i] - 10 + 11, array3[i]);
    					}
    					Skill skill = array[i];
    					if (skill != Char.myCharz().myskill)
    					{
    						veKhungKyNang(g, xSkill + xS[i] - 1, yS[i] - 1, canhOKyNang(), false);
    					}
    					if (skill == null)
    					{
    						continue;
    					}
    					if (skill == Char.myCharz().myskill)
    					{
    						veKhungKyNang(g, xSkill + xS[i] - 1, yS[i] - 1, canhOKyNang(), true);
    						if (GameCanvas.isTouch && !Main.isPC)
    						{
    							g.drawRegion(Mob.imgHP, 0, 12, 9, 6, 0, xSkill + xS[i] + 8, yS[i] - 7, 0);
    						}
    					}
    					// Canh CHINH GIUA khung: khung dat tai (xS-1, yS-1) canh 30
    					// nen tam cua no o (xS-1+15, yS-1+15) = (xS+14, yS+14).
    					// Truoc day ve o +13 - lech 1px ca hai chieu; hoi con dung
    					// anh imgSkill thi anh do co vien in san nen khong ai thay,
    					// ve khung bang ma doi xung thi lech ra ngay.
    					veIconKyNang(g, skill, xSkill + xS[i] + tamOKyNang(),
    							yS[i] + tamOKyNang());
    					if ((i == selectedIndexSkill && !isPaintUI() && GameCanvas.gameTick % 10 > 5) || i == keyTouchSkill)
    					{
    						g.drawImage(ItemMap.imageFlare, xSkill + xS[i] + tamOKyNang(), yS[i] + tamOKyNang(), 3);
    					}
    				}
    			}
    			paintGamePad(g);
    		}
    	}
    
    	public void paintOpen(mGraphics g)
    	{
    		if (isstarOpen)
    		{
    			g.translate(-g.getTranslateX(), -g.getTranslateY());
    			g.fillRect(0, 0, GameCanvas.w, moveUp);
    			g.setColor(10275899);
    			g.fillRect(0, moveUp - 1, GameCanvas.w, 1);
    			g.fillRect(0, moveDow + 1, GameCanvas.w, 1);
    		}
    	}
    
    	public static void startFlyText(string flyString, int x, int y, int dx, int dy, int color)
    	{
    		int num = -1;
    		for (int i = 0; i < 5; i++)
    		{
    			if (flyTextState[i] == -1)
    			{
    				num = i;
    				break;
    			}
    		}
    		if (num == -1)
    		{
    			return;
    		}
    		flyTextColor[num] = color;
    		flyTextString[num] = flyString;
    		flyTextX[num] = x;
    		flyTextY[num] = y;
    		flyTextDx[num] = dx;
    		flyTextDy[num] = ((dy >= 0) ? 5 : (-5));
    		flyTextState[num] = 0;
    		flyTime[num] = 0;
    		flyTextYTo[num] = 10;
    		for (int j = 0; j < 5; j++)
    		{
    			if (flyTextState[j] != -1 && num != j && flyTextDy[num] < 0 && Res.abs(flyTextX[num] - flyTextX[j]) <= 20 && flyTextYTo[num] == flyTextYTo[j])
    			{
    				flyTextYTo[num] += 10;
    			}
    		}
    	}
    
    	public static void updateFlyText()
    	{
    		for (int i = 0; i < 5; i++)
    		{
    			if (flyTextState[i] == -1)
    			{
    				continue;
    			}
    			if (flyTextState[i] > flyTextYTo[i])
    			{
    				flyTime[i]++;
    				if (flyTime[i] == 25)
    				{
    					flyTime[i] = 0;
    					flyTextState[i] = -1;
    					flyTextYTo[i] = 0;
    					flyTextDx[i] = 0;
    					flyTextX[i] = 0;
    				}
    			}
    			else
    			{
    				flyTextState[i] += Res.abs(flyTextDy[i]);
    				flyTextX[i] += flyTextDx[i];
    				flyTextY[i] += flyTextDy[i];
    			}
    		}
    	}
    
    	public static void loadSplash()
    	{
    		if (imgSplash == null)
    		{
    			imgSplash = new Image[3];
    			for (int i = 0; i < 3; i++)
    			{
    				imgSplash[i] = GameCanvas.loadImage("/e/sp" + i + ".png");
    			}
    		}
    		splashX = new int[2];
    		splashY = new int[2];
    		splashState = new int[2];
    		splashF = new int[2];
    		splashDir = new int[2];
    		splashState[0] = (splashState[1] = -1);
    	}
    
    	public static bool startSplash(int x, int y, int dir)
    	{
    		int num = ((splashState[0] != -1) ? 1 : 0);
    		if (splashState[num] != -1)
    		{
    			return false;
    		}
    		splashState[num] = 0;
    		splashDir[num] = dir;
    		splashX[num] = x;
    		splashY[num] = y;
    		return true;
    	}
    
    	public static void updateSplash()
    	{
    		for (int i = 0; i < 2; i++)
    		{
    			if (splashState[i] != -1)
    			{
    				splashState[i]++;
    				splashX[i] += splashDir[i] << 2;
    				splashY[i]--;
    				if (splashState[i] >= 6)
    				{
    					splashState[i] = -1;
    				}
    				else
    				{
    					splashF[i] = (splashState[i] >> 1) % 3;
    				}
    			}
    		}
    	}
    
    	public static void paintSplash(mGraphics g)
    	{
    		for (int i = 0; i < 2; i++)
    		{
    			if (splashState[i] != -1)
    			{
    				if (splashDir[i] == 1)
    				{
    					g.drawImage(imgSplash[splashF[i]], splashX[i], splashY[i], 3);
    				}
    				else
    				{
    					g.drawRegion(imgSplash[splashF[i]], 0, 0, mGraphics.getImageWidth(imgSplash[splashF[i]]), mGraphics.getImageHeight(imgSplash[splashF[i]]), 2, splashX[i], splashY[i], 3);
    				}
    			}
    		}
    	}
    
    	private void loadInforBar()
    	{
    		imgScrW = 84;
    		hpBarW = 66L;
    		mpBarW = 59;
    		hpBarX = 52;
    		hpBarY = 10;
    		spBarW = 61;
    		expBarW = gW - 61;
    	}
    
    	public void updateSS()
    	{
    		if (indexMenu != -1)
    		{
    			if (cmySK != cmtoYSK)
    			{
    				cmvySK = cmtoYSK - cmySK << 2;
    				cmdySK += cmvySK;
    				cmySK += cmdySK >> 4;
    				cmdySK &= 15;
    			}
    			if (Math.abs(cmtoYSK - cmySK) < 15 && cmySK < 0)
    			{
    				cmtoYSK = 0;
    			}
    			if (Math.abs(cmtoYSK - cmySK) < 15 && cmySK > cmyLimSK)
    			{
    				cmtoYSK = cmyLimSK;
    			}
    		}
    	}
    
    	public void updateKeyAlert()
    	{
    		if (!isPaintAlert || GameCanvas.currentDialog != null)
    		{
    			return;
    		}
    		bool flag = false;
    		if (GameCanvas.keyPressed[Key.NUM8])
    		{
    			indexRow++;
    			if (indexRow >= texts.size())
    			{
    				indexRow = 0;
    			}
    			flag = true;
    		}
    		else if (GameCanvas.keyPressed[Key.NUM2])
    		{
    			indexRow--;
    			if (indexRow < 0)
    			{
    				indexRow = texts.size() - 1;
    			}
    			flag = true;
    		}
    		if (flag)
    		{
    			scrMain.moveTo(indexRow * scrMain.ITEM_SIZE);
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    		}
    		if (GameCanvas.isTouch)
    		{
    			ScrollResult scrollResult = scrMain.updateKey();
    			if (scrollResult.isDowning || scrollResult.isFinish)
    			{
    				indexRow = scrollResult.selected;
    				flag = true;
    			}
    		}
    		if (!flag || indexRow < 0 || indexRow >= texts.size())
    		{
    			return;
    		}
    		string text = (string)texts.elementAt(indexRow);
    		int num = -1;
    		fnick = null;
    		alertURL = null;
    		center = null;
    		ChatTextField.gI().center = null;
    		if ((num = text.IndexOf("http://")) >= 0)
    		{
    			Cout.println("currentLine: " + text);
    			alertURL = text.Substring(num);
    			center = new Command(mResources.open_link, 12000);
    			if (!GameCanvas.isTouch)
    			{
    				ChatTextField.gI().center = new Command(mResources.open_link, null, 12000, null);
    			}
    		}
    		else
    		{
    			if ((num = text.IndexOf("@")) < 0)
    			{
    				return;
    			}
    			string text2 = text.Substring(2);
    			text2 = text2.Trim();
    			num = text2.IndexOf("@");
    			string text3 = text2.Substring(num);
    			int num2 = -1;
    			num2 = text3.IndexOf(" ");
    			num2 = ((num2 > 0) ? (num2 + num) : (num + text3.Length));
    			fnick = text2.Substring(num + 1, num2);
    			if (!fnick.Equals(string.Empty) && !fnick.Equals(Char.myCharz().cName))
    			{
    				center = new Command(mResources.SELECT, 12009, fnick);
    				if (!GameCanvas.isTouch)
    				{
    					ChatTextField.gI().center = new Command(mResources.SELECT, null, 12009, fnick);
    				}
    			}
    			else
    			{
    				fnick = null;
    				center = null;
    			}
    		}
    	}
    
    	public bool isPaintPopup()
    	{
    		if (isPaintItemInfo || isPaintInfoMe || isPaintStore || isPaintWeapon || isPaintNonNam || isPaintNonNu || isPaintAoNam || isPaintAoNu || isPaintGangTayNam || isPaintGangTayNu || isPaintQuanNam || isPaintQuanNu || isPaintGiayNam || isPaintGiayNu || isPaintLien || isPaintNhan || isPaintNgocBoi || isPaintPhu || isPaintStack || isPaintStackLock || isPaintGrocery || isPaintGroceryLock || isPaintUpGrade || isPaintConvert || isPaintSplit || isPaintUpPearl || isPaintBox || isPaintTrade || isPaintAlert || isPaintZone || isPaintTeam || isPaintClan || isPaintFindTeam || isPaintTask || isPaintFriend || isPaintEnemies || isPaintCharInMap || isPaintMessage)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isNotPaintTouchControl()
    	{
    		if (!GameCanvas.isTouchControl && GameCanvas.currentScreen == gI())
    		{
    			return true;
    		}
    		if (!GameCanvas.isTouch)
    		{
    			return true;
    		}
    		if (ChatTextField.gI().isShow)
    		{
    			return true;
    		}
    		if (InfoDlg.isShow)
    		{
    			return true;
    		}
    		if (GameCanvas.currentDialog != null || ChatPopup.currChatPopup != null || GameCanvas.menu.showMenu || GameCanvas.panel.isShow || isPaintPopup())
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isPaintUI()
    	{
    		if (isPaintStore || isPaintWeapon || isPaintNonNam || isPaintNonNu || isPaintAoNam || isPaintAoNu || isPaintGangTayNam || isPaintGangTayNu || isPaintQuanNam || isPaintQuanNu || isPaintGiayNam || isPaintGiayNu || isPaintLien || isPaintNhan || isPaintNgocBoi || isPaintPhu || isPaintStack || isPaintStackLock || isPaintGrocery || isPaintGroceryLock || isPaintUpGrade || isPaintConvert || isPaintSplit || isPaintUpPearl || isPaintBox || isPaintTrade)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public bool isOpenUI()
    	{
    		if (isPaintItemInfo || isPaintInfoMe || isPaintStore || isPaintNonNam || isPaintNonNu || isPaintAoNam || isPaintAoNu || isPaintGangTayNam || isPaintGangTayNu || isPaintQuanNam || isPaintQuanNu || isPaintGiayNam || isPaintGiayNu || isPaintLien || isPaintNhan || isPaintNgocBoi || isPaintPhu || isPaintWeapon || isPaintStack || isPaintStackLock || isPaintGrocery || isPaintGroceryLock || isPaintUpGrade || isPaintConvert || isPaintUpPearl || isPaintBox || isPaintSplit || isPaintTrade)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public static void setPopupSize(int w, int h)
    	{
    		if (GameCanvas.w == 128 || GameCanvas.h <= 208)
    		{
    			w = 126;
    			h = 160;
    		}
    		indexTitle = 0;
    		popupW = w;
    		popupH = h;
    		popupX = gW2 - w / 2;
    		popupY = gH2 - h / 2;
    		if (GameCanvas.isTouch && !isPaintZone && !isPaintTeam && !isPaintClan && !isPaintCharInMap && !isPaintFindTeam && !isPaintFriend && !isPaintEnemies && !isPaintTask && !isPaintMessage)
    		{
    			if (GameCanvas.h <= 240)
    			{
    				popupY -= 10;
    			}
    			if (GameCanvas.isTouch && !GameCanvas.isTouchControlSmallScreen && GameCanvas.currentScreen is GameScr)
    			{
    				popupW = 310;
    				popupX = gW / 2 - popupW / 2;
    				if (isPaintInfoMe && indexMenu > 0)
    				{
    					popupW = w;
    					popupX = gW2 - w / 2;
    				}
    			}
    		}
    		if (popupY < -10)
    		{
    			popupY = -10;
    		}
    		if (GameCanvas.h > 208 && popupY < 0)
    		{
    			popupY = 0;
    		}
    		if (GameCanvas.h == 208 && popupY < 10)
    		{
    			popupY = 10;
    		}
    	}
    
    	public static void loadImg()
    	{
    		TileMap.loadTileImage();
    	}
    
    	public void paintTitle(mGraphics g, string title, bool arrow)
    	{
    		int num = 0;
    		num = gW / 2;
    		g.setColor(Paint.COLORDARK);
    		g.fillRoundRect(num - mFont.tahoma_8b.getWidth(title) / 2 - 12, popupY + 4, mFont.tahoma_8b.getWidth(title) + 22, 24, 6, 6);
    		if ((indexTitle == 0 || GameCanvas.isTouch) && arrow)
    		{
    			SmallImage.drawSmallImage(g, 989, num - mFont.tahoma_8b.getWidth(title) / 2 - 15 - 7 - ((GameCanvas.gameTick % 8 <= 3) ? 2 : 0), popupY + 16, 2, StaticObj.VCENTER_HCENTER);
    			SmallImage.drawSmallImage(g, 989, num + mFont.tahoma_8b.getWidth(title) / 2 + 15 + 5 + ((GameCanvas.gameTick % 8 <= 3) ? 2 : 0), popupY + 16, 0, StaticObj.VCENTER_HCENTER);
    		}
    		if (indexTitle == 0)
    		{
    			g.setColor(Paint.COLORFOCUS);
    		}
    		else
    		{
    			g.setColor(Paint.COLORBORDER);
    		}
    		g.drawRoundRect(num - mFont.tahoma_8b.getWidth(title) / 2 - 12, popupY + 4, mFont.tahoma_8b.getWidth(title) + 22, 24, 6, 6);
    		mFont.tahoma_8b.drawString(g, title, num, popupY + 9, 2);
    	}
    
    	public static int getTaskMapId()
    	{
    		int num = 0;
    		if (Char.myCharz().taskMaint == null)
    		{
    			return -1;
    		}
    		return mapTasks[Char.myCharz().taskMaint.index];
    	}
    
    	public static sbyte getTaskNpcId()
    	{
    		sbyte result = 0;
    		if (Char.myCharz().taskMaint == null)
    		{
    			result = -1;
    		}
    		else if (Char.myCharz().taskMaint.index <= tasks.Length - 1)
    		{
    			result = (sbyte)tasks[Char.myCharz().taskMaint.index];
    		}
    		return result;
    	}
    
    	public void refreshTeam()
    	{
    	}
    
    	public void onChatFromMe(string text, string to)
    	{
    		Res.outz("CHAT");
    		if (!isPaintMessage || GameCanvas.isTouch)
    		{
    			ChatTextField.gI().isShow = false;
    		}
    		if (to.Equals(mResources.chat_player))
    		{
    			if (info2.playerID != Char.myCharz().charID)
    			{
    				Service.gI().chatPlayer(text, info2.playerID);
    			}
    		}
    		else if (!text.Equals(string.Empty))
    		{
    			Service.gI().chat(text);
    		}
    	}
    
    	public void onCancelChat()
    	{
    		if (isPaintMessage)
    		{
    			isPaintMessage = false;
    			ChatTextField.gI().center = null;
    		}
    	}
    
    	public void openWeb(string strLeft, string strRight, string url, string title, string str)
    	{
    		isPaintAlert = true;
    		isLockKey = true;
    		indexRow = 0;
    		setPopupSize(175, 200);
    		textsTitle = title;
    		texts = mFont.tahoma_7.splitFontVector(str, popupW - 30);
    		center = null;
    		left = new Command(strLeft, 11068, url);
    		right = new Command(strRight, 11069);
    	}
    
    	public void sendSms(string strLeft, string strRight, short port, string syntax, string title, string str)
    	{
    		isPaintAlert = true;
    		isLockKey = true;
    		indexRow = 0;
    		setPopupSize(175, 200);
    		textsTitle = title;
    		texts = mFont.tahoma_7.splitFontVector(str, popupW - 30);
    		center = null;
    		MyVector myVector = new MyVector();
    		myVector.addElement(string.Empty + port);
    		myVector.addElement(syntax);
    		left = new Command(strLeft, 11074);
    		right = new Command(strRight, 11075);
    	}
    
    	/// <summary>
    	/// Nút menu trên HUD: mở bảng nhân vật KIỂU MỚI.
    	/// </summary>
    	/// <remarks>
    	/// Bảng cũ <b>vẫn còn nguyên</b>, không xoá dòng nào — chỉ là nút này không
    	/// mở nó nữa. Mọi đường khác vào bảng cũ vẫn chạy: mã 110001, các NPC mở
    	/// bảng theo loại riêng (cửa hàng, nâng cấp, giao dịch...), và
    	/// <see cref="moBangCu"/> ngay dưới đây.
    	/// </remarks>
    	public void actMenu()
    	{
    		God.TuiUI.getInstance().moRa();
    	}

    	/// <summary>Mở bảng nhân vật kiểu cũ. Giữ lại để còn đường quay về.</summary>
    	public void moBangCu()
    	{
    		GameCanvas.panel.setTypeMain();
    		GameCanvas.panel.show();
    	}
    
    	public void openUIZone(Message message)
    	{
    		InfoDlg.hide();
    		try
    		{
    			zones = new int[message.reader().readByte()];
    			pts = new int[zones.Length];
    			numPlayer = new int[zones.Length];
    			maxPlayer = new int[zones.Length];
    			rank1 = new int[zones.Length];
    			rankName1 = new string[zones.Length];
    			rank2 = new int[zones.Length];
    			rankName2 = new string[zones.Length];
    			for (int i = 0; i < zones.Length; i++)
    			{
    				zones[i] = message.reader().readByte();
    				pts[i] = message.reader().readByte();
    				numPlayer[i] = message.reader().readByte();
    				maxPlayer[i] = message.reader().readByte();
    				sbyte b = message.reader().readByte();
    				if (b == 1)
    				{
    					rankName1[i] = message.reader().readUTF();
    					rank1[i] = message.reader().readInt();
    					rankName2[i] = message.reader().readUTF();
    					rank2[i] = message.reader().readInt();
    				}
    			}
    		}
    		catch (Exception ex)
    		{
    			Cout.LogError("Loi ham OPEN UIZONE " + ex.ToString());
    		}
    		// Bang chon khu MOI thay cho Panel loai 3.
    		//
    		// Panel loai 3 dung chung bo ve voi hanh trang / cua hang / bang hoi:
    		// truot ngang tu mep man hinh, moi dong la mot dai chu tron, va chiem
    		// gan het man choi chi de hien muoi dong.
    		God.ChonNhanhUI.getInstance().moKhu();
    	}
    
    	public void showViewInfo()
    	{
    		indexMenu = 3;
    		isPaintInfoMe = true;
    		setPopupSize(175, 200);
    	}
    
    	private void actDead()
    	{
    		MyVector myVector = new MyVector();
    		myVector.addElement(new Command(mResources.DIES[1], 110381));
    		myVector.addElement(new Command(mResources.DIES[2], 110382));
    		myVector.addElement(new Command(mResources.DIES[3], 110383));
    		GameCanvas.menu.startAt(myVector, 3);
    	}
    
    	public void startYesNoPopUp(string info, Command cmdYes, Command cmdNo)
    	{
    		popUpYesNo = new PopUpYesNo();
    		popUpYesNo.setPopUp(info, cmdYes, cmdNo);
    	}
    
    	public void player_vs_player(int playerId, int xu, string info, sbyte typePK)
    	{
    		Char @char = findCharInMap(playerId);
    		if (@char != null)
    		{
    			if (typePK == 3)
    			{
    				startYesNoPopUp(info, new Command(mResources.OK, 2000, @char), new Command(mResources.CLOSE, 2009, @char));
    			}
    			if (typePK == 4)
    			{
    				startYesNoPopUp(info, new Command(mResources.OK, 2005, @char), new Command(mResources.CLOSE, 2009, @char));
    			}
    		}
    	}
    
    	public void giaodich(int playerID)
    	{
    		Char @char = findCharInMap(playerID);
    		if (@char != null)
    		{
    			startYesNoPopUp(@char.cName + mResources.want_to_trade, new Command(mResources.YES, 11114, @char), new Command(mResources.NO, 2009, @char));
    		}
    	}
    
    	public void getFlagImage(int charID, sbyte cflag)
    	{
    		if (vFlag.size() == 0)
    		{
    			Service.gI().getFlag(2, cflag);
    			Res.outz("getFlag1");
    			return;
    		}
    		if (charID == Char.myCharz().charID)
    		{
    			Res.outz("my cflag: isme");
    			if (Char.myCharz().isGetFlagImage(cflag))
    			{
    				Res.outz("my cflag: true");
    				for (int i = 0; i < vFlag.size(); i++)
    				{
    					PKFlag pKFlag = (PKFlag)vFlag.elementAt(i);
    					if (pKFlag != null && pKFlag.cflag == cflag)
    					{
    						Res.outz("my cflag: cflag==");
    						Char.myCharz().flagImage = pKFlag.IDimageFlag;
    					}
    				}
    			}
    			else if (!Char.myCharz().isGetFlagImage(cflag))
    			{
    				Res.outz("my cflag: false");
    				Service.gI().getFlag(2, cflag);
    			}
    			return;
    		}
    		Res.outz("my cflag: not me");
    		if (findCharInMap(charID) == null)
    		{
    			return;
    		}
    		if (findCharInMap(charID).isGetFlagImage(cflag))
    		{
    			Res.outz("my cflag: true");
    			for (int j = 0; j < vFlag.size(); j++)
    			{
    				PKFlag pKFlag2 = (PKFlag)vFlag.elementAt(j);
    				if (pKFlag2 != null && pKFlag2.cflag == cflag)
    				{
    					Res.outz("my cflag: cflag==");
    					findCharInMap(charID).flagImage = pKFlag2.IDimageFlag;
    				}
    			}
    		}
    		else if (!findCharInMap(charID).isGetFlagImage(cflag))
    		{
    			Res.outz("my cflag: false");
    			Service.gI().getFlag(2, cflag);
    		}
    	}
    
    	public void actionPerform(int idAction, object p)
    	{
    		Cout.println("PERFORM WITH ID = " + idAction);
    		switch (idAction)
    		{
    		case 888351:
    			Service.gI().petStatus(5);
    			GameCanvas.endDlg();
    			break;
    		case 11112:
    		{
    			Char @char = (Char)p;
    			Service.gI().friend(1, @char.charID);
    			break;
    		}
    		case 11113:
    		{
    			Char char2 = (Char)p;
    			if (char2 != null)
    			{
    				Service.gI().giaodich(0, char2.charID, -1, -1);
    			}
    			break;
    		}
    		case 11114:
    		{
    			popUpYesNo = null;
    			GameCanvas.endDlg();
    			Char char3 = (Char)p;
    			if (char3 != null)
    			{
    				Service.gI().giaodich(1, char3.charID, -1, -1);
    			}
    			break;
    		}
    		case 11111:
    			if (Char.myCharz().charFocus != null)
    			{
    				InfoDlg.showWait();
    				if (GameCanvas.panel.vPlayerMenu.size() <= 0)
    				{
    					playerMenu(Char.myCharz().charFocus);
    				}
    				GameCanvas.panel.setTypePlayerMenu(Char.myCharz().charFocus);
    				GameCanvas.panel.show();
    				Service.gI().getPlayerMenu(Char.myCharz().charFocus.charID);
    				Service.gI().messagePlayerMenu(Char.myCharz().charFocus.charID);
    			}
    			break;
    		case 11115:
    			if (Char.myCharz().charFocus != null)
    			{
    				InfoDlg.showWait();
    				Service.gI().playerMenuAction(Char.myCharz().charFocus.charID, (short)Char.myCharz().charFocus.menuSelect);
    			}
    			break;
    		case 2000:
    			popUpYesNo = null;
    			GameCanvas.endDlg();
    			if ((Char)p == null)
    			{
    				Service.gI().player_vs_player(1, 3, -1);
    				break;
    			}
    			Service.gI().player_vs_player(1, 3, ((Char)p).charID);
    			Service.gI().charMove();
    			break;
    		case 2001:
    			GameCanvas.endDlg();
    			break;
    		case 2003:
    			GameCanvas.endDlg();
    			InfoDlg.showWait();
    			Service.gI().player_vs_player(0, 3, Char.myCharz().charFocus.charID);
    			break;
    		case 2004:
    			GameCanvas.endDlg();
    			Service.gI().player_vs_player(0, 4, Char.myCharz().charFocus.charID);
    			break;
    		case 2005:
    			GameCanvas.endDlg();
    			popUpYesNo = null;
    			if ((Char)p == null)
    			{
    				Service.gI().player_vs_player(1, 4, -1);
    			}
    			else
    			{
    				Service.gI().player_vs_player(1, 4, ((Char)p).charID);
    			}
    			break;
    		case 2009:
    			popUpYesNo = null;
    			break;
    		case 2006:
    			GameCanvas.endDlg();
    			Service.gI().player_vs_player(2, 4, Char.myCharz().charFocus.charID);
    			break;
    		case 2007:
    			GameCanvas.endDlg();
    			GameMidlet.instance.exit();
    			break;
    		case 11038:
    			actDead();
    			break;
    		case 110382:
    			Service.gI().returnTownFromDead();
    			break;
    		case 110383:
    			Service.gI().wakeUpFromDead();
    			break;
    		case 1:
    			GameCanvas.endDlg();
    			break;
    		case 2:
    			GameCanvas.menu.showMenu = false;
    			break;
    		case 8002:
    			doFire(false, true);
    			GameCanvas.clearKeyHold();
    			GameCanvas.clearKeyPressed();
    			break;
    		case 11057:
    		{
    			Effect2.vEffect2Outside.removeAllElements();
    			Effect2.vEffect2.removeAllElements();
    			Npc npc = (Npc)p;
    			if (npc.idItem == 0)
    			{
    				Service.gI().confirmMenu((short)npc.template.npcTemplateId, (sbyte)GameCanvas.menu.menuSelectedItem);
    			}
    			else if (GameCanvas.menu.menuSelectedItem == 0)
    			{
    				Service.gI().pickItem(npc.idItem);
    			}
    			break;
    		}
    		case 11000:
    			actMenu();
    			break;
    		case 11001:
    			Char.myCharz().findNextFocusByKey();
    			break;
    		case 11002:
    			GameCanvas.panel.hide();
    			break;
    		case 11120:
    		{
    			object[] array2 = (object[])p;
    			Skill skill4 = (Skill)array2[0];
    			int num2 = int.Parse((string)array2[1]);
    			for (int j = 0; j < onScreenSkill.Length; j++)
    			{
    				if (onScreenSkill[j] == skill4)
    				{
    					onScreenSkill[j] = null;
    				}
    			}
    			onScreenSkill[num2] = skill4;
    			saveonScreenSkillToRMS();
    			break;
    		}
    		case 11121:
    		{
    			object[] array = (object[])p;
    			Skill skill3 = (Skill)array[0];
    			int num = int.Parse((string)array[1]);
    			for (int i = 0; i < keySkill.Length; i++)
    			{
    				if (keySkill[i] == skill3)
    				{
    					keySkill[i] = null;
    				}
    			}
    			keySkill[num] = skill3;
    			saveKeySkillToRMS();
    			break;
    		}
    		case 110001:
    			GameCanvas.panel.setTypeMain();
    			GameCanvas.panel.show();
    			break;
    		case 110004:
    			GameCanvas.menu.showMenu = false;
    			break;
    		case 11067:
    			if (TileMap.zoneID != indexSelect)
    			{
    				Service.gI().requestChangeZone(indexSelect, indexItemUse);
    				InfoDlg.showWait();
    			}
    			else
    			{
    				info1.addInfo(mResources.ZONE_HERE, 0);
    			}
    			break;
    		case 11059:
    		{
    			Skill skill2 = onScreenSkill[selectedIndexSkill];
    			doUseSkill(skill2, false);
    			center = null;
    			break;
    		}
    		case 12000:
    			Service.gI().getClan(1, -1, null);
    			break;
    		case 12001:
    			GameCanvas.endDlg();
    			break;
    		case 12002:
    		{
    			GameCanvas.endDlg();
    			ClanObject clanObject = (ClanObject)p;
    			Service.gI().clanInvite(1, -1, clanObject.clanID, clanObject.code);
    			popUpYesNo = null;
    			break;
    		}
    		case 12003:
    		{
    			ClanObject clanObject = (ClanObject)p;
    			GameCanvas.endDlg();
    			Service.gI().clanInvite(2, -1, clanObject.clanID, clanObject.code);
    			popUpYesNo = null;
    			break;
    		}
    		case 12004:
    		{
    			Skill skill = (Skill)p;
    			doUseSkill(skill, true);
    			Char.myCharz().saveLoadPreviousSkill();
    			break;
    		}
    		case 110391:
    			Service.gI().clanInvite(0, Char.myCharz().charFocus.charID, -1, -1);
    			break;
    		case 12005:
    			if (GameCanvas.serverScr == null)
    			{
    				GameCanvas.serverScr = new ServerScr();
    			}
    			GameCanvas.serverScr.switchToMe();
    			GameCanvas.endDlg();
    			break;
    		case 12006:
    			GameMidlet.instance.exit();
    			break;
    		}
    	}
    
    	private static void setTouchBtn()
    	{
    		if (isAnalog != 0)
    		{
    			xTG = (xF = GameCanvas.w - 45);
    			if (gamePad.isLargeGamePad)
    			{
    				// Hang o ky nang bam SAT mep phai vung can dieu khien ao.
    				//
    				// Khe 20 diem cu la khe thua: vung can dieu khien khong ve gi
    				// o mep phai cua no, nen 20 diem do chi la khoang trong. Ma
    				// day man hinh dien thoai thi tung diem deu co chu: sau hang
    				// o ky nang con phai xep khung chat, cum Capsule/Dau than, roi
    				// cum Dam/Chuyen muc tieu.
    				//
    				// O 35 xuong 32 cung vi the — 32 la be rong o ky nang ban may
    				// tinh, va van rong hon 31 la muc san icon 24 diem bat dau
    				// tho ra ngoai vien.
    				xSkill = gamePad.wZone + 2;
    				wSkill = 28;
    				xHP = xF - 45;
    			}
    			else if (gamePad.isMediumGamePad)
    			{
    				xHP = xF - 45;
    			}
    			yF = GameCanvas.h - 45;
    			yTG = yF - 45;
    		}
    	}
    
    	private void updateGamePad()
    	{
    		if (isAnalog == 0 || Char.myCharz().statusMe == 14)
    		{
    			return;
    		}
    		if (GameCanvas.isPointerHoldIn(xF, yF, 40, 40))
    		{
    			mScreen.keyTouch = 5;
    			if (GameCanvas.isPointerJustRelease)
    			{
    				GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] = true;
    				GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    			}
    		}
    		gamePad.update();
    		if (GameCanvas.isPointerHoldIn(xTG, yTG, 34, 34))
    		{
    			mScreen.keyTouch = 13;
    			GameCanvas.isPointerJustDown = false;
    			isPointerDowning = false;
    			if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
    			{
    				Char.myCharz().findNextFocusByKey();
    				GameCanvas.isPointerClick = (GameCanvas.isPointerJustDown = (GameCanvas.isPointerJustRelease = false));
    			}
    		}
    	}
    
    	private void paintGamePad(mGraphics g)
    	{
    		if (isAnalog != 0 && Char.myCharz().statusMe != 14)
    		{
    			// Ban PC khong ve nut dam: PC danh bang ban phim/chuot nen nut
    			// nay chi choan cho o goc phai duoi. Can dieu khien va nut chon
    			// muc tieu ben duoi van ve, vi chung con dung duoc bang chuot.
    			if (!Main.isPC)
    			{
    				g.drawImage((mScreen.keyTouch != 5 && mScreen.keyMouse != 5) ? imgFire0 : imgFire1, xF + 20, yF + 20, mGraphics.HCENTER | mGraphics.VCENTER);
    			}
    			gamePad.paint(g);
    			g.drawImage((mScreen.keyTouch != 13) ? imgFocus : imgFocus2, xTG + 20, yTG + 20, mGraphics.HCENTER | mGraphics.VCENTER);
    		}
    	}
    
    	public void showWinNumber(string num, string finish)
    	{
    		winnumber = new int[num.Length];
    		randomNumber = new int[num.Length];
    		tMove = new int[num.Length];
    		moveCount = new int[num.Length];
    		delayMove = new int[num.Length];
    		try
    		{
    			for (int i = 0; i < num.Length; i++)
    			{
    				winnumber[i] = short.Parse(num[i].ToString());
    				randomNumber[i] = Res.random(0, 11);
    				tMove[i] = 1;
    				delayMove[i] = 0;
    			}
    		}
    		catch (Exception)
    		{
    		}
    		tShow = 100;
    		moveIndex = 0;
    		strFinish = finish;
    		lastXS = (currXS = mSystem.currentTimeMillis());
    	}
    
    	public void chatVip(string chatVip)
    	{
    		if (!startChat)
    		{
    			currChatWidth = mFont.tahoma_7b_yellowSmall.getWidth(chatVip);
    			xChatVip = GameCanvas.w;
    			startChat = true;
    		}
    		if (chatVip.StartsWith("!"))
    		{
    			chatVip = chatVip.Substring(1, chatVip.Length);
    			isFireWorks = true;
    		}
    		// KHONG gom vao hang doi nua: dai chu chay da bo, gom tiep thi danh
    		// sach chi phinh ra ma khong ai doc.
    	}
    
    	public void clearChatVip()
    	{
    		vChatVip.removeAllElements();
    		xChatVip = GameCanvas.w;
    		startChat = false;
    	}
    
    	/// <summary>
    	/// Dải chữ chạy dưới đáy màn hình — <b>đã bỏ</b>.
    	/// </summary>
    	/// <remarks>
    	/// Dải này căng một thanh đen mờ ngang hết bề ngang màn hình, ngay dưới
    	/// khung chat, và chữ bò qua mất mấy giây mới đọc hết một câu.
    	///
    	/// Câu loa giờ vào thẳng thẻ "Hệ thống" của khung chat: đọc được ngay,
    	/// cuộn lại xem được, và không chiếm một dải ngang màn chơi.
    	///
    	/// Giữ lại cái hàm rỗng vì nó nằm trong đường vẽ chung; xoá hẳn thì phải
    	/// đụng vào chỗ gọi, mà chỗ đó còn vài việc khác.
    	/// </remarks>
    	public void paintChatVip(mGraphics g)
    	{
    	}
    
    	public void updateChatVip()
    	{
    		if (!startChat)
    		{
    			return;
    		}
    		xChatVip -= 2;
    		if (xChatVip < -currChatWidth)
    		{
    			xChatVip = GameCanvas.w;
    			vChatVip.removeElementAt(0);
    			if (vChatVip.size() == 0)
    			{
    				isFireWorks = false;
    				startChat = false;
    			}
    			else
    			{
    				currChatWidth = mFont.tahoma_7b_white.getWidth((string)vChatVip.elementAt(0));
    			}
    		}
    	}
    
    	public void showYourNumber(string strNum)
    	{
    		yourNumber = strNum;
    		strPaint = mFont.tahoma_7.splitFontArray(yourNumber, 500);
    	}
    
    	public static void checkRemoveImage()
    	{
    		ImgByName.checkDelHash(ImgByName.hashImagePath, 10, false);
    	}
    
    	public static void StartServerPopUp(string strMsg)
    	{
    		GameCanvas.endDlg();
    		int avatar = 1139;
    		Npc npc = new Npc(-1, 0, 0, 0, 0, 0);
    		npc.avatar = avatar;
    		ChatPopup.addBigMessage(strMsg, 100000, npc);
    		ChatPopup.serverChatPopUp.cmdMsg1 = new Command(mResources.CLOSE, ChatPopup.serverChatPopUp, 1001, null);
    		ChatPopup.serverChatPopUp.cmdMsg1.x = GameCanvas.w / 2 - 35;
    		ChatPopup.serverChatPopUp.cmdMsg1.y = GameCanvas.h - 35;
    	}
    
    	public static bool ispaintPhubangBar()
    	{
    		if (TileMap.mapPhuBang() && phuban_Info.type_PB == 0)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	public void paintPhuBanBar(mGraphics g, int x, int y, int w)
    	{
    		if (phuban_Info == null || isPaintOther || isPaintRada != 1 || GameCanvas.panel.isShow || !ispaintPhubangBar())
    		{
    			return;
    		}
    		if (w < fra_PVE_Bar_1.frameWidth + fra_PVE_Bar_0.frameWidth * 4)
    		{
    			w = fra_PVE_Bar_1.frameWidth + fra_PVE_Bar_0.frameWidth * 4;
    		}
    		if (x > GameCanvas.w - w / 2)
    		{
    			x = GameCanvas.w - w / 2;
    		}
    		if (x < mGraphics.getImageWidth(imgKhung) + w / 2 + 10)
    		{
    			x = mGraphics.getImageWidth(imgKhung) + w / 2 + 10;
    		}
    		int frameHeight = fra_PVE_Bar_0.frameHeight;
    		int num = y + frameHeight + mGraphics.getImageHeight(imgBall) / 2 + 2;
    		int frameWidth = fra_PVE_Bar_1.frameWidth;
    		int num2 = w / 2 - frameWidth / 2;
    		int num3 = x - w / 2;
    		int num4 = x + frameWidth / 2;
    		int y2 = y + 3;
    		int num5 = num2 - fra_PVE_Bar_0.frameWidth;
    		int num6 = num5 / fra_PVE_Bar_0.frameWidth;
    		if (num5 % fra_PVE_Bar_0.frameWidth > 0)
    		{
    			num6++;
    		}
    		for (int i = 0; i < num6; i++)
    		{
    			if (i < num6 - 1)
    			{
    				fra_PVE_Bar_0.drawFrame(1, num3 + fra_PVE_Bar_0.frameWidth + i * fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    			}
    			else
    			{
    				fra_PVE_Bar_0.drawFrame(1, num3 + num5, y2, 0, 0, g);
    			}
    			if (i < num6 - 1)
    			{
    				fra_PVE_Bar_0.drawFrame(1, num4 + i * fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    			}
    			else
    			{
    				fra_PVE_Bar_0.drawFrame(1, num4 + num5 - fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    			}
    		}
    		fra_PVE_Bar_0.drawFrame(0, num3, y2, 2, 0, g);
    		fra_PVE_Bar_0.drawFrame(0, num4 + num5, y2, 0, 0, g);
    		if (phuban_Info.pointTeam1 > 0)
    		{
    			int idx = 2;
    			int idx2 = 3;
    			if (phuban_Info.color_1 == 4)
    			{
    				idx = 4;
    				idx2 = 5;
    			}
    			int num7 = phuban_Info.pointTeam1 * num2 / phuban_Info.maxPoint;
    			if (num7 < 0)
    			{
    				num7 = 0;
    			}
    			if (num7 > num2)
    			{
    				num7 = num2;
    			}
    			g.setClip(num3 + num2 - num7, y2, num7, frameHeight);
    			for (int j = 0; j < num6; j++)
    			{
    				if (j < num6 - 1)
    				{
    					fra_PVE_Bar_0.drawFrame(idx2, num3 + fra_PVE_Bar_0.frameWidth + j * fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    				}
    				else
    				{
    					fra_PVE_Bar_0.drawFrame(idx2, num3 + num5, y2, 0, 0, g);
    				}
    			}
    			fra_PVE_Bar_0.drawFrame(idx, num3, y2, 2, 0, g);
    			GameCanvas.resetTrans(g);
    		}
    		if (phuban_Info.pointTeam2 > 0)
    		{
    			int idx3 = 2;
    			int idx4 = 3;
    			if (phuban_Info.color_2 == 4)
    			{
    				idx3 = 4;
    				idx4 = 5;
    			}
    			int num8 = phuban_Info.pointTeam2 * num2 / phuban_Info.maxPoint;
    			if (num8 < 0)
    			{
    				num8 = 0;
    			}
    			if (num8 > num2)
    			{
    				num8 = num2;
    			}
    			g.setClip(num4, y2, num8, frameHeight);
    			for (int k = 0; k < num6; k++)
    			{
    				if (k < num6 - 1)
    				{
    					fra_PVE_Bar_0.drawFrame(idx4, num4 + k * fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    				}
    				else
    				{
    					fra_PVE_Bar_0.drawFrame(idx4, num4 + num5 - fra_PVE_Bar_0.frameWidth, y2, 0, 0, g);
    				}
    			}
    			fra_PVE_Bar_0.drawFrame(idx3, num4 + num5, y2, 0, 0, g);
    			GameCanvas.resetTrans(g);
    		}
    		fra_PVE_Bar_1.drawFrame(0, x - frameWidth / 2, y, 0, 0, g);
    		string timeCountDown = mSystem.getTimeCountDown(phuban_Info.timeStart, phuban_Info.timeSecond, true, false);
    		mFont.tahoma_7b_yellow.drawString(g, timeCountDown, x + 1, y + fra_PVE_Bar_1.frameHeight / 2 - mFont.tahoma_7b_green2.getHeight() / 2, 2);
    		Panel.setTextColor(phuban_Info.color_1, 1).drawString(g, phuban_Info.nameTeam1, x - 5, num + 5, 1);
    		Panel.setTextColor(phuban_Info.color_2, 1).drawString(g, phuban_Info.nameTeam2, x + 5, num + 5, 0);
    		if (phuban_Info.type_PB != 0)
    		{
    			int y3 = y + frameHeight / 2 - 2;
    			mFont.bigNumber_While.drawString(g, string.Empty + phuban_Info.pointTeam1, num3 + num2 / 2, y3, 2);
    			mFont.bigNumber_While.drawString(g, string.Empty + phuban_Info.pointTeam2, num4 + num2 / 2, y3, 2);
    		}
    		g.drawImage(imgVS, x, y + fra_PVE_Bar_1.frameHeight + 2, 3);
    		if (phuban_Info.type_PB == 0)
    		{
    			paintChienTruong_Life(g, phuban_Info.maxLife, phuban_Info.color_1, phuban_Info.lifeTeam1, x - 13, phuban_Info.color_2, phuban_Info.lifeTeam2, x + 13, num);
    		}
    	}
    
    	public static void paintChienTruong_Life(mGraphics g, int maxLife, int cl1, int lifeTeam1, int x1, int cl2, int lifeTeam2, int x2, int y)
    	{
    		if (imgBall == null)
    		{
    			return;
    		}
    		int num = mGraphics.getImageHeight(imgBall) / 2;
    		for (int i = 0; i < maxLife; i++)
    		{
    			int num2 = 0;
    			if (i < lifeTeam1)
    			{
    				num2 = 1;
    			}
    			g.drawRegion(imgBall, 0, num2 * num, mGraphics.getImageWidth(imgBall), num, 0, x1 - i * (num + 1), y, mGraphics.VCENTER | mGraphics.HCENTER);
    		}
    		for (int j = 0; j < maxLife; j++)
    		{
    			int num3 = 0;
    			if (j < lifeTeam2)
    			{
    				num3 = 1;
    			}
    			g.drawRegion(imgBall, 0, num3 * num, mGraphics.getImageWidth(imgBall), num, 0, x2 + j * (num + 1), y, mGraphics.VCENTER | mGraphics.HCENTER);
    		}
    	}
    
    	public static void paintHPBar_NEW(mGraphics g, int x, int y, Char c)
    	{
    		g.drawImage(imgKhung, x, y, 0);
    		int x2 = x + 3;
    		int num = y + 19;
    		int num2 = 0;
    		int num3 = 0;
    		int width = imgHP_NEW.getWidth();
    		int num4 = imgHP_NEW.getHeight() / 2;
    		// Cung ly do voi paintImageBar: cHPFull = 0 la nem ngoai le giua luc
    		// ve, ca khung hinh con lai mat trang.
    		num2 = (int)(c.cHP * width / ((c.cHPFull > 0) ? c.cHPFull : 1));
    		if (num2 <= 0)
    		{
    			num2 = 1;
    		}
    		else if (num2 > width)
    		{
    			num2 = width;
    		}
    		g.drawRegion(imgHP_NEW, 0, num4, num2, num4, 0, x2, num, 0);
    		num3 = c.cMP * width / ((c.cMPFull > 0) ? c.cMPFull : 1);
    		if (num3 <= 0)
    		{
    			num3 = 1;
    		}
    		else if (num3 > width)
    		{
    			num3 = width;
    		}
    		g.drawRegion(imgHP_NEW, 0, 0, num3, num4, 0, x2, num + 6, 0);
    		int x3 = x + imgKhung.getWidth() / 2 + 1;
    		int y2 = num + 13;
    		mFont.tahoma_7_green2.drawString(g, c.cName, x3, y + 4, 2);
    		if (c.mobFocus != null)
    		{
    			if (c.mobFocus.getTemplate() != null)
    			{
    				mFont.tahoma_7_green2.drawString(g, c.mobFocus.getTemplate().name, x3, y2, 2);
    			}
    		}
    		else if (c.npcFocus != null)
    		{
    			mFont.tahoma_7_green2.drawString(g, c.npcFocus.template.name, x3, y2, 2);
    		}
    		else if (c.charFocus != null)
    		{
    			mFont.tahoma_7_green2.drawString(g, c.charFocus.cName, x3, y2, 2);
    		}
    	}
    
    	public static void addEffectEnd(int type, int subtype, int typePaint, int x, int y, int levelPaint, int dir, short timeRemove, Point[] listObj)
    	{
    		Effect_End eff = new Effect_End(type, subtype, typePaint, x, y, levelPaint, dir, timeRemove, listObj);
    		addEffect2Vector(eff);
    	}
    
    	public static void addEffectEnd_Target(int type, int subtype, int typePaint, Char charUse, Point target, int levelPaint, short timeRemove, short range)
    	{
    		Effect_End eff = new Effect_End(type, subtype, typePaint, charUse.clone(), target, levelPaint, timeRemove, range);
    		addEffect2Vector(eff);
    	}
    
    	public static void addEffect2Vector(Effect_End eff)
    	{
    		if (eff.levelPaint == 0)
    		{
    			EffectManager.addHiEffect(eff);
    		}
    		else if (eff.levelPaint == 1)
    		{
    			EffectManager.addMidEffects(eff);
    		}
    		else if (eff.levelPaint == 2)
    		{
    			EffectManager.addMid_2Effects(eff);
    		}
    		else
    		{
    			EffectManager.addLowEffect(eff);
    		}
    	}
    
    	public static bool setIsInScreen(int x, int y, int wOne, int hOne)
    	{
    		if (x < cmx - wOne || x > cmx + GameCanvas.w + wOne || y < cmy - hOne || y > cmy + GameCanvas.h + hOne * 3 / 2)
    		{
    			return false;
    		}
    		return true;
    	}
    
    	public static bool isSmallScr()
    	{
    		if (GameCanvas.w <= 320)
    		{
    			return true;
    		}
    		return false;
    	}
    
    	private void paint_xp_bar(mGraphics g)
    	{
    		g.setColor(8421504);
    		g.fillRect(0, GameCanvas.h - 2, GameCanvas.w, 2);
    		int w = (int)(Char.myCharz().cLevelPercent * GameCanvas.w / 10000);
    		g.setColor(16777215);
    		g.fillRect(0, GameCanvas.h - 2, w, 2);
    		g.setColor(0);
    		w = GameCanvas.w / 10;
    		for (int i = 1; i < 10; i++)
    		{
    			g.fillRect(i * w, GameCanvas.h - 2, 1, 2);
    		}
    	}
    
    	private void paint_ios_bg(mGraphics g)
    	{
    		if (mSystem.clientType == 5)
    		{
    			if (imgBgIOS != null)
    			{
    				g.setColor(16777215);
    				g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
    				g.drawImage(imgBgIOS, GameCanvas.w / 2, GameCanvas.h / 2, mGraphics.VCENTER | mGraphics.HCENTER);
    			}
    			else
    			{
    				int num = ((TileMap.bgID % 2 != 0) ? 1 : 2);
    				imgBgIOS = GameCanvas.loadImage("/bg/bg_ios_" + num + ".png");
    			}
    		}
    	}
    
    	public void paint_CT(mGraphics g, int x, int y, int w)
    	{
    		w = 194;
    		w = 182;
    		w = 170;
    		int num = 66;
    		int num2 = 11;
    		if (x > GameCanvas.w - w / 2)
    		{
    			x = GameCanvas.w - w / 2;
    		}
    		if (x < mGraphics.getImageWidth(imgKhung) + w / 2 + 10)
    		{
    			x = mGraphics.getImageWidth(imgKhung) + w / 2 + 10;
    		}
    		int frameHeight = fra_PVE_Bar_0.frameHeight;
    		int num3 = y + frameHeight + mGraphics.getImageHeight(imgBall) / 2 + 2;
    		int frameWidth = fra_PVE_Bar_1.frameWidth;
    		int num4 = w / 2 - frameWidth / 2;
    		int num5 = x - w / 2 + 3;
    		int num6 = x + frameWidth / 2;
    		int num7 = y + 3;
    		int num8 = num4 - fra_PVE_Bar_0.frameWidth;
    		int num9 = num8 / fra_PVE_Bar_0.frameWidth;
    		if (num8 % fra_PVE_Bar_0.frameWidth > 0)
    		{
    			num9++;
    		}
    		for (int i = 0; i < num9; i++)
    		{
    			if (i < num9 - 1)
    			{
    				g.drawRegion(img_ct_bar_0, 0, 15, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num5 + fra_PVE_Bar_0.frameWidth + i * fra_PVE_Bar_0.frameWidth, num7, mGraphics.TOP | mGraphics.LEFT, true);
    			}
    			else
    			{
    				g.drawRegion(img_ct_bar_0, 0, 15, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num5 + num8, num7, mGraphics.TOP | mGraphics.LEFT, true);
    			}
    			if (i < num9 - 1)
    			{
    				g.drawRegion(img_ct_bar_0, 0, 15, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num6 + i * fra_PVE_Bar_0.frameWidth, num7, mGraphics.TOP | mGraphics.LEFT, true);
    			}
    			else
    			{
    				g.drawRegion(img_ct_bar_0, 0, 15, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num6 + num8 - fra_PVE_Bar_0.frameWidth, num7, mGraphics.TOP | mGraphics.LEFT, true);
    			}
    		}
    		fra_PVE_Bar_0.drawFrame(0, num5, num7, 2, 0, g);
    		fra_PVE_Bar_0.drawFrame(0, num6 + num8, num7, 0, 0, g);
    		int num10 = nCT_TeamA * 100 / (nCT_nBoyBaller / 2) * num / 100;
    		if (num10 > 0)
    		{
    			if (num10 < 6)
    			{
    				num10 = 6;
    			}
    			g.setClip(num5, num7, num10, 15);
    		}
    		if (nCT_TeamA > 0)
    		{
    			for (int j = 0; j < num2; j++)
    			{
    				if (j == 0)
    				{
    					g.drawRegion(img_ct_bar_0, 0, 60, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num5, num7, mGraphics.TOP | mGraphics.LEFT, true);
    				}
    				else
    				{
    					g.drawRegion(img_ct_bar_0, 0, 75, mGraphics.getImageWidth(img_ct_bar_0), 15, 2, num5 + j * 6, num7, mGraphics.TOP | mGraphics.LEFT, true);
    				}
    			}
    		}
    		GameCanvas.resetTrans(g);
    		int num11 = nCT_TeamB * 100 / (nCT_nBoyBaller / 2) * num / 100;
    		if (num - (num - num11) > 0)
    		{
    			if (num11 < 6)
    			{
    				num11 = 6;
    			}
    			g.setClip(num6 + num - num11, num7, num - (num - num11), 15);
    		}
    		if (nCT_TeamB > 0)
    		{
    			for (int k = 0; k < num2; k++)
    			{
    				if (k == 0)
    				{
    					g.drawRegion(img_ct_bar_0, 0, 30, mGraphics.getImageWidth(img_ct_bar_0), 15, 0, num6 + num8, num7, mGraphics.TOP | mGraphics.LEFT, true);
    				}
    				else
    				{
    					g.drawRegion(img_ct_bar_0, 0, 45, mGraphics.getImageWidth(img_ct_bar_0), 15, 0, num6 + num8 - k * 6, num7, mGraphics.TOP | mGraphics.LEFT, true);
    				}
    			}
    		}
    		GameCanvas.resetTrans(g);
    		fra_PVE_Bar_1.drawFrame(0, x - frameWidth / 2 + 1, y, 0, 0, g);
    		string st = NinjaUtil.getTime((int)((nCT_timeBallte - mSystem.currentTimeMillis()) / 1000)) + string.Empty;
    		mFont.tahoma_7b_yellow.drawString(g, st, num5 + w / 2 - 2, y + 5, 2);
    		mFont.tahoma_7_grey.drawString(g, "Tầng " + nCT_floor, num5 + w / 2 - 3, y + fra_PVE_Bar_1.frameHeight, mFont.CENTER);
    		int width = mFont.tahoma_7b_red.getWidth(nCT_TeamA + string.Empty);
    		mFont.tahoma_7b_blue.drawString(g, nCT_TeamA + string.Empty, x - frameWidth / 2 - width, num7 + fra_PVE_Bar_1.frameHeight, 0);
    		SmallImage.drawSmallImage(g, 2325, x - frameWidth / 2 - width - 15, num7 + fra_PVE_Bar_1.frameHeight, 2, mGraphics.TOP | mGraphics.LEFT);
    		width = mFont.tahoma_7b_red.getWidth(nCT_TeamB + string.Empty);
    		mFont.tahoma_7b_red.drawString(g, nCT_TeamB + string.Empty, x + frameWidth / 2, num7 + fra_PVE_Bar_1.frameHeight, 0);
    		SmallImage.drawSmallImage(g, 2323, x + frameWidth / 2 + width + 3, num7 + fra_PVE_Bar_1.frameHeight, 0, mGraphics.TOP | mGraphics.LEFT);
    		paint_board_CT(g, GameCanvas.w - mFont.tahoma_7b_dark.getWidth("#01 AAAAAAAAAA"), 40);
    		GameCanvas.resetTrans(g);
    	}
    
    	private void paint_board_CT(mGraphics g, int x, int y)
    	{
    		if (!is_Paint_boardCT_Expand)
    		{
    			string s = "#01 nnnnnnnnnnnn";
    			int width = mFont.tahoma_7.getWidth(s);
    			int num = GameCanvas.w - width - 20;
    			for (int i = 0; i < nTop; i++)
    			{
    				mFont mFont2 = mFont.tahoma_7_white;
    				switch (i)
    				{
    				case 0:
    					mFont2 = mFont.tahoma_7_red;
    					break;
    				case 1:
    					mFont2 = mFont.tahoma_7_yellow;
    					break;
    				case 2:
    					mFont2 = mFont.tahoma_7_blue;
    					break;
    				}
    				if (i == nTop - 1)
    				{
    					mFont2 = mFont.tahoma_7_green;
    				}
    				string[] array = Res.split((string)res_CT.elementAt(i), "|", 0);
    				int[] array2 = new int[2] { 0, 18 };
    				for (int j = 0; j < 2; j++)
    				{
    					mFont2.drawString(g, array[j], num + array2[j], y + i * mFont.tahoma_7.getHeight(), 0, mFont.tahoma_7);
    				}
    			}
    			GameCanvas.resetTrans(g);
    			xRect = num;
    			yRect = y;
    			wRect = width + 10;
    			hRect = mFont.tahoma_7b_dark.getHeight() * 6;
    		}
    		else
    		{
    			string s2 = "#01 namec1000000 0001   00000";
    			int[] array3 = new int[4] { 0, 18, 80, 101 };
    			int width2 = mFont.tahoma_7.getWidth(s2);
    			int num2 = GameCanvas.w - width2 - 20;
    			int num3 = y;
    			for (int k = 0; k < nTop; k++)
    			{
    				string[] array4 = Res.split((string)res_CT.elementAt(k), "|", 0);
    				mFont mFont3 = mFont.tahoma_7_white;
    				switch (k)
    				{
    				case 0:
    					mFont3 = mFont.tahoma_7_red;
    					break;
    				case 1:
    					mFont3 = mFont.tahoma_7_yellow;
    					break;
    				case 2:
    					mFont3 = mFont.tahoma_7_blue;
    					break;
    				}
    				if (k == nTop - 1)
    				{
    					mFont3 = mFont.tahoma_7_green;
    				}
    				num3 = k * mFont.tahoma_7_white.getHeight() + y;
    				for (int l = 0; l < array3.Length; l++)
    				{
    					mFont3.drawString(g, array4[l], num2 + array3[l], num3, 0, mFont.tahoma_7);
    				}
    			}
    			xRect = num2;
    			yRect = y;
    			wRect = width2 + 10;
    			hRect = mFont.tahoma_7b_dark.getHeight() * 6;
    		}
    		GameCanvas.resetTrans(g);
    	}
    
    	private void paintHPCT(mGraphics g, int x, int y, Char c)
    	{
    		g.drawImage(imgKhung, x, y, 0);
    		int x2 = x + 3;
    		int num = y + 19;
    		int num2 = 0;
    		int num3 = 0;
    		int width = imgHP_NEW.getWidth();
    		int num4 = imgHP_NEW.getHeight() / 2;
    		// Cung ly do voi paintImageBar: cHPFull = 0 la nem ngoai le giua luc
    		// ve, ca khung hinh con lai mat trang.
    		num2 = (int)(c.cHP * width / ((c.cHPFull > 0) ? c.cHPFull : 1));
    		if (num2 <= 0)
    		{
    			num2 = 1;
    		}
    		else if (num2 > width)
    		{
    			num2 = width;
    		}
    		g.drawRegion(imgHP_NEW, 0, num4, 80, num4, 0, x2, num, 0);
    		num3 = c.cMP * width / ((c.cMPFull > 0) ? c.cMPFull : 1);
    		if (num3 <= 0)
    		{
    			num3 = 1;
    		}
    		else if (num3 > width)
    		{
    			num3 = width;
    		}
    		g.drawRegion(imgHP_NEW, 0, 0, 80, num4, 0, x2, num + 6, 0);
    	}
    }
}
