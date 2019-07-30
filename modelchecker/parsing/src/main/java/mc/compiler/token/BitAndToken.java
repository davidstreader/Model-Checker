package mc.compiler.token;

import mc.util.Location;

public class BitAndToken extends OperatorToken {

	public BitAndToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "&";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof BitAndToken)) return false;
    final BitAndToken other = (BitAndToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof BitAndToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}