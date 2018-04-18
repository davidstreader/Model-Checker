package mc.processmodels.petrinet;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
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
/*
   Use of ids as keys is ESSENTIAL as objects will often change
   THE Ids ARE UNIQIE
 */
  public Map<String,String> nameMap = new HashMap<>();
  private Map<String, PetriNetPlace> places = new HashMap<>();
  private Map<String, PetriNetTransition> transitions = new HashMap<>();
  private Multimap<String, PetriNetTransition> alphabet = ArrayListMultimap.create();

  private List<Set<String>> roots = new ArrayList<>();
  //private List<Set<String>> rootNames = new ArrayList<>();
  //private Set<PetriNetPlace> root ;
  private Map<String, PetriNetEdge> edges = new HashMap<>();
  private Set<RelabelElementNode> relabels = new HashSet<>();
  private Multimap<String, String> combinationsTable = ArrayListMultimap.create(); // Glue function owners map
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

  public void addOwner(String o) {
    for(String s: owners) {
      if (s.equals(DEFAULT_OWNER)) {owners.remove(DEFAULT_OWNER);}
    }
    owners.add(o);
  }





public void rebuildAlphabet(){
  Multimap<String, PetriNetTransition> alpha = ArrayListMultimap.create();
  for(String s : alphabet.keySet()){
    for (PetriNetTransition tr: alphabet.get(s)){
      alpha.put(s,tr);
    }
  }
  alphabet = alpha;
  }

  /**
   * termination if the union of owners of all last transitions is the net owners
   * and all pre transitions are last!
   * TODO:
   * @return
   */
  public boolean terminates() {
    System.out.println("\n Terminates "+ myString());
    Set<String> ownTr = new HashSet<>();
    Set<PetriNetTransition> endTr = new HashSet<>();
    for (PetriNetPlace pl: places.values()){
      if (pl.isTerminal()){
        endTr.addAll(pl.pre());
        ownTr.addAll(pl.pre().stream().map(x->x.getOwners()).
                flatMap(Set::stream).collect(Collectors.toSet()));
      }
    }
    System.out.println("owners "+owners+ " ownTr "+ownTr+" "+getOwners().equals(ownTr)+ "\n");
     return getOwners().equals(ownTr);
  }

  public List<Set<String>> getRootNames(){
    return roots;
  }
  public List<Set<PetriNetPlace>> getRootPlacess(){
    List<Set<PetriNetPlace>> out = new ArrayList<>();
    for(Set<String> markNames :roots) {
      out.add(markNames.stream().map(x->places.get(x)).collect(Collectors.toSet()));
    }
    return out;
  }
  public int nextRootNo () {
    return roots.size();
  }
  public void clearRoots() {
    roots = new ArrayList<>();
    for(PetriNetPlace pl : getPlaces().values()) {
      pl.setStartNos(Collections.emptySet());
    }
  }

  public PetriNetPlace getPlace(String id) {
    return places.get(id);
  }
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

    return roots.stream().flatMap(Set::stream).map(x->places.get(x)).collect(Collectors.toSet());
  }
  public void deTagTransitions() {
    for(PetriNetTransition tr: getTransitions().values()) {
      tr.setLabel(tr.getLabel().split("\\.")[0]);
    }
  }
  public boolean  rootContains(PetriNetPlace pl){
     boolean b = roots.stream().flatMap(Set::stream).
       filter(x->x.equals(pl.getId())).
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
       if((r==null) || !r.isStart()) {
         System.out.println("Root " + r.getId() + " not Start ");
         ok = false;
       }
     }
     for(PetriNetPlace p : this.getPlaces().values()){
       if (p.isStart() && !rootContains(p)) {
       //System.out.println(p.getId()+ " StartNos "+ p.getStartNos());
         Set<Integer> failed = p.getStartNos().stream().
              filter(x-> (roots.size()< x) ||!roots.get(x-1).contains(p)).collect(Collectors.toSet());
         if (failed.size() >0) {
          System.out.println("Start "+p.getId()+" is not Root "+ failed);
           ok = false;
         }
       }
     }
     for (int i =0 ; i< roots.size(); i++) {
       Set<String> fail = roots.get(i);
       for (String pl : fail) {
         if ( places.get(pl) == null ||
             !places.get(pl).isStart() ||
             !places.get(pl).getStartNos().contains(i+1)) {
          System.out.println("Not Start " + places.get(pl).myString() + " index " + (i+1));
           ok = false;
         }
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



    for (String s : places.keySet()){
      if (!places.get(s).getId().equals(s)) {
        System.out.println(" Invalid places "+s+" -> " +places.get(s));
        ok = false;
        break;
      }
    }

      if (!ok) {
      Throwable t = new Throwable();
      t.printStackTrace();
      System.out.println("SORT IT OUT \n"+this.myString()+"\nSORT OUT ABOVE\n");
    }
    return ok;
  }

  public void setStartFromRoot() {
    int rNo = 1;
    places.values().stream().forEach(pl -> pl.setStartNos(Collections.emptySet()));
    for (Set<String> m : roots) {
      for(String name : m){
        places.get(name).addStartNo(rNo);
      }
      rNo++;
    }
  }
  public void setRootFromStart(){
  Multimap<Integer, String>  rootBuilder = LinkedHashMultimap.create();
    Set<String> starts = getPlaces().values().stream().
      filter(x->x.isStart()).map(x->x.getId()).collect(Collectors.toSet());
  //System.out.println("setRootFromStart() "+ starts.stream().collect(Collectors.joining()));
   for (String pl : starts){
     for (Integer i : places.get(pl).getStartNos()) {
        rootBuilder.put(i,pl);
     //System.out.println("rBuilder "+i+"->"+pl);
     }
   }

   List<Set<String>> rout = new ArrayList<>();
    for (Integer i :rootBuilder.keySet().stream().sorted().collect(Collectors.toSet()) ){
      rout.add((Set<String>) rootBuilder.get(i));
     //System.out.println("roots "+ i +" "+rootBuilder.get(i).stream().collect(Collectors.joining()));
    }
  //System.out.println("END "+ rout);
    roots = rout;
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

  public Set<PetriNetTransition> preTransitions(PetriNetPlace pl){
    Set<PetriNetTransition> pre = new HashSet<>();
    for(PetriNetEdge edge: pl.getIncoming()){
      pre.add((PetriNetTransition) edge.getFrom());
    }
    return pre;
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
     alphabet.keySet() + "} rt");
    if (roots!= null) {
      sb.append( this.getRoots().stream().
        map(r->r.stream().reduce("{",(x, y)->x+" "+y)+"}").
        reduce("{",(x, y)->x+" "+y)+"}");
    } else sb.append("null");

    sb.append(this.getPlaces().values().stream().
      filter(x->x.isTerminal()).
      map(x->(x.getId()+"=>"+x.getTerminal())).
      reduce(" end{",(x,y)->x+" "+y)+"}" );
    sb.append(" owners "+owners);

    sb.append(this.getPlaces().values().stream().
      map(x->x.getId())
      .reduce("\n"+ getPlaces().size()+" places ",(x,y)->x+", "+y));
    sb.append(this.getPlaces().keySet().stream().
      map(x->"\n"+x+"->"+ getPlaces().get(x).myString())
      .reduce( "",(x,y)->x+" "+y));
    sb.append(this.getTransitions().keySet().stream().reduce("\n" +
      getTransitions().size()+ " transitions ",(x,y)->x+", "+y));
    sb.append(this.getTransitions().values().stream().
      map(tr-> "\n"+tr.myString()).
      reduce("",(x,y)-> x+" "+y));

    sb.append(this.getEdges().values().stream().map(ed-> "\n"+ed.myString()).
      reduce("",(x,y)-> x+" "+y));
    return sb.toString();
  }

  public void setRootPlace(PetriNetPlace r){
    Set<String> rt = Collections.singleton(r.getId());
    List<Set<String>> rts = new ArrayList<Set<String>>();
    rts.add(rt);
    setRoots(rts);
  }
  public static Petrinet oneEventNet(String event)throws CompilationException{
    Petrinet eventNet = new Petrinet(event,false);
    PetriNetPlace start = eventNet.addPlace();
    start.setStart(true);
    start.setStartNos(Collections.singleton(1));
    eventNet.setRootPlace(start);
    PetriNetPlace end = eventNet.addPlace();
    PetriNetTransition tr = eventNet.addTransition(event);
    tr.setOwners(Collections.singleton(DEFAULT_OWNER));
    eventNet.addEdge(end,tr);
    eventNet.addEdge(tr, start);
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
      stop.setOwners(Collections.emptySet());
//?? when are the places added??
    PetriNetPlace p  = stop.getPlaces().values().iterator().next();
      p.addFromRefefances( new HashSet(Collections.singleton(ref)));
      p.setTerminal("STOP");
      p.setStart(true);
      p.setStartNos(Collections.singleton(1));
      stop.setRootPlace(p);
    stop.reown();
    return stop;
  }
  public static Petrinet errorNet(){
    Petrinet error = new Petrinet("error");
    error.setOwners(Collections.emptySet());
    PetriNetPlace p  = error.getPlaces().values().iterator().next();
      p.setStart(true);
      p.setStartNos(Collections.singleton(1));
    error.setRootPlace(p);
    error.reown();
    return error;
  }
public static String marking2String(Collection<PetriNetPlace> mark){
  //return mark.stream().map(x->x.myString()).reduce("{", (x,y)->x+" "+y)+"}";
  if (mark==null) return "null";
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
      origin.setStartNos(Collections.singleton(1));
      Set<String> rt  = new HashSet<>(Collections.singleton(origin.getId()));
      roots.add(rt);

    }
  }


  public void addFirstRoot(PetriNetPlace place) {
    if (places.values().contains(place)) {
      roots.add(Collections.singleton(place.getId()));
      //roots.get(0).add(place);
      place.setStart(true);
    }
  }

  public void addRoot(Set<String> r){
    roots.add(r);
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

  public PetriNetEdge addEdge(PetriNetTransition to, PetriNetPlace from)
    throws CompilationException {
   //System.out.println("adding Edge to "+to.toString()+ "  from "+from.toString());
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
    to.getIncoming().add(edge);
    from.getOutgoing().add(edge);
    edges.put(id, edge);
    //System.out.println("addEdgePT done "+edge.myString());
    return edge;
  }

  public PetriNetEdge addEdge(PetriNetPlace to, PetriNetTransition from) throws CompilationException {

   //System.out.println("addingEdge from "+from.getId()+ " to "+to.getId());
   //System.out.println("trans "+transitions.keySet().stream().map(x->x+"->"+transitions.get(x).getId()).collect(Collectors.joining()));


    if (!transitions.containsKey(from.getId()) ) {
      Throwable t = new Throwable();
      t.printStackTrace();
      throw new CompilationException(getClass(), "Cannot add an edge "+
                          from.getId()+"->"+to.getId()+" in  petrinet "+myString());
    }
    if (!places.containsValue(to)) {
      Throwable t = new Throwable();
      t.printStackTrace();
      throw new CompilationException(getClass(), "Cannot add an edge  "+ from.getId()+"->"+
                         to.getId()+" not inside the petrinet "+myString());
    }
    if (to == null || from == null) {
      throw new CompilationException(getClass(), "Either " + to + " or " + from + "are null");
    }
    //System.out.println("XXX");
    String id = this.id + ":e:" + edgeId++;
    PetriNetEdge edge = new PetriNetEdge(id, to, from);
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
      //TODO: There is alot of weirdness going on streams
      List<Set<String>> Nroots = new ArrayList<>();
      for(Set<String> rs :roots){
        Set<String> Nrts = new HashSet<>();
        for(String r:rs) {
          if (!place.getId().equals(r)) {
            Nrts.add(r);
          }
        }
        Nroots.add(Nrts);
      }
 //System.out.println("\nremoving "+place.getId()+" from " +roots+" gives "+Nroots+"\n");
      setRoots(Nroots);
//System.out.println(root.contains(place));
//System.out.println(root.remove(place));
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
  public Map<PetriNetTransition, PetriNetTransition> addPetrinet(Petrinet petriToAdd) {
    //Set<PetriNetPlace> rts = new HashSet<>();
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    nameMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    owners.remove(Petrinet.DEFAULT_OWNER);
    owners.addAll(petriToAdd.getOwners());

    for (PetriNetPlace place : petriToAdd.getPlaces().values()) {
      PetriNetPlace newPlace = addPlace();
      newPlace.copyProperties(place);
      placeMap.put(place, newPlace);
      nameMap.put(place.getId(), newPlace.getId());
    }
   //System.out.println("this " +getRoots() + " addPetri rt "+ petriToAdd.getRoots());
    for(Set<String> roots: petriToAdd.getRootNames()) {
      Set<String> next = new HashSet<>();
      for (String rpl : roots) {
        //rts.add(placeMap.get(rpl));
        next.add(nameMap.get(rpl));
        //System.out.println("addin root "+placeMap.get(rpl).getId());
      }
      addRoot(next);
    }
    for (PetriNetTransition transition : petriToAdd.getTransitions().values()) {
      PetriNetTransition newTransition = addTransition(transition.getLabel());
      newTransition.setOwners(transition.getOwners());
      transitionMap.put(transition, newTransition);
    }

    for (PetriNetEdge edge : petriToAdd.getEdges().values()) {
      if (edge.getFrom() instanceof PetriNetPlace) {
        PetriNetEdge ed =
          addEdge(transitionMap.get(edge.getTo()), placeMap.get(edge.getFrom()));
       //System.out.println("addEdge "+ed.myString());
      } else {
        PetriNetEdge ed =
          addEdge(placeMap.get(edge.getTo()), transitionMap.get(edge.getFrom()));
       //System.out.println("addEdge "+ed.myString());
      }
    }
    //System.out.println("addPetri");
    this.validatePNet();
   //System.out.println("addPetrinet "+ roots);
    return transitionMap;
  }

  @SneakyThrows(value = {CompilationException.class})
  public Map<PetriNetTransition, PetriNetTransition> addPetrinetNoOwner(Petrinet petriToAdd, String tag) {
   //System.out.println("Start of add "+petriToAdd.myString());
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<String,String> nameMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    owners.addAll(petriToAdd.getOwners());

    for (PetriNetPlace place : petriToAdd.getPlaces().values()) {
      PetriNetPlace newPlace = addPlace(place.getId()+tag);
      newPlace.copyProperties(place);
      placeMap.put(place, newPlace);
      nameMap.put(place.getId(),newPlace.getId());
     //System.out.println("placeMap "+place.getId()+"->"+newPlace.getId());
    }


    for (PetriNetTransition transition : petriToAdd.getTransitions().values()) {
      PetriNetTransition newTransition = this.addTransition(transition.getLabel());
      newTransition.setOwners(transition.getOwners());
      transitionMap.put(transition, newTransition);
      System.out.println(newTransition.myString());
    }
   /*System.out.println("tranMap "+ transitionMap.keySet().stream().
      map(x->x.getId()+"->"+transitionMap.get(x).getId()).collect(Collectors.joining()));
    System.out.println(myString()); */

    for (PetriNetEdge edge : petriToAdd.getEdges().values()) {
      //owners.addAll(postFixed);
     //System.out.println("edge "+edge.myString());
      if (edge.getFrom() instanceof PetriNetPlace) {
  //System.out.println("edging "+transitionMap.get(edge.getTo())+" "+places.get(edge.getFrom().getId()+tag));
        addEdge(transitionMap.get(edge.getTo()), places.get(edge.getFrom().getId()+tag));
      } else {
  //System.out.println("edgeing "+ places.get(edge.getTo().getId()+tag) +
   //     "  "+transitionMap.get(edge.getFrom()));
        addEdge(places.get(edge.getTo().getId()+tag), transitionMap.get(edge.getFrom()));
      }
    }
    this.getRoots().addAll(petriToAdd.getRoots().stream().map(x->markUpGrade(x,nameMap)).collect(Collectors.toList()));
 //this.setRoots(petriToAdd.getRoots().stream().map(x->markUpGrade(x,nameMap)).collect(Collectors.toList()));
//System.out.println("addPetri "+ this.myString());
    //Net not always valid!
    return transitionMap;
  }

  public Set<String> markUpGrade(Set<String> mark, Map<String,String> nameMap){
    return mark.stream().map(x->nameMap.get(x)).collect(Collectors.toSet());
  }
  public void joinPetrinet(Petrinet petriToJoin) {
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();


    this.owners.addAll(petriToJoin.getOwners());
    this.places.putAll(petriToJoin.getPlaces());
    this.transitions.putAll(petriToJoin.getTransitions());
    this.edges.putAll(petriToJoin.getEdges());
    this.alphabet.putAll(petriToJoin.getAlphabet());
    this.roots.addAll(petriToJoin.getRoots());  // new 4 [] will it break other ops?
    return;
  }

  /**
   * For sequential composition A=>B where B has more than one Root
   * we need to copy A_E the end marking of A to be glued to the Roots. This includes
   * copying pre(A_E) the pre transitions of A_E
   */
 public Set<String> copyEnd(Set<String> endPlaces, int i) throws CompilationException {
   // set up place to new palce mapping
  //System.out.println("copy End "+endPlaces);
   Map<PetriNetPlace,PetriNetPlace> plMapping =
     endPlaces.stream().map(x->places.get(x)).
         collect(Collectors.toMap(pl->pl,pl->addPlace(pl.getId()+"*"+i)));
  //System.out.println("plMapping  "+ plMapping.keySet().stream().
  //   map(x->x.getId()+"->"+plMapping.get(x).getId()).collect(Collectors.joining()));
     Set<PetriNetTransition> lastTrans =
     endPlaces.stream().map(x-> preTransitions(places.get(x))).
               flatMap(Set::stream).distinct().collect(Collectors.toSet());
  //System.out.println("lastTran = "+lastTrans.stream().map(x->x.getId()).collect(Collectors.joining()));
     // now build the new transitions
     for(PetriNetTransition tr: lastTrans) {
System.out.println("TR "+tr.myString());
       PetriNetTransition newtr =  addTransition(tr.getId()+"*"+i ,tr.getLabel());
       for(PetriNetEdge ed : tr.getOutgoing()){

         addEdge(plMapping.get(ed.getTo()),newtr);
       }
       for(PetriNetEdge ed : tr.getIncoming()){
        //System.out.println("ed + "+ ed.myString());
         addEdge(newtr ,((PetriNetPlace) ed.getFrom()));
       }
//System.out.println("     "+tr.getId()+"=>"+newtr.getId());
     }


   return  plMapping.values().stream().map(x->x.getId()).collect(Collectors.toSet());
 }

  /**
   * The consturuction of owners when two sets of places are glued together
   * is constructed independently of the places.
   *  Both, and only, owners on the Net and transitions are updated
   * @param owns1
   * @param owns2
   */
 public void glueOwners(Set<String> owns1, Set<String> owns2) {
   System.out.println("\n glueOwners Start "+myString());
   combinationsTable = ArrayListMultimap.create();
   for (String o1 : owns1) {
     for (String o2 : owns2) {
       combinationsTable.put(o1, o1 + MAPLET + o2);//Assumes disjoint ownership!
       combinationsTable.put(o2, o1 + MAPLET + o2);
     }
   }
   System.out.println("GlueOwners Table "+ " \n"+ combinationsTable.keySet().stream().
     map(x->x+"->"+combinationsTable.get(x)+", \n").
     collect(Collectors.joining())+ " ");

   System.out.println("owners1 = "+owners);
   Set<String> newOwners = new HashSet<>();
   for(String o:owners) {
     if(combinationsTable.keySet().contains(o)){
       newOwners.addAll(combinationsTable.get(o));
     } else {
       newOwners.add(o);
     }
   }
   owners = newOwners;
   System.out.println("owners2 = "+owners);

   for (PetriNetTransition tr : getTransitions().values()) {
     System.out.println("tr "+tr.myString());
     Set<String> U = new HashSet<>(combinationsTable.keySet());
     U.retainAll(tr.getOwners());
     if (!U.equals(Collections.emptySet())) {
       Set<String> V = new HashSet<>(tr.getOwners());
       V.removeAll(U);
       tr.setOwners(tr.getOwners().stream().map(x -> (combinationsTable.get(x))).
         flatMap(Collection::stream).collect(Collectors.toSet()));
       tr.addOwners(V);
     }// else its a newPlace
   }
   System.out.println("glueOwners END "+myString()+"\n");
 }
  /**
   * Because of the need clone the objects change only the Ids are fixed.
   * Hence use of ids over objects
   * returns a name to newName mapping for the places built by gluing
   * @param m1
   * @param m2
   * @throws CompilationException
   */
  public Map<String,String>  glueNames (Set<String> m1, Set<String> m2)
    throws CompilationException {
    Set<PetriNetPlace> net1 = m1.stream().map(x-> places.get(x)).collect(Collectors.toSet());
    Set<PetriNetPlace> net2 = m2.stream().map(x-> places.get(x)).collect(Collectors.toSet());

    return this.gluePlaces(net1,net2);
  }


  /* Glueing works for the Net marking to Autom  node mapping
     Ownership needed on the Automata  to build the Automa to Net mapping "OwnersRule'
     Should we compute the owners seperatly on Nets OR  build the Net and
     compute the Owners from the Net structure.

  1. Using owners on the Net(not edges) Build map from oldOwner to set of newOwners "o1 = o1^o2a, o1^o2b"
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
   * @return  mapping from old places to set of net places
   * @throws CompilationException
   */
  public Map<String,String> gluePlaces
      (Set<PetriNetPlace> set1, Set<PetriNetPlace> set2)
      throws CompilationException {
  //System.out.println("\n\n GLUE  START \n"+myString());

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
   System.out.println("set1 = "+ set1.stream().map(x->x.getId()+" ").collect(Collectors.toSet()));
   System.out.println("set2 = "+ set2.stream().map(x->x.getId()+" ").collect(Collectors.toSet()));
    //<String, String> combinationsTable = ArrayListMultimap.create();

    // we lack the root count to compute the new root numbers
    Map<String,String> prodNames = new HashMap<>();
    Multimap<PetriNetPlace, PetriNetPlace> products = ArrayListMultimap.create();
    for (PetriNetPlace place1 : set1) {
      for (PetriNetPlace place2 : set2) {
        PetriNetPlace newPlace = this.addGluePlace();
 //System.out.println("Glue "+place1.getId()+" with "+place2.getId()+" = "+newPlace.getId());
        products.put(place1, newPlace);
        products.put(place2, newPlace);
        prodNames.put(place1.getId()+MAPLET+place2.getId(), newPlace.getId());
        prodNames.put(place2.getId()+MAPLET+place1.getId(), newPlace.getId());
      //System.out.println("poducts "+place1.getId()+", "+place2.getId()+"-->"+newPlace.getId());
        //newPlace.intersectionOf(place1, place2);
        if (place1.isStart() && place2.isStart()) {
         //System.out.println("Gluing ForChoice  "+place1.getId()+MAPLET+place2.getId()+" -> "+prodNames.get(place1.getId()+MAPLET+place2.getId()));
          newPlace.setStart(true);
        }
        if (place1.isStart()) {
          newPlace.setStartNos(place1.getStartNos());
          newPlace.setStart(true);
        } else if (place2.isStart()) {
          newPlace.setStartNos(place2.getStartNos());
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
      //System.out.println("newPlace "+newPlace.myString()); //good
      }
    }

   //System.out.println("PROD "+ prodNames);
//To build the new owners old owners used as key to combinationsTable after it is built
    // How do we cope with second glueing??
   //System.out.println(myString());
   //System.out.println("places  "+places.keySet());
   //System.out.println("product "+products.keySet().stream().map(x->x.getId()).collect(Collectors.joining()));
    for (PetriNetPlace place : Iterables.concat(set1, set2)) {
      for (PetriNetPlace product : products.get(place)) {
      //System.out.println("place  "+place.getId()+"   prod " +product.getId());
        for (PetriNetEdge edge : place.getIncoming()) {
         //System.out.println("X "+edge.myString());
          product.getIncoming().add(addEdge(product, (PetriNetTransition) edge.getFrom()));
        }
        for (PetriNetEdge edge : place.getOutgoing()) {
         //System.out.println("Y "+edge.myString());
          product.getOutgoing().add(addEdge((PetriNetTransition) edge.getTo(), product));
        }
      }
    }
//Built MultiMap now replace Net owners




   // glueOwners(owns1,owns2);

    for (PetriNetPlace place : Iterables.concat(set1, set2)) {
      //System.out.println("Trying to remove "+place.getId());
      if (getPlaces().values().contains(place)) removePlace(place);
      if (getPlaces().keySet().contains(place.getId())) {
        //System.out.println("Opps "+ place.getId());
        removePlace(places.get(place.getId()));
      }
      //System.out.println("Check places" +Petrinet.marking2String(places.values()));
    }

    reown();
    //products.values().stream().distinct()
    //  .filter(PetriNetPlace::isStart).forEach(this::addFirstRoot);
  System.out.println("\n  GLUE END "+ this.myString());
   //System.out.println("PROD "+ prodNames);
 //System.out.println("GLUE END\n\n");
    return prodNames;
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

    for(PetriNetTransition tr: getTransitions().values()){
      tr.setOwners(tr.getOwners().stream().
        map(o->ownerMap.get(o)).collect(Collectors.toSet()));
    }
   //System.out.println("Reowned "+myString());
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