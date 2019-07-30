package mc.compiler.token;


import mc.util.Location;

public class AutomatonToken extends SymbolToken {

    public AutomatonToken(Location location){
        super(location);
    }

    @Override
    public String toString(){
        return "aut";
    }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AutomatonToken)) return false;
    final AutomatonToken other = (AutomatonToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AutomatonToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
