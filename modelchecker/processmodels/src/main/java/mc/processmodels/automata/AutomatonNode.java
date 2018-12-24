package mc.processmodels.automata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import mc.Constant;
import mc.compiler.Guard;
import mc.compiler.ast.ASTNode;
import mc.processmodels.ProcessModelObject;

/**
 * Created by sheriddavi on 24/01/17.
 */

public class AutomatonNode extends ProcessModelObject implements Comparable<AutomatonNode> {

  // fields
  private Map<String, AutomatonEdge> incomingEdges;
  private Map<String, AutomatonEdge> outgoingEdges;

  @Getter
  @Setter
  private String label;

  @Getter
  @Setter
  private boolean startNode;
  @Getter
  @Setter
  private boolean stopNode;
  @Getter
  @Setter
  private boolean errorNode;
  @Getter
  @Setter
  private boolean quiescent;


  @Getter
  @Setter
  private int colour;

  @Getter
  @Setter
  private Set<String> references;

  @Getter
  @Setter
  private int labelNumber;  // should allways be unique  used in product

  @Getter
  @Setter
  private mc.compiler.Guard guard;

  @Getter
  @Setter
  private Map<String, Object> variables = new HashMap<>();

  public AutomatonNode(String id) {
    super(id, "node");
    this.label = null;
    incomingEdges = new HashMap<>();
    outgoingEdges = new HashMap<>();
    this.colour = 999;
  }


  public void copyProperties(AutomatonNode fromThisNode) {
    //System.out.println("from "+fromThisNode.myString());
    this.startNode = fromThisNode.isStartNode();
    this.stopNode = fromThisNode.isStopNode();
    if (fromThisNode.isErrorNode()) this.setErrorNode(true);
    this.colour = fromThisNode.getColour();

    this.labelNumber = fromThisNode.getLabelNumber();

    this.guard = fromThisNode.getGuard();
    this.references = fromThisNode.getReferences();
    this.variables = fromThisNode.getVariables();
    this.quiescent = fromThisNode.quiescent;
    //System.out.println("to   "+this.myString());
  }

  public AutomatonNode copyNode() {
    AutomatonNode nd = new AutomatonNode(this.getId());
    nd.copyProperties(this);
    nd.outgoingEdges = outgoingEdges;
    nd.incomingEdges = incomingEdges;
    return nd;
  }

  public boolean equalId(AutomatonNode nd) {
    return this.getId().equals(nd.getId());
  }
  public boolean observeDistinct(AutomatonNode nd) {
    boolean obsSame = (this.isStartNode()==nd.isStartNode() &&
                       this.isSTOP()==nd.isSTOP());
    return !obsSame;
  }

  public void copyPropertiesFromASTNode(ASTNode fromThisNode) {
    if (fromThisNode.getModelVariables() != null) {
      this.variables = fromThisNode.getModelVariables();
    }

//Needs a deep copy
    if (fromThisNode.getGuard() != null) {
      this.guard = ((Guard) fromThisNode.getGuard()).copy();
    } else {
      this.guard = null;
    }
  }

  /**
   * Creates an intersection between two nodes,
   *
   * @param withThisNode The second node to intersect with
   * @return A node that is the combined result of the intersection
   */

  public AutomatonNode createIntersection(AutomatonNode withThisNode) {
    AutomatonNode newNode = new AutomatonNode("");


    if (this.startNode == withThisNode.isStartNode()) {
      newNode.setStartNode(this.startNode);
    }
    if (this.errorNode == withThisNode.isErrorNode()) {
      newNode.setErrorNode(this.errorNode);
    }

    if (this.colour == withThisNode.getColour()) {
      newNode.setColour(this.colour);
    }

    if (this.references != null && withThisNode.getReferences() != null) {
      if (this.references.equals(withThisNode.getReferences())) {
        newNode.setReferences(this.references);
      }
    }

    if (this.labelNumber == withThisNode.getLabelNumber()) {
      newNode.setLabelNumber(this.labelNumber);
    }

    if (this.guard != null && withThisNode.getGuard() != null) {
      if (this.guard.equals(withThisNode.getGuard())) {
        newNode.setGuard(this.guard);
      }
    }

    return newNode;
  }

  public boolean isTerminal() {
    return isERROR() || isSTOP();
  }

  public boolean isSTOP() {
    return this.stopNode;
  }

  public boolean isERROR() {
    return this.errorNode;
  }

  public boolean isExternal() {
    return isERROR() || isSTOP() || isStartNode();
  }

  public boolean isQuiescent() {
    Set<AutomatonEdge> shouting =
      outgoingEdges.values().stream().filter(x -> x.getLabel().endsWith(Constant.BROADCASTSoutput)).collect(Collectors.toSet());
    return shouting.size() == 0;
  }

