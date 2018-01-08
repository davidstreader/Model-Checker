package mc.processmodels.petrinet.components;


import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.processmodels.ProcessModelObject;

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

}
