package mc.compiler.token;

import mc.util.Location;


public class HidingToken extends SymbolToken {

  public HidingToken(Location location){
    super(location);
  }

  @Override
  public String toString(){
    return "hiding";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof HidingToken)) return false;
    final HidingToken other = (HidingToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof HidingToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
