namespace Game1.God
{
    using System.Collections.Generic;

    /// <summary>
    /// Kho dữ liệu và đường mạng của <b>khu trò chơi nhỏ</b> — mọi trò trừ Tài Xỉu.
    /// </summary>
    /// <remarks>
    /// <para><b>Chỉ giữ và vẽ, không có luật chơi nào ở đây.</b> Toàn bộ luật —
    /// tung xúc xắc, tính tiền thắng, kiểm tra đủ vàng — nằm ở máy chủ, trong
    /// package <c>nro.gameplay.minigame</c>. Lớp này nhận con số đã tính xong
    /// rồi bày ra màn hình.</para>
    ///
    /// <para>Làm ngược lại thì luật nằm trong tệp mà người chơi cầm trong tay, và
    /// mọi con số hiện lên chỉ là gợi ý.</para>
    ///
    /// <h2>Một mã gói cho sáu trò</h2>
    ///
    /// <para>Mã 117, byte đầu là mã trò, byte thứ hai là việc. Xem
    /// <c>MiniGameService</c> bên máy chủ để đối chiếu.</para>
    ///
    /// <h2>Tách hẳn khỏi mã game</h2>
    ///
    /// <para>Cả khu trò chơi nằm trong thư mục <c>God/TroChoi</c>, đi qua đúng
    /// một mã gói, và không sửa gì trong <c>Service.cs</c> — hàm gửi nằm ngay ở
    /// đây. Muốn gỡ thì xoá thư mục và một nhánh trong <c>Controller.cs</c>.</para>
    /// </remarks>
    public class MiniGame
    {
        private static MiniGame instance;

        public static MiniGame gI()
        {
            return instance ?? (instance = new MiniGame());
        }

        /// <summary>Mã gói. Phải khớp <c>MiniGameService.MA_GOI</c> bên máy chủ.</summary>
        public const sbyte MA_GOI = 117;

        // ---- mã trò ----
        public const sbyte TRO_BAU_CUA = 0;
        public const sbyte TRO_XOC_DIA = 1;
        public const sbyte TRO_DUA_NGUA = 2;
        public const sbyte TRO_DAO_VANG = 3;
        public const sbyte TRO_CAO_THAP = 4;
        public const sbyte TRO_CAU_CA = 5;
        public const int SO_TRO = 6;

        // ---- việc, chiều lên ----
        public const sbyte LEN_MO_BANG = 0;
        public const sbyte LEN_HANH_DONG = 1;
        public const sbyte LEN_DONG_BANG = 2;
        public const sbyte LEN_XIN_LS_TOI = 3;
        public const sbyte LEN_XIN_LS_SERVER = 4;

        // ==================================================================
        //  Trạng thái từng trò
        // ==================================================================

        /// <summary>Số thỏi vàng đang có — mọi trò dùng chung một con số.</summary>
        public long thoiKhoa;
        public long thoiThuong;

        public long tongThoi
        {
            get { return thoiKhoa + thoiThuong; }
        }

        // ---- Bầu Cua ----
        public const int BC_SO_CUA = 6;
        public long bcPhien;
        public sbyte bcGiaiDoan;
        public int bcGiay;
        public readonly long[] bcTongCua = new long[BC_SO_CUA];
        public readonly long[] bcCuaToi = new long[BC_SO_CUA];
        public readonly int[] bcXucXac = new int[3];
        public readonly List<sbyte> bcLichSu = new List<sbyte>();
        /// <summary>Mốc lúc nhận kết quả, để chạy hiệu ứng loé.</summary>
        public long bcMocKetQua;

        // ---- Xóc Đĩa ----
        public const int XD_SO_CUA = 4;
        public long xdPhien;
        public sbyte xdGiaiDoan;
        public int xdGiay;
        public readonly long[] xdTongCua = new long[XD_SO_CUA];
        public readonly long[] xdCuaToi = new long[XD_SO_CUA];
        /// <summary>Bốn đồng xu gói trong bốn bit thấp: bit bật là mặt đỏ.</summary>
        public int xdDongXu;
        public readonly List<sbyte> xdLichSu = new List<sbyte>();
        public long xdMocKetQua;

