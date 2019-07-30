package mc.compiler.token;

import mc.util.Location;

public abstract class KeywordToken extends Token {

	public KeywordToken(Location location){
		super(location);
	}

  public String toString() {
    return "KeywordToken()";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof KeywordToken)) return false;
    final KeywordToken other = (KeywordToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof KeywordToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
