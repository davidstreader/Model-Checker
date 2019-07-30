package mc.compiler.token;

import mc.Constant;
import mc.util.Location;

public class ErrorToken extends TerminalToken {

	public ErrorToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return Constant.ERROR;
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ErrorToken)) return false;
    final ErrorToken other = (ErrorToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ErrorToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}