        // ---- Đua Ngựa ----
        public const int DN_SO_NGUA = 6;
        public long dnPhien;
        public sbyte dnGiaiDoan;
        public int dnGiay;
        public readonly long[] dnTongCon = new long[DN_SO_NGUA];
        public readonly long[] dnCuaToi = new long[DN_SO_NGUA];
        /// <summary>Thứ tự về đích: phần tử 0 là con về nhất.</summary>
        public readonly int[] dnThuTu = new int[DN_SO_NGUA];
        public readonly List<sbyte> dnLichSu = new List<sbyte>();
        /// <summary>Mốc lúc pha đua bắt đầu, để chạy hoạt hình chạy đua.</summary>
        public long dnMocBatDauDua;

        // ---- Đào Vàng ----
        public const int DV_SO_O = 25;
        public bool dvCoVan;
        public long dvCuoc;
        public int dvSoDaMo;
        public long dvTienRut;
        public long dvTienNeuMoThem;
        /// <summary>0 chưa mở, 1 mở an toàn, 2 mìn vừa nổ, 3 mìn lộ khi hết ván.</summary>
        public readonly sbyte[] dvO = new sbyte[DV_SO_O];

        // ---- Cao Thấp ----
        public bool ctCoVan;
        public long ctCuoc;
        public int ctLa;
        public int ctSoLanDung;
        public int ctHeSoX100;
        public long ctTienRut;
        /// <summary>Lá vừa lật, hoặc -1. Dùng để chạy hoạt hình lật bài.</summary>
        public int ctLaVuaLat = -1;
        public long ctMocLat;
        public int ctHeSoThapX100 = 100;
        public int ctHeSoCaoX100 = 100;

        // ---- Câu Cá ----
        public int ccGiayCho;
        public long ccTienMoi = 20;
        /// <summary>Loại vừa câu được, hoặc -1 khi chưa câu lần nào.</summary>
        public int ccLoaiVuaCau = -1;
        public long ccThuongVuaCau;
        public long ccMocCau;

        /// <summary>Đang có ván vật lộn treo ở máy chủ.</summary>
        public bool ccDangVatLon;

        /// <summary>Quãng chờ cá cắn của lượt này, máy chủ bốc, tính mili giây.</summary>
        /// <remarks>
        /// Máy chủ bốc chứ không để client tự chọn: chờ là một phần của trò, mà
        /// client tự chọn thì nó chọn không chờ.
        /// </remarks>
        public int ccChoMs = 2500;

        /// <summary>Loại cá vừa cắn mồi, hoặc -1 khi chưa có.</summary>
        public int ccLoaiCan = -1;

        // ---- bộ tham số độ khó, máy chủ gửi kèm lúc cá cắn ----
        //
        // Giữ ở đây chứ không đặt hằng số trong màn vẽ: bảng cân bằng nằm một
        // chỗ ở máy chủ, sửa số không phải dựng lại client.
        public int ccTocDoTang = 50;
        public int ccTocDoGiam = 22;
        public int ccTocDoVach = 40;
        public int ccRongVung = 26;
        public int ccTienTrinhDau = 30;
        public int ccThuongCan;

        // ---- lịch sử chung, dùng lại cho mọi trò ----
        public class DongLsToi
        {
            public long phien;
            public int cua;
            public long soThoi;
            public int ketQua;
            public bool thang;
            public long tienThang;
        }

        public class DongLsServer
        {
            public long phien;
            public int kq1;
            public int kq2;
            public int kq3;
            public int ketQua;
            public long tongCuoc;
            public long tongTra;
            public int soNguoi;
        }

        public readonly List<DongLsToi> lsToi = new List<DongLsToi>();
        public readonly List<DongLsServer> lsServer = new List<DongLsServer>();

        /// <summary>Trò mà hai danh sách lịch sử đang thuộc về.</summary>
        public int lsCuaTro = -1;

