using System.Collections.Generic;
using UnityEngine;

namespace Game3.God
{
    /*Author: HairMod*/
    public class ListChars
    {
        private static ListChars instance;
        public bool isShow = true;
        public bool HideMap;
        public int widthRect;
        public int heightRect;
        public List<Char> listPlayers = new List<Char>();
        public static ListChars getInstance()
        {
            return instance == null ? instance = new ListChars() : instance;
        }
        public void paintPlayerMap(mGraphics g)
        {
            if (!isShow) return;
            // Bat dau NGAY DUOI hang nut Tab / Chat.
            //
            // Con so 37 cu roi dung vao hang nut thu hai o goc tren phai, nen
            // ten nhan vat chay vao sau lung nut "Tab 1" va "Chat" — doc ra hai
            // lop chu chong nhau.
            int num = mocDongDau();
            widthRect = 142;
            heightRect = 9;
            for (int i = 0; i < listPlayers.Count; i++)
            {
                Char @char = listPlayers[i];
                if (@char.cFlag != 0)
                {
                    g.setColor(getFlagColor(@char));
                    g.fillRect(GameCanvas.w - widthRect - 10, num + 2, 7, 8);
                }
                g.setColor(2721889, 0.5f);
                g.fillRect(GameCanvas.w - widthRect, num + 2, widthRect - 2, heightRect);
                if (@char.cName != null && @char.cName != "" && !@char.isPet && !@char.isMiniPet && !@char.cName.StartsWith("#") && !@char.cName.StartsWith("$") && @char.cName != "Trọng tài")
                {
                    bool flag = isBoss(@char);
                    // Tien to cho biet ngay day la ai, khong phai doan theo mau.
                    string text = flag
                            ? "[BOSS] " + @char.cName
                                    + " [" + NinjaUtil.getMoneys(@char.cHP) + "]"
                            : @char.cName
                                    + " [" + NinjaUtil.getMoneys(@char.cHP)
                                    + " - " + @char.getGenderName() + "]";
                    // Ba mau, ba nghia RO RANG:
                    //   do    — muc tieu dang chon
                    //   vang  — BOSS
                    //   xanh  — nguoi choi
                    // Ban cu dung do cho ca "dang chon" lan "nguoi choi manh
                    // hon 100 trieu HP", nen nhin vao khong biet do la vi dang
                    // ngam hay vi manh.
                    bool dangChon = Char.myCharz().charFocus != null
                            && Char.myCharz().charFocus.charID == @char.charID;
                    if (dangChon || flag)
                    {
                        // Ke mot duong tu minh toi muc tieu / boss.
                        g.setColor(dangChon ? 14155776 : 16383818);
                        g.drawLine(Char.myCharz().cx - GameScr.cmx,
                                Char.myCharz().cy - GameScr.cmy + 1,
                                @char.cx - GameScr.cmx, @char.cy - GameScr.cmy);
                    }
                    mFont mf = dangChon ? mFont.tahoma_7_red
                            : (flag ? mFont.tahoma_7_yellow : mFont.tahoma_7_green);
                    mf.drawString(g, i + 1 + ". " + text,
                            GameCanvas.w - widthRect + 2, num, 0);
                    num += heightRect + 1;
                }
            }
        }
        /// <summary>Ô này là BOSS hay người chơi.</summary>
        /// <remarks>
        /// <para>Xét theo <c>charID &lt; 0</c>. Máy chủ đánh số boss bằng số ÂM
        /// (xem <c>BossID</c>: TRUNG_UY_XANH_LO_BDKB = -1, HATCHIYACK = -13…),
        /// còn id người chơi lấy từ khoá tự tăng của bảng <c>player</c> nên luôn
        /// dương. Không lẫn được.</para>
        ///
        /// <para>Bản cũ đoán bằng <b>chữ cái đầu có viết hoa hay không</b>. Mà
        /// gần như mọi người chơi đều đặt tên viết hoa chữ đầu, nên hầu hết
        /// người chơi bị tính là boss — đó là lý do màu trong danh sách sai.</para>
        /// </remarks>
        public bool isBoss(Char ch)
        {
            return ch != null && ch.charID < 0;
        }

