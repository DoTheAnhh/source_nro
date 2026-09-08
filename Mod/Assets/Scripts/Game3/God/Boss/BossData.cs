using System;
using System.Collections.Generic;

namespace Game3.God
{
    /*Author: HAIRMOD*/
    public class BossData
    {
        private static BossData instance{ get; set; }
        private string map;
        public string name;
        public List<BossData> listData = new List<BossData>();
        public DateTime? timeStart;
        public static BossData getInstance()
        {
            return instance == null ? instance = new BossData() : instance;
        }
        /// <summary>
        /// Bao lâu rồi kể từ lúc boss xuất hiện.
        /// </summary>
        /// <remarks>
        /// Bản cũ trả về giờ đồng hồ ("14h:05") — muốn biết boss lên bao lâu
        /// rồi thì phải tự nhẩm trừ với giờ hiện tại. Nay trả thẳng khoảng đã
        /// trôi qua, và tự nâng đơn vị khi để lâu: giây → phút → giờ.
        /// </remarks>
        public string getStartTimeSpan()
        {
            if (!this.timeStart.HasValue)
            {
                return "vừa xong";
            }
            int giay = (int)DateTime.Now.Subtract(this.timeStart.Value).TotalSeconds;
            if (giay < 0)
            {
                giay = 0;
            }
            if (giay < 60)
            {
                return giay + " giây trước";
            }
            int phut = giay / 60;
            int giayDu = giay % 60;
            if (phut < 60)
            {
                return giayDu == 0
                    ? phut + " phút trước"
                    : phut + " phút " + giayDu + " giây trước";
            }
            int gio = phut / 60;
            int phutDu = phut % 60;
            return phutDu == 0
                ? gio + " giờ trước"
                : gio + " giờ " + phutDu + " phút trước";
        }
        public string getMapName()
        {
            if (this.map != null && !(this.map == ""))
            {
                return this.map;
            }
            return "Chưa có thông tin";
        }
        public void getBOSSInfo(string string_0)
        {
            string[] array = string_0.Replace(string_0.StartsWith("BOSS") ? "BOSS " : "Boss ", "").Replace(" vừa xuất hiện tại ", "|").Replace(" appear at ", "|")
                .Split(new char[] { '|' });
            BossData bossInfo = new BossData
            {
                name = array[0].Trim(),
                map = array[1].Trim(),
                timeStart = new DateTime?(DateTime.Now)
            };
            listData.Add(bossInfo);
            if (listData.Count > 5)
            {
                listData.RemoveAt(0);
            }
        }
    }
}
