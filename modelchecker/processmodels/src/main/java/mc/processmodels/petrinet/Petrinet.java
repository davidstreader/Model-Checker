package mc.processmodels.petrinet;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.Data;
import lombok.SneakyThrows;
import mc.compiler.ast.HidingNode;
import mc.compiler.ast.RelabelElementNode;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.ProcessType;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.util.Location;

@Data
public class Petrinet extends ProcessModelObject implements ProcessModel {
  public static final String DEFAULT_OWNER = "_default";


  private Map<String, PetriNetPlace> places = new HashMap<>();
  private Map<String, PetriNetTransition> transitions = new HashMap<>();
  private Multimap<String, PetriNetTransition> alphabet = ArrayListMultimap.create();

  private Set<PetriNetPlace> roots = new LinkedHashSet<>();
  private Map<String, PetriNetEdge> edges = new HashMap<>();
  private Set<RelabelElementNode> relabels = new HashSet<>();

  private HidingNode hiding;
  private Set<String> hiddenVariables = new HashSet<>();
  private Location hiddenVariablesLocation;

  private Set<String> owners = new HashSet<>();

  private Set<String> variables = new HashSet<>();
  private Location variablesLocation;

  private Location location;
  private String id;
  private int placeId = 0;
  private int transitionId = 0;
  private int edgeId = 0;

  public boolean  rootContains(PetriNetPlace pl){

    return roots.stream().
       filter(x->x.getId().equals(pl.getId())).
       collect(Collectors.toSet()).size() == 1;

  }
  /**  USE this when DEBUGGIN
   * Not sure what data consistancy is intended or assumed
   * This method should act both in assertions and as documentation
   * No 2 Transitions should have the same Id
   * No 2 (atomic) Transitions should have the same pre, post sets and label
   * @return
   */
  public boolean validatePNet(){
    boolean ok = true;
     for (PetriNetPlace r : roots) {
       if( !r.isStart()) {
         System.out.println("Root " + r.getId() + " not Start ");
         ok = false;
       }
     }
     for(PetriNetPlace p : this.getPlaces().values()){
       if (p.isStart() && !rootContains(p)) {
         System.out.println("Start "+p.getId()+" is not Root");
         ok = false;
       }
     }

    for (String k: transitions.keySet()){
      String id = transitions.get(k).getId();
      if (!id.equals(k)) {
        System.out.println("transition key "+k+" not trasitionid "+id);
        ok = false;
      }
      boolean match = true;
      for(PetriNetEdge ed: transitions.get(k).getIncoming()){
        if (!ed.getTo().getId().equals(id)) {
          System.out.println(" Incoming transition edge "+ed.myString()+ " not matched "+id);
          match = false;
        }
      } ok = ok && match;
      for(PetriNetEdge ed: transitions.get(k).getIncoming()){
        if (!ed.getTo().getId().equals(id)) {
          System.out.println(" Incoming transition edge "+ed.myString()+ " not matched "+id);
          match = false;
        }
      } ok = ok && match;
    }

    ok = ok &&
      places.keySet().stream().
        map(x->places.get(x).getId().equals(x))
        .reduce(ok, (x,y)->x&&y);
    if (ok) System.out.println(this.getId()+" Valid");
    else {
      System.out.println(this.getId() + " NOT VALID");
      System.out.println(this.myString());
    }
    return ok;
  }

  public void setRoot2Start(){
   this.setRoots( getPlaces().values().stream().
      filter(x->x.isStart()).
      collect(Collectors.toSet()));
  }
  public boolean tranExists(Collection<PetriNetPlace> pre,
                            Collection<PetriNetPlace> post,
                            String label) {
    System.out.print("pre { ");
    for(PetriNetPlace p: pre) {
      System.out.print(p.getId()+" ");
    }System.out.print("}");
    System.out.print(" "+label);
    System.out.print(" post { ");
    for(PetriNetPlace p: post) {
      System.out.print(p.getId()+" ");
    }System.out.println("}");

    for(PetriNetTransition tr: this.getTransitions().values()) {
      System.out.println("Exists "+tr.myString());
      if (this.prePlaces(tr).equals(pre) &&
        this.postPlaces(tr).equals(post) &&
        tr.getLabel().equals(label) ) {
        return true;
      }
    }
    return false;
  }

