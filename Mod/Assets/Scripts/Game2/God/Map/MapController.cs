namespace Game2.God
{
    /*Author: HairMod*/
    public class MapController
    {
        private static MapController instance { get; set; }
        private int[] leftWay, centerWay, rightWay;
        public static MapController getInstance()
        {
            return instance == null ? instance = new MapController() : instance;
        }
        private void ResetWayPoint()
        {
            leftWay = new int[2];
            centerWay = new int[2];
            rightWay = new int[2];
        }
        private void LoadWayPoint()
        {
            ResetWayPoint();
            int size = TileMap.vGo.size();
            if (size != 2)
            {
                for (int i = 0; i < size; i++)
                {
                    Waypoint waypoint = (Waypoint)(TileMap.vGo.elementAt(i));
                    if (waypoint.maxX < 60)
                    {
                        leftWay[0] = waypoint.minX + 15;
                        leftWay[1] = waypoint.maxY;
                    }
                    else if (waypoint.maxX > TileMap.pxw - 60)
                    {
                        rightWay[0] = waypoint.maxX - 15;
                        rightWay[1] = waypoint.maxY;
                    }
                    else
                    {
                        centerWay[0] = waypoint.minX + 15;
                        centerWay[1] = waypoint.maxY;
                    }
                }
                return;
            }
            Waypoint waypoint2 = (Waypoint)TileMap.vGo.elementAt(0);
            Waypoint waypoint3 = (Waypoint)TileMap.vGo.elementAt(1);
            if ((waypoint2.maxX < 60 && waypoint3.maxX < 60) ||
                (waypoint2.minX > TileMap.pxw - 60 && waypoint3.minX > TileMap.pxw - 60))
            {
                leftWay[0] = (waypoint2.minX + 15);
                leftWay[1] = waypoint2.maxY;
                rightWay[0] = (waypoint3.maxX - 15);
                rightWay[1] = waypoint3.maxY;
                return;
            }

            if (waypoint2.maxX < waypoint3.maxX)
            {
                leftWay[0] = (waypoint2.minX + 15);
                leftWay[1] = waypoint2.maxY;
                rightWay[0] = (waypoint3.maxX - 15);
                rightWay[1] = waypoint3.maxY;
                return;
            }
            leftWay[0] = (waypoint3.minX + 15);
            leftWay[1] = waypoint3.maxY;
            rightWay[0] = (waypoint2.maxX - 15);
            rightWay[1] = waypoint2.maxY;
        }
        public void NextMap(int index)
        {
            LoadWayPoint();
            switch (index)
            {
                case 0:
                    if (leftWay[0] != 0 && leftWay[1] != 0)
                    {
                        Utils.Teleport(leftWay[0], leftWay[1]);
                    }
                    else
                    {
                        Utils.Teleport(60, GetYGround(60));
                    }
                    break;
                case 1:
                    if (rightWay[0] != 0 && rightWay[1] != 0)
                    {
                        Utils.Teleport(rightWay[0], rightWay[1]);
                    }
                    else
                    {
                        Utils.Teleport(TileMap.pxw - 60, GetYGround(TileMap.pxw - 60));
                    }
                    break;
                case 2:
                    if (centerWay[0] != 0 && centerWay[1] != 0)
                    {
                        Utils.Teleport(centerWay[0], centerWay[1]);
                    }
                    else
                    {
                        Utils.Teleport(TileMap.pxw / 2, GetYGround(TileMap.pxw / 2));
                    }
                    break;
            }
            if (TileMap.mapID != 7 && TileMap.mapID != 14 && TileMap.mapID != 0)
            {
                Service.gI().requestChangeMap();
                return;
            }
            Service.gI().getMapOffline();
        }
        private int GetYGround(int y)
        {
            int num = 50;
            int i = 0;
            while (i < 30)
            {
                i++;
                num += 24;
                if (TileMap.tileTypeAt(y, num, 2))
                {
                    if (num % 24 != 0)
                    {
                        num -= num % 24;
                    }

                    return num;
                }
            }

            return num;
        }
    }
}