        // ==================================================================
        //  Chiều lên
        // ==================================================================

        /// <summary>
        /// Gửi một gói lên máy chủ.
        /// </summary>
        /// <remarks>
        /// Mất kết nối thì bỏ qua trong im lặng: người chơi bấm lại. Ném ngoại lệ
        /// ra ngoài thì cả khung hình vẽ dở dang, mà lỗi thật chỉ là mạng.
        /// </remarks>
        private void gui(sbyte tro, sbyte viec, byte[] them)
        {
            Message message = null;
            try
            {
                message = new Message(MA_GOI);
                message.writer().writeByte(tro);
                message.writer().writeByte(viec);
                if (them != null)
                {
                    for (int i = 0; i < them.Length; i++)
                    {
                        message.writer().writeByte(them[i]);
                    }
                }
                Session_ME.gI().sendMessage(message);
            }
            catch (System.Exception)
            {
            }
            finally
            {
                if (message != null)
                {
                    message.cleanup();
                }
            }
        }

        private void guiCuoc(sbyte tro, sbyte cua, long soThoi)
        {
            Message message = null;
            try
            {
                message = new Message(MA_GOI);
                message.writer().writeByte(tro);
                message.writer().writeByte(LEN_HANH_DONG);
                message.writer().writeByte(cua);
                message.writer().writeLong(soThoi);
                Session_ME.gI().sendMessage(message);
            }
            catch (System.Exception)
            {
            }
            finally
            {
                if (message != null)
                {
                    message.cleanup();
                }
            }
        }

        public void moBang(sbyte tro)
        {
            gui(tro, LEN_MO_BANG, null);
        }

        public void dongBang(sbyte tro)
        {
            gui(tro, LEN_DONG_BANG, null);
        }

        public void xinLsToi(sbyte tro)
        {
            lsCuaTro = tro;
            gui(tro, LEN_XIN_LS_TOI, null);
        }

        public void xinLsServer(sbyte tro)
        {
            lsCuaTro = tro;
            gui(tro, LEN_XIN_LS_SERVER, null);
        }

        /// <summary>Đặt cược cho ba trò theo phiên.</summary>
        public void datCuoc(sbyte tro, int cua, long soThoi)
        {
            guiCuoc(tro, (sbyte)cua, soThoi);
        }

        /// <summary>Đào Vàng: 0 vào ván, 1 mở ô, 2 rút tiền.</summary>
        public void daoVangVaoVan(long cuoc)
        {
            Message message = null;
            try
            {
                message = new Message(MA_GOI);
                message.writer().writeByte(TRO_DAO_VANG);
                message.writer().writeByte(LEN_HANH_DONG);
                message.writer().writeByte(0);
                message.writer().writeLong(cuoc);
                Session_ME.gI().sendMessage(message);
            }
            catch (System.Exception)
            {
            }
            finally
            {
                if (message != null)
                {
                    message.cleanup();
                }
            }
        }

        public void daoVangMoO(int o)
        {
            gui(TRO_DAO_VANG, LEN_HANH_DONG, new byte[] { 1, (byte)o });
        }

        public void daoVangRut()
        {
            gui(TRO_DAO_VANG, LEN_HANH_DONG, new byte[] { 2 });
        }

        /// <summary>Cao Thấp: 0 vào ván, 1 đoán, 2 rút tiền.</summary>
        public void caoThapVaoVan(long cuoc)
        {
            Message message = null;
            try
            {
                message = new Message(MA_GOI);
                message.writer().writeByte(TRO_CAO_THAP);
                message.writer().writeByte(LEN_HANH_DONG);
                message.writer().writeByte(0);
                message.writer().writeLong(cuoc);
                Session_ME.gI().sendMessage(message);
            }
            catch (System.Exception)
            {
            }
            finally
            {
                if (message != null)
                {
                    message.cleanup();
                }
            }
        }

        public void caoThapDoan(int huong)
        {
            gui(TRO_CAO_THAP, LEN_HANH_DONG, new byte[] { 1, (byte)huong });
        }

