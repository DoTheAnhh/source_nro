
namespace Game2
{
    
    using System;
    using System.Net.NetworkInformation;
    using System.Threading;
    using UnityEngine;
    
    public class Main : MonoBehaviour
    {
        public static Main main;
    
        public static mGraphics g;
    
        public static GameMidlet midlet;
    
        public static string res = "res";
    
        public static string mainThreadName;
    
        public static bool started;
    
        public static bool isIpod;
    
        public static bool isIphone4;
    
        public static bool isPC;
    
        public static bool isWindowsPhone;
    
        public static bool isIPhone;
    
        public static bool IphoneVersionApp;
    
        public static string IMEI;
    
        public static int versionIp;
    
        public static int numberQuit = 1;
    
        public static int typeClient = 4;
    
        public const sbyte PC_VERSION = 4;
    
        public const sbyte IP_APPSTORE = 5;
    
        public const sbyte WINDOWSPHONE = 6;
    
        private int level;
    
        public const sbyte IP_JB = 3;
    
        private int updateCount;
    
        private int paintCount;
    
        private int count;
    
        private int fps;
    
        private int max;
    
        private int up;
    
        private int upmax;
    
        private long timefps;
    
        private long timeup;
    
        private bool isRun;
    
        public static int waitTick;
    
        public static int f;
    
        public static bool isResume;
    
        public static bool isMiniApp = true;
    
        public static bool isQuitApp;
    
        private Vector2 lastMousePos = default(Vector2);
    
        public static int a = 1;
    
        public static bool isCompactDevice = true;
    
        public static string pasteText;

        private void Awake()
        {
            if (main != null)
            {
                TabManagement.tab = TabType.Tab2;
                Destroy(this.gameObject);
                SoundMn.gI().loadSound(TileMap.mapID);
                return;
            }
            DontDestroyOnLoad(this.gameObject);
        }

        private void Start()
        {
    
            if (started)
            {
                return;
            }
            if (Thread.CurrentThread.Name != "Main")
            {
                Thread.CurrentThread.Name = "Main";
            }
            mainThreadName = Thread.CurrentThread.Name;
            if (Application.platform == RuntimePlatform.Android || Application.platform == RuntimePlatform.IPhonePlayer)
            {
                isPC = false;
                if (Application.platform == RuntimePlatform.IPhonePlayer)
                {
                    IphoneVersionApp = true;
                }
            }
            else
            {
                isPC = true;
            }
            started = true;
            if (isPC)
            {
                level = Rms.loadRMSInt("levelScreenKN");
                // Nho lua chon toan man hinh giua cac lan choi.
                toanManHinh = Rms.loadRMSInt("fullScreenKN") == 1;
                apCheDoManHinh();
            }
        }
    
        /// <summary>Ban PC dang chay toan man hinh khong.</summary>
        public static bool toanManHinh;

        /// <summary>
        /// Ap che do man hinh theo <see cref="toanManHinh"/>.
        /// </summary>
        /// <remarks>
        /// <para>Dung <c>FullScreenWindow</c> (toan man hinh khong vien) chu
        /// khong <c>ExclusiveFullScreen</c>: che do doc quyen doi han che do man
        /// hinh cua may, nen alt-tab cham va co may bi nhay den mot nhip. Che do
        /// khong vien nhanh va khong doi gi cua he thong.</para>
        ///
        /// <para>Che do cua so giu nguyen hai co cu (720x320 / 1024x600) theo
        /// khoa <c>levelScreenKN</c>.</para>
        /// </remarks>
        public static void apCheDoManHinh()
        {
            if (!isPC)
            {
                return;
            }
            if (toanManHinh)
            {
                Resolution r = Screen.currentResolution;
                Screen.SetResolution(r.width, r.height,
                        FullScreenMode.FullScreenWindow);
            }
            else if (Rms.loadRMSInt("levelScreenKN") == 1)
            {
                // Doc thang tu RMS chu khong dung field `level`: field do la
                // field INSTANCE, con ham nay static (phai static de phim F11
                // goi duoc tu bat cu dau).
                Screen.SetResolution(720, 320, FullScreenMode.Windowed);
            }
            else
            {
                Screen.SetResolution(1024, 600, FullScreenMode.Windowed);
            }
        }

