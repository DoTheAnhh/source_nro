using System.Collections.Generic;

namespace Game5.God
{
    /// <summary>
    /// Thẻ <b>Thú cưng</b> của bảng nhân vật: xem trước con thú, nuôi cho lên
    /// cấp, và xem ba chiêu của nó.
    /// </summary>
    /// <remarks>
    /// <para>Nằm riêng một tệp vì thẻ này dài gần bằng cả thẻ Đệ tử. Vẫn là
    /// <c>TuiUI</c> (lớp <c>partial</c>) nên dùng chung được mọi hàm vẽ khung,
    /// dải thẻ con, ô vật phẩm… của bảng.</para>
    ///
    /// <para><b>Cấp và kinh nghiệm không có gói tin riêng.</b> Máy chủ gắn hai
    /// con số ấy làm chỉ số phụ của chính món thú cưng, nên chúng về client
    /// theo gói hành trang như mọi chỉ số khác; ở đây chỉ việc đọc ra theo id
    /// chỉ số mà máy chủ báo trong gói 111.</para>
    /// </remarks>
    public partial class TuiUI
    {
        // ==================================================================
        //  Du lieu may chu gui xuong (goi 111)
        // ==================================================================

        /// <summary>Một chiêu của một loại thú.</summary>
        public sealed class ChieuThuCung
        {
            public int itemId;
            public int thuTu;
            public string ten = string.Empty;
            public string moTa = string.Empty;
            public int loai;
            public int thamSo;
            public int phanTram;
            public int giay;
            public int tiLe;
            public int hoiChieu;
            public int capMo;
        }

        /// <summary>Tên từng loại hiệu ứng — khớp <c>ThuCungDAO.TEN_LOAI</c>.</summary>
        private static readonly string[] TEN_LOAI_CHIEU = {
            "Cộng #% sức đánh",
            "Cộng #% sát thương chiêu",
            "Cộng #% tỉ lệ chí mạng",
            "Hồi #% HP tối đa",
            "Giảm #% sát thương phải chịu"
        };

        private static readonly Dictionary<int, List<ChieuThuCung>> chieuTheoThu
                = new Dictionary<int, List<ChieuThuCung>>();

        /// <summary>Đồ ăn: {id vật phẩm, kinh nghiệm}, xếp từ ít exp tới nhiều.</summary>
        private static readonly List<int[]> doAnThuCung = new List<int[]>();

        /// <summary>Hình từng loại thú: id -> {mũ, thân, chân}.</summary>
        private static readonly Dictionary<int, short[]> hinhThuCung
                = new Dictionary<int, short[]>();

        private static int tcCapToiDa = 50;
        private static int tcExpCapMot = 100;

        /// <summary>Id chỉ số phụ giữ cấp và kinh nghiệm, máy chủ báo xuống.</summary>
        private static int tcIdChiSoCap = -1;
        private static int tcIdChiSoExp = -1;

        /// <summary>Đã nhận bảng chưa — chưa thì thẻ hiện "đang tải".</summary>
        private static bool tcDaCoBang;

        /// <summary>Đã hỏi bảng rồi, khỏi hỏi lại mỗi lần mở thẻ.</summary>
        private static bool tcDaXinBang;