        public void caoThapRut()
        {
            gui(TRO_CAO_THAP, LEN_HANH_DONG, new byte[] { 2 });
        }

        /// <summary>Quăng cần với tầm đã căn, 0..100. Byte việc con 0.</summary>
        /// <remarks>
        /// Tầm quyết định bảng xác suất con nào cắn — quăng xa thì cá to hơn.
        /// Máy chủ ép lại về 0..100 và trần kỳ vọng vẫn dưới tiền mồi ngay ở tầm
        /// xa nhất, nên con số này sửa được mà không mở ra đường in tiền.
        /// </remarks>
        public void cauCaQuang(int tam)
        {
            if (tam < 0)
            {
                tam = 0;
            }
            if (tam > 100)
            {
                tam = 100;
            }
            gui(TRO_CAU_CA, LEN_HANH_DONG, new byte[] { 0, (byte) tam });
        }

        /// <summary>Báo kết quả vật lộn. Byte việc con 1.</summary>
        /// <remarks>
        /// Máy chủ <b>xét lại</b> báo cáo này: nó biết con nào đang treo và biết
        /// thời gian tối thiểu để kéo con ấy lên, nên một cú báo quá nhanh bị bỏ.
        /// </remarks>
        public void cauCaBaoKetQua(bool batDuoc)
        {
            gui(TRO_CAU_CA, LEN_HANH_DONG,
                    new byte[] { 1, (byte) (batDuoc ? 1 : 0) });
        }

        // ==================================================================
        //  Chiều xuống
        // ==================================================================

        /// <summary>
        /// Đọc một gói mã 117.
        /// </summary>
        /// <remarks>
        /// <b>Đọc hết cụm rồi mới dùng dữ liệu.</b> Bỏ giữa chừng là lệch cả
        /// luồng byte của những gói tới sau — lỗi hiện ra ở một tính năng khác
        /// hẳn, rất khó lần ra.
        /// </remarks>
        public void nhanGoi(Message msg)
        {
            try
            {
                int tro = msg.reader().readUnsignedByte();
                int viec = msg.reader().readUnsignedByte();
                switch (tro)
                {
                    case TRO_BAU_CUA:
                        docBauCua(msg, viec);
                        break;
                    case TRO_XOC_DIA:
                        docXocDia(msg, viec);
                        break;
                    case TRO_DUA_NGUA:
                        docDuaNgua(msg, viec);
                        break;
                    case TRO_DAO_VANG:
                        docDaoVang(msg, viec);
                        break;
                    case TRO_CAO_THAP:
                        docCaoThap(msg, viec);
                        break;
                    case TRO_CAU_CA:
                        docCauCa(msg, viec);
                        break;
                }
            }
            catch (System.Exception)
            {
            }
        }

        private void docLichSuChung(Message msg, int viec)
        {
            if (viec == 3)
            {
                int n = msg.reader().readUnsignedByte();
                lsToi.Clear();
                for (int i = 0; i < n; i++)
                {
                    DongLsToi d = new DongLsToi();
                    d.phien = msg.reader().readLong();
                    d.cua = msg.reader().readByte();
                    d.soThoi = msg.reader().readLong();
                    d.ketQua = msg.reader().readByte();
                    d.thang = msg.reader().readByte() == 1;
                    d.tienThang = msg.reader().readLong();
                    lsToi.Add(d);
                }
            }
            else
            {
                int n = msg.reader().readUnsignedByte();
                lsServer.Clear();
                for (int i = 0; i < n; i++)
                {
                    DongLsServer d = new DongLsServer();
                    d.phien = msg.reader().readLong();
                    d.kq1 = msg.reader().readByte();
                    d.kq2 = msg.reader().readByte();
                    d.kq3 = msg.reader().readByte();
                    d.ketQua = msg.reader().readByte();
                    d.tongCuoc = msg.reader().readLong();
                    d.tongTra = msg.reader().readLong();
                    d.soNguoi = msg.reader().readInt();
                    lsServer.Add(d);
                }
            }
        }

