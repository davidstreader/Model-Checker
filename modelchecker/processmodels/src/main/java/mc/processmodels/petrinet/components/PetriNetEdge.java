package mc.processmodels.petrinet.components;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.petrinet.Petrinet;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class PetriNetEdge extends ProcessModelObject {


  private Set<String> owners = new HashSet<>();

  private ProcessModelObject from;

  private ProcessModelObject to;

  public PetriNetEdge(String id, PetriNetPlace to, PetriNetTransition from) {
    super(id, "petrinetEdge");
    this.from = from;
    this.to = to;
    this.addOwner(Petrinet.DEFAULT_OWNER);

  }

  public PetriNetEdge(String id, PetriNetTransition to, PetriNetPlace from) {
    super(id, "petrinetEdge");
    this.from = from;
    this.to = to;
    this.addOwner(Petrinet.DEFAULT_OWNER);
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

  public void removeOwner(String name) {
    owners.remove(name);
  }


  public String toString() {
    return "edge{\n"
        + "\tid:" + getId() + "\n"
        + "\tfrom:" + from.getId() + "\n"
        + "\tto:" + to.getId() + "\n"
        + "\towner/s:" + owners + "\n"
        + "}";

  }
  public String myString(){
    String out =  "edge "+this.getId()+" from "+from.getId()+" -> "+to.getId()+" owners "+ owners;
    //for (String o: owners){out = out +o+" ";}
    return out;
  }
}
