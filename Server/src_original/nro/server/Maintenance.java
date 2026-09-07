package nro.server;

import nro.services.Service;
import Utils.Logger;


public class Maintenance extends Thread {

    public static boolean isRunning = false;

    private static Maintenance i;

    private int time;

    private Maintenance() {

    }

    public static Maintenance gI() {
        if (i == null) {
            i = new Maintenance();
        }
        return i;
    }

    public void start(int min) {
        if (!isRunning) {
            isRunning = true;
            this.time = min;
            this.start();
        }
    }
    
    public void startNew(int min) {
        if (!isRunning) {
            isRunning = true;
            this.time = min;
            new Thread(Maintenance.gI(), "Thread Bảo Trì").start();
        }
    }
    private void autoRestartProcess() {
    try {
        int seconds = 5;
        String currentDir = System.getProperty("user.dir");
        String os = System.getProperty("os.name").toLowerCase();

        ProcessBuilder pb;

        if (os.contains("win")) {
            pb = new ProcessBuilder(
                    "cmd", "/c",
                    "start", "\"\"",
                    "cmd", "/c",
                    "timeout /t " + seconds + " /nobreak > nul && call run.bat"
            );
        } else {
            pb = new ProcessBuilder(
                    "bash", "-c",
                    "sleep " + seconds + "; ./run.sh &"
            );
        }

        pb.directory(new java.io.File(currentDir));
        pb.start();

        Logger.log(Logger.YELLOW, "AUTO RESTART: Process launched\n");

    } catch (Exception e) {
        Logger.log(Logger.RED, "AUTO RESTART FAIL: " + e.getMessage() + "\n");
    }
}


    public void startImmediately() {
        if (!isRunning) {
            isRunning = true;
            Logger.log(Logger.YELLOW, "BEGIN MAINTENANCE\n");
            ServerManager.gI().close();
        }
    }

    @Override
    public void run() {
        while (this.time > 0) {
            if (this.time == 60) {
                Service.gI().sendThongBaoAllPlayer("Hệ thống sẽ bảo trì sau 1 phút nữa hãy thoát game ngay để tránh mất mát vật phẩm.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                this.time--;
            } else if (time < 60) {
                Service.gI().sendThongBaoAllPlayer("Hệ thống sẽ bảo trì sau " + time + " giây nữa");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                this.time--;
            } else {
               int hour = this.time / 3600;
                int min = (this.time - hour * 3600) / 60;
                int sec = this.time % 60;

                String hourStr = (hour > 0) ? hour + " giờ " : "";
                String minStr = (min > 0) ? min + " phút " : "";
                String secStr = (sec > 0) ? sec + " giây " : "";
                Service.gI().sendThongBaoAllPlayer("Hệ thống sẽ bảo trì sau " + hourStr + minStr + secStr);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                this.time--;
            }
        }
        Logger.log(Logger.YELLOW, "BEGIN MAINTENANCE\n");

ServerManager.gI().close();

if (ServerManagerUI.REQUEST_AUTO_RESTART) {
    autoRestartProcess();   
}

System.exit(0);

    }
}
