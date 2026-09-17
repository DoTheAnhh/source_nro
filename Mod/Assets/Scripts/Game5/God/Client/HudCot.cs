namespace Game5.God
{
    /// <summary>
    /// Cột nút HUD ở mép phải: Chat, Cờ, Khu, Tab, Boss, Sự kiện, Phúc lợi,
    /// Nhân vật, Trò chơi.
    /// </summary>
    /// <remarks>
    /// <para><b>Một chỗ xếp cho tất cả.</b> Trước đây ba nút Cờ / Khu / Tab do
    /// <c>TabControll</c> tự xếp, ô Chat do <c>GameScr</c> xếp, còn năm mục kia
    /// nằm trong một popup riêng. Ba chỗ xếp ba kiểu nên hàng nút so le nhau, và
    /// mỗi lần đổi lưới là phải sửa cả ba. Nay mọi nút hỏi đúng một hàm
    /// <see cref="oNut"/>.</para>
    ///
    /// <para><b>Nút ba gạch đứng riêng một cột</b> ở góc trên phải và không nằm
    /// trong danh sách này — nó là cái công tắc thu/mở cả cột.</para>
    ///
    /// <para><b>Thu mở trượt mượt.</b> <see cref="mo"/> chạy từ 0 tới 1 theo lối
    /// giảm dần (mỗi khung hình đi một phần quãng còn lại) nên nút trôi ra nhanh
    /// lúc đầu rồi chậm dần khi tới chỗ — không giật cục như cộng đều. Lúc đang
    /// thu, cả cột trượt lên nấp sau nút ba gạch.</para>
    /// </remarks>
    public static class HudCot
    {
        public const int CHAT = 0;
        public const int CO = 1;
        public const int KHU = 2;
        public const int TAB = 3;
        public const int BOSS = 4;
        public const int SU_KIEN = 5;
        public const int PHUC_LOI = 6;
        public const int NHAN_VAT = 7;
        public const int TRO_CHOI = 8;
        public const int SO_NUT = 9;

        /// <summary>Bề rộng một nút.</summary>
        public const int RONG = 38;

        /// <summary>Bề cao một nút: icon 20 điểm cộng hàng chữ.</summary>
        public const int CAO = 32;

        /// <summary>Khe dọc giữa hai nút.</summary>
        private const int KHE = 3;

        /// <summary>Mép phải của cả cột, cách mép màn hình đúng bằng nút ba gạch.</summary>
        private const int LE_PHAI = 7;

        /// <summary>Đỉnh nút đầu tiên — ngay dưới nút ba gạch.</summary>
        private const int Y_DAU = 42;

        /// <summary>Đang mở hay đang thu.</summary>
        public static bool dangMo = true;

        /// <summary>Mức mở hiện tại: 0 nấp hẳn, 1 ra hẳn.</summary>
        private static float mo = 1f;

        public static void doiThuMo()
        {
            dangMo = !dangMo;
        }

        /// <summary>Chạy hoạt ảnh. Gọi mỗi khung hình, trước khi vẽ.</summary>
        public static void capNhat()
        {
            float dich = dangMo ? 1f : 0f;
            float lech = dich - mo;
            if (lech > -0.004f && lech < 0.004f)
            {
                mo = dich;
                return;
            }
            // Moi khung hinh di 22% quang con lai: nhanh luc dau, cham dan khi
            // toi noi. Cong deu thi nut chay nhu bi keo, khong ra truot.
            mo += lech * 0.22f;
        }

        /// <summary>Cột đã ra đủ để bấm được chưa.</summary>
        public static bool bamDuoc()
        {
            return mo > 0.55f;
        }

        /// <summary>Còn thấy được thì còn phải vẽ.</summary>
        public static bool conThay()
        {
            return mo > 0.02f;
        }

        /// <summary>Vùng {x, y, rộng, cao} của nút thứ <paramref name="i"/>.</summary>
        /// <remarks>
        /// Đã tính cả phần trượt: lúc thu, nút nằm chồng lên chỗ nút ba gạch.
        /// </remarks>
        public static int[] oNut(int i)
        {
            int x = GameCanvas.w - LE_PHAI - RONG;
            int yRaHan = Y_DAU + i * (CAO + KHE);
            int yNap = Y_DAU - CAO - 8;
            int y = (int) (yNap + (yRaHan - yNap) * mo);
            return new int[] { x, y, RONG, CAO };
        }

        /// <summary>Mép dưới của cả cột khi đang mở — chỗ khác xếp tiếp từ đây.</summary>
        public static int mepDuoi()
        {
            return Y_DAU + SO_NUT * (CAO + KHE);
        }
    }
}
