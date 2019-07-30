package mc.compiler.token;

import mc.util.Location;

public class ThenToken extends KeywordToken {

  public ThenToken(Location location) {
    super(location);
  }

  @Override
  public String toString() {
    return "then";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ThenToken)) return false;
    final ThenToken other = (ThenToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ThenToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}