package nro.server;

import nro.core.log.Logger;
import nro.service.Service;
import java.io.File;

public class Maintenance implements Runnable {

    public static volatile boolean isRunning = false;

    /**
     * Sau khi bảo trì xong có tự khởi động lại server hay không.
     *
     * <p>Trước đây cờ này nằm trong {@code DashboardPanel} — tức là một hằng số
     * điều khiển vòng đời server lại bị giữ trong một lớp giao diện. Nay chuyển về
     * đúng chỗ: lớp thực sự dùng nó. Panel quản trị chỉ việc gán
     * {@code Maintenance.REQUEST_AUTO_RESTART = true/false}.</p>
     */
    public static volatile boolean REQUEST_AUTO_RESTART = false;

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

    /**
     * min = phút
     */
    public synchronized void start(int min) {
        Logger.log(Logger.YELLOW, "[MAINT] start(" + min + ") called | isRunning=" + isRunning + "\n");

        if (isRunning) {
            Logger.log(Logger.RED, "[MAINT] bỏ qua vì bảo trì đang chạy\n");
            return;
        }

        isRunning = true;
        this.time = min * 60;
        Logger.log(Logger.YELLOW, "[MAINT] thread starting | time=" + this.time + " seconds\n");

        new Thread(this, "Thread Bao Tri").start();
    }

    /**
     * sec = giây
     */
    public synchronized void startSeconds(int sec) {
        Logger.log(Logger.YELLOW, "[MAINT] startSeconds(" + sec + ") called | isRunning=" + isRunning + "\n");

        if (isRunning) {
            Logger.log(Logger.RED, "[MAINT] bỏ qua vì bảo trì đang chạy\n");
            return;
        }

        isRunning = true;
        this.time = sec;
        Logger.log(Logger.YELLOW, "[MAINT] thread starting | time=" + this.time + " seconds\n");

        new Thread(this, "Thread Bao Tri").start();
    }

    public void startNew(int min) {
        start(min);
    }

