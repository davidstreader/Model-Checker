package mc.compiler.token;

import mc.util.Location;

public class DecimalToken extends Token {

    private Double real;

    public DecimalToken(Double dbl, Location location){
        super(location);
        this.real = dbl;
    }

    @Override
    public String toString(){
        return "" + String.format("%.2f", real);
    }

  public Double getReal() {
    return this.real;
  }

  public void setReal(Double real) {
    this.real = real;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof DecimalToken)) return false;
    final DecimalToken other = (DecimalToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$real = this.getReal();
    final Object other$real = other.getReal();
    if (this$real == null ? other$real != null : !this$real.equals(other$real)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof DecimalToken;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $real = this.getReal();
    result = result * PRIME + ($real == null ? 43 : $real.hashCode());
    return result;
  }
}