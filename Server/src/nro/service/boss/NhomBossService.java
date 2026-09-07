package nro.service.boss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.core.util.Util;
import nro.entity.boss.Boss;
import nro.entity.map.Zone;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Hồi sinh boss theo <b>nhóm</b>, dùng một đồng hồ chung.
 *
 * <h2>Khác gì cách cũ</h2>
 *
 * <p>Bình thường mỗi con boss đếm giờ riêng kể từ lúc chính nó chết, nên ba con
 * cùng loại bị giết lệch nhau mười phút thì hồi sinh cũng lệch mười phút. Càng
 * chơi càng lệch, cuối cùng rải rác cả ngày.</p>
 *
 * <p>Nhóm thì khác: <b>đồng hồ chạy liên tục, không phụ thuộc con nào chết lúc
 * nào</b>. Cứ hết một chu kỳ là mọi con đang nằm chờ trong nhóm cùng hiện lại.
 * Nhóm đặt chu kỳ một tiếng, còn mười lăm phút nữa tới lượt mà một con bị giết,
 * thì con đó chờ đúng mười lăm phút rồi lên cùng những con kia.</p>
 *
 * <h2>Đếm theo lượt, không theo mốc thời gian</h2>
 *
 * <p>Mỗi nhóm giữ một <b>số thứ tự lượt</b>. Boss lúc nằm xuống ghi lại số lượt
 * đang chạy, và chỉ hồi sinh khi số lượt đã nhích lên.</p>
 *
 * <p>So mốc thời gian thay vì đếm lượt sẽ hỏng: các con boss được cập nhật lần
 * lượt trong một vòng lặp, không con nào chạm đúng cùng một mili giây. Con kiểm
 * tra sớm vài chục mili giây sẽ thấy "chưa tới giờ" và phải chờ nguyên một chu
 * kỳ nữa — tức là lệch nhóm, đúng cái bệnh cần chữa.</p>
 */
public final class NhomBossService {

    private static NhomBossService instance;

    public static NhomBossService gI() {
        if (instance == null) {
            instance = new NhomBossService();
        }
        return instance;
    }

    /** Một nhóm boss cùng nhịp hồi sinh. */
    public static final class Nhom {

        public int id;
        public String ten = "";
        public int chuKyGiay = 3600;
        public boolean bat = true;
        public String ghiChu = "";

        /**
         * Moi con trong nhom cung xuat hien o MOT khu cua MOT ban do.
         *
         * <p>Tat thi nhom chi dong bo gio hoi sinh — cac con van tu chon khu
         * rieng, nen Zamasu o mot noi, Black Goku o noi khac.</p>
         */
        public boolean cungKhu;

        /**
         * Khu ma con dau tien cua nhom da chon trong luot nay, va luot ung voi
         * no.
         *
         * <p>Buoc phai nho kem so luot: khong thi luot sau ca nhom van doi
         * nhau vao dung cai khu cu, ke ca khi khu do da day nguoi hay ban do da
         * bi doi tren panel.</p>
         */
        /**
         * Khu cua tung CAP, tra theo so thu tu ban sao.
         *
         * <p>Nhom 3 con A va 2 con B thi cap 0 la A#1 + B#1, cap 1 la A#2 +
         * B#2, con A#3 thanh cap 2 va di mot minh — dung nhu xep hang doi.
         * Dung mot khu chung cho ca nhom thi ba con A va hai con B don hep vao
         * mot cho, khong con ra hai cap dep nua.</p>
         *
         * <p>Nho kem so luot: sang luot moi thi bo het, ca nhom boc lai — khong
         * thi mai mai dong do o may cai khu cu, ke ca khi khu do da day nguoi
         * hay admin da doi ban do.</p>
         */
        public final transient java.util.Map<Integer, Zone> khuTheoCap =
                new java.util.HashMap<>();
        public long luotCuaKhu = -1;

        /** Số lượt đã trôi qua. Boss so số này để biết đã tới lượt chưa. */
        public long soLuot;
        /** Thời điểm bắt đầu lượt hiện tại. */
        public long mocLuot = System.currentTimeMillis();