        /// <summary>
        /// Doi qua lai cua so / toan man hinh, va luu lua chon.
        /// </summary>
        /// <remarks>
        /// Tinh lai kich thuoc khung ao ngay sau khi doi, neu khong thi ca HUD
        /// van xep theo co cu — xem <c>GameCanvas.capNhatKichThuoc</c>.
        /// </remarks>
        public static void doiToanManHinh()
        {
            if (!isPC)
            {
                return;
            }
            toanManHinh = !toanManHinh;
            Rms.saveRMSInt("fullScreenKN", toanManHinh ? 1 : 0);
            apCheDoManHinh();
            // Khong tinh lai layout o day: `Screen.SetResolution` chi co hieu
            // luc o cuoi khung hinh nen luc nay Screen.width van la so cu.
            // `theoDoiKichCo()` se bat duoc o khung sau — do cung la duong ma
            // moi ly do doi kich co khac di qua.
        }

        /// <summary>Kich co cua so lan cuoi da tinh layout theo.</summary>
        private static int rongDaTinh = -1;

        private static int caoDaTinh = -1;

        /// <summary>
        /// Bat cua so doi kich co, roi tinh lai layout.
        /// </summary>
        /// <remarks>
        /// Goi moi khung hinh nhung chi la hai phep so sanh int, nen khong ton
        /// gi. Kich co doi vi nhieu ly do — bam F11, keo vien cua so, doi man
        /// hinh, hoac trong Unity Editor la keo lai o Game / keo thanh Scale —
        /// nen bat o day thay vi nhet rieng vao ham F11.
        /// </remarks>
        private static void theoDoiKichCo()
        {
            if (Screen.width == rongDaTinh && Screen.height == caoDaTinh)
            {
                return;
            }
            if (Screen.width <= 0 || Screen.height <= 0)
            {
                // Cua so thu nho xuong thanh bieu tuong: bo qua, cho khi mo lai.
                return;
            }
            rongDaTinh = Screen.width;
            caoDaTinh = Screen.height;
            apDungKichCoMoi();
        }

        /// <summary>
        /// Tinh lai ca nam tang kich co theo co cua so hien tai.
        /// </summary>
        /// <remarks>
        /// <para>Thu tu la bat buoc, tang sau doc tang truoc:</para>
        /// <list type="number">
        /// <item><c>ScaleGUI</c> giu co that cua cua so;</item>
        /// <item><c>zoomLevel</c> tinh tu co do;</item>
        /// <item><c>GameCanvas.w/h</c> = co that chia zoom;</item>
        /// <item>toa do HUD trong <c>GameScr</c>;</item>
        /// <item>hang nut Tab.</item>
        /// </list>
        /// Lam sai thu tu thi tang sau van an so cu.
        /// </remarks>
        public static void apDungKichCoMoi()
        {
            ScaleGUI.initScaleGUI();
            if (MotherCanvas.instance != null)
            {
                MotherCanvas.instance.checkZoomLevel(
                        MotherCanvas.instance.getWidth(),
                        MotherCanvas.instance.getHeight());
            }
            if (GameMidlet.gameCanvas != null)
            {
                // Ham nay cung goi mScreen.initPos(), tuc la man hinh dang mo
                // (dang nhap, chon server, menu...) cung duoc xep lai.
                GameMidlet.gameCanvas.capNhatKichThuoc();
            }
            // `loadCamera` doc Char.myCharz() va TileMap, nen chi goi khi da
            // thuc su vao trong game — goi som la NullReference.
            if (GameScr.instance != null && Char.myCharz() != null
                    && TileMap.tmw > 0)
            {
                GameScr.loadCamera(true, -1, -1);
            }
            TabControll.xepLaiCho();
        }


