package mc.compiler.token;

import mc.util.Location;

public class InterruptToken extends SymbolToken {

	public InterruptToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "~>";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof InterruptToken)) return false;
    final InterruptToken other = (InterruptToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof InterruptToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}