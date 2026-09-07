package nro.task;

/**
 *
 * @author MaiTienDung
 */

public class EventTaskTemplate {

    public int id;
    public String name;
    public int[][] count;

    public EventTaskTemplate() {
        this.count = new int[5][2];
    }
}