        /// <summary>
        /// Đọc gói 111 việc 0: cấu hình, bảng chiêu, bảng đồ ăn, bảng hình thú.
        /// </summary>
        /// <remarks>
        /// Thứ tự đọc là giao kèo với <c>ThuCungService.guiBang</c>. Lệch một
        /// trường là lệch cả gói, nên hai bên phải sửa cùng lúc.
        /// </remarks>
        public static void nhanBangThuCung(Message msg)
        {
            try
            {
                tcCapToiDa = msg.reader().readShort();
                tcExpCapMot = msg.reader().readInt();
                tcIdChiSoCap = msg.reader().readShort();
                tcIdChiSoExp = msg.reader().readShort();

                chieuTheoThu.Clear();
                int soChieu = msg.reader().readShort();
                for (int i = 0; i < soChieu; i++)
                {
                    ChieuThuCung c = new ChieuThuCung();
                    c.itemId = msg.reader().readShort();
                    c.thuTu = msg.reader().readByte();
                    c.ten = msg.reader().readUTF();
                    c.moTa = msg.reader().readUTF();
                    c.loai = msg.reader().readByte();
                    c.thamSo = msg.reader().readShort();
                    c.phanTram = msg.reader().readShort();
                    c.giay = msg.reader().readShort();
                    c.tiLe = msg.reader().readShort();
                    c.hoiChieu = msg.reader().readShort();
                    c.capMo = msg.reader().readShort();
                    if (!chieuTheoThu.ContainsKey(c.itemId))
                    {
                        chieuTheoThu[c.itemId] = new List<ChieuThuCung>();
                    }
                    chieuTheoThu[c.itemId].Add(c);
                }

                doAnThuCung.Clear();
                int soAn = msg.reader().readShort();
                for (int i = 0; i < soAn; i++)
                {
                    int id = msg.reader().readShort();
                    int exp = msg.reader().readInt();
                    doAnThuCung.Add(new int[] { id, exp });
                }
                // Trai sang phai: it exp truoc, nhieu exp sau.
                doAnThuCung.Sort((a, b) => a[1].CompareTo(b[1]));

                hinhThuCung.Clear();
                int soThu = msg.reader().readShort();
                for (int i = 0; i < soThu; i++)
                {
                    int id = msg.reader().readShort();
                    short dau = msg.reader().readShort();
                    short than = msg.reader().readShort();
                    short chan = msg.reader().readShort();
                    hinhThuCung[id] = new short[] { dau, than, chan };
                }
                tcDaCoBang = true;
            }
            catch (System.Exception e)
            {
                Cout.println("Loi doc bang thu cung: " + e);
            }
        }

        // ==================================================================
        //  Trang thai cua the
        // ==================================================================

        private static readonly string[] TEN_THE_THU = { "Chỉ số", "Kỹ năng" };

        private const int THU_CHI_SO = 0;
        private const int THU_KY_NANG = 1;

        private int theThuChon = THU_CHI_SO;

        /// <summary>Con đang xem: -1 là con đang ra trận, còn lại là ô hành trang.</summary>
        private int oThuXem = -1;

        /// <summary>Đang mở hộp chọn đồ ăn.</summary>
        private bool hienHopDoAn;

        /// <summary>Ô đồ ăn đang bị giữ ngón, và lúc cho ăn gần nhất.</summary>
        private int oDoAnDangGiu = -1;
        private long mocGiuDoAn;
        private long mocAnGanNhat;

        /// <summary>Nhân vật dựng tạm để vẽ con thú đang xem.</summary>
        private Char thuVe;
        private int thuVeId = -1;

        /// <summary>Giữ ngón bao lâu thì bắt đầu cho ăn liên tiếp.</summary>
        private const long CHO_TRUOC_KHI_LAP = 400L;

        /// <summary>Cho ăn liên tiếp thì mỗi lần cách nhau bao lâu.</summary>
        private const long NHIP_AN_LAP = 140L;

        /// <summary>
        /// Hỏi máy chủ dữ liệu cho hai thẻ Đệ tử và Thú cưng.
        /// </summary>
        /// <remarks>
        /// <para>Gọi lúc <b>mở bảng</b>, không đợi tới lúc bấm vào thẻ: hai thẻ
        /// ấy sống nhờ dữ liệu máy chủ (gói -107 cho đệ, gói 111 cho bảng chiêu
        /// và bảng đồ ăn), hỏi muộn thì thẻ mở ra trống trơn mất một vòng gói
        /// tin.</para>
        ///
        /// <para>Bảng chiêu chỉ hỏi MỘT lần cả phiên — nó không đổi trong lúc
        /// chơi. Thông tin đệ thì hỏi mỗi lần mở bảng vì chỉ số đệ đổi liên tục.</para>
        /// </remarks>
        private void xinDuLieuDeTuVaThuCung()
        {
            Service.gI().petInfo();
            daXinDe = true;
            if (!tcDaXinBang)
            {
                Service.gI().thuCungXinBang();
                tcDaXinBang = true;
            }
        }

        // ==================================================================
        //  Danh sach thu dang so huu
        // ==================================================================

        /// <summary>
        /// Mọi con thú đang có: con ra trận trước, rồi tới các con trong hành
        /// trang.
        /// </summary>
        /// <remarks>
        /// Mỗi phần tử là {ô, có phải đang ra trận}: ô là chỉ số trong
        /// <c>arrItemBag</c>, hoặc -1 khi con ấy đang mặc.
        /// </remarks>
        private List<int[]> dsThuCung()
        {
            List<int[]> ra = new List<int[]>();
            Item mac = thuDangRaTran();
            if (mac != null)
            {
                ra.Add(new int[] { -1, 1 });
            }
            Item[] tui = Char.myCharz().arrItemBag;
            if (tui != null)
            {
                for (int i = 0; i < tui.Length; i++)
                {
                    if (laThuCung(tui[i]))
                    {
                        ra.Add(new int[] { i, 0 });
                    }
                }
            }
            return ra;
        }

