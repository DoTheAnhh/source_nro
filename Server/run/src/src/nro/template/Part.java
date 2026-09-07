package nro.template;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author VanKhaiPro
 */
public class Part {
    
    public int id;

    public int type;

    public List<PartDetail> partDetails;

    public Part() {
        this.partDetails = new ArrayList();
    }
        
}
