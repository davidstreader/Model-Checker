package mc.processmodels.petrinet;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;

import lombok.*;
import mc.Constant;
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

//@Data
public class Petrinet extends ProcessModelObject implements ProcessModel {
  public static final String DEFAULT_OWNER = "_default";
  public static final String MAPLET = "^";
  public static int netId = 0;
  /*
     Use of ids as keys is ESSENTIAL as objects will often change
     THE Ids ARE UNIQIE
   */
  public Map<String, String> nameMap = new HashMap<>();

  @Getter
  @Setter
  private Map<String, PetriNetPlace> places = new HashMap<>();
  @Getter
  @Setter
  private Map<String, PetriNetTransition> transitions = new HashMap<>();
  //Map from alphabet to transition with that label USED in parallel composition
  @Getter
  @Setter
  private Multimap<String, PetriNetTransition> alphabet = ArrayListMultimap.create();

  @Getter
  @Setter
  private List<Set<String>> roots = new ArrayList<>();
  public void clearRoots() { setRoots(new ArrayList<>());}
  /*
  Symbolic data
   */
  @Getter
  @Setter
  private Map<String, String> rootEvaluation = new TreeMap<>();
  @Getter
  @Setter
  private List<Set<String>> ends = new ArrayList<>();
  //private List<Set<String>> rootNames = new ArrayList<>();
  //private Set<PetriNetPlace> root ;
  @Getter
  @Setter
  private Map<String, PetriNetEdge> edges = new HashMap<>();
  @Getter
  @Setter
  private Set<RelabelElementNode> relabels = new HashSet<>();
  @Getter
  @Setter
  private Multimap<String, String> combinationsTable = ArrayListMultimap.create(); // Glue function owners map
  @Getter
  @Setter
  private HidingNode hiding;
  @Getter
  @Setter
  private Set<String> hiddenVariables = new HashSet<>();
  @Getter
  @Setter
  private Location hiddenVariablesLocation;  //Location in code
  @Getter
  @Setter
  private Set<String> owners = new HashSet<>();  // for nd nets should be List<Set<String>>
  /*
  Needed when gluingPlaces
     must be set in GlueOwners  (think event Refinement)
   */
  @Getter
  @Setter
  private Map<String, String> variable2Owner = new TreeMap<>();
  @Getter
  @Setter
  private Location location;
  @Getter
  @Setter
  private String id;
  @Getter
  @Setter
  private int placeId = 0;

  /*
     Symbolic variables
   */
  @Getter
  @Setter
  private int transitionId = 0;
  @Getter
  @Setter
  private int edgeId = 0;
  @Getter
  @Setter
  private int ownersId = 0;
  //Side effect for gluePlaces
  @Getter
  private Multimap<PetriNetPlace, PetriNetPlace> allProducts = ArrayListMultimap.create();

  public Petrinet(String id) {
    this(id, true);
  }

  public Petrinet(String id, boolean constructRoot) {
    super(id, "Petrinet");
    this.id = id + Petrinet.netId++;
    this.setOwners(new HashSet<>());
    this.addOwner(DEFAULT_OWNER);
    //this.root  = roots.get(0);
    if (constructRoot) {
      this.setOwners(new HashSet<>());
      this.addOwner(DEFAULT_OWNER);
      PetriNetPlace origin = addPlace();
      origin.setStart(true);
      origin.setStartNos(new HashSet<>());
      origin.addStartNo(1);
      origin.setOwners(new HashSet<>());
      origin.addOwner(DEFAULT_OWNER);
      roots.add(Collections.singleton(origin.getId()));

    }
  }

  /**
   * Build the product of two sets of owners
   *
   * @param set1
   * @param set2
   * @return
   */
  private static Set<String> crossProduct(Set<String> set1, Set<String> set2) {
    Set<String> out = new HashSet<>();
    for (String s1 : set1) {
      for (String s2 : set2) {
        out.add(s1 + MAPLET + s2);
        //System.out.println("Adding "+s1+MAPLET+s2);
      }
    }
    //System.out.println("crossProduct = "+out);
    return out;
  }

  public static Set<PetriNetTransition> postTransitions(PetriNetPlace pl) {
    Set<PetriNetTransition> post = new HashSet<>();
    for (PetriNetEdge edge : pl.getOutgoing()) {
      post.add((PetriNetTransition) edge.getTo());
    }
    return post;
  }

  public static Set<PetriNetTransition> postTransitions(Set<PetriNetPlace> pls) {
    Set<PetriNetTransition> post = new HashSet<>();
    post = pls.stream().flatMap(x -> postTransitions(x).stream()).distinct().collect(Collectors.toSet());

    return post;
  }

  public static Petrinet oneEventNet(String event) throws CompilationException {
   return  oneEventNet(event,"");
  }
    public static Petrinet oneEventNet(String event,String tag) throws CompilationException {
      Petrinet eventNet = new Petrinet(event, false);
      String nid = "n"+Petrinet.netId++;
    PetriNetPlace start = eventNet.addPlace(nid+"s");
    start.setOwners(new HashSet<>());
    start.addOwner(DEFAULT_OWNER);
    start.setStart(true);
    start.setStartNos(new HashSet<>());
    start.addStartNo(1);
    eventNet.setRootPlace(start);
    PetriNetPlace end = eventNet.addPlace(nid+"e");
    end.setOwners(new HashSet<>());
    end.addOwner(DEFAULT_OWNER);
    PetriNetTransition tr = eventNet.addTransition(event);
    tr.setOwners(new HashSet<>());
    tr.addOwner(DEFAULT_OWNER);
    eventNet.addEdge(end, tr, false);
    eventNet.addEdge(tr, start, false);
    eventNet.setOwners(new HashSet<>());
    eventNet.addOwner(DEFAULT_OWNER);
    end.setTerminal("STOP");
    end.addEndNo(1);
    eventNet.setEndFromPlace();
    eventNet.reId(tag);
    //Throwable t = new Throwable();

//System.out.println("oneEventNet "+eventNet.myString()+"\n");
    //t.printStackTrace();
    return eventNet;
  }

  /*
     (A+B)[]C  external choice [] must copy root and all transiation attached
     subsequently a copy will be glued to each alternative root
     (A[]B)=>(C+D)  copy ends of A[]B and Roots of C+D
   */

  // called from interpretor
  public static Petrinet stopNet() throws CompilationException {
    return Petrinet.stopNet("", "S");
  }
  /*
     Ownership unique within a net so need to tag when nets are to be combined
   */

  public static Petrinet endNet() throws CompilationException {
    //System.out.println("WHY END?");
    return Petrinet.stopNet("", "E");
  }

  // called from interpretor when local reference needs to be used
  public static Petrinet stopNet(String ref, String SorE) throws CompilationException {
    //System.out.println("stopNet "+SorE);
    Petrinet stop = new Petrinet("stop");
    stop.setOwners(new HashSet<>());
    stop.addOwner(DEFAULT_OWNER);

//?? when are the places added??
    PetriNetPlace p = stop.getPlaces().values().iterator().next();
    if (ref != "") p.addFromRefefances(new HashSet(Collections.singleton(ref)));
    if (SorE.equals("S")) {
      p.setTerminal(Constant.STOP);
      p.addEndNo(1);
    }
    else p.setTerminal(Constant.END);

    p.setStart(true);
    p.setOwners(new HashSet<>());
    p.addOwner(DEFAULT_OWNER);
    p.setStartNos(new HashSet<>());
    p.addStartNo(1);
    stop.setRootPlace(p);
    stop.setEndFromPlace();
    stop.reId("");
    //System.out.println("\nstopNet "+stop.myString());
    return stop;
  }

  /*
     This is to rebuild the key to valure relation after reIding
   */

  public static Petrinet startNet() throws CompilationException {
    Petrinet start = new Petrinet("start");
    start.setOwners(new HashSet<>());
    start.addOwner(DEFAULT_OWNER);

//?? when are the places added??
    PetriNetPlace p = start.getPlaces().values().iterator().next();
    p.setTerminal("");

    p.setStart(true);
    p.setOwners(new HashSet<>());
    p.addOwner(DEFAULT_OWNER);
    p.setStartNos(new HashSet<>());
    p.addStartNo(1);
    start.setRootPlace(p);
    start.reId("");
    //System.out.println("\nstartNet "+start.myString());
    return start;
  }

  public static Petrinet errorNet() throws CompilationException {
    Petrinet error = new Petrinet("error");
    error.setOwners(new HashSet<>());
    error.addOwner(DEFAULT_OWNER);
    PetriNetPlace p = error.getPlaces().values().iterator().next();
    p.setStart(true);
    p.setOwners(new HashSet<>());
    p.addOwner(DEFAULT_OWNER);
    p.setStartNos(new HashSet<>());
    p.addStartNo(1);
    error.setRootPlace(p);
    PetriNetPlace end = error.addPlace();
    end.setOwners(new HashSet<>());
    end.addOwner(DEFAULT_OWNER);
    PetriNetTransition tr = error.addTransition(Constant.DEADLOCK);
    tr.setOwners(new HashSet<>());
    tr.addOwner(DEFAULT_OWNER);
    error.addEdge(end, tr, false);
    error.addEdge(tr, p, false);
    error.setOwners(new HashSet<>());
    error.addOwner(DEFAULT_OWNER);
    end.setTerminal(Constant.ERROR);
    error.reId("");

    return error;
  }

