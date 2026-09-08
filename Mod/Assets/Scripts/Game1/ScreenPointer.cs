namespace Game1
{
    //using UnityEngine;
    
    //internal class ScreenPointer
    //{
    //    static Vector2? clickedPos = null;
    //    static GUIStyle style;
    //    internal static void start()
    //    {
    //    }
    //    internal static void onCheckInput()
    //    {
    //        if (Input.GetMouseButtonDown(0))
    //        {
    //            Vector3 mp = Input.mousePosition;
    //            float x = mp.x ;
    //            float y = Screen.height - mp.y;
    //            clickedPos = new Vector2(x, y);
    //        }
    
    //    }
    //    internal static void onGUI()
    //    {
    //        if (clickedPos != null)
    //        {
    //            Vector2 pos = clickedPos.Value;
    //            GUI.color = Color.red;
    //            GUI.DrawTexture(new Rect(pos.x - 3, pos.y - 3, 10, 10), Texture2D.whiteTexture);
    //            GUI.color = Color.black;
    //            if(style != null)
    //            GUI.Label(new Rect(pos.x + 10, pos.y + 5, 200, 30), $"x: {(int)pos.x / mGraphics.zoomLevel}, y: {(int)pos.y / mGraphics.zoomLevel}", style);
    //            else
    //            {
    //                style = new GUIStyle(GUI.skin.label);
    //                style.normal.textColor = Color.red;
    //                style.fontSize = 8 * mGraphics.zoomLevel;
    //            }
    //        }
    
    //    }
    //}
}