        private void SetInit()
        {
            base.enabled = true;
        }
    
        private void OnHideUnity(bool isGameShown)
        {
            if (!isGameShown)
            {
                Time.timeScale = 0f;
            }
            else
            {
                Time.timeScale = 1f;
            }
        }
    
        private void OnGUI()
        {
            if (count >= 10 )
            {
                if (fps == 0)
                {
                    timefps = mSystem.currentTimeMillis();
                }
                else if (mSystem.currentTimeMillis() - timefps > 1000)
                {
                    max = fps;
                    fps = 0;
                    timefps = mSystem.currentTimeMillis();
                }
                fps++;
                // Doc phim cho o nhap chung o MOI su kien, khong chi Repaint:
                // moi su kien chi mang dung mot phim, bo sot la mat phim.
                God.HopNhapChu.getInstance().docPhim();
                checkInput();
                Session_ME.update();
                Session_ME2.update();
                // Ve MOI khung Repaint. Truoc day co dieu kien paintCount <= updateCount:
                // updateCount tang trong FixedUpdate (50 lan/giay) con Repaint chay theo
                // targetFrameRate (120), nen phan lon khung bi bo qua va khong ve gi ca
                // -> khung trong xen ke khung co hinh = man hinh nhay lien tuc.
                if ( TabManagement.tab == TabType.Tab2 && Event.current.type.Equals(EventType.Repaint))
                {
                    GameMidlet.gameCanvas.paint(g);
                    //Canvas.gI().paint();
                    paintCount++;
                    g.reset();
                }
            }
        }
    
        public void setsizeChange()
        {
            if (!isRun)
            {
                Screen.sleepTimeout = SleepTimeout.NeverSleep;
                Screen.orientation = ScreenOrientation.AutoRotation;
                Application.runInBackground = true;
                // Dien thoai chot o 60, may tinh moi de 120.
                //
                // Man hinh dien thoai phan lon la 60Hz, va Unity van CHAY du
                // 120 khung mot giay du man hinh chi hien duoc mot nua — mot
                // nua so khung do la cong toi khong ai nhin thay. May nong len,
                // ban dan xung nhip xuong, va tu do khung hinh tut that. Dat
                // dung 60 thi may lam dung phan viec co nguoi xem.
                Application.targetFrameRate = isPC ? 120 : 60;
                base.useGUILayout = false;
                isCompactDevice = detectCompactDevice();
                if (main == null)
                {
                    main = this;
                }
                isRun = true;
                ScaleGUI.initScaleGUI();
                if (isPC)
                {
                    IMEI = SystemInfo.deviceUniqueIdentifier;
                }
                else
                {
                    IMEI = GetMacAddress();
                }
                // Truoc day cho nay ep `Screen.fullScreen = false`, nen du co
                // doi cach nao thi game cung tro ve cua so. Gio ton trong lua
                // chon da luu.
                if (isPC)
                {
                    apCheDoManHinh();
                }
                if (isWindowsPhone)
                {
                    typeClient = 6;
                }
                if (isPC)
                {
                    typeClient = 4;
                }
                if (IphoneVersionApp)
                {
                    typeClient = 5;
                }
                if (iPhoneSettings.generation == iPhoneGeneration.iPodTouch4Gen)
                {
                    isIpod = true;
                }
                if (iPhoneSettings.generation == iPhoneGeneration.iPhone4)
                {
                    isIphone4 = true;
                }
                g = new mGraphics();
                midlet = new GameMidlet();
                TileMap.loadBg();
                Paint.loadbg();
                PopUp.loadBg();
                GameScr.loadBg();
                InfoMe.gI().loadCharId();
                Panel.loadBg();
                Menu.loadBg();
                TabCommand.loadBG();
                Key.mapKeyPC();
                SoundMn.gI().loadSound(TileMap.mapID);
            }
        }
    
