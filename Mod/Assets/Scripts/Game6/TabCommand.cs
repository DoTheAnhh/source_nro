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
        /// <summary>
        /// Nút tab, vẽ bằng mã thay cho hai ảnh <c>img1</c>/<c>img2</c>.
        /// </summary>
        /// <remarks>
        /// Hai ảnh cũ chỉ có một cỡ nên phóng to là rỗ, và chúng là hai tấm
        /// phẳng không ăn nhập với phần HUD còn lại đã dựng bằng mã.
        ///
        /// Dùng chung <c>GameScr.veKhungNutNhanh</c> với hai nút Capsule và Đậu
        /// thần: vành đồng, viền kép, lòng ô sáng, bốn đinh tán ở góc.
        /// </remarks>
        public override void paint(mGraphics g)
        {
            GameScr.veKhungNutNhanh(g, x, y, w, h, isFocus);
            // Chu vang co mot net bong toi: long o sang nen chu toi tron se
            // chim, ma chu vang khong bong thi vien chu nhoe vao nen.
            int yChu = y + h / 2 - mFont.tahoma_7b_yellow.getHeight() / 2;
            mFont.tahoma_7b_dark.drawString(g, caption, x + w / 2 + 1,
                    yChu + 1, 3);
            mFont.tahoma_7b_yellow.drawString(g, caption, x + w / 2, yChu, 3);
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
