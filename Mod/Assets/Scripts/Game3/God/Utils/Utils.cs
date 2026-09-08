namespace Game3.God
{
    /*Author: HAIRMOD*/
    public static class Utils
    {
        private static ChatTextField tf => ChatTextField.gI();
        private static long timeUse;
        public static void startChat(IChatable chatable, string name, string text, int typeInput)
        {
            ChatTextField chatTextField = ChatTextField.gI();
            chatTextField.strChat = name;
            chatTextField.tfChat.name = text;
            chatTextField.tfChat.setIputType(typeInput);
            // Nho lai de hop nhap chung biet co bat go tieng Viet khong.
            chatTextField.kieuNhap = typeInput;
            chatTextField.left.caption = "OK";
            chatTextField.right.caption = "Đóng";
            // Hop nhap cua man mod la loai DUNG MOT LAN: bam OK hoac Dong la
            // dong han. Khung chat cua game khong dat co nay nen giu nguyen nep
            // cu — gui xong o lai de nhan tiep.
            chatTextField.tuDongDong = true;
            chatTextField.startChat2(chatable, string.Empty);
        }
        public static void UseItem(int id)
        {
            foreach(var item in Char.myCharz().arrItemBag)
            {
                if(item != null && item.template.id == id)
                {
                    Service.gI().useItem(0, 1, (sbyte)(item.indexUI), -1);
                    break;
                }
            }
        }
        public static void addInfo1(string info, bool a)
        {
            GameScr.info1.addInfo("|1|" +info + "\n" + (a ? "Bật" : "Tắt"), 0);
        }
        public static void Teleport(int x, int y)
        {
            Char.myCharz().cx = x;
            Char.myCharz().cy = y;
            Service.gI().charMove();
        }
        public static void FocusObject(int charId)
        {
            for (int i = 0; i < GameScr.vCharInMap.size(); i++)
            {
                Char @char = (Char)GameScr.vCharInMap.elementAt(i);
                bool flag = !@char.isMiniPet && !@char.isPet && @char.charID == charId;
                if (flag)
                {
                    Char.myCharz().mobFocus = null;
                    Char.myCharz().npcFocus = null;
                    Char.myCharz().itemFocus = null;
                    Char.myCharz().charFocus = @char;
                    break;
                }
            }
        }
        public static bool findItemBag(int id)
        {
            foreach(var item in Char.myCharz().arrItemBag)
            {
                if(item != null && item.template.id == id)
                {
                    return true;
                } 
            }
            return false;
        }
        public static bool findItemName(string name)
        {
            foreach(var item in Char.myCharz().arrItemBag)
            {
                if(item != null && item.template.name.ToLower().StartsWith(name))
                {
                    return true;
                }
            }
            return false;
        }
        public static Item getItemFromName(string name)
        {
            foreach(var item in Char.myCharz().arrItemBag)
            {
                if(item != null && item.template.name.ToLower().StartsWith(name))
                {
                    return item;
                }
            }
            return null;
        }
        public static void useItemWithTime(int id, long time)
        {
            foreach (var item in Char.myCharz().arrItemBag)
            {
                if (item != null && item.template.id == id)
                {
                    if (mSystem.currentTimeMillis() - timeUse >= time)
                    {
                        timeUse = mSystem.currentTimeMillis();
                        time = timeUse;
                        UseItem(id);
                        break;
                    }
                }
            }
        }
        public static void resetTF()
        {
            tf.strChat = "Chat";
            tf.tfChat.name = "chat";
            tf.to = "";
            tf.tfChat.setIputType(TField.INPUT_TYPE_ANY);
            tf.parentScreen = GameScr.gI();
            tf.isShow = false;
        }
        public static string waterMark()
        {
            return "HAIRMOD - CopyRight By HAIRMOD";
        }
        public static string formatLong(long m)
        {
            return NinjaUtil.getMoneys(m);
        }
    }
}
