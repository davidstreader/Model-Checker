package mc.compiler.token;

import mc.util.Location;

public class InfixFunctionToken extends Token{

    private String label;

    public InfixFunctionToken(String label, Location location) {
        super(location);
        this.label = label;

    }

    @Override
    public String toString(){
        return label;
    }

  public String getLabel() {
    return this.label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof InfixFunctionToken)) return false;
    final InfixFunctionToken other = (InfixFunctionToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$label = this.getLabel();
    final Object other$label = other.getLabel();
    if (this$label == null ? other$label != null : !this$label.equals(other$label)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof InfixFunctionToken;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $label = this.getLabel();
    result = result * PRIME + ($label == null ? 43 : $label.hashCode());
    return result;
  }
}
