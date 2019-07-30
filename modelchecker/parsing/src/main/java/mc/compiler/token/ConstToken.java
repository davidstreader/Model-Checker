package mc.compiler.token;

import mc.util.Location;

public class ConstToken extends KeywordToken {

	public ConstToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "const";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ConstToken)) return false;
    final ConstToken other = (ConstToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ConstToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}