package mc.compiler.ast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * ProcessNode holds a global process used in the code.
 * Looks more like a part of an AST holds information about relabeling and
 * hiding yet to be done
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
   * if empty interpreter will not build
   * Valid variables are "forcedautomata", "automata" and "petrinet"
   */
  private Set<String> type;

  /**
   * The identifier for this process (i.e. the name).
   */
  private String identifier;

  /**
   * The process data.
   */
  private ASTNode process;

  /**
   * The sub-processes data. One for each of the local processes
   * Only one for an indexed process after Parsing
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
   * @param identifier     the "name" for the process. {@link #identifier}
   * @param process        the actual process itself. {@link #process}
   * @param localProcesses the subprocesses of the current process. {@link #localProcesses}
   * @param location       the location this process is within the users code.
   *                       {@link ASTNode#location}
   */
  public ProcessNode(String identifier, ASTNode process,
                     List<LocalProcessNode> localProcesses, Location location) {
    this(identifier, process, localProcesses, null, location);
  }

  /**
   * Instantiate a new instance of process node.
   * <p>
   * This is for use with a hiding node.
   *
   * @param identifier     the "name" for the process. {@link #identifier}
   * @param process        the actual process itself. {@link #process}
   * @param localProcesses the subprocesses of the current process. {@link #localProcesses}
   * @param hiding         the hiding node containing instructions on how to hide some transitions
   *                       into tau events. {@link #hiding}
   * @param location       the location this process is within the users code.
   *                       {@link ASTNode#location}
   */
  public ProcessNode(String identifier, ASTNode process, List<LocalProcessNode> localProcesses,
                     HidingNode hiding, Location location) {
    super(location);
    this.type = new HashSet<>();
    this.identifier = identifier;
    this.process = process;
    this.localProcesses = localProcesses;
    this.hiding = hiding;
    variables = null;
    interrupt = null;
  }

  public boolean addType(String type) {
    return this.type.add(type);
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
