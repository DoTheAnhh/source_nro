package QuanLiBoss.Manager;

/*
 * @Author: Văn KHải
 */

public class ValentineEventManager extends BossManager {

    private static ValentineEventManager instance;

    public static ValentineEventManager gI() {
        if (instance == null) {
            instance = new ValentineEventManager();
        }
        return instance;
    }

}
