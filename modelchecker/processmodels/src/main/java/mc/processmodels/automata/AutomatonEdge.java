package mc.processmodels.automata;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import mc.Constant;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModelObject;

public class AutomatonEdge extends ProcessModelObject {

  //private static final String INTERSECTION = "^";

  @Setter
  private Set<String> edgeOwners = new HashSet<>();

  @Getter
  @Setter
  private String label;

  @Getter
  @Setter
  private AutomatonNode from;

  @Getter
  @Setter
  private AutomatonNode to;

  /*Edge built from send event NOT synchronising with receive event
     when there exist receive events in parallel process
   */
  private boolean optionalEdge = false;

  @Getter
  @Setter
  private Set<String> optionalOwners = new HashSet<>();
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

  public boolean getOptionalEdge(){return optionalEdge;}
  public void setOptionalEdge(boolean  b){optionalEdge = b;}
  public boolean isHidden() {
    return label.equals(Constant.HIDDEN);
  }

  public boolean isDeadlocked() {
    return label.equals(Constant.DEADLOCK);
  }

  public Set<String> getOwnerLocation() {
    return new HashSet<>(edgeOwners);
  }

  boolean addOwnerLocation(String owner) {
    return edgeOwners.add(owner);
  }

  boolean removeOwnerLocation(String owner) {
    return edgeOwners.remove(owner);
  }

  public String myString() {
    String out = "";
    if (guard != null) {
      out = from.getId() + "-" + label + "->" + to.getId() + " " +
             guard.myString() + " o= "+edgeOwners+ " optional= "+optionalEdge;
    } else {
      out = from.getId() + "-" + label + "->" + to.getId() +
           " guard null "+ " o= "+edgeOwners + " optional= "+optionalEdge;
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
    return Objects.hash(label, from.getId(), to.getId());
  }

  /*
  NOT a clue what this is trying to do!
   */
  public static Multimap<String, String> createIntersection(Automaton automaton1,
                                                            Automaton automaton2) {
    Set<String> preowners1 = automaton1.getOwners();
    Set<String> preowners2 = automaton2.getOwners();

    Set<String> intersection = new HashSet<>(preowners1);
    intersection.retainAll(preowners2);

    if (intersection.size() > 0) {
      relabelOwners(automaton1,"._1");
      relabelOwners(automaton2,"._2");
      preowners1 = automaton1.getOwners();
      preowners2 = automaton2.getOwners();
    }

    //tricking the lambda expressions to evaluate
    Set<String> owners1 = preowners1;
    Set<String> owners2 = preowners2;

    Multimap<String, String> table = ArrayListMultimap.create();
    owners1.forEach(o1 -> owners2.forEach(o2 -> table.put(o1, o1 + Constant.ACTIVE + o2)));
    owners1.forEach(o1 -> owners2.forEach(o2 -> table.put(o2, o1 + Constant.ACTIVE + o2)));
    return table;
  }

  private static void relabelOwners(Automaton aut, String label) {
    aut.getEdges().forEach(e -> {

      Set<String> owners = e.getOwnerLocation().stream()
          .map(o -> o + label)
          .collect(Collectors.toSet());
      Set<String> toRemove = new HashSet<>(e.getOwnerLocation());
      toRemove.forEach(o -> aut.removeOwnerFromEdge(e, o));
      try {
        aut.addOwnersToEdge(e, owners);
      } catch (CompilationException ignored) {
      }
    });
  }

}