  public static boolean isMarkingSTOP(Collection<PetriNetPlace> mark) {
    return mark.stream().map(x -> x.isSTOP()).reduce(true, (x, y) -> x && y);
  }

  public static boolean isMarkingStart(Collection<PetriNetPlace> mark) {
    return mark.stream().map(x -> x.isStart()).reduce(true, (x, y) -> x && y);
  }

  public static boolean isMarkingERROR(Collection<PetriNetPlace> mark) {
    return mark.stream().map(x -> x.isERROR()).reduce(true, (x, y) -> x && y);
  }

  public static boolean isMarkingExternal(Collection<PetriNetPlace> mark) {
    return isMarkingERROR(mark) || isMarkingStart(mark) || isMarkingSTOP(mark);
  }

  public static String marking2String(Collection<PetriNetPlace> mark) {
    //return mark.stream().map(x->x.myString()).reduce("{", (x,y)->x+" "+y)+"}";
    if (mark == null) return "null";
    return mark.stream().map(x -> x.getId()).reduce("{", (x, y) -> x + " " + y) + "}";
  }

  public static String trans2String(Collection<PetriNetTransition> mark) {
    //return mark.stream().map(x->x.myString()).reduce("{", (x,y)->x+" "+y)+"}";
    return mark.stream().map(x -> x.getId()).reduce("{", (x, y) -> x + " " + y) + "}";
  }

  public List<Set<PetriNetPlace>> getRootsPl() {
    List<Set<PetriNetPlace>> out = new ArrayList<>();
    for (Set<String> root : roots) {
      Set<PetriNetPlace> rootOut = new HashSet<>();
      for (String pl : root) {
        rootOut.add(places.get(pl));
      }
      out.add(rootOut);
    }
    return out;
  }


  public void setRootsPl(List<Set<PetriNetPlace>> roots) {
    List<Set<String>> out = new ArrayList<>();
    for (Set<PetriNetPlace> root : roots) {
      Set<String> rootOut = new HashSet<>();
      for (PetriNetPlace pl : root) {
        rootOut.add(pl.getId());
      }
      out.add(rootOut);
    }
    setRoots(out);
  }

  public List<Set<String>> copyRoots() {
    List<Set<String>> out = new ArrayList<>();
    for (Set<String> e : roots) {
      out.add(e);
    }
    return out;
  }

  public List<Set<String>> copyEnds() {
    List<Set<String>> out = new ArrayList<>();
    for (Set<String> e : ends) {
      out.add(e);
    }
    return out;
  }

  public Set<String> getVariables() {
    return variable2Owner.keySet();
  }

  /*  To help debugging the ids are only unique within a PetriNet
      reId CLONES the Petri Net and recomputes ad ids prefixing the id with the tag
      It is essential when composing two Nets to use different tags
   */
  public Petrinet reId(String tag) throws CompilationException {
    //System.out.println("\nreId " + tag + ": " + this.myString("edge") + "\n");
    Petrinet petri = new Petrinet(getId(), false);

    //System.out.println("places  "+places.keySet());
    Set<String> owns = new HashSet<>();
    Map<String, String> placeIdMap = new HashMap<>();
    Map<String, String> transIdMap = new HashMap<>();
    for (PetriNetPlace pl : places.values()) {
      PetriNetPlace newpl = petri.addPlaceWithTag(tag);
      //System.out.println(newpl.myString());
      newpl.copyProperties(pl);
      placeIdMap.put(pl.getId(), newpl.getId());
      for (String plo : pl.getOwners()) {
        if (!owns.contains(plo)) owns.add(plo);
      }
      //System.out.println("added " + newpl.myString());
    }
    //System.out.println("new places  "+petri.getPlaces().keySet());

    //System.out.println("Transitions "+transitions.keySet());
    for (PetriNetTransition tr : transitions.values()) {
      //System.out.println(tr.myString());
      String lab = new String(tr.getLabel());
      PetriNetTransition newtr = petri.addTransitionWithTag(tag, lab);

      transIdMap.put(tr.getId(), newtr.getId());
      newtr.addOwners(tr.getOwners());
      for (String tro : tr.getOwners()) {
        if (!owns.contains(tro)) owns.add(tro);
        //System.out.println("  tro "+tro+" owns"+owns);
      }
      //System.out.println("added "+newtr.myString());
    }
    //System.out.println("new trans  "+petri.getTransitions().keySet());
    //System.out.println("petri \n"+ petri.myString() +"\n");
    //System.out.println("trMap "+ transIdMap.keySet().stream().map(x->x+"->"+transIdMap.get(x)).reduce("",(x,y)->x+" "+y));
    //System.out.println("plMap "+ placeIdMap.keySet().stream().map(x->x+"->"+placeIdMap.get(x)).reduce("",(x,y)->x+" "+y));
    petri.setOwners(owns);
    for (PetriNetEdge ed : getEdges().values()) {
      if (ed.getFrom() instanceof PetriNetPlace) {
        PetriNetTransition to = petri.getTransitions().get(transIdMap.get(ed.getTo().getId()));
        PetriNetPlace from = petri.getPlace(placeIdMap.get(ed.getFrom().getId()));
        PetriNetEdge newed = petri.addEdgeWithTag(tag, to, from, ed.getOptional());
        newed.setGuard(ed.getGuard());
      } else {
        PetriNetPlace to = petri.getPlace(placeIdMap.get(ed.getTo().getId()));
        PetriNetTransition from = petri.getTransitions().get(transIdMap.get(ed.getFrom().getId()));
        PetriNetEdge newed = petri.addEdgeWithTag(tag, to, from, ed.getOptional());
        newed.setGuard(ed.getGuard());
      }
    }
    petri.setVariable2Owner(variable2Owner);
    petri.setRootEvaluation(rootEvaluation);
    //System.out.println("reId before reown "+petri.getVariable2Owner().toString());
    petri.reown(tag);
    petri.setRootFromStart();
    petri.setEndFromPlace();
    //System.out.println("\nReid end " + petri.myString("edge") + "\n");
    return petri;
  }

  public void replaceRoot(PetriNetPlace from,PetriNetPlace to){
    System.out.println("replace roots "+roots +" from "+from.getId()+"  to "+to.getId());
    List<Set<String>> newRoots = new ArrayList<>();
    for(Set<String> root:roots) {
      Set<String> newRoot = new TreeSet<>();
      for(String r:root) {
        if (r.equals(from.getId())){
          newRoot.add(to.getId());
          to.setStart(true);
        } else {
          newRoot.add(r);
        }
      }
      newRoots.add(newRoot);
    }
    roots = newRoots;
    System.out.println("replace newRoots "+newRoots +" from "+from.getId()+"  to "+to.getId());
  }
  public void replaceEnd(PetriNetPlace from,PetriNetPlace to){
    //System.out.println("replace ends "+ends +" from "+from.getId()+"  to "+to.getId());
    List<Set<String>> newEnds = new ArrayList<>();
    for(Set<String> end:ends) {
      Set<String> newEnd = new TreeSet<>();
      for(String e:end) {
        if (e.equals(from.getId())){
          newEnd.add(to.getId());
          to.setTerminal(Constant.STOP);
        } else {
          newEnd.add(e);
        }
      }
      newEnds.add(newEnd);
    }
    ends = newEnds;
    //System.out.println("replace newEnds "+newEnds +" from "+from.getId()+"  to "+to.getId());
  }
/*
   Used in both => and []  to build a copy of the root/end places prior to Gluing
 */
  public PetriNetPlace copyRootOrEnd(PetriNetPlace pl, String t,boolean root) throws CompilationException {
    //System.out.println("copyRootOrEnd "+root);
    PetriNetPlace newpl = this.addPlaceWithTag("cpy" );
    newpl.setOwners(pl.getOwners());
    ////// Fix the root and End on the net as that is KEY
    if (root||!t.equals("None")) {
       replaceEnd(pl,newpl);
    }
    if (!root||!t.equals("None")) {
      replaceRoot(pl,newpl);
    }
    ////////
    newpl.copyRefs(pl);  // copies only the refs for tree2net

    Set<PetriNetTransition> newTrans = preTransitions(pl);

    newTrans.addAll(Petrinet.postTransitions(pl));
    int i = 0;
    for (PetriNetTransition tr : newTrans) {
      i++;
      PetriNetTransition newtr = this.addTransitionWithTag(tr.getLabel(), tr.getLabel());
      newtr.addOwners(tr.getOwners());
      Set<PetriNetEdge> eds = tr.copyIncoming();
      //System.out.println(" tr "+tr.myString());
      eds.addAll(tr.copyOutgoing());
      //System.out.println(" tr "+tr.myString());
      for (PetriNetEdge ed : eds) {
        //System.out.println(" ed "+ed.myString());
        PetriNetEdge e;
        if (ed.getFrom().getId().equals(pl.getId())) {
          e = this.addEdge(newtr, newpl, ed.getOptional());
        } else if (ed.getTo().getId().equals(pl.getId())) {
          e = this.addEdge(newpl, newtr, ed.getOptional());
        } else if (ed.getTo() instanceof PetriNetPlace) {
          e = this.addEdge((PetriNetPlace) ed.getTo(), newtr, ed.getOptional());
        } else {
          e = this.addEdge(newtr, (PetriNetPlace) ed.getFrom(), ed.getOptional());
        }
        e.setGuard(ed.getGuard());
        //System.out.println(" tr "+tr.myString());
      }
      //System.out.println(" *newtr "+newtr.myString());

    }
    //System.out.println(" copyRootOrEnd  **newpl "+newpl.myString());
    return newpl;
  }


