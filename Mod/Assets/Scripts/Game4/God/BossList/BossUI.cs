// Khong "using System": lop Math cua engine trung ten voi System.Math.
using System.Collections.Generic;

namespace Game4.God
{
    /// <summary>
    /// Màn hình Boss — danh sách bên trái, boss và vật phẩm rơi bên phải.
    /// </summary>
    /// <remarks>
    /// <para>Chỉ để <b>xem</b>: không có nút dịch chuyển, và không hiện khu. Khu
    /// thì boss đổi liên tục nên con số hiện ra sai gần như ngay lập tức, mà
    /// không dịch chuyển được thì biết khu cũng không dùng làm gì.</para>
    ///
    /// <para><b>Nhiều phase là nhiều tab của một dòng.</b> Boss nhiều phase chỉ
    /// chiếm một dòng trong danh sách; mỗi phase một tab ở khung phải, có HP và đồ
    /// rơi riêng. Xếp mỗi phase một dòng thì danh sách phình gấp đôi mà người chơi
    /// vẫn phải đi tìm đúng một con.</para>
    ///
    /// <para>Bố cục tính một lần trong <see cref="tinhBoCuc"/> rồi cả phần vẽ lẫn
    /// phần bắt chạm cùng đọc, tránh hai bên tính lệch nhau.</para>
    /// </remarks>
    public class BossUI
    {
        private static BossUI instance;

        public static BossUI getInstance()
        {
            return instance ?? (instance = new BossUI());
        }

        public bool dangMo;

        // ------------------------------------------------------------------
        //  Dữ liệu
        // ------------------------------------------------------------------
        /// <summary>Một món boss có thể rơi.</summary>
        public class Roi
        {
            public int icon;
            public string ten;

            /// <summary>Khoảng số lượng: "3" hoặc "1–7".</summary>
            public string soLuong;

            /// <summary>Tỉ lệ rơi đã kèm dấu phần trăm: "30%".</summary>
            public string tiLe;

            /// <summary>"vĩnh viễn", hoặc "7 ngày · 10% vĩnh viễn".</summary>
            public string hanDung;
        }

        /// <summary>Một phase của boss — HP và đồ rơi riêng.</summary>
        public class Phase
        {
            public string ten;
            public string hpChu;
            public int phanTramHp;

            // Ba bo phan than nguoi cua con boss, va gioi tinh.
            //
            // Truoc day chi co MOT truong `icon` mang outfit[0], roi ve bang
            // SmallImage.drawSmallImage. Sai loai so: outfit[0] la chi so PART
            // trong GameScr.parts, khong phai ma anh nho — nen preview ra mot
            // hinh ti xiu khong lien quan. Ba so nay dung de dung mot Char tam
            // roi goi paintCharBody, cung duong ma su phu va de tu dang dung.
            public int head = -1;
            public int body = -1;
            public int leg = -1;
            public int gender;
            public readonly List<Roi> roi = new List<Roi>();
        }

        /// <summary>Một con boss trong danh sách.</summary>
        public class Dong
        {
            public int id;

            /// <summary>Khoá định danh của bản boss này.</summary>
            /// <remarks>
            /// Một id có thể có nhiều bản — <c>boss_spawn.so_ban_sao</c> dựng mấy
            /// con cùng id, nên danh sách có ba dòng "Sasuke Lục Đạo" giống nhau.
            /// Riêng id thì không chỉ được dòng nào: máy chủ trả về bản đầu, và
            /// client cũng tìm ra dòng đầu.
            ///
            /// Khoá này KHÔNG phải số thứ tự trong nhóm. Số thứ tự tính theo vị
            /// trí nên nó dịch ngay khi một bản cùng id xuất hiện hay biến mất, mà
            /// danh sách đổi liên tục vì boss chết rồi hồi sinh — giữ con đang xem
            /// bằng số ấy thì cứ vài giây lại mất dấu và nhảy về dòng đầu. Xem
            /// <c>BossManHinhService.khoaCua</c>.
            /// </remarks>
            public int khoa;
            public string ten;
            public string tenMap;
            public string trangThai;
            public string choHoiSinh;

            /// <summary>Tên người hạ con này gần nhất; rỗng nếu chưa ai hạ.</summary>
            /// <remarks>
            /// Là lần GẦN NHẤT chứ không phải trạng thái hiện thời: boss hồi sinh
            /// rồi thì dòng này vẫn còn, vì người chơi muốn biết ai vừa ăn con đó.
            /// </remarks>
            public string nguoiTieuDiet;

            /// <summary>1 đang xuất hiện · 2 đang chờ hồi sinh.</summary>
            public int mau;

            /// <summary>Số phase máy chủ khai, biết trước cả khi tải chi tiết.</summary>
            public int soPhase;

            /// <summary>Đã nhận gói chi tiết cho con này chưa.</summary>
            public bool daCoChiTiet;

            public readonly List<Phase> phase = new List<Phase>();
        }

        private readonly List<Dong> ds = new List<Dong>();
        private int chon;
        private int cuon;

        /// <summary>TAM THOI: ghi lai lan lam moi gan nhat de chan doan.</summary>
        private int phaseChon;