  public Set<PetriNetPlace> prePlaces(PetriNetTransition tr){
    Set<PetriNetPlace> pre = new HashSet<>();
    for(PetriNetEdge edge: tr.getIncoming()){
      pre.add((PetriNetPlace) edge.getFrom());
    }
    return pre;
  }
  public Set<PetriNetPlace> postPlaces(PetriNetTransition tr){
    Set<PetriNetPlace> post = new HashSet<>();
    for(PetriNetEdge edge: tr.getOutgoing()){
      post.add((PetriNetPlace) edge.getFrom());
    }
    return post;
  }
  public boolean hasPlace(PetriNetPlace p){
    return places.containsValue(p);
  }


  /**
   * Simply for easy of visualisation when testing
   * @return
   */
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append(" " + this.getId()+ " root {");
    sb.append(this.getRoots().stream().map(x-> x.getId()).reduce("",(x,y)->x+" "+y)+"}");
    sb.append(this.getPlaces().values().stream().
      filter(x->x.isTerminal()).
      map(x->(x.getId()+"=>"+x.getTerminal())).
      reduce("{",(x,y)->x+" "+y)+"}" );

    sb.append(this.getPlaces().keySet().stream().reduce("\n"+
      getPlaces().size()+" places ",(x,y)->x+" "+y));
    sb.append(this.getTransitions().keySet().stream().reduce("\n" +
      getTransitions().size()+ " transitions ",(x,y)->x+" "+y));
    sb.append(this.getTransitions().values().stream().
      map(tr-> tr.myString()+"\n").
      reduce("\n",(x,y)->x+" "+y));
    return sb.toString();
  }

