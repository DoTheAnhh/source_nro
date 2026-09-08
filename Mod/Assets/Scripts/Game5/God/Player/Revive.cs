namespace Game5.God
{
/*Author: HAIRMOD*/
    public class Revive
    {
        private static Revive instance { get; set; }
        private bool canRevive  = true;
        public static Revive getInstance()
        {
            return (instance == null) ? (instance = new Revive()) : instance;
        }
        public void setRevive()
        {
            canRevive = !canRevive;
        }
        public bool getRevive()
        {
            return canRevive;
        }
        private void PlayerRevive()
        {
            if(canRevive && Char.myCharz().meDead)
            {
                if(GameCanvas.gameTick % 20 == 0)
                {
                    Service.gI().wakeUpFromDead();
                }
            }
        }
        public void Update()
        {
            PlayerRevive();
        }
    }
}
