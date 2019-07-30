package mc.compiler.token;

import mc.util.Location;

public class QuestionMarkToken extends SymbolToken {

	public QuestionMarkToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "?";
	}

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof QuestionMarkToken)) return false;
    final QuestionMarkToken other = (QuestionMarkToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof QuestionMarkToken;
  }

  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}