        private static bool laThuCung(Item it)
        {
            return it != null && it.template != null && it.template.type == 21;
        }

        /// <summary>Con đang mặc ở ô Pet (ô 7), hoặc null.</summary>
        private static Item thuDangRaTran()
        {
            Item[] mac = Char.myCharz().arrItemBody;
            if (mac == null || mac.Length <= 7)
            {
                return null;
            }
            return laThuCung(mac[7]) ? mac[7] : null;
        }

        /// <summary>Con thú đang xem trong thẻ.</summary>
        private Item thuDangXem()
        {
            if (oThuXem < 0)
            {
                Item mac = thuDangRaTran();
                if (mac != null)
                {
                    return mac;
                }
                List<int[]> ds = dsThuCung();
                return ds.Count > 0 ? thuTaiO(ds[0][0]) : null;
            }
            return thuTaiO(oThuXem);
        }

        private static Item thuTaiO(int o)
        {
            if (o < 0)
            {
                return thuDangRaTran();
            }
            Item[] tui = Char.myCharz().arrItemBag;
            return (tui != null && o < tui.Length && laThuCung(tui[o])) ? tui[o] : null;
        }

        /// <summary>Con đang xem có đúng là con đang ra trận không.</summary>
        private bool thuXemDangRaTran()
        {
            return oThuXem < 0 && thuDangRaTran() != null;
        }

        // ==================================================================
        //  Cap va kinh nghiem doc tu chi so phu cua mon
        // ==================================================================

        private static int chiSoCua(Item it, int idChiSo)
        {
            if (it == null || it.itemOption == null || idChiSo < 0)
            {
                return 0;
            }
            for (int i = 0; i < it.itemOption.Length; i++)
            {
                ItemOption io = it.itemOption[i];
                if (io != null && io.optionTemplate != null
                        && io.optionTemplate.id == idChiSo)
                {
                    return io.param;
                }
            }
            return 0;
        }

        private static int capThu(Item it)
        {
            int c = chiSoCua(it, tcIdChiSoCap);
            return c < 1 ? 1 : c;
        }

        private static int expThu(Item it)
        {
            return chiSoCua(it, tcIdChiSoExp);
        }

        /// <summary>Kinh nghiệm cần để lên cấp kế tiếp.</summary>
        private static int expCanChoCap(int cap)
        {
            int m = tcExpCapMot < 1 ? 1 : tcExpCapMot;
            return m * (cap < 1 ? 1 : cap);
        }

        // ==================================================================
        //  Ve the
        // ==================================================================

        private void veThuCung(mGraphics g)
        {
            List<int[]> ds = dsThuCung();
            if (ds.Count == 0)
            {
                veKhungLom(g, xTrai, yThan, rongTrai, caoThan);
                veKhungLom(g, xPhai, yThan, rongPhai, caoThan);
                veChuGiua(g, xTrai, rongTrai, "Chưa sở hữu thú cưng");
                return;
            }
            // Con dang xem bien mat (vua ban, vua mac vao) thi quay ve con dau.
            if (thuDangXem() == null)
            {
                oThuXem = ds[0][0];
            }

            veCotThu(g, ds);

            veDaiTheNho(g, TEN_THE_THU, theThuChon, xPhai, rongPhai);
            veKhungLom(g, xPhai, yNoiDungPhu(), rongPhai, caoNoiDungPhu());
            if (theThuChon == THU_KY_NANG)
            {
                veThuKyNang(g);
            }
            else
            {
                veThuChiSo(g);
            }

            if (hienHopDoAn)
            {
                veHopDoAn(g);
            }
        }

