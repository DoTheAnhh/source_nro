namespace Game2.God
{
    /// <summary>
    /// Hàng nút HUD ở góc trên phải: Chat, Cờ, Khu, Tab, Boss, Sự kiện, Phúc
    /// lợi, Nhân vật, Trò chơi — xếp thành <b>hai hàng</b>, cộng nút ba gạch
    /// đứng riêng một cột ngoài cùng.
    /// </summary>
    /// <remarks>
    /// <para><b>Một chỗ xếp cho tất cả.</b> Trước đây ba nút Cờ / Khu / Tab do
    /// <c>TabControll</c> tự xếp, ô Chat do <c>GameScr</c> xếp, còn năm mục kia
    /// nằm trong một popup riêng. Ba chỗ xếp ba kiểu nên hàng nút so le nhau, và
    /// mỗi lần đổi lưới là phải sửa cả ba. Nay mọi nút hỏi đúng một hàm
    /// <see cref="oNut"/>.</para>
    ///
    /// <para><b>Xếp từ PHẢI sang trái.</b> Nút số 0 (ô Chat) nằm sát nút ba
    /// gạch, rồi lùi dần sang trái; hết một hàng thì xuống hàng dưới, cũng bắt
    /// đầu từ mép phải. Nhờ vậy cụm nút luôn dính vào góc phải màn hình dù bớt
    /// hay thêm nút.</para>
    ///
    /// <para><b>Thu mở trượt mượt.</b> <see cref="mo"/> chạy từ 0 tới 1 theo lối
    /// giảm dần (mỗi khung hình đi một phần quãng còn lại) nên nút trôi ra nhanh
    /// lúc đầu rồi chậm dần khi tới chỗ — không giật cục như cộng đều. Lúc thu,
    /// mọi nút trượt <b>về đúng chỗ nút ba gạch</b> rồi nấp sau nó.</para>
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

        /// <summary>Số nút mỗi hàng. Chín nút chia hai hàng: 5 trên, 4 dưới.</summary>
        private const int SO_MOI_HANG = 5;

        /// <summary>Khe giữa hai nút, cả ngang lẫn dọc.</summary>
        private const int KHE = 3;

        /// <summary>Mép phải của cả cụm, cách mép màn hình bấy nhiêu.</summary>
        private const int LE_PHAI = 6;

        /// <summary>Đỉnh hàng trên.</summary>
        private const int Y_DAU = 6;

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

        /// <summary>Cụm đã ra đủ để bấm được chưa.</summary>
        public static bool bamDuoc()
        {
            return mo > 0.55f;
        }

        /// <summary>Còn thấy được thì còn phải vẽ.</summary>
        public static bool conThay()
        {
            return mo > 0.02f;
        }

        /// <summary>Vùng {x, y, rộng, cao} của nút ba gạch — riêng một cột ngoài cùng.</summary>
        public static int[] oNutMenu()
        {
            return new int[] { GameCanvas.w - LE_PHAI - RONG, Y_DAU, RONG, CAO };
        }

        /// <summary>Vùng {x, y, rộng, cao} của nút thứ <paramref name="i"/>.</summary>
        /// <remarks>Đã tính cả phần trượt: lúc thu, nút chồng lên nút ba gạch.</remarks>
        public static int[] oNut(int i)
        {
            int cot = i % SO_MOI_HANG;
            int hang = i / SO_MOI_HANG;
            // Mep phai cua hai hang: ngay ben trai nut ba gach.
            int xPhai = GameCanvas.w - LE_PHAI - RONG - KHE;
            int xRaHan = xPhai - RONG - cot * (RONG + KHE);
            int yRaHan = Y_DAU + hang * (CAO + KHE);

            int[] oMenu = oNutMenu();
            int x = (int) (oMenu[0] + (xRaHan - oMenu[0]) * mo);
            int y = (int) (oMenu[1] + (yRaHan - oMenu[1]) * mo);
            return new int[] { x, y, RONG, CAO };
        }

        /// <summary>Mép dưới của cả cụm khi đang mở — chỗ khác xếp tiếp từ đây.</summary>
        public static int mepDuoi()
        {
            int soHang = (SO_NUT + SO_MOI_HANG - 1) / SO_MOI_HANG;
            return Y_DAU + soHang * (CAO + KHE);
        }

        /// <summary>Mép trái của cả cụm — bảng mục tiêu dừng trước chỗ này.</summary>
        public static int mepTrai()
        {
            int cotCuoi = (SO_NUT < SO_MOI_HANG ? SO_NUT : SO_MOI_HANG) - 1;
            int xPhai = GameCanvas.w - LE_PHAI - RONG - KHE;
            return xPhai - RONG - cotCuoi * (RONG + KHE);
        }
    }
}
