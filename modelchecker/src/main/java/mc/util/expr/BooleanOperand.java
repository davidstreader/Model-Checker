package mc.util.expr;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Created by sanjay on 3/02/17.
 */
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class BooleanOperand extends Operand{
    private boolean value;

    public boolean getValue() {
        return value;
    }
}