        public static void setBackupIcloud(string path)
        {
        }
    
        public string GetMacAddress()
        {
            string empty = string.Empty;
            NetworkInterface[] allNetworkInterfaces = NetworkInterface.GetAllNetworkInterfaces();
            for (int i = 0; i < allNetworkInterfaces.Length; i++)
            {
                PhysicalAddress physicalAddress = allNetworkInterfaces[i].GetPhysicalAddress();
                if (physicalAddress.ToString() != string.Empty)
                {
                    return physicalAddress.ToString();
                }
            }
            return string.Empty;
        }
    
        public void doClearRMS()
        {
            if (isPC)
            {
                int num = Rms.loadRMSInt("lastZoomlevel");
                if (num != mGraphics.zoomLevel)
                {
                    Rms.clearAll();
                    Rms.saveRMSInt("lastZoomlevel", mGraphics.zoomLevel);
                    Rms.saveRMSInt("levelScreenKN", level);
                }
            }
        }
    
        public static void closeKeyBoard()
        {
            if (TouchScreenKeyboard.visible)
            {
                TField.kb.active = false;
                TField.kb = null;
            }
        }
    
        private void FixedUpdate()
        {
            Rms.update();
            count++;
            if (count >= 10)
            {
                if (up == 0)
                {
                    timeup = mSystem.currentTimeMillis();
                }
                else if (mSystem.currentTimeMillis() - timeup > 1000)
                {
                    upmax = up;
                    up = 0;
                    timeup = mSystem.currentTimeMillis();
                }
                up++;
                setsizeChange();
                updateCount++;
                    ipKeyboard.update();
                    GameMidlet.gameCanvas.update();
                    Image.update();
                    DataInputStream.update();
                
                f++;
                if (f > 8)
                {
                    f = 0;
                }
                if (!isPC)
                {
                    int num = 1 / a;
                }
            }
        }
    
        private void Update()
        {
        }
    
