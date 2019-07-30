package mc.compiler.token;

import mc.util.Location;

public class ActionToken extends Token {

	private String action;

	public ActionToken(String action, Location location){
		super(location);
		this.action = action;
	}

	@Override
	public String toString(){
		return action;
	}

  public String getAction() {
    return this.action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ActionToken)) return false;
    final ActionToken other = (ActionToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$action = this.getAction();
    final Object other$action = other.getAction();
    if (this$action == null ? other$action != null : !this$action.equals(other$action)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ActionToken;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $action = this.getAction();
    result = result * PRIME + ($action == null ? 43 : $action.hashCode());
    return result;
  }
}