        /// <summary>Món rơi đang xem chi tiết, trong phase đang xem.</summary>
        private int roiChon;

        // Bang mau lay DUNG theo man nhan vat kieu moi (TuiMoi/TuiUI): nen kem,
        // vien nau, dai tieu de va o dang chon mau cam. Ba man phu nay truoc day
        // moi cai mot bo mau rieng (nau gan den, xanh than), nen mo lan luot ra
        // nhin nhu ba game khac nhau.
        //
        // Ghi bang rgb() cho doc duoc ma hex; setColor nhan mot so nguyen RGB.
        private static int rgb(int r, int g, int b)
        {
            return (r << 16) | (g << 8) | b;
        }

        /// <summary>Nền bảng.</summary>
        private static readonly int MAU_NEN = rgb(0xFC, 0xE3, 0xC0);

        /// <summary>Dải tiêu đề: cam, cùng màu highlight của thẻ con.</summary>
        private static readonly int MAU_TIEU_DE = rgb(0xF0, 0xA1, 0x64);

        /// <summary>Ô, dòng, nút ở trạng thái thường.</summary>
        private static readonly int MAU_THE = rgb(0xFD, 0xF0, 0xDC);

        /// <summary>Ô mờ hơn: rãnh cuộn, dòng đã xong.</summary>
        private static readonly int MAU_THE_MO = rgb(0xFF, 0xF3, 0xDE);

        /// <summary>Nét viền, và tay cuộn.</summary>
        private static readonly int MAU_VIEN = rgb(0xA8, 0x6E, 0x3C);

        /// <summary>
        /// Nền của ô/nút ĐANG CHỌN.
        /// </summary>
        /// <remarks>
        /// Phải là một màu riêng, không dùng lại <see cref="MAU_VIEN"/>. Bản trước
        /// một hằng số làm cả hai việc — vừa là nét viền vừa là nền ô đang chọn —
        /// nên đổi màu nền kem là ô đang chọn thành nâu đặc, chữ chìm hẳn.
        /// </remarks>
        private static readonly int MAU_CHON = rgb(0xF0, 0xA1, 0x64);

        /// <summary>Bo góc chung, cùng con số với màn nhân vật.</summary>
        private const int BO_GOC = 6;

        /// <summary>
        /// Khung bo góc có viền: tô màu viền cả khối rồi tô nền thụt vào.
        /// </summary>
        /// <remarks>
        /// Dùng <c>fillRect</c> hai lần chứ không <c>drawRect</c>: drawRect của
        /// <c>mGraphics</c> KHÔNG nhận bán kính, nên viền vẽ bằng nó là một khung
        /// vuông chạy quanh một khối bo góc — hở bốn góc.
        /// </remarks>
        private static void veKhungBo(mGraphics g, int x, int y, int w, int h,
                int mauNen, float moNen, int mauVien, float moVien, int day)
        {
            g.setColor(mauVien, moVien);
            g.fillRect(x, y, w, h, BO_GOC);
            g.setColor(mauNen, moNen);
            g.fillRect(x + day, y + day, w - day * 2, h - day * 2, BO_GOC - 1);
        }
        private const int MAU_XANH = 3453691;       // 0x34B03B
        private const int MAU_DO = 12856107;        // 0xC42B2B

        /// <summary>Ảnh dùng cho món "Đồ Thần Linh ngẫu nhiên".</summary>
        /// <remarks>
        /// Món ấy không phải một vật phẩm cụ thể — máy chủ bốc một trong mười ba
        /// món của bộ Thần Linh khi rơi (xem <c>BossDAO.DO_THAN_LINH</c>) — nên
        /// không có mã ảnh nào của riêng nó và máy chủ gửi -1. Lấy ảnh Áo Thần
        /// Linh của Trái Đất (vật phẩm 555, ảnh 4647) làm đại diện, thay vì để ô
        /// trống.
        /// </remarks>
        private const int ICON_THAN_LINH = 4647;

        private const int CAO_TIEU_DE = 22;
        private const int CAO_DONG = 34;
        private const int CAO_TAB = 16;
        private const int O_ROI = 26;
        private const int LE = 6;
        private const int NGUONG_KEO = 6;

        private int x0, y0, rong, cao;
        private int xTrai, rongTrai, yDong, caoVungDong;
        private int xPhai, rongPhai, yThan, caoThan;
        private int soDongHien;

