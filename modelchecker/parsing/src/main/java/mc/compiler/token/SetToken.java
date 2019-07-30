package mc.compiler.token;

import mc.util.Location;

public class SetToken extends KeywordToken {

	public SetToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "set";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof SetToken)) return false;
    final SetToken other = (SetToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof SetToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}