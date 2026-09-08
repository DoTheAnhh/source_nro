namespace Game4.God
{
    /*Author: HairMod*/
    public class Boss
    {
        private static Boss instance { get; set; }
        /// <summary>
        /// Khung thông báo boss có hiện hay không — <b>mặc định TẮT</b>.
        /// </summary>
        /// <remarks>
        /// Khung này nằm ngay dưới hàng nút góc trên phải và che mất chúng.
        /// Tắt sẵn chứ không xoá hẳn phần vẽ: mục "Thông báo BOSS" trong menu
        /// mod vẫn bật lại được, và hai chỗ khác (ListChars, PlayerInfo) đọc cờ
        /// này để biết xếp xuống dưới bao nhiêu.
        /// </remarks>
        public bool isShow = false;
        public static Boss getInstance()
        {
            return instance == null ? instance = new Boss() : instance;
        }
        /// <summary>Khung thông báo boss — góc trên bên phải.</summary>
        /// <remarks>
        /// Gom toạ độ, cỡ chữ và màu vào một chỗ để chỉnh chỉ phải sửa ở đây.
        /// </remarks>
        private const int LE_PHAI = 4;
        private const int DONG_DAU = 37;
        private const int CAO_DONG = 10;
        private const int DEM_NGANG = 4;
        private const int DEM_DOC = 3;
        private const int MAU_NEN = 0;
        private const int MAU_VIEN = 16755200;

        public void PaintBossInfo(mGraphics g)
        {
            if (!isShow) return;
            var bosses = BossData.getInstance().listData;
            if (bosses.Count == 0)
            {
                return;
            }

            // Chữ nhỏ hơn bản trước một cỡ, cho khung gọn lại.
            mFont chu = mFont.tahoma_7b_yellowSmall != null
                    ? mFont.tahoma_7b_yellowSmall
                    : mFont.tahoma_7b_yellow;

            // Dựng sẵn từng dòng rồi mới đo bề ngang: khung phải vừa dòng dài
            // nhất, đo sau khi vẽ thì viền luôn lệch một nhịp so với chữ.
            // Duyệt xuôi: listData thêm con mới vào cuối, nên đi từ đầu là cũ
            // nhất ở trên, mới nhất ở dưới cùng.
            var dongChu = new System.Collections.Generic.List<string>();
            int rong = 0;
            for (int i = 0; i < bosses.Count; i++)
            {
                var b = bosses[i];
                string s = b.name + " - " + b.getMapName() + " - "
                        + b.getStartTimeSpan();
                dongChu.Add(s);
                int w = chu.getWidth(s);
                if (w > rong)
                {
                    rong = w;
                }
            }

            int rongKhung = rong + DEM_NGANG * 2;
            int caoKhung = dongChu.Count * CAO_DONG + DEM_DOC * 2;
            int xKhung = GameCanvas.w - LE_PHAI - rongKhung;
            int yKhung = DONG_DAU - DEM_DOC;

            g.setColor(MAU_NEN, 0.45f);
            g.fillRect(xKhung, yKhung, rongKhung, caoKhung);
            g.setColor(MAU_VIEN);
            g.drawRect(xKhung, yKhung, rongKhung, caoKhung);

            // Căn phải mép trong của khung, không căn theo mép màn hình — nếu
            // không thì chữ tràn ra ngoài viền.
            int xChu = GameCanvas.w - LE_PHAI - DEM_NGANG;
            for (int i = 0; i < dongChu.Count; i++)
            {
                chu.drawString(g, dongChu[i], xChu, DONG_DAU + CAO_DONG * i,
                        mFont.RIGHT, mFont.tahoma_7);
            }
        }
    }
}
