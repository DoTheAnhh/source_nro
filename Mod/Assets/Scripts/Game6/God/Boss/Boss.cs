namespace Game6.God
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
        /// <summary>Mép trên của khung, tính từ đỉnh màn hình.</summary>
        /// <remarks>
        /// <para>Phải nằm DƯỚI hàng nút góc phải — "Cờ", "Khu", "Tab", "Chat".
        /// Con số cũ là 37, tức khung bắt đầu ngay dưới thanh trên cùng và đè
        /// lên đúng chỗ hai hàng nút ấy: nút vẫn bấm được nhưng chữ chồng chữ,
        /// nhìn không ra cái nào.</para>
        ///
        /// <para>Hàng nút cuối cùng ("Tab" / "Chat") kết thúc quanh 150, nên 160
        /// là vừa hở. Không đọc thẳng toạ độ nút vì chúng nằm rải trong
        /// <c>GameScr</c> và đổi theo cỡ màn hình — một con số ở đây dễ chỉnh
        /// hơn nhiều so với việc bám vào bố cục của màn khác.</para>
        /// </remarks>
        private const int DONG_DAU = 160;
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
            // Man hinh thap thi keo len cho khung khong tran khoi day. Khung dai
            // ra theo so boss, nen mot con so co dinh khong du: nam boss la cao
            // gap nam mot boss.
            int yDau = DONG_DAU;
            int yToiDa = GameCanvas.h - 40 - caoKhung;
            if (yDau > yToiDa)
            {
                yDau = (yToiDa < 4) ? 4 : yToiDa;
            }
            int yKhung = yDau - DEM_DOC;

            g.setColor(MAU_NEN, 0.45f);
            g.fillRect(xKhung, yKhung, rongKhung, caoKhung);
            g.setColor(MAU_VIEN);
            g.drawRect(xKhung, yKhung, rongKhung, caoKhung);

            // Căn phải mép trong của khung, không căn theo mép màn hình — nếu
            // không thì chữ tràn ra ngoài viền.
            int xChu = GameCanvas.w - LE_PHAI - DEM_NGANG;
            for (int i = 0; i < dongChu.Count; i++)
            {
                chu.drawString(g, dongChu[i], xChu, yDau + CAO_DONG * i,
                        mFont.RIGHT, mFont.tahoma_7);
            }
        }
    }
}
