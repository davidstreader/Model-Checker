package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * The LocalProcessNode covers "local processes", a processes defined as a child of a parent
 * process.
 * <p>
 * Note: Local Processes may have indexes
 * <p>
 * This is {@code SUBPROCESS :: PROCESS("," PROCESS)*"."}.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 * @see ProcessNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LocalProcessNode extends ASTNode {

  /**
   * The "label" of the process.
   *     C[$i][2][$k] implies variable $i and $k are symbolic
   *     variable j is expanded for automata node where j=2
   */
  private String identifier;
  /**
   * The valid indexes this LocalProcess may have.
   */
  private RangesNode ranges;
  /**
   * The process itself.
   */
  private ASTNode process;

  /**
   * Initialises a new LocalProcessNode.
   *
   * @param identifier the name of the process {@link #identifier}
   * @param ranges     the valid range (if any) this process may use for indexing {@link #ranges}
   * @param process    the contents of the process {@link #process}
   * @param location   Where this LocalProcess is within the users code {@link ASTNode#location}
   */
  public LocalProcessNode(String identifier, RangesNode ranges,
                          ASTNode process, Location location) {
    super(location,"LocelProcess");
    this.identifier = identifier;
    this.ranges = ranges;
    this.process = process;
  }
  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append(" Local Process "+identifier+" process ");
    if (process!=null) sb.append(process.myString());
    sb.append(" ranges ");
    if (ranges!=null) sb.append(ranges.myString());
    return sb.toString();
  }
}