public String myString(Collection<PetriNetPlace> mark){
    return mark.stream().map(x->x.myString()).reduce("{", (x,y)->x+" "+y)+"}";
}


  public Petrinet(String id) {
    this(id, true);
  }

  public Petrinet(String id, boolean constructRoot) {
    super(id, "Petrinet");
    this.id = id;
    this.owners.add(DEFAULT_OWNER);


    if (constructRoot) {
      PetriNetPlace origin = addPlace();
      origin.setStart(true);
      roots.add(origin);
    }
  }

  public void addRoot(PetriNetPlace place) {
    if (places.values().contains(place)) {
      roots.add(place);
      place.setStart(true);
    }
  }

  public PetriNetPlace addPlace() {
    return addPlace(this.id + ":p:" + placeId++);
  }

  public PetriNetPlace addPlace(String id) {
    PetriNetPlace place = new PetriNetPlace(id);
    places.put(id, place);
    return place;
  }

  public PetriNetTransition addTransition(String id, String label) {
    PetriNetTransition transition = new PetriNetTransition(id, label);
    transitions.put(id, transition);
    alphabet.put(label, transition);
    return transition;
  }

  public PetriNetTransition addTransition(String label) {
    return addTransition(id + ":t:" + transitionId++, label);
  }

  public PetriNetEdge addEdge(PetriNetTransition to, PetriNetPlace from, Set<String> owner) throws CompilationException {
    if (!transitions.containsValue(to) || !places.containsValue(from)) {
      throw new CompilationException(getClass(), "Cannot add an edge to an object not inside the petrinet");
    }
    if (to == null || from == null) {
      throw new CompilationException(getClass(), "Either " + to + " or " + from + "are null");
    }

    String id = this.id + ":e:" + edgeId++;

    PetriNetEdge edge = new PetriNetEdge(id, to, from);
    edge.setOwners(owner);
    to.getIncoming().add(edge);
    from.getOutgoing().add(edge);
    edges.put(id, edge);
    return edge;
  }

  public PetriNetEdge addEdge(PetriNetPlace to, PetriNetTransition from, Set<String> owner) throws CompilationException {
    if (!transitions.containsValue(from) || !places.containsValue(to)) {
      throw new CompilationException(getClass(), "Cannot add an edge to an object not inside the petrinet");
    }
    if (to == null || from == null) {
      throw new CompilationException(getClass(), "Either " + to + " or " + from + "are null");
    }

    String id = this.id + ":e:" + edgeId++;
    PetriNetEdge edge = new PetriNetEdge(id, to, from);
    edge.setOwners(owner);
    to.getIncoming().add(edge);
    from.getOutgoing().add(edge);
    edges.put(id, edge);
    return edge;
  }


  public void removePlace(PetriNetPlace place) throws CompilationException {
    if (!places.containsValue(place)) {
      throw new CompilationException(getClass(), "Cannot remove a place that is not part of"
          + "the petrinet");
    }
    Set<PetriNetEdge> toRemove = new HashSet<>();
    toRemove.addAll(place.getIncoming());
    toRemove.addAll(place.getOutgoing());
    toRemove = toRemove.stream().filter(edges::containsValue).collect(Collectors.toSet());

    for (PetriNetEdge edge : toRemove) {
      removeEdge(edge);
    }
    if (place.isStart() || roots.contains(place)) {
      //TODO: There is alot of weirdness going on with this set
      //Code:
      //processes A = (b->STOP||c->STOP).
      //petrinet A.
      roots = roots.stream().filter(((Predicate<PetriNetPlace>) place::equals).negate())
          .collect(Collectors.toSet());
//      System.out.println(roots.contains(place));
//      System.out.println(roots.remove(place));
//      roots.stream().map(place::equals).forEach(System.out::println);
    }

    places.remove(place.getId());
  }

  public void removeTransition(PetriNetTransition transition) throws CompilationException {
    if (!transitions.values().contains(transition)) {
      throw new CompilationException(getClass(), "Cannot remove a transition that is not part of"
          + "the petrinet");
    }
    Set<PetriNetEdge> toRemove = new HashSet<>(transition.getIncoming());
    toRemove.addAll(transition.getOutgoing());

    for (PetriNetEdge edge : toRemove) {
      removeEdge(edge);
    }
    transitions.remove(transition.getId());
    alphabet.remove(transition.getLabel(), transition);
  }

  public void removeEdge(PetriNetEdge edge) throws CompilationException {
    if (!edges.values().contains(edge)) {
      throw new CompilationException(getClass(), "Cannot remove an edge that is not part of"
          + "the petrinet");
    }
    if (edge.getTo() instanceof PetriNetTransition) {
      ((PetriNetTransition) edge.getTo()).getIncoming().remove(edge);
      ((PetriNetPlace) edge.getFrom()).getOutgoing().remove(edge);
    } else {
      ((PetriNetPlace) edge.getTo()).getIncoming().remove(edge);
      ((PetriNetTransition) edge.getFrom()).getOutgoing().remove(edge);
    }
    edges.remove(edge.getId());
  }

  @SneakyThrows(value = {CompilationException.class})
  public Set<PetriNetPlace> addPetrinet(Petrinet petriToAdd) {
    Set<PetriNetPlace> roots = new HashSet<>();
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    owners.remove(Petrinet.DEFAULT_OWNER);


    for (PetriNetPlace place : petriToAdd.getPlaces().values()) {
      PetriNetPlace newPlace = addPlace();
      newPlace.copyProperties(place);
      placeMap.put(place, newPlace);
    }
    //System.out.println("addPetri root"+ myString(petriToAdd.getRoots()));
    for(PetriNetPlace rpl : petriToAdd.getRoots()){
      roots.add(placeMap.get(rpl));
      this.addRoot(placeMap.get(rpl));
      //System.out.println("addin root "+placeMap.get(rpl).getId());
    }
    for (PetriNetTransition transition : petriToAdd.getTransitions().values()) {
      PetriNetTransition newTransition = addTransition(transition.getLabel());
      transitionMap.put(transition, newTransition);
    }

    for (PetriNetEdge edge : petriToAdd.getEdges().values()) {
      Set<String> postFixed = new HashSet<>();

      postFixed.addAll(edge.getOwners());
      owners.addAll(postFixed);

      if (edge.getFrom() instanceof PetriNetPlace) {
        addEdge(transitionMap.get(edge.getTo()), placeMap.get(edge.getFrom()),  postFixed);
      } else {
        addEdge(placeMap.get(edge.getTo()), transitionMap.get(edge.getFrom()),  postFixed);
      }
    }

    return roots;
  }


  public Set<PetriNetPlace> gluePlaces
      (Set<PetriNetPlace> set1, Set<PetriNetPlace> set2)
      throws CompilationException {
    if (!StreamSupport.stream(Iterables.concat(set1, set2).spliterator(), false)
        .allMatch(places::containsValue)) {
      new RuntimeException().printStackTrace();
      throw new CompilationException(getClass(), "Cannot glue places together that are not part"
          + " of the same petrinet");
    }

    Multimap<PetriNetPlace, PetriNetPlace> products = ArrayListMultimap.create();
    for (PetriNetPlace place1 : set1) {
      for (PetriNetPlace place2 : set2) {
        PetriNetPlace newPlace = this.addPlace();
        products.put(place1, newPlace);
        products.put(place2, newPlace);
        newPlace.intersectionOf(place1, place2);
        if (place1.isStart() || place2.isStart()) {
          newPlace.setStart(true);
        }
      }
    }


    for (PetriNetPlace place : Iterables.concat(set1, set2)) {
      for (PetriNetPlace product : products.get(place)) {
        for (PetriNetEdge edge : place.getIncoming()) {
          product.getIncoming().add(addEdge(product, (PetriNetTransition) edge.getFrom(), new LinkedHashSet<>(edge.getOwners())));
        }
        for (PetriNetEdge edge : place.getOutgoing()) {

          product.getOutgoing().add(addEdge((PetriNetTransition) edge.getTo(), product, new LinkedHashSet<>(edge.getOwners())));
        }
      }
    }

    Map<Set<String>, Set<String>> combinationsTable = new HashMap<>();
    Set<PetriNetPlace> petrinetPlaces = new LinkedHashSet<>(products.values());
    for (PetriNetPlace currentPlace : petrinetPlaces) {
      Set<String> currentCombination = new LinkedHashSet<>();
      for (PetriNetEdge inEdge : currentPlace.getIncoming()) {
        Set<String> newInEdgeOwners = new LinkedHashSet<>();
        for (PetriNetEdge outEdge : currentPlace.getOutgoing()) {


          newInEdgeOwners.addAll(outEdge.getOwners());

          Set<String> newOutEdgeOwners = new LinkedHashSet<>(outEdge.getOwners());
          newOutEdgeOwners.addAll(inEdge.getOwners());

          outEdge.setOwners(newOutEdgeOwners);
        }

        newInEdgeOwners.addAll(inEdge.getOwners());
        inEdge.setOwners(newInEdgeOwners);

        currentCombination.addAll(newInEdgeOwners);

        owners.addAll(newInEdgeOwners);
      }


      for (String owner : currentCombination) {
        Set<String> ownerSet = new LinkedHashSet<>(Collections.singleton(owner));
        if (!combinationsTable.containsKey(ownerSet)) {
          combinationsTable.put(ownerSet, new LinkedHashSet<>());
        }

        combinationsTable.get(ownerSet).addAll(currentCombination);
      }


    }

    this.getEdges().values().stream().filter(edge -> combinationsTable.containsKey(edge.getOwners())).forEach(edge -> {
      edge.setOwners(combinationsTable.get(new LinkedHashSet<>(edge.getOwners())));
    });


    for (PetriNetPlace place : Iterables.concat(set1, set2)) {
      removePlace(place);
    }

    products.values().stream().filter(PetriNetPlace::isStart).forEach(this::addRoot);

    return new HashSet<>(products.values());
  }

  public void relabelTransitions(String oldLabel, String newLabel) {
    List<PetriNetTransition> transitions = new ArrayList<>(alphabet.get(oldLabel));
    alphabet.replaceValues(oldLabel, Collections.emptyList());

    for (PetriNetTransition transition : transitions) {
      transition.setLabel(newLabel);
      alphabet.put(newLabel, transition);
    }
  }

  @Override
  public ProcessType getProcessType() {
    return ProcessType.PETRINET;
  }

//clones
  @Override
  public Petrinet copy() throws CompilationException {
    return (Petrinet) super.copy();
  }
}