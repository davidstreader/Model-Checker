package mc.compiler.token;

import mc.util.Location;

public class WhenToken extends KeywordToken {

	public WhenToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "when";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof WhenToken)) return false;
    final WhenToken other = (WhenToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof WhenToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}