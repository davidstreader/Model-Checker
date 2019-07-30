package mc.compiler.ast;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import mc.util.Location;

import java.util.Objects;

/**
 *
 * This covers an "if" statement, or a "when" statement, for conditional transitions.
 * <p>
 * Gramatically there are two forms:
 * IFSTMT :: "if" BOOLEAN_EXPR then PROCESS ["else" PROCESS]
 * IFSTMT :: "when" BOOLEAN_EXPR PROCESS
 *
 * When building processses evaluated in Expander NOT in Interpreter.
 * Also evaluated in operations
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 */
public class IfStatementExpNode extends ASTNode {
  private BoolExpr condition;
  private ASTNode trueBranch;
  private ASTNode falseBranch;
  private Context z3Context;

  /**
   * Instantiate a new instance of an IfStatement.
   *
   * @param condition  The boolean expression that determines if the
   *                   true branch is executed {@link #condition}
   * @param trueBranch The branch directly after the "then",
   *                   what happens when {@link #condition} is true, {@link #trueBranch}
   * @param location   Where in the users code this appears {@link ASTNode#location}
   * @param z3Context  The z3 context for evaluating the boolean
   *                   expression passed {@link #z3Context}
   */
  public IfStatementExpNode(BoolExpr condition, ASTNode trueBranch,
                            Location location, Context z3Context) {
    super(location,"ifStatment");
    this.condition = condition;
    this.trueBranch = trueBranch;
    falseBranch = null;
    this.z3Context = z3Context;
  }

  /**
   * Instantiate a new instance of an IfStatement of the basic if .. then,
   * with the addition of else.
   *
   * @param condition   The boolean expression that determines if the
   *                    true branch is executed {@link #condition}
   * @param trueBranch  The branch directly after the "then",
   *                    what happens when {@link #condition} is true, {@link #trueBranch}
   * @param falseBranch The branch directly after the "else",
   *                    what happens when {@link #condition} is false, {@link #falseBranch}
   * @param location    Where in the users code this appears {@link ASTNode#location}
   * @param z3Context   The z3 context for evaluating the boolean
   *                    expression passed {@link #z3Context}
   */
  public IfStatementExpNode(BoolExpr condition, ASTNode trueBranch, ASTNode falseBranch,
                            Location location, Context z3Context) {
    super(location,"IfStatment");
    this.condition = condition;
    this.trueBranch = trueBranch;
    this.falseBranch = falseBranch;
    this.z3Context = z3Context;
  }

  /**
   * Whether the {@link #falseBranch} exists.
   *
   * @return whether there is a false branch
   */
  public boolean hasFalseBranch() {
    return falseBranch != null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), condition, trueBranch, falseBranch);
  }

  @Override
  public boolean equals(Object obj) {
    boolean result = super.equals(obj);
    if (!result) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj instanceof IfStatementExpNode) {
      IfStatementExpNode node = (IfStatementExpNode) obj;
      if (z3Context.mkEq(condition, node.getCondition()).simplify().isFalse()) {
        return false;
      }
      if (!trueBranch.equals(node.getTrueBranch())) {
        return false;
      }
      if (falseBranch == null && node.hasFalseBranch() || falseBranch != null
          && !node.hasFalseBranch()) {
        return false;
      }
      return falseBranch == null || falseBranch.equals(node.getFalseBranch());
    }

    return false;
  }

  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("If ");
    if (condition != null)
      sb.append("("+condition.toString()+")");
    if (trueBranch != null)
      sb.append(trueBranch.myString());
    sb.append(" else ");
    if (falseBranch != null)
      sb.append(falseBranch.myString());

    return sb.toString();
  }

  public String toString() {
    return "IfStatementExpNode(condition=" + this.condition + ", trueBranch=" + this.trueBranch + ", falseBranch=" + this.falseBranch + ", z3Context=" + this.z3Context + ")";
  }

  public BoolExpr getCondition() {
    return this.condition;
  }

  public ASTNode getTrueBranch() {
    return this.trueBranch;
  }

  public ASTNode getFalseBranch() {
    return this.falseBranch;
  }

  public void setCondition(BoolExpr condition) {
    this.condition = condition;
  }

  public void setTrueBranch(ASTNode trueBranch) {
    this.trueBranch = trueBranch;
  }

  public void setFalseBranch(ASTNode falseBranch) {
    this.falseBranch = falseBranch;
  }
}
