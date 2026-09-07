package QuanLiBoss.Manager;

/*
 * @Author: Văn KHải
 */

public class VuLanEventManager extends BossManager {

    private static VuLanEventManager instance;

    public static VuLanEventManager gI() {
        if (instance == null) {
            instance = new VuLanEventManager();
        }
        return instance;
    }

}
