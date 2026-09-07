package nro.server;

import nro.bot.BotManager;
import nro.bot.NewBot;
import nro.bot.SellBot;
import nro.bot.ShopBot;
import java.net.ServerSocket;
import jbcd.dao.HistoryTransactionDAO;
import nro.clan.ClanService;
import nro.map.The23rdMartialArtCongress.The23rdMartialArtCongressManager;
import nro.services.Service;
import Utils.Logger;
import Utils.TimeUtil;
import Utils.Util;
import QuanLiBoss.Manager.BossManager;
import QuanLiBoss.Manager.BossNomalManager;
import QuanLiBoss.Manager.BossOfTheGangsManager;
import QuanLiBoss.Manager.BrolyManager;
import QuanLiBoss.Manager.ChristmasEventManager;
import QuanLiBoss.Manager.FinalBossManager;
import QuanLiBoss.Manager.GasDestroyManager;
import QuanLiBoss.Manager.HalloweenEventManager;
import QuanLiBoss.Manager.HungVuongEventManager;
import QuanLiBoss.Manager.LunarNewYearEventManager;
import QuanLiBoss.Manager.OtherBossManager;
import QuanLiBoss.Manager.RedRibbonHQManager;
import QuanLiBoss.Manager.SkillSummonedManager;
import QuanLiBoss.Manager.SnakeWayManager;
import QuanLiBoss.Manager.TreasureUnderSeaManager;
import QuanLiBoss.Manager.TrungThuEventManager;
import QuanLiBoss.Manager.ValentineEventManager;
import QuanLiBoss.Manager.VuLanEventManager;
import QuanLiBoss.Manager.YardartManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import event.BakeACake.NauBanh_NewYear;
import jbcd.ConnectDB;
import nro.consignmentstore.ConsignShopManager;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import event.EventManager;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import network.interfaces.ISession;
import network.interfaces.ISessionAcceptHandler;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jbcd.dao.EventDAO;
import lombok.Getter;
import lombok.Setter;
import network.Network;
import network.io.MessageSendCollect;
import network.io.MyKeyHandler;
import network.session.MySession;
import nro.attribute.AttributeManager;
import nro.boss.map.TrainingBoss.TopKillWhisManager;
import nro.giftcode.GiftCodeManager;
import nro.map.DeathOrAliveArena.DeathOrAliveArenaManager;
import nro.map.DestronGas.TopDestronGas;
import nro.map.DragonBallNamec.NgocRongNamec;
import nro.map.RankSuper.SuperRankManager;
import nro.map.SnakeWay.TopSnakeWay;
import nro.map.TreasureUnderSea.TopTreasureUnderSea;
import nro.map.WorldMartialArtsTournament.WorldMartialArtsTournamentManager;
import nro.minigame.ChonAiDay;
import nro.minigame.MiniGame;
import nro.pariry.pariryManager;
import nro.bot.New.NewBot_new;
import nro.bot.New.BotAttackPlayer_1;
import nro.bot.New.BotAttackPlayer_2;
import nro.bot.New.BotAttackPlayer_3;
import nro.bot.New.BotManager_new;
import nro.bot.Event.BotManagerEvent;
import nro.bot.Event.ChristMas.ShopBotChristMas;
import nro.bot.Event.Halloween.ShopBotHalloween;
import nro.bot.Event.LunaNewYear.ShopBotLunaNewYear;
import nro.bot.Event.NewBotEvent;
import nro.bot.Event.VuLan.ShopBotVulan;
import nro.dragon.ChristMasEvent.ShenronChristMasEventManager;
import nro.dragon.HalloweenEvent.ShenronHalloweenEventManager;
import nro.minigame.TaiXiu;
import jbcd.CrisResultSet;
import nro.bot.Event.HungVuong.ShopBotHungVuong;
import nro.clan.Clan;
import nro.minigame.ChanLe;
import static nro.npc.NpcFactory.ChanLe;

public class ServerManager {

    private static final long DELAY = 8000; // Thời gian delay là 8000ms (8 giây)
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static String timeStart;
    public int threadMap;

    public static final Map CLIENTS = new HashMap();

