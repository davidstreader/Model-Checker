package mc.compiler.token;

import mc.util.Location;

public class CastToken extends Token{

    private String castType;

    public CastToken(String type, Location loc) {
        super(loc);
        castType = type;
    }

    @Override
    public String toString(){
        return castType;
    }

  public String getCastType() {
    return this.castType;
  }

  public void setCastType(String castType) {
    this.castType = castType;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof CastToken)) return false;
    final CastToken other = (CastToken) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$castType = this.getCastType();
    final Object other$castType = other.getCastType();
    if (this$castType == null ? other$castType != null : !this$castType.equals(other$castType)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof CastToken;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $castType = this.getCastType();
    result = result * PRIME + ($castType == null ? 43 : $castType.hashCode());
    return result;
  }
}