        /// <summary>Cột trái: con thú đứng giữa, danh sách thú xếp dưới.</summary>
        private void veCotThu(mGraphics g, List<int[]> ds)
        {
            veKhungCoTieuDe(g, xTrai, yThan, rongTrai, caoThan, "Thú cưng", 0,
                    MAU_DAI_CAM);
            int y0 = yThan + CAO_DAI_TD + KHE_KHUNG + 2;
            int cao = yThan + caoThan - y0;

            Item thu = thuDangXem();
            int caoDs = oCoThuCung() + 6;
            int yChan = y0 + (cao - caoDs) / 2 + CAO_NHAN_VAT / 2;
            veHinhThu(g, thu, xTrai + rongTrai / 2, yChan);

            // Ten + trang thai ngay duoi con thu.
            if (thu != null && thu.template != null)
            {
                mFont.tahoma_7b_dark.drawString(g, thu.template.name,
                        xTrai + rongTrai / 2, yChan + 4, mFont.CENTER);
                bool raTran = thuXemDangRaTran();
                mFont mf = raTran ? mFont.tahoma_7b_green : mFont.tahoma_7_grey;
                mf.drawString(g, raTran ? "Đang ra trận" : "Đang nghỉ ngơi",
                        xTrai + rongTrai / 2, yChan + 15, mFont.CENTER);
            }

            // Hang o: tung con dang so huu.
            for (int i = 0; i < ds.Count; i++)
            {
                int[] o = oOThuCung(i);
                if (o == null)
                {
                    continue;
                }
                Item it = thuTaiO(ds[i][0]);
                veMotO(g, it, o[0], o[1], o[2], string.Empty);
                if (ds[i][1] == 1)
                {
                    // Cham xanh goc tren: con nay dang ra tran.
                    g.setColor(rgb(0x3F, 0xA9, 0x3F));
                    g.fillRect(o[0] + o[2] - 8, o[1] + 3, 5, 5);
                }
                if (thuTaiO(ds[i][0]) == thuDangXem())
                {
                    veKhungBo(g, o[0], o[1], o[2], o[2], MAU_O, 0f,
                            MAU_VIEN_SANG, 1f, 1);
                }
            }
        }

        /// <summary>Vẽ con thú bằng ba bộ phận máy chủ gửi kèm bảng.</summary>
        private void veHinhThu(mGraphics g, Item thu, int x, int yChan)
        {
            if (thu == null || thu.template == null)
            {
                return;
            }
            short[] hinh;
            if (!hinhThuCung.TryGetValue(thu.template.id, out hinh))
            {
                // Chua co bang hinh: ve tam cai icon cua mon cho do trong.
                SmallImage.drawSmallImage(g, thu.template.iconID, x, yChan - 20,
                        0, mGraphics.VCENTER | mGraphics.HCENTER);
                return;
            }
            if (thuVe == null || thuVeId != thu.template.id)
            {
                thuVe = new Char();
                thuVe.head = hinh[0];
                thuVe.body = hinh[1];
                thuVe.leg = hinh[2];
                thuVe.bag = -1;
                thuVe.cName = string.Empty;
                thuVeId = thu.template.id;
            }
            try
            {
                thuVe.paintCharBody(g, x, yChan - 4, 1, khungDungCho(), false);
            }
            catch (System.Exception)
            {
                // Anh bo phan chua tai xong: khung sau ve lai.
            }
        }

        /// <summary>Cạnh một ô trong hàng danh sách thú.</summary>
        private int oCoThuCung()
        {
            int o = (rongTrai - 10) / 5;
            if (o > O_TOI_DA)
            {
                o = O_TOI_DA;
            }
            if (o < O_TOI_THIEU)
            {
                o = O_TOI_THIEU;
            }
            return o;
        }

        /// <summary>Vùng ô thứ <paramref name="i"/> của hàng danh sách thú.</summary>
        private int[] oOThuCung(int i)
        {
            int o = oCoThuCung();
            int soCot = (rongTrai - 8) / o;
            if (soCot < 1)
            {
                soCot = 1;
            }
            int cot = i % soCot;
            int hang = i / soCot;
            int yDay = yThan + caoThan - 4 - o;
            int y = yDay - hang * o;
            if (y < yThan + CAO_DAI_TD + KHE_KHUNG + 2)
            {
                return null;
            }
            int xDau = xTrai + (rongTrai - soCot * o) / 2;
            return new int[] { xDau + cot * o, y, o };
        }

        // ==================================================================
        //  The con "Chi so"
        // ==================================================================

