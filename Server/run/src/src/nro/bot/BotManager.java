package nro.bot;

import Utils.Logger;
import nro.server.ServerManager;
import java.util.ArrayList;
import java.util.List;
import utils.Functions;

public class BotManager implements Runnable {

    public static BotManager i;
    
    public List<Bot> bot =  new ArrayList<>();
    
    public List<Bot> getBot() {
        return this.bot;
    }
    
    public static BotManager gI(){
        if (i == null) {
            i = new BotManager();
        }
        return i;
    }
    
    @Override
    public void run() {
        final int delay = 130;
        while (ServerManager.isRunning) {
            long startTime = System.currentTimeMillis();

            // Cập nhật bot
            for (int j = bot.size() - 1; j >= 0; j--) {
                Bot currentBot = bot.get(j);
                if (currentBot != null) {
                    try {
                        currentBot.update();
                    } catch (Exception e) {
                        Logger.logException(BotManager.class, e);
                    }
                }
            }

            // Tính thời gian cần sleep để giữ chu kỳ đều đặn
            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = Math.max(delay - elapsed, 10);

            try {
                Functions.sleep(sleepTime);
            } catch (Exception e) {
                Logger.logException(BotManager.class, e);
            }
        }
    }
}