  /*
    Tidy up the owners on a Net to make debuggin easier
   */
  public void reown(String tag) {
    //System.out.println("Reown Start "+myString("edge"));
    ownersId = 0;
    Map<String, String> ownerMap = new HashMap<>();
    for (String o : owners) {
      ownerMap.put(o, tag + "o" + ownersId++);
    }
    owners = owners.stream().map(o -> ownerMap.get(o)).collect(Collectors.toSet());

    for (PetriNetTransition tr : getTransitions().values()) {
      tr.setOwners(tr.getOwners().stream().
        map(o -> ownerMap.get(o)).collect(Collectors.toSet()));
    }
    for (PetriNetPlace pl : getPlaces().values()) {
      pl.setOwners(pl.getOwners().stream().
        map(o -> ownerMap.get(o)).collect(Collectors.toSet()));
    }
    Map<String, String> v2o = new TreeMap<>();
    for (String key : variable2Owner.keySet()) {
      String o = variable2Owner.get(key);
      if (ownerMap.containsKey(o)) {
        v2o.put(key, ownerMap.get(o));
        //System.out.println("v2o " + ownerMap.get(o) + "->" + o);
      } else System.out.println("\nWARNING reID data inconsistency\n");
    }
    variable2Owner = v2o;
    //System.out.println("Reowned v2o " + v2o.toString());
    //System.out.println("Reowned ENDS " +myString("edge"));
  }

  /*
    private void reOwnAfterMultipleGluings(){
      for(PetriNetTransition tr: getTransitions().values()){
        tr.setOwners(tr.getOwners().stream().
          map(o->allProducts.get(o)).collect(Collectors.toSet()));
      }
      for(PetriNetPlace pl: getPlaces().values()){
        pl.setOwners(pl.getOwners().stream().
          map(o->allProducts.get(o)).collect(Collectors.toSet()));
      }
    }
    */
  public void addOwner(String o) {
    for (String s : owners) {
      if (s.equals(DEFAULT_OWNER)) {
        owners.remove(DEFAULT_OWNER);
      }
    }
    owners.add(o);
  }

  public void rebuildAlphabet() {
    Multimap<String, PetriNetTransition> alpha = ArrayListMultimap.create();
    Set<String> a = this.getTransitions().values().stream().map(x -> x.getLabel()).collect(Collectors.toSet());

    for (String s : a) {
      for (PetriNetTransition tr : alphabet.get(s)) {
        alpha.put(s, tr);
      }
    }
    alphabet = alpha;
  }

  public void buildAlphabetFromTrans() {
    Multimap<String, PetriNetTransition> alpha = ArrayListMultimap.create();

    for (PetriNetTransition tr : this.getTransitions().values()) {
      alpha.put(tr.getLabel(), tr);
      //System.out.println("AfromT Adding "+tr.getLabel()+" -> "+tr.myString());
    }

    alphabet = alpha;
  }

  /**
   * termination if the union of owners of all last transitions is the net owners
   * and all pre transitions are last!
   * TODO:
   *
   * @return
   */
  public boolean terminates() {
    //System.out.println("\n Terminates "+myString());
    Set<String> ownTr = new HashSet<>();
    Set<PetriNetTransition> endTr = new HashSet<>();
    for (PetriNetPlace pl : places.values()) {
      if (pl.isSTOP() && pl.getReferences().size() == 0 && pl.getLeafRef().size() == 0) {
        endTr.addAll(pl.pre());
        ownTr.addAll(pl.pre().stream().map(x -> x.getOwners()).
          flatMap(Set::stream).collect(Collectors.toSet()));
      }
    }
    //System.out.println("owners "+owners+ " ownTr "+ownTr+" "+getOwners().equals(ownTr)+ "\n");
    return getOwners().equals(ownTr);
  }

  public List<Set<String>> getRootNames() {
    return roots;
  }

  public List<Set<PetriNetPlace>> getRootPlacess() {
    List<Set<PetriNetPlace>> out = new ArrayList<>();
    for (Set<String> markNames : roots) {
      out.add(markNames.stream().map(x -> places.get(x)).collect(Collectors.toSet()));
    }
    return out;
  }

  public int nextRootNo() {
    return roots.size();
  }



  public PetriNetPlace getPlace(String id) {
    return places.get(id);
  }

  public Set<PetriNetPlace> getAllRoots() {
    return roots.stream().flatMap(Set::stream).map(x -> places.get(x)).collect(Collectors.toSet());
  }

  public Set<PetriNetPlace> getAllEnds() {
    //System.out.println("getAllEnds "+ends);
    //Set<PetriNetPlace> ends = new TreeSet<>();
    //places.values().stream().filter(x->x.isSTOP()).collect(Collectors.toSet());
    //System.out.println("ends "+ends+" should all be in places "+ places.keySet());
    Set<PetriNetPlace> out = ends.stream().flatMap(Set::stream).map(x -> places.get(x)).collect(Collectors.toSet());
    //System.out.println(out.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
    return out;
  }

  public Set<PetriNetEdge> getFirstEdges() {
    return getAllRoots().stream().map(x -> x.getOutgoing()).flatMap(Collection::stream).collect(Collectors.toSet());
  }

  public Set<PetriNetEdge> getLastEdges() {
    Set<PetriNetEdge> out = new TreeSet<>();
    if (getAllEnds() != null)
      for (PetriNetPlace pn : getAllEnds()) {
        if (pn.getIncoming() != null)
          for (PetriNetEdge edge : pn.getIncoming()) {
            out.add(edge);
          }
      }
    return out;
  }

  //undo  tagEvents()   on Automaton
  public void deTagTransitions() {
    Multimap<String, PetriNetTransition> alph = ArrayListMultimap.create();
    for (PetriNetTransition tr : getTransitions().values()) {
      tr.setLabel(tr.getLabel().split("\\:")[0]);
      alph.put(tr.getLabel(), tr);
    }
    alphabet = alph;
  }

  public void buildAlpahbetFromTransiations() {
    Multimap<String, PetriNetTransition> alph = ArrayListMultimap.create();
    for (PetriNetTransition tr : getTransitions().values()) {
      alph.put(tr.getLabel(), tr);
    }
    alphabet = alph;
  }

  public boolean rootContains(PetriNetPlace pl) {
    boolean b = roots.stream().flatMap(Set::stream).
      filter(x -> x.equals(pl.getId())).
      collect(Collectors.toSet()).size() > 0;
    return b;
  }

  /**
   * TODO add alphabet  + clean up alphabet!
   * USE this when DEBUGGIN
   * Not sure what data consistancy is intended or assumed
   * This method should act both in assertions and as documentation
   * No 2 Transitions should have the same Id
   * No 2 (atomic) Transitions should have the same pre, post sets and label
   *
   * @return
   */
  public boolean validatePNet(String ping) throws CompilationException {
    boolean r = validatePNet();
    System.out.println(ping + " " + r);
    return r;
  }

