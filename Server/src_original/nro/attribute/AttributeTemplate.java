package nro.attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author MaiTienDung
 */

@Getter
@Builder
@AllArgsConstructor
public class AttributeTemplate {

    private int id;
    private String name;
}