        private void docBauCua(Message msg, int viec)
        {
            if (viec == 0)
            {
                bcPhien = msg.reader().readLong();
                bcGiaiDoan = msg.reader().readByte();
                bcGiay = msg.reader().readInt();
                for (int i = 0; i < BC_SO_CUA; i++)
                {
                    bcTongCua[i] = msg.reader().readLong();
                    bcCuaToi[i] = msg.reader().readLong();
                }
                thoiKhoa = msg.reader().readLong();
                thoiThuong = msg.reader().readLong();
                for (int i = 0; i < 3; i++)
                {
                    bcXucXac[i] = msg.reader().readByte();
                }
                int n = msg.reader().readUnsignedByte();
                bcLichSu.Clear();
                for (int i = 0; i < n; i++)
                {
                    bcLichSu.Add(msg.reader().readByte());
                }
            }
            else if (viec == 1)
            {
                bcGiaiDoan = msg.reader().readByte();
                bcGiay = msg.reader().readInt();
                for (int i = 0; i < BC_SO_CUA; i++)
                {
                    bcTongCua[i] = msg.reader().readLong();
                }
            }
            else if (viec == 2)
            {
                bcPhien = msg.reader().readLong();
                for (int i = 0; i < 3; i++)
                {
                    bcXucXac[i] = msg.reader().readByte();
                }
                bcMocKetQua = mSystem.currentTimeMillis();
            }
            else
            {
                docLichSuChung(msg, viec);
            }
        }

        private void docXocDia(Message msg, int viec)
        {
            if (viec == 0)
            {
                xdPhien = msg.reader().readLong();
                xdGiaiDoan = msg.reader().readByte();
                xdGiay = msg.reader().readInt();
                for (int i = 0; i < XD_SO_CUA; i++)
                {
                    xdTongCua[i] = msg.reader().readLong();
                    xdCuaToi[i] = msg.reader().readLong();
                }
                thoiKhoa = msg.reader().readLong();
                thoiThuong = msg.reader().readLong();
                xdDongXu = msg.reader().readByte();
                int n = msg.reader().readUnsignedByte();
                xdLichSu.Clear();
                for (int i = 0; i < n; i++)
                {
                    xdLichSu.Add(msg.reader().readByte());
                }
            }
            else if (viec == 1)
            {
                xdGiaiDoan = msg.reader().readByte();
                xdGiay = msg.reader().readInt();
                for (int i = 0; i < XD_SO_CUA; i++)
                {
                    xdTongCua[i] = msg.reader().readLong();
                }
            }
            else if (viec == 2)
            {
                xdPhien = msg.reader().readLong();
                xdDongXu = msg.reader().readByte();
                xdMocKetQua = mSystem.currentTimeMillis();
            }
            else
            {
                docLichSuChung(msg, viec);
            }
        }

        private void docDuaNgua(Message msg, int viec)
        {
            if (viec == 0)
            {
                dnPhien = msg.reader().readLong();
                sbyte gdCu = dnGiaiDoan;
                dnGiaiDoan = msg.reader().readByte();
                dnGiay = msg.reader().readInt();
                for (int i = 0; i < DN_SO_NGUA; i++)
                {
                    dnTongCon[i] = msg.reader().readLong();
                    dnCuaToi[i] = msg.reader().readLong();
                }
                thoiKhoa = msg.reader().readLong();
                thoiThuong = msg.reader().readLong();
                for (int i = 0; i < DN_SO_NGUA; i++)
                {
                    dnThuTu[i] = msg.reader().readByte();
                }
                int n = msg.reader().readUnsignedByte();
                dnLichSu.Clear();
                for (int i = 0; i < n; i++)
                {
                    dnLichSu.Add(msg.reader().readByte());
                }
                // Vua buoc vao pha dua: ghi moc de chay hoat hinh tu dau.
                if (gdCu != 1 && dnGiaiDoan == 1)
                {
                    dnMocBatDauDua = mSystem.currentTimeMillis();
                }
            }
            else if (viec == 1)
            {
                dnGiaiDoan = msg.reader().readByte();
                dnGiay = msg.reader().readInt();
                for (int i = 0; i < DN_SO_NGUA; i++)
                {
                    dnTongCon[i] = msg.reader().readLong();
                }
            }
            else if (viec == 2)
            {
                dnPhien = msg.reader().readLong();
                for (int i = 0; i < DN_SO_NGUA; i++)
                {
                    dnThuTu[i] = msg.reader().readByte();
                }
            }
            else
            {
                docLichSuChung(msg, viec);
            }
        }

