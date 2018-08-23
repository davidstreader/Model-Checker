package mc.processmodels.petrinet;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.function.Predicate;
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
  public Map<String,String> nameMap = new HashMap<>();

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
  public List<Set<String>> copyRoots() {
    List<Set<String>> out = new ArrayList<>();
    for (Set<String> e:roots) {
      out.add(e);
    }
    return out;
  }
  @Getter
  @Setter
  private List<Set<String>> ends = new ArrayList<>();
  public List<Set<String>> copyEnds() {
    List<Set<String>> out = new ArrayList<>();
    for (Set<String> e:ends) {
      out.add(e);
    }
    return out;
  }
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
  private Location hiddenVariablesLocation;

  @Getter
  @Setter
  private Set<String> owners = new HashSet<>();

  @Getter
  @Setter
  private Set<String> variables = new HashSet<>();

  @Getter
  @Setter
  private Location variablesLocation;

  @Getter
  @Setter
  private Location location;
  @Getter
  @Setter
  private String id;
  @Getter
  @Setter
  private int placeId = 0;
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

  /*  To help debugging the ids are only unique within a PetriNet
      reId CLONES the Petri Net and recomputes ad ids prefixing the id with the tag
      It is essential when composing two Nets to use different tags
   */
    public Petrinet reId(String tag) throws CompilationException {
      //System.out.println("\nreId "+ tag + ": "+this.myString()+"\n");
    Petrinet petri =  new Petrinet(getId(),false);

      //System.out.println("places  "+places.keySet());
    Set<String> owns = new HashSet<>();
    Map<String,String> placeIdMap = new HashMap<>();
    Map<String,String> transIdMap = new HashMap<>();
    for(PetriNetPlace pl: places.values()){
       PetriNetPlace newpl = petri.addPlaceWithTag(tag);
      //System.out.println(newpl.myString());
       newpl.copyProperties(pl);
       placeIdMap.put(pl.getId(),newpl.getId());
       for(String plo: pl.getOwners()){
         if (!owns.contains(plo)) owns.add(plo);
       }
      //System.out.println("added "+newpl.myString());
     }
      //System.out.println("new places  "+petri.getPlaces().keySet());

      //System.out.println("Transitions "+transitions.keySet());
     for(PetriNetTransition tr: transitions.values()){
       //System.out.println(tr.myString());
      PetriNetTransition newtr = petri.addTransitionWithTag(tag,tr.getLabel());
      transIdMap.put(tr.getId(),newtr.getId());
      newtr.addOwners(tr.getOwners());
       for(String tro: tr.getOwners()){
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
     for(PetriNetEdge ed: getEdges().values()){
      if (ed.getFrom() instanceof PetriNetPlace ) {
        PetriNetTransition to = petri.getTransitions().get(transIdMap.get(ed.getTo().getId()));
        PetriNetPlace from = petri.getPlace(placeIdMap.get(ed.getFrom().getId()));
        PetriNetEdge newed = petri.addEdgeWithTag(tag,to, from,ed.getOptional());
      } else {
        PetriNetPlace to = petri.getPlace(placeIdMap.get(ed.getTo().getId()));
        PetriNetTransition from = petri.getTransitions().get(transIdMap.get(ed.getFrom().getId()));
        PetriNetEdge newed = petri.addEdgeWithTag(tag,to, from,ed.getOptional());
      }
     }
     petri.reown(tag);
     petri.setRootFromStart();
     petri.setEndFromPlace();
    //System.out.println("\nReid end "+petri.myString()+"\n");
     return petri;
  }
/*
   (A+B)[]C  external choice [] must copy root and all transiation attached
   subsequently a copy will be glued to each alternative root
   (A[]B)=>(C+D)  copy ends of A[]B and Roots of C+D
 */
  public PetriNetPlace copyRootOrEnd(PetriNetPlace pl, String t) throws CompilationException {

    //System.out.println("to copy "+pl.myString());
    PetriNetPlace newpl = this.addPlaceWithTag("cpy"+t);
    newpl.copyProperties(pl);

    Set<PetriNetTransition> newTrans = preTransitions(pl);

    newTrans.addAll(postTransitions(pl));
    int i = 0;
    for (PetriNetTransition tr: newTrans) {
      i++;
      PetriNetTransition newtr = this.addTransitionWithTag(tr.getLabel(),tr.getLabel());
      newtr.addOwners(tr.getOwners());
      Set<PetriNetEdge> eds = tr.copyIncoming();
      //System.out.println(" tr "+tr.myString());
      eds.addAll(tr.copyOutgoing());
      //System.out.println(" tr "+tr.myString());
      for (PetriNetEdge ed: eds) {
        //System.out.println(" ed "+ed.myString());
        if (ed.getFrom().getId().equals(pl.getId())) {
          this.addEdge(newtr, newpl, ed.getOptional());
        } else if (ed.getTo().getId().equals(pl.getId())) {
          this.addEdge(newpl, newtr,  ed.getOptional());
        } else if (ed.getTo() instanceof  PetriNetPlace) {
          this.addEdge((PetriNetPlace) ed.getTo() , newtr,  ed.getOptional());
        } else {
          this.addEdge(newtr, (PetriNetPlace) ed.getFrom(),  ed.getOptional());
        }
        //System.out.println(" tr "+tr.myString());
      }
      //System.out.println(" *newtr "+newtr.myString());

    }
    //System.out.println("**newpl "+newpl.myString());
    return newpl;
  }
  /*
     Ownership unique within a net so need to tag when nets are to be combined
   */
  public void reown() {
   reown("");
  }
    public void reown(String tag){
    //System.out.println("Reown Start "+myString());
    ownersId = 0;
    Map<String,String> ownerMap = new HashMap<>();
    for(String o:owners){
      ownerMap.put(o,tag+"o"+ownersId++);
    }
    owners = owners.stream().map(o->ownerMap.get(o)).collect(Collectors.toSet());

    for(PetriNetTransition tr: getTransitions().values()){
      tr.setOwners(tr.getOwners().stream().
              map(o->ownerMap.get(o)).collect(Collectors.toSet()));
    }
    for(PetriNetPlace pl: getPlaces().values()){
      pl.setOwners(pl.getOwners().stream().
              map(o->ownerMap.get(o)).collect(Collectors.toSet()));
    }
    //System.out.println("Reowned "+myString());
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
    for(String s: owners) {
      if (s.equals(DEFAULT_OWNER)) {owners.remove(DEFAULT_OWNER);}
    }
    owners.add(o);
  }





public void rebuildAlphabet(){
  Multimap<String, PetriNetTransition> alpha = ArrayListMultimap.create();
  Set<String> a = this.getTransitions().values().stream().map(x->x.getLabel()).collect(Collectors.toSet());
  for(String s : a){
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
    //System.out.println("\n Terminates "+ myString());
    Set<String> ownTr = new HashSet<>();
    Set<PetriNetTransition> endTr = new HashSet<>();
    for (PetriNetPlace pl: places.values()){
      if (pl.isTerminal()&& pl.getReferences().size()==0 &&pl.getFromReferences().size()==0){
        endTr.addAll(pl.pre());
        ownTr.addAll(pl.pre().stream().map(x->x.getOwners()).
                flatMap(Set::stream).collect(Collectors.toSet()));
      }
    }
    //System.out.println("owners "+owners+ " ownTr "+ownTr+" "+getOwners().equals(ownTr)+ "\n");
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
      pl.setStartNos(new HashSet<>());
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
         System.out.println(r.myString());
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
          System.out.println("**Start "+p.getId()+" is not Root "+ failed);
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
          System.out.println("**Not Start " + places.get(pl).myString() + " index " + (i+1));
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

  public void setEndfromTr(){
    for(PetriNetPlace pl: getPlaces().values()){

    }
  }
  //Start data on PetriNetPlace -- Root data on Petrinet
  public void setStartFromRoot() {
    int rNo = 1;
    places.values().stream().forEach(pl -> pl.setStartNos(new HashSet<>()));
    for (Set<String> m : roots) {
      for(String name : m){
        places.get(name).addStartNo(rNo);
      }
      rNo++;
    }
  }
  //Start data on PetriNetPlace -- Root data on Petrinet
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
  //Set end data on PetriNetPlace -- from Petrinet
  public void setEndFromNet() {
    //System.out.println("setEndFromNet()\n"+this.myString());
    int rNo = 1;
    places.values().stream().forEach(pl -> pl.setEndNos(new HashSet<>()));
    for (Set<String> m : ends) {
      //System.out.println(" m "+m);
      for(String name : m){
        //System.out.println("  name "+name);
        places.get(name).addEndNo(rNo);
      }
      rNo++;
    }
  }
  //Start data on PetriNetPlace -- Root data on Petrinet
  public void setEndFromPlace(){
    Multimap<Integer, String>  endBuilder = LinkedHashMultimap.create();
    Set<String> endids = getPlaces().values().stream().
      filter(x->x.isSTOP()).map(x->x.getId()).collect(Collectors.toSet());
    //System.out.println("setEndFromPlace() "+ endids);
    for (String pl : endids){
      for (Integer i : places.get(pl).getEndNos()) {
        endBuilder.put(i,pl);
        //System.out.println("endBuilder "+i+"->"+pl);
      }
    }

    List<Set<String>> eout = new ArrayList<>();
    for (Integer i :endBuilder.keySet().stream().sorted().collect(Collectors.toSet()) ){
      eout.add((Set<String>) endBuilder.get(i));
      //System.out.println("ends "+ i +" "+eout);
    }


    ends = eout;
    //System.out.println("setEndFromPlace() END "+ ends);
  }
  public boolean tranExists(Collection<PetriNetPlace> pre,
                            Collection<PetriNetPlace> post,
                            String label) {
 /*   System.out.print("pre { ");
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
        //System.out.println("Exists "+tr.myString());
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
  public Set<PetriNetTransition> postTransitions(PetriNetPlace pl){
    Set<PetriNetTransition> post = new HashSet<>();
    for(PetriNetEdge edge: pl.getOutgoing()){
      post.add((PetriNetTransition) edge.getTo());
    }
    return post;
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
      post.add((PetriNetPlace) edge.getTo());
    }
    return post;
  }


  /**
   * Simply for easy of visualisation when testing
   * @return
   */
  public String myString() {
   return myString("");
  }
    public String myString(String edin){
      StringBuilder sb = new StringBuilder();
    sb.append(" " + this.getId()+ " alpha " +alphabet.size()+ " {"+
     alphabet.keySet() + "} rt");
    if (roots!= null) {
      sb.append( this.getRoots());
    } else sb.append("null");

    sb.append(" End "+this.ends);
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
if (edin.equals("edge")) {
  sb.append(this.getEdges().values().stream().map(ed -> "\n" + ed.myString()).
    reduce("", (x, y) -> x + " " + y));
} else {
  sb.append("\n myString(\"edge\")  for edges");
}
    return sb.toString();
  }

  public void setRootPlace(PetriNetPlace r){
    List<Set<String>> rts = new ArrayList<Set<String>>();
    rts.add(Collections.singleton(r.getId()));
    setRoots(rts);
  }
  public static Petrinet oneEventNet(String event)throws CompilationException{
    Petrinet eventNet = new Petrinet(event,false);
    PetriNetPlace start = eventNet.addPlace();
    start.setOwners(new HashSet<>()); start.addOwner(DEFAULT_OWNER);
    start.setStart(true);
    start.setStartNos(new HashSet<>()); start.addStartNo(1);
    eventNet.setRootPlace(start);
    PetriNetPlace end = eventNet.addPlace();
    end.setOwners(new HashSet<>()); end.addOwner(DEFAULT_OWNER);
    PetriNetTransition tr = eventNet.addTransition(event);
    tr.setOwners(new HashSet<>()); tr.addOwner(DEFAULT_OWNER);
    eventNet.addEdge(end,tr,false);
    eventNet.addEdge(tr, start,false);
    eventNet.setOwners(new HashSet<>());
    eventNet.addOwner(DEFAULT_OWNER);
    end.setTerminal("STOP"); end.addEndNo(1);
    eventNet.setEndFromPlace();
    eventNet.reId("");
    //Throwable t = new Throwable();

//System.out.println("oneEventNet "+eventNet.myString()+"\n");
    //t.printStackTrace();
    return eventNet;
  }
  // called from interpretor
  public static Petrinet stopNet() throws CompilationException{
    return  Petrinet.stopNet("");
  }

  // called from interpretor when local reference needs to be used
  public static Petrinet stopNet(String ref)throws CompilationException {
      Petrinet stop = new Petrinet("stop");
      stop.setOwners(new HashSet<>());
      stop.addOwner(DEFAULT_OWNER);

//?? when are the places added??
    PetriNetPlace p  = stop.getPlaces().values().iterator().next();
      if (ref != "") p.addFromRefefances( new HashSet(Collections.singleton(ref)));
      p.setTerminal("STOP"); p.addEndNo(1);
      p.setStart(true);
      p.setOwners(new HashSet<>()); p.addOwner(DEFAULT_OWNER);
      p.setStartNos(new HashSet<>()); p.addStartNo(1);
      p.addEndNo(1);
      stop.setRootPlace(p);
      stop.setEndFromPlace();
    stop.reId("");
    //System.out.println("\nstopNet "+stop.myString());
    return stop;
  }
  public static Petrinet errorNet()throws CompilationException{
    Petrinet error = new Petrinet("error");
    error.setOwners(new HashSet<>()); error.addOwner(DEFAULT_OWNER);
    PetriNetPlace p  = error.getPlaces().values().iterator().next();
      p.setStart(true);
    p.setOwners(new HashSet<>()); p.addOwner(DEFAULT_OWNER);
    p.setStartNos(new HashSet<>()); p.addStartNo(1);
    error.setRootPlace(p);
    PetriNetPlace end = error.addPlace();
    end.setOwners(new HashSet<>()); end.addOwner(DEFAULT_OWNER);
    PetriNetTransition tr = error.addTransition(Constant.DEADLOCK);
    tr.setOwners(new HashSet<>()); tr.addOwner(DEFAULT_OWNER);
    error.addEdge(end,tr,false);
    error.addEdge(tr, p,false);
    error.setOwners(new HashSet<>());
    error.addOwner(DEFAULT_OWNER);
    end.setTerminal("ERROR");
    error.reId("");

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
    this.setOwners(new HashSet<>()); this.addOwner(DEFAULT_OWNER);
    //this.root  = roots.get(0);
    if (constructRoot) {
      this.setOwners(new HashSet<>()); this.addOwner(DEFAULT_OWNER);
      PetriNetPlace origin = addPlace();
      origin.setStart(true);
      origin.setStartNos(new HashSet<>()); origin.addStartNo(1);
      origin.setOwners(new HashSet<>()); origin.addOwner(DEFAULT_OWNER);
      roots.add(Collections.singleton(origin.getId()));

    } else {
      this.setOwners(new HashSet<>());
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
    // return addPlace(this.id +":p:" + placeId++);
    PetriNetPlace pl = this.addPlace("p:" + placeId++);

    return pl;
  }

  public PetriNetPlace addPlaceWithTag(String tag) {
    String id = tag+":p:"+placeId++ ;
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
    PetriNetPlace p = addPlace(this.id+":G:" + placeId++);
    p.setStart(false);
    p.setTerminal("");
    return p;
  }

  public PetriNetTransition addTransitionWithTag(String tag, String label) {
    return  addTransition(tag+"t:" + transitionId++, label);

  }
  public PetriNetTransition addTransition(String id, String label) {
    PetriNetTransition transition = new PetriNetTransition(id, label);
    transitions.put(id, transition);
    alphabet.put(label, transition);
  //System.out.println("added "+transition.getId());
    //System.out.println(" to "+this.getId()+" adding "+transition.myString());
    return transition;
  }

  public PetriNetTransition addTransition(String label) {

  //  return addTransition(id + ":t:" + transitionId++, label);
    return addTransition( "t:" + transitionId++, label);
  }
  public PetriNetTransition addTransition(Set<PetriNetPlace> pre,String label,Set<PetriNetPlace> post)
    throws CompilationException {

    //  return addTransition(id + ":t:" + transitionId++, label);
    PetriNetTransition tr =  addTransition( "t:" + transitionId++, label);
    Set<String> own = new HashSet<>();
    for(PetriNetPlace pl: pre){
      this.addEdge(tr,pl, false);
      own.addAll(pl.getOwners());
    }
    for(PetriNetPlace pl: post){
      this.addEdge(pl,tr, false);
    }
    tr.setOwners(own);
    return tr;
  }
/*
Adds edge with new Id
 */
  public PetriNetEdge addEdge(PetriNetTransition to, PetriNetPlace from, boolean op)
          throws CompilationException {
    return addEdgeWithTag("",to,from,op);
  }
    public PetriNetEdge addEdgeWithTag(String tag,PetriNetTransition to, PetriNetPlace from, boolean op)
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

   // String id = this.id + ":e:" + edgeId++;
      String id;
      if (tag.equals(""))
        id =  "e:" + edgeId++;
      else
        id =  tag+":e:" + edgeId++;

    PetriNetEdge edge = new PetriNetEdge(id, to, from);
    to.getIncoming().add(edge);
    from.getOutgoing().add(edge);
    edges.put(id, edge);
    //System.out.println("addEdgePT done "+edge.myString());
    edge.setOptional(op);
    return edge;
  }

  public PetriNetEdge addEdge(PetriNetPlace to, PetriNetTransition from, boolean op) throws CompilationException {

    return addEdgeWithTag("",to,from,op);
  }
    public PetriNetEdge addEdgeWithTag(String tag,PetriNetPlace to, PetriNetTransition from, boolean op) throws CompilationException {

    //System.out.println("addingEdge from "+from.getId()+ " to "+to.getId());
   //System.out.println("trans "+transitions.keySet().stream().map(x->x+"->"+transitions.get(x).getId()).collect(Collectors.joining()));


    if (!transitions.containsKey(from.getId()) ) {
      Throwable t = new Throwable();
      t.printStackTrace();
      //System.out.println("from.getId()"+from.getId());
      //System.out.println(this.myString());
      throw new CompilationException(getClass(), "Cannot add an edge "+
                          from.getId()+"->"+to.getId()+" in  petrinet "+myString());
    }
    if (!places.containsValue(to)) {
      Throwable t = new Throwable();
      t.printStackTrace();
      //System.out.println("to "+to.myString());
      //System.out.println(this.myString());
      throw new CompilationException(getClass(), "Cannot add an edge  "+ from.getId()+"->"+
                         to.getId()+" not inside the petrinet "+myString());
    }
    if (to == null || from == null) {
      throw new CompilationException(getClass(), "Either " + to + " or " + from + "are null");
    }
    //System.out.println("XXX");
      String id;
      if (tag.equals(""))
        id =  "e:" + edgeId++;
      else
        id =  tag+":e:" + edgeId++;//String id = this.id + ":e:" + edgeId++;
    PetriNetEdge edge = new PetriNetEdge(id, to, from);
    to.getIncoming().add(edge);
    from.getOutgoing().add(edge);
    edges.put(id, edge);
    edge.setOptional(op);
 //System.out.println("addEdgeTP done "+edge.myString());
    return edge;
  }


  public void removePlace(PetriNetPlace place) throws CompilationException {
    if (!places.containsValue(place)) {
      System.out.println("Cannot remove a place "+place.getId()+" that is not part of the petrinet");
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
      //System.out.println(transition.myString());
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

  public void joinNet(Petrinet addMe) {
    this.places.putAll(addMe.places);
    this.transitions.putAll(addMe.transitions);
    this.edges.putAll(addMe.edges);
    this.getRoots().addAll(addMe.getRoots());
    this.getEnds().addAll(addMe.getEnds());
  }
  /**
   *  Places with NEW ids
   * Adds the root = this only makes sense sometimes
   * @param petriToAdd
   * @return  the mapping from the old to new Places
   */
  @SneakyThrows(value = {CompilationException.class})
  public Map<PetriNetTransition, PetriNetTransition> addPetrinet(Petrinet petriToAdd, boolean withRoot) {
    ends.addAll(petriToAdd.copyEnds());
    roots.addAll(petriToAdd.copyRoots());
    //System.out.println("\nAdd Petri this = "+ this.myString()+ "\ntoAdd = "+ petriToAdd.myString());


    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    nameMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    //System.out.println("Owners "+owners +" to remove "+ DEFAULT_OWNER);
    if (owners.contains(DEFAULT_OWNER)) {owners = new HashSet<>();}
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
          addEdge(transitionMap.get(edge.getTo()), placeMap.get(edge.getFrom()),edge.getOptional());
       //System.out.println("addEdge "+ed.myString());
      } else {
        PetriNetEdge ed =
          addEdge(placeMap.get(edge.getTo()), transitionMap.get(edge.getFrom()),edge.getOptional());
       //System.out.println("addEdge "+ed.myString());
      }
    }

    setEndFromNet();
    //setStartFromRoot();
    //System.out.println("addPetri");
   //  if (withRoot) this.validatePNet();  //cannot do this when root not finished NOR for "+"
   //System.out.println("addPetrinet END "+ this.myString()+"\n");
    return transitionMap;
  }




//TODO is this needed any more
  @SneakyThrows(value = {CompilationException.class})
  public Map<PetriNetTransition, PetriNetTransition> addPetrinetNoOwner(Petrinet petriToAdd, String tag) {
   //System.out.println("Start of add petriToAdd "+petriToAdd.myString());
   //System.out.println("Start of add this "+this.myString());
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<String,String> nameMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();
    //System.out.println(owners.getClass());
    owners.addAll(petriToAdd.getOwners());
    for(String o: petriToAdd.getOwners()) { addOwner(o);}
    for (PetriNetPlace place : petriToAdd.getPlaces().values()) {

      PetriNetPlace newPlace = addPlace(place.getId()+tag);
      //PetriNetPlace newPlace = addPlace("P"+placeId++);
      newPlace.copyProperties(place);
      newPlace.setOwners(place.getOwners());
      placeMap.put(place, newPlace);
      nameMap.put(place.getId(),newPlace.getId());
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
        addEdge(transitionMap.get(edge.getTo()), places.get(edge.getFrom().getId()+tag),edge.getOptional());
      } else {
  //System.out.println("edgeing "+ places.get(edge.getTo().getId()+tag) +
   //     "  "+transitionMap.get(edge.getFrom()));
        addEdge(places.get(edge.getTo().getId()+tag), transitionMap.get(edge.getFrom()),edge.getOptional());
      }
    }
    //System.out.println("add Petri  nameMap "+nameMap);
    this.getRoots().addAll(petriToAdd.getRoots().stream().map(x->markUpGrade(x,nameMap)).collect(Collectors.toList()));
    this.getEnds().addAll(petriToAdd.getEnds().stream().map(x->markUpGrade(x,nameMap)).collect(Collectors.toList()));
 //this.setRoots(petriToAdd.getRoots().stream().map(x->markUpGrade(x,nameMap)).collect(Collectors.toList()));
   //System.out.println("addPetri END "+ this.myString());
    //Net not always valid!
    return transitionMap;
  }

  public Set<String> markUpGrade(Set<String> mark, Map<String,String> nameMap){
    return mark.stream().map(x->nameMap.get(x)).collect(Collectors.toSet());
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
//System.out.println("TR "+tr.myString());
       PetriNetTransition newtr =  addTransition(tr.getId()+"*"+i ,tr.getLabel());
       for(PetriNetEdge ed : tr.getOutgoing()){

         addEdge(plMapping.get(ed.getTo()),newtr,ed.getOptional());
       }
       for(PetriNetEdge ed : tr.getIncoming()){
        //System.out.println("ed + "+ ed.myString());
         addEdge(newtr ,((PetriNetPlace) ed.getFrom()),ed.getOptional());
       }
//System.out.println("     "+tr.getId()+"=>"+newtr.getId());
     }


   return  plMapping.values().stream().map(x->x.getId()).collect(Collectors.toSet());
 }

  /**
   * The consturuction of owners when two sets of places are glued together
   * is constructed independently of the places.
   *  Owners on the Net Plaes,and Transitions are updated
   *  subsequently Glue places takes the intersection of the component owners
   * @param owns1
   * @param owns2
   */
 public void glueOwners(Set<String> owns1, Set<String> owns2) {
   //System.out.println("\n** glueOwners Start o1 "+owns1+" o2 "+owns2);
   combinationsTable = ArrayListMultimap.create();
   String setAsString;
   for (String o1 : owns1) {
     for (String o2 : owns2) {
       if (o1.compareTo(o2) >0 )
         setAsString = o1 + MAPLET + o2;
       else
         setAsString = o2 + MAPLET + o1;
       boolean found = false;
       for (String el: combinationsTable.get(o1)){
         if (el.equals(setAsString)) found = true;
       }
       if (!found) combinationsTable.put(o1, setAsString);

       found = false;
       for (String el: combinationsTable.get(o2)){
         if (el.equals(setAsString)) found = true;
       }
       if (!found) combinationsTable.put(o2, setAsString);
     }
   }
   /*System.out.println("GlueOwners Table "+ " \n"+ combinationsTable.keySet().stream().
     map(x->x+"->"+combinationsTable.get(x)+", \n").
     collect(Collectors.joining())+ " "); */

   //System.out.println("owners = "+owners);
   Set<String> newOwners = new HashSet<>();
   for(String o:owners) {
     if(combinationsTable.keySet().contains(o)){
       newOwners.addAll(combinationsTable.get(o));
     } else {
       newOwners.add(o);
     }
   }
   owners = newOwners;
   //System.out.println("newOwner = "+owners);

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
   //System.out.println("**glueOwners END "+myString()+"\n");
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

    return this.glueNames(m1,m2,false);
  }
  public Map<String,String>  glueNames (Set<String> m1, Set<String> m2,boolean keep)
    throws CompilationException {
    //System.out.println("Glue Names "+m1+ " TO "+m2);
    Set<PetriNetPlace> net1 = m1.stream().map(x-> places.get(x)).collect(Collectors.toSet());
    Set<PetriNetPlace> net2 = m2.stream().map(x-> places.get(x)).collect(Collectors.toSet());

    return this.gluePlaces(net1,net2,keep);
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
   *
   * Owners for the new Places is the intersection of the owners of the pair of old Places
   * @param set1
   * @param set2
   * @return  mapping from old places to set of net places- Used in tree2net in PetrinetInterpreter
   * @throws CompilationException
   */
  public Map<String,String> gluePlaces
  (Set<PetriNetPlace> set1, Set<PetriNetPlace> set2)
    throws CompilationException {

   return gluePlaces(set1,set2,false);
  }
  /*
     keep==true keep the Places for further gluings (A+B)[]C ....
     Choice [] sets root and end outside of Gluing (|| and + donot glue)
     Sequential =>  also set outside of Gluing
     Refinement root and end removed from  one Net prior to Gluing
   */
  public Map<String,String> gluePlaces
      (Set<PetriNetPlace> set1, Set<PetriNetPlace> set2, boolean keep)
      throws CompilationException {
   //System.out.println("\n\n "+keep+" Glueing starting \n"+myString());

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
    //<String, String> combinationsTable = ArrayListMultimap.create();

    // we lack the root count to compute the new root numbers
    Map<String,String> prodNames = new HashMap<>();
    //if not first glueing add to existing products
    Multimap<PetriNetPlace, PetriNetPlace> products = ArrayListMultimap.create();
    for (PetriNetPlace place1 : set1) {
      for (PetriNetPlace place2 : set2) {

        PetriNetPlace newPlace = this.addGluePlace();
        Set<String> inter = new HashSet<>(place1.getOwners());
        inter.retainAll(place2.getOwners());
        newPlace.setOwners(inter);  // Owners is intersection only works if glueOwners run prior

 //System.out.println("*** Glue "+place1.getId()+"   with "+place2.getId()+" = "+newPlace.getId());
        products.put(place1, newPlace);
        products.put(place2, newPlace);
        if (keep==true){
          allProducts.put(place1, newPlace);
          allProducts.put(place2, newPlace);
        }
        prodNames.put(place1.getId()+MAPLET+place2.getId(), newPlace.getId());
        prodNames.put(place2.getId()+MAPLET+place1.getId(), newPlace.getId());
      //System.out.println("poducts "+place1.getId()+", "+place2.getId()+"-->"+newPlace.getId());
        //newPlace.intersectionOf(place1, place2);

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
          newPlace.setTerminal("STOP"); // endNos is case dependent
        }
        if (place1.isSTOP()) {
          newPlace.setEndNos(place1.getEndNos());
          newPlace.setTerminal("STOP");
        } else if (place2.isSTOP()) {
          newPlace.setEndNos(place2.getEndNos());
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
    StringBuilder sb = new StringBuilder();
    sb.append(" products ");
    for(PetriNetPlace pl:  products.keySet()) {
      sb.append(pl.getId()+" "+products.get(pl).size()+" ");
      for(PetriNetPlace pi: products.get(pl)){
        sb.append(pi.getId()+",");
      }
    }
   //System.out.println(sb.toString());

    for (PetriNetPlace place : Iterables.concat(set1, set2)) {
      for (PetriNetPlace product : products.get(place)) {
      //System.out.println("place  "+place.getId()+"   prod " +product.getId());
        for (PetriNetEdge edge : place.getIncoming()) {
          /* Need Incoming/Outgoing to be a set */
          //System.out.println("X " + edge.myString());
          PetriNetTransition from = (PetriNetTransition) edge.getFrom();
          if (!product.hasIncoming(from)) {
            PetriNetEdge e = addEdge(product, from,edge.getOptional());
            product.getIncoming().add(e);
            //System.out.println("X added "+e.myString());
          }
        }
        for (PetriNetEdge edge : place.getOutgoing()) {
          //System.out.println("Y " + edge.myString());
          PetriNetTransition to = (PetriNetTransition) edge.getTo();
          if (!product.hasOutgoing(to)){
            PetriNetEdge e = addEdge(to, product, edge.getOptional());
            product.getOutgoing().add(e);
            //System.out.println("Y added "+e.myString());
          }
        }
      }
    }
//Built MultiMap now replace Net owners




   // glueOwners(owns1,owns2);
   if (keep==false) {
     for (PetriNetPlace place : Iterables.concat(set1, set2)) {
       //System.out.println("  removeing "+place.getId());
       if (getPlaces().values().contains(place)) removePlace(place);
       if (getPlaces().keySet().contains(place.getId())) {
         //System.out.println("Opps "+ place.getId());
         removePlace(places.get(place.getId()));
       }
       //System.out.println("Check places" +Petrinet.marking2String(places.values()));
     }
   }
    //reown(); MUST not change owners Refinement needs it
    //products.values().stream().distinct()
    //  .filter(PetriNetPlace::isStart).forEach(this::addFirstRoot);
  //System.out.println("\n"+keep+"  Glueing finished "+ this.myString());
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