package mc.compiler.token;

import mc.util.Location;

public class MultiplicationToken extends OperatorToken {

	public MultiplicationToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "*";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof MultiplicationToken)) return false;
    final MultiplicationToken other = (MultiplicationToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof MultiplicationToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}