using System;

namespace Game6
{
    
    public class MyStream
    {
    	public static DataInputStream readFile(string path)
    	{
    		path = Main.res + path;
    		try
    		{
    			return DataInputStream.getResourceAsStream(path);
    		}
    		catch (Exception)
    		{
    			return null;
    		}
    	}
    }
}
