package mc.compiler.token;

import mc.util.Location;

public class IfToken extends KeywordToken {

	public IfToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "if";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof IfToken)) return false;
    final IfToken other = (IfToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof IfToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}