package mc.compiler.ast;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * This is used only in symbolic representation, in particular with index freezing.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @see RangesNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
}
