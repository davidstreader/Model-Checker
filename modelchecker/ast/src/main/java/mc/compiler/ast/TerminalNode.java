package mc.compiler.ast;

import mc.util.Location;

/**
 * This represents a token that signifies the end of a process.
 * <p>
 * Syntactically this is {@code TERMINAL :: "STOP"|"ERROR"}
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @author Jordan Smith
 * @see ASTNode
 */
public class TerminalNode extends ASTNode {

  /**
   * The type of terminal statement this is.
   * The valid values of this are: "STOP" and "ERROR".
   */
  private final String terminal;

  /**
   * Instantiate a new TerminalNode.
   *
   * @param terminal the type of termination this is (STOP,ERROR). {@link #terminal}
   * @param location the location of this token within the users code. {@link ASTNode#location}
   */
  public TerminalNode(String terminal, Location location) {
    super(location,"Terminal");
    this.terminal = terminal;
  }
  public String myString(){
    return terminal;
  }

  public String getTerminal() {
    return this.terminal;
  }

  public String toString() {
    return "TerminalNode(terminal=" + this.getTerminal() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof TerminalNode)) return false;
    final TerminalNode other = (TerminalNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$terminal = this.getTerminal();
    final Object other$terminal = other.getTerminal();
    if (this$terminal == null ? other$terminal != null : !this$terminal.equals(other$terminal)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof TerminalNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $terminal = this.getTerminal();
    result = result * PRIME + ($terminal == null ? 43 : $terminal.hashCode());
    return result;
  }
}