    public static String NAME = "NROANWIN";
    public static String IP = "103.10.198.119";
    public static int PORT = 14445;

    public static String DOMAIN = "NROANWIN.VN";

    public static String NAME_SERVER = "NROANWIN";

    private static ServerManager instance;

    public static ServerSocket listenSocket;
    public static boolean isRunning;

    @Getter
    @Setter
    private AttributeManager attributeManager;
    private long lastUpdateAttribute;

    private ExecutorService gameExecutorService;

    // Manager
    public void init() {
        Manager.gI();
        GiftCodeManager.gI().init();
        try {
            if (Manager.LOCAL) {
                return;
            }
            ConnectDB.executeUpdate("update account set last_time_login = '2000-01-01', " + "last_time_logout = '2001-01-01'");
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e);
        }
        HistoryTransactionDAO.deleteHistory();
    }

    public static ServerManager gI() {
        if (instance == null) {
            instance = new ServerManager();
            instance.init();
        }
        return instance;
    }

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Logger.logException(ServerManager.class, e);
        }
        timeStart = TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss");
//        new nro.server.ServerManagerUI().setVisible(true);//Menu 1
//        new nro.server.MenuUI().setVisible(true);//Menu 2
         new nro.server.ServerManagerUI().setVisible(true);//Menu 3
        backupSrcFolder();
        backupDatabase();
        scheduleDailyReset();
        Antiddos.handleRequest(null);
    }

    public void run() {
        isRunning = true;
        gameExecutorService = Executors.newCachedThreadPool();
        activeServerSocket();
        activeGame();
        autoUpdateBxh();
        AutoSavedGame();
        updateEventPlayer();
        new Thread(BotManager.gI(), "New Thread Bot Player").start();
        new Thread(BotManager_new.gI(), "Thread Bot Player_New").start();
        new Thread(BotManagerEvent.gI(), "Thread BotManagerEvent").start();
        new Thread(() -> {
            NewBot.gI().runBot(0 , null , null, 4000);
            //------------------------------------------------------------------
            NewBot.gI().runBot(1 , new ShopBot(14 , 457 , 20) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(222 , 457 , 10) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(1154 , 457 , 3) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(2012 , 457 , Util.nextInt(20, 40)) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(1150 , 457 , 15) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(380 , 457 , Util.nextInt(19, 22)) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(17 , 457 , 5) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(1151 , 457 , 15) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(18 , 457 , 4) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(221 , 457 , 8) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(16 , 457 , 8) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(443 , 457 , 1) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(1153 , 457 , 15) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(1045 , 457 , 40) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(1152 , 457 , 15) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(15 , 457 , 5) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(220 , 457 , 5) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(447 , 457 , 3) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(987 , 457 , 10) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(380 , 457 , Util.nextInt(21, 25)) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(441 , 457 , 1) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(987 , 457 , Util.nextInt(8, 12)) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(442 , 457 , 1) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(443 , 457 , 1) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(20 , 457 , 2) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(223 , 457 , 8) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(19 , 457 , 3) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(987 , 457 , Util.nextInt(8, 10)) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(223 , 457 , 8) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(224 , 457 , 10) , null, 1);
            NewBot.gI().runBot(1 , new ShopBot(2012 , 457 , Util.nextInt(20, 40)) , null, 1);
            //------------------------------------------------------------------
            NewBot.gI().runBot(3 , null , new SellBot(457 , 16 , 1), 1);
            NewBot.gI().runBot(3 , null , new SellBot(457 , 19 , 99), 1);
            NewBot.gI().runBot(3 , null , new SellBot(457 , 14 , 1), 1);
            NewBot.gI().runBot(3 , null , new SellBot(457 , 18 , 99), 1);
            NewBot.gI().runBot(3 , null , new SellBot(457 , 15 , 1), 1);
            NewBot.gI().runBot(3 , null , new SellBot(457 , 20 , 99), 1);
            NewBot.gI().runBot(3 , null , new SellBot(457 , 17 , 99), 1);
        }).start();
        //-------------------------------------BOT EVENT------------------------
        if (EventManager.LUNNAR_NEW_YEAR) {
            new Thread(() -> {
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(748 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(749 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(750 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(1177 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(1178 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(1179 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(1180 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(1181 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(1473 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(1474 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
                NewBotEvent.gI().runBot(0 , new ShopBotLunaNewYear(1475 , 457 , Util.nextInt(20, 30)), null, null, null, null, 1);
            }).start();
        }
        if (EventManager.CHRISTMAS) {
            new Thread(() -> {
                NewBotEvent.gI().runBot(1 , null, new ShopBotChristMas(1459 , 457 , Util.nextInt(20, 30)), null, null, null, 1);
                NewBotEvent.gI().runBot(1 , null, new ShopBotChristMas(1460 , 457 , Util.nextInt(20, 30)), null, null, null, 1);
                NewBotEvent.gI().runBot(1 , null, new ShopBotChristMas(1461 , 457 , Util.nextInt(20, 30)), null, null, null, 1);
                NewBotEvent.gI().runBot(1 , null, new ShopBotChristMas(1462 , 457 , Util.nextInt(50, 60)), null, null, null, 1);
                NewBotEvent.gI().runBot(1 , null, new ShopBotChristMas(1463 , 457 , 99), null, null, null, 1);
                NewBotEvent.gI().runBot(1 , null, new ShopBotChristMas(1445 , 457 , Util.nextInt(15, 20)), null, null, null, 1);
                NewBotEvent.gI().runBot(1 , null, new ShopBotChristMas(1447 , 457 , Util.nextInt(20, 30)), null, null, null, 1);
                NewBotEvent.gI().runBot(1 , null, new ShopBotChristMas(1446 , 457 , Util.nextInt(30, 35)), null, null, null, 1);
                NewBotEvent.gI().runBot(1 , null, new ShopBotChristMas(1444 , 457 , Util.nextInt(35, 50)), null, null, null, 1);
                NewBotEvent.gI().runBot(1 , null, new ShopBotChristMas(1839 , 457 , Util.nextInt(90, 150)), null, null, null, 1);
            }).start();
        }
        if (EventManager.VU_LAN_FESTIVAL) {
            new Thread(() -> {
                NewBotEvent.gI().runBot(2 , null, null, new ShopBotVulan(1035 , 457 , Util.nextInt(20, 30)), null, null, 1);
                NewBotEvent.gI().runBot(2 , null, null, new ShopBotVulan(1032 , 457 , Util.nextInt(30, 40)), null, null, 1);
                NewBotEvent.gI().runBot(2 , null, null, new ShopBotVulan(1035 , 457 , Util.nextInt(20, 30)), null, null, 1);
                NewBotEvent.gI().runBot(2 , null, null, new ShopBotVulan(1032 , 457 , Util.nextInt(30, 40)), null, null, 1);
            }).start();
        }
        if (EventManager.HALLOWEEN) {
            new Thread(() -> {
                NewBotEvent.gI().runBot(3 , null, null, null, new ShopBotHalloween(901 , 457 , Util.nextInt(30, 50)), null, 1);
                NewBotEvent.gI().runBot(3 , null, null, null, new ShopBotHalloween(585 , 457 , Util.nextInt(50, 80)), null, 1);
                NewBotEvent.gI().runBot(3 , null, null, null, new ShopBotHalloween(901 , 457 , Util.nextInt(30, 50)), null, 1);
                NewBotEvent.gI().runBot(3 , null, null, null, new ShopBotHalloween(585 , 457 , Util.nextInt(50, 80)), null, 1);
                NewBotEvent.gI().runBot(3 , null, null, null, new ShopBotHalloween(901 , 457 , Util.nextInt(30, 50)), null, 1);
                NewBotEvent.gI().runBot(3 , null, null, null, new ShopBotHalloween(585 , 457 , Util.nextInt(50, 80)), null, 1);
            }).start();
        }
        if (EventManager.HUNG_VUONG) {
            new Thread(() -> {
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(1545 , 457 , Util.nextInt(20, 30)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(1547 , 457 , Util.nextInt(20, 30)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(1548 , 457 , Util.nextInt(20, 30)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(1546 , 457 , Util.nextInt(20, 30)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(1220 , 457 , Util.nextInt(80, 120)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(1221 , 457 , Util.nextInt(80, 120)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(1222 , 457 , Util.nextInt(80, 120)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(569 , 457 , Util.nextInt(180, 250)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(569 , 457 , Util.nextInt(180, 250)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(569 , 457 , Util.nextInt(180, 250)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(569 , 457 , Util.nextInt(180, 250)), 1);
                NewBotEvent.gI().runBot(4 , null, null, null, null, new ShopBotHungVuong(569 , 457 , Util.nextInt(180, 250)), 1);
            }).start();
        }
        //----------------------------------------------------------------------
     
        new Thread(() -> {
            NewBot_new.gI().runBot_new(0, null, null, null, 0);
            NewBot_new.gI().runBot_new(1, null, null, null, 0);
            NewBot_new.gI().runBot_new(2, null, null, null, 0);
            NewBot_new.gI().runBot_new(3, null, null, null, 0);
            NewBot_new.gI().runBot_new(4, null, null, null, 0);
            NewBot_new.gI().runBot_new(5, null, null, null, 0);
            NewBot_new.gI().runBot_new(6, null, null, null, 0);
            NewBot_new.gI().runBot_new(7, null, null, null, 0);
            NewBot_new.gI().runBot_new(8, null, null, null, 0);
            NewBot_new.gI().runBot_new(9, null, null, null, 0);
            NewBot_new.gI().runBot_new(10, null, null, null, 0);
            NewBot_new.gI().runBot_new(11, new BotAttackPlayer_1(true), null, null, 0);
            NewBot_new.gI().runBot_new(12, null, new BotAttackPlayer_2(true), null, 0);
            NewBot_new.gI().runBot_new(13, null, null, new BotAttackPlayer_3(true), 0);
        }).start();
        
        ChonAiDay.gI().lastTimeEnd = System.currentTimeMillis() + 300000;
        TaiXiu.gI().lastTimeEnd = System.currentTimeMillis() + 50000;
        MiniGame.gI().MiniGame_S1.activate(1000);
        NauBanh_NewYear nauBanh = new NauBanh_NewYear();
        Thread thread = new Thread(nauBanh);
        thread.setName("Sự kiện nấu bánh Event Tết");
        thread.start();
        new Thread(SuperRankManager.gI(), "Update Super Rank").start();
        new Thread(WorldMartialArtsTournamentManager.gI(), "Update WMAT").start();
        new Thread(The23rdMartialArtCongressManager.gI(), "Update DHVT23").start();
        new Thread(DeathOrAliveArenaManager.gI(), "Update Võ Đài Sinh Tử").start();
        new Thread(ChonAiDay.gI(), "Thread CAD").start();
        new Thread(NgocRongNamec.gI(), "Update NRNM").start();
        new Thread(TaiXiu.gI(), "Thread TaiXiu").start();
        new Thread(AutoMaintenance.gI(), "Thread Auto bảo trì tự dộng").start();
        new Thread(ShenronChristMasEventManager.gI(), "Update Shenron ChristMas").start();
        new Thread(ShenronHalloweenEventManager.gI(), "Update Shenron Halloween").start();
        new Thread(pariryManager.gI(), "Thread Pariry").start();
        BossManager.gI().loadBoss();
        Manager.MAPS.forEach(nro.map.Map::initBoss);
        EventManager.gI().init();
        new Thread(BossManager.gI(), "Update boss").start();
        new Thread(YardartManager.gI(), "Update yardart boss").start();
        new Thread(FinalBossManager.gI(), "Update final boss").start();
        new Thread(SkillSummonedManager.gI(), "Update Skill-summoned boss").start();
        new Thread(BrolyManager.gI(), "Update broly boss").start();
        new Thread(OtherBossManager.gI(), "Update other boss").start();
        new Thread(RedRibbonHQManager.gI(), "Update reb ribbon hq boss").start();
        new Thread(TreasureUnderSeaManager.gI(), "Update treasure under sea boss").start();
        new Thread(SnakeWayManager.gI(), "Update snake way boss").start();
        new Thread(GasDestroyManager.gI(), "Update gas destroy boss").start();
        new Thread(BossOfTheGangsManager.gI(), "Update the gangs boss").start();
        new Thread(TrungThuEventManager.gI(), "Update trung thu event boss").start();
        new Thread(HalloweenEventManager.gI(), "Update halloween event boss").start();
        new Thread(ChristmasEventManager.gI(), "Update christmas event boss").start();
        new Thread(HungVuongEventManager.gI(), "Update Hung Vuong event boss").start();
        new Thread(LunarNewYearEventManager.gI(), "Update lunar new year event boss").start();
        new Thread(VuLanEventManager.gI(), "Update vulan event boss").start();
        new Thread(ValentineEventManager.gI(), "Update valentine event boss").start();
        new Thread(BossNomalManager.gI(), "Update nomal boss").start();
    }
    
    private void activeServerSocket() {
        try {
            Network.gI().init().setAcceptHandler(new ISessionAcceptHandler() {
                @Override
                public void sessionInit(ISession is) {
                    if (!canConnectWithIp(is.getIP())) {
                        is.disconnect();
                        return;
                    }
                    is.setMessageHandler(Controller.getInstance())
                            .setSendCollect(new MessageSendCollect())
                            .setKeyHandler(new MyKeyHandler())
                            .startCollect().startQueueHandler();
                }

                @Override
                public void sessionDisconnect(ISession session) {
                    Client.gI().kickSession((MySession) session);
                }
            }).setTypeSessionClone(MySession.class)
                    .setDoSomeThingWhenClose(() -> {
                        Logger.error("SERVER CLOSE\n");
                        System.exit(0);
                    })
                    .start(PORT);
        } catch (Exception e) {
        }
    }

    private boolean canConnectWithIp(String ipAddress) {
        Object o = CLIENTS.get(ipAddress);
        if (o == null) {
            CLIENTS.put(ipAddress, 1);
            return true;
        } else {
            int n = Integer.parseInt(String.valueOf(o));
            if (n < Manager.MAX_PER_IP) {
                n++;
                CLIENTS.put(ipAddress, n);
                return true;
            } else {
                return false;
            }
        }
    }

    public void disconnect(MySession session) {
        Object o = CLIENTS.get(session.getIP());
        if (o != null) {
            int n = Integer.parseInt(String.valueOf(o));
            n--;
            if (n < 0) {
                n = 0;
            }
            CLIENTS.put(session.getIP(), n);
        }
    }

    private void activeGame() {
        long delay = 500;
        gameExecutorService.submit(() -> {
            while (isRunning) {
                try {
                    long start = System.currentTimeMillis();
                    if (attributeManager != null) {
                        attributeManager.update();
                        if (Util.canDoWithTime(lastUpdateAttribute, 600000)) {
                            Manager.gI().updateAttributeServer();
                        }
                    }
                    long timeUpdate = System.currentTimeMillis() - start;
                    if (timeUpdate < delay) {
                        Thread.sleep(delay - timeUpdate);
                    }
                } catch (Exception e) {
                    Logger.logException(ServerManager.class, e);
                }
            }
        });
    }

    private static void AutoSavedGame() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!isRunning) {
                    scheduler.shutdown(); 
                    return;
                }
                // Cập nhật cơ sở dữ liệu
                Service.gI().AutoSavedDataBase();
            } catch (Exception e) {
                // Xử lý ngoại lệ một cách cẩn thận
                System.err.println("Lỗi ActiveGame " + e.getMessage());
            }
        }, 0, DELAY, TimeUnit.MILLISECONDS);
    }

    public void close() {
        isRunning = false;
        try {
            ClanService.gI().close();
        } catch (Exception e) {
            Logger.error("Lỗi save clan!\n");
        }
        try {
            ConsignShopManager.gI().save();
        } catch (Exception e) {
            Logger.error("Lỗi save shop ký gửi!\n");
        }
        try {
            Manager.gI().updateAttributeServer();
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e);
        }
        EventDAO.save();
        Client.gI().close();
        gameExecutorService.shutdown();
        Logger.success("SUCCESSFULLY MAINTENANCE!\n");
        if (AutoMaintenance.isRunning) {
            AutoMaintenance.isRunning = false;
            try {
                String batchFilePath = "run.bat";
                FileRunner.runBatchFile(batchFilePath);
            } catch (IOException e) {
            }
        }
        System.exit(0);
    }

    public long getNumPlayer() {
        long num = 0;
        try {
            CrisResultSet rs = ConnectDB.executeQuery("SELECT COUNT(*) FROM `player`");
            rs.first();
            num = rs.getLong(1);
        } catch (Exception e) {
        }
        return num;
    }
    
   public void autoUpdateBxh() {
    ScheduledExecutorService autoTop = Executors.newScheduledThreadPool(1);
    autoTop.scheduleWithFixedDelay(() -> {
        try {
            // ✅ Reload tất cả BXH chính (SM, nạp, đệ, coin, v.v…)
            TopServer.LoadingTop();
            

            // ✅ Reload BXH phụ (Gas, SnakeWay, Whis, Treasure)
            TopDestronGas.getInstance().load();
            TopTreasureUnderSea.getInstance().load();
            TopSnakeWay.getInstance().load();
            TopKillWhisManager.getInstance().load();

        } catch (Exception e) {
            Logger.logException(ServerManager.class, e, "Auto load BXH error");
        }
    }, 0, 30000, TimeUnit.MILLISECONDS); // chạy 10 phút 1 lần
}

    
    private static void backupSrcFolder() {
        // Đặt tên file sao lưu với định dạng ngày giờ
        String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFilePath = "backupsrc/src_backup_" + dateTime + ".zip";
        
        // Thư mục cần sao lưu
        String scFolderPath = "src";
        
        // Tạo file ZIP từ thu mục src
        try (FileOutputStream fos = new FileOutputStream(backupFilePath); ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            
        Path srcFolder = Paths.get(scFolderPath);
        Files.walk(srcFolder).filter(path -> !Files.isDirectory(path)).forEach(path -> {
            try {
                // Tạo Entry trong zip file cho mỗi file thư mục
                ZipEntry zipEntry = new ZipEntry(srcFolder.relativize(path).toString()) ;
                zipOut.putNextEntry(zipEntry) ;
                Files.copy(path, zipOut); 
                zipOut.closeEntry();
            } catch (IOException e) {
                System.out.println("Lỗi khi sao lưu file: " + path);
            }
        });
        System.out.println("Sao lưu thành công: " + backupFilePath);
        } catch (IOException e) {
            System.out.println("Lỗi khi sao lưu thư mục: " + e.getMessage());
        }
    }
    
    private static void backupDatabase() {
        // Đặt tên file sao lưu với định dạng ngày giờ
        String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFilePath = "backupsql/nro_backup_" + dateTime + ".sql";
        
        // Lệnh sqldump để sao lưu cơ sở dữ liệu
        String mysqldumpPath = "D:\\xamp\\mysql\\bin\\mysqldump.exe";
        String command = "\"" + mysqldumpPath + "\" -u root --databases nro --password=  --result-file=\"" + backupFilePath + "\"";
        
        try {
            System.out.println("Đang thực hiện sao lưu: " + command);
            
            //thực thi lệnh mysqldump
            Process process = Runtime.getRuntime().exec(command);
            
            //đọc đầu ra của lệnh (nếu có)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Output: " + line);
            }
            
            //đọc lỗi nếu có
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.out.println("Error: " + line);
            }
            
            //kiểm tra kết quả của process
            int processComplete = process.waitFor();
            if (processComplete != 0) {
                System.out.println("Lệnh mysqldump không thành công.");
            } else {
                System.out.println("Sao lưu thành công: " + backupFilePath);
            }
        
        } catch (IOException | InterruptedException e) {
            System.out.println("Lỗi khi thực thi lệnh: " + e.getMessage());
        }
    }
    
    //phương thức chạy sao lưu tự động
    public void scheduleDatabaseBackup() {
        scheduler.scheduleAtFixedRate(ServerManager::backupDatabase, 0, 30, TimeUnit.MINUTES);
    }
    
    public static void scheduleDailyReset() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable resetTask = () -> {
            resetClanAllClans();
        };
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        long initialDelay = Duration.between(now, nextMidnight).toMillis();
        long oneDay = TimeUnit.DAYS.toMillis(1);
        scheduler.scheduleAtFixedRate(resetTask, initialDelay, oneDay, TimeUnit.MILLISECONDS);
    }
        
    private static void resetClanAllClans() {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            ps = con.prepareStatement("UPDATE clan SET Boss_clan = 1");
            int rows = ps.executeUpdate();
            // reset cache
            for (Clan clan : Manager.CLANS) {
                clan.boss_clan_round = 1;
            }
            Logger.success("Đã reset boss_clan_round về 1 cho " + rows + " clan.");
        } catch (SQLException e) {
            Logger.logException(Clan.class, e, "Lỗi khi resetBossClanAllClans");
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Logger.logException(Clan.class, e, "Lỗi khi đóng kết nối resetBossClanAllClans");
            }
        }
    }
    
    public void updateEventPlayer() {
        if (!EventManager.LUNNAR_NEW_YEAR) {
            try {
                ConnectDB.executeUpdate("UPDATE player SET diem_su_kien_tet = '0'");
                ConnectDB.executeUpdate("UPDATE player SET data_event_new_year = '[0,0,0,0]'");
                Manager.resetEventData("luna_new_year");
            } catch (Exception e) {
            }
        }
        if (!EventManager.CHRISTMAS) {
            try {
                ConnectDB.executeUpdate("UPDATE player SET diem_su_kien_giangsinh = '0'");
                ConnectDB.executeUpdate("UPDATE player SET data_event_christ_mas = '[0,0,0,0]'");
                Manager.resetEventData("christ_mas");
            } catch (Exception e) {
            }
        }
        if (!EventManager.VU_LAN_FESTIVAL) {
            try {
                ConnectDB.executeUpdate("UPDATE player SET data_event_vulan = '[0,0,0]'");
            } catch (Exception e) {
            }
        }
        if (!EventManager.HALLOWEEN) {
            try {
                ConnectDB.executeUpdate("UPDATE player SET diem_su_kien_halloween = '0'");
                ConnectDB.executeUpdate("UPDATE player SET data_event_halloween = '[0,0]'");
            } catch (Exception e) {
            }
        }
        if (!EventManager.INTERNATIONAL_WOMANS_DAY) {
            try {
                ConnectDB.executeUpdate("UPDATE player SET diem_su_kien_8_3 = '0'");
                ConnectDB.executeUpdate("UPDATE player SET data_event_8_3 = '[0,0]'");
                ConnectDB.executeUpdate("UPDATE `event` SET `data` = ? WHERE `name` = 'international_womens_day'", 
                        "{\"damePrecent\":0,\"hpPrecent\":0,\"mpPrecent\":0,\"eventPoint\":0,\"lastExpRewardStage\":0}");
            } catch (Exception e) {
            }
        }
        if (!EventManager.TRUNG_THU) {
            try {
                ConnectDB.executeUpdate("UPDATE player SET diem_su_kien_trungthu = '0'");
                ConnectDB.executeUpdate("UPDATE player SET data_event_trung_thu = '[0,0]'");
            } catch (Exception e) {
            }
        }
        if (!EventManager.HUNG_VUONG) {
            try {
                ConnectDB.executeUpdate("UPDATE player SET diem_su_kien_hung_vuong = '0'");
                ConnectDB.executeUpdate("UPDATE player SET data_event_hung_vuong = '[0,0,0,0,0,0]'");
                ConnectDB.executeUpdate("UPDATE `event` SET `data` = ? WHERE `name` = 'hung_vuong'", "{\"ReceiveMelonSeed\":0}");
            } catch (Exception e) {
            }
        }
        if (!EventManager.BLACK_FRIDAY) {
            try {
                ConnectDB.executeUpdate("UPDATE player SET diem_su_kien_black_friday = '0'");
                ConnectDB.executeUpdate("UPDATE player SET data_event_black_friday = '[0,0]'");
            } catch (Exception e) {
                Logger.logException(ServerManager.class, e);
            }
        }
    }    
}