        /// <summary>Nạp dữ liệu vừa nhận, GIỮ nguyên con đang xem.</summary>
        public void nhanDuLieu(List<Dong> moi)
        {
            // Nho con DANG XEM theo id truoc khi thay danh sach.
            //
            // Danh sach do may chu xep lai moi lan gui: locBoss() day con dang
            // song len truoc, con dang cho hoi sinh xuong sau. Mot con chet hay
            // hoi sinh la ca thu tu truot di. Giu "con dang xem" bang CHI SO dong
            // thi cu 5 giay lam moi mot lan, o dang chon lai nhay sang con khac —
            // dung luc nguoi choi bam tab phase thi nhin nhu "bam phase ra con
            // khac".
            int idDangXem = idBossChon();
            int khoaDangXem = khoaBossChon();

            // Giu lai phan chi tiet da tai duoc.
            //
            // Bo het roi xin lai thi cu moi nhip lam moi, khung phai lai ve
            // "Dang tai so lieu…" mot cai — do la cai "giat" thay duoc.
            List<Dong> cu = new List<Dong>(ds);

            ds.Clear();
            if (moi != null)
            {
                ds.AddRange(moi);
            }
            for (int i = 0; i < ds.Count; i++)
            {
                Dong c = timTrong(cu, ds[i].id, ds[i].khoa);
                if (c != null && c.daCoChiTiet)
                {
                    ds[i].phase.AddRange(c.phase);
                    ds[i].daCoChiTiet = true;
                }
            }

            int chonCu = chon;
            chon = viTriCuaId(idDangXem, khoaDangXem);
            if (chon < 0)
            {
                // Con dang xem khong con trong danh sach (vua bi giet, hoac roi
                // khoi khung 120 con). Ve dau va bo phase dang chon: phase 3 cua
                // con cu khong co nghia gi voi con moi.
                // Giu nguyen VI TRI DONG dang xem, chi chan lai trong khoang.
                //
                // Phai lui ve `chonCu` chu khong phai chan `chon`: luc nay `chon`
                // dang bang -1, nen chan no chi ra 0 — tuc van nhay ve dong dau,
                // dung cai dang muon tranh.
                //
                // Nhay ve dong dau la cai giat manh nhat nguoi choi thay: dang doc
                // so lieu con thu ba thi khung nhay ve con dau danh sach. Giu
                // nguyen vi tri thi xau nhat cung chi la xem sang con ke ben.
                chon = chonCu;
                if (chon >= ds.Count)
                {
                    chon = ds.Count - 1;
                }
                if (chon < 0)
                {
                    chon = 0;
                }
                phaseChon = 0;
                roiChon = 0;
            }
            // CHI chan lai cuon theo do dai danh sach moi, KHONG keo ve dong
            // dang chon.
            //
            // Danh sach boss tu lam moi theo nhip. Goi giuChonTrongTam o day thi
            // moi lan lam moi lai keo khung ve dong dang chon — dang cuon giua
            // danh sach la bi giat ve cho con dang chon, ma mac dinh no la dong
            // dau. Dung loi "keo toi nua lai nhay len dau".
            if (soDongHien > 0)
            {
                int toiDaCuon = Math.max(0, ds.Count - soDongHien);
                if (cuon > toiDaCuon)
                {
                    cuon = toiDaCuon;
                }
                if (cuon < 0)
                {
                    cuon = 0;
                }
            }
            capPhaseChon();

            lucXinCuoi = mSystem.currentTimeMillis();
            dangCho = false;
            dangMo = true;
            xinChiTiet();
        }

        /// <summary>Khoá định danh của con boss đang xem, hoặc 0.</summary>
        private int khoaBossChon()
        {
            Dong d = bossChon();
            return (d == null) ? 0 : d.khoa;
        }

        /// <summary>Id con boss đang xem, hoặc -1.</summary>
        private int idBossChon()
        {
            Dong d = bossChon();
            return (d == null) ? -1 : d.id;
        }

        private static Dong timTrong(List<Dong> trong, int id, int khoa)
        {
            for (int i = 0; i < trong.Count; i++)
            {
                if (trong[i] != null && trong[i].id == id
                        && trong[i].khoa == khoa)
                {
                    return trong[i];
                }
            }
            return null;
        }

        /// <summary>Vị trí của boss mang id này trong danh sách, hoặc -1.</summary>
        private int viTriCuaId(int id, int khoa)
        {
            // KHONG con chan "id < 0" o dau ham.
            //
            // Id boss trong du an nay LA SO AM — do dac tu log: -4357, -1000.
            // Phep chan ay nham -1 la dau hieu "chua chon con nao", nhung -1
            // khong tach ra duoc khoi mot id thuc. Ket qua: MOI lan tra cuu deu
            // that bai, nen cu moi nhip lam moi (5 giay) o chon lai bi keo ve
            // dong dau. Day moi la goc cua loi "xem boss cu tro nguoc lai boss
            // dau"; ba lan sua truoc cua toi deu sai cho.
            //
            // Bo chan roi thi truong hop "chua chon gi" tu lo: luc do khoa = 0 va
            // id = -1, khong khop dong nao, ham tra ve -1 dung nhu cu.
            for (int i = 0; i < ds.Count; i++)
            {
                if (ds[i].khoa == khoa)
                {
                    return i;
                }
            }
            // Mat dau theo khoa thi VE so theo id.
            //
            // Khoa mat dau khi bản ấy rời danh sách — chết mà không tự hồi sinh
            // được, hoặc là con do máy chủ dựng lúc chạy nên đối tượng khác. Lúc
            // đó vẫn còn bản khác cùng loại: giữ ô chọn ở đó thì người chơi vẫn
            // đang xem đúng con boss mình quan tâm, thay vì bị kéo về dòng đầu.
            for (int i = 0; i < ds.Count; i++)
            {
                if (ds[i].id == id)
                {
                    return i;
                }
            }
            return -1;
        }

