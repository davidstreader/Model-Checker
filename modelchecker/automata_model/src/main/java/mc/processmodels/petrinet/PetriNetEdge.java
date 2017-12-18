package mc.processmodels.petrinet;


import lombok.Getter;
import lombok.Setter;
import mc.processmodels.ProcessModelObject;


public class PetriNetEdge extends ProcessModelObject {

  @Getter
  @Setter
  private String label;

  @Getter
  @Setter
  private PetriNetTransitions from;// it can also br from a Place

  @Getter
  @Setter
  private PetriNetTransitions to;// it can also br from a Place

  public PetriNetEdge(String id, String label, PetriNetTransitions from, PetriNetTransitions to) {
    super(id, "edge");
    this.label = label;
    this.from = from;
    this.to = to;
  }

  public String toString() {
/*        String builder = "edge{\n" +
                "\tid:" + getId() + "\n" +
                "\tlabel:" + label + "\n" +
                "\tfrom:" + from.getId() + "\n" +
                "\tto:" + to.getId() + "\n" +
                "\tmetadata:" + getGuard() + "\n" +
                "}";

        return builder;*/
    return "";
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PetriNetEdge edge = (PetriNetEdge) o;

    if (!label.equals(edge.label)) {
      return false;
    }
    if (!from.getId().equals(edge.from.getId())) {
      return false;
    }
    return to.getId().equals(edge.to.getId());
  }

  @Override
  public int hashCode() {
    int result = label.hashCode();
    result = 31 * result + from.getId().hashCode();
    result = 31 * result + to.getId().hashCode();
    return result;
  }
}
