package nro.entity.card;

import java.util.ArrayList;
import java.util.List;

/*
 * @author DoTheAnh
 */

/**
 * Thẻ người chơi đang giữ trong Sổ sưu tầm. Cấp: xem {@link RadarCard}.
 *
 * <p>{@link #Options} chỉ để lưu cho khớp dữ liệu cũ; chỉ số thật luôn đọc từ
 * mẫu thẻ ({@link RadarCard}), để sửa trên panel là mọi người nhận ngay.</p>
 */
public class Card {

    public short Id;
    /** Số đã góp vào cấp đang lên. Kiểu {@code int}: một cấp có thể cần hơn 127 món. */
    public int Amount;
    public int MaxAmount;
    public byte Level;
    public byte Used;
    public List<OptionCard> Options;

    public Card() {
        Id = -1;
        Amount = 0;
        MaxAmount = 0;
        Level = 0;
        Used = 0;
        Options = new ArrayList<>();
    }

    public Card(int m, List<OptionCard> o) {
        MaxAmount = m;
        Options = o;
    }

    public Card(short i, int a, int ma, byte le, List<OptionCard> o) {
        Id = i;
        Amount = a;
        MaxAmount = ma;
        Level = le;
        Options = o;
    }

    /**
     * Dựng lại từ dữ liệu đã lưu.
     *
     * <p>Giữ nguyên cấp {@code -1} (đã mở khoá, chưa Lv.1). Bản cũ đổi -1 thành 1
     * lúc nạp — thoát game vào lại là thẻ tự lên Lv.1 không cần góp.</p>
     */
    public Card(short i, int a, int ma, byte le, List<OptionCard> o, byte u) {
        Id = i;
        Amount = a;
        MaxAmount = ma;
        Level = le;
        Options = o;
        Used = u;
    }

    @Override
    public String toString() {
        final String n = "\"";
        return "{"
                + n + "id" + n + ":" + n + Id + n + ","
                + n + "amount" + n + ":" + n + Amount + n + ","
                + n + "max" + n + ":" + n + MaxAmount + n + ","
                + n + "option" + n + ":" + Options + ","
                + n + "level" + n + ":" + n + Level + n + ","
                + n + "used" + n + ":" + n + Used + n
                + "}";
    }
}
