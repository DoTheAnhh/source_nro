package nro.repository.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nro.core.log.Logger;
import nro.entity.item.Item;
import nro.entity.item.ItemOption;
import nro.entity.player.Player;
import nro.entity.skill.Skill;
import nro.repository.ConnectDB;
import nro.repository.CrisResultSet;

/** Bonus kỹ năng lấy trực tiếp từ option trên các món đang mặc. */
public final class TrangBiBonusDAO {

    public static final class CauHinh {
        public int optionId;
        public String loai;
        public int thamSo;
        public boolean active;
        public String ghiChu;
    }

    private static final Map<Integer, CauHinh> THEO_OPTION = new HashMap<>();
    private static volatile boolean loaded;

    private TrangBiBonusDAO() {
    }

    /** Tạo bảng, gieo đủ option mặc định và nạp cache. Gọi lại không tạo trùng. */
    public static synchronized void damBaoVaGieo() {
        nro.repository.schema.LuocDoPanel.damBao();
        try {
            gieo("troi_giam_giap_pct", Skill.TROI,
                    "Khi trói giảm #% giáp mục tiêu");
            // Sat thuong De Trung: mot ma duy nhat la sat thuong chieu 12. Ma cu
            // detrung_dame_pct doi sang ma do (xem gopMaTrung) — khong gieo lai.
            gopMaTrung();
            gieo("skill_crit_pct", Skill.DE_TRUNG,
                    "Tỉ lệ chí mạng pet Đẻ Trứng +#%");
            gieo("skill_sdcm_pct", Skill.DE_TRUNG,
                    "Sát thương chí mạng pet Đẻ Trứng +#%");
            gieo("dame_boss_pct", -1, "Sát thương lên Boss +#%");

            int[] gayDame = {Skill.DRAGON, Skill.KAMEJOKO, Skill.DEMON,
                Skill.MASENKO, Skill.GALICK, Skill.ANTOMIC, Skill.KAIOKEN,
                Skill.MAKANKOSAPPO, Skill.LIEN_HOAN, Skill.DICH_CHUYEN_TUC_THOI,
                Skill.SUPER_KAME, Skill.LIEN_HOAN_CHUONG};
            for (int id : gayDame) {
                String ten = SetBonusDAO.CHIEU.getOrDefault(id, "Kỹ năng " + id);
                gieo("skill_pct", id, "Sát thương " + ten + " +#%");
                gieo("skill_crit_pct", id, "Chí mạng " + ten + " +#%");
                gieo("skill_sdcm_pct", id, "Sát thương chí mạng " + ten + " +#%");
                gieo("skill_xuyen_giap_pct", id, "Xuyên giáp " + ten + " +#%");
                gieo("skill_burn_hp_pct", id,
                        ten + " thiêu đốt #% HP tối đa/giây");
                gieo("skill_slow_pct", id,
                        ten + " giảm #% tốc chạy mục tiêu");
                gieo("skill_attack_slow_pct", id,
                        ten + " giảm #% tốc ra đòn mục tiêu");
                gieo("skill_weaken_pct", id,
                        ten + " giảm #% sát thương mục tiêu");
                gieo("skill_stun_chance_pct", id,
                        ten + " có #% tỉ lệ gây choáng");
                gieo("skill_debuff_chance_pct", id,
                        ten + " có #% tỉ lệ gây hiệu ứng phụ");
                gieo("skill_debuff_duration_pct", id,
                        ten + " tăng #% thời gian hiệu ứng phụ");
            }

            // QCKK và Tự Phát Nổ không chí mạng, nhưng từng mục tiêu trúng đòn
            // vẫn có thể nhận hiệu ứng phụ đã cấu hình.
            int[] gayHieuUngRieng = {Skill.QUA_CAU_KENH_KHI, Skill.TU_SAT};
            for (int id : gayHieuUngRieng) {
                String ten = SetBonusDAO.CHIEU.getOrDefault(id, "Kỹ năng " + id);
                gieo("skill_pct", id, "Sát thương " + ten + " +#%");
                gieo("skill_burn_hp_pct", id, ten + " thiêu đốt #% HP tối đa/giây");
                gieo("skill_slow_pct", id, ten + " giảm #% tốc chạy mục tiêu");
                gieo("skill_attack_slow_pct", id, ten + " giảm #% tốc ra đòn mục tiêu");
                gieo("skill_weaken_pct", id, ten + " giảm #% sát thương mục tiêu");
                gieo("skill_stun_chance_pct", id, ten + " có #% tỉ lệ gây choáng");
                gieo("skill_debuff_chance_pct", id, ten + " có #% tỉ lệ gây hiệu ứng phụ");
                gieo("skill_debuff_duration_pct", id, ten + " tăng #% thời gian hiệu ứng phụ");
            }
            gieo("skill_pct", Skill.DE_TRUNG, "Sát thương kỹ năng Đẻ Trứng +#%");

            // Chieu khong gay sat thuong cung co chi so rieng: thoi gian tac dung
            // (choang, ngu, troi, khien, bien khi…) va hieu luc (luong hoi, % mau).
            for (int id : LoaiChiSo.CHIEU_THOI_GIAN) {
                gieo("skill_duration_pct", id, tenChuan("skill_duration_pct", id));
            }
            for (int id : LoaiChiSo.CHIEU_HIEU_LUC) {
                gieo("skill_power_pct", id, tenChuan("skill_power_pct", id));
            }
            chuanHoaTen();

            int[] nhieuMucTieu = {Skill.DRAGON, Skill.KAMEJOKO, Skill.DEMON,
                Skill.MASENKO, Skill.GALICK, Skill.ANTOMIC, Skill.KAIOKEN,
                Skill.MAKANKOSAPPO, Skill.SOCOLA,
                Skill.DICH_CHUYEN_TUC_THOI, Skill.THOI_MIEN, Skill.TROI,
                Skill.LIEN_HOAN,
                Skill.SUPER_KAME, Skill.LIEN_HOAN_CHUONG, Skill.MA_PHONG_BA};
            for (int id : nhieuMucTieu) {
                gieo("skill_target_add", id, "Thêm # mục tiêu cho "
                        + SetBonusDAO.CHIEU.getOrDefault(id, "Kỹ năng " + id));
            }

            int[] nhanhHon = {Skill.DRAGON, Skill.KAMEJOKO, Skill.DEMON,
                Skill.MASENKO, Skill.GALICK, Skill.ANTOMIC, Skill.KAIOKEN,
                Skill.QUA_CAU_KENH_KHI, Skill.MAKANKOSAPPO, Skill.DE_TRUNG,
                Skill.TU_SAT, Skill.LIEN_HOAN, Skill.SUPER_KAME,
                Skill.LIEN_HOAN_CHUONG, Skill.MA_PHONG_BA};
            for (int id : nhanhHon) {
                gieo("skill_cast_speed_pct", id, "Tốc độ ra đòn "
                        + SetBonusDAO.CHIEU.getOrDefault(id, "Kỹ năng " + id) + " +#%");
            }
        } catch (Exception ex) {
            Logger.logException(TrangBiBonusDAO.class, ex,
                    "Không gieo được option bonus trang bị");
        }
        reload();
    }

