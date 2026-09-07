package nro.entity.boss;

import lombok.Builder;
import lombok.Data;


@Data
public class BossData {

   public static final int DEFAULT_APPEAR = 0;
    public static final int APPEAR_WITH_ANOTHER = 1;
    public static final int ANOTHER_LEVEL = 2;

    private String name;

    private byte gender;

    private short[] outfit;

    private long dame;

    private long[] hp;

    private int[] mapJoin;

    private int[][] skillTemp;

    private String[] textS;

    private String[] textM;

    private String[] textE;

    private int secondsRest;

    /**
     * Giáp, né đòn, chính xác của mẫu boss này. {@code -1} là giữ nguyên hành vi
     * cũ — không đặt gì, boss dùng chỉ số mặc định.
     *
     * <p>Né và chính xác tính bằng phần trăm, cùng thang với bảng chỉ số của
     * người chơi: chính xác của người đánh trừ thẳng vào né của boss, dư thì bỏ.
     * Trước đây mẫu boss chỉ khai được HP và sức đánh, nên mọi con đều có giáp 0
     * và không bao giờ né được đòn nào.</p>
     */
    private int giap = -1;

    private int neDon = -1;

    private int chinhXac = -1;

    public int getGiap() {
        return this.giap;
    }

    public void setGiap(int giap) {
        this.giap = giap;
    }

    public int getNeDon() {
        return this.neDon;
    }

    public void setNeDon(int neDon) {
        this.neDon = neDon;
    }

    public int getChinhXac() {
        return this.chinhXac;
    }

    public void setChinhXac(int chinhXac) {
        this.chinhXac = chinhXac;
    }

    private TypeAppear typeAppear;

    private int[] bossesAppearTogether;
    
    private BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE) {
        this.name = name;
        this.gender = gender;
        this.outfit = outfit;
        this.dame = dame;
        this.hp = hp;
        this.mapJoin = mapJoin;
        this.skillTemp = skillTemp;
        this.textS = textS;
        this.textM = textM;
        this.textE = textE;
        this.secondsRest = 0;
        this.typeAppear = typeAppear.DEFAULT_APPEAR;
    }
    
    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE);
        this.secondsRest = secondsRest;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest, int[] bossesAppearTogether) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE, secondsRest);
        this.bossesAppearTogether = bossesAppearTogether;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, TypeAppear typeAppear) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE);
        this.typeAppear = typeAppear;
    }

    public BossData(String name, byte gender, short[] outfit, long dame, long[] hp,
            int[] mapJoin, int[][] skillTemp, String[] textS, String[] textM,
            String[] textE, int secondsRest, TypeAppear typeAppear) {
        this(name, gender, outfit, dame, hp, mapJoin, skillTemp, textS, textM, textE, secondsRest);
        this.typeAppear = typeAppear;
    }

    @Builder
    public BossData(String name, byte gender, long dame, long[] hp,
                    short[] outfit, int[] mapJoin, int[][] skillTemp,
                    int secondsRest, String[] textS, String[] textM,
                    String[] textE) {
        this.name = name;
        this.gender = gender;
        this.dame = dame;
        this.hp = hp;
        this.outfit = outfit;
        this.mapJoin = mapJoin;
        this.skillTemp = skillTemp;
        this.secondsRest = secondsRest;
        this.textS = new String[]{};
        this.textM = new String[]{};
        this.textE = new String[]{};
    }
}




