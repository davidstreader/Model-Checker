package mc.compiler.token;

import mc.util.Location;

public abstract class TerminalToken extends Token {

	public TerminalToken(Location location){
		super(location);
	}

  public String toString() {
    return "TerminalToken()";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof TerminalToken)) return false;
    final TerminalToken other = (TerminalToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof TerminalToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
