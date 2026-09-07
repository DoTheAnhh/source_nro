package QuanLiBoss.Manager;

/*
 * @Author: MaiTienDung
 */

public class BossNomalManager extends BossManager {

    private static BossNomalManager instance;

    public static BossNomalManager gI() {
        if (instance == null) {
            instance = new BossNomalManager();
        }
        return instance;
    }

}
