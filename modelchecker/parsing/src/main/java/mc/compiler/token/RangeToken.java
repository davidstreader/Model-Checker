package mc.compiler.token;

import mc.util.Location;

public class RangeToken extends KeywordToken {

	public RangeToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "range";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof RangeToken)) return false;
    final RangeToken other = (RangeToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof RangeToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}