  public void tidyUpRootAndEndOnPlaces() {
    //Tidy up root and End nos on Places

      int rint = 1;
      for (PetriNetPlace pl : places.values()) {
        pl.cleanStart();
        pl.cleanSTOP();
      }
      Set<String> netOwners = new TreeSet<>();
      for (Set<String> rts : getRoots()) {
        for (String key : rts) {
          //System.out.println("root " + key + " -> " + places.get(key).myString());
          PetriNetPlace pl = places.get(key);
          pl.setStart(true);
          pl.getStartNos().add(rint);
          netOwners.addAll(pl.getOwners());
        }
        rint++;
      }
      owners = netOwners; //reachability could have removed all Places of a given owner

      rint = 1;
      for (Set<String> ends : getEnds()) {
        for (String key : ends) {
          //System.out.println("end " + key + " -> " + places.get(key).myString());
          places.get(key).setTerminal(Constant.STOP);
          places.get(key).getEndNos().add(rint);
        }
        rint++;
      }

  }
  public boolean validatePNet() throws CompilationException {
    //System.out.println("Validate ==>>" + myString());
    boolean ok = true;
    try {
      Set<String> netOwners = owners; //getOwners();
//Validate root and End on Places
      Set<PetriNetPlace> rtPlaces = places.values().stream().filter(x -> x.isStart()).collect(Collectors.toSet());
      if (rtPlaces.size() != getAllRoots().size()) {
        System.out.println("XXRoots MisMatch");
        ok = false;
      }
      Set<PetriNetPlace> endPlaces = places.values().stream().filter(x -> x.isSTOP()).collect(Collectors.toSet());
      if (endPlaces.size() != getAllEnds().size()) {
        System.out.println("XXEnds MisMatch");
        ok = false;
      }
      for (PetriNetPlace r : getAllRoots()) {
        if (r == null) {
          System.out.println("XxRoot null ");
          ok = false;
        } else if (!r.isStart()) {
          System.out.println(r.myString());
          System.out.println("XXXRoot " + r.getId() + " not Start ");
          ok = false;
        }
      }
      for (PetriNetPlace r : getAllEnds()) {
        if ((r == null)) {
          System.out.println("XXEnd null ");
          ok = false;
        } else if (!r.isSTOP()) {
          System.out.println(r.myString());
          System.out.println("XXEnd " + r.getId() + " not STOP ");
          ok = false;
        }
      }

      Set<String> endAll = new TreeSet<>();
      for (Set<String> end : ends) {
        //System.out.println("ends " + ends);
        Set<String> endOwn = new TreeSet<>();
        for (String e : end) {
          Set<String> owns = places.get(e).getOwners();
          //Need multiset check!  Needs to be ignored with event Refinement
          for (String o : owns) {
            if (!owners.contains(o)) {
              System.out.println("XXEnd owner Multiset so far " + endOwn + " adding " + places.get(e).myString());
              ok = false;
            }
          }
          //System.out.println(places.get(e).getId() + " -> " + owns);
          endOwn.addAll(owns);
        }
        if (!owners.containsAll(endOwn)) {
          System.out.println("XXEnd " + end + " with owners " + endOwn + " BUT netOwn " + netOwners);
          ok = false;
        }
        endAll.addAll(endOwn);
      }
      if (endAll.size()>0 && !owners.equals(endAll)) {
        System.out.println("XX all End with owners " + endAll + " BUT netOwn " + netOwners);
        ok = false;
      }

      //Need multiset check!  Needs to be ignored with event Refinement
      //System.out.println("roots "+roots);

      Set<String> rootAll = new TreeSet<>();
      for (Set<String> rt : roots) {
        //System.out.println("ends " + ends);
        Set<String> rootOwn = new TreeSet<>();
        for (String r : rt) {
          Set<String> owns = places.get(r).getOwners();
          //Need multiset check!  Needs to be ignored with event Refinement
          for (String o : owns) {
            if (!owners.contains(o)) {
              System.out.println("XXRoot owner Multiset so far " + rootOwn + " adding " + places.get(r).myString());
              ok = false;
            }
          }
          //System.out.println(places.get(e).getId() + " -> " + owns);
          rootOwn.addAll(owns);
        }
        if (!owners.containsAll(rootOwn)) {
          System.out.println("XXRoot " + rt + " with owners " + rootOwn + " BUT netOwn " + netOwners);
          ok = false;
        }
        rootAll.addAll(rootOwn);
      }
      if (!owners.equals(rootAll)) {
        System.out.println("XX all Root with owners " + rootAll + " BUT netOwn " + netOwners);
        ok = false;
      }




      //Back to verifying
      for (PetriNetPlace p : this.getPlaces().values()) {
        if (p.isStart() && !rootContains(p)) {
          //System.out.println(p.getId()+ " StartNos "+ p.getStartNos());
          Set<Integer> failed = p.getStartNos().stream().
            filter(x -> (roots.size() < x) || !roots.get(x - 1).contains(p)).collect(Collectors.toSet());
          if (failed.size() > 0) {
            System.out.println("XX**Start " + p.getId() + " is not Root " + failed);
            ok = false;
          }
        }
      }
      for (int i = 0; i < roots.size(); i++) {
        Set<String> fail = roots.get(i);
        for (String pl : fail) {
          if (places.get(pl) == null ||
            !places.get(pl).isStart() ||
            !places.get(pl).getStartNos().contains(i + 1)) {
            System.out.println("XX**Not Start " + places.get(pl).myString() + " index " + (i + 1));
            ok = false;
          }
        }
      }


      for (String k : transitions.keySet()) {
        PetriNetTransition tr = transitions.get(k);
     //System.out.println("tr "+tr.myString());
        String id = tr.getId();
        Set<String> trOwn = tr.getOwners();
        Set<String> inOwn = new TreeSet<>();
        Set<String> outOwn = new TreeSet<>();
        if (!id.equals(k)) {
          System.out.println("XXtransition key " + k + " not trasition id " + id);
          ok = false;
        }
        boolean match = true;
        for (PetriNetEdge ed : tr.getIncoming()) {
          if (!ed.getTo().getId().equals(id)) {
            match = false;
          } else {
            PetriNetPlace pl = ((PetriNetPlace) ed.getFrom());
            //System.out.println("in Place " + pl.myString());
            inOwn.addAll(pl.getOwners());
            //System.out.println("PreOwn "+pl.getOwners()+" added to "+inOwn);
          }
        }
        ok = ok && match;
        if (!tr.getLabel().equals(Constant.DEADLOCK)) {
          for (PetriNetEdge ed : tr.getOutgoing()) {
            if (!ed.getFrom().getId().equals(id)) {
              System.out.println("XX Outgoing transition edge " + ed.myString() + " not matched " + id);
              match = false;

            } else {
              PetriNetPlace pl = ((PetriNetPlace) ed.getTo());
       //System.out.println("outPlace "+pl.myString());
              outOwn.addAll(pl.getOwners());
            }
          }
          ok = ok && match;
          if (!isEqual(trOwn, outOwn)) {
            System.out.println("XXMismatch Outgoing Owners " + outOwn + " " + trOwn+"    tr " + tr.myString());
            ok = false;
          }
        }
        ok = ok && match;
        if (!isEqual(trOwn, inOwn)) {
          System.out.println("XXMismatch Incoming Owners  " + inOwn + " tr " + tr.myString());
          ok = false;
        }
        if (!netOwners.containsAll(trOwn)) {
          System.out.println("XXnetOwners = " + netOwners + " BUT tr = " + tr.myString());
          ok = false;
        }

      }


      for (String s : places.keySet()) {
        if (!places.get(s).getId().equals(s)) {
          System.out.println("XX Invalid places " + s + " -> " + places.get(s));
          ok = false;
          break;
        }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage()+ " Exception");
      ok = false;
    }
    if (!ok) {//KEEP BOTH  as Exception passed on up to gain info but call stack needed
      Throwable t = new Throwable();  t.printStackTrace();
      System.out.println("SORT the Petrinet OUT \n" + this.myString() + "\nSORT OUT Pn ABOVE\n");
      throw new CompilationException(getClass(), " invalid petriNet " + this.getId());
    }
    //System.out.println(this.getId()+" valid= "+ok);
    return ok;
  }

  public void setPlacesfromEnd() {
    int rNo = 1;
    places.values().stream().forEach(pl -> pl.setEndNos(new HashSet<>()));
    for (Set<String> m : ends) {
      for (String name : m) {
        places.get(name).addEndNo(rNo);
      }
      rNo++;
    }
  }

  //Start data on PetriNetPlace -- Root data on Petrinet
  public void setRootFromNet() {
    int rNo = 1;
    places.values().stream().forEach(pl -> {
      pl.setStartNos(new HashSet<>());
      pl.setStart(false);
    });
    for (Set<String> m : roots) {
      for (String name : m) {
        places.get(name).addStartNo(rNo);
        places.get(name).setStart(true);
      }
      rNo++;
    }
  }

  //Start data on PetriNetPlace -- Root data on Petrinet
  public void setRootFromStart() {
    Multimap<Integer, String> rootBuilder = LinkedHashMultimap.create();
    Set<String> starts = getPlaces().values().stream().
      filter(x -> x.isStart()).map(x -> x.getId()).collect(Collectors.toSet());
    //System.out.println("setRootFromStart() "+ starts);
    for (String pl : starts) {
      for (Integer i : places.get(pl).getStartNos()) {
        rootBuilder.put(i, pl);
        //System.out.println("rBuilder "+i+"->"+pl);
      }
    }

    List<Set<String>> rout = new ArrayList<>();
    for (Integer i : rootBuilder.keySet().stream().collect(Collectors.toSet())) {
      rout.add((Set<String>) rootBuilder.get(i));
      //System.out.println("roots "+ i +" "+rootBuilder.get(i));
    }
    //System.out.println("END setRootFromStart "+ rout);
    roots = rout;
    /*System.out.println("ROOT set to "+root.stream()
      .map(r->r.getId()+" ").collect(Collectors.joining()) ); */
  }

  //Set end data on PetriNetPlace -- from Petrinet
  public void setEndFromNet() {
    //System.out.println("setEndFromNet()\n"+this.myString());
    int rNo = 1;
    places.values().stream().forEach(pl -> {
      pl.setEndNos(new HashSet<>());
      pl.setTerminal("");
    });
    for (Set<String> m : ends) {
      //System.out.println(" m "+m);
      for (String name : m) {
        //System.out.println("  name "+name);
        places.get(name).addEndNo(rNo);
        places.get(name).setTerminal(Constant.STOP);
      }
      rNo++;
    }
  }

  private boolean isEqual(Set<String> s1, Set<String> s2) {
    return s1.size() == s2.size() &&
      s1.containsAll(s2);
  }

  //Start data on PetriNetPlace -- Root data on Petrinet
  public void setEndFromPlace() {
    Multimap<Integer, String> endBuilder = LinkedHashMultimap.create();
    Set<String> endids = getPlaces().values().stream().
      filter(x -> x.isSTOP()).map(x -> x.getId()).collect(Collectors.toSet());

    //System.out.println("setEndFromPlace() "+ endids);
    for (String pl : endids) {
      //System.out.println("pl "+places.get(pl).myString());
      for (Integer i : places.get(pl).getEndNos()) {
        endBuilder.put(i, pl);
        //System.out.println("endBuilder "+i+"->"+pl);
      }
    }

    List<Set<String>> eout = new ArrayList<>();
    for (Integer i : endBuilder.keySet().stream().sorted().collect(Collectors.toSet())) {
      eout.add((Set<String>) endBuilder.asMap().get(i));
      //System.out.println("ends "+ i +" "+eout);
    }


    ends = eout;
    //System.out.println("setEndFromPlace() END "+ ends);
  }