        private void veThuChiSo(mGraphics g)
        {
            Item thu = thuDangXem();
            if (thu == null || thu.template == null)
            {
                return;
            }
            int x = xPhai + 8;
            int w = rongPhai - 16;
            int y = yNoiDungPhu() + 6;

            mFont.tahoma_7b_dark.drawString(g, thu.template.name, x, y, mFont.LEFT);
            y += 13;

            int cap = capThu(thu);
            mFont.tahoma_7b_dark.drawString(g, "Cấp " + cap + " / " + tcCapToiDa,
                    x, y, mFont.LEFT);
            y += 12;

            // Thanh kinh nghiem.
            int exp = expThu(thu);
            int can = expCanChoCap(cap);
            int rongThanh = w;
            veKhungBo(g, x, y, rongThanh, 8, MAU_O_DO, 1f, MAU_VIEN_O, 0.8f, 1);
            if (cap >= tcCapToiDa)
            {
                g.setColor(rgb(0xE0, 0xA8, 0x3A));
                g.fillRect(x + 1, y + 1, rongThanh - 2, 6);
            }
            else if (can > 0 && exp > 0)
            {
                int rongDay = (rongThanh - 2) * exp / can;
                if (rongDay > rongThanh - 2)
                {
                    rongDay = rongThanh - 2;
                }
                g.setColor(rgb(0x5A, 0xB8, 0x4A));
                g.fillRect(x + 1, y + 1, rongDay, 6);
            }
            mFont.tahoma_7_grey.drawString(g,
                    cap >= tcCapToiDa ? "Đã tối đa" : (exp + " / " + can),
                    x + rongThanh / 2, y - 1, mFont.CENTER);
            y += 14;

            // Cac chi so cua mon, bo hai dong cap va kinh nghiem.
            if (thu.itemOption != null)
            {
                for (int i = 0; i < thu.itemOption.Length; i++)
                {
                    ItemOption io = thu.itemOption[i];
                    if (io == null || io.optionTemplate == null)
                    {
                        continue;
                    }
                    if (io.optionTemplate.id == tcIdChiSoCap
                            || io.optionTemplate.id == tcIdChiSoExp)
                    {
                        continue;
                    }
                    if (y > yNoiDungPhu() + caoNoiDungPhu() - 40)
                    {
                        break;
                    }
                    mFont.tahoma_7_blue.drawString(g, io.getOptionString(), x, y,
                            mFont.LEFT);
                    y += 11;
                }
            }

            // O "+" mo hop chon do an, va nut ra tran / nghi ngoi.
            int[] oAn = oNutChoAn();
            veKhungBo(g, oAn[0], oAn[1], oAn[2], oAn[3], MAU_O_DO, 1f,
                    MAU_VIEN_O, 0.9f, 1);
            mFont.tahoma_7b_dark.drawString(g, "+",
                    oAn[0] + oAn[2] / 2, oAn[1] + oAn[3] / 2 - 6, mFont.CENTER);
            mFont.tahoma_7_grey.drawString(g, "Cho ăn",
                    oAn[0] + oAn[2] + 6, oAn[1] + oAn[3] / 2 - 5, mFont.LEFT);

            int[] nut = oNutRaTran();
            veNut(g, nut[0], nut[1], nut[2], nut[3],
                    thuXemDangRaTran() ? "Nghỉ ngơi" : "Ra trận", true);
        }

        /// <summary>Ô "+" cho ăn: góc dưới trái cột phải.</summary>
        private int[] oNutChoAn()
        {
            int o = 26;
            return new int[] { xPhai + 8, yThan + caoThan - o - 8, o, o };
        }

        /// <summary>Nút ra trận / nghỉ ngơi: góc dưới phải cột phải.</summary>
        private int[] oNutRaTran()
        {
            int w = 74;
            int h = 20;
            return new int[] { xPhai + rongPhai - w - 8, yThan + caoThan - h - 11, w, h };
        }

        // ==================================================================
        //  The con "Ky nang"
        // ==================================================================

        private void veThuKyNang(mGraphics g)
        {
            Item thu = thuDangXem();
            if (thu == null || thu.template == null)
            {
                return;
            }
            int cap = capThu(thu);
            List<ChieuThuCung> ds;
            if (!chieuTheoThu.TryGetValue(thu.template.id, out ds) || ds.Count == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Con này chưa có chiêu nào.",
                        xPhai + rongPhai / 2, yNoiDungPhu() + 20, mFont.CENTER);
                return;
            }
            for (int i = 0; i < ds.Count && i < 3; i++)
            {
                ChieuThuCung c = ds[i];
                int[] o = oOChieu(i);
                bool mo = cap >= c.capMo;
                veKhungBo(g, o[0], o[1], o[2], o[3], MAU_O_DO, mo ? 1f : 0.55f,
                        MAU_VIEN_O, 0.85f, 1);
                int xc = o[0] + 8;
                int yc = o[1] + 5;
                mFont mfTen = mo ? mFont.tahoma_7b_dark : mFont.tahoma_7_grey;
                mfTen.drawString(g, "Chiêu " + c.thuTu + ": " + c.ten, xc, yc,
                        mFont.LEFT);
                yc += 12;
                if (mo)
                {
                    mFont.tahoma_7_blue.drawString(g, moTaChieu(c, cap), xc, yc,
                            mFont.LEFT);
                    yc += 11;
                    mFont.tahoma_7_grey.drawString(g,
                            "Tỉ lệ " + c.tiLe + "% mỗi đòn · hồi " + c.hoiChieu + "s",
                            xc, yc, mFont.LEFT);
                }
                else
                {
                    mFont.tahoma_7_grey.drawString(g,
                            "Mở khi thú cưng đạt cấp " + c.capMo, xc, yc, mFont.LEFT);
                }
            }
        }

