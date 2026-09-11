using System;

namespace Game1.God
{
    /*Author: HAIRMOD*/
    public class NPoint : IActionListener, IChatable
    {
        private static NPoint instance { get; set; }
        private int index;
        private long hpg, mpg, dameg;
        private int defg;
        private bool canIncrease;
        private string[] strChat;
        private TypePoint type;
        public NPoint()
        {
            strChat = new string[2] { "Cộng Chỉ Số Tự Động", "Nhập Chỉ Số Muốn Cộng Tới" };
        }
        public static NPoint getInstance()
        {
            return (instance == null) ? (instance = new NPoint()) : instance;
        }
        public void setIncrease(bool show)
        {
            canIncrease = show;
        }
        private long getHPPotential()
        {
            long tn = type == TypePoint.Master ? Char.myCharz().cTiemNang : Char.myPetz().cTiemNang;
            long hp = (long)(type == TypePoint.Master ? Char.myCharz().cHPGoc : Char.myPetz().cHPGoc);
            long cTiemNang = tn;
            long cHPGoc = hp;
            long num = 10L * (long)(2 * (cHPGoc + 1000) + 180) / 2L;
            long num2 = 100L * (long)(2 * (cHPGoc + 1000) + 1980) / 2L;
            if (cTiemNang > (long)cHPGoc && cTiemNang < num)
            {
                return 1;
            }
            if (cTiemNang >= num && cTiemNang < num2)
            {
                return 10;
            }
            if (cTiemNang >= num2)
            {
                return 100;
            }
            return 0;
        }
        private long getMPPotential()
        {
            long tn = type == TypePoint.Master ? Char.myCharz().cTiemNang : Char.myPetz().cTiemNang;
            long mp = type == TypePoint.Master ? Char.myCharz().cMPGoc : Char.myPetz().cMPGoc;
            long cTiemNang = tn;
            long cMPGoc = mp;
            long num = 10L * (long)(2 * (cMPGoc + 1000) + 180) / 2L;
            long num2 = 100L * (long)(2 * (cMPGoc + 1000) + 1980) / 2L;
            if (cTiemNang > (long)cMPGoc && cTiemNang < num)
            {
                return 1;
            }
            if (cTiemNang >= num && cTiemNang < num2)
            {
                return 10;
            }
            if (cTiemNang >= num2)
            {
                return 100;
            }
            return 0;
        }
        private long getDamePotential()
        {
            long tn = type == TypePoint.Master ? Char.myCharz().cTiemNang : Char.myPetz().cTiemNang;
            long dame = type == TypePoint.Master ? Char.myCharz().cDamGoc : Char.myPetz().cDamGoc;
            long cTiemNang = tn;
            long cDamGoc = dame;
            long num = 10L * (long)(2 * cDamGoc + 9) / 2L * (long)Char.myCharz().expForOneAdd;
            long num2 = 100L * (long)(2 * cDamGoc + 99) / 2L * (long)Char.myCharz().expForOneAdd;
            if (cTiemNang > (long)cDamGoc && cTiemNang < num)
            {
                return 1;
            }
            if (cTiemNang >= num && cTiemNang < num2)
            {
                return 10;
            }
            if (cTiemNang >= num2)
            {
                return 100;
            }
            return 0;
        }
        private int getDefPotential()
        {
            long tn = type == TypePoint.Master ? Char.myCharz().cTiemNang : Char.myPetz().cTiemNang;
            int def = type == TypePoint.Master ? Char.myCharz().cDefGoc : Char.myPetz().cDefGoc;
            long cTiemNang = tn;
            int cDefGoc = def;
            long num = 10L * (long)(2 * (cDefGoc + 5) + 9) / 2L * 100000L;
            long num2 = 100L * (long)(2 * (cDefGoc + 5) + 99) / 2L * 100000L;
            if (cTiemNang > (long)cDefGoc && cTiemNang < num)
            {
                return 1;
            }
            if (cTiemNang >= num && cTiemNang < num2)
            {
                return 10;
            }
            if (cTiemNang >= num2)
            {
                return 100;
            }
            return 0;
        }
        private long getHPG()
        {
            switch (type)
            {
                case TypePoint.Master:
                    return (long)Char.myCharz().cHPGoc;
                case TypePoint.Pet:
                    return (long)Char.myPetz().cHPGoc;
                default: return 0;
            }
        }
        private long getMPG()
        {
            switch (type)
            {
                case TypePoint.Master:
                    return Char.myCharz().cMPGoc;
                case TypePoint.Pet:
                    return Char.myPetz().cMPGoc;
                default: return 0;
            }
        }
        private long getDameG()
        {
            switch (type)
            {
                case TypePoint.Master:
                    return Char.myCharz().cDamGoc;
                case TypePoint.Pet:
                    return Char.myPetz().cDamGoc;
                default: return 0;
            }
        }
        private int getDefG()
        {
            switch (type)
            {
                case TypePoint.Master:
                    return Char.myCharz().cDefGoc;
                case TypePoint.Pet:
                    return Char.myPetz().cDefGoc;
                default: return 0;
            }
        }
        private int getCritG()
        {
            switch (type)
            {
                case TypePoint.Master:
                    return Char.myCharz().cCriticalGoc;
                case TypePoint.Pet:
                    return Char.myPetz().cCriticalGoc;
                default: return 0;
            }
        }
        public void onChatFromMe(string text, string to)
        {
            ChatTextField chat = ChatTextField.gI();
            if (chat.strChat.Equals(strChat[0]))
            {
                int increase = int.Parse(chat.tfChat.getText());
                if (index <= 1 && increase % 20 != 0)
                {
                    GameCanvas.startOKDlg("HP, KI Phải Chia Hết Cho 20");
                    return;
                }
                switch (index)
                {
                    case 0:
                        setIncrease(true);
                        hpg = increase;
                        break;
                    case 1:
                        setIncrease(true);
                        mpg = increase;
                        break;
                    case 2:
                        setIncrease(true);
                        dameg = increase;
                        break;
                    case 3:
                        setIncrease(true);
                        defg = increase;
                        break;
                    case 4:
                        int cCriticalGoc = getCritG();
                        if (increase <= cCriticalGoc)
                        {
                            GameScr.info1.addInfo("Chỉ Số Không Hợp Lệ, Vui Lòng Nhập Lại!", 0);
                            return;
                        }
                        upPotential(index, increase - cCriticalGoc);
                        break;
                }
                Utils.resetTF();
                return;
            }
        }
        private void upPotential(int type, int num)
        {
            switch (this.type)
            {
                case TypePoint.Master:
                    Service.gI().upPotential(type, num);
                    break;
                case TypePoint.Pet:
                    Service.gI().upPotentialPet(type, num);
                    break;
            }
        }
        public void Update()
        {
            if (!canIncrease) return;
            if (GameCanvas.gameTick % 20 == 0)
            {
                if (getHPG() < hpg)
                {
                    long tnUse = getHPPotential();
                    if (tnUse >= 0)
                    {
                        long num = (hpg - getHPG()) / 20;
                        if (tnUse > num)
                        {
                            tnUse = num;
                        }
                        upPotential(0, (int)tnUse);
                    }
                    return;
                }
                if (getMPG() < mpg)
                {
                    long tnUse = getMPPotential();
                    if (tnUse >= 0)
                    {
                        long num = (mpg - getMPG()) / 20;
                        if (tnUse > num)
                        {
                            tnUse = num;
                        }
                        upPotential(1, (int)tnUse);
                    }
                    return;
                }
                if (getDameG() < dameg)
                {
                    long tnUse = getDamePotential();
                    if (tnUse >= 0)
                    {
                        long num = dameg - getDameG();
                        if (tnUse > num)
                        {
                            tnUse = num;
                        }
                        upPotential(2, (int)tnUse);
                    }
                    return;
                }
                if (getDefG() < defg)
                {
                    int tnUse = getDefPotential();
                    if (tnUse >= 0)
                    {
                        int num = defg - getDefG();
                        if (tnUse > num)
                        {
                            tnUse = num;
                        }
                        upPotential(3, tnUse);
                    }
                    return;
                }
            }
        }
        public void perform(int idAction, object p)
        {
            switch (idAction)
            {
                case int when idAction > 0:
                    type = (TypePoint)p;
                    index = idAction - 1;
                    GameCanvas.panel.hideNow();
                    Utils.startChat(this, strChat[0], strChat[1], TField.INPUT_TYPE_NUMERIC);
                    break;
            }
        }
        public void onCancelChat() => Utils.resetTF();
    }
}
