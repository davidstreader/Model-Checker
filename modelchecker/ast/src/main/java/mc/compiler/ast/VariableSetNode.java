package mc.compiler.ast;

import mc.util.Location;

import java.util.Set;

/**
 * This is used only in symbolic representation, in particular with index freezing.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @see RangesNode
 */
public class VariableSetNode extends ASTNode {

  /**
   * The variables to be frozen.
   */
  private final Set<String> variables;

  /**
   * Initialise a new instance of VariableSetNode.
   *
   * @param variables the variables to be frozen {@link #variables}
   * @param location  where within the users code where this node appears {@link ASTNode#location}
   */
  public VariableSetNode(Set<String> variables, Location location) {
    super(location,"Variable");
    this.variables = variables;
  }
  @Override
  public String myString(){
    return variables.toString();
  }

  public Set<String> getVariables() {
    return this.variables;
  }

  public String toString() {
    return "VariableSetNode(variables=" + this.getVariables() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof VariableSetNode)) return false;
    final VariableSetNode other = (VariableSetNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$variables = this.getVariables();
    final Object other$variables = other.getVariables();
    if (this$variables == null ? other$variables != null : !this$variables.equals(other$variables)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof VariableSetNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $variables = this.getVariables();
    result = result * PRIME + ($variables == null ? 43 : $variables.hashCode());
    return result;
  }
}
