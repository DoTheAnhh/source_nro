package nro.repository.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.entity.boss.BossData;
import nro.entity.boss.BossesData;
import nro.entity.boss.TypeAppear;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/**
 * Đưa số liệu boss ra khỏi mã nguồn, vào bảng {@code boss_data}.
 *
 * <h2>Trước đây</h2>
 *
 * <p>Toàn bộ chỉ số boss nằm trong {@code BossesData.java} — 3.670 dòng, 196
 * mẫu, mỗi mẫu là một lời gọi {@code new BossData(...)} với chín tham số xếp
 * theo thứ tự. Đổi máu một con boss là phải sửa mã, biên dịch lại, khởi động
 * lại máy chủ. Đếm nhầm một dấu phẩy là gán máu vào ô sức đánh.</p>
 *
 * <h2>Bây giờ</h2>
 *
 * <p>Số liệu nằm ở CSDL, sửa được từ panel. <b>Mã nguồn chỉ còn hành vi</b>:
 * kỹ năng riêng, cách đổi phase, lời thoại theo tình huống — 140 trong 189 lớp
 * boss có phần đó và chúng vẫn ở nguyên trong Java, vì đó là logic chứ không
 * phải số liệu.</p>
 *
 * <h2>Gieo lần đầu, rồi CSDL làm chủ</h2>
 *
 * <p>Bảng trống thì {@link #napVaoBoNho()} chép nguyên số đang gõ trong
 * {@code BossesData} xuống CSDL. Từ lần sau, số dưới CSDL đè lên. Nhờ vậy lần
 * chạy đầu tiên sau khi đổi <b>không khác gì trước</b> — không có bước chuyển
 * dữ liệu bằng tay, và cũng không có nguy cơ mất số cũ.</p>
 *
 * <p>Cách áp cũng là <b>sửa tại chỗ</b> chứ không gán lại trường: những lớp
 * boss cầm sẵn tham chiếu tới {@code BossesData.X} vẫn thấy số mới.</p>
 */
public final class BossDataDAO {

    private BossDataDAO() {
    }

    /** Ngăn giữa các câu thoại trong một ô. Thoại không xuống dòng nên an toàn. */
    private static final String NGAN_DONG = "\n";

    private static volatile boolean daNap;

    /**
     * Đọc số liệu boss từ CSDL và áp vào {@code BossesData}.
     *
     * <p>Phải gọi <b>trước</b> khi bất kỳ boss nào được dựng, vì hàm dựng
     * {@code Boss} đọc {@code data[0]} ngay lúc đó.</p>
     *
     * @return số mẫu đã áp
     */
    public static int napVaoBoNho() {
        if (daNap) {
            return 0;
        }
        daNap = true;
        try {
            damBaoBang();
            Map<String, BossData> trongMa = quetTrongMa();
            if (trongMa.isEmpty()) {
                Logger.error("Không đọc được mẫu boss nào trong BossesData\n");
                return 0;
            }
            if (demDong() == 0) {
                gieo(trongMa);
                Logger.success("Đã gieo " + trongMa.size()
                        + " mẫu boss từ mã nguồn xuống bảng boss_data\n");
            }
            return apTuCSDL(trongMa);
        } catch (Exception ex) {
            Logger.logException(BossDataDAO.class, ex,
                    "Không nạp được boss_data — boss dùng số gõ trong mã");
            return 0;
        }
    }

