
package mc.compiler.token;

import mc.Constant;
import mc.util.Location;

/**
 * Token for Stop node that may have exiting edges
 * Initiall used for testing but built by Gal fap2bc
 */
public class EndToken extends TerminalToken {

  public EndToken(Location location){
    super(location);
  }

  @Override
  public String toString(){
    return Constant.END;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof EndToken)) return false;
    final EndToken other = (EndToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof EndToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
