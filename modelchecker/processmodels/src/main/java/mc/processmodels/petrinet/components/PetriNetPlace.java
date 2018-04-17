package mc.processmodels.petrinet.components;

import java.util.*;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.processmodels.ProcessModelObject;

@EqualsAndHashCode(callSuper = true, exclude = {"incoming", "outgoing"})
@Data
public class PetriNetPlace extends ProcessModelObject {
  private static int sid = 0;
  private Set<PetriNetEdge> incoming = new HashSet<>();
  private Set<PetriNetEdge> outgoing = new HashSet<>();
  private boolean start;
  private Set<Integer> startNos = new LinkedHashSet<>();
  private String terminal = "";
  //Used by interpretor to convert Petri Tree into Cyclic Net
  // Place with ref "X" will be glued to other Place with "X" fromRef
  private Set<String> references = new LinkedHashSet<>();
  private Set<String> fromReferences = new LinkedHashSet<>();  // prior to gluing only on Leaf
  private Set<String> owners  = new LinkedHashSet<>();  //Owners of stop Net

  public int getMaxStartNo (){
    int out;
    Optional<Integer> i  = startNos.stream().max(Integer::compare);
    if (i.isPresent()) out = i.get();
    else out = 0;
  //System.out.println("getMaxStartNo for " + getId()+" is "+out);
    return out;
  }
  public void addRefefances(Set<String> inrefs){
    references.addAll(inrefs);
  }
  public void addFromRefefances(Set<String> inrefs){
    fromReferences.addAll(inrefs);
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
  public Set<String> getOwners(){
    return owners;
  }
  public void setOwners(Set<String> s) {owners = s;}
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
  public PetriNetPlace(String id) {
    super(id, "PetriNetPlace");  //Beware id structure used else where
    startNos = new LinkedHashSet<>();
  }

  public boolean isTerminal() {
    return terminal != null && terminal.length() > 0;
  }
  public boolean isSTOP() { return terminal != null && terminal.equals("STOP");}


  public PetriNetPlace copyPlace() {
    PetriNetPlace out = new PetriNetPlace(this.getId());
    out.copyProperties(this);
    out.setOutgoing(this.getOutgoing());
    out.setIncoming(this.getIncoming());
    return out;

  }
  public void copyProperties(PetriNetPlace toCopy) {
    start = toCopy.start;
    terminal = toCopy.terminal;
    references = toCopy.references;
    fromReferences = toCopy.fromReferences;
    owners = new HashSet<>(toCopy.owners);
    startNos = toCopy.getStartNos();
  }

  public void intersectionOf(PetriNetPlace place1, PetriNetPlace place2) {
    if (place1.isStart() && place2.isStart()) {
      start = true;
    }
    if (place1.isTerminal() && place2.isTerminal()) {
      terminal = "STOP";
    }

    if ("ERROR".equalsIgnoreCase(place1.getTerminal()) || "ERROR".equalsIgnoreCase(place2.getTerminal())) {
      terminal = "ERROR";
    }
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
    return "Place "+this.getId()+ " r "+references.toString()+" f "+fromReferences.toString()+
      this.getIncoming().stream().map(ed->ed.getId()).reduce(" in  ",(x,y)->x+" "+y)+
      this.getOutgoing().stream().map(ed->ed.getId()).reduce(" out ",(x,y)->x+" "+y) +
      " own "+getOwners()+
      " end "+this.getTerminal()+ " st "+ this.isStart()+ " "+this.startNos
      ;
  }
}