  public boolean tranExists(Collection<PetriNetPlace> pre,
                            Collection<PetriNetPlace> post,
                            String label) {

    for (PetriNetTransition tr : this.getTransitions().values()) {
      //System.out.println("Exists "+tr.myString());
      if (this.prePlaces(tr).equals(pre) &&
        this.postPlaces(tr).equals(post) &&
        tr.getLabel().equals(label)) {
        //System.out.println("Exists "+tr.myString());
        return true;
      }
    }
    return false;
  }

  public Set<PetriNetTransition> preTransitions(PetriNetPlace pl) {
    Set<PetriNetTransition> pre = new HashSet<>();
    for (PetriNetEdge edge : pl.getIncoming()) {
      pre.add((PetriNetTransition) edge.getFrom());
    }
    return pre;
  }

  public Set<PetriNetPlace> prePlaces(PetriNetTransition tr) {
    Set<PetriNetPlace> pre = new HashSet<>();
    for (PetriNetEdge edge : tr.getIncoming()) {
      pre.add((PetriNetPlace) edge.getFrom());
    }
    return pre;
  }

  public Set<PetriNetPlace> postPlaces(PetriNetTransition tr) {
    Set<PetriNetPlace> post = new HashSet<>();
    for (PetriNetEdge edge : tr.getOutgoing()) {
      post.add((PetriNetPlace) edge.getTo());
    }
    return post;
  }

  /**
   * Simply for easy of visualisation when testing
   *
   * @return
   */
  public String myString() {
    return myString("");
  }

  public String myString(String edge) {
    StringBuilder sb = new StringBuilder();
    sb.append(" " + this.getId() + " alpha " + alphabet.size() + " {" +
      alphabet.keySet() + "} rt");
    if (roots != null) {
      sb.append(this.getRoots());
    } else sb.append("null");

    sb.append(" End " + this.ends);
    sb.append(" owners " + owners);
    sb.append(" v2o " + variable2Owner.keySet().stream().map(x -> x + "->" + variable2Owner.get(x) + " ").collect(Collectors.joining()));
    sb.append("rootEval " + rootEvaluation);

    sb.append(this.getPlaces().values().stream().
      map(x -> x.getId())
      .reduce("\n" + getPlaces().size() + " places ", (x, y) -> x + ", " + y));
    sb.append(this.getPlaces().keySet().stream().
      map(x -> "\n" + x + "->" + getPlaces().get(x).myString())
      .reduce("", (x, y) -> x + " " + y));
    sb.append(this.getTransitions().keySet().stream().reduce("\n" +
      getTransitions().size() + " transitions ", (x, y) -> x + ", " + y));
    sb.append(this.getTransitions().values().stream().
      map(tr -> "\n" + tr.myString()).
      reduce("", (x, y) -> x + " " + y));
    if (edge.equals("edge")) {
      sb.append("\n" + edges.size() + " edges");
      sb.append(this.getEdges().values().stream().map(ed -> "\n" + ed.myString()).
        reduce("", (x, y) -> x + " " + y));
    } else {
      sb.append("\n myString(\"edge\")  for edges");
    }
    return sb.toString();
  }

  public void setRootPlace(PetriNetPlace r) {
    List<Set<String>> rts = new ArrayList<Set<String>>();
    rts.add(Collections.singleton(r.getId()));
    setRoots(rts);
  }

  public void addFirstRoot(PetriNetPlace place) {
    if (places.values().contains(place)) {
      roots.add(Collections.singleton(place.getId()));
      //roots.get(0).add(place);
      place.setStart(true);
    }
  }

  public void addRoot(Set<String> r) {
    roots.add(r);
  }
  public void addRootsPl(List<Set<PetriNetPlace>> rts) {
    for(Set<PetriNetPlace> rt:rts ){
      roots.add(rt.stream().map(x->x.getId()).collect(Collectors.toSet()));
    }

  }
  public void addRoots(List<Set<String>> rts) {
    for(Set<String> rt:rts ){
      roots.add(rt);
    }
  }
  public void addEnds(List<Set<String>> eds) {
    for(Set<String> e:eds ){
      ends.add(e);
    }
  }
  public PetriNetPlace addPlace() {
    // return addPlace(this.id +":p:" + placeId++);
    PetriNetPlace pl = this.addPlace("p:" + placeId++);

    return pl;
  }

  public PetriNetPlace addPlaceWithTag(String tag) {
    String id = tag + ":p:" + placeId++;
    PetriNetPlace place = new PetriNetPlace(id);
    places.put(id, place);
    //System.out.println(" to "+this.getId()+" adding "+place.myString());
    return place;
  }

  public PetriNetPlace addPlace(String id) {
    PetriNetPlace place = new PetriNetPlace(id);
    places.put(id, place);
    //System.out.println(" to "+this.getId()+" adding "+place.myString());
    return place;
  }

  public PetriNetPlace addGluePlace() {
    PetriNetPlace p = addPlace(this.id + ":G:" + placeId++);
    p.setStart(false);
    p.setTerminal("");
    return p;
  }

  public PetriNetTransition addTransitionWithTag(String tag, String label) {
    return addTransition(tag + "t:" + transitionId++, label);

  }

  public PetriNetTransition addTransition(String id, String label) {
    PetriNetTransition transition = new PetriNetTransition(id, label);
    transitions.put(id, transition);
    alphabet.put(label, transition);
    System.out.println("added "+transition.myString());
    //System.out.println(" alpha "+alphabet.keySet());
    return transition;
  }

  public PetriNetTransition addTransition(String label) {

    //  return addTransition(id + ":t:" + transitionId++, label);
    return addTransition("t:" + transitionId++, label);
  }

  public PetriNetTransition addTransition(Set<PetriNetPlace> pre, String label, Set<PetriNetPlace> post)
    throws CompilationException {

    //  return addTransition(id + ":t:" + transitionId++, label);
    PetriNetTransition tr = addTransition("t:" + transitionId++, label);
    //System.out.println("New trans "+this.myString());
    Set<String> own = new HashSet<>();
    for (PetriNetPlace pl : pre) {
      this.addEdge(tr, pl, false);
      own.addAll(pl.getOwners());
    }
    for (PetriNetPlace pl : post) {
      this.addEdge(pl, tr, false);
    }
    tr.setOwners(own);
    //System.out.println("Added trans "+tr.myString());
    return tr;
  }

  /*
  Adds edge with new Id
   */
  public PetriNetEdge addEdge(PetriNetTransition to, PetriNetPlace from, boolean op)
    throws CompilationException {
    return addEdgeWithTag("", to, from, op);
  }

  public PetriNetEdge addEdgeWithTag(String tag, PetriNetTransition to, PetriNetPlace from, boolean op)
    throws CompilationException {
    //System.out.println("adding Edge to "+to.toString()+ "  from "+from.toString());
    //System.out.println("1addingEdge from "+from.getId()+ " to "+to.getId());
    //System.out.println("in net "+myString());
    if (!transitions.containsKey(to.getId())) {
      throw new CompilationException(getClass(), "1Cannot add an edge to transition " +
        to.getId() + " in  petrinet");
    }
    if (!places.containsKey(from.getId())) {
      throw new CompilationException(getClass(), "1Cannot add an edge from Place " +
        from.getId() + " not inside the petrinet");
    }
    if (to == null || from == null) {
      throw new CompilationException(getClass(), "1Either " + to + " or " + from + "are null");
    }

    // String id = this.id + ":e:" + edgeId++;
    String id;
    if (tag.equals(""))
      id = "e:" + edgeId++;
    else
      id = tag + ":e:" + edgeId++;

    PetriNetEdge edge = new PetriNetEdge(id, to, from);
    to.getIncoming().add(edge);
    from.getOutgoing().add(edge);
    edges.put(id, edge);
    //System.out.println("addEdgePT done "+edge.myString());
    edge.setOptional(op);
    return edge;
  }

  public PetriNetEdge addEdge(PetriNetPlace to, PetriNetTransition from, boolean op) throws CompilationException {

    return addEdgeWithTag("", to, from, op);
  }

  public PetriNetEdge addEdgeWithTag(String tag, PetriNetPlace to, PetriNetTransition from, boolean op) throws CompilationException {

    //System.out.println("2addingEdge from "+from.getId()+ " to "+to.getId());
    //System.out.println("trans "+transitions.keySet().stream().map(x->x+"->"+transitions.get(x).getId()).collect(Collectors.joining()));


    if (!transitions.containsKey(from.getId())) {
      Throwable t = new Throwable();
      t.printStackTrace();
      //System.out.println("from.getId()"+from.getId());
      //System.out.println(this.myString());
      throw new CompilationException(getClass(), "2Cannot add an edge " +
        from.getId() + "->" + to.getId() + " in  petrinet " + myString());
    }
    if (!places.containsKey(to.getId())) {
      Throwable t = new Throwable();
      t.printStackTrace();
      //System.out.println("to "+to.myString());
      //System.out.println(this.myString());
      throw new CompilationException(getClass(), "2Cannot add an edge  " + from.getId() + "->" +
        to.getId() + " not inside the petrinet " + myString());
    }
    if (to == null || from == null) {
      throw new CompilationException(getClass(), "2Either " + to + " or " + from + "are null");
    }
    //System.out.println("XXX");
    String id;
    if (tag.equals(""))
      id = "e:" + edgeId++;
    else
      id = tag + ":e:" + edgeId++;//String id = this.id + ":e:" + edgeId++;
    PetriNetEdge edge = new PetriNetEdge(id, to, from);
    to.getIncoming().add(edge);
    from.getOutgoing().add(edge);
    edges.put(id, edge);
    edge.setOptional(op);
    //System.out.println("addEdgeTP done "+edge.myString());
    return edge;
  }


