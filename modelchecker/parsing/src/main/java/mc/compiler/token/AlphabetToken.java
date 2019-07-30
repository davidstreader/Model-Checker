package mc.compiler.token;

import mc.util.Location;

public class AlphabetToken extends Token {

  public AlphabetToken(Location location){
    super(location);
  }

  @Override
  public String toString(){
    return "alphabet";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AlphabetToken)) return false;
    final AlphabetToken other = (AlphabetToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AlphabetToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