        internal void checkInput()
        {
            // F11: doi qua lai cua so / toan man hinh.
            //
            // Dat TRUOC dong chan theo tab: dat sau thi chi tab dang hien moi
            // nghe F11, ma khi nguoi choi mo them tab thi tab nay khong con la
            // tab dang hien, F11 se im.
            //
            // Doc truc tiep Input.GetKeyDown chu khong qua MyKeyMap: bang do
            // doi phim thanh ma phim game roi day vao man hinh dang mo, con
            // F11 la phim cua CUA SO nen phai an du dang o dau.
            if (isPC && Input.GetKeyDown(KeyCode.F11))
            {
                doiToanManHinh();
            }
            // Cung ly do dat truoc dong chan theo tab: cua so la cua chung ca
            // sau tab, tab nao dang hien khong lien quan.
            theoDoiKichCo();
            if (TabManagement.tab != TabType.Tab2) return;
            if (Input.GetMouseButtonDown(0))
            {
                Vector3 mousePosition = Input.mousePosition;
                GameMidlet.gameCanvas.pointerPressed((int)(mousePosition.x / (float)mGraphics.zoomLevel), (int)(((float)Screen.height - mousePosition.y) / (float)mGraphics.zoomLevel) + mGraphics.addYWhenOpenKeyBoard);
                lastMousePos.x = mousePosition.x / (float)mGraphics.zoomLevel;
                lastMousePos.y = mousePosition.y / (float)mGraphics.zoomLevel + (float)mGraphics.addYWhenOpenKeyBoard;
            }
            if (Input.GetMouseButton(0))
            {
                Vector3 mousePosition2 = Input.mousePosition;
                GameMidlet.gameCanvas.pointerDragged((int)(mousePosition2.x / (float)mGraphics.zoomLevel), (int)(((float)Screen.height - mousePosition2.y) / (float)mGraphics.zoomLevel) + mGraphics.addYWhenOpenKeyBoard);
                lastMousePos.x = mousePosition2.x / (float)mGraphics.zoomLevel;
                lastMousePos.y = mousePosition2.y / (float)mGraphics.zoomLevel + (float)mGraphics.addYWhenOpenKeyBoard;
            }
            if (Input.GetMouseButtonUp(0))
            {
                Vector3 mousePosition3 = Input.mousePosition;
                lastMousePos.x = mousePosition3.x / (float)mGraphics.zoomLevel;
                lastMousePos.y = mousePosition3.y / (float)mGraphics.zoomLevel + (float)mGraphics.addYWhenOpenKeyBoard;
                GameMidlet.gameCanvas.pointerReleased((int)(mousePosition3.x / (float)mGraphics.zoomLevel), (int)(((float)Screen.height - mousePosition3.y) / (float)mGraphics.zoomLevel) + mGraphics.addYWhenOpenKeyBoard);
            }
    
            if (TField.currentTField != null && TField.currentTField.isFocus)
                TField.currentTField.HandleInputText();
    
            if (Input.anyKeyDown && Event.current.type == EventType.KeyDown)
            {
                int num = MyKeyMap.map(Event.current.keyCode);
                // WASD di chuyen. Doi thanh ma mui ten TRUOC khi day xuong, va
                // chi khi khong go chu — go "was" trong khung chat ma nhan vat
                // chay lung tung thi khong ai chat noi.
                int huong = dangGoChu() ? 0 : MyKeyMap.wasd(Event.current.keyCode);
                if (huong != 0)
                {
                    num = huong;
                    // Ghi lai la cu bam nay den tu WASD. Ma -1..-4 chung voi
                    // bon mui ten, nen khong danh dau thi cho xu ly khong con
                    // biet duong nao ma lan.
                    GameCanvas.phimWasd[-huong - 1] = true;
                }
                if (num == -30)
                   CheckBackButtonPress();
                if (num != 0)
                    GameMidlet.gameCanvas.keyPressedz(num);
            }
            if (Event.current.type == EventType.KeyUp)
            {
                int num2 = MyKeyMap.map(Event.current.keyCode);
                int huong2 = dangGoChu() ? 0 : MyKeyMap.wasd(Event.current.keyCode);
                if (huong2 != 0)
                {
                    num2 = huong2;
                    GameCanvas.phimWasd[-huong2 - 1] = false;
                }
                if (num2 != 0)
                    GameMidlet.gameCanvas.keyReleasedz(num2);
            }

            // Chi doc chuot tren ban may tinh.
            //
            // Ham nay chay MOI su kien giao dien, khong chi moi khung hinh —
            // tren dien thoai mot cu cham sinh ra ca chuoi su kien. Doc mot cai
            // chuot khong ton tai o day chi de tra ve so khong.
            if (isPC)
            {
                GameMidlet.gameCanvas.scrollMouse((int)(Input.GetAxis("Mouse ScrollWheel") * 10f));
                float x = Input.mousePosition.x;
                float y = Input.mousePosition.y;
                int x2 = (int)x / mGraphics.zoomLevel;
                int y2 = (Screen.height - (int)y) / mGraphics.zoomLevel;
                GameMidlet.gameCanvas.pointerMouse(x2, y2);
            }
        }
        /// <summary>Con tro dang o trong o nhap chu hay khung chat.</summary>
        /// <remarks>
        /// Dung de tach WASD: luc go chu thi W A S D phai la chu cai, luc choi
        /// thi la bon huong di.
        /// </remarks>
        private static bool dangGoChu()
        {
            try
            {
                // Ba hộp nhập thật sự: khung chat cũ, hộp nhập chữ và bàn phím
                // số của bản mod. Cái nào đang mở thì chắc chắn là đang gõ.
                if (ChatTextField.gI() != null && ChatTextField.gI().isShow)
                {
                    return true;
                }
                if (God.HopNhapChu.getInstance().dangMo
                        || God.BanPhimSo.getInstance().dangMo)
                {
                    return true;
                }
                if (TField.currentTField == null || !TField.currentTField.isFocus)
                {
                    return false;
                }
                // Ô nhập ĐANG có con trỏ — nhưng nó nằm ở màn nào?
                //
                // `LoginScr.switchToMe` đặt `tfUser.isFocus = true` trên bản máy
                // tính, và KHÔNG AI xoá cờ đó sau khi vào game. Nên chỉ đọc
                // `isFocus` thì vào tới màn chơi là W A S D chết hẳn — đúng lỗi
                // vừa gặp: bấm không đi được hướng nào.
                //
                // Ở trong màn chơi thì chỉ tính là đang gõ khi CÓ một bảng đang
                // mở (bảng nào cũng có thể chứa ô nhập: tìm bạn, nhập số lượng…).
                // Còn ngoài màn chơi — đăng nhập, đăng ký, tạo nhân vật — thì ô
                // nhập là thứ duy nhất người ta đang làm việc với, cứ tính là gõ.
                if (GameCanvas.currentScreen != GameScr.gI())
                {
                    return true;
                }
                return (GameCanvas.panel != null && GameCanvas.panel.isShow)
                        || (GameCanvas.panel2 != null && GameCanvas.panel2.isShow);
            }
            catch (System.Exception)
            {
                return false;
            }
        }

