package nro.entity.template;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author DoTheAnh
 */
public class Part {
    
    public int id;

    public int type;

    public List<PartDetail> partDetails;

    public Part() {
        this.partDetails = new ArrayList();
    }
        
}






