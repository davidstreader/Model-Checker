package mc.compiler.token;

import mc.util.Location;

public class TraceEquivalentTypeToken extends OperationTypeToken {
    public TraceEquivalentTypeToken(Location loc) {
        super(loc);
    }
    public String toString() {
        return "#";
    }
}
