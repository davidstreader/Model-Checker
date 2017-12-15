package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class TraceEquivalentTypeToken extends OperationTypeToken {

    public TraceEquivalentTypeToken(Location loc) {
        super(loc);
    }

    @Override
    public String toString() {
        return "#";
    }
}
