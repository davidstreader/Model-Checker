package mc.compiler.token;

import mc.util.Location;

public abstract class OperatorToken extends Token {

	public OperatorToken(Location location){
		super(location);
	}

  public String toString() {
    return "OperatorToken()";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof OperatorToken)) return false;
    final OperatorToken other = (OperatorToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof OperatorToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