        /// <summary>Một dòng nói chiêu làm gì, đã tính phần mạnh thêm theo cấp.</summary>
        private static string moTaChieu(ChieuThuCung c, int cap)
        {
            int pt = c.phanTram + (cap > c.capMo ? (cap - c.capMo) : 0);
            string mau = (c.loai >= 0 && c.loai < TEN_LOAI_CHIEU.Length)
                    ? TEN_LOAI_CHIEU[c.loai] : "Hiệu ứng #%";
            string s = mau.Replace("#", string.Empty + pt);
            if (c.loai == 1)
            {
                s += " (chiêu " + c.thamSo + ")";
            }
            if (c.loai != 3)
            {
                s += " trong " + c.giay + "s";
            }
            return s;
        }

        private int[] oOChieu(int i)
        {
            int x = xPhai + 6;
            int w = rongPhai - 12;
            int h = (caoNoiDungPhu() - 14) / 3;
            if (h > 48)
            {
                h = 48;
            }
            return new int[] { x, yNoiDungPhu() + 6 + i * (h + 4), w, h };
        }

        // ==================================================================
        //  Hop chon do an
        // ==================================================================

        /// <summary>Cạnh một ô đồ ăn trong hộp.</summary>
        private const int O_DO_AN = 32;

        private int[] khungHopDoAn()
        {
            int soCot = 6;
            int w = soCot * O_DO_AN + 20;
            int soHang = (doAnThuCung.Count + soCot - 1) / soCot;
            if (soHang < 1)
            {
                soHang = 1;
            }
            if (soHang > 4)
            {
                soHang = 4;
            }
            int h = soHang * (O_DO_AN + 12) + 34;
            int x = x0 + (rong - w) / 2;
            int y = y0 + (cao - h) / 2;
            return new int[] { x, y, w, h };
        }

        private int[] oODoAn(int i)
        {
            int[] k = khungHopDoAn();
            int soCot = 6;
            int cot = i % soCot;
            int hang = i / soCot;
            return new int[] { k[0] + 10 + cot * O_DO_AN,
                k[1] + 26 + hang * (O_DO_AN + 12), O_DO_AN, O_DO_AN };
        }

