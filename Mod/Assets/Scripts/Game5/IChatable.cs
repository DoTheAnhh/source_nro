
namespace Game5
{
    public interface IChatable
    {
    	void onChatFromMe(string text, string to);
    
    	void onCancelChat();
    }
}
