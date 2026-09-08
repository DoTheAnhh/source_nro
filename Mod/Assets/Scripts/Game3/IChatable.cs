
namespace Game3
{
    public interface IChatable
    {
    	void onChatFromMe(string text, string to);
    
    	void onCancelChat();
    }
}