    /** Đổi mã cũ trùng nghĩa sang mã chung (xem {@code SetBonusDAO.gopLoaiTrung}). */
    private static void gopMaTrung() {
        try {
            ConnectDB.executeUpdate("UPDATE trang_bi_bonus SET loai = 'skill_pct', tham_so = ?"
                    + " WHERE loai = 'detrung_dame_pct'", (int) Skill.DE_TRUNG);
        } catch (Exception ex) {
            Logger.logException(TrangBiBonusDAO.class, ex, "Không gộp được mã Đẻ Trứng");
        }
    }

    /**
     * Tên chuẩn của một chỉ số theo chiêu — chữ in lên món đồ.
     *
     * <p>Mẫu {@code "<Chiêu>: <tác dụng>"}: chiêu đứng đầu nên mọi chỉ số của một
     * chiêu nằm liền nhau khi sắp theo tên, và gõ tên chiêu là ra đủ. Bản cũ lấy
     * nguyên nhãn panel "Trái Đất — Kamejoko" chèn vào giữa câu, in lên đồ thành
     * "Sát thương Trái Đất — Kamejoko +5%".</p>
     */
    public static String tenChuan(String loai, int thamSo) {
        String ch = LoaiChiSo.tenChieu(thamSo);
        switch (loai) {
            case "skill_pct": return ch + ": +#% sát thương";
            case "skill_crit_pct": return ch + ": +#% tỉ lệ chí mạng";
            case "skill_sdcm_pct": return ch + ": +#% sát thương chí mạng";
            case "skill_xuyen_giap_pct": return ch + ": +#% xuyên giáp";
            case "skill_burn_hp_pct": return ch + ": thiêu đốt #% HP tối đa mỗi giây";
            case "skill_slow_pct": return ch + ": làm chậm #% tốc chạy mục tiêu";
            case "skill_attack_slow_pct": return ch + ": làm chậm #% tốc ra đòn mục tiêu";
            case "skill_weaken_pct": return ch + ": giảm #% sát thương mục tiêu";
            case "skill_stun_chance_pct": return ch + ": #% tỉ lệ gây choáng";
            case "skill_debuff_chance_pct": return ch + ": #% tỉ lệ gây hiệu ứng phụ";
            case "skill_debuff_duration_pct": return ch + ": +#% thời gian hiệu ứng phụ";
            case "skill_target_add": return ch + ": thêm # mục tiêu";
            case "skill_cast_speed_pct": return ch + ": +#% tốc độ ra đòn";
            case "skill_duration_pct": return ch + ": +#% thời gian tác dụng";
            case "skill_power_pct": return ch + ": +#% hiệu lực";
            case "hoi_chieu_skill_pct": return ch + ": giảm #% hồi chiêu";
            case "lam_moi_pct": return ch + ": #% tỉ lệ làm mới";
            case "troi_giam_giap_pct": return "Trói: giảm #% giáp mục tiêu";
            case "dame_boss_pct": return "+#% sát thương lên Boss";
            default: return null;
        }
    }

