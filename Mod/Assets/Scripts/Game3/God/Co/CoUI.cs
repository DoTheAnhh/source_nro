// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game3.God
{
    /// <summary>
    /// Bảng chọn cờ.
    /// </summary>
    /// <remarks>
    /// <para><b>Vì sao viết mới thay vì dùng bảng cờ sẵn có.</b> Bảng cũ là
    /// <c>Panel</c> đặt <c>type = TYPE_FLAG</c> — cùng một đối tượng dùng chung
    /// cho hơn hai mươi loại nội dung. Bấm ra ngoài thì nó không đóng mà quay
    /// về loại nội dung <i>trước đó</i>, nên thoát bảng cờ lại hiện ra menu
    /// "Chức Năng". Chọn cờ ở đó cũng phải qua hai bước: bấm dòng cờ, rồi bấm
    /// tiếp "Đổi cờ" trong một menu con.</para>
    ///
    /// <para>Bảng này bấm một lần là xong, và chỉ dấu X mới đóng.</para>
    ///
    /// <para><b>Vì sao xếp hàng ngang chứ không phải lưới ô vuông như menu
    /// tổng.</b> Tên cờ dài tới "Cờ xanh dương" — mười ba ký tự. Nhét vào một ô
    /// vuông 52px thì phải cắt còn "Cờ xanh d…". Xếp hàng ngang thì icon nằm
    /// trái, tên có trọn bề ngang còn lại.</para>
    ///
    /// <para>Bảng màu và <c>veKhungBo</c> lấy giống Menu tổng / Sự kiện / Phúc
    /// lợi / Boss để các màn trông cùng một bộ.</para>
    /// </remarks>
    public class CoUI
    {
        private static CoUI instance;

        public static CoUI getInstance()
        {
            return instance ?? (instance = new CoUI());
        }

        public bool dangMo;

        // ------------------------------------------------------------------
        //  Màu — giống Menu tổng / Sự kiện / Phúc lợi / Boss
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
        /// <summary>Viền của lá cờ đang mang.</summary>
        private static readonly int MAU_DANG_MANG = rgb(0x4C, 0x9A, 0x3F);

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
        //  Dữ liệu
        // ------------------------------------------------------------------
        private class Muc
        {
            public int icon;
            public string ten;

            public Muc(int icon, string ten)
            {
                this.icon = icon;
                this.ten = ten;
            }
        }

        private readonly List<Muc> ds = new List<Muc>();
        private int chon = -1;

        /// <summary>
        /// Nhận danh sách cờ từ máy chủ rồi mở bảng.
        /// </summary>
        /// <remarks>
        /// <para>Gọi từ <c>Controller2</c> khi gói -103 loại 0 về. Cờ <b>không có
        /// hàm mở</b> giống Sự kiện / Phúc lợi: cách mở là xin dữ liệu, và chính
        /// lúc gói trả lời về thì hàm này mới bật bảng lên.</para>
        ///
        /// <para><b>Thứ tự trong danh sách chính là mã gửi đi.</b> Chọn cờ là
        /// <c>Service.getFlag(1, chỉ_số)</c> — máy chủ đọc số đó làm chỉ số
        /// trong bảng cờ của nó. Nên không được sắp xếp lại hay lọc bớt danh
        /// sách này; làm vậy là người chơi bấm một lá lại nhận một lá khác.</para>
        ///
        /// <para>Phải gọi <c>InfoDlg.hide()</c>: lệnh Đổi cờ bật hộp "vui lòng
        /// chờ" trước khi gửi lời xin, mà bảng cũ dựa vào <c>Panel.show()</c> để
        /// tắt hộp đó. Bảng này không đi qua <c>Panel</c> nên phải tự tắt, nếu
        /// không hộp chờ nằm lại trên màn hình.</para>
        /// </remarks>
        public void nhanDuLieu(MyVector dsCo)
        {
            ds.Clear();
            if (dsCo != null)
            {
                for (int i = 0; i < dsCo.size(); i++)
                {
                    Item it = (Item)dsCo.elementAt(i);
                    if (it == null || it.template == null)
                    {
                        // Giu cho trong: chi so phai khop voi bang co cua may chu.
                        ds.Add(new Muc(-1, "?"));
                        continue;
                    }
                    ds.Add(new Muc(it.template.iconID, it.template.name));
                }
            }
            chon = -1;
            InfoDlg.hide();
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
        private const int CAO_HANG = 28;
        private const int KHE = 6;
        private const int KHE_DOC = 3;
        private const int COT = 2;
        /// <summary>Bề ngang mong muốn của một hàng.</summary>
        private const int RONG_HANG_CHUAN = 112;

        private int x0;
        private int y0;
        private int rong;
        private int cao;
        private int rongHang;

        private void tinhBoCuc()
        {
            int soHang = (ds.Count + COT - 1) / COT;
            if (soHang < 1)
            {
                soHang = 1;
            }

            // Co lai khi man hinh hep, khong de bang tran ra ngoai mep.
            rongHang = RONG_HANG_CHUAN;
            int toiDa = (GameCanvas.w - 20 - LE * 2 - KHE) / COT;
            if (rongHang > toiDa)
            {
                rongHang = toiDa;
            }
            if (rongHang < 60)
            {
                rongHang = 60;
            }

            rong = LE * 2 + COT * rongHang + (COT - 1) * KHE;
            cao = CAO_TIEU_DE + LE * 2 + soHang * CAO_HANG
                    + (soHang - 1) * KHE_DOC;
            x0 = (GameCanvas.w - rong) / 2;
            y0 = (GameCanvas.h - cao) / 2;
        }

        /// <summary>Góc trên trái của hàng thứ <paramref name="i"/>.</summary>
        private void viTriHang(int i, out int x, out int y)
        {
            int cot = i % COT;
            int hang = i / COT;
            x = x0 + LE + cot * (rongHang + KHE);
            y = y0 + CAO_TIEU_DE + LE + hang * (CAO_HANG + KHE_DOC);
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

            // Làm tối cả màn phía sau: mắt bám ngay vào bảng, và nó cũng nói rõ
            // rằng phần dưới đang không nhận chạm.
            g.setColor(0, 0.45f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);

            veKhungBo(g, x0, y0, rong, cao, MAU_NEN, 0.97f, MAU_VIEN, 1f, 1);

            g.setColor(MAU_TIEU_DE, 1f);
            g.fillRect(x0 + 1, y0 + 1, rong - 2, CAO_TIEU_DE, BO_GOC);
            mFont.tahoma_7b_white.drawString(g, "ĐỔI CỜ", x0 + rong / 2,
                    y0 + 6, mFont.CENTER);

            veNutDong(g, x0 + rong - 20, y0 + 4);

            int dangMang = coDangMang();
            for (int i = 0; i < ds.Count; i++)
            {
                int x, y;
                viTriHang(i, out x, out y);
                bool sang = (i == chon);
                bool mang = (i == dangMang);
                veKhungBo(g, x, y, rongHang, CAO_HANG,
                        sang ? MAU_CHON : MAU_THE, sang ? 1f : 0.95f,
                        mang ? MAU_DANG_MANG : MAU_VIEN,
                        (sang || mang) ? 0.95f : 0.5f, 1);

                if (ds[i].icon >= 0)
                {
                    SmallImage.drawSmallImage(g, ds[i].icon, x + 15,
                            y + CAO_HANG / 2, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                }

                // Chu canh giua theo chieu doc cua hang, khong dat cung mot so:
                // hang co the doi chieu cao ma chu van phai nam giua.
                int xChu = x + 28;
                int caoChu = mFont.tahoma_7b_dark.getHeight();
                mFont mf = sang ? mFont.tahoma_7b_white : mFont.tahoma_7b_dark;
                mf.drawString(g, catVua(ds[i].ten, rongHang - 32, mf), xChu,
                        y + (CAO_HANG - caoChu) / 2, mFont.LEFT);
            }
        }

        /// <summary>Chỉ số lá cờ đang mang, hoặc -1.</summary>
        /// <remarks>
        /// <c>cFlag</c> chính là chỉ số trong bảng cờ của máy chủ — cùng con số
        /// mà <c>Service.getFlag(1, ...)</c> gửi đi — nên so thẳng với vị trí
        /// trong danh sách được.
        /// </remarks>
        private int coDangMang()
        {
            Char me = Char.myCharz();
            return me == null ? -1 : me.cFlag;
        }

        /// <summary>Cắt chuỗi cho vừa <paramref name="rongToiDa"/> pixel.</summary>
        /// <remarks>
        /// Cắt theo bề rộng đo được chứ không theo số ký tự: chữ Việt có dấu
        /// rộng hẹp khác nhau nên đếm ký tự lúc thừa chỗ lúc vẫn tràn.
        /// </remarks>
        private static string catVua(string s, int rongToiDa, mFont mf)
        {
            if (s == null)
            {
                return "";
            }
            if (mf.getWidth(s) <= rongToiDa)
            {
                return s;
            }
            for (int n = s.Length - 1; n > 0; n--)
            {
                string thu = s.Substring(0, n) + "…";
                if (mf.getWidth(thu) <= rongToiDa)
                {
                    return thu;
                }
            }
            return "…";
        }

        private void veNutDong(mGraphics g, int x, int y)
        {
            const int canh = 16;
            veKhungBo(g, x, y, canh, canh, rgb(0xD8, 0x4A, 0x3A), 1f,
                    MAU_VIEN, 0.9f, 1);
            g.setColor(0xFFFFFF, 1f);

            // Can giua theo BE RONG THAT cua dau X, khong dem tu tam nut: moi
            // dau la mot diem 2x2 nen bay diem buoc 1px trai rong 6 + 2 = 8px.
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
        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            tinhBoCuc();

            if (!GameCanvas.isPointerJustRelease)
            {
                // Vẫn nuốt chạm khi đang giữ ngón: nếu không, ngón đè lên bảng
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
                viTriHang(i, out x, out y);
                if (cham(x, y, rongHang, CAO_HANG))
                {
                    chon = i;
                    // Doi mot lan bam la xong. Bang cu bat bam dong co roi bam
                    // tiep "Doi co" trong mot menu con — hai buoc cho mot viec.
                    Service.gI().getFlag(1, (sbyte)i);
                    dong();
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
