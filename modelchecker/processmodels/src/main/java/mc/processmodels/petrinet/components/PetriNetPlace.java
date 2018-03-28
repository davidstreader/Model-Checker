package mc.processmodels.petrinet.components;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
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
  private String terminal = "";
  //Used by interpretor to convert Petri Tree into Cyclic Net
  // Place with ref "X" will be glued to other Place with "X" fromRef
  private Set<String> references = new LinkedHashSet<>();
  private Set<String> fromReferences = new LinkedHashSet<>();  // prior to gluing only on Leaf
  private Set<String> owners  = new LinkedHashSet<>();  //Owners of stop Net

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

  public Set<String> getOwners(){
    return owners;
  }
  public void setOwners(Set<String> s) {owners = s;}
  public PetriNetPlace(String id) {

    super(id, "PetriNetPlace");  //Beware id structure used else where

  }

  public boolean isTerminal() {
    return terminal != null && terminal.length() > 0;
  }

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
      " end "+this.getTerminal()+ " st "+ this.isStart()
      ;
  }
}
