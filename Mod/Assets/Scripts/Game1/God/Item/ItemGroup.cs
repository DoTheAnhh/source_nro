namespace Game1.God
{
    /*Author: HAIRMOD*/
    public struct ItemGroup
    {
        public int id;
        public int indexUI;
        public bool buyGold;
        public bool buyCoin;
        public ItemGroup(int id, int indexUI, bool buyGold, bool buyCoin)
        {
            this.id = id;
            this.indexUI = indexUI;
            this.buyGold = buyGold;
            this.buyCoin = buyCoin;
        }
    }
}
