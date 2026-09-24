using System.Collections.Generic;

namespace Game4.God
{
    /// <summary>
    /// Thẻ <b>Thú cưng</b> của bảng nhân vật: xem con thú, nuôi cho lên cấp, cho
    /// ra trận, và xem ba chiêu của nó.
    /// </summary>
    /// <remarks>
    /// <para>Thú <b>không</b> nằm trong hành trang. Cả danh sách thú, cấp, chỉ số
    /// đều do máy chủ gửi xuống qua gói 111 (xem <c>ThuCungService</c>), nên thẻ
    /// này chỉ vẽ lại thứ máy chủ nói, không tự suy ra gì từ túi đồ.</para>
    ///
    /// <para>Nằm riêng một tệp vì thẻ này dài gần bằng cả thẻ Đệ tử. Vẫn là
    /// <c>TuiUI</c> (lớp <c>partial</c>) nên dùng chung được mọi hàm vẽ khung,
    /// dải thẻ con, ô vật phẩm… của bảng.</para>
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

        /// <summary>Một con thú người chơi đang có.</summary>
        public sealed class ThuSoHuu
        {
            public int id;
            public int itemId;
            public string ten = string.Empty;
            public int cap;
            public int exp;
            public int hp;
            public int ki;
            public int sucDanh;
            public int giap;
            public int chiMang;
            public bool raTran;
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

        /// <summary>Bậc từng loại thú: 0 là D, cao nhất là SSS.</summary>
        private static readonly Dictionary<int, int> bacThuCung = new Dictionary<int, int>();

        /// <summary>Tên các bậc — khớp <c>ThuCungDAO.TEN_BAC</c>.</summary>
        private static readonly string[] TEN_BAC_THU = { "D", "C", "B", "A", "S", "SS", "SSS" };

        /// <summary>Thú của chính người chơi, con ra trận đứng đầu.</summary>
        private static readonly List<ThuSoHuu> dsThuCung = new List<ThuSoHuu>();

        private static int tcCapToiDa = 50;
        private static int tcExpCapMot = 100;

        /// <summary>Đã nhận bảng chung chưa.</summary>
        private static bool tcDaCoBang;

        /// <summary>Đã hỏi máy chủ lần nào chưa.</summary>
        private static bool tcDaXinBang;

        /// <summary>
        /// Đọc gói 111. Byte đầu cho biết là bảng chung hay danh sách thú.
        /// </summary>
        /// <remarks>
        /// Thứ tự đọc là giao kèo với <c>ThuCungService</c>: lệch một trường là
        /// lệch cả gói, nên hai bên phải sửa cùng lúc.
        /// </remarks>
        public static void nhanGoiThuCung(Message msg)
        {
            try
            {
                int viec = msg.reader().readByte();
                if (viec == 0)
                {
                    docBangThuCung(msg);
                }
                else if (viec == 1)
                {
                    docDanhSachThu(msg);
                }
            }
            catch (System.Exception e)
            {
                Cout.println("Loi doc goi thu cung: " + e);
            }
        }

        private static void docBangThuCung(Message msg)
        {
            tcCapToiDa = msg.reader().readShort();
            tcExpCapMot = msg.reader().readInt();

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
            bacThuCung.Clear();
            int soThu = msg.reader().readShort();
            for (int i = 0; i < soThu; i++)
            {
                int id = msg.reader().readShort();
                short dau = msg.reader().readShort();
                short than = msg.reader().readShort();
                short chan = msg.reader().readShort();
                hinhThuCung[id] = new short[] { dau, than, chan };
                bacThuCung[id] = msg.reader().readByte();
            }
            tcDaCoBang = true;
        }

        private static void docDanhSachThu(Message msg)
        {
            dsThuCung.Clear();
            int n = msg.reader().readShort();
            for (int i = 0; i < n; i++)
            {
                ThuSoHuu t = new ThuSoHuu();
                t.id = msg.reader().readInt();
                t.itemId = msg.reader().readShort();
                t.ten = msg.reader().readUTF();
                t.cap = msg.reader().readShort();
                t.exp = msg.reader().readInt();
                t.hp = msg.reader().readInt();
                t.ki = msg.reader().readInt();
                t.sucDanh = msg.reader().readInt();
                t.giap = msg.reader().readInt();
                t.chiMang = msg.reader().readShort();
                t.raTran = msg.reader().readByte() == 1;
                dsThuCung.Add(t);
            }
            // Con ra tran dung dau, con lai bac cao truoc.
            dsThuCung.Sort((a, b) =>
            {
                if (a.raTran != b.raTran)
                {
                    return a.raTran ? -1 : 1;
                }
                int ba = bacCuaLoai(a.itemId);
                int bb = bacCuaLoai(b.itemId);
                if (ba != bb)
                {
                    return bb - ba;
                }
                return a.id - b.id;
            });
            tcDaCoBang = true;
        }

