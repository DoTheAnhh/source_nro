using Game6.Assets.src.g;
using Game6.God;
using System;
using System.Collections.Generic;
using UnityEngine;

namespace Game6
{
    
    public class Panel : IActionListener, IChatable
    {
        public class PlayerChat
        {
            public string name;
    
            public int charID;
    
            public bool isNewMessage;
    
            public List<InfoItem> chats = new List<InfoItem>();
    
            public PlayerChat(string name, int charId)
            {
                this.name = name;
                charID = charId;
                isNewMessage = true;
            }
        }
    
        public bool isShow;
    
        public int X;
    
        public int Y;
    
        public int W;
    
        public int H;
    
        public int ITEM_HEIGHT;
    
        public int TAB_W;
    
        public int TAB_W_NEW;
    
        public int cmtoY;
    
        public int cmy;
    
        public int cmdy;
    
        public int cmvy;
    
        public int cmyLim;
    
        public int xc;
    
        public int[] cmyLast;
    
        public int cmtoX;
    
        public int cmx;
    
        public int cmxLim;
    
        public int cmxMap;
    
        public int cmyMap;
    
        public int cmxMapLim;
    
        public int cmyMapLim;
    
        public int cmyQuest;
    
        public static Image imgBantay;
    
        public static Image imgX;
    
        public static Image imgMap;
    
        public TabClanIcon tabIcon;
    
        public MyVector vItemCombine = new MyVector();
    
        public int moneyGD;
    
        public int friendMoneyGD;
    
        public bool isLock;
    
        public bool isFriendLock;
    
        public bool isAccept;
    
        public bool isFriendAccep;
    
        public string topName;
    
        public ChatTextField chatTField;
    
        public static string specialInfo;
    
        public static short spearcialImage;
    
        public static Image imgStar;
    
        public static Image imgMaxStar;
    
        public static Image imgStar8;
    
        public static Image imgStar9;
    
        public static Image imgStarCuongHoa;
    
        public static Image imgNew;
    
        public static Image imgXu;
    
        public static Image imgTicket;
    
        public static Image imgLuong;
    
        public static Image imgLuongKhoa;
    
        private static Image imgUp;
    
        private static Image imgDown;
    
        private int pa1;
    
        private int pa2;
    
        private bool trans;
    
        private int pX;
    
        private int pY;
    
        private Command left = new Command(mResources.SELECT, 0);
    
        public int type;
    
        public int currentTabIndex;
    
        public int startTabPos;
    
        public int[] lastTabIndex;
    
        public string[][] currentTabName;
    
        private int[] currClanOption;
    
        public int mainTabPos = 4;
    
        public int shopTabPos = 50;
    
        public int boxTabPos = 50;
    
        public string[][] mainTabName;
    
        public string[] mapNames;
    
        public string[] planetNames;
    
        public static string[] strTool = new string[7]
        {
            mResources.gameInfo,
            mResources.change_flag,
            mResources.change_zone,
            mResources.chat_world,
            mResources.account,
            mResources.option,
            mResources.change_account
        };
    
        public static string[] strCauhinh = new string[4]
        {
            (!GameCanvas.isPlaySound) ? mResources.turnOnSound : mResources.turnOffSound,
            mResources.increase_vga,
            mResources.analog,
            (mGraphics.zoomLevel <= 1) ? mResources.x2Screen : mResources.x1Screen
        };
    
        public static string[] strAccount = new string[5]
        {
            mResources.inventory_Pass,
            mResources.friend,
            mResources.enemy,
            mResources.msg,
            mResources.charger
        };
    
        public static string[] strAuto = new string[1] { mResources.useGem };
    
        public static int graphics = 0;
    
        public string[][] shopTabName;
    
        public int[] maxPageShop;
    
        public int[] currPageShop;
    
        private static string[][] boxTabName = new string[2][]
        {
            mResources.chestt,
            mResources.inventory
        };
    
        private static string[][] boxCombine = new string[2][]
        {
            mResources.combine,
            mResources.inventory
        };
    
        private static string[][] boxZone = new string[1][] { mResources.zonee };
    
        private static string[][] boxMap = new string[1][] { mResources.mapp };
    
        private static string[][] boxGD = new string[3][]
        {
            mResources.inventory,
            mResources.item_give,
            mResources.item_receive
        };
    
        private static string[][] boxPet = mResources.petMainTab;
    
        public string[][][] tabName = new string[27][][]
        {
            null,
            null,
            boxTabName,
            boxZone,
            boxMap,
            null,
            null,
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            boxCombine,
            boxGD,
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            boxPet,
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } },
            new string[1][] { new string[1] { string.Empty } }
        };
    
        private static sbyte BOX_BAG = 0;
    
        private static sbyte BAG_BOX = 1;
    
        private static sbyte BOX_BODY = 2;
    
        private static sbyte BODY_BOX = 3;
    
        private static sbyte BAG_BODY = 4;
    
        private static sbyte BODY_BAG = 5;
    
        private static sbyte BAG_PET = 6;
    
        private static sbyte PET_BAG = 7;
    
        public int hasUse;
    
        public int hasUseBag;
    
        public int currentListLength;
    
        private int[] lastSelect;
    
        public static int[] mapIdTraidat = new int[16]
        {
            21, 0, 1, 2, 24, 3, 4, 5, 6, 27,
            28, 29, 30, 42, 47, 46
        };
    
        public static int[] mapXTraidat = new int[16]
        {
            39, 42, 105, 93, 61, 93, 142, 165, 210, 100,
            165, 220, 233, 10, 125, 125
        };
    
        public static int[] mapYTraidat = new int[16]
        {
            28, 60, 48, 96, 88, 131, 136, 95, 32, 200,
            189, 167, 120, 110, 20, 20
        };
    
        public static int[] mapIdNamek = new int[14]
        {
            22, 7, 8, 9, 25, 11, 12, 13, 10, 31,
            32, 33, 34, 43
        };
    
        public static int[] mapXNamek = new int[14]
        {
            55, 30, 93, 80, 24, 149, 219, 220, 233, 170,
            148, 195, 148, 10
        };
    
        public static int[] mapYNamek = new int[14]
        {
            136, 84, 69, 34, 25, 42, 32, 110, 192, 70,
            106, 156, 210, 57
        };
    
        public static int[] mapIdSaya = new int[14]
        {
            23, 14, 15, 16, 26, 17, 18, 20, 19, 35,
            36, 37, 38, 44
        };
    
        public static int[] mapXSaya = new int[14]
        {
            90, 95, 144, 234, 231, 122, 176, 158, 205, 54,
            105, 159, 231, 27
        };
    
        public static int[] mapYSaya = new int[14]
        {
            10, 43, 20, 36, 69, 87, 112, 167, 160, 151,
            173, 207, 194, 29
        };
    
        public static int[][] mapId = new int[3][] { mapIdTraidat, mapIdNamek, mapIdSaya };
    
        public static int[][] mapX = new int[3][] { mapXTraidat, mapXNamek, mapXSaya };
    
        public static int[][] mapY = new int[3][] { mapYTraidat, mapYNamek, mapYSaya };
    
        public Item currItem;
    
        public Clan currClan;
    
        public ClanMessage currMess;
    
        public Member currMem;
    
        public Clan[] clans;
    
        public MyVector member;
    
        public MyVector myMember;
    
        public MyVector logChat = new MyVector();
    
        public MyVector vPlayerMenu = new MyVector();
    
        public MyVector vFriend = new MyVector();
    
        public MyVector vMyGD = new MyVector();
    
        public MyVector vFriendGD = new MyVector();
    
        public MyVector vTop = new MyVector();
    
        public MyVector vEnemy = new MyVector();
    
        public MyVector vFlag = new MyVector();
    
        public MyVector vPlayerMenu_id = new MyVector();
    
        public Command cmdClose;
    
        public static bool CanNapTien = false;
    
        public static int WIDTH_PANEL = 240;
    
        private int position;
    
        public string playerChat;
    
        public Dictionary<string, PlayerChat> chats = new Dictionary<string, PlayerChat>();
    
        public Char charMenu;
    
        private bool isThachDau;
    
        public int typeShop = -1;
    
        public int xScroll;
    
        public int yScroll;
    
        public int wScroll;
    
        public int hScroll;
    
        public ChatPopup cp;
    
        public int idIcon;
    
        public int[] partID;
    
        private int timeShow;
    
        public bool isBoxClan;
    
        public int w;
    
        private int pa;
    
        public int selected;
    
        private int cSelected;
    
        private int newSelected;
    
        private bool isClanOption;
    
        public bool isSearchClan;
    
        public bool isMessage;
    
        public bool isViewMember;
    
        public const int TYPE_MAIN = 0;
    
        public const int TYPE_SHOP = 1;
    
        public const int TYPE_BOX = 2;
    
        public const int TYPE_ZONE = 3;
    
        public const int TYPE_MAP = 4;
    
        public const int TYPE_CLANS = 5;
    
        public const int TYPE_INFOMATION = 6;
    
        public const int TYPE_BODY = 7;
    
        public const int TYPE_MESS = 8;
    
        public const int TYPE_ARCHIVEMENT = 9;
    
        public const int PLAYER_MENU = 10;
    
        public const int TYPE_FRIEND = 11;
    
        public const int TYPE_COMBINE = 12;
    
        public const int TYPE_GIAODICH = 13;
    
        public const int TYPE_MAPTRANS = 14;
    
        public const int TYPE_TOP = 15;
    
        public const int TYPE_ENEMY = 16;
    
        public const int TYPE_KIGUI = 17;
    
        public const int TYPE_FLAG = 18;
    
        public const int TYPE_OPTION = 19;
    
        public const int TYPE_ACCOUNT = 20;
    
        public const int TYPE_PET_MAIN = 21;
    
        public const int TYPE_AUTO = 22;
    
        public const int TYPE_GAMEINFO = 23;
    
        public const int TYPE_GAMEINFOSUB = 24;
    
        public const int TYPE_SPEACIALSKILL = 25;
    
        private int pointerDownTime;
    
        private int pointerDownFirstX;

        /// <summary>Toạ độ y lúc đặt ngón xuống, giữ nguyên tới khi nhả.</summary>
        /// <remarks>
        /// Khác <c>pointerDownFirstX</c>: biến đó bị đặt về -1000 ngay khi vòng
        /// giữ ngón chạy, nên sau đó không còn dùng để biết cú bấm bắt đầu ở đâu.
        /// Biến này chỉ ghi một lần lúc bấm xuống, để phần nhả ngón phân biệt được
        /// "bấm một dòng" với "kéo để cuộn".
        /// </remarks>
        private int yBamDau;
    
        private int[] pointerDownLastX = new int[3];
    
        private bool pointerIsDowning;
    
        private bool isDownWhenRunning;
    
        private bool wantUpdateList;
    
        private int waitToPerform;
    
        private int cmRun;
    
        private int keyTouchLock = -1;
    
        private int keyToundGD = -1;
    
        private int keyTouchCombine = -1;
    
        private int keyTouchMapButton = -1;
    
        public int indexMouse = -1;
    
        private bool justRelease;
    
        private int keyTouchTab = -1;
    
        private int nTableItem;
    
        public string[][] clansOption = new string[2][]
        {
            mResources.findClan,
            mResources.createClan
        };
    
        public string clanInfo = string.Empty;
    
        public string clanReport = string.Empty;
    
        private bool isHaveClan;
    
        private Scroll scroll;
    
        private int cmvx;
    
        private int cmdx;
    
        private bool isSelectPlayerMenu;
    
        private string[] strStatus = new string[6]
        {
            mResources.follow,
            mResources.defend,
            mResources.attack,
            mResources.gohome,
            mResources.fusion,
            mResources.fusionForever
        };
    
        private static string log;
    
        private int tt;
    
        private int currentButtonPress;
    
        public static long[] t_tiemnang = new long[14]
        {
            50000000L, 250000000L, 1250000000L, 5000000000L, 15000000000L, 30000000000L, 45000000000L, 60000000000L, 75000000000L, 90000000000L,
            110000000000L, 130000000000L, 150000000000L, 170000000000L
        };
    
        private int[] zoneColor = new int[3] { 43520, 14743570, 14155776 };
    
        public string[] combineInfo;
    
        public string[] combineTopInfo;
    
        public static int[] color1 = new int[3] { 2327248, 8982199, 16713222 };
    
        public static int[] color2 = new int[3] { 4583423, 16719103, 16714764 };
    
        private int sellectInventory;

        /// <summary>So cot cua hanh trang luoi.</summary>
        private const int SO_COT_LUOI = 8;

        /// <summary>So hang o cua mot trang tui.</summary>
        private const int SO_HANG_LUOI = 6;

        /// <summary>So o moi trang tui — 6 hang x 5 cot.</summary>
        private const int SO_O_MOI_TRANG = SO_COT_LUOI * SO_HANG_LUOI;

    
        private Item itemInvenNew;
    
        private Effect eBanner;
    
        private static FrameImage screenTab6;
    
        private bool isUp;
    
        private int compare;
    
        public static string strWantToBuy = string.Empty;
    
        public int xstart;
    
        public int ystart;
    
        public int popupW = 140;
    
        public int popupH = 160;
    
        public int cmySK;
    
        public int cmtoYSK;
    
        public int cmdySK;
    
        public int cmvySK;
    
        public int cmyLimSK;
    
        public int popupY;
    
        public int popupX;
    
        public int isborderIndex;
    
        public int isselectedRow;
    
        public int indexSize = 28;
    
        public int indexTitle;
    
        public int indexSelect;
    
        public int indexRow = -1;
    
        public int indexRowMax;
    
        public int indexMenu;
    
        public int columns = 6;
    
        public int rows;
    
        public int inforX;
    
        public int inforY;
    
        public int inforW;
    
        public int inforH;
    
        private int yPaint;
    
        private int xMap;
    
        private int yMap;
    
        private int xMapTask;
    
        private int yMapTask;
    
        private int xMove;
    
        private int yMove;
    
        public static bool isPaintMap = true;
    
        public bool isClose;
    
        private int infoSelect;
    
        public static MyVector vGameInfo = new MyVector(string.Empty);
    
        public static string[] contenInfo;
    
        public bool isViewChatServer;
    
        private int currInfoItem;
    
        public Char charInfo;
    
        private bool isChangeZone;
    
        private bool isKiguiXu;
    
        private bool isKiguiLuong;
    
        private int delayKigui;
    
        public sbyte combineSuccess = -1;
    
        public int idNPC;
    
        public int xS;
    
        public int yS;
    
        private int rS;
    
        private int angleS;
    
        private int angleO;
    
        private int iAngleS;
    
        private int iDotS;
    
        private int speed;
    
        private int[] xArgS;
    
        private int[] yArgS;
    
        private int[] xDotS;
    
        private int[] yDotS;
    
        private int time;
    
        private int typeCombine;
    
        private int countUpdate;
    
        private int countR;
    
        private int countWait;
    
        private bool isSpeedCombine;
    
        private bool isCompleteEffCombine = true;
    
        private bool isPaintCombine;
    
        public bool isDoneCombine = true;
    
        public short iconID1;
    
        public short iconID2;
    
        public short iconID3;
    
        public short[] iconID;
    
        public string[][] speacialTabName;
    
        public static int[] sizeUpgradeEff = new int[3] { 2, 1, 1 };
    
        public static int nsize = 1;
    
        public const sbyte COLOR_WHITE = 0;
    
        public const sbyte COLOR_GREEN = 1;
    
        public const sbyte COLOR_PURPLE = 2;
    
        public const sbyte COLOR_ORANGE = 3;
    
        public const sbyte COLOR_BLUE = 4;
    
        public const sbyte COLOR_YELLOW = 5;
    
        public const sbyte COLOR_RED = 6;
    
        public const sbyte COLOR_BLACK = 7;
    
        public static int[][] colorUpgradeEffect = new int[7][]
        {
            new int[6] { 16777215, 15000805, 13487823, 11711155, 9671828, 7895160 },
            new int[6] { 61952, 58624, 52224, 45824, 39168, 32768 },
            new int[6] { 13500671, 12058853, 10682572, 9371827, 7995545, 6684800 },
            new int[6] { 16744192, 15037184, 13395456, 11753728, 10046464, 8404992 },
            new int[6] { 37119, 33509, 28108, 24499, 21145, 17536 },
            new int[6] { 16776192, 15063040, 12635136, 11776256, 10063872, 8290304 },
            new int[6] { 16711680, 15007744, 13369344, 11730944, 10027008, 8388608 }
        };
    
        public const int color_item_white = 15987701;
    
        public const int color_item_green = 2786816;
    
        public const int color_item_purple = 7078041;
    
        public const int color_item_orange = 12537346;
    
        public const int color_item_blue = 1269146;
    
        public const int color_item_yellow = 13279744;
    
        public const int color_item_red = 11599872;
    
        public const int color_item_black = 2039326;
    
        private Image imgo_0;
    
        private Image imgo_1;
    
        private Image imgo_2;
    
        private Image imgo_3;
    
        public const int numItem = 20;
    
        public const sbyte INVENTORY_TAB = 1;
    
        public sbyte size_tab;
    
        // Bat giao dien hanh trang kieu LUOI (4x5 o + cot do dang mac) thay cho
        // danh sach doc cu. Toan bo ma cua giao dien nay da co san trong file --
        // ca phan ve lan phan bam chon -- nhung co nay chua tung duoc gan nen no
        // luon false, khien nhanh do khong bao gio chay.
        /// <summary>
        /// Ve hanh trang theo LUOI o vuong thay vi danh sach doc.
        /// </summary>
        /// <remarks>
        /// Ca duong ve luoi da co san trong lop nay tu truoc, nhung co bi de
        /// false nen khong ai thay. Bat len la duoc luoi 5 cot x 4 dong, chia
        /// trang 20 o mot trang — dung nhu ban goc cua game.
        ///
        /// Cac cho khac trong lop nay deu re nhanh theo co nay (bat cham, cuon,
        /// menu vat pham), nen chi doi mot cho nay la ca hanh trang doi theo.
        /// </remarks>
        private bool isnewInventory = true;
        private static int[][] a_id = new int[][]
      {
            new int[]
            {
            3, 14, 25, 35,
            36, 37, 38, 119
            },
            new int[]
            {
            1, 42, 53, 64,
            75, 86, 97, 108
            }
      };
        private static int[][] b_id = new int[][]
        {
            new int[]
            {
            27, 28, 29, 30,
            31, 32, 33, 34
            },
            new int[]
            {
            8, 19, 20, 21,
            22, 23, 24, 26
            }
        };
        private static int[][] c_id = new int[][]
        {
            new int[]
            {
                48, 49, 50, 51,
                52, 54, 55, 56
            },
            new int[]
            {
                39, 40, 41, 43,
                44, 45, 46, 47
            }
        };
        private static int[][] d_id = new int[][]
        {
            new int[]
            {
                66, 67, 68, 69,
                70, 71, 72, 73
            },
            new int[]
            {
                57, 58, 59, 60,
                61, 62, 63, 65
            }
        };
        private static int[][] e_id = new int[][]
        {
            new int[]
            {
                83, 84, 85, 87,
                88, 89, 90, 91
            },
            new int[]
            {
                74, 76, 77, 78,
                79, 80, 81, 82
            }
        };
        private static int[][] f_id = new int[][]
        {
            new int[]
            {
                101, 102, 103, 104,
                105, 106, 107, 109
            },
            new int[]
            {
                92, 93, 94, 95,
                96, 98, 99, 100
            }
        };
        private static int[][] g_id = new int[][]
        {
            new int[]
            {
                118, 120, 121, 122,
                123, 124, 125, 126
            },
            new int[]
            {
                110, 111, 112, 113,
                114, 115, 116, 117
            }
        };
        private static int[][] h_id = new int[][]
        {
            new int[]
            {
                9, 10, 11, 12,
                13, 15, 16, 17
            },
            new int[]
            {
                4, 5, 6, 7,
                8, 127, 128, 129
            }
        };
    
        private static List<Image> a = new List<Image>(), a_bg = new List<Image>();
        private static List<Image> b = new List<Image>(), b_bg = new List<Image>();
        private static List<Image> c = new List<Image>(), c_bg = new List<Image>();
        private static List<Image> d = new List<Image>(), d_bg = new List<Image>();
        private static List<Image> e = new List<Image>(), e_bg = new List<Image>();
        private static List<Image> f = new List<Image>(), f_bg = new List<Image>();
        private static List<Image> g = new List<Image>(), g_bg = new List<Image>();
        private static List<Image> h = new List<Image>(), h_bg = new List<Image>();
        public Panel()
        {
            init();
            cmdClose = new Command(string.Empty, this, 1003, null);
            cmdClose.img = GameCanvas.loadImage("/mainImage/myTexture2dbtX.png");
            cmdClose.cmdClosePanel = true;
            currItem = null;
        }
        static void loadEff()
        {
            var imageMap = new Dictionary<char, (List<Image> bg, List<Image> img)>
            {
                ['a'] = (a_bg, a),
                ['b'] = (b_bg, b),
                ['c'] = (c_bg, c),
                ['d'] = (d_bg, d),
                ['e'] = (e_bg, e),
                ['f'] = (f_bg, f),
                ['g'] = (g_bg, g),
                ['h'] = (h_bg, h)
            };
    
            var idMap = new Dictionary<char, int[][]>
            {
                ['a'] = a_id,
                ['b'] = b_id,
                ['c'] = c_id,
                ['d'] = d_id,
                ['e'] = e_id,
                ['f'] = f_id,
                ['g'] = g_id,
                ['h'] = h_id
            };
    
            foreach (var key in imageMap.Keys)
            {
                var (bgList, imgList) = imageMap[key];
                var ids = idMap[key];
    
                for (int i = 0; i < 2; i++)
                {
                    for (int j = 0; j < 8; j++)
                    {
                        bgList.Add(GameCanvas.loadImage($"/u/{ids[0][j]}"));
                        imgList.Add(GameCanvas.loadImage($"/u/{ids[1][j]}"));
                    }
                }
            }
        }
        public static void loadBg()
        {
            imgMap = GameCanvas.loadImage("/img/map" + TileMap.planetID + ".png");
            imgBantay = GameCanvas.loadImage("/mainImage/myTexture2dbantay.png");
            imgX = GameCanvas.loadImage("/mainImage/myTexture2dbtX.png");
            imgXu = GameCanvas.loadImage("/mainImage/myTexture2dimgMoney.png");
            imgLuong = GameCanvas.loadImage("/mainImage/myTexture2dimgDiamond.png");
            imgLuongKhoa = GameCanvas.loadImage("/mainImage/luongkhoa.png");
            imgUp = GameCanvas.loadImage("/mainImage/myTexture2dup.png");
            imgDown = GameCanvas.loadImage("/mainImage/myTexture2ddown.png");
            imgStar = GameCanvas.loadImage("/mainImage/star.png");
            imgMaxStar = GameCanvas.loadImage("/mainImage/starE.png");
            imgStar8 = GameCanvas.loadImage("/mainImage/star8.png");
            imgStar9 = mSystem.loadImage("/mainImage/star9.png");
            imgStarCuongHoa = mSystem.loadImage("/mainImage/starCH.png");
            imgNew = GameCanvas.loadImage("/mainImage/new.png");
            imgTicket = GameCanvas.loadImage("/mainImage/ticket12.png");
            loadEff();
        }
        private void paintEff(mGraphics gx, Item item, int x, int y)
        {
            try
            {
                List<Image> img_bg = null;
                List<Image> img_frame = null;
                if (item.itemOption != null && item.isHaveOption(107))
                {
                    foreach (var option in item.itemOption)
                    {
                        if (option != null)
                        {
                            img_frame = option.param switch
                            {
                                1 => a,
                                2 => b,
                                3 => c,
                                4 => d,
                                5 => e,
                                6 => f,
                                7 => g,
                                >= 8 => h,
                                _ => null
                            };
                            img_bg = option.param switch
                            {
                                1 => a_bg,
                                2 => b_bg,
                                3 => c_bg,
                                4 => d_bg,
                                5 => e_bg,
                                6 => f_bg,
                                7 => g_bg,
                                >= 8 => h_bg,
                                _ => null
                            };
                        }
                    }
                }
                if (img_bg != null && img_bg[GameCanvas.gameTick / 4 % img_bg.Count] != null)
                {
                    var img = img_bg[GameCanvas.gameTick / 4 % img_bg.Count];
                    gx.drawImage(img, x - 1, y - 1);
                    if (img_frame != null && img_frame[GameCanvas.gameTick / 4 % img_frame.Count] != null)
                    {
                        gx.drawImage(img_frame[GameCanvas.gameTick / 4 % img_frame.Count], x + img.getWidth() / 2, y + img.getHeight() / 2 - 1, 3);
                    }
                }
            }
            catch (Exception e)
            {
                Debug.LogException(e);
            }
        }
        public void init()
        {
            pX = GameCanvas.pxLast + cmxMap;
            pY = GameCanvas.pyLast + cmyMap;
            lastTabIndex = new int[tabName.Length];
            for (int i = 0; i < lastTabIndex.Length; i++)
            {
                lastTabIndex[i] = -1;
            }
        }
    
        public int getXMap()
        {
            for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
            {
                if (TileMap.mapID == mapId[TileMap.planetID][i])
                {
                    return mapX[TileMap.planetID][i];
                }
            }
            return -1;
        }
    
        public int getYMap()
        {
            for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
            {
                if (TileMap.mapID == mapId[TileMap.planetID][i])
                {
                    return mapY[TileMap.planetID][i];
                }
            }
            return -1;
        }
    
        public int getXMapTask()
        {
            if (Char.myCharz().taskMaint == null)
            {
                return -1;
            }
            for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
            {
                if (GameScr.mapTasks[Char.myCharz().taskMaint.index] == mapId[TileMap.planetID][i])
                {
                    return mapX[TileMap.planetID][i];
                }
            }
            return -1;
        }
    
        public int getYMapTask()
        {
            if (Char.myCharz().taskMaint == null)
            {
                return -1;
            }
            for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
            {
                if (GameScr.mapTasks[Char.myCharz().taskMaint.index] == mapId[TileMap.planetID][i])
                {
                    return mapY[TileMap.planetID][i];
                }
            }
            return -1;
        }
    
        private void setType(int position)
        {
            typeShop = -1;
            W = WIDTH_PANEL;
            H = GameCanvas.h;
            X = 0;
            Y = 0;
            ITEM_HEIGHT = 24;
            this.position = position;
            switch (position)
            {
                case 0:
                    xScroll = 2;
                    yScroll = 80;
                    wScroll = W - 4;
                    hScroll = H - 96;
                    cmx = wScroll;
                    cmtoX = 0;
                    X = 0;
                    break;
                case 1:
                    wScroll = W - 4;
                    xScroll = GameCanvas.w - wScroll;
                    yScroll = 80;
                    hScroll = H - 96;
                    X = xScroll - 2;
                    cmx = -(GameCanvas.w + W);
                    cmtoX = GameCanvas.w - W;
                    break;
            }
            TAB_W = W / 5 - 1;
            currentTabIndex = 0;
            currentTabName = tabName[type];
            if (currentTabName.Length < 5)
            {
                TAB_W += 5;
            }
            startTabPos = xScroll + wScroll / 2 - currentTabName.Length * TAB_W / 2;
            lastSelect = new int[currentTabName.Length];
            cmyLast = new int[currentTabName.Length];
            for (int i = 0; i < currentTabName.Length; i++)
            {
                lastSelect[i] = (GameCanvas.isTouch ? (-1) : 0);
            }
            if (lastTabIndex[type] != -1)
            {
                currentTabIndex = lastTabIndex[type];
            }
            if (currentTabIndex < 0)
            {
                currentTabIndex = 0;
            }
            if (currentTabIndex > currentTabName.Length - 1)
            {
                currentTabIndex = currentTabName.Length - 1;
            }
            scroll = null;
        }
    
        public void setTypeMapTrans()
        {
            type = 14;
            setType(0);
            setTabMapTrans();
            cmx = (cmtoX = 0);
        }
    
        public void setTypeInfomatioin()
        {
            type = 6;
            cmx = wScroll;
            cmtoX = 0;
        }
    
        public void setTypeMap()
        {
            if (!GameScr.gI().isMapFize() && isPaintMap)
            {
                if (Hint.isOnTask(2, 0))
                {
                    Hint.isViewMap = true;
                    GameScr.info1.addInfo(mResources.go_to_quest, 0);
                }
                if (Hint.isOnTask(3, 0))
                {
                    Hint.isViewPotential = true;
                }
                type = 4;
                currentTabName = tabName[type];
                startTabPos = xScroll + wScroll / 2 - currentTabName.Length * TAB_W / 2;
                cmx = (cmtoX = 0);
                setTabMap();
            }
        }
    
        public void setTypeArchivement()
        {
            currentListLength = Char.myCharz().arrArchive.Length;
            setType(0);
            type = 9;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = 0);
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        public void setTypeKiGuiOnly()
        {
            type = 17;
            setType(1);
            setTabKiGui();
            typeShop = 2;
            currentTabIndex = 0;
        }
    
        public void setTabChatManager()
        {
            currentListLength = chats.Count;
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
        }
    
        public void setTabChatPlayer()
        {
        }
    
        public void setTypeChatPlayer()
        {
        }
    
        public void setTabKiGui()
        {
            ITEM_HEIGHT = 24;
            currentListLength = Char.myCharz().arrItemShop[4].Length;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        public void setTypeBodyOnly()
        {
            type = 7;
            setType(1);
            setTabInventory(true);
            currentTabIndex = 0;
        }
    
        public void addChatMessage(InfoItem info)
        {
            logChat.insertElementAt(info, 0);
            if (logChat.size() > 20)
            {
                logChat.removeElementAt(logChat.size() - 1);
            }
        }
    
        private bool IsNewMessage(string name)
        {
            return false;
        }
    
        public bool IsHaveNewMessage()
        {
            return false;
        }
    
        private void ClearNewMessage(string name)
        {
        }
    
        public void addPlayerMenu(Command pm)
        {
            vPlayerMenu.addElement(pm);
        }
    
        public void setTabPlayerMenu()
        {
            ITEM_HEIGHT = 24;
            currentListLength = vPlayerMenu.size();
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        public void setTypeFlag()
        {
            type = 18;
            setType(0);
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            setTabFlag();
        }
    
        public void setTabFlag()
        {
            currentListLength = vFlag.size();
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            if (selected > currentListLength - 1)
            {
                selected = currentListLength - 1;
            }
            cmx = (cmtoX = 0);
        }
    
        public void setTypePlayerMenu(Char c)
        {
            type = 10;
            setType(0);
            setTabPlayerMenu();
            charMenu = c;
        }
    
        public void setTypeFriend()
        {
            type = 11;
            setType(0);
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            setTabFriend();
        }
    
        public void setTypeEnemy()
        {
            type = 16;
            setType(0);
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            setTabEnemy();
        }
    
        public void setTypeTop(sbyte t)
        {
            type = 15;
            setType(0);
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            setTabTop();
            isThachDau = ((t != 0) ? true : false);
        }
    
        public void setTabTop()
        {
            currentListLength = vTop.size();
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            if (selected > currentListLength - 1)
            {
                selected = currentListLength - 1;
            }
            cmx = (cmtoX = 0);
        }
    
        public void setTabFriend()
        {
            currentListLength = vFriend.size();
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            if (selected > currentListLength - 1)
            {
                selected = currentListLength - 1;
            }
            cmx = (cmtoX = 0);
        }
    
        public void setTabEnemy()
        {
            currentListLength = vEnemy.size();
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            if (selected > currentListLength - 1)
            {
                selected = currentListLength - 1;
            }
            cmx = (cmtoX = 0);
        }
    
        public void setTypeMessage()
        {
            type = 8;
            setType(0);
            setTabMessage();
            currentTabIndex = 0;
        }
    
        public void setTypeLockInventory()
        {
            type = 8;
            setType(0);
            setTabMessage();
            currentTabIndex = 0;
        }
    
        public void setTypeShop(int typeShop)
        {
            type = 1;
            setType(0);
            setTabShop();
            currentTabIndex = 0;
            this.typeShop = typeShop;
        }
    
        public void setTypeBox()
        {
            type = 2;
            if (GameCanvas.w >= 2 * WIDTH_PANEL)
            {
                boxTabName = new string[1][] { mResources.chestt };
            }
            else
            {
                boxTabName = new string[2][]
                {
                    mResources.chestt,
                    mResources.inventory
                };
            }
            tabName[2] = boxTabName;
            setType(0);
            if (currentTabIndex == 0)
            {
                setTabBox();
            }
            if (currentTabIndex == 1)
            {
                setTabInventory(true);
            }
            if (GameCanvas.w >= 2 * WIDTH_PANEL)
            {
                GameCanvas.panel2 = new Panel();
                GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                GameCanvas.panel2.setTypeBodyOnly();
                GameCanvas.panel2.show();
            }
        }
    
        public void setTypeCombine()
        {
            type = 12;
            if (GameCanvas.w >= 2 * WIDTH_PANEL)
            {
                boxCombine = new string[1][] { mResources.combine };
            }
            else
            {
                boxCombine = new string[2][]
                {
                    mResources.combine,
                    mResources.inventory
                };
            }
            tabName[type] = boxCombine;
            setType(0);
            if (currentTabIndex == 0)
            {
                setTabCombine();
            }
            if (currentTabIndex == 1)
            {
                setTabInventory(true);
            }
            if (GameCanvas.w >= 2 * WIDTH_PANEL)
            {
                GameCanvas.panel2 = new Panel();
                GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                GameCanvas.panel2.setTypeBodyOnly();
                GameCanvas.panel2.show();
            }
            combineSuccess = -1;
            isDoneCombine = true;
        }
    
        public void setTabCombine()
        {
            currentListLength = vItemCombine.size() + 1;
            ITEM_HEIGHT = 24;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 9;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        public void setTypeAuto()
        {
            type = 22;
            setType(0);
            setTabAuto();
            cmx = (cmtoX = 0);
        }
    
        private void setTabAuto()
        {
            currentListLength = strAuto.Length;
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
        }
    
        public void setTypePetMain()
        {
            type = 21;
            if (GameCanvas.panel2 != null)
            {
                boxPet = mResources.petMainTab2;
            }
            else
            {
                boxPet = mResources.petMainTab;
            }
            tabName[21] = boxPet;
            if (Char.myCharz().cgender == 1)
            {
                strStatus = new string[6]
                {
                    mResources.follow,
                    mResources.defend,
                    mResources.attack,
                    mResources.gohome,
                    mResources.fusion,
                    mResources.fusionForever
                };
            }
            else
            {
                strStatus = new string[5]
                {
                    mResources.follow,
                    mResources.defend,
                    mResources.attack,
                    mResources.gohome,
                    mResources.fusion
                };
            }
            setType(2);
            if (currentTabIndex == 0)
            {
                setTabPetInventory();
            }
            if (currentTabIndex == 1)
            {
                setTabPetStatus();
            }
            if (currentTabIndex == 2)
            {
                setTabInventory(true);
            }
        }
    
        public void setTypeMain()
        {
            type = 0;
            setType(0);
            if (currentTabIndex == 1)
            {
                setTabInventory(true);
            }
            if (currentTabIndex == 2)
            {
                setTabSkill();
            }
            if (currentTabIndex == 3)
            {
                if (mainTabName.Length == 4)
                {
                    setTabTool();
                }
                else
                {
                    setTabClans();
                }
            }
            if (currentTabIndex == 4)
            {
                setTabTool();
            }
        }
    
        public void setTypeZone()
        {
            type = 3;
            setType(0);
            setTabZone();
            cmx = (cmtoX = 0);
        }
    
        /// <summary>Dòng này là dòng TÊN của một set kích hoạt.</summary>
        private static bool laTenSet(string ten)
        {
            return ten != null && ten.StartsWith("Set ");
        }

        /// <summary>Con số đứng đầu dòng mốc, ví dụ "3 món: …" cho 3.</summary>
        /// <remarks>
        /// Phải có chữ "món" ngay sau con số, không thì "8 Sao Pha Lê" cũng bị
        /// nhận là một mốc set và bị tô nhầm.
        /// </remarks>
        private static int mocCuaDongSet(string ten)
        {
            if (ten == null)
            {
                return 0;
            }
            int i = 0;
            int so = 0;
            while (i < ten.Length && ten[i] >= 48 && ten[i] <= 57)
            {
                so = so * 10 + (ten[i] - 48);
                i++;
            }
            if (so == 0 || i >= ten.Length)
            {
                return 0;
            }
            return ten.Substring(i).TrimStart().StartsWith("món") ? so : 0;
        }

        /// <summary>Mốc set CAO NHẤT mà số món đang mặc còn đạt tới.</summary>
        /// <remarks>
        /// <para>Bảng thông tin này trước đây để mọi dòng mốc cùng một màu, nên
        /// nhìn vào không biết mình đang hưởng mốc nào — phải tự đếm số món trên
        /// người. Giờ đếm hộ và tô sáng đúng một dòng.</para>
        ///
        /// <para>Tô mốc CAO NHẤT đạt được chứ không tô mọi mốc nhỏ hơn: tô hết
        /// cũng đúng về nghĩa nhưng đọc rối, không thấy ngay mình đang ở đâu.</para>
        /// </remarks>
        private static int mocDangHuongCua(Item item)
        {
            if (item == null || item.itemOption == null)
            {
                return 0;
            }
            int idSet = -1;
            for (int i = 0; i < item.itemOption.Length; i++)
            {
                ItemOption op = item.itemOption[i];
                if (op != null && op.optionTemplate != null
                        && laTenSet(op.optionTemplate.name))
                {
                    idSet = op.optionTemplate.id;
                    break;
                }
            }
            if (idSet < 0)
            {
                return 0;
            }
            int soMon = demMonCungSet(idSet);
            int cao = 0;
            for (int i = 0; i < item.itemOption.Length; i++)
            {
                ItemOption op = item.itemOption[i];
                if (op == null || op.optionTemplate == null)
                {
                    continue;
                }
                int moc = mocCuaDongSet(op.optionTemplate.name);
                if (moc > 0 && moc <= soMon && moc > cao)
                {
                    cao = moc;
                }
            }
            return cao;
        }

        /// <summary>Số món ĐANG MẶC mang cùng mã chỉ số set này.</summary>
        private static int demMonCungSet(int idSet)
        {
            if (idSet < 0 || Char.myCharz() == null)
            {
                return 0;
            }
            Item[] mac = Char.myCharz().arrItemBody;
            if (mac == null)
            {
                return 0;
            }
            int dem = 0;
            for (int i = 0; i < mac.Length; i++)
            {
                if (mac[i] == null || mac[i].itemOption == null)
                {
                    continue;
                }
                for (int j = 0; j < mac[i].itemOption.Length; j++)
                {
                    ItemOption op = mac[i].itemOption[j];
                    if (op != null && op.optionTemplate != null
                            && op.optionTemplate.id == idSet)
                    {
                        dem++;
                        break;
                    }
                }
            }
            return dem;
        }

        /// <summary>Chỉ số này là chỉ số nhận diện của một set kích hoạt.</summary>
        /// <remarks>
        /// Máy chủ đánh số set trong hai khoảng đó (xem SetBonusDAO). Các dòng
        /// chữ mô tả set thì mang tên bắt đầu bằng "$" và đã gom sẵn ở nhánh
        /// trên, nên chỉ còn dòng tên set cần nhận ra ở đây.
        /// </remarks>
        private static bool laChiSoSet(int id)
        {
            return (id >= 127 && id <= 135) || (id >= 250 && id <= 255);
        }

        public void addItemDetail(Item item)
        {
            try
            {
                cp = new ChatPopup();
                string empty = string.Empty;
                string text = string.Empty;
                // Hai cum chi so gom rieng roi moi ghep vao, de con dong khung:
                // chi so cua mon mot khung, chi so set kich hoat mot khung.
                string nhomChiSo = string.Empty;
                string nhomSet = string.Empty;
                // Moc set DANG huong, de to sang dung mot dong.
                int mocDat = mocDangHuongCua(item);
                if (item.template.gender != Char.myCharz().cgender)
                {
                    if (item.template.gender == 0)
                    {
                        text = text + "\n|7|1|" + mResources.from_earth;
                    }
                    else if (item.template.gender == 1)
                    {
                        text = text + "\n|7|1|" + mResources.from_namec;
                    }
                    else if (item.template.gender == 2)
                    {
                        text = text + "\n|7|1|" + mResources.from_sayda;
                    }
                }
                string text2 = string.Empty;
                if (item.itemOption != null)
                {
                    for (int i = 0; i < item.itemOption.Length; i++)
                    {
                        if (item.itemOption[i].optionTemplate.id == 72)
                        {
                            text2 = " [+" + item.itemOption[i].param + "]";
                        }
                    }
                }
                bool flag = false;
                if (item.itemOption != null)
                {
                    for (int j = 0; j < item.itemOption.Length; j++)
                    {
                        if (item.itemOption[j].optionTemplate.id == 41)
                        {
                            flag = true;
                            if (item.itemOption[j].param == 1)
                            {
                                text = text + "|0|1|" + item.template.name + text2;
                            }
                            if (item.itemOption[j].param == 2)
                            {
                                text = text + "|2|1|" + item.template.name + text2;
                            }
                            if (item.itemOption[j].param == 3)
                            {
                                text = text + "|8|1|" + item.template.name + text2;
                            }
                            if (item.itemOption[j].param == 4)
                            {
                                text = text + "|7|1|" + item.template.name + text2;
                            }
                        }
                    }
                }
                if (!flag)
                {
                    text = text + "|0|1|" + item.template.name + text2;
                }
                if (item.itemOption != null)
                {
                    for (int k = 0; k < item.itemOption.Length; k++)
                    {
                        if (item.itemOption[k].optionTemplate.name.StartsWith("$") ? true : false)
                        {
                            empty = item.itemOption[k].getOptiongColor();
                            if (item.itemOption[k].param == 1)
                            {
                                nhomSet = nhomSet + "\n|2|1|" + empty;
                            }
                            if (item.itemOption[k].param == 0)
                            {
                                nhomSet = nhomSet + "\n|0|1|" + empty;
                            }
                        }
                        else
                        {
                            empty = item.itemOption[k].getOptionString();
                            if (!empty.Equals(string.Empty))
                            {
                                if (item.itemOption[k].optionTemplate.id == 72)
                                {
                                    continue;
                                }
                                if (item.itemOption[k].optionTemplate.id == 102)
                                {
                                    cp.starSlot = (sbyte)item.itemOption[k].param;
                                }
                                else if (item.itemOption[k].optionTemplate.id == 107)
                                {
                                    cp.maxStarSlot = (sbyte)item.itemOption[k].param;
                                }
                                else if (laTenSet(item.itemOption[k].optionTemplate.name))
                                {
                                    // Ten set: do, giong ten set trong tui moi.
                                    nhomSet = nhomSet + "\n|7|1|" + empty;
                                }
                                else if (mocCuaDongSet(
                                        item.itemOption[k].optionTemplate.name) > 0)
                                {
                                    // Moc set: moc DANG huong to xanh duong, cac
                                    // moc chua toi de nau dam.
                                    bool dangHuong = mocCuaDongSet(
                                            item.itemOption[k].optionTemplate.name)
                                            == mocDat;
                                    nhomSet = nhomSet + (dangHuong ? "\n|2|1|" : "\n|0|1|")
                                            + empty;
                                }
                                else if (laChiSoSet(item.itemOption[k].optionTemplate.id))
                                {
                                    nhomSet = nhomSet + "\n|7|1|" + empty;
                                }
                                else
                                {
                                    // Mau 0 la nau dam: dong chi so gio nhan ra
                                    // bang vach mau ben trai, chu de mot mau cho
                                    // de doc.
                                    nhomChiSo = nhomChiSo + "\n|0|1|" + empty;
                                }
                            }
                        }
                        if (item.itemOption[k].optionTemplate.id != 228)
                        {
                            continue;
                        }
                        Res.outz("========>>> " + item.itemOption[k].optionTemplate.name + "_" + item.itemOption[k].param);
                        if (item.itemOption[k].param > 7)
                        {
                            for (int l = 0; l < item.itemOption[k].param - 7; l++)
                            {
                                cp.starCuongHoa[l + 7] = true;
                            }
                        }
                    }
                }
                if (!nhomChiSo.Equals(string.Empty))
                {
                    text = text + "\n[[" + nhomChiSo + "\n]]";
                }
                if (!nhomSet.Equals(string.Empty))
                {
                    text = text + "\n[[" + nhomSet + "\n]]";
                }
                if (currItem.template.strRequire > 1)
                {
                    string text3 = mResources.pow_request + ": " + currItem.template.strRequire;
                    if (currItem.template.strRequire > Char.myCharz().cPower)
                    {
                        text = text + "\n|3|1|" + text3;
                        string text4 = text;
                        text = text4 + "\n|3|1|" + mResources.your_pow + ": " + Char.myCharz().cPower;
                    }
                    else
                    {
                        text = text + "\n|6|1|" + text3;
                    }
                }
                else
                {
                    text += "\n|6|1|";
                }
                currItem.compare = getCompare(currItem);
                text += "\n--";
                text = text + "\n|6|" + item.template.description;
                if (!item.reason.Equals(string.Empty))
                {
                    if (!item.template.description.Equals(string.Empty))
                    {
                        text += "\n--";
                    }
                    text = text + "\n|2|" + item.reason;
                }
                if (cp.maxStarSlot > 0)
                {
                    text += "\n\n";
                }
                popUpDetailInit(cp, text);
                idIcon = item.template.iconID;
                partID = null;
                charInfo = null;
            }
            catch (Exception ex)
            {
                Res.outz("ex " + ex.StackTrace);
            }
        }
    
        public void popUpDetailInit(ChatPopup cp, string chat)
        {
            cp.isClip = false;
            cp.sayWidth = 180;
            cp.cx = 3 + X - ((X != 0) ? (Res.abs(cp.sayWidth - W) + 8) : 0);
            cp.says = mFont.tahoma_7_red.splitFontArray(chat, cp.sayWidth - 10);
            cp.delay = 10000000;
            cp.c = null;
            cp.sayRun = 7;
            cp.ch = 15 - cp.sayRun + cp.says.Length * 12 + 10;
            if (cp.ch > GameCanvas.h - 80)
            {
                cp.ch = GameCanvas.h - 80;
                cp.lim = cp.says.Length * 12 - cp.ch + 17;
                if (cp.lim < 0)
                {
                    cp.lim = 0;
                }
                ChatPopup.cmyText = 0;
                cp.isClip = true;
            }
            cp.cy = GameCanvas.menu.menuY - cp.ch;
            while (cp.cy < 10)
            {
                cp.cy++;
                GameCanvas.menu.menuY++;
            }
            cp.mH = 0;
            cp.strY = 10;
        }
    
        public void popUpDetailInitArray(ChatPopup cp, string[] chat)
        {
            cp.sayWidth = 160;
            cp.cx = 3 + X;
            cp.says = chat;
            cp.delay = 10000000;
            cp.c = null;
            cp.sayRun = 7;
            cp.ch = 15 - cp.sayRun + cp.says.Length * 12 + 10;
            cp.cy = GameCanvas.menu.menuY - cp.ch;
            cp.mH = 0;
            cp.strY = 10;
        }
    
        public void addMessageDetail(ClanMessage cm)
        {
            cp = new ChatPopup();
            string text = "|0|" + cm.playerName;
            text = text + "\n|1|" + Member.getRole(cm.role);
            for (int i = 0; i < myMember.size(); i++)
            {
                Member member = (Member)myMember.elementAt(i);
                if (cm.playerId == member.ID)
                {
                    string text2 = text;
                    text = text2 + "\n|5|" + mResources.clan_capsuledonate + ": " + member.clanPoint;
                    text2 = text;
                    text = text2 + "\n|5|" + mResources.clan_capsuleself + ": " + member.curClanPoint;
                    text2 = text;
                    text = text2 + "\n|4|" + mResources.give_pea + ": " + member.donate + mResources.time;
                    text2 = text;
                    text = text2 + "\n|4|" + mResources.receive_pea + ": " + member.receive_donate + mResources.time;
                    partID = new int[3] { member.head, member.leg, member.body };
                    break;
                }
            }
            text += "\n--";
            for (int j = 0; j < cm.chat.Length; j++)
            {
                text = text + "\n" + cm.chat[j];
            }
            if (cm.type == 1)
            {
                string text2 = text;
                text = text2 + "\n|6|" + mResources.received + " " + cm.recieve + "/" + cm.maxCap;
            }
            popUpDetailInit(cp, text);
            charInfo = null;
        }
    
        public void addThachDauDetail(TopInfo t)
        {
            string text = "|0|1|" + t.name;
            text = text + "\n|1|Top " + t.rank;
            text = text + "\n|1|" + t.info;
            text = text + "\n|2|" + t.info2;
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            partID = new int[3] { t.headID, t.leg, t.body };
            currItem = null;
            charInfo = null;
        }
    
        public void addClanMemberDetail(Member m)
        {
            string text = "|0|1|" + m.name;
            string text2 = "\n|2|1|";
            if (m.role == 0)
            {
                text2 = "\n|7|1|";
            }
            if (m.role == 1)
            {
                text2 = "\n|1|1|";
            }
            if (m.role == 2)
            {
                text2 = "\n|0|1|";
            }
            text = text + text2 + Member.getRole(m.role);
            string text3 = text;
            text = text3 + "\n|2|1|" + mResources.power + ": " + m.powerPoint;
            text += "\n--";
            text3 = text;
            text = text3 + "\n|5|" + mResources.clan_capsuledonate + ": " + m.clanPoint;
            text3 = text;
            text = text3 + "\n|5|" + mResources.clan_capsuleself + ": " + m.curClanPoint;
            text3 = text;
            text = text3 + "\n|4|" + mResources.give_pea + ": " + m.donate + mResources.time;
            text3 = text;
            text = text3 + "\n|4|" + mResources.receive_pea + ": " + m.receive_donate + mResources.time;
            text3 = text;
            text = text3 + "\n|6|" + mResources.join_date + ": " + m.joinTime;
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            partID = new int[3] { m.head, m.leg, m.body };
            currItem = null;
            charInfo = null;
        }
    
        public void addClanDetail(Clan cl)
        {
            try
            {
                string text = "|0|" + cl.name;
                string[] array = mFont.tahoma_7_green.splitFontArray(cl.slogan, wScroll - 60);
                for (int i = 0; i < array.Length; i++)
                {
                    text = text + "\n|2|" + array[i];
                }
                text += "\n--";
                string text2 = text;
                text = text2 + "\n|7|" + mResources.clan_leader + ": " + cl.leaderName;
                text2 = text;
                text = text2 + "\n|1|" + mResources.power_point + ": " + cl.powerPoint;
                text2 = text;
                text = text2 + "\n|4|" + mResources.member + ": " + cl.currMember + "/" + cl.maxMember;
                text2 = text;
                text = text2 + "\n|4|" + mResources.level + ": " + cl.level;
                text2 = text;
                text = text2 + "\n|4|" + mResources.clan_birthday + ": " + NinjaUtil.getDate(cl.date);
                cp = new ChatPopup();
                popUpDetailInit(cp, text);
                idIcon = ClanImage.getClanImage((short)cl.imgID).idImage[0];
                currItem = null;
            }
            catch (Exception ex)
            {
                Res.outz("Throw  exception " + ex.StackTrace);
            }
        }
    
        public void addSkillDetail(SkillTemplate tp, Skill skill, Skill nextSkill)
        {
            string text = "|0|" + tp.name;
            for (int i = 0; i < tp.description.Length; i++)
            {
                text = text + "\n|4|" + tp.description[i];
            }
            text += "\n--";
            if (skill != null)
            {
                string text2 = text;
                text = text2 + "\n|2|" + mResources.cap_do + ": " + skill.point;
                text = text + "\n|5|" + NinjaUtil.replace(tp.damInfo, "#", skill.damage + string.Empty);
                text2 = text;
                text = text2 + "\n|5|" + mResources.KI_consume + skill.manaUse + ((tp.manaUseType != 1) ? string.Empty : "%");
                text2 = text;
                text = text2 + "\n|5|" + mResources.cooldown + ": " + skill.strTimeReplay() + "s";
                text += "\n--";
                if (skill.point == tp.maxPoint)
                {
                    text = text + "\n|0|" + mResources.max_level_reach;
                }
                else
                {
                    if (!skill.template.isSkillSpec())
                    {
                        text2 = text;
                        text = text2 + "\n|1|" + mResources.next_level_require + Res.formatNumber(nextSkill.powRequire) + " " + mResources.potential;
                    }
                    text = text + "\n|4|" + NinjaUtil.replace(tp.damInfo, "#", nextSkill.damage + string.Empty);
                }
            }
            else
            {
                text = text + "\n|2|" + mResources.not_learn;
                string text2 = text;
                text = text2 + "\n|1|" + mResources.learn_require + Res.formatNumber(nextSkill.powRequire) + " " + mResources.potential;
                text = text + "\n|4|" + NinjaUtil.replace(tp.damInfo, "#", nextSkill.damage + string.Empty);
                text2 = text;
                text = text2 + "\n|4|" + mResources.KI_consume + nextSkill.manaUse + ((tp.manaUseType != 1) ? string.Empty : "%");
                text2 = text;
                text = text2 + "\n|4|" + mResources.cooldown + ": " + nextSkill.strTimeReplay() + "s";
            }
            currItem = null;
            partID = null;
            charInfo = null;
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            idIcon = 0;
        }
    
        public void show()
        {
            if (GameCanvas.isTouch)
            {
                cmdClose.x = 156;
                cmdClose.y = 3;
            }
            else
            {
                cmdClose.x = GameCanvas.w - 19;
                cmdClose.y = GameCanvas.h - 19;
            }
            cmdClose.isPlaySoundButton = false;
            ChatPopup.currChatPopup = null;
            InfoDlg.hide();
            timeShow = 20;
            isShow = true;
            isClose = false;
            SoundMn.gI().panelOpen();
            if (isTypeShop())
            {
                Char.myCharz().setPartOld();
            }
        }
    
        public void chatTFUpdateKey()
        {
            if (chatTField != null && chatTField.isShow)
            {
                if (chatTField.left != null && (GameCanvas.keyPressed[12] || mScreen.getCmdPointerLast(chatTField.left)) && chatTField.left != null)
                {
                    chatTField.left.performAction();
                }
                if (chatTField.right != null && (GameCanvas.keyPressed[13] || mScreen.getCmdPointerLast(chatTField.right)) && chatTField.right != null)
                {
                    chatTField.right.performAction();
                }
                if (chatTField.center != null && (GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25] || mScreen.getCmdPointerLast(chatTField.center)) && chatTField.center != null)
                {
                    chatTField.center.performAction();
                }
                if (chatTField.isShow && GameCanvas.keyAsciiPress != 0)
                {
                    chatTField.keyPressed(GameCanvas.keyAsciiPress);
                    GameCanvas.keyAsciiPress = 0;
                }
                GameCanvas.clearKeyHold();
                GameCanvas.clearKeyPressed();
            }
        }
    

        /// <summary>
        /// Bảng chỉ số đầy đủ, mở bằng nút "?" ở góc phải vùng đầu bảng.
        ///
        /// Chỉ dùng dữ liệu client ĐÃ CÓ sẵn — không xin thêm gì từ máy chủ, nên
        /// không phải đụng vào gói tin.
        /// </summary>
        private void moBangChiSoDayDu()
        {
            try
            {
                Char c = Char.myCharz();
                System.Text.StringBuilder sb = new System.Text.StringBuilder();
                sb.Append(c.cName).Append("\n");
                sb.Append("------------------------\n");
                sb.Append(mResources.HP).Append(": ")
                        .Append(NinjaUtil.getMoneys((long)(c.cHP))).Append(" / ")
                        .Append(NinjaUtil.getMoneys((long)(c.cHPFull))).Append("\n");
                sb.Append(mResources.KI).Append(": ")
                        .Append(NinjaUtil.getMoneys(c.cMP)).Append(" / ")
                        .Append(NinjaUtil.getMoneys(c.cMPFull)).Append("\n");
                sb.Append(mResources.hit_point).Append(": ")
                        .Append(NinjaUtil.getMoneys(c.cDamFull)).Append("\n");
                sb.Append(mResources.armor).Append(": ")
                        .Append(NinjaUtil.getMoneys(c.cDefull)).Append("\n");
                sb.Append(mResources.critical).Append(": ")
                        .Append(c.cCriticalFull).Append("%\n");
                sb.Append("Né: ").Append(tinhNeDon()).Append("%\n");
                sb.Append("------------------------\n");
                sb.Append("Sức mạnh: ").Append(NinjaUtil.getMoneys(c.cPower)).Append("\n");
                sb.Append("Tiềm năng: ").Append(NinjaUtil.getMoneys(c.cTiemNang)).Append("\n");
                sb.Append("------------------------\n");
                sb.Append("Vàng: ").Append(NinjaUtil.getMoneys(c.xu)).Append("\n");
                sb.Append("Ngọc: ").Append(NinjaUtil.getMoneys(c.luong)).Append("\n");
                sb.Append("Ngọc khoá: ").Append(NinjaUtil.getMoneys(c.luongKhoa));
                GameCanvas.startOKDlg(sb.ToString());
            }
            catch (Exception)
            {
                // Dữ liệu nhân vật chưa nạp xong -> bỏ qua, không làm treo panel.
            }
        }
        public void updateKey()
        {
            if ((chatTField != null && chatTField.isShow) || !GameCanvas.panel.isDoneCombine || InfoDlg.isShow)
            {
                return;
            }
            // Nut "?" mo bang chi so day du. Dat NGAY DAU updateKey de no
            // duoc xet truoc cac vung bam khac dang chong len vung dau bang.
            // Bat buoc kiem nutChiSoDangHien: khong co no thi tab nao
            // cung bam trung vung nay du chang tab nao ve nut o do.
            if (isShow && nutChiSoDangHien && GameCanvas.isPointerJustRelease
                    && GameCanvas.isPointerHoldIn(X + wScroll - 24, 24, 20, 14))
            {
                GameCanvas.isPointerJustRelease = false;
                moBangChiSoDayDu();
                return;
            }
            if (tabIcon != null && tabIcon.isShow)
            {
                tabIcon.updateKey();
            }
            else
            {
                if (isClose || !isShow)
                {
                    return;
                }
                if (cmdClose.isPointerPressInside())
                {
                    cmdClose.performAction();
                    return;
                }
                if (GameCanvas.keyPressed[13])
                {
                    if (type != 4)
                    {
                        hide();
                        return;
                    }
                    setTypeMain();
                    cmx = (cmtoX = 0);
                }
                if (GameCanvas.keyPressed[12] || GameCanvas.keyPressed[(!Main.isPC) ? 5 : 25])
                {
                    if (left.idAction > 0)
                    {
                        perform(left.idAction, left.p);
                    }
                    else
                    {
                        waitToPerform = 2;
                    }
                }
                if (Equals(GameCanvas.panel) && GameCanvas.panel2 == null && GameCanvas.isPointerJustRelease && !GameCanvas.isPointer(X, Y, W, H) && !pointerIsDowning)
                {
                    hide();
                    return;
                }
                if (!isClanOption)
                {
                    updateKeyInTabBar();
                }
                switch (type)
                {
                    case 23:
                    case 24:
                    case 30:
                        updateKeyScrollView();
                        break;
                    case 21:
                        if (currentTabIndex == 0)
                        {
                            updateKeyScrollView();
                        }
                        if (currentTabIndex == 1)
                        {
                            updateKeyPetStatus();
                        }
                        if (currentTabIndex == 2)
                        {
                            updateKeyScrollView();
                        }
                        break;
                    case 0:
                        if (currentTabIndex == 0)
                        {
                            updateKeyQuest();
                            GameCanvas.clearKeyPressed();
                            return;
                        }
                        if (currentTabIndex == 1)
                        {
                            updateKeyInventory();
                        }
                        if (currentTabIndex == 2)
                        {
                            updateKeySkill();
                        }
                        if (currentTabIndex == 3)
                        {
                            if (mainTabName.Length == 4)
                            {
                                updateKeyTool();
                            }
                            else
                            {
                                updateKeyClans();
                            }
                        }
                        if (currentTabIndex == 4)
                        {
                            updateKeyTool();
                        }
                        break;
                    case 2:
                        updateKeyInventory();
                        break;
                    case 3:
                        updateKeyScrollView();
                        break;
                    case 14:
                        updateKeyScrollView();
                        break;
                    case 1:
                    case 17:
                    case 25:
                        if (currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
                        {
                            updateKeyScrollView();
                        }
                        else if (typeShop == 0)
                        {
                            updateKeyInventory();
                        }
                        else
                        {
                            updateKeyScrollView();
                        }
                        break;
                    case 4:
                        updateKeyMap();
                        GameCanvas.clearKeyPressed();
                        return;
                    case 7:
                        updateKeyInventory();
                        break;
                    case 8:
                        updateKeyScrollView();
                        break;
                    case 9:
                        updateKeyScrollView();
                        break;
                    case 10:
                        updateKeyScrollView();
                        break;
                    case 11:
                    case 16:
                        updateKeyScrollView();
                        break;
                    case 15:
                        updateKeyScrollView();
                        break;
                    case 12:
                        updateKeyCombine();
                        break;
                    case 13:
                        updateKeyGiaoDich();
                        break;
                    case 18:
                        updateKeyScrollView();
                        break;
                    case 19:
                        updateKeyOption();
                        break;
                    case 20:
                        updateKeyOption();
                        break;
                    case 22:
                        updateKeyAuto();
                        break;
                }
                GameCanvas.clearKeyHold();
                for (int i = 0; i < GameCanvas.keyPressed.Length; i++)
                {
                    GameCanvas.keyPressed[i] = false;
                }
            }
        }
    
        private void updateKeyAuto()
        {
        }
    
        private void updateKeyPetStatus()
        {
            updateKeyScrollView();
        }
    
        private void updateKeyPetSkill()
        {
        }
    
        private void keyGiaodich()
        {
            updateKeyScrollView();
        }
    
        private void updateKeyGiaoDich()
        {
            if (currentTabIndex == 0)
            {
                if (Equals(GameCanvas.panel))
                {
                    updateKeyInventory();
                }
                if (Equals(GameCanvas.panel2))
                {
                    keyGiaodich();
                }
            }
            if (currentTabIndex == 1 || currentTabIndex == 2)
            {
                keyGiaodich();
            }
        }
    
        private void updateKeyTool()
        {
            updateKeyScrollView();
        }
    
        private void updateKeySkill()
        {
            updateKeyScrollView();
        }
    
        private void updateKeyClanIcon()
        {
            updateKeyScrollView();
        }
    
        public void setTabGiaoDich(bool isMe)
        {
            currentListLength = ((!isMe) ? (vFriendGD.size() + 3) : (vMyGD.size() + 3));
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
        }
    
        public void setTypeGiaoDich(Char cGD)
        {
            type = 13;
            tabName[type] = boxGD;
            isAccept = false;
            isLock = false;
            isFriendLock = false;
            vMyGD.removeAllElements();
            vFriendGD.removeAllElements();
            moneyGD = 0;
            friendMoneyGD = 0;
            if (GameCanvas.w >= 2 * WIDTH_PANEL)
            {
                GameCanvas.panel2 = new Panel();
                GameCanvas.panel2.type = 13;
                GameCanvas.panel2.tabName[type] = new string[1][] { mResources.item_receive };
                GameCanvas.panel2.setType(1);
                GameCanvas.panel2.setTabGiaoDich(false);
                GameCanvas.panel.tabName[type] = new string[2][]
                {
                    mResources.inventory,
                    mResources.item_give
                };
                GameCanvas.panel2.show();
                GameCanvas.panel2.charMenu = cGD;
            }
            if (Equals(GameCanvas.panel))
            {
                setType(0);
            }
            if (currentTabIndex == 0)
            {
                setTabInventory(true);
            }
            if (currentTabIndex == 1)
            {
                setTabGiaoDich(true);
            }
            if (currentTabIndex == 2)
            {
                setTabGiaoDich(false);
            }
            charMenu = cGD;
        }
    
        private void paintGiaoDich(mGraphics g, bool isMe)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            MyVector myVector = ((!isMe) ? vFriendGD : vMyGD);
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll + 36;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 36;
                int num4 = ITEM_HEIGHT - 1;
                int num5 = xScroll;
                int num6 = yScroll + i * ITEM_HEIGHT;
                int num7 = 34;
                int num8 = ITEM_HEIGHT - 1;
                if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                if (i == currentListLength - 1)
                {
                    if (!isMe)
                    {
                        continue;
                    }
                    g.setColor(15196114);
                    g.fillRect(num5, num2, wScroll, num4);
                    if (!isLock)
                    {
                        if (!isFriendLock)
                        {
                            mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.not_lock_trade, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                        }
                        else
                        {
                            mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.locked_trade, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                        }
                    }
                    else if (isFriendLock)
                    {
                        g.setColor(15196114);
                        g.fillRect(num5, num2, wScroll, num4);
                        g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num2 + 2, StaticObj.TOP_RIGHT);
                        ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, mResources.done, xScroll + wScroll - 22, num2 + 7, 2);
                        mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.locked_trade, xScroll + 5, num2 + num4 / 2 - 4, mFont.LEFT);
                    }
                    else
                    {
                        mFont.tahoma_7_grey.drawString(g, mResources.opponent + mResources.not_lock_trade, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                    }
                    continue;
                }
                if (i == currentListLength - 2)
                {
                    if (isMe)
                    {
                        g.setColor(15196114);
                        g.fillRect(num5, num2, wScroll, num4);
                        if (!isAccept)
                        {
                            if (!isLock)
                            {
                                g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num2 + 2, StaticObj.TOP_RIGHT);
                                ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, mResources.mlock, xScroll + wScroll - 22, num2 + 7, 2);
                                mFont.tahoma_7_grey.drawString(g, mResources.you + mResources.not_lock_trade, xScroll + 5, num2 + num4 / 2 - 4, mFont.LEFT);
                            }
                            else
                            {
                                g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num2 + 2, StaticObj.TOP_RIGHT);
                                ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, mResources.CANCEL, xScroll + wScroll - 22, num2 + 7, 2);
                                mFont.tahoma_7_grey.drawString(g, mResources.you + mResources.locked_trade, xScroll + 5, num2 + num4 / 2 - 4, mFont.LEFT);
                            }
                        }
                    }
                    else if (!isFriendLock)
                    {
                        mFont.tahoma_7b_dark.drawString(g, mResources.not_lock_trade_upper, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                    }
                    else
                    {
                        mFont.tahoma_7b_dark.drawString(g, mResources.locked_trade_upper, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                    }
                    continue;
                }
                if (i == currentListLength - 3)
                {
                    if (isLock)
                    {
                        g.setColor(13748667);
                    }
                    else
                    {
                        g.setColor((i != selected) ? 15196114 : 16383818);
                    }
                    g.fillRect(num, num2, num3, num4);
                    if (isLock)
                    {
                        g.setColor(13748667);
                    }
                    else
                    {
                        g.setColor((i != selected) ? 9993045 : 7300181);
                    }
                    g.fillRect(num5, num6, num7, num8);
                    g.drawImage(imgXu, num5 + num7 / 2, num6 + num8 / 2, 3);
                    mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys((!isMe) ? friendMoneyGD : moneyGD) + " " + mResources.XU, num + 5, num2 + 11, 0);
                    mFont.tahoma_7_green.drawString(g, mResources.money_trade, num + 5, num2, 0);
                    continue;
                }
                if (myVector.size() == 0)
                {
                    return;
                }
                if (isLock)
                {
                    g.setColor(13748667);
                }
                else
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                }
                g.fillRect(num, num2, num3, num4);
                if (isLock)
                {
                    g.setColor(13748667);
                }
                else
                {
                    g.setColor((i != selected) ? 9993045 : 9541120);
                }
                Item item = (Item)myVector.elementAt(i);
                if (item != null)
                {
                    for (int j = 0; j < item.itemOption.Length; j++)
                    {
                        if (item.itemOption[j].optionTemplate.id != 72 || item.itemOption[j].param <= 0)
                        {
                            continue;
                        }
                        sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[j].param);
                        int color_ItemBg = GetColor_ItemBg(color_Item_Upgrade);
                        if (color_ItemBg != -1)
                        {
                            if (isLock)
                            {
                                g.setColor(13748667);
                            }
                            else
                            {
                                g.setColor((i != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
                            }
                        }
                    }
                }
                g.fillRect(num5, num6, num7, num8);
                if (item == null)
                {
                    continue;
                }
                string text = string.Empty;
                mFont mFont2 = mFont.tahoma_7_green2;
                if (item.itemOption != null)
                {
                    for (int k = 0; k < item.itemOption.Length; k++)
                    {
                        if (item.itemOption[k].optionTemplate.id == 72)
                        {
                            text = " [+" + item.itemOption[k].param + "]";
                        }
                        if (item.itemOption[k].optionTemplate.id == 107)
                        {
                            paintItemStar(g, item.itemOption[k].param.ToString(), num + 125, num2 + 1);
                        }
                        if (item.itemOption[k].optionTemplate.id == 41)
                        {
                            if (item.itemOption[k].param == 1)
                            {
                                mFont2 = GetFont(0);
                            }
                            else if (item.itemOption[k].param == 2)
                            {
                                mFont2 = GetFont(2);
                            }
                            else if (item.itemOption[k].param == 3)
                            {
                                mFont2 = GetFont(8);
                            }
                            else if (item.itemOption[k].param == 4)
                            {
                                mFont2 = GetFont(7);
                            }
                        }
                    }
                }
                mFont2.drawString(g, item.template.name + text, num + 5, num2 + 1, 0);
                string text2 = string.Empty;
                if (item.itemOption != null)
                {
                    if (item.itemOption.Length > 0 && item.itemOption[0] != null)
                    {
                        text2 += item.itemOption[0].getOptionString();
                    }
                    mFont mFont3 = mFont.tahoma_7_blue;
                    if (item.compare < 0 && item.template.type != 5)
                    {
                        mFont3 = mFont.tahoma_7_red;
                    }
                    if (item.itemOption.Length > 1)
                    {
                        for (int l = 1; l < item.itemOption.Length; l++)
                        {
                            if (item.itemOption[l] != null && item.itemOption[l].optionTemplate.id != 102 && item.itemOption[l].optionTemplate.id != 107)
                            {
                                text2 = text2 + "," + item.itemOption[l].getOptionString();
                            }
                        }
                    }
                    mFont3.drawString(g, text2, num + 5, num2 + 11, mFont.LEFT);
                }
                SmallImage.drawSmallImage(g, item.template.iconID, num5 + num7 / 2, num6 + num8 / 2, 0, 3);
                if (item.itemOption != null)
                {
                    for (int m = 0; m < item.itemOption.Length; m++)
                    {
                        paintOptItem(g, item.itemOption[m].optionTemplate.id, item.itemOption[m].param, num5, num6, num7, num8);
                    }
                    for (int n = 0; n < item.itemOption.Length; n++)
                    {
                        paintOptSlotItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num5, num6, num7, num8);
                    }
                }
                if (item.quantity > 1)
                {
                    mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
                }
            }
            paintScrollArrow(g);
        }
    
        private void updateKeyMap()
        {
            if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
            {
                yMove -= 5;
                cmyMap = yMove - (yScroll + hScroll / 2);
                if (yMove < yScroll)
                {
                    yMove = yScroll;
                }
            }
            if (GameCanvas.keyHold[(!Main.isPC) ? 8 : 22])
            {
                yMove += 5;
                cmyMap = yMove - (yScroll + hScroll / 2);
                if (yMove > yScroll + 200)
                {
                    yMove = yScroll + 200;
                }
            }
            if (GameCanvas.keyHold[(!Main.isPC) ? 4 : 23])
            {
                xMove -= 5;
                cmxMap = xMove - wScroll / 2;
                if (xMove < 16)
                {
                    xMove = 16;
                }
            }
            if (GameCanvas.keyHold[(!Main.isPC) ? 6 : 24])
            {
                xMove += 5;
                cmxMap = xMove - wScroll / 2;
                if (xMove > 250)
                {
                    xMove = 250;
                }
            }
            if (GameCanvas.isPointerDown)
            {
                pointerIsDowning = true;
                if (!trans)
                {
                    pa1 = cmxMap;
                    pa2 = cmyMap;
                    trans = true;
                }
                cmxMap = pa1 + (GameCanvas.pxLast - GameCanvas.px);
                cmyMap = pa2 + (GameCanvas.pyLast - GameCanvas.py);
            }
            if (GameCanvas.isPointerJustRelease)
            {
                trans = false;
                GameCanvas.pxLast = GameCanvas.px;
                GameCanvas.pyLast = GameCanvas.py;
                pX = GameCanvas.pxLast + cmxMap;
                pY = GameCanvas.pyLast + cmyMap;
            }
            if (GameCanvas.isPointerClick)
            {
                pointerIsDowning = false;
            }
            if (cmxMap < 0)
            {
                cmxMap = 0;
            }
            if (cmxMap > cmxMapLim)
            {
                cmxMap = cmxMapLim;
            }
            if (cmyMap < 0)
            {
                cmyMap = 0;
            }
            if (cmyMap > cmyMapLim)
            {
                cmyMap = cmyMapLim;
            }
        }
    
        private void updateKeyCombine()
        {
            if (currentTabIndex == 0)
            {
                updateKeyScrollView();
                keyTouchCombine = -1;
                if (selected == vItemCombine.size() && GameCanvas.isPointerClick)
                {
                    GameCanvas.isPointerClick = false;
                    keyTouchCombine = 1;
                }
            }
            if (currentTabIndex == 1)
            {
                updateKeyScrollView();
            }
        }
    
        private void updateKeyQuest()
        {
            if (GameCanvas.keyHold[(!Main.isPC) ? 2 : 21])
            {
                cmyQuest -= 5;
            }
            if (GameCanvas.keyHold[(!Main.isPC) ? 8 : 22])
            {
                cmyQuest += 5;
            }
            if (cmyQuest < 0)
            {
                cmyQuest = 0;
            }
            int num = indexRowMax * 12 - (hScroll - 60);
            if (num < 0)
            {
                num = 0;
            }
            if (cmyQuest > num)
            {
                cmyQuest = num;
            }
            if (scroll != null)
            {
                if (!GameCanvas.isTouch)
                {
                    scroll.cmy = cmyQuest;
                }
                scroll.updateKey();
            }
            int num2 = xScroll + wScroll / 2 - 35;
            int num3 = ((GameCanvas.h <= 300) ? 15 : 20);
            int num4 = yScroll + hScroll - num3 - 15;
            int px = GameCanvas.px;
            int py = GameCanvas.py;
            keyTouchMapButton = -1;
            if (isPaintMap && !GameScr.gI().isMapDocNhan() && px >= num2 && px <= num2 + 70 && py >= num4 && py <= num4 + 30 && (scroll == null || !scroll.pointerIsDowning))
            {
                keyTouchMapButton = 1;
                if (GameCanvas.isPointerJustRelease)
                {
                    SoundMn.gI().buttonClick();
                    waitToPerform = 2;
                    GameCanvas.clearAllPointerEvent();
                }
            }
        }
    
        private void getCurrClanOtion()
        {
            isClanOption = false;
            if (type != 0 || mainTabName.Length != 5 || currentTabIndex != 3)
            {
                return;
            }
            isClanOption = false;
            if (selected == 0)
            {
                currClanOption = new int[clansOption.Length];
                for (int i = 0; i < currClanOption.Length; i++)
                {
                    currClanOption[i] = i;
                }
                if (!isViewMember)
                {
                    isClanOption = true;
                }
            }
            else if (selected != 1 && !isSearchClan && selected > 0)
            {
                currClanOption = new int[1];
                for (int j = 0; j < currClanOption.Length; j++)
                {
                    currClanOption[j] = j;
                }
                isClanOption = true;
            }
        }
    
        private void updateKeyClansOption()
        {
            if (currClanOption == null)
            {
                return;
            }
            if (GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23])
            {
                currMess = getCurrMessage();
                cSelected--;
                if (selected == 0 && cSelected < 0)
                {
                    cSelected = currClanOption.Length - 1;
                }
                if (selected > 1 && isMessage && currMess.option != null && cSelected < 0)
                {
                    cSelected = currMess.option.Length - 1;
                }
            }
            else if (GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24])
            {
                currMess = getCurrMessage();
                cSelected++;
                if (selected == 0 && cSelected > currClanOption.Length - 1)
                {
                    cSelected = 0;
                }
                if (selected > 1 && isMessage && currMess.option != null && cSelected > currMess.option.Length - 1)
                {
                    cSelected = 0;
                }
            }
        }
    
        private void updateKeyClans()
        {
            updateKeyScrollView();
            updateKeyClansOption();
        }
    
        private void checkOptionSelect()
        {
            try
            {
                if (type != 0 || currentTabIndex != 3 || mainTabName.Length != 5 || selected == -1)
                {
                    return;
                }
                int num = 0;
                if (selected == 0)
                {
                    num = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
                    cSelected = (GameCanvas.px - num) / TAB_W;
                }
                else
                {
                    currMess = getCurrMessage();
                    if (currMess != null && currMess.option != null)
                    {
                        num = xScroll + wScroll - 2 - currMess.option.Length * 40;
                        cSelected = (GameCanvas.px - num) / 40;
                    }
                }
                if (GameCanvas.px < num)
                {
                    cSelected = -1;
                }
            }
            catch (Exception ex)
            {
                Res.outz("Throw err " + ex.StackTrace);
            }
        }
    
        public void updateScroolMouse(int a)
        {
            bool flag = false;
            if (GameCanvas.pxMouse > wScroll)
            {
                return;
            }
            if (indexMouse == -1)
            {
                indexMouse = selected;
            }
            if (a > 0)
            {
                indexMouse -= a;
                flag = true;
            }
            else if (a < 0)
            {
                indexMouse += -a;
                flag = true;
            }
            if (indexMouse < 0)
            {
                indexMouse = 0;
            }
            if (flag)
            {
                cmtoY = indexMouse * 12;
                if (cmtoY > cmyLim)
                {
                    cmtoY = cmyLim;
                }
                if (cmtoY < 0)
                {
                    cmtoY = 0;
                }
            }
        }
    
        private void updateKeyScrollView()
        {
            if (currentListLength <= 0)
            {
                return;
            }
            bool flag = false;
            if (GameCanvas.keyPressed[(!Main.isPC) ? 2 : 21])
            {
                flag = true;
                if (isTabInven() && isnewInventory)
                {
                    if (selected > 0 && sellectInventory == 0)
                    {
                        selected--;
                    }
                }
                else
                {
                    selected--;
                    if (type == 24)
                    {
                        selected -= 2;
                        if (selected < 0)
                        {
                            selected = 0;
                        }
                    }
                    else if (selected < 0)
                    {
                        if (Equals(GameCanvas.panel) && typeShop == 2 && currentTabIndex <= 3 && maxPageShop[currentTabIndex] > 1)
                        {
                            InfoDlg.showWait();
                            if (currPageShop[currentTabIndex] <= 0)
                            {
                                Service.gI().kigui(4, -1, (sbyte)currentTabIndex, maxPageShop[currentTabIndex] - 1, -1);
                            }
                            else
                            {
                                Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] - 1, -1);
                            }
                            return;
                        }
                        selected = currentListLength - 1;
                        if (isClanOption)
                        {
                            selected = -1;
                        }
                        if (size_tab > 0)
                        {
                            selected = -1;
                        }
                    }
                    lastSelect[currentTabIndex] = selected;
                    cSelected = 0;
                    getCurrClanOtion();
                }
            }
            else if (GameCanvas.keyPressed[(!Main.isPC) ? 8 : 22])
            {
                flag = true;
                if (isTabInven() && isnewInventory)
                {
                    if (selected < 1 && sellectInventory == 0)
                    {
                        selected++;
                    }
                }
                else
                {
                    selected++;
                    if (type == 24)
                    {
                        selected += 2;
                        if (selected > currentListLength - 1)
                        {
                            selected = currentListLength - 1;
                        }
                    }
                    else if (selected > currentListLength - 1)
                    {
                        if (Equals(GameCanvas.panel) && typeShop == 2 && currentTabIndex <= 3 && maxPageShop[currentTabIndex] > 1)
                        {
                            InfoDlg.showWait();
                            if (currPageShop[currentTabIndex] >= maxPageShop[currentTabIndex] - 1)
                            {
                                Service.gI().kigui(4, -1, (sbyte)currentTabIndex, 0, -1);
                            }
                            else
                            {
                                Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] + 1, -1);
                            }
                            return;
                        }
                        selected = 0;
                    }
                    lastSelect[currentTabIndex] = selected;
                    cSelected = 0;
                    getCurrClanOtion();
                }
            }
            if (isnewInventory && GameCanvas.keyPressed[5] && itemInvenNew != null)
            {
                pointerDownTime = 0;
                waitToPerform = 2;
            }
            if (flag)
            {
                cmtoY = selected * ITEM_HEIGHT - hScroll / 2;
                if (cmtoY > cmyLim)
                {
                    cmtoY = cmyLim;
                }
                if (cmtoY < 0)
                {
                    cmtoY = 0;
                }
                cmy = cmtoY;
            }
            if (GameCanvas.isPointerDown)
            {
                justRelease = false;
                if (!pointerIsDowning && GameCanvas.isPointer(xScroll, yScroll, wScroll, hScroll))
                {
                    for (int i = 0; i < pointerDownLastX.Length; i++)
                    {
                        pointerDownLastX[0] = GameCanvas.py;
                    }
                    pointerDownFirstX = GameCanvas.py;
                    yBamDau = GameCanvas.py;
                    pointerIsDowning = true;
                    isDownWhenRunning = cmRun != 0;
                    cmRun = 0;
                }
                else if (pointerIsDowning)
                {
                    pointerDownTime++;
                    if (pointerDownTime > 5 && pointerDownFirstX == GameCanvas.py && !isDownWhenRunning)
                    {
                        pointerDownFirstX = -1000;
                        selected = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                        if (selected >= currentListLength)
                        {
                            selected = -1;
                        }
                        checkOptionSelect();
                    }
                    else
                    {
                        indexMouse = -1;
                    }
                    int num = GameCanvas.py - pointerDownLastX[0];
                    if (num != 0 && selected != -1)
                    {
                        selected = -1;
                        cSelected = -1;
                    }
                    for (int num2 = pointerDownLastX.Length - 1; num2 > 0; num2--)
                    {
                        pointerDownLastX[num2] = pointerDownLastX[num2 - 1];
                    }
                    pointerDownLastX[0] = GameCanvas.py;
                    cmtoY -= num;
                    if (cmtoY < 0)
                    {
                        cmtoY = 0;
                    }
                    if (cmtoY > cmyLim)
                    {
                        cmtoY = cmyLim;
                    }
                    if (cmy < 0 || cmy > cmyLim)
                    {
                        num /= 2;
                    }
                    cmy -= num;
                    if (cmy < -(GameCanvas.h / 3))
                    {
                        wantUpdateList = true;
                    }
                    else
                    {
                        wantUpdateList = false;
                    }
                    if (isnewInventory && isTabInven()
                            && GameCanvas.py >= yOTui())
                    {
                        // Cung phai co isTabInven(): yOTui() la toa do cua luoi o
                        // vuong, o bang khac no la mot con so khong co nghia gi, va
                        // chonOTaiDiem() thi doi o trong luoi ay.
                        //
                        // KHONG dung "return" o day.
                        //
                        // Ban truoc viet "if (py < yOTui()) return;" de bo qua
                        // dai tab. Nhung return thoat ca updateKeyScrollView, ke
                        // ca phan xu ly NHA NGON nam duoi — nen bam vao dai tab
                        // khong bao gio doi tab duoc. Gio doi thanh dieu kien.
                        chonOTaiDiem();
                    }
                }
            }
            if (!GameCanvas.isPointerJustRelease || !pointerIsDowning)
            {
                return;
            }
            justRelease = true;
            int i2 = GameCanvas.py - pointerDownLastX[0];
            GameCanvas.isPointerJustRelease = false;
            // Bam vao DAI TAB thi doi tab va thoat han.
            //
            // Khong thoat thi doan duoi van chay: no dat waitToPerform = 2 khi
            // dang co mot o duoc chon, nen bam vao tab lai mo menu vat pham cua
            // o cu — tab khong doi, ma lai bat ra mot menu khong ai goi.
            if (isnewInventory && isTabInven() && GameCanvas.py < yOTui())
            {
                setNewSelected(0, true);
                waitToPerform = -1;
                pointerIsDowning = false;
                pointerDownTime = 0;
                SoundMn.gI().panelClick();
                return;
            }
            if (Res.abs(i2) < 20 && Res.abs(GameCanvas.py - pointerDownFirstX) < 20 && !isDownWhenRunning)
            {
                cmRun = 0;
                cmtoY = cmy;
                pointerDownFirstX = -1000;
                selected = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                if (selected >= currentListLength)
                {
                    selected = -1;
                }
                checkOptionSelect();
                pointerDownTime = 0;
                waitToPerform = 10;
                if (isnewInventory)
                {
                    // Chon lai o NGAY LUC NHA NGON.
                    //
                    // Phan giu ngon chi chay o nhanh "else if (pointerIsDowning)",
                    // tuc phai co it nhat hai khung hinh giu ngon. Go nhanh mot
                    // cai thi an xuong va nha trong cung mot khung, nhanh do
                    // khong chay lan nao, va o dang chon van la o CU — bam vao do
                    // "doi khi" khong hien gi. Chon lai o day thi go nhanh hay
                    // cham cung an.
                    bool coO = chonOTaiDiem();

                    // KHONG suy ra selected tu ITEM_HEIGHT o luoi o vuong.
                    //
                    // Dong tren vua tinh selected = (py - yScroll) / 24 roi ep ve
                    // -1 khi vuot currentListLength. Luoi o vuong khong co "dong"
                    // 24 diem nao, va currentListLength cua no chi la 2, nen moi
                    // o nam duoi hang thu hai deu ra -1. Ham mo menu vat pham lai
                    // thoat ngay khi selected == -1 — vi the chi ao va quan (hai o
                    // tren cung) la xem duoc thong tin, con cai trang, van bay,
                    // deo lung, sach, chan menh, pet thi bam khong ra gi.
                    //
                    // Dat 1: khac -1 de menu mo, khac 0 de khong roi vao nhanh
                    // doi tab cua ham do.
                    selected = coO ? 1 : -1;
                    // So 2 la ma mo menu vat pham (cung ma luc bam nut chon).
                    // Ban cu de -1 nghia la khong lam gi — dung cho ban danh sach
                    // vi luc do isnewInventory luon false.
                    waitToPerform = (coO && itemInvenNew != null) ? 2 : -1;
                }
                SoundMn.gI().panelClick();
            }
            else if (isnewInventory && isTabInven() && pointerDownTime > 5)
            {
                // Nhanh GIU NGON cua LUOI O VUONG — chi o dung tab hanh trang.
                //
                // Thieu dieu kien isTabInven() thi nhanh nay chiem moi bang dang
                // DANH SACH, vi isnewInventory la mot truong mac dinh TRUE cho
                // moi doi tuong Panel. No goi chonOTaiDiem() — ham chi biet luoi
                // o vuong cua hanh trang — roi dat selected = 1 khi ham do tra
                // true va -1 khi tra false, vut bo `selected` ma vong giu ngon
                // vua tinh dung tu toa do dong.
                //
                // Do la goc cua ba loi cung mot lan: bam mot mon trong khung nang
                // cap NPC khong ra menu (selected thanh -1), capsule bay sai map
                // (selected thanh 1 nen luon di map thu hai), va mot vai map bam
                // khong an (nhung lan chonOTaiDiem tra false).
                //
                // Bam bang chuot thi gan nhu luc nao cung roi vao nhanh giu ngon,
                // khong phai nhanh "go nhanh" o tren.
                //
                // Vi sao: doan giu ngon o tren dat pointerDownFirstX = -1000 sau
                // nam khung hinh, nen phep so "lech duoi 20 diem" cua nhanh go
                // nhanh khong con dung, va no bi bo qua.
                //
                // Ban cu o day con doi them "selected != -1". Ma selected cua
                // luoi o vuong tinh theo dong 24 diem roi bi ep ve -1 tu hang thu
                // ba tro xuong (currentListLength cua luoi chi bang 2). Vi the
                // chi HANG TREN CUNG cua hai cot mo duoc thong tin — dung hai o
                // ao va cai trang — con moi o duoi bam cham la khong ra gi.
                pointerDownTime = 0;
                bool coOGiu = chonOTaiDiem();
                selected = coOGiu ? 1 : -1;
                waitToPerform = (coOGiu && itemInvenNew != null) ? 2 : -1;
            }
            else if (pointerDownTime > 5 && !isDownWhenRunning
                    && Res.abs(GameCanvas.py - yBamDau) < 20)
            {
                // Nhanh GIU NGON cho cac bang dang DANH SACH (nang cap, ghep do,
                // cua hang, ban be...).
                //
                // Bam bang chuot thi gan nhu luon roi vao day chu khong vao nhanh
                // "go nhanh" o tren: vong giu ngon dat pointerDownFirstX = -1000,
                // nen phep so "lech duoi 20 diem" cua nhanh ay khong con dung.
                //
                // Tinh LAI `selected` tu toa do nha ngon. Ban cu chi doi
                // "selected != -1", ma chinh vong giu ngon xoa selected ve -1 ngay
                // khi ngon nhich mot diem — nen bam vao mot dong trong bang NPC
                // thuong khong ra menu nao, dung nhu loi "khong an vao de lay do
                // ra duoc".
                pointerDownTime = 0;
                selected = (cmtoY + GameCanvas.py - yScroll) / ITEM_HEIGHT;
                if (selected >= currentListLength)
                {
                    selected = -1;
                }
                checkOptionSelect();
                waitToPerform = (selected != -1) ? 1 : -1;
            }
            else if (selected == -1 && !isDownWhenRunning)
            {
                if (cmy < 0)
                {
                    cmtoY = 0;
                }
                else if (cmy > cmyLim)
                {
                    cmtoY = cmyLim;
                }
                else
                {
                    int num5 = GameCanvas.py - pointerDownLastX[0] + (pointerDownLastX[0] - pointerDownLastX[1]) + (pointerDownLastX[1] - pointerDownLastX[2]);
                    num5 = ((num5 > 10) ? 10 : ((num5 < -10) ? (-10) : 0));
                    cmRun = -num5 * 100;
                }
            }
            int num6 = 0;
            if ((isTabInven() || type == 13) && GameCanvas.py < yScroll + 21)
            {
                selected = 0;
    
                updateKeyInvenTab();
            }
            pointerIsDowning = false;
            pointerDownTime = 0;
            GameCanvas.isPointerJustRelease = false;
        }
    
        public string subArray(string[] str)
        {
            return null;
        }
    
        private void updateKeyInTabBar()
        {
            if ((scroll != null && scroll.pointerIsDowning) || pointerIsDowning)
            {
                return;
            }
            int num = currentTabIndex;
            if (isTabInven() && isnewInventory)
            {
                if (selected == -1)
                {
                    if (GameCanvas.keyPressed[6])
                    {
                        currentTabIndex++;
                        if (currentTabIndex >= currentTabName.Length)
                        {
                            if (GameCanvas.panel2 != null)
                            {
                                currentTabIndex = currentTabName.Length - 1;
                                GameCanvas.isFocusPanel2 = true;
                            }
                            else
                            {
                                currentTabIndex = 0;
                            }
                        }
                        selected = lastSelect[currentTabIndex];
                        lastTabIndex[type] = currentTabIndex;
                    }
                    if (GameCanvas.keyPressed[4])
                    {
                        currentTabIndex--;
                        if (currentTabIndex < 0)
                        {
                            currentTabIndex = currentTabName.Length - 1;
                        }
                        if (GameCanvas.isFocusPanel2)
                        {
                            GameCanvas.isFocusPanel2 = false;
                        }
                        selected = lastSelect[currentTabIndex];
                        lastTabIndex[type] = currentTabIndex;
                    }
                }
                else if (selected > 0)
                {
                    if (GameCanvas.keyPressed[8])
                    {
                        if (newSelected == 0)
                        {
                            sellectInventory++;
                        }
                        else
                        {
                            sellectInventory += 5;
                        }
                    }
                    else if (GameCanvas.keyPressed[2])
                    {
                        if (newSelected == 0)
                        {
                            sellectInventory--;
                        }
                        else
                        {
                            sellectInventory -= 5;
                        }
                    }
                    else if (GameCanvas.keyPressed[4])
                    {
                        if (newSelected == 0)
                        {
                            sellectInventory -= 5;
                        }
                        else
                        {
                            sellectInventory--;
                        }
                    }
                    else if (GameCanvas.keyPressed[6])
                    {
                        if (newSelected == 0)
                        {
                            sellectInventory += 5;
                        }
                        else
                        {
                            sellectInventory++;
                        }
                    }
                }
                if (sellectInventory < 0)
                {
                }
                if (sellectInventory == nTableItem)
                {
                    sellectInventory = 0;
                }
            }
            else if (!IsTabOption())
            {
                if (GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24])
                {
                    if (isTabInven())
                    {
                        if (selected >= 0)
                        {
                            updateKeyInvenTab();
                        }
                        else
                        {
                            currentTabIndex++;
                            if (currentTabIndex >= currentTabName.Length)
                            {
                                if (GameCanvas.panel2 != null)
                                {
                                    currentTabIndex = currentTabName.Length - 1;
                                    GameCanvas.isFocusPanel2 = true;
                                }
                                else
                                {
                                    currentTabIndex = 0;
                                }
                            }
                            selected = lastSelect[currentTabIndex];
                            lastTabIndex[type] = currentTabIndex;
                        }
                    }
                    else
                    {
                        currentTabIndex++;
                        if (currentTabIndex >= currentTabName.Length)
                        {
                            if (GameCanvas.panel2 != null)
                            {
                                currentTabIndex = currentTabName.Length - 1;
                                GameCanvas.isFocusPanel2 = true;
                            }
                            else
                            {
                                currentTabIndex = 0;
                            }
                        }
                        selected = lastSelect[currentTabIndex];
                        lastTabIndex[type] = currentTabIndex;
                    }
                }
                if (GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23])
                {
                    currentTabIndex--;
                    if (currentTabIndex < 0)
                    {
                        currentTabIndex = currentTabName.Length - 1;
                    }
                    if (GameCanvas.isFocusPanel2)
                    {
                        GameCanvas.isFocusPanel2 = false;
                    }
                    selected = lastSelect[currentTabIndex];
                    lastTabIndex[type] = currentTabIndex;
                }
            }
            keyTouchTab = -1;
            for (int i = 0; i < currentTabName.Length; i++)
            {
                if (!GameCanvas.isPointer(startTabPos + i * TAB_W, 52, TAB_W - 1, 25))
                {
                    continue;
                }
                keyTouchTab = i;
                if (GameCanvas.isPointerJustRelease)
                {
                    currentTabIndex = i;
                    lastTabIndex[type] = i;
                    GameCanvas.isPointerJustRelease = false;
                    selected = lastSelect[currentTabIndex];
                    if (num == currentTabIndex && cmRun == 0)
                    {
                        cmtoY = 0;
                        selected = (GameCanvas.isTouch ? (-1) : 0);
                    }
                    break;
                }
            }
            if (num == currentTabIndex)
            {
                return;
            }
            size_tab = 0;
            SoundMn.gI().panelClick();
            switch (type)
            {
                case 21:
                    if (currentTabIndex == 0)
                    {
                        setTabPetInventory();
                    }
                    if (currentTabIndex == 1)
                    {
                        setTabPetStatus();
                    }
                    if (currentTabIndex == 2)
                    {
                        setTabInventory(true);
                    }
                    break;
                case 0:
                    if (currentTabIndex == 0)
                    {
                        setTabTask();
                    }
                    if (currentTabIndex == 1)
                    {
                        setTabInventory(true);
                    }
                    if (currentTabIndex == 2)
                    {
                        setTabSkill();
                    }
                    if (currentTabIndex == 3)
                    {
                        if (mainTabName.Length > 4)
                        {
                            setTabClans();
                        }
                        else
                        {
                            setTabTool();
                        }
                    }
                    if (currentTabIndex == 4)
                    {
                        setTabTool();
                    }
                    break;
                case 2:
                    if (currentTabIndex == 0)
                    {
                        setTabBox();
                    }
                    if (currentTabIndex == 1)
                    {
                        setTabInventory(true);
                    }
                    break;
                case 3:
                    setTabZone();
                    break;
                case 1:
                    setTabShop();
                    break;
                case 25:
                    setTabSpeacialSkill();
                    break;
                case 12:
                    if (currentTabIndex == 0)
                    {
                        setTabCombine();
                    }
                    if (currentTabIndex == 1)
                    {
                        setTabInventory(true);
                    }
                    break;
                case 13:
                    if (currentTabIndex == 0)
                    {
                        if (Equals(GameCanvas.panel))
                        {
                            setTabInventory(true);
                        }
                        else if (Equals(GameCanvas.panel2))
                        {
                            setTabGiaoDich(false);
                        }
                    }
                    if (currentTabIndex == 1)
                    {
                        setTabGiaoDich(true);
                    }
                    if (currentTabIndex == 2)
                    {
                        setTabGiaoDich(false);
                    }
                    break;
            }
            selected = lastSelect[currentTabIndex];
        }
    
        private void setTabPetStatus()
        {
            currentListLength = strStatus.Length;
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
        }
    
        private void setTabPetSkill()
        {
        }
    
        private void setTabTool()
        {
            SoundMn.gI().getSoundOption();
            currentListLength = strTool.Length;
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
        }
    
        public void initTabClans()
        {
            if (isSearchClan)
            {
                currentListLength = ((clans != null) ? (clans.Length + 2) : 2);
                clanInfo = mResources.clan_list;
            }
            else if (isViewMember)
            {
                clanReport = string.Empty;
                currentListLength = ((member != null) ? member.size() : myMember.size()) + 2;
                clanInfo = mResources.member + " " + ((currClan == null) ? Char.myCharz().clan.name : currClan.name);
            }
            else if (isMessage)
            {
                currentListLength = ClanMessage.vMessage.size() + 2;
                clanInfo = mResources.msg;
                clanReport = string.Empty;
            }
            if (Char.myCharz().clan == null)
            {
                clansOption = new string[2][]
                {
                    mResources.findClan,
                    mResources.createClan
                };
            }
            else if (!isViewMember)
            {
                if (myMember.size() > 1)
                {
                    clansOption = new string[3][]
                    {
                        mResources.chatClan,
                        mResources.request_pea2,
                        mResources.memberr
                    };
                }
                else
                {
                    clansOption = new string[1][] { mResources.memberr };
                }
            }
            else if (Char.myCharz().role > 0)
            {
                clansOption = new string[2][]
                {
                    mResources.msgg,
                    mResources.leaveClan
                };
            }
            else if (myMember.size() > 1)
            {
                clansOption = new string[4][]
                {
                    mResources.msgg,
                    mResources.leaveClan,
                    mResources.khau_hieuu,
                    mResources.bieu_tuongg
                };
            }
            else
            {
                clansOption = new string[3][]
                {
                    mResources.msgg,
                    mResources.khau_hieuu,
                    mResources.bieu_tuongg
                };
            }
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
        }
    
        public void setTabClans()
        {
            GameScr.isNewClanMessage = false;
            ITEM_HEIGHT = 24;
            if (lastSelect != null && lastSelect[3] == 0)
            {
                lastSelect[3] = -1;
            }
            currentListLength = 2;
            if (Char.myCharz().clan != null)
            {
                isMessage = true;
                isViewMember = false;
                isSearchClan = false;
            }
            else
            {
                isMessage = false;
                isViewMember = false;
                isSearchClan = true;
            }
            if (Char.myCharz().clan != null)
            {
                currentListLength = ClanMessage.vMessage.size() + 2;
            }
            initTabClans();
            cSelected = -1;
            if (chatTField == null)
            {
                chatTField = new ChatTextField();
                chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                chatTField.initChatTextField();
                chatTField.parentScreen = GameCanvas.panel;
            }
            if (Char.myCharz().clan == null)
            {
                clanReport = mResources.findingClan;
                Service.gI().searchClan(string.Empty);
            }
            selected = lastSelect[currentTabIndex];
            if (GameCanvas.isTouch)
            {
                selected = -1;
            }
        }
    
        public void initLogMessage()
        {
            currentListLength = logChat.size() + 1;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            cmx = (cmtoX = 0);
        }
    
        private void setTabMessage()
        {
            ITEM_HEIGHT = 24;
            initLogMessage();
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        public void setTabShop()
        {
            ITEM_HEIGHT = 24;
            if (currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null && typeShop != 2)
            {
                currentListLength = checkCurrentListLength(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length);
            }
            else
            {
                currentListLength = Char.myCharz().arrItemShop[currentTabIndex].Length;
            }
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        private void setTabSkill()
        {
            ITEM_HEIGHT = 30;
            currentListLength = Char.myCharz().nClass.skillTemplates.Length + 6;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = cmyLim;
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        private void setTabMapTrans()
        {
            ITEM_HEIGHT = 24;
            currentListLength = mapNames.Length;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            cmy = (cmtoY = 0);
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        private void setTabZone()
        {
            ITEM_HEIGHT = 24;
            currentListLength = GameScr.gI().zones.Length;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            cmy = (cmtoY = 0);
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        private void setTabBox()
        {
            currentListLength = checkCurrentListLength(Char.myCharz().arrItemBox.Length);
            ITEM_HEIGHT = 24;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 9;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        private void setTabPetInventory()
        {
            ITEM_HEIGHT = 30;
            Item[] arrItemBody = Char.myPetz().arrItemBody;
            Skill[] arrPetSkill = Char.myPetz().arrPetSkill;
            currentListLength = arrItemBody.Length + arrPetSkill.Length;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = 0);
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        private void setTabInventory(bool resetSelect)
        {
            if (isnewInventory)
            {
                // KHONG dat lai newSelected o day.
                //
                // Ban cu ghi "newSelected = 0" ngay truoc khi return, ma ham nay
                // duoc setNewSelected goi NGAY SAU khi no dat newSelected = tab
                // vua bam. Ket qua: bam tab nao cung bat lai ve tab dau — nhin
                // nhu "bam tab khong an".
                ITEM_HEIGHT = 24;
                size_tab = (sbyte) TEN_TAB_TUI.Length;

                // Tinh cmyLim THANG tu chieu cao luoi.
                //
                // Ban cu de nguyen cong thuc danh sach
                // (currentListLength * ITEM_HEIGHT - hScroll) va con doat
                // currentListLength = 3, nen cmyLim ra so am, bi ep ve 0 va tab
                // "Hanh trang" khong cuon duoc mot diem nao.
                cmyLim = gioiHanCuonTui();
                currentListLength = cmyLim / ITEM_HEIGHT + 2;
                if (cmy > cmyLim)
                {
                    cmy = (cmtoY = cmyLim);
                }
                if (cmy < 0)
                {
                    cmy = (cmtoY = 0);
                }
                if (resetSelect)
                {
                    sellectInventory = 0;
                    itemInvenNew = oDangChon(0);
                }
                selected = -1;
                return;
            }
            currentListLength = checkCurrentListLength(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length);
            ITEM_HEIGHT = 24;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = 0);
            }
            if (resetSelect)
            {
                selected = (GameCanvas.isTouch ? (-1) : 0);
            }
        }
    
        private void setTabMap()
        {
            if (!isPaintMap)
            {
                return;
            }
            if (TileMap.lastPlanetId != TileMap.planetID)
            {
                Res.outz("LOAD TAM HINH");
                imgMap = GameCanvas.loadImageRMS("/img/map" + TileMap.planetID + ".png");
                TileMap.lastPlanetId = TileMap.planetID;
            }
            cmxMap = getXMap() - wScroll / 2;
            cmyMap = getYMap() + yScroll - (yScroll + hScroll / 2);
            pa1 = cmxMap;
            pa2 = cmyMap;
            cmxMapLim = 250 - wScroll;
            cmyMapLim = 220 - hScroll;
            if (cmxMapLim < 0)
            {
                cmxMapLim = 0;
            }
            if (cmyMapLim < 0)
            {
                cmyMapLim = 0;
            }
            for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
            {
                if (TileMap.mapID == mapId[TileMap.planetID][i])
                {
                    xMove = mapX[TileMap.planetID][i] + xScroll;
                    yMove = mapY[TileMap.planetID][i] + yScroll + 5;
                    break;
                }
            }
            xMap = getXMap() + xScroll;
            yMap = getYMap() + yScroll;
            xMapTask = getXMapTask() + xScroll;
            yMapTask = getYMapTask() + yScroll;
            Resources.UnloadUnusedAssets();
            GC.Collect();
        }
    
        private void setTabTask()
        {
            cmyQuest = 0;
        }
    
        public void moveCamera()
        {
            if (timeShow > 0)
            {
                timeShow--;
            }
            if (justRelease && Equals(GameCanvas.panel) && typeShop == 2 && maxPageShop[currentTabIndex] > 1)
            {
                if (cmy < -50)
                {
                    InfoDlg.showWait();
                    justRelease = false;
                    if (currPageShop[currentTabIndex] <= 0)
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, maxPageShop[currentTabIndex] - 1, -1);
                    }
                    else
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] - 1, -1);
                    }
                }
                else if (cmy > cmyLim + 50)
                {
                    justRelease = false;
                    InfoDlg.showWait();
                    if (currPageShop[currentTabIndex] >= maxPageShop[currentTabIndex] - 1)
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, 0, -1);
                    }
                    else
                    {
                        Service.gI().kigui(4, -1, (sbyte)currentTabIndex, currPageShop[currentTabIndex] + 1, -1);
                    }
                }
            }
            if (cmx != cmtoX && !pointerIsDowning)
            {
                cmvx = cmtoX - cmx << 2;
                cmdx += cmvx;
                cmx += cmdx >> 3;
                cmdx &= 15;
            }
            if (Math.abs(cmtoX - cmx) < 10)
            {
                cmx = cmtoX;
            }
            if (isClose)
            {
                isClose = false;
                cmtoX = wScroll;
            }
            if (cmtoX >= wScroll - 10 && cmx >= wScroll - 10 && position == 0)
            {
                isShow = false;
                cleanCombine();
                if (isChangeZone)
                {
                    isChangeZone = false;
                    // Da gui o doFireZone thi thoi: gui hai lan la may chu doi map
                    // hai lan, hoac lan hai roi vao nhanh khong con doi tuong nao
                    // de doc.
                    if (daGuiChonMap)
                    {
                        daGuiChonMap = false;
                    }
                    else if (Char.myCharz().cHP > 0
                            && Char.myCharz().statusMe != 14)
                    {
                        InfoDlg.showWait();
                        if (type == 3)
                        {
                            Service.gI().requestChangeZone(selected, -1);
                        }
                        else if (type == 14)
                        {
                            Service.gI().requestMapSelect(selected);
                        }
                    }
                }
                if (isSelectPlayerMenu)
                {
                    isSelectPlayerMenu = false;
                    int num = vPlayerMenu.size() - vPlayerMenu_id.size();
                    if (Char.myCharz().charFocus != null)
                    {
                        if (selected - num < 0)
                        {
                            Char.myCharz().charFocus.menuSelect = selected;
                        }
                        else
                        {
                            Char.myCharz().charFocus.menuSelect = short.Parse((string)vPlayerMenu_id.elementAt(selected - num));
                        }
                    }
                    Command command = (Command)vPlayerMenu.elementAt(selected);
                    command.performAction();
                }
                vPlayerMenu.removeAllElements();
                vPlayerMenu_id.removeAllElements();
                charMenu = null;
            }
            if (cmRun != 0 && !pointerIsDowning)
            {
                cmtoY += cmRun / 100;
                if (cmtoY < 0)
                {
                    cmtoY = 0;
                }
                else if (cmtoY > cmyLim)
                {
                    cmtoY = cmyLim;
                }
                else
                {
                    cmy = cmtoY;
                }
                cmRun = cmRun * 9 / 10;
                if (cmRun < 100 && cmRun > -100)
                {
                    cmRun = 0;
                }
            }
            if (cmy != cmtoY && !pointerIsDowning)
            {
                cmvy = cmtoY - cmy << 2;
                cmdy += cmvy;
                cmy += cmdy >> 4;
                cmdy &= 15;
            }
            cmyLast[currentTabIndex] = cmy;
        }
    
        public void paintDetail(mGraphics g)
        {
            if (cp == null || cp.says == null)
            {
                return;
            }
            cp.paint(g);
            int num = cp.cx + 13;
            int num2 = cp.cy + 11;
            if (type == 15)
            {
                num += 5;
                num2 += 26;
            }
            if (type == 0 && currentTabIndex == 3)
            {
                if (isSearchClan)
                {
                    num -= 5;
                }
                else if (partID != null || charInfo != null)
                {
                    num = cp.cx + 21;
                    num2 = cp.cy + 40;
                }
            }
            if (partID != null)
            {
                Part part = GameScr.parts[partID[0]];
                Part part2 = GameScr.parts[partID[1]];
                Part part3 = GameScr.parts[partID[2]];
                SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + Char.CharInfo[0][0][1] + part.pi[Char.CharInfo[0][0][0]].dx, num2 - Char.CharInfo[0][0][2] + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                SmallImage.drawSmallImage(g, part2.pi[Char.CharInfo[0][1][0]].id, num + Char.CharInfo[0][1][1] + part2.pi[Char.CharInfo[0][1][0]].dx, num2 - Char.CharInfo[0][1][2] + part2.pi[Char.CharInfo[0][1][0]].dy, 0, 0);
                SmallImage.drawSmallImage(g, part3.pi[Char.CharInfo[0][2][0]].id, num + Char.CharInfo[0][2][1] + part3.pi[Char.CharInfo[0][2][0]].dx, num2 - Char.CharInfo[0][2][2] + part3.pi[Char.CharInfo[0][2][0]].dy, 0, 0);
            }
            else if (charInfo != null)
            {
                charInfo.paintCharBody(g, num + 5, num2 + 25, 1, 0, true);
            }
            else if (idIcon != -1)
            {
                SmallImage.drawSmallImage(g, idIcon, cp.cx + 8, cp.cy + 2, 0, mGraphics.TOP | mGraphics.LEFT);
            }
            if (currItem != null && currItem.template.type != 5)
            {
                if (currItem.compare > 0)
                {
                    g.drawImage(imgUp, num - 7, num2 + 13, 3);
                    mFont.tahoma_7b_green.drawString(g, Res.abs(currItem.compare) + string.Empty, num + 1, num2 + 8, 0);
                }
                else if (currItem.compare < 0 && currItem.compare != -1)
                {
                    g.drawImage(imgDown, num - 7, num2 + 13, 3);
                    mFont.tahoma_7b_red.drawString(g, Res.abs(currItem.compare) + string.Empty, num + 1, num2 + 8, 0);
                }
            }
        }
    
        public void paintTop(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0)
            {
                return;
            }
            int num = (cmy + hScroll) / 24 + 1;
            if (num < hScroll / 24 + 1)
            {
                num = hScroll / 24 + 1;
            }
            if (num > currentListLength)
            {
                num = currentListLength;
            }
            int num2 = cmy / 24;
            if (num2 >= num)
            {
                num2 = num - 1;
            }
            if (num2 < 0)
            {
                num2 = 0;
            }
            for (int i = num2; i < num; i++)
            {
                int num3 = xScroll;
                int num4 = yScroll + i * ITEM_HEIGHT;
                int num5 = 24;
                int h = ITEM_HEIGHT - 1;
                int num6 = xScroll + num5;
                int num7 = yScroll + i * ITEM_HEIGHT;
                int num8 = wScroll - num5;
                int num9 = ITEM_HEIGHT - 1;
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num6, num7, num8, num9);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num3, num4, num5, h);
                TopInfo topInfo = (TopInfo)vTop.elementAt(i);
                if (topInfo.headICON != -1)
                {
                    SmallImage.drawSmallImage(g, topInfo.headICON, num3, num4, 0, 0);
                }
                else
                {
                    Part part = GameScr.parts[topInfo.headID];
                    SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num3 + part.pi[Char.CharInfo[0][0][0]].dx, num4 + num9 - 1, 0, mGraphics.BOTTOM | mGraphics.LEFT);
                }
                g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                if (topInfo.pId != Char.myCharz().charID)
                {
                    mFont.tahoma_7b_green.drawString(g, topInfo.name, num6 + 5, num7, 0);
                }
                else
                {
                    mFont.tahoma_7b_red.drawString(g, topInfo.name, num6 + 5, num7, 0);
                }
                mFont.tahoma_7_blue.drawString(g, topInfo.info, num6 + num8 - 5, num7 + 11, 1);
                mFont.tahoma_7_green2.drawString(g, mResources.rank + ": " + topInfo.rank + string.Empty, num6 + 5, num7 + 11, 0);
            }
            paintScrollArrow(g);
        }
    
        public void paint(mGraphics g)
        {
            g.translate(-g.getTranslateX(), -g.getTranslateY() + mGraphics.addYWhenOpenKeyBoard);
            g.translate(-cmx, 0);
            g.translate(X, Y);
            if (GameCanvas.panel.combineSuccess != -1)
            {
                if (Equals(GameCanvas.panel))
                {
                    paintCombineEff(g);
                }
                return;
            }
            veKhungPanel(g);
            try
            {
                paintTopInfo(g);
            }
            catch (Exception)
            {
            }
            paintBottomMoneyInfo(g);
            paintTab(g);
            switch (type)
            {
                case 9:
                    paintArchivement(g);
                    break;
                case 21:
                    if (currentTabIndex == 0)
                    {
                        paintPetInventory(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        paintPetStatus(g);
                    }
                    if (currentTabIndex == 2)
                    {
                        paintInventory(g);
                    }
                    break;
                case 24:
                    paintGameSubInfo(g);
                    break;
                case 23:
                    paintGameInfo(g);
                    break;
                case 0:
                    if (currentTabIndex == 0)
                    {
                        paintTask(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        paintInventory(g);
                    }
                    if (currentTabIndex == 2)
                    {
                        paintSkill(g);
                    }
                    if (currentTabIndex == 3)
                    {
                        if (mainTabName.Length == 4)
                        {
                            paintTools(g);
                        }
                        else
                        {
                            paintClans(g);
                        }
                    }
                    if (currentTabIndex == 4)
                    {
                        paintTools(g);
                    }
                    break;
                case 2:
                    if (currentTabIndex == 0)
                    {
                        paintBox(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        paintInventory(g);
                    }
                    break;
                case 3:
                    paintZone(g);
                    break;
                case 1:
                    paintShop(g);
                    break;
                case 25:
                    paintSpeacialSkill(g);
                    break;
                case 4:
                    paintMap(g);
                    break;
                case 7:
                    paintInventory(g);
                    break;
                case 17:
                    paintShop(g);
                    break;
                case 8:
                    paintLogChat(g);
                    break;
                case 10:
                    paintPlayerMenu(g);
                    break;
                case 11:
                    paintFriend(g);
                    break;
                case 16:
                    paintEnemy(g);
                    break;
                case 15:
                    paintTop(g);
                    break;
                case 12:
                    if (currentTabIndex == 0)
                    {
                        paintCombine(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        paintInventory(g);
                    }
                    break;
                case 13:
                    if (currentTabIndex == 0)
                    {
                        if (Equals(GameCanvas.panel))
                        {
                            paintInventory(g);
                        }
                        else
                        {
                            paintGiaoDich(g, false);
                        }
                    }
                    if (currentTabIndex == 1)
                    {
                        paintGiaoDich(g, true);
                    }
                    if (currentTabIndex == 2)
                    {
                        paintGiaoDich(g, false);
                    }
                    break;
                case 14:
                    paintMapTrans(g);
                    break;
                case 18:
                    paintFlagChange(g);
                    break;
                case 19:
                    paintOption(g);
                    break;
                case 20:
                    paintAccount(g);
                    break;
                case 22:
                    paintAuto(g);
                    break;
            }
            GameScr.resetTranslate(g);
            paintDetail(g);
            if (cmx == cmtoX && !GameCanvas.menu.showMenu)
            {
                cmdClose.paint(g);
            }
            if (tabIcon != null && tabIcon.isShow)
            {
                tabIcon.paint(g);
            }
            g.translate(-g.getTranslateX(), -g.getTranslateY());
            g.translate(X, Y);
            g.translate(-cmx, 0);
        }
    
        private void paintShop(mGraphics g)
        {
            try
            {
                if (type == 1 && currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null && typeShop != 2)
                {
                    paintInventory(g);
                    return;
                }
                g.setColor(16711680);
                g.setClip(xScroll, yScroll, wScroll, hScroll);
                if (typeShop == 2 && Equals(GameCanvas.panel))
                {
                    if (currentTabIndex <= 3 && GameCanvas.isTouch)
                    {
                        if (cmy < -50)
                        {
                            GameCanvas.paintShukiren(xScroll + wScroll / 2, yScroll + 30, g);
                        }
                        else if (cmy < 0)
                        {
                            mFont.tahoma_7_grey.drawString(g, mResources.getDown, xScroll + wScroll / 2, yScroll + 15, 2);
                        }
                        else if (cmyLim >= 0)
                        {
                            if (cmy > cmyLim + 50)
                            {
                                GameCanvas.paintShukiren(xScroll + wScroll / 2, yScroll + hScroll - 30, g);
                            }
                            else if (cmy > cmyLim)
                            {
                                mFont.tahoma_7_grey.drawString(g, mResources.getUp, xScroll + wScroll / 2, yScroll + hScroll - 25, 2);
                            }
                        }
                    }
                    if (Char.myCharz().arrItemShop[currentTabIndex].Length == 0 && type != 17)
                    {
                        mFont.tahoma_7_grey.drawString(g, mResources.notYetSell, xScroll + wScroll / 2, yScroll + hScroll / 2 - 10, 2);
                        return;
                    }
                }
                g.translate(0, -cmy);
                Item[] array = Char.myCharz().arrItemShop[currentTabIndex];
                if (typeShop == 2 && (currentTabIndex == 4 || type == 17))
                {
                    array = Char.myCharz().arrItemShop[4];
                    if (array.Length == 0)
                    {
                        mFont.tahoma_7_grey.drawString(g, mResources.notYetSell, xScroll + wScroll / 2, yScroll + hScroll / 2 - 10, 2);
                        return;
                    }
                }
                int num = array.Length;
                for (int i = 0; i < num; i++)
                {
                    int num2 = xScroll + 26;
                    int num3 = yScroll + i * ITEM_HEIGHT;
                    int num4 = wScroll - 26;
                    int h = ITEM_HEIGHT - 1;
                    int num5 = xScroll;
                    int num6 = yScroll + i * ITEM_HEIGHT;
                    int num7 = 24;
                    int num8 = ITEM_HEIGHT - 1;
                    if (num3 - cmy > yScroll + hScroll || num3 - cmy < yScroll - ITEM_HEIGHT)
                    {
                        continue;
                    }
                    // Moi dong la mot the bo goc, khong phai hai o vuong roi.
                    //
                    // Ban truoc ve hai hinh chu nhat sat nhau — o anh mot mau, phan
                    // chu mot mau — nen danh sach nhin nhu bang ke chu khong phai
                    // danh sach vat pham, va dong dang chon chi sang hon mot chut,
                    // rat kho nhan ra dang dung o dau.
                    //
                    // Mau lay theo he mau cua man tui moi: nen kem, vien nau, dong
                    // dang chon nen cam va vien dam.
                    bool dangChon = (i == selected);
                    int xThe = num5;
                    int rongThe = num2 + num4 - num5;
                    g.setColor(0x8B623A, dangChon ? 0.95f : 0.5f);
                    g.fillRect(xThe, num6, rongThe, num8, 5);
                    g.setColor(dangChon ? 0xF0A164
                            : ((i % 2 == 0) ? 0xFDF0DC : 0xF6E7CC), 1f);
                    g.fillRect(xThe + 1, num6 + 1, rongThe - 2, num8 - 2, 4);
                    // O anh dam hon mot chut, de tach khoi phan chu.
                    g.setColor(0xF0DCBE, dangChon ? 0.95f : 0.9f);
                    g.fillRect(xThe + 1, num6 + 1, num7 - 1, num8 - 2, 4);
                    g.setColor(0x8B623A, 0.45f);
                    g.fillRect(xThe + num7, num6 + 2, 1, num8 - 4);
                    Item item = array[i];
                    if (item != null)
                    {
                        string text = string.Empty;
                        mFont mFont2 = mFont.tahoma_7_green2;
                        if (item.isMe != 0 && typeShop == 2 && currentTabIndex <= 3 && !Equals(GameCanvas.panel2) && item.template.name.Length < 20)
                        {
                            mFont2 = mFont.tahoma_7b_green;
                        }
                        if (item.itemOption != null)
                        {
                            for (int j = 0; j < item.itemOption.Length; j++)
                            {
                                if (item.itemOption[j].optionTemplate.id == 72)
                                {
                                    text = " [+" + item.itemOption[j].param + "]";
                                }
                                if (item.itemOption[j].optionTemplate.id == 107)
                                {
                                    paintItemStar(g, item.itemOption[j].param.ToString(), num2 + 125, num3 + 1);
                                }
                                if (item.itemOption[j].optionTemplate.id == 41)
                                {
                                    if (item.itemOption[j].param == 1)
                                    {
                                        mFont2 = GetFont(0);
                                    }
                                    else if (item.itemOption[j].param == 2)
                                    {
                                        mFont2 = GetFont(2);
                                    }
                                    else if (item.itemOption[j].param == 3)
                                    {
                                        mFont2 = GetFont(8);
                                    }
                                    else if (item.itemOption[j].param == 4)
                                    {
                                        mFont2 = GetFont(7);
                                    }
                                }
                            }
                        }
                        mFont2.drawString(g, item.template.name + text, num2 + 5, num3 + 1, 0);
                        string text2 = string.Empty;
                        if (item.itemOption != null && item.itemOption.Length >= 1)
                        {
                            if (item.itemOption[0] != null && item.itemOption[0].optionTemplate.id != 102 && item.itemOption[0].optionTemplate.id != 107)
                            {
                                text2 += item.itemOption[0].getOptionString();
                            }
                            mFont mFont3 = mFont.tahoma_7_blue;
                            if (item.compare < 0 && item.template.type != 5)
                            {
                                mFont3 = mFont.tahoma_7_red;
                            }
                            if (typeShop == 2 && item.itemOption.Length > 1 && item.buyType != -1)
                            {
                                text2 += string.Empty;
                            }
                            if (typeShop != 2 || (typeShop == 2 && item.buyType <= 1))
                            {
                                mFont3.drawString(g, text2, num2 + 5, num3 + 11, 0);
                            }
                        }
                        if (item.buySpec > 0)
                        {
                            SmallImage.drawSmallImage(g, item.iconSpec, num2 + num4 - 7, num3 + 9, 0, 3);
                            mFont.tahoma_7b_blue.drawString(g, Res.formatNumber(item.buySpec), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                        }
                        if (item.buyCoin != 0 || item.buyGold != 0)
                        {
                            if (typeShop != 2 && item.powerRequire == 0)
                            {
                                if (item.buyCoin > 0 && item.buyGold > 0)
                                {
                                    if (item.buyCoin > 0)
                                    {
                                        g.drawImage(imgXu, num2 + num4 - 7, num3 + 7, 3);
                                        mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                    if (item.buyGold > 0)
                                    {
                                        g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7 + 11, 3);
                                        mFont.tahoma_7b_green.drawString(g, Res.formatNumber(item.buyGold), num2 + num4 - 15, num3 + 12, mFont.RIGHT);
                                    }
                                }
                                else
                                {
                                    if (item.buyCoin > 0)
                                    {
                                        g.drawImage(imgXu, num2 + num4 - 7, num3 + 7, 3);
                                        mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                    if (item.buyGold > 0)
                                    {
                                        g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7, 3);
                                        mFont.tahoma_7b_green.drawString(g, Res.formatNumber(item.buyGold), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                }
                            }
                            if (typeShop == 2 && currentTabIndex <= 3 && !Equals(GameCanvas.panel2))
                            {
                                if (item.buyCoin > 0 && item.buyGold > 0)
                                {
                                    if (item.buyCoin > 0)
                                    {
                                        g.drawImage(imgXu, num2 + num4 - 7, num3 + 7, 3);
                                        mFont2 = ((Char.myCharz().xu >= item.buyCoin) ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_red);
                                        mFont2.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                    if (item.buyGold > 0)
                                    {
                                        g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7 + 11, 3);
                                        mFont2 = ((Char.myCharz().luong >= item.buyGold) ? mFont.tahoma_7b_green : mFont.tahoma_7b_red);
                                        mFont2.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 12, mFont.RIGHT);
                                    }
                                }
                                else
                                {
                                    if (item.buyCoin > 0)
                                    {
                                        g.drawImage(imgXu, num2 + num4 - 7, num3 + 7, 3);
                                        mFont2 = ((Char.myCharz().xu >= item.buyCoin) ? mFont.tahoma_7b_yellow : mFont.tahoma_7b_red);
                                        mFont2.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                    if (item.buyGold > 0)
                                    {
                                        g.drawImage(imgLuong, num2 + num4 - 7, num3 + 7, 3);
                                        mFont2 = ((Char.myCharz().luong >= item.buyGold) ? mFont.tahoma_7b_green : mFont.tahoma_7b_red);
                                        mFont2.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 1, mFont.RIGHT);
                                    }
                                    try
                                    {
                                        mFont2 = mFont.tahoma_7b_green;
                                        if (!Char.myCharz().cName.Equals(item.nameNguoiKyGui))
                                        {
                                            mFont2 = mFont.tahoma_7b_green;
                                        }
                                        mFont2.drawString(g, item.nameNguoiKyGui, num2 + num4, num3 + 1 + mFont.tahoma_7b_red.getHeight(), mFont.RIGHT);
                                    }
                                    catch (Exception)
                                    {
                                    }
                                }
                            }
                        }
                        SmallImage.drawSmallImage(g, item.template.iconID, num5 + num7 / 2, num6 + num8 / 2, 0, 3);
                        if (item.quantity > 1)
                        {
                            mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
                        }
                        if (item.newItem && GameCanvas.gameTick % 10 > 5)
                        {
                            g.drawImage(imgNew, num5 + num7 / 2, num3 + 19, 3);
                        }
                    }
                    if (typeShop != 2 || (!Equals(GameCanvas.panel2) && currentTabIndex != 4) || item.buyType == 0)
                    {
                        continue;
                    }
                    if (item.buyType == 1)
                    {
                        mFont.tahoma_7_green.drawString(g, mResources.dangban, num2 + num4 - 5, num3 + 1, mFont.RIGHT);
                        if (item.buyCoin != -1)
                        {
                            g.drawImage(imgXu, num2 + num4 - 7, num3 + 19, 3);
                            mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 13, mFont.RIGHT);
                        }
                        else if (item.buyGold != -1)
                        {
                            g.drawImage(imgLuongKhoa, num2 + num4 - 7, num3 + 17, 3);
                            mFont.tahoma_7b_red.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 11, mFont.RIGHT);
                        }
                    }
                    else if (item.buyType == 2)
                    {
                        mFont.tahoma_7b_blue.drawString(g, mResources.daban, num2 + num4 - 5, num3 + 1, mFont.RIGHT);
                        if (item.buyCoin != -1)
                        {
                            g.drawImage(imgXu, num2 + num4 - 7, num3 + 17, 3);
                            mFont.tahoma_7b_yellow.drawString(g, Res.formatNumber2(item.buyCoin), num2 + num4 - 15, num3 + 11, mFont.RIGHT);
                        }
                        else if (item.buyGold != -1)
                        {
                            g.drawImage(imgLuongKhoa, num2 + num4 - 7, num3 + 17, 3);
                            mFont.tahoma_7b_red.drawString(g, Res.formatNumber2(item.buyGold), num2 + num4 - 15, num3 + 11, mFont.RIGHT);
                        }
                    }
                }
                paintScrollArrow(g);
            }
            catch (Exception)
            {
            }
        }
    
        private void paintAuto(mGraphics g)
        {
        }
    
        private void paintPetStatus(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < strStatus.Length; i++)
            {
                int x = xScroll;
                int num = yScroll + i * ITEM_HEIGHT;
                int num2 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(x, num, num2, h);
                    mFont.tahoma_7b_dark.drawString(g, strStatus[i], xScroll + wScroll / 2, num + 6, mFont.CENTER);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintPetSkill()
        {
        }
    
        private void paintPetInventory(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            Item[] arrItemBody = Char.myPetz().arrItemBody;
            Skill[] arrPetSkill = Char.myPetz().arrPetSkill;
            for (int i = 0; i < arrItemBody.Length + arrPetSkill.Length; i++)
            {
                bool flag = i < arrItemBody.Length;
                int num = i;
                int num2 = i - arrItemBody.Length;
                int num3 = xScroll + 36;
                int num4 = yScroll + i * ITEM_HEIGHT;
                int num5 = wScroll - 36;
                int h = ITEM_HEIGHT - 1;
                int num6 = xScroll;
                int num7 = yScroll + i * ITEM_HEIGHT;
                int num8 = 34;
                int num9 = ITEM_HEIGHT - 1;
                if (num4 - cmy > yScroll + hScroll || num4 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                Item item = ((!flag) ? null : arrItemBody[num]);
                g.setColor((i == selected) ? 16383818 : ((!flag) ? 15723751 : 15196114));
                g.fillRect(num3, num4, num5, h);
                g.setColor((i == selected) ? 9541120 : ((!flag) ? 11837316 : 9993045));
                if (item != null)
                {
                    for (int j = 0; j < item.itemOption.Length; j++)
                    {
                        if (item.itemOption[j].optionTemplate.id == 72 && item.itemOption[j].param > 0)
                        {
                            sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[j].param);
                            int color_ItemBg = GetColor_ItemBg(color_Item_Upgrade);
                            if (color_ItemBg != -1)
                            {
                                g.setColor((i != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
                            }
                        }
                    }
                }
                g.fillRect(num6, num7, num8, num9);
                if (item != null && item.isSelect && GameCanvas.panel.type == 12)
                {
                    g.setColor((i != selected) ? 6047789 : 7040779);
                    g.fillRect(num6, num7, num8, num9);
                }
                if (item != null)
                {
                    string text = string.Empty;
                    mFont mFont2 = mFont.tahoma_7_green2;
                    if (item.itemOption != null)
                    {
                        for (int k = 0; k < item.itemOption.Length; k++)
                        {
                            if (item.itemOption[k].optionTemplate.id == 72)
                            {
                                text = " [+" + item.itemOption[k].param + "]";
                            }
                            if (item.itemOption[k].optionTemplate.id == 41)
                            {
                                if (item.itemOption[k].param == 1)
                                {
                                    mFont2 = GetFont(0);
                                }
                                if (item.itemOption[k].optionTemplate.id == 107)
                                {
                                    paintItemStar(g, item.itemOption[k].param.ToString(), num3 + 125, num4 + 1);
                                }
                                else if (item.itemOption[k].param == 2)
                                {
                                    mFont2 = GetFont(2);
                                }
                                else if (item.itemOption[k].param == 3)
                                {
                                    mFont2 = GetFont(8);
                                }
                                else if (item.itemOption[k].param == 4)
                                {
                                    mFont2 = GetFont(7);
                                }
                            }
                        }
                    }
                    mFont2.drawString(g, item.template.name + text, num3 + 5, num4 + 1, 0);
                    string text2 = string.Empty;
                    if (item.itemOption != null)
                    {
                        if (item.itemOption.Length > 0 && item.itemOption[0] != null && item.itemOption[0].optionTemplate.id != 102 && item.itemOption[0].optionTemplate.id != 107)
                        {
                            text2 += item.itemOption[0].getOptionString();
                        }
                        mFont mFont3 = mFont.tahoma_7_blue;
                        if (item.compare < 0 && item.template.type != 5)
                        {
                            mFont3 = mFont.tahoma_7_red;
                        }
                        if (item.itemOption.Length > 1)
                        {
                            for (int l = 1; l < 2; l++)
                            {
                                if (item.itemOption[l] != null && item.itemOption[l].optionTemplate.id != 102 && item.itemOption[l].optionTemplate.id != 107)
                                {
                                    text2 = text2 + "," + item.itemOption[l].getOptionString();
                                }
                            }
                        }
                        mFont3.drawString(g, text2, num3 + 5, num4 + 11, mFont.LEFT);
                    }
                    SmallImage.drawSmallImage(g, item.template.iconID, num6 + num8 / 2, num7 + num9 / 2, 0, 3);
                    if (item.itemOption != null)
                    {
                        for (int m = 0; m < item.itemOption.Length; m++)
                        {
                            paintOptItem(g, item.itemOption[m].optionTemplate.id, item.itemOption[m].param, num6, num7, num8, num9);
                        }
                        for (int n = 0; n < item.itemOption.Length; n++)
                        {
                            paintOptSlotItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num6, num7, num8, num9);
                        }
                    }
                    if (item.quantity > 1)
                    {
                        mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num6 + num8, num7 + num9 - mFont.tahoma_7_yellow.getHeight(), 1);
                    }
                }
                else if (!flag)
                {
                    Skill skill = arrPetSkill[num2];
                    g.drawImage(GameScr.imgSkill, num6 + num8 / 2, num7 + num9 / 2, 3);
                    if (skill.template != null)
                    {
                        mFont.tahoma_7_blue.drawString(g, skill.template.name, num3 + 5, num4 + 1, 0);
                        mFont.tahoma_7_green2.drawString(g, mResources.level + ": " + skill.point + string.Empty, num3 + 5, num4 + 11, 0);
                        SmallImage.drawSmallImage(g, skill.template.iconId, num6 + num8 / 2, num7 + num9 / 2, 0, 3);
                    }
                    else
                    {
                        mFont.tahoma_7_green2.drawString(g, skill.moreInfo, num3 + 5, num4 + 5, 0);
                        SmallImage.drawSmallImage(g, GameScr.efs[98].arrEfInfo[0].idImg, num6 + num8 / 2, num7 + num9 / 2, 0, 3);
                    }
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintScrollArrow(mGraphics g)
        {
            g.translate(-g.getTranslateX(), -g.getTranslateY());
            if ((cmy > 24 && currentListLength > 0) || (Equals(GameCanvas.panel) && typeShop == 2 && maxPageShop[currentTabIndex] > 1))
            {
                g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 1, xScroll + wScroll - 12, yScroll + 3, 0);
            }
            if ((cmy < cmyLim && currentListLength > 0) || (Equals(GameCanvas.panel) && typeShop == 2 && maxPageShop[currentTabIndex] > 1))
            {
                g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 0, xScroll + wScroll - 12, yScroll + hScroll - 8, 0);
            }
        }
    
        private void paintTools(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < strTool.Length; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num, num2, num3, h);
                mFont.tahoma_7b_dark.drawString(g, strTool[i], xScroll + wScroll / 2, num2 + 6, mFont.CENTER);
                if (!strTool[i].Equals(mResources.gameInfo))
                {
                    continue;
                }
                for (int j = 0; j < vGameInfo.size(); j++)
                {
                    GameInfo gameInfo = (GameInfo)vGameInfo.elementAt(j);
                    if (!gameInfo.hasRead)
                    {
                        if (GameCanvas.gameTick % 20 > 10)
                        {
                            g.drawImage(imgNew, num + 10, num2 + 10, 3);
                        }
                        break;
                    }
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintGameSubInfo(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < contenInfo.Length; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * 15;
                int num3 = wScroll - 1;
                int num4 = ITEM_HEIGHT - 1;
                if (num2 - cmy <= yScroll + hScroll && num2 - cmy >= yScroll - ITEM_HEIGHT)
                {
                    mFont.tahoma_7b_dark.drawString(g, contenInfo[i], xScroll + 5, num2 + 6, mFont.LEFT);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintGameInfo(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < vGameInfo.size(); i++)
            {
                GameInfo gameInfo = (GameInfo)vGameInfo.elementAt(i);
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num2 - cmy <= yScroll + hScroll && num2 - cmy >= yScroll - ITEM_HEIGHT)
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(num, num2, num3, h);
                    mFont.tahoma_7b_dark.drawString(g, gameInfo.main, xScroll + wScroll / 2, num2 + 6, mFont.CENTER);
                    if (!gameInfo.hasRead && GameCanvas.gameTick % 20 > 10)
                    {
                        g.drawImage(imgNew, num + 10, num2 + 10, 3);
                    }
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintSkill(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            int num = Char.myCharz().nClass.skillTemplates.Length;
            for (int i = 0; i < num + 6; i++)
            {
                int num2 = xScroll + 30;
                int num3 = yScroll + i * ITEM_HEIGHT;
                int num4 = wScroll - 30;
                int h = ITEM_HEIGHT - 1;
                int num5 = xScroll;
                int num6 = yScroll + i * ITEM_HEIGHT;
                int num7 = 34;
                int num8 = ITEM_HEIGHT - 1;
                if (num3 - cmy > yScroll + hScroll || num3 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                if (i == 5)
                {
                    g.setColor((i != selected) ? 16765060 : 16776068);
                }
                g.fillRect(num2, num3, num4, h);
                g.drawImage(GameScr.imgSkill, num5, num6, 0);
                if (i == 0)
                {
                    SmallImage.drawSmallImage(g, 567, num5 + 4, num6 + 4, 0, 0);
                    string st = mResources.HP + " " + mResources.root + ": " + NinjaUtil.getMoneys((long)(Char.myCharz().cHPGoc));
                    mFont.tahoma_7b_blue.drawString(g, st, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys((long)(Char.myCharz().cHPGoc + 1000)) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().hpFrom1000TiemNang, num2 + 5, num3 + 15, 0);
                }
                if (i == 1)
                {
                    SmallImage.drawSmallImage(g, 569, num5 + 4, num6 + 4, 0, 0);
                    string st2 = mResources.KI + " " + mResources.root + ": " + NinjaUtil.getMoneys(Char.myCharz().cMPGoc);
                    mFont.tahoma_7b_blue.drawString(g, st2, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys(Char.myCharz().cMPGoc + 1000) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().mpFrom1000TiemNang, num2 + 5, num3 + 15, 0);
                }
                if (i == 2)
                {
                    SmallImage.drawSmallImage(g, 568, num5 + 4, num6 + 4, 0, 0);
                    string st3 = mResources.hit_point + " " + mResources.root + ": " + NinjaUtil.getMoneys(Char.myCharz().cDamGoc);
                    mFont.tahoma_7b_blue.drawString(g, st3, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys(Char.myCharz().cDamGoc * 100) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().damFrom1000TiemNang, num2 + 5, num3 + 15, 0);
                }
                if (i == 3)
                {
                    SmallImage.drawSmallImage(g, 721, num5 + 4, num6 + 4, 0, 0);
                    string st4 = mResources.armor + " " + mResources.root + ": " + NinjaUtil.getMoneys(Char.myCharz().cDefGoc);
                    mFont.tahoma_7b_blue.drawString(g, st4, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, NinjaUtil.getMoneys(500000 + Char.myCharz().cDefGoc * 100000) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().defFrom1000TiemNang, num2 + 5, num3 + 15, 0);
                }
                if (i == 4)
                {
                    SmallImage.drawSmallImage(g, 719, num5 + 4, num6 + 4, 0, 0);
                    string st5 = mResources.critical + " " + mResources.root + ": " + Char.myCharz().cCriticalGoc + "%";
                    long num9 = 50000000L;
                    int num10 = Char.myCharz().cCriticalGoc;
                    if (num10 > t_tiemnang.Length - 1)
                    {
                        num10 = t_tiemnang.Length - 1;
                    }
                    num9 = t_tiemnang[num10];
                    mFont.tahoma_7b_blue.drawString(g, st5, num2 + 5, num3 + 3, 0);
                    long number = num9;
                    mFont.tahoma_7_green2.drawString(g, Res.formatNumber2(number) + " " + mResources.potential + ": " + mResources.increase + " " + Char.myCharz().criticalFrom1000Tiemnang, num2 + 5, num3 + 15, 0);
                }
                if (i == 5)
                {
                    if (specialInfo != null)
                    {
                        SmallImage.drawSmallImage(g, spearcialImage, num5 + 4, num6 + 4, 0, 0);
                        string[] array = mFont.tahoma_7.splitFontArray(specialInfo, 120);
                        for (int j = 0; j < array.Length; j++)
                        {
                            mFont.tahoma_7_green2.drawString(g, array[j], num2 + 5, num3 + 3 + j * 12, 0);
                        }
                    }
                    else
                    {
                        mFont.tahoma_7_green2.drawString(g, string.Empty, num2 + 5, num3 + 9, 0);
                    }
                }
                if (i < 6)
                {
                    continue;
                }
                int num11 = i - 6;
                SkillTemplate skillTemplate = Char.myCharz().nClass.skillTemplates[num11];
                SmallImage.drawSmallImage(g, skillTemplate.iconId, num5 + 4, num6 + 4, 0, 0);
                Skill skill = Char.myCharz().getSkill(skillTemplate);
                if (skill != null)
                {
                    mFont.tahoma_7b_blue.drawString(g, skillTemplate.name, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_blue.drawString(g, mResources.level + ": " + skill.point, num2 + num4 - 5, num3 + 3, mFont.RIGHT);
                    if (skill.point == skillTemplate.maxPoint)
                    {
                        mFont.tahoma_7_green2.drawString(g, mResources.max_level_reach, num2 + 5, num3 + 15, 0);
                    }
                    else if (skill.template.isSkillSpec())
                    {
                        string text = mResources.proficiency + ": ";
                        int x = mFont.tahoma_7_green2.getWidthExactOf(text) + num2 + 5;
                        int num12 = num3 + 15;
                        mFont.tahoma_7_green2.drawString(g, text, num2 + 5, num12, 0);
                        mFont.tahoma_7_green2.drawString(g, "(" + skill.strCurExp() + ")", num2 + num4 - 5, num12, mFont.RIGHT);
                        num12 += 4;
                        g.setColor(7169134);
                        g.fillRect(x, num12, 50, 5);
                        int num13 = skill.curExp * 50 / 1000;
                        g.setColor(11992374);
                        g.fillRect(x, num12, num13, 5);
                        if (skill.curExp < 1000)
                        {
                        }
                    }
                    else
                    {
                        Skill skill2 = skillTemplate.skills[skill.point];
                        mFont.tahoma_7_green2.drawString(g, mResources.level + " " + (skill.point + 1) + " " + mResources.need + " " + Res.formatNumber2(skill2.powRequire) + " " + mResources.potential, num2 + 5, num3 + 15, 0);
                    }
                }
                else
                {
                    Skill skill3 = skillTemplate.skills[0];
                    string st6 = mResources.need_upper + " " + Res.formatNumber2(skill3.powRequire) + " " + mResources.potential_to_learn;
                    if (skill3.template.id == 24 || skill3.template.id == 25 || skill3.template.id == 26)
                    {
                        st6 = mResources.need_upper + " " + Res.formatNumber2(skill3.powRequire) + " " + mResources.potential_to_learn_tuyetKi;
                    }
                    mFont.tahoma_7b_green.drawString(g, skillTemplate.name, num2 + 5, num3 + 3, 0);
                    mFont.tahoma_7_green2.drawString(g, st6, num2 + 5, num3 + 15, 0);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintMapTrans(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < mapNames.Length; i++)
            {
                int num = xScroll + 36;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 36;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll;
                int num5 = yScroll + i * ITEM_HEIGHT;
                int num6 = 34;
                int num7 = ITEM_HEIGHT - 1;
                if (num2 - cmy <= yScroll + hScroll && num2 - cmy >= yScroll - ITEM_HEIGHT)
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(xScroll, num2, wScroll, h);
                    mFont.tahoma_7b_blue.drawString(g, mapNames[i], 5, num2 + 1, 0);
                    mFont.tahoma_7_grey.drawString(g, planetNames[i], 5, num2 + 11, 0);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintZone(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            int[] zones = GameScr.gI().zones;
            int[] pts = GameScr.gI().pts;
            for (int i = 0; i < pts.Length; i++)
            {
                int num = xScroll + 36;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 36;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll;
                int y = yScroll + i * ITEM_HEIGHT;
                int num5 = 34;
                int h2 = ITEM_HEIGHT - 1;
                if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num, num2, num3, h);
                g.setColor(zoneColor[pts[i]]);
                g.fillRect(num4, y, num5, h2);
                if (zones[i] != -1)
                {
                    if (pts[i] != 1)
                    {
                        mFont.tahoma_7_yellow.drawString(g, zones[i] + string.Empty, num4 + num5 / 2, num2 + 6, mFont.CENTER);
                    }
                    else
                    {
                        mFont.tahoma_7_grey.drawString(g, zones[i] + string.Empty, num4 + num5 / 2, num2 + 6, mFont.CENTER);
                    }
                    mFont.tahoma_7_green2.drawString(g, GameScr.gI().numPlayer[i] + "/" + GameScr.gI().maxPlayer[i], num + 5, num2 + 6, 0);
                }
                if (GameScr.gI().rankName1[i] != null)
                {
                    mFont.tahoma_7_grey.drawString(g, GameScr.gI().rankName1[i] + "(Top " + GameScr.gI().rank1[i] + ")", num + num3 - 2, num2 + 1, mFont.RIGHT);
                    mFont.tahoma_7_grey.drawString(g, GameScr.gI().rankName2[i] + "(Top " + GameScr.gI().rank2[i] + ")", num + num3 - 2, num2 + 11, mFont.RIGHT);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintSpeacialSkill(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0)
            {
                return;
            }
            int num = (cmy + hScroll) / 24 + 1;
            if (num < hScroll / 24 + 1)
            {
                num = hScroll / 24 + 1;
            }
            if (num > currentListLength)
            {
                num = currentListLength;
            }
            int num2 = cmy / 24;
            if (num2 >= num)
            {
                num2 = num - 1;
            }
            if (num2 < 0)
            {
                num2 = 0;
            }
            for (int i = num2; i < num; i++)
            {
                int num3 = xScroll;
                int num4 = yScroll + i * ITEM_HEIGHT;
                int num5 = 24;
                int num6 = ITEM_HEIGHT - 1;
                int num7 = xScroll + num5;
                int num8 = yScroll + i * ITEM_HEIGHT;
                int num9 = wScroll - num5;
                int h = ITEM_HEIGHT - 1;
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num7, num8, num9, h);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num3, num4, num5, num6);
                SmallImage.drawSmallImage(g, Char.myCharz().imgSpeacialSkill[currentTabIndex][i], num3 + num5 / 2, num4 + num6 / 2, 0, 3);
                string[] array = mFont.tahoma_7_grey.splitFontArray(Char.myCharz().infoSpeacialSkill[currentTabIndex][i], 140);
                for (int j = 0; j < array.Length; j++)
                {
                    mFont.tahoma_7_grey.drawString(g, array[j], num7 + 5, num8 + 1 + j * 11, 0);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintBox(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            try
            {
                Item[] arrItemBox = Char.myCharz().arrItemBox;
                currentListLength = checkCurrentListLength(arrItemBox.Length);
                int num = arrItemBox.Length / 20 + ((arrItemBox.Length % 20 > 0) ? 1 : 0);
                TAB_W_NEW = wScroll / num;
                for (int i = 0; i < currentListLength; i++)
                {
                    int num2 = xScroll + 36;
                    int num3 = yScroll + i * ITEM_HEIGHT;
                    int num4 = wScroll - 36;
                    int h = ITEM_HEIGHT - 1;
                    int num5 = xScroll;
                    int num6 = yScroll + i * ITEM_HEIGHT;
                    int num7 = 34;
                    int num8 = ITEM_HEIGHT - 1;
                    if (num3 - cmy > yScroll + hScroll || num3 - cmy < yScroll - ITEM_HEIGHT)
                    {
                        continue;
                    }
                    if (i == 0)
                    {
                        for (int j = 0; j < num; j++)
                        {
                            int num9 = ((j == newSelected && selected == 0) ? ((GameCanvas.gameTick % 10 < 7) ? (-1) : 0) : 0);
                            g.setColor((j != newSelected) ? 15723751 : 16383818);
                            g.fillRect(xScroll + j * TAB_W_NEW, num3 + 9 + num9, TAB_W_NEW - 1, 14);
                            mFont.tahoma_7_grey.drawString(g, string.Empty + j, xScroll + j * TAB_W_NEW + TAB_W_NEW / 2, yScroll + 11 + num9, mFont.CENTER);
                        }
                        continue;
                    }
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(num2, num3, num4, h);
                    g.setColor((i != selected) ? 9993045 : 9541120);
                    int inventorySelect_body = GetInventorySelect_body(i, newSelected);
                    Item item = arrItemBox[inventorySelect_body];
                    if (item != null)
                    {
                        for (int k = 0; k < item.itemOption.Length; k++)
                        {
                            if (item.itemOption[k].optionTemplate.id == 72 && item.itemOption[k].param > 0)
                            {
                                sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[k].param);
                                int color_ItemBg = GetColor_ItemBg(color_Item_Upgrade);
                                if (color_ItemBg != -1)
                                {
                                    g.setColor((i != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
                                }
                            }
                        }
                    }
                    g.fillRect(num5, num6, num7, num8);
                    if (item == null)
                    {
                        continue;
                    }
                    string text = string.Empty;
                    mFont mFont2 = mFont.tahoma_7_green2;
                    if (item.itemOption != null)
                    {
                        for (int l = 0; l < item.itemOption.Length; l++)
                        {
                            if (item.itemOption[l].optionTemplate.id == 72)
                            {
                                text = " [+" + item.itemOption[l].getOptionString() + "]";
                            }
                            if (item.itemOption[l].optionTemplate.id == 107)
                            {
                                paintItemStar(g, item.itemOption[l].param.ToString(), num2 + 125, num3 + 1);
                            }
                            if (item.itemOption[l].optionTemplate.id == 41)
                            {
                                if (item.itemOption[l].param == 1)
                                {
                                    mFont2 = GetFont(0);
                                }
                                else if (item.itemOption[l].param == 2)
                                {
                                    mFont2 = GetFont(2);
                                }
                                else if (item.itemOption[l].param == 3)
                                {
                                    mFont2 = GetFont(8);
                                }
                                else if (item.itemOption[l].param == 4)
                                {
                                    mFont2 = GetFont(7);
                                }
                            }
                        }
                    }
                    mFont2.drawString(g, item.template.name + text, num2 + 5, num3 + 1, 0);
                    string text2 = string.Empty;
                    if (item.itemOption != null)
                    {
                        if (item.itemOption.Length > 0 && item.itemOption[0] != null)
                        {
                            text2 += item.itemOption[0].getOptionString();
                        }
                        mFont mFont3 = mFont.tahoma_7_blue;
                        if (item.compare < 0 && item.template.type != 5)
                        {
                            mFont3 = mFont.tahoma_7_red;
                        }
                        if (item.itemOption.Length > 1)
                        {
                            for (int m = 1; m < item.itemOption.Length; m++)
                            {
                                if (item.itemOption[m] != null && item.itemOption[m].optionTemplate.id != 102 && item.itemOption[m].optionTemplate.id != 107)
                                {
                                    text2 = text2 + "," + item.itemOption[m].getOptionString();
                                }
                            }
                        }
                        mFont3.drawString(g, text2, num2 + 5, num3 + 11, mFont.LEFT);
                    }
                    SmallImage.drawSmallImage(g, item.template.iconID, num5 + num7 / 2, num6 + num8 / 2, 0, 3);
                    if (item.itemOption != null)
                    {
                        for (int n = 0; n < item.itemOption.Length; n++)
                        {
                            paintOptItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num5, num6, num7, num8);
                        }
                        for (int num10 = 0; num10 < item.itemOption.Length; num10++)
                        {
                            paintOptSlotItem(g, item.itemOption[num10].optionTemplate.id, item.itemOption[num10].param, num5, num6, num7, num8);
                        }
                    }
                    if (item.quantity > 1)
                    {
                        mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
                    }
                }
            }
            catch (Exception)
            {
            }
            paintScrollArrow(g);
        }
    
        public Member getCurrMember()
        {
            if (selected < 2)
            {
                return null;
            }
            if (selected > ((member == null) ? myMember.size() : member.size()) + 1)
            {
                return null;
            }
            return (member == null) ? ((Member)myMember.elementAt(selected - 2)) : ((Member)member.elementAt(selected - 2));
        }
    
        public ClanMessage getCurrMessage()
        {
            if (selected < 2)
            {
                return null;
            }
            if (selected > ClanMessage.vMessage.size() + 1)
            {
                return null;
            }
            return (ClanMessage)ClanMessage.vMessage.elementAt(selected - 2);
        }
    
        public Clan getCurrClan()
        {
            if (selected < 2)
            {
                return null;
            }
            if (selected > clans.Length + 1)
            {
                return null;
            }
            return clans[selected - 2];
        }
    
        private void paintLogChat(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (logChat.size() == 0)
            {
                mFont.tahoma_7_green2.drawString(g, mResources.no_msg, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2 + 24, 2);
            }
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = 24;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll + num3;
                int num5 = yScroll + i * ITEM_HEIGHT;
                int num6 = wScroll - num3;
                int num7 = ITEM_HEIGHT - 1;
                if (i == 0)
                {
                    g.setColor(15196114);
                    g.fillRect(num, num5, wScroll, num7);
                    g.drawImage((i != selected) ? GameScr.imgLbtn2 : GameScr.imgLbtnFocus2, xScroll + wScroll - 5, num5 + 2, StaticObj.TOP_RIGHT);
                    ((i != selected) ? mFont.tahoma_7b_dark : mFont.tahoma_7b_green2).drawString(g, (!isViewChatServer) ? mResources.on : mResources.off, xScroll + wScroll - 22, num5 + 7, 2);
                    mFont.tahoma_7_grey.drawString(g, (!isViewChatServer) ? mResources.onPlease : mResources.offPlease, xScroll + 5, num5 + num7 / 2 - 4, mFont.LEFT);
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num4, num5, num6, num7);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num, num2, num3, h);
                InfoItem infoItem = (InfoItem)logChat.elementAt(i - 1);
                if (infoItem.charInfo.headICON != -1)
                {
                    SmallImage.drawSmallImage(g, infoItem.charInfo.headICON, num, num2, 0, 0);
                }
                else
                {
                    Part part = GameScr.parts[infoItem.charInfo.head];
                    SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + part.pi[Char.CharInfo[0][0][0]].dx, num2 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                }
                g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                mFont tahoma_7b_dark = mFont.tahoma_7b_dark;
                tahoma_7b_dark = mFont.tahoma_7b_green2;
                tahoma_7b_dark.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                if (!infoItem.isChatServer)
                {
                    mFont.tahoma_7_blue.drawString(g, Res.split(infoItem.s, "|", 0)[2], num4 + 5, num5 + 11, 0);
                }
                else
                {
                    mFont.tahoma_7_red.drawString(g, Res.split(infoItem.s, "|", 0)[2], num4 + 5, num5 + 11, 0);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintFlagChange(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll + 26;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = wScroll - 26;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll;
                int num5 = yScroll + i * ITEM_HEIGHT;
                int num6 = 24;
                int num7 = ITEM_HEIGHT - 1;
                if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num, num2, num3, h);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num4, num5, num6, num7);
                Item item = (Item)vFlag.elementAt(i);
                if (item == null)
                {
                    continue;
                }
                mFont.tahoma_7_green2.drawString(g, item.template.name, num + 5, num2 + 1, 0);
                string text = string.Empty;
                if (item.itemOption != null && item.itemOption.Length >= 1)
                {
                    if (item.itemOption[0] != null && item.itemOption[0].optionTemplate.id != 102 && item.itemOption[0].optionTemplate.id != 107)
                    {
                        text += item.itemOption[0].getOptionString();
                    }
                    mFont tahoma_7_blue = mFont.tahoma_7_blue;
                    tahoma_7_blue.drawString(g, text, num + 5, num2 + 11, 0);
                    SmallImage.drawSmallImage(g, item.template.iconID, num4 + num6 / 2, num5 + num7 / 2, 0, 3);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintEnemy(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0)
            {
                mFont.tahoma_7_green2.drawString(g, mResources.no_enemy, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
                return;
            }
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = 24;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll + num3;
                int num5 = yScroll + i * ITEM_HEIGHT;
                int num6 = wScroll - num3;
                int h2 = ITEM_HEIGHT - 1;
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num4, num5, num6, h2);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num, num2, num3, h);
                InfoItem infoItem = (InfoItem)vEnemy.elementAt(i);
                if (infoItem.charInfo.headICON != -1)
                {
                    SmallImage.drawSmallImage(g, infoItem.charInfo.headICON, num, num2, 0, 0);
                }
                else
                {
                    Part part = GameScr.parts[infoItem.charInfo.head];
                    SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + part.pi[Char.CharInfo[0][0][0]].dx, num2 + 3 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                }
                g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                if (infoItem.isOnline)
                {
                    mFont.tahoma_7b_green.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                    mFont.tahoma_7_blue.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
                }
                else
                {
                    mFont.tahoma_7_grey.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                    mFont.tahoma_7_grey.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintFriend(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0)
            {
                mFont.tahoma_7_green2.drawString(g, mResources.no_friend, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
                return;
            }
            for (int i = 0; i < currentListLength; i++)
            {
                int num = xScroll;
                int num2 = yScroll + i * ITEM_HEIGHT;
                int num3 = 24;
                int h = ITEM_HEIGHT - 1;
                int num4 = xScroll + num3;
                int num5 = yScroll + i * ITEM_HEIGHT;
                int num6 = wScroll - num3;
                int h2 = ITEM_HEIGHT - 1;
                g.setColor((i != selected) ? 15196114 : 16383818);
                g.fillRect(num4, num5, num6, h2);
                g.setColor((i != selected) ? 9993045 : 9541120);
                g.fillRect(num, num2, num3, h);
                InfoItem infoItem = (InfoItem)vFriend.elementAt(i);
                if (infoItem.charInfo.headICON != -1)
                {
                    SmallImage.drawSmallImage(g, infoItem.charInfo.headICON, num, num2, 0, 0);
                }
                else
                {
                    Part part = GameScr.parts[infoItem.charInfo.head];
                    SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num + part.pi[Char.CharInfo[0][0][0]].dx, num2 + 3 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                }
                g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                if (infoItem.isOnline)
                {
                    mFont.tahoma_7b_green.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                    mFont.tahoma_7_blue.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
                }
                else
                {
                    mFont.tahoma_7_grey.drawString(g, infoItem.charInfo.cName, num4 + 5, num5, 0);
                    mFont.tahoma_7_grey.drawString(g, infoItem.s, num4 + 5, num5 + 11, 0);
                }
            }
            paintScrollArrow(g);
        }
    
        public void paintPlayerMenu(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < vPlayerMenu.size(); i++)
            {
                int x = xScroll;
                int num = yScroll + i * ITEM_HEIGHT;
                int num2 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
                {
                    Command command = (Command)vPlayerMenu.elementAt(i);
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(x, num, num2, h);
                    if (command.caption2.Equals(string.Empty))
                    {
                        mFont.tahoma_7b_dark.drawString(g, command.caption, xScroll + wScroll / 2, num + 6, mFont.CENTER);
                        continue;
                    }
                    mFont.tahoma_7b_dark.drawString(g, command.caption, xScroll + wScroll / 2, num + 1, mFont.CENTER);
                    mFont.tahoma_7b_dark.drawString(g, command.caption2, xScroll + wScroll / 2, num + 11, mFont.CENTER);
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintClans(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(-cmx, -cmy);
            g.setColor(0);
            int num = xScroll + wScroll / 2 - clansOption.Length * TAB_W / 2;
            if (currentListLength == 2)
            {
                mFont.tahoma_7_green2.drawString(g, clanReport, xScroll + wScroll / 2, yScroll + 24 + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
                if (isMessage && myMember.size() == 1)
                {
                    for (int i = 0; i < mResources.clanEmpty.Length; i++)
                    {
                        mFont.tahoma_7b_dark.drawString(g, mResources.clanEmpty[i], xScroll + wScroll / 2, yScroll + 24 + hScroll / 2 - mResources.clanEmpty.Length * 12 / 2 + i * 12, mFont.CENTER);
                    }
                }
            }
            if (isMessage)
            {
                currentListLength = ClanMessage.vMessage.size() + 2;
            }
            for (int j = 0; j < currentListLength; j++)
            {
                int num2 = xScroll;
                int num3 = yScroll + j * ITEM_HEIGHT;
                int num4 = 24;
                int num5 = ITEM_HEIGHT - 1;
                int num6 = xScroll + num4;
                int num7 = yScroll + j * ITEM_HEIGHT;
                int num8 = wScroll - num4;
                int num9 = ITEM_HEIGHT - 1;
                if (num7 - cmy > yScroll + hScroll || num7 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                switch (j)
                {
                    case 0:
                        {
                            for (int k = 0; k < clansOption.Length; k++)
                            {
                                g.setColor((k != cSelected || j != selected) ? 15723751 : 16383818);
                                g.fillRect(num + k * TAB_W, num7, TAB_W - 1, 23);
                                for (int l = 0; l < clansOption[k].Length; l++)
                                {
                                    mFont.tahoma_7_grey.drawString(g, clansOption[k][l], num + k * TAB_W + TAB_W / 2, yScroll + l * 11, mFont.CENTER);
                                }
                            }
                            continue;
                        }
                    case 1:
                        g.setColor((j != selected) ? 15196114 : 16383818);
                        g.fillRect(xScroll, num7, wScroll, num9);
                        if (clanInfo != null)
                        {
                            mFont.tahoma_7b_dark.drawString(g, clanInfo, xScroll + wScroll / 2, num7 + 6, mFont.CENTER);
                        }
                        continue;
                }
                if (isSearchClan)
                {
                    if (clans == null || clans.Length == 0)
                    {
                        continue;
                    }
                    g.setColor((j != selected) ? 15196114 : 16383818);
                    g.fillRect(num6, num7, num8, num9);
                    g.setColor((j != selected) ? 9993045 : 9541120);
                    g.fillRect(num2, num3, num4, num5);
                    if (ClanImage.isExistClanImage(clans[j - 2].imgID))
                    {
                        if (ClanImage.getClanImage((short)clans[j - 2].imgID).idImage != null)
                        {
                            SmallImage.drawSmallImage(g, ClanImage.getClanImage((short)clans[j - 2].imgID).idImage[0], num2 + num4 / 2, num3 + num5 / 2, 0, StaticObj.VCENTER_HCENTER);
                        }
                    }
                    else
                    {
                        ClanImage clanImage = new ClanImage();
                        clanImage.ID = clans[j - 2].imgID;
                        if (!ClanImage.isExistClanImage(clanImage.ID))
                        {
                            ClanImage.addClanImage(clanImage);
                        }
                    }
                    string st = ((clans[j - 2].name.Length <= 23) ? clans[j - 2].name : (clans[j - 2].name.Substring(0, 23) + "..."));
                    mFont.tahoma_7b_green2.drawString(g, st, num6 + 5, num7, 0);
                    g.setClip(num6, num7, num8 - 10, num9);
                    mFont.tahoma_7_blue.drawString(g, clans[j - 2].slogan, num6 + 5, num7 + 11, 0);
                    g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                    mFont.tahoma_7_green2.drawString(g, clans[j - 2].currMember + "/" + clans[j - 2].maxMember, num6 + num8 - 5, num7, mFont.RIGHT);
                    continue;
                }
                if (isViewMember)
                {
                    g.setColor((j != selected) ? 15196114 : 16383818);
                    g.fillRect(num6, num7, num8, num9);
                    g.setColor((j != selected) ? 9993045 : 9541120);
                    g.fillRect(num2, num3, num4, num5);
                    Member member = ((this.member == null) ? ((Member)myMember.elementAt(j - 2)) : ((Member)this.member.elementAt(j - 2)));
                    if (member.headICON != -1)
                    {
                        SmallImage.drawSmallImage(g, member.headICON, num2, num3, 0, 0);
                    }
                    else
                    {
                        Part part = GameScr.parts[member.head];
                        SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, num2 + part.pi[Char.CharInfo[0][0][0]].dx, num3 + 3 + part.pi[Char.CharInfo[0][0][0]].dy, 0, 0);
                    }
                    g.setClip(xScroll, yScroll + cmy, wScroll, hScroll);
                    mFont mFont2 = mFont.tahoma_7b_dark;
                    if (member.role == 0)
                    {
                        mFont2 = mFont.tahoma_7b_red;
                    }
                    else if (member.role == 1)
                    {
                        mFont2 = mFont.tahoma_7b_green;
                    }
                    // Thanh vien thuong: giu mau den mac dinh.
                    mFont2.drawString(g, member.name, num6 + 5, num7, 0);
                    mFont.tahoma_7_blue.drawString(g, mResources.power + ": " + member.powerPoint, num6 + 5, num7 + 11, 0);
                    SmallImage.drawSmallImage(g, 7223, num6 + num8 - 7, num7 + 12, 0, 3);
                    mFont.tahoma_7_blue.drawString(g, string.Empty + member.clanPoint, num6 + num8 - 15, num7 + 6, mFont.RIGHT);
                    continue;
                }
                if (!isMessage || ClanMessage.vMessage.size() == 0)
                {
                    continue;
                }
                ClanMessage clanMessage = (ClanMessage)ClanMessage.vMessage.elementAt(j - 2);
                g.setColor((j != selected || clanMessage.option != null) ? 15196114 : 16383818);
                g.fillRect(num2, num3, num8 + num4, num9);
                clanMessage.paint(g, num2, num3);
                if (clanMessage.option == null)
                {
                    continue;
                }
                int num10 = xScroll + wScroll - 2 - clanMessage.option.Length * 40;
                for (int m = 0; m < clanMessage.option.Length; m++)
                {
                    if (m == cSelected && j == selected)
                    {
                        g.drawImage(GameScr.imgLbtnFocus2, num10 + m * 40 + 20, num7 + num9 / 2, StaticObj.VCENTER_HCENTER);
                        mFont.tahoma_7b_green2.drawString(g, clanMessage.option[m], num10 + m * 40 + 20, num7 + 6, mFont.CENTER);
                    }
                    else
                    {
                        g.drawImage(GameScr.imgLbtn2, num10 + m * 40 + 20, num7 + num9 / 2, StaticObj.VCENTER_HCENTER);
                        mFont.tahoma_7b_dark.drawString(g, clanMessage.option[m], num10 + m * 40 + 20, num7 + 6, mFont.CENTER);
                    }
                }
            }
            paintScrollArrow(g);
        }
    
        private void paintArchivement(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            g.setColor(0);
            if (currentListLength == 0)
            {
                mFont.tahoma_7_green2.drawString(g, mResources.no_mission, xScroll + wScroll / 2, yScroll + hScroll / 2 - mFont.tahoma_7.getHeight() / 2, 2);
            }
            else
            {
                if (Char.myCharz().arrArchive == null || Char.myCharz().arrArchive.Length != currentListLength)
                {
                    return;
                }
                for (int i = 0; i < currentListLength; i++)
                {
                    int num = xScroll;
                    int num2 = yScroll + i * ITEM_HEIGHT;
                    int num3 = wScroll;
                    int num4 = ITEM_HEIGHT - 1;
                    Archivement archivement = Char.myCharz().arrArchive[i];
                    g.setColor((i != selected || ((archivement.isRecieve || archivement.isFinish) && (!archivement.isRecieve || !archivement.isFinish))) ? 15196114 : 16383818);
                    g.fillRect(num, num2, num3, num4);
                    if (archivement == null)
                    {
                        continue;
                    }
                    if (!archivement.isFinish)
                    {
                        mFont.tahoma_7.drawString(g, archivement.info1, num + 5, num2, 0);
                        mFont.tahoma_7_green.drawString(g, archivement.money + " " + mResources.RUBY, num + num3 - 5, num2, mFont.RIGHT);
                        mFont.tahoma_7_red.drawString(g, archivement.info2, num + 5, num2 + 11, 0);
                    }
                    else if (archivement.isFinish && !archivement.isRecieve)
                    {
                        mFont.tahoma_7.drawString(g, archivement.info1, num + 5, num2, 0);
                        mFont.tahoma_7_blue.drawString(g, mResources.reward_mission + archivement.money + " " + mResources.RUBY, num + 5, num2 + 11, 0);
                        if (i == selected)
                        {
                            mFont.tahoma_7b_green2.drawString(g, mResources.receive_upper, num + num3 - 20, num2 + 6, mFont.CENTER);
                            mFont.tahoma_7b_dark.drawString(g, mResources.receive_upper, num + num3 - 20, num2 + 6, mFont.CENTER);
                        }
                        else
                        {
                            g.drawImage(GameScr.imgLbtn2, num + num3 - 20, num2 + num4 / 2, StaticObj.VCENTER_HCENTER);
                            mFont.tahoma_7b_dark.drawString(g, mResources.receive_upper, num + num3 - 20, num2 + 6, mFont.CENTER);
                        }
                    }
                    else if (archivement.isFinish && archivement.isRecieve)
                    {
                        mFont.tahoma_7_green.drawString(g, archivement.info1, num + 5, num2, 0);
                        mFont.tahoma_7_green.drawString(g, archivement.info2, num + 5, num2 + 11, 0);
                    }
                }
                paintScrollArrow(g);
            }
        }
    
        private void paintCombine(mGraphics g)
        {
            g.setColor(16711680);
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            if (vItemCombine.size() == 0)
            {
                if (combineInfo != null)
                {
                    for (int i = 0; i < combineInfo.Length; i++)
                    {
                        mFont.tahoma_7b_dark.drawString(g, combineInfo[i], xScroll + wScroll / 2, yScroll + hScroll / 2 - combineInfo.Length * 14 / 2 + i * 14 + 5, 2);
                    }
                }
                return;
            }
            for (int j = 0; j < vItemCombine.size() + 1; j++)
            {
                int num = xScroll + 36;
                int num2 = yScroll + j * ITEM_HEIGHT;
                int num3 = wScroll - 36;
                int num4 = ITEM_HEIGHT - 1;
                int num5 = xScroll;
                int num6 = yScroll + j * ITEM_HEIGHT;
                int num7 = 34;
                int num8 = ITEM_HEIGHT - 1;
                if (num2 - cmy > yScroll + hScroll || num2 - cmy < yScroll - ITEM_HEIGHT)
                {
                    continue;
                }
                if (j == vItemCombine.size())
                {
                    if (vItemCombine.size() > 0)
                    {
                        if (!GameCanvas.isTouch && j == selected)
                        {
                            g.setColor(16383818);
                            g.fillRect(num5, num2, wScroll, num4 + 2);
                        }
                        if ((j == selected && keyTouchCombine == 1) || (!GameCanvas.isTouch && j == selected))
                        {
                            g.drawImage(GameScr.imgLbtnFocus, xScroll + wScroll / 2, num2 + num4 / 2 + 1, StaticObj.VCENTER_HCENTER);
                            mFont.tahoma_7b_green2.drawString(g, mResources.UPGRADE, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                        }
                        else
                        {
                            g.drawImage(GameScr.imgLbtn, xScroll + wScroll / 2, num2 + num4 / 2 + 1, StaticObj.VCENTER_HCENTER);
                            mFont.tahoma_7b_dark.drawString(g, mResources.UPGRADE, xScroll + wScroll / 2, num2 + num4 / 2 - 4, mFont.CENTER);
                        }
                    }
                    continue;
                }
                g.setColor((j != selected) ? 15196114 : 16383818);
                g.fillRect(num, num2, num3, num4);
                g.setColor((j != selected) ? 9993045 : 9541120);
                Item item = (Item)vItemCombine.elementAt(j);
                if (item != null)
                {
                    for (int k = 0; k < item.itemOption.Length; k++)
                    {
                        if (item.itemOption[k].optionTemplate.id == 72 && item.itemOption[k].param > 0)
                        {
                            sbyte color_Item_Upgrade = GetColor_Item_Upgrade(item.itemOption[k].param);
                            int color_ItemBg = GetColor_ItemBg(color_Item_Upgrade);
                            if (color_ItemBg != -1)
                            {
                                g.setColor((j != selected) ? GetColor_ItemBg(color_Item_Upgrade) : GetColor_ItemBg(color_Item_Upgrade));
                            }
                        }
                    }
                }
                g.fillRect(num5, num6, num7, num8);
                if (item == null)
                {
                    continue;
                }
                string text = string.Empty;
                mFont mFont2 = mFont.tahoma_7_green2;
                if (item.itemOption != null)
                {
                    for (int l = 0; l < item.itemOption.Length; l++)
                    {
                        if (item.itemOption[l].optionTemplate.id == 72)
                        {
                            text = " [+" + item.itemOption[l].param + "]";
                        }
                        if (item.itemOption[l].optionTemplate.id == 107)
                        {
                            paintItemStar(g, item.itemOption[l].param.ToString(), num + 125, num2 + 1);
                        }
                        if (item.itemOption[l].optionTemplate.id == 41)
                        {
                            if (item.itemOption[l].param == 1)
                            {
                                mFont2 = GetFont(0);
                            }
                            else if (item.itemOption[l].param == 2)
                            {
                                mFont2 = GetFont(2);
                            }
                            else if (item.itemOption[l].param == 3)
                            {
                                mFont2 = GetFont(8);
                            }
                            else if (item.itemOption[l].param == 4)
                            {
                                mFont2 = GetFont(7);
                            }
                        }
                    }
                }
                mFont2.drawString(g, item.template.name + text, num + 5, num2 + 1, 0);
                string text2 = string.Empty;
                if (item.itemOption != null)
                {
                    if (item.itemOption.Length > 0 && item.itemOption[0] != null && item.itemOption[0].optionTemplate.id != 102 && item.itemOption[0].optionTemplate.id != 107)
                    {
                        text2 += item.itemOption[0].getOptionString();
                    }
                    mFont mFont3 = mFont.tahoma_7_blue;
                    if (item.compare < 0 && item.template.type != 5)
                    {
                        mFont3 = mFont.tahoma_7_red;
                    }
                    if (item.itemOption.Length > 1)
                    {
                        for (int m = 1; m < item.itemOption.Length; m++)
                        {
                            if (item.itemOption[m] != null && item.itemOption[m].optionTemplate.id != 102 && item.itemOption[m].optionTemplate.id != 107)
                            {
                                text2 = text2 + "," + item.itemOption[m].getOptionString();
                            }
                        }
                    }
                    mFont3.drawString(g, text2, num + 5, num2 + 11, mFont.LEFT);
                }
                SmallImage.drawSmallImage(g, item.template.iconID, num5 + num7 / 2, num6 + num8 / 2, 0, 3);
                if (item.itemOption != null)
                {
                    for (int n = 0; n < item.itemOption.Length; n++)
                    {
                        paintOptItem(g, item.itemOption[n].optionTemplate.id, item.itemOption[n].param, num5, num6, num7, num8);
                    }
                    for (int num9 = 0; num9 < item.itemOption.Length; num9++)
                    {
                        paintOptSlotItem(g, item.itemOption[num9].optionTemplate.id, item.itemOption[num9].param, num5, num6, num7, num8);
                    }
                }
                if (item.quantity > 1)
                {
                    mFont.tahoma_7_yellow.drawString(g, string.Empty + item.quantity, num5 + num7, num6 + num8 - mFont.tahoma_7_yellow.getHeight(), 1);
                }
            }
            paintScrollArrow(g);
        }
        private void paintItemStar(mGraphics g, string opt, int x, int y)
        {
            g.drawImage(imgStar, x, y);
            mFont.tahoma_7b_red.drawString(g, opt, x - imgStar.getWidth() / 2 - mFont.tahoma_7b_red.getWidth(opt) / 2 + 1, y - 1, 0);
        }
    
    
        // ==============================================================
        //  Hành trang — hai tab: "Trang bị" và "Hành trang"
        // ==============================================================
        //
        // Toàn bộ phần vẽ và bắt chạm của hai tab này viết lại từ đầu, không
        // dùng lại mã lưới cũ. Bản cũ trộn ba đời thiết kế vào một hàm (danh
        // sách dọc, lưới chia trang, hai cột dọc) nên mỗi lần sửa một chỗ là
        // lệch một chỗ khác.
        //
        // Hai điểm khác hẳn bản cũ:
        //
        //  * Tab "Hành trang" CUỘN DỌC, không chia trang. Chia trang thì phải
        //    giữ đồng bộ ba con số (số ô mỗi trang khi vẽ, khi tính tab, khi
        //    tính vị trí đầu trang) — lệch một cái là bấm tab không ăn.
        //  * Số lượng vật phẩm viết chữ ĐEN có nền sáng, không phải chữ vàng
        //    trên nền vàng.

        /// <summary>Số cột ô của tab "Hành trang".</summary>
        private const int TUI_SO_COT = 6;

        /// <summary>Số ô dọc của cột trang bị bên trái.</summary>
        private const int TB_SO_HANG = 5;

        /// <summary>Số hàng ô tab "Trang bị" chiếm: 5 hàng cột + 1 hàng dưới.</summary>
        private const int TB_SO_HANG_VE = TB_SO_HANG + 1;

        /// <summary>Cỡ ô lớn nhất của tab "Trang bị".</summary>
        /// <remarks>
        /// Không có chặn trên thì trên màn hình cao, ô phình tới bằng một phần ba
        /// bề ngang panel: ảnh vật phẩm 24 điểm nằm lọt thỏm giữa khung trống.
        /// </remarks>
        private const int TB_O_TOI_DA = 52;

        // So do o trang bi. Chi so trong ba mang duoi la CHI SO O CUA MAY CHU,
        // khong phai vi tri tren man hinh:
        //
        //   0 Ao   1 Quan   2 Gang   3 Giay   4 Rada   5 Cai trang
        //   6 Giap luyen tap (item type 32)   7 Pet   8 Deo lung (co)
        //   9 Van bay (type 24)   10 Sach (type 35)   11 Chan menh (type 39)
        //
        // Thu tu do MAY CHU dat, khong phai cho nay: InventoryService ghi
        // itemsBody.set(template.type == 32 ? 6 : template.type, item).
        //
        // Muon doi cach xep tren man hinh thi sua ba mang duoi. KHONG doi thu tu
        // TEN_O_TRANG_BI — ten gan theo chi so o, doi la ten lech sang o khac.

        /// <summary>Cột trái, từ trên xuống.</summary>
        private static readonly int[] TB_COT_TRAI = { 0, 1, 2, 3, 4 };

        /// <summary>Cột phải, từ trên xuống.</summary>
        private static readonly int[] TB_COT_PHAI = { 5, 6, 8, 9, 7 };

        /// <summary>Hàng dưới, từ trái sang. Số -1 là khoảng trống.</summary>
        /// <remarks>
        /// Giu do dai BA de con canh giua nhu cu: hai o Sach va Chan menh nam hai
        /// dau, o giua de trong. Cat xuong hai phan tu thi hai o dinh sat nhau va
        /// lech khoi truc giua cua nhan vat.
        /// </remarks>
        private static readonly int[] TB_HANG_DUOI = { 10, -1, 11 };

        /// <summary>Hai tab: 0 Trang bị, 1 Hành trang.</summary>
        private static readonly string[] TEN_TAB_TUI = { "Trang bị", "Hành trang" };

        /// <summary>Bề cao dải tab.</summary>
        private const int CAO_DAI_TAB = 20;

        /// <summary>
        /// Tên từng ô trang bị, hiện khi ô còn trống.
        /// </summary>
        /// <remarks>
        /// Năm ô đầu đã có ảnh mờ sẵn trong <c>screenTab6</c> nên để trống. Các ô
        /// sau ảnh mờ khó đoán, nên ghi thẳng tên.
        ///
        /// Nếu một tên nằm sai ô thì chỉ cần đổi thứ tự trong mảng này — thứ tự ô
        /// do máy chủ quyết định, không phải do đây.
        /// </remarks>
        private static readonly string[] TEN_O_TRANG_BI = {
            "", "", "", "", "",
            "Cải trang", "Giáp luyện tập", "Pet", "Đeo lưng",
            "Ván bay", "Sách", "Chân mệnh"
        };

        /// <summary>
        /// Bề rộng một ô của tab "Trang bị".
        /// </summary>
        /// <remarks>
        /// Tab này chỉ có hai cột ô ở hai mép và ba ô hàng dưới, nên bề ngang
        /// không phải chia sáu như tab túi — ô to lên được đáng kể. Bó hẹp là bề
        /// cao: sáu hàng phải nằm gọn trong vùng cuộn.
        /// </remarks>
        private int oTrangBi()
        {
            int theoNgang = (wScroll - 6) / TB_HANG_DUOI.Length;
            int theoDoc = (hScroll - CAO_DAI_TAB - 4) / TB_SO_HANG_VE;
            int o = Math.min(theoNgang, theoDoc);
            if (o > TB_O_TOI_DA)
            {
                o = TB_O_TOI_DA;
            }
            return (o < 18) ? 18 : o;
        }

        /// <summary>Bề rộng một ô của tab "Hành trang".</summary>
        private int oTui()
        {
            int o = (wScroll - 2) / TUI_SO_COT;
            return (o < 16) ? 16 : o;
        }

        /// <summary>Đỉnh vùng ô, ngay dưới dải tab.</summary>
        private int yOTui()
        {
            return yScroll + CAO_DAI_TAB + 2;
        }

        /// <summary>Vẽ cả hai tab.</summary>
        private void veTuiMoi(mGraphics g)
        {
            veDaiTabTui(g);
            if (newSelected == 0)
            {
                veTabTrangBi(g);
            }
            else
            {
                veTabHanhTrang(g);
            }
        }

        private void veDaiTabTui(mGraphics g)
        {
            int rong = wScroll / TEN_TAB_TUI.Length;
            for (int i = 0; i < TEN_TAB_TUI.Length; i++)
            {
                int x = xScroll + i * rong;
                bool dangXem = (i == newSelected);
                g.setColor(dangXem ? 16383818 : 15723751);
                g.fillRect(x, yScroll, rong - 1, CAO_DAI_TAB);
                if (dangXem)
                {
                    g.setColor(13524492);
                    g.fillRect(x, yScroll + CAO_DAI_TAB - 3, rong - 1, 3);
                }
                mFont mf = dangXem ? mFont.tahoma_7b_dark : mFont.tahoma_7_grey;
                mf.drawString(g, TEN_TAB_TUI[i], x + rong / 2,
                        yScroll + 5, mFont.CENTER);
            }
        }

        /// <summary>Tab 1: đồ đang mặc, nhân vật ở giữa.</summary>
        private void veTabTrangBi(mGraphics g)
        {
            Item[] mac = Char.myCharz().arrItemBody;
            int o = oTrangBi();
            nTableItem = mac.Length;

            // Nhan vat ve TRUOC cac o: ve sau thi anh nhan vat de len khung o hai
            // ben, o dang chon mat vien vang va nhin nhu khong chon duoc.
            Char.myCharz().paintCharBody(g,
                    xScroll + wScroll / 2,
                    yOTui() + TB_SO_HANG * o * 4 / 5, 1, 0, true);

            for (int m = 0; m < mac.Length; m++)
            {
                int[] xy = viTriOTrangBi(m, o);
                if (xy == null)
                {
                    continue;
                }
                veMotO(g, mac[m], xy[0], xy[1], o, m == sellectInventory,
                        m < TEN_O_TRANG_BI.Length ? TEN_O_TRANG_BI[m] : "", m);
            }
        }

        /// <summary>
        /// Toạ độ ô trang bị thứ <paramref name="m"/>.
        /// </summary>
        /// <remarks>
        /// Cột trái giữ năm ô đầu, cột phải năm ô tiếp, còn lại xuống hàng dưới.
        /// Dùng chung với <see cref="oTrangBiTaiDiem"/> để vùng vẽ và vùng bấm
        /// không bao giờ lệch nhau.
        /// </remarks>
        private int[] viTriOTrangBi(int m, int o)
        {
            for (int i = 0; i < TB_COT_TRAI.Length; i++)
            {
                if (TB_COT_TRAI[i] == m)
                {
                    return new int[] { xScroll + 2, yOTui() + i * o };
                }
            }
            for (int i = 0; i < TB_COT_PHAI.Length; i++)
            {
                if (TB_COT_PHAI[i] == m)
                {
                    return new int[] { xScroll + wScroll - o - 2,
                        yOTui() + i * o };
                }
            }
            for (int i = 0; i < TB_HANG_DUOI.Length; i++)
            {
                if (TB_HANG_DUOI[i] == m)
                {
                    int xDau = xScroll
                            + (wScroll - TB_HANG_DUOI.Length * o) / 2;
                    return new int[] { xDau + i * o,
                        yOTui() + TB_SO_HANG * o };
                }
            }
            // O may chu them ma so do chua biet den. Bo qua chu khong doan cho
            // dat: dat bua thi no de len mot o that, bam vao ra sai mon.
            return null;
        }

        /// <summary>
        /// Chọn ô ở chỗ ngón tay đang chạm, và nạp luôn vật phẩm của ô đó.
        /// </summary>
        /// <remarks>
        /// Nạp <c>itemInvenNew</c> ngay tại đây chứ không để phần vẽ nạp: nhả ngón
        /// xảy ra trước khung hình kế tiếp, nên lúc kiểm "itemInvenNew != null"
        /// thì nó còn là món của ô cũ.
        /// </remarks>
        private bool chonOTaiDiem()
        {
            int oChon = (newSelected == 0)
                    ? oTrangBiTaiDiem(GameCanvas.px, GameCanvas.py)
                    : oTuiTaiDiem(GameCanvas.px, GameCanvas.py);
            if (oChon < 0)
            {
                // Bam ra ngoai o thi bo mon dang giu, khong de lai mon cu: giu
                // lai la bam vao cho trong van bat ra menu cua mon vua xem.
                itemInvenNew = null;
                return false;
            }
            sellectInventory = oChon;
            itemInvenNew = oDangChon(oChon);
            return true;
        }

        /// <summary>Vật phẩm ở ô đang chọn của tab đang xem, hoặc null.</summary>
        /// <summary>Món vừa bấm "Giao dịch", chờ nhập số lượng.</summary>
        /// <remarks>Xác nhận số lượng bằng chính món này. Bản cũ đọc lại
        /// <c>currItem</c> — biến ấy đổi theo ô đang chọn, nên một lần chạm lọt
        /// xuống túi đồ trong lúc gõ số là xác nhận nhầm sang món khác.</remarks>
        private Item itemChoGiaoDich;

        private Item oDangChon(int chiSo)
        {
            Item[] ds = (newSelected == 0)
                    ? Char.myCharz().arrItemBody : Char.myCharz().arrItemBag;
            return (chiSo >= 0 && chiSo < ds.Length) ? ds[chiSo] : null;
        }

        /// <summary>Ô trang bị nào ở điểm này, hoặc -1.</summary>
        private int oTrangBiTaiDiem(int px, int py)
        {
            int o = oTrangBi();
            int soO = Char.myCharz().arrItemBody.Length;
            for (int m = 0; m < soO; m++)
            {
                int[] xy = viTriOTrangBi(m, o);
                if (xy == null)
                {
                    continue;
                }
                if (px >= xy[0] && px < xy[0] + o
                        && py >= xy[1] && py < xy[1] + o)
                {
                    return m;
                }
            }
            return -1;
        }

        /// <summary>Tab 2: túi đồ, cuộn dọc.</summary>
        private void veTabHanhTrang(mGraphics g)
        {
            Item[] tui = Char.myCharz().arrItemBag;
            int o = oTui();
            nTableItem = tui.Length;

            g.setClip(xScroll, yOTui(), wScroll, yScroll + hScroll - yOTui());
            for (int i = 0; i < tui.Length; i++)
            {
                int x = xScroll + (i % TUI_SO_COT) * o;
                int y = yOTui() + (i / TUI_SO_COT) * o - cmy;
                // Bo qua o nam ngoai vung thay: ve het ca ngan o moi khung hinh
                // la tut khung hinh han.
                if (y + o < yOTui() || y > yScroll + hScroll)
                {
                    continue;
                }
                veMotO(g, tui[i], x, y, o, i == sellectInventory, "", -1);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        }

        /// <summary>Ô túi nào ở điểm này, hoặc -1.</summary>
        private int oTuiTaiDiem(int px, int py)
        {
            int o = oTui();
            int cot = (px - xScroll) / o;
            int hang = (py + cmy - yOTui()) / o;
            if (cot < 0 || cot >= TUI_SO_COT || hang < 0 || py < yOTui())
            {
                return -1;
            }
            int i = hang * TUI_SO_COT + cot;
            return (i < Char.myCharz().arrItemBag.Length) ? i : -1;
        }

        /// <summary>Tên ô trống, ngắt hai dòng nếu dài hơn ô.</summary>
        /// <remarks>
        /// Vẽ một dòng thì "Giáp luyện tập" tràn khỏi khung, đè lên ô bên cạnh.
        /// </remarks>
        private void veTenO(mGraphics g, string ten, int x, int y, int o)
        {
            int giua = x + o / 2;
            int cat = ten.LastIndexOf(' ');
            if (cat < 0 || mFont.tahoma_7_grey.getWidth(ten) <= o - 4)
            {
                mFont.tahoma_7_grey.drawString(g, ten, giua, y + o / 2 - 5,
                        mFont.CENTER);
                return;
            }
            mFont.tahoma_7_grey.drawString(g, ten.Substring(0, cat), giua,
                    y + o / 2 - 11, mFont.CENTER);
            mFont.tahoma_7_grey.drawString(g, ten.Substring(cat + 1), giua,
                    y + o / 2 + 1, mFont.CENTER);
        }

        /// <summary>Bao nhiêu điểm cuộn được ở tab "Hành trang".</summary>
        private int gioiHanCuonTui()
        {
            if (newSelected == 0)
            {
                // Tab "Trang bi" xep vua khung, khong co gi de cuon. Tra ve so
                // duong thi keo len duoc va ca bo o troi ra khoi vung ve.
                return 0;
            }
            int o = oTui();
            int soHang = (Char.myCharz().arrItemBag.Length + TUI_SO_COT - 1)
                    / TUI_SO_COT;
            int caoNoiDung = soHang * o;
            int caoVung = yScroll + hScroll - yOTui();
            int gh = caoNoiDung - caoVung;
            return (gh < 0) ? 0 : gh;
        }

        /// <summary>
        /// Vẽ một ô: khung, ảnh vật phẩm, số lượng.
        /// </summary>
        /// <param name="ten">Tên ô, chỉ hiện khi ô trống.</param>
        /// <param name="oMac">Số thứ tự ô trang bị để lấy ảnh mờ, hoặc -1.</param>
        private void veMotO(mGraphics g, Item it, int x, int y, int o,
                bool dangChon, string ten, int oMac)
        {
            g.setColor(15196114);
            g.drawRect(x, y, o, o);
            g.setColor(dangChon ? 16383818 : 9993045);
            g.fillRect(x + 2, y + 2, o - 3, o - 3);

            if (it == null || it.template == null)
            {
                if (oMac >= 0 && (ten == null || ten.Length == 0))
                {
                    screenTab6.drawFrame(oMac, x + o / 2 - 8, y + o / 2 - 8, 0,
                            mGraphics.TOP | mGraphics.LEFT, g);
                }
                else if (ten != null && ten.Length > 0)
                {
                    veTenO(g, ten, x, y, o);
                }
                return;
            }
            SmallImage.drawSmallImage(g, it.template.iconID, x + o / 2,
                    y + o / 2, 0, 3);
            if (it.quantity > 1)
            {
                // Chu DEN tren nen sang: chu vang tren nen vang cua o thi doc
                // khong ra, nhat la voi so nhieu chu so.
                string so = string.Empty + it.quantity;
                int rongChu = mFont.tahoma_7b_dark.getWidth(so);
                g.setColor(16777215, 0.75f);
                g.fillRect(x + o - rongChu - 4, y + o - 11, rongChu + 3, 10);
                mFont.tahoma_7b_dark.drawString(g, so, x + o - 2, y + o - 10,
                        mFont.RIGHT);
            }
        }

        private void paintInventory(mGraphics g)
        {
            bool flag = true;
            if (flag && isnewInventory)
            {
                veTuiMoi(g);
            }
            if (flag && isnewInventory)
            {
                return;
            }
            g.setColor(16711680);
            Item[] arrItemBody2 = Char.myCharz().arrItemBody;
            Item[] arrItemBag2 = Char.myCharz().arrItemBag;
            currentListLength = checkCurrentListLength(arrItemBody2.Length + arrItemBag2.Length);
            int num18 = (arrItemBody2.Length + arrItemBag2.Length) / 20 + (((arrItemBody2.Length + arrItemBag2.Length) % 20 > 0) ? 1 : 0);
            TAB_W_NEW = wScroll / num18;
            for (int num19 = 0; num19 < num18; num19++)
            {
                int num20 = ((num19 == newSelected && selected == 0) ? ((GameCanvas.gameTick % 10 < 7) ? (-1) : 0) : 0);
                g.setColor((num19 != newSelected) ? 15723751 : 16383818);
                g.fillRect(xScroll + num19 * TAB_W_NEW, 89 + num20 - 10, TAB_W_NEW - 1, 21);
                if (num19 == newSelected)
                {
                    g.setColor(13524492);
                    int x3 = xScroll + num19 * TAB_W_NEW;
                    int num21 = 89 + num20 - 10 + 21;
                    g.fillRect(x3, num21 - 3, TAB_W_NEW - 1, 3);
                }
                mFont.tahoma_7_grey.drawString(g, string.Empty + (num19 + 1), xScroll + num19 * TAB_W_NEW + TAB_W_NEW / 2, 91 + num20 - 10, mFont.CENTER);
            }
            g.setClip(xScroll, yScroll + 21, wScroll, hScroll - 21);
            g.translate(0, -cmy);
            try
            {
                for (int num22 = 1; num22 < currentListLength; num22++)
                {
                    int num23 = xScroll + 36;
                    int num24 = yScroll + num22 * ITEM_HEIGHT;
                    int num25 = wScroll - 36;
                    int h2 = ITEM_HEIGHT - 1;
                    int num26 = xScroll;
                    int num27 = yScroll + num22 * ITEM_HEIGHT;
                    int num28 = 34;
                    int num29 = ITEM_HEIGHT - 1;
                    if (num24 - cmy > yScroll + hScroll || num24 - cmy < yScroll - ITEM_HEIGHT)
                    {
                        continue;
                    }
                    bool inventorySelect_isbody = GetInventorySelect_isbody(num22, newSelected, Char.myCharz().arrItemBody);
                    int inventorySelect_body = GetInventorySelect_body(num22, newSelected);
                    int inventorySelect_bag = GetInventorySelect_bag(num22, newSelected, Char.myCharz().arrItemBody);
                    g.setColor((num22 == selected) ? 16383818 : ((!inventorySelect_isbody) ? 15723751 : 15196114));
                    g.fillRect(num23, num24, num25, h2);
                    g.setColor((num22 == selected) ? 9541120 : ((!inventorySelect_isbody) ? 11837316 : 9993045));
                    Item item3 = ((!inventorySelect_isbody) ? arrItemBag2[inventorySelect_bag] : arrItemBody2[inventorySelect_body]);
                    if (item3 != null)
                    {
                        for (int num30 = 0; num30 < item3.itemOption.Length; num30++)
                        {
                            if (item3.itemOption[num30].optionTemplate.id == 72 && item3.itemOption[num30].param > 0)
                            {
                                byte id = (byte)GetColor_Item_Upgrade(item3.itemOption[num30].param);
                                int color_ItemBg = GetColor_ItemBg(id);
                                if (color_ItemBg != -1)
                                {
                                    g.setColor((num22 != selected) ? GetColor_ItemBg(id) : GetColor_ItemBg(id));
                                }
                            }
                        }
                    }
                    g.fillRect(num26, num27, num28, num29);
                    if (item3 != null && item3.isSelect && GameCanvas.panel.type == 12)
                    {
                        g.setColor((num22 != selected) ? 6047789 : 7040779);
                        g.fillRect(num26, num27, num28, num29);
                    }
                    if (item3 == null)
                    {
                        continue;
                    }
                    string text3 = string.Empty;
                    mFont mFont4 = mFont.tahoma_7_green2;
                    if (item3.itemOption != null)
                    {
                        for (int num31 = 0; num31 < item3.itemOption.Length; num31++)
                        {
                            if (item3.itemOption[num31].optionTemplate.id == 72)
                            {
                                text3 = " [+" + item3.itemOption[num31].param + "]";
                            }
                            if (item3.itemOption[num31].optionTemplate.id == 107)
                            {
                                paintItemStar(g, item3.itemOption[num31].param.ToString(), num23 + 125, num24 + 1);
                            }
                            if (item3.itemOption[num31].optionTemplate.id == 41)
                            {
                                if (item3.itemOption[num31].param == 1)
                                {
                                    mFont4 = GetFont(0);
                                }
                                else if (item3.itemOption[num31].param == 2)
                                {
                                    mFont4 = GetFont(2);
                                }
                                else if (item3.itemOption[num31].param == 3)
                                {
                                    mFont4 = GetFont(8);
                                }
                                else if (item3.itemOption[num31].param == 4)
                                {
                                    mFont4 = GetFont(7);
                                }
                            }
                        }
                    }
                    mFont4.drawString(g, item3.template.name + text3, num23 + 5, num24 + 1, 0);
                    string text4 = string.Empty;
                    if (item3.itemOption != null)
                    {
                        if (item3.itemOption.Length > 0 && item3.itemOption[0] != null && item3.itemOption[0].optionTemplate.id != 102 && item3.itemOption[0].optionTemplate.id != 107)
                        {
                            text4 += item3.itemOption[0].getOptionString();
                        }
                        mFont mFont5 = mFont.tahoma_7_blue;
                        if (item3.compare < 0 && item3.template.type != 5)
                        {
                            mFont5 = mFont.tahoma_7_red;
                        }
                        if (item3.itemOption.Length > 1)
                        {
                            for (int num32 = 1; num32 < 2; num32++)
                            {
                                if (item3.itemOption[num32] != null && item3.itemOption[num32].optionTemplate.id != 102 && item3.itemOption[num32].optionTemplate.id != 107)
                                {
                                    text4 = text4 + "," + item3.itemOption[num32].getOptionString();
                                }
                            }
                        }
                        mFont5.drawString(g, text4, num23 + 5, num24 + 11, mFont.LEFT);
                    }
                    SmallImage.drawSmallImage(g, item3.template.iconID, num26 + num28 / 2, num27 + num29 / 2, 0, 3);
                    if (item3.itemOption != null)
                    {
                        for (int num33 = 0; num33 < item3.itemOption.Length; num33++)
                        {
                            paintOptItem(g, item3.itemOption[num33].optionTemplate.id, item3.itemOption[num33].param, num26, num27, num28, num29);
                        }
                        for (int num34 = 0; num34 < item3.itemOption.Length; num34++)
                        {
                            paintOptSlotItem(g, item3.itemOption[num34].optionTemplate.id, item3.itemOption[num34].param, num26, num27, num28, num29);
                        }
                    }
                    if (item3.quantity > 1)
                    {
                        mFont.tahoma_7_yellow.drawString(g, string.Empty + item3.quantity, num26 + num28, num27 + num29 - mFont.tahoma_7_yellow.getHeight(), 1);
                    }
                }
            }
            catch (Exception)
            {
            }
            paintScrollArrow(g);
        }
    
        private void paintTab(mGraphics g)
        {
           
            if (type == 23 || type == 24)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.gameInfo, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 20)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.account, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 22)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.autoFunction, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 19)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.option, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 18)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.change_flag, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 13 && Equals(GameCanvas.panel2))
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.item_receive2, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 12 && GameCanvas.panel2 != null)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.UPGRADE, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 11)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.friend, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 16)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.enemy, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 15)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, topName, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 2 && GameCanvas.panel2 != null)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.chest, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 9)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.achievement_mission, xScroll + wScroll / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 3)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.select_zone, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 14)
            {
                veVachDuoiTab(g);
                mFont.tahoma_7b_dark.drawString(g, mResources.select_map, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                return;
            }
            if (type == 4)
            {
                mFont.tahoma_7b_dark.drawString(g, mResources.map, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                veVachDuoiTab(g);
                return;
            }
            if (type == 7)
            {
                mFont.tahoma_7b_dark.drawString(g, mResources.trangbi, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                veVachDuoiTab(g);
                return;
            }
            if (type == 17)
            {
                mFont.tahoma_7b_dark.drawString(g, mResources.kigui, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                veVachDuoiTab(g);
                return;
            }
            if (type == 8)
            {
                mFont.tahoma_7b_dark.drawString(g, mResources.msg, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                veVachDuoiTab(g);
                return;
            }
            if (type == 10)
            {
                mFont.tahoma_7b_dark.drawString(g, mResources.wat_do_u_want, startTabPos + TAB_W / 2, 59, mFont.CENTER);
                veVachDuoiTab(g);
                return;
            }
            if (currentTabIndex == 3 && mainTabName.Length != 4)
            {
                g.translate(-cmx, 0);
            }
            for (int i = 0; i < currentTabName.Length; i++)
            {
                veTheTab(g, startTabPos + i * TAB_W, TAB_W - 1,
                        i == currentTabIndex);
                if (i == keyTouchTab)
                {
                    g.drawImage(ItemMap.imageFlare, startTabPos + i * TAB_W + TAB_W / 2, 62, 3);
                }
                // Chu nau dam tren nen cam cua the dang chon; the con lai chu xam.
                mFont mFont2 = (i == currentTabIndex)
                        ? mFont.tahoma_7b_dark : mFont.tahoma_7_grey;
                if (!currentTabName[i][1].Equals(string.Empty))
                {
                    mFont2.drawString(g, currentTabName[i][0], startTabPos + i * TAB_W + TAB_W / 2, 53, mFont.CENTER);
                    mFont2.drawString(g, currentTabName[i][1], startTabPos + i * TAB_W + TAB_W / 2, 64, mFont.CENTER);
                }
                else
                {
                    mFont2.drawString(g, currentTabName[i][0], startTabPos + i * TAB_W + TAB_W / 2, 59, mFont.CENTER);
                }
                if (type == 0 && currentTabName.Length == 5 && GameScr.isNewClanMessage && GameCanvas.gameTick % 4 == 0)
                {
                    g.drawImage(ItemMap.imageFlare, startTabPos + 3 * TAB_W + TAB_W / 2, 77, mGraphics.BOTTOM | mGraphics.HCENTER);
                }
            }
            veVachDuoiTab(g);
        }
    
        private void paintBottomMoneyInfo(mGraphics g)
        {
            if (type != 13 || (currentTabIndex != 2 && !Equals(GameCanvas.panel2)))
            {
                g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
                veDaiTien(g);
                g.drawImage(imgXu, X + 11, H - 7, 3);
                g.drawImage(imgLuong, X + 75, H - 8, 3);
                mFont.tahoma_7_yellow.drawString(g, Char.myCharz().xuStr + string.Empty, X + 24, H - 13, mFont.LEFT, mFont.tahoma_7_grey);
                mFont.tahoma_7_yellow.drawString(g, Char.myCharz().luongStr + string.Empty, X + 85, H - 13, mFont.LEFT, mFont.tahoma_7_grey);
                g.drawImage(imgLuongKhoa, X + 130, H - 8, 3);
                mFont.tahoma_7_yellow.drawString(g, Char.myCharz().luongKhoaStr + string.Empty, X + 140, H - 13, mFont.LEFT, mFont.tahoma_7_grey);
            }
        }
    
        private void paintClanInfo(mGraphics g)
        {
            if (Char.myCharz().clan == null)
            {
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), 25, 50, 0, 33);
                mFont.tahoma_7b_white.drawString(g, mResources.not_join_clan, (wScroll - 50) / 2 + 50, 20, mFont.CENTER);
            }
            else if (!isViewMember)
            {
                Clan clan = Char.myCharz().clan;
                if (clan != null)
                {
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), 25, 50, 0, 33);
                    mFont.tahoma_7b_white.drawString(g, clan.name, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
                    mFont.tahoma_7_yellow.drawString(g, mResources.achievement_point + ": " + clan.powerPoint, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
                    mFont.tahoma_7_yellow.drawString(g, mResources.clan_point + ": " + clan.clanPoint, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
                    mFont.tahoma_7_yellow.drawString(g, mResources.level + ": " + clan.level, 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
                    TextInfo.paint(g, clan.slogan, 60, 38, wScroll - 70, ITEM_HEIGHT, mFont.tahoma_7_yellow);
                }
            }
            else
            {
                Clan clan2 = ((currClan == null) ? Char.myCharz().clan : currClan);
                SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), 25, 50, 0, 33);
                mFont.tahoma_7b_white.drawString(g, clan2.name, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
                mFont.tahoma_7_yellow.drawString(g, mResources.member + ": " + clan2.currMember + "/" + clan2.maxMember, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
                mFont.tahoma_7_yellow.drawString(g, mResources.clan_leader + ": " + clan2.leaderName, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
                TextInfo.paint(g, clan2.slogan, 60, 38, wScroll - 70, ITEM_HEIGHT, mFont.tahoma_7_yellow);
            }
        }
    
        private void paintToolInfo(mGraphics g)
        {
            mFont.tahoma_7b_white.drawString(g, mResources.dragon_ball + " " + GameMidlet.VERSION, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
            mFont.tahoma_7_yellow.drawString(g, mResources.character + ": " + Char.myCharz().cName, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
            string text = ((!GameCanvas.loginScr.tfUser.getText().Equals(string.Empty)) ? GameCanvas.loginScr.tfUser.getText() : mResources.not_register_yet);
            mFont.tahoma_7_yellow.drawString(g, mResources.account_server + " " + ServerListScreen.nameServer[ServerListScreen.ipSelect] + ": " + text, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintGiaoDichInfo(mGraphics g)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.select_item, 60, 4, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.lock_trade, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.wait_opp_lock_trade, 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.press_done, 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintMyInfo(mGraphics g)
        {
            paintCharInfo(g, Char.myCharz());
        }
    
        private void paintPetInfo(mGraphics g)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.power + ": " + NinjaUtil.getMoneys(Char.myPetz().cPower), X + 60, 4, mFont.LEFT, mFont.tahoma_7_grey);
            if (Char.myPetz().cPower > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, (!Char.myPetz().me) ? Char.myPetz().currStrLevel : Char.myPetz().getStrLevel(), X + 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
            }
            if (Char.myPetz().cDamFull > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, mResources.hit_point + " :" + NinjaUtil.getMoneys(Char.myPetz().cDamFull), X + 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
            }
            if (Char.myPetz().cMaxStamina > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, mResources.vitality, X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
                g.drawImage(GameScr.imgMPLost, X + 100, 41, 0);
                int num = Char.myPetz().cStamina * mGraphics.getImageWidth(GameScr.imgMP) / Char.myPetz().cMaxStamina;
                g.setClip(100, X + 41, num, 20);
                g.drawImage(GameScr.imgMP, X + 100, 41, 0);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
        }
    
        private void paintCharInfo(mGraphics g, Char c)
        {
            mFont.tahoma_7b_white.drawString(g, ((GameScr.isNewMember == 1) ? "       " : string.Empty) + c.cName, X + 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
            if (GameScr.isNewMember == 1)
            {
                SmallImage.drawSmallImage(g, 5427, X + 55, 4, 0, 0);
            }
            if (c.cMaxStamina > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, mResources.vitality, X + 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
                g.drawImage(GameScr.imgMPLost, X + 95, 19, 0);
                int num = c.cStamina * mGraphics.getImageWidth(GameScr.imgMP) / c.cMaxStamina;
                g.setClip(95, X + 19, num, 20);
                g.drawImage(GameScr.imgMP, X + 95, 19, 0);
            }
            g.setClip(0, 0, GameCanvas.w, GameCanvas.h);
            if (c.cPower > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, (!c.me) ? c.currStrLevel : c.getStrLevel(), X + 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
            }
            mFont.tahoma_7_yellow.drawString(g, mResources.power + ": " + NinjaUtil.getMoneys(c.cPower), X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintCharInfo(mGraphics g, Char c, int x, int y)
        {
            mFont.tahoma_7b_white.drawString(g, ((GameScr.isNewMember == 1) ? "       " : string.Empty) + c.cName, x + 60, y + 4, mFont.LEFT, mFont.tahoma_7b_dark);
            if (GameScr.isNewMember == 1)
            {
                SmallImage.drawSmallImage(g, 5427, x + 55, y + 4, 0, 0);
            }
            if (c.cMaxStamina > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, mResources.vitality, x + 60, y + 16, mFont.LEFT, mFont.tahoma_7_grey);
                g.drawImage(GameScr.imgMPLost, x + 95, y + 19, 0);
                int num = c.cStamina * mGraphics.getImageWidth(GameScr.imgMP) / c.cMaxStamina;
                g.drawImage(GameScr.imgMP, x + 95, y + 19, 0);
            }
            if (c.cPower > 0)
            {
                mFont.tahoma_7_yellow.drawString(g, (!c.me) ? c.currStrLevel : c.getStrLevel(), x + 60, y + 27, mFont.LEFT, mFont.tahoma_7_grey);
            }
            mFont.tahoma_7_yellow.drawString(g, mResources.power + ": " + NinjaUtil.getMoneys(c.cPower), x + 60, y + 38, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintZoneInfo(mGraphics g)
        {
            mFont.tahoma_7b_white.drawString(g, mResources.zone + " " + TileMap.zoneID, 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
            mFont.tahoma_7_yellow.drawString(g, TileMap.mapName, 60, 16, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7b_white.drawString(g, TileMap.zoneID + string.Empty, 25, 27, mFont.CENTER);
        }
    
        public int getCompare(Item item)
        {
            if (item == null)
            {
                return -1;
            }
            if (item.isTypeBody())
            {
                if (item.itemOption == null)
                {
                    return -1;
                }
                ItemOption itemOption = item.itemOption[0];
                if (itemOption.optionTemplate.id == 22)
                {
                    itemOption.optionTemplate = GameScr.gI().iOptionTemplates[6];
                    itemOption.param *= 1000;
                }
                if (itemOption.optionTemplate.id == 23)
                {
                    itemOption.optionTemplate = GameScr.gI().iOptionTemplates[7];
                    itemOption.param *= 1000;
                }
                Item item2 = null;
                for (int i = 0; i < Char.myCharz().arrItemBody.Length; i++)
                {
                    Item item3 = Char.myCharz().arrItemBody[i];
                    if (itemOption.optionTemplate.id == 22)
                    {
                        itemOption.optionTemplate = GameScr.gI().iOptionTemplates[6];
                        itemOption.param *= 1000;
                    }
                    if (itemOption.optionTemplate.id == 23)
                    {
                        itemOption.optionTemplate = GameScr.gI().iOptionTemplates[7];
                        itemOption.param *= 1000;
                    }
                    if (item3 != null && item3.itemOption != null && item3.template.type == item.template.type)
                    {
                        item2 = item3;
                        break;
                    }
                }
                if (item2 == null)
                {
                    isUp = true;
                    return itemOption.param;
                }
                int num = 0;
                num = ((item2 == null || item2.itemOption == null) ? itemOption.param : (itemOption.param - item2.itemOption[0].param));
                if (num < 0)
                {
                    isUp = false;
                }
                else
                {
                    isUp = true;
                }
                return num;
            }
            return 0;
        }
    
        private void paintMapInfo(mGraphics g)
        {
            mFont.tahoma_7b_white.drawString(g, mResources.MENUGENDER[TileMap.planetID], 60, 4, mFont.LEFT);
            string text = string.Empty;
            if (TileMap.mapID >= 135 && TileMap.mapID <= 138)
            {
                text = " " + mResources.tang + TileMap.zoneID;
            }
            mFont.tahoma_7_yellow.drawString(g, TileMap.mapName + text, 60, 16, mFont.LEFT);
            mFont.tahoma_7b_white.drawString(g, mResources.quest_place + ": ", 60, 27, mFont.LEFT);
            if (GameScr.getTaskMapId() >= 0 && GameScr.getTaskMapId() <= TileMap.mapNames.Length - 1)
            {
                mFont.tahoma_7_yellow.drawString(g, TileMap.mapNames[GameScr.getTaskMapId()], 60, 38, mFont.LEFT);
            }
            else
            {
                mFont.tahoma_7_yellow.drawString(g, mResources.random, 60, 38, mFont.LEFT);
            }
        }
    
        private void paintShopInfo(mGraphics g)
        {
            if (currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null)
            {
                paintMyInfo(g);
            }
            else if (selected < 0)
            {
                if (typeShop != 2)
                {
                    mFont.tahoma_7_white.drawString(g, mResources.say_hello, X + 60, 14, 0);
                    mFont.tahoma_7_white.drawString(g, strWantToBuy, X + 60, 26, 0);
                    return;
                }
                mFont.tahoma_7_white.drawString(g, mResources.say_hello, X + 60, 5, 0);
                mFont.tahoma_7_white.drawString(g, strWantToBuy, X + 60, 17, 0);
                mFont.tahoma_7_white.drawString(g, mResources.page + " " + (currPageShop[currentTabIndex] + 1) + "/" + maxPageShop[currentTabIndex], X + 60, 29, 0);
            }
            else
            {
                if (currentTabIndex < 0 || currentTabIndex > Char.myCharz().arrItemShop.Length - 1 || selected < 0 || selected > Char.myCharz().arrItemShop[currentTabIndex].Length - 1)
                {
                    return;
                }
                Item item = Char.myCharz().arrItemShop[currentTabIndex][selected];
                if (item != null)
                {
                    if (Equals(GameCanvas.panel) && currentTabIndex <= 3 && typeShop == 2)
                    {
                        mFont.tahoma_7b_white.drawString(g, mResources.page + " " + (currPageShop[currentTabIndex] + 1) + "/" + maxPageShop[currentTabIndex], X + 55, 4, 0);
                    }
                    mFont.tahoma_7b_white.drawString(g, item.template.name, X + 55, 24, 0);
                    string st = mResources.pow_request + " " + Res.formatNumber(item.template.strRequire);
                    if (item.template.strRequire > Char.myCharz().cPower)
                    {
                        mFont.tahoma_7_yellow.drawString(g, st, X + 55, 35, 0);
                    }
                    else
                    {
                        mFont.tahoma_7_green.drawString(g, st, X + 55, 35, 0);
                    }
                }
            }
        }
    
        private void paintItemBoxInfo(mGraphics g)
        {
            string st = mResources.used + ": " + hasUse + "/" + Char.myCharz().arrItemBox.Length + " " + mResources.place;
            mFont.tahoma_7b_white.drawString(g, mResources.chest, 60, 4, 0);
            mFont.tahoma_7_yellow.drawString(g, st, 60, 16, 0);
        }
    
        private void paintSkillInfo(mGraphics g)
        {
            mFont.tahoma_7_white.drawString(g, "Top " + Char.myCharz().rank, X + 45 + (W - 50) / 2, 2, mFont.CENTER);
            mFont.tahoma_7_yellow.drawString(g, mResources.potential_point, X + 45 + (W - 50) / 2, 14, mFont.CENTER);
            mFont.tahoma_7_white.drawString(g, string.Empty + NinjaUtil.getMoneys(Char.myCharz().cTiemNang), X + ((GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0) + 45 + (W - 50) / 2, 26, mFont.CENTER);
            mFont.tahoma_7_yellow.drawString(g, mResources.active_point + ": " + NinjaUtil.getMoneys(Char.myCharz().cNangdong), X + ((GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0) + 45 + (W - 50) / 2, 38, mFont.CENTER);
        }
    

        /// <summary>
        /// Tỉ lệ né đòn, cộng từ chỉ số 108 ("#% Né đòn") của đồ ĐANG MẶC.
        ///
        /// Máy chủ có sẵn tlNeDon nhưng KHÔNG gửi nó xuống client, và thêm một
        /// trường vào gói tin thì phải sửa protocol cả hai đầu — đúng loại thay
        /// đổi từng gây lỗi treo "Xin chờ". Client vốn đã nhận đủ chỉ số của đồ
        /// đang mặc nên tự cộng lấy là đủ, không phải đụng gói tin.
        ///
        /// Lưu ý: chỉ tính phần đến từ TRANG BỊ. Né cộng từ nguồn khác (ngọc
        /// rồng đen, nội tại...) máy chủ giữ riêng nên số này có thể thấp hơn né
        /// thật.
        /// </summary>
        private static int tinhNeDon()
        {
            int tong = 0;
            try
            {
                Item[] mac = Char.myCharz().arrItemBody;
                if (mac == null)
                {
                    return 0;
                }
                for (int i = 0; i < mac.Length; i++)
                {
                    if (mac[i] == null || mac[i].itemOption == null)
                    {
                        continue;
                    }
                    for (int j = 0; j < mac[i].itemOption.Length; j++)
                    {
                        ItemOption o = mac[i].itemOption[j];
                        if (o != null && o.optionTemplate != null
                                && o.optionTemplate.id == 108)
                        {
                            tong += o.param;
                        }
                    }
                }
            }
            catch (Exception)
            {
                // Đồ đang nạp dở -> trả 0 chứ không làm vỡ cả bảng chỉ số.
            }
            return tong;
        }
        private void paintItemBodyBagInfo(mGraphics g)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.HP + ": " + Char.myCharz().cHP + " / " + Char.myCharz().cHPFull, X + 60, 2, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.KI + ": " + Char.myCharz().cMP + " / " + Char.myCharz().cMPFull, X + 60, 14, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.hit_point + ": " + NinjaUtil.getMoneys(Char.myCharz().cDamFull), X + 60, 26, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.armor + ": " + Char.myCharz().cDefull + ", " + mResources.critical + ": " + Char.myCharz().cCriticalFull + "%", X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
            // Ne don: cong tu chi so 108 cua do dang mac. Xem tinhNeDon().
            mFont.tahoma_7_yellow.drawString(g, "Né: " + tinhNeDon() + "%", X + 60, 50, mFont.LEFT, mFont.tahoma_7_grey);
            veNutChiSo(g);
        }
    
        private void paintItemBodyBagInfo(mGraphics g, int x, int y)
        {
            mFont.tahoma_7_yellow.drawString(g, mResources.HP + ": " + Char.myCharz().cHP + " / " + Char.myCharz().cHPFull, x, y + 2, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.KI + ": " + Char.myCharz().cMP + " / " + Char.myCharz().cMPFull, x, y + 14, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.hit_point + ": " + NinjaUtil.getMoneys(Char.myCharz().cDamFull), x, y + 26, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.armor + ": " + Char.myCharz().cDefull + ", " + mResources.critical + ": " + Char.myCharz().cCriticalFull + "%", x, y + 38, mFont.LEFT, mFont.tahoma_7_grey);
        }

    	/// Nut "?" co dang duoc ve o khung hinh nay khong.
    	///
    	/// Chi tab Hanh Trang ve nut nay. Truoc day vung bam xet vo dieu kien
    	/// nen o cac tab khac van bam trung, mo ra bang chi so tu mot cho khong
    	/// he co nut — nguoi choi bam nham ma khong hieu vi sao.
    	private bool nutChiSoDangHien;

    	/// Nut "?" mo bang chi so day du.
    	///
    	/// Ve rieng mot ham va goi tu paintTopInfo de nut co mat o MOI tab.
    	/// Truoc day no nam trong paintItemBodyBagInfo — ham chi duoc goi o vai
    	/// nhanh tab, nen sang tab khac la nut bien mat du vung bam van song.
    	private void veNutChiSo(mGraphics g)
    	{
    		// Bat co ngay tai cho ve. Vung bam doc dung co nay, nen ve va bam
    		// khong the lech nhau: tab nao khong ve nut thi tab do khong bam duoc.
    		nutChiSoDangHien = true;
    		g.setColor(GameCanvas.panel.isShow ? 9541120 : 9993045);
    		g.fillRect(X + wScroll - 24, 24, 20, 14);
    		mFont.tahoma_7b_white.drawString(g, "?", X + wScroll - 14, 25, mFont.CENTER);
    	}
    
        // ==============================================================
        //  Khung bảng NPC — vẽ lại hẳn, không dùng khung cũ của game
        // ==============================================================

        /// <summary>Nền kem của cả bảng.</summary>
        private const int MAU_NEN_BANG = 0xF6E7CC;

        /// <summary>Kem sáng — vệt sáng, thẻ tab thường, nét ngăn dưới.</summary>
        private const int MAU_NEN_SANG = 0xFDF0DC;

        /// <summary>Nâu đậm của nét viền ngoài.</summary>
        private const int MAU_VIEN_BANG = 0x6B4A24;

        /// <summary>Nâu nhạt hơn, dùng cho nét viền phụ.</summary>
        private const int MAU_VIEN_MO = 0x8B623A;

        /// <summary>Cam của dải đầu bảng.</summary>
        private const int MAU_DAI_DAU = 0xE0A56A;

        /// <summary>Cam sáng của thẻ tab đang chọn.</summary>
        private const int MAU_THE_CHON = 0xF0A164;

        /// <summary>
        /// Khung của cả bảng: viền nâu đôi, bo góc, nền kem.
        /// </summary>
        /// <remarks>
        /// <para>Thay hẳn <c>paintFrameSimple</c> — khung cũ là hai hình chữ nhật
        /// góc vuông, một nét viền nâu và một mảng kem phẳng lì, đúng kiểu bảng
        /// của bản gốc.</para>
        ///
        /// <para>Ở đây là hai nét viền lồng nhau (đậm ngoài, nhạt trong) bo góc,
        /// cộng một vệt sáng dọc cạnh trên để khung có độ dày thay vì nhìn như
        /// một miếng giấy dán.</para>
        /// </remarks>
        private void veKhungPanel(mGraphics g)
        {
            g.setColor(MAU_VIEN_BANG, 1f);
            g.fillRect(X, Y, W, H, 9);
            g.setColor(MAU_VIEN_MO, 1f);
            g.fillRect(X + 1, Y + 1, W - 2, H - 2, 8);
            g.setColor(MAU_NEN_BANG, 1f);
            g.fillRect(X + 3, Y + 3, W - 6, H - 6, 6);
            g.setColor(MAU_NEN_SANG, 0.95f);
            g.fillRect(X + 4, Y + 4, W - 8, 3, 2);
        }

        /// <summary>
        /// Dải đầu bảng — chỗ đặt mặt NPC và lời thoại.
        /// </summary>
        /// <remarks>
        /// Nền cam bo hai góc trên theo khung, đáy thẳng và có hai nét ngăn: một
        /// nâu đậm, một vàng sáng. Bản cũ tô một mảng nâu vuông chằn chặn chạy
        /// hết bề ngang, không có ranh giới nào với phần dưới.
        /// </remarks>
        private void veDaiDau(mGraphics g)
        {
            g.setColor(MAU_DAI_DAU, 1f);
            g.fillRect(X + 3, Y + 3, W - 6, 44, 6);
            // Nua duoi vuong goc: bo goc ca bon phia thi dai nhin nhu mot vien
            // thuoc troi giua khung.
            g.fillRect(X + 3, Y + 30, W - 6, 19);
            g.setColor(0xFFE2B8, 0.55f);
            g.fillRect(X + 4, Y + 4, W - 8, 2, 2);
            g.setColor(MAU_VIEN_BANG, 0.85f);
            g.fillRect(X + 3, Y + 49, W - 6, 1);
            g.setColor(0xFFE9A3, 0.7f);
            g.fillRect(X + 3, Y + 50, W - 6, 1);
        }

        /// <summary>
        /// Một thẻ tab: bo góc, thẻ đang chọn nền cam và dính liền phần dưới.
        /// </summary>
        /// <remarks>
        /// Thẻ đang chọn có một "lưỡi" thò xuống qua nét ngăn, nên nó nối liền
        /// với vùng nội dung thay vì nằm rời phía trên một đường kẻ — đó là chỗ
        /// mắt đọc ra "thẻ này đang mở".
        /// </remarks>
        private void veTheTab(mGraphics g, int x, int w, bool chon)
        {
            int y = 52;
            int h = 26;
            g.setColor(MAU_VIEN_BANG, chon ? 0.95f : 0.4f);
            g.fillRect(x, y, w, h, 6);
            g.setColor(chon ? MAU_THE_CHON : MAU_NEN_SANG, chon ? 1f : 0.8f);
            g.fillRect(x + 1, y + 1, w - 2, h - 2, 5);
            if (chon)
            {
                g.fillRect(x + 3, y + h - 4, w - 6, 8);
                g.setColor(0xFFF3DC, 0.85f);
                g.fillRect(x + 3, y + 2, w - 6, 2, 2);
            }
        }

        /// <summary>Dải tiền ở đáy bảng: nền cam nhạt bo góc, hai nét ngăn trên.</summary>
        /// <remarks>
        /// Cùng tông với dải đầu bảng, nên hai đầu bảng khép lại thành một khối.
        /// Bản cũ là một mảng nâu vuông chạy hết bề ngang với một nét xám.
        /// </remarks>
        private void veDaiTien(mGraphics g)
        {
            g.setColor(MAU_VIEN_MO, 0.7f);
            g.fillRect(X + 3, H - 17, W - 6, 1);
            g.setColor(MAU_NEN_SANG, 0.85f);
            g.fillRect(X + 3, H - 16, W - 6, 1);
            g.setColor(MAU_DAI_DAU, 0.9f);
            g.fillRect(X + 3, H - 15, W - 6, 12, 5);
        }

        /// <summary>Hai nét ngăn dưới hàng thẻ tab.</summary>
        private void veVachDuoiTab(mGraphics g)
        {
            g.setColor(MAU_VIEN_MO, 0.7f);
            g.fillRect(X + 3, 78, W - 6, 1);
            g.setColor(MAU_NEN_SANG, 0.85f);
            g.fillRect(X + 3, 79, W - 6, 1);
        }

        private void paintTopInfo(mGraphics g)
        {
            g.setClip(X + 1, Y, W - 2, yScroll - 2);
            veDaiDau(g);
            // Moi khung hinh coi nhu nut KHONG hien; ham ve nut se bat co len
            // neu tab hien tai that su ve no. paintTopInfo chay truoc cac
            // nhanh ve theo tab nen dat o day la dung thu tu.
            nutChiSoDangHien = false;
            switch (type)
            {
                case 13:
                    if (currentTabIndex == 0 || currentTabIndex == 1)
                    {
                        if (Equals(GameCanvas.panel))
                        {
                            SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                            paintGiaoDichInfo(g);
                        }
                        if (Equals(GameCanvas.panel2) && charMenu != null)
                        {
                            SmallImage.drawSmallImage(g, charMenu.avatarz(), X + 25, 50, 0, 33);
                            paintCharInfo(g, charMenu);
                        }
                    }
                    if (currentTabIndex == 2 && charMenu != null)
                    {
                        SmallImage.drawSmallImage(g, charMenu.avatarz(), X + 25, 50, 0, 33);
                        paintCharInfo(g, charMenu);
                    }
                    break;
                case 12:
                    if (currentTabIndex == 0)
                    {
                        int id = 1410;
                        for (int i = 0; i < GameScr.vNpc.size(); i++)
                        {
                            Npc npc = (Npc)GameScr.vNpc.elementAt(i);
                            if (npc.template.npcTemplateId == idNPC)
                            {
                                id = npc.avatar;
                            }
                        }
                        SmallImage.drawSmallImage(g, id, X + 25, 50, 0, 33);
                        paintCombineInfo(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintMyInfo(g);
                    }
                    break;
                case 11:
                case 16:
                case 23:
                case 24:
                case 30:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 15:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 9:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 21:
                    if (currentTabIndex == 0)
                    {
                        SmallImage.drawSmallImage(g, Char.myPetz().avatarz(), X + 25, 50, 0, 33);
                        paintPetInfo(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        SmallImage.drawSmallImage(g, Char.myPetz().avatarz(), X + 25, 50, 0, 33);
                        paintPetStatusInfo(g);
                    }
                    if (currentTabIndex == 2)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintItemBodyBagInfo(g);
                    }
                    break;
                case 0:
                    if (currentTabIndex == 0)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintMyInfo(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        if (isnewInventory)
                        {
                            paintCharInfo(g, Char.myCharz());
                        }
                        else
                        {
                            paintItemBodyBagInfo(g);
                        }
                    }
                    if (currentTabIndex == 2)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintSkillInfo(g);
                    }
                    if (currentTabIndex == 3)
                    {
                        if (mainTabName.Length == 5)
                        {
                            paintClanInfo(g);
                        }
                        else
                        {
                            SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                            paintToolInfo(g);
                        }
                    }
                    if (currentTabIndex == 4)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintToolInfo(g);
                    }
                    break;
                case 25:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 2:
                    if (currentTabIndex == 0)
                    {
                        SmallImage.drawSmallImage(g, 526, X + 25, 50, 0, 33);
                        paintItemBoxInfo(g);
                    }
                    if (currentTabIndex == 1)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                        paintItemBodyBagInfo(g);
                    }
                    break;
                case 3:
                    SmallImage.drawSmallImage(g, 561, X + 25, 50, 0, 33);
                    paintZoneInfo(g);
                    break;
                case 1:
                    if (currentTabIndex == currentTabName.Length - 1 && GameCanvas.panel2 == null)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    }
                    else if (Char.myCharz().npcFocus != null)
                    {
                        SmallImage.drawSmallImage(g, Char.myCharz().npcFocus.avatar, X + 25, 50, 0, 33);
                    }
                    paintShopInfo(g);
                    break;
                case 4:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMapInfo(g);
                    break;
                case 7:
                case 17:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 8:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 10:
                    if (charMenu != null)
                    {
                        SmallImage.drawSmallImage(g, charMenu.avatarz(), X + 25, 50, 0, 33);
                        paintCharInfo(g, charMenu);
                    }
                    break;
                case 14:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMapInfo(g);
                    break;
                case 18:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintMyInfo(g);
                    break;
                case 19:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintToolInfo(g);
                    break;
                case 20:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintToolInfo(g);
                    break;
                case 22:
                    SmallImage.drawSmallImage(g, Char.myCharz().avatarz(), X + 25, 50, 0, 33);
                    paintToolInfo(g);
                    break;
                case 5:
                case 6:
                    break;
            }
        }
    
        private void paintChatManager(mGraphics g)
        {
        }
    
        private void paintChatPlayer(mGraphics g)
        {
        }
    
        private string getStatus(int status)
        {
            switch (status)
            {
                case 0:
                    return mResources.follow;
                case 1:
                    return mResources.defend;
                case 2:
                    return mResources.attack;
                case 3:
                    return mResources.gohome;
                default:
                    return "aaa";
            }
        }
    
        private void paintPetStatusInfo(mGraphics g)
        {
            mFont.tahoma_7b_white.drawString(g, "HP: " + Char.myPetz().cHP + "/" + Char.myPetz().cHPFull, X + 60, 4, mFont.LEFT, mFont.tahoma_7b_dark);
            mFont.tahoma_7b_white.drawString(g, "MP: " + Char.myPetz().cMP + "/" + Char.myPetz().cMPFull, X + 60, 16, mFont.LEFT, mFont.tahoma_7b_dark);
            mFont.tahoma_7_yellow.drawString(g, mResources.critical + ": " + Char.myPetz().cCriticalFull + "   " + mResources.armor + ": " + Char.myPetz().cDefull, X + 60, 27, mFont.LEFT, mFont.tahoma_7_grey);
            mFont.tahoma_7_yellow.drawString(g, mResources.status + ": " + strStatus[Char.myPetz().petStatus], X + 60, 38, mFont.LEFT, mFont.tahoma_7_grey);
        }
    
        private void paintCombineInfo(mGraphics g)
        {
            if (combineTopInfo != null)
            {
                for (int i = 0; i < combineTopInfo.Length; i++)
                {
                    mFont.tahoma_7_white.drawString(g, combineTopInfo[i], X + 45 + (W - 50) / 2, 5 + i * 14, mFont.CENTER);
                }
            }
        }
    
        private void paintInfomation(mGraphics g)
        {
        }
    
        public void paintMap(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(-cmxMap, -cmyMap);
            g.drawImage(imgMap, xScroll, yScroll, 0);
            int head = Char.myCharz().head;
            Part part = GameScr.parts[head];
            SmallImage.drawSmallImage(g, part.pi[Char.CharInfo[0][0][0]].id, xMap, yMap + 5, 0, 3);
            int align = mFont.CENTER;
            if (xMap <= 40)
            {
                align = mFont.LEFT;
            }
            if (xMap >= 220)
            {
                align = mFont.RIGHT;
            }
            mFont.tahoma_7b_yellow.drawString(g, TileMap.mapName, xMap, yMap - 12, align, mFont.tahoma_7_grey);
            int num = -1;
            if (GameScr.getTaskMapId() != -1)
            {
                for (int i = 0; i < mapId[TileMap.planetID].Length; i++)
                {
                    if (mapId[TileMap.planetID][i] == GameScr.getTaskMapId())
                    {
                        num = i;
                        break;
                    }
                    num = 4;
                }
                if (GameCanvas.gameTick % 4 > 0)
                {
                    g.drawImage(ItemMap.imageFlare, xScroll + mapX[TileMap.planetID][num], yScroll + mapY[TileMap.planetID][num], 3);
                }
            }
            if (!GameCanvas.isTouch)
            {
                g.drawImage(imgBantay, xMove, yMove, StaticObj.TOP_RIGHT);
                for (int j = 0; j < mapX[TileMap.planetID].Length; j++)
                {
                    int num2 = mapX[TileMap.planetID][j] + xScroll;
                    int num3 = mapY[TileMap.planetID][j] + yScroll;
                    if (Res.inRect(num2 - 15, num3 - 15, 30, 30, xMove, yMove))
                    {
                        align = mFont.CENTER;
                        if (num2 <= 20)
                        {
                            align = mFont.LEFT;
                        }
                        if (num2 >= 220)
                        {
                            align = mFont.RIGHT;
                        }
                        mFont.tahoma_7b_yellow.drawString(g, TileMap.mapNames[mapId[TileMap.planetID][j]], num2, num3 - 12, align, mFont.tahoma_7_grey);
                        break;
                    }
                }
            }
            else if (!trans)
            {
                for (int k = 0; k < mapX[TileMap.planetID].Length; k++)
                {
                    int num4 = mapX[TileMap.planetID][k] + xScroll;
                    int num5 = mapY[TileMap.planetID][k] + yScroll;
                    if (Res.inRect(num4 - 15, num5 - 15, 30, 30, pX, pY))
                    {
                        align = mFont.CENTER;
                        if (num4 <= 30)
                        {
                            align = mFont.LEFT;
                        }
                        if (num4 >= 220)
                        {
                            align = mFont.RIGHT;
                        }
                        g.drawImage(imgBantay, num4, num5, StaticObj.TOP_RIGHT);
                        mFont.tahoma_7b_yellow.drawString(g, TileMap.mapNames[mapId[TileMap.planetID][k]], num4, num5 - 12, align, mFont.tahoma_7_grey);
                        break;
                    }
                }
            }
            g.translate(-g.getTranslateX(), -g.getTranslateY());
            if (num != -1)
            {
                if (mapX[TileMap.planetID][num] + xScroll < cmxMap)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 5, xScroll + 5, yScroll + hScroll / 2 - 4, 0);
                }
                if (cmxMap + wScroll < mapX[TileMap.planetID][num] + xScroll)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 6, xScroll + wScroll - 5, yScroll + hScroll / 2 - 4, StaticObj.TOP_RIGHT);
                }
                if (mapY[TileMap.planetID][num] < cmyMap)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 1, xScroll + wScroll / 2, yScroll + 5, StaticObj.TOP_CENTER);
                }
                if (mapY[TileMap.planetID][num] > cmyMap + hScroll)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 0, xScroll + wScroll / 2, yScroll + hScroll - 5, StaticObj.BOTTOM_HCENTER);
                }
            }
        }
    
        public void paintTask(mGraphics g)
        {
            int num = ((GameCanvas.h <= 300) ? 15 : 20);
            if (isPaintMap && !GameScr.gI().isMapDocNhan() && !GameScr.gI().isMapFize())
            {
                g.drawImage((keyTouchMapButton != 1) ? GameScr.imgLbtn : GameScr.imgLbtnFocus, xScroll + wScroll / 2, yScroll + hScroll - num, 3);
                mFont.tahoma_7b_dark.drawString(g, mResources.map, xScroll + wScroll / 2, yScroll + hScroll - (num + 5), mFont.CENTER);
            }
            xstart = xScroll + 5;
            ystart = yScroll + 14;
            yPaint = ystart;
            g.setClip(xScroll, yScroll, wScroll, hScroll - 35);
            if (scroll != null)
            {
                if (scroll.cmy > 0)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 1, xScroll + wScroll - 12, yScroll + 3, 0);
                }
                if (scroll.cmy < scroll.cmyLim)
                {
                    g.drawRegion(Mob.imgHP, 0, 0, 9, 6, 0, xScroll + wScroll - 12, yScroll + hScroll - 45, 0);
                }
                g.translate(0, -scroll.cmy);
            }
            indexRowMax = 0;
            if (indexMenu == 0)
            {
                bool flag = false;
                if (Char.myCharz().taskMaint != null)
                {
                    for (int i = 0; i < Char.myCharz().taskMaint.names.Length; i++)
                    {
                        mFont.tahoma_7_grey.drawString(g, Char.myCharz().taskMaint.names[i], xScroll + wScroll / 2, yPaint - 5 + i * 12, mFont.CENTER);
                        indexRowMax++;
                    }
                    yPaint += (Char.myCharz().taskMaint.names.Length - 1) * 12;
                    int num2 = 0;
                    string empty = string.Empty;
                    for (int j = 0; j < Char.myCharz().taskMaint.subNames.Length; j++)
                    {
                        if (Char.myCharz().taskMaint.subNames[j] != null)
                        {
                            num2 = j;
                            empty = "- " + Char.myCharz().taskMaint.subNames[j];
                            if (Char.myCharz().taskMaint.counts[j] != -1)
                            {
                                if (Char.myCharz().taskMaint.index == j)
                                {
                                    if (Char.myCharz().taskMaint.counts[j] != 1)
                                    {
                                        string text = empty;
                                        empty = text + " (" + Char.myCharz().taskMaint.count + "/" + Char.myCharz().taskMaint.counts[j] + ")";
                                    }
                                    if (Char.myCharz().taskMaint.count == Char.myCharz().taskMaint.counts[j])
                                    {
                                        mFont.tahoma_7.drawString(g, empty, xstart + 5, yPaint += 12, 0);
                                    }
                                    else
                                    {
                                        mFont tahoma_7_grey = mFont.tahoma_7_grey;
                                        if (!flag)
                                        {
                                            flag = true;
                                            tahoma_7_grey = mFont.tahoma_7_blue;
                                            tahoma_7_grey.drawString(g, empty, xstart + 5 + ((tahoma_7_grey == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                        }
                                        else
                                        {
                                            tahoma_7_grey.drawString(g, "- ...", xstart + 5 + ((tahoma_7_grey == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                        }
                                    }
                                }
                                else if (Char.myCharz().taskMaint.index > j)
                                {
                                    if (Char.myCharz().taskMaint.counts[j] != 1)
                                    {
                                        string text = empty;
                                        empty = text + " (" + Char.myCharz().taskMaint.counts[j] + "/" + Char.myCharz().taskMaint.counts[j] + ")";
                                    }
                                    mFont.tahoma_7_white.drawString(g, empty, xstart + 5, yPaint += 12, 0);
                                }
                                else
                                {
                                    if (Char.myCharz().taskMaint.counts[j] != 1)
                                    {
                                        empty = empty + " 0/" + Char.myCharz().taskMaint.counts[j];
                                    }
                                    mFont tahoma_7_grey2 = mFont.tahoma_7_grey;
                                    if (!flag)
                                    {
                                        flag = true;
                                        tahoma_7_grey2 = mFont.tahoma_7_blue;
                                        tahoma_7_grey2.drawString(g, empty, xstart + 5 + ((tahoma_7_grey2 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                    }
                                    else
                                    {
                                        tahoma_7_grey2.drawString(g, "- ...", xstart + 5 + ((tahoma_7_grey2 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                    }
                                }
                            }
                            else if (Char.myCharz().taskMaint.index > j)
                            {
                                mFont.tahoma_7_white.drawString(g, empty, xstart + 5, yPaint += 12, 0);
                            }
                            else
                            {
                                mFont tahoma_7_grey3 = mFont.tahoma_7_grey;
                                if (!flag)
                                {
                                    flag = true;
                                    tahoma_7_grey3 = mFont.tahoma_7_blue;
                                    tahoma_7_grey3.drawString(g, empty, xstart + 5 + ((tahoma_7_grey3 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                }
                                else
                                {
                                    tahoma_7_grey3.drawString(g, "- ...", xstart + 5 + ((tahoma_7_grey3 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                                }
                            }
                            indexRowMax++;
                        }
                        else if (Char.myCharz().taskMaint.index <= j)
                        {
                            empty = "- " + Char.myCharz().taskMaint.subNames[num2];
                            mFont mFont2 = mFont.tahoma_7_grey;
                            if (!flag)
                            {
                                flag = true;
                                mFont2 = mFont.tahoma_7_blue;
                            }
                            mFont2.drawString(g, empty, xstart + 5 + ((mFont2 == mFont.tahoma_7_blue && GameCanvas.gameTick % 20 > 10) ? (GameCanvas.gameTick % 4 / 2) : 0), yPaint += 12, 0);
                        }
                    }
                    yPaint += 5;
                    for (int k = 0; k < Char.myCharz().taskMaint.details.Length; k++)
                    {
                        mFont.tahoma_7_green2.drawString(g, Char.myCharz().taskMaint.details[k], xstart + 5, yPaint += 12, 0);
                        indexRowMax++;
                    }
                }
                else
                {
                    int taskMapId = GameScr.getTaskMapId();
                    sbyte taskNpcId = GameScr.getTaskNpcId();
                    string empty2 = string.Empty;
                    if (taskMapId == -3 || taskNpcId == -3)
                    {
                        empty2 = mResources.DES_TASK[3];
                    }
                    else if (Char.myCharz().taskMaint == null && Char.myCharz().ctaskId == 9 && Char.myCharz().nClass.classId == 0)
                    {
                        empty2 = mResources.TASK_INPUT_CLASS;
                    }
                    else
                    {
                        if (taskNpcId < 0 || taskMapId < 0)
                        {
                            return;
                        }
                        empty2 = mResources.DES_TASK[0] + Npc.arrNpcTemplate[taskNpcId].name + mResources.DES_TASK[1] + TileMap.mapNames[taskMapId] + mResources.DES_TASK[2];
                    }
                    string[] array = mFont.tahoma_7_white.splitFontArray(empty2, 150);
                    for (int l = 0; l < array.Length; l++)
                    {
                        if (l == 0)
                        {
                            mFont.tahoma_7_white.drawString(g, array[l], xstart + 5, yPaint = ystart, 0);
                        }
                        else
                        {
                            mFont.tahoma_7_white.drawString(g, array[l], xstart + 5, yPaint += 12, 0);
                        }
                    }
                }
            }
            else if (indexMenu == 1)
            {
                yPaint = ystart - 12;
                for (int m = 0; m < Char.myCharz().taskOrders.size(); m++)
                {
                    TaskOrder taskOrder = (TaskOrder)Char.myCharz().taskOrders.elementAt(m);
                    mFont.tahoma_7_white.drawString(g, taskOrder.name, xstart + 5, yPaint += 12, 0);
                    if (taskOrder.count == taskOrder.maxCount)
                    {
                        mFont.tahoma_7_white.drawString(g, ((taskOrder.taskId != 0) ? mResources.KILLBOSS : mResources.KILL) + " " + Mob.arrMobTemplate[taskOrder.killId].name + " (" + taskOrder.count + "/" + taskOrder.maxCount + ")", xstart + 5, yPaint += 12, 0);
                    }
                    else
                    {
                        mFont.tahoma_7_blue.drawString(g, ((taskOrder.taskId != 0) ? mResources.KILLBOSS : mResources.KILL) + " " + Mob.arrMobTemplate[taskOrder.killId].name + " (" + taskOrder.count + "/" + taskOrder.maxCount + ")", xstart + 5, yPaint += 12, 0);
                    }
                    indexRowMax += 3;
                    inforW = popupW - 25;
                    paintMultiLine(g, mFont.tahoma_7_grey, taskOrder.description, xstart + 5, yPaint += 12, 0);
                    yPaint += 12;
                }
            }
            if (scroll == null)
            {
                scroll = new Scroll();
                scroll.setStyle(indexRowMax, 12, xScroll, yScroll, wScroll, hScroll - num - 40, true, 1);
            }
        }
    
        public void paintMultiLine(mGraphics g, mFont f, string[] arr, string str, int x, int y, int align)
        {
            for (int i = 0; i < arr.Length; i++)
            {
                string text = arr[i];
                if (text.StartsWith("c"))
                {
                    if (text.StartsWith("c0"))
                    {
                        text = text.Substring(2);
                        f = mFont.tahoma_7b_dark;
                    }
                    else if (text.StartsWith("c1"))
                    {
                        text = text.Substring(2);
                        f = mFont.tahoma_7b_yellow;
                    }
                    else if (text.StartsWith("c2"))
                    {
                        text = text.Substring(2);
                        f = mFont.tahoma_7b_green;
                    }
                }
                if (i == 0)
                {
                    f.drawString(g, text, x, y, align);
                    continue;
                }
                if (i < indexRow + 30 && i > indexRow - 30)
                {
                    f.drawString(g, text, x, y += 12, align);
                }
                else
                {
                    y += 12;
                }
                yPaint += 12;
                indexRowMax++;
            }
        }
    
        public void paintMultiLine(mGraphics g, mFont f, string str, int x, int y, int align)
        {
            int num = ((!GameCanvas.isTouch || GameCanvas.w < 320) ? 10 : 20);
            string[] array = f.splitFontArray(str, inforW - num);
            for (int i = 0; i < array.Length; i++)
            {
                if (i == 0)
                {
                    f.drawString(g, array[i], x, y, align);
                    continue;
                }
                if (i < indexRow + 15 && i > indexRow - 15)
                {
                    f.drawString(g, array[i], x, y += 12, align);
                }
                else
                {
                    y += 12;
                }
                yPaint += 12;
                indexRowMax++;
            }
        }
    
        public void cleanCombine()
        {
            for (int i = 0; i < vItemCombine.size(); i++)
            {
                ((Item)vItemCombine.elementAt(i)).isSelect = false;
            }
            vItemCombine.removeAllElements();
        }
    
        public void hideNow()
        {
            if (timeShow > 0)
            {
                isClose = false;
                return;
            }
            cp = null;
            if (isTypeShop() || TileMap.mapID == 45)
            {
                Char.myCharz().resetPartTemp();
            }
            if (chatTField != null && type == 13 && chatTField.isShow)
            {
                chatTField = null;
            }
            if (type == 13 && !isAccept)
            {
                Service.gI().giaodich(3, -1, -1, -1);
            }
            Res.outz("HIDE PANELLLLLLLLLLLLLLLLLLLLLL");
            SoundMn.gI().buttonClose();
            GameScr.isPaint = true;
            TileMap.lastPlanetId = -1;
            imgMap = null;
            mSystem.gcc();
            isClanOption = false;
            isClose = true;
            cleanCombine();
            Hint.clickNpc();
            GameCanvas.panel2 = null;
            GameCanvas.clearAllPointerEvent();
            GameCanvas.clearKeyPressed();
            pointerDownTime = (pointerDownFirstX = 0);
            pointerIsDowning = false;
            isShow = false;
            if ((Char.myCharz().cHP <= 0 || Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5) && Char.myCharz().meDead)
            {
                Command center = new Command(mResources.DIES[0], 11038, GameScr.gI());
                GameScr.gI().center = center;
                Char.myCharz().cHP = 0;
            }
        }
    

        /// <summary>
        /// Them muc "Dung nhanh" cho vat pham GOP DUOC.
        /// </summary>
        /// <remarks>
        /// Chi hien khi so luong lon hon 1: mon le thi bam "Su dung" mot cai la
        /// xong, them mot muc nua chi lam menu dai.
        ///
        /// Bam vao se hoi so luong muon dung, roi dung lan luot. Tu dung khi het
        /// mon, du so, hoac khi may chu khong tru mon nao nua — do la luc da dat
        /// muc toi da. Mon co thoi gian ton tai (cuong no...) thi cho het hieu luc
        /// moi dung tiep, khong nem chong len nhau.
        /// </remarks>
        private void themDungNhanh(MyVector myVector, Item mon)
        {
        	if (mon == null || mon.template == null || mon.quantity <= 1)
        	{
        		return;
        	}
        	myVector.addElement(new Command("Dùng nhanh", God.Items.getInstance(), 4,
        		new God.ItemGroup(mon.template.id, mon.indexUI, false, false)));
        }

        public void hide()
        {
            if (timeShow > 0)
            {
                isClose = false;
                return;
            }
            cp = null;
            if (isTypeShop() || TileMap.mapID == 45)
            {
                Char.myCharz().resetPartTemp();
            }
            if (chatTField != null && type == 13 && chatTField.isShow)
            {
                chatTField = null;
            }
            if (type == 13 && !isAccept)
            {
                Service.gI().giaodich(3, -1, -1, -1);
            }
            if (type == 15)
            {
                Service.gI().sendThachDau(-1);
            }
            SoundMn.gI().buttonClose();
            GameScr.isPaint = true;
            TileMap.lastPlanetId = -1;
            if (imgMap != null)
            {
                imgMap.texture = null;
                imgMap = null;
            }
            mSystem.gcc();
            isClanOption = false;
            if (type != 4)
            {
                if (type == 24)
                {
                    setTypeGameInfo();
                }
                else if(type == 30)
                {
                    setTypeMain();
                }
                else if (type == 23)
                {
                    setTypeMain();
                }
                else if (type == 3 || type == 14)
                {
                    if (isChangeZone)
                    {
                        isClose = true;
                    }
                    else
                    {
                        setTypeMain();
                        cmx = (cmtoX = 0);
                    }
                }
                else if (type == 18 || type == 19 || type == 20 || type == 21)
                {
                    setTypeMain();
                    cmx = (cmtoX = 0);
                }
                else if (type == 8 || type == 11 || type == 16)
                {
                    setTypeAccount();
                    cmx = (cmtoX = 0);
                }
                else
                {
                    isClose = true;
                }
            }
            else
            {
                setTypeMain();
                cmx = (cmtoX = 0);
            }
            Hint.clickNpc();
            GameCanvas.panel2 = null;
            GameCanvas.clearAllPointerEvent();
            GameCanvas.clearKeyPressed();
            GameCanvas.isFocusPanel2 = false;
            pointerDownTime = (pointerDownFirstX = 0);
            pointerIsDowning = false;
            if ((Char.myCharz().cHP <= 0 || Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5) && Char.myCharz().meDead)
            {
                Command center = new Command(mResources.DIES[0], 11038, GameScr.gI());
                GameScr.gI().center = center;
                Char.myCharz().cHP = 0;
            }
        }
    
        public void update()
        {
            if (chatTField != null && chatTField.isShow)
            {
                chatTField.update();
                return;
            }
            if (isKiguiXu)
            {
                delayKigui++;
                if (delayKigui == 10)
                {
                    delayKigui = 0;
                    isKiguiXu = false;
                    chatTField.tfChat.setText(string.Empty);
                    chatTField.strChat = mResources.kiguiXuchat + " ";
                    chatTField.tfChat.name = mResources.input_money;
                    chatTField.to = string.Empty;
                    chatTField.isShow = true;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                    chatTField.tfChat.setMaxTextLenght(10);
                    if (GameCanvas.isTouch)
                    {
                        chatTField.tfChat.doChangeToTextBox();
                    }
                    if (Main.isWindowsPhone)
                    {
                        chatTField.tfChat.strInfo = chatTField.strChat;
                    }
                    if (!Main.isPC)
                    {
                        chatTField.startChat2(this, string.Empty);
                    }
                }
                return;
            }
            if (isKiguiLuong)
            {
                delayKigui++;
                if (delayKigui == 10)
                {
                    delayKigui = 0;
                    isKiguiLuong = false;
                    chatTField.tfChat.setText(string.Empty);
                    chatTField.strChat = mResources.kiguiLuongchat + "  ";
                    chatTField.tfChat.name = mResources.input_money;
                    chatTField.to = string.Empty;
                    chatTField.isShow = true;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                    chatTField.tfChat.setMaxTextLenght(10);
                    if (GameCanvas.isTouch)
                    {
                        chatTField.tfChat.doChangeToTextBox();
                    }
                    if (Main.isWindowsPhone)
                    {
                        chatTField.tfChat.strInfo = chatTField.strChat;
                    }
                    if (!Main.isPC)
                    {
                        chatTField.startChat2(this, string.Empty);
                    }
                }
                return;
            }
            if (scroll != null)
            {
                scroll.updatecm();
            }
            if (tabIcon != null && tabIcon.isShow)
            {
                tabIcon.update();
                return;
            }
            moveCamera();
            if (isTabInven() && isnewInventory)
            {
                if (eBanner == null)
                {
                    eBanner = new Effect(205, 0, 0, 3, 10, -1);
                    eBanner.typeEff = 2;
                }
                if (eBanner != null)
                {
                    eBanner.update();
                }
            }
            if (waitToPerform > 0)
            {
                waitToPerform--;
                if (waitToPerform == 0)
                {
                    lastSelect[currentTabIndex] = selected;
                    // Dọn menu cũ trước khi mở menu mới — NHƯNG chỉ khi bảng
                    // này thật sự có ô đang chọn.
                    //
                    // Hầu hết các hàm doFireX dưới đây kết thúc bằng
                    // `GameCanvas.menu.startAt(...)`, mà `startAt` mở đầu bằng
                    // `if (showMenu) return;` — còn menu cũ đang mở thì nó LẶNG
                    // LẼ bỏ qua menu mới, không báo lỗi, không log. Người chơi
                    // thấy đúng hiện tượng "lần đầu bấm thì hiện, từ lần hai trở
                    // đi không hiện nữa" — ở cửa hàng là mất luôn nút Mua.
                    //
                    // Đặt lại ở đây chứ không sửa `startAt`: chỗ này chạy theo
                    // SỰ KIỆN bấm, còn sửa trong `startAt` thì bất kỳ chỗ nào gọi
                    // mỗi khung hình sẽ làm menu dựng lại liên tục.
                    //
                    // ĐIỀU KIỆN `selected >= 0` MỚI THÊM, và nó chữa lỗi "không
                    // hiện nút Lấy ra":
                    //
                    //   `GameCanvas.menu` là của CHUNG cả game, mà màn rương —
                    //   và mọi màn hai bảng khác: Bà Hạt Mít, Satan, rương ở
                    //   nhà, Uron, Bunma, Appule, Dende... — có HAI đối tượng
                    //   Panel cùng chạy `update()` mỗi khung hình.
                    //
                    //   Bấm một món trong rương: bảng rương dọn menu rồi mở menu
                    //   "Lấy ra". Ngay sau đó bảng bên kia cũng tới lượt
                    //   `update()`, cũng dọn menu — nhưng nó KHÔNG có ô nào đang
                    //   chọn nên `doFireX` của nó thoát ngay ở dòng
                    //   `if (selected < 0) return;` và không mở menu nào thay
                    //   thế. Menu "Lấy ra" vừa mở đã bị xoá.
                    //
                    //   Vì sao "thỉnh thoảng", và vì sao đúng ở thẻ Trang bị:
                    //   thẻ Hành trang gần như ô nào cũng có đồ nên bảng kia mở
                    //   được menu của nó (nhìn vẫn thấy nút), còn thẻ Trang bị
                    //   phần lớn ô trống nên nó chẳng mở gì cả — và người chơi
                    //   thấy nút biến mất.
                    //
                    //   Bảng không có ô đang chọn thì không có quyền dọn menu
                    //   của bảng kia.
                    if (selected >= 0)
                    {
                        GameCanvas.menu.showMenu = false;
                    }
                    switch (type)
                    {
                        case 30:
                            doFireMoneyAward();
                            break;
                        case 23:
                            doFireGameInfo();
                            break;
                        case 21:
                            doFirePetMain();
                            break;
                        case 0:
                            doFireMain();
                            break;
                        case 2:
                            doFireBox();
                            break;
                        case 3:
                            doFireZone();
                            break;
                        case 1:
                        case 17:
                            doFireShop();
                            break;
                        case 25:
                            doSpeacialSkill();
                            break;
                        case 4:
                            doFireMap();
                            break;
                        case 14:
                            doFireMapTrans();
                            break;
                        case 7:
                            if (Equals(GameCanvas.panel2) && GameCanvas.panel.type == 2)
                            {
                                doFireBox();
                                return;
                            }
                            doFireInventory();
                            break;
                        case 8:
                            doFireLogMessage();
                            break;
                        case 9:
                            doFireArchivement();
                            break;
                        case 10:
                            doFirePlayerMenu();
                            break;
                        case 11:
                            doFireFriend();
                            break;
                        case 16:
                            doFireEnemy();
                            break;
                        case 15:
                            doFireTop();
                            break;
                        case 12:
                            doFireCombine();
                            break;
                        case 13:
                            doFireGiaoDich();
                            break;
                        case 18:
                            doFireChangeFlag();
                            break;
                        case 19:
                            doFireOption();
                            break;
                        case 20:
                            doFireAccount();
                            break;
                        case 22:
                            doFireAuto();
                            break;
                    }
                }
            }
            for (int i = 0; i < ClanMessage.vMessage.size(); i++)
            {
                ((ClanMessage)ClanMessage.vMessage.elementAt(i)).update();
            }
            updateCombineEff();
        }
    
        private void doSpeacialSkill()
        {
        }
    
        private void doFireGameInfo()
        {
            if (selected != -1)
            {
                infoSelect = selected;
                ((GameInfo)vGameInfo.elementAt(infoSelect)).hasRead = true;
                Rms.saveRMSInt(((GameInfo)vGameInfo.elementAt(infoSelect)).id + string.Empty, 1);
                setTypeGameSubInfo();
            }
        }
        private void doFireMoneyAward()
        {
            if (selected != -1)
            {
            }
        }
    
        private void doFireAuto()
        {
        }
    
        private void doFirePetMain()
        {
            if (currentTabIndex == 0)
            {
                if (selected == -1 || selected > Char.myPetz().arrItemBody.Length - 1)
                {
                    return;
                }
                MyVector myVector = new MyVector(string.Empty);
                Item item = Char.myPetz().arrItemBody[selected];
                currItem = item;
                if (currItem != null)
                {
                    myVector.addElement(new Command(mResources.MOVEOUT, this, 2006, currItem));
                    GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                    addItemDetail(currItem);
                }
                else
                {
                    cp = null;
                }
            }
            if (currentTabIndex == 1)
            {
                doFirePetStatus();
            }
            if (currentTabIndex == 2)
            {
                doFireInventory();
            }
        }
    
        private void doFirePetStatus()
        {
            if (selected == -1)
            {
                return;
            }
            if (selected == 5)
            {
                GameCanvas.startYesNoDlg(mResources.sure_fusion, new Command(mResources.YES, 888351), new Command(mResources.NO, 2001));
                return;
            }
            Service.gI().petStatus((sbyte)selected);
            if (selected < 4)
            {
                Char.myPetz().petStatus = (sbyte)selected;
            }
        }
    
        private void doFireTop()
        {
            if (selected >= -1)
            {
                if (isThachDau)
                {
                    Service.gI().sendTop(topName, (sbyte)selected);
                    return;
                }
                MyVector myVector = new MyVector(string.Empty);
                myVector.addElement(new Command(mResources.CHAR_ORDER[0], this, 9999, (TopInfo)vTop.elementAt(selected)));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addThachDauDetail((TopInfo)vTop.elementAt(selected));
            }
        }
    
        private void doFireMapTrans()
        {
            doFireZone();
        }
    
        private void doFireGiaoDich()
        {
            if (currentTabIndex == 0 && Equals(GameCanvas.panel))
            {
                doFireInventory();
                return;
            }
            if ((currentTabIndex == 0 && Equals(GameCanvas.panel2)) || currentTabIndex == 2)
            {
                if (Equals(GameCanvas.panel2))
                {
                    currItem = (Item)GameCanvas.panel2.vFriendGD.elementAt(selected);
                }
                else
                {
                    currItem = (Item)GameCanvas.panel.vFriendGD.elementAt(selected);
                }
                Res.outz2("toi day select= " + selected);
                MyVector myVector = new MyVector();
                myVector.addElement(new Command(mResources.CLOSE, this, 8000, currItem));
                if (currItem != null)
                {
                    GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                    addItemDetail(currItem);
                }
                else
                {
                    cp = null;
                }
            }
            if (currentTabIndex == 1)
            {
                if (selected == currentListLength - 3)
                {
                    if (isLock)
                    {
                        return;
                    }
                    putMoney();
                }
                else if (selected == currentListLength - 2)
                {
                    if (!isAccept)
                    {
                        isLock = !isLock;
                        if (isLock)
                        {
                            Service.gI().giaodich(5, -1, -1, -1);
                        }
                        else
                        {
                            hide();
                            InfoDlg.showWait();
                            Service.gI().giaodich(3, -1, -1, -1);
                        }
                    }
                    else
                    {
                        isAccept = false;
                    }
                }
                else if (selected == currentListLength - 1)
                {
                    if (isLock && !isAccept && isFriendLock)
                    {
                        GameCanvas.startYesNoDlg(mResources.do_u_sure_to_trade, new Command(mResources.YES, this, 7002, null), new Command(mResources.NO, this, 4005, null));
                    }
                }
                else
                {
                    if (isLock)
                    {
                        return;
                    }
                    currItem = (Item)GameCanvas.panel.vMyGD.elementAt(selected);
                    MyVector myVector2 = new MyVector();
                    myVector2.addElement(new Command(mResources.CLOSE, this, 8000, currItem));
                    if (currItem != null)
                    {
                        GameCanvas.menu.startAt(myVector2, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addItemDetail(currItem);
                    }
                    else
                    {
                        cp = null;
                    }
                }
            }
            if (GameCanvas.isTouch)
            {
                selected = -1;
            }
        }
    
        private void doFireCombine()
        {
            if (currentTabIndex == 0)
            {
                if (selected == -1 || vItemCombine.size() == 0)
                {
                    return;
                }
                if (selected == vItemCombine.size())
                {
                    keyTouchCombine = -1;
                    selected = (GameCanvas.isTouch ? (-1) : 0);
                    InfoDlg.showWait();
                    Service.gI().combine(1, vItemCombine);
                    return;
                }
                if (selected > vItemCombine.size() - 1)
                {
                    return;
                }
                currItem = (Item)GameCanvas.panel.vItemCombine.elementAt(selected);
                MyVector myVector = new MyVector();
                // Ghi ro la mon do se ve HANH TRANG.
                //
                // Nhan cu la "Lay ra", khong noi ra dau. Mon trong khung nay von
                // chua roi hanh trang — day chi la danh sach da chon — nen "cat
                // vao hanh trang" moi dung viec that su xay ra.
                myVector.addElement(new Command("Cất vào hành trang", this, 6001,
                        currItem));
                if (currItem != null)
                {
                    GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                    addItemDetail(currItem);
                }
                else
                {
                    cp = null;
                }
            }
            if (currentTabIndex == 1)
            {
                doFireInventory();
            }
        }
    
        private void doFirePlayerMenu()
        {
            if (selected != -1)
            {
                isSelectPlayerMenu = true;
                hide();
            }
        }
    
        private void doFireShop()
        {
            currItem = null;
            if (selected < 0)
            {
                return;
            }
            MyVector myVector = new MyVector();
            if (currentTabIndex < currentTabName.Length - ((GameCanvas.panel2 == null) ? 1 : 0) && type != 17)
            {
                currItem = Char.myCharz().arrItemShop[currentTabIndex][selected];
                if (currItem != null)
                {
                    if (currItem.isBuySpec)
                    {
                        if (currItem.buySpec > 0)
                        {
                            myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buySpec), this, 3005, currItem));
                        }
                    }
                    else if (typeShop == 4)
                    {
                        myVector.addElement(new Command(mResources.receive_upper, this, 30001, currItem));
                        myVector.addElement(new Command(mResources.DELETE, this, 30002, currItem));
                        myVector.addElement(new Command(mResources.receive_all, this, 30003, currItem));
                    }
                    else if (currItem.buyCoin == 0 && currItem.buyGold == 0)
                    {
                        if (currItem.powerRequire != 0)
                        {
                            myVector.addElement(new Command(mResources.learn_with + "\n" + Res.formatNumber(currItem.powerRequire) + " \n" + mResources.potential, this, 3004, currItem));
                        }
                        else
                        {
                            myVector.addElement(new Command(mResources.receive_upper + "\n" + mResources.free, this, 3000, currItem));
                        }
                    }
                    else if (typeShop == 8)
                    {
                        if (currItem.buyCoin > 0)
                        {
                            myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyCoin) + "\n" + mResources.XU, this, 30001, currItem));
                        }
                        if (currItem.buyGold > 0)
                        {
                            myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyGold) + "\n" + mResources.LUONG, this, 30002, currItem));
                        }
                    }
                    else if (typeShop != 2)
                    {
                        if (currItem.buyCoin > 0)
                        {
                            myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyCoin) + "\n" + mResources.XU, this, 3000, currItem));
                        }
                        if (currItem.buyGold > 0)
                        {
                            myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyGold) + "\n" + mResources.LUONG, this, 3001, currItem));
                        }
                    }
                    else
                    {
                        if (currItem.buyCoin != -1)
                        {
                            myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyCoin) + "\n" + mResources.XU, this, 10016, currItem));
                        }
                        if (currItem.buyGold != -1)
                        {
                            myVector.addElement(new Command(mResources.buy_with + "\n" + Res.formatNumber2(currItem.buyGold) + "\n" + "Hồng Ngọc", this, 10017, currItem));
                        }
                    }
                }
            }
            else if (typeShop == 0)
            {
                if (selected == 0)
                {
                    setNewSelected(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length, false);
                }
                else
                {
                    currItem = null;
                    if (!GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody))
                    {
                        Item item = Char.myCharz().arrItemBag[GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)];
                        if (item != null)
                        {
                            currItem = item;
                        }
                    }
                    else
                    {
                        Item item2 = Char.myCharz().arrItemBody[GetInventorySelect_body(selected, newSelected)];
                        if (item2 != null)
                        {
                            currItem = item2;
                        }
                    }
                    if (currItem != null)
                    {
                        myVector.addElement(new Command(mResources.SALE, this, 3002, currItem));
                    }
                }
            }
            else
            {
                if (type == 17)
                {
                    currItem = Char.myCharz().arrItemShop[4][selected];
                }
                else
                {
                    currItem = Char.myCharz().arrItemShop[currentTabIndex][selected];
                }
                if (currItem.buyType == 0)
                {
                    if (currItem.isHaveOption(87))
                    {
                        myVector.addElement(new Command(mResources.kiguiLuong, this, 10013, currItem));
                    }
                    else
                    {
                        myVector.addElement(new Command(mResources.kiguiXu, this, 10012, currItem));
                    }
                }
                else if (currItem.buyType == 1)
                {
                    myVector.addElement(new Command(mResources.huykigui, this, 10014, currItem));
                    myVector.addElement(new Command(mResources.upTop, this, 10018, currItem));
                }
                else if (currItem.buyType == 2)
                {
                    myVector.addElement(new Command(mResources.nhantien, this, 10015, currItem));
                }
            }
            if (currItem != null)
            {
                Char.myCharz().setPartTemp(currItem.headTemp, currItem.bodyTemp, currItem.legTemp, currItem.bagTemp);
                if (typeShop != 2)
                {
                    myVector.addElement(new Command("Mua Nhiều", Items.getInstance(), 3, new ItemGroup(currItem.template.id, currItem.indexUI, currItem.buyGold > 0, currItem.buyCoin > 0)));
                    God.NhatKy.ghi("PANEL them muc Mua Nhieu");
                }
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addItemDetail(currItem);
            }
            else
            {
                cp = null;
            }
        }
    
        private void doFireArchivement()
        {
            if (selected >= 0 && Char.myCharz().arrArchive[selected].isFinish && !Char.myCharz().arrArchive[selected].isRecieve)
            {
                if (!GameCanvas.isTouch)
                {
                    Service.gI().getArchivemnt(selected);
                }
                else if (GameCanvas.px > xScroll + wScroll - 40)
                {
                    Service.gI().getArchivemnt(selected);
                }
            }
        }
    
        private void doFireInventory()
        {
            Res.outz("fire inventory");
            if (Char.myCharz().statusMe == 14)
            {
                GameCanvas.startOKDlg(mResources.can_not_do_when_die);
            }
            else
            {
                if (selected == -1)
                {
                    return;
                }
                if (selected == 0)
                {
                    setNewSelected(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length, false);
                    return;
                }
                currItem = null;
                MyVector myVector = new MyVector();
                if (isnewInventory && isTabInven())
                {
                    currItem = itemInvenNew;
                    if (newSelected == 0)
                    {
                        myVector.addElement(new Command(mResources.GETOUT, this, 2002, currItem));
                    }
                    else if (GameCanvas.panel.type == 12)
                    {
                        myVector.addElement(new Command(mResources.use_for_combine, this, 6000, currItem));
                    }
                    else if (GameCanvas.panel.type == 13)
                    {
                        myVector.addElement(new Command(mResources.use_for_trade, this, 7000, currItem));
                    }
                    else if (currItem.isTypeBody())
                    {
                        myVector.addElement(new Command(mResources.USE, this, 2000, currItem));
                        if (Char.myCharz().havePet)
                        {
                            myVector.addElement(new Command(mResources.MOVEFORPET, this, 2005, currItem));
                        }
                    }
                    else
                    {
                        myVector.addElement(new Command(mResources.USE, this, 2001, currItem));
                    }
                }
                else if (!GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody))
                {
                    Item item = Char.myCharz().arrItemBag[GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)];
                    if (item != null)
                    {
                        currItem = item;
                        if (GameCanvas.panel.type == 12)
                        {
                            myVector.addElement(new Command(mResources.use_for_combine, this, 6000, currItem));
                        }
                        else if (GameCanvas.panel.type == 13)
                        {
                            myVector.addElement(new Command(mResources.use_for_trade, this, 7000, currItem));
                        }
                        else if (item.isTypeBody())
                        {
                            myVector.addElement(new Command(mResources.USE, this, 2000, currItem));
                            if (Char.myCharz().havePet)
                            {
                                myVector.addElement(new Command(mResources.MOVEFORPET, this, 2005, currItem));
                            }
                        }
                        else
                        {
                            myVector.addElement(new Command(mResources.USE, this, 2001, currItem));
                            themDungNhanh(myVector, currItem);

                            if ((currItem.template.type == 27 || currItem.template.type == 29) && currItem.template.id != 457)
                            {
                                if (!Items.getInstance().checkContains(currItem.template.id))
                                {
                                    myVector.addElement(new Command("Auto Dùng", Items.getInstance(), 1, new ItemGroup(currItem.template.id, currItem.indexUI, false, false)));
                                }
                                else
                                {
                                    myVector.addElement(new Command("Ngưng Dùng", Items.getInstance(), 2, currItem.template.id));
                                }
                            }
                        }
                    }
                }
                else
                {
                    Item item2 = Char.myCharz().arrItemBody[GetInventorySelect_body(selected, newSelected)];
                    if (item2 != null)
                    {
                        currItem = item2;
                        myVector.addElement(new Command(mResources.GETOUT, this, 2002, currItem));
                    }
                }
                if (currItem != null)
                {
                    Char.myCharz().setPartTemp(currItem.headTemp, currItem.bodyTemp, currItem.legTemp, currItem.bagTemp);
                    if (GameCanvas.panel.type != 12 && GameCanvas.panel.type != 13)
                    {
                        if (position == 0)
                        {
                            myVector.addElement(new Command(mResources.MOVEOUT, this, 2003, currItem));
                        }
                        if (position == 1)
                        {
                            myVector.addElement(new Command(mResources.SALE, this, 3002, currItem));
                        }
                    }
                    GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                    addItemDetail(currItem);
                }
                else
                {
                    cp = null;
                }
            }
        }
    
        private void doRada()
        {
            hide();
            God.SoSuuTamUI.getInstance().mo();
        }
    
        private void doFireTool()
        {
            if (selected < 0)
            {
                return;
            }
            if (SoundMn.IsDelAcc && selected == strTool.Length - 1)
            {
                Service.gI().sendDelAcc();
                return;
            }
            if (!Char.myCharz().havePet)
            {
                switch (selected)
                {
                    case 0:
                        hide();
                        doRada();
                        break;
                    case 1:
                        Service.gI().openMenu(54);
                        break;
                    case 2:
                        setTypeGameInfo();
                        break;
                    case 3:
                        Service.gI().getFlag(0, -1);
                        InfoDlg.showWait();
                        break;
                    case 4:
                        if (Char.myCharz().statusMe == 14)
                        {
                            GameCanvas.startOKDlg(mResources.can_not_do_when_die);
                        }
                        else
                        {
                            Service.gI().openUIZone();
                        }
                        break;
                    case 5:
                        GameCanvas.endDlg();
                        if (Char.myCharz().checkLuong() < 5)
                        {
                            GameCanvas.startOKDlg(mResources.not_enough_luong_world_channel);
                            break;
                        }
                        if (chatTField == null)
                        {
                            chatTField = new ChatTextField();
                            chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                            chatTField.initChatTextField();
                            chatTField.parentScreen = GameCanvas.panel;
                        }
                        chatTField.strChat = mResources.world_channel_5_luong;
                        chatTField.tfChat.name = mResources.CHAT;
                        chatTField.to = string.Empty;
                        chatTField.isShow = true;
                        chatTField.tfChat.isFocus = true;
                        chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                        if (Main.isWindowsPhone)
                        {
                            chatTField.tfChat.strInfo = chatTField.strChat;
                        }
                        if (!Main.isPC)
                        {
                            chatTField.startChat2(this, string.Empty);
                        }
                        else if (GameCanvas.isTouch)
                        {
                            chatTField.tfChat.doChangeToTextBox();
                        }
                        break;
                    case 6:
                        setTypeAccount();
                        break;
                    case 7:
                        setTypeOption();
                        break;
                    case 8:
                        GameCanvas.loginScr.backToRegister();
                        break;
                    case 9:
                        if (GameCanvas.loginScr.isLogin2)
                        {
                            SoundMn.gI().backToRegister();
                        }
                        break;
                }
                return;
            }
            switch (selected)
            {
                case 0:
                    hide();
                    doRada();
                    break;
                case 1:
                    Service.gI().openMenu(54);
                    break;
                case 2:
                    setTypeGameInfo();
                    break;
                case 3:
                    doFirePet();
                    break;
                case 4:
                    Service.gI().getFlag(0, -1);
                    InfoDlg.showWait();
                    break;
                case 5:
                    if (Char.myCharz().statusMe == 14)
                    {
                        GameCanvas.startOKDlg(mResources.can_not_do_when_die);
                    }
                    else
                    {
                        Service.gI().openUIZone();
                    }
                    break;
                case 6:
                    GameCanvas.endDlg();
                    if (Char.myCharz().checkLuong() < 5)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_luong_world_channel);
                        break;
                    }
                    if (chatTField == null)
                    {
                        chatTField = new ChatTextField();
                        chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                        chatTField.initChatTextField();
                        chatTField.parentScreen = GameCanvas.panel;
                    }
                    chatTField.strChat = mResources.world_channel_5_luong;
                    chatTField.tfChat.name = mResources.CHAT;
                    chatTField.to = string.Empty;
                    chatTField.isShow = true;
                    chatTField.tfChat.isFocus = true;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    if (Main.isWindowsPhone)
                    {
                        chatTField.tfChat.strInfo = chatTField.strChat;
                    }
                    if (!Main.isPC)
                    {
                        chatTField.startChat2(this, string.Empty);
                    }
                    else if (GameCanvas.isTouch)
                    {
                        chatTField.tfChat.doChangeToTextBox();
                    }
                    break;
                case 7:
                    setTypeAccount();
                    break;
                case 8:
                    setTypeOption();
                    break;
                case 9:
                    GameCanvas.loginScr.backToRegister();
                    break;
                case 10:
                    if (GameCanvas.loginScr.isLogin2)
                    {
                        SoundMn.gI().backToRegister();
                    }
                    break;
            }
        }
    
        private void setTypeGameSubInfo()
        {
            string content = ((GameInfo)vGameInfo.elementAt(infoSelect)).content;
            contenInfo = mFont.tahoma_7_grey.splitFontArray(content, wScroll - 40);
            currentListLength = contenInfo.Length;
            ITEM_HEIGHT = 16;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            type = 24;
            setType(0);
        }
    
        private void setTypeGameInfo()
        {
            currentListLength = vGameInfo.size();
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            type = 23;
            setType(0);
        }
    
        private void doFirePet()
        {
            InfoDlg.showWait();
            Service.gI().petInfo();
            if (GameCanvas.w >= 2 * Panel.WIDTH_PANEL)
            {
                GameCanvas.panel2 = new Panel();
                GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                GameCanvas.panel2.setTypeBodyOnly();
                GameCanvas.panel2.show();
                GameCanvas.panel.setTypePetMain();
                GameCanvas.panel.show();
            }
            else
            {
                GameCanvas.panel.tabName[21] = mResources.petMainTab;
                GameCanvas.panel.setTypePetMain();
                GameCanvas.panel.show();
            }
            timeShow = 20;
        }
    
        private void searchClan()
        {
            chatTField.strChat = mResources.input_clan_name;
            chatTField.tfChat.name = mResources.clan_name;
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.isFocus = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            if (Main.isWindowsPhone)
            {
                chatTField.tfChat.strInfo = chatTField.strChat;
            }
            if (!Main.isPC)
            {
                chatTField.startChat2(this, string.Empty);
            }
        }
    
        private void chatClan()
        {
            chatTField.strChat = mResources.chat_clan;
            chatTField.tfChat.name = mResources.CHAT;
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.isFocus = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            if (Main.isWindowsPhone)
            {
                chatTField.tfChat.strInfo = chatTField.strChat;
            }
            if (!Main.isPC)
            {
                chatTField.startChat2(this, string.Empty);
            }
        }
    
        public void creatClan()
        {
            chatTField.strChat = mResources.input_clan_name_to_create;
            chatTField.tfChat.name = mResources.input_clan_name;
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            if (Main.isWindowsPhone)
            {
                chatTField.tfChat.strInfo = chatTField.strChat;
            }
            if (!Main.isPC)
            {
                chatTField.startChat2(this, string.Empty);
            }
        }
    
        public void putMoney()
        {
            if (chatTField == null)
            {
                chatTField = new ChatTextField();
                chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                chatTField.initChatTextField();
                chatTField.parentScreen = GameCanvas.panel;
            }
            chatTField.strChat = mResources.input_money_to_trade;
            chatTField.tfChat.name = mResources.input_money;
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
            chatTField.tfChat.setMaxTextLenght(10);
            if (GameCanvas.isTouch)
            {
                chatTField.tfChat.doChangeToTextBox();
            }
            if (Main.isWindowsPhone)
            {
                chatTField.tfChat.strInfo = chatTField.strChat;
            }
            if (!Main.isPC)
            {
                chatTField.startChat2(this, string.Empty);
            }
        }
    
        public void putQuantily()
        {
            if (chatTField == null)
            {
                chatTField = new ChatTextField();
                chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                chatTField.initChatTextField();
                chatTField.parentScreen = GameCanvas.panel;
            }
            chatTField.strChat = mResources.input_quantity_to_trade;
            chatTField.tfChat.name = mResources.input_quantity;
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
            if (GameCanvas.isTouch)
            {
                chatTField.tfChat.doChangeToTextBox();
            }
            if (Main.isWindowsPhone)
            {
                chatTField.tfChat.strInfo = chatTField.strChat;
            }
            if (!Main.isPC)
            {
                chatTField.startChat2(this, string.Empty);
            }
        }
    
        public void chagenSlogan()
        {
            chatTField.strChat = mResources.input_clan_slogan;
            chatTField.tfChat.name = mResources.input_clan_slogan;
            chatTField.to = string.Empty;
            chatTField.isShow = true;
            chatTField.tfChat.isFocus = true;
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            if (Main.isWindowsPhone)
            {
                chatTField.tfChat.strInfo = chatTField.strChat;
            }
            if (!Main.isPC)
            {
                chatTField.startChat2(this, string.Empty);
            }
        }
    
        public void changeIcon()
        {
            if (tabIcon == null)
            {
                tabIcon = new TabClanIcon();
            }
            tabIcon.text = chatTField.tfChat.getText();
            tabIcon.show(false);
            chatTField.isShow = false;
        }
    
        private void addFriend(InfoItem info)
        {
            string text = "|0|1|" + info.charInfo.cName;
            text += "\n";
            text = ((!info.isOnline) ? (text + "|3|1|" + mResources.is_offline) : (text + "|4|1|" + mResources.is_online));
            text += "\n--";
            string text2 = text;
            text = text2 + "\n|5|" + mResources.power + ": " + info.s;
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            charInfo = info.charInfo;
            currItem = null;
        }
    
        private void doFireEnemy()
        {
            if (selected >= 0 && vEnemy.size() != 0)
            {
                MyVector myVector = new MyVector();
                currInfoItem = selected;
                myVector.addElement(new Command(mResources.REVENGE, this, 10000, (InfoItem)vEnemy.elementAt(currInfoItem)));
                myVector.addElement(new Command(mResources.DELETE, this, 10001, (InfoItem)vEnemy.elementAt(currInfoItem)));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addFriend((InfoItem)vEnemy.elementAt(selected));
            }
        }
    
        private void doFireFriend()
        {
            if (selected >= 0 && vFriend.size() != 0)
            {
                MyVector myVector = new MyVector();
                currInfoItem = selected;
                myVector.addElement(new Command(mResources.CHAT, this, 8001, (InfoItem)vFriend.elementAt(currInfoItem)));
                myVector.addElement(new Command(mResources.DELETE, this, 8002, (InfoItem)vFriend.elementAt(currInfoItem)));
                myVector.addElement(new Command(mResources.den, this, 8004, (InfoItem)vFriend.elementAt(currInfoItem)));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addFriend((InfoItem)vFriend.elementAt(selected));
            }
        }
    
        private void doFireChangeFlag()
        {
            if (selected >= 0)
            {
                MyVector myVector = new MyVector();
                currInfoItem = selected;
                myVector.addElement(new Command(mResources.change_flag, this, 10030, null));
                myVector.addElement(new Command(mResources.BACK, this, 10031, null));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
            }
        }
    
        private void doFireLogMessage()
        {
            if (selected == 0)
            {
                isViewChatServer = !isViewChatServer;
                Rms.saveRMSInt("viewchat", isViewChatServer ? 1 : 0);
                if (GameCanvas.isTouch)
                {
                    selected = -1;
                }
            }
            else if (selected >= 0 && logChat.size() != 0)
            {
                MyVector myVector = new MyVector();
                currInfoItem = selected - 1;
                myVector.addElement(new Command(mResources.CHAT, this, 8001, (InfoItem)logChat.elementAt(currInfoItem)));
                myVector.addElement(new Command(mResources.make_friend, this, 8003, (InfoItem)logChat.elementAt(currInfoItem)));
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addLogMessage((InfoItem)logChat.elementAt(selected - 1));
            }
        }
    
        private void doFireClanOption()
        {
            try
            {
                partID = null;
                charInfo = null;
                Res.outz("cSelect= " + cSelected);
                if (selected < 0)
                {
                    cSelected = -1;
                    return;
                }
                if (Char.myCharz().clan == null)
                {
                    if (selected == 0)
                    {
                        if (cSelected == 0)
                        {
                            searchClan();
                        }
                        else if (cSelected == 1)
                        {
                            InfoDlg.showWait();
                            creatClan();
                            Service.gI().getClan(1, -1, null);
                        }
                    }
                    else if (selected != -1)
                    {
                        if (selected == 1)
                        {
                            if (isSearchClan)
                            {
                                Service.gI().searchClan(string.Empty);
                            }
                            else if (isViewMember && currClan != null)
                            {
                                GameCanvas.startYesNoDlg(mResources.do_u_want_join_clan + currClan.name, new Command(mResources.YES, this, 4000, currClan), new Command(mResources.NO, this, 4005, currClan));
                            }
                        }
                        else if (isSearchClan)
                        {
                            currClan = getCurrClan();
                            if (currClan != null)
                            {
                                MyVector myVector = new MyVector();
                                myVector.addElement(new Command(mResources.request_join_clan, this, 4000, currClan));
                                myVector.addElement(new Command(mResources.view_clan_member, this, 4001, currClan));
                                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                                addClanDetail(getCurrClan());
                            }
                        }
                        else if (isViewMember)
                        {
                            currMem = getCurrMember();
                            if (currMem != null)
                            {
                                MyVector myVector2 = new MyVector();
                                myVector2.addElement(new Command(mResources.CLOSE, this, 8000, currClan));
                                GameCanvas.menu.startAt(myVector2, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                                GameCanvas.menu.startAt(myVector2, 0, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                                addClanMemberDetail(currMem);
                            }
                        }
                    }
                }
                else if (selected == 0)
                {
                    if (isMessage)
                    {
                        if (cSelected == 0)
                        {
                            if (myMember.size() > 1)
                            {
                                chatClan();
                            }
                            else
                            {
                                member = null;
                                isSearchClan = false;
                                isViewMember = true;
                                isMessage = false;
                                currentListLength = myMember.size() + 2;
                                initTabClans();
                            }
                        }
                        if (cSelected == 1)
                        {
                            Service.gI().clanMessage(1, null, -1);
                        }
                        if (cSelected == 2)
                        {
                            member = null;
                            isSearchClan = false;
                            isViewMember = true;
                            isMessage = false;
                            currentListLength = myMember.size() + 2;
                            initTabClans();
                            getCurrClanOtion();
                        }
                    }
                    else if (isViewMember)
                    {
                        if (cSelected == 0)
                        {
                            isSearchClan = false;
                            isViewMember = false;
                            isMessage = true;
                            currentListLength = ClanMessage.vMessage.size() + 2;
                            initTabClans();
                        }
                        if (cSelected == 1)
                        {
                            if (myMember.size() > 1)
                            {
                                Service.gI().leaveClan();
                            }
                            else
                            {
                                chagenSlogan();
                            }
                        }
                        if (cSelected == 2)
                        {
                            if (myMember.size() > 1)
                            {
                                chagenSlogan();
                            }
                            else
                            {
                                Service.gI().getClan(3, -1, null);
                            }
                        }
                        if (cSelected == 3)
                        {
                            Service.gI().getClan(3, -1, null);
                        }
                    }
                }
                else if (selected == 1)
                {
                    if (isSearchClan)
                    {
                        Service.gI().searchClan(string.Empty);
                    }
                }
                else if (isSearchClan)
                {
                    currClan = getCurrClan();
                    if (currClan != null)
                    {
                        MyVector myVector3 = new MyVector();
                        myVector3.addElement(new Command(mResources.view_clan_member, this, 4001, currClan));
                        GameCanvas.menu.startAt(myVector3, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addClanDetail(getCurrClan());
                    }
                }
                else if (isViewMember)
                {
                    Res.outz("TOI DAY 1");
                    currMem = getCurrMember();
                    if (currMem != null)
                    {
                        MyVector myVector4 = new MyVector();
                        Res.outz("TOI DAY 2");
                        if (member != null)
                        {
                            myVector4.addElement(new Command(mResources.CLOSE, this, 8000, null));
                            Res.outz("TOI DAY 3");
                        }
                        else if (myMember != null)
                        {
                            Res.outz("TOI DAY 4");
                            Res.outz("my role= " + Char.myCharz().role);
                            if (Char.myCharz().charID == currMem.ID || Char.myCharz().role == 2)
                            {
                                myVector4.addElement(new Command(mResources.CLOSE, this, 8000, currMem));
                            }
                            if (Char.myCharz().role < 2 && Char.myCharz().charID != currMem.ID)
                            {
                                Res.outz("TOI DAY");
                                if (currMem.role == 0 || currMem.role == 1)
                                {
                                    myVector4.addElement(new Command(mResources.CLOSE, this, 8000, currMem));
                                }
                                if (currMem.role == 2)
                                {
                                    myVector4.addElement(new Command(mResources.create_clan_co_leader, this, 5002, currMem));
                                }
                                if (Char.myCharz().role == 0)
                                {
                                    myVector4.addElement(new Command(mResources.create_clan_leader, this, 5001, currMem));
                                    if (currMem.role == 1)
                                    {
                                        myVector4.addElement(new Command(mResources.disable_clan_mastership, this, 5003, currMem));
                                    }
                                }
                            }
                            if (Char.myCharz().role < currMem.role)
                            {
                                myVector4.addElement(new Command(mResources.kick_clan_mem, this, 5004, currMem));
                            }
                        }
                        GameCanvas.menu.startAt(myVector4, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addClanMemberDetail(currMem);
                    }
                }
                else if (isMessage)
                {
                    currMess = getCurrMessage();
                    if (currMess != null)
                    {
                        if (currMess.type == 0)
                        {
                            MyVector myVector5 = new MyVector();
                            myVector5.addElement(new Command(mResources.CLOSE, this, 8000, currMess));
                            GameCanvas.menu.startAt(myVector5, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                            addMessageDetail(currMess);
                        }
                        else if (currMess.type == 1)
                        {
                            if (currMess.playerId != Char.myCharz().charID && cSelected != -1)
                            {
                                Service.gI().clanDonate(currMess.id);
                            }
                        }
                        else if (currMess.type == 2 && currMess.option != null)
                        {
                            if (cSelected == 0)
                            {
                                Service.gI().joinClan(currMess.id, 1);
                            }
                            else if (cSelected == 1)
                            {
                                Service.gI().joinClan(currMess.id, 0);
                            }
                        }
                    }
                }
                if (GameCanvas.isTouch)
                {
                    cSelected = -1;
                    selected = -1;
                }
            }
            catch (Exception)
            {
                throw;
            }
        }
    
        private void doFireMain()
        {
            try
            {
                if (currentTabIndex == 0)
                {
                    setTypeMap();
                }
                if (currentTabIndex == 1)
                {
                    doFireInventory();
                }
                if (currentTabIndex == 2)
                {
                    doFireSkill();
                }
                if (currentTabIndex == 3)
                {
                    if (mainTabName.Length == 4)
                    {
                        doFireTool();
                    }
                    else
                    {
                        doFireClanOption();
                    }
                }
                if (currentTabIndex == 4)
                {
                    doFireTool();
                }
            }
            catch (Exception ex)
            {
                Res.outz("Throw ex " + ex.StackTrace);
            }
        }
    
    
        /// <summary>
        /// Mở luồng nâng chỉ số bằng tiềm năng cho chỉ số thứ <paramref name="chiSo"/>.
        /// </summary>
        /// <remarks>
        /// <para>Lối vào cho màn nhân vật kiểu mới (<c>God.TuiUI</c>). Nó gọi lại
        /// đúng <see cref="doFireSkill"/> chứ không tự dựng lại phần nâng điểm:
        /// bảng giá ở đó là bậc thang ba mức (×1, ×10, ×100) với năm nhánh điều
        /// kiện và hộp xác nhận riêng cho từng mức. Viết lại mà lệch một nhánh là
        /// người chơi mất tiềm năng và không có gì báo.</para>
        ///
        /// <para><c>doFireSkill</c> đọc chỉ số cần nâng từ <c>selected</c>, và
        /// <c>perform(9000/9006/9007)</c> sau đó cũng đọc lại chính biến đó — nên
        /// phải đặt nó trước khi gọi, và không được đặt gì khác vào giữa.</para>
        ///
        /// <para>Thứ tự chỉ số: 0 HP · 1 KI · 2 sức đánh · 3 giáp · 4 chí mạng.</para>
        /// </remarks>
        public void napTiemNang(int chiSo)
        {
            selected = chiSo;
            doFireSkill();
        }

        private void doFireSkill()
        {
            if (selected < 0)
            {
                return;
            }
            if (Char.myCharz().statusMe == 14)
            {
                GameCanvas.startOKDlg(mResources.can_not_do_when_die);
                return;
            }
            if (selected == 0 || selected == 1 || selected == 2 || selected == 3 || selected == 4 || selected == 5)
            {
                long cTiemNang = Char.myCharz().cTiemNang;
                long cHPGoc = (long)Char.myCharz().cHPGoc;
                long cMPGoc = Char.myCharz().cMPGoc;
                long cDamGoc = Char.myCharz().cDamGoc;
                int cDefGoc = Char.myCharz().cDefGoc;
                int cCriticalGoc = Char.myCharz().cCriticalGoc;
                int num = 0;
                int num2 = 1000;
                if (selected == 0)
                {
                    if (cTiemNang < Char.myCharz().cHPGoc + num2)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Char.myCharz().cTiemNang + mResources.not_enough_potential_point2 + (Char.myCharz().cHPGoc + num2), false);
                        return;
                    }
                    if (cTiemNang > cHPGoc && cTiemNang < 10 * (2 * (cHPGoc + num2) + 180) / 2)
                    {
                        GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + (cHPGoc + num2) + mResources.use_potential_point_for2 + Char.myCharz().hpFrom1000TiemNang + mResources.for_HP, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                        return;
                    }
                    if (cTiemNang >= 10 * (2 * (cHPGoc + num2) + 180) / 2 && cTiemNang < 100 * (2 * (cHPGoc + num2) + 1980) / 2)
                    {
                        MyVector myVector = new MyVector(string.Empty);
                        myVector.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(cHPGoc + num2), this, 9000, null));
                        myVector.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(10 * (2 * (cHPGoc + num2) + 180) / 2), this, 9006, null));
                        myVector.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), 1, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                    if (cTiemNang >= 100 * (2 * (cHPGoc + num2) + 1980) / 2)
                    {
                        MyVector myVector2 = new MyVector(string.Empty);
                        myVector2.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(cHPGoc + num2), this, 9000, null));
                        myVector2.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(10 * (2 * (cHPGoc + num2) + 180) / 2), this, 9006, null));
                        myVector2.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().hpFrom1000TiemNang + mResources.HP + "\n-" + Res.formatNumber2(100 * (2 * (cHPGoc + num2) + 1980) / 2), this, 9007, null));
                        myVector2.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), 1, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector2, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                }
                if (selected == 1)
                {
                    if (Char.myCharz().cTiemNang < Char.myCharz().cMPGoc + num2)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Char.myCharz().cTiemNang + mResources.not_enough_potential_point2 + (Char.myCharz().cMPGoc + num2));
                        return;
                    }
                    if (cTiemNang > cMPGoc && cTiemNang < 10 * (2 * (cMPGoc + num2) + 180) / 2)
                    {
                        GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + (cMPGoc + num2) + mResources.use_potential_point_for2 + Char.myCharz().mpFrom1000TiemNang + mResources.for_KI, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                        return;
                    }
                    if (cTiemNang >= 10 * (2 * (cMPGoc + num2) + 180) / 2 && cTiemNang < 100 * (2 * (cMPGoc + num2) + 1980) / 2)
                    {
                        MyVector myVector3 = new MyVector(string.Empty);
                        myVector3.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(cHPGoc + num2), this, 9000, null));
                        myVector3.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(10 * (2 * (cHPGoc + num2) + 180) / 2), this, 9006, null));
                        myVector3.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), 2, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector3, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                    if (cTiemNang >= 100 * (2 * (cMPGoc + num2) + 1980) / 2)
                    {
                        MyVector myVector4 = new MyVector(string.Empty);
                        myVector4.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(cMPGoc + num2), this, 9000, null));
                        myVector4.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(10 * (2 * (cMPGoc + num2) + 180) / 2), this, 9006, null));
                        myVector4.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().mpFrom1000TiemNang + mResources.KI + "\n-" + Res.formatNumber2(100 * (2 * (cMPGoc + num2) + 1980) / 2), this, 9007, null));
                        myVector4.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), 2, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector4, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                }
                if (selected == 2)
                {
                    if (Char.myCharz().cTiemNang < Char.myCharz().cDamGoc * Char.myCharz().expForOneAdd)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Char.myCharz().cTiemNang + mResources.not_enough_potential_point2 + cDamGoc * 100);
                        return;
                    }
                    if (cTiemNang > cDamGoc && cTiemNang < 10 * (2 * cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd)
                    {
                        GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + cDamGoc * 100 + mResources.use_potential_point_for2 + Char.myCharz().damFrom1000TiemNang + mResources.for_hit_point, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                        return;
                    }
                    if (cTiemNang >= 10 * (2 * cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd && cTiemNang < 100 * (2 * cDamGoc + 99) / 2 * Char.myCharz().expForOneAdd)
                    {
                        MyVector myVector5 = new MyVector(string.Empty);
                        myVector5.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(cDamGoc * 100), this, 9000, null));
                        myVector5.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(10 * (2 * cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd), this, 9006, null));
                        myVector5.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), selected + 1, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector5, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                    if (cTiemNang >= 100 * (2 * cDamGoc + 99) / 2 * Char.myCharz().expForOneAdd)
                    {
                        MyVector myVector6 = new MyVector(string.Empty);
                        myVector6.addElement(new Command(mResources.increase_upper + "\n" + Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(cDamGoc * 100), this, 9000, null));
                        myVector6.addElement(new Command(mResources.increase_upper + "\n" + 10 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(10 * (2 * cDamGoc + 9) / 2 * Char.myCharz().expForOneAdd), this, 9006, null));
                        myVector6.addElement(new Command(mResources.increase_upper + "\n" + 100 * Char.myCharz().damFrom1000TiemNang + "\n" + mResources.hit_point + "\n-" + Res.formatNumber2(100 * (2 * cDamGoc + 99) / 2 * Char.myCharz().expForOneAdd), this, 9007, null));
                        myVector6.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), selected + 1, TypePoint.Master));
                        GameCanvas.menu.startAt(myVector6, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                        addSkillDetail2(selected);
                    }
                }
                if (selected == 3)
                {
                    if (Char.myCharz().cTiemNang < 50000 + Char.myCharz().cDefGoc * 1000)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + NinjaUtil.getMoneys(Char.myCharz().cTiemNang) + mResources.not_enough_potential_point2 + NinjaUtil.getMoneys(50000 + Char.myCharz().cDefGoc * 1000));
                        return;
                    }
                    long number = (long)(2 * (cDefGoc + 5)) / 2L * 100000;
                    long number2 = 10L * (long)(2 * (cDefGoc + 5) + 9) / 2 * 100000;
                    long number3 = 100L * (long)(2 * (cDefGoc + 5) + 99) / 2 * 100000;
                    mResources.use_potential_point_for1 = mResources.increase_upper;
                    MyVector myVector7 = new MyVector(string.Empty);
                    myVector7.addElement(new Command(mResources.use_potential_point_for1 + "\n1 " + mResources.armor + "\n" + Res.formatNumber2(number), this, 9000, null));
                    myVector7.addElement(new Command(mResources.use_potential_point_for1 + "\n10 " + mResources.armor + "\n" + Res.formatNumber2(number2), this, 9006, null));
                    myVector7.addElement(new Command(mResources.use_potential_point_for1 + "\n100 " + mResources.armor + "\n" + Res.formatNumber2(number3), this, 9007, null));
                    myVector7.addElement(new Command("Cộng Nhanh", NPoint.getInstance(), selected + 1, TypePoint.Master));
                    GameCanvas.menu.startAt(myVector7, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                    addSkillDetail2(selected);
                }
                else if (selected == 4)
                {
                    long num3 = 50000000L;
                    int num4 = Char.myCharz().cCriticalGoc;
                    if (num4 > t_tiemnang.Length - 1)
                    {
                        num4 = t_tiemnang.Length - 1;
                    }
                    num3 = t_tiemnang[num4];
                    if (Char.myCharz().cTiemNang < num3)
                    {
                        GameCanvas.startOKDlg(mResources.not_enough_potential_point1 + Res.formatNumber2(Char.myCharz().cTiemNang) + mResources.not_enough_potential_point2 + Res.formatNumber2(num3));
                        return;
                    }
                    GameCanvas.startYesNoDlg(mResources.use_potential_point_for1 + Res.formatNumber(num3) + mResources.use_potential_point_for2 + Char.myCharz().criticalFrom1000Tiemnang + mResources.for_crit, new Command(mResources.increase_upper, this, 9000, null), new Command(mResources.CANCEL, this, 4007, null));
                }
                else if (selected == 5)
                {
                    Service.gI().speacialSkill(0);
                }
                return;
            }
            int num5 = selected - 6;
            SkillTemplate skillTemplate = Char.myCharz().nClass.skillTemplates[num5];
            Skill skill = Char.myCharz().getSkill(skillTemplate);
            Skill skill2 = null;
            MyVector myVector8 = new MyVector(string.Empty);
            if (skill != null)
            {
                if (skill.point == skillTemplate.maxPoint)
                {
                    myVector8.addElement(new Command(mResources.make_shortcut, this, 9003, skill.template));
                    myVector8.addElement(new Command(mResources.CLOSE, 2));
                }
                else
                {
                    skill2 = skillTemplate.skills[skill.point];
                    myVector8.addElement(new Command(mResources.UPGRADE, this, 9002, skill2));
                    myVector8.addElement(new Command(mResources.make_shortcut, this, 9003, skill.template));
                }
            }
            else
            {
                skill2 = skillTemplate.skills[0];
                myVector8.addElement(new Command(mResources.learn, this, 9004, skill2));
            }
            GameCanvas.menu.startAt(myVector8, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
            addSkillDetail(skillTemplate, skill, skill2);
        }
        private void addLogMessage(InfoItem info)
        {
            string text = "|0|1|" + info.charInfo.cName;
            text += "\n";
            text += "\n--";
            text = text + "\n|5|" + Res.split(info.s, "|", 0)[2];
            cp = new ChatPopup();
            popUpDetailInit(cp, text);
            charInfo = info.charInfo;
            currItem = null;
        }
    
        private void addSkillDetail2(int type)
        {
            string empty = string.Empty;
            int num = 0;
            if (selected == 0)
            {
                num = (int)(Char.myCharz().cHPGoc + 1000);
            }
            if (selected == 1)
            {
                num = Char.myCharz().cMPGoc + 1000;
            }
            if (selected == 2)
            {
                num = Char.myCharz().cDamGoc * Char.myCharz().expForOneAdd;
            }
            if (selected == 3)
            {
                num = 500000 + Char.myCharz().cDefGoc * 100000;
            }
            string text = empty;
            empty = text + "|5|2|" + mResources.USE + " " + num + " " + mResources.potential;
            if (type == 0)
            {
                empty = empty + "\n|5|2|" + mResources.to_gain_20hp;
            }
            if (type == 1)
            {
                empty = empty + "\n|5|2|" + mResources.to_gain_20mp;
            }
            if (type == 2)
            {
                empty = empty + "\n|5|2|" + mResources.to_gain_1pow;
            }
            if (type == 3)
            {
                empty = empty + "\n|5|2|" + mResources.to_gain_1pow;
            }
            currItem = null;
            partID = null;
            charInfo = null;
            idIcon = -1;
            cp = new ChatPopup();
            popUpDetailInit(cp, empty);
        }
    
        private void doFireClanIcon()
        {
        }
    
        private void doFireMap()
        {
            if (imgMap != null)
            {
                imgMap.texture = null;
                imgMap = null;
            }
            TileMap.lastPlanetId = -1;
            mSystem.gcc();
            SmallImage.loadBigRMS();
            setTypeMain();
            cmx = (cmtoX = 0);
        }
    
        /// <summary>
        /// Chọn xong một bản đồ trong bảng đổi khu / đổi map.
        /// </summary>
        /// <remarks>
        /// <b>Type 14 (Capsule, Ngọc Đen) gửi ngay tại đây</b>, không chờ bảng
        /// trượt hết ra khỏi màn.
        ///
        /// Đường cũ chỉ gửi ở nhánh "đã đóng xong" trong <c>moveCamera</c>, và
        /// nhánh đó đòi một chuỗi điều kiện không liên quan gì tới việc chọn bản
        /// đồ: <c>position == 0</c>, <c>cmx</c> và <c>cmtoX</c> đã về sát
        /// <c>wScroll</c>, và <c>statusMe != 14</c>. Log máy chủ cho thấy đúng
        /// chỗ này hỏng: <c>openCapsuleUI</c> chạy và đặt
        /// <c>typeChangeMap=500</c>, nhưng KHÔNG có gói -91 nào tới — tức client
        /// chưa bao giờ gửi. Gửi ngay lúc bấm thì bỏ hết chuỗi điều kiện đó.
        ///
        /// Vẫn giữ <c>isChangeZone = true</c> vì <c>hide()</c> đọc nó để biết là
        /// đóng bảng hẳn; để false thì <c>hide()</c> lại quay về bảng chính. Cờ
        /// <c>daGuiChonMap</c> lo phần chống gửi lần hai.
        /// </remarks>
        private void doFireZone()
        {
            if (selected != -1)
            {
                Res.outz("FIRE ZONE");
                if (type == 14 && Char.myCharz().cHP > 0)
                {
                    // Gui roi DONG THANG, khong di qua hide() va vong truot.
                    //
                    // Duong truot ra co ba trang thai phai khop nhau —
                    // isChangeZone, isClose, cap cmx/cmtoX — va chung chi duoc go
                    // o nhanh "da dong xong" trong moveCamera. Neu may chu mo lai
                    // bang nay giua luc dang truot (den noi la Cong Phi Thuyen mo
                    // lai bang chon map) thi nhanh do khong bao gio chay: bang
                    // hien tro lai voi isChangeZone con dinh, va cu bam LAN SAU
                    // dat waitToPerform roi treo o day, khong con dispatch nao.
                    //
                    // Dat isShow = false la dut han moi trang thai trung gian.
                    // Nhung viec don o nhanh cu (cleanCombine, isSelectPlayerMenu)
                    // khong lien quan gi den bang chon map.
                    InfoDlg.showWait();
                    Service.gI().requestMapSelect(selected);
                    daGuiChonMap = false;
                    isChangeZone = false;
                    isClose = false;
                    isShow = false;
                    cmx = (cmtoX = 0);
                    return;
                }
                isChangeZone = true;
                GameCanvas.panel.hide();
            }
        }

        /// <summary>Đã gửi gói chọn bản đồ rồi, đừng gửi lại lúc đóng bảng.</summary>
        private bool daGuiChonMap;
    
        public void updateRequest(int recieve, int maxCap)
        {
            cp.says[cp.says.Length - 1] = mResources.received + " " + recieve + "/" + maxCap;
        }
    
        private void doFireBox()
        {
            if (selected < 0)
            {
                return;
            }
            currItem = null;
            MyVector myVector = new MyVector();
            if (currentTabIndex == 0 && !Equals(GameCanvas.panel2))
            {
                if (selected == 0)
                {
                    setNewSelected(Char.myCharz().arrItemBox.Length, false);
                }
                else
                {
                    sbyte b = (sbyte)GetInventorySelect_body(selected, newSelected);
                    Item item = Char.myCharz().arrItemBox[b];
                    if (item != null)
                    {
                        if (isBoxClan)
                        {
                            myVector.addElement(new Command(mResources.GETOUT, this, 1000, item));
                            myVector.addElement(new Command(mResources.USE, this, 2010, item));
                        }
                        else if (item.isTypeBody())
                        {
                            myVector.addElement(new Command(mResources.GETOUT, this, 1000, item));
                        }
                        else
                        {
                            myVector.addElement(new Command(mResources.GETOUT, this, 1000, item));
                        }
                        currItem = item;
                    }
                }
            }
            if (currentTabIndex == 1 || Equals(GameCanvas.panel2))
            {
                if (selected == 0)
                {
                    setNewSelected(Char.myCharz().arrItemBody.Length + Char.myCharz().arrItemBag.Length, true);
                }
                else if (isnewInventory && isTabInven())
                {
                    // LƯỚI Ô VUÔNG: lấy thẳng ô người chơi vừa chạm.
                    //
                    // Nhánh cũ ở dưới suy ra vị trí món từ `selected` bằng phép
                    // tính của bản DANH SÁCH. Ở lưới ô vuông, `selected` không
                    // còn là dòng nào cả: chỗ nhận chạm đặt nó bằng 1 chỉ để
                    // menu chịu mở ra (xem `chonOTaiDiem`), còn ô thật nằm ở
                    // `sellectInventory` / `itemInvenNew`.
                    //
                    // Nên `GetInventorySelect_bag(1, ...)` luôn trả về CÙNG MỘT
                    // ô, bất kể chạm vào món nào. Đó đúng là lỗi "chọn đồ khác
                    // mà vẫn cất Đá thạch anh tím vào rương": menu hiện tên món
                    // vừa chạm, nhưng món bị cất là món của ô cố định kia.
                    //
                    // Hai bảng còn lại đã chữa theo cách này từ trước —
                    // `doFireInventory` lấy `itemInvenNew`, các lệnh 2000/2001/
                    // 2002 lấy `currItem.indexUI` — chỉ bảng rương là bị bỏ sót.
                    currItem = itemInvenNew;
                    if (currItem != null)
                    {
                        if (newSelected == 0)
                        {
                            // Tab Trang bị: cất món đang mặc vào rương.
                            myVector.addElement(new Command(mResources.move_to_chest2, this, 1002, currItem));
                        }
                        else
                        {
                            myVector.addElement(new Command(mResources.move_to_chest, this, 1001, currItem));
                            if (currItem.isTypeBody())
                            {
                                myVector.addElement(new Command(mResources.USE, this, 2000, currItem));
                            }
                            else
                            {
                                myVector.addElement(new Command(mResources.USE, this, 2001, currItem));
                                themDungNhanh(myVector, currItem);
                            }
                        }
                    }
                }
                else
                {
                    Item[] arrItemBody = Char.myCharz().arrItemBody;
                    if (!GetInventorySelect_isbody(selected, newSelected, arrItemBody))
                    {
                        sbyte b2 = (sbyte)GetInventorySelect_bag(selected, newSelected, arrItemBody);
                        Item item2 = Char.myCharz().arrItemBag[b2];
                        if (item2 != null)
                        {
                            myVector.addElement(new Command(mResources.move_to_chest, this, 1001, item2));
                            if (item2.isTypeBody())
                            {
                                myVector.addElement(new Command(mResources.USE, this, 2000, item2));
                            }
                            else
                            {
                                myVector.addElement(new Command(mResources.USE, this, 2001, item2));
                                themDungNhanh(myVector, item2);

                            }
                            currItem = item2;
                        }
                    }
                    else
                    {
                        Item item3 = Char.myCharz().arrItemBody[GetInventorySelect_body(selected, newSelected)];
                        if (item3 != null)
                        {
                            myVector.addElement(new Command(mResources.move_to_chest2, this, 1002, item3));
                            currItem = item3;
                        }
                    }
                }
            }
            if (currItem != null)
            {
                Char.myCharz().setPartTemp(currItem.headTemp, currItem.bodyTemp, currItem.legTemp, currItem.bagTemp);
                if (isBoxClan)
                {
                    myVector.addElement(new Command(mResources.MOVEOUT, this, 2011, currItem));
                }
                GameCanvas.menu.startAt(myVector, X, (selected + 1) * ITEM_HEIGHT - cmy + yScroll);
                addItemDetail(currItem);
            }
            else
            {
                cp = null;
            }
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
        }
    
        public void itemRequest(sbyte itemAction, string info, sbyte where, sbyte index)
        {
            GameCanvas.endDlg();
            ItemObject itemObject = new ItemObject();
            itemObject.type = itemAction;
            itemObject.id = index;
            itemObject.where = where;
            GameCanvas.startYesNoDlg(info, new Command(mResources.YES, this, 2004, itemObject), new Command(mResources.NO, this, 4005, null));
        }
    
        public void saleRequest(sbyte type, string info, short id)
        {
            ItemObject itemObject = new ItemObject();
            itemObject.type = type;
            itemObject.id = id;
            GameCanvas.startYesNoDlg(info, new Command(mResources.YES, this, 3003, itemObject), new Command(mResources.NO, this, 4005, null));
        }
    
        public void perform(int idAction, object p)
        {
            if (idAction == 9999)
            {
                TopInfo topInfo = (TopInfo)p;
                Service.gI().sendThachDau(topInfo.pId);
            }
            if (idAction == 170391)
            {
                Rms.clearAll();
                if (mGraphics.zoomLevel > 1)
                {
                    Rms.saveRMSInt("levelScreenKN", 1);
                }
                else
                {
                    Rms.saveRMSInt("levelScreenKN", 0);
                }
                GameMidlet.instance.exit();
            }
            if (idAction == 6001)
            {
                Item item = (Item)p;
                item.isSelect = false;
                GameCanvas.panel.vItemCombine.removeElement(item);
                if (GameCanvas.panel.currentTabIndex == 0)
                {
                    GameCanvas.panel.setTabCombine();
                }
            }
            if (idAction == 6000)
            {
                Item item2 = (Item)p;
                for (int i = 0; i < GameCanvas.panel.vItemCombine.size(); i++)
                {
                    Item item3 = (Item)GameCanvas.panel.vItemCombine.elementAt(i);
                    if (item3.template.id == item2.template.id)
                    {
                        GameCanvas.startOKDlg(mResources.already_has_item);
                        return;
                    }
                }
                item2.isSelect = true;
                GameCanvas.panel.vItemCombine.addElement(item2);
                if (GameCanvas.panel.currentTabIndex == 0)
                {
                    GameCanvas.panel.setTabCombine();
                }
            }
            if (idAction == 7000)
            {
                if (isLock)
                {
                    GameCanvas.startOKDlg(mResources.unlock_item_to_trade);
                    return;
                }
                Item item4 = (Item)p;
                for (int j = 0; j < GameCanvas.panel.vMyGD.size(); j++)
                {
                    Item item5 = (Item)GameCanvas.panel.vMyGD.elementAt(j);
                    if (item5.indexUI == item4.indexUI)
                    {
                        GameCanvas.startOKDlg(mResources.already_has_item);
                        return;
                    }
                }
                if (item4.quantity > 1)
                {
                    itemChoGiaoDich = item4;
                    putQuantily();
                    return;
                }
                item4.isSelect = true;
                Item item6 = new Item();
                item6.template = item4.template;
                item6.itemOption = item4.itemOption;
                item6.indexUI = item4.indexUI;
                GameCanvas.panel.vMyGD.addElement(item6);
                Service.gI().giaodich(2, -1, (sbyte)item6.indexUI, item6.quantity);
            }
            if (idAction == 7001)
            {
                Item item7 = (Item)p;
                item7.isSelect = false;
                GameCanvas.panel.vMyGD.removeElement(item7);
                if (GameCanvas.panel.currentTabIndex == 1)
                {
                    GameCanvas.panel.setTabGiaoDich(true);
                }
                Service.gI().giaodich(4, -1, (sbyte)item7.indexUI, -1);
            }
            if (idAction == 7002)
            {
                isAccept = true;
                GameCanvas.endDlg();
                Service.gI().giaodich(7, -1, -1, -1);
                hide();
            }
            if (idAction == 8003)
            {
                InfoItem infoItem = (InfoItem)p;
                Service.gI().friend(1, infoItem.charInfo.charID);
                if (type != 8)
                {
                }
            }
            if (idAction == 8002)
            {
                InfoItem infoItem2 = (InfoItem)p;
                Service.gI().friend(2, infoItem2.charInfo.charID);
            }
            if (idAction == 8004)
            {
                InfoItem infoItem3 = (InfoItem)p;
                Service.gI().gotoPlayer(infoItem3.charInfo.charID);
            }
            if (idAction == 8001)
            {
                Res.outz("chat player");
                InfoItem infoItem4 = (InfoItem)p;
                if (chatTField == null)
                {
                    chatTField = new ChatTextField();
                    chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                    chatTField.initChatTextField();
                    chatTField.parentScreen = GameCanvas.panel;
                }
                chatTField.strChat = mResources.chat_player;
                chatTField.tfChat.name = mResources.chat_with + " " + infoItem4.charInfo.cName;
                chatTField.to = string.Empty;
                chatTField.isShow = true;
                chatTField.tfChat.isFocus = true;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                if (Main.isWindowsPhone)
                {
                    chatTField.tfChat.strInfo = chatTField.strChat;
                }
                if (!Main.isPC)
                {
                    chatTField.startChat2(this, string.Empty);
                }
            }
            if (idAction == 1000)
            {
                Service.gI().getItem(BOX_BAG, (sbyte)GetInventorySelect_body(selected, newSelected));
            }
            if (idAction == 1001)
            {
                sbyte id = (sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody);
                // Luoi o vuong: lay chi so o THAT tu chinh mon gan vao lenh.
                // `selected` o luoi khong phai so dong nen phep tinh tren tra ve
                // sai o - xem chu thich trong doFireBox.
                Item monCat = p as Item;
                if (isnewInventory && isTabInven() && monCat != null)
                {
                    id = (sbyte)monCat.indexUI;
                }
                Service.gI().getItem(BAG_BOX, id);
            }
            if (idAction == 1003)
            {
                hide();
            }
            if (idAction == 1002)
            {
                sbyte idMac = (sbyte)GetInventorySelect_body(selected, newSelected);
                Item monMac = p as Item;
                if (isnewInventory && isTabInven() && monMac != null)
                {
                    idMac = (sbyte)monMac.indexUI;
                }
                Service.gI().getItem(BODY_BOX, idMac);
            }
            if (idAction == 2011)
            {
                Service.gI().useItem(1, 2, (sbyte)GetInventorySelect_body(selected, newSelected), -1);
            }
            if (idAction == 2010)
            {
                Service.gI().useItem(0, 2, (sbyte)GetInventorySelect_body(selected, newSelected), -1);
                Item item8 = (Item)p;
                if (item8 != null && (item8.template.id == 193 || item8.template.id == 194))
                {
                    GameCanvas.panel.hide();
                }
            }
            if (idAction == 2000)
            {
                Item[] arrItemBody = Char.myCharz().arrItemBody;
                sbyte id2 = (sbyte)GetInventorySelect_bag(selected, newSelected, arrItemBody);
                if (isnewInventory)
                {
                    id2 = (sbyte)currItem.indexUI;
                }
                Service.gI().getItem(BAG_BODY, id2);
            }
            if (idAction == 2001)
            {
                Res.outz("use item");
                Item item9 = (Item)p;
                bool inventorySelect_isbody = GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody);
                sbyte b = 0;
                b = (inventorySelect_isbody ? ((sbyte)GetInventorySelect_body(selected, newSelected)) : ((sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)));
                if (isnewInventory)
                {
                    b = (sbyte)currItem.indexUI;
                    sbyte where = 0;
                    if (newSelected != 0)
                    {
                        where = 1;
                    }
                    Service.gI().useItem(0, where, b, -1);
                }
                else
                {
                    Service.gI().useItem(0, (!inventorySelect_isbody) ? ((sbyte)1) : ((sbyte)0), b, -1);
                }
                if (item9.template.id == 193 || item9.template.id == 194)
                {
                    GameCanvas.panel.hide();
                }
            }
            if (idAction == 2002)
            {
                if (isnewInventory)
                {
                    Service.gI().getItem(BODY_BAG, (sbyte)sellectInventory);
                }
                else
                {
                    Service.gI().getItem(BODY_BAG, (sbyte)GetInventorySelect_body(selected, newSelected));
                }
            }
            if (idAction == 2003)
            {
                Res.outz("remove item");
                bool inventorySelect_isbody2 = GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody);
                sbyte b2 = 0;
                b2 = (inventorySelect_isbody2 ? ((sbyte)GetInventorySelect_body(selected, newSelected)) : ((sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)));
                Service.gI().useItem(1, (!inventorySelect_isbody2) ? ((sbyte)1) : ((sbyte)0), b2, -1);
            }
            if (idAction == 2004)
            {
                GameCanvas.endDlg();
                ItemObject itemObject = (ItemObject)p;
                sbyte where2 = (sbyte)itemObject.where;
                sbyte index = (sbyte)itemObject.id;
                Service.gI().useItem((sbyte)((itemObject.type != 0) ? 2 : 3), where2, index, -1);
            }
            if (idAction == 2005)
            {
                sbyte id3 = (sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody);
                Service.gI().getItem(BAG_PET, id3);
            }
            if (idAction == 2006)
            {
                Item[] arrItemBody2 = Char.myPetz().arrItemBody;
                sbyte id4 = (sbyte)selected;
                Service.gI().getItem(PET_BAG, id4);
            }
            if (idAction == 30001)
            {
                Res.outz("nhan do");
                Service.gI().buyItem(0, selected, 0);
            }
            if (idAction == 30002)
            {
                Res.outz("xoa do");
                Service.gI().buyItem(1, selected, 0);
            }
            if (idAction == 30003)
            {
                Res.outz("nhan tat");
                Service.gI().buyItem(2, selected, 0);
            }
            if (idAction == 3000)
            {
                Res.outz("mua do");
                Item item10 = (Item)p;
                Service.gI().buyItem(0, item10.template.id, 0);
            }
            if (idAction == 3001)
            {
                Item item11 = (Item)p;
                GameCanvas.msgdlg.pleasewait();
                Service.gI().buyItem(1, item11.template.id, 0);
            }
            if (idAction == 3002)
            {
                GameCanvas.endDlg();

                // Lay o tu LUOI khi dang o ban hanh trang moi.
                //
                // `selected` cua luoi o vuong chi la mot co bao "co o duoc chon",
                // luon bang 1, khong phai chi so o —
                // xem nhanh isnewInventory trong updateKeyScrollView.
                // GetInventorySelect_bag lai doc no nhu chi so DONG cua ban danh
                // sach cu (`select - 1 + subSelect * 20 - soOTrangBi`), nen ban mon
                // nao cung ra dung mot o. Dung nhu loi "ban vat pham luon chon o
                // hang 2 cot 3".
                //
                // `sellectInventory` la chi so o that, do chonOTaiDiem() dat, va
                // newSelected cho biet dang o ban trang bi (0) hay hanh trang (1)
                // — khop dung hai tri "where" ma may chu doi (0 itemsBody,
                // 1 itemsBag, xem ShopService.showConfirmSellItem).
                if (isnewInventory && isTabInven())
                {
                    Service.gI().saleItem(0,
                            (sbyte) ((newSelected == 0) ? 0 : 1),
                            (short) sellectInventory);
                    return;
                }

                bool inventorySelect_isbody3 = GetInventorySelect_isbody(selected, newSelected, Char.myCharz().arrItemBody);
                sbyte b3 = 0;
                b3 = (inventorySelect_isbody3 ? ((sbyte)GetInventorySelect_body(selected, newSelected)) : ((sbyte)GetInventorySelect_bag(selected, newSelected, Char.myCharz().arrItemBody)));
                Service.gI().saleItem(0, (!inventorySelect_isbody3) ? ((sbyte)1) : ((sbyte)0), b3);
            }
            if (idAction == 3003)
            {
                GameCanvas.endDlg();
                ItemObject itemObject2 = (ItemObject)p;
                Service.gI().saleItem(1, (sbyte)itemObject2.type, (short)itemObject2.id);
            }
            if (idAction == 3004)
            {
                Item item12 = (Item)p;
                Service.gI().buyItem(3, item12.template.id, 0);
            }
            if (idAction == 3005)
            {
                Res.outz("mua do");
                Item item13 = (Item)p;
                Service.gI().buyItem(3, item13.template.id, 0);
            }
            if (idAction == 4000)
            {
                Clan clan = (Clan)p;
                if (clan != null)
                {
                    GameCanvas.endDlg();
                    Service.gI().clanMessage(2, null, clan.ID);
                }
            }
            if (idAction == 4001)
            {
                Clan clan2 = (Clan)p;
                if (clan2 != null)
                {
                    InfoDlg.showWait();
                    clanReport = mResources.PLEASEWAIT;
                    Service.gI().clanMember(clan2.ID);
                }
            }
            if (idAction == 4005)
            {
                GameCanvas.endDlg();
            }
            if (idAction == 4007)
            {
                GameCanvas.endDlg();
            }
            if (idAction == 4006)
            {
                ClanMessage clanMessage = (ClanMessage)p;
                Service.gI().clanDonate(clanMessage.id);
            }
            if (idAction == 5001)
            {
                Member member = (Member)p;
                Service.gI().clanRemote(member.ID, 0);
            }
            if (idAction == 5002)
            {
                Member member2 = (Member)p;
                Service.gI().clanRemote(member2.ID, 1);
            }
            if (idAction == 5003)
            {
                Member member3 = (Member)p;
                Service.gI().clanRemote(member3.ID, 2);
            }
            if (idAction == 5004)
            {
                Member member4 = (Member)p;
                Service.gI().clanRemote(member4.ID, -1);
            }
            if (idAction == 9000)
            {
                Service.gI().upPotential(selected, 1);
                GameCanvas.endDlg();
                InfoDlg.showWait();
            }
            if (idAction == 9006)
            {
                Service.gI().upPotential(selected, 10);
                GameCanvas.endDlg();
                InfoDlg.showWait();
            }
            if (idAction == 9007)
            {
                Service.gI().upPotential(selected, 100);
                GameCanvas.endDlg();
                InfoDlg.showWait();
            }
            if (idAction == 9002)
            {
                Skill skill = (Skill)p;
                if (skill.template.isSkillSpec())
                {
                    GameCanvas.startOKDlg(mResources.updSkill);
                }
                else
                {
                    GameCanvas.startOKDlg(mResources.can_buy_from_Uron1 + skill.powRequire + mResources.can_buy_from_Uron2 + skill.moreInfo + mResources.can_buy_from_Uron3);
                }
            }
            if (idAction == 9003)
            {
                if (GameCanvas.isTouch && !Main.isPC)
                {
                    GameScr.gI().doSetOnScreenSkill((SkillTemplate)p);
                }
                else
                {
                    GameScr.gI().doSetKeySkill((SkillTemplate)p);
                }
            }
            if (idAction == 9004)
            {
                Skill skill2 = (Skill)p;
                if (skill2.template.isSkillSpec())
                {
                    GameCanvas.startOKDlg(mResources.learnSkill);
                }
                else
                {
                    GameCanvas.startOKDlg(mResources.can_buy_from_Uron1 + skill2.powRequire + mResources.can_buy_from_Uron2 + skill2.moreInfo + mResources.can_buy_from_Uron3);
                }
            }
            if (idAction == 10000)
            {
                InfoItem infoItem5 = (InfoItem)p;
                Service.gI().enemy(1, infoItem5.charInfo.charID);
                GameCanvas.panel.hideNow();
            }
            if (idAction == 10001)
            {
                InfoItem infoItem6 = (InfoItem)p;
                Service.gI().enemy(2, infoItem6.charInfo.charID);
                InfoDlg.showWait();
            }
            if (idAction == 10021)
            {
            }
            if (idAction == 10012)
            {
                if (chatTField == null)
                {
                    chatTField = new ChatTextField();
                    chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                    chatTField.initChatTextField();
                    chatTField.parentScreen = ((GameCanvas.panel2 != null) ? GameCanvas.panel2 : GameCanvas.panel);
                }
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                chatTField.tfChat.setText(string.Empty);
                if (currItem.quantity == 1)
                {
                    chatTField.strChat = mResources.kiguiXuchat;
                    chatTField.tfChat.name = mResources.input_money;
                }
                else
                {
                    chatTField.strChat = mResources.input_quantity + " ";
                    chatTField.tfChat.name = mResources.input_quantity;
                }
                chatTField.tfChat.setMaxTextLenght(10);
                chatTField.to = string.Empty;
                chatTField.isShow = true;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                if (GameCanvas.isTouch)
                {
                    chatTField.tfChat.doChangeToTextBox();
                }
                if (Main.isWindowsPhone)
                {
                    chatTField.tfChat.strInfo = chatTField.strChat;
                }
                if (!Main.isPC)
                {
                    chatTField.startChat2(this, string.Empty);
                }
            }
            if (idAction == 10013)
            {
                if (chatTField == null)
                {
                    chatTField = new ChatTextField();
                    chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                    chatTField.initChatTextField();
                    chatTField.parentScreen = ((GameCanvas.panel2 != null) ? GameCanvas.panel2 : GameCanvas.panel);
                }
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                chatTField.tfChat.setText(string.Empty);
                if (currItem.quantity == 1)
                {
                    chatTField.strChat = mResources.kiguiLuongchat;
                    chatTField.tfChat.name = mResources.input_money;
                }
                else
                {
                    chatTField.strChat = mResources.input_quantity + "  ";
                    chatTField.tfChat.name = mResources.input_quantity;
                }
                chatTField.to = string.Empty;
                chatTField.isShow = true;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                if (GameCanvas.isTouch)
                {
                    chatTField.tfChat.doChangeToTextBox();
                }
                if (Main.isWindowsPhone)
                {
                    chatTField.tfChat.strInfo = chatTField.strChat;
                }
                if (!Main.isPC)
                {
                    chatTField.startChat2(this, string.Empty);
                }
            }
            if (idAction == 10014)
            {
                Item item14 = (Item)p;
                Service.gI().kigui(1, item14.itemId, -1, -1, -1);
                InfoDlg.showWait();
            }
            if (idAction == 10015)
            {
                Item item15 = (Item)p;
                Service.gI().kigui(2, item15.itemId, -1, -1, -1);
                InfoDlg.showWait();
            }
            if (idAction == 10016)
            {
                Item item16 = (Item)p;
                Service.gI().kigui(3, item16.itemId, 0, item16.buyCoin, -1);
                InfoDlg.showWait();
            }
            if (idAction == 10017)
            {
                Item item17 = (Item)p;
                Service.gI().kigui(3, item17.itemId, 1, item17.buyGold, -1);
                InfoDlg.showWait();
            }
            if (idAction == 10018)
            {
                Item item18 = (Item)p;
                Service.gI().kigui(5, item18.itemId, -1, -1, -1);
                InfoDlg.showWait();
            }
            if (idAction == 10019)
            {
                Session_ME.gI().close();
                Rms.saveRMSString("acc6", string.Empty);
                Rms.saveRMSString("pass6", string.Empty);
                GameCanvas.loginScr.tfPass.setText(string.Empty);
                GameCanvas.loginScr.tfUser.setText(string.Empty);
                GameCanvas.loginScr.isLogin2 = false;
                GameCanvas.loginScr.switchToMe();
                GameCanvas.endDlg();
                hide();
            }
            if (idAction == 10020)
            {
                GameCanvas.endDlg();
            }
            if (idAction == 10030)
            {
                Service.gI().getFlag(1, (sbyte)selected);
                GameCanvas.panel.hideNow();
            }
            if (idAction == 10031)
            {
                Session_ME.gI().close();
            }
            if (idAction == 11000)
            {
                Service.gI().kigui(0, currItem.itemId, 1, currItem.buyRuby, 1);
                GameCanvas.endDlg();
            }
            if (idAction == 11001)
            {
                Service.gI().kigui(0, currItem.itemId, 1, currItem.buyRuby, currItem.quantilyToBuy);
                GameCanvas.endDlg();
            }
            if (idAction == 11002)
            {
                chatTField.isShow = false;
                GameCanvas.endDlg();
            }
        }
    
        public void onChatFromMe(string text, string to)
        {
            if (chatTField.tfChat.getText() == null || chatTField.tfChat.getText().Equals(string.Empty) || text.Equals(string.Empty) || text == null)
            {
                chatTField.isShow = false;
                return;
            }
            if (chatTField.strChat.Equals(mResources.input_clan_name))
            {
                InfoDlg.showWait();
                chatTField.isShow = false;
                Service.gI().searchClan(text);
                return;
            }
            if (chatTField.strChat.Equals(mResources.chat_clan))
            {
                InfoDlg.showWait();
                chatTField.isShow = false;
                Service.gI().clanMessage(0, text, -1);
                return;
            }
            if (chatTField.strChat.Equals(mResources.input_clan_name_to_create))
            {
                if (chatTField.tfChat.getText() == string.Empty)
                {
                    GameScr.info1.addInfo(mResources.clan_name_blank, 0);
                    return;
                }
                if (tabIcon == null)
                {
                    tabIcon = new TabClanIcon();
                }
                tabIcon.text = chatTField.tfChat.getText();
                tabIcon.show(false);
                chatTField.isShow = false;
                return;
            }
            if (chatTField.strChat.Equals(mResources.input_clan_slogan))
            {
                if (chatTField.tfChat.getText() == string.Empty)
                {
                    GameScr.info1.addInfo(mResources.clan_slogan_blank, 0);
                    return;
                }
                Service.gI().getClan(4, (sbyte)Char.myCharz().clan.imgID, chatTField.tfChat.getText());
                chatTField.isShow = false;
                return;
            }
            if (chatTField.strChat.Equals(mResources.input_Inventory_Pass))
            {
                try
                {
                    int lockInventory = int.Parse(chatTField.tfChat.getText());
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    hide();
                    if (chatTField.tfChat.getText().Length != 6 || chatTField.tfChat.getText().Equals(string.Empty))
                    {
                        GameCanvas.startOKDlg(mResources.input_Inventory_Pass_wrong);
                    }
                    else
                    {
                        Service.gI().setLockInventory(lockInventory);
                        chatTField.isShow = false;
                        chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                        hide();
                    }
                    return;
                }
                catch (Exception)
                {
                    GameCanvas.startOKDlg(mResources.ALERT_PRIVATE_PASS_2);
                    return;
                }
            }
            if (chatTField.strChat.Equals(mResources.world_channel_5_luong))
            {
                if (!chatTField.tfChat.getText().Equals(string.Empty))
                {
                    Service.gI().chatGlobal(chatTField.tfChat.getText());
                    chatTField.isShow = false;
                    hide();
                }
            }
            else if (chatTField.strChat.Equals(mResources.chat_player))
            {
                chatTField.isShow = false;
                InfoItem infoItem = null;
                if (type == 8)
                {
                    infoItem = (InfoItem)logChat.elementAt(currInfoItem);
                }
                else if (type == 11)
                {
                    infoItem = (InfoItem)vFriend.elementAt(currInfoItem);
                }
                if (infoItem.charInfo.charID != Char.myCharz().charID)
                {
                    Service.gI().chatPlayer(text, infoItem.charInfo.charID);
                }
            }
            else if (chatTField.strChat.Equals(mResources.input_quantity_to_trade))
            {
                int num = 0;
                try
                {
                    num = int.Parse(chatTField.tfChat.getText());
                }
                catch (Exception)
                {
                    GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    return;
                }
                Item mon = itemChoGiaoDich ?? currItem;
                itemChoGiaoDich = null;
                if (mon == null)
                {
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    return;
                }
                if (num <= 0 || num > mon.quantity)
                {
                    GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    return;
                }
                mon.isSelect = true;
                Item item = new Item();
                item.template = mon.template;
                item.quantity = num;
                item.indexUI = mon.indexUI;
                item.itemOption = mon.itemOption;
                GameCanvas.panel.vMyGD.addElement(item);
                Service.gI().giaodich(2, -1, (sbyte)item.indexUI, item.quantity);
                chatTField.isShow = false;
                chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            }
            else if (chatTField.strChat == mResources.input_money_to_trade)
            {
                int num2 = 0;
                try
                {
                    num2 = int.Parse(chatTField.tfChat.getText());
                }
                catch (Exception)
                {
                    GameCanvas.startOKDlg(mResources.input_money_wrong);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                    return;
                }
                if (num2 > Char.myCharz().xu)
                {
                    GameCanvas.startOKDlg(mResources.not_enough_money);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                }
                else
                {
                    moneyGD = num2;
                    Service.gI().giaodich(2, -1, -1, num2);
                    chatTField.isShow = false;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
                }
            }
            else if (chatTField.strChat.Equals(mResources.kiguiXuchat))
            {
                try
                {
                    Service.gI().kigui(0, currItem.itemId, 0, int.Parse(chatTField.tfChat.getText()), 1);
                }
                catch (Exception)
                {
                    GameCanvas.startOKDlg(mResources.input_money_wrong);
                }
                chatTField.isShow = false;
            }
            else if (chatTField.strChat.Equals(mResources.kiguiXuchat + " "))
            {
                try
                {
                    Service.gI().kigui(0, currItem.itemId, 0, int.Parse(chatTField.tfChat.getText()), currItem.quantilyToBuy);
                }
                catch (Exception)
                {
                    GameCanvas.startOKDlg(mResources.input_money_wrong);
                }
                chatTField.isShow = false;
            }
            else if (chatTField.strChat.Equals(mResources.kiguiLuongchat))
            {
                doNotiRuby(0);
                chatTField.isShow = false;
            }
            else if (chatTField.strChat.Equals(mResources.kiguiLuongchat + "  "))
            {
                doNotiRuby(1);
                chatTField.isShow = false;
            }
            else if (chatTField.strChat.Equals(mResources.input_quantity + " "))
            {
                currItem.quantilyToBuy = int.Parse(chatTField.tfChat.getText());
                if (currItem.quantilyToBuy > currItem.quantity)
                {
                    GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                    return;
                }
                isKiguiXu = true;
                chatTField.isShow = false;
            }
            else if (chatTField.strChat.Equals(mResources.input_quantity + "  "))
            {
                currItem.quantilyToBuy = int.Parse(chatTField.tfChat.getText());
                if (currItem.quantilyToBuy > currItem.quantity)
                {
                    GameCanvas.startOKDlg(mResources.input_quantity_wrong);
                    return;
                }
                isKiguiLuong = true;
                chatTField.isShow = false;
            }
        }
    
        public void onCancelChat()
        {
            chatTField.tfChat.setIputType(TField.INPUT_TYPE_ANY);
        }
    
        public void setCombineEff(int type)
        {
            typeCombine = type;
            rS = 90;
            if (typeCombine == 0)
            {
                iDotS = 5;
                angleS = (angleO = 90);
                time = 2;
                for (int i = 0; i < vItemCombine.size(); i++)
                {
                    Item item = (Item)vItemCombine.elementAt(i);
                    if (item != null)
                    {
                        if (item.template.type == 14)
                        {
                            iconID2 = item.template.iconID;
                        }
                        else
                        {
                            iconID1 = item.template.iconID;
                        }
                    }
                }
            }
            else if (typeCombine == 1)
            {
                iDotS = 2;
                angleS = (angleO = 0);
                time = 1;
                for (int j = 0; j < vItemCombine.size(); j++)
                {
                    Item item2 = (Item)vItemCombine.elementAt(j);
                    if (item2 != null)
                    {
                        if (j == 0)
                        {
                            iconID1 = item2.template.iconID;
                        }
                        else
                        {
                            iconID2 = item2.template.iconID;
                        }
                    }
                }
            }
            else if (typeCombine == 2)
            {
                iDotS = 7;
                angleS = (angleO = 25);
                time = 1;
                for (int k = 0; k < vItemCombine.size(); k++)
                {
                    Item item3 = (Item)vItemCombine.elementAt(k);
                    if (item3 != null)
                    {
                        iconID1 = item3.template.iconID;
                    }
                }
            }
            else if (typeCombine == 3)
            {
                xS = GameCanvas.hw;
                yS = GameCanvas.hh;
                iDotS = 1;
                angleS = (angleO = 1);
                time = 4;
                for (int l = 0; l < vItemCombine.size(); l++)
                {
                    Item item4 = (Item)vItemCombine.elementAt(l);
                    if (item4 != null)
                    {
                        iconID1 = item4.template.iconID;
                    }
                }
            }
            else if (typeCombine == 4)
            {
                iDotS = vItemCombine.size();
                iconID = new short[iDotS];
                angleS = (angleO = 25);
                time = 1;
                for (int m = 0; m < vItemCombine.size(); m++)
                {
                    Item item5 = (Item)vItemCombine.elementAt(m);
                    if (item5 != null)
                    {
                        iconID[m] = item5.template.iconID;
                    }
                }
            }
            speed = 1;
            isSpeedCombine = true;
            isDoneCombine = false;
            isCompleteEffCombine = false;
            iAngleS = 360 / iDotS;
            xArgS = new int[iDotS];
            yArgS = new int[iDotS];
            xDotS = new int[iDotS];
            yDotS = new int[iDotS];
            setDotStar();
            isPaintCombine = true;
            countUpdate = 10;
            countR = 30;
            countWait = 10;
            addTextCombineNPC(idNPC, mResources.combineSpell);
        }
    
        private void updateCombineEff()
        {
            countUpdate--;
            if (countUpdate < 0)
            {
                countUpdate = 0;
            }
            countR--;
            if (countR < 0)
            {
                countR = 0;
            }
            if (countUpdate != 0)
            {
                return;
            }
            if (!isCompleteEffCombine)
            {
                if (time > 0)
                {
                    if (combineSuccess != -1)
                    {
                        if (typeCombine == 3)
                        {
                            if (GameCanvas.gameTick % 10 == 0)
                            {
                                Effect me = new Effect(21, xS - 10, yS + 25, 4, 1, 1);
                                EffecMn.addEff(me);
                                time--;
                            }
                        }
                        else
                        {
                            if (GameCanvas.gameTick % 2 == 0)
                            {
                                if (isSpeedCombine)
                                {
                                    if (speed < 40)
                                    {
                                        speed += 2;
                                    }
                                }
                                else if (speed > 10)
                                {
                                    speed -= 2;
                                }
                            }
                            if (countR == 0)
                            {
                                if (isSpeedCombine)
                                {
                                    if (rS > 0)
                                    {
                                        rS -= 5;
                                    }
                                    else if (GameCanvas.gameTick % 10 == 0)
                                    {
                                        isSpeedCombine = false;
                                        time--;
                                        countR = 5;
                                        countWait = 10;
                                    }
                                }
                                else if (rS < 90)
                                {
                                    rS += 5;
                                }
                                else if (GameCanvas.gameTick % 10 == 0)
                                {
                                    isSpeedCombine = true;
                                    countR = 10;
                                }
                            }
                            angleS = angleO;
                            angleS -= speed;
                            if (angleS >= 360)
                            {
                                angleS -= 360;
                            }
                            if (angleS < 0)
                            {
                                angleS = 360 + angleS;
                            }
                            angleO = angleS;
                            setDotStar();
                        }
                    }
                }
                else if (GameCanvas.gameTick % 20 == 0)
                {
                    isCompleteEffCombine = true;
                }
                if (GameCanvas.gameTick % 20 == 0)
                {
                    if (typeCombine != 3)
                    {
                        EffectPanel.addServerEffect(132, xS, yS, 2);
                    }
                    EffectPanel.addServerEffect(114, xS, yS + 20, 2);
                }
            }
            else
            {
                if (!isCompleteEffCombine)
                {
                    return;
                }
                if (combineSuccess == 1)
                {
                    if (countWait == 10)
                    {
                        Effect me2 = new Effect(22, xS - 3, yS + 25, 4, 1, 1);
                        EffecMn.addEff(me2);
                    }
                    countWait--;
                    if (countWait < 0)
                    {
                        countWait = 0;
                    }
                    if (rS < 300)
                    {
                        rS = Res.abs(rS + 10);
                        if (rS == 20)
                        {
                            addTextCombineNPC(idNPC, mResources.combineFail);
                        }
                    }
                    else if (GameCanvas.gameTick % 20 == 0)
                    {
                        if (GameCanvas.w >= 2 * WIDTH_PANEL)
                        {
                            GameCanvas.panel2 = new Panel();
                            GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                            GameCanvas.panel2.setTypeBodyOnly();
                            GameCanvas.panel2.show();
                        }
                        combineSuccess = -1;
                        isDoneCombine = true;
                        if (typeCombine == 4)
                        {
                            GameCanvas.panel.hideNow();
                        }
                    }
                    setDotStar();
                }
                else
                {
                    if (combineSuccess != 0)
                    {
                        return;
                    }
                    if (countWait == 10)
                    {
                        if (typeCombine == 2)
                        {
                            Effect me3 = new Effect(20, xS - 3, yS + 15, 4, 2, 1);
                            EffecMn.addEff(me3);
                        }
                        else
                        {
                            Effect me4 = new Effect(21, xS - 10, yS + 25, 4, 1, 1);
                            EffecMn.addEff(me4);
                        }
                        addTextCombineNPC(idNPC, mResources.combineSuccess);
                        isPaintCombine = false;
                    }
                    if (isPaintCombine)
                    {
                        return;
                    }
                    countWait--;
                    if (countWait < -50)
                    {
                        countWait = -50;
                        if (typeCombine < 3 && GameCanvas.w >= 2 * WIDTH_PANEL)
                        {
                            GameCanvas.panel2 = new Panel();
                            GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                            GameCanvas.panel2.setTypeBodyOnly();
                            GameCanvas.panel2.show();
                        }
                        combineSuccess = -1;
                        isDoneCombine = true;
                        if (typeCombine == 4)
                        {
                            GameCanvas.panel.hideNow();
                        }
                    }
                }
            }
        }
    
        public void paintCombineEff(mGraphics g)
        {
            GameScr.gI().paintBlackSky(g);
            paintCombineNPC(g);
            if (GameCanvas.gameTick % 4 == 0)
            {
                g.drawImage(ItemMap.imageFlare, xS, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
            }
            if (typeCombine == 0)
            {
                for (int i = 0; i < yArgS.Length; i++)
                {
                    SmallImage.drawSmallImage(g, iconID1, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    if (isPaintCombine)
                    {
                        SmallImage.drawSmallImage(g, iconID2, xDotS[i], yDotS[i], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    }
                }
            }
            else if (typeCombine == 1)
            {
                if (!isPaintCombine)
                {
                    SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    return;
                }
                for (int j = 0; j < yArgS.Length; j++)
                {
                    SmallImage.drawSmallImage(g, iconID1, xDotS[0], yDotS[0], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    SmallImage.drawSmallImage(g, iconID2, xDotS[1], yDotS[1], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
            }
            else if (typeCombine == 2)
            {
                if (!isPaintCombine)
                {
                    SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    return;
                }
                for (int k = 0; k < yArgS.Length; k++)
                {
                    SmallImage.drawSmallImage(g, iconID1, xDotS[k], yDotS[k], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
            }
            else if (typeCombine == 3)
            {
                if (!isPaintCombine)
                {
                    SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
                else
                {
                    SmallImage.drawSmallImage(g, iconID1, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                }
            }
            else
            {
                if (typeCombine != 4)
                {
                    return;
                }
                if (!isPaintCombine)
                {
                    if (iconID3 != -1)
                    {
                        SmallImage.drawSmallImage(g, iconID3, xS, yS, 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    }
                }
                else
                {
                    for (int l = 0; l < iconID.Length; l++)
                    {
                        SmallImage.drawSmallImage(g, iconID[l], xDotS[l], yDotS[l], 0, mGraphics.VCENTER | mGraphics.HCENTER);
                    }
                }
            }
        }
    
        private void setDotStar()
        {
            for (int i = 0; i < yArgS.Length; i++)
            {
                if (angleS >= 360)
                {
                    angleS -= 360;
                }
                if (angleS < 0)
                {
                    angleS = 360 + angleS;
                }
                yArgS[i] = Res.abs(rS * Res.sin(angleS) / 1024);
                xArgS[i] = Res.abs(rS * Res.cos(angleS) / 1024);
                if (angleS < 90)
                {
                    xDotS[i] = xS + xArgS[i];
                    yDotS[i] = yS - yArgS[i];
                }
                else if (angleS >= 90 && angleS < 180)
                {
                    xDotS[i] = xS - xArgS[i];
                    yDotS[i] = yS - yArgS[i];
                }
                else if (angleS >= 180 && angleS < 270)
                {
                    xDotS[i] = xS - xArgS[i];
                    yDotS[i] = yS + yArgS[i];
                }
                else
                {
                    xDotS[i] = xS + xArgS[i];
                    yDotS[i] = yS + yArgS[i];
                }
                angleS -= iAngleS;
            }
        }
    
        public void paintCombineNPC(mGraphics g)
        {
            g.translate(-GameScr.cmx, -GameScr.cmy);
            if (typeCombine < 3)
            {
                for (int i = 0; i < GameScr.vNpc.size(); i++)
                {
                    Npc npc = (Npc)GameScr.vNpc.elementAt(i);
                    if (npc.template.npcTemplateId == idNPC)
                    {
                        npc.paint(g);
                        if (npc.chatInfo != null)
                        {
                            npc.chatInfo.paint(g, npc.cx, npc.cy - npc.ch - GameCanvas.transY, npc.cdir);
                        }
                    }
                }
            }
            GameCanvas.resetTrans(g);
            if (GameCanvas.gameTick % 4 == 0)
            {
                g.drawImage(ItemMap.imageFlare, xS - 5, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
                g.drawImage(ItemMap.imageFlare, xS + 5, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
                g.drawImage(ItemMap.imageFlare, xS, yS + 15, mGraphics.BOTTOM | mGraphics.HCENTER);
            }
            for (int j = 0; j < Effect2.vEffect3.size(); j++)
            {
                Effect2 effect = (Effect2)Effect2.vEffect3.elementAt(j);
                effect.paint(g);
            }
        }
    
        public void addTextCombineNPC(int idNPC, string text)
        {
            if (typeCombine >= 3)
            {
                return;
            }
            for (int i = 0; i < GameScr.vNpc.size(); i++)
            {
                Npc npc = (Npc)GameScr.vNpc.elementAt(i);
                if (npc.template.npcTemplateId == idNPC)
                {
                    npc.addInfo(text);
                }
            }
        }
    
        public void setTypeOption()
        {
            type = 19;
            setType(0);
            setTabOption();
            cmx = (cmtoX = 0);
        }
    
        private void setTabOption()
        {
            SoundMn.gI().getStrOption();
            currentListLength = strCauhinh.Length;
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
        }
    
        private void paintOption(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < strCauhinh.Length; i++)
            {
                int x = xScroll;
                int num = yScroll + i * ITEM_HEIGHT;
                int num2 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(x, num, num2, h);
                    mFont.tahoma_7b_dark.drawString(g, strCauhinh[i], xScroll + 25, num + 6, mFont.LEFT);
                }
            }
            paintScrollArrow(g);
        }
    
        private void doFireOption()
        {
            if (selected < 0)
            {
                return;
            }
            switch (selected)
            {
                case 0:
                    SoundMn.gI().AuraToolOption();
                    break;
                case 1:
                    SoundMn.gI().AuraToolOption2();
                    break;
                case 2:
                    SoundMn.gI().soundToolOption();
                    break;
                case 3:
                    if (Main.isPC)
                    {
                        GameCanvas.startYesNoDlg(mResources.changeSizeScreen, new Command(mResources.YES, this, 170391, null), new Command(mResources.NO, this, 4005, null));
                    }
                    else
                    {
                        SoundMn.gI().CaseSizeScr();
                    }
                    break;
                case 4:
                    if (Main.isPC)
                    {
                        GameCanvas.startYesNoDlg(mResources.changeSizeScreen, new Command(mResources.YES, this, 170391, null), new Command(mResources.NO, this, 4005, null));
                    }
                    else
                    {
                        SoundMn.gI().CaseAnalog();
                    }
                    break;
                case 5:
                    SoundMn.gI().CaseAnalog();
                    break;
            }
        }
    
        public void setTypeAccount()
        {
            type = 20;
            setType(0);
            setTabAccount();
            cmx = (cmtoX = 0);
        }
    
        /// <summary>
        /// Các thẻ của bảng "Tài khoản".
        /// </summary>
        /// <remarks>
        /// <para><b>iOS bày y hệt Android.</b> Trước đây có một nhánh riêng cho
        /// <c>Main.IphoneVersionApp</c> bỏ thẻ "Nạp tiền" đi — đó là cách né
        /// điều 3.1.1 trong luật duyệt App Store, vốn cấm dẫn người dùng ra
        /// ngoài để trả tiền. Bản này phát hành ngoài App Store nên luật đó
        /// không áp, và người chơi iOS đang mất một thẻ mà Android có.</para>
        ///
        /// <para>Nếu về sau đưa lên App Store thật thì phải dựng lại nhánh đó,
        /// không thì Apple từ chối.</para>
        /// </remarks>
        private void setTabAccount()
        {
            {
                strAccount = new string[5]
                {
                    mResources.inventory_Pass,
                    mResources.friend,
                    mResources.enemy,
                    mResources.msg,
                    mResources.charger
                };
                if (GameScr.canAutoPlay)
                {
                    strAccount = new string[6]
                    {
                        mResources.inventory_Pass,
                        mResources.friend,
                        mResources.enemy,
                        mResources.msg,
                        mResources.charger,
                        mResources.autoFunction
                    };
                }
                if ((mSystem.clientType == 2 || mSystem.clientType == 7) && mResources.language != 2)
                {
                    strAccount = new string[5]
                    {
                        mResources.inventory_Pass,
                        mResources.friend,
                        mResources.enemy,
                        mResources.msg,
                        mResources.charger
                    };
                    if (GameScr.canAutoPlay)
                    {
                        strAccount = new string[6]
                        {
                            mResources.inventory_Pass,
                            mResources.friend,
                            mResources.enemy,
                            mResources.msg,
                            mResources.charger,
                            mResources.autoFunction
                        };
                    }
                }
            }
            currentListLength = strAccount.Length;
            ITEM_HEIGHT = 24;
            selected = (GameCanvas.isTouch ? (-1) : 0);
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
        }
    
        private void paintAccount(mGraphics g)
        {
            g.setClip(xScroll, yScroll, wScroll, hScroll);
            g.translate(0, -cmy);
            for (int i = 0; i < strAccount.Length; i++)
            {
                int x = xScroll;
                int num = yScroll + i * ITEM_HEIGHT;
                int num2 = wScroll - 1;
                int h = ITEM_HEIGHT - 1;
                if (num - cmy <= yScroll + hScroll && num - cmy >= yScroll - ITEM_HEIGHT)
                {
                    g.setColor((i != selected) ? 15196114 : 16383818);
                    g.fillRect(x, num, num2, h);
                    mFont.tahoma_7b_dark.drawString(g, strAccount[i], xScroll + wScroll / 2, num + 6, mFont.CENTER);
                }
            }
            paintScrollArrow(g);
        }
    
        private void doFireAccount()
        {
            if (selected < 0)
            {
                return;
            }
            switch (selected)
            {
                case 0:
                    GameCanvas.endDlg();
                    if (chatTField == null)
                    {
                        chatTField = new ChatTextField();
                        chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                        chatTField.initChatTextField();
                        chatTField.parentScreen = GameCanvas.panel;
                    }
                    chatTField.tfChat.setText(string.Empty);
                    chatTField.strChat = mResources.input_Inventory_Pass;
                    chatTField.tfChat.name = mResources.input_Inventory_Pass;
                    chatTField.to = string.Empty;
                    chatTField.isShow = true;
                    chatTField.tfChat.isFocus = true;
                    chatTField.tfChat.setIputType(TField.INPUT_TYPE_NUMERIC);
                    if (GameCanvas.isTouch)
                    {
                        chatTField.tfChat.doChangeToTextBox();
                    }
                    if (!Main.isPC)
                    {
                        chatTField.startChat2(this, string.Empty);
                    }
                    if (Main.isWindowsPhone)
                    {
                        chatTField.tfChat.strInfo = chatTField.strChat;
                    }
                    break;
                case 1:
                    Service.gI().friend(0, -1);
                    InfoDlg.showWait();
                    break;
                case 2:
                    Service.gI().enemy(0, -1);
                    InfoDlg.showWait();
                    break;
                case 3:
                    setTypeMessage();
                    if (chatTField == null)
                    {
                        chatTField = new ChatTextField();
                        chatTField.tfChat.y = GameCanvas.h - 35 - ChatTextField.gI().tfChat.height;
                        chatTField.initChatTextField();
                        chatTField.parentScreen = GameCanvas.panel;
                    }
                    break;
                case 4:
                    if (mResources.language == 2)
                    {
                        string url = "http://dragonball.indonaga.com/coda/?username=" + GameCanvas.loginScr.tfUser.getText();
                        hideNow();
                        try
                        {
                            GameMidlet.instance.platformRequest(url);
                            break;
                        }
                        catch (Exception ex)
                        {
                            ex.StackTrace.ToString();
                            break;
                        }
                    }
                    hideNow();
                    if (Char.myCharz().taskMaint.taskId <= 10)
                    {
                        GameCanvas.startOKDlg(mResources.finishBomong);
                    }
                    else
                    {
                        MoneyCharge.gI().switchToMe();
                    }
                    break;
                case 5:
                    setTypeAuto();
                    break;
            }
        }
    
        private void updateKeyOption()
        {
            updateKeyScrollView();
        }
    
        public void setTypeSpeacialSkill()
        {
            type = 25;
            setType(0);
            setTabSpeacialSkill();
            currentTabIndex = 0;
        }
    
        private void setTabSpeacialSkill()
        {
            ITEM_HEIGHT = 24;
            currentListLength = Char.myCharz().infoSpeacialSkill[currentTabIndex].Length;
            cmyLim = currentListLength * ITEM_HEIGHT - hScroll;
            if (cmyLim < 0)
            {
                cmyLim = 0;
            }
            cmy = (cmtoY = cmyLast[currentTabIndex]);
            if (cmy < 0)
            {
                cmy = (cmtoY = 0);
            }
            if (cmy > cmyLim)
            {
                cmy = (cmtoY = cmyLim);
            }
            selected = (GameCanvas.isTouch ? (-1) : 0);
        }
    
        public bool isTypeShop()
        {
            if (type == 1)
            {
                return true;
            }
            return false;
        }
    
        private void doNotiRuby(int type)
        {
            try
            {
                currItem.buyRuby = int.Parse(chatTField.tfChat.getText());
            }
            catch (Exception)
            {
                GameCanvas.startOKDlg(mResources.input_money_wrong);
                chatTField.isShow = false;
                return;
            }
            Command cmdYes = new Command(mResources.YES, this, (type != 0) ? 11001 : 11000, null);
            Command cmdNo = new Command(mResources.NO, this, 11002, null);
            GameCanvas.startYesNoDlg(mResources.notiRuby, cmdYes, cmdNo);
        }
    
        public static void paintUpgradeEffect(int x, int y, int wItem, int hItem, int nline, int cl, mGraphics g)
        {
            try
            {
                int num = (wItem << 1) + (hItem << 1);
                int num2 = num / nline;
                nsize = sizeUpgradeEff.Length;
                if (nline > 4)
                {
                    nsize = 2;
                }
                for (int i = 0; i < nline; i++)
                {
                    for (int j = 0; j < nsize; j++)
                    {
                        int wSize = ((sizeUpgradeEff[j] <= 1) ? 1 : ((sizeUpgradeEff[j] >> 1) + 1));
                        int x2 = x + upgradeEffectX(num2 * i, GameCanvas.gameTick - j * 4, wItem, hItem, wSize);
                        int y2 = y + upgradeEffectY(num2 * i, GameCanvas.gameTick - j * 4, wItem, hItem, wSize);
                        g.setColor(colorUpgradeEffect[cl][j]);
                        g.fillRect(x2, y2, sizeUpgradeEff[j], sizeUpgradeEff[j]);
                    }
                }
            }
            catch (Exception)
            {
            }
        }
    
        private static int upgradeEffectX(int dk, int tick, int wItem, int hitem, int wSize)
        {
            int num = (tick + dk) % ((wItem << 1) + (hitem << 1));
            if (0 <= num && num < wItem)
            {
                return num % wItem;
            }
            if (wItem <= num && num < wItem + hitem)
            {
                return wItem - wSize;
            }
            if (wItem + hitem <= num && num < (wItem << 1) + hitem)
            {
                return wItem - (num - hitem) % wItem - wSize;
            }
            return 0;
        }
    
        private static int upgradeEffectY(int dk, int tick, int wItem, int hitem, int wSize)
        {
            int num = (tick + dk) % ((wItem << 1) + (hitem << 1));
            if (0 <= num && num < wItem)
            {
                return 0;
            }
            if (wItem <= num && num < wItem + hitem)
            {
                return num % wItem;
            }
            if (wItem + hitem <= num && num < (wItem << 1) + hitem)
            {
                return hitem - wSize;
            }
            return hitem - (num - (wItem << 1)) % hitem - wSize;
        }
    
        public static int GetColor_ItemBg(int id)
        {
            switch (id)
            {
                case 4:
                    return 1269146;
                case 1:
                    return 2786816;
                case 5:
                    return 13279744;
                case 3:
                    return 12537346;
                case 2:
                    return 7078041;
                case 6:
                    return 11599872;
                default:
                    return -1;
            }
        }
    
        public static sbyte GetColor_Item_Upgrade(int lv)
        {
            if (lv < 0)
            {
                return 0;
            }
            switch (lv)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    return 0;
                case 9:
                    return 4;
                case 10:
                    return 1;
                case 11:
                    return 5;
                case 12:
                    return 3;
                case 13:
                    return 2;
                default:
                    return 6;
            }
        }
    
        public static mFont GetFont(int color)
        {
            mFont result = mFont.tahoma_7;
            switch (color)
            {
                case -1:
                    result = mFont.tahoma_7;
                    break;
                case 0:
                    result = mFont.tahoma_7b_dark;
                    break;
                case 1:
                    result = mFont.tahoma_7b_green;
                    break;
                case 2:
                    result = mFont.tahoma_7b_blue;
                    break;
                case 3:
                    result = mFont.tahoma_7_red;
                    break;
                case 4:
                    result = mFont.tahoma_7_green;
                    break;
                case 5:
                    result = mFont.tahoma_7_blue;
                    break;
                case 7:
                    result = mFont.tahoma_7b_red;
                    break;
                case 8:
                    result = mFont.tahoma_7b_yellow;
                    break;
            }
            return result;
        }
    
        public void paintOptItem(mGraphics g, int idOpt, int param, int x, int y, int w, int h)
        {
            switch (idOpt)
            {
                case 34:
                    if (imgo_0 != null)
                    {
                        g.drawImage(imgo_0, x, y + h - imgo_0.getHeight());
                    }
                    else
                    {
                        imgo_0 = mSystem.loadImage("/mainImage/o_0.png");
                    }
                    if (imgo_1 != null)
                    {
                        g.drawImage(imgo_1, x, y + h - imgo_1.getHeight());
                    }
                    else
                    {
                        imgo_1 = mSystem.loadImage("/mainImage/o_1.png");
                    }
                    break;
                case 35:
                    if (imgo_0 != null)
                    {
                        g.drawImage(imgo_0, x, y + h - imgo_0.getHeight());
                    }
                    else
                    {
                        imgo_0 = mSystem.loadImage("/mainImage/o_0.png");
                    }
                    if (imgo_2 != null)
                    {
                        g.drawImage(imgo_2, x, y + h - imgo_2.getHeight());
                    }
                    else
                    {
                        imgo_2 = mSystem.loadImage("/mainImage/o_2.png");
                    }
                    break;
                case 36:
                    if (imgo_0 != null)
                    {
                        g.drawImage(imgo_0, x, y + h - imgo_0.getHeight());
                    }
                    else
                    {
                        imgo_0 = mSystem.loadImage("/mainImage/o_0.png");
                    }
                    if (imgo_3 != null)
                    {
                        g.drawImage(imgo_3, x, y + h - imgo_3.getHeight());
                    }
                    else
                    {
                        imgo_3 = mSystem.loadImage("/mainImage/o_3.png");
                    }
                    break;
            }
        }
    
        public void paintOptSlotItem(mGraphics g, int idOpt, int param, int x, int y, int w, int h)
        {
            if (idOpt == 102 && param > ChatPopup.numSlot)
            {
                sbyte color_Item_Upgrade = GetColor_Item_Upgrade(param);
                int nline = param - ChatPopup.numSlot;
                paintUpgradeEffect(x, y, w, h, nline, color_Item_Upgrade, g);
            }
        }
    
        public static mFont setTextColor(int id, int type)
        {
            if (type == 0)
            {
                switch (id)
                {
                    case 0:
                        return mFont.bigNumber_While;
                    case 1:
                        return mFont.bigNumber_green;
                    case 3:
                        return mFont.bigNumber_orange;
                    case 4:
                        return mFont.bigNumber_blue;
                    case 5:
                        return mFont.bigNumber_yellow;
                    case 6:
                        return mFont.bigNumber_red;
                    default:
                        return mFont.bigNumber_While;
                }
            }
            switch (id)
            {
                case 0:
                    return mFont.tahoma_7b_white;
                case 1:
                    return mFont.tahoma_7b_green;
                case 3:
                    return mFont.tahoma_7b_yellowSmall2;
                case 4:
                    return mFont.tahoma_7b_blue;
                case 5:
                    return mFont.tahoma_7b_yellow;
                case 6:
                    return mFont.tahoma_7b_red;
                case 7:
                    return mFont.tahoma_7b_dark;
                default:
                    return mFont.tahoma_7b_white;
            }
        }
    
        private bool GetInventorySelect_isbody(int select, int subSelect, Item[] arrItem)
        {
            int num = select - 1 + subSelect * 20;
            return subSelect == 0 && num < arrItem.Length;
        }
    
        private int GetInventorySelect_body(int select, int subSelect)
        {
            return select - 1 + subSelect * 20;
        }
    
        private int GetInventorySelect_bag(int select, int subSelect, Item[] arrItem)
        {
            int num = select - 1 + subSelect * 20;
            return num - arrItem.Length;
        }
    
        /// <summary>
        /// Bảng này đang vẽ LƯỚI TÚI kiểu mới hay không.
        /// </summary>
        /// <remarks>
        /// Phải khớp đúng những nhánh gọi <c>paintInventory</c> trong <c>paint</c>:
        /// túi thường, cửa hàng (thẻ bán đồ), rương, hộp đồ, ghép đồ, thú cưng và
        /// bảng giao dịch.
        ///
        /// <para>Bản cũ chỉ nhận túi thường và hộp đồ. Nhưng lưới vẫn được VẼ ở các
        /// bảng kia, trong khi phần bắt chạm của lưới lại bị tắt — nên cú chạm rơi
        /// xuống nhánh danh sách kiểu cũ, và <c>itemInvenNew</c> không bao giờ đổi:
        /// nó nằm nguyên ở ô đặt sẵn lúc mở bảng, tức ô trang bị đầu tiên. Đó là lỗi
        /// "chọn đồ giao dịch cứ ra món bên thẻ Trang bị".</para>
        /// </remarks>
        private bool isTabInven()
        {
            switch (type)
            {
            case 0:
                return currentTabIndex == 1;
            case 1:
                return currentTabIndex == currentTabName.Length - 1
                    && GameCanvas.panel2 == null && typeShop != 2;
            case 2:
                return currentTabIndex == 1;
            case 7:
                return true;
            case 12:
                return currentTabIndex == 1;
            case 13:
                return currentTabIndex == 0 && Equals(GameCanvas.panel);
            case 21:
                return currentTabIndex == 2;
            default:
                return false;
            }
        }
    
        private void updateKeyInvenTab()
        {
            if (selected < 0)
            {
                return;
            }
            if (GameCanvas.keyPressed[(!Main.isPC) ? 4 : 23])
            {
                newSelected--;
                if (isnewInventory)
                {
                    currentListLength = 5;
                }
                if (newSelected < 0)
                {
                    newSelected = 0;
                    if (GameCanvas.isFocusPanel2)
                    {
                        GameCanvas.isFocusPanel2 = false;
                        GameCanvas.panel.selected = 0;
                    }
                }
            }
            else
            {
                if (!GameCanvas.keyPressed[(!Main.isPC) ? 6 : 24])
                {
                    return;
                }
                newSelected++;
                if (isnewInventory)
                {
                    currentListLength = 5;
                }
                if (newSelected > size_tab - 1)
                {
                    newSelected = size_tab - 1;
                    if (GameCanvas.panel2 != null)
                    {
                        GameCanvas.isFocusPanel2 = true;
                        GameCanvas.panel2.selected = 0;
                    }
                }
            }
        }
    
        private void updateKeyInventory()
        {
            updateKeyScrollView();
            if (selected == 0)
            {
                updateKeyInvenTab();
            }
        }
    
        private bool IsTabOption()
        {
            if (size_tab > 0)
            {
                if (currentTabName.Length > 1)
                {
                    if (selected == 0)
                    {
                        return true;
                    }
                }
                else if (selected >= 0)
                {
                    return true;
                }
            }
            return false;
        }
    
        /// <summary>
        /// So dong danh sach cua trang dang xem, va dat lai so tab.
        /// </summary>
        /// <remarks>
        /// So o moi trang phai KHOP voi phan ve. Phan ve doi sang luoi 6x5 (30 o)
        /// ma cho nay con giu 20 thi so tab tinh ra nhieu hon so tab ve ra, va
        /// bam vao tab thu ba tro ra mot trang khong ton tai — nhin nhu "khong
        /// doi tab duoc".
        /// </remarks>
        private int checkCurrentListLength(int arrLength)
        {
            if (isnewInventory)
            {
                // Hai tab co dinh. Tra ve so dong "ao" du de cmyLim tinh ra dung
                // quang cuon can thiet cho tab tui.
                size_tab = (sbyte) TEN_TAB_TUI.Length;
                // Tra ve so dong sao cho "so dong * ITEM_HEIGHT - hScroll" ra
                // dung quang cuon can thiet — vi do la cong thuc cmyLim dung o
                // moi cho khac.
                int caoCuon = gioiHanCuonTui();
                return (caoCuon + hScroll + ITEM_HEIGHT - 1) / ITEM_HEIGHT;
            }
            int num = SO_O_MOI_TRANG;
            int num2 = soTrang(arrLength);
            size_tab = (sbyte)num2;
            if (newSelected > num2 - 1)
            {
                newSelected = num2 - 1;
            }
            if (arrLength % SO_O_MOI_TRANG > 0 && newSelected == num2 - 1)
            {
                num = arrLength % SO_O_MOI_TRANG;
            }
            return num + 1;
        }

        /// <summary>So trang can de chua het <paramref name="soO"/> o.</summary>
        private static int soTrang(int soO)
        {
            int n = soO / SO_O_MOI_TRANG + ((soO % SO_O_MOI_TRANG > 0) ? 1 : 0);
            return n < 1 ? 1 : n;
        }
    
        /// <summary>
        /// Chọn tab theo chỗ vừa bấm trên dải tab.
        /// </summary>
        /// <remarks>
        /// Hành trang giờ chỉ có HAI tab cố định ("Trang bị", "Hành trang"), nên
        /// bề rộng mỗi tab tính từ số tab đó, không phải từ số trang túi. Bản cũ
        /// chia theo số trang: số trang đổi theo lượng đồ trong túi, nên vùng bấm
        /// của tab trôi đi mỗi lần nhặt thêm đồ.
        /// </remarks>
        private void setNewSelected(int arrLength, bool resetSelect)
        {
            if (isnewInventory)
            {
                int rong = wScroll / TEN_TAB_TUI.Length;
                int t = (GameCanvas.px - xScroll) / (rong < 1 ? 1 : rong);
                if (t < 0)
                {
                    t = 0;
                }
                else if (t >= TEN_TAB_TUI.Length)
                {
                    t = TEN_TAB_TUI.Length - 1;
                }
                if (t != newSelected)
                {
                    // Doi tab thi ve dau danh sach va bo o dang chon: o thu ba
                    // cua tab nay khong co nghia gi o tab kia.
                    cmy = 0;
                    cmtoY = 0;
                    sellectInventory = 0;
                }
                newSelected = t;
                setTabInventory(resetSelect);
                return;
            }
            int num = soTrang(arrLength);
            int num2 = xScroll;
            newSelected = (GameCanvas.px - num2) / TAB_W_NEW;
            if (newSelected > num - 1)
            {
                newSelected = num - 1;
            }
            if (GameCanvas.px < num2)
            {
                newSelected = 0;
            }
            setTabInventory(resetSelect);
        }
    }
}
