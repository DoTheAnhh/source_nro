
namespace Game4
{
    public class Message
    {
    	public sbyte command;
    
    	private myReader dis;
    
    	private myWriter dos;
    
    	public Message(int command)
    	{
    		this.command = (sbyte)command;
    		dos = new myWriter();
    	}
    
    	public Message()
    	{
    		dos = new myWriter();
    	}
    
    	public Message(sbyte command)
    	{
    		this.command = command;
    		dos = new myWriter();
    	}
    
    	public Message(sbyte command, sbyte[] data)
    	{
    		this.command = command;
    		dis = new myReader(data);
    	}
    
    	public sbyte[] getData()
    	{
    		return dos.getData();
    	}
    
    	public myReader reader()
    	{
    		return dis;
    	}
    
    	public myWriter writer()
    	{
    		return dos;
    	}
    
    	public int readInt3Byte()
    	{
    		return dis.readInt();
    	}

    	/// <summary>Máu (HP) người chơi / đệ tử: 8 byte, giữ dạng số thực nhưng luôn là số nguyên.</summary>
    	/// <remarks>Trước đây máu đi 4 byte nên chặn ở khoảng 2,1 tỉ. Chỉ gọi ở gói máy chủ
    	/// đã ghi bằng writeHp — đọc lệch một bên là lệch cả gói.</remarks>
    	public double readHp()
    	{
    		return (double)dis.readLong();
    	}
    
    	public void cleanup()
    	{
    	}
    }
}
