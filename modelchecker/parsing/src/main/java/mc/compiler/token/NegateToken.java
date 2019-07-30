package mc.compiler.token;

import mc.util.Location;

public class NegateToken extends OperatorToken {

	public NegateToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "!";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof NegateToken)) return false;
    final NegateToken other = (NegateToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof NegateToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}