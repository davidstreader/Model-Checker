package mc.compiler.token;

import mc.util.Location;


public class ProcessesDefintionToken extends Token {

    public ProcessesDefintionToken(Location location) {
        super(location);
    }

    @Override
    public String toString(){
        return "processes";
    }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ProcessesDefintionToken)) return false;
    final ProcessesDefintionToken other = (ProcessesDefintionToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ProcessesDefintionToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}