  public void removePlace(PetriNetPlace place) throws CompilationException {
    removePlace(place, true);
  }

  public void removePlace(PetriNetPlace place, boolean simple) throws CompilationException {
    //System.out.println("Removing "+place.getId());
    if (!places.containsKey(place.getId())) {
      System.out.println("Cannot remove a place " + place.getId() + " that is not part of the petrinet");
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
      System.out.println("remove  "+ edge.myString());
      removeEdge(edge);
    }

    if (place.isStart() || getAllRoots().contains(place)) {

      List<Set<String>> Nroots = new ArrayList<>();
      for (Set<String> rs : roots) {
        Set<String> Nrts = new HashSet<>();
        for (String r : rs) {
          if (!place.getId().equals(r)) {
            Nrts.add(r);
          }
        }
        if (Nrts.size()>0) Nroots.add(Nrts);
      }
      setRoots(Nroots);
    }
    //System.out.println("Remove "+place.getId()+ " simp "+simple);
    if (simple) {
      if (place.isSTOP() || getAllEnds().contains(place)) {
        List<Set<String>> Nends = new ArrayList<>();
        //System.out.println("Ends "+ends);
        for (Set<String> rs : ends) {
          Set<String> Nrts = new HashSet<>();
          for (String r : rs) {
            if (!place.getId().equals(r)) {
              Nrts.add(r);
            }
          }
          if (Nrts.size()>0) Nends.add(Nrts);
        }
        setEnds(Nends);
        //System.out.println("Ends "+ends);
      }
    } else { // unreachABILTY  needs this
      if (place.isSTOP() || getAllEnds().contains(place)) {
        //System.out.println("NOT Ends " + ends);
        List<Set<String>> Nends = new ArrayList<>();
        for (Set<String> rs : ends) {
          if (!rs.contains(place.getId()))  //One place not reached the End is not an End
            Nends.add(rs);
        }
        setEnds(Nends);
        //System.out.println("NOT Ends " + ends);
      }
    }

    places.remove(place.getId());
    //System.out.println("REMOVED "+place.getId()+ " so CHECK "+myString());
  }

