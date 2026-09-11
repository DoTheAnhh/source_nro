using Game2.Assets.src.e;
using Game2.Assets.src.f;
using Game2.Assets.src.g;
using System;
using UnityEngine;

namespace Game2
{
    
    public class Controller : IMessageHandler
    {
        protected static Controller me;
    
        protected static Controller me2;
    
        public Message messWait;

        // Dinh danh cua ban do DANG CHO duoc ap.
        //
        // Goi -24 mang ten, tile, nen cua ban do moi. Ban cu ghi de thang len
        // TileMap TRUOC khi kiem tra xem may co mau ban do do khong; khong co
        // thi no xin mau roi thoat ra — de lai ten va nen cua ban do MOI ngoi
        // tren dia hinh CU. Do chinh la canh "map vo": nen tuyet cua map nay
        // dan voi co cua map kia, mau 0/0.
        //
        // Nay doc vao day truoc, dung duoc mau roi moi ap sang TileMap.
        private int mapIdCho;

        private sbyte planetIdCho;

        private int tileIdCho;

        private int bgIdCho;

        private int typeMapCho;

        private string mapNameCho = string.Empty;

        private int zoneIdCho;

        // So goi mau ban do con phai BO khi toi.
        //
        // May chu tra mau dung thu tu xin. Mot goi -24 moi toi trong luc goi
        // cu con cho mau thi goi cu bi bo — nhung mau cua no van se toi, va
        // phai bo theo, khong thi no ghi de dia hinh cua ban do dang hien.
        private int mauCanBo;

        /// <summary>
        /// Ap dinh danh ban do dang cho sang TileMap.
        ///
        /// Chi goi khi chac chan da co mau ban do trong may, tuc la dia hinh
        /// sap dung duoc dung khop voi cai ten sap hien ra.
        /// </summary>
        private void apDungDinhDanhBanDo(int mapCu, int khuCu)
        {
            TileMap.mapID = mapIdCho;
            TileMap.planetID = planetIdCho;
            TileMap.tileID = tileIdCho;
            TileMap.bgID = bgIdCho;
            TileMap.typeMap = typeMapCho;
            TileMap.mapName = mapNameCho;
            TileMap.zoneID = zoneIdCho;
            GameScr.isPaint_CT = TileMap.mapID != 170;
            try
            {
                God.ChatUI.getInstance().doiChoDung(mapCu, khuCu,
                        TileMap.mapID, TileMap.zoneID);
            }
            catch (Exception)
            {
            }
        }
    
        public static bool isLoadingData = false;
    
        public static bool isConnectOK;
    
        public static bool isConnectionFail;
    
        public static bool isDisconnected;
    
        public static bool isMain;
    
        private float demCount;
    
        private int move;
    
        private int total;
    
        public static bool isStopReadMessage;
    
        public static MyHashTable frameHT_NEWBOSS = new MyHashTable();
    
        public const sbyte PHUBAN_TYPE_CHIENTRUONGNAMEK = 0;
    
        public const sbyte PHUBAN_START = 0;
    
        public const sbyte PHUBAN_UPDATE_POINT = 1;
    
        public const sbyte PHUBAN_END = 2;
    
        public const sbyte PHUBAN_LIFE = 4;
    
        public const sbyte PHUBAN_INFO = 5;
    
        public static Controller gI()
        {
            if (me == null)
            {
                me = new Controller();
            }
            return me;
        }
    
        public static Controller gI2()
        {
            if (me2 == null)
            {
                me2 = new Controller();
            }
            return me2;
        }
    
        public void onConnectOK(bool isMain1)
        {
            isMain = isMain1;
            mSystem.onConnectOK();
        }
    
        public void onConnectionFail(bool isMain1)
        {
            isMain = isMain1;
            mSystem.onConnectionFail();
        }
    
        public void onDisconnected(bool isMain1)
        {
            isMain = isMain1;
            mSystem.onDisconnected();
        }
    
        public void requestItemPlayer(Message msg)
        {
            try
            {
                int num = msg.reader().readUnsignedByte();
                Item item = GameScr.currentCharViewInfo.arrItemBody[num];
                item.saleCoinLock = msg.reader().readInt();
                item.sys = msg.reader().readByte();
                item.options = new MyVector();
                try
                {
                    while (true)
                    {
                        item.options.addElement(new ItemOption(msg.reader().readUnsignedByte(), msg.reader().readUnsignedShort()));
                    }
                }
                catch (Exception ex)
                {
                    Cout.println("Loi tairequestItemPlayer 1" + ex.ToString());
                }
            }
            catch (Exception ex2)
            {
                Cout.println("Loi tairequestItemPlayer 2" + ex2.ToString());
            }
        }
    
