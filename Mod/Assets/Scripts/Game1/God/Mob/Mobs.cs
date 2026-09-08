
using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
/*Author: Pk9r327*/
namespace Game1.God
{
    public class Mobs
    {
        private static readonly sbyte[] IdSkillsBase = new sbyte[5] { 0, 2, 17, 4, 13 };

        // 225 (Manh da vun) da bo khoi danh sach chan: no la vat pham nhat binh
        // thuong, chan la auto nhat bo qua mai. Cac id 353-362 la Ngoc Rong
        // Namec — chan co chu dich, nhat len la bi danh dau.
        public static readonly short[] IdItemBlockBase = new short[9] { 353, 354, 355, 356, 357, 358, 359, 360, 362 };

        public static bool IsTanSat = false;

        public static bool tsPlayer = false;

        public static bool neSieuQuai = false;

        public static bool vuotDiaHinh = true;

        public static bool telePem = true;

        public static bool isGoBack;

        public static int mapGoback;

        public static int zoneGoback;

        public static int xGoback;

        public static int yGoback;

        public static List<int> IdMobsTanSat = new List<int>();

        public static List<int> TypeMobsTanSat = new List<int>();

        public static List<sbyte> IdSkillsTanSat = new List<sbyte>(IdSkillsBase);

        public static bool IsAutoPickItems = true;

        public static bool IsPickItemsAll = true;

        public static bool IsPickItemsDis = false;

        public static bool IsLimitTimesPickItem = true;

        public static int TimesAutoPickItemMax = 20;

        public static List<short> IdItemPicks = new List<short>();

        public static List<short> IdItemBlocks = new List<short>(IdItemBlockBase);

        public static List<sbyte> TypeItemPicks = new List<sbyte>();

        public static List<sbyte> TypeItemBlock = new List<sbyte>();

        public static int HpBuff = 0;

        public static int MpBuff = 0;

        public static void Update()
        {
            MobController.Update();
        }

        public static void GotoXY(int x, int y)
        {
            Char.myCharz().cx = x;
            Char.myCharz().cy = y;
            Service.gI().charMove();
        }
    }

}