        // ==================================================================
        //  Trang thai cua the
        // ==================================================================

        private static readonly string[] TEN_THE_THU = { "Chỉ số", "Kỹ năng" };

        private const int THU_CHI_SO = 0;
        private const int THU_KY_NANG = 1;

        private int theThuChon = THU_CHI_SO;

        /// <summary>Id con đang xem (id dòng của máy chủ), -1 là chưa chọn.</summary>
        private int idThuXem = -1;

        /// <summary>Đang mở hộp chọn đồ ăn.</summary>
        private bool hienHopDoAn;

        /// <summary>Ô đồ ăn đang bị giữ ngón, và lúc cho ăn gần nhất.</summary>
        private int oDoAnDangGiu = -1;
        private long mocGiuDoAn;
        private long mocAnGanNhat;

        /// <summary>Nhân vật dựng tạm để vẽ con thú đang xem.</summary>
        private Char thuVe;
        private int thuVeId = -1;

        private const long CHO_TRUOC_KHI_LAP = 400L;
        private const long NHIP_AN_LAP = 140L;

        // ==================================================================
        //  Tra cuu
        // ==================================================================

        private static int bacCuaLoai(int itemId)
        {
            int b;
            if (!bacThuCung.TryGetValue(itemId, out b) || b < 0 || b >= TEN_BAC_THU.Length)
            {
                return 0;
            }
            return b;
        }

        /// <summary>Tên hiện lên: tên tự đặt, không có thì tên loại thú.</summary>
        private static string tenThu(ThuSoHuu t)
        {
            if (t == null)
            {
                return string.Empty;
            }
            if (t.ten != null && t.ten.Length > 0)
            {
                return t.ten;
            }
            ItemTemplate m = ItemTemplates.get((short) t.itemId);
            return m == null ? ("Thú #" + t.itemId) : m.name;
        }

        private ThuSoHuu thuDangXem()
        {
            if (dsThuCung.Count == 0)
            {
                return null;
            }
            for (int i = 0; i < dsThuCung.Count; i++)
            {
                if (dsThuCung[i].id == idThuXem)
                {
                    return dsThuCung[i];
                }
            }
            // Chua chon gi (hoac con vua chon khong con nua): lay con dau —
            // danh sach da xep con ra tran len truoc.
            idThuXem = dsThuCung[0].id;
            return dsThuCung[0];
        }

        private static int expCanChoCap(int cap)
        {
            int m = tcExpCapMot < 1 ? 1 : tcExpCapMot;
            return m * (cap < 1 ? 1 : cap);
        }

        // ==================================================================
        //  Huy hieu bac
        // ==================================================================

        /// <summary>Màu nền huy hiệu từng bậc, xếp từ D tới SSS.</summary>
        private static readonly int[] MAU_BAC = {
            rgb(0x8A, 0x8A, 0x8A), // D  xam
            rgb(0x4C, 0xA0, 0x50), // C  xanh la
            rgb(0x35, 0x82, 0xC4), // B  xanh duong
            rgb(0x8B, 0x5C, 0xD6), // A  tim
            rgb(0xE8, 0x8B, 0x1A), // S  cam
            rgb(0xD6, 0x3A, 0x3A), // SS do
            rgb(0xC9, 0xA2, 0x27)  // SSS vang kim
        };

        /// <summary>
        /// Huy hiệu bậc: chữ trắng trên nền màu riêng của bậc.
        /// </summary>
        /// <returns>Bề rộng đã vẽ, để chỗ gọi biết chừa chỗ.</returns>
        private int veHuyBac(mGraphics g, int x, int y, int bac, bool nho)
        {
            if (bac < 0 || bac >= TEN_BAC_THU.Length)
            {
                bac = 0;
            }
            string chu = TEN_BAC_THU[bac];
            int cao = nho ? 10 : 12;
            int rong = mFont.tahoma_7b_white.getWidth(chu) + (nho ? 6 : 8);
            veKhungBo(g, x, y, rong, cao, MAU_BAC[bac], 1f, MAU_VIEN_O, 0.85f, 1);
            mFont.tahoma_7b_white.drawString(g, chu, x + rong / 2, y + (nho ? 0 : 1),
                    mFont.CENTER);
            return rong;
        }

