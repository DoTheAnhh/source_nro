package nro.repository.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Dữ liệu <b>NRO Pass</b> — kiểu Royale Pass: mỗi mùa có một dãy cấp, mỗi cấp
 * hai ô quà: hàng <b>Miễn phí</b> ai cũng nhận, hàng <b>Cao cấp</b> phải mở
 * khoá bằng tiền. Lên cấp bằng <b>điểm pass</b> — nhận khi xong nhiệm vụ Bò Mộng.
 *
 * <h2>Bảng</h2>
 * <ul>
 *   <li>{@code nro_pass_cau_hinh} — khoá/giá trị: tên, mùa hiện tại, ngày bắt
 *       đầu, số ngày một mùa, số cấp, điểm mỗi cấp, nguồn điểm, giá cao cấp,
 *       ưu đãi chỉ số của cao cấp.</li>
 *   <li>{@code nro_pass_qua} — quà theo (cấp, hàng). Dùng chung cho mọi mùa.</li>
 *   <li>{@code nro_pass_nguoi} — điểm và cờ cao cấp của từng người, <b>theo
 *       mùa</b>: sang mùa mới là một dòng mới, mùa cũ để nguyên làm lịch sử.</li>
 *   <li>{@code nro_pass_nhan} — ô đã nhận; khoá chính (mùa, người, cấp, hàng)
 *       nên một ô không nhận được hai lần.</li>
 * </ul>
 *
 * <p>Tự tạo bảng và gieo mặc định lúc dùng lần đầu: máy chủ thật chỉ
 * {@code git pull} rồi chạy, không ai chạy SQL tay.</p>
 */
public class NroPassDAO {

    public static final int HANG_MIEN_PHI = 0;
    public static final int HANG_CAO_CAP = 1;

    public static final String TIEN_VND = "vnd";
    public static final String TIEN_HONG_NGOC = "hong_ngoc";
    public static final String TIEN_NGOC = "ngoc";
    public static final String[] CAC_LOAI_TIEN = {TIEN_VND, TIEN_HONG_NGOC, TIEN_NGOC};

    public static final long MOT_NGAY_MS = 24L * 60 * 60 * 1000;
    private static final long HAN_BO_NHO_MS = 30_000L;

    public static String tenTien(String loai) {
        if (TIEN_HONG_NGOC.equals(loai)) {
            return "hồng ngọc";
        }
        if (TIEN_NGOC.equals(loai)) {
            return "ngọc";
        }
        return "VNĐ";
    }

    // =====================================================================
    //  Cấu hình
    // =====================================================================
    public static final String K_TEN = "ten";
    public static final String K_MUA = "mua";
    public static final String K_MUA_BAT_DAU = "mua_bat_dau";
    public static final String K_SO_NGAY_MUA = "so_ngay_mua";
    public static final String K_SO_CAP = "so_cap";
    public static final String K_DIEM_MOI_CAP = "diem_moi_cap";
    public static final String K_SO_NV_NGAY = "so_nv_bo_mong_ngay";
    /** Điểm theo độ khó nhiệm vụ Bò Mộng: dễ, thường, khó, siêu khó, địa ngục. */
    public static final String[] K_DIEM_NV = {"diem_nv_de", "diem_nv_thuong", "diem_nv_kho",
        "diem_nv_sieu_kho", "diem_nv_dia_nguc"};
    public static final String[] TEN_DO_KHO = {"Dễ", "Thường", "Khó", "Siêu khó", "Địa ngục"};
    public static final String K_GIA_CAO_CAP = "gia_cao_cap";
    public static final String K_LOAI_TIEN = "loai_tien";
    public static final String K_PT_HP = "pt_hp";
    public static final String K_PT_KI = "pt_ki";
    public static final String K_PT_SD = "pt_sd";
    public static final String K_PT_TIEM_NANG = "pt_tiem_nang";

