package nro.server;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author MaiTienDung
 */

public class Antiddos {
    
    private static final int MAX_REQUESTS_PER_SECOND = 30;
    private static final long BLOCK_DURATION = 120 * 1000; 
    private static final Map<String, Integer> ipRequestCount = new HashMap<>();
    private static final Map<String, Timer> blockedIps = new HashMap<>(); 
   
    public static void handleRequest(String ipAddress) {
        int count = ipRequestCount.getOrDefault(ipAddress, 0) + 1;
        ipRequestCount.put(ipAddress, count);
        if (count > MAX_REQUESTS_PER_SECOND) {
            blockIP(ipAddress);
        }
        AntiddosSucces();
    }

    private static void blockIP(String ipAddress) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override

            public void run() {
                unblockIP(ipAddress);
            }
        }, BLOCK_DURATION);
        blockedIps.put(ipAddress, timer);
        ipRequestCount.put(ipAddress, 0);
        System.out.println("NROTUOITHO " + ipAddress + " block ip " + BLOCK_DURATION / 1000 + " seconds due to DDoS attack.");
    }

    private static void unblockIP(String ipAddress) {
        blockedIps.remove(ipAddress);
        System.out.println("NROTUOITHO" + ipAddress + " mở khoá block ip " + BLOCK_DURATION / 1000 + " seconds.");
    }
    
    private static void AntiddosSucces() {
        System.out.println("Anti-DDOS đã được khởi tạo thành công.");
    }
}



