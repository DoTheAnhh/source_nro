using System;
using System.Diagnostics;

namespace Game3.God
{
    /*Author: HairMod*/
    public class nSkill
    {
        private static nSkill instance { get; set; }
        public bool canAttack;
        private long[] timeAttack = new long[10];
        public static nSkill getInstance()
        {
            return instance == null ? instance = new nSkill() : instance;
        }
        private int getSkill()
        {
            for (int i = 0; i < GameScr.keySkill.Length; i++)
            {
                if (GameScr.keySkill[i] == Char.myCharz().myskill)
                {
                    return i;
                }
            }
            return 0;
        }
        private void vAttack()
        {
            try
            {
                MyVector myVector = new MyVector();
                myVector.addElement(Char.myCharz().mobFocus);
                Service.gI().sendPlayerAttack(myVector, new MyVector(), 1);
            }
            catch(Exception e)
            {
                UnityEngine.Debug.LogException(e);
            }
        }
        private void sendAttackPlayer()
        {
            try
            {
                MyVector myVector = new MyVector();
                myVector.addElement(Char.myCharz().charFocus);
                Service.gI().sendPlayerAttack(new MyVector(), myVector, 2);
            }
            catch
            {
            }
        }
        private long getMana(Skill skill)
        {
            if (skill.template.id != 20 && skill.template.id != 22 && skill.template.id != 7 && skill.template.id != 18)
            {
                if (skill.template.id != 23)
                {
                    long num = (long)((double)skill.coolDown * 1.2);
                    if (num < 415L)
                    {
                        return 415L;
                    }
                    return num;
                }
            }
            return (long)skill.coolDown + 500L;
        }
        public bool checkSkill(Char @char)
        {
            if (TileMap.mapID == 113)
            {
                return @char != null && Char.myCharz().myskill != null && (@char.cTypePk == 5 || @char.cTypePk == 3);
            }
            if (@char != null && Char.myCharz().myskill != null)
            {
                if (@char.statusMe != 14 && @char.statusMe != 5 && Char.myCharz().myskill.template.type != 2)
                {
                    if ((Char.myCharz().cFlag != 8 || @char.cFlag == 0) && (Char.myCharz().cFlag == 0 || @char.cFlag != 8) && (Char.myCharz().cFlag == @char.cFlag || Char.myCharz().cFlag == 0 || @char.cFlag == 0) && (@char.cTypePk != 3 || Char.myCharz().cTypePk != 3) && Char.myCharz().cTypePk != 5 && @char.cTypePk != 5 && (Char.myCharz().cTypePk != 1 || @char.cTypePk != 1))
                    {
                        if (Char.myCharz().cTypePk != 4)
                        {
                            goto IL_159;
                        }
                        if (@char.cTypePk != 4)
                        {
                            goto IL_159;
                        }
                    }
                    return true;
                }
            IL_159:
                return Char.myCharz().myskill.template.type == 2 && @char.cTypePk != 5;
            }
            return false;
        }
        public void sendAttack()
        {
            if (!Char.myCharz().meDead && Char.myCharz().cHP > 0 && Char.myCharz().statusMe != 14 && Char.myCharz().statusMe != 5 && Char.myCharz().myskill.template.type != 3 && Char.myCharz().myskill.template.id != 10 && Char.myCharz().myskill.template.id != 11 && (!Char.myCharz().myskill.paintCanNotUseSkill || GameCanvas.panel.isShow))
            {
                int num = this.getSkill();
                if (mSystem.currentTimeMillis() - this.timeAttack[num] > this.getMana(Char.myCharz().myskill))
                {
                    if (GameScr.gI().isMeCanAttackMob(Char.myCharz().mobFocus) && (double)Res.abs(Char.myCharz().mobFocus.xFirst - Char.myCharz().cx) < (double)Char.myCharz().myskill.dx * 1.7)
                    {
                        Char.myCharz().myskill.lastTimeUseThisSkill = mSystem.currentTimeMillis();
                        this.vAttack();
                        this.timeAttack[num] = mSystem.currentTimeMillis();
                        return;
                    }
                    if (Char.myCharz().charFocus != null && this.checkSkill(Char.myCharz().charFocus) && (double)Res.abs(Char.myCharz().charFocus.cx - Char.myCharz().cx) < (double)Char.myCharz().myskill.dx * 1.7)
                    {
                        Char.myCharz().myskill.lastTimeUseThisSkill = mSystem.currentTimeMillis();
                        this.sendAttackPlayer();
                        this.timeAttack[num] = mSystem.currentTimeMillis();
                    }
                }
                return;
            }
        }
        public void Update()
        {
            if (canAttack) sendAttack();
        }
    }
}