    /**
     * Đặt lại tên mọi chỉ số theo chiêu cho đúng mẫu {@link #tenChuan}.
     *
     * <p>Chỉ đổi dòng đang khác tên, nên từ lần khởi động thứ hai trở đi không
     * làm gì. Id giữ nguyên — đồ người chơi đang cầm chỉ đổi chữ.</p>
     */
    private static void chuanHoaTen() {
        CrisResultSet rs = null;
        List<Object[]> can = new ArrayList<>();
        try {
            rs = ConnectDB.executeQuery("SELECT option_id, loai, tham_so FROM trang_bi_bonus");
            while (rs.next()) {
                can.add(new Object[]{rs.getInt("option_id"), rs.getString("loai"),
                    rs.getInt("tham_so")});
            }
        } catch (Exception ex) {
            Logger.logException(TrangBiBonusDAO.class, ex, "Không đọc được trang_bi_bonus");
            return;
        } finally {
            dispose(rs);
        }
        for (Object[] o : can) {
            int id = (Integer) o[0];
            String ten = tenChuan((String) o[1], (Integer) o[2]);
            if (ten == null) {
                continue;
            }
            String cu = null;
            try {
                if (nro.server.Manager.ITEM_OPTION_TEMPLATES != null
                        && id < nro.server.Manager.ITEM_OPTION_TEMPLATES.size()) {
                    cu = nro.server.Manager.ITEM_OPTION_TEMPLATES.get(id).name;
                }
            } catch (Exception boQua) {
                // Bang chua nap: doi thang.
            }
            if (!ten.equals(cu)) {
                ChiSoOptionDAO.doiTen(id, ten);
            }
        }
    }

    /**
     * Chỉ số này có <b>trùng tác dụng</b> với một chỉ số id nhỏ hơn không.
     *
     * <p>Sau khi gộp mã, hai chỉ số khác id có thể cùng một tác dụng (đồ cũ vẫn
     * mang id cũ nên không xoá được). Ô chọn chỉ số ẩn id lớn hơn để không hiện
     * hai dòng giống hệt nhau.</p>
     */
    public static boolean laOptionTrung(int optionId) {
        ensureLoaded();
        CauHinh c = THEO_OPTION.get(optionId);
        if (c == null) {
            return false;
        }
        for (CauHinh k : THEO_OPTION.values()) {
            if (k.optionId < optionId && k.thamSo == c.thamSo
                    && k.loai != null && k.loai.equals(c.loai)) {
                return true;
            }
        }
        return false;
    }

