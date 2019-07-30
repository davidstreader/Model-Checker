package mc.compiler.ast;


import mc.util.Location;

import java.util.List;

/**
 * This represents an the quantifier \forall. Used to compute Galois connectons
 *   forall{X,..}( operation )
 *   X,...  is held in vars  and operation in op
 *
 * @author David Streader
 * @see ASTNode
 *
 */
public class ForAllNode extends OperationNode {

  private List<String> vars;
  private OperationNode op;
  /**
   * The symbol used in the operation.
   * <p>
   * e.g. {@code ~}
   */


  /**
   * first implies second
   *
   * @param Operation     the first Operation
   * @param location         the location within the users code where this takes
   *                         place {@link ASTNode#location}
   */
  public ForAllNode( OperationNode Operation, List<String> Vars, Location location) {
    // super(location,"Implies");
    super(location);
    if(Operation==null) {
      Throwable t = new Throwable(); t.printStackTrace();
      System.out.println("*******************\n\n");
    }
    op = Operation;
    this.vars = Vars;
    //this.setFlags(Collections.singletonList("*"));
    //System.out.println("forAll Location "+vars+ " "+ op.getOperation()+" "+op.getFlags()+" ");
  }
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("forAll");
    if (vars!= null && vars.size()>0) sb.append(vars);
    sb.append(op.myString());
    return sb.toString();
  }

  public List<String> getBound(){
    return vars;
  }

  public List<String> getVars() {
    return this.vars;
  }

  public OperationNode getOp() {
    return this.op;
  }

  public void setVars(List<String> vars) {
    this.vars = vars;
  }

  public void setOp(OperationNode op) {
    this.op = op;
  }

  public String toString() {
    return "ForAllNode(vars=" + this.getVars() + ", op=" + this.getOp() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ForAllNode)) return false;
    final ForAllNode other = (ForAllNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$vars = this.getVars();
    final Object other$vars = other.getVars();
    if (this$vars == null ? other$vars != null : !this$vars.equals(other$vars)) return false;
    final Object this$op = this.getOp();
    final Object other$op = other.getOp();
    if (this$op == null ? other$op != null : !this$op.equals(other$op)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ForAllNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $vars = this.getVars();
    result = result * PRIME + ($vars == null ? 43 : $vars.hashCode());
    final Object $op = this.getOp();
    result = result * PRIME + ($op == null ? 43 : $op.hashCode());
    return result;
  }
}