        public void onMessage(Message msg)
        {
            GameCanvas.debugSession.removeAllElements();
            GameCanvas.debug("SA1", 2);
            try
            {
                if (msg.command != -74)
                {
                    Res.outz("=========> [READ] cmd= " + msg.command);
                }
                Char @char = null;
                Mob mob = null;
                MyVector myVector = new MyVector();
                int num = 0;
                GameCanvas.timeLoading = 15;
                Controller2.readMessage(msg);
                switch (msg.command)
                {
                    case 0:
                        readLogin(msg);
                        break;
                    case 24:
                        read_opt(msg);
                        break;
                    case 20:
                        phuban_Info(msg);
                        break;
                    case 66:
                        readGetImgByName(msg);
                        break;
                    case 65:
                        {
                            sbyte b68 = msg.reader().readSByte();
                            string text6 = msg.reader().readUTF();
                            short num166 = msg.reader().readShort();
                            if (ItemTime.isExistMessage(b68))
                            {
                                if (num166 != 0)
                                {
                                    ItemTime.getMessageById(b68).initTimeText(b68, text6, num166);
                                }
                                else
                                {
                                    GameScr.textTime.removeElement(ItemTime.getMessageById(b68));
                                }
                            }
                            else
                            {
                                ItemTime itemTime = new ItemTime();
                                itemTime.initTimeText(b68, text6, num166);
                                GameScr.textTime.addElement(itemTime);
                            }
                            break;
                        }
                    case 112:
                        {
                            sbyte b24 = msg.reader().readByte();
                            Res.outz("spec type= " + b24);
                            if (b24 == 0)
                            {
                                Panel.spearcialImage = msg.reader().readShort();
                                Panel.specialInfo = msg.reader().readUTF();
                            }
                            else if (b24 == 5)
                            {
                                // Bang Noi tai (God.NoiTaiUI). Thu tu doc phai
                                // khop dung IntrinsicService.guiBangNoiTai.
                                string tenNT = msg.reader().readUTF();
                                int iconNT = msg.reader().readShort();
                                long giaVangNT = msg.reader().readLong();
                                int giaNgocNT = msg.reader().readInt();
                                long vangCoNT = msg.reader().readLong();
                                int ngocCoNT = msg.reader().readInt();
                                int soNT = msg.reader().readByte();
                                System.Collections.Generic.List<God.NoiTaiUI.Muc> dsNT
                                        = new System.Collections.Generic.List<God.NoiTaiUI.Muc>();
                                for (int iNT = 0; iNT < soNT; iNT++)
                                {
                                    God.NoiTaiUI.Muc mNT = new God.NoiTaiUI.Muc();
                                    mNT.id = msg.reader().readByte();
                                    mNT.icon = msg.reader().readShort();
                                    mNT.moTa = msg.reader().readUTF();
                                    mNT.chiSoMin = msg.reader().readShort();
                                    mNT.chiSoMax = msg.reader().readShort();
                                    dsNT.Add(mNT);
                                }
                                God.NoiTaiUI.getInstance().nhanBang(tenNT, iconNT,
                                        giaVangNT, giaNgocNT, vangCoNT, ngocCoNT, dsNT);
                            }
                            else
                            {
                                if (b24 != 1)
                                {
                                    break;
                                }
                                sbyte b25 = msg.reader().readByte();
                                Char.myCharz().infoSpeacialSkill = new string[b25][];
                                Char.myCharz().imgSpeacialSkill = new short[b25][];
                                GameCanvas.panel.speacialTabName = new string[b25][];
                                for (int num48 = 0; num48 < b25; num48++)
                                {
                                    GameCanvas.panel.speacialTabName[num48] = new string[2];
                                    string[] array4 = Res.split(msg.reader().readUTF(), "\n", 0);
                                    if (array4.Length == 2)
                                    {
                                        GameCanvas.panel.speacialTabName[num48] = array4;
                                    }
                                    if (array4.Length == 1)
                                    {
                                        GameCanvas.panel.speacialTabName[num48][0] = array4[0];
                                        GameCanvas.panel.speacialTabName[num48][1] = string.Empty;
                                    }
                                    int num49 = msg.reader().readByte();
                                    Char.myCharz().infoSpeacialSkill[num48] = new string[num49];
                                    Char.myCharz().imgSpeacialSkill[num48] = new short[num49];
                                    for (int num50 = 0; num50 < num49; num50++)
                                    {
                                        Char.myCharz().imgSpeacialSkill[num48][num50] = msg.reader().readShort();
                                        Char.myCharz().infoSpeacialSkill[num48][num50] = msg.reader().readUTF();
                                    }
                                }
                                GameCanvas.panel.tabName[25] = GameCanvas.panel.speacialTabName;
                                GameCanvas.panel.setTypeSpeacialSkill();
                                GameCanvas.panel.show();
                            }
                            break;
                        }
                    case -98:
                        {
                            sbyte b66 = msg.reader().readByte();
                            GameCanvas.menu.showMenu = false;
                            if (b66 == 0)
                            {
                                GameCanvas.startYesNoDlg(msg.reader().readUTF(), new Command(mResources.YES, GameCanvas.instance, 888397, msg.reader().readUTF()), new Command(mResources.NO, GameCanvas.instance, 888396, null));
                            }
                            break;
                        }
                    case -97:
                        Char.myCharz().cNangdong = msg.reader().readInt();
                        break;
                    case -96:
                        {
                            sbyte typeTop = msg.reader().readByte();
                            GameCanvas.panel.vTop.removeAllElements();
                            string topName = msg.reader().readUTF();
                            sbyte b49 = msg.reader().readByte();
                            for (int num128 = 0; num128 < b49; num128++)
                            {
                                int rank = msg.reader().readInt();
                                int pId = msg.reader().readInt();
                                short headID = msg.reader().readShort();
                                short headICON = msg.reader().readShort();
                                short body = msg.reader().readShort();
                                short leg = msg.reader().readShort();
                                string name = msg.reader().readUTF();
                                string info3 = msg.reader().readUTF();
                                TopInfo topInfo = new TopInfo();
                                topInfo.rank = rank;
                                topInfo.headID = headID;
                                topInfo.headICON = headICON;
                                topInfo.body = body;
                                topInfo.leg = leg;
                                topInfo.name = name;
                                topInfo.info = info3;
                                topInfo.info2 = msg.reader().readUTF();
                                topInfo.pId = pId;
                                GameCanvas.panel.vTop.addElement(topInfo);
                            }
                            GameCanvas.panel.topName = topName;
                            GameCanvas.panel.setTypeTop(typeTop);
                            GameCanvas.panel.show();
                            break;
                        }
                    case -94:
                        while (msg.reader().available() > 0)
                        {
                            short num45 = msg.reader().readShort();
                            int num46 = msg.reader().readInt();
                            for (int num47 = 0; num47 < Char.myCharz().vSkill.size(); num47++)
                            {
                                Skill skill = (Skill)Char.myCharz().vSkill.elementAt(num47);
                                if (skill != null && skill.skillId == num45)
                                {
                                    if (num46 < skill.coolDown)
                                    {
                                        skill.lastTimeUseThisSkill = mSystem.currentTimeMillis() - (skill.coolDown - num46);
                                    }
                                    Res.outz("1 chieu id= " + skill.template.id + " cooldown= " + num46 + "curr cool down= " + skill.coolDown);
                                }
                            }
                        }
                        break;
                    case -95:
                        {
                            sbyte b29 = msg.reader().readByte();
                            Res.outz("type= " + b29);
                            if (b29 == 0)
                            {
                                int num61 = msg.reader().readInt();
                                short templateId = msg.reader().readShort();
                                int num62 = msg.readInt3Byte();
                                SoundMn.gI().explode_1();
                                if (num61 == Char.myCharz().charID)
                                {
                                    Char.myCharz().mobMe = new Mob(num61, false, false, false, false, false, templateId, 1, num62, 0, num62, (short)(Char.myCharz().cx + ((Char.myCharz().cdir != 1) ? (-40) : 40)), (short)Char.myCharz().cy, 4, 0);
                                    Char.myCharz().mobMe.isMobMe = true;
                                    EffecMn.addEff(new Effect(18, Char.myCharz().mobMe.x, Char.myCharz().mobMe.y, 2, 10, -1));
                                    Char.myCharz().tMobMeBorn = 30;
                                    GameScr.vMob.addElement(Char.myCharz().mobMe);
                                }
                                else
                                {
                                    @char = GameScr.findCharInMap(num61);
                                    if (@char != null)
                                    {
                                        Mob mob4 = new Mob(num61, false, false, false, false, false, templateId, 1, num62, 0, num62, (short)@char.cx, (short)@char.cy, 4, 0);
                                        mob4.isMobMe = true;
                                        @char.mobMe = mob4;
                                        GameScr.vMob.addElement(@char.mobMe);
                                    }
                                    else
                                    {
                                        Mob mob5 = GameScr.findMobInMap(num61);
                                        if (mob5 == null)
                                        {
                                            mob5 = new Mob(num61, false, false, false, false, false, templateId, 1, num62, 0, num62, -100, -100, 4, 0);
                                            mob5.isMobMe = true;
                                            GameScr.vMob.addElement(mob5);
                                        }
                                    }
                                }
                            }
                            if (b29 == 1)
                            {
                                int num63 = msg.reader().readInt();
                                int mobId = msg.reader().readByte();
                                Res.outz("mod attack id= " + num63);
                                if (num63 == Char.myCharz().charID)
                                {
                                    if (GameScr.findMobInMap(mobId) != null)
                                    {
                                        Char.myCharz().mobMe.attackOtherMob(GameScr.findMobInMap(mobId));
                                    }
                                }
                                else
                                {
                                    @char = GameScr.findCharInMap(num63);
                                    if (@char != null && GameScr.findMobInMap(mobId) != null)
                                    {
                                        @char.mobMe.attackOtherMob(GameScr.findMobInMap(mobId));
                                    }
                                }
                            }
                            if (b29 == 2)
                            {
                                int num64 = msg.reader().readInt();
                                int num65 = msg.reader().readInt();
                                int num66 = msg.readInt3Byte();
                                int cHPNew = msg.readInt3Byte();
                                if (num64 == Char.myCharz().charID)
                                {
                                    Res.outz("mob dame= " + num66);
                                    @char = GameScr.findCharInMap(num65);
                                    if (@char != null)
                                    {
                                        @char.cHPNew = cHPNew;
                                        if (Char.myCharz().mobMe.isBusyAttackSomeOne)
                                        {
                                            @char.doInjure(num66, 0, false, true);
                                        }
                                        else
                                        {
                                            Char.myCharz().mobMe.dame = num66;
                                            Char.myCharz().mobMe.setAttack(@char);
                                        }
                                    }
                                }
                                else
                                {
                                    mob = GameScr.findMobInMap(num64);
                                    if (mob != null)
                                    {
                                        if (num65 == Char.myCharz().charID)
                                        {
                                            Char.myCharz().cHPNew = cHPNew;
                                            if (mob.isBusyAttackSomeOne)
                                            {
                                                Char.myCharz().doInjure(num66, 0, false, true);
                                            }
                                            else
                                            {
                                                mob.dame = num66;
                                                mob.setAttack(Char.myCharz());
                                            }
                                        }
                                        else
                                        {
                                            @char = GameScr.findCharInMap(num65);
                                            if (@char != null)
                                            {
                                                @char.cHPNew = cHPNew;
                                                if (mob.isBusyAttackSomeOne)
                                                {
                                                    @char.doInjure(num66, 0, false, true);
                                                }
                                                else
                                                {
                                                    mob.dame = num66;
                                                    mob.setAttack(@char);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (b29 == 3)
                            {
                                int num67 = msg.reader().readInt();
                                int mobId2 = msg.reader().readInt();
                                int hp = msg.readInt3Byte();
                                int num68 = msg.readInt3Byte();
                                @char = null;
                                @char = ((Char.myCharz().charID != num67) ? GameScr.findCharInMap(num67) : Char.myCharz());
                                if (@char != null)
                                {
                                    mob = GameScr.findMobInMap(mobId2);
                                    if (@char.mobMe != null)
                                    {
                                        @char.mobMe.attackOtherMob(mob);
                                    }
                                    if (mob != null)
                                    {
                                        mob.hp = hp;
                                        mob.updateHp_bar();
                                        if (num68 == 0)
                                        {
                                            mob.x = mob.xFirst;
                                            mob.y = mob.yFirst;
                                            GameScr.startFlyText(mResources.miss, mob.x, mob.y - mob.h, 0, -2, mFont.MISS);
                                        }
                                        else
                                        {
                                            GameScr.startFlyText("-" + num68, mob.x, mob.y - mob.h, 0, -2, mFont.ORANGE);
                                        }
                                    }
                                }
                            }
                            if (b29 == 4)
                            {
                            }
                            if (b29 == 5)
                            {
                                int num69 = msg.reader().readInt();
                                sbyte b30 = msg.reader().readByte();
                                int mobId3 = msg.reader().readInt();
                                int num70 = msg.readInt3Byte();
                                int hp2 = msg.readInt3Byte();
                                @char = null;
                                @char = ((num69 != Char.myCharz().charID) ? GameScr.findCharInMap(num69) : Char.myCharz());
                                if (@char == null)
                                {
                                    return;
                                }
                                if ((TileMap.tileTypeAtPixel(@char.cx, @char.cy) & 2) == 2)
                                {
                                    @char.setSkillPaint(GameScr.sks[b30], 0);
                                }
                                else
                                {
                                    @char.setSkillPaint(GameScr.sks[b30], 1);
                                }
                                Mob mob6 = GameScr.findMobInMap(mobId3);
                                if (@char.cx <= mob6.x)
                                {
                                    @char.cdir = 1;
                                }
                                else
                                {
                                    @char.cdir = -1;
                                }
                                @char.mobFocus = mob6;
                                mob6.hp = hp2;
                                mob6.updateHp_bar();
                                GameCanvas.debug("SA83v2", 2);
                                if (num70 == 0)
                                {
                                    mob6.x = mob6.xFirst;
                                    mob6.y = mob6.yFirst;
                                    GameScr.startFlyText(mResources.miss, mob6.x, mob6.y - mob6.h, 0, -2, mFont.MISS);
                                }
                                else
                                {
                                    GameScr.startFlyText("-" + num70, mob6.x, mob6.y - mob6.h, 0, -2, mFont.ORANGE);
                                }
                            }
                            if (b29 == 6)
                            {
                                int num71 = msg.reader().readInt();
                                if (num71 == Char.myCharz().charID)
                                {
                                    Char.myCharz().mobMe.startDie();
                                }
                                else
                                {
                                    @char = GameScr.findCharInMap(num71);
                                    if (@char != null)
                                    {
                                        @char.mobMe.startDie();
                                    }
                                }
                            }
                            if (b29 != 7)
                            {
                                break;
                            }
                            int num72 = msg.reader().readInt();
                            if (num72 == Char.myCharz().charID)
                            {
                                Char.myCharz().mobMe = null;
                                for (int num73 = 0; num73 < GameScr.vMob.size(); num73++)
                                {
                                    if (((Mob)GameScr.vMob.elementAt(num73)).mobId == num72)
                                    {
                                        GameScr.vMob.removeElementAt(num73);
                                    }
                                }
                                break;
                            }
                            @char = GameScr.findCharInMap(num72);
                            for (int num74 = 0; num74 < GameScr.vMob.size(); num74++)
                            {
                                if (((Mob)GameScr.vMob.elementAt(num74)).mobId == num72)
                                {
                                    GameScr.vMob.removeElementAt(num74);
                                }
                            }
                            if (@char != null)
                            {
                                @char.mobMe = null;
                            }
                            break;
                        }
                    case -92:
                        Main.typeClient = msg.reader().readByte();
                        if (Rms.loadRMSString("ResVersion") == null)
                        {
                            Rms.clearAll();
                        }
                        Rms.saveRMSInt("clienttype", Main.typeClient);
                        Rms.saveRMSInt("lastZoomlevel", mGraphics.zoomLevel);
                        if (Rms.loadRMSString("ResVersion") == null)
                        {
                            GameCanvas.startOK(mResources.plsRestartGame, 8885, null);
                        }
                        break;
                    case -91:
                        {
                            sbyte b59 = msg.reader().readByte();
                            GameCanvas.panel.mapNames = new string[b59];
                            GameCanvas.panel.planetNames = new string[b59];
                            for (int num142 = 0; num142 < b59; num142++)
                            {
                                GameCanvas.panel.mapNames[num142] = msg.reader().readUTF();
                                GameCanvas.panel.planetNames[num142] = msg.reader().readUTF();
                            }
                            // Bang dich chuyen MOI thay cho Panel loai 14 —
                            // xem God.ChonNhanhUI.
                            God.ChonNhanhUI.getInstance().moMap(
                                    GameCanvas.panel.mapNames,
                                    GameCanvas.panel.planetNames);
                            break;
                        }
                    case -90:
                        {
                            sbyte b63 = msg.reader().readByte();
                            int num149 = msg.reader().readInt();
                            Res.outz("===> UPDATE_BODY:    type = " + b63);
                            @char = ((Char.myCharz().charID != num149) ? GameScr.findCharInMap(num149) : Char.myCharz());
                            if (b63 != -1)
                            {
                                short num150 = msg.reader().readShort();
                                short num151 = msg.reader().readShort();
                                short num152 = msg.reader().readShort();
                                sbyte isMonkey = msg.reader().readByte();
                                if (@char != null)
                                {
                                    if (@char.charID == num149)
                                    {
                                        @char.isMask = true;
                                        @char.isMonkey = isMonkey;
                                        if (@char.isMonkey != 0)
                                        {
                                            @char.isWaitMonkey = false;
                                            @char.isLockMove = false;
                                        }
                                    }
                                    else if (@char != null)
                                    {
                                        @char.isMask = true;
                                        @char.isMonkey = isMonkey;
                                    }
                                    if (num150 != -1)
                                    {
                                        @char.head = num150;
                                    }
                                    if (num151 != -1)
                                    {
                                        @char.body = num151;
                                    }
                                    if (num152 != -1)
                                    {
                                        @char.leg = num152;
                                    }
                                }
                            }
                            if (b63 == -1 && @char != null)
                            {
                                @char.isMask = false;
                                @char.isMonkey = 0;
                            }
                            if (@char == null)
                            {
                                break;
                            }
                            for (int num153 = 0; num153 < 54; num153++)
                            {
                                @char.removeEffChar(0, 201 + num153);
                            }
                            if (@char.bag >= 201 && @char.bag < 255)
                            {
                                Effect effect2 = new Effect(@char.bag, @char, 2, -1, 10, 1);
                                effect2.typeEff = 5;
                                @char.addEffChar(effect2);
                            }
                            if (@char.bag == 30 && @char.me)
                            {
                                GameScr.isPickNgocRong = true;
                            }
                            if (!@char.me)
                            {
                                break;
                            }
                            GameScr.isudungCapsun4 = false;
                            GameScr.isudungCapsun3 = false;
                            for (int num154 = 0; num154 < Char.myCharz().arrItemBag.Length; num154++)
                            {
                                Item item4 = Char.myCharz().arrItemBag[num154];
                                if (item4 == null)
                                {
                                    continue;
                                }
                                if (item4.template.id == 194)
                                {
                                    GameScr.isudungCapsun4 = item4.quantity > 0;
                                    if (GameScr.isudungCapsun4)
                                    {
                                        break;
                                    }
                                }
                                else if (item4.template.id == 193)
                                {
                                    GameScr.isudungCapsun3 = item4.quantity > 0;
                                }
                            }
                            break;
                        }
                    case -88:
                        GameCanvas.endDlg();
                        GameCanvas.serverScreen.switchToMe();
                        break;
                    case -87:
                        {
                            Res.outz("GET UPDATE_DATA " + msg.reader().available() + " bytes");
                            msg.reader().mark(100000);
                            createData(msg.reader(), true);
                            msg.reader().reset();
                            sbyte[] data2 = new sbyte[msg.reader().available()];
                            msg.reader().readFully(ref data2);
                            sbyte[] data3 = new sbyte[1] { GameScr.vcData };
                            Rms.saveRMS("NRdataVersion", data3);
                            LoginScr.isUpdateData = false;
                            if (GameScr.vsData == GameScr.vcData && GameScr.vsMap == GameScr.vcMap && GameScr.vsSkill == GameScr.vcSkill && GameScr.vsItem == GameScr.vcItem)
                            {
                                Res.outz(GameScr.vsData + "," + GameScr.vsMap + "," + GameScr.vsSkill + "," + GameScr.vsItem);
                                GameScr.gI().readDart();
                                GameScr.gI().readEfect();
                                GameScr.gI().readArrow();
                                GameScr.gI().readSkill();
                                Service.gI().clientOk();
                                return;
                            }
                            break;
                        }
                    case -86:
                        {
                            sbyte b60 = msg.reader().readByte();
                            Res.outz("server gui ve giao dich action = " + b60);
                            if (b60 == 0)
                            {
                                int playerID = msg.reader().readInt();
                                GameScr.gI().giaodich(playerID);
                            }
                            if (b60 == 1)
                            {
                                int num143 = msg.reader().readInt();
                                Char char10 = GameScr.findCharInMap(num143);
                                if (char10 == null)
                                {
                                    return;
                                }
                                GameCanvas.panel.setTypeGiaoDich(char10);
                                GameCanvas.panel.show();
                                Service.gI().getPlayerMenu(num143);
                            }
                            if (b60 == 2)
                            {
                                sbyte b61 = msg.reader().readByte();
                                for (int num144 = 0; num144 < GameCanvas.panel.vMyGD.size(); num144++)
                                {
                                    Item item2 = (Item)GameCanvas.panel.vMyGD.elementAt(num144);
                                    if (item2.indexUI == b61)
                                    {
                                        GameCanvas.panel.vMyGD.removeElement(item2);
                                        break;
                                    }
                                }
                            }
                            if (b60 == 5)
                            {
                            }
                            if (b60 == 6)
                            {
                                GameCanvas.panel.isFriendLock = true;
                                if (GameCanvas.panel2 != null)
                                {
                                    GameCanvas.panel2.isFriendLock = true;
                                }
                                GameCanvas.panel.vFriendGD.removeAllElements();
                                if (GameCanvas.panel2 != null)
                                {
                                    GameCanvas.panel2.vFriendGD.removeAllElements();
                                }
                                int friendMoneyGD = msg.reader().readInt();
                                sbyte b62 = msg.reader().readByte();
                                Res.outz("item size = " + b62);
                                for (int num145 = 0; num145 < b62; num145++)
                                {
                                    Item item3 = new Item();
                                    item3.template = ItemTemplates.get(msg.reader().readShort());
                                    item3.quantity = msg.reader().readInt();
                                    int num146 = msg.reader().readUnsignedByte();
                                    if (num146 != 0)
                                    {
                                        item3.itemOption = new ItemOption[num146];
                                        for (int num147 = 0; num147 < item3.itemOption.Length; num147++)
                                        {
                                            int num148 = msg.reader().readInt();
                                            int param6 = msg.reader().readInt();
                                            if (num148 != -1)
                                            {
                                                item3.itemOption[num147] = new ItemOption(num148, param6);
                                                item3.compare = GameCanvas.panel.getCompare(item3);
                                            }
                                        }
                                    }
                                    if (GameCanvas.panel2 != null)
                                    {
                                        GameCanvas.panel2.vFriendGD.addElement(item3);
                                    }
                                    else
                                    {
                                        GameCanvas.panel.vFriendGD.addElement(item3);
                                    }
                                }
                                if (GameCanvas.panel2 != null)
                                {
                                    GameCanvas.panel2.setTabGiaoDich(false);
                                    GameCanvas.panel2.friendMoneyGD = friendMoneyGD;
                                }
                                else
                                {
                                    GameCanvas.panel.friendMoneyGD = friendMoneyGD;
                                    if (GameCanvas.panel.currentTabIndex == 2)
                                    {
                                        GameCanvas.panel.setTabGiaoDich(false);
                                    }
                                }
                            }
                            if (b60 == 7)
                            {
                                InfoDlg.hide();
                                if (GameCanvas.panel.isShow)
                                {
                                    GameCanvas.panel.hide();
                                }
                            }
                            break;
                        }
                    case -85:
                        {
                            Res.outz("CAP CHAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                            sbyte b69 = msg.reader().readByte();
                            if (b69 == 0)
                            {
                                int num167 = msg.reader().readUnsignedShort();
                                Res.outz("lent =" + num167);
                                sbyte[] data5 = new sbyte[num167];
                                msg.reader().read(ref data5, 0, num167);
                                GameScr.imgCapcha = Image.createImage(data5, 0, num167);
                                GameScr.gI().keyInput = "-----";
                                GameScr.gI().strCapcha = msg.reader().readUTF();
                                GameScr.gI().keyCapcha = new int[GameScr.gI().strCapcha.Length];
                                GameScr.gI().mobCapcha = new Mob();
                                GameScr.gI().right = null;
                            }
                            if (b69 == 1)
                            {
                                MobCapcha.isAttack = true;
                            }
                            if (b69 == 2)
                            {
                                MobCapcha.explode = true;
                                GameScr.gI().right = GameScr.gI().cmdFocus;
                            }
                            break;
                        }
                    case -112:
                        {
                            sbyte b50 = msg.reader().readByte();
                            if (b50 == 0)
                            {
                                sbyte mobIndex = msg.reader().readByte();
                                GameScr.findMobInMap(mobIndex).clearBody();
                            }
                            if (b50 == 1)
                            {
                                sbyte mobIndex2 = msg.reader().readByte();
                                GameScr.findMobInMap(mobIndex2).setBody(msg.reader().readShort());
                            }
                            break;
                        }
                    case -84:
                        {
                            int index2 = msg.reader().readUnsignedByte();
                            Mob mob7 = null;
                            try
                            {
                                mob7 = (Mob)GameScr.vMob.elementAt(index2);
                            }
                            catch (Exception)
                            {
                            }
                            if (mob7 != null)
                            {
                                mob7.maxHp = msg.reader().readInt();
                            }
                            break;
                        }
                    case -83:
                        {
                            sbyte b7 = msg.reader().readByte();
                            if (b7 == 0)
                            {
                                int num11 = msg.reader().readShort();
                                int bgRID = msg.reader().readShort();
                                int num12 = msg.reader().readUnsignedByte();
                                int num13 = msg.reader().readInt();
                                string text = msg.reader().readUTF();
                                int num14 = msg.reader().readShort();
                                int num15 = msg.reader().readShort();
                                sbyte b8 = msg.reader().readByte();
                                if (b8 == 1)
                                {
                                    GameScr.gI().isRongNamek = true;
                                }
                                else
                                {
                                    GameScr.gI().isRongNamek = false;
                                }
                                GameScr.gI().xR = num14;
                                GameScr.gI().yR = num15;
                                Res.outz("xR= " + num14 + " yR= " + num15 + " +++++++++++++++++++++++++++++++++++++++");
                                if (Char.myCharz().charID == num13)
                                {
                                    GameCanvas.panel.hideNow();
                                    GameScr.gI().activeRongThanEff(true);
                                }
                                else if (TileMap.mapID == num11 && TileMap.zoneID == num12)
                                {
                                    GameScr.gI().activeRongThanEff(false);
                                }
                                else if (mGraphics.zoomLevel > 1)
                                {
                                    GameScr.gI().doiMauTroi();
                                }
                                GameScr.gI().mapRID = num11;
                                GameScr.gI().bgRID = bgRID;
                                GameScr.gI().zoneRID = num12;
                            }
                            if (b7 == 1)
                            {
                                Res.outz("map RID = " + GameScr.gI().mapRID + " zone RID= " + GameScr.gI().zoneRID);
                                Res.outz("map ID = " + TileMap.mapID + " zone ID= " + TileMap.zoneID);
                                if (TileMap.mapID == GameScr.gI().mapRID && TileMap.zoneID == GameScr.gI().zoneRID)
                                {
                                    GameScr.gI().hideRongThanEff();
                                }
                                else
                                {
                                    GameScr.gI().isRongThanXuatHien = false;
                                    if (GameScr.gI().isRongNamek)
                                    {
                                        GameScr.gI().isRongNamek = false;
                                    }
                                }
                            }
                            if (b7 != 2)
                            {
                            }
                            break;
                        }
                    case -82:
                        {
                            sbyte b38 = msg.reader().readByte();
                            TileMap.tileIndex = new int[b38][][];
                            TileMap.tileType = new int[b38][];
                            for (int num111 = 0; num111 < b38; num111++)
                            {
                                sbyte b39 = msg.reader().readByte();
                                TileMap.tileType[num111] = new int[b39];
                                TileMap.tileIndex[num111] = new int[b39][];
                                for (int num112 = 0; num112 < b39; num112++)
                                {
                                    TileMap.tileType[num111][num112] = msg.reader().readInt();
                                    sbyte b40 = msg.reader().readByte();
                                    TileMap.tileIndex[num111][num112] = new int[b40];
                                    for (int num113 = 0; num113 < b40; num113++)
                                    {
                                        TileMap.tileIndex[num111][num112][num113] = msg.reader().readByte();
                                    }
                                }
                            }
                            break;
                        }
                    case -81:
                        {
                            sbyte b42 = msg.reader().readByte();
                            if (b42 == 0)
                            {
                                string src = msg.reader().readUTF();
                                string src2 = msg.reader().readUTF();
                                GameCanvas.panel.setTypeCombine();
                                GameCanvas.panel.combineInfo = mFont.tahoma_7b_blue.splitFontArray(src, Panel.WIDTH_PANEL);
                                GameCanvas.panel.combineTopInfo = mFont.tahoma_7.splitFontArray(src2, Panel.WIDTH_PANEL);
                                GameCanvas.panel.show();
                            }
                            if (b42 == 1)
                            {
                                GameCanvas.panel.vItemCombine.removeAllElements();
                                sbyte b43 = msg.reader().readByte();
                                for (int num115 = 0; num115 < b43; num115++)
                                {
                                    sbyte b44 = msg.reader().readByte();
                                    for (int num116 = 0; num116 < Char.myCharz().arrItemBag.Length; num116++)
                                    {
                                        Item item = Char.myCharz().arrItemBag[num116];
                                        if (item != null && item.indexUI == b44)
                                        {
                                            item.isSelect = true;
                                            GameCanvas.panel.vItemCombine.addElement(item);
                                        }
                                    }
                                }
                                if (GameCanvas.panel.isShow)
                                {
                                    GameCanvas.panel.setTabCombine();
                                }
                            }
                            if (b42 == 2)
                            {
                                GameCanvas.panel.combineSuccess = 0;
                                GameCanvas.panel.setCombineEff(0);
                            }
                            if (b42 == 3)
                            {
                                GameCanvas.panel.combineSuccess = 1;
                                GameCanvas.panel.setCombineEff(0);
                            }
                            if (b42 == 4)
                            {
                                short iconID = msg.reader().readShort();
                                GameCanvas.panel.iconID3 = iconID;
                                GameCanvas.panel.combineSuccess = 0;
                                GameCanvas.panel.setCombineEff(1);
                            }
                            if (b42 == 5)
                            {
                                short iconID2 = msg.reader().readShort();
                                GameCanvas.panel.iconID3 = iconID2;
                                GameCanvas.panel.combineSuccess = 0;
                                GameCanvas.panel.setCombineEff(2);
                            }
                            if (b42 == 6)
                            {
                                short iconID3 = msg.reader().readShort();
                                short iconID4 = msg.reader().readShort();
                                GameCanvas.panel.combineSuccess = 0;
                                GameCanvas.panel.setCombineEff(3);
                                GameCanvas.panel.iconID1 = iconID3;
                                GameCanvas.panel.iconID3 = iconID4;
                            }
                            if (b42 == 7)
                            {
                                short iconID5 = msg.reader().readShort();
                                GameCanvas.panel.iconID3 = iconID5;
                                GameCanvas.panel.combineSuccess = 0;
                                GameCanvas.panel.setCombineEff(4);
                            }
                            if (b42 == 8)
                            {
                                GameCanvas.panel.iconID3 = -1;
                                GameCanvas.panel.combineSuccess = 1;
                                GameCanvas.panel.setCombineEff(4);
                            }
                            short num117 = 21;
                            int num118 = 0;
                            int num119 = 0;
                            try
                            {
                                num117 = msg.reader().readShort();
                                num118 = msg.reader().readShort();
                                num119 = msg.reader().readShort();
                                GameCanvas.panel.xS = num118 - GameScr.cmx;
                                GameCanvas.panel.yS = num119 - GameScr.cmy;
                            }
                            catch (Exception)
                            {
                            }
                            for (int num120 = 0; num120 < GameScr.vNpc.size(); num120++)
                            {
                                Npc npc3 = (Npc)GameScr.vNpc.elementAt(num120);
                                if (npc3.template.npcTemplateId == num117)
                                {
                                    GameCanvas.panel.xS = npc3.cx - GameScr.cmx;
                                    GameCanvas.panel.yS = npc3.cy - GameScr.cmy;
                                    GameCanvas.panel.idNPC = num117;
                                    break;
                                }
                            }
                            break;
                        }
                    case -80:
                        {
                            sbyte b21 = msg.reader().readByte();
                            InfoDlg.hide();
                            if (b21 == 0)
                            {
                                GameCanvas.panel.vFriend.removeAllElements();
                                int num35 = msg.reader().readUnsignedByte();
                                for (int num36 = 0; num36 < num35; num36++)
                                {
                                    Char char6 = new Char();
                                    char6.charID = msg.reader().readInt();
                                    char6.head = msg.reader().readShort();
                                    char6.headICON = msg.reader().readShort();
                                    char6.body = msg.reader().readShort();
                                    char6.leg = msg.reader().readShort();
                                    char6.bag = msg.reader().readUnsignedByte();
                                    char6.cName = msg.reader().readUTF();
                                    bool isOnline = msg.reader().readBoolean();
                                    InfoItem infoItem2 = new InfoItem(mResources.power + ": " + msg.reader().readUTF());
                                    infoItem2.charInfo = char6;
                                    infoItem2.isOnline = isOnline;
                                    GameCanvas.panel.vFriend.addElement(infoItem2);
                                }
                                GameCanvas.panel.setTypeFriend();
                                GameCanvas.panel.show();
                            }
                            if (b21 == 3)
                            {
                                MyVector vFriend = GameCanvas.panel.vFriend;
                                int num37 = msg.reader().readInt();
                                Res.outz("online offline id=" + num37);
                                for (int num38 = 0; num38 < vFriend.size(); num38++)
                                {
                                    InfoItem infoItem3 = (InfoItem)vFriend.elementAt(num38);
                                    if (infoItem3.charInfo != null && infoItem3.charInfo.charID == num37)
                                    {
                                        Res.outz("online= " + infoItem3.isOnline);
                                        infoItem3.isOnline = msg.reader().readBoolean();
                                        break;
                                    }
                                }
                            }
                            if (b21 != 2)
                            {
                                break;
                            }
                            MyVector vFriend2 = GameCanvas.panel.vFriend;
                            int num39 = msg.reader().readInt();
                            for (int num40 = 0; num40 < vFriend2.size(); num40++)
                            {
                                InfoItem infoItem4 = (InfoItem)vFriend2.elementAt(num40);
                                if (infoItem4.charInfo != null && infoItem4.charInfo.charID == num39)
                                {
                                    vFriend2.removeElement(infoItem4);
                                    break;
                                }
                            }
                            if (GameCanvas.panel.isShow)
                            {
                                GameCanvas.panel.setTabFriend();
                            }
                            break;
                        }
                    case -99:
                        {
                            InfoDlg.hide();
                            sbyte b11 = msg.reader().readByte();
                            if (b11 == 0)
                            {
                                GameCanvas.panel.vEnemy.removeAllElements();
                                int num19 = msg.reader().readUnsignedByte();
                                for (int l = 0; l < num19; l++)
                                {
                                    Char char4 = new Char();
                                    char4.charID = msg.reader().readInt();
                                    char4.head = msg.reader().readShort();
                                    char4.headICON = msg.reader().readShort();
                                    char4.body = msg.reader().readShort();
                                    char4.leg = msg.reader().readShort();
                                    char4.bag = msg.reader().readShort();
                                    char4.cName = msg.reader().readUTF();
                                    InfoItem infoItem = new InfoItem(msg.reader().readUTF());
                                    bool flag3 = msg.reader().readBoolean();
                                    infoItem.charInfo = char4;
                                    infoItem.isOnline = flag3;
                                    Res.outz("isonline = " + flag3);
                                    GameCanvas.panel.vEnemy.addElement(infoItem);
                                }
                                GameCanvas.panel.setTypeEnemy();
                                GameCanvas.panel.show();
                            }
                            break;
                        }
                    case -79:
                        {
                            InfoDlg.hide();
                            int num127 = msg.reader().readInt();
                            Char charMenu = GameCanvas.panel.charMenu;
                            if (charMenu == null)
                            {
                                return;
                            }
                            charMenu.cPower = msg.reader().readLong();
                            charMenu.currStrLevel = msg.reader().readUTF();
                            break;
                        }
                    case -93:
                        {
                            short num163 = msg.reader().readShort();
                            BgItem.newSmallVersion = new sbyte[num163];
                            for (int num164 = 0; num164 < num163; num164++)
                            {
                                BgItem.newSmallVersion[num164] = msg.reader().readByte();
                            }
                            break;
                        }
                    case -77:
                        {
                            short num129 = msg.reader().readShort();
                            SmallImage.newSmallVersion = new sbyte[num129];
                            SmallImage.maxSmall = num129;
                            SmallImage.imgNew = new Small[num129];
                            for (int num130 = 0; num130 < num129; num130++)
                            {
                                SmallImage.newSmallVersion[num130] = msg.reader().readByte();
                            }
                            break;
                        }
                    case -76:
                        {
                            sbyte b45 = msg.reader().readByte();
                            if (b45 == 0)
                            {
                                sbyte b46 = msg.reader().readByte();
                                if (b46 <= 0)
                                {
                                    return;
                                }
                                Char.myCharz().arrArchive = new Archivement[b46];
                                for (int num122 = 0; num122 < b46; num122++)
                                {
                                    Char.myCharz().arrArchive[num122] = new Archivement();
                                    Char.myCharz().arrArchive[num122].info1 = num122 + 1 + ". " + msg.reader().readUTF();
                                    Char.myCharz().arrArchive[num122].info2 = msg.reader().readUTF();
                                    Char.myCharz().arrArchive[num122].money = msg.reader().readShort();
                                    Char.myCharz().arrArchive[num122].isFinish = msg.reader().readBoolean();
                                    Char.myCharz().arrArchive[num122].isRecieve = msg.reader().readBoolean();
                                }
                                GameCanvas.panel.setTypeArchivement();
                                GameCanvas.panel.show();
                            }
                            else if (b45 == 1)
                            {
                                int num123 = msg.reader().readUnsignedByte();
                                if (Char.myCharz().arrArchive[num123] != null)
                                {
                                    Char.myCharz().arrArchive[num123].isRecieve = true;
                                }
                            }
                            break;
                        }
                    case -74:
                        {
                            if (ServerListScreen.stopDownload)
                            {
                                return;
                            }
                            if (!GameCanvas.isGetResourceFromServer())
                            {
                                Service.gI().getResource(3, null);
                                SmallImage.loadBigRMS();
                                SplashScr.imgLogo = null;
                                if (Rms.loadRMSString("acc2") != null || Rms.loadRMSString("userAo2" + ServerListScreen.ipSelect) != null)
                                {
                                    LoginScr.isContinueToLogin = true;
                                }
                                GameCanvas.loginScr = new LoginScr();
                                GameCanvas.loginScr.switchToMe();
                                return;
                            }
                            bool flag7 = true;
                            sbyte b36 = msg.reader().readByte();
                            if (b36 == 0)
                            {
                                int num91 = msg.reader().readInt();
                                string text3 = Rms.loadRMSString("ResVersion");
                                int num92 = ((text3 == null || !(text3 != string.Empty)) ? (-1) : int.Parse(text3));
                                if (text3 != null)
                                {
                                    mSystem.println(num91 + ">>>strVersion: " + text3 + " >> " + num92);
                                }
                                else
                                {
                                    mSystem.println(">>>strVersion: nulll: " + num92);
                                }
                                if (Session_ME.gI().isCompareIPConnect())
                                {
                                    if (num92 == -1 || num92 != num91)
                                    {
                                        GameCanvas.serverScreen.show2();
                                    }
                                    else
                                    {
                                        Res.outz("login ngay");
                                        SmallImage.loadBigRMS();
                                        SplashScr.imgLogo = null;
                                        ServerListScreen.loadScreen = true;
                                        if (GameCanvas.currentScreen != GameCanvas.loginScr)
                                        {
                                            GameCanvas.serverScreen.switchToMe();
                                        }
                                        else
                                        {
                                            if (GameCanvas.loginScr == null)
                                            {
                                                GameCanvas.loginScr = new LoginScr();
                                            }
                                            GameCanvas.loginScr.doLogin();
                                        }
                                    }
                                }
                                else
                                {
                                    Session_ME.gI().close();
                                    ServerListScreen.loadScreen = true;
                                    ServerListScreen.isAutoConect = false;
                                    ServerListScreen.countDieConnect = 1000;
                                    GameCanvas.serverScreen.switchToMe();
                                }
                            }
                            if (b36 == 1)
                            {
                                ServerListScreen.strWait = mResources.downloading_data;
                                short nBig = msg.reader().readShort();
                                ServerListScreen.nBig = nBig;
                                Service.gI().getResource(2, null);
                            }
                            if (b36 == 2)
                            {
                                try
                                {
                                    isLoadingData = true;
                                    GameCanvas.endDlg();
                                    ServerListScreen.demPercent++;
                                    ServerListScreen.percent = ServerListScreen.demPercent * 100 / ServerListScreen.nBig;
                                    string original = msg.reader().readUTF();
                                    string[] array9 = Res.split(original, "/", 0);
                                    string filename = "x" + mGraphics.zoomLevel + array9[array9.Length - 1];
                                    int num93 = msg.reader().readInt();
                                    sbyte[] data = new sbyte[num93];
                                    msg.reader().read(ref data, 0, num93);
                                    Rms.saveRMS(filename, data);
                                }
                                catch (Exception)
                                {
                                    GameCanvas.startOK(mResources.pls_restart_game_error, 8885, null);
                                }
                            }
                            if (b36 == 3 && flag7)
                            {
                                isLoadingData = false;
                                int num94 = msg.reader().readInt();
                                Res.outz("last version= " + num94);
                                Rms.saveRMSString("ResVersion", num94 + string.Empty);
                                Service.gI().getResource(3, null);
                                GameCanvas.endDlg();
                                SplashScr.imgLogo = null;
                                SmallImage.loadBigRMS();
                                mSystem.gcc();
                                ServerListScreen.bigOk = true;
                                ServerListScreen.loadScreen = true;
                                GameScr.gI().loadGameScr();
                                if (GameCanvas.currentScreen != GameCanvas.loginScr)
                                {
                                    GameCanvas.serverScreen.switchToMe();
                                }
                            }
                            break;
                        }
                    case -43:
                        {
                            sbyte itemAction = msg.reader().readByte();
                            sbyte where = msg.reader().readByte();
                            sbyte index = msg.reader().readByte();
                            string info = msg.reader().readUTF();
                            GameCanvas.panel.itemRequest(itemAction, info, where, index);
                            break;
                        }
                    case -59:
                        {
                            sbyte typePK = msg.reader().readByte();
                            GameScr.gI().player_vs_player(msg.reader().readInt(), msg.reader().readInt(), msg.reader().readUTF(), typePK);
                            break;
                        }
                    case -62:
                        {
                            int num51 = msg.reader().readUnsignedByte();
                            sbyte b26 = msg.reader().readByte();
                            if (b26 <= 0)
                            {
                                break;
                            }
                            ClanImage clanImage2 = ClanImage.getClanImage((short)num51);
                            if (clanImage2 == null)
                            {
                                break;
                            }
                            clanImage2.idImage = new short[b26];
                            for (int num52 = 0; num52 < b26; num52++)
                            {
                                clanImage2.idImage[num52] = msg.reader().readShort();
                                if (clanImage2.idImage[num52] > 0)
                                {
                                    SmallImage.vKeys.addElement(clanImage2.idImage[num52] + string.Empty);
                                }
                            }
                            break;
                        }
                    case -65:
                        {
                            InfoDlg.hide();
                            int num114 = msg.reader().readInt();
                            sbyte b41 = msg.reader().readByte();
                            if (b41 == 0)
                            {
                                break;
                            }
                            if (Char.myCharz().charID == num114)
                            {
                                isStopReadMessage = true;
                                GameScr.lockTick = 500;
                                GameScr.gI().center = null;
                                if (b41 == 0 || b41 == 1 || b41 == 3)
                                {
                                    Teleport p = new Teleport(Char.myCharz().cx, Char.myCharz().cy, Char.myCharz().head, Char.myCharz().cdir, 0, true, (b41 != 1) ? b41 : Char.myCharz().cgender);
                                    Teleport.addTeleport(p);
                                }
                                if (b41 == 2)
                                {
                                    GameScr.lockTick = 50;
                                    Char.myCharz().hide();
                                }
                            }
                            else
                            {
                                Char char7 = GameScr.findCharInMap(num114);
                                if ((b41 == 0 || b41 == 1 || b41 == 3) && char7 != null)
                                {
                                    char7.isUsePlane = true;
                                    Teleport teleport = new Teleport(char7.cx, char7.cy, char7.head, char7.cdir, 0, false, (b41 != 1) ? b41 : char7.cgender);
                                    teleport.id = num114;
                                    Teleport.addTeleport(teleport);
                                }
                                if (b41 == 2)
                                {
                                    char7.hide();
                                }
                            }
                            break;
                        }
                    case -64:
                        {
                            int num95 = msg.reader().readInt();
                            int num96 = msg.reader().readUnsignedByte();
                            @char = null;
                            @char = ((num95 != Char.myCharz().charID) ? GameScr.findCharInMap(num95) : Char.myCharz());
                            if (@char == null)
                            {
                                return;
                            }
                            @char.bag = num96;
                            for (int num97 = 0; num97 < 54; num97++)
                            {
                                @char.removeEffChar(0, 201 + num97);
                            }
                            if (@char.bag >= 201 && @char.bag < 255)
                            {
                                Effect effect = new Effect(@char.bag, @char, 2, -1, 10, 1);
                                effect.typeEff = 5;
                                @char.addEffChar(effect);
                            }
                            Res.outz("cmd:-64 UPDATE BAG PLAER = " + ((@char != null) ? @char.cName : string.Empty) + num95 + " BAG ID= " + num96);
                            if (num96 == 30 && @char.me)
                            {
                                GameScr.isPickNgocRong = true;
                            }
                            break;
                        }
                    case -63:
                        {
                            Res.outz("GET BAG");
                            int num16 = msg.reader().readUnsignedByte();
                            sbyte b9 = msg.reader().readByte();
                            ClanImage clanImage = new ClanImage();
                            clanImage.ID = num16;
                            if (b9 > 0)
                            {
                                clanImage.idImage = new short[b9];
                                for (int j = 0; j < b9; j++)
                                {
                                    clanImage.idImage[j] = msg.reader().readShort();
                                    Res.outz("ID=  " + num16 + " frame= " + clanImage.idImage[j]);
                                }
                                ClanImage.idImages.put(num16 + string.Empty, clanImage);
                            }
                            break;
                        }
                    case -57:
                        {
                            string strInvite = msg.reader().readUTF();
                            int clanID = msg.reader().readInt();
                            int code = msg.reader().readInt();
                            GameScr.gI().clanInvite(strInvite, clanID, code);
                            break;
                        }
                    case -51:
                        InfoDlg.hide();
                        readClanMsg(msg, 0);
                        if (GameCanvas.panel.isMessage && GameCanvas.panel.type == 5)
                        {
                            GameCanvas.panel.initTabClans();
                        }
                        break;
                    case -53:
                        {
                            InfoDlg.hide();
                            bool flag8 = false;
                            int num107 = msg.reader().readInt();
                            Res.outz("clanId= " + num107);
                            if (num107 == -1)
                            {
                                flag8 = true;
                                Char.myCharz().clan = null;
                                VoiceChat.daRoiBang();
                                ClanMessage.vMessage.removeAllElements();
                                // Xoa luon kenh Bang hoi trong khung chat.
                                //
                                // Roi bang, bi duoi, hay bang giai tan deu di
                                // qua day. Khong xoa thi vao bang MOI van con
                                // doc duoc tin cua bang cu — vua sai vua lo
                                // chuyen noi bo cua ho.
                                God.ChatUI.getInstance().batDauNapLaiBang();
                                if (GameCanvas.panel.member != null)
                                {
                                    GameCanvas.panel.member.removeAllElements();
                                }
                                if (GameCanvas.panel.myMember != null)
                                {
                                    GameCanvas.panel.myMember.removeAllElements();
                                }
                                if (GameCanvas.currentScreen == GameScr.gI())
                                {
                                    GameCanvas.panel.setTabClans();
                                }
                                return;
                            }
                            GameCanvas.panel.tabIcon = null;
                            if (Char.myCharz().clan == null)
                            {
                                Char.myCharz().clan = new Clan();
                            }
                            Char.myCharz().clan.ID = num107;
                            Char.myCharz().clan.name = msg.reader().readUTF();
                            Char.myCharz().clan.slogan = msg.reader().readUTF();
                            Char.myCharz().clan.imgID = msg.reader().readUnsignedByte();
                            Char.myCharz().clan.powerPoint = msg.reader().readUTF();
                            Char.myCharz().clan.leaderName = msg.reader().readUTF();
                            Char.myCharz().clan.currMember = msg.reader().readUnsignedByte();
                            Char.myCharz().clan.maxMember = msg.reader().readUnsignedByte();
                            Char.myCharz().role = msg.reader().readByte();
                            Char.myCharz().clan.clanPoint = msg.reader().readInt();
                            Char.myCharz().clan.level = msg.reader().readByte();
                            GameCanvas.panel.myMember = new MyVector();
                            for (int num108 = 0; num108 < Char.myCharz().clan.currMember; num108++)
                            {
                                Member member5 = new Member();
                                member5.ID = msg.reader().readInt();
                                member5.head = msg.reader().readShort();
                                member5.headICON = msg.reader().readShort();
                                member5.leg = msg.reader().readShort();
                                member5.body = msg.reader().readShort();
                                member5.name = msg.reader().readUTF();
                                member5.role = msg.reader().readByte();
                                member5.powerPoint = msg.reader().readUTF();
                                member5.donate = msg.reader().readInt();
                                member5.receive_donate = msg.reader().readInt();
                                member5.clanPoint = msg.reader().readInt();
                                member5.curClanPoint = msg.reader().readInt();
                                member5.joinTime = NinjaUtil.getDate(msg.reader().readInt());
                                GameCanvas.panel.myMember.addElement(member5);
                            }
                            int num109 = msg.reader().readUnsignedByte();
                            // May chu gui lai CA danh sach tin bang moi lan
                            // nap thong tin bang (dang nhap, mo bang hoi...).
                            // Xoa kenh Bang hoi truoc roi moi nap lai, khong
                            // thi moi lan nap la moi dong cu vao them mot ban
                            // — dung canh dang xuat vao lai thay chat x2.
                            God.ChatUI.getInstance().batDauNapLaiBang();
                            for (int num110 = 0; num110 < num109; num110++)
                            {
                                readClanMsg(msg, -1);
                            }
                            // Nap xong ca danh sach thi moi xep va ghi vao
                            // khung chat — xem ChatUI.ketThucNapBang().
                            God.ChatUI.getInstance().ketThucNapBang();
                            if (GameCanvas.panel.isSearchClan || GameCanvas.panel.isViewMember || GameCanvas.panel.isMessage)
                            {
                                GameCanvas.panel.setTabClans();
                            }
                            if (flag8)
                            {
                                GameCanvas.panel.setTabClans();
                            }
                            Res.outz("=>>>>>>>>>>>>>>>>>>>>>> -537 MY CLAN INFO");
                            break;
                        }
                    case -52:
                        {
                            sbyte b20 = msg.reader().readByte();
                            if (b20 == 0)
                            {
                                Member member2 = new Member();
                                member2.ID = msg.reader().readInt();
                                member2.head = msg.reader().readShort();
                                member2.headICON = msg.reader().readShort();
                                member2.leg = msg.reader().readShort();
                                member2.body = msg.reader().readShort();
                                member2.name = msg.reader().readUTF();
                                member2.role = msg.reader().readByte();
                                member2.powerPoint = msg.reader().readUTF();
                                member2.donate = msg.reader().readInt();
                                member2.receive_donate = msg.reader().readInt();
                                member2.clanPoint = msg.reader().readInt();
                                member2.joinTime = NinjaUtil.getDate(msg.reader().readInt());
                                if (GameCanvas.panel.myMember == null)
                                {
                                    GameCanvas.panel.myMember = new MyVector();
                                }
                                GameCanvas.panel.myMember.addElement(member2);
                                GameCanvas.panel.initTabClans();
                            }
                            if (b20 == 1)
                            {
                                GameCanvas.panel.myMember.removeElementAt(msg.reader().readByte());
                                GameCanvas.panel.currentListLength--;
                                GameCanvas.panel.initTabClans();
                            }
                            if (b20 == 2)
                            {
                                Member member3 = new Member();
                                member3.ID = msg.reader().readInt();
                                member3.head = msg.reader().readShort();
                                member3.headICON = msg.reader().readShort();
                                member3.leg = msg.reader().readShort();
                                member3.body = msg.reader().readShort();
                                member3.name = msg.reader().readUTF();
                                member3.role = msg.reader().readByte();
                                member3.powerPoint = msg.reader().readUTF();
                                member3.donate = msg.reader().readInt();
                                member3.receive_donate = msg.reader().readInt();
                                member3.clanPoint = msg.reader().readInt();
                                member3.joinTime = NinjaUtil.getDate(msg.reader().readInt());
                                for (int num34 = 0; num34 < GameCanvas.panel.myMember.size(); num34++)
                                {
                                    Member member4 = (Member)GameCanvas.panel.myMember.elementAt(num34);
                                    if (member4.ID == member3.ID)
                                    {
                                        if (Char.myCharz().charID == member3.ID)
                                        {
                                            Char.myCharz().role = member3.role;
                                        }
                                        Member o = member3;
                                        GameCanvas.panel.myMember.removeElement(member4);
                                        GameCanvas.panel.myMember.insertElementAt(o, num34);
                                        return;
                                    }
                                }
                            }
                            Res.outz("=>>>>>>>>>>>>>>>>>>>>>> -52  MY CLAN UPDSTE");
                            break;
                        }
                    case -50:
                        {
                            InfoDlg.hide();
                            GameCanvas.panel.member = new MyVector();
                            sbyte b17 = msg.reader().readByte();
                            for (int num26 = 0; num26 < b17; num26++)
                            {
                                Member member = new Member();
                                member.ID = msg.reader().readInt();
                                member.head = msg.reader().readShort();
                                member.headICON = msg.reader().readShort();
                                member.leg = msg.reader().readShort();
                                member.body = msg.reader().readShort();
                                member.name = msg.reader().readUTF();
                                member.role = msg.reader().readByte();
                                member.powerPoint = msg.reader().readUTF();
                                member.donate = msg.reader().readInt();
                                member.receive_donate = msg.reader().readInt();
                                member.clanPoint = msg.reader().readInt();
                                member.joinTime = NinjaUtil.getDate(msg.reader().readInt());
                                GameCanvas.panel.member.addElement(member);
                            }
                            GameCanvas.panel.isViewMember = true;
                            GameCanvas.panel.isSearchClan = false;
                            GameCanvas.panel.isMessage = false;
                            GameCanvas.panel.currentListLength = GameCanvas.panel.member.size() + 2;
                            GameCanvas.panel.initTabClans();
                            break;
                        }
                    case -47:
                        {
                            InfoDlg.hide();
                            sbyte b10 = msg.reader().readByte();
                            Res.outz("clan = " + b10);
                            if (b10 == 0)
                            {
                                GameCanvas.panel.clanReport = mResources.cannot_find_clan;
                                GameCanvas.panel.clans = null;
                            }
                            else
                            {
                                GameCanvas.panel.clans = new Clan[b10];
                                Res.outz("clan search lent= " + GameCanvas.panel.clans.Length);
                                for (int k = 0; k < GameCanvas.panel.clans.Length; k++)
                                {
                                    GameCanvas.panel.clans[k] = new Clan();
                                    GameCanvas.panel.clans[k].ID = msg.reader().readInt();
                                    GameCanvas.panel.clans[k].name = msg.reader().readUTF();
                                    GameCanvas.panel.clans[k].slogan = msg.reader().readUTF();
                                    GameCanvas.panel.clans[k].imgID = msg.reader().readUnsignedByte();
                                    GameCanvas.panel.clans[k].powerPoint = msg.reader().readUTF();
                                    GameCanvas.panel.clans[k].leaderName = msg.reader().readUTF();
                                    GameCanvas.panel.clans[k].currMember = msg.reader().readUnsignedByte();
                                    GameCanvas.panel.clans[k].maxMember = msg.reader().readUnsignedByte();
                                    GameCanvas.panel.clans[k].date = msg.reader().readInt();
                                }
                            }
                            GameCanvas.panel.isSearchClan = true;
                            GameCanvas.panel.isViewMember = false;
                            GameCanvas.panel.isMessage = false;
                            if (GameCanvas.panel.isSearchClan)
                            {
                                GameCanvas.panel.initTabClans();
                            }
                            break;
                        }
                    case -46:
                        {
                            InfoDlg.hide();
                            sbyte b65 = msg.reader().readByte();
                            if (b65 == 1 || b65 == 3)
                            {
                                GameCanvas.endDlg();
                                ClanImage.vClanImage.removeAllElements();
                                int num161 = msg.reader().readUnsignedByte();
                                for (int num162 = 0; num162 < num161; num162++)
                                {
                                    ClanImage clanImage3 = new ClanImage();
                                    clanImage3.ID = msg.reader().readUnsignedByte();
                                    clanImage3.name = msg.reader().readUTF();
                                    clanImage3.xu = msg.reader().readInt();
                                    clanImage3.luong = msg.reader().readInt();
                                    if (!ClanImage.isExistClanImage(clanImage3.ID))
                                    {
                                        ClanImage.addClanImage(clanImage3);
                                        continue;
                                    }
                                    ClanImage.getClanImage((short)clanImage3.ID).name = clanImage3.name;
                                    ClanImage.getClanImage((short)clanImage3.ID).xu = clanImage3.xu;
                                    ClanImage.getClanImage((short)clanImage3.ID).luong = clanImage3.luong;
                                }
                                if (Char.myCharz().clan != null)
                                {
                                    GameCanvas.panel.changeIcon();
                                }
                            }
                            if (b65 == 4)
                            {
                                Char.myCharz().clan.imgID = msg.reader().readUnsignedByte();
                                Char.myCharz().clan.slogan = msg.reader().readUTF();
                            }
                            break;
                        }
                    case -61:
                        {
                            int num131 = msg.reader().readInt();
                            if (num131 != Char.myCharz().charID)
                            {
                                if (GameScr.findCharInMap(num131) != null)
                                {
                                    GameScr.findCharInMap(num131).clanID = msg.reader().readInt();
                                    if (GameScr.findCharInMap(num131).clanID == -2)
                                    {
                                        GameScr.findCharInMap(num131).isCopy = true;
                                    }
                                }
                            }
                            else if (Char.myCharz().clan != null)
                            {
                                Char.myCharz().clan.ID = msg.reader().readInt();
                            }
                            break;
                        }
                    case -42:
                        Char.myCharz().cHPGoc = msg.readHp();
                        Char.myCharz().cMPGoc = msg.readInt3Byte();
                        Char.myCharz().cDamGoc = msg.reader().readInt();
                        Char.myCharz().cHPFull = msg.readHp();
                        Char.myCharz().cMPFull = msg.readInt3Byte();
                        Char.myCharz().cHP = msg.readHp();
                        Char.myCharz().cMP = msg.readInt3Byte();
                        Char.myCharz().cspeed = msg.reader().readByte();
                        Char.myCharz().hpFrom1000TiemNang = msg.reader().readByte();
                        Char.myCharz().mpFrom1000TiemNang = msg.reader().readByte();
                        Char.myCharz().damFrom1000TiemNang = msg.reader().readByte();
                        Char.myCharz().cDamFull = msg.reader().readInt();
                        Char.myCharz().cDefull = msg.reader().readInt();
                        Char.myCharz().cCriticalFull = msg.reader().readByte();
                        Char.myCharz().cTiemNang = msg.reader().readLong();
                        Char.myCharz().expForOneAdd = msg.reader().readShort();
                        Char.myCharz().cDefGoc = msg.reader().readShort();
                        Char.myCharz().cCriticalGoc = msg.reader().readByte();
                        InfoDlg.hide();
                        break;
                    case 1:
                        {
                            bool flag9 = msg.reader().readBool();
                            Res.outz("isRes= " + flag9);
                            if (!flag9)
                            {
                                GameCanvas.startOKDlg(msg.reader().readUTF());
                                break;
                            }
                            GameCanvas.loginScr.isLogin2 = false;
                            Rms.saveRMSString("userAo2" + ServerListScreen.ipSelect, string.Empty);
                            GameCanvas.endDlg();
                            GameCanvas.loginScr.doLogin();
                            break;
                        }
                    case 2:
                        Char.isLoadingMap = false;
                        LoginScr.isLoggingIn = false;
                        if (!GameScr.isLoadAllData)
                        {
                            GameScr.gI().initSelectChar();
                        }
                        BgItem.clearHashTable();
                        GameCanvas.endDlg();
                        CreateCharScr.isCreateChar = true;
                        CreateCharScr.gI().switchToMe();
                        break;
                    case -107:
                        {
                            sbyte b37 = msg.reader().readByte();
                            if (b37 == 0)
                            {
                                Char.myCharz().havePet = false;
                            }
                            if (b37 == 1)
                            {
                                Char.myCharz().havePet = true;
                            }
                            if (b37 != 2)
                            {
                                break;
                            }
                            InfoDlg.hide();
                            Char.myPetz().head = msg.reader().readShort();
                            Char.myPetz().setDefaultPart();
                            int num98 = msg.reader().readUnsignedByte();
                            Res.outz("num body = " + num98);
                            Char.myPetz().arrItemBody = new Item[num98];
                            for (int num99 = 0; num99 < num98; num99++)
                            {
                                short num100 = msg.reader().readShort();
                                Res.outz("template id= " + num100);
                                if (num100 == -1)
                                {
                                    continue;
                                }
                                Res.outz("1");
                                Char.myPetz().arrItemBody[num99] = new Item();
                                Char.myPetz().arrItemBody[num99].template = ItemTemplates.get(num100);
                                int num101 = Char.myPetz().arrItemBody[num99].template.type;
                                Char.myPetz().arrItemBody[num99].quantity = msg.reader().readInt();
                                Res.outz("3");
                                Char.myPetz().arrItemBody[num99].info = msg.reader().readUTF();
                                Char.myPetz().arrItemBody[num99].content = msg.reader().readUTF();
                                int num102 = msg.reader().readUnsignedByte();
                                Res.outz("option size= " + num102);
                                if (num102 != 0)
                                {
                                    Char.myPetz().arrItemBody[num99].itemOption = new ItemOption[num102];
                                    for (int num103 = 0; num103 < Char.myPetz().arrItemBody[num99].itemOption.Length; num103++)
                                    {
                                        int num104 = msg.reader().readInt();
                                        int param5 = msg.reader().readInt();
                                        if (num104 != -1)
                                        {
                                            Char.myPetz().arrItemBody[num99].itemOption[num103] = new ItemOption(num104, param5);
                                        }
                                    }
                                }
                                switch (num101)
                                {
                                    case 0:
                                        Char.myPetz().body = Char.myPetz().arrItemBody[num99].template.part;
                                        break;
                                    case 1:
                                        Char.myPetz().leg = Char.myPetz().arrItemBody[num99].template.part;
                                        break;
                                }
                            }
                            Char.myPetz().cHP = msg.readHp();
                            Char.myPetz().cHPFull = msg.readHp();
                            Char.myPetz().cMP = msg.readInt3Byte();
                            Char.myPetz().cMPFull = msg.readInt3Byte();
                            Char.myPetz().cDamFull = msg.readInt3Byte();
                            Char.myPetz().cName = msg.reader().readUTF();
                            Char.myPetz().currStrLevel = msg.reader().readUTF();
                            Char.myPetz().cPower = msg.reader().readLong();
                            Char.myPetz().cTiemNang = msg.reader().readLong();
                            Char.myPetz().petStatus = msg.reader().readByte();
                            Char.myPetz().cStamina = msg.reader().readShort();
                            Char.myPetz().cMaxStamina = msg.reader().readShort();
                            Char.myPetz().cCriticalFull = msg.reader().readByte();
                            Char.myPetz().cDefull = msg.reader().readShort();
                            Char.myPetz().arrPetSkill = new Skill[msg.reader().readByte()];
                            Res.outz("SKILLENT = " + Char.myPetz().arrPetSkill);
                            for (int num105 = 0; num105 < Char.myPetz().arrPetSkill.Length; num105++)
                            {
                                short num106 = msg.reader().readShort();
                                if (num106 != -1)
                                {
                                    Char.myPetz().arrPetSkill[num105] = Skills.get(num106);
                                    continue;
                                }
                                Char.myPetz().arrPetSkill[num105] = new Skill();
                                Char.myPetz().arrPetSkill[num105].template = null;
                                Char.myPetz().arrPetSkill[num105].moreInfo = msg.reader().readUTF();
                            }

                            // Gioi tinh cua DE, may chu ghi o cuoi goi.
                            //
                            // De co gioi tinh rieng, khong theo su phu. Truoc day
                            // goi nay khong co truong do nen cgender cua de luon
                            // la 0, va setDefaultPart() lay than/chan cua Trai
                            // Dat. De Namec hay Xayda thi hai ma anh do khong ton
                            // tai — than va chan bien mat, chi con cai dau.
                            //
                            // Phai dat gioi tinh TRUOC roi moi goi lai
                            // setDefaultPart(): ham do doc cgender de chon bo
                            // phan, goi truoc khi biet gioi tinh thi vo ich.
                            Char.myPetz().cgender = msg.reader().readByte();
                            Char.myPetz().setDefaultPart();

                            // Than va chan lay THANG tu may chu, ghi de len phan
                            // setDefaultPart() vua dat.
                            //
                            // setDefaultBody/setDefaultLeg chi tra ve bo phan mac
                            // dinh theo hanh tinh, va chi dung khi de dang mac ao
                            // quan. De chua mac gi thi hai so do khong phai bo
                            // phan that cua no — trong game ra mot cuc toi khong
                            // ra hinh gi. May chu co san getBody()/getLeg(), la
                            // dung ham no dung o moi noi khac de ve nhan vat.
                            Char.myPetz().body = msg.reader().readShort();
                            Char.myPetz().leg = msg.reader().readShort();

                            // Ma anh DAU, cung lay thang tu may chu.
                            //
                            // setDefaultPart() suy dau tu gioi tinh, nen de dang
                            // BIEN KHI van hien dau nguoi: dau khi khong theo gioi
                            // tinh ma theo CAP bien khi, va chi may chu biet cap
                            // do. Cai trang, hoa da, bien hinh su kien cung the.
                            Char.myPetz().head = msg.reader().readShort();
                            break;
                        }
                    case -37:
                        {
                            sbyte b27 = msg.reader().readByte();
                            Res.outz("cAction= " + b27);
                            if (b27 != 0)
                            {
                                break;
                            }
                            Char.myCharz().head = msg.reader().readShort();
                            Char.myCharz().setDefaultPart();
                            int num53 = msg.reader().readUnsignedByte();
                            Res.outz("num body = " + num53);
                            Char.myCharz().arrItemBody = new Item[num53];
                            for (int num54 = 0; num54 < num53; num54++)
                            {
                                short num55 = msg.reader().readShort();
                                if (num55 == -1)
                                {
                                    continue;
                                }
                                Char.myCharz().arrItemBody[num54] = new Item();
                                Char.myCharz().arrItemBody[num54].template = ItemTemplates.get(num55);
                                int num56 = Char.myCharz().arrItemBody[num54].template.type;
                                Char.myCharz().arrItemBody[num54].quantity = msg.reader().readInt();
                                Char.myCharz().arrItemBody[num54].info = msg.reader().readUTF();
                                Char.myCharz().arrItemBody[num54].content = msg.reader().readUTF();
                                int num57 = msg.reader().readUnsignedByte();
                                if (num57 != 0)
                                {
                                    Char.myCharz().arrItemBody[num54].itemOption = new ItemOption[num57];
                                    for (int num58 = 0; num58 < Char.myCharz().arrItemBody[num54].itemOption.Length; num58++)
                                    {
                                        int num59 = msg.reader().readInt();
                                        int param3 = msg.reader().readInt();
                                        if (num59 != -1)
                                        {
                                            Char.myCharz().arrItemBody[num54].itemOption[num58] = new ItemOption(num59, param3);
                                        }
                                    }
                                }
                                switch (num56)
                                {
                                    case 0:
                                        Char.myCharz().body = Char.myCharz().arrItemBody[num54].template.part;
                                        break;
                                    case 1:
                                        Char.myCharz().leg = Char.myCharz().arrItemBody[num54].template.part;
                                        break;
                                }
                            }
                            break;
                        }
                    case -36:
                        {
                            sbyte b18 = msg.reader().readByte();
                            Res.outz("cAction= " + b18);
                            GameScr.isudungCapsun4 = false;
                            GameScr.isudungCapsun3 = false;
                            if (b18 == 0)
                            {
                                int num27 = msg.reader().readUnsignedByte();
                                Char.myCharz().arrItemBag = new Item[num27];
                                GameScr.hpPotion = 0;
                                Res.outz("numC=" + num27);
                                for (int num28 = 0; num28 < num27; num28++)
                                {
                                    short num29 = msg.reader().readShort();
                                    if (num29 == -1)
                                    {
                                        continue;
                                    }
                                    Char.myCharz().arrItemBag[num28] = new Item();
                                    Char.myCharz().arrItemBag[num28].template = ItemTemplates.get(num29);
                                    Char.myCharz().arrItemBag[num28].quantity = msg.reader().readInt();
                                    Char.myCharz().arrItemBag[num28].info = msg.reader().readUTF();
                                    Char.myCharz().arrItemBag[num28].content = msg.reader().readUTF();
                                    Char.myCharz().arrItemBag[num28].indexUI = num28;
                                    int num30 = msg.reader().readUnsignedByte();
                                    if (num30 != 0)
                                    {
                                        Char.myCharz().arrItemBag[num28].itemOption = new ItemOption[num30];
                                        for (int num31 = 0; num31 < Char.myCharz().arrItemBag[num28].itemOption.Length; num31++)
                                        {
                                            int num32 = msg.reader().readInt();
                                            int param2 = msg.reader().readInt();
                                            if (num32 != -1)
                                            {
                                                Char.myCharz().arrItemBag[num28].itemOption[num31] = new ItemOption(num32, param2);
                                            }
                                        }
                                        Char.myCharz().arrItemBag[num28].compare = GameCanvas.panel.getCompare(Char.myCharz().arrItemBag[num28]);
                                    }
                                    // Mau vat pham la khong biet -> bo o do, dung
                                    // de vo ca goi.
                                    //
                                    // ItemTemplates.get() tra null khi bang mau
                                    // cua may cu hon may chu (quen tang vs_item
                                    // sau khi them mon moi). Doc tiep vao
                                    // .template.type la nem NullReference NGAY
                                    // GIUA vong doc, nen ca goi -36 bi bo — hanh
                                    // trang khong cap nhat gi het, khong rieng
                                    // mon la. Trieu chung ben ngoai la "phat do
                                    // tu panel khong vao tui", va no keo dai mai
                                    // vi mon la van nam trong tui.
                                    if (Char.myCharz().arrItemBag[num28].template == null)
                                    {
                                        Char.myCharz().arrItemBag[num28] = null;
                                        continue;
                                    }
                                    if (Char.myCharz().arrItemBag[num28].template.type == 11)
                                    {
                                    }
                                    if (Char.myCharz().arrItemBag[num28].template.type == 6)
                                    {
                                        GameScr.hpPotion += Char.myCharz().arrItemBag[num28].quantity;
                                    }
                                    if (Char.myCharz().arrItemBag[num28].template.id == 194)
                                    {
                                        GameScr.isudungCapsun4 = Char.myCharz().arrItemBag[num28].quantity > 0;
                                    }
                                    else if (Char.myCharz().arrItemBag[num28].template.id == 193 && !GameScr.isudungCapsun4)
                                    {
                                        GameScr.isudungCapsun3 = Char.myCharz().arrItemBag[num28].quantity > 0;
                                    }
                                }
                            }
                            if (b18 == 2)
                            {
                                sbyte b19 = msg.reader().readByte();
                                int num33 = msg.reader().readInt();
                                int quantity2 = Char.myCharz().arrItemBag[b19].quantity;
                                int id = Char.myCharz().arrItemBag[b19].template.id;
                                Char.myCharz().arrItemBag[b19].quantity = num33;
                                if (Char.myCharz().arrItemBag[b19].quantity < quantity2 && Char.myCharz().arrItemBag[b19].template.type == 6)
                                {
                                    GameScr.hpPotion -= quantity2 - Char.myCharz().arrItemBag[b19].quantity;
                                }
                                if (Char.myCharz().arrItemBag[b19].quantity == 0)
                                {
                                    Char.myCharz().arrItemBag[b19] = null;
                                }
                                switch (id)
                                {
                                    case 194:
                                        GameScr.isudungCapsun4 = num33 > 0;
                                        break;
                                    case 193:
                                        GameScr.isudungCapsun3 = num33 > 0;
                                        break;
                                }
                            }
                            break;
                        }
                    case -35:
                        {
                            sbyte b12 = msg.reader().readByte();
                            Res.outz("cAction= " + b12);
                            if (b12 == 0)
                            {
                                int num20 = msg.reader().readUnsignedByte();
                                Char.myCharz().arrItemBox = new Item[num20];
                                GameCanvas.panel.hasUse = 0;
                                for (int m = 0; m < num20; m++)
                                {
                                    short num21 = msg.reader().readShort();
                                    if (num21 == -1)
                                    {
                                        continue;
                                    }
                                    Char.myCharz().arrItemBox[m] = new Item();
                                    Char.myCharz().arrItemBox[m].template = ItemTemplates.get(num21);
                                    Char.myCharz().arrItemBox[m].quantity = msg.reader().readInt();
                                    Char.myCharz().arrItemBox[m].info = msg.reader().readUTF();
                                    Char.myCharz().arrItemBox[m].content = msg.reader().readUTF();
                                    int num22 = msg.reader().readUnsignedByte();
                                    if (num22 != 0)
                                    {
                                        Char.myCharz().arrItemBox[m].itemOption = new ItemOption[num22];
                                        for (int n = 0; n < Char.myCharz().arrItemBox[m].itemOption.Length; n++)
                                        {
                                            int num23 = msg.reader().readInt();
                                            int param = msg.reader().readInt();
                                            if (num23 != -1)
                                            {
                                                Char.myCharz().arrItemBox[m].itemOption[n] = new ItemOption(num23, param);
                                            }
                                        }
                                    }
                                    GameCanvas.panel.hasUse++;
                                }
                            }
                            if (b12 == 1)
                            {
                                bool isBoxClan = false;
                                try
                                {
                                    sbyte b13 = msg.reader().readByte();
                                    if (b13 == 1)
                                    {
                                        isBoxClan = true;
                                    }
                                }
                                catch (Exception)
                                {
                                }
                                GameCanvas.panel.setTypeBox();
                                GameCanvas.panel.isBoxClan = isBoxClan;
                                GameCanvas.panel.show();
                            }
                            if (b12 == 2)
                            {
                                sbyte b14 = msg.reader().readByte();
                                int quantity = msg.reader().readInt();
                                Char.myCharz().arrItemBox[b14].quantity = quantity;
                                if (Char.myCharz().arrItemBox[b14].quantity == 0)
                                {
                                    Char.myCharz().arrItemBox[b14] = null;
                                }
                            }
                            break;
                        }
                    case -45:
                        {
                            sbyte b51 = msg.reader().readByte();
                            int num135 = msg.reader().readInt();
                            short num136 = msg.reader().readShort();
                            Res.outz(">.SKILL_NOT_FOCUS      skillNotFocusID: " + num136 + " skill type= " + b51 + "   player use= " + num135);
                            if (b51 == 20)
                            {
                                sbyte b52 = msg.reader().readByte();
                                sbyte dir = msg.reader().readByte();
                                short timeGong = msg.reader().readShort();
                                bool isFly = ((msg.reader().readByte() != 0) ? true : false);
                                sbyte typePaint = msg.reader().readByte();
                                sbyte typeItem = -1;
                                try
                                {
                                    typeItem = msg.reader().readByte();
                                }
                                catch (Exception)
                                {
                                }
                                Res.outz(">.SKILL_NOT_FOCUS  skill typeFrame= " + b52);
                                @char = ((Char.myCharz().charID != num135) ? GameScr.findCharInMap(num135) : Char.myCharz());
                                @char.SetSkillPaint_NEW(num136, isFly, b52, typePaint, dir, timeGong, typeItem);
                            }
                            if (b51 == 21)
                            {
                                Point point = new Point();
                                point.x = msg.reader().readShort();
                                point.y = msg.reader().readShort();
                                short timeDame = msg.reader().readShort();
                                short rangeDame = msg.reader().readShort();
                                sbyte typePaint2 = 0;
                                sbyte typeItem2 = -1;
                                Point[] array10 = null;
                                @char = ((Char.myCharz().charID != num135) ? GameScr.findCharInMap(num135) : Char.myCharz());
                                try
                                {
                                    typePaint2 = msg.reader().readByte();
                                    sbyte b53 = msg.reader().readByte();
                                    if (b53 > 0)
                                    {
                                        array10 = new Point[b53];
                                        for (int num137 = 0; num137 < array10.Length; num137++)
                                        {
                                            array10[num137] = new Point();
                                            array10[num137].type = msg.reader().readByte();
                                            if (array10[num137].type == 0)
                                            {
                                                array10[num137].id = msg.reader().readByte();
                                            }
                                            else
                                            {
                                                array10[num137].id = msg.reader().readInt();
                                            }
                                        }
                                    }
                                }
                                catch (Exception)
                                {
                                }
                                try
                                {
                                    typeItem2 = msg.reader().readByte();
                                }
                                catch (Exception)
                                {
                                }
                                Res.outz(">.SKILL_NOT_FOCUS  skill targetDame= " + point.x + ":" + point.y + "    c:" + @char.cx + ":" + @char.cy + "   cdir:" + @char.cdir);
                                @char.SetSkillPaint_STT(1, num136, point, timeDame, rangeDame, typePaint2, array10, typeItem2);
                            }
                            if (b51 == 0)
                            {
                                Res.outz("id use= " + num135);
                                if (Char.myCharz().charID != num135)
                                {
                                    @char = GameScr.findCharInMap(num135);
                                    if ((TileMap.tileTypeAtPixel(@char.cx, @char.cy) & 2) == 2)
                                    {
                                        @char.setSkillPaint(GameScr.sks[num136], 0);
                                    }
                                    else
                                    {
                                        @char.setSkillPaint(GameScr.sks[num136], 1);
                                        @char.delayFall = 20;
                                    }
                                }
                                else
                                {
                                    Char.myCharz().saveLoadPreviousSkill();
                                    Res.outz("LOAD LAST SKILL");
                                }
                                sbyte b54 = msg.reader().readByte();
                                Res.outz("npc size= " + b54);
                                for (int num138 = 0; num138 < b54; num138++)
                                {
                                    sbyte b55 = msg.reader().readByte();
                                    sbyte b56 = msg.reader().readByte();
                                    Res.outz("index= " + b55);
                                    if (num136 >= 42 && num136 <= 48)
                                    {
                                        ((Mob)GameScr.vMob.elementAt(b55)).isFreez = true;
                                        ((Mob)GameScr.vMob.elementAt(b55)).seconds = b56;
                                        ((Mob)GameScr.vMob.elementAt(b55)).last = (((Mob)GameScr.vMob.elementAt(b55)).cur = mSystem.currentTimeMillis());
                                    }
                                }
                                sbyte b57 = msg.reader().readByte();
                                for (int num139 = 0; num139 < b57; num139++)
                                {
                                    int num140 = msg.reader().readInt();
                                    sbyte b58 = msg.reader().readByte();
                                    Res.outz("player ID= " + num140 + " my ID= " + Char.myCharz().charID);
                                    if (num136 < 42 || num136 > 48)
                                    {
                                        continue;
                                    }
                                    if (num140 == Char.myCharz().charID)
                                    {
                                        if (!Char.myCharz().isFlyAndCharge && !Char.myCharz().isStandAndCharge)
                                        {
                                            GameScr.gI().isFreez = true;
                                            Char.myCharz().isFreez = true;
                                            Char.myCharz().freezSeconds = b58;
                                            Char.myCharz().lastFreez = (Char.myCharz().currFreez = mSystem.currentTimeMillis());
                                            Char.myCharz().isLockMove = true;
                                        }
                                    }
                                    else
                                    {
                                        @char = GameScr.findCharInMap(num140);
                                        if (@char != null && !@char.isFlyAndCharge && !@char.isStandAndCharge)
                                        {
                                            @char.isFreez = true;
                                            @char.seconds = b58;
                                            @char.freezSeconds = b58;
                                            @char.lastFreez = (GameScr.findCharInMap(num140).currFreez = mSystem.currentTimeMillis());
                                        }
                                    }
                                }
                            }
                            if (b51 == 1 && num135 != Char.myCharz().charID)
                            {
                                try
                                {
                                    GameScr.findCharInMap(num135).isCharge = true;
                                }
                                catch (Exception)
                                {
                                }
                            }
                            if (b51 == 3)
                            {
                                if (num135 == Char.myCharz().charID)
                                {
                                    Char.myCharz().isCharge = false;
                                    SoundMn.gI().taitaoPause();
                                    Char.myCharz().saveLoadPreviousSkill();
                                }
                                else
                                {
                                    GameScr.findCharInMap(num135).isCharge = false;
                                }
                            }
                            if (b51 == 4)
                            {
                                if (num135 == Char.myCharz().charID)
                                {
                                    Char.myCharz().seconds = msg.reader().readShort() - 1000;
                                    Char.myCharz().last = mSystem.currentTimeMillis();
                                    Res.outz("second= " + Char.myCharz().seconds + " last= " + Char.myCharz().last);
                                }
                                else if (GameScr.findCharInMap(num135) != null)
                                {
                                    Char char9 = GameScr.findCharInMap(num135);
                                    switch (char9.cgender)
                                    {
                                        case 0:
                                            if (TileMap.mapID != 170)
                                            {
                                                @char.useChargeSkill(false);
                                                break;
                                            }
                                            if (num136 >= 77 && num136 <= 83)
                                            {
                                                @char.useChargeSkill(true);
                                            }
                                            if (num136 >= 70 && num136 <= 76)
                                            {
                                                @char.useChargeSkill(false);
                                            }
                                            break;
                                        case 1:
                                            {
                                                if (TileMap.mapID != 170)
                                                {
                                                    @char.useChargeSkill(true);
                                                    break;
                                                }
                                                bool isGround2 = true;
                                                if (num136 >= 70 && num136 <= 76)
                                                {
                                                    isGround2 = false;
                                                }
                                                if (num136 >= 77 && num136 <= 83)
                                                {
                                                    isGround2 = true;
                                                }
                                                @char.useChargeSkill(isGround2);
                                                break;
                                            }
                                        default:
                                            if (TileMap.mapID == 170)
                                            {
                                                bool isGround = true;
                                                if (num136 >= 70 && num136 <= 76)
                                                {
                                                    isGround = false;
                                                }
                                                if (num136 >= 77 && num136 <= 83)
                                                {
                                                    isGround = true;
                                                }
                                                @char.useChargeSkill(isGround);
                                            }
                                            break;
                                    }
                                    @char.skillTemplateId = num136;
                                    if (num136 >= 70 && num136 <= 76)
                                    {
                                        @char.isUseSkillAfterCharge = true;
                                    }
                                    @char.seconds = msg.reader().readShort();
                                    @char.last = mSystem.currentTimeMillis();
                                }
                            }
                            if (b51 == 5)
                            {
                                if (num135 == Char.myCharz().charID)
                                {
                                    Char.myCharz().stopUseChargeSkill();
                                }
                                else if (GameScr.findCharInMap(num135) != null)
                                {
                                    GameScr.findCharInMap(num135).stopUseChargeSkill();
                                }
                            }
                            if (b51 == 6)
                            {
                                if (num135 == Char.myCharz().charID)
                                {
                                    Char.myCharz().setAutoSkillPaint(GameScr.sks[num136], 0);
                                }
                                else if (GameScr.findCharInMap(num135) != null)
                                {
                                    GameScr.findCharInMap(num135).setAutoSkillPaint(GameScr.sks[num136], 0);
                                    SoundMn.gI().gong();
                                }
                            }
                            if (b51 == 7)
                            {
                                if (num135 == Char.myCharz().charID)
                                {
                                    Char.myCharz().seconds = msg.reader().readShort();
                                    Res.outz("second = " + Char.myCharz().seconds);
                                    Char.myCharz().last = mSystem.currentTimeMillis();
                                }
                                else if (GameScr.findCharInMap(num135) != null)
                                {
                                    GameScr.findCharInMap(num135).useChargeSkill(true);
                                    GameScr.findCharInMap(num135).seconds = msg.reader().readShort();
                                    GameScr.findCharInMap(num135).last = mSystem.currentTimeMillis();
                                    SoundMn.gI().gong();
                                }
                            }
                            if (b51 == 8 && num135 != Char.myCharz().charID && GameScr.findCharInMap(num135) != null)
                            {
                                GameScr.findCharInMap(num135).setAutoSkillPaint(GameScr.sks[num136], 0);
                            }
                            break;
                        }
                    case -44:
                        {
                            bool flag6 = false;
                            if (GameCanvas.w >= 2 * Panel.WIDTH_PANEL)
                            {
                                flag6 = true;
                            }
                            sbyte b33 = msg.reader().readByte();
                            int num78 = msg.reader().readUnsignedByte();
                            Char.myCharz().arrItemShop = new Item[num78][];
                            GameCanvas.panel.shopTabName = new string[num78 + ((!flag6) ? 1 : 0)][];
                            for (int num79 = 0; num79 < GameCanvas.panel.shopTabName.Length; num79++)
                            {
                                GameCanvas.panel.shopTabName[num79] = new string[2];
                            }
                            if (b33 == 2)
                            {
                                GameCanvas.panel.maxPageShop = new int[num78];
                                GameCanvas.panel.currPageShop = new int[num78];
                            }
                            if (!flag6)
                            {
                                GameCanvas.panel.shopTabName[num78] = mResources.inventory;
                            }
                            for (int num80 = 0; num80 < num78; num80++)
                            {
                                string[] array5 = Res.split(msg.reader().readUTF(), "\n", 0);
                                if (b33 == 2)
                                {
                                    GameCanvas.panel.maxPageShop[num80] = msg.reader().readUnsignedByte();
                                }
                                if (array5.Length == 2)
                                {
                                    GameCanvas.panel.shopTabName[num80] = array5;
                                }
                                if (array5.Length == 1)
                                {
                                    GameCanvas.panel.shopTabName[num80][0] = array5[0];
                                    GameCanvas.panel.shopTabName[num80][1] = string.Empty;
                                }
                                int num81 = msg.reader().readUnsignedByte();
                                Char.myCharz().arrItemShop[num80] = new Item[num81];
                                Panel.strWantToBuy = mResources.say_wat_do_u_want_to_buy;
                                if (b33 == 1)
                                {
                                    Panel.strWantToBuy = mResources.say_wat_do_u_want_to_buy2;
                                }
                                for (int num82 = 0; num82 < num81; num82++)
                                {
                                    short num83 = msg.reader().readShort();
                                    if (num83 == -1)
                                    {
                                        continue;
                                    }
                                    Char.myCharz().arrItemShop[num80][num82] = new Item();
                                    Char.myCharz().arrItemShop[num80][num82].template = ItemTemplates.get(num83);
                                    Res.outz("name " + num80 + " = " + Char.myCharz().arrItemShop[num80][num82].template.name + " id templat= " + Char.myCharz().arrItemShop[num80][num82].template.id);
                                    if (b33 == 8)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].buyCoin = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].buyGold = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].quantity = msg.reader().readInt();
                                    }
                                    else if (b33 == 4)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].reason = msg.reader().readUTF();
                                    }
                                    else if (b33 == 0)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].buyCoin = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].buyGold = msg.reader().readInt();
                                    }
                                    else if (b33 == 1)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].powerRequire = msg.reader().readLong();
                                    }
                                    else if (b33 == 2)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].itemId = msg.reader().readShort();
                                        Char.myCharz().arrItemShop[num80][num82].buyCoin = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].buyGold = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].buyType = msg.reader().readByte();
                                        Char.myCharz().arrItemShop[num80][num82].quantity = msg.reader().readInt();
                                        Char.myCharz().arrItemShop[num80][num82].isMe = msg.reader().readByte();
                                    }
                                    else if (b33 == 3)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].isBuySpec = true;
                                        Char.myCharz().arrItemShop[num80][num82].iconSpec = msg.reader().readShort();
                                        Char.myCharz().arrItemShop[num80][num82].buySpec = msg.reader().readInt();
                                    }
                                    int num84 = msg.reader().readUnsignedByte();
                                    if (num84 != 0)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].itemOption = new ItemOption[num84];
                                        for (int num85 = 0; num85 < Char.myCharz().arrItemShop[num80][num82].itemOption.Length; num85++)
                                        {
                                            int num86 = msg.reader().readInt();
                                            int param4 = msg.reader().readInt();
                                            // Tran cua khoang ngau nhien, may chu
                                            // moi gui them. -1 la tri co dinh.
                                            int paramMax4 = msg.reader().readInt();
                                            if (num86 != -1)
                                            {
                                                Char.myCharz().arrItemShop[num80][num82].itemOption[num85] = new ItemOption(num86, param4);
                                                Char.myCharz().arrItemShop[num80][num82].itemOption[num85].paramMax = paramMax4;
                                                Char.myCharz().arrItemShop[num80][num82].compare = GameCanvas.panel.getCompare(Char.myCharz().arrItemShop[num80][num82]);
                                            }
                                        }
                                    }
                                    sbyte b34 = msg.reader().readByte();
                                    Char.myCharz().arrItemShop[num80][num82].newItem = ((b34 != 0) ? true : false);
                                    sbyte b35 = msg.reader().readByte();
                                    if (b35 == 1)
                                    {
                                        int headTemp = msg.reader().readShort();
                                        int bodyTemp = msg.reader().readShort();
                                        int legTemp = msg.reader().readShort();
                                        int bagTemp = msg.reader().readShort();
                                        Char.myCharz().arrItemShop[num80][num82].setPartTemp(headTemp, bodyTemp, legTemp, bagTemp);
                                    }
                                    if (b33 == 2 && GameMidlet.intVERSION >= 237)
                                    {
                                        Char.myCharz().arrItemShop[num80][num82].nameNguoiKyGui = msg.reader().readUTF();
                                        Res.err("nguoi ki gui  " + Char.myCharz().arrItemShop[num80][num82].nameNguoiKyGui);
                                    }
                                }
                            }
                            if (flag6)
                            {
                                if (b33 != 2)
                                {
                                    GameCanvas.panel2 = new Panel();
                                    GameCanvas.panel2.tabName[7] = new string[1][] { new string[1] { string.Empty } };
                                    GameCanvas.panel2.setTypeBodyOnly();
                                    GameCanvas.panel2.show();
                                }
                                else
                                {
                                    GameCanvas.panel2 = new Panel();
                                    GameCanvas.panel2.setTypeKiGuiOnly();
                                    GameCanvas.panel2.show();
                                }
                            }
                            GameCanvas.panel.tabName[1] = GameCanvas.panel.shopTabName;
                            if (b33 == 2)
                            {
                                string[][] array6 = GameCanvas.panel.tabName[1];
                                if (flag6)
                                {
                                    GameCanvas.panel.tabName[1] = new string[4][]
                                    {
                                array6[0],
                                array6[1],
                                array6[2],
                                array6[3]
                                    };
                                }
                                else
                                {
                                    GameCanvas.panel.tabName[1] = new string[5][]
                                    {
                                array6[0],
                                array6[1],
                                array6[2],
                                array6[3],
                                array6[4]
                                    };
                                }
                            }
                            GameCanvas.panel.setTypeShop(b33);
                            GameCanvas.panel.show();
                            break;
                        }
                    case -41:
                        {
                            sbyte b28 = msg.reader().readByte();
                            Char.myCharz().strLevel = new string[b28];
                            for (int num60 = 0; num60 < b28; num60++)
                            {
                                string text2 = msg.reader().readUTF();
                                Char.myCharz().strLevel[num60] = text2;
                            }
                            Res.outz("---   xong  level caption cmd : " + msg.command);
                            break;
                        }
                    case -34:
                        {
                            sbyte b15 = msg.reader().readByte();
                            Res.outz("act= " + b15);
                            if (b15 == 0 && GameScr.gI().magicTree != null)
                            {
                                Res.outz("toi duoc day");
                                MagicTree magicTree = GameScr.gI().magicTree;
                                magicTree.id = msg.reader().readShort();
                                magicTree.name = msg.reader().readUTF();
                                magicTree.name = Res.changeString(magicTree.name);
                                magicTree.x = msg.reader().readShort();
                                magicTree.y = msg.reader().readShort();
                                magicTree.level = msg.reader().readByte();
                                magicTree.currPeas = msg.reader().readShort();
                                magicTree.maxPeas = msg.reader().readShort();
                                Res.outz("curr Peas= " + magicTree.currPeas);
                                magicTree.strInfo = msg.reader().readUTF();
                                magicTree.seconds = msg.reader().readInt();
                                magicTree.timeToRecieve = magicTree.seconds;
                                sbyte b16 = msg.reader().readByte();
                                magicTree.peaPostionX = new int[b16];
                                magicTree.peaPostionY = new int[b16];
                                for (int num24 = 0; num24 < b16; num24++)
                                {
                                    magicTree.peaPostionX[num24] = msg.reader().readByte();
                                    magicTree.peaPostionY[num24] = msg.reader().readByte();
                                }
                                magicTree.isUpdate = msg.reader().readBool();
                                magicTree.last = (magicTree.cur = mSystem.currentTimeMillis());
                                GameScr.gI().magicTree.isUpdateTree = true;
                            }
                            if (b15 == 1)
                            {
                                myVector = new MyVector();
                                try
                                {
                                    while (msg.reader().available() > 0)
                                    {
                                        string caption = msg.reader().readUTF();
                                        myVector.addElement(new Command(caption, GameCanvas.instance, 888392, null));
                                    }
                                }
                                catch (Exception ex5)
                                {
                                    Cout.println("Loi MAGIC_TREE " + ex5.ToString());
                                }
                                GameCanvas.menu.startAt(myVector, 3);
                            }
                            if (b15 == 2)
                            {
                                GameScr.gI().magicTree.remainPeas = msg.reader().readShort();
                                GameScr.gI().magicTree.seconds = msg.reader().readInt();
                                GameScr.gI().magicTree.last = (GameScr.gI().magicTree.cur = mSystem.currentTimeMillis());
                                GameScr.gI().magicTree.isUpdateTree = true;
                                GameScr.gI().magicTree.isPeasEffect = true;
                            }
                            break;
                        }
                    case 11:
                        {
                            GameCanvas.debug("SA9", 2);
                            int num9 = msg.reader().readByte();
                            sbyte b6 = msg.reader().readByte();
                            if (b6 != 0)
                            {
                                Mob.arrMobTemplate[num9].data.readDataNewBoss(NinjaUtil.readByteArray(msg), b6);
                            }
                            else
                            {
                                Mob.arrMobTemplate[num9].data.readData(NinjaUtil.readByteArray(msg));
                            }
                            for (int i = 0; i < GameScr.vMob.size(); i++)
                            {
                                mob = (Mob)GameScr.vMob.elementAt(i);
                                if (mob.templateId == num9)
                                {
                                    mob.w = Mob.arrMobTemplate[num9].data.width;
                                    mob.h = Mob.arrMobTemplate[num9].data.height;
                                }
                            }
                            sbyte[] array2 = NinjaUtil.readByteArray(msg);
                            Image img = Image.createImage(array2, 0, array2.Length);
                            Mob.arrMobTemplate[num9].data.img = img;
                            int num10 = msg.reader().readByte();
                            Mob.arrMobTemplate[num9].data.typeData = num10;
                            if (num10 == 1 || num10 == 2)
                            {
                                readFrameBoss(msg, num9);
                            }
                            break;
                        }
                    case -69:
                        Char.myCharz().cMaxStamina = msg.reader().readShort();
                        break;
                    case -68:
                        Char.myCharz().cStamina = msg.reader().readShort();
                        break;
                    case -67:
                        {
                            demCount += 1f;
                            int num168 = msg.reader().readInt();
                            sbyte[] array17 = null;
                            try
                            {
                                array17 = NinjaUtil.readByteArray(msg);
                                if (num168 == 3896)
                                {
                                }
                                SmallImage.imgNew[num168].img = createImage(array17);
                            }
                            catch (Exception)
                            {
                                array17 = null;
                                SmallImage.imgNew[num168].img = Image.createRGBImage(new int[1], 1, 1, true);
                            }
                            if (array17 != null && mGraphics.zoomLevel > 1)
                            {
                                Rms.saveRMS(mGraphics.zoomLevel + "Small" + num168, array17);
                            }
                            break;
                        }
                    case -66:
                        {
                            short id3 = msg.reader().readShort();
                            sbyte[] data4 = NinjaUtil.readByteArray(msg);
                            EffectData effDataById = Effect.getEffDataById(id3);
                            sbyte b67 = msg.reader().readSByte();
                            if (b67 == 0)
                            {
                                effDataById.readData(data4);
                            }
                            else
                            {
                                effDataById.readDataNewBoss(data4, b67);
                            }
                            sbyte[] array15 = NinjaUtil.readByteArray(msg);
                            effDataById.img = Image.createImage(array15, 0, array15.Length);
                            break;
                        }
                    case -32:
                        {
                            short num156 = msg.reader().readShort();
                            int num157 = msg.reader().readInt();
                            sbyte[] array11 = null;
                            Image image = null;
                            try
                            {
                                array11 = new sbyte[num157];
                                for (int num158 = 0; num158 < num157; num158++)
                                {
                                    array11[num158] = msg.reader().readByte();
                                }
                                image = Image.createImage(array11, 0, num157);
                                BgItem.imgNew.put(num156 + string.Empty, image);
                            }
                            catch (Exception)
                            {
                                array11 = null;
                                BgItem.imgNew.put(num156 + string.Empty, Image.createRGBImage(new int[1], 1, 1, true));
                            }
                            if (array11 != null)
                            {
                                if (mGraphics.zoomLevel > 1)
                                {
                                    Rms.saveRMS(mGraphics.zoomLevel + "bgItem" + num156, array11);
                                }
                                BgItemMn.blendcurrBg(num156, image);
                            }
                            break;
                        }
                    case 92:
                        {
                            if (GameCanvas.currentScreen == GameScr.instance)
                            {
                                GameCanvas.endDlg();
                            }
                            string text4 = msg.reader().readUTF();
                            string str2 = msg.reader().readUTF();
                            str2 = Res.changeString(str2);
                            string empty = string.Empty;
                            Char char8 = null;
                            sbyte b48 = 0;
                            if (!text4.Equals(string.Empty))
                            {
                                char8 = new Char();
                                char8.charID = msg.reader().readInt();
                                char8.head = msg.reader().readShort();
                                char8.headICON = msg.reader().readShort();
                                char8.body = msg.reader().readShort();
                                char8.bag = msg.reader().readShort();
                                char8.leg = msg.reader().readShort();
                                b48 = msg.reader().readByte();
                                char8.cName = text4;
                            }
                            empty += str2;
                            InfoDlg.hide();
                            if (text4.Equals(string.Empty))
                            {
                                GameScr.info1.addInfo(empty, 0);
                                break;
                            }
                            GameScr.info2.addInfoWithChar(empty, char8, b48 == 0);
                            if (GameCanvas.panel.isShow && GameCanvas.panel.type == 8)
                            {
                                GameCanvas.panel.initLogMessage();
                            }
                            break;
                        }
                    case -26:
                        ServerListScreen.testConnect = 2;
                        GameCanvas.debug("SA2", 2);
                        GameCanvas.startOKDlg(msg.reader().readUTF());
                        InfoDlg.hide();
                        LoginScr.isContinueToLogin = false;
                        Char.isLoadingMap = false;
                        if (GameCanvas.currentScreen == GameCanvas.loginScr)
                        {
                            GameCanvas.serverScreen.switchToMe();
                        }
                        break;
                    case -25:
                        GameCanvas.debug("SA3", 2);
                        GameScr.info1.addInfo(msg.reader().readUTF(), 0);
                        break;
                    case 94:
                        GameCanvas.debug("SA3", 2);
                        GameScr.info1.addInfo(msg.reader().readUTF(), 0);
                        break;
                    case 47:
                        GameCanvas.debug("SA4", 2);
                        GameScr.gI().resetButton();
                        break;
                    case 81:
                        {
                            GameCanvas.debug("SXX4", 2);
                            Mob mob8 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            mob8.isDisable = msg.reader().readBool();
                            break;
                        }
                    case 82:
                        {
                            GameCanvas.debug("SXX5", 2);
                            Mob mob8 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            mob8.isDontMove = msg.reader().readBool();
                            break;
                        }
                    case 85:
                        {
                            GameCanvas.debug("SXX5", 2);
                            Mob mob8 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            mob8.isFire = msg.reader().readBool();
                            break;
                        }
                    case 86:
                        {
                            GameCanvas.debug("SXX5", 2);
                            Mob mob8 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            mob8.isIce = msg.reader().readBool();
                            if (!mob8.isIce)
                            {
                                ServerEffect.addServerEffect(77, mob8.x, mob8.y - 9, 1);
                            }
                            break;
                        }
                    case 87:
                        {
                            GameCanvas.debug("SXX5", 2);
                            Mob mob8 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            mob8.isWind = msg.reader().readBool();
                            break;
                        }
                    case 56:
                        {
                            GameCanvas.debug("SXX6", 2);
                            @char = null;
                            int num18 = msg.reader().readInt();
                            if (num18 == Char.myCharz().charID)
                            {
                                bool flag4 = false;
                                @char = Char.myCharz();
                                @char.cHP = msg.readHp();
                                int num41 = msg.readInt3Byte();
                                Res.outz("dame hit = " + num41);
                                if (num41 != 0)
                                {
                                    @char.doInjure();
                                }
                                int num42 = 0;
                                try
                                {
                                    flag4 = msg.reader().readBoolean();
                                    sbyte b22 = msg.reader().readByte();
                                    if (b22 != -1)
                                    {
                                        Res.outz("hit eff= " + b22);
                                        EffecMn.addEff(new Effect(b22, @char.cx, @char.cy, 3, 1, -1));
                                    }
                                }
                                catch (Exception)
                                {
                                }
                                num41 += num42;
                                if (Char.myCharz().cTypePk != 4)
                                {
                                    if (num41 == 0)
                                    {
                                        GameScr.startFlyText(mResources.miss, @char.cx, @char.cy - @char.ch, 0, -3, mFont.MISS_ME);
                                    }
                                    else
                                    {
                                        GameScr.startFlyText("-" + num41, @char.cx, @char.cy - @char.ch, 0, -3, flag4 ? mFont.FATAL : mFont.RED);
                                    }
                                }
                                break;
                            }
                            @char = GameScr.findCharInMap(num18);
                            if (@char == null)
                            {
                                return;
                            }
                            @char.cHP = msg.readHp();
                            bool flag5 = false;
                            int num43 = msg.readInt3Byte();
                            if (num43 != 0)
                            {
                                @char.doInjure();
                            }
                            int num44 = 0;
                            try
                            {
                                flag5 = msg.reader().readBoolean();
                                sbyte b23 = msg.reader().readByte();
                                if (b23 != -1)
                                {
                                    Res.outz("hit eff= " + b23);
                                    EffecMn.addEff(new Effect(b23, @char.cx, @char.cy, 3, 1, -1));
                                }
                            }
                            catch (Exception)
                            {
                            }
                            num43 += num44;
                            if (@char.cTypePk != 4)
                            {
                                if (num43 == 0)
                                {
                                    GameScr.startFlyText(mResources.miss, @char.cx, @char.cy - @char.ch, 0, -3, mFont.MISS);
                                }
                                else
                                {
                                    GameScr.startFlyText("-" + num43, @char.cx, @char.cy - @char.ch, 0, -3, flag5 ? mFont.FATAL : mFont.ORANGE);
                                }
                            }
                            break;
                        }
                    case 83:
                        {
                            GameCanvas.debug("SXX8", 2);
                            int num18 = msg.reader().readInt();
                            @char = ((num18 != Char.myCharz().charID) ? GameScr.findCharInMap(num18) : Char.myCharz());
                            if (@char == null)
                            {
                                return;
                            }
                            Mob mobToAttack = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            if (@char.mobMe != null)
                            {
                                @char.mobMe.attackOtherMob(mobToAttack);
                            }
                            break;
                        }
                    case 84:
                        {
                            int num18 = msg.reader().readInt();
                            if (num18 == Char.myCharz().charID)
                            {
                                @char = Char.myCharz();
                            }
                            else
                            {
                                @char = GameScr.findCharInMap(num18);
                                if (@char == null)
                                {
                                    return;
                                }
                            }
                            @char.cHP = @char.cHPFull;
                            @char.cMP = @char.cMPFull;
                            @char.cx = msg.reader().readShort();
                            @char.cy = msg.reader().readShort();
                            @char.liveFromDead();
                            break;
                        }
                    case 46:
                        GameCanvas.debug("SA5", 2);
                        Cout.LogWarning("Controler RESET_POINT  " + Char.ischangingMap);
                        Char.isLockKey = false;
                        Char.myCharz().setResetPoint(msg.reader().readShort(), msg.reader().readShort());
                        break;
                    case -29:
                        messageNotLogin(msg);
                        break;
                    case -28:
                        messageNotMap(msg);
                        break;
                    case -30:
                        messageSubCommand(msg);
                        break;
                    case 62:
                        GameCanvas.debug("SZ3", 2);
                        @char = GameScr.findCharInMap(msg.reader().readInt());
                        if (@char != null)
                        {
                            @char.killCharId = Char.myCharz().charID;
                            Char.myCharz().npcFocus = null;
                            Char.myCharz().mobFocus = null;
                            Char.myCharz().itemFocus = null;
                            Char.myCharz().charFocus = @char;
                            Char.isManualFocus = true;
                            GameScr.info1.addInfo(@char.cName + mResources.CUU_SAT, 0);
                        }
                        break;
                    case 63:
                        GameCanvas.debug("SZ4", 2);
                        Char.myCharz().killCharId = msg.reader().readInt();
                        Char.myCharz().npcFocus = null;
                        Char.myCharz().mobFocus = null;
                        Char.myCharz().itemFocus = null;
                        Char.myCharz().charFocus = GameScr.findCharInMap(Char.myCharz().killCharId);
                        Char.isManualFocus = true;
                        break;
                    case 64:
                        GameCanvas.debug("SZ5", 2);
                        @char = Char.myCharz();
                        try
                        {
                            @char = GameScr.findCharInMap(msg.reader().readInt());
                        }
                        catch (Exception ex2)
                        {
                            Cout.println("Loi CLEAR_CUU_SAT " + ex2.ToString());
                        }
                        @char.killCharId = -9999;
                        break;
                    case 39:
                        GameCanvas.debug("SA49", 2);
                        GameScr.gI().typeTradeOrder = 2;
                        if (GameScr.gI().typeTrade >= 2 && GameScr.gI().typeTradeOrder >= 2)
                        {
                            InfoDlg.showWait();
                        }
                        break;
                    case 57:
                        {
                            GameCanvas.debug("SZ6", 2);
                            MyVector myVector2 = new MyVector();
                            myVector2.addElement(new Command(msg.reader().readUTF(), GameCanvas.instance, 88817, null));
                            GameCanvas.menu.startAt(myVector2, 3);
                            break;
                        }
                    case 58:
                        {
                            GameCanvas.debug("SZ7", 2);
                            int num18 = msg.reader().readInt();
                            Char char11 = ((num18 != Char.myCharz().charID) ? GameScr.findCharInMap(num18) : Char.myCharz());
                            char11.moveFast = new short[3];
                            char11.moveFast[0] = 0;
                            short num173 = msg.reader().readShort();
                            short num174 = msg.reader().readShort();
                            char11.moveFast[1] = num173;
                            char11.moveFast[2] = num174;
                            try
                            {
                                num18 = msg.reader().readInt();
                                Char char12 = ((num18 != Char.myCharz().charID) ? GameScr.findCharInMap(num18) : Char.myCharz());
                                char12.cx = num173;
                                char12.cy = num174;
                            }
                            catch (Exception ex26)
                            {
                                Cout.println("Loi MOVE_FAST " + ex26.ToString());
                            }
                            break;
                        }
                    case 88:
                        {
                            string info4 = msg.reader().readUTF();
                            short num172 = msg.reader().readShort();
                            // Hop nhap chung: go duoc tieng Viet, va cung mot dang
                            // nhin voi cac hop khac cua ban mod.
                            short menuIdTx = num172;
                            // Dong bang cua game truoc khi mo hop — cung ly do nhu nhanh -125
                            // ben Controller2: chot an toan cua ClientManager dong HopNhapChu
                            // sau mot giay neu bang menu con dang mo phia sau.
                            if (GameCanvas.panel != null && GameCanvas.panel.isShow)
                            {
                                GameCanvas.panel.hide();
                            }
                            God.HopNhapChu.getInstance().moRa(info4, string.Empty, 100,
                                    chu => Service.gI().textBoxId(menuIdTx, chu));
                            break;
                        }
                    case 27:
                        {
                            myVector = new MyVector();
                            string text7 = msg.reader().readUTF();
                            int num169 = msg.reader().readByte();
                            for (int num170 = 0; num170 < num169; num170++)
                            {
                                string caption4 = msg.reader().readUTF();
                                short num171 = msg.reader().readShort();
                                myVector.addElement(new Command(caption4, GameCanvas.instance, 88819, num171));
                            }
                            GameCanvas.menu.startWithoutCloseButton(myVector, 3);
                            break;
                        }
                    case 33:
                        {
                            GameCanvas.debug("SA51", 2);
                            InfoDlg.hide();
                            GameCanvas.clearKeyHold();
                            GameCanvas.clearKeyPressed();
                            myVector = new MyVector();
                            try
                            {
                                while (true)
                                {
                                    string caption3 = msg.reader().readUTF();
                                    myVector.addElement(new Command(caption3, GameCanvas.instance, 88822, null));
                                }
                            }
                            catch (Exception ex24)
                            {
                                Cout.println("Loi OPEN_UI_MENU " + ex24.ToString());
                            }
                            if (Char.myCharz().npcFocus == null)
                            {
                                return;
                            }
                            for (int num165 = 0; num165 < Char.myCharz().npcFocus.template.menu.Length; num165++)
                            {
                                string[] array16 = Char.myCharz().npcFocus.template.menu[num165];
                                myVector.addElement(new Command(array16[0], GameCanvas.instance, 88820, array16));
                            }
                            GameCanvas.menu.startAt(myVector, 3);
                            break;
                        }
                    case 40:
                        {
                            GameCanvas.debug("SA52", 2);
                            GameCanvas.taskTick = 150;
                            short taskId = msg.reader().readShort();
                            sbyte index3 = msg.reader().readByte();
                            string str3 = msg.reader().readUTF();
                            str3 = Res.changeString(str3);
                            string str4 = msg.reader().readUTF();
                            str4 = Res.changeString(str4);
                            string[] array12 = new string[msg.reader().readByte()];
                            string[] array13 = new string[array12.Length];
                            GameScr.tasks = new int[array12.Length];
                            GameScr.mapTasks = new int[array12.Length];
                            short[] array14 = new short[array12.Length];
                            short count = -1;
                            for (int num159 = 0; num159 < array12.Length; num159++)
                            {
                                string str5 = msg.reader().readUTF();
                                str5 = Res.changeString(str5);
                                GameScr.tasks[num159] = msg.reader().readByte();
                                GameScr.mapTasks[num159] = msg.reader().readShort();
                                string str6 = msg.reader().readUTF();
                                str6 = Res.changeString(str6);
                                array14[num159] = -1;
                                array12[num159] = str5;
                                if (!str6.Equals(string.Empty))
                                {
                                    array13[num159] = str6;
                                }
                            }
                            try
                            {
                                count = msg.reader().readShort();
                                for (int num160 = 0; num160 < array12.Length; num160++)
                                {
                                    array14[num160] = msg.reader().readShort();
                                }
                            }
                            catch (Exception ex23)
                            {
                                Cout.println("Loi TASK_GET " + ex23.ToString());
                            }
                            Char.myCharz().taskMaint = new Task(taskId, index3, str3, str4, array12, array14, count, array13);
                            if (Char.myCharz().npcFocus != null)
                            {
                                Npc.clearEffTask();
                            }
                            Char.taskAction(true);
                            break;
                        }
                    case 41:
                        GameCanvas.debug("SA53", 2);
                        GameCanvas.taskTick = 100;
                        Res.outz("TASK NEXT");
                        Char.myCharz().taskMaint.index++;
                        Char.myCharz().taskMaint.count = 0;
                        Npc.clearEffTask();
                        Char.taskAction(true);
                        break;
                    case 50:
                        {
                            sbyte b64 = msg.reader().readByte();
                            Panel.vGameInfo.removeAllElements();
                            for (int num155 = 0; num155 < b64; num155++)
                            {
                                GameInfo gameInfo = new GameInfo();
                                gameInfo.id = msg.reader().readShort();
                                gameInfo.main = msg.reader().readUTF();
                                gameInfo.content = msg.reader().readUTF();
                                Panel.vGameInfo.addElement(gameInfo);
                                bool hasRead = ((Rms.loadRMSInt(gameInfo.id + string.Empty) != -1) ? true : false);
                                gameInfo.hasRead = hasRead;
                            }
                            break;
                        }
                    case 43:
                        GameCanvas.taskTick = 50;
                        GameCanvas.debug("SA55", 2);
                        Char.myCharz().taskMaint.count = msg.reader().readShort();
                        if (Char.myCharz().npcFocus != null)
                        {
                            Npc.clearEffTask();
                        }
                        try
                        {
                            short x_hint = msg.reader().readShort();
                            short y_hint = msg.reader().readShort();
                            Char.myCharz().x_hint = x_hint;
                            Char.myCharz().y_hint = y_hint;
                        }
                        catch (Exception)
                        {
                        }
                        break;
                    case 90:
                        GameCanvas.debug("SA577", 2);
                        requestItemPlayer(msg);
                        break;
                    case 29:
                        GameCanvas.debug("SA58", 2);
                        GameScr.gI().openUIZone(msg);
                        break;
                    case -21:
                        {
                            GameCanvas.debug("SA60", 2);
                            short itemMapID = msg.reader().readShort();
                            for (int num141 = 0; num141 < GameScr.vItemMap.size(); num141++)
                            {
                                if (((ItemMap)GameScr.vItemMap.elementAt(num141)).itemMapID == itemMapID)
                                {
                                    GameScr.vItemMap.removeElementAt(num141);
                                    break;
                                }
                            }
                            break;
                        }
                    case -20:
                        {
                            GameCanvas.debug("SA61", 2);
                            Char.myCharz().itemFocus = null;
                            short itemMapID = msg.reader().readShort();
                            for (int num134 = 0; num134 < GameScr.vItemMap.size(); num134++)
                            {
                                ItemMap itemMap4 = (ItemMap)GameScr.vItemMap.elementAt(num134);
                                if (itemMap4.itemMapID != itemMapID)
                                {
                                    continue;
                                }
                                itemMap4.setPoint(Char.myCharz().cx, Char.myCharz().cy - 10);
                                string text5 = msg.reader().readUTF();
                                num = 0;
                                try
                                {
                                    num = msg.reader().readShort();
                                    if (itemMap4.template.type == 9)
                                    {
                                        num = msg.reader().readShort();
                                        Char.myCharz().xu += num;
                                        Char.myCharz().xuStr = Res.formatNumber(Char.myCharz().xu);
                                    }
                                    else if (itemMap4.template.type == 10)
                                    {
                                        num = msg.reader().readShort();
                                        Char.myCharz().luong += num;
                                        Char.myCharz().luongStr = mSystem.numberTostring(Char.myCharz().luong);
                                    }
                                    else if (itemMap4.template.type == 34)
                                    {
                                        num = msg.reader().readShort();
                                        Char.myCharz().luongKhoa += num;
                                        Char.myCharz().luongKhoaStr = mSystem.numberTostring(Char.myCharz().luongKhoa);
                                    }
                                }
                                catch (Exception)
                                {
                                }
                                if (text5.Equals(string.Empty))
                                {
                                    if (itemMap4.template.type == 9)
                                    {
                                        GameScr.startFlyText(((num >= 0) ? "+" : string.Empty) + num, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch, 0, -2, mFont.YELLOW);
                                        SoundMn.gI().getItem();
                                    }
                                    else if (itemMap4.template.type == 10)
                                    {
                                        GameScr.startFlyText(((num >= 0) ? "+" : string.Empty) + num, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch, 0, -2, mFont.GREEN);
                                        SoundMn.gI().getItem();
                                    }
                                    else if (itemMap4.template.type == 34)
                                    {
                                        GameScr.startFlyText(((num >= 0) ? "+" : string.Empty) + num, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch, 0, -2, mFont.RED);
                                        SoundMn.gI().getItem();
                                    }
                                    else
                                    {
                                        GameScr.info1.addInfo(mResources.you_receive + " " + ((num <= 0) ? string.Empty : (num + " ")) + itemMap4.template.name, 0);
                                        SoundMn.gI().getItem();
                                    }
                                    if (num > 0 && Char.myCharz().petFollow != null && Char.myCharz().petFollow.smallID == 4683)
                                    {
                                        ServerEffect.addServerEffect(55, Char.myCharz().petFollow.cmx, Char.myCharz().petFollow.cmy, 1);
                                        ServerEffect.addServerEffect(55, Char.myCharz().cx, Char.myCharz().cy, 1);
                                    }
                                }
                                else if (text5.Length == 1)
                                {
                                    Cout.LogError3("strInf.Length =1:  " + text5);
                                }
                                else
                                {
                                    GameScr.info1.addInfo(text5, 0);
                                }
                                break;
                            }
                            break;
                        }
                    case -19:
                        {
                            GameCanvas.debug("SA62", 2);
                            short itemMapID = msg.reader().readShort();
                            @char = GameScr.findCharInMap(msg.reader().readInt());
                            for (int num133 = 0; num133 < GameScr.vItemMap.size(); num133++)
                            {
                                ItemMap itemMap3 = (ItemMap)GameScr.vItemMap.elementAt(num133);
                                if (itemMap3.itemMapID != itemMapID)
                                {
                                    continue;
                                }
                                if (@char == null)
                                {
                                    return;
                                }
                                itemMap3.setPoint(@char.cx, @char.cy - 10);
                                if (itemMap3.x < @char.cx)
                                {
                                    @char.cdir = -1;
                                }
                                else if (itemMap3.x > @char.cx)
                                {
                                    @char.cdir = 1;
                                }
                                break;
                            }
                            break;
                        }
                    case -18:
                        {
                            GameCanvas.debug("SA63", 2);
                            int num132 = msg.reader().readByte();
                            GameScr.vItemMap.addElement(new ItemMap(msg.reader().readShort(), Char.myCharz().arrItemBag[num132].template.id, Char.myCharz().cx, Char.myCharz().cy, msg.reader().readShort(), msg.reader().readShort()));
                            Char.myCharz().arrItemBag[num132] = null;
                            break;
                        }
                    case 68:
                        {
                            Res.outz("ADD ITEM TO MAP --------------------------------------");
                            GameCanvas.debug("SA6333", 2);
                            short itemMapID = msg.reader().readShort();
                            short itemTemplateID = msg.reader().readShort();
                            int x = msg.reader().readShort();
                            int y = msg.reader().readShort();
                            int num125 = msg.reader().readInt();
                            short r = 0;
                            if (num125 == -2)
                            {
                                r = msg.reader().readShort();
                            }
                            ItemMap itemMap = new ItemMap(num125, itemMapID, itemTemplateID, x, y, r);
                            bool flag10 = false;
                            for (int num126 = 0; num126 < GameScr.vItemMap.size(); num126++)
                            {
                                ItemMap itemMap2 = (ItemMap)GameScr.vItemMap.elementAt(num126);
                                if (itemMap2.itemMapID == itemMap.itemMapID)
                                {
                                    flag10 = true;
                                    break;
                                }
                            }
                            if (!flag10)
                            {
                                GameScr.vItemMap.addElement(itemMap);
                            }
                            break;
                        }
                    case 69:
                        SoundMn.IsDelAcc = ((msg.reader().readByte() != 0) ? true : false);
                        break;
                    case -14:
                        GameCanvas.debug("SA64", 2);
                        @char = GameScr.findCharInMap(msg.reader().readInt());
                        if (@char == null)
                        {
                            return;
                        }
                        GameScr.vItemMap.addElement(new ItemMap(msg.reader().readShort(), msg.reader().readShort(), @char.cx, @char.cy, msg.reader().readShort(), msg.reader().readShort()));
                        break;
                    case -22:
                        GameCanvas.debug("SA65", 2);
                        Char.isLockKey = true;
                        Char.ischangingMap = true;
                        GameScr.gI().timeStartMap = 0;
                        GameScr.gI().timeLengthMap = 0;
                        Char.myCharz().mobFocus = null;
                        Char.myCharz().npcFocus = null;
                        Char.myCharz().charFocus = null;
                        Char.myCharz().itemFocus = null;
                        Char.myCharz().focus.removeAllElements();
                        Char.myCharz().testCharId = -9999;
                        Char.myCharz().killCharId = -9999;
                        GameCanvas.resetBg();
                        GameScr.gI().resetButton();
                        GameScr.gI().center = null;
                        if (Effect.vEffData.size() > 15)
                        {
                            for (int num124 = 0; num124 < 5; num124++)
                            {
                                Effect.vEffData.removeElementAt(0);
                            }
                        }
                        break;
                    // [-71] Chi cap nhat hao quang, khong dong toi ngoai hinh.
                    // May chu gui goi nay moi khi hao quang doi (mac/coi cai
                    // trang, doi the, suc manh vuot moc). Truoc day phai gui ca
                    // goi thong tin nhan vat, ma goi do cho theo mang do dang
                    // mac nen ghi de luon hinh dang.
                    case -109:
                        {
                            // Goi tieng tu nguoi khac. Doc het roi moi bo neu tat
                            // loa: bo doc giua chung la lech ca luong byte sau do.
                            sbyte kenhTieng = msg.reader().readByte();
                            int idNoi = msg.reader().readInt();
                            string tenNoi = msg.reader().readUTF();
                            int soByteTieng = msg.reader().readUnsignedShort();
                            sbyte[] tieng = new sbyte[soByteTieng];
                            msg.reader().readFully(ref tieng);
                            VoiceChat.gI().nhan(kenhTieng, idNoi, tenNoi, tieng, soByteTieng);
                        }
                        break;

                    case -72:
                        {
                            // Mở đồng hồ đếm lùi lượt đấm máy đo sức mạnh. Máy chủ
                            // chỉ gửi số giây một lần, client tự đếm từ đó.
                            short giayDam = msg.reader().readShort();
                            Game2.God.ClientManager.ketThucDemNguocMayDam =
                                    (giayDam <= 0) ? 0L
                                    : mSystem.currentTimeMillis() + giayDam * 1000L;
                        }
                        break;
                    // [117] Khu tro choi nho: Bau Cua, Xoc Dia, Dua Ngua, Dao
                    // Vang, Cao Thap, Cau Ca. Mot ma goi cho ca sau tro, byte
                    // dau la ma tro — xem God/TroChoi/MiniGame.cs.
                    case 117:
                        God.MiniGame.gI().nhanGoi(msg);
                        break;
                    case 118:
                        {
                            // Tai Xiu. Byte dau la loai viec:
                            //   0 trang thai day du, 1 nhip moi giay, 2 ket qua van.
                            //
                            // Doc HET cum roi moi dung du lieu: bo giua chung la
                            // lech ca luong byte cua nhung goi toi sau.
                            int viecTx = msg.reader().readUnsignedByte();
                            if (viecTx == 0)
                            {
                                sbyte gdTx = msg.reader().readByte();
                                int giayTx = msg.reader().readInt();
                                long taiTx = msg.reader().readLong();
                                long xiuTx = msg.reader().readLong();
                                sbyte cuaTx = msg.reader().readByte();
                                long thoiTx = msg.reader().readLong();
                                long khoaTx = msg.reader().readLong();
                                long thuongTx = msg.reader().readLong();
                                int x1Tx = msg.reader().readUnsignedByte();
                                int x2Tx = msg.reader().readUnsignedByte();
                                int x3Tx = msg.reader().readUnsignedByte();
                                // Cua thang do MAY CHU tinh. Client khong tu so
                                // nguong — luat choi chi viet o mot cho.
                                sbyte kqTx = msg.reader().readByte();
                                int soLsTx = msg.reader().readUnsignedByte();
                                var lsTx = new System.Collections.Generic.List<sbyte>();
                                for (int iTx = 0; iTx < soLsTx; iTx++)
                                {
                                    lsTx.Add(msg.reader().readByte());
                                }
                                long phienTx = msg.reader().readLong();
                                God.TroChoiUI.getInstance().nhanTrangThai(gdTx, giayTx,
                                        taiTx, xiuTx, cuaTx, thoiTx, khoaTx, thuongTx,
                                        x1Tx, x2Tx, x3Tx, lsTx, phienTx, kqTx);
                            }
                            else if (viecTx == 1)
                            {
                                sbyte gdTx = msg.reader().readByte();
                                int giayTx = msg.reader().readInt();
                                long taiTx = msg.reader().readLong();
                                long xiuTx = msg.reader().readLong();
                                God.TroChoiUI.getInstance().nhanNhip(gdTx, giayTx,
                                        taiTx, xiuTx);
                            }
                            else if (viecTx == 2)
                            {
                                int x1Tx = msg.reader().readUnsignedByte();
                                int x2Tx = msg.reader().readUnsignedByte();
                                int x3Tx = msg.reader().readUnsignedByte();
                                sbyte kqTx = msg.reader().readByte();
                                long thangTx = msg.reader().readLong();
                                long khoaTx = msg.reader().readLong();
                                long thuongTx = msg.reader().readLong();
                                God.TroChoiUI.getInstance().nhanKetQua(x1Tx, x2Tx, x3Tx,
                                        kqTx, thangTx, khoaTx, thuongTx);
                            }
                            else if (viecTx == 3)
                            {
                                var dsTx = new System.Collections.Generic.List<God.TroChoiUI.DongLsToi>();
                                int nTx = msg.reader().readUnsignedByte();
                                for (int iTx = 0; iTx < nTx; iTx++)
                                {
                                    var d = new God.TroChoiUI.DongLsToi();
                                    d.phien = msg.reader().readLong();
                                    d.cua = msg.reader().readByte();
                                    d.soThoi = msg.reader().readLong();
                                    d.ketQua = msg.reader().readByte();
                                    d.thang = msg.reader().readByte() == 1;
                                    d.tienThang = msg.reader().readLong();
                                    dsTx.Add(d);
                                }
                                God.TroChoiUI.getInstance().nhanLsToi(dsTx);
                            }
                            else
                            {
                                var dsTx = new System.Collections.Generic.List<God.TroChoiUI.DongLsServer>();
                                int nTx = msg.reader().readUnsignedByte();
                                for (int iTx = 0; iTx < nTx; iTx++)
                                {
                                    var d = new God.TroChoiUI.DongLsServer();
                                    d.phien = msg.reader().readLong();
                                    d.x1 = msg.reader().readUnsignedByte();
                                    d.x2 = msg.reader().readUnsignedByte();
                                    d.x3 = msg.reader().readUnsignedByte();
                                    d.ketQua = msg.reader().readByte();
                                    d.tongTai = msg.reader().readLong();
                                    d.tongXiu = msg.reader().readLong();
                                    d.soNguoi = msg.reader().readInt();
                                    dsTx.Add(d);
                                }
                                God.TroChoiUI.getInstance().nhanLsServer(dsTx);
                            }
                        }
                        break;
                    case 120:
                        {
                            // Cay nhom - diem cua man ban do nhanh.
                            int viecBd = msg.reader().readUnsignedByte();
                            if (viecBd == 0)
                            {
                                var dsNhom = new System.Collections.Generic.List<God.TuiUI.NhomMap>();
                                int soNhom = msg.reader().readUnsignedByte();
                                for (int iN = 0; iN < soNhom; iN++)
                                {
                                    var nhom = new God.TuiUI.NhomMap();
                                    nhom.ten = msg.reader().readUTF();
                                    int soDiem = msg.reader().readUnsignedByte();
                                    for (int iD = 0; iD < soDiem; iD++)
                                    {
                                        var diem = new God.TuiUI.DiemMap();
                                        diem.id = msg.reader().readInt();
                                        diem.ten = msg.reader().readUTF();
                                        diem.mapId = msg.reader().readShort();
                                        nhom.diem.Add(diem);
                                    }
                                    dsNhom.Add(nhom);
                                }
                                God.TuiUI.getInstance().nhanBanDo(dsNhom);
                            }
                        }
                        break;

                    case 119:
                        {
                            // Hai loai goi: 0 danh sach, 1 chi tiet mot con.
                            //
                            // Ma goi la 119, KHONG phai -110. Ma -110 da bi
                            // Controller2.readMessage dung cho giao thuc RMS, va
                            // ham do chay TRUOC switch nay tren cung mot reader —
                            // no doc mat byte dau roi hut ca phan con lai, nen man
                            // hinh boss dung mai o "Dang tai" ma khong bao loi.
                            // Them man moi thi phai soat ca hai switch client va
                            // switch may chu.
                            int viecBoss = msg.reader().readUnsignedByte();
                            if (viecBoss == 0)
                            {
                                var dsBoss = new System.Collections.Generic.List<God.BossUI.Dong>();
                                int soBoss = msg.reader().readUnsignedByte();
                                for (int iB = 0; iB < soBoss; iB++)
                                {
                                    var d = new God.BossUI.Dong();
                                    d.id = msg.reader().readInt();
                                    // Khoa dinh danh cua ban boss nay.
                                    d.khoa = msg.reader().readInt();
                                    d.ten = msg.reader().readUTF();
                                    d.tenMap = msg.reader().readUTF();
                                    d.trangThai = msg.reader().readUTF();
                                    d.choHoiSinh = msg.reader().readUTF();
                                    d.mau = msg.reader().readUnsignedByte();
                                    d.soPhase = msg.reader().readUnsignedByte();
                                    dsBoss.Add(d);
                                }
                                God.BossUI.getInstance().nhanDuLieu(dsBoss);
                            }
                            else
                            {
                                int idBoss = msg.reader().readInt();
                                int khoaBoss = msg.reader().readInt();
                                var dsPhase = new System.Collections.Generic.List<God.BossUI.Phase>();
                                int soPhase = msg.reader().readUnsignedByte();
                                for (int iP = 0; iP < soPhase; iP++)
                                {
                                    var ph = new God.BossUI.Phase();
                                    ph.ten = msg.reader().readUTF();
                                    ph.hpChu = msg.reader().readUTF();
                                    ph.phanTramHp = msg.reader().readUnsignedByte();
                                    // Ba bo phan than nguoi thay cho mot ma anh.
                                    // Xem BossUI.veThanBoss.
                                    ph.head = msg.reader().readShort();
                                    ph.body = msg.reader().readShort();
                                    ph.leg = msg.reader().readShort();
                                    ph.gender = msg.reader().readUnsignedByte();
                                    int soRoi = msg.reader().readUnsignedByte();
                                    for (int iR = 0; iR < soRoi; iR++)
                                    {
                                        var r = new God.BossUI.Roi();
                                        r.icon = msg.reader().readShort();
                                        r.ten = msg.reader().readUTF();
                                        r.soLuong = msg.reader().readUTF();
                                        r.tiLe = msg.reader().readUTF();
                                        r.hanDung = msg.reader().readUTF();
                                        ph.roi.Add(r);
                                    }
                                    dsPhase.Add(ph);
                                }
                                God.BossUI.getInstance().nhanChiTiet(idBoss, khoaBoss, dsPhase);
                            }
                        }
                        break;

                    case -48:
                        {
                            // Du lieu man hinh Su kien. Doc het ca cum roi moi mo.
                            var dsSK = new System.Collections.Generic.List<God.SuKienUI.SuKien>();
                            int soSK = msg.reader().readUnsignedByte();
                            for (int iS = 0; iS < soSK; iS++)
                            {
                                var sk = new God.SuKienUI.SuKien();
                                sk.ten = msg.reader().readUTF();
                                sk.khungGio = msg.reader().readUTF();
                                sk.thongBao = msg.reader().readUTF();
                                int soRoi = msg.reader().readUnsignedByte();
                                for (int iR = 0; iR < soRoi; iR++)
                                {
                                    var d = new God.SuKienUI.Dong();
                                    var v = new God.SuKienUI.VatPham();
                                    d.icon = msg.reader().readShort();
                                    d.ten = msg.reader().readUTF();
                                    v.icon = d.icon;
                                    v.ten = d.ten;
                                    v.soLuong = msg.reader().readInt();
                                    v.soLuongMax = msg.reader().readInt();
                                    v.tiLe = msg.reader().readUTF();
                                    d.moTa = "Rơi " + v.soLuongChu()
                                            + " · tỉ lệ " + v.tiLe;
                                    d.qua.Add(v);
                                    docTrongGoi(msg, d);
                                    sk.doRoi.Add(d);
                                }
                                int soCT = msg.reader().readUnsignedByte();
                                for (int iC = 0; iC < soCT; iC++)
                                {
                                    var d = new God.SuKienUI.Dong();
                                    d.laCongThuc = true;
                                    string tenCT = msg.reader().readUTF();
                                    var qua = new God.SuKienUI.VatPham();
                                    qua.icon = msg.reader().readShort();
                                    qua.ten = msg.reader().readUTF();
                                    qua.soLuong = msg.reader().readInt();
                                    qua.soLuongMax = qua.soLuong;
                                    d.icon = qua.icon;
                                    d.ten = (tenCT.Length > 0 ? tenCT
                                            : qua.ten + " " + qua.soLuongChu());
                                    d.qua.Add(qua);
                                    docTrongGoi(msg, d);
                                    int soNL = msg.reader().readUnsignedByte();
                                    for (int iL = 0; iL < soNL; iL++)
                                    {
                                        var nl = new God.SuKienUI.VatPham();
                                        nl.icon = msg.reader().readShort();
                                        nl.ten = msg.reader().readUTF();
                                        nl.soLuong = msg.reader().readInt();
                                        nl.soLuongMax = nl.soLuong;
                                        d.yeuCau.Add(nl);
                                    }
                                    d.moTa = "Cần " + soNL + " món · nhận "
                                            + qua.ten + " " + qua.soLuongChu();
                                    sk.congThuc.Add(d);
                                }
                                dsSK.Add(sk);
                            }
                            God.SuKienUI.getInstance().nhanDuLieu(dsSK);
                        }
                        break;
                    case -58:

                        {
                            // Du lieu phuc loi: doc CA cum roi moi mo man hinh.
                            // Doc do dang ma bo giua chung la lech ca luong byte
                            // cua nhung goi sau.
                            var dsNhomPL = new System.Collections.Generic.List<God.PhucLoiUI.Nhom>();
                            int soNhomPL = msg.reader().readUnsignedByte();
                            for (int iN = 0; iN < soNhomPL; iN++)
                            {
                                var nPL = new God.PhucLoiUI.Nhom();
                                nPL.ten = msg.reader().readUTF();
                                nPL.tenLoai = msg.reader().readUTF();
                                nPL.tienDo = msg.reader().readUTF();
                                int soMocPL = msg.reader().readUnsignedByte();
                                for (int iM = 0; iM < soMocPL; iM++)
                                {
                                    var mPL = new God.PhucLoiUI.Moc();
                                    mPL.id = msg.reader().readInt();
                                    mPL.nhan = msg.reader().readUTF();
                                    mPL.trangThai = msg.reader().readByte();
                                    mPL.moTaThieu = msg.reader().readUTF();
                                    mPL.phanTram = msg.reader().readUnsignedByte();
                                    int soQuaPL = msg.reader().readUnsignedByte();
                                    for (int iQ = 0; iQ < soQuaPL; iQ++)
                                    {
                                        var qPL = new God.PhucLoiUI.Qua();
                                        qPL.icon = msg.reader().readShort();
                                        qPL.soLuong = msg.reader().readInt();
                                        mPL.qua.Add(qPL);
                                    }
                                    nPL.moc.Add(mPL);
                                }
                                dsNhomPL.Add(nPL);
                            }
                            God.PhucLoiUI.getInstance().nhanDuLieu(dsNhomPL);
                        }
                        break;
                    case 127:
                        // So suu tam (the ra-da): may chu RadarService.
                        God.SoSuuTamUI.getInstance().nhanGoi(msg);
                        break;
                    case -71:

                        {
                            short auraMoi = msg.reader().readShort();
                            if (Char.myCharz() != null)
                            {
                                Char.myCharz().idAuraEff = auraMoi;
                            }
                        }
                        break;
                    case -70:
                        {
                            Res.outz("BIG MESSAGE .......................................");
                            GameCanvas.endDlg();
                            int avatar2 = msg.reader().readShort();
                            string chat3 = msg.reader().readUTF();
                            Npc npc6 = new Npc(-1, 0, 0, 0, 0, 0);
                            npc6.avatar = avatar2;
                            ChatPopup.addBigMessage(chat3, 100000, npc6);
                            sbyte b47 = msg.reader().readByte();
                            if (b47 == 0)
                            {
                                ChatPopup.serverChatPopUp.cmdMsg1 = new Command(mResources.CLOSE, ChatPopup.serverChatPopUp, 1001, null);
                                ChatPopup.serverChatPopUp.cmdMsg1.x = GameCanvas.w / 2 - 35;
                                ChatPopup.serverChatPopUp.cmdMsg1.y = GameCanvas.h - 35;
                            }
                            if (b47 == 1)
                            {
                                string p2 = msg.reader().readUTF();
                                string caption2 = msg.reader().readUTF();
                                ChatPopup.serverChatPopUp.cmdMsg1 = new Command(caption2, ChatPopup.serverChatPopUp, 1000, p2);
                                ChatPopup.serverChatPopUp.cmdMsg1.x = GameCanvas.w / 2 - 75;
                                ChatPopup.serverChatPopUp.cmdMsg1.y = GameCanvas.h - 35;
                                ChatPopup.serverChatPopUp.cmdMsg2 = new Command(mResources.CLOSE, ChatPopup.serverChatPopUp, 1001, null);
                                ChatPopup.serverChatPopUp.cmdMsg2.x = GameCanvas.w / 2 + 11;
                                ChatPopup.serverChatPopUp.cmdMsg2.y = GameCanvas.h - 35;
                            }
                            break;
                        }
                    case 38:
                        {
                            GameCanvas.debug("SA67", 2);
                            InfoDlg.hide();
                            int num87 = msg.reader().readShort();
                            Res.outz("OPEN_UI_SAY ID= " + num87);
                            string str = msg.reader().readUTF();
                            str = Res.changeString(str);
                            for (int num121 = 0; num121 < GameScr.vNpc.size(); num121++)
                            {
                                Npc npc4 = (Npc)GameScr.vNpc.elementAt(num121);
                                Res.outz("npc id= " + npc4.template.npcTemplateId);
                                if (npc4.template.npcTemplateId == num87)
                                {
                                    ChatPopup.addChatPopupMultiLine(str, 100000, npc4);
                                    GameCanvas.panel.hideNow();
                                    return;
                                }
                            }
                            Npc npc5 = new Npc(num87, 0, 0, 0, num87, GameScr.info1.charId[Char.myCharz().cgender][2]);
                            if (npc5.template.npcTemplateId == 5)
                            {
                                npc5.charID = 5;
                            }
                            try
                            {
                                npc5.avatar = msg.reader().readShort();
                            }
                            catch (Exception)
                            {
                            }
                            ChatPopup.addChatPopupMultiLine(str, 100000, npc5);
                            GameCanvas.panel.hideNow();
                            break;
                        }
                    case 32:
                        {
                            GameCanvas.debug("SA68", 2);
                            int num87 = msg.reader().readShort();
                            for (int num88 = 0; num88 < GameScr.vNpc.size(); num88++)
                            {
                                Npc npc = (Npc)GameScr.vNpc.elementAt(num88);
                                if (npc.template.npcTemplateId == num87 && npc.Equals(Char.myCharz().npcFocus))
                                {
                                    string chat = msg.reader().readUTF();
                                    string[] array7 = new string[msg.reader().readByte()];
                                    for (int num89 = 0; num89 < array7.Length; num89++)
                                    {
                                        array7[num89] = msg.reader().readUTF();
                                    }
                                    GameScr.gI().createMenu(array7, npc);
                                    ChatPopup.addChatPopup(chat, 100000, npc);
                                    return;
                                }
                            }
                            Npc npc2 = new Npc(num87, 0, -100, 100, num87, GameScr.info1.charId[Char.myCharz().cgender][2]);
                            Res.outz((Char.myCharz().npcFocus == null) ? "null" : "!null");
                            string chat2 = msg.reader().readUTF();
                            string[] array8 = new string[msg.reader().readByte()];
                            for (int num90 = 0; num90 < array8.Length; num90++)
                            {
                                array8[num90] = msg.reader().readUTF();
                            }
                            try
                            {
                                short avatar = msg.reader().readShort();
                                npc2.avatar = avatar;
                            }
                            catch (Exception)
                            {
                            }
                            Res.outz((Char.myCharz().npcFocus == null) ? "null" : "!null");
                            GameScr.gI().createMenu(array8, npc2);
                            ChatPopup.addChatPopup(chat2, 100000, npc2);
                            break;
                        }
                    case 7:
                        {
                            sbyte type = msg.reader().readByte();
                            short id2 = msg.reader().readShort();
                            string info2 = msg.reader().readUTF();
                            GameCanvas.panel.saleRequest(type, info2, id2);
                            break;
                        }
                    case 6:
                        GameCanvas.debug("SA70", 2);
                        Char.myCharz().xu = msg.reader().readLong();
                        Char.myCharz().luong = msg.reader().readInt();
                        Char.myCharz().luongKhoa = msg.reader().readInt();
                        Char.myCharz().xuStr = Res.formatNumber(Char.myCharz().xu);
                        Char.myCharz().luongStr = mSystem.numberTostring(Char.myCharz().luong);
                        Char.myCharz().luongKhoaStr = mSystem.numberTostring(Char.myCharz().luongKhoa);
                        GameCanvas.endDlg();
                        break;
                    case -24:
                        Res.outz("***************MAP_INFO**************");
                        GameScr.isPickNgocRong = false;
                        Char.isLoadingMap = true;
                        // Bat dau mot luot tai ban do moi: dat lai han
                        // chot cua man hinh cho, khong thi no van la han
                        // cua luot truoc va da qua tu doi nao.
                        GameCanvas.hanChotTaiBanDo = 0L;
                        GameCanvas.canGuiXongTaiBanDo = false;
                        // Doi khu / ban do: dung mic va loa kenh Khu, Map cua cho cu.
                        VoiceChat.daDoiCho();
                        Cout.println("GET MAP INFO");
                        GameScr.gI().magicTree = null;
                        GameCanvas.isLoading = true;
                        GameCanvas.debug("SA75", 2);
                        GameScr.resetAllvector();
                        GameCanvas.endDlg();
                        TileMap.vGo.removeAllElements();
                        PopUp.vPopups.removeAllElements();
                        mSystem.gcc();
                        // Nho cho dang dung TRUOC khi ghi de, de con biet co
                        // doi bang do hay doi khu khong — xem ChatUI.doiChoDung.
                        int mapCu = TileMap.mapID;
                        int khuCu = TileMap.zoneID;
                        // Goi -24 moi toi khi goi cu con dang cho mau: bo goi cu, va
                        // nho bo luon mau cua no khi toi — ban do moi hon luon thang.
                        if (messWait != null)
                        {
                            messWait = null;
                            mauCanBo++;
                        }
                        mapIdCho = msg.reader().readUnsignedByte();
                        planetIdCho = msg.reader().readByte();
                        tileIdCho = msg.reader().readByte();
                        bgIdCho = msg.reader().readByte();
                        typeMapCho = msg.reader().readByte();
                        mapNameCho = msg.reader().readUTF();
                        zoneIdCho = msg.reader().readByte();
                        GameCanvas.debug("SA75x1", 2);
                        try
                        {
                            TileMap.loadMapFromResource(mapIdCho);
                        }
                        catch (Exception)
                        {
                            // Chua co mau ban do nay trong may. Xin mau, cat goi
                            // lai, va GIU NGUYEN ban do cu tren man hinh — man
                            // hinh cho da che kin roi. Ap dinh danh moi ngay bay
                            // gio la de ten va nen cua ban do moi ngoi len dia
                            // hinh cu, dung canh "map vo" da gap.
                            Service.gI().requestMaptemplate(mapIdCho);
                            messWait = msg;
                            break;
                        }
                        apDungDinhDanhBanDo(mapCu, khuCu);
                        loadInfoMap(msg);

                        try
                        {
                            sbyte b32 = msg.reader().readByte();
                            TileMap.isMapDouble = ((b32 != 0) ? true : false);
                        }
                        catch (Exception)
                        {
                        }
                        GameScr.cmx = GameScr.cmtoX;
                        GameScr.cmy = GameScr.cmtoY;
                        GameCanvas.isRequestMapID = 2;
                        GameCanvas.waitingTimeChangeMap = mSystem.currentTimeMillis() + 150;
                        break;
                    case -31:
                        {
                            TileMap.vItemBg.removeAllElements();
                            short num75 = msg.reader().readShort();
                            Res.err("[ITEM_BACKGROUND] nItem= " + num75);
                            for (int num76 = 0; num76 < num75; num76++)
                            {
                                BgItem bgItem = new BgItem();
                                bgItem.id = num76;
                                bgItem.idImage = msg.reader().readShort();
                                bgItem.layer = msg.reader().readByte();
                                bgItem.dx = msg.reader().readShort();
                                bgItem.dy = msg.reader().readShort();
                                sbyte b31 = msg.reader().readByte();
                                bgItem.tileX = new int[b31];
                                bgItem.tileY = new int[b31];
                                for (int num77 = 0; num77 < b31; num77++)
                                {
                                    bgItem.tileX[num76] = msg.reader().readByte();
                                    bgItem.tileY[num76] = msg.reader().readByte();
                                }
                                TileMap.vItemBg.addElement(bgItem);
                            }
                            break;
                        }
                    case -4:
                        {
                            GameCanvas.debug("SA76", 2);
                            @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char == null)
                            {
                                return;
                            }
                            GameCanvas.debug("SA76v1", 2);
                            if ((TileMap.tileTypeAtPixel(@char.cx, @char.cy) & 2) == 2)
                            {
                                @char.setSkillPaint(GameScr.sks[msg.reader().readUnsignedByte()], 0);
                            }
                            else
                            {
                                @char.setSkillPaint(GameScr.sks[msg.reader().readUnsignedByte()], 1);
                            }
                            GameCanvas.debug("SA76v2", 2);
                            @char.attMobs = new Mob[msg.reader().readByte()];
                            for (int num25 = 0; num25 < @char.attMobs.Length; num25++)
                            {
                                Mob mob3 = (Mob)GameScr.vMob.elementAt(msg.reader().readByte());
                                @char.attMobs[num25] = mob3;
                                if (num25 == 0)
                                {
                                    if (@char.cx <= mob3.x)
                                    {
                                        @char.cdir = 1;
                                    }
                                    else
                                    {
                                        @char.cdir = -1;
                                    }
                                }
                            }
                            GameCanvas.debug("SA76v3", 2);
                            @char.charFocus = null;
                            @char.mobFocus = @char.attMobs[0];
                            Char[] array = new Char[10];
                            num = 0;
                            try
                            {
                                for (num = 0; num < array.Length; num++)
                                {
                                    int num18 = msg.reader().readInt();
                                    Char char5 = (array[num] = ((num18 != Char.myCharz().charID) ? GameScr.findCharInMap(num18) : Char.myCharz()));
                                    if (num == 0)
                                    {
                                        if (@char.cx <= char5.cx)
                                        {
                                            @char.cdir = 1;
                                        }
                                        else
                                        {
                                            @char.cdir = -1;
                                        }
                                    }
                                }
                            }
                            catch (Exception ex6)
                            {
                                Cout.println("Loi PLAYER_ATTACK_N_P " + ex6.ToString());
                            }
                            GameCanvas.debug("SA76v4", 2);
                            if (num > 0)
                            {
                                @char.attChars = new Char[num];
                                for (num = 0; num < @char.attChars.Length; num++)
                                {
                                    @char.attChars[num] = array[num];
                                }
                                @char.charFocus = @char.attChars[0];
                                @char.mobFocus = null;
                            }
                            GameCanvas.debug("SA76v5", 2);
                            break;
                        }
                    case 54:
                        {
                            @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char == null)
                            {
                                return;
                            }
                            int num17 = msg.reader().readUnsignedByte();
                            if ((TileMap.tileTypeAtPixel(@char.cx, @char.cy) & 2) == 2)
                            {
                                @char.setSkillPaint(GameScr.sks[num17], 0);
                            }
                            else
                            {
                                @char.setSkillPaint(GameScr.sks[num17], 1);
                            }
                            Mob[] array3 = new Mob[10];
                            num = 0;
                            try
                            {
                                for (num = 0; num < array3.Length; num++)
                                {
                                    Mob mob2 = (array3[num] = (Mob)GameScr.vMob.elementAt(msg.reader().readByte()));
                                    if (num == 0)
                                    {
                                        if (@char.cx <= mob2.x)
                                        {
                                            @char.cdir = 1;
                                        }
                                        else
                                        {
                                            @char.cdir = -1;
                                        }
                                    }
                                }
                            }
                            catch (Exception)
                            {
                            }
                            if (num > 0)
                            {
                                @char.attMobs = new Mob[num];
                                for (num = 0; num < @char.attMobs.Length; num++)
                                {
                                    @char.attMobs[num] = array3[num];
                                }
                                @char.charFocus = null;
                                @char.mobFocus = @char.attMobs[0];
                            }
                            break;
                        }
                    case -60:
                        {
                            GameCanvas.debug("SA7666", 2);
                            int num2 = msg.reader().readInt();
                            int num3 = -1;
                            if (num2 != Char.myCharz().charID)
                            {
                                Char char2 = GameScr.findCharInMap(num2);
                                if (char2 == null)
                                {
                                    return;
                                }
                                if (char2.currentMovePoint != null)
                                {
                                    char2.createShadow(char2.cx, char2.cy, 10);
                                    char2.cx = char2.currentMovePoint.xEnd;
                                    char2.cy = char2.currentMovePoint.yEnd;
                                }
                                int num4 = msg.reader().readUnsignedByte();
                                if ((TileMap.tileTypeAtPixel(char2.cx, char2.cy) & 2) == 2)
                                {
                                    char2.setSkillPaint(GameScr.sks[num4], 0);
                                }
                                else
                                {
                                    char2.setSkillPaint(GameScr.sks[num4], 1);
                                }
                                sbyte b = msg.reader().readByte();
                                Char[] array = new Char[b];
                                for (num = 0; num < array.Length; num++)
                                {
                                    num3 = msg.reader().readInt();
                                    Char char3;
                                    if (num3 == Char.myCharz().charID)
                                    {
                                        char3 = Char.myCharz();
                                        if (!GameScr.isChangeZone && GameScr.isAutoPlay && GameScr.canAutoPlay)
                                        {
                                            Service.gI().requestChangeZone(-1, -1);
                                            GameScr.isChangeZone = true;
                                        }
                                    }
                                    else
                                    {
                                        char3 = GameScr.findCharInMap(num3);
                                    }
                                    array[num] = char3;
                                    if (num == 0)
                                    {
                                        if (char2.cx <= char3.cx)
                                        {
                                            char2.cdir = 1;
                                        }
                                        else
                                        {
                                            char2.cdir = -1;
                                        }
                                    }
                                }
                                if (num > 0)
                                {
                                    char2.attChars = new Char[num];
                                    for (num = 0; num < char2.attChars.Length; num++)
                                    {
                                        char2.attChars[num] = array[num];
                                    }
                                    char2.mobFocus = null;
                                    char2.charFocus = char2.attChars[0];
                                }
                            }
                            else
                            {
                                sbyte b2 = msg.reader().readByte();
                                sbyte b3 = msg.reader().readByte();
                                num3 = msg.reader().readInt();
                            }
                            try
                            {
                                sbyte b4 = msg.reader().readByte();
                                Res.outz("isRead continue = " + b4);
                                if (b4 != 1)
                                {
                                    break;
                                }
                                sbyte b5 = msg.reader().readByte();
                                Res.outz("type skill = " + b5);
                                if (num3 == Char.myCharz().charID)
                                {
                                    bool flag = false;
                                    @char = Char.myCharz();
                                    int num5 = msg.readInt3Byte();
                                    Res.outz("dame hit = " + num5);
                                    @char.isDie = msg.reader().readBoolean();
                                    if (@char.isDie)
                                    {
                                        Char.isLockKey = true;
                                    }
                                    Res.outz("isDie=" + @char.isDie + "---------------------------------------");
                                    int num6 = 0;
                                    flag = (@char.isCrit = msg.reader().readBoolean());
                                    @char.isMob = false;
                                    num5 = (@char.damHP = num5 + num6);
                                    if (b5 == 0)
                                    {
                                        @char.doInjure(num5, 0, flag, false);
                                    }
                                }
                                else
                                {
                                    @char = GameScr.findCharInMap(num3);
                                    if (@char == null)
                                    {
                                        return;
                                    }
                                    bool flag2 = false;
                                    int num7 = msg.readInt3Byte();
                                    Res.outz("dame hit= " + num7);
                                    @char.isDie = msg.reader().readBoolean();
                                    Res.outz("isDie=" + @char.isDie + "---------------------------------------");
                                    int num8 = 0;
                                    flag2 = (@char.isCrit = msg.reader().readBoolean());
                                    @char.isMob = false;
                                    num7 = (@char.damHP = num7 + num8);
                                    if (b5 == 0)
                                    {
                                        @char.doInjure(num7, 0, flag2, false);
                                    }
                                }
                            }
                            catch (Exception)
                            {
                            }
                            break;
                        }
                }
                switch (msg.command)
                {
                    case -2:
                        {
                            GameCanvas.debug("SA77", 22);
                            int num197 = msg.reader().readInt();
                            Char.myCharz().yen += num197;
                            GameScr.startFlyText((num197 <= 0) ? (string.Empty + num197) : ("+" + num197), Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 10, 0, -2, mFont.YELLOW);
                            break;
                        }
                    case 95:
                        {
                            GameCanvas.debug("SA77", 22);
                            int num184 = msg.reader().readInt();
                            Char.myCharz().xu += num184;
                            Char.myCharz().xuStr = Res.formatNumber(Char.myCharz().xu);
                            GameScr.startFlyText((num184 <= 0) ? (string.Empty + num184) : ("+" + num184), Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 10, 0, -2, mFont.YELLOW);
                            break;
                        }
                    case 96:
                        GameCanvas.debug("SA77a", 22);
                        Char.myCharz().taskOrders.addElement(new TaskOrder(msg.reader().readByte(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readUTF(), msg.reader().readUTF(), msg.reader().readByte(), msg.reader().readByte()));
                        break;
                    case 97:
                        {
                            sbyte b75 = msg.reader().readByte();
                            for (int num190 = 0; num190 < Char.myCharz().taskOrders.size(); num190++)
                            {
                                TaskOrder taskOrder = (TaskOrder)Char.myCharz().taskOrders.elementAt(num190);
                                if (taskOrder.taskId == b75)
                                {
                                    taskOrder.count = msg.reader().readShort();
                                    break;
                                }
                            }
                            break;
                        }
                    case -1:
                        {
                            GameCanvas.debug("SA77", 222);
                            int num196 = msg.reader().readInt();
                            Char.myCharz().xu += num196;
                            Char.myCharz().xuStr = Res.formatNumber(Char.myCharz().xu);
                            Char.myCharz().yen -= num196;
                            GameScr.startFlyText("+" + num196, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 10, 0, -2, mFont.YELLOW);
                            break;
                        }
                    case -3:
                        {
                            GameCanvas.debug("SA78", 2);
                            sbyte b71 = msg.reader().readByte();
                            int num181 = msg.reader().readInt();
                            if (b71 == 0)
                            {
                                Char.myCharz().cPower += num181;
                            }
                            if (b71 == 1)
                            {
                                Char.myCharz().cTiemNang += num181;
                            }
                            if (b71 == 2)
                            {
                                Char.myCharz().cPower += num181;
                                Char.myCharz().cTiemNang += num181;
                            }
                            Char.myCharz().applyCharLevelPercent();
                            if (Char.myCharz().cTypePk != 3)
                            {
                                GameScr.startFlyText(((num181 <= 0) ? string.Empty : "+") + num181, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch, 0, -4, mFont.GREEN);
                                if (num181 > 0 && Char.myCharz().petFollow != null && Char.myCharz().petFollow.smallID == 5002)
                                {
                                    ServerEffect.addServerEffect(55, Char.myCharz().petFollow.cmx, Char.myCharz().petFollow.cmy, 1);
                                    ServerEffect.addServerEffect(55, Char.myCharz().cx, Char.myCharz().cy, 1);
                                }
                            }
                            break;
                        }
                    case -73:
                        {
                            sbyte b77 = msg.reader().readByte();
                            for (int num195 = 0; num195 < GameScr.vNpc.size(); num195++)
                            {
                                Npc npc7 = (Npc)GameScr.vNpc.elementAt(num195);
                                if (npc7.template.npcTemplateId == b77)
                                {
                                    sbyte b78 = msg.reader().readByte();
                                    if (b78 == 0)
                                    {
                                        npc7.isHide = true;
                                    }
                                    else
                                    {
                                        npc7.isHide = false;
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    case -5:
                        {
                            GameCanvas.debug("SA79", 2);
                            int charID = msg.reader().readInt();
                            int num186 = msg.reader().readInt();
                            Char char16;
                            if (num186 != -100)
                            {
                                char16 = new Char();
                                char16.charID = charID;
                                char16.clanID = num186;
                            }
                            else
                            {
                                char16 = new Mabu();
                                char16.charID = charID;
                                char16.clanID = num186;
                            }
                            if (char16.clanID == -2)
                            {
                                char16.isCopy = true;
                            }
                            if (readCharInfo(char16, msg))
                            {
                                sbyte b73 = msg.reader().readByte();
                                if (char16.cy <= 10 && b73 != 0 && b73 != 2)
                                {
                                    Res.outz("nhân vật bay trên trời xuống x= " + char16.cx + " y= " + char16.cy);
                                    Teleport teleport2 = new Teleport(char16.cx, char16.cy, char16.head, char16.cdir, 1, false, (b73 != 1) ? b73 : char16.cgender);
                                    teleport2.id = char16.charID;
                                    char16.isTeleport = true;
                                    Teleport.addTeleport(teleport2);
                                }
                                if (b73 == 2)
                                {
                                    char16.show();
                                }
                                for (int num187 = 0; num187 < GameScr.vMob.size(); num187++)
                                {
                                    Mob mob10 = (Mob)GameScr.vMob.elementAt(num187);
                                    if (mob10 != null && mob10.isMobMe && mob10.mobId == char16.charID)
                                    {
                                        Res.outz("co 1 con quai");
                                        char16.mobMe = mob10;
                                        char16.mobMe.x = char16.cx;
                                        char16.mobMe.y = char16.cy - 40;
                                        break;
                                    }
                                }
                                if (GameScr.findCharInMap(char16.charID) == null)
                                {
                                    GameScr.vCharInMap.addElement(char16);
                                }
                                char16.isMonkey = msg.reader().readByte();
                                short num188 = msg.reader().readShort();
                                Res.outz("mount id= " + num188 + "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                                if (num188 != -1)
                                {
                                    char16.isHaveMount = true;
                                    switch (num188)
                                    {
                                        case 346:
                                        case 347:
                                        case 348:
                                            char16.isMountVip = false;
                                            break;
                                        case 349:
                                        case 350:
                                        case 351:
                                            char16.isMountVip = true;
                                            break;
                                        case 396:
                                            char16.isEventMount = true;
                                            break;
                                        case 532:
                                            char16.isSpeacialMount = true;
                                            break;
                                        default:
                                            if (num188 >= Char.ID_NEW_MOUNT)
                                            {
                                                char16.idMount = num188;
                                            }
                                            break;
                                    }
                                }
                                else
                                {
                                    char16.isHaveMount = false;
                                }
                            }
                            sbyte b74 = msg.reader().readByte();
                            Res.outz("addplayer:   " + b74);
                            char16.cFlag = b74;
                            char16.isNhapThe = msg.reader().readByte() == 1;
                            try
                            {
                                char16.idAuraEff = msg.reader().readShort();
                                char16.idEff_Set_Item = msg.reader().readSByte();
                                char16.idHat = msg.reader().readShort();
                                if (char16.bag >= 201 && char16.bag < 255)
                                {
                                    Effect effect3 = new Effect(char16.bag, char16, 2, -1, 10, 1);
                                    effect3.typeEff = 5;
                                    char16.addEffChar(effect3);
                                }
                                else
                                {
                                    for (int num189 = 0; num189 < 54; num189++)
                                    {
                                        char16.removeEffChar(0, 201 + num189);
                                    }
                                }
                            }
                            catch (Exception ex38)
                            {
                                Res.outz("cmd: -5 err: " + ex38.StackTrace);
                            }
                            GameScr.gI().getFlagImage(char16.charID, char16.cFlag);
                            break;
                        }
                    case -7:
                        {
                            GameCanvas.debug("SA80", 2);
                            int num179 = msg.reader().readInt();
                            for (int num182 = 0; num182 < GameScr.vCharInMap.size(); num182++)
                            {
                                Char char15 = null;
                                try
                                {
                                    char15 = (Char)GameScr.vCharInMap.elementAt(num182);
                                }
                                catch (Exception)
                                {
                                    continue;
                                }
                                if (char15 == null || char15.charID != num179)
                                {
                                    continue;
                                }
                                GameCanvas.debug("SA8x2y" + num182, 2);
                                char15.moveTo(msg.reader().readShort(), msg.reader().readShort(), 0);
                                char15.lastUpdateTime = mSystem.currentTimeMillis();
                                break;
                            }
                            GameCanvas.debug("SA80x3", 2);
                            break;
                        }
                    case -6:
                        {
                            GameCanvas.debug("SA81", 2);
                            int num179 = msg.reader().readInt();
                            for (int num180 = 0; num180 < GameScr.vCharInMap.size(); num180++)
                            {
                                Char char14 = (Char)GameScr.vCharInMap.elementAt(num180);
                                if (char14 != null && char14.charID == num179)
                                {
                                    if (!char14.isInvisiblez && !char14.isUsePlane)
                                    {
                                        ServerEffect.addServerEffect(60, char14.cx, char14.cy, 1);
                                    }
                                    if (!char14.isUsePlane)
                                    {
                                        GameScr.vCharInMap.removeElementAt(num180);
                                    }
                                    return;
                                }
                            }
                            break;
                        }
                    case -13:
                        {
                            GameCanvas.debug("SA82", 2);
                            int num191 = msg.reader().readUnsignedByte();
                            if (num191 > GameScr.vMob.size() - 1 || num191 < 0)
                            {
                                return;
                            }
                            Mob mob9 = (Mob)GameScr.vMob.elementAt(num191);
                            mob9.sys = msg.reader().readByte();
                            mob9.levelBoss = msg.reader().readByte();
                            if (mob9.levelBoss != 0)
                            {
                                mob9.typeSuperEff = Res.random(0, 3);
                            }
                            mob9.x = mob9.xFirst;
                            mob9.y = mob9.yFirst;
                            mob9.status = 5;
                            mob9.injureThenDie = false;
                            mob9.hp = msg.reader().readInt();
                            mob9.maxHp = mob9.hp;
                            mob9.updateHp_bar();
                            ServerEffect.addServerEffect(60, mob9.x, mob9.y, 1);
                            break;
                        }
                    case -75:
                        {
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception)
                            {
                            }
                            if (mob9 != null)
                            {
                                mob9.levelBoss = msg.reader().readByte();
                                if (mob9.levelBoss > 0)
                                {
                                    mob9.typeSuperEff = Res.random(0, 3);
                                }
                            }
                            break;
                        }
                    case -9:
                        {
                            GameCanvas.debug("SA83", 2);
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception)
                            {
                            }
                            GameCanvas.debug("SA83v1", 2);
                            if (mob9 != null)
                            {
                                mob9.hp = msg.readInt3Byte();
                                mob9.updateHp_bar();
                                int num183 = msg.readInt3Byte();
                                if (num183 == 1)
                                {
                                    return;
                                }
                                if (num183 > 1)
                                {
                                    mob9.setInjure();
                                }
                                bool flag11 = false;
                                try
                                {
                                    flag11 = msg.reader().readBoolean();
                                }
                                catch (Exception)
                                {
                                }
                                sbyte b72 = msg.reader().readByte();
                                if (b72 != -1)
                                {
                                    EffecMn.addEff(new Effect(b72, mob9.x, mob9.getY(), 3, 1, -1));
                                }
                                GameCanvas.debug("SA83v2", 2);
                                if (flag11)
                                {
                                    GameScr.startFlyText("-" + num183, mob9.x, mob9.getY() - mob9.getH(), 0, -2, mFont.FATAL);
                                }
                                else if (num183 == 0)
                                {
                                    mob9.x = mob9.xFirst;
                                    mob9.y = mob9.yFirst;
                                    GameScr.startFlyText(mResources.miss, mob9.x, mob9.getY() - mob9.getH(), 0, -2, mFont.MISS);
                                }
                                else if (num183 > 1)
                                {
                                    GameScr.startFlyText("-" + num183, mob9.x, mob9.getY() - mob9.getH(), 0, -2, mFont.ORANGE);
                                }
                            }
                            GameCanvas.debug("SA83v3", 2);
                            break;
                        }
                    case 45:
                        {
                            GameCanvas.debug("SA84", 2);
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception ex29)
                            {
                                Cout.println("Loi tai NPC_MISS  " + ex29.ToString());
                            }
                            if (mob9 != null)
                            {
                                mob9.hp = msg.reader().readInt();
                                mob9.updateHp_bar();
                                GameScr.startFlyText(mResources.miss, mob9.x, mob9.y - mob9.h, 0, -2, mFont.MISS);
                            }
                            break;
                        }
                    case -12:
                        {
                            Res.outz("SERVER SEND MOB DIE");
                            GameCanvas.debug("SA85", 2);
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception)
                            {
                                Cout.println("LOi tai NPC_DIE cmd " + msg.command);
                            }
                            if (mob9 == null || mob9.status == 0 || mob9.status == 0)
                            {
                                break;
                            }
                            mob9.startDie();
                            try
                            {
                                int num192 = msg.readInt3Byte();
                                if (msg.reader().readBool())
                                {
                                    GameScr.startFlyText("-" + num192, mob9.x, mob9.y - mob9.h, 0, -2, mFont.FATAL);
                                }
                                else
                                {
                                    GameScr.startFlyText("-" + num192, mob9.x, mob9.y - mob9.h, 0, -2, mFont.ORANGE);
                                }
                                sbyte b76 = msg.reader().readByte();
                                for (int num193 = 0; num193 < b76; num193++)
                                {
                                    ItemMap itemMap6 = new ItemMap(msg.reader().readShort(), msg.reader().readShort(), mob9.x, mob9.y, msg.reader().readShort(), msg.reader().readShort());
                                    int num194 = (itemMap6.playerId = msg.reader().readInt());
                                    Res.outz("playerid= " + num194 + " my id= " + Char.myCharz().charID);
                                    GameScr.vItemMap.addElement(itemMap6);
                                    if (Res.abs(itemMap6.y - Char.myCharz().cy) < 24 && Res.abs(itemMap6.x - Char.myCharz().cx) < 24)
                                    {
                                        Char.myCharz().charFocus = null;
                                    }
                                }
                            }
                            catch (Exception)
                            {
                            }
                            break;
                        }
                    case 74:
                        {
                            GameCanvas.debug("SA85", 2);
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception)
                            {
                                Cout.println("Loi tai NPC CHANGE " + msg.command);
                            }
                            if (mob9 != null && mob9.status != 0 && mob9.status != 0)
                            {
                                mob9.status = 0;
                                ServerEffect.addServerEffect(60, mob9.x, mob9.y, 1);
                                ItemMap itemMap5 = new ItemMap(msg.reader().readShort(), msg.reader().readShort(), mob9.x, mob9.y, msg.reader().readShort(), msg.reader().readShort());
                                GameScr.vItemMap.addElement(itemMap5);
                                if (Res.abs(itemMap5.y - Char.myCharz().cy) < 24 && Res.abs(itemMap5.x - Char.myCharz().cx) < 24)
                                {
                                    Char.myCharz().charFocus = null;
                                }
                            }
                            break;
                        }
                    case -11:
                        {
                            GameCanvas.debug("SA86", 2);
                            Mob mob9 = null;
                            try
                            {
                                int index4 = msg.reader().readUnsignedByte();
                                mob9 = (Mob)GameScr.vMob.elementAt(index4);
                            }
                            catch (Exception ex27)
                            {
                                Res.outz("Loi tai NPC_ATTACK_ME " + msg.command + " err= " + ex27.StackTrace);
                            }
                            if (mob9 != null)
                            {
                                Char.myCharz().isDie = false;
                                Char.isLockKey = false;
                                int num176 = msg.readInt3Byte();
                                int num177;
                                try
                                {
                                    num177 = msg.readInt3Byte();
                                }
                                catch (Exception)
                                {
                                    num177 = 0;
                                }
                                if (mob9.isBusyAttackSomeOne)
                                {
                                    Char.myCharz().doInjure(num176, num177, false, true);
                                    break;
                                }
                                mob9.dame = num176;
                                mob9.dameMp = num177;
                                mob9.setAttack(Char.myCharz());
                            }
                            break;
                        }
                    case -10:
                        {
                            GameCanvas.debug("SA87", 2);
                            Mob mob9 = null;
                            try
                            {
                                mob9 = (Mob)GameScr.vMob.elementAt(msg.reader().readUnsignedByte());
                            }
                            catch (Exception)
                            {
                            }
                            GameCanvas.debug("SA87x1", 2);
                            if (mob9 != null)
                            {
                                GameCanvas.debug("SA87x2", 2);
                                @char = GameScr.findCharInMap(msg.reader().readInt());
                                if (@char == null)
                                {
                                    return;
                                }
                                GameCanvas.debug("SA87x3", 2);
                                double num185 = msg.readHp();
                                mob9.dame = (int)(@char.cHP - num185);
                                @char.cHPNew = num185;
                                GameCanvas.debug("SA87x4", 2);
                                try
                                {
                                    @char.cMP = msg.readInt3Byte();
                                }
                                catch (Exception)
                                {
                                }
                                GameCanvas.debug("SA87x5", 2);
                                if (mob9.isBusyAttackSomeOne)
                                {
                                    @char.doInjure(mob9.dame, 0, false, true);
                                }
                                else
                                {
                                    mob9.setAttack(@char);
                                }
                                GameCanvas.debug("SA87x6", 2);
                            }
                            break;
                        }
                    case -17:
                        GameCanvas.debug("SA88", 2);
                        Char.myCharz().meDead = true;
                        Char.myCharz().cPk = msg.reader().readByte();
                        Char.myCharz().startDie(msg.reader().readShort(), msg.reader().readShort());
                        try
                        {
                            Char.myCharz().cPower = msg.reader().readLong();
                            Char.myCharz().applyCharLevelPercent();
                        }
                        catch (Exception)
                        {
                            Cout.println("Loi tai ME_DIE " + msg.command);
                        }
                        Char.myCharz().countKill = 0;
                        break;
                    case 66:
                        Res.outz("ME DIE XP DOWN NOT IMPLEMENT YET!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        break;
                    case -8:
                        GameCanvas.debug("SA89", 2);
                        @char = GameScr.findCharInMap(msg.reader().readInt());
                        if (@char == null)
                        {
                            return;
                        }
                        @char.cPk = msg.reader().readByte();
                        @char.waitToDie(msg.reader().readShort(), msg.reader().readShort());
                        break;
                    case -16:
                        GameCanvas.debug("SA90", 2);
                        if (Char.myCharz().wdx != 0 || Char.myCharz().wdy != 0)
                        {
                            Char.myCharz().cx = Char.myCharz().wdx;
                            Char.myCharz().cy = Char.myCharz().wdy;
                            Char.myCharz().wdx = (Char.myCharz().wdy = 0);
                        }
                        Char.myCharz().liveFromDead();
                        Char.myCharz().isLockMove = false;
                        Char.myCharz().meDead = false;
                        break;
                    // Ma lenh 59, KHONG dung 48.
                    //
                    // Ma 48 da co nghia trong giao thuc rieng cua mod:
                    // Controller2.readMessage bat no truoc, doc mot byte roi
                    // GOI Session_ME.close() va tra ve man dang nhap. Gui chat
                    // the gioi bang ma do la tu da nguoi choi ve man "Xin cho".
                    case 59:
                        {
                            // Chat the gioi: chi ghi vao khung chat, KHONG bat
                            // hop noi. Mot hop noi cho moi dong thi ban chat
                            // dong nguoi la khong con nhin thay ban do nua.
                            // Boc try/catch: mot loi o day chay nguoc len luong
                            // doc goi tin va giet ca phien — nguoi choi thay la
                            // "chat xong van g game". Ghi lai loi de con biet
                            // vi sao, roi bo qua dong tin do.
                            try
                            {
                                string tenTG = msg.reader().readUTF();
                                string chuTG = msg.reader().readUTF();
                                God.ChatUI.getInstance().nhanChatTheGioi(tenTG, chuTG);
                            }
                            catch (Exception exTG)
                            {
                                UnityEngine.Debug.LogError("[CHAT_TG] loi doc: "
                                        + exTG);
                            }
                            break;
                        }
                    case 44:
                        {
                            GameCanvas.debug("SA91", 2);
                            int num178 = msg.reader().readInt();
                            string text8 = msg.reader().readUTF();
                            Res.outz("user id= " + num178 + " text= " + text8);
                            @char = ((Char.myCharz().charID != num178) ? GameScr.findCharInMap(num178) : Char.myCharz());
                            if (@char == null)
                            {
                                return;
                            }
                            @char.addInfo(text8);
                            break;
                        }
                    case 18:
                        {
                            sbyte b70 = msg.reader().readByte();
                            for (int num175 = 0; num175 < b70; num175++)
                            {
                                int charId = msg.reader().readInt();
                                int cx = msg.reader().readShort();
                                int cy = msg.reader().readShort();
                                int cHPShow = msg.readInt3Byte();
                                Char char13 = GameScr.findCharInMap(charId);
                                if (char13 != null)
                                {
                                    char13.cx = cx;
                                    char13.cy = cy;
                                    char13.cHP = (char13.cHPShow = cHPShow);
                                    char13.lastUpdateTime = mSystem.currentTimeMillis();
                                }
                            }
                            break;
                        }
                    case 19:
                        Char.myCharz().countKill = msg.reader().readUnsignedShort();
                        Char.myCharz().countKillMax = msg.reader().readUnsignedShort();
                        break;
                }
                GameCanvas.debug("SA92", 2);
            }
            catch (Exception ex41)
            {
                Res.err("[Controller] [error] " + ex41.StackTrace + " msg: " + ex41.Message + " cause " + ex41.Data);
            }
            finally
            {
                if (msg != null)
                {
                    msg.cleanup();
                }
            }
        }
    
        private void readLogin(Message msg)
        {
            sbyte b = msg.reader().readByte();
            ChooseCharScr.playerData = new PlayerData[b];
            Res.outz("[LEN] sl nguoi choi " + b);
            for (int i = 0; i < b; i++)
            {
                int playerID = msg.reader().readInt();
                string name = msg.reader().readUTF();
                short head = msg.reader().readShort();
                short body = msg.reader().readShort();
                short leg = msg.reader().readShort();
                long ppoint = msg.reader().readLong();
                ChooseCharScr.playerData[i] = new PlayerData(playerID, name, head, body, leg, ppoint);
            }
            GameCanvas.chooseCharScr.switchToMe();
            GameCanvas.chooseCharScr.updateChooseCharacter((byte)b);
        }
    
        private void createItem(myReader d)
        {
            GameScr.vcItem = d.readByte();
            ItemTemplates.itemTemplates.clear();
            GameScr.gI().iOptionTemplates = new ItemOptionTemplate[d.readUnsignedByte()];
            for (int i = 0; i < GameScr.gI().iOptionTemplates.Length; i++)
            {
                GameScr.gI().iOptionTemplates[i] = new ItemOptionTemplate();
                GameScr.gI().iOptionTemplates[i].id = i;
                GameScr.gI().iOptionTemplates[i].name = d.readUTF();
                GameScr.gI().iOptionTemplates[i].type = d.readByte();
            }
            int num = d.readShort();
            for (int j = 0; j < num; j++)
            {
                ItemTemplate it = new ItemTemplate((short)j, d.readByte(), d.readByte(), d.readUTF(), d.readUTF(), d.readByte(), d.readInt(), d.readShort(), d.readShort(), d.readBool());
                ItemTemplates.add(it);
            }
        }
    
        private void createSkill(myReader d)
        {
            GameScr.vcSkill = d.readByte();
            GameScr.gI().sOptionTemplates = new SkillOptionTemplate[d.readByte()];
            for (int i = 0; i < GameScr.gI().sOptionTemplates.Length; i++)
            {
                GameScr.gI().sOptionTemplates[i] = new SkillOptionTemplate();
                GameScr.gI().sOptionTemplates[i].id = i;
                GameScr.gI().sOptionTemplates[i].name = d.readUTF();
            }
            GameScr.nClasss = new NClass[d.readByte()];
            for (int j = 0; j < GameScr.nClasss.Length; j++)
            {
                GameScr.nClasss[j] = new NClass();
                GameScr.nClasss[j].classId = j;
                GameScr.nClasss[j].name = d.readUTF();
                GameScr.nClasss[j].skillTemplates = new SkillTemplate[d.readByte()];
                for (int k = 0; k < GameScr.nClasss[j].skillTemplates.Length; k++)
                {
                    GameScr.nClasss[j].skillTemplates[k] = new SkillTemplate();
                    GameScr.nClasss[j].skillTemplates[k].id = d.readByte();
                    GameScr.nClasss[j].skillTemplates[k].name = d.readUTF();
                    GameScr.nClasss[j].skillTemplates[k].maxPoint = d.readByte();
                    GameScr.nClasss[j].skillTemplates[k].manaUseType = d.readByte();
                    GameScr.nClasss[j].skillTemplates[k].type = d.readByte();
                    GameScr.nClasss[j].skillTemplates[k].iconId = d.readShort();
                    GameScr.nClasss[j].skillTemplates[k].damInfo = d.readUTF();
                    int lineWidth = 130;
                    if (GameCanvas.w == 128 || GameCanvas.h <= 208)
                    {
                        lineWidth = 100;
                    }
                    GameScr.nClasss[j].skillTemplates[k].description = mFont.tahoma_7_green2.splitFontArray(d.readUTF(), lineWidth);
                    GameScr.nClasss[j].skillTemplates[k].skills = new Skill[d.readByte()];
                    for (int l = 0; l < GameScr.nClasss[j].skillTemplates[k].skills.Length; l++)
                    {
                        GameScr.nClasss[j].skillTemplates[k].skills[l] = new Skill();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].skillId = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].template = GameScr.nClasss[j].skillTemplates[k];
                        GameScr.nClasss[j].skillTemplates[k].skills[l].point = d.readByte();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].powRequire = d.readLong();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].manaUse = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].coolDown = d.readInt();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].dx = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].dy = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].maxFight = d.readByte();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].damage = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].price = d.readShort();
                        GameScr.nClasss[j].skillTemplates[k].skills[l].moreInfo = d.readUTF();
                        Skills.add(GameScr.nClasss[j].skillTemplates[k].skills[l]);
                    }
                }
            }
        }
    
        private void createMap(myReader d)
        {
            GameScr.vcMap = d.readByte();
            TileMap.mapNames = new string[d.readUnsignedByte()];
            for (int i = 0; i < TileMap.mapNames.Length; i++)
            {
                TileMap.mapNames[i] = d.readUTF();
            }
            Npc.arrNpcTemplate = new NpcTemplate[d.readByte()];
            for (sbyte b = 0; b < Npc.arrNpcTemplate.Length; b++)
            {
                Npc.arrNpcTemplate[b] = new NpcTemplate();
                Npc.arrNpcTemplate[b].npcTemplateId = b;
                Npc.arrNpcTemplate[b].name = d.readUTF();
                Npc.arrNpcTemplate[b].headId = d.readShort();
                Npc.arrNpcTemplate[b].bodyId = d.readShort();
                Npc.arrNpcTemplate[b].legId = d.readShort();
                Npc.arrNpcTemplate[b].menu = new string[d.readByte()][];
                for (int j = 0; j < Npc.arrNpcTemplate[b].menu.Length; j++)
                {
                    Npc.arrNpcTemplate[b].menu[j] = new string[d.readByte()];
                    for (int k = 0; k < Npc.arrNpcTemplate[b].menu[j].Length; k++)
                    {
                        Npc.arrNpcTemplate[b].menu[j][k] = d.readUTF();
                    }
                }
            }
            Mob.arrMobTemplate = new MobTemplate[d.readByte()];
            for (sbyte b2 = 0; b2 < Mob.arrMobTemplate.Length; b2++)
            {
                Mob.arrMobTemplate[b2] = new MobTemplate();
                Mob.arrMobTemplate[b2].mobTemplateId = b2;
                Mob.arrMobTemplate[b2].type = d.readByte();
                Mob.arrMobTemplate[b2].name = d.readUTF();
                Mob.arrMobTemplate[b2].hp = d.readInt();
                Mob.arrMobTemplate[b2].rangeMove = d.readByte();
                Mob.arrMobTemplate[b2].speed = d.readByte();
                Mob.arrMobTemplate[b2].dartType = d.readByte();
            }
        }
    
        private void createData(myReader d, bool isSaveRMS)
        {
            GameScr.vcData = d.readByte();
            if (isSaveRMS)
            {
                Rms.saveRMS("NR_dart", NinjaUtil.readByteArray(d));
                Rms.saveRMS("NR_arrow", NinjaUtil.readByteArray(d));
                Rms.saveRMS("NR_effect", NinjaUtil.readByteArray(d));
                Rms.saveRMS("NR_image", NinjaUtil.readByteArray(d));
                Rms.saveRMS("NR_part", NinjaUtil.readByteArray(d));
                Rms.saveRMS("NR_skill", NinjaUtil.readByteArray(d));
                Rms.DeleteStorage("NRdata");
            }
        }
    
        private Image createImage(sbyte[] arr)
        {
            try
            {
                return Image.createImage(arr, 0, arr.Length);
            }
            catch (Exception)
            {
            }
            return null;
        }
    
        public int[] arrayByte2Int(sbyte[] b)
        {
            int[] array = new int[b.Length];
            for (int i = 0; i < b.Length; i++)
            {
                int num = b[i];
                if (num < 0)
                {
                    num += 256;
                }
                array[i] = num;
            }
            return array;
        }
    
        public void readClanMsg(Message msg, int index)
        {
            try
            {
                ClanMessage clanMessage = new ClanMessage();
                sbyte b = msg.reader().readByte();
                clanMessage.type = b;
                clanMessage.id = msg.reader().readInt();
                clanMessage.playerId = msg.reader().readInt();
                clanMessage.playerName = msg.reader().readUTF();
                clanMessage.role = msg.reader().readByte();
                clanMessage.time = msg.reader().readInt() + 1000000000;
                bool flag = false;
                GameScr.isNewClanMessage = false;
                if (b == 0)
                {
                    string text = msg.reader().readUTF();
                    // Ghi vao nhat ky khung chat: the "Bang hoi" doc tu
                    // day. Day la nguon DUY NHAT client chac chan biet la
                    // chat bang, nen phan loai ngay tai cho nhan.
                    God.ChatUI.getInstance().nhanChatBang(clanMessage.id,
                            clanMessage.playerName, text);
                    // KHONG bat co nhap nhay nua: tin bang gio hien thang
                    // trong khung chat, con canh bao vang nhay o goc man hinh
                    // thanh ra bao mot thu nguoi choi da nhin thay roi.
                    GameScr.isNewClanMessage = false;
                    if (mFont.tahoma_7.getWidth(text) > Panel.WIDTH_PANEL - 60)
                    {
                        clanMessage.chat = mFont.tahoma_7.splitFontArray(text, Panel.WIDTH_PANEL - 10);
                    }
                    else
                    {
                        clanMessage.chat = new string[1];
                        clanMessage.chat[0] = text;
                    }
                    clanMessage.color = msg.reader().readByte();
                }
                else if (b == 1)
                {
                    clanMessage.recieve = msg.reader().readByte();
                    clanMessage.maxCap = msg.reader().readByte();
                    flag = msg.reader().readByte() == 1;
                    if (flag)
                    {
                        GameScr.isNewClanMessage = true;
                    }
                    if (clanMessage.playerId != Char.myCharz().charID)
                    {
                        if (clanMessage.recieve < clanMessage.maxCap)
                        {
                            clanMessage.option = new string[1] { mResources.donate };
                        }
                        else
                        {
                            clanMessage.option = null;
                        }
                    }
                    if (GameCanvas.panel.cp != null)
                    {
                        GameCanvas.panel.updateRequest(clanMessage.recieve, clanMessage.maxCap);
                    }
                }
                else if (b == 2 && Char.myCharz().role == 0)
                {
                    GameScr.isNewClanMessage = true;
                    clanMessage.option = new string[2]
                    {
                        mResources.CANCEL,
                        mResources.receive
                    };
                }
                if (GameCanvas.currentScreen != GameScr.instance)
                {
                    GameScr.isNewClanMessage = false;
                }
                else if (GameCanvas.panel.isShow && GameCanvas.panel.type == 0 && GameCanvas.panel.currentTabIndex == 3)
                {
                    GameScr.isNewClanMessage = false;
                }
                ClanMessage.addMessage(clanMessage, index, flag);
            }
            catch (Exception)
            {
                Cout.println("LOI TAI CMD -= " + msg.command);
            }
        }
    
        public void loadCurrMap(sbyte teleport3)
        {
            Res.outz("[CONTROLER] start load map " + teleport3);
            GameScr.gI().auto = 0;
            GameScr.isChangeZone = false;
            CreateCharScr.instance = null;
            GameScr.info1.isUpdate = false;
            GameScr.info2.isUpdate = false;
            GameScr.lockTick = 0;
            GameCanvas.panel.isShow = false;
            SoundMn.gI().stopAll();
            if (!GameScr.isLoadAllData && !CreateCharScr.isCreateChar)
            {
                GameScr.gI().initSelectChar();
            }
            GameScr.loadCamera(false, (teleport3 != 1) ? (-1) : Char.myCharz().cx, (teleport3 == 0) ? (-1) : 0);
            TileMap.loadMainTile();
            TileMap.loadMap(TileMap.tileID);
            Res.outz("LOAD GAMESCR 2");
            Char.myCharz().cvx = 0;
            Char.myCharz().statusMe = 4;
            Char.myCharz().currentMovePoint = null;
            Char.myCharz().mobFocus = null;
            Char.myCharz().charFocus = null;
            Char.myCharz().npcFocus = null;
            Char.myCharz().itemFocus = null;
            Char.myCharz().skillPaint = null;
            Char.myCharz().setMabuHold(false);
            Char.myCharz().skillPaintRandomPaint = null;
            GameCanvas.clearAllPointerEvent();
            if (Char.myCharz().cy >= TileMap.pxh - 100)
            {
                Char.myCharz().isFlyUp = true;
                Char.myCharz().cx += Res.abs(Res.random(0, 80));
                Service.gI().charMove();
            }
            GameScr.gI().loadGameScr();
            GameCanvas.loadBG(TileMap.bgID);
            Char.isLockKey = false;
            Res.outz("cy= " + Char.myCharz().cy + "---------------------------------------------");
            for (int i = 0; i < Char.myCharz().vEff.size(); i++)
            {
                EffectChar effectChar = (EffectChar)Char.myCharz().vEff.elementAt(i);
                if (effectChar.template.type == 10)
                {
                    Char.isLockKey = true;
                    break;
                }
            }
            GameCanvas.clearKeyHold();
            GameCanvas.clearKeyPressed();
            GameScr.gI().dHP = Char.myCharz().cHP;
            GameScr.gI().dMP = Char.myCharz().cMP;
            Char.ischangingMap = false;
            GameScr.gI().switchToMe();
            if (Char.myCharz().cy <= 10 && teleport3 != 0 && teleport3 != 2)
            {
                Teleport p = new Teleport(Char.myCharz().cx, Char.myCharz().cy, Char.myCharz().head, Char.myCharz().cdir, 1, true, (teleport3 != 1) ? teleport3 : Char.myCharz().cgender);
                Teleport.addTeleport(p);
                Char.myCharz().isTeleport = true;
            }
            if (teleport3 == 2)
            {
                Char.myCharz().show();
            }
            if (GameScr.gI().isRongThanXuatHien)
            {
                if (TileMap.mapID == GameScr.gI().mapRID && TileMap.zoneID == GameScr.gI().zoneRID)
                {
                    GameScr.gI().callRongThan(GameScr.gI().xR, GameScr.gI().yR);
                }
                if (mGraphics.zoomLevel > 1)
                {
                    GameScr.gI().doiMauTroi();
                }
            }
            InfoDlg.hide();
            InfoDlg.show(TileMap.mapName, mResources.zone + " " + TileMap.zoneID, 30);
            GameCanvas.endDlg();
            GameCanvas.isLoading = false;
            Hint.clickMob();
            Hint.clickNpc();
            GameCanvas.debug("SA75x9", 2);
            GameCanvas.isRequestMapID = 2;
            GameCanvas.waitingTimeChangeMap = mSystem.currentTimeMillis() + 150;
            Res.outz("[CONTROLLER] loadMap DONE!!!!!!!!!");
        }
    
        /// <summary>
        /// Ghi một lỗi nạp bản đồ ra log Unity và tệp <c>loi_nap_map.txt</c> trong
        /// thư mục dữ liệu của game — luôn ghi, không phụ thuộc chế độ thử.
        /// </summary>
        public static void ghiLoiNapMap(string buoc, Exception ex)
        {
            string dong = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss") + " map " + TileMap.mapID
                    + " [" + buoc + "] " + (ex == null ? "?" : ex.GetType().Name + ": " + ex.Message
                    + "\n" + ex.StackTrace) + "\n";
            try
            {
                UnityEngine.Debug.LogError("[NapMap] " + dong);
            }
            catch (Exception)
            {
            }
            try
            {
                System.IO.File.AppendAllText(System.IO.Path.Combine(
                        UnityEngine.Application.persistentDataPath, "loi_nap_map.txt"), dong);
            }
            catch (Exception)
            {
            }
        }

        public void loadInfoMap(Message msg)
        {
            bool daGoiLoadCurrMap = false;
            try
            {
                if (mGraphics.zoomLevel == 1)
                {
                    SmallImage.clearHastable();
                }
                Char.myCharz().cx = (Char.myCharz().cxSend = (Char.myCharz().cxFocus = msg.reader().readShort()));
                Char.myCharz().cy = (Char.myCharz().cySend = (Char.myCharz().cyFocus = msg.reader().readShort()));
                Char.myCharz().xSd = Char.myCharz().cx;
                Char.myCharz().ySd = Char.myCharz().cy;
                Res.outz("head= " + Char.myCharz().head + " body= " + Char.myCharz().body + " left= " + Char.myCharz().leg + " x= " + Char.myCharz().cx + " y= " + Char.myCharz().cy + " chung toc= " + Char.myCharz().cgender);
                if (Char.myCharz().cx >= 0 && Char.myCharz().cx <= 100)
                {
                    Char.myCharz().cdir = 1;
                }
                else if (Char.myCharz().cx >= TileMap.tmw - 100 && Char.myCharz().cx <= TileMap.tmw)
                {
                    Char.myCharz().cdir = -1;
                }
                GameCanvas.debug("SA75x4", 2);
                int num = msg.reader().readByte();
                Res.outz("vGo size= " + num);
                if (!GameScr.info1.isDone)
                {
                    GameScr.info1.cmx = Char.myCharz().cx - GameScr.cmx;
                    GameScr.info1.cmy = Char.myCharz().cy - GameScr.cmy;
                }
                for (int i = 0; i < num; i++)
                {
                    Waypoint waypoint = new Waypoint(msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readShort(), msg.reader().readBoolean(), msg.reader().readBoolean(), msg.reader().readUTF());
                    if ((TileMap.mapID != 21 && TileMap.mapID != 22 && TileMap.mapID != 23) || waypoint.minX < 0 || waypoint.minX <= 24)
                    {
                    }
                }
                Resources.UnloadUnusedAssets();
                GC.Collect();
                GameCanvas.debug("SA75x5", 2);
                num = msg.reader().readByte();
                Mob.newMob.removeAllElements();
                for (sbyte b = 0; b < num; b++)
                {
                    // Doc HET cac truong cua con quai truoc roi moi dung doi tuong:
                    // dung loi giua chung la cac byte sau bi doc lech, hong ca goi.
                    bool mqTat = msg.reader().readBoolean();
                    bool mqDungYen = msg.reader().readBoolean();
                    bool mqLua = msg.reader().readBoolean();
                    bool mqBang = msg.reader().readBoolean();
                    bool mqGio = msg.reader().readBoolean();
                    sbyte mqMau = msg.reader().readByte();
                    sbyte mqHe = msg.reader().readByte();
                    int mqHp = msg.reader().readInt();
                    sbyte mqCap = msg.reader().readByte();
                    int mqHpMax = msg.reader().readInt();
                    short mqX = msg.reader().readShort();
                    short mqY = msg.reader().readShort();
                    sbyte mqTrangThai = msg.reader().readByte();
                    sbyte mqCapBoss = msg.reader().readByte();
                    bool mqLaBoss = msg.reader().readBoolean();
                    try
                    {
                    Mob mob = new Mob(b, mqTat, mqDungYen, mqLua, mqBang, mqGio, mqMau, mqHe, mqHp, mqCap, mqHpMax, mqX, mqY, mqTrangThai, mqCapBoss);
                    mob.xSd = mob.x;
                    mob.ySd = mob.y;
                    mob.isBoss = mqLaBoss;
                    if (mob.coMauQuai() && Mob.arrMobTemplate[mob.templateId].type != 0)
                    {
                        if (b % 3 == 0)
                        {
                            mob.dir = -1;
                        }
                        else
                        {
                            mob.dir = 1;
                        }
                        mob.x += 10 - b % 20;
                    }
                    mob.isMobMe = false;
                    BigBoss bigBoss = null;
                    BachTuoc bachTuoc = null;
                    BigBoss2 bigBoss2 = null;
                    NewBoss newBoss = null;
                    if (mob.templateId == 70)
                    {
                        bigBoss = new BigBoss(b, (short)mob.x, (short)mob.y, 70, mob.hp, mob.maxHp, mob.sys);
                    }
                    if (mob.templateId == 71)
                    {
                        bachTuoc = new BachTuoc(b, (short)mob.x, (short)mob.y, 71, mob.hp, mob.maxHp, mob.sys);
                    }
                    if (mob.templateId == 72)
                    {
                        bigBoss2 = new BigBoss2(b, (short)mob.x, (short)mob.y, 72, mob.hp, mob.maxHp, 3);
                    }
                    if (mob.isBoss)
                    {
                        newBoss = new NewBoss(b, (short)mob.x, (short)mob.y, mob.templateId, mob.hp, mob.maxHp, mob.sys);
                    }
                    if (newBoss != null)
                    {
                        GameScr.vMob.addElement(newBoss);
                    }
                    else if (bigBoss != null)
                    {
                        GameScr.vMob.addElement(bigBoss);
                    }
                    else if (bachTuoc != null)
                    {
                        GameScr.vMob.addElement(bachTuoc);
                    }
                    else if (bigBoss2 != null)
                    {
                        GameScr.vMob.addElement(bigBoss2);
                    }
                    else
                    {
                        GameScr.vMob.addElement(mob);
                    }
                    }
                    catch (Exception exQuai)
                    {
                        ghiLoiNapMap("quai mau " + mqMau, exQuai);
                    }
                }
                if (Char.myCharz().mobMe != null && GameScr.findMobInMap(Char.myCharz().mobMe.mobId) == null)
                {
                    Char.myCharz().mobMe.getData();
                    Char.myCharz().mobMe.x = Char.myCharz().cx;
                    Char.myCharz().mobMe.y = Char.myCharz().cy - 40;
                    GameScr.vMob.addElement(Char.myCharz().mobMe);
                }
                num = msg.reader().readByte();
                for (byte b2 = 0; b2 < num; b2++)
                {
                }
                GameCanvas.debug("SA75x6", 2);
                num = msg.reader().readByte();
                Res.outz("NPC size= " + num);
                for (int j = 0; j < num; j++)
                {
                    sbyte b3 = msg.reader().readByte();
                    short cx = msg.reader().readShort();
                    short num2 = msg.reader().readShort();
                    sbyte b4 = msg.reader().readByte();
                    short num3 = msg.reader().readShort();
                    try
                    {
                    if (b4 != 6 && ((Char.myCharz().taskMaint.taskId >= 7 && (Char.myCharz().taskMaint.taskId != 7 || Char.myCharz().taskMaint.index > 1)) || (b4 != 7 && b4 != 8 && b4 != 9)) && (Char.myCharz().taskMaint.taskId >= 6 || b4 != 16))
                    {
                        if (b4 == 4)
                        {
                            GameScr.gI().magicTree = new MagicTree(j, b3, cx, num2, b4, num3);
                            Service.gI().magicTree(2);
                            GameScr.vNpc.addElement(GameScr.gI().magicTree);
                        }
                        else
                        {
                            Npc o = new Npc(j, b3, cx, num2 + 3, b4, num3);
                            GameScr.vNpc.addElement(o);
                        }
                    }
                    }
                    catch (Exception exPt)
                    {
                        ghiLoiNapMap("npc mau " + b4, exPt);
                    }
                }
                GameCanvas.debug("SA75x7", 2);
                num = msg.reader().readByte();
                string empty = string.Empty;
                Res.outz("item size = " + num);
                empty = empty + "item: " + num;
                for (int k = 0; k < num; k++)
                {
                    short itemMapID = msg.reader().readShort();
                    short num4 = msg.reader().readShort();
                    int x = msg.reader().readShort();
                    int y = msg.reader().readShort();
                    int num5 = msg.reader().readInt();
                    short r = 0;
                    if (num5 == -2)
                    {
                        r = msg.reader().readShort();
                    }
                    try
                    {
                    ItemMap itemMap = new ItemMap(num5, itemMapID, num4, x, y, r);
                    bool flag = false;
                    for (int l = 0; l < GameScr.vItemMap.size(); l++)
                    {
                        ItemMap itemMap2 = (ItemMap)GameScr.vItemMap.elementAt(l);
                        if (itemMap2.itemMapID == itemMap.itemMapID)
                        {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag)
                    {
                        GameScr.vItemMap.addElement(itemMap);
                    }
                    }
                    catch (Exception exPt)
                    {
                        ghiLoiNapMap("vat pham " + num4, exPt);
                    }
                    empty = empty + num4 + ",";
                }
                Res.err("sl item on map " + empty + "\n");
                TileMap.vCurrItem.removeAllElements();
                if (mGraphics.zoomLevel == 1)
                {
                    BgItem.clearHashTable();
                }
                BgItem.vKeysNew.removeAllElements();
                if (!GameCanvas.lowGraphic || (GameCanvas.lowGraphic && TileMap.isVoDaiMap()) || TileMap.mapID == 45 || TileMap.mapID == 46 || TileMap.mapID == 47 || TileMap.mapID == 48 || TileMap.mapID == 120 || TileMap.mapID == 128 || TileMap.mapID == 170 || TileMap.mapID == 49)
                {
                    short num6 = msg.reader().readShort();
                    empty = "item high graphic: ";
                    for (int m = 0; m < num6; m++)
                    {
                        short num7 = msg.reader().readShort();
                        short num8 = msg.reader().readShort();
                        short num9 = msg.reader().readShort();
                        try
                        {
                        if (TileMap.getBIById(num7) != null)
                        {
                            BgItem bIById = TileMap.getBIById(num7);
                            BgItem bgItem = new BgItem();
                            bgItem.id = num7;
                            bgItem.idImage = bIById.idImage;
                            bgItem.dx = bIById.dx;
                            bgItem.dy = bIById.dy;
                            bgItem.x = num8 * TileMap.size;
                            bgItem.y = num9 * TileMap.size;
                            bgItem.layer = bIById.layer;
                            if (TileMap.isExistMoreOne(bgItem.id))
                            {
                                bgItem.trans = ((m % 2 != 0) ? 2 : 0);
                                if (TileMap.mapID == 45)
                                {
                                    bgItem.trans = 0;
                                }
                            }
                            Image image = null;
                            if (!BgItem.imgNew.containsKey(bgItem.idImage + string.Empty))
                            {
                                if (mGraphics.zoomLevel == 1)
                                {
                                    image = GameCanvas.loadImage("/mapBackGround/" + bgItem.idImage + ".png");
                                    if (image == null)
                                    {
                                        image = Image.createRGBImage(new int[1], 1, 1, true);
                                        Service.gI().getBgTemplate(bgItem.idImage);
                                    }
                                    BgItem.imgNew.put(bgItem.idImage + string.Empty, image);
                                }
                                else
                                {
                                    bool flag2 = false;
                                    sbyte[] array = Rms.loadRMS(mGraphics.zoomLevel + "bgItem" + bgItem.idImage);
                                    if (array != null)
                                    {
                                        // Bang phien ban may chu gui co the ngan hon id anh: vuot bang thi coi nhu
                                        // chua biet phien ban, dung ban da luu. Doc vuot bang la nem loi giua luc nap map.
                                        if (BgItem.newSmallVersion != null && bgItem.idImage >= 0 && bgItem.idImage < BgItem.newSmallVersion.Length)
                                        {
                                            if (array.Length % 127 != BgItem.newSmallVersion[bgItem.idImage])
                                            {
                                                flag2 = true;
                                            }
                                        }
                                        if (!flag2)
                                        {
                                            image = Image.createImage(array, 0, array.Length);
                                            if (image != null)
                                            {
                                                BgItem.imgNew.put(bgItem.idImage + string.Empty, image);
                                            }
                                            else
                                            {
                                                flag2 = true;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        flag2 = true;
                                    }
                                    if (flag2)
                                    {
                                        image = GameCanvas.loadImage("/mapBackGround/" + bgItem.idImage + ".png");
                                        if (image == null)
                                        {
                                            image = Image.createRGBImage(new int[1], 1, 1, true);
                                            Service.gI().getBgTemplate(bgItem.idImage);
                                        }
                                        BgItem.imgNew.put(bgItem.idImage + string.Empty, image);
                                    }
                                }
                                BgItem.vKeysLast.addElement(bgItem.idImage + string.Empty);
                            }
                            if (!BgItem.isExistKeyNews(bgItem.idImage + string.Empty))
                            {
                                BgItem.vKeysNew.addElement(bgItem.idImage + string.Empty);
                            }
                            bgItem.changeColor();
                            TileMap.vCurrItem.addElement(bgItem);
                        }
                        }
                        catch (Exception exPt)
                        {
                            ghiLoiNapMap("do nen " + num7, exPt);
                        }
                        empty = empty + num7 + ",";
                    }
                    Res.err("item High Graphics: " + empty);
                    for (int n = 0; n < BgItem.vKeysLast.size(); n++)
                    {
                        string text = (string)BgItem.vKeysLast.elementAt(n);
                        if (!BgItem.isExistKeyNews(text))
                        {
                            BgItem.imgNew.remove(text);
                            if (BgItem.imgNew.containsKey(text + "blend" + 1))
                            {
                                BgItem.imgNew.remove(text + "blend" + 1);
                            }
                            if (BgItem.imgNew.containsKey(text + "blend" + 3))
                            {
                                BgItem.imgNew.remove(text + "blend" + 3);
                            }
                            BgItem.vKeysLast.removeElementAt(n);
                            n--;
                        }
                    }
                    BackgroudEffect.isFog = false;
                    BackgroudEffect.nCloud = 0;
                    EffecMn.vEff.removeAllElements();
                    BackgroudEffect.vBgEffect.removeAllElements();
                    Effect.newEff.removeAllElements();
                    short num10 = msg.reader().readShort();
                    for (int num11 = 0; num11 < num10; num11++)
                    {
                        string key = msg.reader().readUTF();
                        string value = msg.reader().readUTF();
                        try
                        {
                            keyValueAction(key, value);
                        }
                        catch (Exception exKv)
                        {
                            ghiLoiNapMap("khoa " + key, exKv);
                        }
                    }
                }
                else
                {
                    short num12 = msg.reader().readShort();
                    for (int num13 = 0; num13 < num12; num13++)
                    {
                        short num14 = msg.reader().readShort();
                        short num15 = msg.reader().readShort();
                        short num16 = msg.reader().readShort();
                    }
                    short num17 = msg.reader().readShort();
                    for (int num18 = 0; num18 < num17; num18++)
                    {
                        string text2 = msg.reader().readUTF();
                        string text3 = msg.reader().readUTF();
                    }
                }
                TileMap.bgType = msg.reader().readByte();
                sbyte teleport = msg.reader().readByte();
                daGoiLoadCurrMap = true;
                loadCurrMap(teleport);
                GameCanvas.debug("SA75x8", 2);
            }
            catch (Exception ex)
            {
                // KHONG da ve dang nhap nua. Ban cu goi doResetToLoginScr: xoa
                // nhan vat (HP 0/0), xoa quai va nguoi, DONG KET NOI — ma man hinh
                // van dung o ban do voi dia hinh da nap. Do chinh la "dung map".
                //
                // Nay ghi loi lai roi van dung xong ban do: nen, camera, bao may
                // chu da nap xong (-39) de no gui nguoi va quai xung quanh.
                ghiLoiNapMap("loadInfoMap", ex);
                if (!daGoiLoadCurrMap)
                {
                    try
                    {
                        loadCurrMap(0);
                    }
                    catch (Exception ex2)
                    {
                        ghiLoiNapMap("loadCurrMap du phong", ex2);
                    }
                }
                else
                {
                    // Loi nam trong chinh loadCurrMap: lam not hai viec cuoi cua no.
                    try
                    {
                        GameCanvas.loadBG(TileMap.bgID);
                    }
                    catch (Exception)
                    {
                    }
                    try
                    {
                        GameScr.gI().switchToMe();
                    }
                    catch (Exception ex3)
                    {
                        ghiLoiNapMap("switchToMe du phong", ex3);
                    }
                }
                GameCanvas.endDlg();
            }
            GameCanvas.isLoading = false;
            Res.err(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Loadmap thanh cong");
        }
    
        public void keyValueAction(string key, string value)
        {
            if (key.Equals("eff"))
            {
                if (Panel.graphics > 0)
                {
                    return;
                }
                string[] array = Res.split(value, ".", 0);
                int id = int.Parse(array[0]);
                int layer = int.Parse(array[1]);
                int x = int.Parse(array[2]);
                int y = int.Parse(array[3]);
                int loop;
                int loopCount;
                if (array.Length <= 4)
                {
                    loop = -1;
                    loopCount = 1;
                }
                else
                {
                    loop = int.Parse(array[4]);
                    loopCount = int.Parse(array[5]);
                }
                Effect effect = new Effect(id, x, y, layer, loop, loopCount);
                if (array.Length > 6)
                {
                    effect.typeEff = int.Parse(array[6]);
                    if (array.Length > 7)
                    {
                        effect.indexFrom = int.Parse(array[7]);
                        effect.indexTo = int.Parse(array[8]);
                    }
                }
                EffecMn.addEff(effect);
            }
            else if (key.Equals("beff") && Panel.graphics <= 1)
            {
                BackgroudEffect.addEffect(int.Parse(value));
            }
        }
    
        public void messageNotMap(Message msg)
        {
            GameCanvas.debug("SA6", 2);
            try
            {
                sbyte b = msg.reader().readByte();
                Res.outz("---messageNotMap : " + b);
                switch (b)
                {
                    case 16:
                        MoneyCharge.gI().switchToMe();
                        break;
                    case 17:
                        GameCanvas.debug("SYB123", 2);
                        Char.myCharz().clearTask();
                        break;
                    case 18:
                        {
                            GameCanvas.isLoading = false;
                            GameCanvas.endDlg();
                            int num2 = msg.reader().readInt();
                            // Tat go tieng Viet: ten nhan vat khong nhan dau, bat
                            // len chi lam nguoi choi go ra ten bi may chu tu choi.
                            int idDoiTen = num2;
                            God.HopNhapChu.getInstance().moRa(mResources.changeNameChar,
                                    string.Empty, 30, false, chu =>
                                    {
                                        Service.gI().changeName(chu, idDoiTen);
                                        InfoDlg.showWait();
                                    });
                            break;
                        }
                    case 20:
                        Char.myCharz().cPk = msg.reader().readByte();
                        GameScr.info1.addInfo(mResources.PK_NOW + " " + Char.myCharz().cPk, 0);
                        break;
                    case 35:
                        GameCanvas.endDlg();
                        GameScr.gI().resetButton();
                        GameScr.info1.addInfo(msg.reader().readUTF(), 0);
                        break;
                    case 36:
                        GameScr.typeActive = msg.reader().readByte();
                        Res.outz("load Me Active: " + GameScr.typeActive);
                        break;
                    case 4:
                        {
                            GameCanvas.debug("SA8", 2);
                            GameCanvas.loginScr.savePass();
                            GameScr.isAutoPlay = false;
                            GameScr.canAutoPlay = false;
                            LoginScr.isUpdateAll = true;
                            LoginScr.isUpdateData = true;
                            LoginScr.isUpdateMap = true;
                            LoginScr.isUpdateSkill = true;
                            LoginScr.isUpdateItem = true;
                            GameScr.vsData = msg.reader().readByte();
                            GameScr.vsMap = msg.reader().readByte();
                            GameScr.vsSkill = msg.reader().readByte();
                            GameScr.vsItem = msg.reader().readByte();
                            sbyte b3 = msg.reader().readByte();
                            if (GameCanvas.loginScr.isLogin2)
                            {
                                Rms.saveRMSString("acc2", string.Empty);
                                Rms.saveRMSString("pass2", string.Empty);
                            }
                            else
                            {
                                Rms.saveRMSString("userAo2" + ServerListScreen.ipSelect, string.Empty);
                            }
                            if (GameScr.vsData != GameScr.vcData)
                            {
                                GameScr.isLoadAllData = false;
                                Service.gI().updateData();
                            }
                            else
                            {
                                try
                                {
                                    LoginScr.isUpdateData = false;
                                }
                                catch (Exception)
                                {
                                    GameScr.vcData = -1;
                                    Service.gI().updateData();
                                }
                            }
                            if (GameScr.vsMap != GameScr.vcMap)
                            {
                                GameScr.isLoadAllData = false;
                                Service.gI().updateMap();
                            }
                            else
                            {
                                try
                                {
                                    if (!GameScr.isLoadAllData)
                                    {
                                        DataInputStream dataInputStream = new DataInputStream(Rms.loadRMS("NRmap"));
                                        createMap(dataInputStream.r);
                                    }
                                    LoginScr.isUpdateMap = false;
                                }
                                catch (Exception)
                                {
                                    GameScr.vcMap = -1;
                                    Service.gI().updateMap();
                                }
                            }
                            if (GameScr.vsSkill != GameScr.vcSkill)
                            {
                                GameScr.isLoadAllData = false;
                                Service.gI().updateSkill();
                            }
                            else
                            {
                                try
                                {
                                    if (!GameScr.isLoadAllData)
                                    {
                                        DataInputStream dataInputStream2 = new DataInputStream(Rms.loadRMS("NRskill"));
                                        createSkill(dataInputStream2.r);
                                    }
                                    LoginScr.isUpdateSkill = false;
                                }
                                catch (Exception)
                                {
                                    GameScr.vcSkill = -1;
                                    Service.gI().updateSkill();
                                }
                            }
                            if (GameScr.vsItem != GameScr.vcItem)
                            {
                                GameScr.isLoadAllData = false;
                                Service.gI().updateItem();
                            }
                            else
                            {
                                try
                                {
                                    DataInputStream dataInputStream3 = new DataInputStream(Rms.loadRMS("NRitem0"));
                                    loadItemNew(dataInputStream3.r, 0, false);
                                    DataInputStream dataInputStream4 = new DataInputStream(Rms.loadRMS("NRitem1"));
                                    loadItemNew(dataInputStream4.r, 1, false);
                                    DataInputStream dataInputStream5 = new DataInputStream(Rms.loadRMS("NRitem2"));
                                    loadItemNew(dataInputStream5.r, 2, false);
                                    DataInputStream dataInputStream6 = new DataInputStream(Rms.loadRMS("NRitem100"));
                                    loadItemNew(dataInputStream6.r, 100, false);
                                    LoginScr.isUpdateItem = false;
                                }
                                catch (Exception)
                                {
                                    GameScr.vcItem = -1;
                                    Service.gI().updateItem();
                                }
                                try
                                {
                                    DataInputStream dataInputStream7 = new DataInputStream(Rms.loadRMS("NRitem101"));
                                    loadItemNew(dataInputStream7.r, 101, false);
                                }
                                catch (Exception)
                                {
                                }
                            }
                            if (GameScr.vsData == GameScr.vcData && GameScr.vsMap == GameScr.vcMap && GameScr.vsSkill == GameScr.vcSkill && GameScr.vsItem == GameScr.vcItem)
                            {
                                if (!GameScr.isLoadAllData)
                                {
                                    GameScr.gI().readDart();
                                    GameScr.gI().readEfect();
                                    GameScr.gI().readArrow();
                                    GameScr.gI().readSkill();
                                }
                                Service.gI().clientOk();
                            }
                            sbyte b4 = msg.reader().readByte();
                            Res.outz("CAPTION LENT= " + b4);
                            GameScr.exps = new long[b4];
                            for (int j = 0; j < GameScr.exps.Length; j++)
                            {
                                GameScr.exps[j] = msg.reader().readLong();
                            }
                            break;
                        }
                    case 6:
                        {
                            Res.outz("GET UPDATE_MAP " + msg.reader().available() + " bytes");
                            msg.reader().mark(100000);
                            createMap(msg.reader());
                            msg.reader().reset();
                            sbyte[] data3 = new sbyte[msg.reader().available()];
                            msg.reader().readFully(ref data3);
                            Rms.saveRMS("NRmap", data3);
                            sbyte[] data4 = new sbyte[1] { GameScr.vcMap };
                            Rms.saveRMS("NRmapVersion", data4);
                            LoginScr.isUpdateMap = false;
                            if (GameScr.vsData == GameScr.vcData && GameScr.vsMap == GameScr.vcMap && GameScr.vsSkill == GameScr.vcSkill && GameScr.vsItem == GameScr.vcItem)
                            {
                                GameScr.gI().readDart();
                                GameScr.gI().readEfect();
                                GameScr.gI().readArrow();
                                GameScr.gI().readSkill();
                                Service.gI().clientOk();
                            }
                            break;
                        }
                    case 7:
                        {
                            Res.outz("GET UPDATE_SKILL " + msg.reader().available() + " bytes");
                            msg.reader().mark(100000);
                            createSkill(msg.reader());
                            msg.reader().reset();
                            sbyte[] data = new sbyte[msg.reader().available()];
                            msg.reader().readFully(ref data);
                            Rms.saveRMS("NRskill", data);
                            sbyte[] data2 = new sbyte[1] { GameScr.vcSkill };
                            Rms.saveRMS("NRskillVersion", data2);
                            LoginScr.isUpdateSkill = false;
                            if (GameScr.vsData == GameScr.vcData && GameScr.vsMap == GameScr.vcMap && GameScr.vsSkill == GameScr.vcSkill && GameScr.vsItem == GameScr.vcItem)
                            {
                                GameScr.gI().readDart();
                                GameScr.gI().readEfect();
                                GameScr.gI().readArrow();
                                GameScr.gI().readSkill();
                                Service.gI().clientOk();
                            }
                            break;
                        }
                    case 8:
                        Res.outz("GET UPDATE_ITEM " + msg.reader().available() + " bytes");
                        createItemNew(msg.reader());
                        break;
                    case 10:
                        // Doc mau vao bien tam truoc, CHUA dung vao TileMap.
                        int tmwMau = 0;
                        int tmhMau = 0;
                        int[] mapsMau = null;
                        try
                        {
                            Res.outz("REQUEST MAP TEMPLATE");
                            GameCanvas.debug("SA99", 2);
                            tmwMau = msg.reader().readByte();
                            tmhMau = msg.reader().readByte();
                            mapsMau = new int[tmwMau * tmhMau];
                            Res.err("   M apsize= " + tmwMau * tmhMau);
                            for (int i = 0; i < mapsMau.Length; i++)
                            {
                                int num = msg.reader().readByte();
                                if (num < 0)
                                {
                                    num += 256;
                                }
                                mapsMau[i] = (ushort)num;
                            }
                        }
                        catch (Exception ex2)
                        {
                            mapsMau = null;
                            Res.err("2 LOI TAI CASE REQUEST_MAPTEMPLATE " + ex2.ToString());
                        }

                        // Mau cua mot luot doi ban do DA BI THAY (co goi -24 moi hon
                        // toi trong luc cho), hoac khong con goi -24 nao dang cho:
                        // BO, khong dung vao ban do dang hien.
                        //
                        // Ban cu ghi thang vao TileMap.maps ngay dau nhanh nay. Mau cu
                        // toi sau ban do moi la ban do dang dung bi thay dia hinh cua
                        // ban do khac — dung canh "dung capsule xong map vo": ten ban
                        // do moi, nen moi, ma dia hinh xep lung tung.
                        if (mauCanBo > 0 || messWait == null)
                        {
                            if (mauCanBo > 0)
                            {
                                mauCanBo--;
                            }
                            Res.err("REQUEST_MAPTEMPLATE: bo mau cua luot doi ban do cu");
                            break;
                        }
                        Char.isLoadingMap = true;
                        GameCanvas.isLoading = true;
                        if (mapsMau != null)
                        {
                            TileMap.maps = null;
                            TileMap.types = null;
                            mSystem.gcc();
                            TileMap.tmw = tmwMau;
                            TileMap.tmh = tmhMau;
                            TileMap.maps = mapsMau;
                            TileMap.types = new int[mapsMau.Length];
                        }

                        // Áp gói -24 đang chờ, rồi DÙ THẾ NÀO cũng phải vào màn.
                        //
                        // Đây là chỗ sinh ra cảnh "sang map thì địa hình vỡ, máu
                        // 0/0, nhân vật không hiện". Đường đi như sau:
                        //
                        //   Gói -24 tới, client GHI ĐÈ ngay TileMap.mapID,
                        //   tileID, bgID, mapName... rồi mới gọi
                        //   loadMapFromResource(). Hàm đó ném lỗi (chưa có mẫu
                        //   bản đồ trong máy) thì client xin mẫu và CẤT gói -24
                        //   lại ở messWait. Tới đây mẫu về, và nhánh này phải áp
                        //   nốt gói đã cất.
                        //
                        // Bản cũ hỏng ở hai chỗ, cả hai đều nằm NGOÀI try:
                        //
                        //   msg = messWait;   -> messWait null (mẫu về mà không
                        //   có gói nào đang chờ) thì loadInfoMap(null) ném lỗi,
                        //   bị catch nuốt, rồi msg.cleanup() ném NullReference
                        //   LẦN NỮA — lần này không ai bắt. Cả onMessage văng ra,
                        //   nên switchToMe() KHÔNG BAO GIỜ chạy.
                        //
                        //   Kết quả: tên bản đồ, tileID, bgID đã là của bản đồ
                        //   mới, còn địa hình, quái và chỉ số nhân vật thì vẫn
                        //   chưa nạp. Đúng cái ảnh chụp: khung máu 0/0 và nền
                        //   vẽ dở.
                        //
                        //   Ngoài ra msg được gán bằng messWait rồi dọn hai lần
                        //   cùng một đối tượng, còn gói mẫu bản đồ vừa nhận thì
                        //   không ai dọn.
                        try
                        {
                            if (messWait != null)
                            {
                                apDungDinhDanhBanDo(TileMap.mapID, TileMap.zoneID);
                                loadInfoMap(messWait);
                                try
                                {
                                    sbyte b2 = messWait.reader().readByte();
                                    TileMap.isMapDouble = ((b2 != 0) ? true : false);
                                }
                                catch (Exception ex)
                                {
                                    Res.err(" 1 LOI TAI CASE REQUEST_MAPTEMPLATE " + ex.ToString());
                                }
                            }
                            else
                            {
                                Res.err("REQUEST_MAPTEMPLATE: mau ban do ve ma "
                                        + "khong co goi -24 nao dang cho");
                            }
                        }
                        catch (Exception ex3)
                        {
                            Res.err("3 LOI TAI CASE REQUEST_MAPTEMPLATE " + ex3.ToString());
                        }

                        // Dọn hai gói KHÁC NHAU, mỗi gói một lần, và chịu được null.
                        if (msg != null)
                        {
                            msg.cleanup();
                        }
                        if (messWait != null)
                        {
                            messWait.cleanup();
                        }
                        msg = (messWait = null);

                        // switchToMe() nằm ngoài mọi try nên luôn chạy: kẹt ở
                        // trạng thái nửa vời còn tệ hơn vào màn với dữ liệu
                        // thiếu, vì người chơi ít nhất còn đi được ra chỗ khác.
                        GameScr.gI().switchToMe();
                        break;
                    case 12:
                        GameCanvas.debug("SA10", 2);
                        break;
                    case 9:
                        GameCanvas.debug("SA11", 2);
                        break;
                }
            }
            catch (Exception)
            {
                Cout.LogError("LOI TAI messageNotMap + " + msg.command);
            }
            finally
            {
                if (msg != null)
                {
                    msg.cleanup();
                }
            }
        }
    
        public void messageNotLogin(Message msg)
        {
            try
            {
                sbyte b = msg.reader().readByte();
                Res.outz("---messageNotLogin : " + b);
                if (b != 2)
                {
                    return;
                }
                string linkDefault = msg.reader().readUTF();
                ServerListScreen.linkDefault = linkDefault;
                mSystem.AddIpTest();
                ServerListScreen.getServerList(ServerListScreen.linkDefault);
                try
                {
                    sbyte b2 = msg.reader().readByte();
                    Panel.CanNapTien = b2 == 1;
                }
                catch (Exception)
                {
                }
            }
            catch (Exception)
            {
            }
            finally
            {
                if (msg != null)
                {
                    msg.cleanup();
                }
            }
        }
    
        public void messageSubCommand(Message msg)
        {
            try
            {
                GameCanvas.debug("SA12", 2);
                sbyte b = msg.reader().readByte();
                Res.outz("---messageSubCommand : " + b);
                switch (b)
                {
                    case 63:
                        {
                            sbyte b5 = msg.reader().readByte();
                            if (b5 > 0)
                            {
                                GameCanvas.panel.vPlayerMenu_id.removeAllElements();
                                InfoDlg.showWait();
                                MyVector vPlayerMenu = GameCanvas.panel.vPlayerMenu;
                                for (int j = 0; j < b5; j++)
                                {
                                    string caption = msg.reader().readUTF();
                                    string caption2 = msg.reader().readUTF();
                                    short num5 = msg.reader().readShort();
                                    GameCanvas.panel.vPlayerMenu_id.addElement(num5 + string.Empty);
                                    Char.myCharz().charFocus.menuSelect = num5;
                                    Command command = new Command(caption, 11115, Char.myCharz().charFocus);
                                    command.caption2 = caption2;
                                    vPlayerMenu.addElement(command);
                                }
                                InfoDlg.hide();
                                GameCanvas.panel.setTabPlayerMenu();
                            }
                            break;
                        }
                    case 1:
                        GameCanvas.debug("SA13", 2);
                        Char.myCharz().nClass = GameScr.nClasss[msg.reader().readByte()];
                        Char.myCharz().cTiemNang = msg.reader().readLong();
                        Char.myCharz().vSkill.removeAllElements();
                        Char.myCharz().vSkillFight.removeAllElements();
                        Char.myCharz().myskill = null;
                        break;
                    case 2:
                        {
                            GameCanvas.debug("SA14", 2);
                            if (Char.myCharz().statusMe != 14 && Char.myCharz().statusMe != 5)
                            {
                                Char.myCharz().cHP = Char.myCharz().cHPFull;
                                Char.myCharz().cMP = Char.myCharz().cMPFull;
                                Cout.LogError2(" ME_LOAD_SKILL");
                            }
                            Char.myCharz().vSkill.removeAllElements();
                            Char.myCharz().vSkillFight.removeAllElements();
                            sbyte b2 = msg.reader().readByte();
                            for (sbyte b3 = 0; b3 < b2; b3++)
                            {
                                short skillId = msg.reader().readShort();
                                Skill skill2 = Skills.get(skillId);
                                useSkill(skill2);
                            }
                            GameScr.gI().sortSkill();
                            if (GameScr.isPaintInfoMe)
                            {
                                GameScr.indexRow = -1;
                                GameScr.gI().left = (GameScr.gI().center = null);
                            }
                            break;
                        }
                    case 19:
                        GameCanvas.debug("SA17", 2);
                        Char.myCharz().boxSort();
                        break;
                    case 21:
                        {
                            GameCanvas.debug("SA19", 2);
                            int num3 = msg.reader().readInt();
                            Char.myCharz().xuInBox -= num3;
                            Char.myCharz().xu += num3;
                            Char.myCharz().xuStr = mSystem.numberTostring(Char.myCharz().xu);
                            break;
                        }
                    case 0:
                        {
                            GameCanvas.debug("SA21", 2);
                            RadarScr.list = new MyVector();
                            Teleport.vTeleport.removeAllElements();
                            GameScr.vCharInMap.removeAllElements();
                            GameScr.vItemMap.removeAllElements();
                            Char.vItemTime.removeAllElements();
                            GameScr.loadImg();
                            GameScr.currentCharViewInfo = Char.myCharz();
                            Char.myCharz().charID = msg.reader().readInt();
                            Char.myCharz().ctaskId = msg.reader().readByte();
                            Char.myCharz().cgender = msg.reader().readByte();
                            Char.myCharz().head = msg.reader().readShort();
                            Char.myCharz().cName = msg.reader().readUTF();
                            Char.myCharz().cPk = msg.reader().readByte();
                            Char.myCharz().cTypePk = msg.reader().readByte();
                            Char.myCharz().cPower = msg.reader().readLong();
                            Char.myCharz().applyCharLevelPercent();
                            Char.myCharz().eff5BuffHp = msg.reader().readShort();
                            Char.myCharz().eff5BuffMp = msg.reader().readShort();
                            Char.myCharz().nClass = GameScr.nClasss[msg.reader().readByte()];
                            Char.myCharz().vSkill.removeAllElements();
                            Char.myCharz().vSkillFight.removeAllElements();
                            GameScr.gI().dHP = Char.myCharz().cHP;
                            GameScr.gI().dMP = Char.myCharz().cMP;
                            sbyte b2 = msg.reader().readByte();
                            for (sbyte b6 = 0; b6 < b2; b6++)
                            {
                                Skill skill3 = Skills.get(msg.reader().readShort());
                                useSkill(skill3);
                            }
                            GameScr.gI().sortSkill();
                            GameScr.gI().loadSkillShortcut();
                            Char.myCharz().xu = msg.reader().readLong();
                            Char.myCharz().luongKhoa = msg.reader().readInt();
                            Char.myCharz().luong = msg.reader().readInt();
                            Char.myCharz().xuStr = Res.formatNumber(Char.myCharz().xu);
                            Char.myCharz().luongStr = mSystem.numberTostring(Char.myCharz().luong);
                            Char.myCharz().luongKhoaStr = mSystem.numberTostring(Char.myCharz().luongKhoa);
                            Char.myCharz().arrItemBody = new Item[msg.reader().readByte()];
                            try
                            {
                                Char.myCharz().setDefaultPart();
                                for (int k = 0; k < Char.myCharz().arrItemBody.Length; k++)
                                {
                                    short num6 = msg.reader().readShort();
                                    if (num6 == -1)
                                    {
                                        continue;
                                    }
                                    ItemTemplate itemTemplate = ItemTemplates.get(num6);
                                    int num7 = itemTemplate.type;
                                    Char.myCharz().arrItemBody[k] = new Item();
                                    Char.myCharz().arrItemBody[k].template = itemTemplate;
                                    Char.myCharz().arrItemBody[k].quantity = msg.reader().readInt();
                                    Char.myCharz().arrItemBody[k].info = msg.reader().readUTF();
                                    Char.myCharz().arrItemBody[k].content = msg.reader().readUTF();
                                    int num8 = msg.reader().readUnsignedByte();
                                    if (num8 != 0)
                                    {
                                        Char.myCharz().arrItemBody[k].itemOption = new ItemOption[num8];
                                        for (int l = 0; l < Char.myCharz().arrItemBody[k].itemOption.Length; l++)
                                        {
                                            int num9 = msg.reader().readInt();
                                            int param = msg.reader().readInt();
                                            if (num9 != -1)
                                            {
                                                Char.myCharz().arrItemBody[k].itemOption[l] = new ItemOption(num9, param);
                                            }
                                        }
                                    }
                                    switch (num7)
                                    {
                                        case 0:
                                            Res.outz("toi day =======================================" + Char.myCharz().body);
                                            Char.myCharz().body = Char.myCharz().arrItemBody[k].template.part;
                                            break;
                                        case 1:
                                            Char.myCharz().leg = Char.myCharz().arrItemBody[k].template.part;
                                            Res.outz("toi day =======================================" + Char.myCharz().leg);
                                            break;
                                    }
                                }
                            }
                            catch (Exception)
                            {
                            }
                            Char.myCharz().arrItemBag = new Item[msg.reader().readByte()];
                            GameScr.hpPotion = 0;
                            GameScr.isudungCapsun4 = false;
                            GameScr.isudungCapsun3 = false;
                            for (int m = 0; m < Char.myCharz().arrItemBag.Length; m++)
                            {
                                short num10 = msg.reader().readShort();
                                if (num10 == -1)
                                {
                                    continue;
                                }
                                Char.myCharz().arrItemBag[m] = new Item();
                                Char.myCharz().arrItemBag[m].template = ItemTemplates.get(num10);
                                Char.myCharz().arrItemBag[m].quantity = msg.reader().readInt();
                                Char.myCharz().arrItemBag[m].info = msg.reader().readUTF();
                                Char.myCharz().arrItemBag[m].content = msg.reader().readUTF();
                                Char.myCharz().arrItemBag[m].indexUI = m;
                                sbyte b7 = msg.reader().readByte();
                                if (b7 != 0)
                                {
                                    Char.myCharz().arrItemBag[m].itemOption = new ItemOption[b7];
                                    for (int n = 0; n < Char.myCharz().arrItemBag[m].itemOption.Length; n++)
                                    {
                                        int num11 = msg.reader().readInt();
                                        int param2 = msg.reader().readInt();
                                        if (num11 != -1)
                                        {
                                            Char.myCharz().arrItemBag[m].itemOption[n] = new ItemOption(num11, param2);
                                            Char.myCharz().arrItemBag[m].getCompare();
                                        }
                                    }
                                }
                                // Mau la thi bo o do — xem chu thich cung viec o
                                // nhanh doc ca hanh trang (goi -36).
                                if (Char.myCharz().arrItemBag[m].template == null)
                                {
                                    Char.myCharz().arrItemBag[m] = null;
                                    continue;
                                }
                                if (Char.myCharz().arrItemBag[m].template.type == 6)
                                {
                                    GameScr.hpPotion += Char.myCharz().arrItemBag[m].quantity;
                                }
                                switch (num10)
                                {
                                    case 194:
                                        GameScr.isudungCapsun4 = Char.myCharz().arrItemBag[m].quantity > 0;
                                        break;
                                    case 193:
                                        if (!GameScr.isudungCapsun4)
                                        {
                                            GameScr.isudungCapsun3 = Char.myCharz().arrItemBag[m].quantity > 0;
                                        }
                                        break;
                                }
                            }
                            Char.myCharz().arrItemBox = new Item[msg.reader().readByte()];
                            GameCanvas.panel.hasUse = 0;
                            for (int num12 = 0; num12 < Char.myCharz().arrItemBox.Length; num12++)
                            {
                                short num13 = msg.reader().readShort();
                                if (num13 == -1)
                                {
                                    continue;
                                }
                                Char.myCharz().arrItemBox[num12] = new Item();
                                Char.myCharz().arrItemBox[num12].template = ItemTemplates.get(num13);
                                Char.myCharz().arrItemBox[num12].quantity = msg.reader().readInt();
                                Char.myCharz().arrItemBox[num12].info = msg.reader().readUTF();
                                Char.myCharz().arrItemBox[num12].content = msg.reader().readUTF();
                                Char.myCharz().arrItemBox[num12].itemOption = new ItemOption[msg.reader().readByte()];
                                for (int num14 = 0; num14 < Char.myCharz().arrItemBox[num12].itemOption.Length; num14++)
                                {
                                    int num15 = msg.reader().readInt();
                                    int param3 = msg.reader().readInt();
                                    if (num15 != -1)
                                    {
                                        Char.myCharz().arrItemBox[num12].itemOption[num14] = new ItemOption(num15, param3);
                                        Char.myCharz().arrItemBox[num12].getCompare();
                                    }
                                }
                                GameCanvas.panel.hasUse++;
                            }
                            Char.myCharz().statusMe = 4;
                            int num16 = Rms.loadRMSInt(Char.myCharz().cName + "vci");
                            if (num16 < 1)
                            {
                                GameScr.isViewClanInvite = false;
                            }
                            else
                            {
                                GameScr.isViewClanInvite = true;
                            }
                            short num17 = msg.reader().readShort();
                            Char.idHead = new short[num17];
                            Char.idAvatar = new short[num17];
                            for (int num18 = 0; num18 < num17; num18++)
                            {
                                Char.idHead[num18] = msg.reader().readShort();
                                Char.idAvatar[num18] = msg.reader().readShort();
                            }
                            for (int num19 = 0; num19 < GameScr.info1.charId.Length; num19++)
                            {
                                GameScr.info1.charId[num19] = new int[3];
                            }
                            GameScr.info1.charId[Char.myCharz().cgender][0] = msg.reader().readShort();
                            GameScr.info1.charId[Char.myCharz().cgender][1] = msg.reader().readShort();
                            GameScr.info1.charId[Char.myCharz().cgender][2] = msg.reader().readShort();
                            Char.myCharz().isNhapThe = msg.reader().readByte() == 1;
                            Res.outz("NHAP THE= " + Char.myCharz().isNhapThe);
                            GameScr.deltaTime = mSystem.currentTimeMillis() - (long)msg.reader().readInt() * 1000L;
                            GameScr.isNewMember = msg.reader().readByte();
                            Service.gI().updateCaption((sbyte)Char.myCharz().cgender);
                            Service.gI().androidPack();
                            try
                            {
                                Char.myCharz().idAuraEff = msg.reader().readShort();
                                Char.myCharz().idEff_Set_Item = msg.reader().readSByte();
                                Char.myCharz().idHat = msg.reader().readShort();
                                break;
                            }
                            catch (Exception)
                            {
                                break;
                            }
                        }
                    case 4:
                        GameCanvas.debug("SA23", 2);
                        Char.myCharz().xu = msg.reader().readLong();
                        Char.myCharz().luong = msg.reader().readInt();
                        Char.myCharz().cHP = msg.readHp();
                        Char.myCharz().cMP = msg.readInt3Byte();
                        Char.myCharz().luongKhoa = msg.reader().readInt();
                        Char.myCharz().xuStr = Res.formatNumber2(Char.myCharz().xu);
                        Char.myCharz().luongStr = mSystem.numberTostring(Char.myCharz().luong);
                        Char.myCharz().luongKhoaStr = mSystem.numberTostring(Char.myCharz().luongKhoa);
                        break;
                    case 5:
                        {
                            GameCanvas.debug("SA24", 2);
                            double cHP = Char.myCharz().cHP;
                            Char.myCharz().cHP = msg.readHp();
                            if (Char.myCharz().cHP > cHP && Char.myCharz().cTypePk != 4)
                            {
                                GameScr.startFlyText("+" + (Char.myCharz().cHP - cHP) + " " + mResources.HP, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 20, 0, -1, mFont.HP);
                                SoundMn.gI().HP_MPup();
                                if (Char.myCharz().petFollow != null && Char.myCharz().petFollow.smallID == 5003)
                                {
                                    MonsterDart.addMonsterDart(Char.myCharz().petFollow.cmx + ((Char.myCharz().petFollow.dir != 1) ? (-10) : 10), Char.myCharz().petFollow.cmy + 10, true, -1, -1, Char.myCharz(), 29);
                                }
                            }
                            if (Char.myCharz().cHP < cHP)
                            {
                                GameScr.startFlyText("-" + (cHP - Char.myCharz().cHP) + " " + mResources.HP, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 20, 0, -1, mFont.HP);
                            }
                            GameScr.gI().dHP = Char.myCharz().cHP;
                            if (GameScr.isPaintInfoMe)
                            {
                            }
                            break;
                        }
                    case 6:
                        {
                            GameCanvas.debug("SA25", 2);
                            if (Char.myCharz().statusMe == 14 || Char.myCharz().statusMe == 5)
                            {
                                break;
                            }
                            int cMP = Char.myCharz().cMP;
                            Char.myCharz().cMP = msg.readInt3Byte();
                            if (Char.myCharz().cMP > cMP)
                            {
                                GameScr.startFlyText("+" + (Char.myCharz().cMP - cMP) + " " + mResources.KI, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 23, 0, -2, mFont.MP);
                                SoundMn.gI().HP_MPup();
                                if (Char.myCharz().petFollow != null && Char.myCharz().petFollow.smallID == 5001)
                                {
                                    MonsterDart.addMonsterDart(Char.myCharz().petFollow.cmx + ((Char.myCharz().petFollow.dir != 1) ? (-10) : 10), Char.myCharz().petFollow.cmy + 10, true, -1, -1, Char.myCharz(), 29);
                                }
                            }
                            if (Char.myCharz().cMP < cMP)
                            {
                                GameScr.startFlyText("-" + (cMP - Char.myCharz().cMP) + " " + mResources.KI, Char.myCharz().cx, Char.myCharz().cy - Char.myCharz().ch - 23, 0, -2, mFont.MP);
                            }
                            Res.outz("curr MP= " + Char.myCharz().cMP);
                            GameScr.gI().dMP = Char.myCharz().cMP;
                            if (GameScr.isPaintInfoMe)
                            {
                            }
                            break;
                        }
                    case 7:
                        {
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char == null)
                            {
                                break;
                            }
                            @char.clanID = msg.reader().readInt();
                            if (@char.clanID == -2)
                            {
                                @char.isCopy = true;
                            }
                            readCharInfo(@char, msg);
                            try
                            {
                                @char.idAuraEff = msg.reader().readShort();
                                @char.idEff_Set_Item = msg.reader().readSByte();
                                @char.idHat = msg.reader().readShort();
                                if (@char.bag >= 201)
                                {
                                    Effect effect = new Effect(@char.bag, @char, 2, -1, 10, 1);
                                    effect.typeEff = 5;
                                    @char.addEffChar(effect);
                                }
                                else
                                {
                                    @char.removeEffChar(0, 201);
                                }
                                break;
                            }
                            catch (Exception)
                            {
                                break;
                            }
                        }
                    case 8:
                        {
                            GameCanvas.debug("SA26", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cspeed = msg.reader().readByte();
                            }
                            break;
                        }
                    case 9:
                        {
                            GameCanvas.debug("SA27", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cHP = msg.readHp();
                                @char.cHPFull = msg.readHp();
                            }
                            break;
                        }
                    case 10:
                        {
                            GameCanvas.debug("SA28", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cHP = msg.readInt3Byte();
                                @char.cHPFull = msg.readInt3Byte();
                                @char.eff5BuffHp = msg.reader().readShort();
                                @char.eff5BuffMp = msg.reader().readShort();
                                @char.wp = msg.reader().readShort();
                                if (@char.wp == -1)
                                {
                                    @char.setDefaultWeapon();
                                }
                            }
                            break;
                        }
                    case 11:
                        {
                            GameCanvas.debug("SA29", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cHP = msg.readInt3Byte();
                                @char.cHPFull = msg.readInt3Byte();
                                @char.eff5BuffHp = msg.reader().readShort();
                                @char.eff5BuffMp = msg.reader().readShort();
                                @char.body = msg.reader().readShort();
                                if (@char.body == -1)
                                {
                                    @char.setDefaultBody();
                                }
                            }
                            break;
                        }
                    case 12:
                        {
                            GameCanvas.debug("SA30", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cHP = msg.readInt3Byte();
                                @char.cHPFull = msg.readInt3Byte();
                                @char.eff5BuffHp = msg.reader().readShort();
                                @char.eff5BuffMp = msg.reader().readShort();
                                @char.leg = msg.reader().readShort();
                                if (@char.leg == -1)
                                {
                                    @char.setDefaultLeg();
                                }
                            }
                            break;
                        }
                    case 13:
                        {
                            GameCanvas.debug("SA31", 2);
                            int num2 = msg.reader().readInt();
                            Char @char = ((num2 != Char.myCharz().charID) ? GameScr.findCharInMap(num2) : Char.myCharz());
                            if (@char != null)
                            {
                                @char.cHP = msg.readInt3Byte();
                                @char.cHPFull = msg.readInt3Byte();
                                @char.eff5BuffHp = msg.reader().readShort();
                                @char.eff5BuffMp = msg.reader().readShort();
                            }
                            break;
                        }
                    case 14:
                        {
                            GameCanvas.debug("SA32", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char == null)
                            {
                                break;
                            }
                            @char.cHP = msg.readHp();
                            sbyte b4 = msg.reader().readByte();
                            Res.outz("player load hp type= " + b4);
                            if (b4 == 1)
                            {
                                ServerEffect.addServerEffect(11, @char, 5);
                                ServerEffect.addServerEffect(104, @char, 4);
                            }
                            if (b4 == 2)
                            {
                                @char.doInjure();
                            }
                            try
                            {
                                @char.cHPFull = msg.readHp();
                                break;
                            }
                            catch (Exception)
                            {
                                break;
                            }
                        }
                    case 15:
                        {
                            GameCanvas.debug("SA33", 2);
                            Char @char = GameScr.findCharInMap(msg.reader().readInt());
                            if (@char != null)
                            {
                                @char.cHP = msg.readHp();
                                @char.cHPFull = msg.readHp();
                                @char.cx = msg.reader().readShort();
                                @char.cy = msg.reader().readShort();
                                @char.statusMe = 1;
                                @char.cp3 = 3;
                                ServerEffect.addServerEffect(109, @char, 2);
                            }
                            break;
                        }
                    case 35:
                        {
                            GameCanvas.debug("SY3", 2);
                            int num4 = msg.reader().readInt();
                            Res.outz("CID = " + num4);
                            if (TileMap.mapID == 130)
                            {
                                GameScr.gI().starVS();
                            }
                            if (num4 == Char.myCharz().charID)
                            {
                                Char.myCharz().cTypePk = msg.reader().readByte();
                                if (GameScr.gI().isVS() && Char.myCharz().cTypePk != 0)
                                {
                                    GameScr.gI().starVS();
                                }
                                Res.outz("type pk= " + Char.myCharz().cTypePk);
                                Char.myCharz().npcFocus = null;
                                if (!GameScr.gI().isMeCanAttackMob(Char.myCharz().mobFocus))
                                {
                                    Char.myCharz().mobFocus = null;
                                }
                                Char.myCharz().itemFocus = null;
                            }
                            else
                            {
                                Char @char = GameScr.findCharInMap(num4);
                                if (@char != null)
                                {
                                    Res.outz("type pk= " + @char.cTypePk);
                                    @char.cTypePk = msg.reader().readByte();
                                    if (@char.isAttacPlayerStatus())
                                    {
                                        Char.myCharz().charFocus = @char;
                                    }
                                }
                            }
                            for (int i = 0; i < GameScr.vCharInMap.size(); i++)
                            {
                                Char char2 = GameScr.findCharInMap(i);
                                if (char2 != null && char2.cTypePk != 0 && char2.cTypePk == Char.myCharz().cTypePk)
                                {
                                    if (!Char.myCharz().mobFocus.isMobMe)
                                    {
                                        Char.myCharz().mobFocus = null;
                                    }
                                    Char.myCharz().npcFocus = null;
                                    Char.myCharz().itemFocus = null;
                                    break;
                                }
                            }
                            Res.outz("update type pk= ");
                            break;
                        }
                    case 61:
                        {
                            string text = msg.reader().readUTF();
                            sbyte[] data = new sbyte[msg.reader().readInt()];
                            msg.reader().read(ref data);
                            if (data.Length == 0)
                            {
                                data = null;
                            }
                            if (text.Equals("KSkill"))
                            {
                                GameScr.gI().onKSkill(data);
                            }
                            else if (text.Equals("OSkill"))
                            {
                                GameScr.gI().onOSkill(data);
                            }
                            else if (text.Equals("CSkill"))
                            {
                                GameScr.gI().onCSkill(data);
                            }
                            break;
                        }
                    case 23:
                        {
                            short num = msg.reader().readShort();
                            Skill skill = Skills.get(num);
                            useSkill(skill);
                            if (num != 0 && num != 14 && num != 28)
                            {
                                GameScr.info1.addInfo(mResources.LEARN_SKILL + " " + skill.template.name, 0);
                            }
                            break;
                        }
                    case 62:
                        Res.outz("ME UPDATE SKILL");
                        read_UpdateSkill(msg);
                        break;
                }
            }
            catch (Exception ex5)
            {
                Cout.println("Loi tai Sub : " + ex5.ToString());
            }
            finally
            {
                if (msg != null)
                {
                    msg.cleanup();
                }
            }
        }
    
        private void useSkill(Skill skill)
        {
            if (Char.myCharz().myskill == null)
            {
                Char.myCharz().myskill = skill;
            }
            else if (skill.template.Equals(Char.myCharz().myskill.template))
            {
                Char.myCharz().myskill = skill;
            }
            Char.myCharz().vSkill.addElement(skill);
            if ((skill.template.type == 1 || skill.template.type == 4 || skill.template.type == 2 || skill.template.type == 3) && (skill.template.maxPoint == 0 || (skill.template.maxPoint > 0 && skill.point > 0)))
            {
                if (skill.template.id == Char.myCharz().skillTemplateId)
                {
                    Service.gI().selectSkill(Char.myCharz().skillTemplateId);
                }
                Char.myCharz().vSkillFight.addElement(skill);
            }
        }
    
        public bool readCharInfo(Char c, Message msg)
        {
            try
            {
                c.clevel = msg.reader().readByte();
                c.isInvisiblez = msg.reader().readBoolean();
                c.cTypePk = msg.reader().readByte();
                Res.outz("ADD TYPE PK= " + c.cTypePk + " to player " + c.charID + " @@ " + c.cName);
                c.nClass = GameScr.nClasss[msg.reader().readByte()];
                c.cgender = msg.reader().readByte();
                c.head = msg.reader().readShort();
                c.cName = msg.reader().readUTF();
                c.cHP = msg.readHp();
                c.dHP = c.cHP;
                if (c.cHP == 0)
                {
                    c.statusMe = 14;
                }
                c.cHPFull = msg.readHp();
                if (c.cy >= TileMap.pxh - 100)
                {
                    c.isFlyUp = true;
                }
                c.body = msg.reader().readShort();
                c.leg = msg.reader().readShort();
                c.bag = msg.reader().readUnsignedByte();
                Res.outz(" body= " + c.body + " leg= " + c.leg + " bag=" + c.bag + "BAG ==" + c.bag + "*********************************");
                c.isShadown = true;
                sbyte b = msg.reader().readByte();
                if (c.wp == -1)
                {
                    c.setDefaultWeapon();
                }
                if (c.body == -1)
                {
                    c.setDefaultBody();
                }
                if (c.leg == -1)
                {
                    c.setDefaultLeg();
                }
                c.cx = msg.reader().readShort();
                c.cy = msg.reader().readShort();
                c.xSd = c.cx;
                c.ySd = c.cy;
                c.eff5BuffHp = msg.reader().readShort();
                c.eff5BuffMp = msg.reader().readShort();
                int num = msg.reader().readByte();
                for (int i = 0; i < num; i++)
                {
                    EffectChar effectChar = new EffectChar(msg.reader().readByte(), msg.reader().readInt(), msg.reader().readInt(), msg.reader().readShort());
                    c.vEff.addElement(effectChar);
                    if (effectChar.template.type == 12 || effectChar.template.type == 11)
                    {
                        c.isInvisiblez = true;
                    }
                }
                return true;
            }
            catch (Exception ex)
            {
                ex.StackTrace.ToString();
            }
            return false;
        }
    
        private void readGetImgByName(Message msg)
        {
            try
            {
                string name = msg.reader().readUTF();
                sbyte nFrame = msg.reader().readByte();
                sbyte[] array = null;
                array = NinjaUtil.readByteArray(msg);
                Image img = createImage(array);
                ImgByName.SetImage(name, img, nFrame);
                if (array == null)
                {
                }
            }
            catch (Exception)
            {
            }
        }
    
        private void createItemNew(myReader d)
        {
            try
            {
                loadItemNew(d, -1, true);
            }
            catch (Exception)
            {
            }
        }
    
        private void loadItemNew(myReader d, sbyte type, bool isSave)
        {
            try
            {
                d.mark(100000);
                GameScr.vcItem = d.readByte();
                type = d.readByte();
                if (type == 0)
                {
                    // Server ghi so luong bang writeInt (4 byte). Doc 1 byte se ra 0 khi bang
                    // chi so co hon 255 dong (hien tai 322) -> mang rong -> moi item tham chieu
                    // option deu vo -> hanh trang / nhiem vu hien trong tron.
                    GameScr.gI().iOptionTemplates = new ItemOptionTemplate[d.readInt()];
                    for (int i = 0; i < GameScr.gI().iOptionTemplates.Length; i++)
                    {
                        GameScr.gI().iOptionTemplates[i] = new ItemOptionTemplate();
                        GameScr.gI().iOptionTemplates[i].id = i;
                        GameScr.gI().iOptionTemplates[i].name = d.readUTF();
                        GameScr.gI().iOptionTemplates[i].type = d.readByte();
                    }
                    if (isSave)
                    {
                        d.reset();
                        sbyte[] data = new sbyte[d.available()];
                        d.readFully(ref data);
                        Rms.saveRMS("NRitem0", data);
                    }
                }
                else if (type == 1)
                {
                    ItemTemplates.itemTemplates.clear();
                    int num = d.readShort();
                    for (int j = 0; j < num; j++)
                    {
                        ItemTemplate it = new ItemTemplate((short)j, d.readByte(), d.readByte(), d.readUTF(), d.readUTF(), d.readByte(), d.readInt(), d.readShort(), d.readShort(), d.readBoolean());
                        ItemTemplates.add(it);
                    }
                    if (isSave)
                    {
                        d.reset();
                        sbyte[] data2 = new sbyte[d.available()];
                        d.readFully(ref data2);
                        Rms.saveRMS("NRitem1", data2);
                    }
                }
                else if (type == 2)
                {
                    int num2 = d.readShort();
                    int num3 = d.readShort();
                    for (int k = num2; k < num3; k++)
                    {
                        ItemTemplate it2 = new ItemTemplate((short)k, d.readByte(), d.readByte(), d.readUTF(), d.readUTF(), d.readByte(), d.readInt(), d.readShort(), d.readShort(), d.readBoolean());
                        ItemTemplates.add(it2);
                    }
                    if (isSave)
                    {
                        d.reset();
                        sbyte[] data3 = new sbyte[d.available()];
                        d.readFully(ref data3);
                        Rms.saveRMS("NRitem2", data3);
                        sbyte[] data4 = new sbyte[1] { GameScr.vcItem };
                        Rms.saveRMS("NRitemVersion", data4);
                        LoginScr.isUpdateItem = false;
                        if (GameScr.vsData == GameScr.vcData && GameScr.vsMap == GameScr.vcMap && GameScr.vsSkill == GameScr.vcSkill && GameScr.vsItem == GameScr.vcItem)
                        {
                            GameScr.gI().readDart();
                            GameScr.gI().readEfect();
                            GameScr.gI().readArrow();
                            GameScr.gI().readSkill();
                            Service.gI().clientOk();
                        }
                    }
                }
                else if (type == 100)
                {
                    Char.Arr_Head_2Fr = readArrHead(d);
                    if (isSave)
                    {
                        d.reset();
                        sbyte[] data5 = new sbyte[d.available()];
                        d.readFully(ref data5);
                        Rms.saveRMS("NRitem100", data5);
                    }
                }
                else
                {
                    if (type != 101)
                    {
                        return;
                    }
                    try
                    {
                        int num4 = d.readShort();
                        Char.Arr_Head_FlyMove = new short[num4];
                        for (int l = 0; l < num4; l++)
                        {
                            short num5 = d.readShort();
                            Char.Arr_Head_FlyMove[l] = num5;
                        }
                        if (isSave)
                        {
                            d.reset();
                            sbyte[] data6 = new sbyte[d.available()];
                            d.readFully(ref data6);
                            Rms.saveRMS("NRitem101", data6);
                        }
                        return;
                    }
                    catch (Exception)
                    {
                        Char.Arr_Head_FlyMove = new short[0];
                        return;
                    }
                }
            }
            catch (Exception ex2)
            {
                ex2.ToString();
            }
        }
    
        private void readFrameBoss(Message msg, int mobTemplateId)
        {
            try
            {
                int num = msg.reader().readByte();
                int[][] array = new int[num][];
                for (int i = 0; i < num; i++)
                {
                    int num2 = msg.reader().readByte();
                    array[i] = new int[num2];
                    for (int j = 0; j < num2; j++)
                    {
                        array[i][j] = msg.reader().readByte();
                    }
                }
                frameHT_NEWBOSS.put(mobTemplateId + string.Empty, array);
            }
            catch (Exception)
            {
            }
        }
    
        private int[][] readArrHead(myReader d)
        {
            int[][] array = new int[1][] { new int[2] { 542, 543 } };
            try
            {
                int num = d.readShort();
                array = new int[num][];
                for (int i = 0; i < array.Length; i++)
                {
                    int num2 = d.readByte();
                    array[i] = new int[num2];
                    for (int j = 0; j < num2; j++)
                    {
                        array[i][j] = d.readShort();
                    }
                }
            }
            catch (Exception)
            {
            }
            return array;
        }
    
        public void phuban_Info(Message msg)
        {
            try
            {
                sbyte b = msg.reader().readByte();
                if (b == 0)
                {
                    readPhuBan_CHIENTRUONGNAMEK(msg, b);
                }
            }
            catch (Exception)
            {
            }
        }
    
        private void readPhuBan_CHIENTRUONGNAMEK(Message msg, int type_PB)
        {
            try
            {
                sbyte b = msg.reader().readByte();
                if (b == 0)
                {
                    short idmapPaint = msg.reader().readShort();
                    string nameTeam = msg.reader().readUTF();
                    string nameTeam2 = msg.reader().readUTF();
                    int maxPoint = msg.reader().readInt();
                    short timeSecond = msg.reader().readShort();
                    int maxLife = msg.reader().readByte();
                    GameScr.phuban_Info = new InfoPhuBan(type_PB, idmapPaint, nameTeam, nameTeam2, maxPoint, timeSecond);
                    GameScr.phuban_Info.maxLife = maxLife;
                    GameScr.phuban_Info.updateLife(type_PB, 0, 0);
                }
                else if (b == 1)
                {
                    int pointTeam = msg.reader().readInt();
                    int pointTeam2 = msg.reader().readInt();
                    if (GameScr.phuban_Info != null)
                    {
                        GameScr.phuban_Info.updatePoint(type_PB, pointTeam, pointTeam2);
                    }
                }
                else if (b == 2)
                {
                    sbyte b2 = msg.reader().readByte();
                    short type = 0;
                    short num = -1;
                    if (b2 == 1)
                    {
                        type = 1;
                        num = 3;
                    }
                    else if (b2 == 2)
                    {
                        type = 2;
                    }
                    num = -1;
                    GameScr.phuban_Info = null;
                    GameScr.addEffectEnd(type, num, 0, GameCanvas.hw, GameCanvas.hh, 0, 0, -1, null);
                }
                else if (b == 5)
                {
                    short timeSecond2 = msg.reader().readShort();
                    if (GameScr.phuban_Info != null)
                    {
                        GameScr.phuban_Info.updateTime(type_PB, timeSecond2);
                    }
                }
                else if (b == 4)
                {
                    int lifeTeam = msg.reader().readByte();
                    int lifeTeam2 = msg.reader().readByte();
                    if (GameScr.phuban_Info != null)
                    {
                        GameScr.phuban_Info.updateLife(type_PB, lifeTeam, lifeTeam2);
                    }
                }
            }
            catch (Exception)
            {
            }
        }
    
        public void read_opt(Message msg)
        {
            try
            {
                sbyte b = msg.reader().readByte();
                if (b == 0)
                {
                    short idHat = msg.reader().readShort();
                    Char.myCharz().idHat = idHat;
                    SoundMn.gI().getStrOption();
                }
                else if (b == 2)
                {
                    int num = msg.reader().readInt();
                    sbyte b2 = msg.reader().readByte();
                    short num2 = msg.reader().readShort();
                    string v = num2 + "," + b2;
                    MainImage imagePath = ImgByName.getImagePath("banner_" + num2, ImgByName.hashImagePath);
                    GameCanvas.danhHieu.put(num + string.Empty, v);
                }
                else if (b == 3)
                {
                    short num3 = msg.reader().readShort();
                    SmallImage.createImage(num3);
                    BackgroudEffect.id_water1 = num3;
                }
                else if (b == 4)
                {
                    string o = msg.reader().readUTF();
                    GameCanvas.messageServer.addElement(o);
                }
            }
            catch (Exception e)
            {
                Debug.LogException(e);
            }
        }
    
        public void read_UpdateSkill(Message msg)
        {
            try
            {
                short num = msg.reader().readShort();
                sbyte b = -1;
                try
                {
                    b = msg.reader().readSByte();
                }
                catch (Exception)
                {
                }
                if (b == 0)
                {
                    short curExp = msg.reader().readShort();
                    for (int i = 0; i < Char.myCharz().vSkill.size(); i++)
                    {
                        Skill skill = (Skill)Char.myCharz().vSkill.elementAt(i);
                        if (skill.skillId == num)
                        {
                            skill.curExp = curExp;
                            break;
                        }
                    }
                }
                else if (b == 1)
                {
                    sbyte b2 = msg.reader().readByte();
                    for (int j = 0; j < Char.myCharz().vSkill.size(); j++)
                    {
                        Skill skill2 = (Skill)Char.myCharz().vSkill.elementAt(j);
                        if (skill2.skillId == num)
                        {
                            for (int k = 0; k < 20; k++)
                            {
                                string nameImg = "Skills_" + skill2.template.id + "_" + b2 + "_" + k;
                                MainImage imagePath = ImgByName.getImagePath(nameImg, ImgByName.hashImagePath);
                            }
                            break;
                        }
                    }
                }
                else
                {
                    if (b != -1)
                    {
                        return;
                    }
                    Skill skill3 = Skills.get(num);
                    for (int l = 0; l < Char.myCharz().vSkill.size(); l++)
                    {
                        Skill skill4 = (Skill)Char.myCharz().vSkill.elementAt(l);
                        if (skill4.template.id == skill3.template.id)
                        {
                            Char.myCharz().vSkill.setElementAt(skill3, l);
                            break;
                        }
                    }
                    for (int m = 0; m < Char.myCharz().vSkillFight.size(); m++)
                    {
                        Skill skill5 = (Skill)Char.myCharz().vSkillFight.elementAt(m);
                        if (skill5.template.id == skill3.template.id)
                        {
                            Char.myCharz().vSkillFight.setElementAt(skill3, m);
                            break;
                        }
                    }
                    for (int n = 0; n < GameScr.onScreenSkill.Length; n++)
                    {
                        if (GameScr.onScreenSkill[n] != null && GameScr.onScreenSkill[n].template.id == skill3.template.id)
                        {
                            GameScr.onScreenSkill[n] = skill3;
                            break;
                        }
                    }
                    for (int num2 = 0; num2 < GameScr.keySkill.Length; num2++)
                    {
                        if (GameScr.keySkill[num2] != null && GameScr.keySkill[num2].template.id == skill3.template.id)
                        {
                            GameScr.keySkill[num2] = skill3;
                            break;
                        }
                    }
                    if (Char.myCharz().myskill.template.id == skill3.template.id)
                    {
                        Char.myCharz().myskill = skill3;
                    }
                    GameScr.info1.addInfo(mResources.hasJustUpgrade1 + skill3.template.name + mResources.hasJustUpgrade2 + skill3.point, 0);
                }
            }
            catch (Exception)
            {
            }
        }
        /// <summary>Doc phan "noi dung goi" cua mot mon trong goi -48.</summary>
        /// <remarks>
        /// May chu LUON ghi mot byte so luong o day, ke ca khi mon do khong phai
        /// goi qua (luc do ghi 0). Bo qua khong doc byte ay la lech ca goi tu
        /// day tro di, nen ham nay phai duoc goi dung mot lan cho moi mon.
        /// </remarks>
        private static void docTrongGoi(Message msg, God.SuKienUI.Dong d)
        {
            int so = msg.reader().readUnsignedByte();
            for (int i = 0; i < so; i++)
            {
                var v = new God.SuKienUI.VatPham();
                v.icon = msg.reader().readShort();
                v.ten = msg.reader().readUTF();
                v.soLuong = msg.reader().readInt();
                v.soLuongMax = msg.reader().readInt();
                v.tiLe = msg.reader().readUTF();
                d.trongQua.Add(v);
            }
        }

    }
}