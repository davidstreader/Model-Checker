package mc.compiler.token;

import mc.util.Location;
/*
  Could be dead code?
 */
public class DisplayTypeToken extends Token {

    private final String processType;

    public DisplayTypeToken(String processType, Location location){
        super(location);
        this.processType = processType;
    }

    @Override
    public String toString(){
        return processType;
    }

  public String getProcessType() {
    return this.processType;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof DisplayTypeToken)) return false;
    final DisplayTypeToken other = (DisplayTypeToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$processType = this.getProcessType();
    final Object other$processType = other.getProcessType();
    if (this$processType == null ? other$processType != null : !this$processType.equals(other$processType))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof DisplayTypeToken;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $processType = this.getProcessType();
    result = result * PRIME + ($processType == null ? 43 : $processType.hashCode());
    return result;
  }
}