package mc.processmodels.petrinet.components;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import mc.processmodels.ProcessModelObject;
import mc.compiler.Guard;
import mc.processmodels.petrinet.Petrinet;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class PetriNetEdge extends ProcessModelObject implements Comparable {

  public int compareTo(Object ed){
    if (ed instanceof PetriNetEdge)
      return getId().compareTo(((PetriNetEdge) ed).getId());
    else
      return -1;
  }
  private ProcessModelObject from;

  private ProcessModelObject to;

  private Guard guard;

  private Set<String> variables = new HashSet<>();
  // for all Edges in a Sequential Net have the same variables  the PetriNet
  // Parallel composition dose not change the Edge variables
  // but the Net variables are the union of the Net variables

  /*
    Optional is when a transition is the result of broadcast synchronisation
     (The petri Net equivalence to adding listening loop is to add transitions with wings Pn<-->Tr)
     to prevent clutter we use "Optional" edges and build the Token rule to add the listening loops
   */
  private boolean  optional = false;
  public void setOptional(boolean b){ optional = b;}
  public boolean getOptional(){return optional;}
  public boolean notOptional(){return !optional; }
  public PetriNetEdge(String id, PetriNetPlace to, PetriNetTransition from) {
    super(id, "petrinetEdge");
    this.from = from;
    this.to = to;

  }

  public PetriNetEdge(String id, PetriNetTransition to, PetriNetPlace from) {
    super(id, "petrinetEdge");
    this.from = from;
    this.to = to;
  }

  public boolean isNotBlocked() {
      if (from instanceof PetriNetTransition) {
          return !((PetriNetTransition) from).isBlocked();
      } else if (to instanceof PetriNetTransition) {
          return !((PetriNetTransition) to).isBlocked();
      } else {
          System.out.println("DATA ERROR "+this.myString());
          return false;
      }

  }


  public String toString() {
      String fr, t;
      if (from.equals(null))  fr = "null"; else fr = from.getId();
      if (to.equals(null))  t = "null"; else t = to.getId();
      return "edge{\n"
        + "\tid:" + getId() + "\n"
        + "\tfrom:" + fr + "\n"
        + "\tto:" + t + "\n"
        + "}";

  }
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("Edge "+this.getId()+" from "+from.getId());
    if (guard!=null) sb.append(" - "+guard.myString());
    sb.append(" -> "+to.getId()+" optional= "+optional);

     //for (String o: owners){out = out +o+" ";}
    return sb.toString();
  }
}
