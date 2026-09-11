namespace Game6.God
{
 /*Author: HairMod*/
 public class PetService
    {
        private static PetService instance { get; set; }
        private bool canUp;
        public static PetService getInstance()
        {
            return instance == null ? instance = new PetService() : instance;   
        }
        public void Update()
        {
            if(canUp && Char.myCharz().havePet)
            {
                if(GameCanvas.gameTick % 50 == 0)
                {
                    Service.gI().petInfo();
                }
                var p = Char.myPetz();
                if(GameCanvas.gameTick % 20 == 0 && !p.meDead && (p.cHP < p.cHPFull / 4 || p.cMP < p.cMPFull >> 2 || p.cStamina < 150))
                {
                    GameScr.gI().doUseHP();
                }
            }
        }
        public bool getUp()
        {
            return canUp;
        }
        public void setUp()
        {
            canUp = !canUp;
        }
    }
}
