package mc.processmodels.petrinet.components;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.petrinet.Petrinet;

import java.util.HashSet;
import java.util.Set;

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
    owners.add(ownerName);
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
}
