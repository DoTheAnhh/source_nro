using System.Collections.Generic;
using UnityEngine;

namespace Game5.God
{
    /*Author: HairMod*/
    public class ListChars
    {
        private static ListChars instance;
        public bool isShow = true;
        public bool HideMap;
        public int widthRect;
        public int heightRect;
        public List<Char> listPlayers = new List<Char>();
        public static ListChars getInstance()
        {
            return instance == null ? instance = new ListChars() : instance;
        }
        public void paintPlayerMap(mGraphics g)
        {
            if (!isShow) return;
            // Bat dau NGAY DUOI hang nut Tab / Chat.
            //
            // Con so 37 cu roi dung vao hang nut thu hai o goc tren phai, nen
            // ten nhan vat chay vao sau lung nut "Tab 1" va "Chat" — doc ra hai
            // lop chu chong nhau.
            int num = TabControll.mepDuoiHangNut() + 6;
            if (Boss.getInstance().isShow && num < 96)
            {
                num = 96;
            }
            widthRect = 142;
            heightRect = 9;
            for (int i = 0; i < listPlayers.Count; i++)
            {
                Char @char = listPlayers[i];
                if (@char.cFlag != 0)
                {
                    g.setColor(getFlagColor(@char));
                    g.fillRect(GameCanvas.w - widthRect - 10, num + 2, 7, 8);
                }
                g.setColor(2721889, 0.5f);
                g.fillRect(GameCanvas.w - widthRect, num + 2, widthRect - 2, heightRect);
                if (@char.cName != null && @char.cName != "" && !@char.isPet && !@char.isMiniPet && !@char.cName.StartsWith("#") && !@char.cName.StartsWith("$") && @char.cName != "Trọng tài")
                {
                    string text = string.Concat(new object[4]
                    {
                        @char.cName,
                        " [",
                        NinjaUtil.getMoneys(@char.cHP),
                        "]"
                    });
                    bool flag;
                    if (!(flag = isBoss(@char)))
                        text = string.Concat(new object[6]
                        {
                            @char.cName,
                            " [",
                            NinjaUtil.getMoneys(@char.cHP),
                            " - ",
                            @char.getGenderName(),
                            "]"
                        });
                    if (Char.myCharz().charFocus != null && Char.myCharz().charFocus.cName == @char.cName)
                    {
                        g.setColor(14155776);
                        g.drawLine(Char.myCharz().cx - GameScr.cmx, Char.myCharz().cy - GameScr.cmy + 1, @char.cx - GameScr.cmx, @char.cy - GameScr.cmy);
                        mFont.tahoma_7_red.drawString(g, i + 1 + ". " + text, GameCanvas.w - widthRect + 2, num, 0);
                    }
                    else if (flag)
                    {
                        g.setColor(16383818);
                        g.drawLine(Char.myCharz().cx - GameScr.cmx, Char.myCharz().cy - GameScr.cmy + 1, @char.cx - GameScr.cmx, @char.cy - GameScr.cmy);
                        mFont.tahoma_7_yellow.drawString(g, i + 1 + ". " + text, GameCanvas.w - widthRect + 2, num, 0);
                    }
                    else if (@char.cHPFull > 100000000 && @char.cHP > 0)
                    {
                        mFont.tahoma_7_red.drawString(g, i + 1 + ". " + text, GameCanvas.w - widthRect + 2, num, 0);
                    }
                    else
                    {
                        mFont.tahoma_7.drawString(g, i + 1 + ". " + text, GameCanvas.w - widthRect + 2, num, 0);
                    }
                    num += heightRect + 1;
                }
            }
        }
        public bool isBoss(Char ch)
        {
            if (ch.cName != null && ch.cName != "" && !ch.isPet && !ch.isMiniPet && char.IsUpper(char.Parse(ch.cName.Substring(0, 1))) && ch.cName != "Trọng tài" && !ch.cName.StartsWith("#"))
                return !ch.cName.StartsWith("$");
            return false;
        }
        private Color getFlagColor(Char @char)
        {
            switch (@char.cFlag)
            {
                case 1:
                    return Color.cyan;
                case 2:
                    return Color.red;
                case 3:
                    return new Color(0.56f, 0.19f, 0.77f);
                case 4:
                    return Color.yellow;
                case 5:
                    return Color.green;
                case 6:
                    return Color.magenta;
                case 7:
                    return new Color(1f, 0.5f, 0f);
                case 8:
                    return new Color(0.18f, 0.18f, 0.18f);
                case 9:
                    return Color.blue;
                case 10:
                    return Color.red;
                case 11:
                    return Color.blue;
                case 12:
                    return Color.white;
                case 13:
                    return Color.black;
                default:
                    return Color.clear;
            }
        }
        public void UpdateTouch()
        {
            for (int i = 0; i < listPlayers.Count; i++)
            {
                if ((Boss.getInstance().isShow) ? GameCanvas.isPointerHoldIn(GameCanvas.w - widthRect, 96 + (heightRect + 1) * i, widthRect, heightRect) :
                    GameCanvas.isPointerHoldIn(GameCanvas.w - widthRect, 37 + (heightRect + 1) * i, widthRect, heightRect))
                {
                    GameCanvas.isPointerJustDown = false;
                    GameScr.gI().isPointerDowning = false;
                    if (GameCanvas.isPointerClick && GameCanvas.isPointerJustRelease)
                    {
                        Char @char = listPlayers[i];
                        if (Char.myCharz().charFocus != null && Char.myCharz().charFocus.cName == @char.cName)
                        {
                            Utils.Teleport(Char.myCharz().charFocus.cx, Char.myCharz().charFocus.cy);
                        }
                        else
                        {
                            Utils.FocusObject(@char.charID);
                        }
                        Char.myCharz().currentMovePoint = null;
                        GameCanvas.clearAllPointerEvent();
                        return;
                    }
                }
            }
        }
        public void Update()
        {
            if (!isShow) return;
            listPlayers.Clear();
            for (int i = 0; i < GameScr.vCharInMap.size(); i++)
            {
                Char @char = (Char)GameScr.vCharInMap.elementAt(i);
                if (@char.cName != null && @char.cName != "" && !@char.isPet && !@char.isMiniPet && !@char.cName.StartsWith("#") && !@char.cName.StartsWith("$") && @char.cName != "Trọng tài")
                    listPlayers.Add(@char);
            }
        }
    }
}
