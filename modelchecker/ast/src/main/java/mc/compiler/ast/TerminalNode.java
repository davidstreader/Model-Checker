package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@Data
@EqualsAndHashCode(callSuper = true)
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
    super(location);
    this.terminal = terminal;
  }
}
