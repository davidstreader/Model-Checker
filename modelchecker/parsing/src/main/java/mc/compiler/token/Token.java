package mc.compiler.token;

import mc.util.Location;

public abstract class Token {

	private Location location;

	public Token(Location location){
		this.location = location;
	}


  public Location getLocation() {
    return this.location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof Token)) return false;
    final Token other = (Token) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$location = this.getLocation();
    final Object other$location = other.getLocation();
    if (this$location == null ? other$location != null : !this$location.equals(other$location)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof Token;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $location = this.getLocation();
    result = result * PRIME + ($location == null ? 43 : $location.hashCode());
    return result;
  }

  public String toString() {
    return "Token(location=" + this.getLocation() + ")";
  }
}
