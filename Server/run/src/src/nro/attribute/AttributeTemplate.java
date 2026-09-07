package nro.attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author Văn khải
 */

@Getter
@Builder
@AllArgsConstructor
public class AttributeTemplate {

    private int id;
    private String name;
}
