package nro.service;

import java.util.ArrayList;
import java.util.List;
import nro.core.log.Logger;
import nro.core.util.FormatStyle;
import nro.core.util.Util;
import nro.entity.boss.Boss;
import nro.entity.boss.BossData;
import nro.entity.boss.TypeAppear;
import nro.entity.player.Player;
import nro.entity.template.ItemTemplate;
import nro.net.io.Message;
import nro.repository.dao.BossDAO;
import nro.service.boss.BossManager;
import nro.service.item.ItemService;

/**
 * Gửi dữ liệu <b>màn hình Boss</b> xuống client.
 *
 * <h2>Lấy dữ liệu ở đâu</h2>
 *
 * <p>Danh sách và trạng thái lấy từ {@code BossManager} — tức đúng những con boss
 * đang sống trong máy chủ. Vật phẩm rơi lấy từ {@code boss_drop}, chính bảng mà
 * tab "1. Đang chạy &amp; đồ rơi" trên panel quản lý; sửa ở panel là trong game
 * thấy ngay.</p>
 *
 * <h2>Nhiều phase là nhiều tab của một dòng</h2>
 *
 * <p>Boss nhiều phase chỉ chiếm <b>một dòng</b> trong danh sách; mỗi phase là một
 * tab ở khung bên trái, có HP và đồ rơi riêng. Xếp mỗi phase một dòng thì danh
 * sách phình gấp đôi mà người chơi vẫn phải đi tìm đúng một con.</p>
 *
 * <h2>Không có khu, không có dịch chuyển</h2>
 *
 * <p>Chỉ hiện tên bản đồ. Khu thì boss đổi liên tục nên con số hiện ra sai gần
 * như ngay lập tức, và không có nút dịch chuyển nên biết khu cũng không dùng làm
 * gì.</p>
 */
public class BossManHinhService {

    private static BossManHinhService instance;

    public static BossManHinhService gI() {
        if (instance == null) {
            instance = new BossManHinhService();
        }
        return instance;
    }

    /**
     * Mã gói màn hình boss.
     *
     * <h3>Vì sao là 119 mà không phải -110</h3>
     *
     * <p>Mã -110 <b>đã có chủ</b>: phía client, {@code Controller2.readMessage}
     * dùng nó cho giao thức RMS (lưu dữ liệu xuống máy người chơi), và hàm đó
     * chạy <b>trước</b> switch chính của {@code Controller}. Cả hai đọc chung một
     * {@code reader} — {@code Message.reader()} trả về đúng một đối tượng chứ
     * không tạo mới — nên nhánh RMS đọc mất byte đầu, thấy giá trị 0 thì đọc tiếp
     * một int, một short rồi hút cả phần còn lại vào {@code Rms.saveRMS}. Đến lúc
     * switch chính chạy thì gói đã cạn: màn hình boss đứng mãi ở "Đang tải" và
     * không có lỗi nào hiện ra.</p>
     *
     * <p>119 là mã còn trống ở <b>cả bốn</b> chỗ định tuyến: hai switch phía
     * client ({@code Controller}, {@code Controller2}) và switch phía máy chủ.
     * Thêm màn mới thì phải soát cả bốn, không chỉ hai.</p>
     */
    public static final byte OPCODE = 119;

    /** Số boss nhiều nhất gửi một lần, cho gói không phình. */
    private static final int TOI_DA_BOSS = 120;

    /** Số món rơi nhiều nhất hiện cho mỗi phase — khung bên trái chỉ vừa nhiêu này. */
    private static final int TOI_DA_ROI = 12;

