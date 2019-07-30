package mc.compiler.token;

import mc.util.Location;

public class ForAllToken extends KeywordToken {

	public ForAllToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "forall";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ForAllToken)) return false;
    final ForAllToken other = (ForAllToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ForAllToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}