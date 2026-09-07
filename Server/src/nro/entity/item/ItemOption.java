package nro.entity.item;

import nro.service.item.ItemService;
import nro.core.util.Util;
import nro.entity.template.ItemOptionTemplate;

/**
 *
 * @author DoTheAnh
 */

public class ItemOption {

    public int param;

    /**
     * Trần của khoảng ngẫu nhiên, { -1} là không có khoảng.
     *
     * <p>Chỉ dùng cho món bày trong cửa hàng: chỗ đó cần hiện "5 ~ 10" để người
     * mua biết trước, còn vật phẩm đã nằm trong hành trang thì trị số đã chốt
     * rồi nên trường này luôn là -1.</p>
     */
    public int paramMax = -1;

    public ItemOptionTemplate optionTemplate;

    public ItemOption() {
    }

    public ItemOption(ItemOption io) {
        this.param = io.param;
        this.paramMax = io.paramMax;
        this.optionTemplate = io.optionTemplate;
    }

    public ItemOption(int tempId, int param) {
        this.optionTemplate = ItemService.gI().getItemOptionTemplate(tempId);
        this.param = param;
    }

    public ItemOption(ItemOptionTemplate temp, int param) {
        this.optionTemplate = temp;
        this.param = param;
    }

    public String getOptionString() {
        String tri = paramMax > param
                ? (param + " ~ " + paramMax) : String.valueOf(param);
        return Util.replace(this.optionTemplate.name, "#", tri);
    }

    public boolean isOptionCanUpgrade() {
        int opId = this.optionTemplate.id;
        return opId == 0 || opId == 6 || opId == 7 || opId == 14 || opId == 22 || opId == 23 || opId == 27 || opId == 28 || opId == 47;
    }
    
    public boolean haveExpiryDate() {
        return optionTemplate.id == 93 || optionTemplate.id == 260 || optionTemplate.id == 261;
    }

    public void dispose() {
        this.optionTemplate = null;
    }

    @Override
    public String toString() {
        final String n = "\"";
        return "{"
                + n + "id" + n + ":" + n + optionTemplate.id + n + ","
                + n + "param" + n + ":" + n + param + n
                + "}";
    }
}






