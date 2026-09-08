
namespace Game4
{
    public interface IChatable
    {
    	void onChatFromMe(string text, string to);
    
    	void onCancelChat();
    }
}