    /** Khoá, giá trị mặc định, mô tả — thứ tự này cũng là thứ tự trên panel. */
    public static final String[][] CAU_HINH_GOC = {
        {K_TEN, "NRO Pass", "Tên hiển thị"},
        {K_MUA, "1", "Mùa hiện tại. Hết mùa máy chủ tự sang mùa sau"},
        {K_MUA_BAT_DAU, "0", "Lúc bắt đầu mùa (mili giây). 0 = lấy lúc khởi động đầu tiên"},
        {K_SO_NGAY_MUA, "30", "Một mùa kéo dài bao nhiêu ngày"},
        {K_SO_CAP, "50", "Số cấp của pass"},
        {K_DIEM_MOI_CAP, "100", "Điểm cần cho MỖI cấp"},
        {K_SO_NV_NGAY, "10", "Số nhiệm vụ Bò Mộng nhận được mỗi ngày"},
        {"diem_nv_de", "5", "Điểm pass khi xong nhiệm vụ Bò Mộng Dễ"},
        {"diem_nv_thuong", "10", "Điểm pass khi xong nhiệm vụ Bò Mộng Thường"},
        {"diem_nv_kho", "15", "Điểm pass khi xong nhiệm vụ Bò Mộng Khó"},
        {"diem_nv_sieu_kho", "20", "Điểm pass khi xong nhiệm vụ Bò Mộng Siêu khó"},
        {"diem_nv_dia_nguc", "30", "Điểm pass khi xong nhiệm vụ Bò Mộng Địa ngục"},
        {K_GIA_CAO_CAP, "100000", "Giá mở khoá hàng Cao cấp (mỗi mùa)"},
        {K_LOAI_TIEN, TIEN_VND, "Loại tiền: vnd / hong_ngoc / ngoc"},
        {K_PT_HP, "10", "Cao cấp: % HP cộng thêm suốt mùa"},
        {K_PT_KI, "10", "Cao cấp: % KI cộng thêm suốt mùa"},
        {K_PT_SD, "10", "Cao cấp: % sức đánh cộng thêm suốt mùa"},
        {K_PT_TIEM_NANG, "150", "Cao cấp: % tiềm năng cộng thêm suốt mùa"},
    };

    private static final Map<String, String> CAU_HINH = new HashMap<>();
    private static long lucDocCauHinh;