        /// <summary>
        /// Toạ độ dòng đầu của danh sách — dùng chung cho cả vẽ và bắt chạm.
        /// </summary>
        /// <remarks>
        /// Trước đây hai chỗ tính hai kiểu: phần vẽ lấy
        /// <c>TabControll.mepDuoiHangNut() + 6</c>, còn phần bắt chạm lại gõ
        /// cứng 37 (hoặc 96 khi đang hiện bảng boss). Hai con số đó không bằng
        /// nhau, nên vùng bấm lệch khỏi dòng chữ: bấm vào tên này lại chọn tên
        /// khác, hoặc bấm không ăn gì.
        /// </remarks>
        private int mocDongDau()
        {
            int y = TabControll.mepDuoiHangNut() + 6;
            if (ThongTinDeTu.getInstance().isShow && y < 96)
            {
                y = 96;
            }
            return y;
        }
        private Color getFlagColor(Char @char)
        {
            switch (@char.cFlag)
            {
                case 1:
                    return Color.cyan;
                case 2:
                    return Color.red;
                case 3:
                    return new Color(0.56f, 0.19f, 0.77f);
                case 4:
                    return Color.yellow;
                case 5:
                    return Color.green;
                case 6:
                    return Color.magenta;
                case 7:
                    return new Color(1f, 0.5f, 0f);
                case 8:
                    return new Color(0.18f, 0.18f, 0.18f);
                case 9:
                    return Color.blue;
                case 10:
                    return Color.red;
                case 11:
                    return Color.blue;
                case 12:
                    return Color.white;
                case 13:
                    return Color.black;
                default:
                    return Color.clear;
            }
        }
        public void UpdateTouch()
        {
            for (int i = 0; i < listPlayers.Count; i++)
            {
                if (GameCanvas.isPointerHoldIn(GameCanvas.w - widthRect,
                        mocDongDau() + (heightRect + 1) * i, widthRect, heightRect))
                {
                    GameCanvas.isPointerJustDown = false;
                    GameScr.gI().isPointerDowning = false;
                    if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
                    {
                        Char @char = listPlayers[i];

                        // MỘT lần bấm làm cả hai việc: nhảy tới và ngắm.
                        //
                        // Bản cũ chia làm hai nhịp — bấm lần đầu chỉ ngắm, bấm
                        // lần hai mới nhảy tới. Mà giữa hai nhịp đó mục tiêu có
                        // thể đã đi mất hoặc người chơi đã ngắm sang chỗ khác,
                        // nên lần bấm thứ hai nhảy tới một chỗ không còn ai.
                        //
                        // Nhảy TRƯỚC rồi ngắm: `Teleport` gửi luôn vị trí mới
                        // lên máy chủ, còn `FocusObject` chỉ đặt mục tiêu ở
                        // client. Ngắm xong vẫn đổi mục tiêu tự do như thường —
                        // đây chỉ là lần ngắm đầu cho khỏi phải tìm.
                        Utils.Teleport(@char.cx, @char.cy);
                        Utils.FocusObject(@char.charID);

                        Char.myCharz().currentMovePoint = null;
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }
                }
            }
        }
        public void Update()
        {
            if (!isShow) return;
            listPlayers.Clear();
            for (int i = 0; i < GameScr.vCharInMap.size(); i++)
            {
                Char @char = (Char)GameScr.vCharInMap.elementAt(i);
                if (@char.cName != null && @char.cName != "" && !@char.isPet && !@char.isMiniPet && !@char.cName.StartsWith("#") && !@char.cName.StartsWith("$") && @char.cName != "Trọng tài")
                    listPlayers.Add(@char);
            }
        }
    }
}
