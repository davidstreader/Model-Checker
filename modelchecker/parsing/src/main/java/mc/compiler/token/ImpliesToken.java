
package mc.compiler.token;

import mc.util.Location;

public class ImpliesToken extends OperatorToken {

  public ImpliesToken(Location location){
    super(location);
  }

  @Override
  public String toString(){
    return "==>";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ImpliesToken)) return false;
    final ImpliesToken other = (ImpliesToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ImpliesToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
