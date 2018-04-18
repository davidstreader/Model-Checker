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


  private ProcessModelObject from;

  private ProcessModelObject to;

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




  public String toString() {
    return "edge{\n"
        + "\tid:" + getId() + "\n"
        + "\tfrom:" + from.getId() + "\n"
        + "\tto:" + to.getId() + "\n"
        + "}";

  }
  public String myString(){
    String out =  "edge "+this.getId()+" from "+from.getId()+" -> "+to.getId();
    //for (String o: owners){out = out +o+" ";}
    return out;
  }
}
