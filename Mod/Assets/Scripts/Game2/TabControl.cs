
namespace Game2
{
    
    using System;
    using System.Linq;
    using UnityEngine;
    using UnityEngine.SceneManagement;
    
    public class TabControll : mScreen
    {
        private static TabControll _Instance;
        public static TabControll Instance => _Instance ?? (_Instance = new TabControll());
    
        public TabControll()
        {
            initCommand();
        }
    
        private static bool _selectTab;
    
        public static bool selectTab
        {
            get => _selectTab;
            set => _selectTab = value;
        }
    
        private static bool _isShow = true;
    
        public static bool isShow
        {
            get => _isShow;
            set => _isShow = value;
        }
    
        private static sbyte tabIndex = 0;
    
        private static TabCommand firstCommand = new TabCommand("Tab", () => showTabSelect());

        /// <summary>
        /// Nút "Khu" — cùng việc với phím <c>m</c>: mở bảng đổi khu.
        /// </summary>
        /// <remarks>
        /// Đẩy một phím giả vào <c>keyAsciiPress</c> chứ không gọi thẳng lệnh
        /// đổi khu: đường phím đã lo sẵn mọi điều kiện (đang ở bản đồ nào, có
        /// đang bận việc gì không), gọi tắt là phải chép lại từng điều kiện đó.
        /// </remarks>
        private static TabCommand khuCommand = new TabCommand("Khu", () =>
        {
            GameCanvas.keyAsciiPress = 'm';
        });

        /// <summary>
        /// Nút "Cờ" — xin danh sách cờ, máy chủ trả về thì bảng chọn tự bật.
        /// </summary>
        /// <remarks>
        /// Bảng cờ <c>God.CoUI</c> không có hàm mở: cách mở nó là <b>xin dữ
        /// liệu</b>, và chính lúc gói trả lời về thì nó mới bật lên (xem
        /// <c>CoUI.nhanDuLieu</c>). Nên nút này chỉ gửi lời xin và bật hộp
        /// "vui lòng chờ" — đúng như chỗ gọi cũ trong <c>GameCanvas</c>.
        /// </remarks>
        private static TabCommand coCommand = new TabCommand("Cờ", () =>
        {
            Service.gI().getFlag(0, -1);
            InfoDlg.showWait();
        });
    
        /// <summary>So tab cho hien trong bang chon tab.</summary>
        /// <remarks>
        /// Dat 3 de an bot, du <see cref="SceneNames"/> van co du 6 scene.
        /// Hai bang <see cref="SceneNames"/> va <see cref="tabTypes"/> tra cuu
        /// theo chi so nen KHONG cat bot theo — cat la lech chi so. Muon mo
        /// lai du 6 tab thi doi rieng con so nay.
        /// </remarks>
        private const int SO_TAB = 3;

        private static TabCommand[] TransferTab = Enumerable.Range(0, SO_TAB)
              .Select(i => new TabCommand((i + 1).ToString(), () => TransferTabIndex((sbyte)(i - 1))))
              .ToArray();
    
        private static string[] SceneNames = new string[]
        {
            "DTA",
            "DTA2",
            "DTA3",
            "DTA4",
            "DTA5",
            "DTA6"
        };
        private static TabType[] tabTypes = new TabType[]
        {
            TabType.Tab1,
            TabType.Tab2, TabType.Tab3,
            TabType.Tab4, TabType.Tab5, TabType.Tab6
        };
        private static void initCommand()
        {
            xepLaiCho();
        }

        /// <summary>Xep lai cho nut Tab va hang so tab theo co man hinh.</summary>
        /// <remarks>
        /// Tach ra khoi <c>initCommand</c> (chi chay mot lan trong ham tao) de
        /// goi lai duoc khi cua so doi kich co — xem <c>Main.apDungKichCoMoi</c>.
        /// </remarks>
        /// <summary>Mép dưới của hàng nút thứ hai (Tab / Chat).</summary>
        /// <remarks>
        /// Danh sách nhân vật trong map neo theo con số này, nên nó nằm hẳn dưới
        /// cụm nút góc trên phải thay vì chui vào sau lưng chúng.
        /// </remarks>
        public static int mepDuoiHangNut()
        {
            // Day cua CA COT nut HUD, khong phai day cua rieng nut Tab: cot gio
            // dai chin nut, neo theo nut Tab thi danh sach nhan vat trong map
            // chui vao sau lung may nut duoi.
            return God.HudCot.mepDuoi();
        }