  public List<AutomatonEdge> getIncomingEdges() {
    return new ArrayList<>(incomingEdges.values());
  }

  public boolean addIncomingEdge(AutomatonEdge edge) {
    if (!incomingEdges.containsKey(edge.getId())) {
      incomingEdges.put(edge.getId(), edge);
      return true;
    }

    return false;
  }

  public boolean removeIncomingEdge(AutomatonEdge edge) {
    if (incomingEdges.containsKey(edge.getId())) {
      incomingEdges.remove(edge.getId());
      return true;
    }

    return false;
  }

  public List<AutomatonEdge> getOutgoingEdges() {
    return new ArrayList<>(outgoingEdges.values());
  }

  /*
     ALAS nodes and edges do not have unique keys!
   */
  public boolean addOutgoingEdge(AutomatonEdge edge) {
    if (!outgoingEdges.containsKey(edge.getId())) {
      outgoingEdges.put(edge.getId(), edge);
      return true;
    }

    return false;
  }

  public boolean removeOutgoingEdge(AutomatonEdge edge) {
    if (outgoingEdges.containsKey(edge.getId())) {
      outgoingEdges.remove(edge.getId());
      return true;
    }

    return false;
  }
  public String stringString(){
    StringBuilder sb = new StringBuilder();
    sb.append("Node "+getId()+" in "+incomingEdges.size()+" out "+outgoingEdges.size()+"\n");
    for (AutomatonEdge ed: incomingEdges.values()){
      sb.append(ed.myString()+"\n");
    }
    for (AutomatonEdge ed: outgoingEdges.values()){
      sb.append(ed.myString()+"\n");
    }
    sb.append(myString());
    return sb.toString();
  }
  public String myString() {

    return "node " + this.getId() + " labNo " + labelNumber + " col " + this.colour +
      "   quies " + quiescent+ " error " + isERROR()  + " end " + isSTOP() + " start " + isStartNode()+"\n   " +
      "         out = " + outgoingEdges.keySet() + " in "+incomingEdges.keySet();
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    List<AutomatonEdge> incoming = getIncomingEdges();

    builder.append("node{\n");
    builder.append("\tid:").append(getId());


    if (isStartNode()) {
      builder.append(" (START)");
    }
    if (isERROR()) {
      builder.append(" ERROR ");
    }
    builder.append(" g= " + getGuard() + "\n");
    builder.append("\tincoming:{");
    for (int i = 0; i < incoming.size(); i++) {
      builder.append(incoming.get(i).getId());
      builder.append(", ");

    }
    builder.append("}\n");

    builder.append("\toutgoing:{");
    List<AutomatonEdge> outgoing = getOutgoingEdges();
    for (int i = 0; i < outgoing.size(); i++) {
      builder.append(outgoing.get(i).getId());
      if (i < outgoing.size() - 1) {
        builder.append(", ");
      }
    }
    builder.append("}\n}");

    return builder.toString();
  }

  public int compareTo(AutomatonNode nd) {
//System.out.println(this.getId()+" "+nd.getId()+" ** "+
//                    this.getLabel()+" "+nd.getLabel());
    int xout = this.getId().compareTo(nd.getId());
    if (xout == 0 && this.getLabel() != null && nd.getLabel() != null) {
      xout = this.getLabel().compareTo(nd.getLabel());
    }
    //System.out.println(" xout = "+xout);
    return xout;

  }
  /*
     Currently only used in QuiescentTrace
     flag events previously added
   */
  public Set<String> quiescentReadySet(boolean cong) {
    Set<String> out = getOutgoingEdges().stream().
         map(x -> x.getLabel()).
         filter(x->!x.equals(Constant.Quiescent)).
         collect(Collectors.toSet());

    return out;
  }


  public Set<String> readySet(boolean cong) {
    Set<String> out = getOutgoingEdges().stream().map(x -> x.getLabel()).collect(Collectors.toSet());
    if (cong) {
      if (isSTOP()) out.add(Constant.STOP);
    //  if (isERROR()) out.add(Constant.ERROR);  is Quiescent or empty
    //  if (quiescentCheck()) out.add(Constant.Quiescent);
      if (isStartNode())  out.add(Constant.Start);
    }
    return out;
  }
  /*
    if node dose not shout then it is quiescent
   */
  private boolean  quiescentCheck(){
    Set<AutomatonEdge> next = outgoingEdges.values().stream().
      filter(x->((x.getLabel().endsWith("!")))).
      collect(Collectors.toSet());
    return next.size()==0 && !this.isSTOP();
    //ERROR node Quiescent not STOP Node as stop used to model can do something with =>
  }
}
