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

  private ProcessModelObject from;
  private ProcessModelObject to;
  private Guard guard;
  // id and type in ProcessModelObject id unique with in Processes
  private TreeSet<String> variables = new TreeSet<>();
  private boolean  optional = false;
    /*
      Optional is when a transition is the result of broadcast synchronisation
       (The petri Net equivalence to adding listening loop is to add transitions with wings Pn<-->Tr)
       to prevent clutter we use "Optional" edges and build the Token rule to add the listening loops
       For a!  || a?->.. -a?->  need optionNum to distinguish listeners of single owner
       and to connect in from out.
     */
    private Integer optionNum = 0;  // only set to none zero by parallel composition

    // for all Edges in a Sequential Net have the same variables  the PetriNet
  // Parallel composition dose not change the Edge variables
  // but the Net variables are the union of the Net variables

  /*
     set of probability distributions held on the PetriNet
   */

  public void setOptionNum(int o) {optionNum = o;}
  public Integer getOptionNum() {return optionNum;}

    // set of probability distibution ids - data on PetriNet
    private Set<String> probDists = new TreeSet<>();
    public   Set<String> getProbabilityDistributions(){return probDists;}
    public void addProbabilityDistribution(String id) {probDists.add(id);}
    public void clearProbabilityDistributions(){probDists.clear();}

    public void setOptional(boolean b){ optional = b;}
    public boolean getOptional(){return optional;}
    public boolean notOptional(){return !optional; }


    public PetriNetEdge(String id, PetriNetPlace to, PetriNetTransition from) {
    super(id, "petrinetEdge");
    this.from = from;
    this.to = to;

  }

  public PetriNetEdge copy(){
      PetriNetEdge ned;
        if (getTo() instanceof  PetriNetPlace) {
            ned = new PetriNetEdge(getId(),
                                 ((PetriNetPlace) getTo()),
                                 ((PetriNetTransition) getFrom()));
        } else {
            ned = new PetriNetEdge(getId(),
                ((PetriNetTransition) getTo()),
                ((PetriNetPlace) getFrom()));
        }
        ned.setOptionNum(getOptionNum());
        ned.setOptional(getOptional());
        ned.setGuard(getGuard());
        ned.probDists = probDists;
        ned.variables = variables;
        return ned;
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
    public int compareTo(Object ed){
        if (ed instanceof PetriNetEdge)
            return getId().compareTo(((PetriNetEdge) ed).getId());
        else
            return -1;
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
    sb.append(" -> "+to.getId()+" optional= "+optional+ " optN= "+optionNum);

     //for (String o: owners){out = out +o+" ";}
    return sb.toString();
  }
}