        /// <summary>Mép trái của nút "Cờ" — bảng mục tiêu dừng trước chỗ này.</summary>
        /// <remarks>
        /// Kiểu <b>công bố ô vừa xếp</b>: chỗ xếp chỗ ghi lại con số nó dùng,
        /// chỗ khác đọc lại. Chép tay con số 133 sang bên kia thì đổi lưới nút
        /// một cái là hai bên lệch nhau mà không ai báo.
        /// </remarks>
        public static int mepTraiNutCo()
        {
            return (coCommand != null && coCommand.w > 0)
                    ? coCommand.x : (GameCanvas.w - 133);
        }

        /// <summary>Xếp lại chỗ ba nút theo cột HUD.</summary>
        /// <remarks>
        /// Cả hàng nút nay do <c>God.HudCot</c> xếp — một chỗ duy nhất cho chín
        /// nút, kể cả ô Chat của <c>GameScr</c> và năm nút do
        /// <c>ClientManager</c> vẽ. Hàm này còn để những chỗ gọi cũ (đổi cỡ cửa
        /// sổ) không phải sửa.
        /// </remarks>
        public static void xepLaiCho()
        {
            theoCotHud();
        }
        /// <summary>Co ve hang nut Tab luc nay khong.</summary>
        /// <remarks>
        /// <para>Man phu cua mod (menu tong, boss, su kien, phuc loi...) dang mo
        /// thi giau di. <c>paint</c> cua lop nay la thu VE CUOI CUNG trong
        /// <c>GameCanvas.paint()</c>, con cac man phu ve trong
        /// <c>GameScr.paint()</c> — chay truoc — nen khong giau thi nut Tab noi
        /// tren mat popup.</para>
        ///
        /// <para>Giau thay vi doi thu tu ve, vi con mot nua o phia CHAM:
        /// <c>GameCanvas</c> hoi <c>isPointerHoldInTab()</c> TRUOC khi day cham
        /// cho <c>currentScreen</c>. Chi day nut xuong duoi ma khong giau thi
        /// bam vao cho do van an vao nut Tab chu khong vao popup.</para>
        ///
        /// <para>Chi hoi man phu khi da vao game: <c>coManPhuDangMo()</c> goi
        /// <c>getInstance()</c> cua sau man phu, ma ngoai game thi khong the co
        /// popup nao — hoi chi de tao som sau the instance do.</para>
        /// </remarks>
        /// <summary>
        /// Hàng nút Cờ / Khu / Tab có được vẽ và nhận chạm ở khung hình này không.
        /// </summary>
        /// <remarks>
        /// Ba nút này nằm <b>dưới cùng</b> mọi thứ: hễ có bảng, menu hay màn phụ
        /// nào mở ra là chúng nhường chỗ.
        ///
        /// Trước đây chúng chỉ tránh màn phụ của mod, còn bảng của game (trang
        /// bị, cửa hàng, bang hội…) thì không — nên mở bảng ra là ba cái nút nổi
        /// đè lên mặt bảng, che mất phần trên bên phải của nó.
        ///
        /// Cách nhường chỗ là <b>không vẽ</b> chứ không phải vẽ sớm hơn: chuỗi
        /// vẽ của game đặt bảng sau cùng, muốn nút nằm dưới thì phải chen vào
        /// giữa chuỗi ấy, mà chen vào đó thì mỗi lần game đổi thứ tự vẽ là hỏng.
        /// </remarks>
        private static bool choHien()
        {
            if (!isShow)
            {
                return false;
            }
            if (GameCanvas.panel != null && GameCanvas.panel.isShow)
            {
                return false;
            }
            if (GameCanvas.menu != null && GameCanvas.menu.showMenu)
            {
                return false;
            }
            if (GameCanvas.currentDialog != null)
            {
                return false;
            }
            // CHI hien khi da vao man choi.
            //
            // Truoc day dieu kien la "khong phai GameScr THI hien", nen o man
            // dang nhap va man chon may chu ba cai nut nay noi lung lung giua
            // troi — Khu voi Co luc do con chua co nghia gi.
            if (!(GameCanvas.currentScreen is GameScr))
            {
                return false;
            }
            return !God.ClientManager.coManPhuDangMo();
        }

