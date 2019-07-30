package mc.compiler.ast;

import mc.util.Location;

/**
 * AutomataNode contains one process that must be interpreted as an automata
 *
 */
public class AutomataNode extends ASTNode {

    /**
     * The first process used in the choice.
     * <p>
     * e.g. {@code B} in {@code A=B|C}
     */
    private ASTNode process;


    /**
     * Instantiate ChoiceNode.
     *
     * @param process  process to be interpreted as an automata
     * @param location      the location of the choice within the users code {@link ASTNode#location}
     */
    public AutomataNode(ASTNode process, Location location) {
        super(location,"Automata");
        this.process = process;

    }
    public String myString(){
        return "Automata "+process.myString();
    }

  public ASTNode getProcess() {
    return this.process;
  }

  public void setProcess(ASTNode process) {
    this.process = process;
  }

  public String toString() {
    return "AutomataNode(process=" + this.getProcess() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AutomataNode)) return false;
    final AutomataNode other = (AutomataNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$process = this.getProcess();
    final Object other$process = other.getProcess();
    if (this$process == null ? other$process != null : !this$process.equals(other$process)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AutomataNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $process = this.getProcess();
    result = result * PRIME + ($process == null ? 43 : $process.hashCode());
    return result;
  }
}

