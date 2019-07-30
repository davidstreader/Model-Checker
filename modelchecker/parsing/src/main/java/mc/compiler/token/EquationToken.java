package mc.compiler.token;

import mc.util.Location;

public class EquationToken extends Token {
    public EquationToken(Location location) {
        super(location);
    }

    @Override
    public String toString(){
        return "equation";
    }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof EquationToken)) return false;
    final EquationToken other = (EquationToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof EquationToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
