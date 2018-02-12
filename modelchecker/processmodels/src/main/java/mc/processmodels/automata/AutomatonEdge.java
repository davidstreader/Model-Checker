package mc.processmodels.automata;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import mc.Constant;
import mc.compiler.Guard;
import mc.processmodels.ProcessModelObject;

public class AutomatonEdge extends ProcessModelObject {

  private static final String INTERSECTION = "^";

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

  /**
   *
   */
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
    String out = "";
    if (guard != null) {
      out = from.getId() + "-" + label + "->" + to.getId() + " " + guard.myString();
    }else {
      out = from.getId() + "-" + label + "->" + to.getId() + " null guard";
    }
    return out;
  }


  public String toString() {
    String builder = "edge{\n"
        + "\tid:" + getId() + "\n"
        + "\tlabel:" + label + "\n"
        + "\tfrom:" + from.getId() + "\n"
        + "\tto:" + to.getId() + "\n"
        + "\tmetadata:" + getGuard() + "\n"
        + "\t owners: " + getOwnerLocation() + "\n"
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

  public static Multimap<String, String> createIntersection(Set<String> owners1,
                                                            Set<String> owners2) {
    Multimap<String, String> table = ArrayListMultimap.create();
    owners1.forEach(o1 -> owners2.forEach(o2 -> table.put(o1,o1 + INTERSECTION + o2)));
    owners1.forEach(o1 -> owners2.forEach(o2 -> table.put(o2,o1 + INTERSECTION + o2)));
    return table;
  }

}
