package mc.processmodels.petrinet;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
  public static final String MAPLET = "^";
  public static int netId = 0;

  private Map<String, PetriNetPlace> places = new HashMap<>();
  private Map<String, PetriNetTransition> transitions = new HashMap<>();
  private Multimap<String, PetriNetTransition> alphabet = ArrayListMultimap.create();

  private List<Set<PetriNetPlace>> roots = new ArrayList<>();
  private Set<PetriNetPlace> root ;
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
  private int ownersId = 0;

  /**
   * Build the product of two sets of owners
   * @param set1
   * @param set2
   * @return
   */
  private static Set<String> crossProduct(Set<String> set1, Set<String> set2){
    Set<String> out = new HashSet<>();
    for(String s1: set1) {
      for(String s2:set2){
        out.add(s1+MAPLET+s2);
       //System.out.println("Adding "+s1+MAPLET+s2);
      }
    }
   //System.out.println("crossProduct = "+out);
    return out;
  }
  public Set<PetriNetPlace> getAllRoots(){

    return roots.stream().flatMap(Set::stream).collect(Collectors.toSet());
  }
  public void deTagTransitions() {
    for(PetriNetTransition tr: getTransitions().values()) {
      tr.setLabel(tr.getLabel().split("\\.")[0]);
    }
  }
  public boolean  rootContains(PetriNetPlace pl){
     boolean b = roots.stream().flatMap(Set::stream).
       filter(x->x.getId().equals(pl.getId())).
       collect(Collectors.toSet()).size() >0;
   return b;
  }
  /**
   * TODO add alphabet  + clean up alphabet!
   * USE this when DEBUGGIN
   * Not sure what data consistancy is intended or assumed
   * This method should act both in assertions and as documentation
   * No 2 Transitions should have the same Id
   * No 2 (atomic) Transitions should have the same pre, post sets and label
   * @return
   */
  public boolean validatePNet(){
    boolean ok = true;

     for (PetriNetPlace r : getAllRoots()) {
       if( !r.isStart()) {
         //System.out.println("Root " + r.getId() + " not Start ");
         ok = false;
       }
     }
     for(PetriNetPlace p : this.getPlaces().values()){
       if (p.isStart() && !rootContains(p)) {
         //System.out.println("Start "+p.getId()+" is not Root");
         ok = false;
       }
     }
/*
     for(PetriNetTransition t: getTransitions().values()) {
       if (!alphabet.containsKey(t.getLabel())){
         System.out.println("Transition "+t.getId()+" - "+t.getLabel()+" not in alphabet");
         ok = false;
       }
     } */

    for (String k: transitions.keySet()){
      String id = transitions.get(k).getId();
      if (!id.equals(k)) {
        //System.out.println("transition key "+k+" not trasitionid "+id);
        ok = false;
      }
      boolean match = true;
      for(PetriNetEdge ed: transitions.get(k).getIncoming()){
        if (!ed.getTo().getId().equals(id)) {
          //System.out.println(" Incoming transition edge "+ed.myString()+ " not matched "+id);
          match = false;
        }
      } ok = ok && match;
      for(PetriNetEdge ed: transitions.get(k).getIncoming()){
        if (!ed.getTo().getId().equals(id)) {
          //System.out.println(" Incoming transition edge "+ed.myString()+ " not matched "+id);
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
      //System.out.println(this.getId() + " NOT VALID");
      //System.out.println(this.myString());
    }
    return ok;
  }

  public void setRoot2Start(){

   this.setRoot( getPlaces().values().stream().
      filter(x->x.isStart()).
      collect(Collectors.toSet()));
    /*System.out.println("ROOT set to "+root.stream()
      .map(r->r.getId()+" ").collect(Collectors.joining()) ); */
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
    sb.append(" " + this.getId()+ " alpha " +alphabet.size()+ " {"+
     alphabet.keySet() + "} rt{");
    sb.append( this.getRoots().stream().
            map(r->r.stream().map(x->x.getId()).reduce("",(x, y)->x+" "+y)+"}").
            reduce("{",(x, y)->x+" "+y)+"}}");
    sb.append(this.getPlaces().values().stream().
      filter(x->x.isTerminal()).
      map(x->(x.getId()+"=>"+x.getTerminal())).
      reduce(" end{",(x,y)->x+" "+y)+"}" );
    sb.append(" owners "+owners);

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

    sb.append(this.getEdges().values().stream().map(ed-> "\n"+ed.myString()).
      reduce("",(x,y)-> x+" "+y));
    return sb.toString();
  }
  public static Petrinet oneEventNet(String event)throws CompilationException{
    Petrinet eventNet = new Petrinet(event,false);
    PetriNetPlace start = eventNet.addPlace();
    start.setOwners(Collections.singleton(DEFAULT_OWNER));
    start.setStart(true);
    eventNet.mySetRoots(Collections.singleton(start));
    PetriNetPlace end = eventNet.addPlace();
    end.setOwners(Collections.singleton(DEFAULT_OWNER));
    PetriNetTransition tr = eventNet.addTransition(event);
    eventNet.addEdge(end,tr, Collections.singleton(DEFAULT_OWNER));
    eventNet.addEdge(tr, start, Collections.singleton(DEFAULT_OWNER));
    eventNet.setOwners(Collections.singleton(DEFAULT_OWNER));
    end.setTerminal("STOP");
    eventNet.reown();
    //System.out.println("oneEventNet "+eventNet.myString());
    return eventNet;
  }
  // called from interpretor
  public static Petrinet stopNet() {
    return  Petrinet.stopNet("");
  }

  // called from interpretor when local reference needs to be used
  public static Petrinet stopNet(String ref){
      Petrinet stop = new Petrinet("stop");
      stop.setOwners(Collections.singleton(DEFAULT_OWNER));
//?? when are the places added??
    for(PetriNetPlace p : stop.getPlaces().values()) {
      p.addFromRefefances( new HashSet(Collections.singleton(ref)));
      p.setTerminal("STOP");
      p.setOwners(Collections.singleton(DEFAULT_OWNER));
    }
    stop.reown();
    return stop;
  }
  public static Petrinet errorNet(){
    Petrinet error = new Petrinet("error");
    error.setOwners(Collections.singleton(DEFAULT_OWNER));
    for(PetriNetPlace p : error.getPlaces().values()) {
      p.setOwners(Collections.singleton(DEFAULT_OWNER));
    }
    error.reown();
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
    this.id = id+Petrinet.netId++;
    this.owners.add(DEFAULT_OWNER);
    //this.root  = roots.get(0);
    if (constructRoot) {
      PetriNetPlace origin = addPlace();
      origin.setStart(true);
      Set<PetriNetPlace> rt  = new HashSet<>(Collections.singleton(origin));
      roots.add(rt);
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
  public void addFirstRoot(PetriNetPlace place) {
    if (places.values().contains(place)) {
      roots.add(Collections.singleton(place));
      //roots.get(0).add(place);
      place.setStart(true);
    }
  }

  public PetriNetPlace addPlace() {
    return addPlace(this.id +":p:" + placeId++);
  }

  public PetriNetPlace addGluePlace() {
    PetriNetPlace p = addPlace(this.id+":G:" + placeId++);
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
      Throwable t = new Throwable();
      t.printStackTrace();
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

    if (place.isStart() || getAllRoots().contains(place)) {
      //TODO: There is alot of weirdness going on with this set
      //Code:
      //processes A = (b->STOP||c->STOP).
      //petrinet A.
      for(Set<PetriNetPlace> rts :roots){
        rts = rts.stream().filter(((Predicate<PetriNetPlace>) place::equals).negate())
          .collect(Collectors.toSet());
      }
//      System.out.println(root.contains(place));
//      System.out.println(root.remove(place));
//      root.stream().map(place::equals).forEach(System.out::println);
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
   * @return  the mapping from the old to new Places
   */
  @SneakyThrows(value = {CompilationException.class})
  public Map<PetriNetPlace, PetriNetPlace> addPetrinet(Petrinet petriToAdd) {
    Set<PetriNetPlace> rts = new HashSet<>();
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    owners.remove(Petrinet.DEFAULT_OWNER);

    for (PetriNetPlace place : petriToAdd.getPlaces().values()) {
      PetriNetPlace newPlace = addPlace();
      newPlace.copyProperties(place);
      placeMap.put(place, newPlace);
    }
    //System.out.println("addPetri rt"+ myString(petriToAdd.getRoot()));
    for(PetriNetPlace rpl : petriToAdd.getRoot()){
      rts.add(placeMap.get(rpl));
      this.addFirstRoot(placeMap.get(rpl));
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
    //System.out.println("addPetri");
    this.validatePNet();
    return placeMap;
  }

  @SneakyThrows(value = {CompilationException.class})
  public Map<PetriNetPlace, PetriNetPlace> addPetrinetNoOwner(Petrinet petriToAdd) {
    Set<PetriNetPlace> rts = new HashSet<>();
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    //owners.remove(Petrinet.DEFAULT_OWNER);

    for (PetriNetPlace place : petriToAdd.getPlaces().values()) {
      PetriNetPlace newPlace = addPlace();
      newPlace.copyProperties(place);
      placeMap.put(place, newPlace);
    }
    //System.out.println("addPetri root"+ myString(petriToAdd.getRoot()));
    for(PetriNetPlace rpl : petriToAdd.getRoot()){
      rts.add(placeMap.get(rpl));
      this.addFirstRoot(placeMap.get(rpl));
      //System.out.println("addin root "+placeMap.get(rpl).getId());
    }
    for (PetriNetTransition transition : petriToAdd.getTransitions().values()) {
      PetriNetTransition newTransition = addTransition(transition.getLabel());
      transitionMap.put(transition, newTransition);
    }

    for (PetriNetEdge edge : petriToAdd.getEdges().values()) {
      Set<String> postFixed = new HashSet<>();
      postFixed.addAll(edge.getOwners());
      //owners.addAll(postFixed);
      if (edge.getFrom() instanceof PetriNetPlace) {
        addEdge(transitionMap.get(edge.getTo()), placeMap.get(edge.getFrom()),  edge.getOwners());
      } else {
        addEdge(placeMap.get(edge.getTo()), transitionMap.get(edge.getFrom()),  edge.getOwners());
      }
    }
    //System.out.println("addPetri");
    this.validatePNet();
    return placeMap;
  }

  public void joinPetrinet(Petrinet petriToJoin) {
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    owners.remove(Petrinet.DEFAULT_OWNER);


    this.places.putAll(petriToJoin.getPlaces());
    this.transitions.putAll(petriToJoin.getTransitions());
    this.edges.putAll(petriToJoin.getEdges());
    this.alphabet.putAll(petriToJoin.getAlphabet());
    return;
  }

  /*
  1. Build map  from oldOwner to set of newOwners "o1 = o1^o2a, o1^o2b"
  2. Apply mapping to Petrinet
  3. Rebuild owners of a PetriNet (simplify by removing "^"  may be  "o1,o2 => ox")
     Note a Place may have a set of owners
     the product of two sets = the (flat) product of its subsets
     Let S = {S1,S2} and R = {R1,R2} then S*R = {S1*R1,S1*R2,S2*R1,S2*R2}
 */

  /** Gluing must be sequential. For second glueing sets must be computed AFTER first glueing
   * Let A = a->A.
   * B = x->B.
   * C = B||d->x->e->STOP.
   * Consider    P = A[]C   and P = C/{A/x}
   *  inout Place IDs so that nets can be joined prior to glueing!
   * @param set1
   * @param set2
   * @return
   * @throws CompilationException
   */
  public Set<PetriNetPlace> gluePlaces
      (Set<PetriNetPlace> set1, Set<PetriNetPlace> set2, boolean changeOwner)
      throws CompilationException {
   //System.out.println("\n GLUE  START "+myString());
   //System.out.println("s1 "+ Petrinet.marking2String(set1));
   //System.out.println("s2 "+ Petrinet.marking2String(set2));

    for(PetriNetPlace pl : set1){
      if (!places.containsValue(pl)){
        new RuntimeException().printStackTrace();
        throw new CompilationException(getClass(), "set1 node "+ pl.getId()+ " not part"
        + " of the petrinet\n");
      }
    }
    for(PetriNetPlace pl : set2){
      if (!places.containsValue(pl)){
        new RuntimeException().printStackTrace();
        throw new CompilationException(getClass(), "set2 node "+ pl.getId()+ " not part"
          + " of the petrinet\n");
      }
    }
    //Build owner 2 owner MultiMap  Essential that old owners not the same new owners
   //System.out.println("set1 = "+ set1.stream().map(x->x.getId()+" ").collect(Collectors.toSet()));
   //System.out.println("set2 = "+ set2.stream().map(x->x.getId()+" ").collect(Collectors.toSet()));
    Multimap<String, String> combinationsTable = ArrayListMultimap.create();
    Set<String> owns1 = set1.stream().map(x->x.getOwners()).
      flatMap(Set::stream).distinct().collect(Collectors.toSet());
    Set<String> owns2 = set2.stream().map(x->x.getOwners()).
      flatMap(Set::stream).distinct().collect(Collectors.toSet());
   //System.out.println("owns1 "+owns1);
   //System.out.println("owns2 "+owns2);
    for(String o1:owns1) {
      for (String o2 : owns2) {
        combinationsTable.put(o1, o1+MAPLET+o2);
        combinationsTable.put(o2, o1+MAPLET+o2);
      }
    }
    /*System.out.println("COMBINATION "+ combinationsTable.keySet().stream().
      map(x->x+"->"+combinationsTable.get(x)+", ").
      collect(Collectors.joining())+ " "); */




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
        newPlace.setOwners(Petrinet.crossProduct(place1.getOwners(),place2.getOwners()));
        if ((place1.getTerminal() != null) && place1.getTerminal().equals("STOP") ||
            (place2.getTerminal() != null) && place2.getTerminal().equals("STOP")) {
          newPlace.setTerminal("STOP");
        }
       //System.out.println("newPlace "+newPlace.myString()); //good
      }
    }


//Owners used as key to combinationsTable after it is built
    // How do we cope with second glueing
    for (PetriNetPlace place : Iterables.concat(set1, set2)) {
      for (PetriNetPlace product : products.get(place)) {
       //System.out.println("product Owners  "+product.getOwners());
        for (PetriNetEdge edge : place.getIncoming()) {
          product.getIncoming().add(addEdge(product, (PetriNetTransition) edge.getFrom(),
            product.getOwners()));
        }
        for (PetriNetEdge edge : place.getOutgoing()) {
          product.getOutgoing().add(addEdge((PetriNetTransition) edge.getTo(), product,
            product.getOwners()));
        }
      }
    }
//Built MultiMap now replace Net owners

   //System.out.println("So far "+ myString());

    owners =  owners.stream().map(x->combinationsTable.get(x)).
            flatMap(Collection::stream).collect(Collectors.toSet());
   //System.out.println("owners = "+owners);

    for(PetriNetEdge edge: getEdges().values()){
      Set<String> U = new HashSet<>(combinationsTable.keySet());
      U.retainAll(edge.getOwners());
      if(!U.equals(Collections.emptySet())) {
        edge.setOwners(edge.getOwners().stream().map(x -> (combinationsTable.get(x))).
          flatMap(Collection::stream).collect(Collectors.toSet()));
      }
    }

    for(PetriNetPlace pl: getPlaces().values()){
      Set<String> U = new HashSet<>(combinationsTable.keySet());
      U.retainAll(pl.getOwners());
      if(!U.equals(Collections.emptySet())) {
        pl.setOwners(pl.getOwners().stream().map(x -> (combinationsTable.get(x))).
          flatMap(Collection::stream).collect(Collectors.toSet()));
      }// else its a newPlace
    }

    for (PetriNetPlace place : Iterables.concat(set1, set2)) {
     //System.out.println("Trying to remove "+place.getId());
      if (getPlaces().values().contains(place)) removePlace(place);
      if (getPlaces().keySet().contains(place.getId())){
       //System.out.println("Opps "+ place.getId());
        removePlace(places.get(place.getId()));
      }
      //System.out.println("Check places" +Petrinet.marking2String(places.values()));
    }
    reown();
    products.values().stream().filter(PetriNetPlace::isStart).forEach(this::addFirstRoot);
   //System.out.println("GLUE END "+ this.myString());
   //System.out.println("GLUE END");
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

  public void reown(){
    //System.out.println("Reown Start "+myString());
    Map<String,String> ownerMap = new HashMap<>();
    for(String o:owners){
      ownerMap.put(o,id+"o"+ownersId++);
    }
    owners = owners.stream().map(o->ownerMap.get(o)).collect(Collectors.toSet());
    for(PetriNetEdge ed: getEdges().values()){
      ed.setOwners(ed.getOwners().stream().
           map(o->ownerMap.get(o)).collect(Collectors.toSet()));
    }
    for(PetriNetPlace pl: getPlaces().values()){
      pl.setOwners(pl.getOwners().stream().
        map(o->ownerMap.get(o)).collect(Collectors.toSet()));
    }
   //System.out.println("Reowned "+myString());
  }

  public Set<String> getTranOwners(PetriNetTransition tr) {
    Set<String> trOwn = tr.getOutgoing().stream().map(x->x.getOwners()).
                     flatMap(Set::stream).distinct(). collect(Collectors.toSet());
    trOwn.addAll(tr.getIncoming().stream().map(x->x.getOwners()).
      flatMap(Set::stream).distinct(). collect(Collectors.toSet()));
    return trOwn;
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