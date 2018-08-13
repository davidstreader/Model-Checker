package mc.processmodels.petrinet.components;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import mc.processmodels.ProcessModelObject;

@EqualsAndHashCode(callSuper = true, exclude = {"incoming", "outgoing"})

public class PetriNetTransition extends ProcessModelObject {
  @Getter
  @Setter
  String label;
  @Getter
  @Setter
  Set<PetriNetEdge> incoming = new HashSet<>();
  public Set<PetriNetEdge> copyIncoming() {
    //System.out.println("in size "+incoming.size());
    Set<PetriNetEdge> out = new HashSet<>();
    for(PetriNetEdge ed: incoming){
      //System.out.println("ed "+ed.myString());
      out.add(ed);
    }
    return out;
  }
  @Getter
  @Setter
  Set<PetriNetEdge> outgoing = new HashSet<>();
  public Set<PetriNetEdge> copyOutgoing() {
    //System.out.println("out size "+outgoing.size());
    Set<PetriNetEdge> out = new HashSet<>();
    for(PetriNetEdge ed: outgoing){
      //System.out.println("ed "+ed.myString());
      out.add(ed);
    }
    return out;
  }
  @Getter
  @Setter
  Set<String> owners = new HashSet<>();

  public boolean equals(PetriNetTransition tr){
    return this.getId().equals(tr.getId());
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
  }public void clearOwners() {
      owners = new HashSet<>();

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
  public PetriNetTransition(String id, String label) {
    super(id, "node");
    this.label = label;
  }

  public Set<PetriNetPlace> pre() {
    //System.out.println(incoming.size());
    Set<PetriNetPlace> out = new HashSet<>();
    if (incoming.size()==0) return out;
    for(PetriNetEdge ed: incoming){
      //System.out.println("ed "+ed.myString());
      PetriNetPlace p = (PetriNetPlace) ed.getFrom();
      //System.out.println(p.getId());
      out.add(p);
    }
    //System.out.println("out.size "+out.size());
    return out;
  /*  return incoming.stream()
        .map(PetriNetEdge::getFrom)
        .map(PetriNetPlace.class::cast)
        .distinct()
        .collect(Collectors.toSet()); */
  }
  public Set<PetriNetPlace> preNonBlocking() {
    return incoming.stream()
            .filter(ed->!ed.getOptional())
            .map(PetriNetEdge::getFrom)

            .map(PetriNetPlace.class::cast)
            .distinct()
            .collect(Collectors.toSet());

  }public Set<String> optionalOwners() {  //TokenRule
    return incoming.stream()
            .filter(ed->ed.getOptional())
            .map(PetriNetEdge::getFrom)
            .map(x->((PetriNetPlace)x).getOwners())
            .flatMap(x->x.stream())
            .collect(Collectors.toSet());
  }

  public Set<PetriNetPlace> post() {
    return outgoing.stream()
        .map(PetriNetEdge::getTo)
        .map(PetriNetPlace.class::cast)
        .distinct()
        .collect(Collectors.toSet());
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("transition{\n");
    builder.append("\tid:").append(getId());
    builder.append("\tlabel:").append(getLabel());
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
    StringBuilder builder = new StringBuilder();
    builder.append(getId()+" ");
    for (PetriNetEdge edge : getIncoming()) {
      builder.append(edge.getFrom().getId()+",");
    }
    builder.append("-"+label+"->");
    for (PetriNetEdge edge : getOutgoing()) {
      builder.append(edge.getTo().getId()+",");
    }
    builder.append(" own "+this.getOwners());
     return builder.toString();
  }
}