        /// <summary>Chặn phase đang chọn theo số phase của con đang xem.</summary>
        private void capPhaseChon()
        {
            Dong d = bossChon();
            if (d == null)
            {
                phaseChon = 0;
                return;
            }
            if (d.phase.Count > 0 && phaseChon >= d.phase.Count)
            {
                phaseChon = 0;
            }
            if (phaseChon < 0)
            {
                phaseChon = 0;
            }
        }

        /// <summary>Nạp chi tiết một con boss vừa nhận về.</summary>
        /// <remarks>
        /// Đối chiếu bằng <c>id</c> chứ không bằng thứ tự trong danh sách: giữa lúc
        /// hỏi và lúc trả lời, một con khác có thể đã chết và rơi khỏi danh sách,
        /// khi đó thứ tự trượt đi và số liệu sẽ gắn sang con bên cạnh.
        /// </remarks>
        public void nhanChiTiet(int bossId, int khoa, List<Phase> dsPhase)
        {
            // Goi rong thi GIU nguyen so lieu cu.
            //
            // May chu tra khung rong khi con boss vua bien mat giua luc xin. Xoa
            // theo no la khung phai trong mot nhip roi day lai — nhin ra nhu ca
            // the vua nap lai, dung loi "tab ben phai bi reload ca tab".
            if (dsPhase == null || dsPhase.Count == 0)
            {
                return;
            }
            for (int i = 0; i < ds.Count; i++)
            {
                if (ds[i].id != bossId || ds[i].khoa != khoa)
                {
                    continue;
                }
                ds[i].phase.Clear();
                ds[i].phase.AddRange(dsPhase);
                ds[i].daCoChiTiet = true;
                if (i == chon && phaseChon >= ds[i].phase.Count)
                {
                    phaseChon = 0;
                }
                return;
            }
        }

        /// <summary>Xin số liệu phase và đồ rơi của con đang chọn.</summary>
        private void xinChiTiet()
        {
            Dong d = bossChon();
            if (d != null)
            {
                Service.gI().bossXinChiTiet(d.id, d.khoa);
            }
        }

        private long lucXinCuoi;

        /// <summary>Bao lâu thì xin lại danh sách, tính bằng mili giây.</summary>
        /// <remarks>
        /// Boss mọc, bị đánh và chết liên tục; không làm mới thì thanh HP đứng yên
        /// từ lúc mở đến lúc đóng và nói sai về con nào đang sống.
        /// </remarks>
        private const long NHIP_LAM_MOI = 5000L;

        public void capNhat()
        {
            if (!dangMo)
            {
                return;
            }
            if (mSystem.currentTimeMillis() - lucXinCuoi >= NHIP_LAM_MOI)
            {
                lucXinCuoi = mSystem.currentTimeMillis();
                Service.gI().bossXin();
                // Xin lai ca chi tiet: thanh HP cua con dang xem phai chay theo
                // tran danh that, khong dung yen tu luc mo den luc dong.
                xinChiTiet();
            }
        }

        /// <summary>Mở màn hình ngay, chờ dữ liệu về.</summary>
        /// <remarks>
        /// Giữ danh sách cũ nếu có, để mở lại lần hai không nháy trắng một nhịp.
        /// </remarks>
        public void moCho()
        {
            dangMo = true;
            dangCho = true;
            lucXinCuoi = mSystem.currentTimeMillis();
        }

        /// <summary>Đã mở nhưng chưa nhận được dữ liệu lần nào.</summary>
        private bool dangCho;

        public void dong()
        {
            dangMo = false;
        }

        private void tinhBoCuc()
        {
            int rongToiDa = GameCanvas.w - 20;
            rong = Math.max(300, rongToiDa * 78 / 100);
            cao = Math.max(160, GameCanvas.h - 30);
            x0 = (GameCanvas.w - rong) / 2;
            y0 = (GameCanvas.h - cao) / 2;

            // Danh sach ben trai, chiem 46% — vua du hai dong chu ten boss va
            // ten ban do khong bi cat.
            xTrai = x0 + LE;
            rongTrai = (rong - LE * 3) * 46 / 100;
            yDong = y0 + CAO_TIEU_DE + LE + 14;
            caoVungDong = cao - CAO_TIEU_DE - LE * 2 - 14;
            soDongHien = Math.max(1, caoVungDong / CAO_DONG);

            xPhai = xTrai + rongTrai + LE;
            rongPhai = x0 + rong - LE - xPhai;
            yThan = y0 + CAO_TIEU_DE + LE;
            caoThan = cao - CAO_TIEU_DE - LE * 2;
        }

        /// <summary>Con boss đang chọn, hoặc null.</summary>
        private Dong bossChon()
        {
            return (chon >= 0 && chon < ds.Count) ? ds[chon] : null;
        }

