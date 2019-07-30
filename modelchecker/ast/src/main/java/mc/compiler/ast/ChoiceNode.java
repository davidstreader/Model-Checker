package mc.compiler.ast;

import mc.util.Location;

/**
 * ChoiceNode represents a choice in the syntax of the LTS. When used, this appears as two (or more)
 * possible actions happening from the same state.
 * <p>
 * Syntax: {@code Choice :: Process "|" Process}
 *
 * @author Jacob Beal
 * @see ASTNode
 * @see FunctionNode
 * @see CompositeNode
 */
public class ChoiceNode extends ASTNode {

  /**
   * The first process used in the choice.
   * <p>
   * e.g. {@code B} in {@code A=B|C}
   */
  private ASTNode firstProcess;

  /**
   * The second process used in the choice.
   * <p>
   * e.g. {@code C} in {@code A=B|C}
   */
  private ASTNode secondProcess;

  /**
   * Instantiate ChoiceNode.
   *
   * @param firstProcess  the first process branch for the 'choice' {@link #firstProcess}
   * @param secondProcess the second processbranch for the 'choice' {@link #secondProcess}
   * @param location      the location of the choice within the users code {@link ASTNode#location}
   */
  public ChoiceNode(ASTNode firstProcess, ASTNode secondProcess, Location location) {
    super(location,"Choice");
    this.firstProcess = firstProcess;
    this.secondProcess = secondProcess;
  }

  @Override
  public String myString(){
     StringBuilder sb = new StringBuilder();
    sb.append("("+firstProcess.myString()  +"|");
    sb.append( secondProcess.myString()+")");
    return sb.toString();
  }

  public ASTNode getFirstProcess() {
    return this.firstProcess;
  }

  public ASTNode getSecondProcess() {
    return this.secondProcess;
  }

  public void setFirstProcess(ASTNode firstProcess) {
    this.firstProcess = firstProcess;
  }

  public void setSecondProcess(ASTNode secondProcess) {
    this.secondProcess = secondProcess;
  }

  public String toString() {
    return "ChoiceNode(firstProcess=" + this.getFirstProcess() + ", secondProcess=" + this.getSecondProcess() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ChoiceNode)) return false;
    final ChoiceNode other = (ChoiceNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$firstProcess = this.getFirstProcess();
    final Object other$firstProcess = other.getFirstProcess();
    if (this$firstProcess == null ? other$firstProcess != null : !this$firstProcess.equals(other$firstProcess))
      return false;
    final Object this$secondProcess = this.getSecondProcess();
    final Object other$secondProcess = other.getSecondProcess();
    if (this$secondProcess == null ? other$secondProcess != null : !this$secondProcess.equals(other$secondProcess))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ChoiceNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $firstProcess = this.getFirstProcess();
    result = result * PRIME + ($firstProcess == null ? 43 : $firstProcess.hashCode());
    final Object $secondProcess = this.getSecondProcess();
    result = result * PRIME + ($secondProcess == null ? 43 : $secondProcess.hashCode());
    return result;
  }
}
