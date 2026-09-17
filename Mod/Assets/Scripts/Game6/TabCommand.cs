using System;

namespace Game6
{
    
    public class TabCommand : BaseCommand
    {
        private static Image menu, menu1;
        public TabCommand(string caption, Action action) : base(caption, action)
        {
            this.caption = caption;
            this.action = action;
            this.w = 26;
            this.h = 26;
        }
        public static void loadBG()
        {
            menu = GameCanvas.loadImage("/mainImage/img1.png");
            menu1 = GameCanvas.loadImage("/mainImage/img2.png");
        }
        /// <summary>Hình của nút này, đoán theo chữ trên nút.</summary>
        /// <remarks>
        /// Ba nút dùng chung một lớp và chỉ khác nhau ở chữ, nên đọc chữ là
        /// cách duy nhất phân biệt mà không phải thêm một trường vào cả ba chỗ
        /// dựng nút. Chữ "Khu" và "Tab" còn kèm số ("Khu 0", "Tab 1") nên so
        /// bằng <c>StartsWith</c>; không khớp cái nào thì lấy hình thẻ.
        /// </remarks>
        private int hinhNut()
        {
            if (caption != null)
            {
                if (caption.StartsWith("Cờ"))
                {
                    return GameScr.NUT_CO;
                }
                if (caption.StartsWith("Khu"))
                {
                    return GameScr.NUT_KHU;
                }
            }
            return GameScr.NUT_TAB;
        }

        /// <summary>
        /// Nút Cờ / Khu / Tab: <b>hình ở trên, tên ở dưới</b>.
        /// </summary>
        /// <remarks>
        /// Cả năm nút của hàng HUD đi qua <c>GameScr.veNutHud</c> — kể cả ô
        /// Chat và nút ba gạch do hai lớp khác vẽ. Sửa kiểu một lần là cả hàng
        /// đổi theo, không có nút nào lạc kiểu.
        /// </remarks>
        public override void paint(mGraphics g)
        {
            GameScr.veNutHud(g, x, y, w, h, hinhNut(), caption, isFocus);
        }
    
        public override bool isPointerInside()
        {
            isFocus = false;
            if (GameCanvas.isPointerHoldIn(x, y, w, h))
            {
                if (GameCanvas.isPointerDown)
                {
                    isFocus = true;
                }
                if(GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
                {
                    return true;
                }
            }
            return false;
        }
        public override void Invoke()
        {
            GameCanvas.clearAllPointerEvent();
            action?.Invoke();
        }
    }
}
