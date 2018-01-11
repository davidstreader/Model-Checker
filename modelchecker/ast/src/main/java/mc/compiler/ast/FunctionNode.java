package mc.compiler.ast;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Expr;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * This node manages functions used within the users code.
 * Functions are syntactically {@code f(x)} in style
 * <p>
 * By default, the following functions exist {@code abstract}, {@code hide}, {@code nfa2dfa},
 * {@code prune}, {@code safe}, {@code simp}
 * <p>
 * The gramatical style of these functions are
 * {@code FUNCTION:: FUNCTIONNAME ["{" ARGUMENTS "}"]"("(PROCESS ", ")* PROCESS")"}
 *
 * @author Sanjay Govind
 * @author David Sheridan
 * @author Jacob Beal
 * @see mc.plugins.IProcessFunction
 * @see ASTNode
 */
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"processes", "flags", "prune", "replacements"})
public class FunctionNode extends ASTNode {

  /**
   * The name of the function. This is used to lookup te correct function to call on the processes.
   *
   * @see mc.plugins.IProcessFunction#getFunctionName()
   */
  private String function;
  /**
   * The processes from within the functions arguments.
   */
  private List<ASTNode> processes;
  /**
   * The arguments put into the function curly brace syntax.
   *
   * @see mc.plugins.IProcessFunction#getValidFlags()
   */
  private ImmutableSet<String> flags;

  /**
   * Whether or not prune should be done afterwards.
   */
  private boolean prune;

  /**
   * Special case for simplification, requires replacement of expressions.
   */
  private Map<String, Expr> replacements;

  /**
   * Initialise a new instance of the function node.
   *
   * @param function  the name of the function (e.g. {@code simp}) which is used to lookup
   *                  the correct operation to perform {@link #function}.
   * @param processes the process arguments of the function {@link #processes}.
   * @param location  the location of the function within the users code {@link ASTNode#location}
   */
  public FunctionNode(String function, List<ASTNode> processes, Location location) {
    super(location);
    this.function = function;
    this.processes = new ArrayList<>(processes);
  }
}
