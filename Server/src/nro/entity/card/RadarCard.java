package nro.entity.card;

/*
 * @author DoTheAnh
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Một thẻ trong <b>Sổ sưu tầm</b> — một dòng của bảng {@code radar}, sửa ở tab
 * "Sổ sưu tầm" của panel ({@code SoSuuTamDAO}).
 *
 * <h2>Cấp của thẻ người chơi đang giữ ({@link Card#Level})</h2>
 *
 * <ul>
 *   <li>{@code 0} — chưa có. Client vẽ ổ khoá đúng khi cấp bằng 0.</li>
 *   <li>{@code -1} — đã mở khoá (có món đầu tiên), chưa lên Lv.1.</li>
 *   <li>{@code 1..}{@link #capToiDa()} — Lv.1, Lv.2...</li>
 * </ul>
 *
 * <p>{@link #bac(Card)} gộp {@code -1} và {@code 0} thành bậc 0 để tính số
 * lượng cần.</p>
 */
public class RadarCard {

    public short Id;
    public short IconId;
    public byte Rank;
    /** Số lượng lên cấp kiểu cũ — một số cho mọi cấp. Dùng khi {@link #CanMoiCap} rỗng. */
    public int Max;
    public byte Type;
    public short Template;
    public short Head;
    public short Body;
    public short Leg;
    public short Bag;
    public String Name;
    public String Info;
    public List<OptionCard> Options;
    public short Require;
    public short RequireLevel;
    public short AuraId;

    /**
     * Số lượng cần cho từng cấp: phần tử 0 là từ lúc mở khoá lên Lv.1, phần tử
     * 1 là lên Lv.2... Độ dài chính là cấp tối đa.
     */
    public int[] CanMoiCap = new int[0];
    /** Hào quang chỉ hiện khi thẻ đang bật đạt cấp này trở lên; 0 là bật là có. */
    public int AuraTuCap;
    /** Thứ tự trong sổ, số nhỏ đứng trước. */
    public int ThuTu;

    public RadarCard() {
        Id = -1;
        IconId = -1;
        Rank = 0;
        Max = 0;
        Type = 0;
        Template = 1;
        Head = -1;
        Body = -1;
        Leg = -1;
        Bag = -1;
        Name = "";
        Info = "";
        Options = new ArrayList<>();
        Require = -1;
        RequireLevel = 0;
        AuraId = -1;
    }

    /**
     * Cấp tối đa.
     *
     * <p>Dòng kiểu cũ (chưa khai số lượng từng cấp) lấy theo dòng chỉ số có cấp
     * cao nhất — đúng những cấp mà mô tả thẻ đã hứa với người chơi.</p>
     */
    public int capToiDa() {
        if (CanMoiCap != null && CanMoiCap.length > 0) {
            return CanMoiCap.length;
        }
        int cao = 1;
        if (Options != null) {
            for (OptionCard o : Options) {
                if (o != null && o.active > cao) {
                    cao = o.active;
                }
            }
        }
        return cao;
    }

    /** Số lượng cần để đi từ bậc {@code bac} lên bậc kế tiếp; ít nhất 1. */
    public int canLenCap(int bac) {
        int v;
        if (CanMoiCap != null && CanMoiCap.length > 0) {
            v = CanMoiCap[Math.max(0, Math.min(bac, CanMoiCap.length - 1))];
        } else {
            v = Max;
        }
        return Math.max(1, v);
    }

    /** Bậc để tính: chưa có và vừa mở khoá đều là 0, còn lại là số Lv. */
    public static int bac(Card c) {
        return c == null || c.Level < 0 ? 0 : c.Level;
    }
}
