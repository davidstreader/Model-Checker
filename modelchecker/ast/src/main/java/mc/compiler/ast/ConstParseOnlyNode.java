package mc.compiler.ast;

import mc.util.Location;

/**
 * The ConstNode represents a numerical literal value within the code.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 * @see IdentifierNode
 */
public class ConstParseOnlyNode extends ASTNode {

  /**
   * The numeric value associated with the node.
   */
  private int value = 0;
  private Double realValue = 0.0;
  private boolean isInt = true;

  /**
   * This initialises an instance of the node.
   *
   * @param value    the numerical value of the node {@link #value}
   * @param location the location in the code where this occurs
   */
  public ConstParseOnlyNode(int value, Location location) {
    super(location,"ConstParse");
    this.value = value;
  }
  public ConstParseOnlyNode(double value, Location location) {
    super(location,"ConstParse");
    this.realValue = value;
  }
  public String myString(){
    if (isInt)
        return " "+value;
    else
      return String.format("%.2f",realValue);
  }

  public int getValue() {
    return this.value;
  }

  public Double getRealValue() {
    return this.realValue;
  }

  public boolean isInt() {
    return this.isInt;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public void setRealValue(Double realValue) {
    this.realValue = realValue;
  }

  public void setInt(boolean isInt) {
    this.isInt = isInt;
  }

  public String toString() {
    return "ConstParseOnlyNode(value=" + this.getValue() + ", realValue=" + this.getRealValue() + ", isInt=" + this.isInt() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ConstParseOnlyNode)) return false;
    final ConstParseOnlyNode other = (ConstParseOnlyNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    if (this.getValue() != other.getValue()) return false;
    final Object this$realValue = this.getRealValue();
    final Object other$realValue = other.getRealValue();
    if (this$realValue == null ? other$realValue != null : !this$realValue.equals(other$realValue)) return false;
    if (this.isInt() != other.isInt()) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ConstParseOnlyNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    result = result * PRIME + this.getValue();
    final Object $realValue = this.getRealValue();
    result = result * PRIME + ($realValue == null ? 43 : $realValue.hashCode());
    result = result * PRIME + (this.isInt() ? 79 : 97);
    return result;
  }
}
