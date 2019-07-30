package mc.compiler.token;

import mc.util.Location;

public class TraceEquivalentTypeToken extends OperationTypeToken {

    public TraceEquivalentTypeToken(Location loc) {
        super(loc);
    }

    @Override
    public String toString() {
        return "#";
    }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof TraceEquivalentTypeToken)) return false;
    final TraceEquivalentTypeToken other = (TraceEquivalentTypeToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof TraceEquivalentTypeToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
