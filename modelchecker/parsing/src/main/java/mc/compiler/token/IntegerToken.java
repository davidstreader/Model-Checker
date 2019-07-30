package mc.compiler.token;

import mc.util.Location;

public class IntegerToken extends Token {

	private int integer;

	public IntegerToken(int integer, Location location){
		super(location);
		this.integer = integer;
	}

	@Override
	public String toString(){
		return "" + integer;
	}

  public int getInteger() {
    return this.integer;
  }

  public void setInteger(int integer) {
    this.integer = integer;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof IntegerToken)) return false;
    final IntegerToken other = (IntegerToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    if (this.getInteger() != other.getInteger()) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof IntegerToken;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    result = result * PRIME + this.getInteger();
    return result;
  }
}