    private static synchronized void docCauHinh() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDocCauHinh < HAN_BO_NHO_MS && !CAU_HINH.isEmpty()) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT khoa, gia_tri FROM nro_pass_cau_hinh");
            Map<String, String> moi = new HashMap<>();
            while (rs.next()) {
                moi.put(rs.getString("khoa"), rs.getString("gia_tri"));
            }
            CAU_HINH.clear();
            CAU_HINH.putAll(moi);
            lucDocCauHinh = bayGio;
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không đọc được cấu hình NRO Pass");
        } finally {
            dong(rs);
        }
    }

    public static synchronized String chu(String khoa) {
        docCauHinh();
        String v = CAU_HINH.get(khoa);
        if (v == null) {
            for (String[] d : CAU_HINH_GOC) {
                if (d[0].equals(khoa)) {
                    return d[1];
                }
            }
            return "";
        }
        return v;
    }

    public static long so(String khoa) {
        try {
            return Long.parseLong(chu(khoa).trim());
        } catch (NumberFormatException sai) {
            for (String[] d : CAU_HINH_GOC) {
                if (d[0].equals(khoa)) {
                    try {
                        return Long.parseLong(d[1]);
                    } catch (NumberFormatException khongPhaiSo) {
                        return 0;
                    }
                }
            }
            return 0;
        }
    }

    public static void datCauHinh(String khoa, String giaTri) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO nro_pass_cau_hinh (khoa, gia_tri) VALUES (?, ?)"
                    + " ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri)", khoa, giaTri);
            lucDocCauHinh = 0;
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không lưu được cấu hình NRO Pass");
        }
    }

    public static String ten() {
        String t = chu(K_TEN);
        return t.isEmpty() ? "NRO Pass" : t;
    }

    public static int soCap() {
        return (int) Math.max(1, Math.min(200, so(K_SO_CAP)));
    }

    public static long diemMoiCap() {
        return Math.max(1, so(K_DIEM_MOI_CAP));
    }

    /** Số nhiệm vụ Bò Mộng nhận được mỗi ngày (đọc ở {@code SideTask}). */
    public static int soNhiemVuMoiNgay() {
        return (int) Math.max(1, Math.min(100, so(K_SO_NV_NGAY)));
    }

    /** Điểm pass của nhiệm vụ Bò Mộng độ khó {@code doKho} (0 dễ … 4 địa ngục). */
    public static long diemNhiemVu(int doKho) {
        if (doKho < 0 || doKho >= K_DIEM_NV.length) {
            return 0;
        }
        return Math.max(0, so(K_DIEM_NV[doKho]));
    }

    // --- mùa ---
    /**
     * Mùa hiện tại; hết mùa thì tự sang mùa sau.
     *
     * <p>Sang mùa bằng cách cộng đúng một độ dài mùa vào mốc bắt đầu (lặp nếu
     * máy chủ tắt lâu), không đặt lại bằng "bây giờ": có thế các mùa mới nối
     * nhau đều đặn, không trôi dần theo giờ khởi động máy.</p>
     */
    public static synchronized int mua() {
        long batDau = so(K_MUA_BAT_DAU);
        long bayGio = System.currentTimeMillis();
        if (batDau <= 0) {
            batDau = bayGio;
            datCauHinh(K_MUA_BAT_DAU, String.valueOf(batDau));
        }
        long doDai = Math.max(1, so(K_SO_NGAY_MUA)) * MOT_NGAY_MS;
        int mua = (int) Math.max(1, so(K_MUA));
        boolean doi = false;
        while (bayGio >= batDau + doDai) {
            batDau += doDai;
            mua++;
            doi = true;
        }
        if (doi) {
            datCauHinh(K_MUA, String.valueOf(mua));
            datCauHinh(K_MUA_BAT_DAU, String.valueOf(batDau));
            Logger.success("NRO Pass: sang mùa " + mua + "\n");
        }
        return mua;
    }

    /** Lúc mùa hiện tại kết thúc (mili giây). */
    public static long hetMua() {
        mua();
        return so(K_MUA_BAT_DAU) + Math.max(1, so(K_SO_NGAY_MUA)) * MOT_NGAY_MS;
    }

    /** Quản trị bấm "Bắt đầu mùa mới": sang mùa kế tiếp ngay từ bây giờ. */
    public static synchronized void batDauMuaMoi() {
        int mua = mua() + 1;
        datCauHinh(K_MUA, String.valueOf(mua));
        datCauHinh(K_MUA_BAT_DAU, String.valueOf(System.currentTimeMillis()));
    }

    // --- ưu đãi chỉ số, đọc trong NPoint ---
    /**
     * % ưu đãi của {@code Player.THE_THANG = bac}. Bậc 2 là NRO Pass cao cấp,
     * đọc từ cấu hình; bậc 1 là thẻ tháng thường đời cũ — giữ đúng con số
     * {@code NPoint} từng viết cứng, cho ai còn dữ liệu cũ không bị đổi chỉ số.
     */
    public static int ptHp(int bac) {
        return bac == 2 ? (int) so(K_PT_HP) : (bac == 1 ? 7 : 0);
    }

    public static int ptKi(int bac) {
        return bac == 2 ? (int) so(K_PT_KI) : (bac == 1 ? 7 : 0);
    }

    public static int ptSd(int bac) {
        return bac == 2 ? (int) so(K_PT_SD) : (bac == 1 ? 7 : 0);
    }

    public static int ptTiemNang(int bac) {
        return bac == 2 ? (int) so(K_PT_TIEM_NANG) : (bac == 1 ? 100 : 0);
    }

    public static String tenBac(int bac) {
        return bac == 2 ? (ten() + " cao cấp") : "Thẻ tháng";
    }

    /** Dòng ưu đãi của hàng cao cấp, gộp các chỉ số bằng nhau cho gọn. */
    public static String uuDaiCaoCap() {
        int hp = ptHp(2);
        int ki = ptKi(2);
        int sd = ptSd(2);
        int tn = ptTiemNang(2);
        StringBuilder s = new StringBuilder();
        if (hp == ki && ki == sd && hp > 0) {
            s.append("+").append(hp).append("% HP, KI, sức đánh");
        } else {
            noi(s, hp, "HP");
            noi(s, ki, "KI");
            noi(s, sd, "sức đánh");
        }
        noi(s, tn, "tiềm năng");
        return s.toString();
    }

    private static void noi(StringBuilder s, int pt, String ten) {
        if (pt <= 0) {
            return;
        }
        if (s.length() > 0) {
            s.append(", ");
        }
        s.append("+").append(pt).append("% ").append(ten);
    }

    public static String giaCaoCap() {
        return nro.core.util.Util.soCham(so(K_GIA_CAO_CAP)) + " " + tenTien(chu(K_LOAI_TIEN));
    }

    // =====================================================================
    //  Tạo bảng
    // =====================================================================
    private static boolean daTaoBang;

    public static synchronized void damBaoBang() {
        if (daTaoBang) {
            return;
        }
        daTaoBang = true;
        try {
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS nro_pass_cau_hinh ("
                    + " khoa VARCHAR(40) NOT NULL,"
                    + " gia_tri VARCHAR(80) NOT NULL DEFAULT '',"
                    + " mo_ta VARCHAR(255) DEFAULT NULL,"
                    + " PRIMARY KEY (khoa)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS nro_pass_qua ("
                    + " id INT(11) NOT NULL AUTO_INCREMENT,"
                    + " cap INT(11) NOT NULL,"
                    + " hang INT(11) NOT NULL DEFAULT 0,"
                    + " item_id INT(11) NOT NULL,"
                    + " so_luong INT(11) NOT NULL DEFAULT 1,"
                    + " chi_so VARCHAR(255) NOT NULL DEFAULT '',"
                    + " PRIMARY KEY (id), KEY (cap, hang)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS nro_pass_nguoi ("
                    + " mua INT(11) NOT NULL,"
                    + " player_id INT(11) NOT NULL,"
                    + " diem BIGINT(20) NOT NULL DEFAULT 0,"
                    + " cao_cap TINYINT(1) NOT NULL DEFAULT 0,"
                    + " PRIMARY KEY (mua, player_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS nro_pass_nhan ("
                    + " mua INT(11) NOT NULL,"
                    + " player_id INT(11) NOT NULL,"
                    + " cap INT(11) NOT NULL,"
                    + " hang INT(11) NOT NULL,"
                    + " PRIMARY KEY (mua, player_id, cap, hang)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            for (String[] d : CAU_HINH_GOC) {
                ConnectDB.executeUpdate("INSERT IGNORE INTO nro_pass_cau_hinh (khoa, gia_tri, mo_ta)"
                        + " VALUES (?, ?, ?)", d[0], d[1], d[2]);
            }
            if (demDong("nro_pass_qua") == 0) {
                gieoQua();
            }
        } catch (Exception ex) {
            daTaoBang = false;
            Logger.logException(NroPassDAO.class, ex, "Không tạo được bảng NRO Pass");
        }
    }

    /**
     * Bộ quà mặc định cho 50 cấp.
     *
     * <p>Nhịp quen thuộc của các game pass: cấp thường là quà nhỏ, mỗi 5 cấp
     * một món khá, mỗi 10 cấp một rương, cấp cuối là món lớn nhất. Hàng cao
     * cấp lúc nào cũng hơn hàng miễn phí cùng cấp một bậc.</p>
     */
    private static void gieoQua() throws Exception {
        for (int cap = 1; cap <= 50; cap++) {
            // Mien phi.
            if (cap == 50) {
                them(cap, HANG_MIEN_PHI, 2454, 1);          // Ruong thu cung thuong
            } else if (cap % 10 == 0) {
                them(cap, HANG_MIEN_PHI, 571, 1);           // Ruong bac
            } else if (cap % 5 == 0) {
                them(cap, HANG_MIEN_PHI, 380, 2);           // Vien capsule ki bi
            } else if (cap % 2 == 1) {
                them(cap, HANG_MIEN_PHI, 457, 1 + cap / 10); // Thoi vang
            } else {
                them(cap, HANG_MIEN_PHI, 595, 5);           // Dau than cap 10
            }
            // Cao cap.
            if (cap == 50) {
                them(cap, HANG_CAO_CAP, 2453, 1);           // Ruong thu cung cao cap
                them(cap, HANG_CAO_CAP, 1453, 1);           // Ruong sao pha le VIP
            } else if (cap % 10 == 0) {
                them(cap, HANG_CAO_CAP, 572, 1);            // Ruong vang
            } else if (cap % 5 == 0) {
                them(cap, HANG_CAO_CAP, 1440, 1);           // Ruong sao pha le
            } else if (cap % 3 == 0) {
                them(cap, HANG_CAO_CAP, 987, 1);            // Da bao ve
            } else {
                them(cap, HANG_CAO_CAP, 457, 3 + cap / 5);  // Thoi vang
            }
        }
        Logger.success("NRO Pass: gieo quà mặc định 50 cấp\n");
    }

    private static void them(int cap, int hang, int itemId, int soLuong) throws Exception {
        ConnectDB.executeUpdate("INSERT INTO nro_pass_qua (cap, hang, item_id, so_luong)"
                + " VALUES (?, ?, ?, ?)", cap, hang, itemId, soLuong);
    }

    // =====================================================================
    //  Quà
    // =====================================================================
    public static final class Qua {

        public int id;
        public int cap;
        public int hang;
        public int itemId;
        public int soLuong = 1;
        public String chiSo = "";
    }

    private static final List<Qua> QUA = new ArrayList<>();
    private static long lucDocQua;

    private static synchronized void docQua() {
        long bayGio = System.currentTimeMillis();
        if (bayGio - lucDocQua < HAN_BO_NHO_MS && lucDocQua != 0) {
            return;
        }
        damBaoBang();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT * FROM nro_pass_qua ORDER BY cap, hang, id");
            List<Qua> moi = new ArrayList<>();
            while (rs.next()) {
                Qua q = new Qua();
                q.id = rs.getInt("id");
                q.cap = rs.getInt("cap");
                q.hang = rs.getInt("hang");
                q.itemId = rs.getInt("item_id");
                q.soLuong = rs.getInt("so_luong");
                q.chiSo = rs.getString("chi_so");
                if (q.chiSo == null) {
                    q.chiSo = "";
                }
                moi.add(q);
            }
            QUA.clear();
            QUA.addAll(moi);
            lucDocQua = bayGio;
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không đọc được quà NRO Pass");
        } finally {
            dong(rs);
        }
    }

    /** Quà của một ô (cấp, hàng). */
    public static synchronized List<Qua> dsQua(int cap, int hang) {
        docQua();
        List<Qua> ra = new ArrayList<>();
        for (Qua q : QUA) {
            if (q.cap == cap && q.hang == hang) {
                ra.add(q);
            }
        }
        return ra;
    }

    public static void themQua(int cap, int hang, int itemId, int soLuong, String chiSo) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO nro_pass_qua (cap, hang, item_id, so_luong, chi_so)"
                    + " VALUES (?, ?, ?, ?, ?)", cap, hang, itemId, Math.max(1, soLuong),
                    chiSo == null ? "" : chiSo.trim());
            lucDocQua = 0;
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không thêm được quà NRO Pass");
        }
    }

    public static void suaQua(int id, int soLuong, String chiSo) {
        try {
            ConnectDB.executeUpdate("UPDATE nro_pass_qua SET so_luong = ?, chi_so = ? WHERE id = ?",
                    Math.max(1, soLuong), chiSo == null ? "" : chiSo.trim(), id);
            lucDocQua = 0;
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không sửa được quà NRO Pass");
        }
    }

    public static void xoaQua(int id) {
        try {
            ConnectDB.executeUpdate("DELETE FROM nro_pass_qua WHERE id = ?", id);
            lucDocQua = 0;
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không xoá được quà NRO Pass");
        }
    }

    // =====================================================================
    //  Người chơi
    // =====================================================================
    /** Tiến độ một người trong một mùa. */
    public static final class TienDo {

        public long diem;
        public boolean caoCap;
    }

    public static TienDo tienDo(long playerId, int mua) {
        damBaoBang();
        TienDo t = new TienDo();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT diem, cao_cap FROM nro_pass_nguoi"
                    + " WHERE mua = ? AND player_id = ?", mua, playerId);
            if (rs.next()) {
                t.diem = rs.getLong("diem");
                t.caoCap = rs.getInt("cao_cap") == 1;
            }
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không đọc được tiến độ NRO Pass");
        } finally {
            dong(rs);
        }
        return t;
    }

    public static void congDiem(long playerId, int mua, long them) {
        if (them <= 0) {
            return;
        }
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO nro_pass_nguoi (mua, player_id, diem) VALUES (?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE diem = diem + VALUES(diem)", mua, playerId, them);
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không cộng được điểm NRO Pass");
        }
    }

    /** @return {@code true} nếu ghi được */
    public static boolean moCaoCap(long playerId, int mua) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO nro_pass_nguoi (mua, player_id, cao_cap) VALUES (?, ?, 1)"
                    + " ON DUPLICATE KEY UPDATE cao_cap = 1", mua, playerId);
            return true;
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không mở được cao cấp NRO Pass cho " + playerId);
            return false;
        }
    }

    /** Các ô đã nhận của một người trong một mùa, mã hoá {@code cap * 2 + hang}. */
    public static java.util.Set<Integer> daNhan(long playerId, int mua) {
        damBaoBang();
        java.util.Set<Integer> ra = new java.util.HashSet<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT cap, hang FROM nro_pass_nhan WHERE mua = ? AND player_id = ?",
                    mua, playerId);
            while (rs.next()) {
                ra.add(rs.getInt("cap") * 2 + rs.getInt("hang"));
            }
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không đọc được ô đã nhận NRO Pass");
        } finally {
            dong(rs);
        }
        return ra;
    }

    /** @return {@code false} nếu ô này đã nhận rồi — khoá chính chặn. */
    public static boolean ghiNhan(long playerId, int mua, int cap, int hang) {
        try {
            damBaoBang();
            return ConnectDB.executeUpdate("INSERT IGNORE INTO nro_pass_nhan (mua, player_id, cap, hang)"
                    + " VALUES (?, ?, ?, ?)", mua, playerId, cap, hang) > 0;
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không ghi được ô nhận NRO Pass");
            return false;
        }
    }

    public static void boNhan(long playerId, int mua, int cap, int hang) {
        try {
            ConnectDB.executeUpdate("DELETE FROM nro_pass_nhan WHERE mua = ? AND player_id = ?"
                    + " AND cap = ? AND hang = ?", mua, playerId, cap, hang);
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không trả được ô nhận NRO Pass");
        }
    }

    /** Một dòng bảng xếp hạng cho panel. */
    public static final class Dong {

        public long playerId;
        public String ten = "";
        public long diem;
        public boolean caoCap;
    }

    /** Người chơi mùa này, điểm cao trước. */
    public static List<Dong> dsNguoi(int mua, int toiDa) {
        damBaoBang();
        List<Dong> ra = new ArrayList<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT n.player_id, n.diem, n.cao_cap, p.name"
                    + " FROM nro_pass_nguoi n LEFT JOIN player p ON p.id = n.player_id"
                    + " WHERE n.mua = ? ORDER BY n.diem DESC LIMIT " + Math.max(1, toiDa), mua);
            while (rs.next()) {
                Dong d = new Dong();
                d.playerId = rs.getLong("player_id");
                d.diem = rs.getLong("diem");
                d.caoCap = rs.getInt("cao_cap") == 1;
                d.ten = rs.getString("name");
                if (d.ten == null) {
                    d.ten = "#" + d.playerId;
                }
                ra.add(d);
            }
        } catch (Exception ex) {
            Logger.logException(NroPassDAO.class, ex, "Không đọc được danh sách NRO Pass");
        } finally {
            dong(rs);
        }
        return ra;
    }

    public static long idTheoTen(String ten) {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT id FROM player WHERE name = ?", ten);
            return rs.next() ? rs.getLong("id") : -1;
        } catch (Exception ex) {
            return -1;
        } finally {
            dong(rs);
        }
    }

    // =====================================================================
    private static int demDong(String bang) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery("SELECT COUNT(*) AS n FROM " + bang);
            return rs.next() ? rs.getInt("n") : 0;
        } finally {
            dong(rs);
        }
    }

    private static void dong(CrisResultSet rs) {
        if (rs == null) {
            return;
        }
        try {
            rs.dispose();
        } catch (Exception boQua) {
            // Dong khong duoc thi cung khong lam gi them duoc.
        }
    }
}
