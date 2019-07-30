package mc.compiler.token;

import mc.util.Location;

public class OperationToken extends Token {

    public OperationToken(Location location){
        super(location);
    }

    @Override
    public String toString(){
        return "operation";
    }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof OperationToken)) return false;
    final OperationToken other = (OperationToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof OperationToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