    /**
     * Gửi <b>danh sách</b> boss — không kèm chi tiết phase.
     *
     * <h3>Vì sao phải tách làm hai gói</h3>
     *
     * <p>Độ dài một gói được đóng khung bằng <b>short</b> ({@code writeShort} ở
     * {@code MessageSendCollect}), tức nhiều nhất 32 KB. Nhồi cả 55 con boss kèm
     * mọi phase và tối đa mười hai món rơi mỗi phase thì gói lên khoảng 57 KB —
     * quá khổ. Tách ra thì gói danh sách còn khoảng 6 KB, gói chi tiết khoảng
     * 2 KB, còn xa mức tràn.</p>
     *
     * <p><b>Lưu ý:</b> đây <i>không</i> phải nguyên nhân của lần treo "Đang tải"
     * đầu tiên — nguyên nhân đó là đụng mã gói, xem {@link #OPCODE}. Kích cỡ chỉ
     * là một giới hạn thật cần tôn trọng, không phải cái đã làm màn hình treo.</p>
     *
     * <h3>Khuôn gói</h3>
     * <pre>
     * byte  0                   (việc: danh sách)
     * byte  soBoss
     *   int   bossId
     *   UTF   ten
     *   UTF   tenMap            (rỗng nếu chưa xuất hiện)
     *   UTF   trangThai         ("đã hồi sinh", "chưa hồi sinh"…)
     *   UTF   choHoiSinh        ("56 phút 10 giây", rỗng nếu đang sống)
     *   byte  mau               1 đang xuất hiện · 2 chờ hồi sinh
     *   byte  soPhase
     * </pre>
     */
    public void guiDuLieu(Player pl) {
        if (pl == null) {
            return;
        }
        Message msg = null;
        try {
            List<Boss> ds = locBoss();
            msg = new Message(OPCODE);
            msg.writer().writeByte(0);
            msg.writer().writeByte(ds.size());
            for (int i = 0; i < ds.size(); i++) {
                Boss b = ds.get(i);
                msg.writer().writeInt((int) b.id);
                // Khoa dinh danh cua BAN nay. Xem khoaCua.
                msg.writer().writeInt(khoaCua(b));
                msg.writer().writeUTF(tenCua(b));
                msg.writer().writeUTF(tenMap(b));
                msg.writer().writeUTF(trangThai(b));
                msg.writer().writeUTF(choHoiSinh(b));
                msg.writer().writeByte(dangSong(b) ? 1 : 2);
                msg.writer().writeByte(b.data == null ? 0 : b.data.length);
            }
            msg.writer().flush();
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(BossManHinhService.class, ex,
                    "Lỗi gửi danh sách boss");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Gửi <b>chi tiết</b> một con boss: HP và đồ rơi của từng phase.
     *
     * <h3>Khuôn gói</h3>
     * <pre>
     * byte  1                   (việc: chi tiết)
     * int   bossId
     * int   khoa                (khoá định danh của bản, xem khoaCua)
     * byte  soPhase
     *   UTF   tenPhase          ("Phase 1")
     *   UTF   hpChu             ("80Tr / 80Tr")
     *   byte  phanTramHp        (0–100)
     *   short head, body, leg   (-1 nếu không tra được)
     *   byte  gender
     *   byte  soDoRoi
     *     short iconId            (-1 nếu không tra được mẫu)
     *     UTF   tenMon
     *     UTF   soLuong           ("3" hoặc "1–7")
     *     UTF   tiLe              ("30%")
     *     UTF   hanDung           ("vĩnh viễn", "7 ngày · 10% vĩnh viễn")
     * </pre>
     */
    public void guiChiTiet(Player pl, int bossId, int khoa) {
        if (pl == null) {
            return;
        }
        Message msg = null;
        try {
            Boss b = timBoss(bossId, khoa);
            msg = new Message(OPCODE);
            msg.writer().writeByte(1);
            msg.writer().writeInt(bossId);
            // Tra lai ca khoa: client phai gan so lieu vao DUNG dong da xin, ma
            // rieng id thi khong phan biet duoc may ban cung id.
            msg.writer().writeInt(khoa);
            if (b == null) {
                // Con boss vua bien mat giua luc nguoi choi bam. Gui khung rong
                // chu khong bo qua: client dang cho goi nay, khong tra loi thi no
                // treo o "Dang tai" mai.
                msg.writer().writeByte(0);
            } else {
                guiCacPhase(msg, b);
            }
            msg.writer().flush();
            pl.sendMessage(msg);
        } catch (Exception ex) {
            Logger.logException(BossManHinhService.class, ex,
                    "Lỗi gửi chi tiết boss " + bossId);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /** Nhận gói từ client: 0 xin danh sách, 1 kèm id xin chi tiết. */
    public void nhanGoi(Player pl, Message msg) {
        if (pl == null || msg == null) {
            return;
        }
        try {
            byte viec = msg.reader().readByte();
            if (viec == 0) {
                guiDuLieu(pl);
            } else {
                int idBoss = msg.reader().readInt();
                int khoa = msg.reader().readInt();
                guiChiTiet(pl, idBoss, khoa);
            }
        } catch (Exception ex) {
            Logger.logException(BossManHinhService.class, ex,
                    "Lỗi nhận gói boss");
        }
    }

    /**
     * Tìm bản thứ {@code ban} trong nhóm các boss mang {@code bossId}.
     *
     * <p>Duyệt trên {@link #locBoss()} chứ không trên bản chụp thô: chỉ danh
     * sách ấy mới cùng một thứ tự với thứ client đang hiện, nên số thứ tự bản mà
     * client gửi lên mới chỉ đúng con.</p>
     */
    private Boss timBoss(int bossId, int khoa) {
        List<Boss> ds = locBoss();
        for (Boss b : ds) {
            if (b != null && khoaCua(b) == khoa) {
                return b;
            }
        }
        // Khong khop khoa: ban do vua bien mat. Ve so theo id de con tra duoc so
        // lieu cua mot ban cung loai, hon la tra khung rong.
        for (Boss b : ds) {
            if (b != null && (int) b.id == bossId) {
                return b;
            }
        }
        return null;
    }

    /**
     * Ghi từng phase của một con boss.
     *
     * <p>HP hiện tại chỉ có thật ở <b>phase đang đứng</b>; các phase khác ghi mốc
     * HP đầy của chúng. Chép HP của phase hiện tại sang mọi phase là đọc nhầm
     * tưởng phase 2 cũng đang bị đánh.</p>
     */
    private void guiCacPhase(Message msg, Boss b) throws Exception {
        BossData[] data = b.data;
        int soPhase = (data == null) ? 0 : data.length;
        msg.writer().writeByte(soPhase);
        for (int i = 0; i < soPhase; i++) {
            BossData d = data[i];
            boolean dangODay = dangSong(b) && i == b.currentLevel;

            long hpMax = hpDayCua(d);
            long hpHienTai = dangODay ? Math.max(0L, b.nPoint.hp) : hpMax;

            msg.writer().writeUTF(soPhase > 1 ? ("Phase " + (i + 1)) : "Số liệu");
            msg.writer().writeUTF(Util.formatNumber(hpHienTai, FormatStyle.KMB)
                    + " / " + Util.formatNumber(hpMax, FormatStyle.KMB));
            msg.writer().writeByte(phanTram(hpHienTai, hpMax));
            // Ba bo phan than nguoi, va gioi tinh.
            //
            // Truoc day chi gui outfit[0] roi client ve bang drawSmallImage. Sai
            // loai so: outfit[0] la chi so PART, khong phai ma anh nho — client
            // ve ra mot hinh ti xiu khong lien quan gi den con boss. Gui ca ba de
            // client dung mot Char tam roi goi paintCharBody, dung duong ma nhan
            // vat va de tu dang dung.
            short[] tp = trangPhuc(d);
            msg.writer().writeShort(tp[0]);
            msg.writer().writeShort(tp[1]);
            msg.writer().writeShort(tp[2]);
            msg.writer().writeByte(d == null ? 0 : d.getGender());
            guiDoRoi(msg, (int) b.id, i);
        }
    }


    /**
     * Ghi các món rơi của một phase.
     *
     * <p>{@code roiOPhase} quyết định món nào thuộc phase nào — mỗi phase là một
     * bảng rơi riêng, đúng như tab đồ rơi trên panel.</p>
     */
    private void guiDoRoi(Message msg, int bossId, int phase) throws Exception {
        List<BossDAO.Drop> tatCa;
        try {
            tatCa = BossDAO.drops(bossId);
        } catch (Exception ignored) {
            tatCa = new ArrayList<>();
        }
        List<BossDAO.Drop> loc = new ArrayList<>();
        for (BossDAO.Drop d : tatCa) {
            if (d != null && d.active && d.roiOPhase(phase)
                    && loc.size() < TOI_DA_ROI) {
                loc.add(d);
            }
        }
        msg.writer().writeByte(loc.size());
        for (BossDAO.Drop d : loc) {
            boolean thanLinh = d.itemId == BossDAO.ITEM_THAN_LINH_NGAU_NHIEN;
            ItemTemplate t = thanLinh ? null : mau(d.itemId);
            msg.writer().writeShort(t == null ? -1 : t.iconID);
            msg.writer().writeUTF(tenMon(d, t, thanLinh));
            msg.writer().writeUTF(soLuong(d));
            msg.writer().writeUTF(phanTram(d.rateNum, d.rateDen) + "%");
            msg.writer().writeUTF(hanDung(d));
        }
    }

    /** Tên món, khớp cột "Tên vật phẩm" của bảng đồ rơi trên panel. */
    private String tenMon(BossDAO.Drop d, ItemTemplate t, boolean thanLinh) {
        if (thanLinh) {
            return "Đồ Thần Linh ngẫu nhiên";
        }
        return (t != null) ? t.name : ("#" + d.itemId);
    }

    /** Khoảng số lượng: "3" nếu cố định, "1–7" nếu bốc trong khoảng. */
    private String soLuong(BossDAO.Drop d) {
        int min = Math.min(d.qtyMin, d.qtyMax);
        int max = Math.max(d.qtyMin, d.qtyMax);
        return (min == max) ? String.valueOf(min) : (min + "–" + max);
    }

    /**
     * Hạn dùng, khớp hai cột "HSD (ngày)" và "% vĩnh viễn" của panel.
     */
    private String hanDung(BossDAO.Drop d) {
        if (d.hsdMin <= 0 || d.hsdMax <= 0) {
            return "vĩnh viễn";
        }
        String ngay = (d.hsdMin == d.hsdMax)
                ? (d.hsdMin + " ngày")
                : (d.hsdMin + "–" + d.hsdMax + " ngày");
        if (d.hsdVinhVien > 0) {
            // Mon co han, nhung mot phan so lan roi ra la vinh vien. Bo ve nay di
            // thi nguoi choi doc "7 ngay" roi nhat duoc mon vinh vien, tuong game
            // hien sai.
            return ngay + " · " + d.hsdVinhVien + "% vĩnh viễn";
        }
        return ngay;
    }

    /**
     * Đổi {@code rateNum/rateDen} thành chuỗi phần trăm.
     *
     * <p>Cùng cách viết với {@code SystemPanel.phanTramRoi} — cố ý chép lại sáu
     * dòng chứ không gọi sang đó: {@code SystemPanel} là lớp giao diện Swing, một
     * lớp dịch vụ mạng phụ thuộc vào nó là buộc cả phần chơi vào phần quản trị.
     * Sửa cách hiện thì phải sửa <b>cả hai</b>, kẻo panel và trong game nói khác
     * nhau về cùng một con số.</p>
     */
    private static String phanTram(int num, int den) {
        if (den <= 0) {
            return "0";
        }
        double pct = num * 100.0 / den;
        // Bo duoi ",0" cho so tron, giu ba chu so thap phan cho ti le hiem.
        String s = String.format(java.util.Locale.US, "%.3f", pct);
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * Các boss đáng liệt kê, theo một thứ tự <b>cố định</b>.
     *
     * <p>Bỏ con nào không tự hồi sinh được: những con phải chờ boss khác gọi ra
     * thì ghi "chưa hồi sinh" là nói sai — người chơi đứng đợi mãi không thấy.</p>
     *
     * <p><b>Không xếp con đang sống lên trước nữa.</b> Trạng thái sống/chờ đổi
     * liên tục, nên xếp theo nó là cứ mỗi nhịp làm mới (5 giây) các dòng lại đảo
     * chỗ: người chơi đang cuộn giữa danh sách thì thấy khung nhảy, và dòng đang
     * chọn trượt sang con khác. Sống hay chờ đã đọc được bằng màu tên và dòng
     * trạng thái ngay trên mỗi dòng, không cần xếp lại.</p>
     *
     * <p>Thứ tự: theo <c>id</c>, rồi theo {@code identityHashCode} của đối
     * tượng. Cần khoá thứ hai vì <b>một id có thể có nhiều bản</b> —
     * {@code boss_spawn.so_ban_sao} dựng mấy con cùng id — và
     * {@code identityHashCode} thì cố định suốt đời một đối tượng, nên hai bản
     * cùng id không bao giờ đổi chỗ nhau.</p>
     */
    private List<Boss> locBoss() {
        List<Boss> ra = new ArrayList<>();
        // Chup ban sao TRUOC khi loc.
        //
        // getBosses() tra ve chinh danh sach dang song cua BossManager, ma luong
        // boss them va xoa lien tuc. Duyet thang tren do la ConcurrentModification
        // — no bay ra giua chung, goi nay khong gui duoc gi, va nguoi choi bam
        // nut Boss thay nhu nut chet.
        List<Boss> banSao = chupDanhSach();
        for (Boss b : banSao) {
            if (b == null) {
                continue;
            }
            try {
                if (dangSong(b) || tuHoiSinhDuoc(b)) {
                    ra.add(b);
                }
            } catch (Exception boQua) {
                // Mot con doc loi thi bo rieng con do, khong bo ca danh sach.
            }
        }
        ra.sort((x, y) -> {
            int d = Integer.compare((int) x.id, (int) y.id);
            return (d != 0) ? d
                    : Integer.compare(System.identityHashCode(x),
                            System.identityHashCode(y));
        });
        return ra.size() > TOI_DA_BOSS ? ra.subList(0, TOI_DA_BOSS) : ra;
    }

    /**
     * Khoá định danh của một bản boss.
     *
     * <p>Client cần khoá này để chỉ đúng bản nào: một id có thể có nhiều bản —
     * {@code boss_spawn.so_ban_sao} dựng mấy con cùng id — nên riêng id thì
     * {@link #timBoss} luôn trả về bản đầu.</p>
     *
     * <p><b>Vì sao là {@code identityHashCode} chứ không phải số thứ tự trong
     * nhóm.</b> Số thứ tự tính theo vị trí, nên nó <b>dịch</b> ngay khi một bản
     * cùng id xuất hiện hay biến mất khỏi danh sách — và danh sách đổi liên tục
     * vì boss chết rồi hồi sinh. Client giữ con đang xem bằng số ấy thì cứ vài
     * giây lại mất dấu và nhảy về dòng đầu. {@code identityHashCode} thì cố định
     * suốt đời một đối tượng, nên bản nào cũng giữ đúng một khoá từ lúc dựng tới
     * lúc máy chủ tắt.</p>
     *
     * <p>Hai đối tượng khác nhau về lý thuyết có thể trùng mã băm; gặp trường
     * hợp đó thì {@link #timBoss} rơi về so theo id, tức xấu nhất cũng chỉ trở
     * lại nếp cũ.</p>
     */
    private int khoaCua(Boss b) {
        return System.identityHashCode(b);
    }

    /**
     * Chụp bản sao danh sách boss, thử lại một lần nếu bị sửa giữa chừng.
     *
     * <p>Thử lại đúng một lần rồi bỏ: nếu lần hai cũng trượt thì trả danh sách
     * rỗng — người chơi thấy "Chưa có boss nào" và bấm lại được, hơn là gói không
     * bao giờ tới và nút Boss trông như nút chết.</p>
     */
    private List<Boss> chupDanhSach() {
        for (int lan = 0; lan < 2; lan++) {
            try {
                return new ArrayList<>(BossManager.gI().getBosses());
            } catch (Exception dangSua) {
                // Luong boss vua them hoac xoa mot con — thu lai.
            }
        }
        Logger.error("Khong chup duoc danh sach boss, tra ve rong\n");
        return new ArrayList<>();
    }

    private boolean dangSong(Boss b) {
        if (b.bossStatus == null) {
            return false;
        }
        switch (b.bossStatus) {
            case RESPAWN:
            case JOIN_MAP:
            case CHAT_S:
            case ACTIVE:
            case AFK:
                return true;
            default:
                return false;
        }
    }

    /**
     * Con boss này có tự hồi sinh theo giờ được không.
     *
     * <p>Xét <b>phase kế tiếp</b>, chứ không phải phase đang đứng: boss nhiều
     * phase đang ở phase cuối thì lần tới nó quay về phase đầu, và chính phase
     * đầu mới quyết định nó có tự mọc lại hay phải chờ con khác gọi.</p>
     */
    private boolean tuHoiSinhDuoc(Boss b) {
        if (b.data == null || b.data.length == 0) {
            return true;
        }
        int ke = b.currentLevel + 1;
        if (ke >= b.data.length || ke < 0) {
            ke = 0;
        }
        BossData d = b.data[ke];
        return d == null || d.getTypeAppear() == TypeAppear.DEFAULT_APPEAR;
    }

    /**
     * Trạng thái của boss, cùng cách nói với tab "1. Đang chạy" trên panel.
     */
    private String trangThai(Boss b) {
        if (dangSong(b)) {
            return "đã hồi sinh";
        }
        return tuHoiSinhDuoc(b) ? "chưa hồi sinh" : "đang chờ phase trước";
    }

    /**
     * Còn bao lâu nữa boss mọc lại, hoặc rỗng nếu đang sống.
     *
     * <p>Đọc {@code lastTimeRest} và {@code secondsRest} của phase kế tiếp — đúng
     * phần tử mà {@code Boss.rest()} xét. Lấy phase đang đứng là ra sai số với
     * boss nhiều phase.</p>
     */
    private String choHoiSinh(Boss b) {
        if (dangSong(b)) {
            return "";
        }
        try {
            // Dung getSecondsUntilRespawn(): no da xet ca truong hop boss theo
            // NHOM (dung dong ho cua nhom, khong dung giay_hoi_sinh rieng). Tinh
            // tay tu lastTimeRest thi con theo nhom hien mot dang ma moc mot neo
            // — va lastTimeRest la protected, khong doc duoc tu goi nay.
            long giay = b.getSecondsUntilRespawn();
            if (giay < 0) {
                return "";
            }
            if (giay == 0) {
                return "sắp mọc";
            }
            long phut = giay / 60L;
            long du = giay % 60L;
            return phut > 0 ? (phut + " phút " + du + " giây") : (du + " giây");
        } catch (Exception boQua) {
            return "";
        }
    }

    private String tenCua(Boss b) {
        if (b.name != null && !b.name.isEmpty()) {
            return b.name;
        }
        try {
            BossData d = b.data[Math.max(0, b.currentLevel)];
            String t = d == null ? null : d.getName();
            // Ten mau co the chua %d cho so ngau nhien; thay bang dau sao cho
            // khoi hien "Ngo Khong %d" ra man hinh.
            return t == null ? ("Boss #" + b.id) : t.replace("%d", "*");
        } catch (Exception ignored) {
            return "Boss #" + b.id;
        }
    }

    /** Chỉ tên bản đồ, không có khu. */
    private String tenMap(Boss b) {
        try {
            if (b.zone == null || b.zone.map == null) {
                return "";
            }
            return b.zone.map.mapName == null ? "" : b.zone.map.mapName;
        } catch (Exception ignored) {
            return "";
        }
    }

    /** Mốc HP đầy của một phase; boss nhiều mốc thì lấy mốc lớn nhất. */
    private long hpDayCua(BossData d) {
        try {
            long[] moc = d == null ? null : d.getHp();
            if (moc == null || moc.length == 0) {
                return 0L;
            }
            long lon = moc[0];
            for (long m : moc) {
                if (m > lon) {
                    lon = m;
                }
            }
            return lon;
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private byte phanTram(long hienTai, long day) {
        if (day <= 0) {
            return 0;
        }
        long p = hienTai * 100 / day;
        if (p > 100) {
            p = 100;
        } else if (p < 0) {
            p = 0;
        }
        return (byte) p;
    }

    /**
     * Trang phục của một phase: đầu, thân, chân.
     *
     * <p>Ba số này là chỉ số <b>part</b> — cùng ba số mà
     * {@code Boss.getHead/getBody/getLeg} trả về — nên client dựng lại được
     * đúng thân người của con boss.</p>
     *
     * <p>Tra không ra thì trả về ba số -1, và client bỏ hẳn phần preview chứ
     * không vẽ một hình sai.</p>
     */
    private short[] trangPhuc(BossData d) {
        short[] ra = {-1, -1, -1};
        try {
            short[] tp = (d == null) ? null : d.getOutfit();
            if (tp != null) {
                for (int i = 0; i < 3 && i < tp.length; i++) {
                    ra[i] = tp[i];
                }
            }
        } catch (Exception ignored) {
            // Giu ba so -1.
        }
        return ra;
    }

    private ItemTemplate mau(int id) {
        try {
            return ItemService.gI().getTemplate(id);
        } catch (Exception ignored) {
            return null;
        }
    }
}
