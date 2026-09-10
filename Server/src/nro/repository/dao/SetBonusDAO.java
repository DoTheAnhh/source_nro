package nro.repository.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.entity.player.SetClothes;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Chỉ số của <b>set kích hoạt</b>, chỉnh được từ panel.
 *
 * <h2>Trước đây các chỉ số này ở đâu</h2>
 *
 * <p>Viết cứng rải rác trong {@code NPoint.java} — khoảng hai mươi khối kiểu:</p>
 *
 * <pre>{@code
 * if (this.player.setClothes.cumber == 5) {
 *     dame += calPercent(dame, 20);
 * }
 * }</pre>
 *
 * <p>Muốn đổi "+20% sức đánh" thành thứ khác thì phải sửa mã nguồn rồi biên dịch
 * lại, và phải tìm cho hết vì cùng một set nằm ở nhiều chỗ khác nhau.</p>
 *
 * <h2>Hai bảng</h2>
 *
 * <ul>
 *   <li>{@code set_bonus} — từng dòng chỉ số: set nào, mặc mấy món, loại gì,
 *       bao nhiêu.</li>
 * </ul>
 *
 * <p>Nhờ cờ đó mới làm được đúng việc người dùng muốn: đang "+100% HP" đổi sang
 * "+100% sức đánh" chứ không phải được cả hai.</p>
 *
 * <h2>Bộ nhớ đệm</h2>
 *
 * <p>Đọc trong {@code NPoint.calPoint()} — hàm chạy mỗi lần người chơi thay đồ,
 * ăn buff, vào map. Truy vấn CSDL ở đó sẽ giết máy chủ. Panel gọi
 * {@link #reload()} sau khi sửa nên đổi có hiệu lực ngay ở lần tính chỉ số kế
 * tiếp.</p>
 */
public class SetBonusDAO {

    private SetBonusDAO() {
    }

    // =====================================================================
    //  Loại chỉ số
    // =====================================================================

    /**
     * Các loại chỉ số một dòng có thể đặt.
     *
     * <p>Hậu tố {@code _pct} là phần trăm tính trên giá trị <b>đã có</b>; không
     * hậu tố là cộng thẳng.</p>
     */
    public static final Map<String, String> LOAI = new LinkedHashMap<>();

    static {
        LOAI.put("hp", "HP + (cộng thẳng)");
        LOAI.put("hp_pct", "HP + %");
        LOAI.put("ki", "KI + (cộng thẳng)");
        LOAI.put("ki_pct", "KI + %");
        LOAI.put("dame", "Sức đánh + (cộng thẳng)");
        LOAI.put("dame_pct", "Sức đánh + %");
        LOAI.put("def", "Giáp + (cộng thẳng)");
        LOAI.put("def_pct", "Giáp + %");
        LOAI.put("crit", "Chí mạng + % (điểm)");
        LOAI.put("sdcm", "Sức đánh chí mạng + %");
        LOAI.put("chi_mang_ca_hai",
                "Chí mạng: cộng cùng lúc CẢ tỉ lệ lẫn sát thương + % (một số cho cả hai)");
        LOAI.put("giam_tieu_hao_pct",
                "Giảm HP/KI tiêu hao khi dùng kỹ năng + % (tối đa 90)");
        LOAI.put("hp_hoi", "HP hồi + (cộng thẳng)");
        LOAI.put("ne_don", "Né đòn + %");
        LOAI.put("pst", "Phản sát thương + %");
        LOAI.put("hut_hp", "Hút HP + %");
        LOAI.put("hut_ki", "Hút KI + %");
        LOAI.put("skill_pct", "Sát thương một chiêu + % (điền id chiêu vào Tham số)");
        LOAI.put("lam_moi_pct",
                "Tỉ lệ làm mới một chiêu sau khi dùng % (điền id chiêu vào Tham số)");
        LOAI.put("may_man", "May mắn + %");
        LOAI.put("gold_pct", "Vàng rơi + %");
        LOAI.put("tiem_nang_pct", "Tiềm năng nhận được + %");
        LOAI.put("qckk_pct", "Sát thương quả cầu ki + %");
        LOAI.put("danh_thuong_pct", "Sát thương đánh thường + %");
        LOAI.put("dame_mob_pct", "Sát thương lên quái + %");
        LOAI.put("giap_pct", "Giảm sát thương nhận vào + % (giáp)");
        LOAI.put("chinh_xac", "Chính xác + % (xuyên né đòn)");
        LOAI.put("xuyen_giap", "Xuyên giáp + %");
        LOAI.put("xuyen_giap_cm", "Xuyên giáp khi chí mạng + %");
        LOAI.put("bom_pct", "Sát thương Tự sát + %");
        LOAI.put("hp_hoi_pct", "HP hồi mỗi 30 giây + % (tối đa 100)");
        LOAI.put("ki_hoi_pct", "KI hồi mỗi 30 giây + % (tối đa 100)");
        LOAI.put("fix_stun", "Giảm thời gian bị choáng + % (khi đang bật khiên)");
        LOAI.put("tai_tao_pct", "Hồi phục từ Tái tạo năng lượng + %");
        LOAI.put("hoi_chieu_pct", "Giảm thời gian hồi chiêu + %");
        LOAI.put("choang_pct", "Thời gian choáng Dịch chuyển tức thời + %");
        LOAI.put("choang_tdhs_pct", "Thời gian choáng Thái Dương Hạ San + %");
        LOAI.put("ne_don_pct", "Né đòn + % (bằng ne_don, tên rõ hơn)");
    }

    /**
     * Diễn một dòng chỉ số thành câu đọc được.
     *
     * <p>Bảng {@link #LOAI} viết cho người <i>đang điền form</i> nên có cả lời
     * dặn ("điền id chiêu vào Tham số"). Câu ở đây viết cho người <i>đang đọc
     * xem set làm gì</i>, nên bỏ lời dặn và ghép luôn giá trị.</p>
     *
     * <p>Ví dụ: {@code "5 món: +100% sát thương chiêu Kamejoko"}.</p>
     */
    public static String moTaChiSo(int soMon, String loai, long giaTri, int thamSo) {
        String dau = giaTri >= 0 ? "+" : "";
        String than;
        switch (loai == null ? "" : loai) {
            case "hp": than = dau + giaTri + " HP"; break;
            case "hp_pct": than = dau + giaTri + "% HP"; break;
            case "ki": than = dau + giaTri + " KI"; break;
            case "ki_pct": than = dau + giaTri + "% KI"; break;
            case "dame": than = dau + giaTri + " sức đánh"; break;
            case "dame_pct": than = dau + giaTri + "% sức đánh"; break;
            case "def": than = dau + giaTri + " giáp"; break;
            case "def_pct": than = dau + giaTri + "% giáp"; break;
            case "crit": than = dau + giaTri + "% tỷ lệ chí mạng"; break;
            case "sdcm": than = dau + giaTri + "% sát thương chí mạng"; break;
            case "chi_mang_ca_hai":
                than = dau + giaTri + "% tỷ lệ và " + dau + giaTri + "% sát thương chí mạng";
                break;
            case "giam_tieu_hao_pct":
                than = dau + giaTri + "% giảm HP/KI tiêu hao khi dùng kỹ năng";
                break;
            case "hp_hoi": than = dau + giaTri + " HP hồi"; break;
            case "ne_don": than = dau + giaTri + "% né đòn"; break;
            case "pst": than = dau + giaTri + "% phản sát thương"; break;
            case "hut_hp": than = dau + giaTri + "% hút HP"; break;
            case "hut_ki": than = dau + giaTri + "% hút KI"; break;
            case "may_man": than = dau + giaTri + "% may mắn"; break;
            case "gold_pct": than = dau + giaTri + "% vàng rơi"; break;
            case "tiem_nang_pct": than = dau + giaTri + "% tiềm năng nhận được"; break;
            case "qckk_pct": than = dau + giaTri + "% sát thương quả cầu ki"; break;
            case "danh_thuong_pct": than = dau + giaTri + "% sát thương đánh thường"; break;
            case "dame_mob_pct": than = dau + giaTri + "% sát thương lên quái"; break;
            case "giap_pct": than = dau + giaTri + "% giáp (giảm sát thương nhận)"; break;
            case "chinh_xac": than = dau + giaTri + "% chính xác"; break;
            case "xuyen_giap": than = dau + giaTri + "% xuyên giáp"; break;
            case "xuyen_giap_cm": than = dau + giaTri + "% xuyên giáp khi chí mạng"; break;
            case "bom_pct": than = dau + giaTri + "% sát thương Tự sát"; break;
            case "hp_hoi_pct": than = dau + giaTri + "% HP hồi mỗi 30 giây"; break;
            case "ki_hoi_pct": than = dau + giaTri + "% KI hồi mỗi 30 giây"; break;
            case "fix_stun": than = dau + giaTri + "% giảm thời gian bị choáng"; break;
            case "tai_tao_pct": than = dau + giaTri + "% hồi phục từ Tái tạo năng lượng"; break;
            case "hoi_chieu_pct": than = dau + giaTri + "% giảm thời gian hồi chiêu"; break;
            case "choang_pct":
                than = dau + giaTri + "% thời gian choáng Dịch chuyển tức thời";
                break;
            case "choang_tdhs_pct":
                than = dau + giaTri + "% thời gian choáng Thái Dương Hạ San";
                break;
            case "ne_don_pct": than = dau + giaTri + "% né đòn"; break;
            case "skill_pct":
                than = dau + giaTri + "% sát thương chiêu "
                        + CHIEU.getOrDefault(thamSo, "id " + thamSo);
                break;
            case "lam_moi_pct":
                than = dau + giaTri + "% làm mới chiêu "
                        + CHIEU.getOrDefault(thamSo, "id " + thamSo);
                break;
            default:
                than = dau + giaTri + " " + LOAI.getOrDefault(loai, String.valueOf(loai));
                break;
        }
        return soMon + " món: " + than;
    }

    /**
     * Tên các chiêu, để panel hiện ô chọn thay vì bắt nhớ id.
     *
     * <p>Đủ <b>cả 25 kỹ năng của cả ba hành tinh</b> cộng nhóm dùng chung, chép
     * từ {@code nro.entity.skill.Skill} — bảng cũ chỉ có 9 chiêu, thiếu cả
     * những chiêu đang có tác dụng như Tự sát và Dịch chuyển tức thời.</p>
     *
     * <p>Phần trăm đặt cho chiêu nào cũng chạy: nó được cộng vào <b>sau</b> khối
     * {@code switch} trong {@code NPoint.getDamage()}, tức trên đường tính chung
     * của mọi kỹ năng, chứ không phải liệt kê tay từng chiêu như trước.</p>
     *
     * <p><b>Chiêu không gây sát thương</b> (trị thương, khiên, biến khỉ, thôi
     * miên…) vẫn nằm trong danh sách cho đủ, nhưng đặt phần trăm sát thương cho
     * chúng thì không có gì để nhân — nhãn có ghi rõ.</p>
     */
    public static final Map<Integer, String> CHIEU = new LinkedHashMap<>();

    static {
        // --- Trái Đất
        CHIEU.put(0, "Trái Đất — Dragon");
        CHIEU.put(1, "Trái Đất — Kamejoko");
        CHIEU.put(6, "Trái Đất — Thái Dương Hạ San");
        CHIEU.put(9, "Trái Đất — Kaioken");
        CHIEU.put(10, "Trái Đất — Quả Cầu Kênh Khí");
        CHIEU.put(20, "Trái Đất — Dịch Chuyển Tức Thời");
        CHIEU.put(22, "Trái Đất — Thôi Miên (không sát thương)");
        // --- Namếc
        CHIEU.put(2, "Namếc — Demon");
        CHIEU.put(3, "Namếc — Masenko");
        CHIEU.put(7, "Namếc — Trị Thương (không sát thương)");
        CHIEU.put(11, "Namếc — Makankosappo");
        CHIEU.put(12, "Namếc — Đẻ Trứng");
        CHIEU.put(17, "Namếc — Liên Hoàn");
        CHIEU.put(18, "Namếc — Sôcôla (không sát thương)");
        // --- Xayda
        CHIEU.put(4, "Xayda — Galick");
        CHIEU.put(5, "Xayda — Antomic");
        CHIEU.put(8, "Xayda — Tái Tạo Năng Lượng (không sát thương)");
        CHIEU.put(13, "Xayda — Biến Khỉ (không sát thương)");
        CHIEU.put(14, "Xayda — Tự Sát");
        CHIEU.put(21, "Xayda — Huýt Sáo (không sát thương)");
        CHIEU.put(23, "Xayda — Trói (không sát thương)");
        // --- Dùng chung
        CHIEU.put(19, "Chung — Khiên Năng Lượng (không sát thương)");
        CHIEU.put(24, "Chung — Super Kame");
        CHIEU.put(25, "Chung — Liên Hoàn Chưởng");
        CHIEU.put(26, "Chung — Ma Phong Ba");
    }

    // =====================================================================
    //  Dữ liệu
    // =====================================================================

    /** Một dòng chỉ số của set. */
    public static final class Bonus {

        public int id;
        public String setKey;
        public int soMon = 5;
        public String loai;
        public long giaTri;
        /**
         * Tham số phụ. Với {@code skill_pct} đây là <b>id chiêu</b>
         * (0 Dragon, 1 Kamejoko, 2 Demon, 3 Masenko, 4 Galick, 5 Antomic…).
         */
        public int thamSo;
        public boolean active = true;
        public String ghiChu;
    }

    private static final Map<String, List<Bonus>> THEO_SET = new HashMap<>();
    private static volatile boolean loaded;

    /** Đọc lại cả hai bảng. */
    public static void reload() {
        nro.repository.schema.LuocDoPanel.damBao();
        Map<String, List<Bonus>> ds = new HashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM set_bonus WHERE active = 1");
            while (rs.next()) {
                Bonus b = new Bonus();
                b.id = rs.getInt("id");
                b.setKey = rs.getString("set_key");
                b.soMon = rs.getInt("so_mon");
                b.loai = rs.getString("loai");
                b.giaTri = rs.getLong("gia_tri");
                b.thamSo = rs.getInt("tham_so");
                b.active = true;
                b.ghiChu = rs.getStringOrNull("ghi_chu");
                ds.computeIfAbsent(b.setKey, k -> new ArrayList<>()).add(b);
            }
        } catch (Exception ex) {
            Logger.logException(SetBonusDAO.class, ex, "Không đọc được set_bonus");
        } finally {
            dispose(rs);
        }


        synchronized (THEO_SET) {
            THEO_SET.clear();
            THEO_SET.putAll(ds);
        }
        // Đặt cờ TRƯỚC khi nạp định nghĩa: napDinhNghia() không gọi ensureLoaded()
        // nên không đệ quy, nhưng đặt trước cho chắc.
        loaded = true;
        napDinhNghia();
        apDungNgayChoNguoiOnline();
    }

    /**
     * Tính lại chỉ số cho <b>mọi người chơi đang online</b> ngay lập tức.
     *
     * <h2>Vì sao cần</h2>
     *
     * <p>{@link #reload()} chỉ làm mới bảng trong bộ nhớ. Chỉ số thật của người
     * chơi thì nằm trong {@code NPoint} và chỉ đổi khi {@code calPoint()} chạy —
     * mà nó chỉ chạy lúc mặc/cởi đồ, đăng nhập, hay đổi bản đồ. Nên sửa một dòng
     * chỉ số set trên panel xong, người đang mặc set đó <b>vẫn giữ chỉ số cũ</b>
     * cho tới khi họ tình cờ làm một trong ba việc kia. Nhìn từ ngoài là "sửa
     * xong không thấy gì đổi".</p>
     *
     * <p>Gọi thẳng trong {@code reload()} nên mọi đường sửa trên panel — thêm
     * dòng, sửa dòng, xoá dòng, bật/tắt set — đều được áp ngay, không phải nhớ
     * gọi thêm ở từng nút.</p>
     *
     * <h2>Ghi chú</h2>
     *
     * <p>Chỉ tính lại, <b>không hồi máu</b>. Nhưng nếu máu tối đa vừa tụt xuống
     * dưới máu hiện tại thì phải kéo về bằng trần, không thì thanh máu hiện
     * "12/10" và mọi phép so sánh về sau đều lệch.</p>
     *
     * <p>Chạy từ luồng của panel chứ không phải luồng game. Chấp nhận được vì
     * {@code calPoint()} chỉ đọc trang bị rồi ghi vào chính người chơi đó, và
     * danh sách người chơi lấy bằng bản chụp nên không vỡ giữa chừng.</p>
     */
    public static void apDungNgayChoNguoiOnline() {
        int soNguoi = 0;
        try {
            for (nro.entity.player.Player pl
                    : nro.server.Client.gI().getPlayersSnapshot()) {
                if (pl == null || pl.nPoint == null || !pl.isPl()) {
                    continue;
                }
                try {
                    pl.nPoint.calPoint();
                    if (pl.nPoint.hp > pl.nPoint.hpMax) {
                        pl.nPoint.setHp(pl.nPoint.hpMax);
                    }
                    if (pl.nPoint.mp > pl.nPoint.mpMax) {
                        pl.nPoint.setMp(pl.nPoint.mpMax);
                    }
                    nro.service.Service.gI().point(pl);
                    nro.service.PlayerService.gI().sendInfoHpMp(pl);
                    soNguoi++;
                } catch (Exception boQua) {
                    // Một người hỏng thì không được kéo cả vòng lặp xuống theo.
                }
            }
            if (soNguoi > 0) {
                Logger.log(Logger.GREEN, "Đã áp lại chỉ số set cho "
                        + soNguoi + " người đang online\n");
            }
        } catch (Exception ex) {
            // Nạp bảng lúc khởi động chạy TRƯỚC khi có danh sách người chơi —
            // lúc đó hàm này không có việc gì làm, và đó là chuyện bình thường.
            Logger.logException(SetBonusDAO.class, ex,
                    "Không áp lại được chỉ số set cho người đang online");
        }
    }

    private static void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }

    /**
     * Set này có bật chế độ thay thế không.
     *
     * <p>{@code NPoint} gọi hàm này ngay trong điều kiện của từng khối viết cứng:
     * bật thì khối đó không chạy.</p>
     */

    // =====================================================================
    //  Áp dụng
    // =====================================================================

    /**
     * Số món đang mặc của một set, đọc bằng phản chiếu.
     *
     * <p>{@link SetClothes} khai báo mỗi set một trường {@code public byte}; đọc
     * bằng phản chiếu thì thêm set mới vào lớp đó là panel và phần áp dụng tự
     * biết, không phải sửa ở đây.</p>
     */
    /**
     * Số món của một set mà người chơi đang mặc.
     *
     * <p>Chỉ đọc {@code setCauHinh} — bản đếm của các set định nghĩa trên panel.
     * Hệ set viết cứng đã bỏ hẳn nên không còn tra ngược vào trường Java của
     * {@code SetClothes} nữa; các trường đó vẫn còn trong lớp nhưng luôn bằng 0
     * và không ai cập nhật chúng.</p>
     */
    public static int soMonDangMac(SetClothes sc, String setKey) {
        if (sc == null) {
            return 0;
        }
        Integer n = sc.setCauHinh.get(setKey);
        return n == null ? 0 : n;
    }

    /** Nhãn cho set chưa xếp được vào hành tinh nào. */
    public static final String KHAC = "Khác";

    /** Các hành tinh, theo thứ tự muốn hiện trên panel. */
    public static final String[] CAC_HANH_TINH = {"Trái Đất", "Namếc", "Xayda", KHAC};

    /**
     * Hành tinh của một set.
     *
     * <p>Đọc từ cột {@code hanh_tinh} của bảng {@code set_kich_hoat} — tức do
     * người tạo set đặt. Hệ set viết cứng cũ đã bỏ nên không còn bảng tra nào
     * để suy ra hành tinh nữa.</p>
     */
    public static String hanhTinh(String setKey) {
        DinhNghia d = dinhNghia().get(setKey);
        if (d == null || d.hanhTinh == null || d.hanhTinh.trim().isEmpty()) {
            return KHAC;
        }
        return d.hanhTinh.trim();
    }

    /**
     * Mã của mọi set — <b>chỉ những set định nghĩa trên panel</b>.
     *
     * <p>Hệ set viết cứng cũ đã bỏ hẳn: không còn bảng tra chỉ số gốc, không còn
     * đếm số món của chúng, nên các
     * khối hiệu ứng cũ trong {@code NPoint} không bao giờ chạy.</p>
     */
    public static List<String> cacSet() {
        return new ArrayList<>(dinhNghia().keySet());
    }

    /** Tên hiển thị của một set, hoặc chính mã nếu chưa đặt tên. */
    public static String tenSet(String setKey) {
        DinhNghia d = dinhNghia().get(setKey);
        return d != null && d.ten != null && !d.ten.isEmpty() ? d.ten : setKey;
    }

    /** Các dòng chỉ số đang bật của một set. */
    public static List<Bonus> bonusCua(String setKey) {
        ensureLoaded();
        List<Bonus> l;
        synchronized (THEO_SET) {
            l = THEO_SET.get(setKey);
        }
        return l == null ? java.util.Collections.emptyList() : l;
    }

    /** Toàn bộ dòng, kể cả dòng tắt — dùng cho panel. */
    public static List<Bonus> tatCa() {
        List<Bonus> out = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM set_bonus ORDER BY set_key, so_mon, id");
            while (rs.next()) {
                Bonus b = new Bonus();
                b.id = rs.getInt("id");
                b.setKey = rs.getString("set_key");
                b.soMon = rs.getInt("so_mon");
                b.loai = rs.getString("loai");
                b.giaTri = rs.getLong("gia_tri");
                b.thamSo = rs.getInt("tham_so");
                b.active = rs.getBoolean("active");
                b.ghiChu = rs.getStringOrNull("ghi_chu");
                out.add(b);
            }
        } catch (Exception ex) {
            Logger.logException(SetBonusDAO.class, ex, "Lỗi đọc set_bonus");
        } finally {
            dispose(rs);
        }
        return out;
    }

    public static String luu(Bonus b) {
        if (b.setKey == null || b.setKey.trim().isEmpty()) {
            return "Chưa chọn set.";
        }
        if (!LOAI.containsKey(b.loai)) {
            return "Loại chỉ số không hợp lệ.";
        }
        if (b.soMon < 1) {
            return "Số món phải từ 1 trở lên.";
        }
        try {
            if (b.id > 0) {
                ConnectDB.executeUpdate(
                        "UPDATE set_bonus SET set_key = ?, so_mon = ?, loai = ?, gia_tri = ?,"
                        + " tham_so = ?, active = ?, ghi_chu = ? WHERE id = ?",
                        b.setKey, b.soMon, b.loai, b.giaTri, b.thamSo,
                        b.active ? 1 : 0, b.ghiChu, b.id);
            } else {
                ConnectDB.executeUpdate(
                        "INSERT INTO set_bonus (set_key, so_mon, loai, gia_tri, tham_so,"
                        + " active, ghi_chu) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        b.setKey, b.soMon, b.loai, b.giaTri, b.thamSo,
                        b.active ? 1 : 0, b.ghiChu);
            }
            reload();
            return null;
        } catch (Exception ex) {
            Logger.logException(SetBonusDAO.class, ex, "Lỗi lưu set_bonus");
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    public static boolean xoa(int id) {
        try {
            boolean ok = ConnectDB.executeUpdate("DELETE FROM set_bonus WHERE id = ?", id) == 1;
            reload();
            return ok;
        } catch (Exception ex) {
            Logger.logException(SetBonusDAO.class, ex, "Lỗi xoá set_bonus " + id);
            return false;
        }
    }


    // =====================================================================
    //  Set tự định nghĩa
    // =====================================================================

    /**
     * Một set do admin tự đặt ra.
     *
     * <p>Set trong game <b>không nhận diện bằng id vật phẩm</b> mà bằng
     * <i>option gắn trên vật phẩm</i>: {@code SetClothes.setupSetTuCauHinh()} duyệt năm ô
     * trang bị, thấy option 129 hoặc 141 thì cộng một món cho set Sôngôku. Nên
     * định nghĩa một set mới chỉ cần khai: những option nào tính là set này.</p>
     */
    public static final class DinhNghia {

        public String setKey;
        public String ten;
        /** Các option id kích hoạt set, ngăn nhau bằng dấu phẩy. */
        public String optionIds;
        public boolean active = true;
        public String ghiChu;
        /** Hành tinh admin chọn cho set này. Rỗng nghĩa là "Khác". */
        public String hanhTinh;
        /**
         * Các dòng chữ in trên món đồ, mỗi mốc số món một dòng.
         *
         * <p>Dạng {@code "2:273,4:274,5:275"} — mốc số món ứng với id chỉ số
         * giữ dòng chữ đó.</p>
         *
         * <p>Dòng chữ hiện trên món đồ phải là một chỉ số thật trong
         * {@code item_option_template}: client chỉ biết in tên chỉ số, không đọc
         * được bảng set. Nên mỗi dòng mô tả được cất vào một chỉ số riêng tên
         * {@code $[n] chữ}, và id ghi ở đây. Sửa chữ lần sau là <b>đổi tên</b>
         * chính chỉ số đó, id giữ nguyên — món đồ người chơi đang cầm cũng đổi
         * chữ theo, không phải phát lại đồ.</p>
         */
        public String moTaDong;
    }

    /**
     * Thêm cột {@code hanh_tinh} nếu bảng chưa có.
     *
     * <p>Bảng {@code set_kich_hoat} đã tồn tại trước khi có bộ lọc hành tinh.
     * Dùng {@code ADD COLUMN IF NOT EXISTS} nên gọi lại không sao.</p>
     */
    private static volatile boolean daVaCotHanhTinh;

    private static void vaCotHanhTinh() {
        if (daVaCotHanhTinh) {
            return;
        }
        daVaCotHanhTinh = true;
        try {
            ConnectDB.executeUpdate("ALTER TABLE set_kich_hoat ADD COLUMN IF NOT EXISTS"
                    + " hanh_tinh VARCHAR(20) NULL");
            ConnectDB.executeUpdate("ALTER TABLE set_kich_hoat ADD COLUMN IF NOT EXISTS"
                    + " option_mo_ta VARCHAR(255) NULL");
            // Cot nay tung la INT (mot dong mo ta duy nhat). Doi sang chuoi de
            // chua duoc nhieu dong "so mon:id". MODIFY chay lai khong sao.
            ConnectDB.executeUpdate("ALTER TABLE set_kich_hoat"
                    + " MODIFY option_mo_ta VARCHAR(255) NULL");
        } catch (Exception ex) {
            Logger.logException(SetBonusDAO.class, ex,
                    "Không thêm được cột hanh_tinh — set tự tạo sẽ nằm ở mục Khác");
        }
    }

    private static final Map<String, DinhNghia> DINH_NGHIA = new LinkedHashMap<>();

    /** Các set tự định nghĩa đang bật. */
    public static Map<String, DinhNghia> dinhNghia() {
        ensureLoaded();
        synchronized (DINH_NGHIA) {
            return new LinkedHashMap<>(DINH_NGHIA);
        }
    }

    private static void napDinhNghia() {
        Map<String, DinhNghia> m = new LinkedHashMap<>();
        CrisResultSet rs = null;
        try {
            vaCotHanhTinh();
            rs = ConnectDB.executeQuery("SELECT * FROM set_kich_hoat ORDER BY set_key");
            while (rs.next()) {
                DinhNghia d = new DinhNghia();
                d.setKey = rs.getString("set_key");
                d.ten = rs.getString("ten");
                d.optionIds = rs.getString("option_ids");
                d.active = rs.getBoolean("active");
                d.ghiChu = rs.getStringOrNull("ghi_chu");
                d.hanhTinh = rs.getStringOrNull("hanh_tinh");
                d.moTaDong = rs.getStringOrNull("option_mo_ta");
                m.put(d.setKey, d);
            }
        } catch (Exception ex) {
            Logger.logException(SetBonusDAO.class, ex, "Không đọc được set_kich_hoat");
        } finally {
            dispose(rs);
        }
        synchronized (DINH_NGHIA) {
            DINH_NGHIA.clear();
            DINH_NGHIA.putAll(m);
        }
    }

    public static String luuDinhNghia(DinhNghia d) {
        if (d.setKey == null || !d.setKey.matches("[a-zA-Z0-9_]{2,40}")) {
            return "Mã set chỉ được dùng chữ, số, gạch dưới — dài 2 đến 40 ký tự.";
        }
        if (d.ten == null || d.ten.trim().isEmpty()) {
            return "Tên set không được để trống.";
        }
        try {
            for (String p : d.optionIds.split(",")) {
                if (!p.trim().isEmpty()) {
                    Integer.parseInt(p.trim());
                }
            }
        } catch (NumberFormatException ex) {
            return "Danh sách option phải là các số nguyên ngăn nhau bằng dấu phẩy.";
        }
        try {
            vaCotHanhTinh();
            ConnectDB.executeUpdate(
                    "INSERT INTO set_kich_hoat (set_key, ten, option_ids, active, ghi_chu,"
                    + " hanh_tinh, option_mo_ta) VALUES (?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten),"
                    + " option_ids = VALUES(option_ids), active = VALUES(active),"
                    + " ghi_chu = VALUES(ghi_chu), hanh_tinh = VALUES(hanh_tinh),"
                    + " option_mo_ta = VALUES(option_mo_ta)",
                    d.setKey, d.ten, d.optionIds, d.active ? 1 : 0, d.ghiChu, d.hanhTinh,
                    d.moTaDong);
            reload();
            return null;
        } catch (Exception ex) {
            Logger.logException(SetBonusDAO.class, ex, "Lỗi lưu set_kich_hoat " + d.setKey);
            return "Lỗi ghi CSDL: " + ex.getMessage();
        }
    }

    /**
     * Xoá một set tự định nghĩa, kèm mọi dòng chỉ số của nó.
     *
     * <p>Để lại dòng chỉ số mồ côi thì set đã xoá vẫn không ai thấy nhưng dữ liệu
     * còn nằm đó, và tạo lại set cùng mã là chỉ số cũ sống lại bất ngờ.</p>
     */
    public static boolean xoaDinhNghia(String setKey) {
        try {
            ConnectDB.executeUpdate("DELETE FROM set_bonus WHERE set_key = ?", setKey);
            boolean ok = ConnectDB.executeUpdate(
                    "DELETE FROM set_kich_hoat WHERE set_key = ?", setKey) == 1;
            reload();
            return ok;
        } catch (Exception ex) {
            Logger.logException(SetBonusDAO.class, ex, "Lỗi xoá set " + setKey);
            return false;
        }
    }

    /**
     * Tổng phần trăm sát thương cộng thêm cho một chiêu, từ các set đang mặc.
     *
     * <p>Gọi ngay trong phần tính sát thương chiêu của {@code NPoint} — chỗ mà
     * hiệu ứng kiểu "mặc đủ set Sôngôku thì Kamejoko +100%" vốn viết cứng.</p>
     */
    public static int phanTramSkill(SetClothes sc, int idChieu) {
        return phanTramTheoChieu(sc, "skill_pct", idChieu);
    }

    /**
     * Tỉ lệ <b>làm mới</b> một chiêu sau khi dùng, cộng dồn từ các set đang mặc.
     *
     * <p>Làm mới nghĩa là xoá sạch thời gian hồi chiêu — bấm được lại ngay.
     * Khác hẳn {@code hoi_chieu_pct} (rút ngắn thời gian chờ): cái kia luôn có
     * tác dụng nhưng chỉ một phần, cái này thì ăn cả hoặc không, theo may.</p>
     */
    public static int phanTramLamMoi(SetClothes sc, int idChieu) {
        return phanTramTheoChieu(sc, "lam_moi_pct", idChieu);
    }

    /**
     * Tổng {@code giaTri} của các dòng thuộc {@code loai} và nhắm đúng chiêu này.
     *
     * <p>Chỉ tính các mốc đã đạt ({@code soMon >= b.soMon}), nên các mốc
     * <b>cộng dồn</b>: mặc 5 món thì ăn cả mốc 2, 3, 4 và 5.</p>
     */
    /**
     * Các dòng chỉ số của một set đang <b>thực sự có hiệu lực</b>.
     *
     * <p>Với mỗi cặp (loại, tham số) chỉ giữ dòng ở <b>mốc cao nhất đã đạt</b>.
     * Các mốc là một cái thang thay thế nhau chứ không cộng dồn.</p>
     *
     * <h3>Vì sao không cộng dồn</h3>
     *
     * <p>Số liệu trong bảng viết theo thang: {@code setTuPhatNo1} ghi 2 món 10%,
     * 3 món 30%, 4 món 50%, 5 món 70%. Cộng dồn thì mặc đủ năm món thành 160%
     * chứ không phải 70%.</p>
     *
     * <p>Rõ nhất là {@code lam_moi_pct} (20/30/40/50): cộng dồn thành 140%, mà
     * đó là một <b>tỉ lệ may rủi</b> — vượt 100% nghĩa là lần nào cũng trúng, ba
     * mốc dưới thành vô nghĩa và chiêu hết hẳn thời gian chờ. Không ai viết một
     * cái thang bốn bậc để bậc nào cũng cho cùng một kết quả.</p>
     *
     * <p>Khung thông tin bên máy khách cũng chỉ tô <b>một</b> mốc — mốc cao nhất
     * đạt được — nên cộng dồn còn làm số người chơi đọc được khác số thật.</p>
     */
    public static List<Bonus> mocDangHuong(SetClothes sc, String setKey) {
        int soMon = soMonDangMac(sc, setKey);
        if (soMon <= 0) {
            return java.util.Collections.emptyList();
        }
        Map<String, Bonus> cao = new LinkedHashMap<>();
        for (Bonus b : bonusCua(setKey)) {
            if (b == null || b.loai == null || b.soMon > soMon) {
                continue;
            }
            String khoa = b.loai + "#" + b.thamSo;
            Bonus dangGiu = cao.get(khoa);
            if (dangGiu == null || b.soMon > dangGiu.soMon) {
                cao.put(khoa, b);
            }
        }
        return new ArrayList<>(cao.values());
    }

    private static int phanTramTheoChieu(SetClothes sc, String loai, int idChieu) {
        if (sc == null || loai == null) {
            return 0;
        }
        ensureLoaded();
        int tong = 0;
        for (String setKey : cacSet()) {
            // Trong MOT set chi lay moc cao nhat; giua CAC set thi cong lai.
            for (Bonus b : mocDangHuong(sc, setKey)) {
                if (loai.equals(b.loai) && b.thamSo == idChieu) {
                    tong += (int) b.giaTri;
                }
            }
        }
        return tong;
    }

    private static void dispose(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Đếm số <b>món hàng</b> trong cửa hàng đang gắn một chỉ số.
     *
     * <p>Dùng để cảnh báo lúc tạo set: khai một chỉ số mà không món nào mang thì
     * set đó <b>không bao giờ kích hoạt</b> — mặc đủ năm món cũng không tính, vì
     * cách nhận diện set là đọc chỉ số gắn trên món.</p>
     *
     * <p><b>Chỉ đếm được bảng {@code item_shop_option}.</b> Món rơi từ quái nhận
     * chỉ số lúc chạy chứ không có bảng nào liệt kê sẵn, nên số 0 ở đây
     * <i>không</i> chắc chắn là "không món nào có" — nó chỉ có nghĩa là không
     * cửa hàng nào bán món mang chỉ số này. Panel nói đúng như vậy chứ không
     * khẳng định quá.</p>
     */
    public static int soChoDungOption(int optionId) {
        return demMot("SELECT COUNT(*) c FROM item_shop_option WHERE option_id = ?", optionId);
    }

    private static int demMot(String sql, int thamSo) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(sql, thamSo);
            if (rs.next()) {
                return rs.getInt("c");
            }
        } catch (Exception ex) {
            // Bang khong ton tai o ban CSDL nay -> coi nhu khong dem duoc, tra 0.
            // KHONG log: ham nay chi de canh bao, khong dang lam ban log.
        } finally {
            dispose(rs);
        }
        return 0;
    }

    /**
     * Tổng phần trăm của một loại chỉ số, gộp từ mọi set đang mặc đủ số món.
     *
     * <p>Dùng cho những loại <b>không cộng vào trường nào của {@code NPoint}</b>
     * mà phải nhân vào một giá trị tính tại chỗ — hồi phục khi gồng, thời gian
     * hồi chiêu, thời gian choáng. Những chỗ đó nằm ngoài {@code NPoint} nên
     * không đọc được trường riêng của nó.</p>
     */
    public static int phanTramLoai(SetClothes sc, String loai) {
        if (sc == null || loai == null) {
            return 0;
        }
        ensureLoaded();
        int tong = 0;
        for (String setKey : cacSet()) {
            // Trong MOT set chi lay moc cao nhat; giua CAC set thi cong lai.
            for (Bonus b : mocDangHuong(sc, setKey)) {
                if (loai.equals(b.loai)) {
                    tong += (int) b.giaTri;
                }
            }
        }
        return tong;
    }

    /**
     * Các option id của một set <b>hợp với hệ người chơi</b>, chọn ngẫu nhiên.
     *
     * <p>Dùng khi phát đồ set kích hoạt. Món rơi ra phải mang option của một set
     * <b>đang có trong bảng {@code set_kich_hoat}</b> — hệ set viết cứng đã bỏ
     * hẳn nên không còn id nào khác để lấy.</p>
     *
     * <h3>Lọc theo hành tinh</h3>
     *
     * <p>Chỉ lấy set có hành tinh <b>đúng bằng hệ người chơi</b>. Không có set
     * nào của hệ đó thì lấy set để hành tinh <b>"Khác"</b> — coi là dùng chung.
     * <b>Không bao giờ</b> lấy set của hệ khác: người Xayda đánh quái mà rơi ra
     * đồ mang set của Namếc là mặc vào không kích hoạt được gì.</p>
     *
     * @param can    số option lấy tối đa; set có ít hơn thì lấy đúng số đang có
     * @param gender hệ người chơi: 0 Trái Đất, 1 Namếc, 2 Xayda; số khác thì
     *               không lọc
     * @return mảng {@code can} phần tử, hoặc {@code null} nếu không set nào hợp
     */
    public static int[] optionSetMoi(int can, int gender) {
        if (can <= 0) {
            return null;
        }
        String heCuaNguoiChoi = gender >= 0 && gender < 3 ? CAC_HANH_TINH[gender] : null;
        List<int[]> hopHe = new ArrayList<>();
        List<int[]> dungChung = new ArrayList<>();
        for (DinhNghia d : dinhNghia().values()) {
            if (!d.active || d.optionIds == null) {
                continue;
            }
            List<Integer> ids = new ArrayList<>();
            for (String p : d.optionIds.split(",")) {
                try {
                    ids.add(Integer.parseInt(p.trim()));
                } catch (NumberFormatException ignored) {
                    // Dong cau hinh hong -> bo qua option do.
                }
            }
            if (ids.isEmpty()) {
                continue;
            }
            int[] mang = new int[ids.size()];
            for (int i = 0; i < mang.length; i++) {
                mang[i] = ids.get(i);
            }
            String ht = hanhTinh(d.setKey);
            if (heCuaNguoiChoi == null || heCuaNguoiChoi.equals(ht)) {
                hopHe.add(mang);
            } else if (KHAC.equals(ht)) {
                dungChung.add(mang);
            }
        }
        List<int[]> nguon = !hopHe.isEmpty() ? hopHe : dungChung;
        if (nguon.isEmpty()) {
            return null;
        }
        int[] chon = nguon.get(nro.core.util.Util.nextInt(nguon.size()));
        // KHONG lap lai id cho du so luong: lap thi mon do hien hai dong chi so
        // y het nhau, nguoi choi tuong bi loi. Set khai bao may id thi gan may.
        int lay = Math.min(can, chon.length);
        int[] ra = new int[lay];
        System.arraycopy(chon, 0, ra, 0, lay);
        return ra;
    }

    /**
     * Id dòng mô tả của set chứa chỉ số nhận diện này; {@code -1} nếu không có.
     *
     * <p>Tra ngược từ chỉ số nhận diện chứ không từ mã set, vì chỗ phát đồ chỉ
     * cầm trong tay các option id vừa bốc được.</p>
     */
    public static java.util.Map<Integer, Integer> docMoTaDong(String raw) {
        java.util.Map<Integer, Integer> ra = new java.util.TreeMap<>();
        if (raw == null || raw.trim().isEmpty() || "-1".equals(raw.trim())) {
            return ra;
        }
        for (String p : raw.split(",")) {
            String[] x = p.split(":");
            if (x.length == 1) {
                // Dang CU: cot nay tung chua dung mot id, la dong chu cua bo
                // day du. Doc ve moc 5 thay vi bo qua — bo qua la dong chu
                // admin da viet am tham bien mat khoi mon do.
                try {
                    ra.put(5, Integer.parseInt(x[0].trim()));
                } catch (NumberFormatException ignored) {
                    // khong phai so -> khong phai dang cu, bo qua
                }
                continue;
            }
            if (x.length != 2) {
                continue;       // dong hong -> bo qua, con hon hong ca set
            }
            try {
                ra.put(Integer.parseInt(x[0].trim()), Integer.parseInt(x[1].trim()));
            } catch (NumberFormatException ignored) {
                // bo qua dong khong doc duoc
            }
        }
        return ra;
    }

    /** Dựng lại chuỗi {@code "2:273,4:274"} từ bảng mốc → id. */
    public static String ghiMoTaDong(java.util.Map<Integer, Integer> m) {
        if (m == null || m.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<Integer, Integer> e
                : new java.util.TreeMap<>(m).entrySet()) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(e.getKey()).append(':').append(e.getValue());
        }
        return sb.toString();
    }

    /**
     * Các id dòng mô tả của set chứa chỉ số nhận diện này.
     *
     * <p>Tra ngược từ chỉ số nhận diện chứ không từ mã set, vì chỗ phát đồ chỉ
     * cầm trong tay các option id vừa bốc được.</p>
     */
    public static int[] moTaTheoNhanDien(int optionId) {
        for (DinhNghia d : dinhNghia().values()) {
            if (!d.active || d.optionIds == null) {
                continue;
            }
            java.util.Map<Integer, Integer> dong = docMoTaDong(d.moTaDong);
            if (dong.isEmpty()) {
                continue;
            }
            for (String p : d.optionIds.split(",")) {
                try {
                    if (Integer.parseInt(p.trim()) == optionId) {
                        int[] ra = new int[dong.size()];
                        int i = 0;
                        for (int id : dong.values()) {
                            ra[i++] = id;
                        }
                        return ra;
                    }
                } catch (NumberFormatException ignored) {
                    // Dong cau hinh hong -> bo qua.
                }
            }
        }
        return new int[0];
    }

    /**
     * Thêm dòng mô tả vào sau các chỉ số nhận diện vừa bốc.
     *
     * <p>Gọi ở mọi chỗ phát đồ set kích hoạt, để món đồ hiện đúng dòng chữ mà
     * admin đã viết trên panel.</p>
     */
    public static int[] kemMoTa(int[] nhanDien) {
        if (nhanDien == null || nhanDien.length == 0) {
            return nhanDien;
        }
        int[] moTa = moTaTheoNhanDien(nhanDien[0]);
        if (moTa.length == 0) {
            return nhanDien;
        }
        int[] ra = new int[nhanDien.length + moTa.length];
        System.arraycopy(nhanDien, 0, ra, 0, nhanDien.length);
        System.arraycopy(moTa, 0, ra, nhanDien.length, moTa.length);
        return ra;
    }


    /**
     * Các option của <b>đúng một set</b>, chọn theo {@code setKey}.
     *
     * <p>{@link #optionSetMoi(int, int)} bốc ngẫu nhiên trong các set hợp hệ.
     * Hàm này dùng khi admin đã chỉ đích danh một set ở dòng rơi trên panel —
     * đánh con quái đó là ra đúng set ấy, không bốc nữa.</p>
     *
     * @return mảng option đã kèm dòng mô tả, hoặc {@code null} nếu không có set
     *         nào mang khoá đó, set đang tắt, hoặc set khai rỗng option
     */
    public static int[] optionCuaSet(String setKey) {
        if (setKey == null || setKey.trim().isEmpty()) {
            return null;
        }
        DinhNghia d = dinhNghia().get(setKey.trim());
        if (d == null || !d.active || d.optionIds == null) {
            return null;
        }
        List<Integer> ids = new ArrayList<>();
        for (String p : d.optionIds.split(",")) {
            try {
                ids.add(Integer.parseInt(p.trim()));
            } catch (NumberFormatException boQua) {
                // Dong cau hinh hong -> bo qua rieng option do.
            }
        }
        if (ids.isEmpty()) {
            return null;
        }
        int[] mang = new int[ids.size()];
        for (int i = 0; i < mang.length; i++) {
            mang[i] = ids.get(i);
        }
        return kemMoTa(mang);
    }
    /**
     * Như trên nhưng không lọc hệ — dùng khi chưa biết người chơi là hệ nào.
     */
    public static int[] optionSetMoi(int can) {
        return optionSetMoi(can, -1);
    }
}
