package mc.compiler.token;

import mc.util.Location;

public class LessThanEqToken extends OperatorToken {

	public LessThanEqToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "<=";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof LessThanEqToken)) return false;
    final LessThanEqToken other = (LessThanEqToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof LessThanEqToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}