package mc.compiler.token;

import mc.util.Location;

public class FunctionToken extends Token {

	private final String function;

	public FunctionToken(String function, Location location){
		super(location);
		this.function = function;
	}

	@Override
	public String toString(){
		return function;
	}

  public String getFunction() {
    return this.function;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof FunctionToken)) return false;
    final FunctionToken other = (FunctionToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$function = this.getFunction();
    final Object other$function = other.getFunction();
    if (this$function == null ? other$function != null : !this$function.equals(other$function)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof FunctionToken;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $function = this.getFunction();
    result = result * PRIME + ($function == null ? 43 : $function.hashCode());
    return result;
  }
}