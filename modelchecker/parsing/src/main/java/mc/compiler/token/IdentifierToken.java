package mc.compiler.token;

import mc.util.Location;

/**
 * Identifier token is any variable starting with an uppercase letter.
 */
public class IdentifierToken extends Token {

	private final String identifier;

	public IdentifierToken(String identifier, Location location){
		super(location);
		this.identifier = identifier;
	}

	@Override
	public String toString(){
		return identifier;
	}

  public String getIdentifier() {
    return this.identifier;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof IdentifierToken)) return false;
    final IdentifierToken other = (IdentifierToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$identifier = this.getIdentifier();
    final Object other$identifier = other.getIdentifier();
    if (this$identifier == null ? other$identifier != null : !this$identifier.equals(other$identifier)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof IdentifierToken;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $identifier = this.getIdentifier();
    result = result * PRIME + ($identifier == null ? 43 : $identifier.hashCode());
    return result;
  }
}