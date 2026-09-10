package nro.entity.skill;

import nro.entity.template.SkillTemplate;

public class Skill {
    
    public static final int RANGE_ATTACK_CHIEU_DAM = 100;
    public static final int RANGE_ATTACK_CHIEU_CHUONG = 300;

    public static final byte DEMON = 2;
    public static final byte MASENKO = 3;
    public static final byte TRI_THUONG = 7;
    public static final byte MAKANKOSAPPO = 11;
    public static final byte DE_TRUNG = 12;
    public static final byte LIEN_HOAN = 17;
    public static final byte SOCOLA = 18;

    public static final byte GALICK = 4;
    public static final byte ANTOMIC = 5;
    public static final byte TAI_TAO_NANG_LUONG = 8;
    public static final byte BIEN_KHI = 13;
    public static final byte TU_SAT = 14;
    public static final byte HUYT_SAO = 21;
    public static final byte TROI = 23;

    public static final byte DRAGON = 0;
    public static final byte KAMEJOKO = 1;
    public static final byte THAI_DUONG_HA_SAN = 6;
    public static final byte KAIOKEN = 9;
    public static final byte QUA_CAU_KENH_KHI = 10;
    public static final byte DICH_CHUYEN_TUC_THOI = 20;
    public static final byte THOI_MIEN = 22;

    public static final byte KHIEN_NANG_LUONG = 19;
    
    public static final byte SUPER_KAME = 24;
    public static final byte LIEN_HOAN_CHUONG = 25;
    public static final byte MA_PHONG_BA = 26;
    
    public SkillTemplate template;

    public short skillId;

    public int point;

    public long powRequire;

    public int coolDown;

    public long lastTimeUseThisSkill;

    /**
     * Mốc sớm nhất được dùng lại chiêu này — <b>đồng hồ ảo</b> của thùng nhịp.
     *
     * <p>Khác {@link #lastTimeUseThisSkill} ở chỗ nó không phải "lần cuối đã
     * đánh" mà là "lần tới được phép đánh". Mỗi đòn ăn được đẩy nó lên đúng một
     * vòng hồi chiêu tính từ <b>mốc cũ</b>, chứ không phải từ lúc gói tới. Nhờ
     * thế một chùm gói dồn (mạng vấp rồi thông) vẫn ăn đủ, mà nhịp đánh trung
     * bình dài hạn vẫn đúng bằng hồi chiêu.</p>
     *
     * <p>Chỉ dùng cho chiêu đánh liên tục — xem {@code SkillService}.</p>
     */
    public long mocSanSang;

    public long lastTimeUseThisSkillbot;

    public int dx;

    public int dy;

    public int maxFight;

    public int manaUse;

    public short damage;

    public short currLevel;

    public String moreInfo;

    public short price;

    public Skill() {

    }

    public Skill(Skill skill) {
        this.skillId = skill.skillId;
        this.point = skill.point;
        this.powRequire = skill.powRequire;
        this.coolDown = skill.coolDown;
        this.lastTimeUseThisSkill = skill.lastTimeUseThisSkill;
        this.dx = skill.dx;
        this.dy = skill.dy;
        this.maxFight = skill.maxFight;
        this.manaUse = skill.manaUse;
        this.damage = skill.damage;
        this.moreInfo = skill.moreInfo;
        this.price = skill.price;
        this.template = skill.template;
    }

    public void dispose() {
        this.template = null;
    }
}