        private void veHopDoAn(mGraphics g)
        {
            int[] k = khungHopDoAn();
            veKhungCoTieuDe(g, k[0], k[1], k[2], k[3], "Chọn thức ăn", 0,
                    MAU_DAI_CAM);

            int chuot = oDoAnTaiChuot();
            for (int i = 0; i < doAnThuCung.Count; i++)
            {
                int[] o = oODoAn(i);
                if (o[1] + o[3] > k[1] + k[3] - 4)
                {
                    break;
                }
                int idMon = doAnThuCung[i][0];
                int soCo = demTrongTui(idMon);
                veKhungBo(g, o[0] + 1, o[1] + 1, o[2] - 2, o[3] - 2,
                        MAU_O_DO, soCo > 0 ? 1f : 0.5f, MAU_VIEN_O,
                        i == chuot ? 1f : 0.8f, 1);
                SmallImage.drawSmallImage(g, iconCuaMon(idMon),
                        o[0] + o[2] / 2, o[1] + o[3] / 2, 0,
                        mGraphics.VCENTER | mGraphics.HCENTER);
                // So luong LUON hien, ke ca khi bang 0 — de biet ngay con hay het.
                mFont mfSo = soCo > 0 ? mFont.tahoma_7b_dark : mFont.tahoma_7_grey;
                mfSo.drawString(g, string.Empty + soCo,
                        o[0] + o[2] / 2, o[1] + o[3] - 2, mFont.CENTER);
            }

            mFont.tahoma_7_grey.drawString(g,
                    "Bấm để cho ăn · giữ để cho ăn liên tiếp",
                    k[0] + k[2] / 2, k[1] + k[3] - 13, mFont.CENTER);

            // Chu goi y khi re chuot: ten mon + exp moi cai.
            if (chuot >= 0 && chuot < doAnThuCung.Count)
            {
                int[] o = oODoAn(chuot);
                string ten = tenCuaMon(doAnThuCung[chuot][0]);
                string chu = ten + " · +" + doAnThuCung[chuot][1] + " exp";
                int wChu = mFont.tahoma_7b_dark.getWidth(chu) + 10;
                int xChu = o[0] + o[2] / 2 - wChu / 2;
                if (xChu < k[0] + 2)
                {
                    xChu = k[0] + 2;
                }
                if (xChu + wChu > k[0] + k[2] - 2)
                {
                    xChu = k[0] + k[2] - 2 - wChu;
                }
                int yChu = o[1] - 14;
                veKhungBo(g, xChu, yChu, wChu, 14, MAU_TIEU_DE, 0.98f,
                        MAU_VIEN, 0.9f, 1);
                mFont.tahoma_7b_dark.drawString(g, chu, xChu + wChu / 2, yChu + 2,
                        mFont.CENTER);
            }
        }

        /// <summary>Ô đồ ăn dưới con trỏ chuột, hoặc -1.</summary>
        private int oDoAnTaiChuot()
        {
            if (!Main.isPC)
            {
                return -1;
            }
            int cx, cy;
            try
            {
                UnityEngine.Vector3 v = UnityEngine.Input.mousePosition;
                cx = (int) (v.x / (float) mGraphics.zoomLevel);
                cy = (int) ((UnityEngine.Screen.height - v.y)
                        / (float) mGraphics.zoomLevel) + mGraphics.addYWhenOpenKeyBoard;
            }
            catch (System.Exception)
            {
                return -1;
            }
            for (int i = 0; i < doAnThuCung.Count; i++)
            {
                int[] o = oODoAn(i);
                if (cx >= o[0] && cx < o[0] + o[2] && cy >= o[1] && cy < o[1] + o[3])
                {
                    return i;
                }
            }
            return -1;
        }

        private static int demTrongTui(int idMon)
        {
            Item[] tui = Char.myCharz().arrItemBag;
            if (tui == null)
            {
                return 0;
            }
            int n = 0;
            for (int i = 0; i < tui.Length; i++)
            {
                if (tui[i] != null && tui[i].template != null
                        && tui[i].template.id == idMon)
                {
                    n += tui[i].quantity < 1 ? 1 : tui[i].quantity;
                }
            }
            return n;
        }

        /// <summary>Ô hành trang đầu tiên chứa món này, hoặc -1.</summary>
        private static int oTuiCuaMon(int idMon)
        {
            Item[] tui = Char.myCharz().arrItemBag;
            if (tui == null)
            {
                return -1;
            }
            for (int i = 0; i < tui.Length; i++)
            {
                if (tui[i] != null && tui[i].template != null
                        && tui[i].template.id == idMon)
                {
                    return i;
                }
            }
            return -1;
        }

        private static short iconCuaMon(int idMon)
        {
            ItemTemplate t = ItemTemplates.get((short) idMon);
            return t == null ? (short) (-1) : t.iconID;
        }

        private static string tenCuaMon(int idMon)
        {
            ItemTemplate t = ItemTemplates.get((short) idMon);
            return t == null ? ("Món " + idMon) : t.name;
        }

        // ==================================================================
        //  Cho an
        // ==================================================================

        /// <summary>Gửi lệnh cho con đang xem ăn món ở ô <paramref name="iMon"/>.</summary>
        private void choThuAn(int iMon)
        {
            if (iMon < 0 || iMon >= doAnThuCung.Count)
            {
                return;
            }
            Item thu = thuDangXem();
            if (thu == null)
            {
                return;
            }
            int oTui = oTuiCuaMon(doAnThuCung[iMon][0]);
            if (oTui < 0)
            {
                GameCanvas.startOKDlg("Bạn không có " + tenCuaMon(doAnThuCung[iMon][0]));
                return;
            }
            Service.gI().thuCungChoAn(thuXemDangRaTran(),
                    oThuXem < 0 ? 0 : oThuXem, oTui);
        }

