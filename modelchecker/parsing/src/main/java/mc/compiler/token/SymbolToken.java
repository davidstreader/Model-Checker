package mc.compiler.token;

import mc.util.Location;

public abstract class SymbolToken extends Token {

	public SymbolToken(Location location){
		super(location);
	}

  public String toString() {
    return "SymbolToken()";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof SymbolToken)) return false;
    final SymbolToken other = (SymbolToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof SymbolToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
