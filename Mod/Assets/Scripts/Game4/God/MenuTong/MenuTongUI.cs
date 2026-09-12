// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game4.God
{
    /// <summary>
    /// Popup menu tổng — một chỗ vào cho mọi màn phụ.
    /// </summary>
    /// <remarks>
    /// <para><b>Vì sao có màn này.</b> Trước đây mỗi màn phụ có một nút riêng
    /// nằm thành hàng trên HUD (bánh răng, Sự kiện, Phúc lợi, Boss). Cứ thêm
    /// một màn là hàng nút dài ra, và hàng nút đó nằm <i>đè lên</i> khung nhìn
    /// — thêm vài nút nữa là che hết nửa trên màn hình.</para>
    ///
    /// <para>Nay HUD chỉ còn <b>một</b> nút ba gạch; mọi màn phụ nằm trong đây.
    /// Thêm màn mới = thêm một dòng vào <see cref="dungDanhSach"/>, không phải
    /// đi tìm chỗ trống trên HUD nữa.</para>
    ///
    /// <para>Bảng màu và <c>veKhungBo</c> lấy giống Sự kiện / Phúc lợi / Boss để
    /// bốn màn trông cùng một bộ, không phải bốn phong cách rời.</para>
    /// </remarks>
    public class MenuTongUI
    {
        private static MenuTongUI instance;

        public static MenuTongUI getInstance()
        {
            return instance ?? (instance = new MenuTongUI());
        }

        public bool dangMo;

        // ------------------------------------------------------------------
        //  Màu — giống Sự kiện / Phúc lợi / Boss
        // ------------------------------------------------------------------
        private static int rgb(int r, int g, int b)
        {
            return (r << 16) | (g << 8) | b;
        }

        private static readonly int MAU_NEN = rgb(0xFC, 0xE3, 0xC0);
        private static readonly int MAU_TIEU_DE = rgb(0xF0, 0xA1, 0x64);
        private static readonly int MAU_THE = rgb(0xFD, 0xF0, 0xDC);
        private static readonly int MAU_VIEN = rgb(0xA8, 0x6E, 0x3C);
        private static readonly int MAU_CHON = rgb(0xF0, 0xA1, 0x64);

        private const int BO_GOC = 6;

        /// <summary>Khung bo góc có viền: hai lần fillRect, ngoài viền trong nền.</summary>
        /// <remarks>
        /// <c>drawRect</c> của engine <b>không</b> nhận bán kính bo, nên vẽ viền
        /// bằng nó sẽ ra một khung vuông quanh phần nền đã bo — hở bốn góc.
        /// </remarks>
        private static void veKhungBo(mGraphics g, int x, int y, int w, int h,
                int mauNen, float aNen, int mauVien, float aVien, int day)
        {
            g.setColor(mauVien, aVien);
            g.fillRect(x, y, w, h, BO_GOC);
            g.setColor(mauNen, aNen);
            g.fillRect(x + day, y + day, w - day * 2, h - day * 2, BO_GOC - 1);
        }

        // ------------------------------------------------------------------
        //  Một mục trong menu
        // ------------------------------------------------------------------
        private class Muc
        {
            /// <summary>Id icon trong kho icon máy chủ (data/icon/xN/&lt;id&gt;.png).</summary>
            public int icon;
            public string chu;
            /// <summary>Việc chạy khi bấm.</summary>
            public System.Action lam;

            public Muc(int icon, string chu, System.Action lam)
            {
                this.icon = icon;
                this.chu = chu;
                this.lam = lam;
            }
        }

        private readonly List<Muc> ds = new List<Muc>();

        /// <summary>
        /// Dựng danh sách mục. Thêm màn phụ mới thì thêm một dòng ở đây.
        /// </summary>
        /// <remarks>
        /// Dựng lại mỗi lần mở chứ không dựng một lần lúc khởi tạo: mục "Menu cũ"
        /// gọi vào <c>GameScr</c>, mà lúc lớp này được tạo thì màn game có thể
        /// chưa dựng xong.
        /// </remarks>
        private void dungDanhSach()
        {
            ds.Clear();
            // Boss mở được ngay vì dữ liệu đã có sẵn trong máy.
            ds.Add(new Muc(27016, "Boss", () => BossUI.getInstance().moCho()));

            // Sự kiện và Phúc lợi thì KHÁC: chúng không có hàm mở. Cách mở là
            // xin dữ liệu từ máy chủ, và chính lúc gói trả lời về thì
            // nhanDuLieu() mới bật màn lên. Nên ở đây chỉ gửi lời xin.
            ds.Add(new Muc(12769, "Sự kiện", () => Service.gI().suKienXin()));
            ds.Add(new Muc(27135, "Phúc lợi", () => Service.gI().phucLoiXin()));

            ds.Add(new Muc(27017, "Nhân vật", () => TuiUI.getInstance().moRa()));
            // 4028 la ma ICON cua thoi vang, khong phai 457 — 457 la ma VAT PHAM.
            // Hai bang khac nhau: drawSmallImage tra cuu theo ma icon.
            ds.Add(new Muc(4028, "Trò chơi", () => TroChoiUI.getInstance().moRa()));
        }

        public void moRa()
        {
            dungDanhSach();
            chon = -1;
            dangMo = true;
        }

        public void dong()
        {
            dangMo = false;
        }

        // ------------------------------------------------------------------
        //  Bố cục
        // ------------------------------------------------------------------
        private const int CAO_TIEU_DE = 22;
        private const int LE = 8;
        /// <summary>Ô mỗi mục: vuông, icon giữa, chữ dưới đáy.</summary>
        private const int CANH_O = 52;
        private const int KHE = 6;
        /// <summary>Số ô một hàng. Ba ô vừa khít bề ngang màn hình nhỏ nhất.</summary>
        private const int O_MOI_HANG = 3;

        private int x0;
        private int y0;
        private int rong;
        private int cao;
        private int chon = -1;

        private void tinhBoCuc()
        {
            int soHang = (ds.Count + O_MOI_HANG - 1) / O_MOI_HANG;
            if (soHang < 1)
            {
                soHang = 1;
            }
            rong = LE * 2 + O_MOI_HANG * CANH_O + (O_MOI_HANG - 1) * KHE;
            cao = CAO_TIEU_DE + LE * 2 + soHang * CANH_O + (soHang - 1) * KHE;
            x0 = (GameCanvas.w - rong) / 2;
            y0 = (GameCanvas.h - cao) / 2;
        }

        /// <summary>Góc trên trái của ô thứ <paramref name="i"/>.</summary>
        private void viTriO(int i, out int x, out int y)
        {
            int cot = i % O_MOI_HANG;
            int hang = i / O_MOI_HANG;
            x = x0 + LE + cot * (CANH_O + KHE);
            y = y0 + CAO_TIEU_DE + LE + hang * (CANH_O + KHE);
        }

        // ------------------------------------------------------------------
        //  Vẽ
        // ------------------------------------------------------------------
        public void ve(mGraphics g)
        {
            if (!dangMo)
            {
                return;
            }
            tinhBoCuc();

            // Làm tối cả màn phía sau: mắt bám ngay vào popup, và nó cũng nói
            // rõ rằng phần dưới đang không nhận chạm.
            g.setColor(0, 0.45f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            veKhungBo(g, x0, y0, rong, cao, MAU_NEN, 0.97f, MAU_VIEN, 1f, 1);

            g.setColor(MAU_TIEU_DE, 1f);
            g.fillRect(x0 + 1, y0 + 1, rong - 2, CAO_TIEU_DE, BO_GOC);
            mFont.tahoma_7b_white.drawString(g, "MENU", x0 + rong / 2,
                    y0 + 6, mFont.CENTER);

            veNutDong(g, x0 + rong - 20, y0 + 4);

            for (int i = 0; i < ds.Count; i++)
            {
                int x, y;
                viTriO(i, out x, out y);
                bool sang = (i == chon);
                veKhungBo(g, x, y, CANH_O, CANH_O,
                        sang ? MAU_CHON : MAU_THE, sang ? 1f : 0.95f,
                        MAU_VIEN, sang ? 0.9f : 0.5f, 1);

                veIconVaChu(g, ds[i].icon, ds[i].chu,
                        x + CANH_O / 2, y, CANH_O, sang);
                // Cham bao tren muc "Nhan vat" khi co nguoi xin vao bang.
                //
                // Cho xu ly nam sau hai lop nua (Nhan vat -> the Bang hoi) nen
                // dau hieu phai dat o dung muc dan toi do, khong thi nguoi choi
                // mo menu ra van khong biet di dau.
                if (ds[i].chu == "Nhân vật" && GameScr.isNewClanMessage
                        && GameCanvas.gameTick % 8 < 5)
                {
                    g.setColor(0xFF4A3C, 1f);
                    g.fillRect(x + CANH_O - 10, y + 4, 6, 6, 3);
                }
            }
        }

        /// <summary>Chiều cao thẻ nền của dòng chữ.</summary>
        private const int CAO_THE_CHU = 13;

        /// <summary>
        /// Icon ở giữa ô, dòng chữ nằm trên một thẻ nền ở đáy ô.
        /// </summary>
        /// <remarks>
        /// <para><b>Chân icon đè lên thẻ chữ khoảng một phần ba.</b> Đặt chữ hẳn
        /// dưới icon thì ô phải cao thêm, mà ô cao thêm thì ba ô một hàng không
        /// còn vừa bề ngang màn hình nhỏ. Cho chồng một phần vừa tiết kiệm chỗ
        /// vừa buộc icon và tên nó thành một khối, mắt không phải ghép.</para>
        ///
        /// <para><b>Chữ có thẻ nền bo góc + viền</b> chứ không vẽ trần: nền kem
        /// của bảng và màu icon rất gần nhau, chữ vẽ trần lên đó thì chìm. Thẻ
        /// nền tối làm chữ nổi bật bất kể icon phía sau màu gì.</para>
        /// </remarks>
        private void veIconVaChu(mGraphics g, int icon, string chu,
                int xGiua, int yO, int canhO, bool sang)
        {
            // Đáy thẻ chữ cách đáy ô 3px; icon canh giữa phần còn lại phía trên.
            int yThe = yO + canhO - CAO_THE_CHU - 3;
            int yIcon = yO + (yThe - yO) / 2 + 2;

            SmallImage.drawSmallImage(g, icon, xGiua, yIcon,
                    0, mGraphics.VCENTER | mGraphics.HCENTER);

            int rongThe = canhO - 8;
            int xThe = xGiua - rongThe / 2;
            g.setColor(0x2A211A, sang ? 0.92f : 0.78f);
            g.fillRect(xThe, yThe, rongThe, CAO_THE_CHU, 4);
            g.setColor(sang ? 0xFFD9A8 : 0x8A6A46, 0.9f);
            g.fillRect(xThe, yThe, rongThe, 1);

            mFont.tahoma_7b_white.drawString(g, chu, xGiua, yThe + 2,
                    mFont.CENTER);
        }

        /// <summary>
        /// Nút đóng: ô vuông đỏ bo góc, dấu X vẽ tay ở chính giữa.
        /// </summary>
        /// <remarks>
        /// Vẽ X bằng tay thay vì <c>drawString("X")</c> vì <c>mFont.getWidth</c>
        /// trả bề rộng <b>ô chữ</b> chứ không phải bề rộng nét mực, nên canh giữa
        /// theo nó là luôn lệch một hai điểm ảnh.
        /// </remarks>
        private void veNutDong(mGraphics g, int x, int y)
        {
            const int canh = 16;
            veKhungBo(g, x, y, canh, canh, rgb(0xD8, 0x4A, 0x3A), 1f,
                    MAU_VIEN, 0.9f, 1);
            g.setColor(0xFFFFFF, 1f);

            // Can giua theo BEN RONG THAT cua dau X, khong dem tu tam nut.
            //
            // Moi dau la mot diem 2x2 chu khong phai 1px, nen bay diem buoc
            // 1px trai rong 6 + 2 = 8px. Dem tu tam kieu `c - 3 + i` thi diem
            // dau nam o x+5 va diem cuoi het o x+13 — lech 1,5px sang phai va
            // xuong duoi trong o rong 16.
            //
            // Viet ben rong ra thanh cong thuc de sau doi so diem hay co diem
            // thi le tu dong theo.
            const int soDiem = 7;
            const int coDiem = 2;
            const int benX = soDiem - 1 + coDiem;
            int xX = x + (canh - benX) / 2;
            int yX = y + (canh - benX) / 2;
            for (int i = 0; i < soDiem; i++)
            {
                g.fillRect(xX + i, yX + i, coDiem, coDiem);
                g.fillRect(xX + benX - coDiem - i, yX + i, coDiem, coDiem);
            }
        }

        // ------------------------------------------------------------------
        //  Chạm
        // ------------------------------------------------------------------
        /// <summary>Xử lý chạm. Trả <c>true</c> nghĩa là đã nuốt lượt chạm này.</summary>
        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            tinhBoCuc();

            if (!GameCanvas.isPointerJustRelease)
            {
                // Vẫn nuốt chạm khi đang giữ ngón: nếu không, ngón đè lên popup
                // sẽ điều khiển nhân vật phía dưới.
                return true;
            }

            if (cham(x0 + rong - 20, y0 + 4, 16, 16))
            {
                dong();
                return true;
            }

            for (int i = 0; i < ds.Count; i++)
            {
                int x, y;
                viTriO(i, out x, out y);
                if (cham(x, y, CANH_O, CANH_O))
                {
                    chon = i;
                    // Đóng TRƯỚC khi mở màn kia: hai popup cùng mở thì cái này
                    // che cái kia, và cả hai đều nuốt chạm.
                    dong();
                    if (ds[i].lam != null)
                    {
                        ds[i].lam();
                    }
                    return true;
                }
            }

            // Bấm ra ngoài khung KHÔNG đóng bảng — chỉ dấu X mới đóng.
            //
            // Trước đây bấm hụt một cái ra mép là bảng biến mất, phải mở lại từ
            // đầu. Vẫn nuốt cú chạm: không nuốt thì nó rơi xuống bản đồ phía
            // dưới và nhân vật chạy tới chỗ vừa bấm.
            GameCanvas.clearAllPointerEvent();
            return true;
        }

        private static bool cham(int x, int y, int w, int h)
        {
            if (GameCanvas.isPointerHoldIn(x, y, w, h))
            {
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            return false;
        }
    }
}
