package mc.compiler.token;

import mc.Constant;
import mc.util.Location;

public class StopToken extends TerminalToken {

	public StopToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return Constant.STOP;
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof StopToken)) return false;
    final StopToken other = (StopToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof StopToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}