    public static void damBaoBang() throws Exception {
        ConnectDB.executeUpdate("CREATE TABLE IF NOT EXISTS boss_data ("
                + " ma VARCHAR(64) NOT NULL,"
                + " ten VARCHAR(100) NOT NULL DEFAULT '',"
                + " gioi_tinh INT(11) NOT NULL DEFAULT 0,"
                + " trang_phuc VARCHAR(120) NOT NULL DEFAULT '',"
                + " suc_danh BIGINT(20) NOT NULL DEFAULT 0,"
                + " hp VARCHAR(500) NOT NULL DEFAULT '',"
                + " map_join VARCHAR(1000) NOT NULL DEFAULT '',"
                + " ky_nang VARCHAR(2000) NOT NULL DEFAULT '',"
                + " text_s TEXT, text_m TEXT, text_e TEXT,"
                + " giay_hoi_sinh INT(11) NOT NULL DEFAULT 0,"
                + " kieu_xuat_hien VARCHAR(32) NOT NULL DEFAULT '',"
                + " boss_di_kem VARCHAR(500) NOT NULL DEFAULT '',"
                + " giap INT(11) NOT NULL DEFAULT -1,"
                + " ne_don INT(11) NOT NULL DEFAULT -1,"
                + " chinh_xac INT(11) NOT NULL DEFAULT -1,"
                + " PRIMARY KEY (ma)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        // Ba cot nay them sau. Bang da ton tai thi CREATE o tren khong lam gi,
        // nen phai ALTER rieng. -1 = khong dat, giu dung hanh vi cu.
        ConnectDB.executeUpdate("ALTER TABLE boss_data ADD COLUMN IF NOT EXISTS"
                + " giap INT(11) NOT NULL DEFAULT -1");
        ConnectDB.executeUpdate("ALTER TABLE boss_data ADD COLUMN IF NOT EXISTS"
                + " ne_don INT(11) NOT NULL DEFAULT -1");
        ConnectDB.executeUpdate("ALTER TABLE boss_data ADD COLUMN IF NOT EXISTS"
                + " chinh_xac INT(11) NOT NULL DEFAULT -1");
    }

    /**
     * Mọi mẫu boss khai trong {@code BossesData}, tra theo tên hằng.
     *
     * <p>Dò bằng phản chiếu chứ không liệt kê tay: 196 mẫu mà chép tay thì
     * thêm một con boss mới là phải nhớ quay lại sửa ở đây, và quên là con đó
     * lặng lẽ không bao giờ đọc được số từ CSDL.</p>
     */
    public static Map<String, BossData> quetTrongMa() {
        Map<String, BossData> ra = new LinkedHashMap<>();
        for (Field f : BossesData.class.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())
                    || f.getType() != BossData.class) {
                continue;
            }
            try {
                f.setAccessible(true);
                Object v = f.get(null);
                if (v instanceof BossData) {
                    ra.put(f.getName(), (BossData) v);
                }
            } catch (Exception ex) {
                Logger.logException(BossDataDAO.class, ex,
                        "Không đọc được mẫu boss " + f.getName());
            }
        }
        return ra;
    }

    private static int demDong() throws Exception {
        CrisResultSet rs = ConnectDB.executeQuery("SELECT COUNT(*) n FROM boss_data");
        try {
            return rs.next() ? rs.getInt("n") : 0;
        } finally {
            rs.dispose();
        }
    }

    private static void gieo(Map<String, BossData> trongMa) throws Exception {
        for (Map.Entry<String, BossData> e : trongMa.entrySet()) {
            ghi(e.getKey(), e.getValue());
        }
    }

    /** Ghi một mẫu xuống CSDL, thêm mới hoặc đè lên dòng cũ. */
    public static String ghi(String ma, BossData d) {
        try {
            damBaoBang();
            ConnectDB.executeUpdate("INSERT INTO boss_data (ma, ten, gioi_tinh,"
                    + " trang_phuc, suc_danh, hp, map_join, ky_nang, text_s,"
                    + " text_m, text_e, giay_hoi_sinh, kieu_xuat_hien, boss_di_kem)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    + " ON DUPLICATE KEY UPDATE ten = VALUES(ten),"
                    + " gioi_tinh = VALUES(gioi_tinh),"
                    + " trang_phuc = VALUES(trang_phuc),"
                    + " suc_danh = VALUES(suc_danh), hp = VALUES(hp),"
                    + " map_join = VALUES(map_join), ky_nang = VALUES(ky_nang),"
                    + " text_s = VALUES(text_s), text_m = VALUES(text_m),"
                    + " text_e = VALUES(text_e),"
                    + " giay_hoi_sinh = VALUES(giay_hoi_sinh),"
                    + " kieu_xuat_hien = VALUES(kieu_xuat_hien),"
                    + " boss_di_kem = VALUES(boss_di_kem)",
                    ma,
                    d.getName() == null ? "" : d.getName(),
                    (int) d.getGender(),
                    soShort(d.getOutfit()),
                    d.getDame(),
                    soLong(d.getHp()),
                    soInt(d.getMapJoin()),
                    kyNang(d.getSkillTemp()),
                    noiDong(d.getTextS()),
                    noiDong(d.getTextM()),
                    noiDong(d.getTextE()),
                    d.getSecondsRest(),
                    d.getTypeAppear() == null ? "" : d.getTypeAppear().name(),
                    soInt(d.getBossesAppearTogether()));
            return null;
        } catch (Exception ex) {
            Logger.logException(BossDataDAO.class, ex, "Lỗi ghi boss_data " + ma);
            return "Lỗi ghi CSDL — xem log máy chủ.";
        }
    }

    /**
     * Áp số liệu dưới CSDL lên các mẫu trong bộ nhớ.
     *
     * <p>Dòng nào trong CSDL không khớp mẫu nào trong mã thì <b>bỏ qua và ghi
     * log</b>, chứ không dừng cả lượt: một dòng thừa do gõ nhầm tên hằng không
     * đáng để cả máy chủ chạy bằng số cũ.</p>
     */

    /**
     * Dựng một {@link BossData} <b>chỉ từ dòng trong CSDL</b>.
     *
     * <h3>Khác gì {@code apTuCSDL}</h3>
     *
     * <p>{@code apTuCSDL} chỉ <i>đắp</i> số từ CSDL lên các mẫu đã khai sẵn
     * trong {@code BossesData}. Con nào mã nguồn không khai thì dòng CSDL của
     * nó bị bỏ, và {@code BossID} có tới chín mươi mấy hằng số rơi vào diện đó
     * — thêm vào game không được dù bảng có đủ số.</p>
     *
     * <p>Hàm này dựng mẫu rỗng rồi đắp dòng lên, nên chỉ cần có dòng trong
     * {@code boss_data} là con boss đó dùng được. Nhờ vậy thêm boss mới không
     * còn phải sửa mã nguồn.</p>
     *
     * @return mẫu đọc được, hoặc {@code null} nếu không có dòng nào mang mã đó
     */
    public static BossData mauTuCSDL(String ma) {
        if (ma == null || ma.trim().isEmpty()) {
            return null;
        }
        CrisResultSet rs = null;
        try {
            damBaoBang();
            rs = ConnectDB.executeQuery(
                    "SELECT * FROM boss_data WHERE ma = ?", ma.trim());
            if (!rs.next()) {
                return null;
            }
            // Mau rong: mo ta bang chinh ma hang, khong trang phuc, mot moc HP
            // toi thieu. Moi truong sau do bi apMotDong ghi de bang so that.
            BossData d = new BossData(ma.trim(), (byte) 0, new short[]{-1, -1, -1, -1, -1, -1},
                    1000L, new long[]{1000L}, new int[]{0}, new int[][]{{0, 1}},
                    new String[]{}, new String[]{}, new String[]{}, 3600);
            apMotDong(d, rs);
            return d;
        } catch (Exception ex) {
            Logger.logException(BossDataDAO.class, ex,
                    "Không dựng được mẫu boss từ CSDL cho mã " + ma);
            return null;
        } finally {
            if (rs != null) {
                try {
                    rs.dispose();
                } catch (Exception boQua) {
                }
            }
        }
    }
    private static int apTuCSDL(Map<String, BossData> trongMa) throws Exception {
        CrisResultSet rs = ConnectDB.executeQuery("SELECT * FROM boss_data");
        int n = 0;
        List<String> la = new ArrayList<>();
        try {
            while (rs.next()) {
                String ma = rs.getString("ma");
                BossData d = trongMa.get(ma);
                if (d == null) {
                    la.add(ma);
                    continue;
                }
                apMotDong(d, rs);
                n++;
            }
        } finally {
            rs.dispose();
        }
        if (!la.isEmpty()) {
            Logger.error("boss_data có " + la.size() + " dòng không khớp mẫu nào"
                    + " trong mã: " + String.join(", ", la) + "\n");
        }
        return n;
    }

    private static void apMotDong(BossData d, CrisResultSet rs) {
        try {
            String ten = rs.getString("ten");
            if (ten != null && !ten.isEmpty()) {
                d.setName(ten);
            }
            d.setGender((byte) rs.getInt("gioi_tinh"));
            short[] tp = docShort(rs.getString("trang_phuc"));
            if (tp != null) {
                d.setOutfit(tp);
            }
            d.setDame(rs.getLong("suc_danh"));
            long[] hp = docLong(rs.getString("hp"));
            if (hp != null && hp.length > 0) {
                d.setHp(hp);
            }
            int[] mj = docInt(rs.getString("map_join"));
            if (mj != null) {
                d.setMapJoin(mj);
            }
            int[][] kn = docKyNang(rs.getString("ky_nang"));
            if (kn != null) {
                d.setSkillTemp(kn);
            }
            d.setTextS(tachDong(rs.getString("text_s")));
            d.setTextM(tachDong(rs.getString("text_m")));
            d.setTextE(tachDong(rs.getString("text_e")));
            d.setSecondsRest(rs.getInt("giay_hoi_sinh"));
            String kx = rs.getString("kieu_xuat_hien");
            if (kx != null && !kx.trim().isEmpty()) {
                try {
                    d.setTypeAppear(TypeAppear.valueOf(kx.trim()));
                } catch (IllegalArgumentException boQua) {
                    // Ten kieu go sai thi giu nguyen kieu cu, khong doi lieu.
                }
            }
            int[] dk = docInt(rs.getString("boss_di_kem"));
            if (dk != null && dk.length > 0) {
                d.setBossesAppearTogether(dk);
            }
            // Ba o nay chiu duoc ban CSDL chua co cot: doc loi thi giu -1, tuc
            // khong dat gi.
            try {
                d.setGiap(rs.getInt("giap"));
                d.setNeDon(rs.getInt("ne_don"));
                d.setChinhXac(rs.getInt("chinh_xac"));
            } catch (Exception thieuCot) {
                d.setGiap(-1);
                d.setNeDon(-1);
                d.setChinhXac(-1);
            }
        } catch (Exception ex) {
            Logger.logException(BossDataDAO.class, ex,
                    "Một dòng boss_data sai định dạng, giữ số cũ của mẫu đó");
        }
    }

    // ==================================================================
    //  Đổi qua lại giữa mảng và chuỗi
    // ==================================================================

    private static String soShort(short[] a) {
        if (a == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (short v : a) {
            sb.append(sb.length() > 0 ? "," : "").append(v);
        }
        return sb.toString();
    }

    private static String soInt(int[] a) {
        if (a == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int v : a) {
            sb.append(sb.length() > 0 ? "," : "").append(v);
        }
        return sb.toString();
    }

    private static String soLong(long[] a) {
        if (a == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (long v : a) {
            sb.append(sb.length() > 0 ? "," : "").append(v);
        }
        return sb.toString();
    }

    /** Kỹ năng dạng {@code id:cấp:hồi,id:cấp:hồi}. */
    private static String kyNang(int[][] a) {
        if (a == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int[] r : a) {
            if (r == null || r.length == 0) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            for (int i = 0; i < r.length; i++) {
                sb.append(i > 0 ? ":" : "").append(r[i]);
            }
        }
        return sb.toString();
    }

    private static String noiDong(String[] a) {
        return a == null || a.length == 0 ? "" : String.join(NGAN_DONG, a);
    }

    private static String[] tachDong(String s) {
        if (s == null || s.isEmpty()) {
            return new String[]{};
        }
        return s.split(NGAN_DONG, -1);
    }

    private static short[] docShort(String s) {
        int[] t = docInt(s);
        if (t == null) {
            return null;
        }
        short[] ra = new short[t.length];
        for (int i = 0; i < t.length; i++) {
            ra[i] = (short) t[i];
        }
        return ra;
    }

    private static long[] docLong(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        String[] p = s.trim().split(",");
        List<Long> ds = new ArrayList<>();
        for (String x : p) {
            try {
                ds.add(Long.parseLong(x.trim()));
            } catch (NumberFormatException boQua) {
                // Bo qua phan go sai, giu lai nhung phan doc duoc.
            }
        }
        long[] ra = new long[ds.size()];
        for (int i = 0; i < ra.length; i++) {
            ra[i] = ds.get(i);
        }
        return ra;
    }

    private static int[] docInt(String s) {
        long[] t = docLong(s);
        if (t == null) {
            return null;
        }
        int[] ra = new int[t.length];
        for (int i = 0; i < t.length; i++) {
            ra[i] = (int) t[i];
        }
        return ra;
    }

    private static int[][] docKyNang(String s) {
        if (s == null || s.trim().isEmpty()) {
            return new int[0][];
        }
        List<int[]> ds = new ArrayList<>();
        for (String phan : s.trim().split(",")) {
            String p = phan.trim();
            if (p.isEmpty()) {
                continue;
            }
            String[] o = p.split(":");
            int[] r = new int[o.length];
            boolean ok = true;
            for (int i = 0; i < o.length; i++) {
                try {
                    r[i] = Integer.parseInt(o[i].trim());
                } catch (NumberFormatException ex) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                ds.add(r);
            }
        }
        return ds.toArray(new int[0][]);
    }
}
