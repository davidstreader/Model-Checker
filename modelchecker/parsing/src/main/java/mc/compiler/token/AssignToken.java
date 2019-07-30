package mc.compiler.token;

import mc.processmodels.ProcessType;
import mc.util.Location;

public class AssignToken extends SymbolToken {
    ProcessType pType = ProcessType.PETRINET;

    public AssignToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "=";
	}

  public ProcessType getPType() {
    return this.pType;
  }

  public void setPType(ProcessType pType) {
    this.pType = pType;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AssignToken)) return false;
    final AssignToken other = (AssignToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$pType = this.getPType();
    final Object other$pType = other.getPType();
    if (this$pType == null ? other$pType != null : !this$pType.equals(other$pType)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AssignToken;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $pType = this.getPType();
    result = result * PRIME + ($pType == null ? 43 : $pType.hashCode());
    return result;
  }
}