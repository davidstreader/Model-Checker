package mc.processmodels.petrinet.components;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Object;
import com.rits.cloning.Cloner;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModelObject;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PetriNetPlace extends ProcessModelObject implements Comparable<PetriNetPlace> {
  private Set<PetriNetEdge> incoming = new HashSet<>();
  private Set<PetriNetEdge> outgoing = new HashSet<>();
  private boolean start;
  private int colour = 0;
  private Set<Integer> startNos = new LinkedHashSet<>(); //Propably to hard to maintain so recompute when needed
  private String terminal = "";
  private Set<Integer> endNos = new LinkedHashSet<>(); //Propably to hard to maintain so recompute when needed
  //Used by interpretor to convert Petri Tree into Cyclic Net
  // Place with ref "X" will be glued to other Place with "X" fromRef
  private Set<String> references = new LinkedHashSet<>();      // where leaf is to be glued to
  private Set<String> leafRef = new LinkedHashSet<>();  // prior to gluing only on Leaf
  private Set<String> owners = new HashSet<>();  // this is needed in event Refinement and broadcast events
  public void cleanStart(){
    startNos = new LinkedHashSet<>();
    start = false;
  }
  public void cleanSTOP(){
    endNos = new LinkedHashSet<>();
    terminal = "";
  }
  public String getTerminal() {
    String x;
    if (terminal == null || terminal.equals(""))
        x= "____";
    else
        x = terminal;
    return x;
  }
  public Set<Integer> copyStartNos() {
    Set<Integer> out = new HashSet<>();
    for(Integer i: startNos) {
      out.add(i);
    }
    return out;
  }
  public Set<Integer> copyEndNos() {
    Set<Integer> out = new HashSet<>();
    for(Integer i: endNos) {
      out.add(i);
    }
    return out;
  }
  public Set<String> copyOwners() {
    Set<String> out = new HashSet<>();
    for(String i: owners) {
      out.add(i);
    }
    return out;
  }
  public boolean hasIncoming(PetriNetTransition tr) {

    for (PetriNetEdge ed: incoming) {
      //System.out.println(ed.getFrom().getId() + " ? "+ tr.getId());
      if (ed.getFrom().getId().equals(tr.getId())) {
        return true;
      }
    }
    return false;
  }
  public boolean hasOutgoing(PetriNetTransition tr) {
    for (PetriNetEdge ed: incoming) {
    //System.out.println(ed.getTo().getId() + " ? "+ tr.getId());
    if (ed.getTo().getId().equals(tr.getId())) {
      return true;
    }
  }
    return false;
  }
  public boolean equals(Object o) {
    if (!(o instanceof PetriNetPlace)) {
      return false;
    } else {
      if ( ((PetriNetPlace)o).getId().equals(getId()) )
        return true;
      else
        return false;
    }
  }
  public int compareTo(PetriNetPlace p){
    return getId().compareTo(p.getId());
  }
  public void addOwner(String ownerName) {
    //System.out.println("addOwner "+ownerName);
    owners.add(ownerName);
    //System.out.println("X");
  }
  public void addOwners(Set<String> ownersName) {
    //System.out.println(this.owners.toString());
    for(String o: ownersName) {
      //System.out.println("o "+o);
      owners.add(o);
    }
  }

  public int getMaxStartNo (){
    int out;
    Optional<Integer> i  = startNos.stream().max(Integer::compare);
    if (i.isPresent()) out = i.get();
    else out = 0;
  //System.out.println("getMaxStartNo for " + getId()+" is "+out);
    return out;
  }
  public int getMaxEndNo (){
    int out;
    Optional<Integer> i  = endNos.stream().max(Integer::compare);
    if (i.isPresent()) out = i.get();
    else out = 0;
    //System.out.println("getMaxStartNo for " + getId()+" is "+out);
    return out;
  }

  public void addRefefances(Set<String> inrefs){
    references.addAll(inrefs);
  }
  public void addFromRefefances(Set<String> inrefs){
    leafRef.addAll(inrefs);
  }
  public void removeEdge(PetriNetEdge ed){
    //this seems clumsy but many shorter version failed
    Set<PetriNetEdge> xin = new HashSet<>();
    for(PetriNetEdge edi: incoming){
      if (!edi.getId().equals(ed.getId())) {
        xin.add(edi);
      }
    }
    Set<PetriNetEdge> xout = new HashSet<>();
    for(PetriNetEdge edi: outgoing){
      if (!edi.getId().equals(ed.getId())) {
        xout.add(edi);
      }
    }
    incoming = xin;
    outgoing = xout;

    //System.out.println("removed in "+incoming.size()+" removed out "+outgoing.size());
  }
  public void addStartNo(int i){
   //System.out.println(this.getId()+" startNos " + startNos + " adding "+i);
    Set<Integer> Nos = new HashSet<>();
    for(Integer n : startNos) {
      Nos.add(n);
    }
    Nos.add(i);
    //boolean b = startNos.add( new Integer(i));
    //System.out.println("succss = "+b);
    startNos = Nos;
  }
  public void addEndNo(int i){
    Set<Integer> Nos = new HashSet<>();
    for(Integer n : endNos) {
      Nos.add(n);
    }
    Nos.add(i);
    endNos = Nos;
  }

  public PetriNetPlace(String id) {
    super(id, "PetriNetPlace");  //Beware id structure used else where
    startNos = new LinkedHashSet<>();
  }

  public boolean isTerminal() {
    return terminal != null && terminal.length() > 0;
  }
  public boolean isSTOP() { return terminal != null && terminal.equals(Constant.STOP);}
  public boolean isERROR() { return terminal != null && terminal.equals(Constant.ERROR);}



  public void copyProperties(PetriNetPlace toCopy) {
    start = toCopy.start;
    terminal = toCopy.terminal;
    references = toCopy.references;
    leafRef = toCopy.leafRef;
    startNos = toCopy.copyStartNos();
    endNos = toCopy.copyEndNos();
    owners = toCopy.copyOwners();
  }

  public void copyRefs(PetriNetPlace toCopy) {
    references = toCopy.references;
    leafRef = toCopy.leafRef;
  }

  public Set<PetriNetTransition> pre() {
    return incoming.stream()
        .map(PetriNetEdge::getFrom)
        .map(PetriNetTransition.class::cast)
        .distinct()
        .collect(Collectors.toSet());
  }

  public Set<PetriNetTransition> post() {
    return outgoing.stream()
        .map(PetriNetEdge::getTo)
        .map(PetriNetTransition.class::cast)
        .distinct()
        .collect(Collectors.toSet());
  }
  public Set<PetriNetTransition> postNotOpt() {
    return outgoing.stream()
      .filter(PetriNetEdge::notOptional)
      .map(PetriNetEdge::getTo)
      .map(PetriNetTransition.class::cast)
      .distinct()
      .collect(Collectors.toSet());
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("place{\n");
    if (isStart()) {
      builder.append("\tStarting Place\n");
    }
    if (isTerminal()) {
      builder.append("\tTermination: ").append(getTerminal());
    }
    builder.append("\tid:").append(getId());

    builder.append("\n");
    builder.append("\tincoming:{");

    for (PetriNetEdge edge : getIncoming()) {
      builder.append(edge.getId()).append(",");
    }

    builder.append("}\n");

    builder.append("\toutgoing:{");
    for (PetriNetEdge edge : getOutgoing()) {
      builder.append(edge.getId()).append(",");
    }
    builder.append("}\n}");

    return builder.toString();
  }

  public String myString(){
    return "Place "+this.getId()+
       " End "+this.getTerminal()+  " endnos "+ this.endNos +
      " Start "+ this.isStart()+ " "+this.startNos +
      " Own="+ this.owners +
      " col="+ this.getColour() +
      " ref "+references.toString()+" leafRef "+ leafRef.toString()+
      this.getIncoming().stream().map(ed->ed.getId()).reduce(" in  ",(x,y)->x+" "+y)+
      this.getOutgoing().stream().map(ed->ed.getId()).reduce(" out ",(x,y)->x+" "+y)
    ;
  }
  public ProcessModelObject copy() throws CompilationException {
    Cloner cloner = new Cloner();
    cloner.dontClone(Context.class);
    cloner.dontClone(Z3Object.class);
    cloner.dontClone(Expr.class);
    cloner.dontClone(BoolExpr.class);
    return cloner.deepClone(this);
  }

  public Set<PetriNetEdge> getIncoming() {
    return this.incoming;
  }

  public Set<PetriNetEdge> getOutgoing() {
    return this.outgoing;
  }

  public boolean isStart() {
    return this.start;
  }

  public int getColour() {
    return this.colour;
  }

  public Set<Integer> getStartNos() {
    return this.startNos;
  }

  public Set<Integer> getEndNos() {
    return this.endNos;
  }

  public Set<String> getReferences() {
    return this.references;
  }

  public Set<String> getLeafRef() {
    return this.leafRef;
  }

  public Set<String> getOwners() {
    return this.owners;
  }

  public void setIncoming(Set<PetriNetEdge> incoming) {
    this.incoming = incoming;
  }

  public void setOutgoing(Set<PetriNetEdge> outgoing) {
    this.outgoing = outgoing;
  }

  public void setStart(boolean start) {
    this.start = start;
  }

  public void setColour(int colour) {
    this.colour = colour;
  }

  public void setStartNos(Set<Integer> startNos) {
    this.startNos = startNos;
  }

  public void setTerminal(String terminal) {
    this.terminal = terminal;
  }

  public void setEndNos(Set<Integer> endNos) {
    this.endNos = endNos;
  }

  public void setReferences(Set<String> references) {
    this.references = references;
  }

  public void setLeafRef(Set<String> leafRef) {
    this.leafRef = leafRef;
  }

  public void setOwners(Set<String> owners) {
    this.owners = owners;
  }
}