        private void docDaoVang(Message msg, int viec)
        {
            if (viec == 0)
            {
                dvCoVan = msg.reader().readByte() == 1;
                dvCuoc = msg.reader().readLong();
                dvSoDaMo = msg.reader().readByte();
                dvTienRut = msg.reader().readLong();
                dvTienNeuMoThem = msg.reader().readLong();
                int n = msg.reader().readUnsignedByte();
                for (int i = 0; i < n && i < DV_SO_O; i++)
                {
                    dvO[i] = msg.reader().readByte();
                }
                thoiKhoa = msg.reader().readLong();
                thoiThuong = msg.reader().readLong();
            }
            else
            {
                docLichSuChung(msg, viec);
            }
        }

        private void docCaoThap(Message msg, int viec)
        {
            if (viec == 0)
            {
                ctCoVan = msg.reader().readByte() == 1;
                ctCuoc = msg.reader().readLong();
                ctLa = msg.reader().readByte();
                ctSoLanDung = msg.reader().readByte();
                ctHeSoX100 = msg.reader().readInt();
                ctTienRut = msg.reader().readLong();
                int laMoi = msg.reader().readByte();
                ctHeSoThapX100 = msg.reader().readInt();
                ctHeSoCaoX100 = msg.reader().readInt();
                thoiKhoa = msg.reader().readLong();
                thoiThuong = msg.reader().readLong();
                if (laMoi >= 0)
                {
                    ctLaVuaLat = laMoi;
                    ctMocLat = mSystem.currentTimeMillis();
                }
            }
            else
            {
                docLichSuChung(msg, viec);
            }
        }

        private void docCauCa(Message msg, int viec)
        {
            if (viec == 0)
            {
                ccGiayCho = msg.reader().readInt();
                ccTienMoi = msg.reader().readLong();
                ccDangVatLon = msg.reader().readByte() == 1;
                thoiKhoa = msg.reader().readLong();
                thoiThuong = msg.reader().readLong();
            }
            else if (viec == 5)
            {
                // Ca da can moi: nhan bo tham so do kho roi vao pha vat lon.
                ccLoaiCan = msg.reader().readByte();
                ccTocDoTang = msg.reader().readUnsignedByte();
                ccTocDoGiam = msg.reader().readUnsignedByte();
                ccTocDoVach = msg.reader().readUnsignedByte();
                ccRongVung = msg.reader().readUnsignedByte();
                ccTienTrinhDau = msg.reader().readUnsignedByte();
                ccThuongCan = msg.reader().readInt();
                ccChoMs = msg.reader().readInt();
                ccDangVatLon = true;
                // Doc HET goi roi moi goi sang man ve: goi som thi phan byte con
                // lai chua doc, va goi toi sau se doc lech het.
                TroChoiUI.getInstance().cauCaBatDauLuot();
            }
            else if (viec == 2)
            {
                ccLoaiVuaCau = msg.reader().readByte();
                ccThuongVuaCau = msg.reader().readLong();
                bool bat = msg.reader().readByte() == 1;
                thoiKhoa = msg.reader().readLong();
                thoiThuong = msg.reader().readLong();
                ccMocCau = mSystem.currentTimeMillis();
                ccDangVatLon = false;
                TroChoiUI.getInstance().cauCaNhanKetQua(bat);
            }
            else
            {
                docLichSuChung(msg, viec);
            }
        }
    }
}