        /// <summary>
        /// Giữ ngón trên một ô đồ ăn thì cho ăn liên tiếp.
        /// </summary>
        /// <remarks>
        /// Chạy trong <c>capNhat()</c> chứ không trong phần bắt chạm: phần ấy chỉ
        /// nhận ĐÚNG lúc nhả ngón, nên giữ bao lâu cũng chỉ ăn một cái.
        /// </remarks>
        private void giuChoAn()
        {
            if (!hienHopDoAn)
            {
                oDoAnDangGiu = -1;
                return;
            }
            if (!GameCanvas.isPointerDown)
            {
                oDoAnDangGiu = -1;
                return;
            }
            long bayGio = mSystem.currentTimeMillis();
            if (oDoAnDangGiu < 0)
            {
                for (int i = 0; i < doAnThuCung.Count; i++)
                {
                    int[] o = oODoAn(i);
                    if (GameCanvas.isPointerHoldIn(o[0], o[1], o[2], o[3]))
                    {
                        oDoAnDangGiu = i;
                        mocGiuDoAn = bayGio;
                        mocAnGanNhat = bayGio;
                        break;
                    }
                }
                return;
            }
            int[] oGiu = oODoAn(oDoAnDangGiu);
            if (!GameCanvas.isPointerHoldIn(oGiu[0], oGiu[1], oGiu[2], oGiu[3]))
            {
                oDoAnDangGiu = -1;
                return;
            }
            if (bayGio - mocGiuDoAn < CHO_TRUOC_KHI_LAP
                    || bayGio - mocAnGanNhat < NHIP_AN_LAP)
            {
                return;
            }
            mocAnGanNhat = bayGio;
            choThuAn(oDoAnDangGiu);
        }

        // ==================================================================
        //  Bat cham
        // ==================================================================

        private bool chamThuCung()
        {
            if (hienHopDoAn)
            {
                for (int i = 0; i < doAnThuCung.Count; i++)
                {
                    int[] o = oODoAn(i);
                    if (cham(o[0], o[1], o[2], o[3]))
                    {
                        // Giu ngon da cho an roi thi cai nha ngon nay khong an them.
                        if (oDoAnDangGiu != i)
                        {
                            choThuAn(i);
                        }
                        oDoAnDangGiu = -1;
                        return true;
                    }
                }
                int[] k = khungHopDoAn();
                if (!cham(k[0], k[1], k[2], k[3]))
                {
                    // Bam ra ngoai hop thi dong hop.
                    hienHopDoAn = false;
                }
                return true;
            }

            int t = theNhoTaiDiem(TEN_THE_THU.Length, xPhai, rongPhai);
            if (t >= 0)
            {
                theThuChon = t;
                return true;
            }

            List<int[]> ds = dsThuCung();
            for (int i = 0; i < ds.Count; i++)
            {
                int[] o = oOThuCung(i);
                if (o != null && cham(o[0], o[1], o[2], o[2]))
                {
                    oThuXem = ds[i][0];
                    return true;
                }
            }

            if (theThuChon == THU_CHI_SO && thuDangXem() != null)
            {
                int[] oAn = oNutChoAn();
                if (cham(oAn[0], oAn[1], oAn[2], oAn[3]))
                {
                    hienHopDoAn = true;
                    if (!tcDaCoBang)
                    {
                        Service.gI().thuCungXinBang();
                    }
                    return true;
                }
                int[] nut = oNutRaTran();
                if (cham(nut[0], nut[1], nut[2], nut[3]))
                {
                    doiRaTran();
                    return true;
                }
            }
            return true;
        }

        /// <summary>
        /// Cho con đang xem ra trận, hoặc cho con đang ra trận về nghỉ.
        /// </summary>
        /// <remarks>
        /// Chỉ có <b>một</b> ô thú cưng nên không cần luật "chỉ một con ra
        /// trận": mặc con mới là con cũ tự về hành trang. Dùng đúng hai lệnh
        /// mặc/tháo sẵn có, không thêm gói tin mới.
        /// </remarks>
        private void doiRaTran()
        {
            if (thuXemDangRaTran())
            {
                Service.gI().getItem((sbyte) 5, (sbyte) 7);
                return;
            }
            if (oThuXem >= 0)
            {
                Service.gI().getItem((sbyte) 4, (sbyte) oThuXem);
                // Con vua mac se nam o o Pet; chuyen sang xem no.
                oThuXem = -1;
            }
        }
    }
}