        internal static void CheckBackButtonPress()

        {
            if (GameCanvas.panel != null || GameCanvas.panel2 != null)
            {
                if (GameCanvas.panel != null && GameCanvas.panel.isShow)
                {
                    GameCanvas.panel.hide();
                    return;
                }
                if (GameCanvas.panel2 != null && GameCanvas.panel2.isShow)
                {
                    GameCanvas.panel2.hide();
                    return;
                }
            }
            if (InfoDlg.isShow)
                return;
            if (GameCanvas.currentDialog != null && GameCanvas.currentDialog is MsgDlg)
            {
                GameCanvas.endDlg();
                return;
            }
            if (ChatTextField.gI().isShow)
            {
                ChatTextField.gI().close();
                return;
            }
            if (GameCanvas.menu.showMenu)
            {
                GameCanvas.menu.doCloseMenu();
                return;
            }
            GameCanvas.checkBackButton();
        }
        private void OnApplicationQuit()
        {
            Debug.LogWarning("APP QUIT");
            GameCanvas.bRun = false;
            Session_ME.gI().close();
            Session_ME2.gI().close();
            if (isPC)
            {
                Application.Quit();
            }
        }
    
        private void OnApplicationPause(bool paused)
        {
            isResume = false;
            if (paused)
            {
                if (GameCanvas.isWaiting())
                {
                    isQuitApp = true;
                }
            }
            else
            {
                isResume = true;
            }
            if (TouchScreenKeyboard.visible)
            {
                TField.kb.active = false;
                TField.kb = null;
            }
            if (isQuitApp)
            {
                Application.Quit();
            }
        }
    
        public static void exit()
        {
            if (isPC)
            {
                main.OnApplicationQuit();
            }
            else
            {
                a = 0;
            }
        }
    
        public static bool detectCompactDevice()
        {
            if (iPhoneSettings.generation == iPhoneGeneration.iPhone || iPhoneSettings.generation == iPhoneGeneration.iPhone3G || iPhoneSettings.generation == iPhoneGeneration.iPodTouch1Gen || iPhoneSettings.generation == iPhoneGeneration.iPodTouch2Gen)
            {
                return false;
            }
            return true;
        }
    
        public static bool checkCanSendSMS()
        {
            if (iPhoneSettings.generation == iPhoneGeneration.iPhone3GS || iPhoneSettings.generation == iPhoneGeneration.iPhone4 || iPhoneSettings.generation > iPhoneGeneration.iPodTouch4Gen)
            {
                return true;
            }
            return false;
        }
    }
}