        /// <summary>Nhãn "Ra trận": chữ trắng trên nền xanh, có viền.</summary>
        private int veNhanRaTran(mGraphics g, int x, int y)
        {
            string chu = "Ra trận";
            int rong = mFont.tahoma_7b_white.getWidth(chu) + 8;
            veKhungBo(g, x, y, rong, 12, rgb(0x3F, 0xA9, 0x4F), 1f,
                    rgb(0x22, 0x6B, 0x2C), 1f, 1);
            mFont.tahoma_7b_white.drawString(g, chu, x + rong / 2, y + 1, mFont.CENTER);
            return rong;
        }

        // ==================================================================
        //  Ve the
        // ==================================================================

        private void veThuCung(mGraphics g)
        {
            if (dsThuCung.Count == 0)
            {
                veKhungLom(g, xTrai, yThan, rongTrai, caoThan);
                veKhungLom(g, xPhai, yThan, rongPhai, caoThan);
                veChuGiua(g, xTrai, rongTrai, "Chưa sở hữu thú cưng");
                return;
            }

            veCotThu(g);

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
        private void veCotThu(mGraphics g)
        {
            veKhungCoTieuDe(g, xTrai, yThan, rongTrai, caoThan, "Thú cưng", 0,
                    MAU_DAI_CAM);
            int y0d = yThan + CAO_DAI_TD + KHE_KHUNG + 2;
            int caoD = yThan + caoThan - y0d;

            ThuSoHuu thu = thuDangXem();
            int caoDs = oCoThuCung() * soHangThu() + 6;
            int yChan = y0d + (caoD - caoDs) / 2 + CAO_NHAN_VAT / 2;
            veHinhThu(g, thu, xTrai + rongTrai / 2, yChan);

            if (thu != null)
            {
                // Huy hieu bac dat ngay truoc ten, ca cum can giua cot.
                string ten = tenThu(thu);
                int bac = bacCuaLoai(thu.itemId);
                int rongTen = mFont.tahoma_7b_dark.getWidth(ten);
                int rongHuy = mFont.tahoma_7b_white.getWidth(TEN_BAC_THU[bac]) + 8;
                int xCum = xTrai + rongTrai / 2 - (rongTen + rongHuy + 4) / 2;
                veHuyBac(g, xCum, yChan + 3, bac, false);
                mFont.tahoma_7b_dark.drawString(g, ten, xCum + rongHuy + 4, yChan + 4,
                        mFont.LEFT);

                mFont mf = thu.raTran ? mFont.tahoma_7b_green : mFont.tahoma_7_grey;
                mf.drawString(g, thu.raTran ? "Đang ra trận" : "Đang nghỉ ngơi",
                        xTrai + rongTrai / 2, yChan + 16, mFont.CENTER);
            }

            // Hang o: tung con dang so huu.
            for (int i = 0; i < dsThuCung.Count; i++)
            {
                int[] o = oOThuCung(i);
                if (o == null)
                {
                    continue;
                }
                ThuSoHuu t = dsThuCung[i];
                veKhungBo(g, o[0] + 1, o[1] + 1, o[2] - 2, o[2] - 2, MAU_O_DO, 1f,
                        MAU_VIEN_O, t.id == idThuXem ? 1f : 0.8f, 1);
                ItemTemplate m = ItemTemplates.get((short) t.itemId);
                if (m != null)
                {
                    SmallImage.drawSmallImage(g, m.iconID, o[0] + o[2] / 2,
                            o[1] + o[2] / 2 - 2, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                }
                veHuyBac(g, o[0] + 2, o[1] + o[2] - 12, bacCuaLoai(t.itemId), true);
                if (t.raTran)
                {
                    veNhanRaTran(g, o[0] + 2, o[1] + 2);
                }
                if (t.id == idThuXem)
                {
                    veKhungBo(g, o[0], o[1], o[2], o[2], MAU_O, 0f,
                            MAU_VIEN_SANG, 1f, 1);
                }
            }
        }

        /// <summary>Vẽ con thú bằng ba bộ phận máy chủ gửi kèm bảng.</summary>
        private void veHinhThu(mGraphics g, ThuSoHuu thu, int x, int yChan)
        {
            if (thu == null)
            {
                return;
            }
            short[] hinh;
            if (!hinhThuCung.TryGetValue(thu.itemId, out hinh))
            {
                ItemTemplate m = ItemTemplates.get((short) thu.itemId);
                if (m != null)
                {
                    SmallImage.drawSmallImage(g, m.iconID, x, yChan - 20, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                }
                return;
            }
            if (thuVe == null || thuVeId != thu.itemId)
            {
                thuVe = new Char();
                thuVe.head = hinh[0];
                thuVe.body = hinh[1];
                thuVe.leg = hinh[2];
                thuVe.bag = -1;
                thuVe.cName = string.Empty;
                thuVeId = thu.itemId;
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

        private int oCoThuCung()
        {
            int o = (rongTrai - 10) / 5;
            if (o > 42)
            {
                o = 42;
            }
            if (o < O_TOI_THIEU)
            {
                o = O_TOI_THIEU;
            }
            return o;
        }

        private int soCotThu()
        {
            int o = oCoThuCung();
            int soCot = (rongTrai - 8) / o;
            return soCot < 1 ? 1 : soCot;
        }

        private int soHangThu()
        {
            int n = (dsThuCung.Count + soCotThu() - 1) / soCotThu();
            if (n < 1)
            {
                n = 1;
            }
            return n > 3 ? 3 : n;
        }

        /// <summary>Vùng ô thứ <paramref name="i"/> của hàng danh sách thú.</summary>
        private int[] oOThuCung(int i)
        {
            int o = oCoThuCung();
            int soCot = soCotThu();
            int cot = i % soCot;
            int hang = i / soCot;
            if (hang >= soHangThu())
            {
                return null;
            }
            int yDay = yThan + caoThan - 4 - o;
            int y = yDay - (soHangThu() - 1 - hang) * o;
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
            ThuSoHuu thu = thuDangXem();
            if (thu == null)
            {
                return;
            }
            int x = xPhai + 8;
            int w = rongPhai - 16;
            int y = yNoiDungPhu() + 6;

            // Hang ten: huy hieu bac, ten, nut doi ten.
            int bac = bacCuaLoai(thu.itemId);
            int rongHuy = veHuyBac(g, x, y + 1, bac, false);
            mFont.tahoma_7b_dark.drawString(g, tenThu(thu), x + rongHuy + 5, y,
                    mFont.LEFT);
            int[] nutTen = oNutDoiTen();
            veKhungBo(g, nutTen[0], nutTen[1], nutTen[2], nutTen[3], MAU_O, 0.95f,
                    MAU_VIEN, 0.8f, 1);
            mFont.tahoma_7b_dark.drawString(g, "Sửa", nutTen[0] + nutTen[2] / 2,
                    nutTen[1] + 2, mFont.CENTER);
            y += 14;

            mFont.tahoma_7b_dark.drawString(g, "Cấp " + thu.cap + " / " + tcCapToiDa,
                    x, y, mFont.LEFT);
            y += 12;

            // Thanh kinh nghiem.
            int can = expCanChoCap(thu.cap);
            veKhungBo(g, x, y, w, 8, MAU_O_DO, 1f, MAU_VIEN_O, 0.8f, 1);
            if (thu.cap >= tcCapToiDa)
            {
                g.setColor(rgb(0xE0, 0xA8, 0x3A));
                g.fillRect(x + 1, y + 1, w - 2, 6);
            }
            else if (can > 0 && thu.exp > 0)
            {
                int rongDay = (w - 2) * thu.exp / can;
                if (rongDay > w - 2)
                {
                    rongDay = w - 2;
                }
                g.setColor(rgb(0x5A, 0xB8, 0x4A));
                g.fillRect(x + 1, y + 1, rongDay, 6);
            }
            mFont.tahoma_7_grey.drawString(g,
                    thu.cap >= tcCapToiDa ? "Đã tối đa" : (thu.exp + " / " + can),
                    x + w / 2, y - 1, mFont.CENTER);
            y += 14;

            // Nam chi so con thu cong cho chu.
            y = veDongChiSoThu(g, x, w, y, "HP", thu.hp, string.Empty);
            y = veDongChiSoThu(g, x, w, y, "KI", thu.ki, string.Empty);
            y = veDongChiSoThu(g, x, w, y, "Sức đánh", thu.sucDanh, string.Empty);
            y = veDongChiSoThu(g, x, w, y, "Giáp", thu.giap, string.Empty);
            veDongChiSoThu(g, x, w, y, "Chí mạng", thu.chiMang, "%");

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
                    thu.raTran ? "Nghỉ ngơi" : "Ra trận", true);
        }

        /// <summary>Một dòng chỉ số: tên bên trái, số bên phải.</summary>
        private int veDongChiSoThu(mGraphics g, int x, int w, int y, string ten,
                int gt, string duoi)
        {
            mFont.tahoma_7_grey.drawString(g, ten, x, y, mFont.LEFT);
            mFont.tahoma_7b_dark.drawString(g, "+" + NinjaUtil.getMoneys(gt) + duoi,
                    x + w, y, mFont.RIGHT);
            return y + 12;
        }

        /// <summary>Nút "Sửa" tên, nằm sát bên phải hàng tên.</summary>
        private int[] oNutDoiTen()
        {
            int w = 30;
            int h = 13;
            return new int[] { xPhai + rongPhai - w - 8, yNoiDungPhu() + 5, w, h };
        }

        private int[] oNutChoAn()
        {
            int o = 26;
            return new int[] { xPhai + 8, yThan + caoThan - o - 8, o, o };
        }

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
            ThuSoHuu thu = thuDangXem();
            if (thu == null)
            {
                return;
            }
            List<ChieuThuCung> ds;
            if (!chieuTheoThu.TryGetValue(thu.itemId, out ds) || ds.Count == 0)
            {
                mFont.tahoma_7_grey.drawString(g, "Con này chưa có chiêu nào.",
                        xPhai + rongPhai / 2, yNoiDungPhu() + 20, mFont.CENTER);
                return;
            }
            for (int i = 0; i < ds.Count && i < 3; i++)
            {
                ChieuThuCung c = ds[i];
                int[] o = oOChieu(i);
                bool mo = thu.cap >= c.capMo;
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
                    mFont.tahoma_7_blue.drawString(g, moTaChieu(c, thu.cap), xc, yc,
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

        private void choThuAn(int iMon)
        {
            if (iMon < 0 || iMon >= doAnThuCung.Count)
            {
                return;
            }
            ThuSoHuu thu = thuDangXem();
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
            Service.gI().thuCungChoAn(thu.id, oTui);
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
            if (!hienHopDoAn || !GameCanvas.isPointerDown)
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
        //  Xin du lieu
        // ==================================================================

        /// <summary>
        /// Hỏi máy chủ dữ liệu cho hai thẻ Đệ tử và Thú cưng.
        /// </summary>
        /// <remarks>
        /// <para>Gọi lúc <b>mở bảng</b>, không đợi tới lúc bấm vào thẻ: hai thẻ ấy
        /// sống nhờ dữ liệu máy chủ, hỏi muộn thì thẻ mở ra trống mất một vòng gói
        /// tin.</para>
        ///
        /// <para>Gói thú cưng trả về cả bảng chung lẫn danh sách thú, nên hỏi lại
        /// mỗi lần mở bảng cũng đúng: cấp và chỉ số của thú đổi theo từng lần cho
        /// ăn.</para>
        /// </remarks>
        private void xinDuLieuDeTuVaThuCung()
        {
            Service.gI().petInfo();
            daXinDe = true;
            Service.gI().thuCungXinBang();
            tcDaXinBang = true;
        }

        /// <summary>Người chơi vừa gõ xong tên mới cho thú.</summary>
        private void goXongTenThu(string chu)
        {
            ThuSoHuu thu = thuDangXem();
            if (thu == null)
            {
                return;
            }
            Service.gI().thuCungDoiTen(thu.id, chu == null ? string.Empty : chu.Trim());
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

            for (int i = 0; i < dsThuCung.Count; i++)
            {
                int[] o = oOThuCung(i);
                if (o != null && cham(o[0], o[1], o[2], o[2]))
                {
                    idThuXem = dsThuCung[i].id;
                    return true;
                }
            }

            ThuSoHuu thu = thuDangXem();
            if (theThuChon == THU_CHI_SO && thu != null)
            {
                int[] nutTen = oNutDoiTen();
                if (cham(nutTen[0], nutTen[1], nutTen[2], nutTen[3]))
                {
                    viecGoChu = GO_TEN_THU;
                    moHopGoChu("Tên thú cưng", tenThu(thu), 0);
                    return true;
                }
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
                    // Dang ra tran thi cho ve nghi (gui 0), khong thi cho ra tran.
                    Service.gI().thuCungRaTran(thu.raTran ? 0 : thu.id);
                    return true;
                }
            }
            return true;
        }
    }
}
