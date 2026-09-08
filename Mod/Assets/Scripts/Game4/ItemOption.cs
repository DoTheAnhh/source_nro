
namespace Game4
{
    public class ItemOption
    {
    	public int param;

    	/// <summary>Tran cua khoang ngau nhien, -1 la tri co dinh.</summary>
    	/// <remarks>
    	/// Chi mon bay trong cua hang moi co: nguoi mua can thay truoc "5 ~ 10".
    	/// Mon da nam trong hanh trang thi tri so da chot nen luon -1.
    	/// </remarks>
    	public int paramMax = -1;

    
    	public sbyte active;
    
    	public sbyte activeCard;
    
    	public ItemOptionTemplate optionTemplate;
    
    	public ItemOption()
    	{
    	}
    
    	public ItemOption(int optionTemplateId, int param)
    	{
    		if (optionTemplateId == 22)
    		{
    			optionTemplateId = 6;
    			param *= 1000;
    		}
    		if (optionTemplateId == 23)
    		{
    			optionTemplateId = 7;
    			param *= 1000;
    		}
    		this.param = param;
    		optionTemplate = GameScr.gI().iOptionTemplates[optionTemplateId];
    	}
    
    	public string getOptionString()
    	{
    		string tri = (paramMax > param)
    			? (param + " ~ " + paramMax)
    			: (param + string.Empty);
    		return NinjaUtil.replace(optionTemplate.name, "#", tri);
    	}

    
    	public string getOptionName()
    	{
    		return NinjaUtil.replace(optionTemplate.name, "+#", string.Empty);
    	}
    
    	public string getOptiongColor()
    	{
    		return NinjaUtil.replace(optionTemplate.name, "$", string.Empty);
    	}
    }
}
