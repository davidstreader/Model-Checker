package mc.processmodels.automata;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
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
    //@Getter
    //@Setter  Must not set to a singleton
    private Set<String> edgeOwners = new HashSet<>();

    public void setEdgeOwners(Collection<String> os) {
        Set<String> eos = new HashSet<>();
        os.forEach(o -> eos.add(o));
        edgeOwners = eos;
    }

    public void addEdgeOwners(Collection<String> os) {
        os.forEach(o -> edgeOwners.add(o));
    }

    public Set<String> getEdgeOwners() {
        return new HashSet<>(edgeOwners);
    }

    boolean addOwner(String owner) {
        return edgeOwners.add(owner);
    }

    public boolean isOrthoganal(Set<String> owners) {
        boolean orth = true;
        for (String s : this.edgeOwners) {
            if (owners.contains(s)) {
                orth = false;
                break;
            }
        }
        return orth;
    }

    boolean removeOwnerLocation(String owner) {
        return edgeOwners.remove(owner);
    }

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

    public boolean getOptionalEdge() {
        return optionalEdge;
    }

    public void setOptionalEdge(boolean b) {
        optionalEdge = b;
    }

    public boolean isHidden() {
        return label.equals(Constant.HIDDEN);
    }

    /**
     * if edge is a tau But  bridges the gap between external and
     * internal nodes.
     *
     * @return
     */
    public boolean stateObservable() {
        return getTo().observeDistinct(getFrom());
    /*  !((getTo().isSTOP() == getFrom().isSTOP()) &&
      (getTo().isStartNode() == getFrom().isStartNode())); */
    }
 /* public boolean isObservableHidden() {
    return label.equals(Constant.HIDDEN) &&
      !((getTo().isSTOP() == getFrom().isSTOP()) &&
        (getTo().isStartNode() == getFrom().isStartNode()));
  } */

    public boolean isDeadlocked() {
        return label.equals(Constant.DEADLOCK);
    }


    public String myString() {
        String out = "";
        if (guard != null) {
            out = getId() + "  " + from.getId() + "-" + label + "->" + to.getId() + " " +
                guard.myString() + " o= " + edgeOwners + " optional= " + optionalEdge;
        } else {
            out = getId() + "  " + from.getId() + "-" + label + "->" + to.getId() +
                " guard null " + " o= " + edgeOwners + " optional= " + optionalEdge;
        }
        return out;
    }

    public String myString(String simple) {
        String out = "";
        if (simple.equals("simple")) {
            out = from.getId() + "-" + label + "->" + to.getId();
        } else {
            out = myString();
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
            + "\t owners: " + getEdgeOwners() + "\n"
            + "\n"
            + "}";

        return builder;
    }

    @Override
    public boolean equals(Object o) {
        AutomatonEdge edge;
        boolean out;
        if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            edge = (AutomatonEdge) o;
            if (!label.equals(edge.label)) {
                //System.out.println("label");
                out = false;
            } else if (!from.getId().equals(edge.from.getId())) {
                out = false;
                //System.out.println("from");
            } else {
                out = to.getId().equals(edge.to.getId());
            }
        }
        //System.out.println(myString() + " EQUAL? " + edge.myString() + " ==>> " + out);
        return out;
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
            relabelOwners(automaton1, "._1");
            relabelOwners(automaton2, "._2");
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

            Set<String> owners = e.getEdgeOwners().stream()
                .map(o -> o + label)
                .collect(Collectors.toSet());
            Set<String> toRemove = new HashSet<>(e.getEdgeOwners());
            toRemove.forEach(o -> aut.removeOwnerFromEdge(e, o));
            try {
                aut.addOwnersToEdge(e, owners);
            } catch (CompilationException ignored) {
            }
        });
    }

}
