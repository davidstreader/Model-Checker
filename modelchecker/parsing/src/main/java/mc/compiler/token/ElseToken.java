package mc.compiler.token;

import mc.util.Location;

public class ElseToken extends KeywordToken {

	public ElseToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "else";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ElseToken)) return false;
    final ElseToken other = (ElseToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ElseToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}