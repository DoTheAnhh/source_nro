package Utils;

import nro.player.Player;
import nro.server.Client;
import java.lang.instrument.Instrumentation;
/**
 *
 * @author Văn Khải
 */
public class GetSizeObject {
    
     private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        System.out.println("Init Size");
    }

    public static long getSizeObject(Object o) {
        if (instrumentation == null) return 0;
        return instrumentation.getObjectSize(o);
    }
    public static long sizeListPlayer(){
        long sum = 0;
        for (Player pl : Client.gI().getPlayers()) {
            if (pl != null) sum += getSizeObject(pl);
        }
        return sum;
    }
    public static long sizeListBoss(){
        long sum = 0;
        
        return sum;
    }
}