        /** Còn bao nhiêu giây nữa tới lượt kế tiếp. */
        public long conLaiGiay() {
            long troiQua = (System.currentTimeMillis() - mocLuot) / 1000;
            return Math.max(0, chuKyGiay - troiQua);
        }
    }

    private final Map<Integer, Nhom> nhomTheoId = new HashMap<>();
    /** Boss id thuộc nhóm nào. Không có trong bảng nghĩa là không theo nhóm. */
    private final Map<Integer, Integer> nhomCuaBoss = new HashMap<>();

    private NhomBossService() {
    }

    /**
     * Đọc lại bảng nhóm.
     *
     * <p>Giữ nguyên số lượt và mốc lượt của nhóm đã có: nạp lại vì admin vừa
     * đổi tên hay ghi chú mà lại tua đồng hồ về đầu thì mọi con đang chờ bị
     * đẩy lùi thêm nguyên một chu kỳ.</p>
     */
    public synchronized void napLai() {
        Map<Integer, Nhom> moi = new HashMap<>();
        Map<Integer, Integer> cuaBoss = new HashMap<>();
        CrisResultSet rs = null;
        try {
            damBaoBang();
            rs = ConnectDB.executeQuery("SELECT id, ten, chu_ky_giay, bat, ghi_chu,"
                    + " cung_khu FROM boss_nhom");
            while (rs.next()) {
                Nhom n = new Nhom();
                n.id = rs.getInt("id");
                n.ten = rs.getString("ten");
                n.chuKyGiay = rs.getInt("chu_ky_giay");
                n.bat = rs.getBoolean("bat");
                n.ghiChu = rs.getString("ghi_chu");
                n.cungKhu = rs.getInt("cung_khu") != 0;
                Nhom cu = nhomTheoId.get(n.id);
                if (cu != null) {
                    n.soLuot = cu.soLuot;
                    n.mocLuot = cu.mocLuot;
                    n.khuTheoCap.putAll(cu.khuTheoCap);
                    n.luotCuaKhu = cu.luotCuaKhu;
                }
                moi.put(n.id, n);
            }
            rs.dispose();
            rs = ConnectDB.executeQuery(
                    "SELECT boss_id, nhom_id FROM boss_spawn WHERE nhom_id > 0");
            while (rs.next()) {
                cuaBoss.put(rs.getInt("boss_id"), rs.getInt("nhom_id"));
            }
        } catch (Exception ex) {
            Logger.logException(NhomBossService.class, ex,
                    "Không đọc được bảng nhóm boss — boss hồi sinh theo giờ riêng");
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                    // Dong khong duoc thi thoi.
                }
            }
        }
        nhomTheoId.clear();
        nhomTheoId.putAll(moi);
        nhomCuaBoss.clear();
        nhomCuaBoss.putAll(cuaBoss);
    }

    public static void damBaoBang() throws Exception {
        ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS boss_nhom ("
                + " id INT(11) NOT NULL AUTO_INCREMENT,"
                + " ten VARCHAR(100) NOT NULL DEFAULT '',"
                + " chu_ky_giay INT(11) NOT NULL DEFAULT 3600,"
                + " bat TINYINT(1) NOT NULL DEFAULT 1,"
                + " ghi_chu VARCHAR(255) NOT NULL DEFAULT '',"
                + " PRIMARY KEY (id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        ConnectDB.executeUpdate("ALTER TABLE boss_nhom ADD COLUMN IF NOT EXISTS"
                + " cung_khu INT(11) NOT NULL DEFAULT 1");
        ConnectDB.executeUpdate("ALTER TABLE boss_spawn ADD COLUMN IF NOT EXISTS"
                + " nhom_id INT(11) NOT NULL DEFAULT 0");
    }

    /** Nhóm của một boss, hoặc {@code null} nếu con này không theo nhóm nào. */
    public synchronized Nhom nhomCua(int bossId) {
        Integer id = nhomCuaBoss.get(bossId);
        if (id == null) {
            return null;
        }
        Nhom n = nhomTheoId.get(id);
        return n != null && n.bat && n.chuKyGiay > 0 ? n : null;
    }

    public synchronized List<Nhom> tatCa() {
        return new ArrayList<>(nhomTheoId.values());
    }

    /**
     * Nhích đồng hồ của mọi nhóm. Gọi trong vòng cập nhật boss.
     *
     * <p>Chu kỳ trôi qua nhiều lượt cùng lúc — máy chủ vừa lag chẳng hạn — thì
     * <b>nhảy thẳng tới lượt hiện tại</b> chứ không cộng dồn từng lượt: cộng
     * dồn chỉ tốn vòng lặp mà kết quả y hệt, vì mọi con đang chờ đều lên ở lượt
     * gần nhất.</p>
     */
    public synchronized void capNhat() {
        long now = System.currentTimeMillis();
        for (Nhom n : nhomTheoId.values()) {
            if (!n.bat || n.chuKyGiay <= 0) {
                continue;
            }
            long mot = n.chuKyGiay * 1000L;
            long troiQua = now - n.mocLuot;
            if (troiQua >= mot) {
                long soLuotMoi = troiQua / mot;
                n.soLuot += soLuotMoi;
                n.mocLuot += soLuotMoi * mot;
            }
        }
    }

    /**
     * Boss này đã tới lượt hồi sinh chưa.
     *
     * <p>Không theo nhóm thì trả về giờ riêng như cũ, để mọi con boss còn lại
     * chạy y hệt trước khi có tính năng này.</p>
     */
    public boolean denLuotHoiSinh(Boss b, long lastTimeRest, int secondsRest) {
        Nhom n = nhomCua((int) b.id);
        if (n == null) {
            return Util.canDoWithTime(lastTimeRest, secondsRest * 1000L);
        }
        return n.soLuot > b.luotNhomLucNghi;
    }

    /**
     * Số thứ tự bản sao của một con boss trong loại của nó — cũng là <b>số
     * cặp</b> của nó.
     *
     * <p>Nhóm khai 3 con A và 2 con B thì A đánh số 0,1,2 và B đánh số 0,1.
     * Ghép theo số này ra cặp 0 = A#1+B#1, cặp 1 = A#2+B#2, còn A#3 thành cặp 2
     * đứng một mình — đúng như xếp hàng đôi, dư ai người đó đi lẻ.</p>
     *
     * <p>Các bản sao cùng loại dùng chung {@code id}, nên phân biệt bằng
     * <b>vị trí</b> trong danh sách chứ không bằng id. Danh sách giữ nguyên thứ
     * tự tạo nên số này ổn định suốt phiên chạy.</p>
     */
    private static int soCap(Boss b) {
        if (b == null) {
            return 0;
        }
        int i = 0;
        for (Boss x : Boss.tatCaBoss()) {
            if (x == null || x.id != b.id) {
                continue;
            }
            if (x == b) {
                return i;
            }
            i++;
        }
        return 0;
    }

    /**
     * Khu mà con này phải vào để đứng cùng bạn cặp của nó, hoặc {@code null}.
     *
     * <p>Con nào của cặp lên map trước thì tự chọn khu như thường rồi báo lại
     * bằng {@link #ghiKhuChung}; con còn lại của <b>đúng cặp đó</b> nhận lại
     * khu này. Cặp khác vẫn bốc khu riêng, nên ba con A và hai con B không dồn
     * hết vào một chỗ.</p>
     *
     * <p>Trả {@code null} khi: con này không theo nhóm nào, nhóm không bật
     * "cùng khu", hoặc nó là con đầu tiên của cặp trong lượt này.</p>
     */
    public synchronized Zone khuChung(Boss b) {
        if (b == null) {
            return null;
        }
        Nhom n = nhomCua((int) b.id);
        if (n == null || !n.cungKhu) {
            return null;
        }
        if (n.luotCuaKhu != n.soLuot) {
            // Luot moi -> khu cua luot truoc khong con gia tri.
            n.khuTheoCap.clear();
            return null;
        }
        Zone z = n.khuTheoCap.get(soCap(b));
        // Khu co the da bi don di giua chung (doi ban do, tai lai du lieu).
        if (z == null || z.map == null) {
            return null;
        }
        return z;
    }

    /**
     * Con đầu tiên của một cặp báo lại khu nó vừa chọn.
     *
     * <p>Gọi <b>sau</b> khi boss đã chốt khu. Con không theo nhóm, hoặc nhóm
     * không bật "cùng khu", thì hàm này không làm gì — nên gọi vô điều kiện ở
     * chỗ boss lên map là được.</p>
     */
    public synchronized void ghiKhuChung(Boss b, Zone z) {
        if (b == null || z == null || z.map == null) {
            return;
        }
        Nhom n = nhomCua((int) b.id);
        if (n == null || !n.cungKhu) {
            return;
        }
        if (n.luotCuaKhu != n.soLuot) {
            n.khuTheoCap.clear();
            n.luotCuaKhu = n.soLuot;
        }
        // Da co con khac cua cap nay chot truoc roi -> giu nguyen.
        n.khuTheoCap.putIfAbsent(soCap(b), z);
    }

    /**
     * Bỏ thời gian chờ của <b>mọi</b> con trong một nhóm, kể cả mọi bản sao.
     *
     * <p>Xoá luôn khu của lượt cũ để cả nhóm bốc lại từ đầu — không thì các cặp
     * lại chui về đúng mấy khu vừa rồi.</p>
     *
     * @return số con vừa được gọi dậy, và danh sách lý do của những con không gọi được
     */
    public synchronized int[] hoiSinhCaNhom(int nhomId, java.util.List<String> loi) {
        Nhom n = nhomTheoId.get(nhomId);
        if (n == null) {
            return new int[]{0, 0};
        }
        n.khuTheoCap.clear();
        n.luotCuaKhu = -1;
        int xong = 0;
        int hong = 0;
        for (Boss b : Boss.tatCaBoss()) {
            if (b == null) {
                continue;
            }
            Integer cua = nhomCuaBoss.get((int) b.id);
            if (cua == null || cua != nhomId) {
                continue;
            }
            // Boss theo nhom KHONG dung dong ho rieng — no cho "so luot cua nhom
            // lon hon so luot luc no nam xuong". Xoa lastTimeRest (viec ma
            // hoiSinhNgay lam) khong dong den dieu kien do, nen phai ha moc
            // luot cua tung con xuong mot bac thi cong moi mo.
            b.luotNhomLucNghi = n.soLuot - 1;
            String l = b.hoiSinhNgay();
            if (l == null) {
                xong++;
            } else {
                hong++;
                if (loi != null && loi.size() < 8) {
                    String ten = b.name != null && !b.name.isEmpty()
                            ? b.name : ("#" + b.id);
                    loi.add(ten + ": " + l);
                }
            }
        }
        return new int[]{xong, hong};
    }

    /**
     * Mở cổng chờ của nhóm cho <b>một</b> con boss.
     *
     * <p>Boss theo nhóm không đếm giờ riêng — nó chờ {@code soLuot} của nhóm
     * vượt qua mốc lúc nó nằm xuống. Nút "Hồi sinh ngay" chỉ xoá đồng hồ riêng
     * nên với con theo nhóm là bấm xong vẫn nằm im, mà panel lại báo đã gọi
     * dậy. Hạ mốc xuống một bậc thì điều kiện mở ra ngay.</p>
     *
     * <p>Con không theo nhóm thì hàm này không làm gì.</p>
     */
    public synchronized void moCongNhom(Boss b) {
        if (b == null) {
            return;
        }
        Nhom n = nhomCua((int) b.id);
        if (n != null) {
            b.luotNhomLucNghi = n.soLuot - 1;
        }
    }
    /** Ghi lại số lượt đang chạy vào lúc boss nằm xuống. */
    public void ghiLuotLucNghi(Boss b) {
        Nhom n = nhomCua((int) b.id);
        b.luotNhomLucNghi = n == null ? 0 : n.soLuot;
    }
}
