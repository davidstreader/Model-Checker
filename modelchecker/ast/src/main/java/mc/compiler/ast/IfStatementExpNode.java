package mc.compiler.ast;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import java.util.Objects;
import lombok.Getter;
import lombok.ToString;
import mc.util.Location;

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
 * @see mc.compiler.Parser
 */
@ToString
public class IfStatementExpNode extends ASTNode {

  @Getter
  private BoolExpr condition;
  @Getter
  private ASTNode trueBranch;
  @Getter
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
    super(location);
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
    super(location);
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
}