  public void removeTransition(PetriNetTransition transition) throws CompilationException {
    if (!transitions.values().contains(transition)) {
      //System.out.println(transition.myString());
      throw new CompilationException(getClass(), "Cannot remove a transition that is not part of"
        + "the petrinet");
    }
    Set<PetriNetEdge> toRemove = new HashSet<>(transition.getIncoming());
    toRemove.addAll(transition.getOutgoing());

    for (PetriNetEdge edge : toRemove) {
      if(edges.values().contains(edge)) removeEdge(edge);
    }
    transitions.remove(transition.getId());
    // Opps alphabet.remove(transition.getLabel(), transition);
  }
  public void removeStarE() throws CompilationException {
    System.out.println(" E* ");
    List<Set<String>> eds = new ArrayList<>();
    List<PetriNetTransition> trStar = transitions.values().stream().filter(x->x.getLabel().equals("*E")).collect(Collectors.toList());
   for(PetriNetTransition tr: trStar ){
     System.out.println(tr.myString());
     Set<String> ed = new TreeSet<>();
     for(PetriNetPlace pl: tr.pre()){
       ed.add(pl.getId());
       System.out.println("pl "+pl.getId()+" -> "+pl.getOwners());
     }
     if (ed.size()>0) eds.add(ed);
     removeTransition(tr);
   }
    System.out.println("eds "+eds);
   setEnds(eds);
   setEndFromNet();
  }
  public void removeEdge(PetriNetEdge edge) throws CompilationException {
    if (!edges.values().contains(edge)) {
      Throwable t = new Throwable(); t.printStackTrace();
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

  public void joinNet(Petrinet addMe) {
    //NOTE IDS only unique in one Net  hense all must be changed
    this.places.putAll(addMe.places);  //copies into the maping
    this.transitions.putAll(addMe.transitions);
    this.edges.putAll(addMe.edges);
    this.getRoots().addAll(addMe.getRoots());
    this.getEnds().addAll(addMe.getEnds());
  }

  public Set<String> reName(Set<String> in) throws CompilationException {
    Set<String> out = new TreeSet<>();
    for(String i:in){
      if (nameMap.containsKey(i)) out.add(nameMap.get(i));
      else {
        Throwable t = new Throwable(); t.printStackTrace();
        throw new CompilationException(getClass(), "reName Failure");
      }
    }
    return out;
  }
  public List<Set<String>> reName(List<Set<String>> in) throws CompilationException {
    System.out.println("nameMap = "+nameMap);
    System.out.println("     in = "+in);
    List<Set<String>> out = new ArrayList<>();
    for(Set<String> s:in) {
      out.add(reName(s));
    }
   return out;
  }
  /**
   * To keep all id unique in a Petri Net  the combination of two nets requires the
   * the ids are changed
   * Changing the Root and End only makes sense somnetines
   *
   * @param petriToAdd
   * @param setEnd
   * @return the mapping from the old to new Transitioins +
   *   SETS nameMap  from old to new Places  Needed to adjust root & end afterwards
   */
  @SneakyThrows(value = {CompilationException.class})
  public Map<PetriNetTransition, PetriNetTransition> addPetrinet(Petrinet petriToAdd, boolean withRoot, boolean setEnd) {

    //System.out.println("\nAdd Petri this = "+ this.myString()+ "\ntoAdd = "+ petriToAdd.myString());


    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    nameMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    //System.out.println("Owners "+owners +" to remove "+ DEFAULT_OWNER);
    if (owners.contains(DEFAULT_OWNER)) {
      owners = new HashSet<>();
    }
    //owners.remove(DEFAULT_OWNER);  FAILS ?
    owners.addAll(petriToAdd.getOwners());  //assumes disjoint owners
    for (PetriNetPlace place : petriToAdd.getPlaces().values()) {
      PetriNetPlace newPlace = addPlace();  //new id
      newPlace.setOwners(place.getOwners());
      newPlace.copyProperties(place);
      if (!withRoot) newPlace.setStart(false);
      placeMap.put(place, newPlace);
      nameMap.put(place.getId(), newPlace.getId());
    }
    //System.out.println("this " +getRoots() + " addPetri rt "+ petriToAdd.getRoots());
    if (withRoot) {
      for (Set<String> roots : petriToAdd.getRootNames()) {
        Set<String> next = new HashSet<>();
        for (String rpl : roots) {
          //rts.add(placeMap.get(rpl));
          next.add(nameMap.get(rpl));
          //System.out.println("addin root "+placeMap.get(rpl).getId());
        }
        addRoot(next);
      }
    }

    for (PetriNetTransition transition : petriToAdd.getTransitions().values()) {
      PetriNetTransition newTransition = addTransition(transition.getLabel());
      newTransition.setOwners(transition.getOwners());
      transitionMap.put(transition, newTransition);
    }

    for (PetriNetEdge edge : petriToAdd.getEdges().values()) {
      if (edge.getFrom() instanceof PetriNetPlace) {
        PetriNetEdge ed =
          addEdge(transitionMap.get(edge.getTo()), placeMap.get(edge.getFrom()), edge.getOptional());
        if (edge.getGuard() != null) ed.setGuard(edge.getGuard());
        //System.out.println("addEdge "+ed.myString());
      } else {
        PetriNetEdge ed =
          addEdge(placeMap.get(edge.getTo()), transitionMap.get(edge.getFrom()), edge.getOptional());
        if (edge.getGuard() != null) ed.setGuard(edge.getGuard());
        //System.out.println("addEdge "+ed.myString());
      }
    }

    //if (setEnd) setEndFromNet();
    List<Set<String>> e = reName(petriToAdd.copyEnds());  //concurrent Modification
    if (setEnd) {addEnds(e);}
    List<Set<String>> r =reName(petriToAdd.copyRoots());
    if (withRoot) { addRoots(r);}
    //System.out.println("addPetri");
    //  if (withRoot) this.validatePNet();  //cannot do this when root not finished NOR for "+"
    System.out.println("addPetrinet END "+ this.myString()+"\n");
    return transitionMap;
  }



  /*
    Beware
   */
  @SneakyThrows(value = {CompilationException.class})
  public Map<PetriNetTransition, PetriNetTransition>
        addPetrinetNoOwner(Petrinet petriToAdd, String tag) { // DO NOT USE need place2 place mapping
    //System.out.println("Start of add petriToAdd "+petriToAdd.myString());
    //System.out.println("Start of add this "+this.myString());
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<String, String> nameMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    //System.out.println(owners.getClass());
    //owners.addAll(petriToAdd.getOwners());
    for (String o : petriToAdd.getOwners()) {
      addOwner(o);
    }
    for (PetriNetPlace place : petriToAdd.getPlaces().values()) {

      PetriNetPlace newPlace = addPlace(place.getId() + tag);
      //PetriNetPlace newPlace = addPlace("P"+placeId++);
      newPlace.copyProperties(place);
      newPlace.setOwners(place.getOwners());
      placeMap.put(place, newPlace);
      nameMap.put(place.getId(), newPlace.getId());
      //System.out.println("placeMap "+place.getId()+"->"+newPlace.getId());
    }


    for (PetriNetTransition transition : petriToAdd.getTransitions().values()) {
      PetriNetTransition newTransition = this.addTransition(transition.getLabel());
      newTransition.setOwners(transition.getOwners());
      transitionMap.put(transition, newTransition);
      //System.out.println(newTransition.myString());
    }
   /*System.out.println("tranMap "+ transitionMap.keySet().stream().
      map(x->x.getId()+"->"+transitionMap.get(x).getId()).collect(Collectors.joining()));
    System.out.println(myString()); */

    for (PetriNetEdge edge : petriToAdd.getEdges().values()) {
      //owners.addAll(postFixed);
      //System.out.println("edge "+edge.myString());
      if (edge.getFrom() instanceof PetriNetPlace) {
        //System.out.println("edging "+transitionMap.get(edge.getTo())+" "+places.get(edge.getFrom().getId()+tag));
        PetriNetEdge e =
          addEdge(transitionMap.get(edge.getTo()), places.get(edge.getFrom().getId() + tag), edge.getOptional());
        if (edge.getGuard() != null) e.setGuard(edge.getGuard());
      } else {
        //System.out.println("edgeing "+ places.get(edge.getTo().getId()+tag) +
        //     "  "+transitionMap.get(edge.getFrom()));
        PetriNetEdge e =
          addEdge(places.get(edge.getTo().getId() + tag), transitionMap.get(edge.getFrom()), edge.getOptional());
        if (edge.getGuard() != null) e.setGuard(edge.getGuard());
      }
    }
    //System.out.println("add Petri  nameMap "+nameMap);
    //this.getRoots().addAll(petriToAdd.getRoots().stream().map(x -> markUpGrade(x, nameMap)).collect(Collectors.toList()));
    //this.getEnds().addAll(petriToAdd.getEnds().stream().map(x -> markUpGrade(x, nameMap)).collect(Collectors.toList()));
    this.setEndFromPlace();
    this.setRootFromStart();
    //System.out.println("addPetri END "+ this.myString());
    //Net not always valid!
    return transitionMap;
  }

  public Set<String> markUpGrade(Set<String> mark, Map<String, String> nameMap) {
    return mark.stream().map(x -> nameMap.get(x)).collect(Collectors.toSet());
  }


  /**
   * For sequential composition A=>B where B has more than one Root
   * we need to copy A_E the end marking of A to be glued to the Roots. This includes
   * copying pre(A_E) the pre transitions of A_E
   */
  public Set<String> copyEnd(Set<String> endPlaces, int i) throws CompilationException {
    // set up place to new palce mapping
    //System.out.println("copy End "+endPlaces);
    Map<PetriNetPlace, PetriNetPlace> plMapping =
      endPlaces.stream().map(x -> places.get(x)).
        collect(Collectors.toMap(pl -> pl, pl -> addPlace(pl.getId() + "*" + i)));
    //System.out.println("plMapping  "+ plMapping.keySet().stream().
    //   map(x->x.getId()+"->"+plMapping.get(x).getId()).collect(Collectors.joining()));
    Set<PetriNetTransition> lastTrans =
      endPlaces.stream().map(x -> preTransitions(places.get(x))).
        flatMap(Set::stream).distinct().collect(Collectors.toSet());
    //System.out.println("lastTran = "+lastTrans.stream().map(x->x.getId()).collect(Collectors.joining()));
    // now build the new transitions
    for (PetriNetTransition tr : lastTrans) {
//System.out.println("TR "+tr.myString());
      PetriNetTransition newtr = addTransition(tr.getId() + "*" + i, tr.getLabel());
      for (PetriNetEdge ed : tr.getOutgoing()) {
        PetriNetEdge e =
          addEdge(plMapping.get(ed.getTo()), newtr, ed.getOptional());
        if (ed.getGuard() != null) e.setGuard(ed.getGuard());
      }
      for (PetriNetEdge ed : tr.getIncoming()) {
        //System.out.println("ed + "+ ed.myString());
        PetriNetEdge e =
          addEdge(newtr, ((PetriNetPlace) ed.getFrom()), ed.getOptional());
        if (ed.getGuard() != null) e.setGuard(ed.getGuard());
      }
//System.out.println("     "+tr.getId()+"=>"+newtr.getId());
    }


    return plMapping.values().stream().map(x -> x.getId()).collect(Collectors.toSet());
  }

  /**
   * The consturuction of owners when two sets of places are glued together
   * is constructed independently of the places.
   * Owners on the Net Plaes,and Transitions are updated
   * subsequently Glue places takes the intersection of the component owners
   *
   * @param owns1
   * @param owns2
   */
  public void glueOwners(Set<String> owns1, Set<String> owns2) {
    //System.out.println("\n** glueOwners Start o1 " + owns1 + " o2 " + owns2);
    //System.out.println("** glueOwners Start " + myString("edge"));

    combinationsTable = ArrayListMultimap.create();
    String setAsString;
    for (String o1 : owns1) {
      for (String o2 : owns2) {
        if (o1.compareTo(o2) > 0)
          setAsString = o1 + MAPLET + o2;
        else
          setAsString = o2 + MAPLET + o1;
        boolean found = false;
        for (String el : combinationsTable.get(o1)) {
          if (el.equals(setAsString)) found = true;
        }
        if (!found) combinationsTable.put(o1, setAsString);

        found = false;
        for (String el : combinationsTable.get(o2)) {
          if (el.equals(setAsString)) found = true;
        }
        if (!found) combinationsTable.put(o2, setAsString);
      }
    }
   /*System.out.println("GlueOwners Table "+ " \n"+ combinationsTable.keySet().stream().
     map(x->x+"->"+combinationsTable.get(x)+", \n").
     collect(Collectors.joining())+ " "); */
    //System.out.println("combinationsTable " + combinationsTable.toString());

    Set<String> temp = new TreeSet<>();
    if (owners.size() > 0) temp.addAll(owners);  // event Refinement
    else temp.addAll(owns1);                   // all others
    //temp.addAll(owns2);
    //System.out.println("owners = " + temp);
    Set<String> newOwners = new HashSet<>();
    for (String o : temp) {
      if (combinationsTable.keySet().contains(o)) {
        newOwners.addAll(combinationsTable.get(o));
      } else {
        newOwners.add(o);
      }
    }
    owners = newOwners;

    //System.out.println("newOwner = " + owners);
    //System.out.println("combinationsTable " + combinationsTable.toString());

    for (PetriNetTransition tr : getTransitions().values()) {

      Set<String> U = new HashSet<>(combinationsTable.keySet());
      U.retainAll(tr.getOwners());
      if (!U.isEmpty()) {
        Set<String> V = new HashSet<>(tr.getOwners());
        V.removeAll(U);
        tr.setOwners(tr.getOwners().stream().map(x -> (combinationsTable.get(x))).
          flatMap(Collection::stream).collect(Collectors.toSet()));
        tr.addOwners(V);
      }
      //System.out.println("  tr "+tr.myString());
    }
    for (PetriNetPlace pl : getPlaces().values()) {

      Set<String> U = new HashSet<>(combinationsTable.keySet());
      U.retainAll(pl.getOwners());
      if (!U.isEmpty()) {
        Set<String> V = new HashSet<>(pl.getOwners());
        V.removeAll(U);
        pl.setOwners(pl.getOwners().stream().map(x -> (combinationsTable.get(x))).
          flatMap(Collection::stream).collect(Collectors.toSet()));
        pl.addOwners(V);
      }
      //System.out.println("  pl "+pl.myString());
    }
    //System.out.println("**glueOwners END " + myString("edge") + "\n");
  }

  public void repairRootEnd(Multimap<String, String> s2s) {
    System.out.println("repairRootEnd "+s2s);
    List<Set<String>> newEnds = new ArrayList<>();
    System.out.println("ends "+ends);
     for(Set<String> end :ends ){
       Set<String> newEnd = new TreeSet<>();
       for (String e :end){
         if (s2s.containsKey(e)) {
           newEnd.addAll(s2s.get(e));
         } else {
           newEnd.add(e);
         }
       }
        if (!newEnds.contains(newEnd)) newEnds.add(newEnd);
     }
    System.out.println("newEnd "+newEnds);
     setEnds(newEnds);
     setEndFromNet();
    System.out.println("roots "+roots);
    List<Set<String>> newRoots = new ArrayList<>();
    for(Set<String> root :roots ){
      Set<String> newRoot = new TreeSet<>();
      for (String r :root){
        if (s2s.containsKey(r)) {
          newRoot.addAll(s2s.get(r));
        } else {
          newRoot.add(r);
        }
      }
      if (!newRoots.contains(newRoot)) newRoots.add(newRoot);
    }
    System.out.println("newRoots "+newRoots);
    setRoots(newRoots);
    setRootFromNet();

  }
  /**
   * Because of the need clone the objects change only the Ids are fixed.
   * Hence use of ids over objects
   * returns a name to newName mapping for the places built by gluing
   *
   * @param m1
   * @param m2
   * @throws CompilationException
   */
  public Multimap<String, String> glueNames(Set<String> m1, Set<String> m2)
    throws CompilationException {

    return this.glueNames(m1, m2, false);
  }

  public Multimap<String, String> glueNames(Set<String> m1, Set<String> m2, boolean keep)
    throws CompilationException {
    //System.out.println("Glue Names "+m1+ " TO "+m2);
    Set<PetriNetPlace> net1 = m1.stream().map(x -> places.get(x)).collect(Collectors.toSet());
    Set<PetriNetPlace> net2 = m2.stream().map(x -> places.get(x)).collect(Collectors.toSet());

    return this.gluePlaces(net1, net2, keep);
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

  /**
   * Gluing must be sequential. For second glueing sets must be computed AFTER first glueing
   * Let A = a->A.
   * B = x->B.
   * C = B||d->x->e->STOP.
   * Consider    P = A[]C   and P = C/{A/x}
   * inout Place IDs so that nets can be joined prior to glueing!
   * <p>
   * Owners for the new Places is the intersection of the owners of the pair of old Places
   * <p>
   * Symbolic Petri Nets -  put processing in the glueing function
   * Variables are attached to an edge and exist at a location - just like Places
   * <p>
   * (p1,p2) TIMES (p3,p4,p5)
   * var x is on e1 at l1 and e4 at l4
   * new Net has x on p{1,4} at location l(1,4)
   * Boolean Gards only meaningful on Place->Transitiom edge
   * MUST be true on Transition->Place
   * Assignment only meaningful on Transition->Place
   * MUST be .size()==0 on Place->Transition
   * Parallel sync BoolGuard is on edge and where Loc(g1)= Loc(pl1)
   * Guard on Transition = g1/\g2
   * <p>
   * Boolean Guards  on Place->Transition edges
   * g1 on edge from p1 to appear on new edge from (p1,p2)
   * Assignmnets, ax (only on edges entering E1x an n1 End Place)
   * T1-ax->E1x   to appear on new Place (p1,p2) where Loc(ax) \sub Loc(p1) /\  Loc(g1) \sub Loc(p2)
   *
   * @param set1
   * @param set2
   * @return mapping from old places to set of net places- Used in tree2net in PetrinetInterpreter
   * @throws CompilationException
   */
  public Multimap<String, String> gluePlaces (Set<PetriNetPlace> set1, Set<PetriNetPlace> set2)
    throws CompilationException {

    return gluePlaces(set1, set2, false);
  }

  /*  Nothing to do with owners  OR Root /END
      because of the dificulty maintaining correct root/end Numbers
     keep==true keep the Places for further gluings (A+B)[]C ....
     Choice [] sets root and end outside of Gluing (|| and + donot glue)
     Sequential =>  also set outside of Gluing
     Refinement root and end removed from  one Net prior to Gluing
   */
  public Multimap<String, String> gluePlaces
  (Set<PetriNetPlace> set1, Set<PetriNetPlace> set2, boolean keep)
    throws CompilationException {
    System.out.println("set1 "+set1.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
    System.out.println("set2 "+set2.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
    System.out.println("\n   Glueing starting \n"+myString());

    for (PetriNetPlace pl : set1) {
      if (!places.containsValue(pl)) {
        new RuntimeException().printStackTrace();
        throw new CompilationException(getClass(), "set1 node " + pl.getId() + " not part"
          + " of the petrinet\n");
      }
    }
    for (PetriNetPlace pl : set2) {
      if (!places.containsValue(pl)) {
        new RuntimeException().printStackTrace();
        throw new CompilationException(getClass(), "set2 node " + pl.getId() + " not part"
          + " of the petrinet\n");
      }
    }


    // we lack the root count to compute the new root numbers
    Multimap<String, String> prodNames = ArrayListMultimap.create();
    //if not first glueing add to existing products
    Multimap<PetriNetPlace, PetriNetPlace> products = ArrayListMultimap.create();
    for (PetriNetPlace place1 : set1) {
      for (PetriNetPlace place2 : set2) {

        PetriNetPlace newPlace = this.addGluePlace();
        System.out.println("From "+place1.getId()+" and "+place2.getId()+" -> "+newPlace.getId());
        Set<String> inter = new HashSet<>(place1.getOwners());
        inter.retainAll(place2.getOwners());
        if (inter.size()==0) System.out.println("INTERXXX\n place1 "+place1.myString()+"\n place2 "+place2.myString());
        newPlace.setOwners(inter);  // Owners is intersection only works if glueOwners run prior

        //System.out.println("*** Glue "+place1.getId()+"   with "+place2.getId()+" = "+newPlace.getId());
        products.put(place1, newPlace);
        products.put(place2, newPlace);
        if (keep == true) {
          allProducts.put(place1, newPlace);
          allProducts.put(place2, newPlace);
        }
        prodNames.put(place1.getId(), newPlace.getId());
        prodNames.put(place2.getId(), newPlace.getId());
        //System.out.println("poducts "+place1.getId()+", "+place2.getId()+"-->"+newPlace.getId());
        //newPlace.intersectionOf(place1, place2);

      /* CAN only be done after gluing
        if (place1.isStart()) {
          newPlace.setStartNos(place1.getStartNos());
          newPlace.setStart(true);
        } else if (place2.isStart()) {
          newPlace.setStartNos(place2.getStartNos());
          newPlace.setStart(true);
        } */
        newPlace.addRefefances(place1.getReferences());
        newPlace.addRefefances(place2.getReferences());
        newPlace.addFromRefefances(place1.getLeafRef());
        newPlace.addFromRefefances(place2.getLeafRef());

        System.out.println(" gluePlaces  newPlace "+newPlace.myString()); //good
      }
    }


    Set<PetriNetPlace> con = new TreeSet<>();
    con.addAll(set1);
    con.addAll(set2);

    for (PetriNetPlace place : con) { //Iterables.concat(set1, set2 )) {
      for (PetriNetPlace newProductPlace : products.get(place)) { // the new palces built from the old
        //System.out.println("place  "+place.getId()+"   prod " +newProductPlace.getId());
        for (PetriNetEdge edge : place.getIncoming()) {  //assignmnet
          /* Need Incoming/Outgoing to be a set */
          //System.out.println("X " + edge.myString());
          PetriNetTransition from = (PetriNetTransition) edge.getFrom();
          if (!newProductPlace.hasIncoming(from)) {
            PetriNetEdge e = addEdge(newProductPlace, from, edge.getOptional());
            if (edge.getGuard() != null)
              e.setGuard(edge.getGuard());
            //newProductPlace.getIncoming().add(e);  already done in addEdge
            //System.out.println("X added "+e.myString());
          }
        }
        for (PetriNetEdge edge : place.getOutgoing()) {  //boolean guard
          //System.out.println("Y " + edge.myString());
          PetriNetTransition to = (PetriNetTransition) edge.getTo();
          if (!newProductPlace.hasOutgoing(to)) {
            PetriNetEdge e = addEdge(to, newProductPlace, edge.getOptional());
            if (edge.getGuard() != null)
              e.setGuard(edge.getGuard());
            // newProductPlace.getOutgoing().add(e);
            //System.out.println("Y added "+e.myString());
          }
        }
      }
    }
    // Must repair Root an End prior to removing the old places
    repairRootEnd(prodNames);
    System.out.println( "  Glueing half way "+ this.myString());
    if (keep == false) {
      for (PetriNetPlace place : Iterables.concat(set1, set2)) {
        //System.out.println("  removeing "+place.getId());
        if (getPlaces().values().contains(place)) {
          removePlace(place);
        }
        if (getPlaces().keySet().contains(place.getId())) {
          //System.out.println("Opps "+ place.getId());
          removePlace(places.get(place.getId()));
        }
        //System.out.println("Check places" +Petrinet.marking2String(places.values()));
      }
    }

    System.out.println( "************\n************\n  GLUEING finished "+ this.myString());
    return prodNames;
  }

  public void setUpv2o(Petrinet n1, Petrinet n2) {
    //System.out.println("setUpv2o");
    //System.out.println(n1.getId() + " " + n1.getVariable2Owner().toString());
    //System.out.println(n2.getId() + " " + n2.getVariable2Owner().toString());
    setVariable2Owner(n1.v2o(n2));

  }

  /*
    used to set the variable2Owners Map
   */
  private Map<String, String> v2o(Petrinet pin) {
    //System.out.println("v2o " + variable2Owner.toString() + "  " + pin.variable2Owner.toString());
    Map<String, String> out = new TreeMap<>();
    for (String var1 : variable2Owner.keySet()) {
      for (String var2 : pin.getVariable2Owner().keySet()) {
        if (var1.equals(var2)) {
          String setAsString;
          if (var1.compareTo(var2) > 0)
            setAsString = variable2Owner.get(var1) + MAPLET + pin.getVariable2Owner().get(var2);
          else
            setAsString = pin.getVariable2Owner().get(var2) + MAPLET + variable2Owner.get(var1);
          out.put(var1, setAsString);
        }
      }
    }
    //System.out.println("v2o out " + out.toString());
    return out;
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