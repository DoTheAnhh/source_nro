package nro.server;

import jbcd.dao.PlayerDAO;
import models.Item.Item;
import nro.map.ItemMap;
import nro.player.Player;
import models.Item.ItemTimeService;
import nro.services.Service;
import nro.services.Fun.ChangeMapService;
import nro.services.Fun.TransactionService;
import Utils.Logger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nro.inventory.InventoryService;
import jbcd.ConnectDB;
import network.interfaces.ISession;
import lombok.Getter;
import network.session.MySession;
import network.session.SessionManager;
import nro.dragon.SummonDragon;
import nro.dragon.SummonDragonNamek;
import nro.map.DragonBallNamec.NgocRongNamec;
import nro.map.DragonNamecWar.TranhNgoc;
import utils.Functions;

public class Client implements Runnable {

    private static Client instance;
       
    private final Map<Long, Player> players_id = new HashMap<>();
    private final Map<Integer, Player> players_userId = new HashMap<>();
    private final Map<String, Player> players_name = new HashMap<>();
    @Getter
    private final List<Player> players = new ArrayList<>();
    
    private Client() {
        new Thread(this, "Update Client").start();
    }

    public static Client gI() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }
    

    public void put(Player player) {
        if (!players_id.containsKey(player.id)) {
            this.players_id.put(player.id, player);
        }
        if (!players_name.containsValue(player)) {
            this.players_name.put(player.name, player);
        }
        if (!players_userId.containsValue(player)) {
            this.players_userId.put(player.getSession().userId, player);
        }
        if (!players.contains(player)) {
            this.players.add(player);
        }

    }

    private void remove(MySession session) {
        if (session.player != null) {
            this.dispose(session.player);
            session.player.dispose();
        }
        if (session.joinedGame) {
            session.joinedGame = false;
            try {
                ConnectDB.executeUpdate("update account set last_time_logout = ? where id = ?", new Timestamp(System.currentTimeMillis()), session.userId);
            } catch (Exception e) {
                Logger.logException(Client.class, e);
            }
        }
        ServerManager.gI().disconnect(session);
    }

    private void dispose(Player player) {
        this.players_id.remove(player.id);
        this.players_name.remove(player.name);
        this.players_userId.remove(player.getSession().userId);
        this.players.remove(player);
        if (!player.beforeDispose) {
            TranhNgoc.gI().removePlayersCadic(player);
            TranhNgoc.gI().removePlayersFide(player);
            player.beforeDispose = true;
            player.mapIdBeforeLogout = player.zone.map.mapId;
            if (player.idNRNM != -1) {
                ItemMap itemMap = new ItemMap(player.zone, player.idNRNM, 1, player.location.x, player.location.y, -1);
                Service.gI().dropItemMap(player.zone, itemMap);
                NgocRongNamec.gI().pNrNamec[player.idNRNM - 353] = "";
                NgocRongNamec.gI().idpNrNamec[player.idNRNM - 353] = -1;
                player.idNRNM = -1;
            }
            ChangeMapService.gI().exitMap(player);
            TransactionService.gI().cancelTrade(player);
            if (player.clan != null) {
                player.clan.removeMemberOnline(null, player);
            }
            if (player.itemTime != null && player.itemTime.isUseTDLT) {
                Item tdlt = null;
                try {
                    tdlt = InventoryService.gI().findItemBag(player, 521);
                } catch (Exception ex) {
                }
                if (tdlt != null) {
                    ItemTimeService.gI().turnOffTDLT(player, tdlt);
                }
            }
            if (SummonDragon.gI().playerSummonShenron != null
                    && SummonDragon.gI().playerSummonShenron.id == player.id) {
                SummonDragon.gI().isPlayerDisconnect = true;
            }
            if (SummonDragonNamek.gI().playerSummonShenron != null
                    && SummonDragonNamek.gI().playerSummonShenron.id == player.id) {
                SummonDragonNamek.gI().isPlayerDisconnect = true;
            }
            if (player.shenronEvent_Christmas != null) {
                player.shenronEvent_Christmas.isPlayerDisconnect = true;
            }
            if (player.shenronEvent_Halloween != null) {
                player.shenronEvent_Halloween.isPlayerDisconnect = true;
            }
            if (player.DeTrung != null) {
                player.DeTrung.mobMeDie();
            }
            if (player.Detu != null) {
                if (player.Detu.DeTrung != null) {
                    player.Detu.DeTrung.mobMeDie();
                }
                ChangeMapService.gI().exitMap(player.Detu);
            }
            
        }
        PlayerDAO.updatePlayer(player);
    }

    public void kickSession(MySession session) {
        if (session != null) {
            this.remove(session);
            session.disconnect();
        }
    }

    public Player getPlayerByID(long playerId) {
        return this.players_id.get(playerId);
    }

    public Player getPlayerByUser(int userId) {
        return this.players_userId.get(userId);
    }

    public Player getPlayerByName(String name) {
        return this.players_name.get(name);
    }
    
    public void close() {
        Logger.log(Logger.YELLOW, "BEGIN KICK OUT SESSION " + players.size() + "\n");
        while (!players.isEmpty()) {
            Player pl = players.remove(0);
            if (pl != null && pl.getSession() != null) {
                this.kickSession(pl.getSession());
            }
        }
        Logger.success("SUCCESSFUL\n");
    }
    
    private void update() {
        for (int i = SessionManager.gI().getSessions().size() - 1; i >= 0; i--) {
            ISession s = SessionManager.gI().getSessions().get(i);
            MySession session = (MySession) s;
            if (session == null) {
                SessionManager.gI().getSessions().remove(i);
                continue;
            }
            if (session.timeWait > 0) {
                session.timeWait--;
                if (session.timeWait == 0) {
                    kickSession(session);
                }
            }
        }
    }
        
    @Override
    public void run() {
        while (ServerManager.isRunning) {
            long st = System.currentTimeMillis();
            try {
                update();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Functions.sleep(Math.max(1000 - (System.currentTimeMillis() - st), 10));
        }
    }
}
