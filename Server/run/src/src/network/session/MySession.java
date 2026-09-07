package network.session;

import java.net.Socket;
import nro.player.Player;
import nro.server.Controller;
import Data.DataGame;
import jbcd.data.GodGK;
import models.Item.Item;
import nro.server.Client;
import nro.server.Maintenance;
import nro.server.Manager;
import nro.services.Service;
import Utils.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nro.server.AntiLogin;
import Utils.TimeUtil;
import Utils.Util;
import java.io.IOException;
import jbcd.data.DatabaseUpdater;
import network.io.Message;

public class MySession extends Session {

    private static final Map<String, AntiLogin> ANTILOGIN = new HashMap<>();
    public Player player;
    
    public byte timeWait = 100;

    public boolean connected;
    public boolean sentKey;

    public static final byte[] KEYS = {0};
    public byte curR, curW;

    public String ipAddress;
    public boolean isQuanTriVien;
    public boolean isFounder;
    public boolean isJail;
    public int userId;
    public String uu;
    public String pp;

    public int typeClient;
    public byte zoomLevel;

    public long lastTimeLogout;
    public boolean joinedGame;

    public long lastTimeReadMessage;

    public boolean actived;

    public int goldBar;
    
    public List<Item> itemsReward;
    public String dataReward;
    public boolean is_gift_box;
    public double bdPlayer;

    public int version;
    public int vnd;
    public int coin;
    public int Bar;
    public long timeout;
    public int tongnap;
    
    public int Vip_Point;
    
    public int accountAgeDays;
    public long timeCreateAcount;
    
    public int getIdTask;
    
    public int danap;
    
    public boolean finishUpdate;

    public MySession(Socket socket) {
        super(socket);
        ipAddress = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void sendKey() throws Exception {
        super.sendKey();
        this.startSend();
    }

    public void sendSessionKey() {
        Message msg = new Message(-27);
        try {
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            this.sendMessage(msg);
            msg.cleanup();
            sentKey = true;
        } catch (IOException e) {
        }
    }

    public void login(String username, String password) {
        AntiLogin al = ANTILOGIN.get(this.ipAddress);
        if (al == null) {
            al = new AntiLogin();
            ANTILOGIN.put(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            Service.gI().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }
//        if(!"admin".equals(username)){
//            Service.gI().sendThongBaoOK(this, "Hiện tại server đang được sửa lỗi, hẹn bạn trong vài phút!");
//            return;
//        }
        if (Manager.LOCAL) {
            Service.gI().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }
        if (Maintenance.isRunning) {
            Service.gI().sendThongBaoOK(this, "Server đang trong thời gian bảo trì, vui lòng quay lại sau");
            return;
        }
        if (!this.isFounder && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }
        if (this.player == null) {
            Player pl = null;
            try {
                long st = System.currentTimeMillis();
                this.uu = username;
                this.pp = password;
                pl = GodGK.login(this, al);
                if (pl != null) {
                    // -77 max small
                    DataGame.sendSmallVersion(this);
                    // -93 bgitem version
                    DataGame.sendBgItemVersion(this);
                    this.timeWait = 0;
                    this.joinedGame = true;
                    pl.nPoint.calPoint();
                    pl.nPoint.setHp(Util.CrisGH(pl.nPoint.hp));
                    pl.nPoint.setMp(Util.CrisGH(pl.nPoint.mp));
                    pl.zone.addPlayer(pl);
                    if (pl.Detu != null) {
                        pl.Detu.nPoint.calPoint();
                        pl.Detu.nPoint.setHp(Util.CrisGH(pl.Detu.nPoint.hp));
                        pl.Detu.nPoint.setMp(Util.CrisGH(pl.Detu.nPoint.mp));
                    }
                    
                    pl.setSession(this);
                    Client.gI().put(pl);
                    this.player = pl;
                    //-28 -4 version data game
                    DataGame.sendVersionGame(this);
                    //-31 data item background
                    DataGame.sendDataItemBG(this);
                    Controller.getInstance().sendInfo(this);
                    Logger.primary(TimeUtil.getCurrHour() + "h" + TimeUtil.getCurrMin() + "m: Login Succesfully Player : " + this.player.name + " {" + (System.currentTimeMillis() - st) + " ms}\n");
                    if (this.player.notify != null && !this.player.notify.equals("null") && !this.player.notify.isEmpty() && this.player.notify.length() > 0) {
                        Service.gI().sendThongBao(this.player, this.player.notify);
                        this.player.notify = null;
                    }
                    if (this.player.isNewMember) {
                        DatabaseUpdater.refreshAccountAgeDays(this.player.getSession());
                        Service.gI().sendThongBao(this.player, "Thời gian tìm set kích hoạt đến " + TimeUtil.getDeadline(this.player.getSession().timeCreateAcount, this.player.getSession().accountAgeDays));
                    }
                }
            } catch (Exception e) {
                if (pl != null) {
                    pl.dispose();
                }
            }
        }
    }
}