        public override void paint(mGraphics g)
        {
            if (!choHien())
            {
                // Dong luon hang so tab: mo popup roi dong popup thi hang so
                // khong con treo lo lung.
                _selectTab = false;
                return;
            }
            // So tab ghi THANG TRONG nut, khong con dong chu do treo ben duoi.
            //
            // Dong chu do do nam ngoai nut nen no khong gan voi cai gi ca, va
            // mau do thi lac han giua mot HUD toan tong dong vang.
            int currentTabIndex = -1;
            string currentScene = UnityEngine.SceneManagement.SceneManager.GetActiveScene().name;
            for (int i = 0; i < SceneNames.Length; i++)
            {
                if (SceneNames[i] == currentScene)
                {
                    currentTabIndex = i;
                    break;
                }
            }
            if (!God.HudCot.conThay())
            {
                // Cot dang thu han: khong ve nut nao.
                return;
            }
            // Xep lai theo cot HUD ngay truoc khi ve: cot con dang truot nen
            // moi khung hinh mot cho khac.
            theoCotHud();
            firstCommand.caption = "Tab " + (currentTabIndex + 1);
            firstCommand.paint(g);
            // So khu doc thang tu ban do dang dung, khong giu mot ban sao rieng:
            // giu ban sao thi doi khu xong nut van ghi so cu cho toi khi co ai
            // nho cap nhat no.
            // So khu in THANG len tam bang go cua icon, chu duoi chi con "Khu".
            khuCommand.caption = "Khu";
            khuCommand.soTrenIcon = TileMap.zoneID + "";
            khuCommand.paint(g);
            coCommand.paint(g);
            paintTab(g);
            base.paint(g);
        }
        private void paintTab(mGraphics g)
        {
            if (!selectTab) return;
            foreach (var cmd in TransferTab)
            {
                cmd.paint(g);
            }
        }
        private static void TransferTabIndex(sbyte index)
        {
            tabIndex = (sbyte)(index + 1);
            
            SceneManager.LoadScene(SceneNames[index + 1]);
            TabManagement.tab = tabTypes[index + 1];
            _selectTab = false;
        }
        private static void showTabSelect()
        {
            _selectTab = !_selectTab;
        }
        /// <summary>Đặt ba nút vào đúng chỗ của chúng trong cột HUD.</summary>
        /// <remarks>
        /// Cột do <c>God.HudCot</c> xếp, và nó còn trượt lúc thu mở nên phải hỏi
        /// lại mỗi khung hình — cả lúc vẽ lẫn lúc bắt chạm, kẻo bấm một nơi mà
        /// nút nằm một nẻo.
        /// </remarks>
        private static void theoCotHud()
        {
            datO(coCommand, God.HudCot.CO);
            datO(khuCommand, God.HudCot.KHU);
            datO(firstCommand, God.HudCot.TAB);
            // Hang so tab nam ngay ben TRAI nut Tab.
            for (int i = 0; i < TransferTab.Length; i++)
            {
                TransferTab[i].w = 20;
                TransferTab[i].h = God.HudCot.CAO;
                TransferTab[i].x = firstCommand.x
                        - (TransferTab.Length - i) * 23;
                TransferTab[i].y = firstCommand.y;
            }
        }

        private static void datO(TabCommand c, int chiSo)
        {
            int[] o = God.HudCot.oNut(chiSo);
            c.x = o[0];
            c.y = o[1];
            c.w = o[2];
            c.h = o[3];
        }

        public bool isPointerHoldInTab()
        {
            if (!choHien() || !God.HudCot.bamDuoc())
                return false;
            theoCotHud();
            if (firstCommand.isPointerInside())
            {
                firstCommand.Invoke();
                return true;
            }
            if (khuCommand.isPointerInside())
            {
                khuCommand.Invoke();
                return true;
            }
            if (coCommand.isPointerInside())
            {
                coCommand.Invoke();
                return true;
            }
            if (selectTab)
            {
                foreach (var cmd in TransferTab)
                {
                    if (cmd.isPointerInside())
                    {
                        cmd.Invoke();
                        return true;
                    }
                }
            }
            return false;
        }
        public override void updateKey()
        {
            base.updateKey();
        }
    }
}
