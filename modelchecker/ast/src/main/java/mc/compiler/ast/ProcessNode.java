package mc.compiler.ast;

import mc.util.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class ProcessNode extends ASTNode {

  /**
   * What kind of process this is.
   * if empty interpreter will not build
   * Valid variables are "forcedautomata", "automata" and "petrinet"
   */
  private Set<String> type;

  /**  P1 = (a->STOP);  P1 is the identifier
   * The identifier for this process (i.e. the name).
   */
  private String identifier;

  /**
   * Used in equations quantified over Variable:Domain
   */
  private String domain = "*";

  /**
   * P1 = (a->STOP); has identifier P1 and  process = (a->STOP)
   */
  private ASTNode process;

  /**
   * The sub-processes data. One for each of the local processes
   * Only one for an indexed process after Parsing
   */
  private List<LocalProcessNode> localProcesses = new ArrayList<>();

  /**
   * The relabeling information, to be executed on the process.
   */
  private RelabelNode relabels;

  /**
   * The hiding information, to be executed on the process.
   */
  private HideNode hiding;

  /**
   * Process House${x,n}   has symbolicVariables x and n
   */
  private VariableSetNode symbolicVariables;
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
  public ProcessNode(String identifier, String domain, ASTNode process,
                     List<LocalProcessNode> localProcesses, Location location) {
    this(identifier, domain, process, localProcesses, null, location);
  }

  /**
   * Instantiate a NEW INSTANCE of a process.
   *     LocalProcessNode for new local process
   *     Idnetifier for referances
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
  public ProcessNode(String identifier, String domain, ASTNode process, List<LocalProcessNode> localProcesses,
                     HideNode hiding, Location location) {
    super(location,"Process");
    this.type = new HashSet<>();
    this.identifier = identifier;
    this.process =  process;
    this.localProcesses = localProcesses;
    this.hiding = hiding;
    this.domain = domain;
    symbolicVariables = null;
    interrupt = null;
  }
  /*
     Used parsing FT forall [i:0..N](p)
   */
  public ProcessNode(String identifier,  ASTNode process,  Location location) {
    super(location,"Process");
    this.type = new HashSet<>();
    this.identifier = identifier;
    this.process =  process;
    symbolicVariables = null;
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

  public boolean hasSymbolicVariableSet() {
    return symbolicVariables != null;
  }
  public String getDomain() {return domain;}


  public String wholeString(){
    return identifier+" -> "+ process.wholeString();

  }
  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("ProcessNode "+identifier+" process "+process.myString() +" Local: "+localProcesses.size());
    if ( localProcesses.size()>0) sb.append("\n ");
    for(LocalProcessNode lpn: localProcesses){
      sb.append(lpn.myString()+"\n ");
    }
   if (symbolicVariables != null) sb.append(" Hidden var "+symbolicVariables.myString());
    return sb.toString();
  }

  public Set<String> getType() {
    return this.type;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public ASTNode getProcess() {
    return this.process;
  }

  public List<LocalProcessNode> getLocalProcesses() {
    return this.localProcesses;
  }

  public RelabelNode getRelabels() {
    return this.relabels;
  }

  public HideNode getHiding() {
    return this.hiding;
  }

  public VariableSetNode getSymbolicVariables() {
    return this.symbolicVariables;
  }

  public InterruptNode getInterrupt() {
    return this.interrupt;
  }

  public void setType(Set<String> type) {
    this.type = type;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void setProcess(ASTNode process) {
    this.process = process;
  }

  public void setLocalProcesses(List<LocalProcessNode> localProcesses) {
    this.localProcesses = localProcesses;
  }

  public void setRelabels(RelabelNode relabels) {
    this.relabels = relabels;
  }

  public void setHiding(HideNode hiding) {
    this.hiding = hiding;
  }

  public void setSymbolicVariables(VariableSetNode symbolicVariables) {
    this.symbolicVariables = symbolicVariables;
  }

  public void setInterrupt(InterruptNode interrupt) {
    this.interrupt = interrupt;
  }

  public String toString() {
    return "ProcessNode(type=" + this.getType() + ", identifier=" + this.getIdentifier() + ", domain=" + this.getDomain() + ", process=" + this.getProcess() + ", localProcesses=" + this.getLocalProcesses() + ", relabels=" + this.getRelabels() + ", hiding=" + this.getHiding() + ", symbolicVariables=" + this.getSymbolicVariables() + ", interrupt=" + this.getInterrupt() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ProcessNode)) return false;
    final ProcessNode other = (ProcessNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$type = this.getType();
    final Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    final Object this$identifier = this.getIdentifier();
    final Object other$identifier = other.getIdentifier();
    if (this$identifier == null ? other$identifier != null : !this$identifier.equals(other$identifier)) return false;
    final Object this$domain = this.getDomain();
    final Object other$domain = other.getDomain();
    if (this$domain == null ? other$domain != null : !this$domain.equals(other$domain)) return false;
    final Object this$process = this.getProcess();
    final Object other$process = other.getProcess();
    if (this$process == null ? other$process != null : !this$process.equals(other$process)) return false;
    final Object this$localProcesses = this.getLocalProcesses();
    final Object other$localProcesses = other.getLocalProcesses();
    if (this$localProcesses == null ? other$localProcesses != null : !this$localProcesses.equals(other$localProcesses))
      return false;
    final Object this$relabels = this.getRelabels();
    final Object other$relabels = other.getRelabels();
    if (this$relabels == null ? other$relabels != null : !this$relabels.equals(other$relabels)) return false;
    final Object this$hiding = this.getHiding();
    final Object other$hiding = other.getHiding();
    if (this$hiding == null ? other$hiding != null : !this$hiding.equals(other$hiding)) return false;
    final Object this$symbolicVariables = this.getSymbolicVariables();
    final Object other$symbolicVariables = other.getSymbolicVariables();
    if (this$symbolicVariables == null ? other$symbolicVariables != null : !this$symbolicVariables.equals(other$symbolicVariables))
      return false;
    final Object this$interrupt = this.getInterrupt();
    final Object other$interrupt = other.getInterrupt();
    if (this$interrupt == null ? other$interrupt != null : !this$interrupt.equals(other$interrupt)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ProcessNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    final Object $identifier = this.getIdentifier();
    result = result * PRIME + ($identifier == null ? 43 : $identifier.hashCode());
    final Object $domain = this.getDomain();
    result = result * PRIME + ($domain == null ? 43 : $domain.hashCode());
    final Object $process = this.getProcess();
    result = result * PRIME + ($process == null ? 43 : $process.hashCode());
    final Object $localProcesses = this.getLocalProcesses();
    result = result * PRIME + ($localProcesses == null ? 43 : $localProcesses.hashCode());
    final Object $relabels = this.getRelabels();
    result = result * PRIME + ($relabels == null ? 43 : $relabels.hashCode());
    final Object $hiding = this.getHiding();
    result = result * PRIME + ($hiding == null ? 43 : $hiding.hashCode());
    final Object $symbolicVariables = this.getSymbolicVariables();
    result = result * PRIME + ($symbolicVariables == null ? 43 : $symbolicVariables.hashCode());
    final Object $interrupt = this.getInterrupt();
    result = result * PRIME + ($interrupt == null ? 43 : $interrupt.hashCode());
    return result;
  }
}
