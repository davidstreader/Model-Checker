package mc.compiler.ast;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * ProcessNode holds a global process used in the code.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @author Jordan Smith
 * @see ProcessRootNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessNode extends ASTNode {

  /**
   * What kind of process this is.
   * <p>
   * Valid variables are "processes", "automata" and "petrinet"
   */
  private String type;

  /**
   * The identifier for this process (i.e. the name).
   */
  private String identifier;

  /**
   * The process data.
   */
  private ASTNode process;

  /**
   * The sub-processes data.
   */
  private List<LocalProcessNode> localProcesses;

  /**
   * The relabeling information, to be executed on the process.
   */
  private RelabelNode relabels;

  /**
   * The hiding information, to be executed on the process.
   */
  private HidingNode hiding;

  /**
   * TODO:.
   */
  private VariableSetNode variables;
  private InterruptNode interrupt;

  /**
   * Instantiate a new instance of process node.
   * <p>
   * This is for use with no hiding node.
   *
   * @param type           The type of process this is. This can be either {@code processes},
   *                       {@code automata}, or {@code petrinet}. {@link #type}
   * @param identifier     the "name" for the process. {@link #identifier}
   * @param process        the actual process itself. {@link #process}
   * @param localProcesses the subprocesses of the current process. {@link #localProcesses}
   * @param location       the location this process is within the users code.
   *                       {@link ASTNode#location}
   */
  public ProcessNode(String type, String identifier, ASTNode process,
                     List<LocalProcessNode> localProcesses, Location location) {
    this(type, identifier, process, localProcesses, null, location);
  }

  /**
   * Instantiate a new instance of process node.
   * <p>
   * This is for use with a hiding node.
   *
   * @param type           The type of process this is. This can be either {@code processes},
   *                       {@code automata}, or {@code petrinet}. {@link #type}
   * @param identifier     the "name" for the process. {@link #identifier}
   * @param process        the actual process itself. {@link #process}
   * @param localProcesses the subprocesses of the current process. {@link #localProcesses}
   * @param hiding         the hiding node containing instructions on how to hide some transitions
   *                       into tau events. {@link #hiding}
   * @param location       the location this process is within the users code.
   *                       {@link ASTNode#location}
   */
  public ProcessNode(String type, String identifier, ASTNode process,
                     List<LocalProcessNode> localProcesses, HidingNode hiding, Location location) {
    super(location);
    this.type = type;
    this.identifier = identifier;
    this.process = process;
    this.localProcesses = localProcesses;
    this.hiding = hiding;
    variables = null;
    interrupt = null;
  }

  public boolean hasRelabels() {
    return relabels != null;
  }

  public boolean hasHiding() {
    return hiding != null;
  }

  public boolean hasVariableSet() {
    return variables != null;
  }
}
