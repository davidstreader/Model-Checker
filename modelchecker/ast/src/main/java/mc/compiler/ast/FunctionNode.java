package mc.compiler.ast;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Expr;
import mc.util.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 *  @see ASTNode
 */
public class FunctionNode extends ASTNode {

  /**
   * The name of the function. This is used to lookup te correct function to call on the processes.
   *
   */
  private String function;
  /**
   * The processes from within the functions arguments.
   */
  private List<ASTNode> processes;
  /**
   * The arguments put into the function curly brace syntax.
   * Currently fixed at Compile Time - this includes the fixed alphabet of listeners
   *
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
    super(location,"Function");
    this.function = function;
    this.processes = new ArrayList<>(processes);

  }
  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append(function);
    if (flags.size()>0) sb.append(flags);
    sb.append("(");
    processes.stream().forEach(x->sb.append(x.myString()));
    sb.append(")");
    return sb.toString();

  }
  @Override
  public FunctionNode instantiate(String from , String to) {
    return new FunctionNode(function,processes.stream().
           map(x->x.instantiate(from,to)).collect(Collectors.toList()), getLocation());
  }

  public String getFunction() {
    return this.function;
  }

  public List<ASTNode> getProcesses() {
    return this.processes;
  }

  public ImmutableSet<String> getFlags() {
    return this.flags;
  }

  public boolean isPrune() {
    return this.prune;
  }

  public Map<String, Expr> getReplacements() {
    return this.replacements;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public void setProcesses(List<ASTNode> processes) {
    this.processes = processes;
  }

  public void setFlags(ImmutableSet<String> flags) {
    this.flags = flags;
  }

  public void setPrune(boolean prune) {
    this.prune = prune;
  }

  public void setReplacements(Map<String, Expr> replacements) {
    this.replacements = replacements;
  }

  public String toString() {
    return "FunctionNode(function=" + this.getFunction() + ", processes=" + this.getProcesses() + ", flags=" + this.getFlags() + ", prune=" + this.isPrune() + ", replacements=" + this.getReplacements() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof FunctionNode)) return false;
    final FunctionNode other = (FunctionNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$function = this.getFunction();
    final Object other$function = other.getFunction();
    if (this$function == null ? other$function != null : !this$function.equals(other$function)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof FunctionNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $function = this.getFunction();
    result = result * PRIME + ($function == null ? 43 : $function.hashCode());
    return result;
  }
}