        /// <summary>Phase đang xem của con đang chọn, hoặc null.</summary>
        private Phase phaseDangXem()
        {
            Dong d = bossChon();
            if (d == null || d.phase.Count == 0)
            {
                return null;
            }
            int i = phaseChon;
            if (i < 0 || i >= d.phase.Count)
            {
                i = 0;
            }
            return d.phase[i];
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

            g.setColor(0, 0.66f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
            veKhungBo(g, x0, y0, rong, cao, MAU_NEN, 0.97f, MAU_VIEN, 1f, 1);
            g.setColor(MAU_TIEU_DE, 1f);
            g.fillRect(x0 + 1, y0 + 1, rong - 2, CAO_TIEU_DE, BO_GOC);

            mFont.tahoma_7b_red.drawString(g, "BOSS", x0 + LE + 2, y0 + 4,
                    mFont.LEFT);
            veNut(g, x0 + rong - 20, y0 + 4, 16, 14, "X", false);

            if (ds.Count == 0)
            {
                mFont.tahoma_7b_dark.drawString(g,
                        dangCho ? "Đang tải danh sách boss…" : "Chưa có boss nào",
                        x0 + rong / 2, y0 + cao / 2 - 5, mFont.CENTER);
                return;
            }
            veDanhSach(g);
            veChiTiet(g);
        }

        private void veDanhSach(mGraphics g)
        {
            mFont.tahoma_7b_red.drawString(g,
                    "DANH SÁCH BOSS (" + ds.Count + ")",
                    xTrai + rongTrai / 2, y0 + CAO_TIEU_DE + LE - 2,
                    mFont.CENTER);

            if (cuon > Math.max(0, ds.Count - soDongHien))
            {
                cuon = Math.max(0, ds.Count - soDongHien);
            }
            for (int i = cuon; i < ds.Count && i - cuon < soDongHien; i++)
            {
                veMotDong(g, ds[i], yDong + (i - cuon) * CAO_DONG, i == chon);
            }
            if (ds.Count > soDongHien)
            {
                veVachCuon(g, ds.Count, soDongHien);
            }
        }

        private void veMotDong(mGraphics g, Dong d, int y, bool dangChon)
        {
            int h = CAO_DONG - 4;
            int w = rongTrai - 4;
            g.setColor(dangChon ? MAU_CHON : MAU_THE, dangChon ? 1f : 0.95f);
            g.fillRect(xTrai, y, w, h, 5);

            // Vach mau ben trai: xanh la dang xuat hien, cam la dang cho. Doc mot
            // cai la biet ngay con nao di duoc.
            g.setColor(d.mau == 1 ? MAU_XANH : MAU_VIEN, 0.95f);
            g.fillRect(xTrai, y, 3, h);

            mFont mfTen = dangChon ? mFont.tahoma_7b_dark
                    : (d.mau == 1 ? mFont.tahoma_7b_red : mFont.tahoma_7);
            mfTen.drawString(g, catBot(d.ten, 22), xTrai + 8, y + 3, mFont.LEFT);

            // Dang song thi hien BAN DO, dang cho thi hien con bao lau nua moc.
            // Hai thu do khong bao gio cung co nghia mot luc, nen dung chung mot
            // dong — khong ton them mot hang cho ca danh sach.
            string duoi;
            if (d.mau == 1)
            {
                duoi = (d.tenMap == null || d.tenMap.Length == 0)
                        ? "đang xuất hiện" : d.tenMap;
            }
            else if (d.choHoiSinh != null && d.choHoiSinh.Length > 0)
            {
                duoi = "còn " + d.choHoiSinh;
            }
            else
            {
                duoi = (d.trangThai == null || d.trangThai.Length == 0)
                        ? "chưa hồi sinh" : d.trangThai;
            }
            mFont mfMap = d.mau == 1 ? mFont.tahoma_7b_green2 : mFont.tahoma_7;
            mfMap.drawString(g, catBot(duoi, 26), xTrai + 8, y + 15, mFont.LEFT);
        }

        private void veChiTiet(mGraphics g)
        {
            Dong d = bossChon();
            Phase p = phaseDangXem();
            if (d == null)
            {
                return;
            }
            g.setColor(MAU_THE_MO, 0.6f);
            g.fillRect(xPhai, yThan, rongPhai, caoThan, 6);
            if (p == null)
            {
                // Chi tiet di rieng mot goi nen co mot nhip trong giua luc bam va
                // luc so lieu ve. Noi ra dang tai, dung de khung trong khong loi.
                mFont.tahoma_7.drawString(g,
                        d.daCoChiTiet ? "Chưa khai số liệu" : "Đang tải số liệu…",
                        xPhai + rongPhai / 2, yThan + caoThan / 2 - 5,
                        mFont.CENTER);
                return;
            }

            int y = yThan + 4;

            // ---- vật phẩm rơi ----
            mFont.tahoma_7b_red.drawString(g, "VẬT PHẨM RƠI",
                    xPhai + rongPhai / 2, y, mFont.CENTER);
            y += 14;
            yODoRoi = y;
            veODoRoi(g, p, y);
            y += O_ROI + 4;

            // ---- tab phase ----
            //
            // Ve NGAY duoi hang o roi, TRUOC ba dong so lieu mon roi.
            //
            // Vi sao thu tu nay: ba dong so lieu khong phai luc nao cung co (phase
            // chua khai do roi thi khong co dong nao), nen dat tab phase sau chung
            // la moi lan doi phase, dai tab nhay len hoac tut xuong 37 diem. Bam
            // lan thu hai vao dung cho cu la truot ra ngoai dai tab.
            if (d.phase.Count > 1)
            {
                yTabPhase = y;
                int rongTab = Math.min(64, (rongPhai - 8) / d.phase.Count);
                for (int i = 0; i < d.phase.Count; i++)
                {
                    veNut(g, xPhai + 4 + i * (rongTab + 2), y, rongTab, CAO_TAB,
                            d.phase[i].ten, i == phaseChon);
                }
                y += CAO_TAB + 6;
            }
            y = veChiTietRoi(g, p, y) + 4;

            // ---- tên và HP ----
            mFont.tahoma_7b_dark.drawString(g, catBot(d.ten, 26),
                    xPhai + rongPhai / 2, y, mFont.CENTER);
            y += 12;
            mFont.tahoma_7b_red.drawString(g, "HP: " + p.hpChu,
                    xPhai + rongPhai / 2, y, mFont.CENTER);
            y += 12;
            string dongTT = (d.trangThai == null) ? "" : d.trangThai;
            if (d.mau != 1 && d.choHoiSinh != null && d.choHoiSinh.Length > 0)
            {
                dongTT = dongTT + " · còn " + d.choHoiSinh;
            }
            if (d.tenMap != null && d.tenMap.Length > 0)
            {
                dongTT = d.tenMap + " · " + dongTT;
            }
            mFont.tahoma_7.drawString(g, catBot(dongTT, 42),
                    xPhai + rongPhai / 2, y, mFont.CENTER);
            y += 12;
            if (d.nguoiTieuDiet != null && d.nguoiTieuDiet.Length > 0)
            {
                // Chi hien khi da co nguoi ha. Hien "Nguoi tieu diet: —" cho moi
                // con chua ai dung toi chi lam day khung ma khong noi them gi.
                mFont.tahoma_7b_dark.drawString(g,
                        catBot("Người tiêu diệt: " + d.nguoiTieuDiet, 42),
                        xPhai + rongPhai / 2, y, mFont.CENTER);
                y += 12;
            }

            int rongThanh = rongPhai - 24;
            g.setColor(MAU_THE, 0.95f);
            g.fillRect(xPhai + 12, y, rongThanh, 6, 3);
            int chay = rongThanh * p.phanTramHp / 100;
            if (chay > 0)
            {
                g.setColor(MAU_DO, 0.95f);
                g.fillRect(xPhai + 12, y, chay, 6, 3);
            }
            y += 14;

            // ---- ảnh boss ----
            //
            // Chi ve khi con du cho. Tren man hinh thap, mấy dong so lieu mon roi
            // day y xuong qua day khung: cong thuc diem giua "(day - y) / 2" ra so
            // AM va anh boss nhay len de vao thanh HP.
            int choConLai = yThan + caoThan - y;
            if (p.head >= 0 && choConLai >= 40)
            {
                // paintCharBody nhan toa do CHAN, khong phai tam.
                int yChan = y + choConLai / 2 + 20;
                veThanBoss(g, p, xPhai + rongPhai / 2, yChan);
            }
        }

        /// <summary>Một Char dùng lại cho mọi preview boss.</summary>
        /// <remarks>
        /// Dựng một lần rồi đổi ba bộ phận cho từng con: mỗi khung hình cấp một
        /// <c>Char</c> mới thì máy dọn rác chạy liên tục chỉ để vẽ một hình.
        /// </remarks>
        private static Char charTam;

        /// <summary>
        /// Thân người của con boss, vẽ bằng chính đường mà nhân vật đang dùng.
        /// </summary>
        /// <remarks>
        /// Ba số <c>head</c>, <c>body</c>, <c>leg</c> là chỉ số PART trong
        /// <c>GameScr.parts</c> — máy chủ lấy từ <c>outfit</c> của boss, cùng ba
        /// số mà <c>Boss.getHead/getBody/getLeg</c> trả về. Vẽ chúng bằng
        /// <c>SmallImage.drawSmallImage</c> như bản trước là nhầm loại số.
        ///
        /// Đối số cuối <c>false</c>: KHÔNG vẽ đồ đeo lưng — boss không có.
        /// </remarks>
        private void veThanBoss(mGraphics g, Phase p, int xGiua, int yChan)
        {
            if (charTam == null)
            {
                charTam = new Char();
            }
            charTam.cgender = (sbyte) p.gender;
            charTam.head = (short) p.head;
            charTam.body = (short) p.body;
            charTam.leg = (short) p.leg;
            charTam.bag = -1;
            charTam.paintCharBody(g, xGiua, yChan, 1, khungDungCho(), false);
        }

        /// <summary>Khung ảnh của dáng đứng chờ: 0 hoặc 1, đổi theo nhịp.</summary>
        /// <remarks>
        /// Cùng nhịp mà <c>Char.updateChar</c> dùng cho trạng thái đứng chờ, và
        /// cùng nhịp với preview ở màn nhân vật — nên hai chỗ trông như một.
        /// </remarks>
        private static int khungDungCho()
        {
            int nhip = GameCanvas.gameTick % 30;
            return (nhip % 15 < 5) ? 0 : 1;
        }

        /// <summary>Hàng ô vuông chứa icon các món rơi.</summary>
        /// <remarks>
        /// Vẽ đủ số ô trên MỘT hàng, ô nào không có món thì để trống. Xếp nhiều
        /// hàng thì khung phải cao lên và đẩy thanh HP ra khỏi vùng vẽ, nên máy chủ
        /// đã chặn số món ở mười hai.
        /// </remarks>
        private void veODoRoi(mGraphics g, Phase p, int y)
        {
            int soO = soODoRoi();
            int xBatDau = xDauODoRoi(soO);
            for (int i = 0; i < soO; i++)
            {
                int x = xBatDau + i * (O_ROI + 2);
                bool dangXem = (i == roiChon && i < p.roi.Count);
                // Bo goc CA nen va vien: bản trước tô nền bo góc rồi viền bằng
                // drawRect, mà drawRect khong nhan ban kinh — thanh mot khung
                // vuong chay quanh khoi bo goc, ho bon goc.
                veKhungBo(g, x, y, O_ROI, O_ROI,
                        dangXem ? MAU_CHON : MAU_THE, 0.95f,
                        MAU_VIEN, dangXem ? 1f : 0.5f, 1);
                if (i < p.roi.Count)
                {
                    int ic = (p.roi[i].icon >= 0) ? p.roi[i].icon : ICON_THAN_LINH;
                    SmallImage.drawSmallImage(g, ic,
                            x + O_ROI / 2, y + O_ROI / 2, 0,
                            mGraphics.VCENTER | mGraphics.HCENTER);
                }
            }
            // Boss khong roi gi thi de O TRONG, khong ghi mot dong chu.
            //
            // Dong "chua khai do roi" doc ra nhu mot loi cau hinh, trong khi phan
            // lon boss von khong roi gi ca — no dung. Hang o trong da noi du.
        }

        /// <summary>Bao nhiêu ô rơi vẽ được trên một hàng.</summary>
        private int soODoRoi()
        {
            return Math.max(1, (rongPhai - 8) / (O_ROI + 2));
        }

        private int xDauODoRoi(int soO)
        {
            return xPhai + (rongPhai - (soO * (O_ROI + 2) - 2)) / 2;
        }

        /// <summary>Ô rơi nào đang bị chạm, hoặc -1.</summary>
        private int oRoiTaiDiem(Phase p)
        {
            int soO = soODoRoi();
            int xBatDau = xDauODoRoi(soO);
            for (int i = 0; i < soO && i < p.roi.Count; i++)
            {
                if (cham(xBatDau + i * (O_ROI + 2), yODoRoi, O_ROI, O_ROI))
                {
                    return i;
                }
            }
            return -1;
        }

        /// <summary>
        /// Đỉnh hàng ô rơi của khung hình vừa vẽ.
        /// </summary>
        /// <remarks>
        /// Ghi lại lúc vẽ chứ không tính lại lúc bắt chạm: hàng ô rơi nằm sau mấy
        /// dòng chữ có bề cao đổi theo nội dung, tính lại theo công thức riêng là
        /// vùng bấm lệch khỏi vùng vẽ mà không có gì báo.
        /// </remarks>
        private int yODoRoi;

        /// <summary>Đỉnh dải tab phase của khung hình vừa vẽ.</summary>
        private int yTabPhase;

        /// <summary>
        /// Ba dòng số liệu của món rơi đang chọn: số lượng, tỉ lệ, hạn dùng.
        /// </summary>
        /// <remarks>
        /// Cùng những con số mà bảng đồ rơi trên panel hiện. Hàng ô chỉ có ảnh nên
        /// không đọc ra được món nào bao nhiêu — thiếu ba dòng này thì màn hình
        /// boss chỉ nói "có rơi", không nói "rơi bao nhiêu, mấy phần trăm".
        /// </remarks>
        private int veChiTietRoi(mGraphics g, Phase p, int y)
        {
            if (p.roi.Count == 0)
            {
                return y;
            }
            int i = (roiChon < 0 || roiChon >= p.roi.Count) ? 0 : roiChon;
            Roi r = p.roi[i];

            mFont.tahoma_7b_dark.drawString(g, catBot(r.ten, 30),
                    xPhai + rongPhai / 2, y, mFont.CENTER);
            y += 11;

            string dong = "SL " + chuoi(r.soLuong, "1")
                    + "  ·  tỉ lệ " + chuoi(r.tiLe, "?");
            mFont.tahoma_7b_green2.drawString(g, dong,
                    xPhai + rongPhai / 2, y, mFont.CENTER);
            y += 11;

            mFont.tahoma_7.drawString(g, catBot(chuoi(r.hanDung, ""), 34),
                    xPhai + rongPhai / 2, y, mFont.CENTER);
            return y + 11;
        }

        private static string chuoi(string s, string thay)
        {
            return (s == null || s.Length == 0) ? thay : s;
        }

        private void veVachCuon(mGraphics g, int soDong, int soHien)
        {
            int xv = xTrai + rongTrai - 3;
            int caoRanh = soHien * CAO_DONG;
            g.setColor(MAU_THE_MO, 0.85f);
            g.fillRect(xv, yDong, 3, caoRanh, 2);
            int caoTay = Math.max(12, caoRanh * soHien / soDong);
            int chay = caoRanh - caoTay;
            int toiDa = soDong - soHien;
            int dy = toiDa <= 0 ? 0 : chay * cuon / toiDa;
            g.setColor(MAU_VIEN, 0.9f);
            g.fillRect(xv, yDong + dy, 3, caoTay, 2);
        }

        private void veNut(mGraphics g, int x, int y, int w, int h, string chu,
                bool sang)
        {
            // Khong con vet loe trang o dinh nut: tren nen kem no doc ra nhu mot
            // duong ke lac cho, khong ra hieu ung noi khoi.
            veKhungBo(g, x, y, w, h, sang ? MAU_CHON : MAU_THE,
                    sang ? 1f : 0.95f, MAU_VIEN, 0.85f, 1);
            mFont.tahoma_7b_dark.drawString(g, chu, x + w / 2, y + h / 2 - 5,
                    mFont.CENTER);
        }

        private static string catBot(string s, int toiDa)
        {
            if (s == null)
            {
                return "";
            }
            return s.Length <= toiDa ? s : s.Substring(0, toiDa - 1) + "…";
        }

        // ------------------------------------------------------------------
        //  Cuộn
        // ------------------------------------------------------------------
        private int yMocKeo;
        private bool dangKeo;
        private bool daKeoXa;

        /// <summary>Cuộn danh sách theo lăn chuột và kéo tay.</summary>
        /// <remarks>
        /// Gọi ở đầu phần bắt chạm, TRƯỚC chỗ thoát sớm khi chưa nhả ngón: kéo
        /// phải thấy nhích liên tục. Kéo quá ngưỡng thì bỏ lần nhả ngón đó, kẻo
        /// cuộn xong ngón nhả đúng dòng nào là chọn luôn dòng đó.
        /// </remarks>
        private void cuonDanhSach()
        {
            int toiDa = Math.max(0, ds.Count - soDongHien);
            if (toiDa <= 0)
            {
                cuon = 0;
                dangKeo = false;
                return;
            }
            if (GameCanvas.pXYScrollMouse != 0
                    && trongVungCuon(GameCanvas.pxMouse, GameCanvas.pyMouse))
            {
                cuon += GameCanvas.pXYScrollMouse > 0 ? -1 : 1;
            }
            if (GameCanvas.isPointerDown)
            {
                if (!dangKeo)
                {
                    if (!trongVungCuon(GameCanvas.pxFirst, GameCanvas.pyFirst))
                    {
                        return;
                    }
                    dangKeo = true;
                    daKeoXa = false;
                    yMocKeo = GameCanvas.pyFirst;
                }
                if (Math.abs(GameCanvas.py - GameCanvas.pyFirst) > NGUONG_KEO)
                {
                    daKeoXa = true;
                }
                int buoc = (GameCanvas.py - yMocKeo) / CAO_DONG;
                if (buoc != 0)
                {
                    cuon -= buoc;
                    yMocKeo += buoc * CAO_DONG;
                }
            }
            else
            {
                dangKeo = false;
            }
            if (cuon < 0)
            {
                cuon = 0;
            }
            else if (cuon > toiDa)
            {
                cuon = toiDa;
            }
        }

        private bool trongVungCuon(int x, int y)
        {
            return x >= xTrai && x <= xTrai + rongTrai
                    && y >= yDong && y <= yDong + caoVungDong;
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
            cuonDanhSach();
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }
            if (daKeoXa)
            {
                daKeoXa = false;
                return true;
            }
            if (cham(x0 + rong - 20, y0 + 4, 16, 14))
            {
                dong();
                return true;
            }
            // Chon boss trong danh sach ben trai.
            for (int i = cuon; i < ds.Count && i - cuon < soDongHien; i++)
            {
                int y = yDong + (i - cuon) * CAO_DONG;
                if (cham(xTrai, y, rongTrai - 4, CAO_DONG - 4))
                {
                    if (chon != i)
                    {
                        // Doi con khac thi ve tab phase dau: phase 3 cua con truoc
                        // khong co nghia gi voi con moi chon.
                        phaseChon = 0;
                        roiChon = 0;
                    }
                    chon = i;
                    roiChon = 0;
                    xinChiTiet();
                    return true;
                }
            }
            // Mot o do roi: bam vao thi doc so lieu cua chinh mon do.
            Phase pXem = phaseDangXem();
            if (pXem != null)
            {
                int iRoi = oRoiTaiDiem(pXem);
                if (iRoi >= 0)
                {
                    roiChon = iRoi;
                    return true;
                }
            }
            // Tab phase ben phai.
            Dong d = bossChon();
            if (d != null && d.phase.Count > 1)
            {
                // Dung dung y DA VE, khong tinh lai.
                //
                // Ban cu tinh "yThan + 4 + 14 + O_ROI + 6" — dung cho tan luc them
                // ba dong so lieu mon roi vao giua, roi dai tab phase truot xuong
                // ma vung bam con o cho cu: bam vao tab phase khong an nua.
                int yTab = yTabPhase;
                int rongTab = Math.min(64, (rongPhai - 8) / d.phase.Count);
                for (int i = 0; i < d.phase.Count; i++)
                {
                    if (cham(xPhai + 4 + i * (rongTab + 2), yTab, rongTab,
                            CAO_TAB))
                    {
                        phaseChon = i;
                        roiChon = 0;
                        return true;
                    }
                }
            }
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