    private void autoRestartProcess() {
        try {
            int seconds = 5;
            String currentDir = System.getProperty("user.dir");
            String os = System.getProperty("os.name").toLowerCase();

            Logger.log(Logger.YELLOW, "[RESTART] begin | dir=" + currentDir + " | os=" + os + "\n");

            File runBat = new File(currentDir, "run.bat");
            File runSh = new File(currentDir, "run.sh");

            Logger.log(Logger.YELLOW, "[RESTART] run.bat exists=" + runBat.exists()
                    + " | path=" + runBat.getAbsolutePath() + "\n");
            Logger.log(Logger.YELLOW, "[RESTART] run.sh exists=" + runSh.exists()
                    + " | path=" + runSh.getAbsolutePath() + "\n");

            ProcessBuilder pb;

            if (os.contains("win")) {
                String cmd = "timeout /t " + seconds + " /nobreak > nul && call run.bat";
                Logger.log(Logger.YELLOW, "[RESTART] windows cmd=" + cmd + "\n");

                pb = new ProcessBuilder(
                        "cmd", "/c",
                        "start", "",
                        "cmd", "/c",
                        cmd
                );
            } else {
                String cmd = "sleep " + seconds + "; ./run.sh &";
                Logger.log(Logger.YELLOW, "[RESTART] linux cmd=" + cmd + "\n");

                pb = new ProcessBuilder(
                        "bash", "-c",
                        cmd
                );
            }

            pb.directory(new File(currentDir));
            pb.redirectErrorStream(true);

            Logger.log(Logger.YELLOW, "[RESTART] processBuilder directory="
                    + pb.directory().getAbsolutePath() + "\n");

            pb.start();

            Logger.log(Logger.YELLOW, "[RESTART] Process launched OK\n");

        } catch (Exception e) {
            Logger.log(Logger.RED, "[RESTART] FAIL: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    public synchronized void startImmediately() {
        if (isRunning) {
            Logger.log(Logger.RED, "[MAINT] startImmediately bỏ qua vì đang chạy\n");
            return;
        }

        isRunning = true;

        try {
            Logger.log(Logger.YELLOW, "[MAINT] BEGIN MAINTENANCE IMMEDIATELY\n");
            Logger.log(Logger.YELLOW, "[MAINT] REQUEST_AUTO_RESTART=" + REQUEST_AUTO_RESTART + "\n");

            if (REQUEST_AUTO_RESTART) {
                Logger.log(Logger.YELLOW, "[MAINT] calling autoRestartProcess() before close\n");
                autoRestartProcess();
                Thread.sleep(1000);
            } else {
                Logger.log(Logger.RED, "[MAINT] skip auto restart because REQUEST_AUTO_RESTART=false\n");
            }

            Logger.log(Logger.YELLOW, "[MAINT] calling ServerManager.gI().close()\n");
            ServerManager.gI().close();
            Logger.log(Logger.YELLOW, "[MAINT] after close\n");

            Logger.log(Logger.YELLOW, "[MAINT] calling System.exit(0)\n");
            System.exit(0);

        } catch (Exception e) {
            Logger.log(Logger.RED, "[MAINT] startImmediately error: " + e.getMessage() + "\n");
            e.printStackTrace();
        } finally {
            isRunning = false;
        }
    }

    @Override
    public void run() {
        try {
            Logger.log(Logger.YELLOW, "[MAINT] run() entered | time=" + this.time
                    + " | autoRestart=" + REQUEST_AUTO_RESTART + "\n");

            while (this.time > 0) {
                Logger.log(Logger.YELLOW, "[MAINT] countdown=" + this.time + "\n");

                if (this.time == 60) {
                    Service.gI().sendThongBaoAllPlayer(
                            "Hệ thống sẽ bảo trì sau 1 phút nữa, hãy thoát game ngay để tránh mất mát vật phẩm."
                    );
                } else if (this.time < 60) {
                    Service.gI().sendThongBaoAllPlayer(
                            "Hệ thống sẽ bảo trì sau " + this.time + " giây nữa"
                    );
                } else {
                    int hour = this.time / 3600;
                    int min = (this.time - hour * 3600) / 60;
                    int sec = this.time % 60;

                    String hourStr = (hour > 0) ? hour + " giờ " : "";
                    String minStr = (min > 0) ? min + " phút " : "";
                    String secStr = (sec > 0) ? sec + " giây " : "";

                    Service.gI().sendThongBaoAllPlayer(
                            "Hệ thống sẽ bảo trì sau " + hourStr + minStr + secStr
                    );
                }

                Thread.sleep(1000);
                this.time--;
            }

            Logger.log(Logger.YELLOW, "[MAINT] countdown done -> BEGIN MAINTENANCE\n");
            Logger.log(Logger.YELLOW, "[MAINT] before close | autoRestart="
                    + REQUEST_AUTO_RESTART + "\n");

            if (REQUEST_AUTO_RESTART) {
                Logger.log(Logger.YELLOW, "[MAINT] calling autoRestartProcess()\n");
                autoRestartProcess();
                Logger.log(Logger.YELLOW, "[MAINT] autoRestartProcess() returned\n");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logger.log(Logger.RED, "[MAINT] sleep after restart interrupted\n");
                }
            } else {
                Logger.log(Logger.RED, "[MAINT] skip auto restart because REQUEST_AUTO_RESTART=false\n");
            }

            Logger.log(Logger.YELLOW, "[MAINT] calling ServerManager.gI().close()\n");
            ServerManager.gI().close();
            Logger.log(Logger.YELLOW, "[MAINT] after close\n");

            Logger.log(Logger.YELLOW, "[MAINT] calling System.exit(0)\n");
            System.exit(0);

        } catch (Exception e) {
            Logger.log(Logger.RED, "[MAINT] ERROR: " + e.getMessage() + "\n");
            e.printStackTrace();
        } finally {
            isRunning = false;
            Logger.log(Logger.YELLOW, "[MAINT] finished | isRunning=false\n");
        }
    }
}