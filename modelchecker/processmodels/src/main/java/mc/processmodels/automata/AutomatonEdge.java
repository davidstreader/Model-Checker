package mc.processmodels.automata;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import mc.Constant;
import mc.compiler.Guard;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.automata.serializers.JSONEdgeSerializer;

@JsonSerialize(using = JSONEdgeSerializer.class)
//@ToString
public class AutomatonEdge extends ProcessModelObject {

  private Set<String> automatonLocation = new HashSet<>();

  @Getter
  @Setter
  private String label;

  @Getter
  @Setter
  private AutomatonNode from;

  @Getter
  @Setter
  private AutomatonNode to;

  @Getter
  @Setter
  private Guard guard;


  public AutomatonEdge(String id, String label, AutomatonNode from, AutomatonNode to) {
    super(id, "edge");
    this.label = label;
    this.from = from;
    this.to = to;
  }

  public boolean isHidden() {
    return label.equals(Constant.HIDDEN);
  }

  public boolean isDeadlocked() {
    return label.equals(Constant.DEADLOCK);
  }

  public Set<String> getOwnerLocation() {
    return new HashSet<>(automatonLocation);
  }

  boolean addOwnerLocation(String owner) {
    return automatonLocation.add(owner);
  }

  boolean removeOwnerLocation(String owner) {
    return automatonLocation.remove(owner);
  }

  public String myString(){
    return from.getId()+"-"+label+"->"+to.getId()+ " "+ guard.myString();
  }
/*  public String myString(){
    return getId()+" "+from.getId()+"-"+label+"->"+to.getId();
  } */

  public String toString() {
    String builder = "edge{\n"
        + "\tid:" + getId() + "\n"
        + "\tlabel:" + label + "\n"
        + "\tfrom:" + from.getId() + "\n"
        + "\tto:" + to.getId() + "\n"
        + "\tmetadata:" + getGuard()
        + "\n"
        + "}";

    return builder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AutomatonEdge edge = (AutomatonEdge) o;

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
    return Objects.hash(label,from.getId(),to.getId());
  }

}