    /** Id chỉ số trên đồ ứng với một tác dụng + chiêu, {@code -1} nếu chưa có. */
    public static int optionCua(String loai, int thamSo) {
        ensureLoaded();
        int nho = -1;
        for (CauHinh k : THEO_OPTION.values()) {
            if (k.thamSo == thamSo && loai != null && loai.equals(k.loai)
                    && (nho < 0 || k.optionId < nho)) {
                nho = k.optionId;
            }
        }
        return nho;
    }

    private static void gieo(String loai, int thamSo, String ten) throws Exception {
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT option_id FROM trang_bi_bonus WHERE loai = ? AND tham_so = ?",
                    loai, thamSo);
            if (rs.next()) {
                return;
            }
        } finally {
            dispose(rs);
        }
        int optionId = ChiSoOptionDAO.them(ten, 0);
        if (optionId < 0) {
            throw new IllegalStateException("Không tạo được option " + ten);
        }
        ConnectDB.executeUpdate(
                "INSERT INTO trang_bi_bonus(option_id, loai, tham_so, active, ghi_chu)"
                + " VALUES (?, ?, ?, 1, ?)",
                optionId, loai, thamSo, "Tự tạo khi khởi động server");
    }

    public static synchronized void reload() {
        nro.repository.schema.LuocDoPanel.damBao();
        Map<Integer, CauHinh> moi = new HashMap<>();
        CrisResultSet rs = null;
        try {
            rs = ConnectDB.executeQuery(
                    "SELECT * FROM trang_bi_bonus WHERE active = 1");
            while (rs.next()) {
                CauHinh c = new CauHinh();
                c.optionId = rs.getInt("option_id");
                c.loai = rs.getString("loai");
                c.thamSo = rs.getInt("tham_so");
                c.active = true;
                c.ghiChu = rs.getStringOrNull("ghi_chu");
                moi.put(c.optionId, c);
            }
        } catch (Exception ex) {
            Logger.logException(TrangBiBonusDAO.class, ex,
                    "Không đọc được trang_bi_bonus");
        } finally {
            dispose(rs);
        }
        THEO_OPTION.clear();
        THEO_OPTION.putAll(moi);
        loaded = true;
    }

    public static int giaTri(Player player, String loai, int thamSo) {
        if (player == null || player.inventory == null
                || player.inventory.itemsBody == null || loai == null) {
            return 0;
        }
        ensureLoaded();
        long tong = 0;
        for (Item item : player.inventory.itemsBody) {
            if (item == null || !item.isNotNullItem() || item.itemOptions == null) {
                continue;
            }
            for (ItemOption io : item.itemOptions) {
                if (io == null || io.optionTemplate == null) {
                    continue;
                }
                CauHinh c = THEO_OPTION.get(io.optionTemplate.id);
                if (c != null && loai.equals(c.loai)
                        && (c.thamSo == thamSo || c.thamSo == -1)) {
                    tong += io.param;
                }
            }
        }
        if (tong > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (tong < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) tong;
    }

    /** Tổng theo loại, bỏ qua kỹ năng được ánh xạ; dùng cho bonus loại toàn cục. */
    public static int giaTriTheoLoai(Player player, String loai) {
        if (player == null || player.inventory == null
                || player.inventory.itemsBody == null || loai == null) {
            return 0;
        }
        ensureLoaded();
        long tong = 0;
        for (Item item : player.inventory.itemsBody) {
            if (item == null || !item.isNotNullItem() || item.itemOptions == null) {
                continue;
            }
            for (ItemOption io : item.itemOptions) {
                if (io == null || io.optionTemplate == null) {
                    continue;
                }
                CauHinh c = THEO_OPTION.get(io.optionTemplate.id);
                if (c != null && loai.equals(c.loai)) {
                    tong += io.param;
                }
            }
        }
        return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, tong));
    }

    public static List<CauHinh> tatCa() {
        ensureLoaded();
        List<CauHinh> ra = new ArrayList<>(THEO_OPTION.values());
        Collections.sort(ra, (a, b) -> Integer.compare(a.optionId, b.optionId));
        return ra;
    }

    private static void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }

    private static void dispose(CrisResultSet rs) {
        if (rs != null) {
            try {
                rs.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
