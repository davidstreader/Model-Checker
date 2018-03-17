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
    // if (ok) //System.out.println(this.getId()+" Valid");

      if (!ok) {
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
  /*  System.out.print("pre { ");
    for(PetriNetPlace p: pre) {
      System.out.print(p.getId()+" ");
    }System.out.print("}");
    System.out.print(" "+label);
    System.out.print(" post { ");
    for(PetriNetPlace p: post) {
      System.out.print(p.getId()+" ");
    }System.out.println("}"); */

    for(PetriNetTransition tr: this.getTransitions().values()) {
     //System.out.println("Exists "+tr.myString());
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

    sb.append(this.getPlaces().values().stream().
      map(x->x.getId())
      .reduce("\n"+ getPlaces().size()+" places ",(x,y)->x+" "+y));
    sb.append(this.getPlaces().values().stream().
      map(x->"\n"+x.myString())
      .reduce( "",(x,y)->x+" "+y));
    sb.append(this.getTransitions().keySet().stream().reduce("\n" +
      getTransitions().size()+ " transitions ",(x,y)->x+" "+y));
    sb.append(this.getTransitions().values().stream().
      map(tr-> "\n"+tr.myString()).
      reduce("",(x,y)-> x+" "+y));
    return sb.toString();
  }
  public static Petrinet oneEventNet(String event)throws CompilationException{
    Petrinet eventNet = new Petrinet(event,false);
    PetriNetPlace start = eventNet.addPlace();
    eventNet.mySetRoots(Collections.singleton(start));
    PetriNetPlace end = eventNet.addPlace();
    PetriNetTransition tr = eventNet.addTransition(event);
    eventNet.addEdge(end,tr, Collections.singleton(DEFAULT_OWNER));
    eventNet.addEdge(tr, start, Collections.singleton(DEFAULT_OWNER));
    end.setTerminal("STOP");
  //  System.out.println("oneEventNet "+eventNet.myString());
    return eventNet;
  }
  // called from interpretor
  public static Petrinet stopNet() {
    return  Petrinet.stopNet("");
  }

  // called from interpretor when local reference needs to be used
  public static Petrinet stopNet(String ref){
      Petrinet stop = new Petrinet("stop");

    for(PetriNetPlace p : stop.getPlaces().values()) {
      p.addFromRefefances( new HashSet(Collections.singleton(ref)));
      p.setTerminal("STOP");
    }
  //  System.out.println("stopNet "+stop.myString());
    return stop;
  }
  public static Petrinet errorNet(){
    Petrinet error = new Petrinet("error");
    for(PetriNetPlace p : error.getPlaces().values()) {
      p.setTerminal("ERROR");
    };
    return error;
  }
public static String marking2String(Collection<PetriNetPlace> mark){
  //return mark.stream().map(x->x.myString()).reduce("{", (x,y)->x+" "+y)+"}";
  return mark.stream().map(x->x.getId()).reduce("{", (x,y)->x+" "+y)+"}";
}

  public static String trans2String(Collection<PetriNetTransition> mark){
    //return mark.stream().map(x->x.myString()).reduce("{", (x,y)->x+" "+y)+"}";
    return mark.stream().map(x->x.getId()).reduce("{", (x,y)->x+" "+y)+"}";
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

  public void mySetRoots(Set<PetriNetPlace> pls) {
    for(PetriNetPlace pl: places.values()){
      if (pls.contains(pl)) {
        pl.setStart(true);
      } else {
        pl.setStart(false);
      }
    }
    setRoot2Start();
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

  public PetriNetPlace addGluePlace() {
    PetriNetPlace p = addPlace(getId()+":G:" + placeId++);
    p.setStart(false);
    p.setTerminal("");
    return p;
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
    //System.out.println("added "+transition.getId());
    //System.out.println("Check "+ myString());
    return transition;
  }

  public PetriNetTransition addTransition(String label) {

    return addTransition(id + ":t:" + transitionId++, label);
  }

  public PetriNetEdge addEdge(PetriNetTransition to, PetriNetPlace from, Set<String> owner)
    throws CompilationException {
    //System.out.println("addEdge from "+from.getId()+ " to "+to.getId());
    //System.out.println("in net "+myString());
    if (!transitions.containsValue(to) ) {
      throw new CompilationException(getClass(), "Cannot add an edge to transition "+
        to.getId()+" in  petrinet");
    }
    if (!places.containsValue(from)) {
      throw new CompilationException(getClass(), "Cannot add an edge from Place "+
        from.getId()+" not inside the petrinet");
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
    //System.out.println("addEdgePT done "+edge.myString());
    return edge;
  }

  public PetriNetEdge addEdge(PetriNetPlace to, PetriNetTransition from, Set<String> owner) throws CompilationException {

    //System.out.println("addEdge from "+from.getId()+ " to "+to.getId());
    if (!transitions.containsValue(from) ) {
      throw new CompilationException(getClass(), "Cannot add an edge from transition "+
                          from.getId()+" in  petrinet");
    }
    if (!places.containsValue(to)) {
      throw new CompilationException(getClass(), "Cannot add an edge to Place "+
                         to.getId()+" not inside the petrinet");
    }
    if (to == null || from == null) {
      throw new CompilationException(getClass(), "Either " + to + " or " + from + "are null");
    }
    //System.out.println("XXX");
    String id = this.id + ":e:" + edgeId++;
    PetriNetEdge edge = new PetriNetEdge(id, to, from);
    edge.setOwners(owner);
    to.getIncoming().add(edge);
    from.getOutgoing().add(edge);
    edges.put(id, edge);
//System.out.println("addEdgeTP done "+edge.myString());
    return edge;
  }


  public void removePlace(PetriNetPlace place) throws CompilationException {
    if (!places.containsValue(place)) {
      throw new CompilationException(getClass(), "Cannot remove a place that is not part of"
          + "the petrinet");
    }
   //System.out.println("Removing "+place.getId());
    Set<PetriNetEdge> toRemove = new HashSet<>();
    toRemove.addAll(place.getIncoming());
    toRemove.addAll(place.getOutgoing());
    toRemove = toRemove.stream().filter(edges::containsValue).collect(Collectors.toSet());
    for (PetriNetEdge edge : toRemove) {
      //System.out.println("remove  "+ edge.myString());
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
   //System.out.println("REMOVED "+place.getId()+ " so CHECK "+myString());
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
    // Opps alphabet.remove(transition.getLabel(), transition);
  }

  public void removeEdge(PetriNetEdge edge) throws CompilationException {
    if (!edges.values().contains(edge)) {
      throw new CompilationException(getClass(), "Cannot remove an edge that is not part of"
          + "the petrinet");
    }

   if (edge.getTo() instanceof PetriNetTransition) {
      ((PetriNetTransition) edge.getTo()).removeEdge(edge);
      ((PetriNetPlace) edge.getFrom()).removeEdge(edge);
    } else {
      ((PetriNetPlace) edge.getTo()).removeEdge(edge);
      ((PetriNetTransition) edge.getFrom()).removeEdge(edge);
    }
    edges.remove(edge.getId());
    //System.out.println("removedEdge "+this.myString());
  }

  /**
   *
   * @param petriToAdd
   * @return
   */
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

  public void joinPetrinet(Petrinet petriToJoin) {
    Set<PetriNetPlace> roots = new HashSet<>();
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    owners.remove(Petrinet.DEFAULT_OWNER);

    this.places.putAll(petriToJoin.getPlaces());
    this.transitions.putAll(petriToJoin.getTransitions());
    this.edges.putAll(petriToJoin.getEdges());

    return;
  }

  /**
   *  inout Place IDs so that nets can be joined prior to glueing!
   * @param set1
   * @param set2
   * @return
   * @throws CompilationException
   */
  public Set<PetriNetPlace> gluePlaces
      (Set<PetriNetPlace> set1, Set<PetriNetPlace> set2)
      throws CompilationException {
 /*   System.out.println("\n GLUE  START "+myString());
    System.out.println("s1 "+ Petrinet.marking2String(set1));
    System.out.println("s2 "+ Petrinet.marking2String(set2));
    */
    for(PetriNetPlace pl : set1){

      if (!places.containsValue(pl)){
        new RuntimeException().printStackTrace();
        throw new CompilationException(getClass(), "set1 node "+ pl.getId()+ " not part"
        + " of the petrinet");
      }
    }
    for(PetriNetPlace pl : set2){

      if (!places.containsValue(pl)){
        new RuntimeException().printStackTrace();
        throw new CompilationException(getClass(), "set2 node "+ pl.getId()+ " not part"
          + " of the petrinet");
      }
    }



    Multimap<PetriNetPlace, PetriNetPlace> products = ArrayListMultimap.create();
    for (PetriNetPlace place1 : set1) {
      for (PetriNetPlace place2 : set2) {
        PetriNetPlace newPlace = this.addGluePlace();
   //System.out.println("Glue "+place1.getId()+" with "+place2.getId()+" = "+newPlace.getId());
        products.put(place1, newPlace);
        products.put(place2, newPlace);
        //newPlace.intersectionOf(place1, place2);
        if (place1.isStart() || place2.isStart()) {
          newPlace.setStart(true);
        }
        newPlace.addRefefances(place1.getReferences());
        newPlace.addRefefances(place2.getReferences());
        newPlace.addFromRefefances(place1.getFromReferences());
        newPlace.addFromRefefances(place2.getFromReferences());
        if ((place1.getTerminal() != null) && place1.getTerminal().equals("STOP") ||
            (place2.getTerminal() != null) && place2.getTerminal().equals("STOP")) {
          newPlace.setTerminal("STOP");
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
      //System.out.println("Trying to remove "+place.getId());
      removePlace(place);
      //System.out.println("Check places" +Petrinet.marking2String(places.values()));
    }

    products.values().stream().filter(PetriNetPlace::isStart).forEach(this::addRoot);
//    System.out.println("GLUE END "+ this.myString());
//    System.out.println("GLUE END");
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